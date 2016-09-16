/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import static org.chocosolver.solver.Solver.Action.extend;
import static org.chocosolver.solver.Solver.Action.initialize;
import static org.chocosolver.solver.Solver.Action.propagate;
import static org.chocosolver.solver.Solver.Action.repair;
import static org.chocosolver.solver.Solver.Action.validate;
import static org.chocosolver.util.ESat.FALSE;
import static org.chocosolver.util.ESat.TRUE;
import static org.chocosolver.util.ESat.UNDEFINED;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.ExplanationEngine;
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
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.criteria.Criterion;

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
public final class Solver implements ISolver, IMeasures, IOutputFactory {

    /** Define the possible actions of SearchLoop */
    protected enum Action {
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
    private Propagate P;

    /** The learning component of this search loop */
    private Learn L;

    /** The moving component of this search loop */
    private Move M;

    /** The declaring model */
    private Model mModel;

    /** The objective manager declare */
    private IObjectiveManager objectivemanager;

    /** The next action to execute in the search <u>loop</u> */
    private Action action;

    /** The measure recorder to keep up to date */
    private MeasuresRecorder mMeasures;

    /** The current decision */
    private DecisionPath dpath;

    /**
     * Index of the initial world, before initialization.
     * May be different from 0 if some external backups have been made.
     */
    private int rootWorldIndex = 0;

    /** Index of the world where the search starts, after initialization. */
    private int searchWorldIndex = 0;

    /**
     * List of stopping criteria.
     * When at least one is satisfied, the search loop ends.
     */
    private List<Criterion> criteria;

    /** Indicates if the default search loop is in use (set to <tt>true</tt> in that case). */
    private boolean defaultSearch = false;

    /** Indicates if a complementary search strategy should be added (set to <tt>true</tt> in that case). */
    private boolean completeSearch = false;

    /** An explanation engine */
    private IExplanationEngine explainer;

    /** List of search monitors attached to this search loop */
    private SearchMonitorList searchMonitors;

    /** The propagation engine to use */
    private IPropagationEngine engine;

    /**
     * Problem feasbility:
     * - UNDEFINED if unknown,
     * - TRUE if satisfiable,
     * - FALSE if unsatisfiable
     */
    private ESat feasible = ESat.UNDEFINED;

    /** Counter that indicates how many world should be rolled back when backtracking */
    private int jumpTo;

    /** Set to <tt>true</tt> to stop the search loop **/
    private boolean stop;

    /** Set to <tt>true</tt> when no more reparation can be achinved, ie entire search tree explored. */
    private boolean canBeRepaired = true;

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
    private boolean searchLoop() {
        boolean solution = false;
        mMeasures.setSearchState(SearchState.RUNNING);
        boolean left = true;
        while (!stop) {
            stop = isStopCriterionMet();
            if (stop || Thread.currentThread().isInterrupted()) {
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
                    break;
                case extend:
                    left = true;
                    searchMonitors.beforeOpenNode();
                    mMeasures.incNodeCount();
                    if (!M.extend(this)) {
                        action = validate;
                    } else {
                        action = propagate;
                    }
                    searchMonitors.afterOpenNode();
                    break;
                case repair:
                    left = false;
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
                    break;
                case validate:
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
                    stop = solution = true;
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
    private void initialize() {
        // for fast construction of "external" constraint, they are initialized once for all
        if (mModel.minisat != null) {
            mModel.minisat.getPropSat().initialize();
        }
        if (mModel.nogoods != null) {
            mModel.nogoods.getPropNogoods().initialize();
        }
        if (mModel.condis != null) {
            mModel.condis.getPropCondis().initialize();
        }
        // note jg : new (used to be in model)
        if (engine == NoPropagationEngine.SINGLETON) {
            this.setEngine(PropagationEngineFactory.DEFAULT.make(mModel));
        }
        engine.initialize();
        getMeasures().setReadingTimeCount(System.currentTimeMillis() - mModel.getCreationTime());
        // end note


        mMeasures.startStopwatch();
        rootWorldIndex = mModel.getEnvironment().getWorldIndex();
        mModel.getEnvironment().buildFakeHistoryOn(mModel.getSettings().getEnvironmentHistorySimulationCondition());
        // Indicates which decision was previously applied before selecting the move.
        // Always sets to ROOT for the first move
        M.setTopDecisionPosition(0);
        mModel.getEnvironment().worldPush(); // store state before initial propagation; w = 0 -> 1
        try {
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
        for (Criterion c : criteria) {
            if (c instanceof ICounter) {
                ((ICounter) c).init();
            }
        }
    }

    /**
     * Close the search:
     * - set satisfaction
     * - update statistics
     */
    private void closeSearch() {
        mMeasures.setSearchState(SearchState.TERMINATED);
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
     * This method enables to reset the search loop, for instance, to solve a problem twice.
     * <ul>
     * <li>It backtracks up to the root node of the search tree,</li>
     * <li>it sets the objective manager to STATISFACTION,</li>
     * <li>it resets the measures,</li>
     * <li>and sets the propagation engine to NoPropagationEngine,</li>
     * </ul>
     */
    public void reset() {
        // if a resolution has already been done
        if (rootWorldIndex > -1) {
            mModel.getEnvironment().worldPopUntil(rootWorldIndex);
            engine.flush();
            dpath.synchronize();
            feasible = ESat.UNDEFINED;
            action = initialize;
            mMeasures.reset();
            canBeRepaired = true; // resetting force to reconsider possible reparation
        }
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
    public boolean hasEndedUnexpectedly() {
        return mMeasures.getSearchState() == SearchState.KILLED;
    }

    /**
     * @return the state of this search. This method is designed for use in monitoring of the system state, not for synchronization control.
     */
    public SearchState getState() {
        return mMeasures.getSearchState();
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
    public void setExplainer(ExplanationEngine explainer) {
        this.explainer = explainer;
    }

    /**
     * Attaches a propagation engine {@code this}.
     * It overrides the previously defined one, if any.
     * @param propagationEngine a propagation strategy
     */
    public void setEngine(IPropagationEngine propagationEngine) {
        this.engine = propagationEngine;
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
