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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;

/**
 * Redundant filtering for a tree for which the max degree of each vertex is restricted:
 * if dMax(i) = dMax(j) = 1, then edge (i,j) is infeasible
 * if dMax(k) = 2 and (i,k) is already forced, then (k,j) is infeasible
 * ...
 *
 * @author Jean-Guillaume Fages
 */
public class PropMaxDegVarTree extends PropMaxDegTree {


    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected IntVar[] deg;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropMaxDegVarTree(UndirectedGraphVar g, IntVar[] degrees) {
        super(g, new int[degrees.length]);
        deg = degrees;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            dMax[i] = deg[i].getUB();
        }
        super.propagate(evtmask);
    }
}
