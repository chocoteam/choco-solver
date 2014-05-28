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

import memory.IEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.objective.ObjectiveManager;
import solver.propagation.NoPropagationEngine;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.SearchMonitorList;
import solver.search.measure.IMeasures;
import solver.search.strategy.GraphStrategyFactory;
import solver.search.strategy.ISF;
import solver.search.strategy.SetStrategyFactory;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.RootDecision;
import solver.search.strategy.selectors.values.RealDomainMiddle;
import solver.search.strategy.selectors.variables.Cyclic;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.RealStrategy;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.variables.*;
import solver.variables.graph.GraphVar;
import util.ESat;

import java.util.Arrays;

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

	protected final static Logger LOGGER = LoggerFactory.getLogger(ISearchLoop.class);

	// keep an int, that's faster than a long, and the domain of definition is large enough
	public int timeStamp;

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

	/** Stores the search measures */
	final protected IMeasures measures;

	boolean hasReachedLimit;

	public SearchMonitorList smList;

	/** Objective manager. Default object is no objective. */
	ObjectiveManager objectivemanager;

	private boolean alive;
	
	public Decision decision = RootDecision.ROOT;

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
			timeStamp++;
			Decision tmp;
			while (decision != RootDecision.ROOT) {
				tmp = decision;
				decision = tmp.getPrevious();
				tmp.free();
			}
			nextState = INIT;
			rootWorldIndex = -1;
			searchWorldIndex = -1;
			measures.reset();
			objectivemanager = ObjectiveManager.SAT();
			solver.set(NoPropagationEngine.SINGLETON);
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
		timeStamp++; // to force clear delta, on solution recording
		Decision tmp;
		while (decision != RootDecision.ROOT) {
			tmp = decision;
			decision = tmp.getPrevious();
			tmp.free();
		}
	}

	@Override
	public final void interrupt(String message) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Search interruption: {}", message);
		}
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
		close();
		smList.afterClose();
	}

	/**
	 * Initializes the measures, just before the beginning of the search
	 */
	private void initialize() {
		this.rootWorldIndex = env.getWorldIndex();
		this.nextState = INITIAL_PROPAGATION;
	}

	/**
	 * Runs the initial propagation, awaking each constraints and call filter on the initial state of variables.
	 */
	private void initialPropagation(){
		this.env.worldPush();
		try {
			solver.getEngine().propagate();
		} catch (ContradictionException e) {
			this.env.worldPop();
			solver.setFeasible(ESat.FALSE);
			solver.getEngine().flush();
			interrupt(MSG_INIT);
			return;
		}
		this.env.worldPush(); // push another wolrd to recover the state after initial propagation
		this.searchWorldIndex = env.getWorldIndex();
		// call to HeuristicVal.update(Action.initial_propagation)
		if (strategy == null) {
			defaultSearchStrategy(solver);
		}
		try {
			strategy.init(); // the initialisation of the strategy can detect inconsistency
		} catch (ContradictionException cex) {
			this.env.worldPop();
			solver.setFeasible(ESat.FALSE);
			solver.getEngine().flush();
			interrupt(MSG_SEARCH_INIT + ": " + cex.getMessage());
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
		solver.setFeasible(ESat.TRUE);
		assert (ESat.TRUE.equals(solver.isSatisfied())) : Reporting.fullReport(solver);
		objectivemanager.update();
		if (stopAtFirstSolution) {
			interrupt(MSG_FIRST_SOL);
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
	 *
	 * Rolls back the previous state.
	 * Then, if it goes back to the base world, stop the search.
	 * Otherwise, gets the opposite decision, applies it and calls the propagation.
	 */
	private void upBranch() {
		env.worldPop();
		if (decision == RootDecision.ROOT) {// Issue#55
			// The entire tree search has been explored, the search cannot be followed
			interrupt(MSG_ROOT);
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
			interrupt(MSG_CUT);
		}
	}

	/**
	 * Close the search, restore the last solution if any,
	 * and set the feasibility and optimality variables.
	 */
	private void close() {
		ESat sat = ESat.FALSE;
		if (measures.getSolutionCount() > 0) {
			sat = ESat.TRUE;
			if (objectivemanager.isOptimization()) {
				measures.setObjectiveOptimal(!hasReachedLimit);
			}
		} else if (hasReachedLimit) {
			measures.setObjectiveOptimal(false);
			sat = ESat.UNDEFINED;
		}
		solver.setFeasible(sat);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void plugSearchMonitor(ISearchMonitor sm) {
		if (!smList.contains(sm)) {
			smList.add(sm);
		} else {
			LOGGER.warn("The search monitor already exists and is ignored");
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
		interrupt(MSG_LIMIT);
	}

	@Override
	public void setLastDecision(Decision d){
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
		while (tmp != RootDecision.ROOT) {
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
	public int getTimeStamp(){
		return timeStamp;
	}

	@Override
	public Decision getLastDecision(){
		return decision;
	}

	@Override
	public SearchMonitorList getSMList(){
		return smList;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// DEFAULT SEARCH /////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void defaultSearchStrategy(Solver solver) {
		AbstractStrategy[] strats = new AbstractStrategy[5];
		int nb = 0;

		// INTEGER VARIABLES DEFAULT SEARCH STRATEGY
		IntVar[] ivars = excludeConstants(solver.retrieveIntVars());
		if (ivars.length > 0) {
			strats[nb++] = ISF.minDom_LB(ivars);
		}

		// BOOLEAN VARIABLES DEFAULT SEARCH STRATEGY
		BoolVar[] bvars = excludeConstants(solver.retrieveBoolVars());
		if (bvars.length > 0) {
			strats[nb++] = ISF.lexico_UB(bvars);
		}

		// SET VARIABLES DEFAULT SEARCH STRATEGY
		SetVar[] svars = excludeConstants(solver.retrieveSetVars());
		if (svars.length > 0) {
			strats[nb++] = SetStrategyFactory.force_minDelta_first(svars);
		}

		// GRAPH VARIABLES DEFAULT SEARCH STRATEGY
		GraphVar[] gvars = excludeConstants(solver.retrieveGraphVars());
		if (gvars.length > 0) {
			AbstractStrategy<GraphVar>[] gstrats = new AbstractStrategy[gvars.length];
			for (int g = 0; g < gvars.length; g++) {
				gstrats[g] = GraphStrategyFactory.graphLexico(gvars[g]);
			}
			strats[nb++] = new StrategiesSequencer(gstrats);
		}

		// REAL VARIABLES DEFAULT SEARCH STRATEGY
		RealVar[] rvars = excludeConstants(solver.retrieveRealVars());
		if (rvars.length > 0) {
			strats[nb] = new RealStrategy(rvars, new Cyclic(), new RealDomainMiddle());
		}

		if (nb==0) {
			// simply to avoid null pointers in case all variables are instantiated
			solver.set(ISF.minDom_LB(solver.ONE));
		}else{
			solver.set(Arrays.copyOf(strats, nb));
		}
	}

	private static <V extends Variable> V[] excludeConstants(V[] vars){
		int nb = 0;
		for(V v:vars){
			if((v.getTypeAndKind() & Variable.CSTE) == 0){
				nb++;
			}
		}
		if(nb==vars.length)return vars;
		V[] noCsts;
		switch (vars[0].getTypeAndKind() & Variable.KIND){
			case Variable.BOOL:	noCsts = (V[]) new BoolVar[nb];	break;
			case Variable.INT:	noCsts = (V[]) new IntVar[nb];	break;
			case Variable.SET:	noCsts = (V[]) new SetVar[nb];	break;
			case Variable.GRAPH:noCsts = (V[]) new GraphVar[nb];break;
			case Variable.REAL:	noCsts = (V[]) new RealVar[nb];	break;
			default:
				throw new UnsupportedOperationException();
		}
		nb = 0;
		for(V v:vars){
			if((v.getTypeAndKind() & Variable.CSTE) == 0){
				noCsts[nb++] = v;
			}
		}
		return noCsts;
	}
}
