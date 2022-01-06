/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.solver.variables.SetVar;

/**
 * Selects the variables maximising envelopeSize-kernelSize.
 *
 * @author Jean-Guillaume Fages
 * @since 6/10/13
 */
public class MaxDelta implements VariableSelector<SetVar>,VariableEvaluator<SetVar> {
    @Override
    public SetVar getVariable(SetVar[] variables) {
        int small_idx = -1;
        int delta = 0;
        for (int idx = 0; idx < variables.length; idx++) {
            SetVar variable = variables[idx];
            int d = variable.getUB().size() - variable.getLB().size();
            if (d > delta) {
                delta = d;
                small_idx = idx;
            }
        }
        return small_idx > -1 ? variables[small_idx] : null;
    }

    @Override
    public double evaluate(SetVar variable) {
        return -variable.getUB().size() - variable.getLB().size();
    }
}
