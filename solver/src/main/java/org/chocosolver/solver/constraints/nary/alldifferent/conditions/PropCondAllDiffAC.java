/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffAC;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffACFast;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 * subject to conditions (e.g. allDifferent_except_0)
 * AllDiff only applies on the subset of variables satisfying the given condition
 *
 * @author Jean-Guillaume Fages
 */
public class PropCondAllDiffAC extends PropCondAllDiffBase {

    protected final boolean fast;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * Holds only on the subset of variables satisfying the given condition
     *
     * @param variables array of integer variables
     * @param condition defines the subset of variables which is considered by the
     *                  allDifferent constraint
     */
    public PropCondAllDiffAC(IntVar[] variables, Condition condition, boolean fast) {
        super(variables, condition, PropagatorPriority.QUADRATIC);
        this.fast = fast;
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (fast) {
            AlgoAllDiffACFast filter = new AlgoAllDiffACFast(filterVariables(), this);
            filter.propagate();
        } else {
            AlgoAllDiffAC filter = new AlgoAllDiffAC(filterVariables(), this);
            filter.propagate();
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // redundant propagator (used with PropCondAllDiffInst)
    }


}
