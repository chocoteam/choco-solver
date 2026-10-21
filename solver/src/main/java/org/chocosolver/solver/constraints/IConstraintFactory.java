/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.constraints.reification.PropConditional;
import org.chocosolver.solver.variables.Variable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface to make constraints over BoolVar, IntVar, RealVar and SetVar
 * <p>
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 */
public interface IConstraintFactory extends IIntConstraintFactory, IRealConstraintFactory, ISchedulingFactory, ISetConstraintFactory, IGraphConstraintFactory {

    /**
     * Create a constraint that acts as an observer.
     * and anytime a variable from {@code scope} is modified,
     * the condition defined by {@code checker} is checked.
     * If the condition holds, then the constraints provided by {@code provider} are posted in the current sub-tree
     * (i.e., they are removed upon backtrack).
     *
     * @param scope    set of variables on the modifications of which the condition in {@code checker} is verified
     * @param checker  condition checker
     * @param provider constraints provider
     * @return a conditional constraint. Can supply {@code null}.
     * @implNote the constraint does not filter values from variables in {@code scope}, it just acts as an observer.
     * @see PropConditional#ALL_INSTANTIATED
     * @see PropConditional#ONE_INSTANTIATED
     */
    default Constraint conditional(
            Variable[] scope,
            Function<Variable[], Boolean> checker,
            Supplier<Constraint[]> provider) {
        return new Constraint(
                ConstraintsName.CONDITION,
                new PropConditional(scope, checker, provider)
        );
    }

    /**
     * Create a custom constraint, made of the propagator built by {@code propagator} on {@code vars}.
     * <p>
     * Prefer this factory to {@code new Constraint(name, propagator)}: a model created with
     * {@link org.chocosolver.solver.Model#record()} can then be duplicated, provided that {@code propagator} captures
     * nothing (e.g., a constructor reference such as {@code PropFoo::new}), since it is shared by all the copies.
     *
     * @param name       name of the constraint
     * @param vars       variables of the constraint
     * @param propagator builds the propagator from the variables; must capture nothing
     * @param <V>        type of the variables
     * @return a custom constraint
     */
    default <V extends Variable> Constraint custom(String name, V[] vars, Function<V[], Propagator<?>> propagator) {
        return new Constraint(name, propagator.apply(vars));
    }

    /**
     * Create a custom constraint, made of the propagator built by {@code propagator} on {@code vars} and
     * {@code data}, e.g. {@code custom("atMostK", x, 3, PropAtMostK::new)}.
     * <p>
     * Prefer this factory to {@code new Constraint(name, propagator)}: a model created with
     * {@link org.chocosolver.solver.Model#record()} can then be duplicated, provided that {@code propagator} captures
     * nothing (e.g., a constructor reference such as {@code PropFoo::new}), since it is shared by all the copies.
     * What the propagator needs, besides the variables, is passed in {@code data}, which is journalized as any
     * constant argument: immutable values and arrays of them, tuples, or a {@code record} of such values (several
     * parameters). An array of variables is mapped to the variables of each copy.
     *
     * @param name       name of the constraint
     * @param vars       variables of the constraint
     * @param data       the other parameters of the propagator
     * @param propagator builds the propagator from the variables and the data; must capture nothing
     * @param <V>        type of the variables
     * @param <D>        type of the data
     * @return a custom constraint
     */
    default <V extends Variable, D> Constraint custom(String name, V[] vars, D data,
                                                     BiFunction<V[], D, Propagator<?>> propagator) {
        return new Constraint(name, propagator.apply(vars, data));
    }
}
