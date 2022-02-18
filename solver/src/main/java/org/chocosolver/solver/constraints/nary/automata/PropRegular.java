/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.automata.FA.IAutomaton;
import org.chocosolver.solver.constraints.nary.automata.structure.Node;
import org.chocosolver.solver.constraints.nary.automata.structure.regular.Arc;
import org.chocosolver.solver.constraints.nary.automata.structure.regular.StoredDirectedMultiGraph;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class PropRegular extends Propagator<IntVar> {

    private final StoredDirectedMultiGraph graph;
    private final IAutomaton automaton;
    private final RemProc rem_proc;
    private final IIntDeltaMonitor[] idms;

    public PropRegular(IntVar[] variables, IAutomaton automaton) {
        super(variables, PropagatorPriority.LINEAR, true);
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
            idms[i].startMonitoring();
        }
        rem_proc = new RemProc(this);
        this.automaton = automaton;
        graph = initGraph(model.getEnvironment(), vars, automaton);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        assert evtmask == PropagatorEventType.FULL_PROPAGATION.getMask();
        for (int i = 0; i < idms.length; i++) {
            idms[i].forEachRemVal(rem_proc.set(i));
            for (int j = vars[i].getLB(); j <= vars[i].getUB(); j = vars[i].nextValue(j)) {
                if (!graph.hasSupport(i, j)) {
                    vars[i].removeValue(j, this);
                }
            }
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        idms[varIdx].forEachRemVal(rem_proc.set(varIdx));
    }

    @Override
    public ESat isEntailed() {
        if (this.isCompletelyInstantiated()) {
            int[] str = new int[vars.length];
            for (int i = 0; i < vars.length; i++) {
                str[i] = vars[i].getValue();
            }
            return ESat.eval(automaton.run(str));
        }
        return ESat.UNDEFINED;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class RemProc implements UnaryIntProcedure<Integer> {

        private final PropRegular p;
        private int idxVar;

        public RemProc(PropRegular p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure<Integer> set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.graph.clearSupports(idxVar, i, p);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("Regular").append("(");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            Variable var = vars[i];
            sb.append(var.getName());
        }
        sb.append(")");
        //        sb.append(propagators[0].toString());
        return sb.toString();
    }

    //////////////////////

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
        graph = new DirectedMultigraph<>(null, null, false);
        ArrayList<HashSet<Arc>> tmp = new ArrayList<>(totalSizes);
        for (int i = 0; i < totalSizes; i++)
            tmp.add(new HashSet<>());


        int i, j, k;
        TIntIterator layerIter;
        TIntIterator qijIter;


        //forward pass, construct all paths described by the automaton for word of length nbVars.
        TIntHashSet[] layer = new TIntHashSet[n + 1];
        TIntHashSet[] tmpQ = new TIntHashSet[totalSizes];
        for (i = 0; i <= n; i++) {
            layer[i] = new TIntHashSet();
        }
        layer[0].add(auto.getInitialState());
        TIntHashSet nexts = new TIntHashSet();
        for (i = 0; i < n; i++) {
            int ub = vars[i].getUB();
            for (j = vars[i].getLB(); j <= ub; j = vars[i].nextValue(j)) {
                layerIter = layer[i].iterator();
                while (layerIter.hasNext()) {
                    k = layerIter.next();
                    nexts.clear();
                    auto.delta(k, j, nexts);
                    for (TIntIterator it = nexts.iterator(); it.hasNext(); ) {
                        int succ = it.next();
                        layer[i + 1].add(succ);
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
        layerIter = layer[n].iterator();
        while (layerIter.hasNext()) {
            k = layerIter.next();
            if (auto.isNotFinal(k)) {
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
                            if (layer[i + 1].contains(qn)) {

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
            layerIter = layer[i].iterator();
            // If no more arcs go out of a given state in the layer, then we remove the state from that layer
            while (layerIter.hasNext())
                if (!mark.get(layerIter.next()))
                    layerIter.remove();
        }
        return new StoredDirectedMultiGraph(environment, graph, starts, offsets, totalSizes);
    }

}
