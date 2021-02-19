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
 * <b>Largest</b> variable selector.
 * It chooses the variable with the largest value in its domain (instantiated variables are ignored).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class Largest implements VariableSelector<IntVar>,VariableEvaluator<IntVar>{


    @Override
    public IntVar getVariable(IntVar[] variables) {
        int large_idx = -1;
        int large_value = Integer.MIN_VALUE;
        for (int idx = 0; idx < variables.length; idx++) {
            int dsize = variables[idx].getDomainSize();
            int upper = variables[idx].getUB();
            if (dsize > 1 && upper > large_value) {
                large_value = upper;
                large_idx = idx;
            }
        }
        return large_idx > -1 ? variables[large_idx] : null;
    }

    @Override
    public double evaluate(IntVar variable) {
        return -variable.getUB();
    }
}
