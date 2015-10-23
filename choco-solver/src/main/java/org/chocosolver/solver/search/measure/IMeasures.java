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

import java.io.Serializable;

/**
 * Interface for providing resolution statistics
 *
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public interface IMeasures extends Serializable, Cloneable {

    /**
     * Clones the IMeasure object (copy every measure)
     *
     * @return a new instance of IMeasure
     */
    IMeasures duplicate();

    /**
     * Reset every measure to its default value (mostly 0)
     */
    void reset();

    /**
     * @return a summary of recorded statistics
     */
    String toOneLineString();

    /**
     * @return a short summary of recorded statistics
     */
    String toOneShortLineString();

    /**
     * @return statistics
     */
    String toString();

    /**
     * @return statistics in a CSV format
     */
    String toCSV();

    /**
     * @return statistic values only
     */
    Number[] toArray();

    /**
     * @return the current world unique id
     */
    long timestamp();

    /**
     * @return the time count (in sec), including initial propagation time count
     */
    float getTimeCount();

    /**
     * Returns the elapsed time in nanoseconds
     * @return the elapsed time in nanoseconds
     */
    long getElapsedTimeInNanoseconds();

    void startStopwatch();

    void updateTime();

    /**
     * @return the reading time count (in sec)
     */
    float getReadingTimeCount();

    /**
     * set the reading time count
     */
    void setReadingTimeCount(long time);

    /**
     * @return the node count
     */
    long getNodeCount();

    /**
     * increment node counter
     */
    void incNodeCount();

    /**
     * @return the backtrack count
     */
    long getBackTrackCount();

    /**
     * increment backtrack counter
     */
    void incBackTrackCount();

    /**
     * @return the fail count
     */
    long getFailCount();

    /**
     * increment fail counter
     */
    void incFailCount();

    /**
     * @return the restart count
     */
    long getRestartCount();

    /**
     * increment restart counter
     */
    void incRestartCount();

    /**
     * @return the solution count of the measure
     */
    long getSolutionCount();

    /**
     * increment solution counter
     */
    void incSolutionCount();

    /**
     * indicates an objective variable
     */
    void declareObjective();

    /**
     * @return true iff the problem has an objective variable (i.e. optimization problem)
     */
    boolean hasObjective();

    /**
     * indicates whether or not the optimum has been found and proved
     */
    void setObjectiveOptimal(boolean objectiveOptimal);

    /**
     * @return true iff the optimum has been found and proved
     */
    boolean isObjectiveOptimal();

    /**
     * @return the objective value of the best solution found (can be Integer or Double)
     */
    Number getBestSolutionValue();
}
