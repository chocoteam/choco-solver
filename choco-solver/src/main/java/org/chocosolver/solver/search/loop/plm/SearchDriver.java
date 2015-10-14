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
package org.chocosolver.solver.search.loop.plm;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.bind.DefaultSearchBinder;
import org.chocosolver.solver.search.bind.ISearchBinder;
import org.chocosolver.solver.search.loop.ISearchLoop;
import org.chocosolver.solver.search.loop.monitors.*;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.solver.objective.ObjectiveManager.SAT;
import static org.chocosolver.solver.search.loop.Reporting.fullReport;
import static org.chocosolver.solver.search.loop.plm.SearchDriver.Action.*;
import static org.chocosolver.solver.search.strategy.decision.RootDecision.ROOT;
import static org.chocosolver.util.ESat.FALSE;
import static org.chocosolver.util.ESat.TRUE;

/**
 * This class is inspired from :
 * <cite>
 * Inspired from "Unifying search algorithms for CSP" N. Jussien and O. Lhomme, Technical report 02-3-INFO, EMN
 * </cite>
 * <p>
 * It declares a composable search loop made of three components:
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
 */
public class SearchDriver implements ISearchLoop {

    public static boolean PRINTDEC = false;

    /**
     * Define the possible actions of SearchDriver
     */
    protected enum Action {
        initialize,
        propagate,
        extend,
        validate,
        repair,
        stop,
    }

    /**
     * The three main components of SearchDriver
     */
    protected Propagate P;
    protected Learn L;
    protected Move M;

    /**
     * The internal variables
     */
    protected Solver mSolver; // the solver
    protected ObjectiveManager objectivemanager; // the objective manager
    protected Action action; // the action to execute in the search loop
    protected IMeasures mMeasures; // the resolutions measures
    protected Decision decision; // the current decisions
    protected int jumpTo; // counter that indicates the number of world to backtrack to
    protected int rootWorldIndex = 0, searchWorldIndex = 0; // initial world and search world
    protected Criterion criterion = () -> false; // early end criterion
    protected boolean limit, kill, entire;
    protected List<IMonitorSolution> actionOnSolution; // action to do on a solution
    protected List<IMonitorContradiction> actionOnContradiction; // action to do on a failure
    protected List<IMonitorRestart> actionOnRestart; // action to do on a restart
    protected boolean defaultSearch = false; // is default search selected
    protected boolean completeSearch = false; // does complete search is required

    /**
     * Define a SearchDriver framework that run the search loop
     *
     * @param aSolver the target solver
     * @param p       the {@code Propagate} component
     * @param l       the {@code Learn} component
     * @param m       the {@code Move} component
     */
    public SearchDriver(Solver aSolver, Propagate p, Learn l, Move m) {
        mSolver = aSolver;
        P = p;
        L = l;
        M = m;
        this.actionOnSolution = new ArrayList<>();
        this.actionOnContradiction = new ArrayList<>();
        this.actionOnRestart = new ArrayList<>();
        defaultSettings();
    }

    /**
     * Set variables to default their values
     */
    private void defaultSettings() {
        objectivemanager = SAT();
        decision = RootDecision.ROOT;
        action = initialize;
        mMeasures = mSolver.getMeasures();
        limit = false;
        kill = true;
        entire = false;
        actionOnSolution.clear();
        actionOnContradiction.clear();
        actionOnRestart.clear();
    }

    /**
     * Execute the search loop
     *
     * @param stopAtFirst should stop at first solution (<code>true</code>) or not.
     */
    public void launch(boolean stopAtFirst) {
        do {
            switch (action) {
                case initialize:
                    initialize();
                    break;
                case propagate:
                    try {
                        if (PRINTDEC) {
                            System.out.printf("%s %s\n",
                                    StringUtils.pad("", mSolver.getEnvironment().getWorldIndex(), "."), decision);
                        }
                        P.execute(this);
                        action = extend;
                    } catch (ContradictionException ce) {
                        mSolver.getEngine().flush();
                        mMeasures.incFailCount();
                        jumpTo = 1;
                        action = repair;
                    }
                    break;
                case extend:
                    if (!M.extend(this)) {
                        action = validate;
                    } else {
                        mMeasures.incNodeCount();
                        action = propagate;
                    }
                    break;
                case repair:
                    L.record(this);
                    if (!M.repair(this)) {
                        entire = true;
                        action = stop;
                    } else {
                        L.forget(this);
                        action = propagate;
                    }
                    break;
                case validate:
                    mSolver.setFeasible(TRUE);
                    assert (TRUE.equals(mSolver.isSatisfied())) : fullReport(mSolver);
                    mMeasures.incSolutionCount();
                    mMeasures.incNodeCount();
                    mSolver.getObjectiveManager().update();
                    actionOnSolution.forEach(IMonitorSolution::onSolution);
                    if (stopAtFirst) {
                        action = stop;
                    } else {
                        jumpTo = 1;
                        action = repair;
                    }
                    break;
                case stop:
                    mMeasures.updateTime();
                    kill = false;
                    return;
            }
            if (criterion.isMet()) {
                action = stop;
                limit = true;
            }
        } while (true);
    }

    /**
     * Preparation of the search:
     * - start time recording,
     * - store root world,
     * - push a back up world,
     * - run the initial propagation,
     * - initialize the Move and the search strategy
     */
    private void initialize() {
        mMeasures.startStopwatch();
        rootWorldIndex = mSolver.getEnvironment().getWorldIndex();
        mSolver.getEnvironment().worldPush(); // store state before initial propagation; w = 0 -> 1
        try {
            P.execute(this);
            action = extend;
            mSolver.getEnvironment().worldPush(); // store state after initial propagation; w = 1 -> 2
            searchWorldIndex = mSolver.getEnvironment().getWorldIndex(); // w = 2
            mSolver.getEnvironment().worldPush(); // store another time for restart purpose: w = 2 -> 3
        } catch (ContradictionException ce) {
            mSolver.getEngine().flush();
            action = stop;
            mMeasures.incFailCount();
            mSolver.getEnvironment().worldPop();
            actionOnContradiction.forEach(a -> a.onContradiction(ce));
        }
        // call to HeuristicVal.update(Action.initial_propagation)
        if (M.getStrategy() == null) {
            defaultSearch = true;
            ISearchBinder binder = mSolver.getSettings().getSearchBinder();
            binder.configureSearch(mSolver);
        }
        if (completeSearch && !defaultSearch) {
            AbstractStrategy<Variable> declared = M.getStrategy();
            DefaultSearchBinder dbinder = new DefaultSearchBinder();
            AbstractStrategy[] complete = dbinder.getDefault(mSolver);
            mSolver.set(ArrayUtils.append(new AbstractStrategy[]{declared}, complete));
        }
        if (!M.init()) { // the initialisation of the Move and strategy can detect inconsistency
            mSolver.getEnvironment().worldPop();
            mSolver.setFeasible(FALSE);
            mSolver.getEngine().flush();
            mSolver.getMeasures().incFailCount();
            interrupt(MSG_SEARCH_INIT, true);
            entire = false;
        }
        mMeasures.updateTime();
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
    public Solver getSolver() {
        return mSolver;
    }

    /**
     * Define a stop criterion, which, when met, stops the search loop.
     * Examples:
     * <ul>
     * <li>No criterion (by default):
     * <pre>
     *     () -> false
     * </pre>
     * </li>
     * <li>
     * Stop after 20 seconds:
     * <pre>
     *         () -> solver.getMeasures().getTimeCount() >= 20000
     *     </pre>
     * </li>
     * <li>Stop when 10 nodes are visited:
     * <pre>
     *     () -> solver.getMeasures().getNodeCount() >= 10
     * </pre>
     * </li>
     * <p>
     * </ul>
     * <p>
     * <pre>
     *
     * </pre>
     *
     * @param stopCriterion a stop condition
     */
    public void setStopCriterion(Criterion stopCriterion) {
        this.criterion = stopCriterion;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restart() {
        actionOnRestart.forEach(IMonitorRestart::beforeRestart);
        restoreRootNode();
        mSolver.getEnvironment().worldPush();
        mSolver.getMeasures().incRestartCount();
        actionOnRestart.forEach(IMonitorRestart::afterRestart);
    }

    @Override
    public void restoreRootNode() {
        mSolver.getEnvironment().worldPopUntil(searchWorldIndex); // restore state after initial propagation
        Decision tmp;
        while (decision != ROOT) {
            tmp = decision;
            decision = tmp.getPrevious();
            tmp.free();
        }
    }

    @Override
    public void plugSearchMonitor(ISearchMonitor sm) {
        if (sm instanceof IMonitorSolution) {
            actionOnSolution.add((IMonitorSolution) sm);
        }
        if (sm instanceof IMonitorContradiction) {
            actionOnContradiction.add((IMonitorContradiction) sm);
        }
        if (sm instanceof IMonitorRestart) {
            actionOnRestart.add((IMonitorRestart) sm);
        }
    }

    public void unplugSearchMonitor(ISearchMonitor sm) {
        if (sm instanceof IMonitorSolution) {
            actionOnSolution.remove(sm);
        }
        if (sm instanceof IMonitorContradiction) {
            actionOnContradiction.remove(sm);
        }
        if (sm instanceof IMonitorRestart) {
            actionOnRestart.remove(sm);
        }
    }

    @Override
    public void set(AbstractStrategy strategy) {
        M.setStrategy(strategy);
    }

    @Override
    public AbstractStrategy getStrategy() {
        return M.getStrategy();
    }

    @Override
    public void setObjectiveManager(ObjectiveManager om) {
        this.objectivemanager = om;
        if (objectivemanager.isOptimization()) {
            mMeasures.declareObjective();
        }
    }

    @Override
    public void makeCompleteStrategy(boolean isComplete) {
        this.completeSearch = isComplete;
    }

    @Override
    public ObjectiveManager getObjectiveManager() {
        return objectivemanager;
    }

    @Override
    public Decision getLastDecision() {
        return decision;
    }

    @Override
    public boolean isDefaultSearchUsed() {
        return defaultSearch;
    }

    @Override
    public boolean isSearchCompleted() {
        return completeSearch;
    }

    @Override
    public boolean isComplete() {
        return entire;
    }

    @Override
    public boolean hasEndedUnexpectedly() {
        return kill;
    }

    @Override
    public boolean hasReachedLimit() {
        return limit;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////// USELESS ///////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Deprecated
    public int getTimeStamp() {
        return mSolver.getEnvironment().getTimeStamp();
    }

    @Override
    public void interrupt(String msgNgood, boolean voidable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reachLimit(boolean voidable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBeResumed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCurrentDepth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SearchMonitorList getSMList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forceAlive(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLastDecision(Decision cobdec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void overridePreviousWorld(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveTo(int nextState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSearchWorldIndex() {
        throw new UnsupportedOperationException();
    }

}
