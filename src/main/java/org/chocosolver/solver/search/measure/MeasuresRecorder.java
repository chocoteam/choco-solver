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

import org.chocosolver.solver.objective.BoundsManager;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.SearchState;

/**
 * Object which stores resolution information to get statistics
 *
 * @author Charles Prud'Homme, Arnaud Malapert
 * @since 3.0.0
 */
public class MeasuresRecorder implements IMeasures, Cloneable {

    //***********************************************************************************
    // VARIABLE
    //***********************************************************************************

    /**
     * To transform time from nanoseconds to seconds
     */
    protected static final float IN_SEC = 1000 * 1000 * 1000f;

    /**
     * Name of the model observed -- no reference to the model should be done in this class
     */
    protected String modelName;

    /**
     * Reference to the bound manager
     */
    protected BoundsManager<?> boundsManager;

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
     * When the clock watch starts
     */
    protected long startingTime;

    /**
     * Search state
     */
    protected SearchState state;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create a measures recorder
     */
    public MeasuresRecorder(String modelName) {
        super();
        this.modelName = modelName;
        this.boundsManager = ObjectiveManager.SAT();
    }

    //****************************************************************************************************************//
    //**************************************** GETTERS ***************************************************************//
    //****************************************************************************************************************//

    @Override
    public long getBackTrackCount() {
        return backtrackCount;
    }

    @Override
    public long getFailCount() {
        return failCount;
    }

    @Override
    public long getNodeCount() {
        return nodeCount;
    }

    @Override
    public float getTimeCount() {
        updateTime();
        return timeCount / MeasuresRecorder.IN_SEC;
    }

    @Override
    public long getTimeCountInNanoSeconds() {
        updateTime();
        return timeCount;
    }

    @Override
    public float getReadingTimeCount() {
        return readingTimeCount / MeasuresRecorder.IN_SEC;
    }

    /**
     * set the reading time count
     * @param time time needed to read the model
     */
    public void setReadingTimeCount(long time) {
        readingTimeCount = time;
    }

    @Override
    public long getRestartCount() {
        return restartCount;
    }

    @Override
    public long getMaxDepth() {
        return maxDepth;
    }

    @Override
    public long getCurrentDepth() {
        return depth;
    }

    @Override
    public boolean isObjectiveOptimal() {
        return objectiveOptimal;
    }

    @Override
    public boolean hasObjective() {
        return boundsManager.isOptimization();
    }

    @Override
    public Number getBestSolutionValue() {
        return boundsManager.getBestSolutionValue();
    }

    @Override
    public SearchState getSearchState() {
        return state;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public long getTimestamp() {
        return nodeCount + backtrackCount;
    }

    //****************************************************************************************************************//
    //**************************************** SETTERS ***************************************************************//
    //****************************************************************************************************************//

    @Override
    public long getSolutionCount() {
        return solutionCount;
    }

    /**
     * indicates whether or not the optimum has been found and proved
     * @param objectiveOptimal <tt>true</tt> if the objective is proven to be optimal
     */
    public void setObjectiveOptimal(boolean objectiveOptimal) {
        this.objectiveOptimal = objectiveOptimal;
    }

    /**
     * Reset every measure to its default value (mostly 0)
     */
    public final void reset() {
        timeCount = 0;
        nodeCount = 0;
        backtrackCount = 0;
        restartCount = 0;
        failCount = 0;
        solutionCount = 0;
        depth = 0;
        maxDepth = 0;
    }

    //****************************************************************************************************************//
    //**************************************** INCREMENTERS **********************************************************//
    //****************************************************************************************************************//

    private void updateTime() {
        timeCount = System.nanoTime() - startingTime;
    }

    /**
     * increment node counter
     */
    public void incNodeCount() {
        nodeCount++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    /**
     * increment backtrack counter
     */
    public void incBackTrackCount() {
        backtrackCount++;
    }

    /**
     * increment fail counter
     */
    public void incFailCount() {
        failCount++;
    }

    /**
     * increment restart counter
     */
    public void incRestartCount() {
        restartCount++;
    }

    /**
     * increment solution counter
     */
    public void incSolutionCount() {
        solutionCount++;
        updateTime();
    }

    /**
     * Increments current depth
     */
    public void incDepth() {
        depth++;
    }

    /**
     * Decrements current depth
     */
    public void decDepth() {
        depth--;
    }

    /**
     * Start the stopwatch, to compute resolution time
     */
    public void startStopwatch() {
        startingTime = System.nanoTime();
    }

    /**
     * Update the current search state
     * @param state new search state
     */
    public void setSearchState(SearchState state){
        this.state = state;
    }

    /**
     * Update the bounds managed
     * @param boundsManager new bound manager
     */
    public void setBoundsManager(BoundsManager<?> boundsManager){
        Objects.requireNonNull(boundsManager);
	this.boundsManager = boundsManager;
    }

    @Override
    public IMeasures copyMeasures() {
        MeasuresRecorder ret = new MeasuresRecorder(modelName);
        ret.boundsManager = new BoundsManager<>(boundsManager);
        ret.timeCount = this.timeCount;
        ret.nodeCount = this.nodeCount;
        ret.backtrackCount = this.backtrackCount;
        ret.restartCount = this.restartCount;
        ret.failCount = this.failCount;
        ret.solutionCount = this.solutionCount;
        ret.depth = this.depth;
        ret.maxDepth = this.maxDepth;
        ret.objectiveOptimal = this.objectiveOptimal;
        ret.readingTimeCount = this.readingTimeCount;
        ret.startingTime = this.startingTime;
        return ret;
    }

    protected void set(IMeasures measures) {
        timeCount = (long) (measures.getTimeCount() * MeasuresRecorder.IN_SEC);
        nodeCount = measures.getNodeCount();
        backtrackCount = measures.getBackTrackCount();
        restartCount = measures.getRestartCount();
        failCount = measures.getFailCount();
        solutionCount = measures.getSolutionCount();
        depth = measures.getCurrentDepth();
        maxDepth = measures.getMaxDepth();
        objectiveOptimal = measures.isObjectiveOptimal();
        readingTimeCount = (long) (measures.getReadingTimeCount() * MeasuresRecorder.IN_SEC);
    }

    @Override
    public BoundsManager<?> getBoundsManager() {
        return boundsManager;
    }

    //****************************************************************************************************************//

    @Override
    public String toString() {
        updateTime();
        StringBuilder st = new StringBuilder(256);
//        st.append("- Search statistics\n");
        switch (state){
            case NEW:
                st.append("- Search not started- ");
                break;
            case RUNNING:
                st.append("- Running search - ");
                break;
            case TERMINATED:
                st.append("- Complete search - ");
                if (solutionCount == 0) {
                    st.append("No solution.");
                } else if (solutionCount == 1) {
                    st.append("1 solution found.");
                } else {
                    st.append(String.format("%,d solution(s) found.", solutionCount));
                }
                st.append('\n');
                break;
            case STOPPED:
                st.append("- Incomplete search - Limit reached.\n");
                break;
            case KILLED:
                st.append("- Incomplete search - Unexpected interruption.\n");
                break;
        }
        st.append("\tModel[").append(modelName).append("]\n");
        st.append(String.format("\tSolutions: %,d\n", solutionCount));
        if (hasObjective()) {
            st.append("\t").append(boundsManager).append(",\n");
        }
        st.append(String.format("\tBuilding time : %,.3fs" +
                        "\n\tResolution time : %,.3fs\n\tNodes: %,d (%,.1f n/s) \n\tBacktracks: %,d\n\tFails: %,d\n\t" +
                        "Restarts: %,d",
                getReadingTimeCount(),
                getTimeCount(),
                getNodeCount(),
                getNodeCount() / getTimeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount()
        ));
        return st.toString();
    }
}
