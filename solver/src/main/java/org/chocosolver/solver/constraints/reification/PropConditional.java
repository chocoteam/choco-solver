/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A specific propagator which temporarily posts constraint on condition.
 * <br/>
 * The user declares a scope of variables to observe,
 * and a {@link BooleanSupplier} as a condition checker.
 * When the condition holds, a {@link Supplier<Constraint[]>} is called to
 * create <i>on-the-fly</i> the set of constraints to post.
 * These constraints will automatically be removed on backtrack.
 *
 * @author Charles Prud'homme
 * @since 31/08/2020
 */
public class PropConditional extends Propagator<Variable> {

    private final Function<Variable[], Boolean> checker;

    private final Supplier<Constraint[]> constraints;

    /**
     * Declare a conditionnal propagator.
     *
     * @param vars2observe Set of variables to observe, their modifications triggers the condition checking
     * @param checker      Check a specific condition anytime a variable in scope is modified.
     * @param constraints  Provide constraints to be added temporarily to the model.
     */
    public PropConditional(Variable[] vars2observe,
                           Function<Variable[], Boolean> checker,
                           Supplier<Constraint[]> constraints) {
        super(vars2observe, PropagatorPriority.VERY_SLOW, false);
        this.checker = checker;
        this.constraints = constraints;
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (checker.apply(this.vars)) {
            setPassive();
            model.postTemp(constraints.get());
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }


    /**
     * Returns {@code true} when all variables are instantiated.
     */
    public static final Function<Variable[], Boolean> ALL_INSTANTIATED =
            (vars) -> Arrays.stream(vars).allMatch(Variable::isInstantiated);
    /**
     * Returns {@code true} when at least one variable is instantiated.
     */
    public static final Function<Variable[], Boolean> ONE_INSTANTIATED =
            (vars) -> Arrays.stream(vars).anyMatch(Variable::isInstantiated);
}
