/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffBC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * Propagator for AllDifferent with condition using BC filtering algorithm (AlgoAllDiffBC).
 * Adapted from `PropAllDiffBC` and `PropCondAllDiff_AC`.
 *
 * @author Dimitri Justeau-Allaire (Adapted from `PropAllDiffBC` and `PropCondAllDiff_AC`)
 */
public class PropCondAllDiffBC extends PropCondAllDiffBase {

    private final AlgoAllDiffBC filter;

    public PropCondAllDiffBC(IntVar[] variables, Condition condition) {
        super(variables, condition, PropagatorPriority.LINEAR);
        this.filter = new AlgoAllDiffBC(this);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        IntVar[] vars = filterVariables();
        if (vars.length == 0) {
            return;
        }
        filter.reset(vars);
        filter.filter();
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // redundant propagator (use PropCondAllDiffInst)
    }
}
