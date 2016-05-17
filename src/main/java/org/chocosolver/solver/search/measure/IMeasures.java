/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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


import org.chocosolver.solver.objective.BoundsManager;
import org.chocosolver.solver.search.SearchState;

/**
 * Interface for providing resolution statistics
 *
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public interface IMeasures<N extends Number> {

    /**
     * @return name of the model/solver observed
     */
    String getModelName();

    /**
     * @return the current world unique id
     */
    long getTimestamp();

    /**
     * @return the time count (in seconds), including initial propagation time count
     */
    float getTimeCount();

    /**
     * @return the time count (in nano seconds), including initial propagation time count
     */
    long getTimeCountInNanoSeconds();

    /**
     * @return the reading time count (in sec)
     */
    float getReadingTimeCount();

    /**
     * @return the node count
     */
    long getNodeCount();

    /**
     * @return the backtrack count
     */
    long getBackTrackCount();

    /**
     * @return the fail count
     */
    long getFailCount();

    /**
     * @return the restart count
     */
    long getRestartCount();

    /**
     * @return the solution count of the measure
     */
    long getSolutionCount();

    /**
     * @return the maximum depth of the search tree
     */
    long getMaxDepth();

    /**
     * @return the current depth in the search tree
     */
    long getCurrentDepth();

    /**
     * @return true iff the problem has an objective variable (i.e. optimization problem)
     */
    boolean hasObjective();

    /**
     * @return true iff the optimum has been found and proved
     */
    boolean isObjectiveOptimal();

    /**
     * @return the objective value of the best solution found (can be Integer or Double)
     */
    N getBestSolutionValue();

    /**
     * @return the search state
     */
    SearchState getSearchState();

    /**
     * @return current bound manager
     */
    BoundsManager<N> getBoundsManager();

    /**
     * @return a summary of recorded statistics
     */
    default String toOneLineString() {
        StringBuilder st = new StringBuilder(256);
        st.append("Model[").append(getModelName()).append("], ");
        st.append(String.format("%d Solutions, ", getSolutionCount()));
        if (hasObjective()) {
            st.append(getBoundsManager()).append(", ");
        }
        st.append(String.format("Resolution time %.3fs, %d Nodes (%,.1f n/s), %d Backtracks, %d Fails, %d Restarts",
                getTimeCount(),
                getNodeCount(),
                getNodeCount() / getTimeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount()));
        return st.toString();
    }

    /**
     * @return statistic values only
     */
    default Number[] toArray() {
        return new Number[]{
                getSolutionCount(),
                getReadingTimeCount(),
                getTimeCount(),
                hasObjective() ? getBestSolutionValue() : 0,
                getNodeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount()
        };
    }

    /**
     * @return statistics in a CSV format
     */
    default String toCSV() {
        // solutionCount;buildingTime(sec);initTime(sec);initPropag(sec);totalTime(sec);objective;nodes;backtracks;fails;restarts;fineProp;coarseProp;
        return String.format("%d;%.3f;%.3f;%e;%d;%d;%d;%d;",
                getSolutionCount(),
                getReadingTimeCount(),
                getTimeCount(),
                hasObjective() ? getBestSolutionValue().doubleValue() : 0,
                getNodeCount(),
                getBackTrackCount(),
                getFailCount(),
                getRestartCount());
    }

    /**
     * copy the values
     *
     * @return a new IMeasures with same values.
     */
    IMeasures copyMeasures();
}
