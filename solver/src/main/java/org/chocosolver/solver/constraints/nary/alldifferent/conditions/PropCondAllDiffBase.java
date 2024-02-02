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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

/**
 * Base class for conditional AllDifferent propagators.
 *
 * @author Dimitri Justeau-Allaire.
 */
public abstract class PropCondAllDiffBase extends Propagator<IntVar> {

    protected final Condition condition;

    public PropCondAllDiffBase(IntVar[] variables, Condition condition, PropagatorPriority priority) {
        super(variables, priority, false);
        this.condition = condition;
    }

    /**
     * @return The variables respecting the condition.
     */
    protected IntVar[] filterVariables() {
        return Arrays.stream(vars).filter(v -> condition.holdOnVar(v)).toArray(IntVar[]::new);
    }
}
