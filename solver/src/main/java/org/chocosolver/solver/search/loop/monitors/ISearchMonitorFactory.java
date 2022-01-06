/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.*;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.tools.TimeUtils;

/**
 * Interface to define some search monitors to be used
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 */
public interface ISearchMonitorFactory extends ISelf<Solver> {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Record nogoods from solution, that is, anytime a solution is found, a nogood is produced to prevent from
     * finding the same solution later during the search.
     * <code>vars</code> are the decision variables (to reduce ng size).
     *
     * @param vars array of decision variables
     */
    default void setNoGoodRecordingFromSolutions(IntVar... vars) {
        ref().plugMonitor(new NogoodFromSolutions(vars));
    }

    /**
     * * Record nogoods from restart, that is, anytime the search restarts, a nogood is produced, based on the decision path, to prevent from
     * scanning the same sub-search tree.
     */
    default void setNoGoodRecordingFromRestarts() {
        ref().plugMonitor(new NogoodFromRestarts(ref().getModel()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Limit the exploration of the search space with the help of a <code>aStopCriterion</code>.
     * When the condition depicted in the criterion is met,
     * the search stops.
     *
     * @param aStopCriterion the stop criterion which, when met, stops the search.
     */
    default void limitSearch(Criterion aStopCriterion) {
        ref().addStopCriterion(aStopCriterion);
    }

    /**
     * Defines a limit on the number of nodes allowed in the tree search.
     * When the limit is reached, the resolution is stopped.
     * @param limit maximal number of nodes to open
     */
    default void limitNode(long limit) {
       limitSearch(new NodeCounter(ref().getModel(), limit));
    }

    /**
     * Defines a limit over the number of fails allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     * @param limit maximal number of fails
     */

    default void limitFail(long limit) {
        limitSearch(new FailCounter(ref().getModel(), limit));
    }

    /**
     * Defines a limit over the number of backtracks allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param limit maximal number of backtracks
     */
    default void limitBacktrack(long limit) {
        limitSearch(new BacktrackCounter(ref().getModel(), limit));
    }

    /**
     * Defines a limit over the number of solutions found during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     * @param limit maximal number of solutions
     */
    default void limitSolution(long limit) {
        limitSearch(new SolutionCounter(ref().getModel(), limit));
    }

    /**
     * Defines a limit over the number of restarts during the resolution.
     * When the limit is reached, the resolution is stopped.
     * @param limit maximal number of solutions
     */
    default void limitRestart(long limit) {
        limitSearch(new RestartCounter(ref().getModel(), limit));
    }


    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     * <br/>
     * <b>One must consider also {@code SearchMonitorFactory.limitThreadTime(long)}, that runs the limit in a separated thread.</b>
     * @param limit  maximal resolution time in millisecond
     */
    default void limitTime(long limit) {
        limitSearch(new TimeCounter(ref().getModel(), limit * TimeUtils.MILLISECONDS_IN_NANOSECONDS));
    }

    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     * <br/>
     * <br/>
     * <b>One must consider also {@code SearchMonitorFactory.limitThreadTime(String)}, that runs the limit in a separated thread.</b>
     * <p>
     * Based on {@code SearchMonitorFactory.convertInMilliseconds(String duration)}
     *
     * @param duration a String which states the duration like "WWd XXh YYm ZZs".
     * @see TimeUtils#convertInMilliseconds(String)
     */
    default void limitTime(String duration) {
        limitTime(TimeUtils.convertInMilliseconds(duration));
    }

    /**
     * Attach a solution recorder to this solver
     * @param solution solution object to be filled on solutions.
     */
    default void attach(Solution solution){
        ref().plugMonitor((IMonitorSolution) solution::record);
    }
}
