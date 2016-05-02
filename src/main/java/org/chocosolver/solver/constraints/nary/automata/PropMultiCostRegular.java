/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.automata;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.Bounds;
import org.chocosolver.solver.constraints.nary.automata.FA.utils.ICounter;
import org.chocosolver.solver.constraints.nary.automata.structure.Node;
import org.chocosolver.solver.constraints.nary.automata.structure.multicostregular.Arc;
import org.chocosolver.solver.constraints.nary.automata.structure.multicostregular.FastPathFinder;
import org.chocosolver.solver.constraints.nary.automata.structure.multicostregular.StoredDirectedMultiGraph;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableIntIterator;
import org.chocosolver.util.objects.StoredIndexedBipartiteSet;
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.chocosolver.util.tools.ArrayUtils;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: julien          S
 * Date: Jul 16, 2008
 * Time: 5:56:50 PM
 * <p/>
 * Multi-Cost-Regular is a propagator for the constraint ensuring that, given :
 * an automaton Pi;
 * a sequence of domain variables X;
 * a set of bound variables Z;
 * a assignment cost matrix for each bound variable C;
 * <p/>
 * The word formed by the sequence of assigned variables is accepted by Pi;
 * for each z^k in Z, sum_i(C_i(x_k)k) = z^k
 * <p/>
 * AC is NP hard for such a constraint.
 * The propagation is based on a Lagrangian Relaxation approach of the underlying
 * Resource constrained  shortest/longest path problems
 */
public final class PropMultiCostRegular extends Propagator<IntVar> {

    private static final boolean DEBUG = false;

    /**
     * Maximum number of iteration during a bound computation
     */
    public static final int MAXBOUNDITER = 10;

    /**
     * Maximum number of non improving iteration while computing a bound
     */
    public static final int MAXNONIMPROVEITER = 15;

    /**
     * Constant coefficient of the lagrangian relaxation
     */
    public static final double U0 = 10.0;

    /**
     * Lagrangian multiplier decreasing factor
     */
    public static final double RO = 0.7;


    /**
     * Map to retrieve rapidly the index of a given variable.
     */
    public final TObjectIntHashMap<IntVar> map;

    /**
     * Decision variables
     */
    private final IntVar[] vs;

    private final int offset;

    /**
     * Cost variables
     */
    private final IntVar[] z;

    /**
     * The finite automaton which defines the regular language the variable sequence must belong
     */
    private final ICostAutomaton pi;

    /**
     * Layered graph of the unfolded automaton
     */
    private StoredDirectedMultiGraph graph;

    /**
     * Boolean array which record whether a bound has been modified by the propagator
     */
    private final boolean[] modifiedBound;

    /**
     * Lagrangian multiplier container to compute an UB
     */
    private final double[] uUb;

    /**
     * Lagrangian multiplier container to compute a LB
     */
    private final double[] uLb;

    /**
     * Instance of the class containing all path finding algorithms
     * Also contains graph filtering algorithms
     */
    private FastPathFinder slp;

    /**
     * Store the number of resources = z.length
     */
    private final int nbR;


    /**
     * Stack to store removed edges index, for delayed update
     */
    private final TIntStack toRemove;

    private final TIntStack[] toUpdateLeft;
    private final TIntStack[] toUpdateRight;

    private int lastWorld = -1;
    private long lastNbOfBacktracks = -1;
    private long lastNbOfRestarts = -1;
    private TIntHashSet boundUpdate;
    private boolean computed;

    private final IIntDeltaMonitor[] idms;
    private final RemProc rem_proc;

    public final double _MCR_DECIMAL_PREC;

    private final IntIterableSet vrms;

    /**
     * Constructs a multi-cost-regular propagator
     *
     * @param variables     decision variables
     * @param costvariables cost variables
     * @param cauto         finite automaton with costs
     */
    public PropMultiCostRegular(IntVar[] variables, final IntVar[] costvariables, ICostAutomaton cauto) {
        super(ArrayUtils.append(variables, costvariables), PropagatorPriority.CUBIC, true);
        _MCR_DECIMAL_PREC = model.getSettings().getMCRDecimalPrecision();
        this.vs = Arrays.copyOfRange(vars, 0, variables.length);
        this.offset = vs.length;
        this.z = Arrays.copyOfRange(vars, offset, vars.length);
        this.nbR = this.z.length - 1;

        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
        }
        this.modifiedBound = new boolean[]{true, true};

        this.uUb = new double[2 * nbR];
        this.uLb = new double[2 * nbR];

        this.map = new TObjectIntHashMap<>();
        for (int i = 0; i < vars.length; i++) {
            this.map.put(vars[i], i);
        }
        this.toRemove = new TIntArrayStack();
        this.toUpdateLeft = new TIntArrayStack[nbR + 1];
        this.toUpdateRight = new TIntArrayStack[nbR + 1];

        for (int i = 0; i <= nbR; i++) {
            this.toUpdateLeft[i] = new TIntArrayStack();
            this.toUpdateRight[i] = new TIntArrayStack();
        }
        this.boundUpdate = new TIntHashSet();
        this.pi = cauto;
        rem_proc = new RemProc(this);
        vrms = new IntIterableBitSet();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return (vIdx < vs.length ?
                IntEventType.all() :
                IntEventType.boundAndInst());
    }

    /**
     * Build internal structure of the propagator, if necessary
     *
     * @throws org.chocosolver.solver.exception.ContradictionException if initialisation encounters a contradiction
     */
    protected void initialize() throws ContradictionException {
        checkBounds();
        initGraph();
        this.slp = graph.getPathFinder();
        for (int i = 0; i < offset; i++) {
            vrms.clear();
            vrms.setOffset(vs[i].getLB());
            for (int j = vs[i].getLB(); j <= vs[i].getUB(); j = vs[i].nextValue(j)) {
                StoredIndexedBipartiteSet sup = graph.getUBport(i, j);
                if (sup == null || sup.isEmpty()) {
                    vrms.add(j);
                }
            }
            vs[i].removeValues(vrms, this);//, false);
        }
        this.slp.computeShortestAndLongestPath(toRemove, z, this);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            // as the graph was build on initial domain, this is allowed (specific case)
            for (int i = 0; i < idms.length; i++) {
                idms[i].freeze();
                idms[i].forEachRemVal(rem_proc.set(i));
                idms[i].unfreeze();
            }
            initialize();
        }
        filter();
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx < offset) {
            checkWorld();
            idms[varIdx].freeze();
            idms[varIdx].forEachRemVal(rem_proc.set(varIdx));
            idms[varIdx].unfreeze();
        } else {// if (EventType.isInstantiate(mask) || EventType.isBound(mask)) {
            boundUpdate.add(varIdx - offset);
            computed = false;
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void initGraph() {
        int aid = 0;
        int nid = 0;


        int[] offsets = new int[offset];
        int[] sizes = new int[offset];
        int[] starts = new int[offset];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0; i < offset; i++) {
            offsets[i] = vs[i].getLB();
            sizes[i] = vs[i].getUB() - vs[i].getLB() + 1;
            if (i > 0) starts[i] = sizes[i - 1] + starts[i - 1];
            totalSizes += sizes[i];
        }


        DirectedMultigraph<Node, Arc> graph;

        int n = offset;
        graph = new DirectedMultigraph<>(new Arc.ArcFactory());
        ArrayList<HashSet<Arc>> tmp = new ArrayList<>(totalSizes);
        for (int i = 0; i < totalSizes; i++) {
            tmp.add(new HashSet<>());
        }

        int i, j, k;
        TIntIterator layerIter;
        TIntIterator qijIter;

        ArrayList<TIntHashSet> layer = new ArrayList<>();
        TIntHashSet[] tmpQ = new TIntHashSet[totalSizes];
        // DLList[vars.length+1];

        for (i = 0; i <= n; i++) {
            layer.add(new TIntHashSet());// = new DLList(nbNodes);
        }

        //forward pass, construct all paths described by the automaton for word of length nbVars.
        layer.get(0).add(pi.getInitialState());
        TIntHashSet nexts = new TIntHashSet();

        for (i = 0; i < n; i++) {
            int UB = vs[i].getUB();
            for (j = vs[i].getLB(); j <= UB; j = vs[i].nextValue(j)) {
                layerIter = layer.get(i).iterator();//getIterator();
                while (layerIter.hasNext()) {
                    k = layerIter.next();
                    nexts.clear();
                    pi.delta(k, j, nexts);
                    TIntIterator it = nexts.iterator();
                    for (; it.hasNext(); ) {
                        int succ = it.next();
                        layer.get(i + 1).add(succ);
                    }
                    if (!nexts.isEmpty()) {
                        int idx = starts[i] + j - offsets[i];
                        if (tmpQ[idx] == null)
                            tmpQ[idx] = new TIntHashSet();

                        tmpQ[idx].add(k);

                    }
                }
            }
        }

        //removing reachable non accepting states
        layerIter = layer.get(n).iterator();
        while (layerIter.hasNext()) {
            k = layerIter.next();
            if (pi.isNotFinal(k)) {
                layerIter.remove();
            }

        }

        //backward pass, removing arcs that does not lead to an accepting state
        int nbNodes = pi.getNbStates();
        BitSet mark = new BitSet(nbNodes);

        Node[] in = new Node[pi.getNbStates() * (n + 1)];
        Node tink = new Node(pi.getNbStates() + 1, n + 1, nid++);
        graph.addVertex(tink);

        for (i = n - 1; i >= 0; i--) {
            mark.clear(0, nbNodes);
            int UB = vs[i].getUB();
            for (j = vs[i].getLB(); j <= UB; j = vs[i].nextValue(j)) {
                int idx = starts[i] + j - offsets[i];
                TIntHashSet l = tmpQ[idx];
                if (l != null) {
                    qijIter = l.iterator();
                    while (qijIter.hasNext()) {
                        k = qijIter.next();
                        nexts.clear();
                        pi.delta(k, j, nexts);
                        if (nexts.size() > 1)
                            System.err.println("STOP");
                        boolean added = false;
                        for (TIntIterator it = nexts.iterator(); it.hasNext(); ) {
                            int qn = it.next();

                            if (layer.get(i + 1).contains(qn)) {
                                added = true;
                                Node a = in[i * pi.getNbStates() + k];
                                if (a == null) {
                                    a = new Node(k, i, nid++);
                                    in[i * pi.getNbStates() + k] = a;
                                    graph.addVertex(a);
                                }

                                Node b = in[(i + 1) * pi.getNbStates() + qn];
                                if (b == null) {
                                    b = new Node(qn, i + 1, nid++);
                                    in[(i + 1) * pi.getNbStates() + qn] = b;
                                    graph.addVertex(b);
                                }


                                Arc arc = new Arc(a, b, j, aid++);
                                graph.addEdge(a, b, arc);
                                tmp.get(idx).add(arc);

                                mark.set(k);
                            }
                        }
                        if (!added)
                            qijIter.remove();
                    }
                }
            }
            layerIter = layer.get(i).iterator();

            // If no more arcs go out of a given state in the layer, then we remove the state from that layer
            while (layerIter.hasNext())
                if (!mark.get(layerIter.next()))
                    layerIter.remove();
        }

        TIntHashSet th = new TIntHashSet();
        int[][] intLayer = new int[n + 2][];
        for (k = 0; k < pi.getNbStates(); k++) {
            Node o = in[n * pi.getNbStates() + k];
            {
                if (o != null) {
                    Arc a = new Arc(o, tink, 0, aid++);
                    graph.addEdge(o, tink, a);
                }
            }
        }


        for (i = 0; i <= n; i++) {
            th.clear();
            for (k = 0; k < pi.getNbStates(); k++) {
                Node o = in[i * pi.getNbStates() + k];
                if (o != null) {
                    th.add(o.id);
                }
            }
            intLayer[i] = th.toArray();
        }
        intLayer[n + 1] = new int[]{tink.id};

        if (intLayer[0].length > 0) {
            IEnvironment environment = model.getEnvironment();
            this.graph = new StoredDirectedMultiGraph(environment, graph, intLayer, starts, offsets, totalSizes, pi, z);
            this.graph.makePathFinder();
        }
    }

    private void filter() throws ContradictionException {
        checkWorld();
        this.delayedBoundUpdate();
        this.delayedGraphUpdate();
        this.modifiedBound[0] = true;
        this.modifiedBound[1] = true;
        this.computeSharpBounds();
        assert (toRemove.size() == 0);
        assert (check());
        assert (isGraphConsistent());
    }

    /**
     * Performs a lagrangian relaxation to compute a new Upper bound of the underlying RCSPP problem
     * Each built subproblem is a longest path one can use to perform cost based filtering
     *
     * @throws ContradictionException if a domain becomes empty
     */
    protected void updateUpperBound() throws ContradictionException {
        int k = 0;
        double uk;
        double lp;
        double axu;
        double newLB;
        double newLA;
        boolean modif;
        int[] P;
        double coeff;
        double bk = RO;
        int nbNSig = 0;
        int nbNSig2 = 0;
        double bestVal = Double.POSITIVE_INFINITY;
        //   Arrays.fill(uUb,0.0);
        do {
            coeff = 0.0;
            for (int i = 0; i < nbR; i++) {
                coeff += (uUb[i] * z[i + 1].getUB());
                coeff -= (uUb[i + nbR] * z[i + 1].getLB());
            }


            modif = false;

            slp.computeLongestPath(toRemove, z[0].getLB() - coeff, uUb, true, true, 0, this);

            lp = slp.getLongestPathValue();
            P = slp.getLongestPath();
            filterUp(lp + coeff);

            if (bestVal - (lp + coeff) < 1.0 / 2.0) {
                nbNSig++;
                nbNSig2++;
            } else {
                nbNSig = 0;
                nbNSig2 = 0;

            }
            if (nbNSig == 3) {
                bk *= 0.8;
                nbNSig = 0;
            }
            if (lp + coeff < bestVal) {
                bestVal = lp + coeff;
            }

            uk = U0 * Math.pow(bk, k);

            for (int l = 0; l < uUb.length / 2; l++) {
                axu = 0.0;
                for (int e : P) {
                    int i = graph.GNodes.layers[graph.GArcs.origs[e]];//  e.getOrigin().getLayer();
                    //int j = graph.GArcs.values[e];//e.getLabel();
                    if (i < offset)
                        axu += graph.GArcs.originalCost[e][l + 1];//costs[i][j][l+1];
                }
                newLB = Math.max(uUb[l] - uk * (z[l + 1].getUB() - axu), 0);
                newLA = Math.max(uUb[l + nbR] - uk * (axu - z[l + 1].getLB()), 0);
                if (Math.abs(uUb[l] - newLB) >= _MCR_DECIMAL_PREC) {
                    uUb[l] = newLB;
                    modif = true;
                }
                if (Math.abs(uUb[l + nbR] - newLA) >= _MCR_DECIMAL_PREC) {
                    uUb[l + nbR] = newLA;
                    modif = true;
                }
            }
            k++;

        } while (modif && nbNSig2 < MAXNONIMPROVEITER && k < MAXBOUNDITER);
    }


    /**
     * Performs a lagrangian relaxation to compute a new Lower bound of the underlying RCSPP problem
     * Each built subproblem is a shortest path one can use to perform cost based filtering
     *
     * @throws ContradictionException if a domain becomes empty
     */
    protected void updateLowerBound() throws ContradictionException {


        int k = 0;
        boolean modif;
        double sp;
        double uk;
        double axu;
        double newLB;
        double newLA;
        int[] P;
        double coeff;
        double bk = RO;
        double bestVal = Double.NEGATIVE_INFINITY;
        int nbNSig = 0;
        int nbNSig2 = 0;
        //  Arrays.fill(uLb,0.0);
        int[] bestPath = new int[offset + 1];
        do {
            coeff = 0.0;
            for (int i = 0; i < nbR; i++) {
                coeff += (uLb[i] * z[i + 1].getUB());
                coeff -= (uLb[i + nbR] * z[i + 1].getLB());
            }

            modif = false;

            slp.computeShortestPath(toRemove, z[0].getUB() + coeff, uLb, true, false, 0, this);


            sp = slp.getShortestPathValue();
            P = slp.getShortestPath();
            filterDown(sp - coeff);


            if ((sp - coeff) - bestVal < 1.0 / 2.0) {
                nbNSig++;
                nbNSig2++;
            } else {
                nbNSig = 0;
                nbNSig2 = 0;
            }
            if (nbNSig == 3) {
                bk *= 0.8;
                nbNSig = 0;
            }
            if (sp - coeff > bestVal) {
                bestVal = sp - coeff;
                System.arraycopy(P, 0, bestPath, 0, P.length);
            }


            uk = U0 * Math.pow(bk, k);

            for (int l = 0; l < uLb.length / 2; l++) {

                axu = 0.0;
                for (int e : P) {
                    int i = graph.GNodes.layers[graph.GArcs.origs[e]];
                    if (i < offset)
                        axu += graph.GArcs.originalCost[e][l + 1];
                }

                newLB = Math.max(uLb[l] + uk * (axu - z[l + 1].getUB()), 0);
                newLA = Math.max(uLb[l + nbR] + uk * (z[l + 1].getLB() - axu), 0);
                if (Math.abs(uLb[l] - newLB) >= _MCR_DECIMAL_PREC) {
                    uLb[l] = newLB;
                    modif = true;
                }
                if (Math.abs(uLb[l + nbR] - newLA) >= _MCR_DECIMAL_PREC) {
                    uLb[l + nbR] = newLA;
                    modif = true;
                }


            }
            k++;
        } while (modif && nbNSig2 < MAXNONIMPROVEITER && k < MAXBOUNDITER);
    }


    /**
     * Performs cost based filtering w.r.t. each cost dimension.
     *
     * @throws ContradictionException if a domain is emptied
     */
    protected boolean prefilter() throws ContradictionException {
        FastPathFinder p = this.graph.getPathFinder();

        boolean cont = true;
        boolean[] modified;
        while (cont) {
            modified = p.computeShortestAndLongestPath(toRemove, z, this);
            cont = toRemove.size() > 0;
            modifiedBound[0] |= modified[0];
            modifiedBound[1] |= modified[1];
            this.delayedGraphUpdate();

        }
        return (modifiedBound[0] || modifiedBound[1]);
    }


    /**
     * Filters w.r.t. a given lower bound.
     *
     * @param realsp a given lower bound
     * @throws ContradictionException if the cost variable domain is emptied
     */
    protected void filterDown(final double realsp) throws ContradictionException {

        if (realsp - z[0].getUB() >= _MCR_DECIMAL_PREC) {
            // "cost variable domain is emptied"
            fails();
        }
        if (realsp - z[0].getLB() >= _MCR_DECIMAL_PREC) {
            double mr = Math.round(realsp);
            double rsp = (realsp - mr <= _MCR_DECIMAL_PREC) ? mr : realsp;
            z[0].updateLowerBound((int) Math.ceil(rsp), this);//, false);
            modifiedBound[0] = true;
        }
    }

    /**
     * Filters w.r.t. a given upper bound.
     *
     * @param reallp a given upper bound
     * @throws ContradictionException if the cost variable domain is emptied
     */
    protected void filterUp(final double reallp) throws ContradictionException {
        if (reallp - z[0].getLB() <= -_MCR_DECIMAL_PREC) {
            // "cost variable domain is emptied"
            fails();
        }
        if (reallp - z[0].getUB() <= -_MCR_DECIMAL_PREC) {
            double mr = Math.round(reallp);
            double rsp = (reallp - mr <= _MCR_DECIMAL_PREC) ? mr : reallp;
            z[0].updateUpperBound((int) Math.floor(rsp), this);//, false);
            modifiedBound[1] = true;
        }
    }

    protected void checkWorld() throws ContradictionException {
        int currentworld = model.getEnvironment().getWorldIndex();
        long currentbt = model.getSolver().getBackTrackCount();
        long currentrestart = model.getSolver().getRestartCount();
        //System.err.println("TIME STAMP : "+currentbt+"   BT COUNT : "+solver.getBackTrackCount());
        // assert (currentbt == model.getBackTrackCount());
        if (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts) {

            for (int i = 0; i <= nbR; i++) {
                this.toUpdateLeft[i].clear();
                this.toUpdateRight[i].clear();
            }

            this.toRemove.clear();
            this.graph.inStack.clear();


            this.getGraph().getPathFinder().computeShortestAndLongestPath(toRemove, z, this);
            computed = true;
            //assert(toRemove.size() == 0); // PAS SUR DE L'ASSERT
            // this.graph.toUpdateLeft.reset();
            //this.graph.toUpdateRight.reset();
        }
        lastWorld = currentworld;
        lastNbOfBacktracks = currentbt;
        lastNbOfRestarts = currentrestart;
    }


    /**
     * Updates the graphs w.r.t. the caught event during event-based propagation
     *
     * @throws ContradictionException if removing an edge causes a domain to be emptied
     */
    protected void delayedGraphUpdate() throws ContradictionException {

        try {
            do
            //while (toRemove.size() > 0)
            {
                while (toRemove.size() > 0) {
                    int n = toRemove.pop();
//                    needUpdate =
                    this.graph.removeArc(n, toRemove, toUpdateLeft, toUpdateRight, this);
                    // modifiedBound[0] = modifiedBound[1]  = true;
                }
                // if (needUpdate)
                for (int k = 0; k <= nbR; k++) {
                    while (this.toUpdateLeft[k].size() > 0) {
                        this.graph.updateLeft(this.toUpdateLeft[k], toRemove, k, modifiedBound, this);
                        if (toRemove.size() > 0) break;
                    }
                    while (this.toUpdateRight[k].size() > 0) {
                        this.graph.updateRight(this.toUpdateRight[k], toRemove, k, modifiedBound, this);
                        if (toRemove.size() > 0) break;
                    }
                }


            } while (toRemove.size() > 0);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        // System.err.println("MAX : "+max);
        //  this.prefilter();
    }


    /**
     * Iteratively compute upper and lower bound for the underlying RCSPP
     *
     * @throws ContradictionException if a domain gets empty
     */
    public void computeSharpBounds() throws ContradictionException {
        // do
        // {
        while (modifiedBound[0] || modifiedBound[1]) {
            if (modifiedBound[1]) {
                modifiedBound[1] = false;
                updateLowerBound();
            }
            if (modifiedBound[0]) {
                modifiedBound[0] = false;
                updateUpperBound();
            }
            /*if (!modifiedBound[0] && !modifiedBound[1]) */
            this.delayedGraphUpdate();
        }  // } while(this.prefilter());
    }


    private boolean remContains(int e) {
        int[] element = toRemove.toArray();
        for (int i = 0; i < toRemove.size(); i++)
            if (element[i] == e)
                return true;
        return false;
    }

    private void checkBounds() throws ContradictionException {
        List<ICounter> counters = pi.getCounters();
        int nbCounters = pi.getNbResources();
        for (int i = 0; i < nbCounters; i++) {
            IntVar z = this.z[i];
            Bounds bounds = counters.get(i).bounds();
            z.updateBounds(bounds.min.value, bounds.max.value, this);//, false);

        }
    }

    private void delayedBoundUpdate() throws ContradictionException {
        if (!computed && boundUpdate.size() > 0) {
            this.getGraph().delayedBoundUpdate(toRemove, z, boundUpdate.toArray());
            boundUpdate.clear();
        }
    }

    public void rebuildCostRegInfo() throws ContradictionException {
        checkWorld();
    }

    public final boolean needPropagation() {
        int currentworld = model.getEnvironment().getWorldIndex();
        long currentbt = model.getSolver().getBackTrackCount();
        long currentrestart = model.getSolver().getRestartCount();

        return (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts);

    }


    public boolean isGraphConsistent() {
        for (int i = 0; i < offset; i++) {
            DisposableIntIterator iter = this.graph.layers[i].getIterator();
            while (iter.hasNext()) {
                int n = iter.next();
                DisposableIntIterator it = this.graph.GNodes.outArcs[n].getIterator();
                while (it.hasNext()) {
                    int arc = it.next();
                    int val = this.graph.GArcs.values[arc];
                    if (!vars[i].contains(val)) {
                        System.err.println("Arc " + arc + " from node " + n + " to node" + this.graph.GArcs.dests[arc] + " with value " + val + " in layer " + i + " should not be here");
                        return false;
                    }
                }
            }
            iter.dispose();
        }
        return true;
    }


    public final StoredDirectedMultiGraph getGraph() {
        return graph;
    }

    public final int getRegret(int layer, int value, int... resources) {
        return this.graph.getRegret(layer, value, resources);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(isSatisfied());
        } else {
            return ESat.UNDEFINED;
        }
    }


    public boolean isSatisfied() {
        for (IntVar var : this.vars) {
            if (!var.isInstantiated())
                return false;
        }
        return check();
    }

    public boolean isSatisfied(int[] word) {
        int first[] = new int[offset];
        System.arraycopy(word, 0, first, 0, first.length);
        return check(first);
    }

    public boolean check(int[] word) {
        if (!pi.run(word)) {
            System.err.println("Word is not accepted by the automaton");
            System.err.print("{" + word[0]);
            for (int i = 1; i < word.length; i++)
                System.err.print("," + word[i]);
            System.err.println("}");

            return false;
        }
        int[] gcost = new int[z.length];
        for (int l = 0; l < graph.layers.length - 2; l++) {
            DisposableIntIterator it = graph.layers[l].getIterator();
            while (it.hasNext()) {
                int orig = it.next();
                DisposableIntIterator arcIter = graph.GNodes.outArcs[orig].getIterator();
                while (arcIter.hasNext()) {
                    int arc = arcIter.next();
                    for (int i = 0; i < z.length; i++)
                        gcost[i] += graph.GArcs.originalCost[arc][i];
                }
                arcIter.dispose();

            }
            it.dispose();
        }
        for (int i = 0; i < gcost.length; i++) {
            if (!z[i].isInstantiated()) {
                if(DEBUG) model.getSolver().getOut().printf("z[" + i + "] in MCR should be instantiated : " + z[i]);
                return false;
            } else if (z[i].getValue() != gcost[i]) {
                if(DEBUG) model.getSolver().getOut().printf("cost: " + gcost[i] + " != z:" + z[i].getValue());
                return false;
            }

        }
        return true;

    }

    /**
     * Necessary condition : checks whether the constraint is violted or not
     *
     * @return true if the constraint is not violated
     */
    public boolean check() {
        int[] word = new int[offset];
        for (int i = 0; i < offset; i++) {
            if (!vs[i].isInstantiated())
                return true;
            word[i] = vs[i].getValue();
        }
        for (IntVar aZ : z) {
            if (!aZ.isInstantiated()) return true;
        }
        return check(word);
    }

    public int getMinPathCostForAssignment(int col, int val, int... resources) {
        return this.graph.getMinPathCostForAssignment(col, val, resources);
    }

    public int[] getMinMaxPathCostForAssignment(int col, int val, int... resources) {
        return this.graph.getMinMaxPathCostForAssignment(col, val, resources);
    }

    public int getMinPathCost(int... resources) {
        return this.graph.getMinPathCost(resources);
    }

    public double[] getInstantiatedLayerCosts(int layer) {
        return this.graph.getInstantiatedLayerCosts(layer);
    }

    public void forcePathRecomputation() throws ContradictionException {
        lastWorld = Integer.MAX_VALUE;
        checkWorld();
    }


    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropMultiCostRegular p;
        private int idxVar;

        public RemProc(PropMultiCostRegular p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            StoredIndexedBipartiteSet support = p.graph.getUBport(idxVar, i);
            if (support != null) {
                final int[] list = support._getStructure();
                final int size = support.size();
                for (int j = 0; j < size; j++) {
                    int e = list[j];//t.next();
                    assert (p.graph.isInStack(e) == p.remContains(e));
                    if (!p.graph.isInStack(e)) {
                        p.graph.setInStack(e);
                        p.toRemove.push(e);
                    }
                }
            }
        }
    }

}
