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

import org.chocosolver.solver.variables.Variable;

/**
 * A cyclic variable selector :
 * Iterates over variables according to lexicographic ordering in a cyclic manner (loop back to the first variable)
 *
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/07/12
 */
public class Cyclic <V extends Variable> implements VariableSelector<V> {

    protected int current;

    public Cyclic() {
        this.current = -1;
    }

    @Override
    public V getVariable(V[] vars) {
        int nbvars = vars.length;
        int start = current == -1 ? nbvars - 1 : current;
        int n = (current + 1) % nbvars;
        while (n != start && vars[n].isInstantiated()) {
            n = (n + 1) % nbvars;
        }
        current = n;
        return vars[current].isInstantiated() ? null : vars[current];
    }
}
