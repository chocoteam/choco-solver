/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;


import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.ValueSortedMap;

import java.util.function.Consumer;

/**
 * This interface describes services of smallest element which can act on variables.
 * As an example, propagator is a cause because it filters values from variable domain.
 * So do decision, objective manager, etc.
 * It has an impact on domain variables and so it can fails.
 *
 * @author Charles Prud'homme
 * @since 26 oct. 2010
 */
public interface ICause  {

    /**
     * Clausal explanation for this cause.
     * <p>
     *     This method must filled <i>explanations</i> with inferred literals.
     *     These literals are inferred from the analysis of
     *     (a subset of) conflicting nodes stored in <i>front</i>,
     *     the implication graph <i>ig</i> and the current node in conflict,
     *     not yet contained in <i>front</i>.
     * </p>
     * <p>
     *     Optionally, this method can update <i>front</i> by looking for a predecessor of any node
     *     that seems more relevant than the declared one.
     * </p>
     * @param explanation explanation to compute
     * @param front ordered map of (variable,node> in the conflict frontier of the implication graph.
     * @param implicationGraph an implication graph
     * @param pivot the pivot node out of <i>front</i>
     */
    default void explain(ExplanationForSignedClause explanation,
                         ValueSortedMap<IntVar> front,
                         Implications implicationGraph,
                         int pivot){
        throw new SolverException("Undefined explain(...) method for " + this.getClass().getSimpleName());
    }

    /**
     * Apply an <i>action</i> on each variable declared on the scope of this cause, if any.
     * @param action action to perform on each variable declared in this cause.
     */
    default void forEachIntVar(Consumer<IntVar> action){
        throw new SolverException("Undefined forEachIntVar(...) method for " + this.getClass().getSimpleName());
    }

}
