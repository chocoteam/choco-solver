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
package org.chocosolver.solver.search.measure;

import java.util.Objects;
import java.util.function.LongSupplier;

import org.chocosolver.solver.objective.BoundsManager;
import org.chocosolver.solver.search.SearchState;

/**
 * Object which stores resolution information to get statistics
 *
 * @author Charles Prud'Homme, Arnaud Malapert
 * @since 3.0.0
 */
public final class MeasuresRecorder extends Measures {

    /**
     * When the clock watch starts
     */
    private long startingTime;

    private LongSupplier currentNanoTime;
    /**
     * Create a measures recorder
     */
    public MeasuresRecorder(String modelName) {
        super(modelName);
        stopTimer();
    }

    @Override
    public long getTimeCountInNanoSeconds() {
	timeCount = currentNanoTime.getAsLong();
	return super.getTimeCountInNanoSeconds();
    }

    /**
     * Start the stopwatch, to compute resolution time
     * @deprecated
     * 
     * @see {@link MeasuresRecorder#startTimer()} and {@link MeasuresRecorder#stopTimer()} 
     */
    @Deprecated
    public void startStopwatch() {
        startTimer();
    }
    
    /**
     * Start the watch, to compute resolution time
     */
    public void startTimer() {
        startingTime = System.nanoTime();
        currentNanoTime = () -> System.nanoTime()-startingTime;
    }
    
    /**
     * Stop the watch, the resolution time is fixed.
     */
    public void stopTimer() {
        currentNanoTime = () -> timeCount;
    }
    
    //****************************************************************************************************************//
    //**************************************** SETTERS ***************************************************************//
    //****************************************************************************************************************//


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
	// TODO state = SearchState.NEW; // CPRU ?
	objectiveOptimal = false;
	solutionCount = 0;
	timeCount = 0;
	// TODO readingTimeCount = 0; //CPRU ?
	// TODO stopWatch() //CPRU 
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
	if (depth > maxDepth) {
	    maxDepth = depth;
	}
    }

    /**
     * increment backtrack counter
     */
    public final void incBackTrackCount() {
	backtrackCount++;
    }

    /**
     * increment fail counter
     */
    public final void incFailCount() {
	failCount++;
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
     * Increments current depth
     */
    public final void incDepth() {
	depth++;
    }

    /**
     * Decrements current depth
     */
    public final void decDepth() {
	depth--;
    }

    /**
     * Update the current search state
     * @param state new search state
     */
    public final void setSearchState(SearchState state){
	Objects.requireNonNull(state);
	this.state = state;
    }

    /**
     * Update the bounds managed
     * @param boundsManager new bound manager
     */
    public final void setBoundsManager(BoundsManager<?> boundsManager){
	Objects.requireNonNull(boundsManager);
	this.boundsManager = boundsManager;
    }
    
    public final void setTimeCount(long timeCount) {
        this.timeCount = timeCount;
    }

    public final void setReadingTimeCount(long readingTimeCount) {
        this.readingTimeCount = readingTimeCount;
    }

}
