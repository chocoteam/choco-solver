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

import gnu.trove.map.hash.THashMap;
import org.chocosolver.memory.Environments;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.solution.LastSolutionRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * A portfolio of {@code Solver} enabling solving one problem with various strategies, each one attached to one single thread.
 * <p>
 * By default, the first solver is the front end, wherein model is declared.
 * <p>
 * Created by cprudhom on 02/06/15.
 * Project: choco.
 */
public class Portfolio implements Serializable, ISolver {

    private static final int FRONTEND = 0;

    private int nvars, ncstrs;
    /**
     * Number of threads used, ie, number of workers
     */
    int nthreads;

    /**
     * The workers/solvers.
     */
    final Solver[] workers;

    /**
     * The identity maps
     */
    final THashMap<Object, Object>[] imaps;

    /**
     * A synchronisation aid
     */
    CyclicBarrier barrier;

    /**
     * Count the number of new solutions -- for internal purpose only
     */
    private long new_solutions[];

    private boolean skip_conformity = false;

    @SuppressWarnings("unchecked")
    public Portfolio(String name, int nthreads) {
        if (nthreads < 2) {
            throw new SolverException(String.format("Please consider creating a Solver instead of a Portfolio since %d solver are required.", nthreads));
        }
        this.nthreads = nthreads;
        this.workers = new Solver[nthreads];
        this.imaps = new THashMap[nthreads];
        for (int i = 0; i < nthreads; i++) {
            this.workers[i] = SolverFactory.makeSolver(Environments.DEFAULT.make(), name + "_" + i);
            this.imaps[i] = new THashMap<>();
        }
        this.barrier = new CyclicBarrier(nthreads + 1);
        this.new_solutions = new long[nthreads];
        this.nvars = 0;
        this.ncstrs = 0;
    }

    @Override
    public void associates(Variable variable) {
        _fes_().associates(variable);
    }

    @Override
    public void post(Constraint c) {
        _fes_().post(c);
    }

    @Override
    public void post(Constraint... cs) {
        for (int i = 0; i < cs.length; i++) {
            this.post(cs[i]);
        }
    }

    private boolean needCopy() {
        return skip_conformity
                || nvars != _fes_().vIdx
                || ncstrs != _fes_().cIdx
                || _fes_().minisat != null && workers[1].minisat == null
                || _fes_().minisat != null && workers[1].minisat != null &&
                (_fes_().minisat.getSatSolver().numvars() != workers[1].minisat.getSatSolver().numvars()
                        || _fes_().minisat.getSatSolver().nbclauses() != workers[1].minisat.getSatSolver().nbclauses());
    }

    /**
     * Make a carbon copy of the front end solver to the other ones.
     */
    public void carbonCopy() {
        // TODO: Ibex may have received new constraints or NogoodStore
        if (needCopy()) {
            if (_fes_().getEnvironment().getWorldIndex() > 0) {
                throw new SolverException("Duplicating a solver cannot be achieved once the resolution has begun.");
            }
            for (int n = 1; n < nthreads; n++) {
                Solver worker = workers[n];
                THashMap<Object, Object> imap = imaps[n];
                // duplicate variables
                if (nvars != _fes_().vIdx) {
                    for (int i = 0; i < _fes_().vIdx; i++) {
                        _fes_().vars[i].duplicate(worker, imap);
                    }
                }
                // duplicate constraints
                if (ncstrs != _fes_().cIdx) {
                    for (int i = 0; i < _fes_().cIdx; i++) {
                        _fes_().cstrs[i].duplicate(worker, imap);
                        //TODO How to deal with temporary constraints ?
                        worker.post((Constraint) imap.get(_fes_().cstrs[i]));
                    }
                }
                if (_fes_().minisat != null && workers[1].minisat != null
                        && (_fes_().minisat.getSatSolver().numvars() != workers[n].minisat.getSatSolver().numvars()
                        || _fes_().minisat.getSatSolver().nbclauses() != workers[n].minisat.getSatSolver().nbclauses())) {
                    workers[n].minisat.getSatSolver().copyFrom(_fes_().minisat.getSatSolver());
                }
            }
            nvars = _fes_().vIdx;
            ncstrs = _fes_().cIdx;
        }
    }

    private long countNewSolutions() {
        long nsol = 0;
        for (int i = 0; i < nthreads; i++) {
            new_solutions[i] = workers[i].getMeasures().getSolutionCount() - new_solutions[i];
            nsol += new_solutions[i];
        }
        return nsol;
    }

    /**
     * Check feasibility of all workers:
     * returns {@link ESat#FALSE} if one at least is false,
     * otherwise return {@link ESat#TRUE} if at least one is true and the others are undefined,
     * otherwise, return {@link ESat#UNDEFINED}
     */
    @Override
    public ESat isFeasible() {
        if (needCopy()) {
            carbonCopy();
        }
        ESat feas = _fes_().isFeasible();
        for (int i = 1; i < nthreads; i++) {
            switch (workers[i].isFeasible()) {
                case TRUE:
                    feas = ESat.TRUE;
                    break;
                case FALSE:
                    return ESat.FALSE;
                default:
                    break;
            }
        }
        return feas;
    }

    /**
     * Indicates whether or not all worker-solvers have reached a limit.
     *
     * @return true if all worker-solvers have reached a limit
     */
    @Override
    public boolean hasReachedLimit() {
        if (needCopy()) {
            carbonCopy();
        }
        boolean lim = true;
        for (int i = 0; i < nthreads; i++) {
            lim &= workers[i].hasReachedLimit();
        }
        return lim;
    }

    @Override
    public boolean findSolution() {
        // TODO: deal with distinct search strategies, if none is declared
        // TODO: deal with same solutions
        if (needCopy()) {
            carbonCopy();
        }
        Arrays.fill(new_solutions, 0);
        for (int s = 0; s < nthreads; s++) {
            int _s = s;
            Thread r = new Thread() {
                @Override
                public void run() {
                    workers[_s].solve(true);
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

    @Override
    public boolean nextSolution() {
        // TODO: deal with distinct search strategies, if none is declared
        // TODO: deal with same solutions
        if (needCopy()) {
            throw new SolverException("Calling this method required all workers to be populated (see Portfolio.carbonCopy()).");
        }
        barrier.reset();
        for (int s = 0; s < nthreads; s++) {
            int _s = s;
            Thread r = new Thread() {
                @Override
                public void run() {
                    // If the search tree has not been fully run out..
                    if (workers[_s].getSearchLoop().getCurrentDepth() < workers[_s].getEnvironment().getWorldIndex()) {
                        workers[_s].getSearchLoop().forceAlive(true); // beacuse last stop was strong
                        workers[_s].getSearchLoop().resume();
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
    public long findAllSolutions() {
        // TODO: deal with distinct search strategies, if none is declared
        // TODO: deal with same solutions
        if (needCopy()) {
            carbonCopy();
        }
        Arrays.fill(new_solutions, 0);
        for (int s = 0; s < nthreads; s++) {
            int _s = s;
            Thread r = new Thread() {
                @Override
                public void run() {
                    workers[_s].solve(false);
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
        // TODO merge all solutions into _fes_()
        long nsols = countNewSolutions();
        return nsols;
    }

    @Override
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        // TODO: deal with distinct search strategies, if none is declared
        // TODO: deal with same solutions
        if (policy == ResolutionPolicy.SATISFACTION) {
            throw new SolverException("Solver.findOptimalSolution(...) cannot be called with ResolutionPolicy.SATISFACTION.");
        }
        if (objective == null) {
            throw new SolverException("No objective variable has been defined");
        }
        if (needCopy()) {
            carbonCopy();
        }
        Portfolio _me = this;

        for (int s = 0; s < nthreads; s++) {
            int _s = s;
            Thread r = new Thread() {
                @Override
                public void run() {
                    if (!workers[_s].getObjectiveManager().isOptimization()) {
                        workers[_s].set(new ObjectiveManager<IntVar, Integer>(_s == 0 ? objective : retrieveVarIn(_s, objective), policy, true));
                    }
                    workers[_s].set(new LastSolutionRecorder(new Solution(), true, workers[_s]));
                    workers[_s].plugMonitor(new SyncObjective(_me, _s, policy));
                    workers[_s].solve(false);

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
        // TODO remove SyncObjective from SM list of each solvers
        // TODO restore the best solution into _fes_()
    }

    @Override
    public Solver _fes_() {
        return workers[FRONTEND];
    }

    /**
     * Return the solvers used in the portfolio.
     */
    public Solver[] getWorkers() {
        return workers;
    }

    /**
     * Return widx^th the worker-solver
     *
     * @param widx index of the worker in the portfolio
     * @return a worker solver
     */
    public Solver getWorker(int widx) {
        if (widx < 0 || widx > nthreads) {
            throw new SolverException(String.format("Worker-solver index %d not in range [0,%d].", widx, nthreads));
        }
        return workers[widx];
    }

    /**
     * Return the number of workers
     *
     * @return
     */
    public int getNbWorkers() {
        return nthreads;
    }

    private void stopAll() {
        for (int i = 0; i < nthreads; i++) {
            workers[i].getSearchLoop().interrupt("Portfolio orders to interrupt");
        }
    }

    @SuppressWarnings("unchecked")
    public <V extends Variable> V retrieveVarIn(int i, V var) {
        if (i == FRONTEND) {
            return var;
        }
        return (V) imaps[i].get(var);
    }

    @SuppressWarnings("unchecked")
    public <V extends Variable> V[] retrieveVarIn(int i, V... vars) {
        if (i == FRONTEND) {
            return vars;
        }
        V[] _vars = vars.clone();
        for (int j = 0; j < vars.length; j++) {
            _vars[j] = (V) imaps[i].get(vars[j]);
        }
        return _vars;
    }

    /**
     * Enable to skip the conformity check, that is, that all solvers are carbon copies of the first one.
     * The default value of <code>sc</code> is <code>false</code>, setting it to <code>true</code> will skip the conformity checks.
     * Skipping conformity checks may be interesting when various models of the same problem are evaluated altogether.
     * Or if the duplication has already been handled outside.
     *
     * @param sc set to true to skip the conformity checks.
     */
    public void skipConformity(boolean sc) {
        this.skip_conformity = sc;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class SyncObjective implements IMonitorSolution {

        Portfolio prtfl; // Portfolio
        int widx; // worker idx
        ResolutionPolicy policy;

        public SyncObjective(Portfolio prtfl, int widx, ResolutionPolicy policy) {
            this.prtfl = prtfl;
            this.widx = widx;
            this.policy = policy;
        }

        @Override
        public synchronized void onSolution() {
            switch (policy) {
                case MINIMIZE:
                    Number bub = prtfl.workers[widx].getObjectiveManager().getBestUB();
                    for (int i = 0; i < prtfl.nthreads; i++) {
                        prtfl.workers[i].getObjectiveManager().updateBestUB(bub);
                    }
                    break;
                case MAXIMIZE:
                    Number blb = prtfl.workers[widx].getObjectiveManager().getBestLB();
                    for (int i = 0; i < prtfl.nthreads; i++) {
                        prtfl.workers[i].getObjectiveManager().updateBestLB(blb);
                    }
                    break;
            }

        }
    }


}
