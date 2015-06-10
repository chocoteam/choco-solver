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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.LNSFactory;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.solution.ISolutionRecorder;
import org.chocosolver.solver.search.solution.LastSharedSolutionRecorder;
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

    protected ISolutionRecorder solutionRecorder;

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
        solutionRecorder = new LastSharedSolutionRecorder(new Solution(), this);
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
    public ISolutionRecorder getSolutionRecorder() {
        return solutionRecorder;
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

    private void restoreSolution(Solution solution) {
        for (int i = 0; i < nthreads; i++) {
            try {
                workers[i].getSearchLoop().restoreRootNode();
                workers[i].getEnvironment().worldPush();
                solution.restore(workers[i]);
            } catch (ContradictionException e) {
                throw new SolverException("restoring the last solution ended in a failure");
            }
            workers[i].getEngine().flush();
        }
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

    /**
     * Look for a new solution, if any.
     * Beware, some search strategies allow finding the same solutions more than once.
     * In consequence, calling this method in while-loop from a Portfolio may lead to infinite loop.
     * @return true if a new solution is found
     */
    @Override
    public boolean nextSolution() {
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
                    // TODO: the test is not safe,
                    if (!workers[_s].getSearchLoop().isComplete()) {
                        workers[_s].getSearchLoop().forceAlive(true); // because last stop was strong
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

    /**
     * This method is not implemented for solver portfolio.
     * Actually, looking for all solutions of a given problem with a portfolio approach is relevant if the solution pool is shared which
     * prevent from finding the same solution more than twice.
     * As Portfolio limits the data shared between workers to objective cuts (which is out of the scope when looking for all solutions),
     * some solutions may be discovered more than once.
     * In conclusion, one should use a Solver instead for such purpose.
     * @exception SolverException always thrown
     */
    @Override
    public long findAllSolutions() {
        throw new SolverException("A solver portfolio does not allow to search for all solutions of a problem. See Portfolio.findAllSolutions() javadoc for more details.");
    }

    @Override
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        // TODO: deal with same solutions
        // TODO remove SyncObjective from SM list of each solvers
        // TODO deal with directed SyncObjective
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
        // restore the best solution into the workers
        Solution bstsol = workers[FRONTEND].getSolutionRecorder().getLastSolution();
        int bstval = bstsol.getIntVal(objective);
        for (int i = 1; i < nthreads; i++) {
            Solution crtsol = workers[i].getSolutionRecorder().getLastSolution();
            int crtval = crtsol.getIntVal(objective);
            switch (policy) {
                case MINIMIZE:
                    if (crtval < bstval) {
                        bstsol = crtsol;
                        bstval = crtval;
                    }
                    break;
                case MAXIMIZE:
                    if (crtval > bstval) {
                        bstsol = crtsol;
                        bstval = crtval;
                    }
                    break;
                default:
                    throw new SolverException("Unknown policy");
            }
        }
        restoreSolution(bstsol);
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
            workers[i].getSearchLoop().interrupt("Portfolio orders to interrupt", false);
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
        for (int w = 1; w < nthreads; w++) {
            pickStrategy(w, dvars, policy);
        }
    }

    private void pickStrategy(int w, IntVar[] vars, ResolutionPolicy policy) {
        switch (w) {
            default:
            case 1: {
                IntVar[] mvars = retrieveVarIn(w, vars);
                workers[w].set(ISF.lastConflict(workers[w], ISF.activity(mvars, w)));
                SMF.geometrical(workers[w], 500, 1.2, new FailCounter(100), 200);
                SMF.nogoodRecordingFromRestarts(workers[0]);
            }
            break;
            case 2: {
                IntVar[] mvars = retrieveVarIn(w, vars);
                workers[w].set(ISF.minDom_LB(mvars));
                ExplanationFactory.CBJ.plugin(workers[w], false, false);
            }
            break;
            case 3:
                switch (policy) {
                    case SATISFACTION: {
                        IntVar[] mvars = retrieveVarIn(w, vars);
                        workers[w].set(ISF.random(mvars, w));
                        SMF.geometrical(workers[w], 100, 1.0001, new FailCounter(100), Integer.MAX_VALUE);
                        SMF.nogoodRecordingFromRestarts(workers[0]);
                    }
                    break;
                    default: {
                        IntVar[] mvars = retrieveVarIn(w, vars);
                        LNSFactory.pglns(workers[w], mvars, 30, 10, 200, w, new FailCounter(100));
                    }
                    break;
                }
                break;
        }
    }
}
