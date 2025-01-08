/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.alldifferent.conditions;

import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffAC;
import org.chocosolver.solver.constraints.nary.alldifferent.algo.AlgoAllDiffACFast;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

/**
 * Adapted from `PropAllDiffAdaptative` for conditional AllDifferent.
 *
 * @author Dimitri Justeau-Allaire (adapted from `PropAllDiffAdaptative`)
 */
public class PropCondAllDiffAdaptative extends PropCondAllDiffAC {

    private final Random rd;
    private int calls, success;

    public PropCondAllDiffAdaptative(IntVar[] variables, Condition condition, boolean fast) {
        super(variables, condition, fast);
        this.rd = new Random(vars[0].getModel().getSeed());
        this.calls = 1;
        this.success = 1;
    }

    public void propagate(int evtmask) throws ContradictionException {
        IntVar[] vars = filterVariables();
        if (vars.length == 0) {
            return;
        }
        double p = (success * 1.d) / (calls * 1.d);
        if (rd.nextFloat() < p) {
            boolean rem = true;
            try {
                if (fast) {
                    AlgoAllDiffACFast filter = new AlgoAllDiffACFast(vars, this);
                    rem = filter.propagate();
                } else {
                    AlgoAllDiffAC filter = new AlgoAllDiffAC(vars, this);
                    rem = filter.propagate();
                }
            } finally {
                calls++;
                if (rem) {
                    success++;
                }
            }
        }
    }
}
