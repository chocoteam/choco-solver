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
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;import gnu.trove.iterator.TIntIterator;
import org.jgrapht.graph.DirectedMultigraph;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.nary.automata.FA.CostAutomaton;
import solver.constraints.nary.automata.FA.IAutomaton;
import solver.constraints.nary.automata.FA.ICostAutomaton;
import solver.constraints.nary.automata.structure.Node;
import solver.constraints.nary.automata.structure.costregular.Arc;
import solver.constraints.nary.automata.structure.costregular.StoredValuedDirectedMultiGraph;
import solver.constraints.propagators.nary.automaton.PropCostRegular;
import solver.exception.SolverException;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.*;

/**
 * COST_REGULAR constraint
 * <br/>
 *
 * @author Julien Menana, Charles Prud'homme
 * @since 06/06/11
 */
public class CostRegular extends IntConstraint<IntVar> {

    final StoredValuedDirectedMultiGraph graph;
    final ICostAutomaton cautomaton;

//    public CostRegular(IntVar[] vars, Solver solver, PropagatorPriority storeThreshold) {
//        super(vars, solver, storeThreshold);
//    }

    public CostRegular(IntVar[] vars, IAutomaton automaton, int[][][] costs, Solver solver) {
        super(vars, solver);
        int zIdx = vars.length - 1;
        cautomaton =
                CostAutomaton.makeSingleResource(automaton, costs, vars[zIdx].getLB(), vars[zIdx].getUB());
        graph = initGraph(vars, cautomaton, solver.getEnvironment());
        setPropagators(new PropCostRegular(vars, cautomaton, graph, solver, this));
    }

    public CostRegular(IntVar[] vars, IAutomaton automaton, int[][] costs, Solver solver) {
        super(vars, solver);
        int zIdx = vars.length - 1;
        cautomaton =
                CostAutomaton.makeSingleResource(automaton, costs, vars[zIdx].getLB(), vars[zIdx].getUB());
        graph = initGraph(vars, cautomaton, solver.getEnvironment());
        setPropagators(new PropCostRegular(vars, cautomaton, graph, solver, this));
    }

    public CostRegular(IntVar[] vars, ICostAutomaton cautomaton, Solver solver) {
        super(vars, solver);
        this.cautomaton = cautomaton;
        graph = initGraph(vars, cautomaton, solver.getEnvironment());
        setPropagators(new PropCostRegular(vars, cautomaton, graph, solver, this));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int first = this.graph.sourceIndex;
        boolean found;
        double cost = 0.0;
        for (int i = 0; i < tuple.length - 1; i++) {
            found = false;
            StoredIndexedBipartiteSet bs = this.graph.GNodes.outArcs[first];
            DisposableIntIterator it = bs.getIterator();
            while (!found && it.hasNext()) {
                int idx = it.next();
                if (this.graph.GArcs.values[idx] == tuple[i]) {
                    found = true;
                    first = this.graph.GArcs.dests[idx];
                    cost += this.graph.GArcs.costs[idx];
                }
            }
            if (!found)
                return ESat.FALSE;

        }
        int intCost = tuple[tuple.length - 1];
        return ESat.eval(cost == intCost && cautomaton.run(Arrays.copyOf(tuple, tuple.length - 1)));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("CostRegular({");
        for (int i = 0; i < vars.length - 1; i++) {
            if (i > 0) sb.append(", ");
            Variable var = vars[i];
            sb.append(var.getName());
        }
        sb.append("},");
        sb.append(vars[vars.length - 1].getName());
        sb.append(")");
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private StoredValuedDirectedMultiGraph initGraph(DirectedMultigraph<Node, Arc> graph, Node source) {
        int size = vars.length - 1;
        int[] offsets = new int[size];
        int[] sizes = new int[size];
        int[] starts = new int[size];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0; i < size; i++) {
            offsets[i] = vars[i].getLB();
            sizes[i] = vars[i].getUB() - vars[i].getLB() + 1;
            if (i > 0) starts[i] = sizes[i - 1] + starts[i - 1];
            totalSizes += sizes[i];
        }

        TIntArrayList[] layers = new TIntArrayList[size + 1];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new TIntArrayList();
        }
        Queue<Node> queue = new ArrayDeque<Node>();
        source.layer = 0;
        queue.add(source);

        int nid = 0;
        int aid = 0;
        while (!queue.isEmpty()) {
            Node n = queue.remove();
            n.id = nid++;
            layers[n.layer].add(n.id);
            Set<Arc> tmp = graph.outgoingEdgesOf(n);
            for (Arc a : tmp) {
                a.id = aid++;
                Node next = graph.getEdgeTarget(a);
                next.layer = n.layer + 1;
                queue.add(next);
            }
        }
        int[][] lays = new int[layers.length][];
        for (int i = 0; i < lays.length; i++) {
            lays[i] = layers[i].toArray();
        }
        return new StoredValuedDirectedMultiGraph(solver.getEnvironment(), graph, lays, starts, offsets, totalSizes);
    }

    private static StoredValuedDirectedMultiGraph initGraph(IntVar[] vars, ICostAutomaton pi, IEnvironment environment) {
        int aid = 0;
        int nid = 0;

        int size = vars.length - 1;

        int[] offsets = new int[size];
        int[] sizes = new int[size];
        int[] starts = new int[size];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0; i < size; i++) {
            offsets[i] = vars[i].getLB();
            sizes[i] = vars[i].getUB() - vars[i].getLB() + 1;
            if (i > 0) starts[i] = sizes[i - 1] + starts[i - 1];
            totalSizes += sizes[i];
        }


        DirectedMultigraph<Node, Arc> graph;
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

        for (i = 0; i <= size; i++) {
            layer.add(new TIntHashSet());// = new DLList(nbNodes);
        }

        //forward pass, construct all paths described by the automaton for word of length nbVars.

        layer.get(0).add(pi.getInitialState());

        TIntHashSet succ = new TIntHashSet();
        for (i = 0; i < size; i++) {
            int ub = vars[i].getUB();
            for (j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                layerIter = layer.get(i).iterator();
                while (layerIter.hasNext()) {
                    k = layerIter.next();
                    succ.clear();
                    pi.delta(k, j, succ);
                    if (!succ.isEmpty()) {
                        TIntIterator it = succ.iterator();
                        for (; it.hasNext(); )
                            layer.get(i + 1).add(it.next());
                        int idx = starts[i] + j - offsets[i];
                        if (tmpQ[idx] == null)
                            tmpQ[idx] = new TIntHashSet();

                        tmpQ[idx].add(k);


                    }
                }
            }
        }

        //removing reachable non accepting states

        layerIter = layer.get(size).iterator();
        while (layerIter.hasNext()) {
            k = layerIter.next();
            if (!pi.isFinal(k)) {
                layerIter.remove();
            }

        }


        //backward pass, removing arcs that does not lead to an accepting state
        int nbNodes = pi.getNbStates();
        BitSet mark = new BitSet(nbNodes);

        Node[] in = new Node[pi.getNbStates() * (size + 1)];
        Node tink = new Node(pi.getNbStates() + 1, size + 1, nid++);
        graph.addVertex(tink);

        for (i = size - 1; i >= 0; i--) {
            mark.clear(0, nbNodes);
            int ub = vars[i].getUB();
            for (j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                int idx = starts[i] + j - offsets[i];
                TIntHashSet l = tmpQ[idx];
                if (l != null) {
                    qijIter = l.iterator();
                    while (qijIter.hasNext()) {
                        k = qijIter.next();
                        succ.clear();
                        pi.delta(k, j, succ);
                        TIntIterator it = succ.iterator();
                        boolean added = false;
                        for (; it.hasNext(); ) {
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


                                Arc arc = new Arc(a, b, j, aid++, pi.getCostByState(i, j, a.state));
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
        int[][] intLayer = new int[size + 2][];
        for (k = 0; k < pi.getNbStates(); k++) {
            Node o = in[size * pi.getNbStates() + k];
            {
                if (o != null) {
                    Arc a = new Arc(o, tink, 0, aid++, 0.0);
                    graph.addEdge(o, tink, a);
                }
            }
        }


        for (i = 0; i <= size; i++) {
            th.clear();
            for (k = 0; k < pi.getNbStates(); k++) {
                Node o = in[i * pi.getNbStates() + k];
                if (o != null) {
                    th.add(o.id);
                }
            }
            intLayer[i] = th.toArray();
        }
        intLayer[size + 1] = new int[]{tink.id};


        if (intLayer[0].length > 0)
            return new StoredValuedDirectedMultiGraph(environment, graph, intLayer, starts, offsets, totalSizes);
        else
            throw new SolverException("intLayer[0].length <= 0");
    }


}
