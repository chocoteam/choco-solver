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
     * To transform time from nanoseconds to seconds
     */
    protected static final float IN_SEC = 1000 * 1000 * 1000f;

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
     * @param measures to copy
     */
    public Measures(IMeasures measures) {
        super();
        boundsManager = ObjectiveFactory.copy(measures.getBoundsManager());
        objectiveOptimal = measures.isObjectiveOptimal();
        solutionCount = measures.getSolutionCount();
        timeCount = measures.getTimeCountInNanoSeconds();
        readingTimeCount = (long) (measures.getReadingTimeCount() * IN_SEC);
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
    public final long getNodeCount() {
        return nodeCount;
    }


    @Override
    public long getTimeCountInNanoSeconds() {
        return timeCount;
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
    public String toString() {
        return toMultiLineString();
    }
}
