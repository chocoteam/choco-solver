/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.algo;

import java.util.BitSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Algorithm of Alldifferent with AC
 *
 * Uses Zhang algorithm in the paper of IJCAI-18
 * "A Fast Algorithm for Generalized Arc Consistency of the Alldifferent Constraint"
 *
 *
 * @author Jean-Guillaume Fages, Jia'nan Chen
 */
public class AlgoAllDiffACFast extends AlgoAllDiffAC{

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************
    /**
     * The 'distinction' set is used to hold variables belonging to Γ(A)
     * and values belonging to A in the paper.
     */
    private BitSet distinction;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public AlgoAllDiffACFast(IntVar[] variables, ICause cause) {
        super(variables, cause);
        n = vars.length;
        distinction = new BitSet(n2);
    }

    /**
     * The new algorithm does not need to add an auxiliary node to the directed graph,
     * so the number of nodes in the graph is n2, rather than n2 + 1.
     */
    protected void makeDigraph(){
        digraph = new DirectedGraph(n2, SetType.BITSET, false);
    }

    //***********************************************************************************
    // PRUNING
    //***********************************************************************************

    /**
     * Find variables belonging to Γ(A) and values belonging to A by BFS from free nodes.
     */
    void distinguish() {
        distinction.clear();
        int indexFirst = 0, indexLast = 0;
        ISetIterator predece;
        for (int i = free.nextSetBit(n); i >= n && i < n2; i = free.nextSetBit(i + 1)) {
            distinction.set(i);
            predece = digraph.getPredOf(i).iterator();
            while (predece.hasNext()) {
                int x = predece.nextInt();
                if (!distinction.get(x)) {
                    fifo[indexLast++] = x;
                    distinction.set(x);
                }
            }
            while (indexFirst != indexLast) {
                int y = fifo[indexFirst++];
                int v = matching[y];
                distinction.set(v);
                predece = digraph.getPredOf(v).iterator();
                while (predece.hasNext()) {
                    int x = predece.nextInt();
                    if (!distinction.get(x)) {
                        fifo[indexLast++] = x;
                        distinction.set(x);
                    }
                }
            }
        }
    }

    void buildSCC() {
        SCCfinder.findAllSCC(distinction);
        nodeSCC = SCCfinder.getNodesSCC();
    }

    boolean filterVar(int i) throws ContradictionException {
        boolean filter =false;
        IntVar v = vars[i];
        if (v.getDomainSize() > 1) {
            int ub = v.getUB();
            for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                int j = map.get(k);
                if (!distinction.get(j)) {
                    if (distinction.get(i)) { // Remove type 1 redundant edges between Γ(A) and Dc-A.
                        filter |= v.removeValue(k, aCause);
                        digraph.removeArc(i, j);
                    } else { // Remove type 2 redundant edges between Xc-Γ(A) and Dc-A.
                        if (nodeSCC[i] != nodeSCC[j]) {
                            if (matching[i] == j) {
                                filter |= v.instantiateTo(k, aCause);
                            } else {
                                filter |= v.removeValue(k, aCause);
                                digraph.removeArc(i, j);
                            }
                        }
                    }
                }
            }
        }
        return filter;
    }
}