/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.restart;

import org.chocosolver.solver.Solver;

import java.util.function.Consumer;

/**
 * Restart the search before a cut is applied.
 * <p>
 * This class is used to restart the search before a cut is applied.
 * It is used to properly handle the cut and the restart.
 * This class is mandatory when LCG is plugged with Parallel Portfolio.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/10/2024
 */
public class ForceRestartBeforeCut extends AbstractRestart {

    /**
     * Indicate if the search must restart
     */
    private boolean mustRestart;
    /**
     * The value to cut
     */
    private Number value;
    /**
     * The cut to apply before restarting
     */
    private final Consumer<Number> cut;

    /**
     * Create a restart strategy which restart the search before a cut is applied.
     * @param solver the solver to restart
     */
    public ForceRestartBeforeCut(Solver solver) {
        cut = newValue -> {
            solver.getObjectiveManager().updateBestSolution(newValue);
        };
        value = solver.getObjectiveManager().getBestSolutionValue();
        mustRestart = false;
    }

    @Override
    public void init() {
        this.next.init();
    }

    /**
     * Store the cut to apply before restarting
     *
     * @param n value to cut
     */
    public synchronized void storeCut(Number n) {
        value = n;
        mustRestart = true;
    }


    /**
     * @implSpec The search is restarted after a new cut is stored.
     */
    @Override
    public boolean mustRestart(Solver solver) {
        if (mustRestart) {
            cut.accept(value);
            mustRestart = false;
            return true;
        }
        return next.mustRestart(solver);
    }
}
