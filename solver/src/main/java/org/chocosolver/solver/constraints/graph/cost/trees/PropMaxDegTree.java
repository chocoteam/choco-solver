/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.trees;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;

import java.util.BitSet;

/**
 * Redundant filtering for a tree for which the max degree of each vertex is restricted:
 * if dMax(i) = dMax(j) = 1, then edge (i,j) is infeasible
 * if dMax(k) = 2 and (i,k) is already forced, then (k,j) is infeasible
 * ...
 *
 * @author Jean-Guillaume Fages
 */
public class PropMaxDegTree extends Propagator<UndirectedGraphVar> {


    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected int n;
    protected int[] dMax;
    private final int[] counter;
    private final BitSet oneNode;
    private int[] list;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropMaxDegTree(UndirectedGraphVar g, int[] maxDegrees) {
        super(new UndirectedGraphVar[]{g}, PropagatorPriority.LINEAR, false);
        n = maxDegrees.length;
        oneNode = new BitSet(n);
        counter = new int[n];
        dMax = maxDegrees;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.ADD_EDGE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        preprocessOneNodes();
        UndirectedGraphVar g = vars[0];
        if (oneNode.cardinality() < n) {
            for (int i = 0; i < n; i++) {
                ISet nei = g.getPotentialNeighborsOf(i);
                if (oneNode.get(i)) {
                    for (int j : nei) {
                        if (oneNode.get(j) && !g.getMandatoryNeighborsOf(i).contains(j)) {
                            g.removeEdge(i, j, this);
                        }
                    }
                }
            }
        }
    }

    private void preprocessOneNodes() throws ContradictionException {
        ISet nei;
        oneNode.clear();
        for (int i = 0; i < n; i++) {
            counter[i] = 0;
        }
        UndirectedGraphVar g = vars[0];
        int[] maxDegree = dMax;
        if (list == null) {
            list = new int[n];
        }
        int first = 0;
        int last = 0;
        for (int i = 0; i < n; i++) {
            if (maxDegree[i] == 1) {
                list[last++] = i;
                oneNode.set(i);
            }
        }
        while (first < last) {
            int k = list[first++];
            nei = g.getMandatoryNeighborsOf(k);
            for (int s : nei) {
                if (!oneNode.get(s)) {
                    counter[s]++;
                    if (counter[s] > maxDegree[s]) {
                        fails();
                    } else if (counter[s] == maxDegree[s] - 1) {
                        oneNode.set(s);
                        list[last++] = s;
                    }
                }
            }
        }
    }


    @Override // redundant propagator
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
