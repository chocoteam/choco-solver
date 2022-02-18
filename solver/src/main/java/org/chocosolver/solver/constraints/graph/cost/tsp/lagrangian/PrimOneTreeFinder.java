/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.tsp.lagrangian;

import org.chocosolver.solver.constraints.graph.cost.GraphLagrangianRelaxation;
import org.chocosolver.solver.constraints.graph.cost.trees.lagrangian.PrimMSTFinder;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.setDataStructures.ISet;

public class PrimOneTreeFinder extends PrimMSTFinder {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    private int oneNode;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PrimOneTreeFinder(int nbNodes, GraphLagrangianRelaxation propagator) {
        super(nbNodes, propagator);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    protected void prim() throws ContradictionException {
        minVal = propHK.getMinArcVal();
        if (FILTER) {
            maxTArc = minVal;
        }
        chooseOneNode();
        inTree.set(oneNode);
        ISet nei = g.getNeighborsOf(oneNode);
        int min1 = -1;
        int min2 = -1;
        boolean b1 = false, b2 = false;
        for (int j : nei) {
            if (!b1) {
                if (min1 == -1) {
                    min1 = j;
                }
                if (costs[oneNode][j] < costs[oneNode][min1]) {
                    min2 = min1;
                    min1 = j;
                }
                if (propHK.isMandatory(oneNode, j)) {
                    if (min1 != j) {
                        min2 = min1;
                    }
                    min1 = j;
                    b1 = true;
                }
            }
            if (min1 != j && !b2) {
                if (min2 == -1 || costs[oneNode][j] < costs[oneNode][min2]) {
                    min2 = j;
                }
                if (propHK.isMandatory(oneNode, j)) {
                    min2 = j;
                    b2 = true;
                }
            }
        }
        if (min1 == -1 || min2 == -1) {
            propHK.contradiction();
        }
        if (FILTER) {
            if (!propHK.isMandatory(oneNode, min1)) {
                maxTArc = Math.max(maxTArc, costs[oneNode][min1]);
            }
            if (!propHK.isMandatory(oneNode, min2)) {
                maxTArc = Math.max(maxTArc, costs[oneNode][min2]);
            }
        }
        int first = -1, sizeFirst = n + 1;
        for (int i = 0; i < n; i++) {
            if (i != oneNode && g.getNeighborsOf(i).size() < sizeFirst) {
                first = i;
                sizeFirst = g.getNeighborsOf(i).size();
            }
        }
        if (first == -1) {
            propHK.contradiction();
        }
        addNode(first);
        int from, to;
        while (tSize < n - 2 && !heap.isEmpty()) {
            to = heap.removeFirstElement();
            from = mate[to];
            addArc(from, to);
        }
        if (tSize != n - 2) {
            propHK.contradiction();
        }
        addArc(oneNode, min1);
        addArc(oneNode, min2);
        if (Tree.getNeighborsOf(oneNode).size() != 2) {
            throw new UnsupportedOperationException();
        }
    }

    private void chooseOneNode() {
        oneNode = 0;
    }
}
