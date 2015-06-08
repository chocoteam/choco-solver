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
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.cnf.SatConstraint;
import org.chocosolver.solver.constraints.nary.nogood.NogoodConstraint;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.solution.LastSolutionRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    /**
     * Number of threads used, ie, number of workers
     */
    int nthreads;

    /**
     * FOR ADVANCED USAGES ONLY.
     * The workers/solvers.
     */
    public final Solver[] workers;

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

    private boolean skip_strategy_configuration = false;

    /**
     * Ordered list of created model objects, to ensure carbon copies are EXACTLY the same (wrt to the variables and proapgators ID)
     */
    private List<Object> cmo;

    /**
     * Index of the last copied object form cmo to workers[i], i>0
     */
    private int lco;


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
        this.cmo = new ArrayList<>();
        this.lco = 0;
    }

    /**
     * Associate a variable to the front-end solver
     *
     * @param variable a newly created variable, not already added
     */
    @Override
    public void associates(Variable variable) {
        _fes_().associates(variable);
        cmo.add(variable);
    }

    /**
     * Post a constraint in the front-end solver
     *
     * @param c a Constraint
     */
    @Override
    public void post(Constraint c) {
        _fes_().post(c);
        cmo.add(c);
    }

    /**
     * Post constraints in the front-end solver
     *
     * @param cs Constraints
     */
    @Override
    public void post(Constraint... cs) {
        for (int i = 0; i < cs.length; i++) {
            this.post(cs[i]);
        }
    }

    @Override
    public IMeasures getMeasures() {
        throw new SolverException("Solver Portfolio does not yet enable to get global measures.");
    }

    @Override
    public SatConstraint getMinisat() {
        SatConstraint sc = _fes_().minisat;
        if (sc == null) {
            sc = _fes_().getMinisat();
            cmo.add(sc);
        }
        return sc;
    }

    @Override
    public NogoodConstraint getNogoodStore() {
        NogoodConstraint ngc = _fes_().nogoods;
        if (ngc == null) {
            ngc = _fes_().getNogoodStore();
            cmo.add(ngc);
        }
        return ngc;
    }

    /**
     * Set the search strategy of the front-end solver
     *
     * @param strategies the search strategies to use.
     */
    @Override
    public void set(AbstractStrategy... strategies) {
        _fes_().set(strategies);
    }

    private boolean needCopy() {
        return skip_conformity
                || cmo.size() != lco
                || _fes_().minisat != null && workers[1].minisat == null
                || _fes_().minisat != null && workers[1].minisat != null &&
                (_fes_().minisat.getSatSolver().numvars() != workers[1].minisat.getSatSolver().numvars()
                        || _fes_().minisat.getSatSolver().nbclauses() != workers[1].minisat.getSatSolver().nbclauses());
    }

    /**
     * Make a carbon copy of the front-end solver to the other ones.
     */
    public void carbonCopy() {
        // TODO: Ibex may have received new constraints or NogoodStore
        if (needCopy()) {
            if (_fes_().getEnvironment().getWorldIndex() > 0) {
                throw new SolverException("Duplicating a solver cannot be achieved once the resolution has begun.");
            }
            // Iterate over objects and over workers.
            // like this, the objects are ensured to be created in the same order,
            // and then their ID are the same too.
            for (; lco < cmo.size(); lco++) {
                Object o = cmo.get(lco);
                if (o instanceof Constraint) {
                    Constraint c = (Constraint) o;
                    Propagator[] ops = c.getPropagators();
                    for (int i = 1; i < nthreads; i++) {
                        Propagator[] cps = new Propagator[ops.length];
                        for (int j = 0; j < ops.length; j++) {
                            ops[j].duplicate(workers[i], imaps[i]);
                            cps[j] = (Propagator) imaps[i].get(ops[j]);
                            assert cps[j].getId() == ops[j].getId() :
                                    String.format("%s [%d]: wrong ID",
                                            ops[j].getClass().getSimpleName(),
                                            ops[j].getId());
                        }
                        Constraint cc = new Constraint(c.getName(), cps);
                        workers[i].post(cc);
                    }
                } else {
                    Variable v = (Variable) o;
                    for (int i = 1; i < nthreads; i++) {
                        v.duplicate(workers[i], imaps[i]);
                        assert v.getId() == ((Variable) imaps[i].get(v)).getId();
                    }
                }
            }
            // Then deal with clauses
            for (int n = 1; n < nthreads; n++) {
                if (_fes_().minisat != null && workers[1].minisat != null
                        && (_fes_().minisat.getSatSolver().numvars() != workers[n].minisat.getSatSolver().numvars()
                        || _fes_().minisat.getSatSolver().nbclauses() != workers[n].minisat.getSatSolver().nbclauses())) {
                    workers[n].minisat.getSatSolver().copyFrom(_fes_().minisat.getSatSolver());
                }
            }
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
        if (!skip_strategy_configuration) {
            setStrategies(ResolutionPolicy.SATISFACTION);
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
        if (!skip_strategy_configuration) {
            setStrategies(ResolutionPolicy.SATISFACTION);
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
        if (!skip_strategy_configuration) {
            setStrategies(policy);
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

    /**
     * Enable to skip the strategy configrations, that is, that all solvers, except the front-end, have an automatically defined search strategy
     * based (or not) on the decisions variables (if any).
     * The default value of <code>ssc</code> is <code>false</code>, setting it to <code>true</code> will skip the configuration steps
     * achieved before calling {@link #findSolution()}, {@link #findAllSolutions()}
     * and {@link #findOptimalSolution(ResolutionPolicy, org.chocosolver.solver.variables.IntVar)}.
     * Skipping the configurations may be interesting if one have a better knowledge of the strategies adapted to solve the underlying problem.
     *
     * @param ssc set to true to skip the strategy configurations.
     */
    public void skipStrategyConfiguration(boolean ssc) {
        this.skip_strategy_configuration = ssc;
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


    /**
     * Retrieve decisions variables from the front-end solver and define strategies for the others
     *
     * @param policy resolution policy, among SATISFACTION, MINIMIZE and MAXIMIZE
     */
    private void setStrategies(ResolutionPolicy policy) {
        //TODO deal with other type of variables
        IntVar[] dvars = new IntVar[_fes_().getNbVars()];
        Variable[] vars = _fes_().getVars();
        if (_fes_().getStrategy() != null
                && _fes_().getStrategy().getVariables().length > 0) {
            vars = _fes_().getStrategy().getVariables();
        }
        assert vars.length > 0;
        int k = 0;
        for (int i = 0; i < vars.length; i++) {
            if ((vars[i].getTypeAndKind() & Variable.INT) > 0) {
                dvars[k++] = (IntVar) vars[i];
            }
        }
        dvars = Arrays.copyOf(dvars, k);
        int t = 1;
        switch (policy) {
            default:
                // 2nd worker
                if (t < nthreads) {
                    IntVar[] mvars = retrieveVarIn(t, dvars);
                    workers[t].set(ISF.activity(mvars, 0));
                    t++;
                }
                // 3rd worker
                if (t < nthreads) {
                    IntVar[] mvars = retrieveVarIn(t, dvars);
                    workers[t].set(ISF.domOverWDeg(mvars, 0));
                    t++;
                }
                // 4th worker
                if (t < nthreads) {
                    IntVar[] mvars = retrieveVarIn(t, dvars);
                    workers[t].set(
                            ISF.lastConflict(workers[t], ISF.minDom_LB(mvars))
                    );
                    t++;
                }
                // then I'm feeling lucky
                for (; t < nthreads; t++) {
                    IntVar[] mvars = retrieveVarIn(t, dvars);
                    workers[t].set(ISF.activity(mvars, t));
                }
                break;
        }
    }
}
