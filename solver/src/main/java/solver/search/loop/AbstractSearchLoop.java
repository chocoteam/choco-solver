/**
 *  Copyright (c) 2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.loop;

import choco.kernel.memory.IEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.Configuration;
import solver.Constant;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.SolverException;
import solver.objective.IObjectiveManager;
import solver.objective.NoObjectiveManager;
import solver.propagation.engines.IPropagationEngine;
import solver.search.limits.LimitFactory;
import solver.search.limits.TimeCacheThread;
import solver.search.measure.IMeasures;
import solver.search.solution.ISolutionPool;
import solver.search.solution.SolutionPoolFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;

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

    final static Logger LOGGER = LoggerFactory.getLogger(AbstractSearchLoop.class);
    public static int timeStamp; // keep an int, that's faster than a long, and the domain of definition is large enough

    static final int INIT = 0;
    static final int INITIAL_PROPAGATION = 1;
    static final int OPEN_NODE = 1 << 1;
    static final int DOWN_LEFT_BRANCH = 1 << 2;
    static final int DOWN_RIGHT_BRANCH = 1 << 3;
    static final int UP_BRANCH = 1 << 4;
    static final int RESTART = 1 << 5;
    static final int RESUME = 1 << 6;

    /* The configuraiton file */
    Configuration configuration;

    /* Reference to the solver */
    final Solver solver;

    /* Reference to the environment of the solver */
    IEnvironment env;

    /* Reference to the propagation pilot */
    public IPropagationEngine pilotPropag;

    /* Node selection, or how to select a couple variable-value to continue branching */
    AbstractStrategy<Variable> strategy;

    boolean stopAtFirstSolution;

    /* index of the initial world of search (can be different from 0) */
    int baseWorld;

    /* store the next state of the search loop.
     * initial value is <code>INITIAL_PROPAGATION</code> */
    int nextState;

    /**
     * Stores the search measures
     */
    final protected IMeasures measures;

    /* Current time at the very beginning of the search  */
    long startingTime;

    /* Current memory uses at the very beginning of the search  */
    long startingMemory;

    /* Previous solution count, to inform on state*/
    long previousSolutionCount = 0;

    /* factory for limits management */
    LimitFactory limitsfactory;

    /**
     * Solution pool -- way to record solutions. Default object is no solution recorded.
     */
    ISolutionPool solutionpool;

    /**
     * Search monitor : print statistics every X ms -- can be null.
     */
    SearchMonitor searchmonitor;

    /**
     * Objective manager. Default object is no objective.
     */
    IObjectiveManager objectivemanager = NoObjectiveManager.get();

    private boolean alive;

    protected SearchLayout searchLayout = SearchLayout.nolayout;


    @SuppressWarnings({"unchecked"})
    public AbstractSearchLoop(Solver solver, IPropagationEngine pilotPropag, AbstractStrategy strategy
    ) {
        this.solver = solver;
        this.env = solver.getEnvironment();
        this.measures = solver.getMeasures();
        this.pilotPropag = pilotPropag;
        this.nextState = INIT;
        this.strategy = strategy;
        this.limitsfactory = new LimitFactory(this);
    }

    /**
     * Solves the problem states by the solver.
     *
     * @return a Boolean indicating wether the problem is satisfiable, not satisfiable or unknown
     */
    public Boolean launch() {
        if (nextState != INIT) {
            throw new SolverException("The search has not been initialized.\n" +
                    "Be sure you are respecting one of these call configurations :\n " +
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
                    initialize();
                    break;
                // INITIAL PROPAGATION -- ROOT NODE FEASABILITY
                case INITIAL_PROPAGATION:
                    initialPropagation();
                    break;
                // OPENING A NEW NODE IN THE TREE SEARCH
                case OPEN_NODE:
                    measures.incNodeCount(1);
                    openNode();
                    limitsfactory.hasEncounteredLimit();
                    break;
                // GOING DOWN IN THE TREE SEARCH TO APPLY THE NEXT COMPUTED DECISION
                case DOWN_LEFT_BRANCH:
                    timeStamp++;
                    downLeftBranch();
                    break;
                // GOING DOWN IN THE TREE SEARCH TO APPLY THE NEXT COMPUTED DECISION
                case DOWN_RIGHT_BRANCH:
                    timeStamp++;
                    downRightBranch();
                    break;
                // GOING UP IN THE TREE SEARCH TO RECONSIDER THE CURRENT DECISION
                case UP_BRANCH:
                    measures.incBacktrackCount(1);
                    upBranch();
                    limitsfactory.hasEncounteredLimit();
                    break;
                // RESTARTING THE SEARCH FROM A PREVIOUS NODE -- COMMONLY, THE ROOT NODE
                case RESTART:
                    measures.incRestartCount(1);
                    restartSearch();
                    break;
            }
        }
        return close();
    }

    /**
     * Initializes the measures, just before the beginning of the search
     */
    public void initialize() {
        LOGGER.info(Constant.WELCOME_TITLE);
        LOGGER.info(Constant.WELCOME_VERSION);
        LOGGER.info(Constant.CALLER, solver.getName());

        this.baseWorld = env.getWorldIndex();
        measures.reset();
        previousSolutionCount = 0;
        startingMemory = memoryUsedInMB();
        startingTime = System.currentTimeMillis();
        TimeCacheThread.currentTimeMillis = startingTime;

        if (searchmonitor != null) {
            searchmonitor.start();
        }
        pilotPropag.init();
        limitsfactory.init();
        this.nextState = INITIAL_PROPAGATION;
        updateTimeCount();

        measures.setInitialisation(TimeCacheThread.currentTimeMillis - startingTime);
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

    /**
     * Close the search
     */
    public Boolean close() {
        updateTimeCount();
        updateMemoryUsed();
        updatePropagationCount();
        searchLayout.onClose();
        if (solutionpool.size() > 0 && (!stopAtFirstSolution)) {
            env.worldPopUntil(baseWorld);
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

    /**
     * Resume the search
     */
    public abstract Boolean resume();

    /**
     * Force the search to stop
     */
    public final void interrupt() {
        alive = false;
    }

    /**
     * Sets the following action in the search to be a restart instruction.
     */
    public final void restart() {
        nextState = RESTART;
    }


    public void _forceInit() {
        nextState = INIT;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Reads the configuration file and sets this <code>AbstractSearchLoop</code> up.
     *
     * @param configuration main configuration file
     */
    public void setup(Configuration configuration) {
        this.configuration = configuration;
        this.solutionpool =
                SolutionPoolFactory.makeSolutionPool(configuration.readInt(Configuration.SOLUTION_POOL_CAPACITY));
    }


    public void stopAtFirstSolution(boolean value) {
        this.stopAtFirstSolution = value;
    }

    /**
     * Gets the limit factory in order to define limits.
     *
     * @return the limit factory
     */
    public LimitFactory getLimitsFactory() {
        return limitsfactory;
    }

    static long memoryUsedInMB() {
        return Runtime.getRuntime().freeMemory() / 1024 / 1024;
    }

    /**
     * Updates the used memory
     */
    void updateMemoryUsed() {
        measures.setMemoryUsed(memoryUsedInMB() - startingMemory);
    }

    void updatePropagationCount() {
        Constraint[] cstrs = solver.getCstrs();
        long c = 0;
        for(int i = 0 ; i < cstrs.length; i++){
            Propagator[] propagators = cstrs[i].propagators;
            for(int j = 0; j < propagators.length; j++){
                c +=propagators[j].filterCall;
            }
        }
        measures.setPropagationsCount(c);

    }

    /**
     * Updates the time recorder
     */
    void updateTimeCount() {
        measures.setTimeCount(TimeCacheThread.currentTimeMillis - startingTime);
    }

    /**
     * Print statistics of the searhc every "everyXms" ms
     *
     * @param everyXms delay between two prints
     */
    public void monitorSearch(int everyXms) {
        searchmonitor = new SearchMonitor(this, everyXms);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// SETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setObjectivemanager(IObjectiveManager objectivemanager) {
        this.objectivemanager = objectivemanager;
        this.measures.declareObjective();
    }

    public void setSolutionpool(ISolutionPool solutionpool) {
        this.solutionpool = solutionpool;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// GETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public IMeasures getMeasures() {
        return measures;
    }

    public IObjectiveManager getObjectivemanager() {
        return objectivemanager;
    }

    public void setSearchLayout(SearchLayout searchLayout) {
        this.searchLayout = searchLayout;
        searchLayout.searchLoop = this;
    }

    public Solver getSolver() {
        return solver;
    }
}
