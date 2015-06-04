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
package org.chocosolver.solver.search.loop;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.bind.DefaultSearchBinder;
import org.chocosolver.solver.search.bind.ISearchBinder;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorList;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.chocosolver.solver.objective.ObjectiveManager.SAT;
import static org.chocosolver.solver.propagation.NoPropagationEngine.SINGLETON;
import static org.chocosolver.solver.search.loop.Reporting.fullReport;
import static org.chocosolver.solver.search.strategy.decision.RootDecision.ROOT;
import static org.chocosolver.util.ESat.*;

/**
 * An <code>AbstractSearchLoop</code> object is part of the <code>Solver</code> object
 * and the way to guide the search, it builds the <i>tree search</i>.
 * <br/>When the initial propagation does not lead to a solution but a fix point, some decisions have to
 * be applied to find solutions. It also deals with <code>IEnvironment</code> worlds backups and rollbacks,
 * ie the backtracking system.
 * <br/>
 * By default, a <code>AbstractSearchLoop</code> object is a flatten representation of a recurcive concept.
 * Once a fix point is reached, a decision is taken to restart the propagation loop and find solutions or detect fails.
 * This is decomposed in 6 steps:
 * <ul>
 * <li><code>INIT</code>: initializes the internal structures and statistics,</li>
 * <li><code>INITIAL_PROPAGATION</code>: runs the initial propagations, checks the root node feasiblity,</li>
 * <li><code>OPEN_NODE</code>: checks if the current state is a solution -- every variables are instantiated--
 * (next step is <code>STOP</code>)
 * or if fix point is reached, requiring a new decision, (next step is DOWN_LEFT_BRANCH);</li>
 * <li><code>DOWN_LEFT_BRANCH</code>: backs up the current state, applies a decision (commonly domain reduction to a singleton),
 * then <code>propagate</code>s this new information on the <code>Constraint</code> network.
 * If a contradiction occurs, it reconsiders the current decision (next step is <code>UP_BRANCH</code>),
 * otherwise, a new fix point is reached (next step is <code>OPEN_NODE</code>;</li>
 * <li><code>UP_BRANCH</code>: rolls back the previous state, reconsiders the previsous decision
 * then <code>propagate</code>s this new information on the <code>Constraint</code> network.
 * If a contradiction occurs, it reconsiders the decision before (next step is <code>UP_BRANCH</code>),
 * otherwise, requires a new decision (next step is <code>DOWN_LEFT_BRANCH</code>;</li>
 * <li><code>RESTART</code>: restarts the search from a previous node, commonly the root node.</li>
 * </ul>
 * <br/>
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @since 0.01
 */
public class SearchLoop implements ISearchLoop {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    //***********************************************************************************

    protected final static Logger LOGGER = LoggerFactory.getLogger(SearchLoop.class);

    /* Reference to the solver */
    final Solver solver;

    /* Reference to the environment of the solver */
    IEnvironment env;

    /* Node selection, or how to select a couple variable-value to continue branching */
    AbstractStrategy<Variable> strategy;

    boolean stopAtFirstSolution;

    /* initila world index, before initial propagation (can be different from 0) */
    int rootWorldIndex;

    /* world index just after initial propagation (commonly rootWorldIndex + 2) */
    int searchWorldIndex;

    /* store the next state of the search loop.
     * initial value is <code>INITIAL_PROPAGATION</code> */
    int nextState;

    // Store the number of wrolds to jump to -- usefull in UpBranch
    int jumpTo;

    /**
     * Stores the search measures
     */
    final protected IMeasures measures;

    boolean hasReachedLimit = false;

    boolean hasEndedUnexpectedly = true; // to capture external shut down

    boolean canBeResumed = true;

    public SearchMonitorList smList;

    /**
     * Objective manager. Default object is no objective.
     */
    ObjectiveManager objectivemanager;

    private boolean alive;

    public Decision decision = ROOT;

    protected boolean defaultSearch = false;

    protected boolean completeSearch = false;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    @SuppressWarnings({"unchecked"})
    public SearchLoop(Solver solver) {
        this.solver = solver;
        this.env = solver.getEnvironment();
        this.measures = solver.getMeasures();
        smList = new SearchMonitorList();
        smList.add(this.measures);
        this.nextState = INIT;
        rootWorldIndex = -1;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void reset() {
        // if a resolution has already been done
        if (rootWorldIndex > -1) {
            env.worldPopUntil(rootWorldIndex);
            Decision tmp;
            while (decision != ROOT) {
                tmp = decision;
                decision = tmp.getPrevious();
                tmp.free();
            }
            nextState = INIT;
            rootWorldIndex = -1;
            searchWorldIndex = -1;
            measures.reset();
            objectivemanager = SAT();
            solver.set(SINGLETON);
            canBeResumed = true;
        }
    }

    @Override
    public void launch(boolean stopatfirst) {
        if (nextState != INIT) {
            throw new SolverException("!! The search has not been initialized.\n" +
                    "!! Be sure you are respecting one of these call configurations :\n " +
                    "\tfindSolution ( nextSolution )* | findAllSolutions | findOptimalSolution\n");
        }
        this.stopAtFirstSolution = stopatfirst;
        loop();
    }

    @Override
    public void resume() {
        if (nextState == INIT) {
            throw new SolverException("the search loop has not been initialized.\n " +
                    "This appears when 'nextSolution' is called before 'findSolution'.");
        } else if (nextState != RESUME) {
            throw new SolverException("The search cannot be resumed.");
        }
        moveTo(UP_BRANCH);
        loop();
    }

    /**
     * Required method to be sure a restart is taken into account.
     * Because, restart limit checker are threads, si they can interrupt the search loop at any moment.
     * And the interruption must not be forget and replaced by the wrong next state.
     * <br/>
     * <b>Beware, if this method is called from RESTART case, it leads to an infinite loop!</b>
     *
     * @param to STEP to reach
     */
    @Override
    public void moveTo(int to) {
        if ((nextState & RESTART) == 0) {
            nextState = to;
        }
    }

    @Override
    public void restoreRootNode() {
        env.worldPopUntil(searchWorldIndex); // restore state after initial propagation
        Decision tmp;
        while (decision != ROOT) {
            tmp = decision;
            decision = tmp.getPrevious();
            tmp.free();
        }
    }

    @Override
    public final void interrupt(String message, boolean voidable) {
        canBeResumed = voidable;
        nextState = RESUME;
        alive = false;
        smList.afterInterrupt();
    }

    @Override
    public final void forceAlive(boolean bvalue) {
        alive = bvalue;
    }

    @Override
    public final void restart() {
        nextState = RESTART;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Main loop. Flatten representation of recursive tree search.
     */
    private void loop() {
        alive = canBeResumed;
        while (alive) {
            switch (nextState) {
                // INITIALIZE THE SEARCH LOOP
                case INIT:
                    smList.beforeInitialize();
                    initialize();
                    smList.afterInitialize();
                    break;
                // INITIAL PROPAGATION -- ROOT NODE FEASABILITY
                case INITIAL_PROPAGATION:
                    smList.beforeInitialPropagation();
                    initialPropagation();
                    smList.afterInitialPropagation();
                    break;
                // OPENING A NEW NODE IN THE TREE SEARCH
                case OPEN_NODE:
                    smList.beforeOpenNode();
                    openNode();
                    smList.afterOpenNode();
                    break;
                // GOING DOWN IN THE TREE SEARCH TO APPLY THE NEXT COMPUTED DECISION
                case DOWN_LEFT_BRANCH:
                    smList.beforeDownLeftBranch();
                    downLeftBranch();
                    smList.afterDownLeftBranch();
                    break;
                // GOING DOWN IN THE TREE SEARCH TO APPLY THE NEXT COMPUTED DECISION
                case DOWN_RIGHT_BRANCH:
                    smList.beforeDownRightBranch();
                    downRightBranch();
                    smList.afterDownRightBranch();
                    break;
                // GOING UP IN THE TREE SEARCH TO RECONSIDER THE CURRENT DECISION
                case UP_BRANCH:
                    smList.beforeUpBranch();
                    upBranch();
                    smList.afterUpBranch();
                    break;
                // RESTARTING THE SEARCH FROM A PREVIOUS NODE -- COMMONLY, THE ROOT NODE
                case RESTART:
                    smList.beforeRestart();
                    restartSearch();
                    smList.afterRestart();
                    break;
            }
        }
        smList.beforeClose();
        close();
        smList.afterClose();
    }

    /**
     * Initializes the measures, just before the beginning of the search
     */
    private void initialize() {
        this.rootWorldIndex = env.getWorldIndex();
        this.nextState = INITIAL_PROPAGATION;
        this.env.buildFakeHistoryOn(solver.getSettings().getEnvironmentHistorySimulationCondition());
    }

    /**
     * Runs the initial propagation, awaking each constraints and call filter on the initial state of variables.
     */
    private void initialPropagation() {
        this.env.worldPush(); // store state before initial propagation; w = 0 -> 1
        try {
            solver.getEngine().propagate();
        } catch (ContradictionException e) {
            solver.getEngine().flush();
            solver.setFeasible(FALSE);
            interrupt(MSG_INIT, true);
            smList.onContradiction(e);
            this.env.worldPop();
            return;
        }
        this.env.worldPush(); // store state after initial propagation; w = 1 -> 2
        this.searchWorldIndex = env.getWorldIndex(); // w = 2
        this.env.worldPush(); // store another time for restart purpose: w = 2 -> 3
        // call to HeuristicVal.update(Action.initial_propagation)
        if (strategy == null) {
            defaultSearch = true;
            ISearchBinder binder = solver.getSettings().getSearchBinder();
            binder.configureSearch(solver);
        }
        if (completeSearch && !defaultSearch) {
            AbstractStrategy<Variable> declared = strategy;
            DefaultSearchBinder dbinder = new DefaultSearchBinder();
            AbstractStrategy[] complete = dbinder.getDefault(solver);
            solver.set(ArrayUtils.append(new AbstractStrategy[]{declared}, complete));
        }

        try {
            strategy.init(); // the initialisation of the strategy can detect inconsistency
        } catch (ContradictionException cex) {
            this.env.worldPop();
            solver.setFeasible(FALSE);
            solver.getEngine().flush();
            interrupt(MSG_SEARCH_INIT + ": " + cex.getMessage(), true);
        }
        moveTo(OPEN_NODE);
    }

    /**
     * Opens a new node in the tree search : compute the next decision or store a solution.
     */
    private void openNode() {
        Decision tmp = decision;
        decision = strategy.getDecision();
        if (decision != null) { // null means there is no more decision
            decision.setPrevious(tmp);
            moveTo(DOWN_LEFT_BRANCH);
        } else {
            decision = tmp;
            recordSolution();
        }
    }

    private void recordSolution() {
        //todo: checker d'etat
        solver.setFeasible(TRUE);
        assert (TRUE.equals(solver.isSatisfied())) : fullReport(solver);
        objectivemanager.update();
        if (stopAtFirstSolution) {
            interrupt(MSG_FIRST_SOL, true);
        } else {
            moveTo(UP_BRANCH);
        }
        smList.onSolution();
    }

    /**
     * Goes down in the tree search : apply the current decision.
     */
    private void downLeftBranch() {
        downBranch();
    }

    private void downRightBranch() {
        downBranch();
    }

    private void downBranch() {
        env.worldPush();
        try {
            decision.buildNext();
            objectivemanager.apply(decision);
            objectivemanager.postDynamicCut();

            solver.getEngine().propagate();
            moveTo(OPEN_NODE);
        } catch (ContradictionException e) {
            solver.getEngine().flush();
            moveTo(UP_BRANCH);
            jumpTo = 1;
            smList.onContradiction(e);
        }
    }

    /**
     * Goes up in the tree search : reconsider the current decision.
     * <p>
     * Rolls back the previous state.
     * Then, if it goes back to the base world, stop the search.
     * Otherwise, gets the opposite decision, applies it and calls the propagation.
     */
    private void upBranch() {
        env.worldPop();
        if (decision == ROOT) {// Issue#55
            // The entire tree search has been explored, the search cannot be followed
            interrupt(MSG_ROOT, true);
        } else {
            jumpTo--;
            if (jumpTo <= 0 && decision.hasNext()) {
                moveTo(DOWN_RIGHT_BRANCH);
            } else {
                Decision tmp = decision;
                decision = decision.getPrevious();
                tmp.free();
            }
        }
    }

    /**
     * Force restarts of the search from a previous node in the tree search.
     */
    private void restartSearch() {
        restoreRootNode();
        solver.getEnvironment().worldPush(); //issue#55
        try {
            objectivemanager.postDynamicCut();
            solver.getEngine().propagate();
            nextState = OPEN_NODE;
        } catch (ContradictionException e) {
            interrupt(MSG_CUT, true);
        }
    }

    /**
     * Close the search, restore the last solution if any,
     * and set the feasibility and optimality variables.
     */
    private void close() {
        hasEndedUnexpectedly = false;
        ESat sat = FALSE;
        if (measures.getSolutionCount() > 0) {
            sat = TRUE;
            if (objectivemanager.isOptimization()) {
                measures.setObjectiveOptimal(!hasReachedLimit);
            }
        } else if (hasReachedLimit) {
            measures.setObjectiveOptimal(false);
            sat = UNDEFINED;
        }
        solver.setFeasible(sat);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void plugSearchMonitor(ISearchMonitor sm) {
        if (!smList.contains(sm)) {
            smList.add(sm);
        } else {
            LOGGER.debug("The search monitor already exists and is ignored");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// SETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setObjectiveManager(ObjectiveManager objectivemanager) {
        this.objectivemanager = objectivemanager;
        if (objectivemanager.isOptimization()) {
            this.measures.declareObjective();
        }
    }

    @Override
    public void overridePreviousWorld(int gap) {
        this.jumpTo = gap;
    }

    @Override
    public void set(AbstractStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public final void reachLimit() {
        hasReachedLimit = true;
        interrupt(MSG_LIMIT, true);
    }

    @Override
    public void setLastDecision(Decision d) {
        decision = d;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// GETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ObjectiveManager getObjectiveManager() {
        return objectivemanager;
    }

    @Override
    public AbstractStrategy<Variable> getStrategy() {
        return strategy;
    }

    @Override
    public int getCurrentDepth() {
        int d = 0;
        Decision tmp = decision;
        while (tmp != ROOT) {
            tmp = tmp.getPrevious();
            d++;
        }
        return d;
    }

    @Override
    public boolean hasReachedLimit() {
        return hasReachedLimit;
    }

    @Override
    public boolean hasEndedUnexpectedly() {
        return hasEndedUnexpectedly;
    }

    @Override
    public boolean canBeResumed() {
        return canBeResumed;
    }

    @Override
    public int getTimeStamp() {
        return env.getTimeStamp();
    }

    @Override
    public Decision getLastDecision() {
        return decision;
    }

    @Override
    public SearchMonitorList getSMList() {
        return smList;
    }

    @Override
    public int getSearchWorldIndex() {
        return searchWorldIndex;
    }

    @Override
    public void makeCompleteStrategy(boolean isComplete) {
        this.completeSearch = isComplete;
    }

    @Override
    public boolean isDefaultSearchUsed() {
        return defaultSearch;
    }

    @Override
    public boolean isSearchCompleted() {
        return completeSearch;
    }
}
