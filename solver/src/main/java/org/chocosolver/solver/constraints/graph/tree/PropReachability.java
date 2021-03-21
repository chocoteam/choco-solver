/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.tree;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;

/**
 * Every vertex is reachable from the root
 * Removes unreachable nodes
 * Enforces dominator nodes
 * Enforces dominator arc
 *
 * @author Jean-Guillaume Fages
 */
public class PropReachability extends PropArborescence {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropReachability(DirectedGraphVar graph, int root) {
        super(graph, root);
    }

    public PropReachability(DirectedGraphVar graph, int root, boolean simple) {
        super(graph, root, simple);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************


    protected void remBackArcs() throws ContradictionException {
        // reachability allows circuits
        // it does not have to remove backward arcs
    }
}
