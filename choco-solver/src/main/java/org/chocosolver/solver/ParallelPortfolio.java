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
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * A portfolio of {@code Solver} enabling solving one problem with various strategies:
 * the solvers solve the same problem in parallel, each one attached to one single thread.
 * <p>
 * Created by cprudhom on 02/06/15.
 * Project: choco.
 */
public class ParallelPortfolio extends Portfolio {

    /**
     * A synchronisation aid
     */
    CyclicBarrier barrier;

    @SuppressWarnings("unchecked")
    public ParallelPortfolio(String name, int nthreads) {
        super(name, nthreads);
        this.barrier = new CyclicBarrier(nthreads + 1);
    }


    @Override
    public IMeasures getMeasures() {
        throw new SolverException("Solver Portfolio does not yet enable to get global measures.");
    }

    @Override
    public boolean findSolution() {
        // TODO: deal with same solutions
        if (needCopy()) {
            carbonCopy();
        }
        if (!skip_strategy_configuration) {
            setStrategies(ResolutionPolicy.SATISFACTION);
        }
        Arrays.fill(new_solutions, 0);
        initWorkers();
        for (int s = 0; s < nbworkers; s++) {
            int _s = s;
            Thread r = new Thread() {
                @Override
                public void run() {
                    workers[_s].getSearchLoop().launch(true);
                    stopAll();
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            };
            r.setName(workers[s].getName());
            r.start();
        }
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        long nsols = countNewSolutions();
        return nsols > 0;
    }

    /**
     * Look for a new solution, if any.
     * Beware, some search strategies allow finding the same solutions more than once.
     * In consequence, calling this method in while-loop from a Portfolio may lead to infinite loop.
     *
     * @return true if a new solution is found
     */
    @Override
    public boolean nextSolution() {
        // TODO: deal with same solutions
        if (needCopy()) {
            throw new SolverException("Calling this method required all workers to be populated (see Portfolio.carbonCopy()).");
        }
        barrier.reset();
        for (int s = 0; s < nbworkers; s++) {
            int _s = s;
            Thread r = new Thread() {
                @Override
                public void run() {
                    // If the search tree has not been fully run out..
                    // TODO: the test is not safe,
                    if (!workers[_s].getSearchLoop().isComplete()) {
                        workers[_s].getSearchLoop().forceAlive(true); // because last stop was strong
                        workers[_s].getSearchLoop().launch(true);
                        stopAll();
                    }
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            };
            r.setName(workers[s].getName());
            r.start();
        }
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        long nsols = countNewSolutions();
        return nsols > 0;
    }

    @Override
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        // TODO remove SyncObjective from SM list of each solvers
        if (policy == ResolutionPolicy.SATISFACTION) {
            throw new SolverException("Solver.findOptimalSolution(...) cannot be called with ResolutionPolicy.SATISFACTION.");
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
        for (int s = 0; s < nbworkers; s++) {
            workers[s].set(new ObjectiveManager<IntVar, Integer>(s == 0 ? objective : retrieveVarIn(s, objective), policy, true));
            workers[s].plugMonitor(new SyncObjective(this, s, policy));
        }
        initWorkers();
        for (int s = 0; s < nbworkers; s++) {
            int _s = s;
            Thread r = new Thread() {
                @Override
                public void run() {
                    workers[_s].getSearchLoop().launch(false);
                    stopAll();
                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            };
            r.setName(workers[s].getName());
            r.start();
        }
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        restoreSolution(objective, policy);
    }

    private void stopAll() {
        for (int i = 0; i < nbworkers; i++) {
            workers[i].getSearchLoop().interrupt("Portfolio orders to interrupt", false);
        }
    }

}
