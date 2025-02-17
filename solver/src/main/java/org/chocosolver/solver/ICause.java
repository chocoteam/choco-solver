/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;


import org.chocosolver.sat.Reason;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This interface describes services of smallest element which can act on variables.
 * As an example, propagator is a cause because it filters values from variable domain.
 * So do decision, objective manager, etc.
 * It has an impact on domain variables and so it can fail.
 *
 * @author Charles Prud'homme
 * @since 26 oct. 2010
 */
public interface ICause {
    /**
     * Apply an <i>action</i> on each variable declared on the scope of this cause, if any.
     *
     * @param action action to perform on each variable declared in this cause.
     */
    default void forEachIntVar(Consumer<IntVar> action) {
        throw new SolverException("Undefined forEachIntVar(...) method for " + this.getClass().getSimpleName());
    }

    /**
     * Return the reason why the cause is responsible for the variable modification.
     *
     * @param pivot the modified variable
     * @return the reason why the cause is responsible for the variable modification.
     * @implSpec by default, return {@link Reason#undef()}
     */
    default Reason defaultReason(Variable pivot) {
        return Reason.undef();
    }

    /**
     * Manage reification when it matters
     */
    default Function<Reason, Reason> manageReification() {
        return r -> r;
    }

}
