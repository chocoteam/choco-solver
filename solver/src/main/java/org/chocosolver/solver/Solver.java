/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.InvalidSolutionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.objective.IBoundsManager;
import org.chocosolver.solver.objective.IObjectiveManager;
import org.chocosolver.solver.objective.ObjectiveFactory;
import org.chocosolver.solver.propagation.PropagationEngine;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.Reporting;
import org.chocosolver.solver.search.loop.learn.LazyClauseGeneration;
import org.chocosolver.solver.search.loop.learn.Learn;
import org.chocosolver.solver.search.loop.learn.LearnNothing;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorList;
import org.chocosolver.solver.search.loop.move.Move;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.loop.move.MoveSeq;
import org.chocosolver.solver.search.loop.propagate.Propagate;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.chocosolver.solver.search.restart.AbstractRestart;
import org.chocosolver.solver.search.restart.ForceRestartBeforeCut;
import org.chocosolver.solver.search.strategy.BlackBoxConfigurator;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.search.strategy.strategy.WarmStart;
import org.chocosolver.solver.trace.IOutputFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.logger.ANSILogger;
import org.chocosolver.util.logger.Logger;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Stream;

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
public final class Solver implements ISolver, IMeasures, IOutputFactory {

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
     * The learning component of this search loop
     */
    private Learn L;

    /**
     * The moving component of this search loop
     */
    private Move M;

    /**
     * The declaring model
     */
    private final Model mModel;

    /**
     * SAT solver, only for LCG
     */
    private final MiniSat mSat;
    /**
     * The objective manager declare
     */
    @SuppressWarnings({"WeakerAccess", "rawtypes"})
    private IObjectiveManager objectivemanager;

    /**
     * The next action to execute in the search <u>loop</u>
     */
    private Action action;

    /**
     * The measure recorder to keep up to date
     */
    @SuppressWarnings("WeakerAccess")
    private final MeasuresRecorder mMeasures;

    /**
     * The current decision
     */
    @SuppressWarnings("WeakerAccess")
    private final DecisionPath dpath;
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
    private final List<Criterion> criteria;

    /**
     * Indicates if the default search loop is in use (set to <tt>true</tt> in that case).
     */
    private boolean defaultSearch = false;

    /**
     * Indicates if a complementary search strategy should be added (set to <tt>true</tt> in that case).
     */
    private boolean completeSearch = false;

    /**
     * List of search monitors attached to this search loop
     */
    @SuppressWarnings("WeakerAccess")
    private final SearchMonitorList searchMonitors;

    /**
     * The propagation engine to use
     */
    private PropagationEngine engine;
    /**
     * Internal unique contradiction exception, used on propagation failures
     */
    private final ContradictionException exception;
    /**
     * Problem feasbility:
     * - UNDEFINED if unknown,
     * - TRUE if satisfiable,
     * - FALSE if unsatisfiable
     */
    private ESat feasible = ESat.UNDEFINED;

    /**
     * Counter that indicates how many world should be rolled back when backtracking
     */
    private int jumpTo;

    /**
     * Set to <tt>true</tt> to stop the search loop
     **/
    private boolean stop;

    /**
     * Set to <tt>true</tt> when no more reparation can be achieved, ie entire search tree explored.
     */
    private boolean canBeRepaired = true;

    /**
     * The restarting strategy
     */
    private AbstractRestart restarter;

    /**
     * This object is accessible lazily
     */
    private Solution lastSol = null;

    /**
     * Store hints on partial solution, to better start the search
     */
    private WarmStart warmStart = null;

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
    Solver(Model aModel) {
        mModel = aModel;
        exception = new ContradictionException();
        objectivemanager = ObjectiveFactory.SAT();
        dpath = new DecisionPath(aModel.getEnvironment());
        action = initialize;
        mMeasures = new MeasuresRecorder(mModel.getName());
        criteria = new ArrayList<>();
        mMeasures.setSearchState(SearchState.NEW);
        mMeasures.setBoundsManager(objectivemanager);
        searchMonitors = new SearchMonitorList();
        M = new MoveBinaryDFS();
        L = new LearnNothing();
        restarter = AbstractRestart.NO_RESTART;
        if (mModel.getSettings().isLCG()) {
            mSat = new MiniSat(true);
            setLearner(new LazyClauseGeneration(this, mSat));
        } else {
            mSat = null;
        }
        engine = new PropagationEngine(mModel, mSat);
    }

    public void throwsException(ICause c, Variable v, String s) throws ContradictionException {
        throw exception.set(c, v, s);
    }

    public void throwsException(ICause c, Variable v, String s, Reason reason) throws ContradictionException {
        if (isLCG()) {
            mSat.cEnqueue(0, c.manageReification().apply(reason));
        }
        throwsException(c, v, s);
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
     * - push a backup world,
     * - run the initial propagation,
     * - initialize the Move and the search strategy
     */
    private boolean initialize() {
        boolean ok = true;
        checkDeclaredConstraints();
        checkExplainedVariables();
        checkExplainedConstraints();
        engine.initialize();
        getMeasures().setReadingTimeCount(System.nanoTime() - mModel.getCreationTime());
        // end note

        mMeasures.startStopwatch();
        rootWorldIndex = mModel.getEnvironment().getWorldIndex();
        // Indicates which decision was previously applied before selecting the move.
        // Always sets to ROOT for the first move
        M.setTopDecisionPosition(0);
        pushTrail(); // store state before initial propagation; w = 0 -> 1
        try {
            checkTasks();
            mMeasures.incFixpointCount();
            // check sat
            if (isLCG() && !getSat().ok_) {
                this.throwsException(Cause.Sat, null, null);
            }
            doPropagate();
            action = extend;
            pushTrail(); // store state after initial propagation; w = 1 -> 2
            searchWorldIndex = mModel.getEnvironment().getWorldIndex(); // w = 2
            pushTrail(); // store another time for restart purpose: w = 2 -> 3
            L.init();
        } catch (ContradictionException ce) {
            engine.flush();
            mMeasures.incFailCount();
            searchMonitors.onContradiction(ce);
            cancelTrail();
            stop = true;
            ok = false;
        }
        // call to HeuristicVal.update(Action.initial_propagation)
        if (M.getChildMoves().size() <= 1 && M.getStrategy() == null) {
            if (getModel().getSettings().warnUser()) {
                logger.white().println("No search strategies defined.");
                logger.white().println("Set to default ones.");
            }
            defaultSearch = true;
            mModel.getSettings().makeDefaultSearch(mModel);
        }
        preprocessing(getModel().getSettings().getTimeLimitForPreprocessing());
        if (completeSearch && !defaultSearch) {
            BlackBoxConfigurator bb = BlackBoxConfigurator.init();
            bb.complete(mModel, M.getStrategy());
        }
        if (warmStart != null) {
            AbstractStrategy<Variable> declared = M.getStrategy();
            warmStart.setStrategy(declared);
            setSearch(warmStart);
        }
        if (isLCG() && getObjectiveManager().isOptimization()) {
            setRestartOnSolutions();
        }
        restarter.init();
        if (!M.init()) { // the initialisation of the Move and strategy can detect inconsistency
            cancelTrail();
            feasible = FALSE;
            engine.flush();
            getMeasures().incFailCount();
            ok = stop = true;
        }
        criteria.stream().filter(c -> c instanceof ICounter).forEach(c -> ((ICounter) c).init());
        return ok;
    }

    private void checkDeclaredConstraints() {
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
    }

    /**
     * Check the number of explained propagators
     */
    private void checkExplainedVariables() {
        if (isLCG()) {
            HashSet<String> warned = new HashSet<>();
            int c = 0, e = 0;
            for (Variable var : getModel().getVars()) {
                boolean isExplained = false;
                boolean isPartial = false;
                // Check if the instance supports annotations
                Annotation[] annotations = var.getClass().getAnnotations();
                String comment = "";
                for (Annotation annotation : annotations) {
                    if (Objects.equals(annotation.annotationType(), Explained.class)) {
                        isExplained = true;
                        isPartial |= ((Explained) annotation).partial();
                        comment = ((Explained) annotation).comment();
                        e++;
                    }
                }
                c++;
                if (!isExplained) {
                    if (getModel().getSettings().warnUser() && !warned.contains(var.getClass().getSimpleName())) {
                        warned.add(var.getClass().getSimpleName());
                        logger.white().println(
                                "Warning: " + var.getClass().getSimpleName() + " is not explained.");
                    }
                } else if (isPartial) {
                    if (getModel().getSettings().warnUser() && !warned.contains(var.getClass().getSimpleName())) {
                        warned.add(var.getClass().getSimpleName());
                        logger.white().println(
                                "Warning: " + var.getClass().getSimpleName() + " is partially explained" +
                                        (!comment.isEmpty() ? " (" + comment + ".)" : "."));
                    }
                }
            }
            if (getModel().getSettings().warnUser() && e < c) {
                logger.printf(
                        "%.2f%% variables are explained\n", e * 100. / c);
            }
            warned.clear();
        }
    }

    /**
     * Check the number of explained propagators
     */
    private void checkExplainedConstraints() {
        if (isLCG()) {
            HashSet<String> warned = new HashSet<>();
            int c = 0, e = 0;
            for (Constraint cstr : getModel().getCstrs()) {
                for (Propagator<?> propagator : cstr.getPropagators()) {
                    boolean isExplained = false;
                    boolean isPartial = false;
                    // Check if the instance supports annotations
                    Annotation[] annotations = propagator.getClass().getAnnotations();
                    String comment = "";
                    for (Annotation annotation : annotations) {
                        if (Objects.equals(annotation.annotationType(), Explained.class)) {
                            isExplained = true;
                            isPartial |= ((Explained) annotation).partial();
                            comment = ((Explained) annotation).comment();
                            e++;
                        }
                    }
                    c++;
                    if (!isExplained) {
                        if (getModel().getSettings().warnUser() && !warned.contains(propagator.getClass().getSimpleName())) {
                            warned.add(propagator.getClass().getSimpleName());
                            logger.white().println(
                                    "Warning: " + propagator.getClass().getSimpleName() + " is not explained.");
                        }
                    } else if (isPartial) {
                        if (getModel().getSettings().warnUser() && !warned.contains(propagator.getClass().getSimpleName())) {
                            warned.add(propagator.getClass().getSimpleName());
                            logger.white().println(
                                    "Warning: " + propagator.getClass().getSimpleName() + " is partially explained" +
                                            (!comment.isEmpty() ? " (" + comment + ".)" : "."));
                        }
                    }
                }
            }
            if (getModel().getSettings().warnUser() && e < c) {
                logger.printf(
                        "%.2f%% propagators are explained\n", e * 100. / c);
            }
            warned.clear();
        }
    }

    private void checkTasks() throws ContradictionException {
        if (mModel.getHook(Model.TASK_SET_HOOK_NAME) != null) {
            //noinspection unchecked
            ArrayList<Task> tset = (ArrayList<Task>) mModel.getHook(Model.TASK_SET_HOOK_NAME);
            for (int i = 0; i < tset.size(); i++) {
                tset.get(i).ensureBoundConsistency();
            }
        }
    }

    /**
     * Push a world on the environment's stack, and same for MiniSat if relevant
     */
    public void pushTrail() {
        mModel.getEnvironment().worldPush();
        if (isLCG()) {
            mSat.pushTrailMarker();
        }
    }

    /**
     * Cancel the last pushed world, and same for MiniSat if relevant
     */
    public void cancelTrail() {
        mModel.getEnvironment().worldPop();
        if (isLCG() && mSat.trailMarker() > mModel.getEnvironment().getWorldIndex()) {
            // the second condition is required because of LCG
            // (more precisely LazyClauseGeneration.analyse() -> findConflictLevel())
            mSat.cancel();
        }
    }

    /**
     * This method is called after the initial propagation and before the search loop starts.
     * It sequentially applies Arc Consistency on every combination of (variable, value).
     * If a value is not supported by any other variable, it is removed from the domain of the variable.
     * The method ends when the time limit is reached or when all combination have been checked.
     *
     * @implSpec A first propagation must have been done before calling this method.
     */
    public void preprocessing(long timeLimitInMS) {
        if (!getEngine().isInitialized()) {
            throw new SolverException("A call to solver.propagate() must be done before calling solver.preprocessing()");
        }
        if (timeLimitInMS > 0 && getModel().getSettings().warnUser()) {
            logger.white().printf("Running preprocessing step (%dms).\n", timeLimitInMS);
        }
        long tl = System.currentTimeMillis() + timeLimitInMS;
        IntVar[] ivars = mModel.retrieveIntVars(true);
        loop:
        for (int i = 0; i < ivars.length; i++) {
            IntVar v = ivars[i];
            if (!v.isInstantiated()) { // if the variable is not instantiated
                DisposableValueIterator it = v.getValueIterator(true);
                while (it.hasNext()) {
                    if (System.currentTimeMillis() > tl) {
                        break loop;
                    }
                    int a = it.next();
                    if (!hasSupport(v, a)) {
                        try {
                            v.removeValue(a, Cause.Null);
                            if (getModel().getSettings().warnUser()) {
                                logger.white().printf("Preprocessing removed value %d from %s\n", a, v.getName());
                            }
                        } catch (ContradictionException e) {
                            throw new SolverException("Preprocessing failed");
                        }
                    }
                }
                it.dispose();
            }
        }
    }

    private boolean hasSupport(IntVar var, int val) {
        mModel.getEnvironment().worldPush();
        try {
            var.instantiateTo(val, Cause.Null);
            mModel.getSolver().getEngine().propagate();
            return true;
        } catch (ContradictionException e) {
            mModel.getSolver().getEngine().flush();
            return false;
        } finally {
            mModel.getEnvironment().worldPop();
        }
    }

    /**
     * Basic propagation:
     * <ul>
     *     <li>First, prepare the decision (to ensure good behavior of the
     *     {@link org.chocosolver.solver.search.loop.move.Move#repair(Solver)} call)</li>
     *     <li>then, a first propagation ensures that, if learning is on,
     *     the unit no-good learnt on failure filters,
     *     <li>the cut is posted before applying the decision to ensure good nogood,
     *     and a second propagation ensures the cut is taken into account</li>
     *     <li>the decision is applied (if learning is on and the decision is refuted,
     *     it is bypassed by the learnt unit nogood),</li>
     *     <li>finally, a fix point is reached.</li>
     * </ul>
     *
     * @throws ContradictionException if failure occurs during propagation
     */
    private void doPropagate() throws ContradictionException {
        //WARNING: keep the order as is (read javadoc for more details)
        dpath.buildNext();
        objectivemanager.postDynamicCut();
        engine.propagate();
        dpath.apply();
        engine.propagate();
    }

    /**
     * Search loop propagation phase. This needs to be distinguished from {@link #propagate()}
     *
     * @param left true if we are branching on the left false otherwise
     */
    private void propagate(boolean left) {
        searchMonitors.beforeDownBranch(left);
        try {
            mMeasures.incFixpointCount();
            doPropagate();
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
     */
    private void extend() {
        searchMonitors.beforeOpenNode();
        mMeasures.incNodeCount();
        action = propagate;
        if (restarter.mustRestart(this)) {
            this.restart();
            L.forget();
        } else
            if (!M.extend(this)) {
            action = validate;
        }
        searchMonitors.afterOpenNode();
    }

    /**
     * Search loop repair phase
     */
    private void repair() {
        L.record();
        // this is done before the reparation,
        // since restart is a move which can stop the search if the cut fails
        action = propagate;
        searchMonitors.beforeUpBranch();
        if (restarter.mustRestart(this)) {
            canBeRepaired = true;
            this.restart();
        } else {
            canBeRepaired = M.repair(this);
        }
        searchMonitors.afterUpBranch();
        if (!canBeRepaired) {
            stop = true;
        } else {
            L.forget();
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
     * That is, {@link Learn}, {@link Move} and {@link Search} are kept as declared.
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
     *     <li>call {@link Solver#setNoLearning()}</li>
     *     <li>remove warm start hints</li>
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
        setNoLearning();
        //no need to unplug, done by searchMonitors.reset()
        this.lastSol = null;
        if (this.warmStart != null) {
            this.warmStart.clearHints();
            this.warmStart = null;
        }
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
        checkTasks();
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
        pushTrail();
        getMeasures().incRestartCount();
        try {
            objectivemanager.postDynamicCut();
            mMeasures.incFixpointCount();
            doPropagate();
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
            cancelTrail();
        }
        dpath.synchronize();
    }

    /**
     * Actions to perform when a cut is injected in the search loop from an external source.
     * This typically occurs when a solver is declared in a portfolio of solvers.
     * The cut is posted while the search is running.
     *
     * @param newBestVal the new best value found
     */
    public void onReceivingExternalCut(int newBestVal) {
        if (isLCG()) {
            // When LCG is plugged-in, injecting the cut will lead to error.
            // Actually, the current bound of the objective variable could
            // have been as part of reasons for domain reduction. So adding a cut could
            // lead to chronological inconsistency.
            // To prevent such a situation, the search should restart then the cut can be injected.
            ForceRestartBeforeCut rbc = getRestartBeforeCut();
            rbc.storeCut(newBestVal);
        } else {
            // The cut can be injected as soon as it is received.
            this.getObjectiveManager().updateBestSolution(newBestVal);
        }
    }

    /**
     * Return or declare and return the restart strategy that manages the cut injection.
     *
     * @return the restart strategy that manages the cut injection
     */
    private ForceRestartBeforeCut getRestartBeforeCut() {
        AbstractRestart rst = this.getRestarter();
        while (rst != null && !(rst instanceof ForceRestartBeforeCut)) {
            rst = rst.getNext();
        }
        ForceRestartBeforeCut rbc;
        if (rst == null) {
            // create a new instance of ForceRestartBeforeCut
            rbc = new ForceRestartBeforeCut(this);
            // define it as the first restart strategy
            rbc.setNext(restarter);
            this.restarter = rbc;
        } else {
            rbc = (ForceRestartBeforeCut) rst;
        }
        return rbc;
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
     * In case of success, a call moveForward(Decision) is possible.
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
     * @return <i>true</i> if LCG is on, <i>false</i> otherwise
     */
    public boolean isLCG() {
        return mSat != null;
    }

    public MiniSat getSat() {
        return mSat;
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
     * @deprecated
     */
    @Deprecated
    public Propagate getPropagate() {
        return null;
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
     * @deprecated
     */
    @Deprecated
    public void setPropagate(Propagate p) {
    }

    /**
     * Add or complete a restart policy.
     *
     * @param restarter restarter policy
     * @implNote There can be multiple restart policies, stored in as linked list.
     * @see #clearRestarter()
     */
    public void addRestarter(AbstractRestart restarter) {
        if (restarter != AbstractRestart.NO_RESTART) {
            restarter.setNext(this.restarter);
            this.restarter = restarter;
        }
    }

    /**
     * @return the current declared restart policy or {@link AbstractRestart#NO_RESTART}
     */
    public AbstractRestart getRestarter() {
        return this.restarter;
    }

    /**
     * Clear the declared restart strategy.
     * Consequently, no restarting will occur.
     *
     * @implNote replace the declared restart policy by {@link AbstractRestart#NO_RESTART}
     */
    public void clearRestarter() {
        this.restarter = AbstractRestart.NO_RESTART;
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
    @SuppressWarnings({"rawtypes", "ReassignedVariable", "unchecked"})
    public void setSearch(AbstractStrategy... strategies) {
        if (strategies == null || strategies.length == 0) {
            throw new UnsupportedOperationException("no search strategy has been specified");
        }
        if (M.getChildMoves().size() > 1) {
            throw new UnsupportedOperationException("The Move declared is composed of many Moves.\n" +
                    "A strategy must be attached to each of them independently, and it cannot be achieved calling this method." +
                    "An iteration over it child moves is needed: this.getMove().getChildMoves().");
        } else {
            strategies = Arrays.stream(strategies).filter(Objects::nonNull)
                    .flatMap(s -> (s instanceof StrategiesSequencer) ? Arrays.stream(((StrategiesSequencer<?>) s).getStrategies()) : Stream.of(s))
                    .toArray(AbstractStrategy[]::new);
            if (strategies.length == 0) {
                M.removeStrategy();
            } else if (strategies.length == 1) {
                M.setStrategy(strategies[0]);
            } else {
                M.setStrategy(Search.sequencer(strategies));
            }
        }
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
     * Declare a <i>warm start</i> strategy that consists of a set of variables and a set of values.
     * It allows to define either a solution or at least a partial solution in order to drive the search toward
     * a solution.
     * <p> Such a (partial) solution serves only once.
     * <p>Note that a variable can appears more than once.
     */
    public void addHint(IntVar var, int val) {
        if (warmStart == null) {
            warmStart = new WarmStart(this);
        }
        warmStart.addHint(var, val);
    }

    /**
     * Remove declare hints
     */
    public void removeHints() {
        setSearch(warmStart.getStrategy());
        warmStart.clearHints();
        warmStart = null;
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
     * Operation to execute when a solution is found
     *
     * @param r operation to execute
     */
    public void onSolution(Runnable r) {
        searchMonitors.add((IMonitorSolution) r::run);
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

    /**
     * @return whether or not the default solution object has been created
     */
    public boolean defaultSolutionExists() {
        return lastSol != null;
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
     *
     * @param ansi {@code true} to enable colors
     */
    public void logWithANSI(boolean ansi) {
        logger = ansi ? new ANSILogger(logger) : new Logger(logger);
    }
}