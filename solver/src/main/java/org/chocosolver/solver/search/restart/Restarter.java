/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import org.chocosolver.solver.Solver;
import org.chocosolver.util.criteria.LongCriterion;

/**
 * This enables restarting a search on certain conditions
 * (most of the time based on a counter).
 * <p>
 * Created by cprudhom on 03/09/15.
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 03/09/2015
 */
public final class Restarter extends AbstractRestart {

    /**
     * How often the restart should occur
     */
    private final ICutoff restartStrategy;
    /**
     * How to trigger a restart
     */
    private final LongCriterion criterion;
    /**
     * Count the number of restarts
     */
    private int restartFromStrategyCount;
    /**
     * restrict the total number of restart
     */
    private final int restartLimit;
    /**
     * When the next restart should be triggered
     */
    private long limit;
    /**
     * Number of solutions found so far
     */
    private long solutions;

    private final boolean resetCutoffOnSolution;

    private AbstractRestart next = AbstractRestart.NO_RESTART;

    /**
     * @param restartStrategy       defines when restarts happen
     * @param criterion             defines how to trigger a restart
     * @param restartLimit          restrict the total number of restart
     * @param resetCutoffOnSolution reset cutoff sequence on solutions
     */
    public Restarter(ICutoff restartStrategy,
                     LongCriterion criterion,
                     int restartLimit,
                     boolean resetCutoffOnSolution) {
        this.restartStrategy = restartStrategy;
        this.criterion = criterion;
        this.restartLimit = restartLimit;
        this.resetCutoffOnSolution = resetCutoffOnSolution;
    }

    @Override
    public void init() {
        restartFromStrategyCount = 0;
        limit = restartStrategy.getNextCutoff();
        this.next.init();
    }

    @Override
    public void setNext(AbstractRestart restart) {
        this.next = restart;
    }

    @Override
    public AbstractRestart getNext() {
        return this.next;
    }

    /**
     * Check conditions for restarting
     *
     * @param solver the caller
     * @return true if restarting is required
     */
    @Override
    public boolean mustRestart(Solver solver) {
        if (resetCutoffOnSolution && solutions < solver.getSolutionCount()) {
            solutions = solver.getSolutionCount();
            restartStrategy.reset();
        }
        if (criterion.isMet(limit)) {
            // update parameters for restarts
            restartFromStrategyCount++;
            if (restartFromStrategyCount >= restartLimit) {
                limit = Long.MAX_VALUE;
            } else if (criterion.isMet(limit)) {
                limit += restartStrategy.getNextCutoff();
            }
            return true;
        }
        return next.mustRestart(solver);
    }
}
