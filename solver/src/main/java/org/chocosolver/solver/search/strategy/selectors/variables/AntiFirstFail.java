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

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

/**
 * <b>Anti first fail</b> variable selector.
 * It chooses the variable with the largest domain (instantiated variables are ignored).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class AntiFirstFail implements VariableSelector<IntVar>,VariableEvaluator<IntVar> {

    private IStateInt lastIdx; // index of the last non-instantiated variable

    /**
     * <b>First fail</b> variable selector.
     * @param model reference to the model (does not define the variable scope)
     */
    public AntiFirstFail(Model model){
        lastIdx = model.getEnvironment().makeInt(0);
    }

    @Override
    public IntVar getVariable(IntVar[] variables) {
        int large_idx = -1;
        int large_dsize = Integer.MIN_VALUE;
        boolean got = false;
        for (int idx = lastIdx.get(); idx < variables.length; idx++) {
            int dsize = variables[idx].getDomainSize();

            if (!got && !variables[idx].isInstantiated()) {
                //got is just to call 'set' at most once
                lastIdx.set(idx);
                got = true;
            }

            if (dsize > 1 && dsize > large_dsize) {
                large_dsize = dsize;
                large_idx = idx;
            }
        }
        return large_idx > -1 ? variables[large_idx] : null;
    }

    @Override
    public double evaluate(IntVar variable) {
        return -variable.getDomainSize();
    }
}
