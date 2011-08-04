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
import choco.kernel.memory.IEnvironment;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import org.jgrapht.graph.DirectedMultigraph;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.nary.automata.FA.IAutomaton;
import solver.constraints.nary.automata.structure.Node;
import solver.constraints.nary.automata.structure.regular.Arc;
import solver.constraints.nary.automata.structure.regular.StoredDirectedMultiGraph;
import solver.constraints.propagators.nary.automaton.PropRegular;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class Regular extends IntConstraint<IntVar> {

    final StoredDirectedMultiGraph graph;
    final IAutomaton automaton;

    public Regular(IntVar[] vars, IAutomaton automaton, Solver solver) {
        super(vars, solver);
        this.automaton = automaton;
        graph = initGraph(solver.getEnvironment(), vars, automaton);
        setPropagators(new PropRegular(vars, automaton, graph, solver, this));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        return ESat.eval(automaton.run(tuple));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Regular(");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            Variable var = vars[i];
            sb.append(var.getName());
        }
        sb.append(")");
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static StoredDirectedMultiGraph initGraph(IEnvironment environment, IntVar[] vars, IAutomaton auto) {
        int aid = 0;
        int nid = 0;

        int[] offsets = new int[vars.length];
        int[] sizes = new int[vars.length];
        int[] starts = new int[vars.length];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0; i < vars.length; i++) {
            offsets[i] = vars[i].getLB();
            sizes[i] = vars[i].getUB() - vars[i].getLB() + 1;
            if (i > 0) starts[i] = sizes[i - 1] + starts[i - 1];
            totalSizes += sizes[i];
        }


        DirectedMultigraph<Node, Arc> graph;

        int n = vars.length;
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
        layer.get(0).add(auto.getInitialState());
        TIntHashSet nexts = new TIntHashSet();

        for (i = 0; i < n; i++) {
            int ub = vars[i].getUB();
            for (j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                layerIter = layer.get(i).iterator();//getIterator();
                while (layerIter.hasNext()) {
                    k = layerIter.next();
                    nexts.clear();

                    auto.delta(k, j, nexts);
                    for (TIntIterator it = nexts.iterator(); it.hasNext(); ) {
                        int succ = it.next();
                        layer.get(i + 1).add(succ);
                        //incrQ(i,j,);
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
            if (!auto.isFinal(k)) {
                layerIter.remove();
            }
        }
        //backward pass, removing arcs that does not lead to an accepting state
        int nbNodes = auto.getNbStates();
        BitSet mark = new BitSet(nbNodes);

        Node[] in = new Node[auto.getNbStates() * (n + 1)];

        for (i = n - 1; i >= 0; i--) {
            mark.clear(0, nbNodes);
            int ub = vars[i].getUB();
            for (j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                int idx = starts[i] + j - offsets[i];
                TIntHashSet l = tmpQ[idx];
                if (l != null) {
                    qijIter = l.iterator();
                    while (qijIter.hasNext()) {
                        k = qijIter.next();
                        nexts.clear();
                        auto.delta(k, j, nexts);
                        boolean added = false;
                        for (TIntIterator it = nexts.iterator(); it.hasNext(); ) {
                            int qn = it.next();
                            if (layer.get(i + 1).contains(qn)) {

                                added = true;
                                Node a = in[i * auto.getNbStates() + k];
                                if (a == null) {
                                    a = new Node(k, i, nid++);
                                    in[i * auto.getNbStates() + k] = a;
                                    graph.addVertex(a);
                                }

                                Node b = in[(i + 1) * auto.getNbStates() + qn];
                                if (b == null) {
                                    b = new Node(qn, i + 1, nid++);
                                    in[(i + 1) * auto.getNbStates() + qn] = b;
                                    graph.addVertex(b);
                                }

                                // BEWARE<CPRU>: cost is not required, 0.0 is a default value
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
        return new StoredDirectedMultiGraph(environment, graph, starts, offsets, totalSizes);
    }
}
