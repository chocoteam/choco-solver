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

import org.chocosolver.solver.variables.Variable;

/**
 * <b>Occurrence</b> variable selector.
 * It chooses the variable with the largest number of attached propagators (instantiated variables are ignored).
 * <br/>
 * TODO: could be based on the number of not entailed propagators
 *
 * @author Charles Prud'homme
 * @since 2 juil. 2010
 */
public class Occurrence<V extends Variable> implements VariableSelector<V>,VariableEvaluator<V> {

    @Override
    public V getVariable(V[] variables) {
        int large_idx = -1;
        int large_nb_cstrs = Integer.MIN_VALUE;
        for (int idx = 0; idx < variables.length; idx++) {
            int nb_cstrs = variables[idx].getNbProps();
            if (!variables[idx].isInstantiated() && nb_cstrs > large_nb_cstrs) {
                large_nb_cstrs = nb_cstrs;
                large_idx = idx;
            }
        }
        return large_idx > -1 ? variables[large_idx] : null;
    }

    @Override
    public double evaluate(V variable) {
        return -(variable.getNbProps());
    }
}
