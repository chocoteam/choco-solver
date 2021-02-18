/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.objective;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;

import java.util.function.Function;

/**
 * interface to monitor the bounds of the objective variable.
 *
 * @param <V> type of objective variable
 * @author Jean-Guillaume Fages, Charles Prud'homme, Arnaud Malapert
 */
public interface IObjectiveManager<V extends Variable> extends IBoundsManager, ICause {

    /**
     * @return the objective variable
     */
    V getObjective();

    /**
     * Informs the manager that a new solution has been found
     */
    boolean updateBestSolution(Number n);

    /**
     * Informs the manager that a new solution has been found
     */
    boolean updateBestSolution();

    /**
     * Set a user-defined cut computer to avoid "worse" solutions
     */
    void setCutComputer(Function<Number, Number> cutComputer);

    /**
     * Define a strict cut computer where in the next solution to find should be strictly greater (resp. lesser) than
     * the best solution found so far when maximizing (resp. minimizing) a problem.
     */
    void setStrictDynamicCut();

    /**
     * Define a <i>walking</i> cut computer where in the next solution to find should be greater than (resp. less than)
     * or equal to the best solution found so far when maximizing (resp. minimizing) a problem.
     */
    void setWalkingDynamicCut();

    /**
     * Prevent the model from computing worse quality solutions
     *
     * @throws org.chocosolver.solver.exception.ContradictionException if posting this cut fails
     */
    void postDynamicCut() throws ContradictionException;
}