/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cnf.SatConstraint;
import org.chocosolver.solver.constraints.nary.nogood.NogoodConstraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.IExplanationEngine;
import org.chocosolver.solver.explanations.NoExplanationEngine;
import org.chocosolver.solver.objective.IBoundsManager;
import org.chocosolver.solver.objective.IObjectiveManager;
import org.chocosolver.solver.objective.ObjectiveFactory;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.NoPropagationEngine;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.Reporting;
import org.chocosolver.solver.search.loop.learn.Learn;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorList;
import org.chocosolver.solver.search.loop.move.Move;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.loop.move.MoveSeq;
import org.chocosolver.solver.search.loop.propagate.Propagate;
import org.chocosolver.solver.search.loop.propagate.PropagateBasic;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.trace.IOutputFactory;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.criteria.Criterion;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.chocosolver.solver.Solver.Action.extend;
import static org.chocosolver.solver.Solver.Action.initialize;
import static org.chocosolver.solver.Solver.Action.propagate;
import static org.chocosolver.solver.Solver.Action.repair;
import static org.chocosolver.solver.Solver.Action.validate;
import static org.chocosolver.solver.constraints.Constraint.Status.FREE;
import static org.chocosolver.util.ESat.FALSE;
import static org.chocosolver.util.ESat.TRUE;
import static org.chocosolver.util.ESat.UNDEFINED;

/**
 * This class is inspired from :
 * <cite>
 * Inspired from "Unifying search algorithms for CSP" N. Jussien and O. Lhomme, Technical report 02-3-INFO, EMN
 * </cite>
 * <p>
 * It declares a search loop made of three components:
 * <ul>
 * <li>
 * Propagate: it aims at propagating information throughout the constraint network when a decision is made,
 * </li>
 * <li>
 * Learn: it aims at ensuring that the search mechanism will avoid (as much as possible) to get back to states that have been explored and proved to be solution-less,
 * </li>
 * <li>
 * Move: aims at, unlike other ones, not pruning the search space but rather exploring it.
 * </li>
 * <p>
 * </ul>
 * <p>
 * Created by cprudhom on 01/09/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 01/09/15.
 */
public class Solver implements ISolver, IMeasures, IOutputFactory {

    /** Define the possible actions of SearchLoop */
    public enum Action {
        /**
         * Initialization step
         */
        initialize,
        /**
         * propagation step
         */
        propagate,
        /**
         * extension step
         */
        extend,
        /**
         * validation step
         */
        validate,
        /**
         * reparation step
         */
        repair
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////    PRIVATE FIELDS     //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The standard output stream (default: System.out)
     */
    private transient PrintStream out = System.out;

    /**
     * The standard error stream (default: System.err)
     */
    private transient PrintStream err = System.err;

    /** The propagate component of this search loop */
    protected Propagate P;

    /** The learning component of this search loop */
    protected Learn L;

    /** The moving component of this search loop */
    protected Move M;

    /** The declaring model */
    protected Model mModel;

    /** The objective manager declare */
    protected IObjectiveManager objectivemanager;

    /** The next action to execute in the search <u>loop</u> */
    protected Action action;

    /** The measure recorder to keep up to date */
    protected MeasuresRecorder mMeasures;

    /** The current decision */
    protected DecisionPath dpath;

    /**
     * Index of the initial world, before initialization.
     * May be different from 0 if some external backups have been made.
     */
    protected int rootWorldIndex = 0;

    /** Index of the world where the search starts, after initialization. */
    protected int searchWorldIndex = 0;

    /**
     * List of stopping criteria.
     * When at least one is satisfied, the search loop ends.
     */
    protected List<Criterion> criteria;

    /** Indicates if the default search loop is in use (set to <tt>true</tt> in that case). */
    protected boolean defaultSearch = false;

    /** Indicates if a complementary search strategy should be added (set to <tt>true</tt> in that case). */
    protected boolean completeSearch = false;

    /** An explanation engine */
    protected IExplanationEngine explainer;

    /** List of search monitors attached to this search loop */
    protected SearchMonitorList searchMonitors;

    /** The propagation engine to use */
    protected IPropagationEngine engine;
    /**
     * Internal unique contradiction exception, used on propagation failures
     */
    protected final ContradictionException exception;
    /**
     * Problem feasbility:
     * - UNDEFINED if unknown,
     * - TRUE if satisfiable,
     * - FALSE if unsatisfiable
     */
    protected ESat feasible = ESat.UNDEFINED;

    /** Counter that indicates how many world should be rolled back when backtracking */
    protected int jumpTo;

    /** Set to <tt>true</tt> to stop the search loop **/
    protected boolean stop;

    /** Set to <tt>true</tt> when no more reparation can be achieved, ie entire search tree explored. */
    protected boolean canBeRepaired = true;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////      CONSTRUCTOR      //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create a resolver based for the model <i>aModel</i>.
     *
     * @param aModel the target model
     */
    protected Solver(Model aModel) {
        mModel = aModel;
        engine = NoPropagationEngine.SINGLETON;
        exception = new ContradictionException();
        explainer = NoExplanationEngine.SINGLETON;
        objectivemanager = ObjectiveFactory.SAT();
        dpath = new DecisionPath(aModel.getEnvironment());
        action = initialize;
        mMeasures = new MeasuresRecorder(mModel.getName());
        criteria = new ArrayList<>();
        mMeasures.setSearchState(SearchState.NEW);
        mMeasures.setBoundsManager(objectivemanager);
        searchMonitors = new SearchMonitorList();
        setMove(new MoveBinaryDFS());
        setPropagate(new PropagateBasic());
        setNoLearning();
    }

    public void throwsException(ICause c, Variable v, String s) throws ContradictionException {
        throw exception.set(c,v,s);
    }

    public ContradictionException  getContradictionException() {
        return exception;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////     SEARCH LOOP       //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Executes the resolver as it is configured.
     *
     * Default configuration:
     * - SATISFACTION : Computes a feasible solution. Use while(solve()) to enumerate all solutions.
     * - OPTIMISATION : Computes a feasible solution, wrt to the objective defined. Use while(solve()) to find the optimal solution.
     * Indeed, each new solution improves the objective. If no new solution is found (and no stop criterion encountered),
     * the last one is guaranteed to be the optimal one.
     * @return if at least one new solution has been found.
     */
    public boolean solve() {
        mMeasures.setSearchState(SearchState.RUNNING);
        // prepare
        boolean satPb = getModel().getResolutionPolicy() == ResolutionPolicy.SATISFACTION;
        if (getModel().getObjective() == null && !satPb) {
            throw new SolverException("No objective variable has been defined whereas policy implies optimization");
        }
        stop = !canBeRepaired;
        if (action == initialize) {
            searchMonitors.beforeInitialize();
            initialize();
            searchMonitors.afterInitialize();
        }
        // solve
        boolean newSolutionFound = searchLoop();
        // close
        searchMonitors.beforeClose();
        closeSearch();
        searchMonitors.afterClose();
        // restoration
        return newSolutionFound;
    }

    /**
     * Executes the search loop
     * @return <tt>true</tt> if ends on a solution, <tt>false</tt> otherwise
     */
    public boolean searchLoop() {
        boolean solution = false;
        boolean left = true;
        Thread th = Thread.currentThread();
        while (!stop) {
            stop = isStopCriterionMet();
            if (stop || th.isInterrupted()) {
                if (stop) {
                    mMeasures.setSearchState(SearchState.STOPPED);
                } else {
                    mMeasures.setSearchState(SearchState.KILLED);
                }
            }
            switch (action) {
                case initialize:
                    throw new UnsupportedOperationException("should not initialize during search loop");
                case propagate:
                    propagate(left);
                    break;
                case extend:
                    left = true;
                    extend(left);
                    break;
                case repair:
                    left = false;
                    repair(left);
                    break;
                case validate:
                    stop = solution = validate();
                    break;
                default:
                    throw new SolverException("Invalid Solver loop action " + action);
            }
        }
        return solution;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////      MAIN METHODS     //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Preparation of the search:
     * - start time recording,
     * - store root world
     * - push a back up world,
     * - run the initial propagation,
     * - initialize the Move and the search strategy
     */
    protected void initialize() {
        if (mModel.getSettings().checkDeclaredConstraints() && mModel.getSettings().warnUser()) {
            Set<Constraint> instances = (Set<Constraint>) mModel.getHook("cinstances");
            instances
                    .stream()
                    .filter(c -> c.getStatus() == FREE)
                    .forEach(c -> getErr().printf("%s is free (neither posted or reified).\n", c.getName()));
        }

        // for fast construction of "external" constraint, they are initialized once for all
        if(mModel.getHook(Model.MINISAT_HOOK_NAME) != null){
            SatConstraint minisat = (SatConstraint) mModel.getHook(Model.MINISAT_HOOK_NAME);
            minisat.getPropSat().initialize();
        }
        if(mModel.getHook(Model.NOGOODS_HOOK_NAME) != null){
            NogoodConstraint nogoods = (NogoodConstraint) mModel.getHook(Model.NOGOODS_HOOK_NAME);
            nogoods.getPropNogoods().initialize();
        }
        // note jg : new (used to be in model)
        if (engine == NoPropagationEngine.SINGLETON) {
            this.setEngine(PropagationEngineFactory.DEFAULT.make(mModel));
        }
        engine.initialize();
        getMeasures().setReadingTimeCount(System.nanoTime() - mModel.getCreationTime());
        // end note

        mMeasures.startStopwatch();
        rootWorldIndex = mModel.getEnvironment().getWorldIndex();
        mModel.getEnvironment().buildFakeHistoryOn(mModel.getSettings().getEnvironmentHistorySimulationCondition());
        // Indicates which decision was previously applied before selecting the move.
        // Always sets to ROOT for the first move
        M.setTopDecisionPosition(0);
        mModel.getEnvironment().worldPush(); // store state before initial propagation; w = 0 -> 1
        try {
            if(mModel.getHook(Model.TASK_SET_HOOK_NAME) != null){
                ArrayList<Task> tset = (ArrayList<Task>) mModel.getHook(Model.TASK_SET_HOOK_NAME);
                for(int i = 0; i< tset.size(); i++){
                    tset.get(i).ensureBoundConsistency();
                }
            }
            P.execute(this);
            action = extend;
            mModel.getEnvironment().worldPush(); // store state after initial propagation; w = 1 -> 2
            searchWorldIndex = mModel.getEnvironment().getWorldIndex(); // w = 2
            mModel.getEnvironment().worldPush(); // store another time for restart purpose: w = 2 -> 3
        } catch (ContradictionException ce) {
            engine.flush();
            mMeasures.incFailCount();
            searchMonitors.onContradiction(ce);
            L.record(this);
            L.forget(this);
            mModel.getEnvironment().worldPop();
            stop = true;
        }
        // call to HeuristicVal.update(Action.initial_propagation)
        if (M.getChildMoves().size() <= 1 && M.getStrategy() == null) {
            if (mModel.getSettings().warnUser()) {
                getErr().printf("No search strategies defined.\nSet to default ones.");
            }
            defaultSearch = true;
            setSearch(mModel.getSettings().makeDefaultSearch(mModel));
        }
        if (completeSearch && !defaultSearch) {
            AbstractStrategy<Variable> declared = M.getStrategy();
            AbstractStrategy complete = mModel.getSettings().makeDefaultSearch(mModel);
            setSearch(declared, complete);
        }
        if (!M.init()) { // the initialisation of the Move and strategy can detect inconsistency
            mModel.getEnvironment().worldPop();
            feasible = FALSE;
            engine.flush();
            getMeasures().incFailCount();
            stop = true;
        }
        criteria.stream().filter(c -> c instanceof ICounter).forEach(c -> ((ICounter) c).init());
    }

    /**
     * Search loop propagation phase. This needs to be distinguished from {@link #propagate()}
     * @param left true if we are branching on the left false otherwise
     */
    protected void propagate(boolean left){
        searchMonitors.beforeDownBranch(left);
        mMeasures.incDepth();
        try {
            P.execute(this);
            action = extend;
        } catch (ContradictionException ce) {
            engine.flush();
            mMeasures.incFailCount();
            jumpTo = 1;
            action = repair;
            searchMonitors.onContradiction(ce);
        }
        searchMonitors.afterDownBranch(left);
    }

    /**
     * Search loop extend phase
     * @param left true if we are branching on the left false otherwise
     */
    protected void extend(boolean left){
        searchMonitors.beforeOpenNode();
        mMeasures.incNodeCount();
        if (!M.extend(this)) {
            action = validate;
        } else {
            action = propagate;
        }
        searchMonitors.afterOpenNode();
    }

    /**
     * Search loop repair phase
     * @param left true if we are branching on the left false otherwise
     */
    protected void repair(boolean left){
        L.record(this);
        searchMonitors.beforeUpBranch();
        // this is done before the reparation,
        // since restart is a move which can stop the search if the cut fails
        action = propagate;
        canBeRepaired = M.repair(this);
        searchMonitors.afterUpBranch();
        if (!canBeRepaired) {
            stop = true;
        } else {
            L.forget(this);
        }
    }

    /**
     * Search loop validate phase
     * @return
     */
    protected boolean validate(){
        if (!getModel().getSettings().checkModel(this)) {
            throw new SolverException("The current solution does not satisfy the checker.\n" +
                    Reporting.fullReport(mModel));
        }
        feasible = TRUE;
        mMeasures.incSolutionCount();
        objectivemanager.updateBestSolution();
        searchMonitors.onSolution();
        jumpTo = 1;
        action = repair;
        return true;
    }

    /**
     * Close the search:
     * - set satisfaction
     * - update statistics
     */
    protected void closeSearch() {
        if(mMeasures.getSearchState() == SearchState.RUNNING){
            mMeasures.setSearchState(SearchState.TERMINATED);
        }
        feasible = FALSE;
        if (mMeasures.getSolutionCount() > 0) {
            feasible = TRUE;
            if (objectivemanager.isOptimization()) {
                mMeasures.setObjectiveOptimal(!isStopCriterionMet());
            }
        } else if (isStopCriterionMet()) {
            mMeasures.setObjectiveOptimal(false);
            feasible = UNDEFINED;
        }
    }

    /**
     * <p>
     * Resetting a solver to the state just before running the last resolution instruction.
     * That is, {@link Propagate}, {@link Learn}, {@link Move} and {@link Search} are kept as declared.
     * {@link ISearchMonitor} are also kept plugged to the search loop.
     * </p>
     * <p>
     * For hard reset, see {@link #hardReset()}.
     * </p>
     * In details, calling this method will:
     * <ul>
     *     <li>backtrack to {@link #rootWorldIndex}</li>
     *     <li>set {@link #searchWorldIndex} to 0</li>
     *     <li>set {@link #action} to {@link Action#initialize}</li>
     *     <li>reset {@link #mMeasures}</li>
     *     <li>flush {@link #engine}</li>
     *     <li>synchronize {@link #dpath} to erase out-dated decisions, presumably all of them</li>
     *     <li>reset bounds of {@link #objectivemanager} (calling {@link IObjectiveManager#resetBestBounds()}</li>
     *     <li>remove all stop criteria {@link #removeAllStopCriteria()}</li>
     *     <li>set {@link #feasible} to {@link ESat#UNDEFINED}</li>
     * </ul>
     * @see #hardReset()
     */
    public void reset() {
        if(rootWorldIndex > -1){
            mModel.getEnvironment().worldPopUntil(rootWorldIndex);
        }
        searchWorldIndex = 0;
        action = initialize;
        mMeasures.reset();
        engine.flush();
        dpath.synchronize();
        objectivemanager.resetBestBounds();
        removeAllStopCriteria();
        feasible = ESat.UNDEFINED;
        jumpTo = 0;
        stop = false;
        canBeRepaired = true;
    }

    /**
     * <p>
     *  Resetting a solver to its creation state.
     * </p>
     *
     * <p>
     *  For soft reset, see {@link #reset()}.
     * </p>
     * <p>
     *     In details, calling this method will, first call {@link #reset()} and then:
     * <ul>
     *     <li>replace {@link #M} by {@link MoveBinaryDFS}</li>
     *     <li>replace {@link #P} by {@link PropagateBasic}</li>
     *     <li>call {@link Solver#setNoLearning()}</li>
     *     <li>clear {@link #searchMonitors}, that forget any declared one</li>
     *     <li>call {@link Model#removeMinisat()}</li>
     *     <li>call {@link Model#removeNogoodStore()}</li>
     * </ul>
     * </p>
     * @see #reset()
     */
    public void hardReset() {
        reset();
        setMove(new MoveBinaryDFS());
        setPropagate(new PropagateBasic());
        setNoLearning();
        searchMonitors.reset();
        defaultSearch = false;
        completeSearch = false;
        mModel.removeMinisat();
        mModel.removeNogoodStore();
    }

    /**
     * Propagates constraints and related events through the constraint network until a fix point is find,
     * or a contradiction is detected.
     *
     * @throws ContradictionException inconsistency is detected, the problem has no solution with the current set of domains and constraints.
     */
    public void propagate() throws ContradictionException {
        if (engine == NoPropagationEngine.SINGLETON) {
            setEngine(PropagationEngineFactory.DEFAULT.make(mModel));
        }
        if (!engine.isInitialized()) {
            engine.initialize();
        }
        engine.propagate();
    }

    /**
     * Sets the following action in the search to be a restart instruction.
     * Note that the restart may not be immediate
     */
    public void restart() {
        searchMonitors.beforeRestart();
        restoreRootNode();
        mModel.getEnvironment().worldPush();
        getMeasures().incRestartCount();
        try {
            objectivemanager.postDynamicCut();
            P.execute(this);
            action = extend;
        } catch (ContradictionException e) {
            // trivial inconsistency is detected, due to the cut
            stop = true;
        }
        searchMonitors.afterRestart();
    }

    /**
     * Retrieves the state of the root node (after the initial propagation)
     * Has an immediate effect
     */
    public void restoreRootNode() {
        mModel.getEnvironment().worldPopUntil(searchWorldIndex); // restore state after initial propagation
        dpath.synchronize();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////        GETTERS        //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return the model of this resolver
     */
    public Model getModel() {
        return mModel;
    }

    /**
     * @return the current learn.
     */
    public Learn getLearner() {
        return L;
    }

    /**
     * @return the current move.
     */
    public Move getMove() {
        return M;
    }

    /**
     * @return the current propagate.
     */
    public Propagate getPropagate() {
        return P;
    }

    /**
     * @return the backtracking environment used for this solver
     */
    public IEnvironment getEnvironment() {
        return getModel().getEnvironment();
    }

    /**
     * @return the current decision path
     */
    public DecisionPath getDecisionPath() {
        return dpath;
    }

    /**
     * @param <V> kind of variables the search strategy deals with
     * @return the current search strategy in use
     */
    public <V extends Variable> AbstractStrategy<V> getSearch() {
        if (M.getChildMoves().size() > 1 && mModel.getSettings().warnUser()) {
            err.print("This search loop is based on a sequential Move, the strategy returned may not reflect the reality.");
        }
        return M.getStrategy();
    }

    /**
     * @param <V> type of the objective variable
     * @return the currently used objective manager
     */
    @SuppressWarnings("unchecked")
    public <V extends Variable> IObjectiveManager<V> getObjectiveManager() {
        return objectivemanager;
    }

    /**
     * Indicates if the default search strategy is used
     * @return false if a specific search strategy is used
     */
    public boolean isDefaultSearchUsed() {
        return defaultSearch;
    }

    /**
     * Indicates if the search strategy is completed with one over all variables
     * @return false if no strategy over all variables complete the declared one
     */
    public boolean isSearchCompleted() {
        return completeSearch;
    }

    /**
     * @return <tt>true</tt> if the search loops ends unexpectedly (externally killed, for instance).
     */
    @SuppressWarnings("unused")
    public boolean hasEndedUnexpectedly() {
        return mMeasures.getSearchState() == SearchState.KILLED;
    }

    /**
     * @return <tt>true</tt> if the search loops encountered at least one of the stop criteria declared.
     */
    public boolean isStopCriterionMet() {
        boolean ismet = false;
        for (int i = 0; i < criteria.size() && !ismet; i++) {
            ismet = criteria.get(i).isMet();
        }
        return ismet;
    }

    /**
     * @return the index of the world where the search starts, after initialization.
     */
    public int getSearchWorldIndex() {
        return searchWorldIndex;
    }

    /**
     * Returns a reference to the measures recorder.
     * This enables to get, for instance, the number of solutions found, time count, etc.
     * @return this model's measure recorder
     */
    public MeasuresRecorder getMeasures() {
        //TODO Should the user have write-permission on the solver measures ?
        return mMeasures;
    }

    /**
     * Return the explanation engine plugged into {@code this}.
     * @return this model's explanation engine
     */
    public IExplanationEngine getExplainer() {
        return explainer;
    }

    /**
     * @return the propagation engine used in {@code this}.
     */
    public IPropagationEngine getEngine() {
        return engine;
    }

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
     * Return the current state of the CSP.
     * <p>
     * Given the current domains, it can return a value among:
     * <br/>- {@link ESat#TRUE}: all constraints of the CSP are satisfied for sure,
     * <br/>- {@link ESat#FALSE}: at least one constraint of the CSP is not satisfied.
     * <br/>- {@link ESat#UNDEFINED}: neither satisfiability nor  unsatisfiability could be proven so far.
     * <p>
     * Presumably, not all variables are instantiated.
     * @return <tt>ESat.TRUE</tt> if all constraints of the problem are satisfied,
     * <tt>ESat.FLASE</tt> if at least one constraint is not satisfied,
     * <tt>ESat.UNDEFINED</tt> neither satisfiability nor  unsatisfiability could be proven so far.
     */
    public ESat isSatisfied() {
        if (feasible != ESat.FALSE) {
            int OK = 0;
            for (Constraint c : mModel.getCstrs()) {
                ESat satC = c.isSatisfied();
                if (ESat.FALSE == satC) {
                    System.err.println(String.format("FAILURE >> %s (%s)", c.toString(), satC));
                    return ESat.FALSE;
                } else if (ESat.TRUE == satC) {
                    OK++;
                }
            }
            if (OK == mModel.getCstrs().length) {
                return ESat.TRUE;
            } else {
                return ESat.UNDEFINED;
            }
        }
        return ESat.FALSE;
    }

    /**
     * @return how many worlds should be rolled back when backtracking (usually 1)
     */
    public int getJumpTo() {
        return jumpTo;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////        SETTERS        //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Replaces the current learn with {@code l}
     * @param l the new learn to apply
     */
    public void setLearner(Learn l) {
        this.L = l;
    }

    /**
     * Replaces the current move with {@code m}
     * @param m the new move to apply
     */
    public void setMove(Move... m) {
        if (m == null) {
            this.M = null;
        } else if (m.length == 1) {
            this.M = m[0];
        } else {
            this.M = new MoveSeq(getModel(), m);
        }
    }

    /**
     * Overrides the Propagate object
     * @param p the new Propagate to use
     */
    public void setPropagate(Propagate p) {
        this.P = p;
    }

    /**
     * Declares an objective manager to use.
     * @param om the objective manager to use instead of the declared one (if any).
     */
    public void setObjectiveManager(IObjectiveManager om) {
        this.objectivemanager = om;
        mMeasures.setBoundsManager(om);
    }

    /**
     * Override the default search strategies to use in {@code this}.
     * In case many strategies are given, they will be called in sequence:
     * The first strategy in parameter is first called to compute a decision, if possible.
     * If it cannot provide a new decision, the second strategy is called ...
     * and so on, until the last strategy.
     * <p>
     *
     * @param strategies the search strategies to use.
     */
    public void setSearch(AbstractStrategy... strategies) {
        if (strategies == null || strategies.length == 0) {
            throw new UnsupportedOperationException("no search strategy has been specified");
        }
        if (M.getChildMoves().size() > 1) {
            throw new UnsupportedOperationException("The Move declared is composed of many Moves.\n" +
                    "A strategy must be attached to each of them independently, and it cannot be achieved calling this method." +
                    "An iteration over it child moves is needed: this.getMove().getChildMoves().");
        } else {
            M.setStrategy(strategies.length == 1 ? strategies[0] : Search.sequencer(strategies));
        }
    }

    /**
     * Overrides the explanation engine.
     * @param explainer the explanation to use
     */
    public void setExplainer(IExplanationEngine explainer) {
        this.explainer = explainer;
    }

    /**
     * Attaches a propagation engine {@code this}.
     * It overrides the previously defined one, only
     * if no propagation was done yet.
     * Indeed, some incremental propagators may have set up their internal structure,
     * which cannot be set up twice safely.
     *
     * If propagation was done calling {@link #solve()},
     * calling {@link #reset()} enables to set the propagation engine anew.
     *
     * If propagation was done "manually" (calling {@link #propagate()}, then nothing can be done.
     *
     * @param propagationEngine a propagation strategy
     * @exception SolverException if the current propagation is not {@link NoPropagationEngine#SINGLETON}
     * and is already initialized.
     */
    public void setEngine(IPropagationEngine propagationEngine) {
        if (engine == NoPropagationEngine.SINGLETON
                || !engine.isInitialized()
                || getEnvironment().getWorldIndex() == rootWorldIndex
                || propagationEngine == NoPropagationEngine.SINGLETON) {
            this.engine = propagationEngine;
        }else{
            throw new SolverException("Illegal propagation engine modification.");
        }
    }

    /**
     * Completes (or not) the declared search strategy with one over all variables
     * @param isComplete set to true to complete the current search strategy
     */
    public void makeCompleteStrategy(boolean isComplete) {
        this.completeSearch = isComplete;
    }

    /**
     * Adds a stop criterion, which, when met, stops the search loop.
     * There can be multiple stop criteria, a logical OR is then applied.
     * The stop criteria are declared to the search loop just before launching the search,
     * the previously defined ones are erased.
     *
     * There is no check if there are any duplicates.
     *
     * <br/>
     * Examples:
     * <br/>
     * With a built-in counter, stop after 20 seconds:
     * <pre>
     *         SMF.limitTime(solver, "20s");
     * </pre>
     * With lambda, stop when 10 nodes are visited:
     * <pre>
     *     () -> solver.getNodeCount() >= 10
     * </pre>
     *
     * @param criterion one or many stop criterion to add.
     * @see #removeStopCriterion(Criterion...)
     * @see #removeAllStopCriteria()
     */
    public void addStopCriterion(Criterion... criterion) {
        if (criterion != null) {
            Collections.addAll(criteria, criterion);
        }
    }

    /**
     * Removes one or many stop criterion from the one to declare to the search loop.
     * @param criterion criterion to remove
     */
    public void removeStopCriterion(Criterion... criterion) {
        if (criterion != null) {
            for (Criterion c : criterion) {
                criteria.remove(c);
            }
        }
    }

    /**
     * Empties the list of stop criteria declared.
     * This is not automatically called on {@link #reset()}.
     */
    public void removeAllStopCriteria() {
        this.criteria.clear();
    }

    /**
     * @return the list of search monitors plugged in this resolver
     */
    public SearchMonitorList getSearchMonitors() {
        return searchMonitors;
    }

    /**
     * Put a search monitor to react on search events (solutions, decisions, fails, ...).
     * Any search monitor is actually plugged just before the search starts.
     *
     * There is no check if there are any duplicates.
     * A search monitor added during while the resolution has started will not be taken into account.
     *
     * @param sm a search monitor to be plugged in the solver
     */
    public void plugMonitor(ISearchMonitor sm) {
        searchMonitors.add(sm);
    }

    /**
     * Removes a search monitors from the ones to plug when the search will start.
     * @param sm a search monitor to be unplugged in the solver
     */
    public void unplugMonitor(ISearchMonitor sm) {
        searchMonitors.remove(sm);
    }

    /**
     * Empties the list of search monitors.
     */
    public void unplugAllSearchMonitors() {
        searchMonitors.reset();
    }

    /**
     * Sets how many worlds to rollback when backtracking
     * @param jto how many worlds to rollback when backtracking
     */
    public void setJumpTo(int jto) {
        this.jumpTo = jto;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       FACTORY         //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Solver _me() {
        return this;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       MEASURES        //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String getModelName() {
        return getMeasures().getModelName();
    }

    @Override
    public long getTimestamp() {
        return getMeasures().getTimestamp();
    }

    @Override
    public float getTimeCount() {
        return getMeasures().getTimeCount();
    }

    @Override
    public long getTimeCountInNanoSeconds() {
        return getMeasures().getTimeCountInNanoSeconds();
    }


    @Override
    public long getReadingTimeCountInNanoSeconds() {
        return getMeasures().getReadingTimeCountInNanoSeconds();
    }

    @Override
    public float getReadingTimeCount() {
        return getMeasures().getReadingTimeCount();
    }

    @Override
    public long getNodeCount() {
        return getMeasures().getNodeCount();
    }

    @Override
    public long getBackTrackCount() {
        return getMeasures().getBackTrackCount();
    }

    @Override
    public long getFailCount() {
        return getMeasures().getFailCount();
    }

    @Override
    public long getRestartCount() {
        return getMeasures().getRestartCount();
    }

    @Override
    public long getSolutionCount() {
        return getMeasures().getSolutionCount();
    }

    @Override
    public long getDecisionCount() {
        return getMeasures().getDecisionCount();
    }

    @Override
    public long getMaxDepth() {
        return getMeasures().getMaxDepth();
    }

    @Override
    public long getCurrentDepth() {
        return getMeasures().getCurrentDepth();
    }

    @Override
    public boolean hasObjective() {
        return getMeasures().hasObjective();
    }

    @Override
    public boolean isObjectiveOptimal() {
        return getMeasures().isObjectiveOptimal();
    }

    @Override
    public Number getBestSolutionValue() {
        return getMeasures().getBestSolutionValue();
    }

    @Override
    public SearchState getSearchState() {
        return getMeasures().getSearchState();
    }

    /**
     * @return the currently used objective manager
     */
    @Override
    public IBoundsManager getBoundsManager() {
        assert getMeasures().getBoundsManager() == objectivemanager;
        return getMeasures().getBoundsManager();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       OUTPUT        ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setOut(PrintStream printStream) {
        out = printStream;
    }

    @Override
    public PrintStream getOut() {
        return out;
    }

    @Override
    public void setErr(PrintStream printStream) {
        err = printStream;
    }

    @Override
    public PrintStream getErr() {
        return out;
    }

}
