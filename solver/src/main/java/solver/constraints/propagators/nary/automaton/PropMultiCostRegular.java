/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.nary.automaton;

import choco.kernel.ESat;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.procedure.IntProcedure1;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import gnu.trove.TObjectIntHashMap;
import org.jgrapht.graph.DirectedMultigraph;
import org.slf4j.LoggerFactory;
import solver.Constant;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.automata.FA.ICostAutomaton;
import solver.constraints.nary.automata.FA.utils.Bounds;
import solver.constraints.nary.automata.FA.utils.ICounter;
import solver.constraints.nary.automata.MultiCostRegular;
import solver.constraints.nary.automata.structure.Node;
import solver.constraints.nary.automata.structure.multicostregular.Arc;
import solver.constraints.nary.automata.structure.multicostregular.FastPathFinder;
import solver.constraints.nary.automata.structure.multicostregular.StoredDirectedMultiGraph;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.domain.delta.IntDelta;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;


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


    /**
     * Maximum number of iteration during a bound computation
     */
    public static int MAXBOUNDITER = 10;

    /**
     * Maximum number of non improving iteration while computing a bound
     */
    public static int MAXNONIMPROVEITER = 15;

    /**
     * Constant coefficient of the lagrangian relaxation
     */
    public static double U0 = 10.0;

    /**
     * Lagrangian multiplier decreasing factor
     */
    public static double RO = 0.7;


    /**
     * Map to retrieve rapidly the index of a given variable.
     */
    public final TObjectIntHashMap<IntVar> map;

    /**
     * The last computed Shortest Path
     */
    public int[] lastSp;
    public double lastSpValue;


    /**
     * The last computed Longest Path
     */
    public int[] lastLp;
    public double lastLpValue;


    /**
     * Decision variables
     */
    protected final IntVar[] vs;

    protected final int offset;

    /**
     * Cost variables
     */
    public final IntVar[] z;

/**
 * Integral costs : c[i][j][k][s] is the cost over dimension k of x_i = j on state s
 */
//protected final int[][][][] costs;

    /**
     * The finite automaton which defines the regular language the variable sequence must belong
     */
    protected final ICostAutomaton pi;

    /**
     * Layered graph of the unfolded automaton
     */
    protected StoredDirectedMultiGraph graph;

    /**
     * Boolean array which record whether a bound has been modified by the propagator
     */
    protected final boolean[] modifiedBound;

/**
 * Cost to be applied to the graph for a given relaxation
 */
// protected final double[][] newCosts;

    /**
     * Lagrangian multiplier container to compute an UB
     */
    protected final double[] uUb;

    /**
     * Lagrangian multiplier container to compute a LB
     */
    protected final double[] uLb;

    /**
     * Instance of the class containing all path finding algorithms
     * Also contains graph filtering algorithms
     */
    protected FastPathFinder slp;

    /**
     * Store the number of resources = z.length
     */
    protected final int nbR;


    /**
     * Stack to store removed edges index, for delayed update
     */
    protected final TIntStack toRemove;

    protected final TIntStack[] toUpdateLeft;
    protected final TIntStack[] toUpdateRight;

    /**
     * Buffer to check whether an arc needs to be removed.
     */
    protected final TIntHashSet removed = new TIntHashSet();

    public int lastWorld = -1;
    public long lastNbOfBacktracks = -1;
    public long lastNbOfRestarts = -1;
    private TIntHashSet boundUpdate;
    private boolean computed;

    Solver solver;

    protected final RemProc rem_proc;


    /**
     * Constructs a multi-cost-regular propagator
     *
     * @param vars        decision variables
     * @param counterVars cost variables
     * @param cauto       finite automaton with costs
     * @param solver      solver
     * @param constraint  constraint
     */
    public PropMultiCostRegular(IntVar[] vars, final IntVar[] counterVars, ICostAutomaton cauto,
                                Solver solver,
                                Constraint<IntVar, Propagator<IntVar>> constraint) {
        super(ArrayUtils.<IntVar>append(vars, counterVars), solver, constraint, PropagatorPriority.CUBIC, false);
        this.solver = solver;
        this.vs = vars;
        this.offset = vars.length;
        this.z = counterVars;
        this.nbR = this.z.length - 1;
        this.modifiedBound = new boolean[]{true, true};

        this.uUb = new double[2 * nbR];
        this.uLb = new double[2 * nbR];

        this.map = new TObjectIntHashMap<IntVar>();
        for (int i = 0; i < vars.length; i++) {
            this.map.put(vars[i], i);
        }
        this.toRemove = new TIntStack();
        this.toUpdateLeft = new TIntStack[nbR + 1];
        this.toUpdateRight = new TIntStack[nbR + 1];

        for (int i = 0; i <= nbR; i++) {
            this.toUpdateLeft[i] = new TIntStack();
            this.toUpdateRight[i] = new TIntStack();
        }
        this.boundUpdate = new TIntHashSet();
        this.pi = cauto;
        rem_proc = new RemProc(this);
    }


    @Override
    public int getPropagationConditions(int vIdx) {
//TODO        return (vIdx < vs.length ? EventType.REMOVE.mask : EventType.BOUND.mask + EventType.INSTANTIATE.mask);
        return EventType.ALL_MASK();
    }


    @Override
    public void propagate() throws ContradictionException {
        checkBounds();
        initGraph();
        this.slp = graph.getPathFinder();
        int left, right;
        for (int i = 0; i < offset; i++) {
            left = right = Integer.MIN_VALUE;
            for (int j = vs[i].getLB(); j <= vs[i].getUB(); j = vs[i].nextValue(j)) {
                StoredIndexedBipartiteSet sup = graph.getUBport(i, j);
                if (sup == null || sup.isEmpty()) {
                    if (j == right + 1) {
                        right = j;
                    } else {
                        vs[i].removeInterval(left, right, this);//, false);
                        left = right = j;
                    }
                }
            }
            vs[i].removeInterval(left, right, this);//, false);
        }
        this.slp.computeShortestAndLongestPath(toRemove, z, this);

        filter();
    }

    @Override
    public void propagateOnRequest(IRequest<IntVar> request, int vIdx, int mask) throws ContradictionException {
        if (vIdx < offset) {
            checkWorld();
            IntVar var = request.getVariable();
            IntDelta delta = var.getDelta();
            int f = request.fromDelta();
            int l = request.toDelta();
            delta.forEach(rem_proc.set(vIdx), f, l);
        } else if (EventType.isInstantiate(mask) || EventType.isBound(mask)) {
            boundUpdate.add(vIdx - offset);
            computed = false;
        }
        if (getNbRequestEnqued() == 0 && toRemove.size() > 0) {
            filter();
        }
//        propagate();
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
        graph = new DirectedMultigraph<Node, Arc>(new Arc.ArcFacroty());
        ArrayList<HashSet<Arc>> tmp = new ArrayList<HashSet<Arc>>(totalSizes);
        for (int i = 0; i < totalSizes; i++)
            tmp.add(new HashSet<Arc>());


        int i, j, k;
        TIntIterator layerIter;
        TIntIterator qijIter;

        ArrayList<TIntHashSet> layer = new ArrayList<TIntHashSet>();
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
            if (!pi.isFinal(k)) {
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
            this.graph = new StoredDirectedMultiGraph(environment, graph, intLayer, starts, offsets, totalSizes, pi, z);
            this.graph.makePathFinder();
            ((MultiCostRegular) this.constraint).setGraph(this.graph);
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
                if (Math.abs(uUb[l] - newLB) >= Constant.MCR_DECIMAL_PREC) {
                    uUb[l] = newLB;
                    modif = true;
                }
                if (Math.abs(uUb[l + nbR] - newLA) >= Constant.MCR_DECIMAL_PREC) {
                    uUb[l + nbR] = newLA;
                    modif = true;
                }
            }
            k++;

        } while (modif && nbNSig2 < MAXNONIMPROVEITER && k < MAXBOUNDITER);
        this.lastLp = P;
        this.lastLpValue = lp + coeff;

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
                if (Math.abs(uLb[l] - newLB) >= Constant.MCR_DECIMAL_PREC) {
                    uLb[l] = newLB;
                    modif = true;
                }
                if (Math.abs(uLb[l + nbR] - newLA) >= Constant.MCR_DECIMAL_PREC) {
                    uLb[l + nbR] = newLA;
                    modif = true;
                }


            }
            k++;
        } while (modif && nbNSig2 < MAXNONIMPROVEITER && k < MAXBOUNDITER);
        this.lastSp = bestPath;
        this.lastSpValue = bestVal;
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

        if (realsp - z[0].getUB() >= Constant.MCR_DECIMAL_PREC) {
            this.contradiction(null, "cost variable domain is emptied");
        }
        if (realsp - z[0].getLB() >= Constant.MCR_DECIMAL_PREC) {
            double mr = Math.round(realsp);
            double rsp = (realsp - mr <= Constant.MCR_DECIMAL_PREC) ? mr : realsp;
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
        if (reallp - z[0].getLB() <= -Constant.MCR_DECIMAL_PREC) {
            this.contradiction(null, "cost variable domain is emptied");
        }
        if (reallp - z[0].getUB() <= -Constant.MCR_DECIMAL_PREC) {
            double mr = Math.round(reallp);
            double rsp = (reallp - mr <= Constant.MCR_DECIMAL_PREC) ? mr : reallp;
            z[0].updateUpperBound((int) Math.floor(rsp), this);//, false);
            modifiedBound[1] = true;
        }
    }

    protected void checkWorld() throws ContradictionException {
        int currentworld = environment.getWorldIndex();
        long currentbt = solver.getSearchLoop().getMeasures().getBackTrackCount();
        long currentrestart = solver.getSearchLoop().getMeasures().getRestartCount();
        //System.err.println("TIME STAMP : "+currentbt+"   BT COUNT : "+solver.getBackTrackCount());
        // assert (currentbt == solver.getBackTrackCount());
        if (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts) {

            for (int i = 0; i <= nbR; i++) {
                this.toUpdateLeft[i].reset();
                this.toUpdateRight[i].reset();
            }

            this.toRemove.reset();
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

        boolean needUpdate = false;
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
        int[] element = toRemove.toNativeArray();
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
            z.updateLowerBound(bounds.min.value, this);//, false);
            z.updateUpperBound(bounds.max.value, this);//, false);

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
        int currentworld = environment.getWorldIndex();
        long currentbt = solver.getSearchLoop().getMeasures().getBackTrackCount();
        long currentrestart = solver.getSearchLoop().getMeasures().getRestartCount();

        return (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts);

    }


    public boolean isGraphConsistent() {
        boolean ret = true;
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
        return ret;
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
            if (!var.instantiated())
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
            if (!z[i].instantiated()) {
                LoggerFactory.getLogger("solver").error("z[" + i + "] in MCR should be instantiated : " + z[i]);
                return false;
            } else if (z[i].getValue() != gcost[i]) {
                LoggerFactory.getLogger("solver").error("cost: " + gcost[i] + " != z:" + z[i].getValue());
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
            if (!vs[i].instantiated())
                return true;
            word[i] = vs[i].getValue();
        }
        for (IntVar aZ : z) {
            if (!aZ.instantiated()) return true;
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


    private static class RemProc implements IntProcedure1<Integer> {

        private final PropMultiCostRegular p;
        private int idxVar;

        public RemProc(PropMultiCostRegular p) {
            this.p = p;
        }

        @Override
        public IntProcedure1 set(Integer idxVar) {
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
