/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package solver.constraints.nary.automata;

import choco.kernel.ESat;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import org.jgrapht.graph.DirectedMultigraph;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.nary.automata.FA.CostAutomaton;
import solver.constraints.nary.automata.FA.IAutomaton;
import solver.constraints.nary.automata.FA.ICostAutomaton;
import solver.constraints.nary.automata.structure.Node;
import solver.constraints.nary.automata.structure.multicostregular.Arc;
import solver.constraints.nary.automata.structure.multicostregular.StoredDirectedMultiGraph;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.nary.automaton.PropMultiCostRegular;
import solver.variables.IntVar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

/**
 * <br/>
 *
 * @author Julien Menana, Charles Prud'homme
 * @since 18/07/11
 */
public class MultiCostRegular extends IntConstraint<IntVar> {

    /**
     * The finite automaton which defines the regular language the variable sequence must belong
     */
    protected ICostAutomaton pi;

    /**
     * Layered graph of the unfolded automaton
     */
    protected StoredDirectedMultiGraph graph;

    private final int offset;


    private MultiCostRegular(final IntVar[] vars, final IntVar[] counterVars, final Solver solver) {
        super(ArrayUtils.<IntVar>append(vars, counterVars), solver, PropagatorPriority.CUBIC);
        this.offset = vars.length;
        initGraph(vars, counterVars, solver.getEnvironment());
    }


    /**
     * Constructs a multi-cost-regular constraint propagator
     *
     * @param vars   decision variables
     * @param CR     cost variables
     * @param auto   finite automaton
     * @param costs  assignment cost arrays
     * @param solver solver
     */
    public MultiCostRegular(final IntVar[] vars, final IntVar[] CR, final IAutomaton auto, final int[][][] costs, Solver solver) {
        this(vars, CR, solver);
        this.pi = CostAutomaton.makeMultiResources(auto, costs, CR);
        setPropagators(new PropMultiCostRegular(vars, CR, pi, graph, solver, this));
    }

    /**
     * Constructs a multi-cost-regular constraint propagator
     *
     * @param vars   decision variables
     * @param CR     cost variables
     * @param auto   finite automaton
     * @param costs  assignment cost arrays
     * @param solver solver
     */
    public MultiCostRegular(final IntVar[] vars, final IntVar[] CR, final IAutomaton auto, final int[][][][] costs, final Solver solver) {
        this(vars, CR, solver);
        this.pi = CostAutomaton.makeMultiResources(auto, costs, CR);
        setPropagators(new PropMultiCostRegular(vars, CR, pi, graph, solver, this));
    }

    public MultiCostRegular(final IntVar[] vars, final IntVar[] CR, final ICostAutomaton pi, final Solver solver) {
        this(vars, CR, solver);
        this.pi = pi;
        setPropagators(new PropMultiCostRegular(vars, CR, pi, graph, solver, this));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int word[] = new int[offset];
        System.arraycopy(tuple, 0, word, 0, word.length);
        if (!pi.run(word)) {
            System.err.println("Word is not accepted by the automaton");
            System.err.print("{" + word[0]);
            for (int i = 1; i < word.length; i++)
                System.err.print("," + word[i]);
            System.err.println("}");

            return ESat.FALSE;
        }
        int coffset = vars.length - offset;
        int[] gcost = new int[coffset];
        for (int l = 0; l < graph.layers.length - 2; l++) {
            DisposableIntIterator it = graph.layers[l].getIterator();
            while (it.hasNext()) {
                int orig = it.next();
                DisposableIntIterator arcIter = graph.GNodes.outArcs[orig].getIterator();
                while (arcIter.hasNext()) {
                    int arc = arcIter.next();
                    for (int i = 0; i < coffset; i++)
                        gcost[i] += graph.GArcs.originalCost[arc][i];
                }
                arcIter.dispose();

            }
            it.dispose();
        }
        for (int i = 0; i < gcost.length; i++) {
            if (!vars[coffset + i].instantiated()) {
                LoggerFactory.getLogger("solver").error("z[" + i + "] in MCR should be instantiated : " + vars[coffset + i]);
                return ESat.FALSE;
            } else if (vars[coffset + i].getValue() != gcost[i]) {
                LoggerFactory.getLogger("solver").error("cost: " + gcost[i] + " != z:" + vars[coffset + i].getValue());
                return ESat.FALSE;
            }

        }
        return ESat.TRUE;
    }


    public void initGraph(IntVar[] vs, IntVar[] z, IEnvironment environment) {
        int aid = 0;
        int nid = 0;


        int[] offsets = new int[vs.length];
        int[] sizes = new int[vs.length];
        int[] starts = new int[vs.length];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0; i < vs.length; i++) {
            offsets[i] = vs[i].getLB();
            sizes[i] = vs[i].getUB() - vs[i].getLB() + 1;
            if (i > 0) starts[i] = sizes[i - 1] + starts[i - 1];
            totalSizes += sizes[i];
        }


        DirectedMultigraph<Node, Arc> graph;

        int n = vs.length;
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
            int ub = vs[i].getUB();
            for (j = vs[i].getLB(); j <= ub; j = vs[i].nextValue(j)) {
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
            int ub = vs[i].getUB();
            for (j = vs[i].getLB(); j <= ub; j = vs[i].nextValue(j)) {
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
        }
    }
}
