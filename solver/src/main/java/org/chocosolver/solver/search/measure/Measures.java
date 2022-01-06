/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.measure;


import org.chocosolver.solver.objective.IBoundsManager;
import org.chocosolver.solver.objective.ObjectiveFactory;
import org.chocosolver.solver.search.SearchState;

/**
 * Object which stores resolution information to get statistics
 *
 * @author Charles Prud'Homme, Arnaud Malapert
 */
public class Measures implements IMeasures, Cloneable {

    private static final long serialVersionUID = -474763044797821410L;

    //***********************************************************************************
    // VARIABLE
    //***********************************************************************************

    /**
     * Name of the model observed -- no reference to the model should be done in this class
     */
    protected String modelName;

    /**
     * Search state
     */
    protected SearchState state;

    /**
     * Reference to the bound manager
     */
    protected IBoundsManager boundsManager;

    /**
     * Indicates if the optimal value has been proven for the objective (set to <tt>true</tt>).
     */
    protected boolean objectiveOptimal;

    /**
     * Counts the number of solutions found so far.
     */
    protected long solutionCount;

    /**
     * Counts the time spent so far, starting from solver construction call.
     */
    protected long timeCount;

    /**
     * Gives the time to get the best (or first in case of satisfaction problems) solution.
     */
    protected long timeToBestSolution;

    /**
     * Counts the time spent into reading the model
     */
    protected long readingTimeCount;

    /**
     * Counts the number of nodes opened so far.
     */
    protected long nodeCount;

    /**
     * Counts the number of backtracks done so far.
     */
    protected long backtrackCount;

    /**
     * Counts the number of failures encountered so far.
     */
    protected long failCount;

    /**
     * Counts the number of restarts done so far.
     */
    protected long restartCount;

    /**
     * Stores the overall maximum depth
     */
    protected long maxDepth;

    /**
     * Stores the current depth
     */
    protected long depth;

    /**
     * Stores the number of fixpoints
     */
    protected long fixpointCount;

    /**
     * Counts the number of non chronological backtracks done so far.
     */
    protected long backjumpCount;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create empty measures.
     */
    public Measures(String modelName) {
        super();
        this.modelName = modelName;
        this.state = SearchState.NEW;
        this.boundsManager = ObjectiveFactory.SAT();
    }

    /**
     * Copy constructor
     * with shallow copy of the bounds manager.
     * @param measures to copy
     */
    public Measures(IMeasures measures) {
        super();
        modelName = measures.getModelName();
        state = measures.getSearchState();
        boundsManager = measures.getBoundsManager();
        objectiveOptimal = measures.isObjectiveOptimal();
        solutionCount = measures.getSolutionCount();
        timeCount = measures.getTimeCountInNanoSeconds();
        readingTimeCount = measures.getReadingTimeCountInNanoSeconds();
        nodeCount = measures.getNodeCount();
        backtrackCount = measures.getBackTrackCount();
        restartCount = measures.getRestartCount();
        failCount = measures.getFailCount();
        depth = measures.getCurrentDepth();
        maxDepth = measures.getMaxDepth();
    }

    //****************************************************************************************************************//
    //**************************************** GETTERS ***************************************************************//
    //****************************************************************************************************************//


    @Override
    public final long getBackTrackCount() {
        return backtrackCount;
    }

    @Override
    public final long getFailCount() {
        return failCount;
    }

    @Override
    public final long getFixpointCount() {
        return fixpointCount;
    }

    @Override
    public long getBackjumpCount() {
        return backjumpCount;
    }

    @Override
    public final long getNodeCount() {
        return nodeCount;
    }


    @Override
    public long getTimeCountInNanoSeconds() {
        return timeCount;
    }

    @Override
    public long getTimeToBestSolutionInNanoSeconds() {
        return timeToBestSolution;
    }

    @Override
    public long getReadingTimeCountInNanoSeconds() {
        return readingTimeCount;
    }


    @Override
    public final long getRestartCount() {
        return restartCount;
    }

    @Override
    public final long getMaxDepth() {
        return maxDepth;
    }

    @Override
    public final long getCurrentDepth() {
        return depth;
    }

    @Override
    public final boolean isObjectiveOptimal() {
        return objectiveOptimal;
    }

    @Override
    public final boolean hasObjective() {
        return boundsManager.isOptimization();
    }

    @Override
    public final Number getBestSolutionValue() {
        return boundsManager.getBestSolutionValue();
    }

    @Override
    public final SearchState getSearchState() {
        return state;
    }

    @Override
    public final String getModelName() {
        return modelName;
    }

    @Override
    public long getTimestamp() {
        return nodeCount + backtrackCount;
    }

    @Override
    public final IBoundsManager getBoundsManager() {
        return boundsManager;
    }

    @Override
    public final long getSolutionCount() {
        return solutionCount;
    }

    @Override
    public long getDecisionCount() {
        return nodeCount - solutionCount;
    }

    @Override
    public String toString() {
        return toMultiLineString();
    }
}
