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
 * A variable evaluator. One provide a way to evaluate a variable (domain size, smallest values, ...).
 * It should return a value which can be minimized.
 * For instance, to select the integer variable with the smallest value in its domain, return ivar.getLB().
 * To select the variable with the largest value in its domain, return -ivar.getUB().
 * <p/>
 * Such evaluator can be called and combined with others to define a variable selector which enables tie breaking.
 * Indeed, many uninstantied variables may return the same value for the evaluation.
 * In that case, the next evaluator should break ties, otherwise the first computed variable would be returned.
 * <p/>
 * Be aware that using a single variable evaluator in {@code solver.search.strategy.selectors.VariableSelectorWithTies} may result
 * in a slower execution due to the generalisation it requires.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17/03/2014
 */
public interface VariableEvaluator<V extends Variable> {

    /**
     * Evaluates the heuristic that is <b>minimized</b> in order to find the best variable
     *
     * @param variable array of variable
     * @return the result of the evaluation, to minimize
     */
    double evaluate(V variable);
}
