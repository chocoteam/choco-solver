/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.constraints.reification.PropConditional;
import org.chocosolver.solver.variables.Variable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interface to make constraints over BoolVar, IntVar, RealVar and SetVar
 * <p>
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 */
public interface IConstraintFactory extends IIntConstraintFactory, IRealConstraintFactory, ISetConstraintFactory, IGraphConstraintFactory {

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
}
