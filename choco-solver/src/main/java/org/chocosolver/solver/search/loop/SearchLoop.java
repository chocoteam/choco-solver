/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.bind.DefaultSearchBinder;
import org.chocosolver.solver.search.bind.ISearchBinder;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorList;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.tools.ArrayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.solver.objective.ObjectiveManager.SAT;
import static org.chocosolver.solver.propagation.NoPropagationEngine.SINGLETON;
import static org.chocosolver.solver.search.loop.Reporting.fullReport;
import static org.chocosolver.solver.search.loop.SearchLoop.Action.*;
import static org.chocosolver.solver.search.strategy.decision.RootDecision.ROOT;
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
 * @author Charles Prud'homme
 */
public final class SearchLoop implements Serializable {

    /**
     * Define the possible actions of SearchLoop
     */
    protected enum Action {
        /**
         * initialization step
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
        repair,
        /**
         * ending step
         */
        stop,
    }

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
     * The declaring solver
     */
    protected Model mModel;
    /**
     * The objective manager declare
     */
    protected ObjectiveManager objectivemanager;

    /**
     * The next action to execute in the search <u>loop</u>
     */
    protected Action action;

    /**
     * The measure recorder to keep up to date
     */
    protected IMeasures mMeasures;

    /**
     * The current decision
     */
    protected Decision decision;

    /**
     * Counter that indicates how many world should be rolled back when backtracking
     */
    protected int jumpTo;

    /**
     * Index of the initial world, before initialization.
     * May be different from 0 if some external backups have been made.
     */
    protected int rootWorldIndex = 0;

    /**
     * Index of the world where the search starts, after initialization.
     */
    protected int searchWorldIndex = 0;

    /**
     * List of stopping criteria.
     * When at least one is satisfied, the search loop ends.
     */
    protected List<Criterion> criteria;

    /**
     * Indicates if a stop criterion is satisfied (set to <tt>true</tt> in that case).
     */
    protected boolean crit_met;

    /**
     * Indicates if the search loops unexpectedly ends (set to <tt>true</tt> in that case).
     */
    protected boolean kill;

    /**
     * Indicates if the entire search space has been explored (set to <tt>true</tt> in that case).
     */
    protected boolean entire;

    /**
     * List of search monitors attached to this search loop
     */
    protected SearchMonitorList searchMonitors;

    /**
     * Indicates if the default search loop is in use (set to <tt>true</tt> in that case).
     */
    protected boolean defaultSearch = false;

    /**
     * Indicates if a complementary search strategy has been added (set to <tt>true</tt> in that case).
     */
    protected boolean completeSearch = false;

    /**
     * Create a search loop based on three components.
     *
     * @param aModel the target solver
     * @param p       the {@code Propagate} component
     * @param l       the {@code Learn} component
     * @param m       the {@code Move} component
     */
    public SearchLoop(Model aModel, Propagate p, Learn l, Move m) {
        mModel = aModel;
        P = p;
        L = l;
        M = m;
        this.searchMonitors = new SearchMonitorList();
        defaultSettings();
    }

    /**
     * Set variables to default their values
     */
    private void defaultSettings() {
        objectivemanager = SAT();
        decision = RootDecision.ROOT;
        action = initialize;
        mMeasures = mModel.getMeasures();
        criteria = new ArrayList<>();
        crit_met = false;
        kill = true;
        entire = false;
        unplugAllSearchMonitors();
    }

    /**
     * This method enables to reset the search loop, for instance, to solve a problem twice.
     * <ul>
     * <li>It backtracks up to the root node of the search tree,</li>
     * <li>it sets the objective manager to STATISFACTION,</li>
     * <li>it resets the measures,</li>
     * <li>and sets the propagation engine to NoPropagationEngine,</li>
     * <li>remove all search monitors,</li>
     * <li>remove all stop criteria.</li>
     * </ul>
     */
    public void reset() {
        // if a resolution has already been done
        if (rootWorldIndex > -1) {
            mModel.getEnvironment().worldPopUntil(rootWorldIndex);
            Decision tmp;
            while (decision != ROOT) {
                tmp = decision;
                decision = tmp.getPrevious();
                tmp.free();
            }
            action = initialize;
            rootWorldIndex = -1;
            searchWorldIndex = -1;
            mMeasures.reset();
            objectivemanager = SAT();
            mModel.set(SINGLETON);
            crit_met = false;
            kill = true;
            entire = false;
            unplugAllSearchMonitors();
            removeAllStopCriteria();
        }
    }

    /**
     * Execute the search loop
     *
     * @param stopAtFirst should stop at first solution (<code>true</code>) or not.
     */
    public void launch(boolean stopAtFirst) {
        boolean left = true;
        do {
            switch (action) {
                case initialize:
                    searchMonitors.beforeInitialize();
                    initialize();
                    searchMonitors.afterInitialize();
                    break;
                case propagate:
                    searchMonitors.beforeDownBranch(left);
                    mMeasures.incDepth();
                    try {
                        P.execute(this);
                        action = extend;
                    } catch (ContradictionException ce) {
                        mModel.getEngine().flush();
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
                    boolean repaired = M.repair(this);
                    searchMonitors.afterUpBranch();
                    if (!repaired) {
                        entire = true;
                        action = stop;
                    } else {
                        L.forget(this);
                    }
                    break;
                case validate:
                    assert (TRUE.equals(mModel.isSatisfied())) : fullReport(mModel);
                    mModel.setFeasible(TRUE);
                    mMeasures.incSolutionCount();
                    mModel.getObjectiveManager().update();
                    searchMonitors.onSolution();
                    if (stopAtFirst) {
                        action = stop;
                    } else {
                        jumpTo = 1;
                        action = repair;
                    }
                    break;
                case stop:
                    searchMonitors.beforeClose();
                    mMeasures.updateTime();
                    kill = false;
                    entire = (decision == ROOT);
                    ESat sat = FALSE;
                    if (mMeasures.getSolutionCount() > 0) {
                        sat = TRUE;
                        if (objectivemanager.isOptimization()) {
                            mMeasures.setObjectiveOptimal(!crit_met);
                        }
                    } else if (crit_met) {
                        mMeasures.setObjectiveOptimal(false);
                        sat = UNDEFINED;
                    }
                    mModel.setFeasible(sat);
                    if (stopAtFirst) { // for the next call, if needed
                        jumpTo = 1;
                        action = repair;
                    }
                    searchMonitors.afterClose();
                    return;
            }
            if (metCriterion()) {
                action = stop;
                crit_met = true;
            }
        } while (true);
    }

    /**
     * Preparation of the search:
     * - start time recording,
     * - store root world
     * - push a back up world,
     * - run the initial propagation,
     * - initialize the Move and the search strategy
     */
    private void initialize() {
        mMeasures.startStopwatch();
        rootWorldIndex = mModel.getEnvironment().getWorldIndex();
        mModel.getEnvironment().buildFakeHistoryOn(mModel.getSettings().getEnvironmentHistorySimulationCondition());
        mModel.getEnvironment().worldPush(); // store state before initial propagation; w = 0 -> 1
        try {
            P.execute(this);
            action = extend;
            mModel.getEnvironment().worldPush(); // store state after initial propagation; w = 1 -> 2
            searchWorldIndex = mModel.getEnvironment().getWorldIndex(); // w = 2
            mModel.getEnvironment().worldPush(); // store another time for restart purpose: w = 2 -> 3
        } catch (ContradictionException ce) {
            mModel.getEngine().flush();
            mMeasures.incFailCount();
            searchMonitors.onContradiction(ce);
            L.record(this);
            mModel.getEnvironment().worldPop();
            action = stop;
        }
        // call to HeuristicVal.update(Action.initial_propagation)
        if (M.getChildMoves().size() <= 1 && M.getStrategy() == null) {
            defaultSearch = true;
            ISearchBinder binder = mModel.getSettings().getSearchBinder();
            binder.configureSearch(mModel);
        }
        if (completeSearch && !defaultSearch) {
            AbstractStrategy<Variable> declared = M.getStrategy();
            DefaultSearchBinder dbinder = new DefaultSearchBinder();
            AbstractStrategy[] complete = dbinder.getDefault(mModel);
            mModel.set(ArrayUtils.append(new AbstractStrategy[]{declared}, complete));
        }
        if (!M.init()) { // the initialisation of the Move and strategy can detect inconsistency
            mModel.getEnvironment().worldPop();
            mModel.setFeasible(FALSE);
            mModel.getEngine().flush();
            mModel.getMeasures().incFailCount();
            entire = true;
            action = stop;
        }
        // Indicates which decision was previously applied before selecting the move.
        // Always sets to ROOT for the first move
        M.setTopDecision(ROOT);
        mMeasures.updateTime();
        for (Criterion c : criteria) {
            if (c instanceof ICounter) {
                ((ICounter) c).init();
            }
        }
    }

    /**
     * Replace the current learn with <code>l</code>
     *
     * @param l the new learn to apply
     */
    public void setLearn(Learn l) {
        this.L = l;
    }

    /**
     * Return the current learn.
     *
     * @return the current learn.
     */
    public Learn getLearn() {
        return L;
    }

    /**
     * Replace the current move with <code>m</code>
     *
     * @param m the new move to apply
     */
    public void setMove(Move m) {
        this.M = m;
    }

    /**
     * Return the current move.
     *
     * @return the current move.
     */
    public Move getMove() {
        return M;
    }

    /**
     * Return the target solver
     *
     * @return the solver
     */
    public Model getSolver() {
        return mModel;
    }


    /**
     * Sets the following action in the search to be a restart instruction.
     * Note that the restart may not be immediate
     */
    protected void restart() {
        searchMonitors.beforeRestart();
        restoreRootNode();
        mModel.getEnvironment().worldPush();
        mModel.getMeasures().incRestartCount();
        try {
            objectivemanager.postDynamicCut();
            P.execute(this);
            action = extend;
        } catch (ContradictionException e) {
            // trivial inconsistency is detected, due to the cut
            action = stop;
        }

        searchMonitors.afterRestart();
    }

    /**
     * Retrieves the state of the root node (after the initial propagation)
     * Has an immediate effect
     */
    public void restoreRootNode() {
        mModel.getEnvironment().worldPopUntil(searchWorldIndex); // restore state after initial propagation
        Decision tmp;
        while (decision != ROOT) {
            tmp = decision;
            decision = tmp.getPrevious();
            tmp.free();
        }
    }

    /**
     * Add all search monitors in the list to the current this search loop.
     *
     * There is no check if there are any duplicates.
     *
     * On a call to {@link #reset()} or {@link #unplugAllSearchMonitors()}, the list will be emptied.
     * @param searchMonitorList list of search monitors to add
     */
    public void transferSearchMonitors(List<ISearchMonitor> searchMonitorList) {
        for (ISearchMonitor sm : searchMonitorList) {
            searchMonitors.add(sm);
        }
    }

    /**
     * Empties the list of search monitors.
     */
    public void unplugAllSearchMonitors() {
        searchMonitors.reset();
    }

    /**
     * Add stop criteria, which, when met, stop the search loop.
     * There can be multiple stop criteria, a logical OR is then applied.
     * <br/>
     * Examples:
     * <br/>
     * With a built-in counter, stop after 20 seconds:
     * <pre>
     *         SMF.limitTime(solver, "20s");
     * </pre>
     * With lambda, stop when 10 nodes are visited:
     * <pre>
     *     () -> solver.getMeasures().getNodeCount() >= 10
     * </pre>
     *
     * @param stopCriteria one or more stop condition
     */
    public void transferStopCriteria(List<Criterion> stopCriteria) {
        this.criteria.addAll(stopCriteria);
    }


    /**
     * Empties the list of stop criteria declared.
     * This is not automatically called on {@link #reset()}.
     */
    public void removeAllStopCriteria() {
        this.criteria.clear();
    }

    /**
     * Check wether one (at least) stop criteria is met
     * @return true if one at least stop criteria is met
     */
    private boolean metCriterion() {
        boolean ismet = false;
        for (int i = 0; i < criteria.size() && !ismet; i++) {
            ismet = criteria.get(i).isMet();
        }
        return ismet;
    }


    /**
     * Declares the search strategy to use.
     * @param strategy a search strategy to use instead of the declared one (if any)
     * @param <V> kind of variables the search strategy deals with
     */
    public <V extends Variable>  void set(AbstractStrategy<V> strategy) {
        if (M.getChildMoves().size() > 1) {
            throw new UnsupportedOperationException("The Move declared is composed of many Moves.\n" +
                    "A strategy must be attached to each of them independently, and it cannot be achieved calling this method." +
                    "An iteration over it child moves is needed: this.getMove().getChildMoves().");
        } else {
            M.setStrategy(strategy);
        }
    }

    /**
     * Returns the search strategy in use
     * @param <V> kind of variables the search strategy deals with
     * @return the current search strategy
     */
    public <V extends Variable>  AbstractStrategy<V> getStrategy() {
        if(M.getChildMoves().size()>1 && mModel.getSettings().warnUser()){
            Chatterbox.err.print("This search loop is based on a sequential Move, the strategy returned may not reflect the reality.");
        }
        return M.getStrategy();
    }

    /**
     * Declares an objective manager to use.
     * @param om the objective manager to use instead of the declared one (if any).
     */
    public void setObjectiveManager(ObjectiveManager om) {
        this.objectivemanager = om;
        if (objectivemanager.isOptimization()) {
            mMeasures.declareObjective();
        }
    }

    /**
     * @return the currently used objective manager
     */
    public ObjectiveManager getObjectiveManager() {
        return objectivemanager;
    }

    /**
     * Complete (or not) the declared search strategy with one over all variables
     *
     * @param isComplete set to true to complete the current search strategy
     */
    public void makeCompleteStrategy(boolean isComplete) {
        this.completeSearch = isComplete;
    }

    /**
     * @return the downmost taken decision
     */
    public Decision getLastDecision() {
        return decision;
    }

    /**
     * Replaces the downmost taken decision by <code>cobdec</code>.
     * @param cobdec the new downmost decision
     */
    @SuppressWarnings("unused")
    public void setLastDecision(Decision cobdec) {
        this.decision = cobdec;
    }

    /**
     * Indicate if the default search strategy is used
     *
     * @return false if a search strategy is used
     */
    public boolean isDefaultSearchUsed() {
        return defaultSearch;
    }

    /**
     * Indicate if the search strategy is completed with one over all variables
     *
     * @return false if no strategy over all variables complete the declared one
     */
    public boolean isSearchCompleted() {
        return completeSearch;
    }


    /**
     * @return <tt>true</tt> if the search loops explores the entire search space, <tt>false</tt> otherwise.
     */
    public boolean isComplete() {
        return entire;
    }


    /**
     * @return <tt>true</tt> if the search loops ends unexpectedly (externally killed, for instance).
     */
    public boolean hasEndedUnexpectedly() {
        return kill;
    }


    /**
     * @return <tt>true</tt> if the search loops encountered at least one of the stop criteria declared.
     */
    public boolean hasReachedLimit() {
        return crit_met;
    }


    /**
     * @return the index of the world where the search starts, after initialization.
     */
    public int getSearchWorldIndex() {
        return searchWorldIndex;
    }


    /**
     * @return <tt>true</tt> if the resolution already began, <tt>false</tt> otherwise
     */
    public boolean hasResolutionBegun(){
        return action != initialize;
    }
}
