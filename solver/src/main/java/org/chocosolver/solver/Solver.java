/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.InvalidSolutionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.learn.AbstractEventObserver;
import org.chocosolver.solver.objective.IBoundsManager;
import org.chocosolver.solver.objective.IObjectiveManager;
import org.chocosolver.solver.objective.ObjectiveFactory;
import org.chocosolver.solver.propagation.PropagationEngine;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.Reporting;
import org.chocosolver.solver.search.loop.learn.Learn;
import org.chocosolver.solver.search.loop.learn.LearnNothing;
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
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.trace.IOutputFactory;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.logger.ANSILogger;
import org.chocosolver.util.logger.Logger;

import java.util.*;

import static org.chocosolver.solver.Solver.Action.*;
import static org.chocosolver.solver.constraints.Constraint.Status.FREE;
import static org.chocosolver.util.ESat.*;

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
 *
 * @author Charles Prud'homme
 * @since 01/09/15.
 */
public class Solver implements ISolver, IMeasures, IOutputFactory {

    /**
     * Define the possible actions of SearchLoop
     */
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
         * fixpoint step
         */
        fixpoint,
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
     * The propagate component of this search loop
     */
    protected Propagate P;

    /**
     * The learning component of this search loop
     */
    protected Learn L;

    /**
     * The moving component of this search loop
     */
    protected Move M;

    /**
     * The declaring model
     */
    protected Model mModel;

    /**
     * The objective manager declare
     */
    @SuppressWarnings("WeakerAccess")
    protected IObjectiveManager objectivemanager;

    /**
     * The next action to execute in the search <u>loop</u>
     */
    protected Action action;

    /**
     * The measure recorder to keep up to date
     */
    @SuppressWarnings("WeakerAccess")
    protected MeasuresRecorder mMeasures;

    /**
     * The current decision
     */
    @SuppressWarnings("WeakerAccess")
    protected DecisionPath dpath;
    /**
     * Index of the initial world, before initialization.
     * May be different from 0 if some external backups have been made.
     */
    private int rootWorldIndex = 0;

    /**
     * Index of the world where the search starts, after initialization.
     */
    private int searchWorldIndex = 0;
    /**
     * List of stopping criteria.
     * When at least one is satisfied, the search loop ends.
     */
    protected List<Criterion> criteria;

    /**
     * Indicates if the default search loop is in use (set to <tt>true</tt> in that case).
     */
    private boolean defaultSearch = false;

    /**
     * Indicates if a complementary search strategy should be added (set to <tt>true</tt> in that case).
     */
    private boolean completeSearch = false;

    /**
     * An events observer
     */
    private AbstractEventObserver eventObserver;

    /**
     * List of search monitors attached to this search loop
     */
    @SuppressWarnings("WeakerAccess")
    protected SearchMonitorList searchMonitors;

    /**
     * The propagation engine to use
     */
    protected PropagationEngine engine;
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

    /**
     * Counter that indicates how many world should be rolled back when backtracking
     */
    private int jumpTo;

    /**
     * Set to <tt>true</tt> to stop the search loop
     **/
    protected boolean stop;

    /**
     * Set to <tt>true</tt> when no more reparation can be achieved, ie entire search tree explored.
     */
    private boolean canBeRepaired = true;

    /**
     * This object is accessible lazily
     */
    private Solution lastSol = null;

    /**
     * Default logger
     */
    private Logger logger = new ANSILogger();

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
        engine = new PropagationEngine(mModel);
        exception = new ContradictionException();
        eventObserver = AbstractEventObserver.SILENT_OBSERVER;
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
        throw exception.set(c, v, s);
    }

    public ContradictionException getContradictionException() {
        return exception;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////     SEARCH LOOP       //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Executes the resolver as it is configured.
     * <p>
     * Default configuration:
     * - SATISFACTION : Computes a feasible solution. Use while(solve()) to enumerate all solutions.
     * - OPTIMISATION : Computes a feasible solution, wrt to the objective defined. Use while(solve()) to find the optimal solution.
     * Indeed, each new solution improves the objective. If no new solution is found (and no stop criterion encountered),
     * the last one is guaranteed to be the optimal one.
     *
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
            boolean ok = initialize();
            searchMonitors.afterInitialize(ok);
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
     *
     * @return <tt>true</tt> if ends on a solution, <tt>false</tt> otherwise
     */
    @SuppressWarnings("WeakerAccess")
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
                case fixpoint:
                    fixpoint();
                    break;
                case extend:
                    left = true;
                    extend();
                    break;
                case repair:
                    left = false;
                    repair();
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
    protected boolean initialize() {
        boolean ok = true;
        if (mModel.getSettings().checkDeclaredConstraints()) {
            //noinspection unchecked
            Set<Constraint> instances = (Set<Constraint>) mModel.getHook("cinstances");
            if (instances != null) {
                Optional<Constraint> undeclared = instances
                        .stream()
                        .filter(c -> (c.getStatus() == FREE))
                        .findFirst();
                if (undeclared.isPresent()) {
                    logger.white().println(
                            "At least one constraint is free, i.e., neither posted or reified. ).");
                    instances
                            .stream()
                            .filter(c -> c.getStatus() == FREE)
                            .limit(mModel.getSettings().printAllUndeclaredConstraints() ? Integer.MAX_VALUE
                                    : 1)
                            .forEach(c -> logger.white().printf(String.format("%s is free\n", c)));
                }
            }
        }
        engine.initialize();
        getMeasures().setReadingTimeCount(System.nanoTime() - mModel.getCreationTime());
        // end note

        mMeasures.startStopwatch();
        rootWorldIndex = mModel.getEnvironment().getWorldIndex();
        // Indicates which decision was previously applied before selecting the move.
        // Always sets to ROOT for the first move
        M.setTopDecisionPosition(0);
        mModel.getEnvironment().worldPush(); // store state before initial propagation; w = 0 -> 1
        try {
            if (mModel.getHook(Model.TASK_SET_HOOK_NAME) != null) {
                //noinspection unchecked
                ArrayList<Task> tset = (ArrayList<Task>) mModel.getHook(Model.TASK_SET_HOOK_NAME);
                for (int i = 0; i < tset.size(); i++) {
                    tset.get(i).ensureBoundConsistency();
                }
            }
            mMeasures.incFixpointCount();
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
            ok = false;
        }
        // call to HeuristicVal.update(Action.initial_propagation)
        if (M.getChildMoves().size() <= 1 && M.getStrategy() == null) {
            if(getModel().getSettings().warnUser()) {
                logger.white().println("No search strategies defined.");
                logger.white().println("Set to default ones.");
            }
            defaultSearch = true;
            setSearch(mModel.getSettings().makeDefaultSearch(mModel));
        }
        if (completeSearch && !defaultSearch) {
            AbstractStrategy<Variable> declared = M.getStrategy();
            AbstractStrategy<?> complete = mModel.getSettings().makeDefaultSearch(mModel);
            setSearch(declared, complete);
        }
        if (!M.init()) { // the initialisation of the Move and strategy can detect inconsistency
            mModel.getEnvironment().worldPop();
            feasible = FALSE;
            engine.flush();
            getMeasures().incFailCount();
            ok = stop = true;
        }
        criteria.stream().filter(c -> c instanceof ICounter).forEach(c -> ((ICounter) c).init());
        return ok;
    }

    /**
     * Search loop propagation phase. This needs to be distinguished from {@link #propagate()}
     *
     * @param left true if we are branching on the left false otherwise
     */
    protected void propagate(boolean left) {
        searchMonitors.beforeDownBranch(left);
        try {
            mMeasures.incFixpointCount();
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

    private void fixpoint() {
        try {
            mMeasures.incFixpointCount();
            objectivemanager.postDynamicCut();
            engine.propagate();
            action = propagate;
        } catch (ContradictionException ce) {
            engine.flush();
            //            mMeasures.incFailCount();
            jumpTo = 1;
            action = repair;
            searchMonitors.onContradiction(ce);
        }
    }

    /**
     * Search loop extend phase
     */
    protected void extend() {
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
     */
    protected void repair() {
        if (L.record(this)) {
            // this is done before the reparation,
            // since restart is a move which can stop the search if the cut fails
            action = fixpoint;
        } else {
            // this is done before the reparation,
            // since restart is a move which can stop the search if the cut fails
            action = propagate;
        }
        searchMonitors.beforeUpBranch();
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
     *
     * @return <code>true</code> if a solution is found
     */
    private boolean validate() {
        if (!getModel().getSettings().checkModel(this)) {
            throw new InvalidSolutionException("The current solution does not satisfy the checker." +
                    "Either (a) the search strategy is not complete or " +
                    "(b) the model is not constrained enough or " +
                    "(c) a constraint's checker (\"isSatisfied()\") is not correct or " +
                    "(d) some constraints' filtering algorithm (\"propagate(...)\") is not correct.\n" +
                    Reporting.fullReport(mModel),
                    mModel);
        }
        feasible = TRUE;
        mMeasures.incSolutionCount();
        if (mModel.getResolutionPolicy() == ResolutionPolicy.SATISFACTION && mMeasures.getSolutionCount() == 1) {
            mMeasures.updateTimeToBestSolution();
        } else if (mModel.getResolutionPolicy() != ResolutionPolicy.SATISFACTION) {
            boolean bestSolutionHasBeenUpdated = objectivemanager.updateBestSolution();
            if (bestSolutionHasBeenUpdated) {
                mMeasures.updateTimeToBestSolution();
            }
        }
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
    private void closeSearch() {
        if (mMeasures.getSearchState() == SearchState.RUNNING) {
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
     *
     * @see #hardReset()
     */
    public void reset() {
        if (rootWorldIndex > -1) {
            mModel.getEnvironment().worldPopUntil(rootWorldIndex);
        }
        searchWorldIndex = 0;
        action = initialize;
        mMeasures.reset();
        engine.reset();
        dpath.synchronize();
        objectivemanager.resetBestBounds();
        removeAllStopCriteria();
        feasible = UNDEFINED;
        jumpTo = 0;
        stop = false;
        canBeRepaired = true;
    }

    /**
     * <p>
     * Resetting a solver to its creation state.
     * </p>
     *
     * <p>
     * For soft reset, see {@link #reset()}.
     * </p>
     * <p>
     * In details, calling this method will, first call {@link #reset()} and then:
     * <ul>
     *     <li>replace {@link #M} by {@link MoveBinaryDFS}</li>
     *     <li>replace {@link #P} by {@link PropagateBasic}</li>
     *     <li>call {@link Solver#setNoLearning()}</li>
     *     <li>clear {@link #searchMonitors}, that forget any declared one</li>
     *     <li>call {@link Model#removeMinisat()}</li>
     * </ul>
     * </p>
     *
     * @see #reset()
     */
    public void hardReset() {
        reset();
        this.M.removeStrategy();
        setMove(new MoveBinaryDFS());
        setPropagate(new PropagateBasic());
        setNoLearning();
        searchMonitors.reset();
        defaultSearch = false;
        completeSearch = false;
        mModel.removeMinisat();
    }

    /**
     * Propagates constraints and related events through the constraint network until a fix point is find,
     * or a contradiction is detected.
     *
     * @throws ContradictionException inconsistency is detected, the problem has no solution with the current set of domains and constraints.
     * @implNote The propagation engine is ensured to be empty (no pending events) after this method.
     * Indeed, if no contradiction occurs, a fix point is reached.
     * Otherwise, a call to {@link PropagationEngine#flush()} is made.
     */
    public void propagate() throws ContradictionException {
        if (!engine.isInitialized()) {
            engine.initialize();
        }
        if (mModel.getHook(Model.TASK_SET_HOOK_NAME) != null) {
            ArrayList<Task> tset = (ArrayList<Task>) mModel.getHook(Model.TASK_SET_HOOK_NAME);
            for (int i = 0; i < tset.size(); i++) {
                tset.get(i).ensureBoundConsistency();
            }
        }
        try {
            engine.propagate();
        } finally {
            engine.flush();
        }
    }

    /**
     * Return the minimum conflicting set from a conflicting set that is causing contradiction.
     *
     * @param conflictingSet the super-set of constraints causing contradiction
     * @return minimumConflictingSet of constraints (the root cause of contradiction)
     * @throws SolverException when MCS is called during solving
     */
    public List<Constraint> findMinimumConflictingSet(List<Constraint> conflictingSet) {
        if (isSolving()) {
            throw new SolverException("Minimum Conflicting Set (MCS) can't be executed during solving");
        }
        return new QuickXPlain(getModel()).findMinimumConflictingSet(conflictingSet);
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
            mMeasures.incFixpointCount();
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
    private void restoreRootNode() {
        IEnvironment environment = mModel.getEnvironment();
        while (environment.getWorldIndex() > searchWorldIndex) {
            getMeasures().incBackTrackCount();
            environment.worldPop();
        }
        dpath.synchronize();
    }


    /**
     * <p>
     * Move forward in the search space by adding a new decision.
     * A call to this method will :
     * <ol>
     *     <li>add <i>dec</i> to the decision path</li>
     *     <li>push a back-up copy of internal states</li>
     *     <li>propagate</li>
     * </ol>
     * <p>
     * Steps 1. and 2. are ignored when <i>dec</i> is <i>null</i>.
     * <p>
     * In case of success, a call {@link #moveForward(Decision)} is possible.
     * Otherwise, a call {@link #moveBackward()} is required to keep on exploring the search space.
     * If no such call is done, the state maybe inconsistent with the decision path.
     * </p>
     * <p>
     * Example of usage: looking for all solutions of a problem.
     * </p>
     * <pre> {@code
     * // Declare model, variables and constraints, then
     * Decision<IntVar> dec = null;
     * boolean search = true;
     * while(search) {
     *     if (solver.moveForward(dec)) {
     *         dec = strategy.getDecision();
     *         if (dec == null) {
     *             // here a solution is found
     *         }else {
     *             continue;
     *         }
     *     }
     *     search = solver.moveBackward();
     *     dec = strategy.getDecision();
     * }
     * }</pre>
     *
     * @param decision decision to add, can be <i>null</i>.
     * @return <i>true</i> if extension is successful, <i>false</i> otherwise.
     * @see #moveBackward()
     * @see #getDecisionPath()
     * @see AbstractStrategy#getDecision()
     */
    public boolean moveForward(Decision<?> decision) {
        if (!engine.isInitialized()) {
            engine.initialize();
        }
        if (this.getEnvironment().getWorldIndex() == 0) {
            this.getEnvironment().worldPush();
        }
        boolean success = true;
        if (decision != null) { // null means there is no more decision
            this.getDecisionPath().pushDecision(decision);
            this.getEnvironment().worldPush();
            this.getDecisionPath().buildNext();
        }
        try {
            this.getDecisionPath().apply();
            this.getObjectiveManager().postDynamicCut();
            this.getEngine().propagate();
        } catch (ContradictionException cex) {
            engine.flush();
            success = false;
        }
        return success;
    }

    /**
     * <p></p>
     * Move backward in the search space.
     * A call to this method will :
     * <ol>
     *     <li>pop the last copy of internal states</li>
     *     <li>refute the last decision of the decision path</li>
     *     <li>propagate</li>
     * </ol>
     * If step 2. is not possible or step 3. throws a failure,
     * the last decision of the decision path is popped and the three-step loop is applied
     * until a successful refutation or emptying decision path.
     * <p>
     * In case of success, a call {@link #moveForward(Decision)} is possible.
     * </p>
     * <p>
     * Example of usage: looking for all solutions of a problem.
     * </p>
     * <pre> {@code
     * // Declare model, variables and constraints, then
     * Decision<IntVar> dec = null;
     * boolean search = true;
     * while(search) {
     *     if (solver.moveForward(dec)) {
     *         dec = strategy.getDecision();
     *         if (dec == null) {
     *             // here a solution is found
     *         }else {
     *             continue;
     *         }
     *     }
     *     search = solver.moveBackward();
     *     dec = strategy.getDecision();
     * }
     * }</pre>
     *
     * @return <i>true</i> in case of success, <i>false</i> otherwise
     * @see #moveForward(Decision)
     * @see #getDecisionPath()
     */
    public boolean moveBackward() {
        this.getEnvironment().worldPop();
        boolean success = false;
        Decision<?> head = dpath.getLastDecision();
        while (!success && head.getPosition() > 0) {
            if (head.hasNext()) {
                this.getEnvironment().worldPush();
                this.getDecisionPath().buildNext();
                try {
                    this.getDecisionPath().apply();
                    this.getObjectiveManager().postDynamicCut();
                    this.getEngine().propagate();
                    success = true;
                } catch (ContradictionException cex) {
                    engine.flush();
                }
            } else {
                dpath.synchronize();
                this.getEnvironment().worldPop();
            }
            head = dpath.getLastDecision();
        }
        return success;
    }

    /**
     * Solving is executing if the search state is different from NEW, that is,
     * if it has started to branch decisions.
     * A double check for execution is done looking if the environment trailing
     * has started as well.
     *
     * @return isSolving if the solver is executing searching or branching
     */
    public boolean isSolving() {
        boolean isSearching = getSearchState() != SearchState.NEW;
        boolean isTrailing = getEnvironment().getWorldIndex() > rootWorldIndex;
        return isSearching || isTrailing;
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
            logger.bold().println(
                    "This search loop is based on a sequential Move, the returned strategy may not reflect the reality.");
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
     *
     * @return false if a specific search strategy is used
     */
    public boolean isDefaultSearchUsed() {
        return defaultSearch;
    }

    /**
     * Indicates if the search strategy is completed with one over all variables
     *
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
     *
     * @return this model's measure recorder
     */
    public MeasuresRecorder getMeasures() {
        //TODO Should the user have write-permission on the solver measures ?
        return mMeasures;
    }

    /**
     * Return the events observer plugged into {@code this}.
     *
     * @return this events observer
     */
    public AbstractEventObserver getEventObserver() {
        return eventObserver;
    }

    /**
     * @return the propagation engine used in {@code this}.
     */
    public PropagationEngine getEngine() {
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
     *
     * @return <tt>ESat.TRUE</tt> if all constraints of the problem are satisfied,
     * <tt>ESat.FLASE</tt> if at least one constraint is not satisfied,
     * <tt>ESat.UNDEFINED</tt> neither satisfiability nor  unsatisfiability could be proven so far.
     */
    public ESat isSatisfied() {
        int OK = 0;
        for (Constraint c : mModel.getCstrs()) {
            if (c.isEnabled()) {
                ESat satC = c.isSatisfied();
                if (FALSE == satC) {
                    if (getModel().getSettings().warnUser()) {
                        logger.bold().red().printf("FAILURE >> %s (%s)%n", c, satC);
                    }
                    return FALSE;
                } else if (TRUE == satC) {
                    OK++;
                }
            } else {
                OK++;
            }
        }
        if (OK == mModel.getCstrs().length) {
            return TRUE;
        } else {
            return UNDEFINED;
        }
    }

    /**
     * @return how many worlds should be rolled back when backtracking (usually 1)
     */
    public int getJumpTo() {
        return jumpTo;
    }

    /**
     * @return <i>true</i> when learning algorithm is not plugged in
     */
    public boolean isLearnOff() {
        return L instanceof LearnNothing;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////        SETTERS        //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Replaces the current learn with {@code l}
     *
     * @param l the new learn to apply
     */
    public void setLearner(Learn l) {
        this.L = l;
    }

    /**
     * Replaces the current move with {@code m}
     *
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
     *
     * @param p the new Propagate to use
     */
    public void setPropagate(Propagate p) {
        this.P = p;
    }

    /**
     * Declares an objective manager to use.
     *
     * @param om the objective manager to use instead of the declared one (if any).
     */
    public void setObjectiveManager(IObjectiveManager<?> om) {
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
            //noinspection unchecked
            M.setStrategy(strategies.length == 1 ? strategies[0] : Search.sequencer(strategies));
        }
    }

    /**
     * Overrides the explanation engine.
     *
     * @param explainer the explanation to use
     */
    public void setEventObserver(AbstractEventObserver explainer) {
        this.eventObserver = explainer;
    }

    /**
     * Attaches a propagation engine {@code this}.
     * It overrides the previously defined one, only
     * if no propagation was done yet.
     * Indeed, some incremental propagators may have set up their internal structure,
     * which cannot be set up twice safely.
     * <p>
     * If propagation was done calling {@link #solve()},
     * calling {@link #reset()} enables to set the propagation engine anew.
     * <p>
     * If propagation was done "manually" (calling {@link #propagate()}, then nothing can be done.
     *
     * @param propagationEngine a propagation strategy
     * @throws SolverException is already initialized.
     */
    public void setEngine(PropagationEngine propagationEngine) {
        if (!engine.isInitialized()
                || getEnvironment().getWorldIndex() == rootWorldIndex) {
            this.engine = propagationEngine;
        } else {
            throw new SolverException("Illegal propagation engine modification.");
        }
    }

    /**
     * Completes (or not) the declared search strategy with one over all variables
     *
     * @param isComplete set to true to complete the current search strategy
     */
    @SuppressWarnings("WeakerAccess")
    public void makeCompleteStrategy(boolean isComplete) {
        this.completeSearch = isComplete;
    }

    /**
     * Adds a stop criterion, which, when met, stops the search loop.
     * There can be multiple stop criteria, a logical OR is then applied.
     * The stop criteria are declared to the search loop just before launching the search,
     * the previously defined ones are erased.
     * <p>
     * There is no check if there are any duplicates.
     * <p>
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
     *
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
     * This is automatically called on {@link #reset()}.
     */
    @SuppressWarnings("WeakerAccess")
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
     * <p>
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
     *
     * @param sm a search monitor to be unplugged in the solver
     */
    public void unplugMonitor(ISearchMonitor sm) {
        searchMonitors.remove(sm);
    }

    /**
     * Empties the list of search monitors.
     */
    @SuppressWarnings("WeakerAccess")
    public void unplugAllSearchMonitors() {
        searchMonitors.reset();
    }

    /**
     * Sets how many worlds to rollback when backtracking
     *
     * @param jto how many worlds to rollback when backtracking
     */
    public void setJumpTo(int jto) {
        this.jumpTo = jto;
    }

    /**
     * The first call to this method will create a new solution based on all variables
     * of the model and attach it to this.
     * Next calls return the solution instance.
     *
     * @return a global solution.
     */
    public Solution defaultSolution() {
        if (lastSol == null) {
            lastSol = new Solution(this.getModel());
            this.attach(lastSol);
        }
        return lastSol;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       FACTORY         //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Solver ref() {
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
    public long getTimeToBestSolutionInNanoSeconds() {
        return getMeasures().getTimeToBestSolutionInNanoSeconds();
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
    public long getBackjumpCount() {
        return getMeasures().getBackjumpCount();
    }

    @Override
    public long getFailCount() {
        return getMeasures().getFailCount();
    }

    @Override
    public long getFixpointCount() {
        return getMeasures().getFixpointCount();
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
        return getDecisionPath().size();
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


    /**
     * Return the current used logger.
     * By default, logger prints to {@link System#out}.
     * Any trace from choco-solver are redirected to this logger.
     *
     * @return the current logger.
     * @see #logWithANSI(boolean)
     */
    public Logger log() {
        return logger;
    }

    /**
     * Defines whether (when {@code ansi} is set to {@code true}) or not
     * ANSI tags are added to any trace from choco-solver.
     * @param ansi {@code true} to enable colors
     */
    public void logWithANSI(boolean ansi) {
        logger = ansi ? new ANSILogger(logger) : new Logger(logger);
    }
}