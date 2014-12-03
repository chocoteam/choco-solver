/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.search.measure;

import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;

import java.io.Serializable;

/**
 * Interface for providing resolution statistics
 * @author Charles Prud'Homme, Jean-Guillaume Fages
 */
public interface IMeasures extends ISearchMonitor, Serializable, Cloneable {

	/**
	 * Clones the IMeasure object (copy every measure)
	 * @return a new instance of IMeasure
	 */
	IMeasures clone();

	/** Reset every measure to its default value (mostly 0) */
    void reset();

	/** @return a summary of recorded statistics */
    String toOneLineString();

	/** @return a short summary of recorded statistics */
    String toOneShortLineString();

	/** @return statistics */
    String toString();

	/** @return statistics in a CSV format */
    String toCSV();

	/** @return statistic values only */
    Number[] toArray();

    /** @return the current world unique id */
    long timestamp();

    /** @return the time count (in sec), including initial propagation time count */
    float getTimeCount();

    /** @return the reading time count (in sec) */
    float getReadingTimeCount();

    /** @return the initialization time count (in sec) */
    float getInitialisationTimeCount();

    /** @return the initial propagation time count (in sec)*/
    float getInitialPropagationTimeCount();

	/** set the reading time count */
    void setReadingTimeCount(long time);

    /** Updates the time recorder */
    void updateTimeCount();

    /** @return the node count */
    long getNodeCount();

    /** @return the backtrack count */
    long getBackTrackCount();

    /** @return the fail count */
    long getFailCount();

    /** @return the restart count */
    long getRestartCount();

	/** @return the maximum depth of the search tree */
    long getMaxDepth();

	/** @return the current depth in the search tree */
    long getCurrentDepth();

    /** @return the number of call to event recorder execution */
    long getPropagationsCount();

	/** Updates the propagation recorder */
    void updatePropagationCount();

	/** @return the fine event count (incremental propagations) */
    long getEventsCount();

    /** @return the used memory */
    long getUsedMemory();

	/** updates the memory usage count */
	void updateMemoryUsed();

    /** @return the solution count of the measure */
    long getSolutionCount();

	/** indicates an objective variable */
    void declareObjective();

	/** @return true iff the problem has an objective variable (i.e. optimization problem) */
	boolean hasObjective();

	/** indicates whether or not the optimum has been found and proved */
	void setObjectiveOptimal(boolean objectiveOptimal);

	/** @return true iff the optimum has been found and proved */
	boolean isObjectiveOptimal();

	/** @return the objective value of the best solution found (can be Integer or Double) */
	public Number getBestSolutionValue();
}
