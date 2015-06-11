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
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.LNSFactory;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SMF;
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

/**
 * To deal with multi-solver resolutions, such as Solver Portfolio.
 * Created by cprudhom on 10/06/15.
 * Project: choco.
 */
public abstract class MultiSolvers implements Serializable, ISolver {


    protected static final int FRONTEND = 0;
    /**
     * Number of workers used, ie, number of solvers
     */
    int nbworkers;

    /**
     * FOR ADVANCED USAGES ONLY.
     * The workers/solvers.
     */
    public Solver[] workers;

    /**
     * The identity maps
     */
    THashMap<Object, Object>[] imaps;

    /**
     * Ordered list of created model objects, to ensure carbon copies are EXACTLY the same (wrt to the variables and proapgators ID)
     */
    List<Object> cmo;

    /**
     * Index of the last copied object form cmo to workers[i], i>0
     */
    int lco;

    ISolutionRecorder solutionRecorder;

    /**
     * Count the number of new solutions -- for internal purpose only
     */
    long[] new_solutions;


    boolean skip_conformity = false;

    boolean skip_strategy_configuration = false;


    public MultiSolvers(String name, int nbworkers) {
        if (nbworkers < 2) {
            throw new SolverException(String.format("Please consider creating a Solver instead of a MultiSolvers since %d solver are required.", nbworkers));
        }
        this.nbworkers = nbworkers;
        this.workers = new Solver[this.nbworkers];
        this.imaps = new THashMap[this.nbworkers];
        for (int i = 0; i < this.nbworkers; i++) {
            this.workers[i] = SolverFactory.makeSolver(Environments.DEFAULT.make(), name + "_" + i);
            this.imaps[i] = new THashMap<>();
        }
        this.cmo = new ArrayList<>();
        this.lco = 0;
        this.new_solutions = new long[nbworkers];
        solutionRecorder = new LastSharedSolutionRecorder(new Solution(), this);
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
        ESat feas = ESat.UNDEFINED;
        for (int i = 0; i < nbworkers; i++) {
            switch (workers[i].isFeasible()) {
                case TRUE:
                    feas = ESat.TRUE;
                    break;
                case FALSE:
                    if(workers[i].getSearchLoop().isComplete()) {
                        return ESat.FALSE;
                    }
                default:
                    break;
            }
        }
        return feas;
    }

    /**
     * This method is not implemented for MultiSolvers.
     * Actually, looking for all solutions of a given problem with a MultiSolvers approach is relevant if the solution pool is shared which
     * prevent from finding the same solution more than twice.
     * As MultiSolvers limits the data shared between workers to objective cuts (which is out of the scope when looking for all solutions),
     * some solutions may be discovered more than once.
     * In conclusion, one should use a single Solver instead for such purpose.
     *
     * @throws SolverException always thrown
     */
    @Override
    public long findAllSolutions() {
        throw new SolverException("A MultiSolvers does not allow to search for all solutions of a problem. See MultiSolvers.findAllSolutions() javadoc for more details.");
    }

    boolean needCopy() {
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
                    for (int i = 1; i < nbworkers; i++) {
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
                    for (int i = 1; i < nbworkers; i++) {
                        v.duplicate(workers[i], imaps[i]);
                        assert v.getId() == ((Variable) imaps[i].get(v)).getId();
                    }
                }
            }
            // Then deal with clauses
            for (int n = 1; n < nbworkers; n++) {
                if (_fes_().minisat != null && workers[1].minisat != null
                        && (_fes_().minisat.getSatSolver().numvars() != workers[n].minisat.getSatSolver().numvars()
                        || _fes_().minisat.getSatSolver().nbclauses() != workers[n].minisat.getSatSolver().nbclauses())) {
                    workers[n].minisat.getSatSolver().copyFrom(_fes_().minisat.getSatSolver());
                }
            }
        }
    }

    long countNewSolutions() {
        long nsol = 0;
        for (int i = 0; i < nbworkers; i++) {
            new_solutions[i] = workers[i].getMeasures().getSolutionCount() - new_solutions[i];
            nsol += new_solutions[i];
        }
        return nsol;
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
        return nbworkers;
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


    /**
     * Restore the best solution into the workers
     *
     * @param objective objective variable
     * @param policy    resolution policy
     */
    void restoreSolution(IntVar objective, ResolutionPolicy policy) {
        Solution bstsol = null;
        int bstval = policy == ResolutionPolicy.MINIMIZE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        for (int i = 0; i < nbworkers; i++) {
            if (workers[i].getMeasures().getSolutionCount() > 0) {
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
        }
        if (bstsol != null) {
            for (int i = 0; i < nbworkers; i++) {
                try {
                    workers[i].getSearchLoop().restoreRootNode();
                    workers[i].getEnvironment().worldPush();
                    bstsol.restore(workers[i]);
                } catch (ContradictionException e) {
                    throw new SolverException("restoring the last solution ended in a failure");
                }
                workers[i].getEngine().flush();
            }
        }
    }

    /**
     * Retrieve decisions variables from the front-end solver and define strategies for the others
     *
     * @param policy resolution policy, among SATISFACTION, MINIMIZE and MAXIMIZE
     */
    void setStrategies(ResolutionPolicy policy) {
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
        for (int w = 1; w < nbworkers; w++) {
            pickStrategy(w, dvars, policy);
        }
    }

    void pickStrategy(int w, IntVar[] vars, ResolutionPolicy policy) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static class SyncObjective implements IMonitorSolution {

        MultiSolvers mltslvr;
        int widx; // worker idx
        ResolutionPolicy policy;

        public SyncObjective(MultiSolvers mltslvr, int widx, ResolutionPolicy policy) {
            this.mltslvr = mltslvr;
            this.widx = widx;
            this.policy = policy;
        }

        @Override
        public synchronized void onSolution() {
            switch (policy) {
                case MINIMIZE:
                    Number bub = mltslvr.workers[widx].getObjectiveManager().getBestUB();
                    for (int i = 0; i < mltslvr.nbworkers; i++) {
                        mltslvr.workers[i].getObjectiveManager().updateBestUB(bub);
                    }
                    break;
                case MAXIMIZE:
                    Number blb = mltslvr.workers[widx].getObjectiveManager().getBestLB();
                    for (int i = 0; i < mltslvr.nbworkers; i++) {
                        mltslvr.workers[i].getObjectiveManager().updateBestLB(blb);
                    }
                    break;
            }

        }
    }
}
