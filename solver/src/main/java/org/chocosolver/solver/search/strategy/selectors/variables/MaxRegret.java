/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.variables;

import org.chocosolver.solver.variables.IntVar;

/**
 * <b>Max regret</b> variable selector.
 * It chooses the variable with the largest difference between the two smallest values in its domain
 * (instantiated variables are ignored).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class MaxRegret implements VariableSelector<IntVar>,VariableEvaluator<IntVar> {

    @Override
    public IntVar getVariable(IntVar[] variables) {
        int small_idx = -1;
        int small_value = Integer.MIN_VALUE;
        for (int idx = 0; idx < variables.length; idx++) {
            int dsize = variables[idx].getDomainSize();
            int lower = variables[idx].getLB();
            int value = variables[idx].nextValue(lower) - lower;
            if (dsize > 1 && value > small_value) {
                small_value = value;
                small_idx = idx;
            }
        }
        return small_idx > -1 ? variables[small_idx] : null;
    }

    @Override
    public double evaluate(IntVar variable) {
        int lower = variable.getLB();
        return -(variable.nextValue(lower) - lower);
    }
}
