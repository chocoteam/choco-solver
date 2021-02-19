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
import org.chocosolver.solver.variables.Variable;

/**
 * <b>Input order</b> variable selector.
 * It chooses variables in order they appears (instantiated variables are ignored).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class InputOrder<V extends Variable> implements VariableSelector<V> {

    private IStateInt lastIdx; // index of the last non-instantiated variable

	/**
     * <b>Input order</b> variable selector.
     * It chooses variables in order they appears (instantiated variables are ignored).
     * @param model reference to the model (does not define the variable scope)
     */
    public InputOrder(Model model){
        lastIdx = model.getEnvironment().makeInt(0);
    }

    @Override
    public V getVariable(V[] variables) {
        for (int idx = lastIdx.get(); idx < variables.length; idx++) {
            if (!variables[idx].isInstantiated()) {
                lastIdx.set(idx);
                return variables[idx];
            }
        }
        lastIdx.set(variables.length);
        return null;
    }
}
