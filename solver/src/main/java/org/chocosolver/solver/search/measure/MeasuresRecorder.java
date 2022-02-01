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
import org.chocosolver.solver.search.SearchState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Object which stores resolution information to get statistics
 *
 * @author Charles Prud'Homme, Arnaud Malapert
 * @since 3.0.0
 */
public final class MeasuresRecorder extends Measures {

    private static final long serialVersionUID = -2027525308178413040L;

    /**
     * When the clock watch starts
     */
    private long startingTime;

    transient private LongSupplier currentNanoTime;

    /**
     * Create a measures recorder
     */
    public MeasuresRecorder(String modelName) {
        super(modelName);
        currentNanoTime = () -> timeCount;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        getTimeCountInNanoSeconds();
        out.defaultWriteObject();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // our "pseudo-constructor"
        in.defaultReadObject();
        // now we are a "live" object again, so let's run rebuild and start
        currentNanoTime = () -> timeCount;
    }

    @Override
    public long getTimeCountInNanoSeconds() {
        timeCount = currentNanoTime.getAsLong();
        return super.getTimeCountInNanoSeconds();
    }

    public void updateTimeToBestSolution() {
        timeToBestSolution = currentNanoTime.getAsLong();
    }

    /**
     * Start the stopwatch, to compute resolution time
     *
     */
    public void startStopwatch() {
        startingTime = System.nanoTime();
        currentNanoTime = () -> System.nanoTime() - startingTime;
    }

    /**
     * Stop the stopwatch, the resolution time is fixed.
     */
    public void stopStopwatch() {
        timeCount = currentNanoTime.getAsLong();
        currentNanoTime = () -> timeCount;
    }

    //****************************************************************************************************************//
    //**************************************** SETTERS ***************************************************************//
    //****************************************************************************************************************//

    /**
     * Update the name of the model to Measures
     * @param name name of the model
     */
    public final void setModelName(String name){
        this.modelName = name;
    }

    /**
     * indicates whether or not the optimum has been found and proved
     * @param objectiveOptimal <tt>true</tt> if the objective is proven to be optimal
     */
    public final void setObjectiveOptimal(boolean objectiveOptimal) {
        this.objectiveOptimal = objectiveOptimal;
    }

    /**
     * Reset every measure to its default value (mostly 0)
     */
    public void reset() {
        state = SearchState.NEW;
        objectiveOptimal = false;
        solutionCount = 0;
        stopStopwatch();
        timeCount = 0;
        nodeCount = 0;
        backtrackCount = 0;
        failCount = 0;
        restartCount = 0;
        depth = 0;
        maxDepth = 0;
    }

    //****************************************************************************************************************//
    //**************************************** INCREMENTERS **********************************************************//
    //****************************************************************************************************************//

    /**
     * increment node counter
     */
    public final void incNodeCount() {
        nodeCount++;
        depth = getCurrentDepth();
        maxDepth = Math.max(maxDepth, depth);
    }

    /**
     * increment backtrack counter
     */
    public final void incBackTrackCount() {
        backtrackCount++;
    }

    /**
     * increment backtrack counter
     */
    public final void incBackjumpCount() {
        backjumpCount++;
    }

    /**
     * increment fail counter
     */
    public final void incFailCount() {
        failCount++;
    }

    /**
     * increment fail counter
     */
    public final void incFixpointCount() {
        fixpointCount++;
    }

    /**
     * increment restart counter
     */
    public final void incRestartCount() {
        restartCount++;
    }

    /**
     * increment solution counter
     */
    public final void incSolutionCount() {
        solutionCount++;
    }


    /**
     * Update the current search state
     * @param state new search state
     */
    public final void setSearchState(SearchState state) {
        Objects.requireNonNull(state);
        this.state = state;
    }

    /**
     * Update the bounds managed
     * @param boundsManager new bound manager
     */
    public final void setBoundsManager(IBoundsManager boundsManager) {
        Objects.requireNonNull(boundsManager);
        this.boundsManager = boundsManager;
    }

    public final void setReadingTimeCount(long readingTimeCount) {
        if(this.readingTimeCount <= 0) {
            this.readingTimeCount = readingTimeCount;
        }
    }

}
