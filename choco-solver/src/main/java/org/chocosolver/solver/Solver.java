/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.memory.Environments;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.cnf.PropFalse;
import org.chocosolver.solver.constraints.nary.cnf.PropTrue;
import org.chocosolver.solver.constraints.nary.cnf.SatConstraint;
import org.chocosolver.solver.constraints.nary.nogood.NogoodConstraint;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.NoPropagationEngine;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.propagation.PropagationTrigger;
import org.chocosolver.solver.search.loop.ISearchLoop;
import org.chocosolver.solver.search.loop.SearchLoop;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.chocosolver.solver.search.solution.*;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.observers.FilteringMonitorList;
import org.chocosolver.util.ESat;

import java.io.*;
import java.util.Arrays;

/**
 * The <code>Solver</code> is the header component of Constraint Programming.
 * It embeds the list of <code>Variable</code> (and their <code>Domain</code>), the <code>Constraint</code>'s network,
 * and a <code>IPropagationEngine</code> to pilot the propagation.<br/>
 * <code>Solver</code> includes a <code>AbstractSearchLoop</code> to guide the search loop: apply decisions and propagate,
 * run backups and rollbacks and store solutions.
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @see org.chocosolver.solver.variables.Variable
 * @see org.chocosolver.solver.constraints.Constraint
 * @since 0.01
 */
public class Solver implements Serializable {

    private static final long serialVersionUID = 1L;

    private Settings settings = new Settings() {
    };

    private ExplanationEngine explainer;

    private FilteringMonitorList eoList;

    /**
     * Variables of the solver
     */
    Variable[] vars;
    int vIdx;

    /**
     * Constraints of the solver
     */
    Constraint[] cstrs;
    int cIdx;

    public TIntObjectHashMap<IntVar> cachedConstants;

    /**
     * Environment, based of the search tree (trailing or copying)
     */
    final IEnvironment environment;

    /**
     * Search loop of the solver
     */
    protected ISearchLoop search;

    protected IPropagationEngine engine;

    /**
     * Solver's measures
     */
    protected final IMeasures measures;

    protected ISolutionRecorder solutionRecorder;

    /**
     * Solver name
     */
    protected String name;

    /**
     * Problem feasbility:
     * - UNDEFINED if unknown,
     * - TRUE if satisfiable,
     * - FALSE if unsatisfiable
     */
    ESat feasible = ESat.UNDEFINED;

    protected long creationTime;

    protected int id = 1;

    /**
     * Two basic constraints TRUE and FALSE, cached to avoid multiple useless occurrences
     */
    public final Constraint TRUE, FALSE;

    /**
     * Two basic constants ZERO and ONE, cached to avoid multiple useless occurrences.
     */
    public final BoolVar ZERO, ONE;


    protected SatConstraint minisat;
    protected NogoodConstraint nogoods;
    private Ibex ibex;

    /**
     * Create a solver object embedding a <code>environment</code>,  named <code>name</code> and with the specific set of
     * properties <code>solverProperties</code>.
     *
     * @param environment a backtracking environment
     * @param name        a name
     */
    public Solver(IEnvironment environment, String name) {
        this.name = name;
        this.vars = new Variable[32];
        vIdx = 0;
        this.cstrs = new Constraint[32];
        cIdx = 0;
        this.environment = environment;
        this.measures = new MeasuresRecorder(this); // must be created before calling search loop.
        this.search = new SearchLoop(this);
        this.eoList = new FilteringMonitorList();
        this.creationTime -= System.nanoTime();
        this.cachedConstants = new TIntObjectHashMap<>(16, 1.5f, Integer.MAX_VALUE);
        this.engine = NoPropagationEngine.SINGLETON;
        ZERO = (BoolVar) VF.fixed(0, this);
        ONE = (BoolVar) VF.fixed(1, this);
        ZERO._setNot(ONE);
        ONE._setNot(ZERO);
        TRUE = new Constraint("TRUE cstr", new PropTrue(ONE));
        FALSE = new Constraint("FALSE cstr", new PropFalse(ZERO));
        solutionRecorder = new LastSolutionRecorder(new Solution(), false, this);
        set(ObjectiveManager.SAT());
    }

    /**
     * Create a solver object with default parameters.
     */
    public Solver() {
        this(Environments.DEFAULT.make(), "");
    }

    /**
     * Create a solver object with default parameters, named <code>name</code>.
     */
    public Solver(String name) {
        this(Environments.DEFAULT.make(), name);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// GETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Returns the unique and internal search loop.
     *
     * @return the unique and internal <code>AbstractSearchLoop</code> object.
     */
    public ISearchLoop getSearchLoop() {
        return search;
    }

    /**
     * Get the objective manager
     */
    public ObjectiveManager getObjectiveManager() {
        return this.search.getObjectiveManager();
    }

    public AbstractStrategy getStrategy() {
        return search.getStrategy();
    }

    /**
     * Returns the propagation engine used in <code>this</code>.
     *
     * @return a propagation engine.
     */
    public IPropagationEngine getEngine() {
        return engine;
    }

    /**
     * Returns the array of declared <code>Variable</code> objects defined in this <code>Solver</code>.
     *
     * @return array of variables
     */
    public Variable[] getVars() {
        return Arrays.copyOf(vars, vIdx);
    }


    /**
     * Returns the number of variables involved in <code>this</code>.
     *
     * @return number of variables
     */
    public int getNbVars() {
        return vIdx;
    }

    /**
     * Returns the i<sup>th</sup> variables within the array of variables defined in <code>this</code>.
     *
     * @param i index of the variables to return.
     * @return a variable
     */
    public Variable getVar(int i) {
        return vars[i];
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains the IntVar only (<b>excluding</b> BoolVar).
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of IntVars of <code>this</code>
     */
    public IntVar[] retrieveIntVars() {
        IntVar[] ivars = new IntVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.KIND) == Variable.INT) {
                ivars[k++] = (IntVar) vars[i];
            }
        }
        return Arrays.copyOf(ivars, k);
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains the BoolVar only.
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of BoolVars of <code>this</code>
     */
    public BoolVar[] retrieveBoolVars() {
        BoolVar[] bvars = new BoolVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.KIND) == Variable.BOOL) {
                bvars[k++] = (BoolVar) vars[i];
            }
        }
        return Arrays.copyOf(bvars, k);
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains the SetVar only.
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of SetVars of <code>this</code>
     */
    public SetVar[] retrieveSetVars() {
        SetVar[] bvars = new SetVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.KIND) == Variable.SET) {
                bvars[k++] = (SetVar) vars[i];
            }
        }
        return Arrays.copyOf(bvars, k);
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains the RealVar only.
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of RealVars of <code>this</code>
     */
    public RealVar[] retrieveRealVars() {
        RealVar[] bvars = new RealVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.KIND) == Variable.REAL) {
                bvars[k++] = (RealVar) vars[i];
            }
        }
        return Arrays.copyOf(bvars, k);
    }

    /**
     * Returns the array of declared <code>Constraint</code> objects defined in this <code>Solver</code>.
     *
     * @return array of constraints
     */
    public Constraint[] getCstrs() {
        return Arrays.copyOf(cstrs, cIdx);
    }

    /**
     * Return the number of constraints declared in <code>this</code>.
     *
     * @return number of constraints.
     */
    public int getNbCstrs() {
        return cIdx;
    }

    /**
     * Return the name, if any, of <code>this</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the backtracking environment of <code>this</code>.
     */
    public IEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Return a reference to the measures recorder.
     * This enables to get, for instance, the number of solutions found, time count, etc.
     */
    public IMeasures getMeasures() {
        return measures;
    }

    /**
     * Return the explanation engine plugged into <code>this</code>.
     */
    public ExplanationEngine getExplainer() {
        return explainer;
    }

    /**
     * Return the solution recorder
     */
    public ISolutionRecorder getSolutionRecorder() {
        return solutionRecorder;
    }

    /**
     * Return the current settings for the solver
     *
     * @return a {@link org.chocosolver.solver.Settings}
     */
    public Settings getSettings() {
        return this.settings;
    }


    /**
     * Return the current event observer list
     */
    public FilteringMonitor getEventObserver() {
        return this.eoList;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// SETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Override the default search loop to use in <code>this</code>.
     *
     * @param searchLoop the search loop to use
     */
    public void set(ISearchLoop searchLoop) {
        this.search = searchLoop;
    }

    /**
     * Override the default search strategies to use in <code>this</code>.
     * In case many strategies are given, they will be called in sequence:
     * The first strategy in parameter is first called to compute a decision, if possible.
     * If it cannot provide a new decision, the second strategy is called ...
     * and so on, until the last strategy.
     * <p>
     *
     * @param strategies the search strategies to use.
     */
    public void set(AbstractStrategy... strategies) {
        if (strategies == null || strategies.length == 0) {
            throw new UnsupportedOperationException("no search strategy has been specified");
        }
        if (strategies.length == 1) {
            search.set(strategies[0]);
        } else {
            search.set(ISF.sequencer(strategies));
        }

    }

    /**
     * Attach a propagation engine <code>this</code>.
     * It overrides the previously defined one, if any.
     *
     * @param propagationEngine a propagation strategy
     */
    public void set(IPropagationEngine propagationEngine) {
        this.engine = propagationEngine;
    }

    /**
     * Override the explanation engine.
     */
    public void set(ExplanationEngine explainer) {
        this.explainer = explainer;
        plugMonitor(explainer);
    }

    /**
     * Override the objective manager
     */
    public void set(ObjectiveManager om) {
        this.search.setObjectiveManager(om);
    }

    /**
     * Override the solution recorder.
     * Beware : multiple recorders which restore a solution might create a conflict.
     */
    public void set(ISolutionRecorder sr) {
        this.solutionRecorder = sr;
    }

    /**
     * Put a search monitor to react on search events (solutions, decisions, fails, ...)
     *
     * @param sm a search monitor to be plugged in the solver
     */
    public void plugMonitor(ISearchMonitor sm) {
        search.plugSearchMonitor(sm);
    }

    /**
     * Remove a search monitor.
     *
     * @param sm a search monitor to be unplugged from the solver
     */
    public void unplugMonitor(ISearchMonitor sm) {
        search.unplugSearchMonitor(sm);
    }

    /**
     * Override the default {@link org.chocosolver.solver.Settings} object.
     *
     * @param defaults new settings
     */
    public void set(Settings defaults) {
        this.settings = defaults;
    }

    /**
     * Add an event observer, that is an object that is kept informed of all (propagation) events generated during the resolution.
     * <p>
     * Erase the current event observer if any.
     *
     * @param filteringMonitor an event observer
     */
    public void plugMonitor(FilteringMonitor filteringMonitor) {
        this.eoList.add(filteringMonitor);
    }

    /**
     * If {@code isComplete} is set to true, a complementary search strategy is added to the declared one in order to
     * ensure that all variables are covered by a search strategy.
     * Otherwise, the declared search strategy is used as is.
     *
     * @param isComplete completeness of the declared search strategy
     */
    public void makeCompleteSearch(boolean isComplete){
        this.search.makeCompleteStrategy(isComplete);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// RELATED TO VAR AND CSTR DECLARATION ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Link a variable to <code>this</code>.
     * This is executed AUTOMATICALLY in variable constructor, so no checked are done on multiple occurrences of
     * the very same variable.
     *
     * @param variable a newly created variable, not already added
     */
    public void associates(Variable variable) {
        if (vIdx == vars.length) {
            Variable[] tmp = vars;
            vars = new Variable[tmp.length * 2];
            System.arraycopy(tmp, 0, vars, 0, vIdx);
        }
        vars[vIdx++] = variable;
    }

    /**
     * Unlink the variable from <code>this</code>.
     *
     * @param variable variable to un-associate
     */
    public void unassociates(Variable variable) {
        if (variable.getNbProps() > 0) {
            throw new SolverException("Try to remove a variable (" + variable.getName() + ")which is still involved in at least one constraint");
        }
        int idx = 0;
        for (; idx < vIdx; idx++) {
            if (variable == vars[idx]) break;
        }
        System.arraycopy(vars, idx + 1, vars, idx + 1 - 1, vIdx - (idx + 1));
        vars[--vIdx] = null;
    }

    /**
     * Post permanently a constraint <code>c</code> in the constraints network of <code>this</code>:
     * - add it to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param c a Constraint
     */
    public void post(Constraint c) {
        _post(true, c);
    }

    /**
     * Post constraints <code>cs</code> permanently in the constraints network of <code>this</code>:
     * - add them to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param cs Constraints
     */
    public void post(Constraint... cs) {
        _post(true, cs);
    }

    /**
     * Post a constraint temporary, that is, it will be unposted upon backtrack.
     *
     * @param c constraint to add
     */
    public void postTemp(Constraint c) throws ContradictionException {
        _post(false, c);
        if (engine == NoPropagationEngine.SINGLETON || !engine.isInitialized()) {
            throw new SolverException("Try to post a temporary constraint while the resolution has not begun.\n" +
                    "A call to Solver.post(Constraint) is more appropriate.");
        }
        for (Propagator propagator : c.getPropagators()) {
            PropagationTrigger.execute(propagator, engine);
        }
    }


    /**
     * Add constraints to the model.
     *
     * @param permanent specify whether the constraints are added permanently (if set to true) or temporary (ie, should be removed on backtrack)
     * @param cs        list of constraints
     */
    private void _post(boolean permanent, Constraint... cs) {
        boolean dynAdd = false;
        // check if the resolution already started -> if true, dynamic addition
        if (engine != NoPropagationEngine.SINGLETON && engine.isInitialized()) {
            dynAdd = true;
        }
        // then store the constraints
        if (cIdx + cs.length >= cstrs.length) {
            int nsize = cstrs.length;
            while (cIdx + cs.length >= nsize) {
                nsize *= 3 / 2 + 1;
            }
            Constraint[] tmp = cstrs;
            cstrs = new Constraint[nsize];
            System.arraycopy(tmp, 0, cstrs, 0, cIdx);
        }
        System.arraycopy(cs, 0, cstrs, cIdx, cs.length);
        cIdx += cs.length;
        // specific behavior for dynamic addition and/or reified constraints
        for (int i = 0; i < cs.length; i++) {
            if (dynAdd) {
                engine.dynamicAddition(permanent, cs[i].getPropagators());
            }
            if (cs[i].isReified()) {
                try {
                    cs[i].reif().setToTrue(Cause.Null);
                } catch (ContradictionException e) {
                    throw new SolverException("post a constraint whose reification BoolVar is already set to false: no solution can exist");
                }
            }
        }
    }

    /**
     * Remove permanently the constraint <code>c</code> from the constraint network.
     *
     * @param c the constraint to remove
     */
    public void unpost(Constraint c) {
        // 1. look for the constraint c
        int idx = 0;
        while (idx < cIdx && cstrs[idx] != c) {
            idx++;
        }
        // 2. remove it from the network
        if (idx < cIdx) {
            Constraint cm = cstrs[--cIdx];
            cstrs[idx] = cm;
            cstrs[cIdx] = null;
            // 3. check if the resolution already started -> if true, dynamic deletion
            if (engine != NoPropagationEngine.SINGLETON && engine.isInitialized()) {
                engine.dynamicDeletion(c.getPropagators());
            }
            // 4. remove the propagators of the constraint from its variables
            for (Propagator prop : c.getPropagators()) {
                for (int v = 0; v < prop.getNbVars(); v++) {
                    prop.getVar(v).unlink(prop);
                }
            }
        }
    }

    /**
     * Return a constraint embedding a minisat solver.
     * It is highly recommended that there is only once instance of this constraint in a solver.
     * So a call to this method will create and post the constraint if it does not exist.
     *
     * @return the minisat constraint
     */
    public SatConstraint getMinisat() {
        if (minisat == null) {
            minisat = new SatConstraint(this);
            this.post(minisat);
        }
        return minisat;
    }

    /**
     * Return a constraint embedding a nogood store (based on a sat solver).
     * It is highly recommended that there is only once instance of this constraint in a solver.
     * So a call to this method will create and post the constraint if it does not exist.
     *
     * @return the minisat constraint
     */
    public NogoodConstraint getNogoodStore() {
        if (nogoods == null) {
            nogoods = new NogoodConstraint(this);
            this.post(nogoods);
        }
        return nogoods;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// RELATED TO RESOLUTION //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns information on the feasibility of the current problem defined by the solver.
     * <p>
     * Possible back values are:
     * <br/>- {@link ESat#TRUE}: a solution has been found,
     * <br/>- {@link ESat#FALSE}: the CSP has been proven to have no solution,
     * <br/>- {@link ESat#UNDEFINED}: no solution has been found so far (within given limits)
     * without proving the unfeasibility, though.
     *
     * @return an {@link ESat}.
     */
    public ESat isFeasible() {
        return feasible;
    }

    /**
     * Changes the current feasibility state of the <code>Solver</code> object.
     * <p>
     * <b>Commonly called by the search loop, should not used without any knowledge of side effects.</b>
     *
     * @param feasible new state
     */
    public void setFeasible(ESat feasible) {
        this.feasible = feasible;
    }

    /**
     * Returns information on the completeness of the search process.
     * <p>
     * A call to {@link #isFeasible()} may provide complementary information.
     * <p>
     * Possible back values are:
     * <p>
     * <br/>- <code>false</code> : the resolution is complete and
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findSolution()}: a solution has been found or the CSP has been proven to be unsatisfiable.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #nextSolution()}: a new solution has been found, or no more solutions exist.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findAllSolutions()}: all solutions have been found, or the CSP has been proven to be unsatisfiable.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findOptimalSolution(ResolutionPolicy, org.chocosolver.solver.variables.IntVar)}: the optimal solution has been found and
     * proven to be optimal, or the CSP has been proven to be unsatisfiable.
     * <br/>- <code>true</code>: the resolution stopped after reaching a limit.
     */
    public boolean hasReachedLimit() {
        return search.hasReachedLimit();
    }

    /**
     * Attempts to find the first solution of the declared problem.
     * Then, following solutions can be found using {@link org.chocosolver.solver.Solver#nextSolution()}.
     * <p>
     * An alternative is to call {@link org.chocosolver.solver.Solver#isFeasible()} which tells, whether or not, a solution has been found.
     *
     * @return <code>true</code> if and only if a solution has been found.
     */
    public boolean findSolution() {
        solve(true);
        return measures.getSolutionCount() > 0;
    }

    /**
     * Once {@link Solver#findSolution()} has been called once, other solutions can be found using this method.
     * <p>
     * The search is then resume to the last found solution point.
     *
     * @return a boolean stating whereas a new solution has been found (<code>true</code>), or not (<code>false</code>).
     */
    public boolean nextSolution() {
        long nbsol = measures.getSolutionCount();
        search.resume();
        return (measures.getSolutionCount() - nbsol) > 0;
    }

    /**
     * Attempts to find all solutions of the declared problem.
     *
     * @return the number of found solutions.
     */
    public long findAllSolutions() {
        solve(false);
        return measures.getSolutionCount();
    }

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Restores the best solution found so far (if any)
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     */
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        if (policy == ResolutionPolicy.SATISFACTION) {
            throw new SolverException("Solver.findOptimalSolution(...) cannot be called with ResolutionPolicy.SATISFACTION.");
        }
        if (objective == null) {
            throw new SolverException("No objective variable has been defined");
        }
        if (!getObjectiveManager().isOptimization()) {
            set(new ObjectiveManager<IntVar, Integer>(objective, policy, true));
        }
        LastSolutionRecorder recorder = new LastSolutionRecorder(new Solution(), true, this);
        try {
                solve(false);
        } finally {
                recorder.stop();
        }
    }

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Finds and stores all optimal solution
     * Restores the best solution found so far (if any)
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     * @param twoSteps  When set to true it calls two resolution:
     *                  1) It finds and prove the optimum
     *                  2) It reset search and enumerates all solutions of optimal cost
     *                  When set to false, it performs only one resolution but which does impose to find strictly
     *                  better solutions. This means it will spend time enumerating intermediary solutions equal to the
     *                  the best cost found so far (but not necessarily optimal).
     */
    public void findAllOptimalSolutions(ResolutionPolicy policy, IntVar objective, boolean twoSteps) {
        if (twoSteps) {
            findOptimalSolution(policy, objective);
            if (getMeasures().getSolutionCount() > 0) {
                int opt = getObjectiveManager().getBestSolutionValue().intValue();
                getEngine().flush();
                search.reset();
                post(ICF.arithm(objective, "=", opt));
                set(new AllSolutionsRecorder(this));
                findAllSolutions();
            }
        } else {
            if (policy == ResolutionPolicy.SATISFACTION) {
                throw new SolverException("Solver.findAllOptimalSolutions(...) cannot be called with ResolutionPolicy.SATISFACTION.");
            }
            if (objective == null) {
                throw new SolverException("No objective variable has been defined");
            }
            if (!getObjectiveManager().isOptimization()) {
                set(new ObjectiveManager<IntVar, Integer>(objective, policy, false));
            }
            set(new BestSolutionsRecorder(objective));
            solve(false);
        }
    }

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Finds and stores all optimal solution
     * Restores the best solution found so far (if any)
     *
     * @param policy     optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objectives the variables to optimize. BEWARE they should all respect the SAME optimization policy
     */
    public void findParetoFront(ResolutionPolicy policy, IntVar... objectives) {
        if (policy == ResolutionPolicy.SATISFACTION) {
            throw new SolverException("Solver.findParetoFront(...) cannot be called with ResolutionPolicy.SATISFACTION.");
        }
        if (objectives == null || objectives.length == 0) {
            throw new SolverException("No objective variable has been defined");
        }
        if (objectives.length == 1) {
            throw new SolverException("Only one objective variable has been defined. Pareto is relevant with >1 objective");
        }
        // BEWARE the usual optimization manager is only defined for mono-objective optimization
        // so we use a satisfaction manager by default (it does nothing)
        if (getObjectiveManager().isOptimization()) {
            set(new ObjectiveManager<IntVar, Integer>(null, ResolutionPolicy.SATISFACTION, false));
        }
        set(new ParetoSolutionsRecorder(policy, objectives));
        solve(false);
    }

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Restores the last solution found so far (if any)
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     */
    public void findOptimalSolution(ResolutionPolicy policy, RealVar objective, double precision) {
        if (policy == ResolutionPolicy.SATISFACTION) {
            throw new SolverException("Solver.findOptimalSolution(...) can not be called with ResolutionPolicy.SATISFACTION.");
        }
        if (objective == null) {
            throw new SolverException("No objective variable has been defined");
        }
        if (!getObjectiveManager().isOptimization()) {
            set(new ObjectiveManager<RealVar, Double>(objective, policy, precision, true));
        }
        LastSolutionRecorder recorder = new LastSolutionRecorder(new Solution(), true, this);
        try {
                solve(false);
        } finally {
                recorder.stop();
        }
    }

    /**
     * This method should not be called externally. It launches the resolution process.
     */
    protected void solve(boolean stopAtFirst) {
        if (engine == NoPropagationEngine.SINGLETON) {
            this.set(PropagationEngineFactory.DEFAULT.make(this));
        }
        if(!engine.isInitialized()){
            engine.initialize();
        }
        measures.setReadingTimeCount(creationTime + System.nanoTime());
        search.launch(stopAtFirst);
    }

    /**
     * Propagate constraints and related events through the constraint network until a fix point is find, or a contradiction
     * is detected.
     *
     * @throws ContradictionException
     */
    public void propagate() throws ContradictionException {
        if (engine == NoPropagationEngine.SINGLETON) {
            this.set(PropagationEngineFactory.DEFAULT.make(this));
        }
        if(!engine.isInitialized()){
            engine.initialize();
        }
        engine.propagate();
    }

    /**
     * Return the current state of the CSP.
     * <p>
     * Given the current domains, it can return a value among:
     * <br/>- {@link ESat#TRUE}: all constraints of the CSP are satisfied for sure,
     * <br/>- {@link ESat#FALSE}: at least one constraint of the CSP is not satisfied.
     * <br/>- {@link ESat#UNDEFINED}: neither satisfiability nor  unsatisfiability could be proven so far.
     * <p>
     * Presumably, not all variables are instantiated.
     */
    public ESat isSatisfied() {
        int OK = 0;
        for (int c = 0; c < cIdx; c++) {
            ESat satC = cstrs[c].isSatisfied();
            if (ESat.FALSE == satC) {
                System.err.println(String.format("FAILURE >> %s (%s)", cstrs[c].toString(), satC));
                return ESat.FALSE;
            } else if (ESat.TRUE == satC) {
                OK++;
            }
        }
        if (OK == cIdx) {
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Return a string describing the CSP defined in <code>this</code>.
     */
    @Override
    public String toString() {
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("\n Solver %s\n", name));
        st.append(String.format("\n[ %d vars -- %d cstrs ]\n", vIdx, cIdx));
        st.append(String.format("Feasability: %s\n", feasible));
        st.append("== variables ==\n");
        for (int v = 0; v < vIdx; v++) {
            st.append(vars[v].toString()).append('\n');
        }
        st.append("== constraints ==\n");
        for (int c = 0; c < cIdx; c++) {
            st.append(cstrs[c].toString()).append('\n');
        }
        return st.toString();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// RELATED TO I/O ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Kicks off the serialization mechanism and flatten the {@code solver} into the given {@code file}.
     *
     * @param solver to flatten
     * @param file   scope file
     * @throws java.io.IOException if an I/O exception occurs.
     */
    public static void writeInFile(final Solver solver, final File file) throws IOException {
        FileOutputStream fos;
        ObjectOutputStream out;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(solver);
        out.close();
    }

    /**
     * Kicks off the serialization mechanism and flatten the {@code model} into a file
     * in the default temporary-file directory.
     *
     * @param solver to flatten
     * @return output file
     * @throws IOException if an I/O exception occurs.
     */
    public static File writeInFile(final Solver solver) throws IOException {
        final File file = File.createTempFile("SOLVER_", ".ser");
        FileOutputStream fos;
        ObjectOutputStream out;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(solver);
        out.close();
        return file;
    }


    /**
     * Restore flatten {@link Solver} from the given {@code file}.
     *
     * @param file input file
     * @return a {@link Solver}
     * @throws IOException            if an I/O exception occurs.
     * @throws ClassNotFoundException if wrong flattened object.
     */
    public static Solver readFromFile(final String file) throws IOException, ClassNotFoundException {
        FileInputStream fis;
        ObjectInputStream in;
        fis = new FileInputStream(file);
        in = new ObjectInputStream(fis);
        final Solver model = (Solver) in.readObject();
        in.close();
        return model;
    }


    /**
     * Duplicate the model declares within <code>this</code>, ie only variables and constraints.
     * Some parameters are reset to default value: search loop (set to binary), explanation engine (set to NONE),
     * propagation engine (set to NONE), objective manager (set to SAT), solution recorder (set to LastSolutionRecorder) and
     * feasibility (set to UNDEFINED).
     * The search strategies and search monitors are simply not reported in the copy.
     * <p>
     * Note that a new instance of the environment is made, preserving the initial choice.
     * <p>
     * Duplicating a solver is only possible before any resolution process began.
     * This is a strong restriction which may be removed in the future.
     * Indeed, duplicating a solver should only be considered while dealing with multi-threading.
     *
     * @return a copy of <code>this</code>
     * @throws org.chocosolver.solver.exception.SolverException if the search has already begun.
     */
    public Solver duplicateModel() {
        if (environment.getWorldIndex() > 0) {
            throw new SolverException("Duplicating a solver cannot be achieved once the resolution has begun.");
        }
        // Create a fresh solver
        Solver clone;
        try {
            clone = new Solver(this.environment.getClass().newInstance(), this.name);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SolverException("The current solver cannot be duplicated:\n" + e.getMessage());
        }

        THashMap<Object, Object> identitymap = new THashMap<>();
        // duplicate variables
        for (int i = 0; i < this.vIdx; i++) {
            this.vars[i].duplicate(clone, identitymap);
        }
        // duplicate constraints
        for (int i = 0; i < this.cIdx; i++) {
            this.cstrs[i].duplicate(clone, identitymap);
            //TODO How to deal with temporary constraints ?
            clone.post((Constraint) identitymap.get(this.cstrs[i]));
        }

        return clone;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>This methods should not be called by the user.</b>
     */
    public int getNbIdElt() {
        return id;
    }

    /**
     * <b>This methods should not be called by the user.</b>
     */
    public int nextId() {
        return id++;
    }

    /**
     * Get the ibex reference
     * Creates one if none
     *
     * @return the ibex reference
     */
    public Ibex getIbex() {
        if (ibex == null) ibex = new Ibex();
        return ibex;
    }
}
