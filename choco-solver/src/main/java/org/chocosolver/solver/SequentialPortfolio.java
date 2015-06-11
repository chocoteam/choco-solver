/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.limits.ACounter;
import org.chocosolver.solver.search.limits.TimeCounter;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

/**
 * A portfolio of {@code Solver} enabling solving one problem with various strategies: a time limit is allocated
 * to each of the solvers to solve the problem, in turn (kind of a "relay race" for a solvers team).
 * Each solver searches for a solution in a given time limit then another one search...
 * <p>
 * Created by cprudhom on 10/06/15.
 * Project: choco.
 */
public class SequentialPortfolio extends Portfolio {

    /**
     * The relay limit
     */
    long limit;

    /**
     * For nextsolution() only, store whether or not findSolution() has initialized all workers.
     */
    boolean early_found;

    /**
     * index of the running working
     */
    int ridx;

    /**
     * Array of counters, one for each worker
     */
    ACounter[] counters;

    public SequentialPortfolio(String name, int nbworkers, long limit) {
        super(name, nbworkers);
        this.limit = limit;
        counters = new ACounter[nbworkers];
        for (int i = nbworkers - 1; i >= 0; i--) {
            final int finalI = i;
            counters[i] = new TimeCounter(workers[i], limit);
//            counters[i] = new NodeCounter(limit); // for tests only
            counters[i].setAction(() -> {
                ridx = finalI;
                workers[finalI].getSearchLoop().interrupt("SequentialPortfolio orders to interrupt", false);
            });
            workers[i].plugMonitor(counters[i]);
        }
        this.new_solutions = new long[nbworkers];
    }

    @Override
    public IMeasures getMeasures() {
        throw new SolverException("SequentialPortfolio does not yet enable to get global measures.");
    }

    @Override
    public boolean findSolution() {
        if (needCopy()) {
            carbonCopy();
        }
        if (!skip_strategy_configuration) {
            setStrategies(ResolutionPolicy.SATISFACTION);
        }
        Arrays.fill(new_solutions, 0);
        early_found = false;
        for (int i = 0; !early_found && i < nbworkers; i++) {
            ridx = i;
            workers[i].solve(true);
            if (workers[ridx].getMeasures().getSolutionCount() != new_solutions[ridx]) {
                early_found = true;
            }
        }
        boolean run = !early_found;
        while (run) {
            ridx = (ridx + 1) % nbworkers;

            // manually reset the worker
            workers[ridx].getSearchLoop().forceAlive(true);
            // update the limit
            counters[ridx].reset();
            // resume the search
            workers[ridx].getSearchLoop().resume();
            // keep on running until no solution is found
            run = workers[ridx].getMeasures().getSolutionCount() == new_solutions[ridx]
                    && !workers[ridx].getSearchLoop().isComplete();
        }
        long nsols = countNewSolutions();
        return nsols > 0;
    }

    @Override
    public boolean nextSolution() {
        if (needCopy()) {
            throw new SolverException("Calling this method required all workers to be populated (see Portfolio.carbonCopy()).");
        }
        if (early_found) {
            // some solvers have not been initialized
            early_found = false;
            // so, keep on initializing them
            for (int i = ridx + 1; !early_found && i < nbworkers; i++) {
                ridx = i;
                workers[i].solve(true);
                if (workers[ridx].getMeasures().getSolutionCount() != new_solutions[ridx]) {
                    early_found = true;
                }
            }
        }
        boolean run = !early_found;
        while (run) {
            ridx = (ridx + 1) % nbworkers;
            // manually reset the worker
            workers[ridx].getSearchLoop().forceAlive(true);
            // update the limit
            counters[ridx].reset();
            // resume the search
            workers[ridx].getSearchLoop().resume();
            // keep on running until no solution is found
            run = workers[ridx].getMeasures().getSolutionCount() == new_solutions[ridx]
                    && !workers[ridx].getSearchLoop().isComplete();

        }
        long nsols = countNewSolutions();
        return nsols > 0;
    }


    @Override
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        // TODO remove SyncObjective from SM list of each solvers
        if (policy == ResolutionPolicy.SATISFACTION) {
            throw new SolverException("SequentialPortfolio.findOptimalSolution(...) cannot be called with ResolutionPolicy.SATISFACTION.");
        }
        if (objective == null) {
            throw new SolverException("No objective variable has been defined");
        }
        if (needCopy()) {
            carbonCopy();
        }
        if (!skip_strategy_configuration) {
            setStrategies(policy);
        }
        for (int w = 0; w < nbworkers; w++) {
            workers[w].set(new ObjectiveManager<IntVar, Integer>(w == 0 ? objective : retrieveVarIn(w, objective), policy, true));
            workers[w].plugMonitor(new SyncObjective(this, w, policy));
        }
        for (int i = 0; i < nbworkers; i++) {
            ridx = i;
            workers[i].solve(false);
            if (workers[ridx].getSearchLoop().isComplete()) {
                early_found = true;
            }
        }
        boolean run = !early_found;
        while (run) {
            ridx = (ridx + 1) % nbworkers;
            // manually reset the worker
            workers[ridx].getSearchLoop().forceAlive(true);
            // update the limit
            counters[ridx].reset();
            // resume the search
            workers[ridx].getSearchLoop().resume();
            // keep on running until no solution is found
            run = !workers[ridx].getSearchLoop().isComplete();
        }
        restoreSolution(objective, policy);
    }
}
