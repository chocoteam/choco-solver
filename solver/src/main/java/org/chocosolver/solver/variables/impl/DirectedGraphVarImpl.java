/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.graphs.DirectedGraph;

public class DirectedGraphVarImpl extends AbstractGraphVar<DirectedGraph> implements DirectedGraphVar {

    ////////////////////////////////// GRAPH PART ///////////////////////////////////////

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a graph variable
     *
     * @param name
     * @param solver
     * @param LB
     * @param UB
     */
    public DirectedGraphVarImpl(String name, Model solver, DirectedGraph LB, DirectedGraph UB) {
        super(name, solver, LB, UB);
    }

}
