/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.procedure.UnaryIntProcedure;

import java.util.BitSet;

import static org.chocosolver.solver.constraints.PropagatorPriority.QUADRATIC;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * AtLeastNValues Propagator (similar to SoftAllDiff)
 * The number of distinct values in vars is at least nValues
 * Performs Generalized Arc Consistency based on Maximum Bipartite Matching
 * The worst case time complexity is O(nm) but this is very pessimistic
 * In practice it is more like O(m) where m is the number of variable-value pairs
 * <p/>
 * BEWARE UNSAFE : BUG DETECTED THROUGH DOBBLE(3,4,6)
 * <p/>
 * !redundant propagator!
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtLeastNValues_AC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final int n2;
    private final DirectedGraph digraph;
    private int[] nodeSCC;
    private final BitSet free;
    private final UnaryIntProcedure<Integer> remProc;
    private final IIntDeltaMonitor[] idms;
    private final StrongConnectivityFinder SCCfinder;
    // for augmenting matching (BFS)
    private final int[] father;
    private final BitSet in;
    private final TIntIntHashMap map;
    private final int[] fifo;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AtLeastNValues Propagator (similar to SoftAllDiff)
     * The number of distinct values in vars is at least nValues
     * Performs Generalized Arc Consistency based on Maximum Bipartite Matching
     * The worst case time complexity is O(nm) but this is very pessimistic
     * In practice it is more like O(m) where m is the number of variable-value pairs
     *
     * @param variables array of integer variables
     * @param nValues integer variable
     */
    public PropAtLeastNValues_AC(IntVar[] variables, int[] vals, IntVar nValues) {
        super(concat(variables, nValues), QUADRATIC, true);
        this.idms = new IIntDeltaMonitor[this.vars.length];
        for (int i = 0; i < this.vars.length; i++) {
            idms[i] = this.vars[i].monitorDelta(this);
        }
        n = variables.length;
        map = new TIntIntHashMap(vals.length);
        IntVar v;
        int ub;
        int idx = n;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (int j = v.getLB(); j <= ub; j = v.nextValue(j)) {
                if (!map.containsKey(j)) {
                    map.put(j, idx);
                    idx++;
                }
            }
        }
        n2 = idx;
        fifo = new int[n2];
        digraph = new DirectedGraph(model, n2 + 2, SetType.LINKED_LIST, false);
        free = new BitSet(n2);
        remProc = new DirectedRemProc();
        father = new int[n2];
        in = new BitSet(n2);
        SCCfinder = new StrongConnectivityFinder(digraph);
    }

    //***********************************************************************************
    // Initialization
    //***********************************************************************************

    private void buildDigraph() {
        for (int i = 0; i < n2; i++) {
            digraph.getSuccessorsOf(i).clear();
            digraph.getPredecessorsOf(i).clear();
        }
        free.set(0, n2);
        int j, k, ub;
        IntVar v;
        for (int i = 0; i < n2 + 2; i++) {
            digraph.removeNode(i);
        }
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                j = map.get(k);
                digraph.addEdge(i, j);
            }
        }
    }

    //***********************************************************************************
    // MATCHING
    //***********************************************************************************

    private int repairMatching() {
        for (int i = free.nextSetBit(0); i >= 0 && i < n; i = free.nextSetBit(i + 1)) {
            tryToMatch(i);
        }
        int card = 0;
        for (int i = 0; i < n; i++) {
            if (digraph.getPredecessorsOf(i).size()>0) {
                card++;
            }
        }
        return card;
    }

    private void tryToMatch(int i) {
        int mate = augmentPath_BFS(i);
        if (mate != -1) {
            free.clear(mate);
            free.clear(i);
            int tmp = mate;
            while (tmp != i) {
                digraph.removeEdge(father[tmp], tmp);
                digraph.addEdge(tmp, father[tmp]);
                tmp = father[tmp];
            }
        }
    }

    private int augmentPath_BFS(int root) {
        in.clear();
        int indexFirst = 0, indexLast = 0;
        fifo[indexLast++] = root;
        int x;
        ISetIterator succs;
        while (indexFirst != indexLast) {
            x = fifo[indexFirst++];
            succs = digraph.getSuccessorsOf(x).iterator();
            while (succs.hasNext()) {
                int y = succs.nextInt();
                if (!in.get(y)) {
                    father[y] = x;
                    fifo[indexLast++] = y;
                    in.set(y);
                    if (free.get(y)) {
                        return y;
                    }
                }
            }
        }
        return -1;
    }

    //***********************************************************************************
    // PRUNING
    //***********************************************************************************

    private void buildSCC() {
        digraph.removeNode(n2);
        digraph.removeNode(n2 + 1);
        digraph.addNode(n2);
        digraph.addNode(n2 + 1);
        //TODO CHECK THIS PART
        for (int i = 0; i < n; i++) {
            if (free.get(i)) {
                digraph.addEdge(n2, i);
            } else {
                digraph.addEdge(i, n2);
            }
        }
        for (int i = n; i < n2; i++) {
            if (free.get(i)) {
                digraph.addEdge(i, n2 + 1);
            } else {
                digraph.addEdge(n2 + 1, i);
            }
        }
        SCCfinder.findAllSCC();
        nodeSCC = SCCfinder.getNodesSCC();
        digraph.removeNode(n2);
        digraph.removeNode(n2 + 1);
    }

    private void filter() throws ContradictionException {
        buildSCC();
        int j, ub;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                j = map.get(k);
                if (nodeSCC[i] != nodeSCC[j]) {
                    if (digraph.getPredecessorsOf(i).contains(j)) {
                        v.instantiateTo(k, this);
                    } else {
                        v.removeValue(k, this);
                        digraph.removeEdge(i, j);
                    }
                }
            }
            if (!v.hasEnumeratedDomain()) {
                ub = v.getUB();
                for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                    j = map.get(k);
                    if (digraph.containsEdge(i, j) || digraph.containsEdge(j, i)) {
                        break;
                    } else {
                        v.removeValue(k, this);
                    }
                }
                int lb = v.getLB();
                for (int k = ub; k >= lb; k = v.previousValue(k)) {
                    j = map.get(k);
                    if (digraph.containsEdge(i, j) || digraph.containsEdge(j, i)) {
                        break;
                    } else {
                        v.removeValue(k, this);
                    }
                }
            }
        }
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            if (n2 < n + vars[n].getLB()) {
                fails(); // TODO: could be more precise, for explanation purpose
            }
            buildDigraph();
        }
        digraph.removeNode(n2);
        digraph.removeNode(n2 + 1);
        free.clear();
        for (int i = 0; i < n; i++) {
            if (digraph.getPredecessorsOf(i).size() == 0) {
                free.set(i);
            }
        }
        for (int i = n; i < n2; i++) {
            if (digraph.getSuccessorsOf(i).size() == 0) {
                free.set(i);
            }
        }
        int card = repairMatching();
        vars[n].updateUpperBound(card, this);
        if (vars[n].getLB() == card) {
            filter();
        }
        for (int i = 0; i < idms.length; i++) {
            idms[i].startMonitoring();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx < n) {
            idms[varIdx].forEachRemVal(remProc.set(varIdx));
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        BitSet values = new BitSet(n2);
        BitSet mandatoryValues = new BitSet(n2);
        IntVar v;
        int ub;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            if (v.isInstantiated()) {
                mandatoryValues.set(map.get(ub));
            }
            for (int j = v.getLB(); j <= ub; j++) {
                values.set(map.get(j));
            }
        }
        if (mandatoryValues.cardinality() >= vars[n].getUB()) {
            return ESat.TRUE;
        }
        if (values.cardinality() < vars[n].getLB()) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    private class DirectedRemProc implements UnaryIntProcedure<Integer> {

        private int idx;

        public void execute(int i) throws ContradictionException {
            digraph.removeEdge(idx, map.get(i));
            digraph.removeEdge(map.get(i), idx);
        }

        @Override
        public UnaryIntProcedure<Integer> set(Integer integer) {
            this.idx = integer;
            return this;
        }
    }
}
