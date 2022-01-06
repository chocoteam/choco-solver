/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.algo;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * Algorithm of Alldifferent with AC
 *
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
    protected IntVar[] vars;
    ICause aCause;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AlgoAllDiffAC(IntVar[] variables, ICause cause) {
        this.vars = variables;
		aCause = cause;
        n = vars.length;
        matching = new int[n];
        for (int i = 0; i < n; i++) {
            matching[i] = -1;
        }
        map = new TIntIntHashMap();
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
        makeDigraph();
        free = new BitSet(n2);
        father = new int[n2];
        in = new BitSet(n2);
        SCCfinder = new StrongConnectivityFinder(digraph);
    }

    protected void makeDigraph(){
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
            matching[i] = digraph.getPredecessorsOf(i).isEmpty()?-1:digraph.getPredecessorsOf(i).iterator().next();
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
            vars[0].instantiateTo(vars[0].getLB()-1,aCause);
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
        boolean filter =false;
        IntVar v = vars[i];
        int ub = v.getUB();
        for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
            int j = map.get(k);
            if (nodeSCC[i] != nodeSCC[j]) {
                if (matching[i] == j) {
                    filter |= v.instantiateTo(k, aCause);
                } else {
                    filter |= v.removeValue(k, aCause);
                    digraph.removeEdge(i, j);
                }
            }
        }
        return filter;
    }

    private boolean filter() throws ContradictionException {
        boolean filter =false;
        distinguish();
        buildSCC();
        int j, ub;
        IntVar v;
        for (int i = 0; i < n; i++) {
            filter|=filterVar(i);
        }
        for (int i = 0; i < n; i++) {
            v = vars[i];
            if (!v.hasEnumeratedDomain()) {
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
