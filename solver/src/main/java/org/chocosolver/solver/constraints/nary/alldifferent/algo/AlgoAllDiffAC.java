/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.algo;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;

import static org.chocosolver.solver.variables.IntVar.*;

/**
 * Algorithm of Alldifferent with AC
 * <p>
 * Uses Regin algorithm
 * Runs in O(m.n) worst case time for the initial propagation
 * but has a good average behavior in practice
 * <p/>
 * Keeps track of previous matching for further calls
 * <p/>
 *
 * @author Jean-Guillaume Fages
 */
public class AlgoAllDiffAC {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    int n, n2;
    DirectedGraph digraph;
    int[] matching;
    int[] nodeSCC;
    BitSet free;
    StrongConnectivityFinder SCCfinder;
    // for augmenting matching (BFS)
    private final int[] father;
    private final BitSet in;
    TIntIntHashMap map;
    int[] fifo;
    // for LCG
    TIntIntHashMap rmap;
    IntIterableRangeSet set;
    // input
    protected IntVar[] vars;
    Propagator<IntVar> aCause;
    int[] order;
    ArraySort<?> sorter;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AlgoAllDiffAC(IntVar[] variables, Propagator<IntVar> cause) {
        this.vars = variables;
        aCause = cause;
        n = vars.length;
        matching = new int[n];
        for (int i = 0; i < n; i++) {
            matching[i] = -1;
        }
        map = new TIntIntHashMap();
        rmap = new TIntIntHashMap();
        IntVar v;
        int ub;
        int idx = n;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (int j = v.getLB(); j <= ub; j = v.nextValue(j)) {
                if (!map.containsKey(j)) {
                    map.put(j, idx);
                    rmap.put(idx, j);
                    idx++;
                }
            }
        }
        n2 = idx;
        fifo = new int[n2];
        makeDigraph();
        free = new BitSet(n2);
        father = new int[n2];
        in = new BitSet(n2);
        SCCfinder = new StrongConnectivityFinder(digraph);
        order = ArrayUtils.array(0, n - 1);
        if (aCause.getModel().getSolver().isLCG()) {
            set = new IntIterableRangeSet();
            sorter = new ArraySort<>(n, false, true);
        }
    }

    protected void makeDigraph() {
        digraph = new DirectedGraph(n2 + 1, SetType.BITSET, false);
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    public boolean propagate() throws ContradictionException {
        findMaximumMatching();
        return filter();
    }

    //***********************************************************************************
    // Initialization
    //***********************************************************************************

    private void findMaximumMatching() throws ContradictionException {
        for (int i = 0; i < n2; i++) {
            digraph.getSuccessorsOf(i).clear();
            digraph.getPredecessorsOf(i).clear();
        }
        free.set(0, n2);
        int k, ub;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            int mate = matching[i];
            for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                int j = map.get(k);
                if (mate == j) {
                    assert free.get(i) && free.get(j);
                    digraph.addEdge(j, i);
                    free.clear(i);
                    free.clear(j);
                } else {
                    digraph.addEdge(i, j);
                }
            }
        }
        for (int i = free.nextSetBit(0); i >= 0 && i < n; i = free.nextSetBit(i + 1)) {
            tryToMatch(i);
        }
        for (int i = 0; i < n; i++) {
            matching[i] = digraph.getPredecessorsOf(i).isEmpty() ? -1 : digraph.getPredecessorsOf(i).iterator().next();
        }
    }

    private void tryToMatch(int i) throws ContradictionException {
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
        } else {
            Reason reason = Reason.undef();
            if (aCause.lcg()) {
                // check last cut
                set.clear();
                in.set(i);
                int nvars = 0;
                for (int l = in.nextSetBit(0); l > -1; l = in.nextSetBit(l + 1)) {
                    if (l < n) {
                        nvars++;
                    } else {
                        set.add(rmap.get(l));
                    }
                }
                int[] ps = new int[1 + nvars * (2 + (set.max() + 1 - set.min()) - set.size())];
                int m = 1;
                for (int l = in.nextSetBit(0); l > -1 && l < n; l = in.nextSetBit(l + 1)) {
                    ps[m++] = MiniSat.neg(vars[l].getLit(set.min(), LR_GE));
                    for (int w = set.nextValueOut(set.min()); w < set.max(); w = set.nextValueOut(w)) {
                        ps[m++] = MiniSat.neg(vars[l].getLit(w, LR_NE));
                    }
                    ps[m++] = MiniSat.neg(vars[l].getLit(set.max(), LR_LE));
                }
                assert m == ps.length;
                reason = Reason.r(ps);

            }
            aCause.fails(reason);
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

    void buildSCC() {
        if (n2 > n * 2) {
            digraph.removeNode(n2);
            digraph.addNode(n2);
            for (int i = n; i < n2; i++) {
                if (free.get(i)) {
                    digraph.addEdge(i, n2);
                } else {
                    digraph.addEdge(n2, i);
                }
            }
        }
        SCCfinder.findAllSCC();
        nodeSCC = SCCfinder.getNodesSCC();
        digraph.removeNode(n2);
    }

    void distinguish() {
        // void
    }

    boolean filterVar(int i) throws ContradictionException {
        boolean filter = false;
        IntVar v = vars[i];
        int ub = v.getUB();
        for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
            int j = map.get(k);
            filter |= filterVar(i, j, v, k);
        }
        return filter;
    }

    boolean filterVar(int i, int j, IntVar v, int k) throws ContradictionException {
        if (nodeSCC[i] != nodeSCC[j]) {
            if (matching[i] == j) { // fail fast
                if (!v.getModel().getSolver().isLCG()) {
                    return v.instantiateTo(k, aCause);
                }// else: ignore and let the loop empties the domain
            } else {
                Reason reason = Reason.undef();
                if (v.getModel().getSolver().isLCG()) {
                    int nvars = 0;
                    int val;
                    set.clear();
                    set.add(rmap.get(j));
                    for (int l = SCCfinder.getSCCFirstNode(nodeSCC[j]); l > -1; l = SCCfinder.getNextNode(l)) {
                        if (l < n) {
                            nvars++;
                        } else {
                            val = rmap.get(l);
                            set.add(val);
                        }
                    }
                    if (set.size() == 1) {
                        int vidx = digraph.getSuccessorsOf(j).min();
                        if (vars[vidx].isInstantiated()) {
                            assert vars[vidx].isInstantiatedTo(k);
                            reason = Reason.r(vars[vidx].getValLit());
                        } else {
                            // todo: While the component variable has not yet been determined,
                            //  but will be shortly, the chronology of events has not been respected.
                            return false;
                        }
                    } else {
                        int[] ps = new int[1 + nvars * (2 + (set.max() + 1 - set.min()) - set.size())];
                        int m = 1;
                        for (int l = SCCfinder.getSCCFirstNode(nodeSCC[j]); l > -1; l = SCCfinder.getNextNode(l)) {
                            if (l < n) {
                                ps[m++] = MiniSat.neg(vars[l].getLit(set.min(), LR_GE));
                                for (int w = set.nextValueOut(set.min()); w < set.max(); w = set.nextValueOut(w)) {
                                    ps[m++] = MiniSat.neg(vars[l].getLit(w, LR_NE));
                                }
                                ps[m++] = MiniSat.neg(vars[l].getLit(set.max(), LR_LE));
                            }
                        }
                        assert m == ps.length;
                        reason = Reason.r(ps);
                    }
                }
                if (v.removeValue(k, aCause, reason)) {
                    digraph.removeEdge(i, j);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean filter() throws ContradictionException {
        boolean filter = false;
        distinguish();
        buildSCC();
        int j, ub;
        IntVar v;
        if (aCause.getModel().getSolver().isLCG()) {
            sorter.sort(order, n, (k, l) -> nodeSCC[k] - nodeSCC[l]); // todo extract
        }
        for (int i = 0; i < n; i++) {
            filter |= filterVar(order[i]);
        }
        for (int i = 0; i < n; i++) {
            v = vars[i];
            if (!v.hasEnumeratedDomain()) {
                assert !v.getModel().getSolver().isLCG() : "not implemented yet for LCG";
                ub = v.getUB();
                for (int k = v.getLB(); k <= ub; k++) {
                    j = map.get(k);
                    if (!(digraph.containsEdge(i, j) || digraph.containsEdge(j, i))) {
                        filter |= v.removeValue(k, aCause);
                    }
                }
                int lb = v.getLB();
                for (int k = v.getUB(); k >= lb; k--) {
                    j = map.get(k);
                    if (!(digraph.containsEdge(i, j) || digraph.containsEdge(j, i))) {
                        filter |= v.removeValue(k, aCause);
                    }
                }
            }
        }
        return filter;
    }
}
