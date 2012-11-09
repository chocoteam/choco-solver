/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package solver.search.loop;

import choco.kernel.ResolutionPolicy;
import choco.kernel.memory.IEnvironment;
import solver.Solver;
import solver.exception.SolverException;
import solver.objective.ObjectiveManager;
import solver.search.limits.LimitBox;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.SearchMonitorList;
import solver.search.measure.IMeasures;
import solver.search.solution.ISolutionPool;
import solver.search.solution.SolutionPoolFactory;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;

import java.util.Properties;

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
public abstract class AbstractSearchLoop implements ISearchLoop {

    //    public static int timeStamp; // keep an int, that's faster than a long, and the domain of definition is large enough
    public int timeStamp;

    static final int INIT = 0;
    static final int INITIAL_PROPAGATION = 1;
    static final int OPEN_NODE = 1 << 1;
    static final int DOWN_LEFT_BRANCH = 1 << 2;
    static final int DOWN_RIGHT_BRANCH = 1 << 3;
    static final int UP_BRANCH = 1 << 4;
    static final int RESTART = 1 << 5;
    static final int RESUME = 1 << 6;

    /* Reference to the solver */
    final Solver solver;

    /* Reference to the environment of the solver */
    IEnvironment env;

    /* Define the state to move to once a solution is found : UP_BRANCH or RESTART */
    public int stateAfterSolution = UP_BRANCH;

    /* Define the state to move to once a fail occured : UP_BRANCH or RESTART */
    public int stateAfterFail = UP_BRANCH;

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


    /* Previous solution count, to inform on state*/
    long previousSolutionCount = 0;

    /* factory for limits management */
    LimitBox limitsfactory;


    protected int solutionPoolCapacity;
    /**
     * Solution pool -- way to record solutions. Default object is no solution recorded.
     */
    ISolutionPool solutionpool;

    public SearchMonitorList smList;

    /**
     * Objective manager. Default object is no objective.
     */
    ObjectiveManager objectivemanager;

    private boolean alive;
    public Decision decision = RootDecision.ROOT;

    @SuppressWarnings({"unchecked"})
    public AbstractSearchLoop(Solver solver) {
		objectivemanager = new ObjectiveManager(null, ResolutionPolicy.SATISFACTION,solver);//default
        this.solver = solver;
        this.env = solver.getEnvironment();
        this.measures = solver.getMeasures();
        smList = new SearchMonitorList();
        smList.add(this.measures);
        this.nextState = INIT;
        this.limitsfactory = new LimitBox(this);
        loadProperties(solver.properties);
        rootWorldIndex = -1;
    }

    public void reset() {
        this.nextState = INIT;
        restaureRootNode();
        rootWorldIndex = -1;
        searchWorldIndex = -1;
        previousSolutionCount = 0;
        this.measures.reset();
    }

    protected void loadProperties(Properties properties) {
        solutionPoolCapacity = Integer.parseInt((String) properties.get("solver.solution.capacity"));
    }

    @SuppressWarnings({"unchecked"})
    public void set(AbstractStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Solves the problem states by the solver.
     *
     * @return a Boolean indicating wether the problem is satisfiable, not satisfiable or unknown
     */
    public Boolean launch() {
        if (nextState != INIT) {
            throw new SolverException("!! The search has not been initialized.\n" +
                    "!! Be sure you are respecting one of these call configurations :\n " +
                    "\tfindSolution ( nextSolution )* | findAllSolutions | findOptimalSolution\n");
        }
        return loop();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Main loop. Flatten representation of recursive tree search.
     *
     * @return a Boolean indicating wether the problem is satisfiable, not satisfiable or unknown
     */
    Boolean loop() {
        alive = true;
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
                    timeStamp++;
                    smList.beforeDownLeftBranch();
                    downLeftBranch();
                    smList.afterDownLeftBranch();
                    break;
                // GOING DOWN IN THE TREE SEARCH TO APPLY THE NEXT COMPUTED DECISION
                case DOWN_RIGHT_BRANCH:
                    timeStamp++;
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
        Boolean close = close();
        smList.afterClose();
        return close;
    }

    /**
     * Initializes the measures, just before the beginning of the search
     */
    public void initialize() {
        this.rootWorldIndex = env.getWorldIndex();
        previousSolutionCount = 0;
        solver.getEngine().init(solver);
        this.nextState = INITIAL_PROPAGATION;
    }

    /**
     * Runs the initial propagation, awaking each constraints and call filter on the initial state of variables.
     */
    protected abstract void initialPropagation();

    /**
     * Opens a new node in the tree search : compute the next decision or store a solution.
     */
    protected abstract void openNode();

    /**
     * Goes down in the tree search : apply the current decision.
     */
    protected abstract void downLeftBranch();

    protected abstract void downRightBranch();

    /**
     * Goes up in the tree search : reconsider the current decision.
     */
    protected abstract void upBranch();

    /**
     * Force restarts of the search from a previous node in the tree search.
     */
    protected abstract void restartSearch();

    public abstract void moveTo(int to);

    /**
     * Close the search
     *
     * @return <code>true</code> if at least one solution has been found, <br/>
     *         <code>null</code> if a limit has been reached before finding one solution, <br/>
     *         <code>false</code> otherwise
     */
    public Boolean close() {
        if (solutionpool.size() > 0 && objectivemanager.isOptimization()) {
            restaureRootNode();
            solutionpool.getBest().restore();
        }
//            return existsSolution() && !stopAtFirstSolution && !isEncounteredLimit();
        measures.setObjectiveOptimal(measures.getSolutionCount() > previousSolutionCount
                && stopAtFirstSolution && limitsfactory.isReached());
        if (measures.getSolutionCount() > previousSolutionCount) {
            return true;
        } else if (limitsfactory.isReached()) {
            measures.setObjectiveOptimal(false);
            return null;
        }
        return false;
    }

    public void restaureRootNode() {
        env.worldPopUntil(searchWorldIndex); // restore state after initial propagation
        timeStamp++; // to force clear delta, on solution recording
        Decision tmp;
        while (decision != RootDecision.ROOT) {
            tmp = decision;
            decision = tmp.getPrevious();
            tmp.free();
        }
    }

    /**
     * Resume the search
     */
    public abstract Boolean resume();

    /**
     * Force the search to stop
     */
    public final void interrupt() {
        nextState = RESUME;
        alive = false;
        smList.afterInterrupt();
    }

    public final void forceAlive(boolean bvalue) {
        alive = bvalue;
    }


    /**
     * Sets the following action in the search to be a restart instruction.
     */
    public final void restart() {
        nextState = RESTART;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setup() {
        this.solutionpool = SolutionPoolFactory.makeSolutionPool(solutionPoolCapacity);
    }


    public void stopAtFirstSolution(boolean value) {
        this.stopAtFirstSolution = value;
    }

    public boolean stopAtFirstSolution() {
        return this.stopAtFirstSolution;
    }

    /**
     * Gets the limit factory in order to define limits.
     *
     * @return the limit factory
     */
    public LimitBox getLimitsBox() {
        return limitsfactory;
    }


    @Override
    public void plugSearchMonitor(ISearchMonitor sm) {
        smList.add(sm);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// SETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setObjectivemanager(ObjectiveManager objectivemanager) {
        this.objectivemanager = objectivemanager;
        this.measures.declareObjective();
    }

    public ISolutionPool getSolutionpool() {
        return solutionpool;
    }

    public void setSolutionpool(ISolutionPool solutionpool) {
        this.solutionpool = solutionpool;
    }

    public void restartAfterEachSolution(boolean does) {
        stateAfterSolution = does ? RESTART : UP_BRANCH;
    }

    public void restartAfterEachFail(boolean does) {
        stateAfterFail = does ? RESTART : UP_BRANCH;
    }

    public void setSolutionPoolCapacity(int solutionPoolCapacity) {
        this.solutionPoolCapacity = solutionPoolCapacity;
    }


    public void overridePreviousWorld(int gap) {
        this.jumpTo = gap;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// GETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public IMeasures getMeasures() {
        return measures;
    }

    public ObjectiveManager getObjectivemanager() {
        return objectivemanager;
    }

    public Solver getSolver() {
        return solver;
    }

    public AbstractStrategy<Variable> getStrategy() {
        return strategy;
    }

    public abstract String decisionToString();

    public int getSolutionPoolCapacity() {
        return solutionPoolCapacity;
    }

    public int getCurrentDepth() {
        int d = 0;
        Decision tmp = decision;
        while (tmp != RootDecision.ROOT) {
            tmp = tmp.getPrevious();
            d++;
        }
        return d;
    }
}
