/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package solver.search.measure;


import solver.search.loop.monitors.ISearchMonitor;

import java.io.Serializable;

public interface IMeasures extends ISearchMonitor, Serializable {

    void reset();

    String toOneLineString();

    String toString();

    /**
     * Get current world unique id
     * @return
     */
    long timestamp();

    /**
     * Get the time count in milliseconds of the measure
     *
     * @return time count
     */
    long getTimeCount();

    /**
     * Get the node count of the measure
     *
     * @return node count
     */
    long getNodeCount();

    /**
     * Get the backtrack count of the measure
     *
     * @return backtrack count
     */
    long getBackTrackCount();

    /**
     * Get the fail count of the measure
     *
     * @return fail count
     */
    long getFailCount();

    /**
     * Get the restart count of the measure
     *
     * @return restart count
     */
    long getRestartCount();

    /**
     * Get the number of call to event recorder execution
     *
     * @return propagations count
     */
    long getPropagationsCount();

    long getEventsCount();

    /**
     * Get the used memory
     *
     * @return used memory
     */
    long getUsedMemory();

    /**
     * Get the solution count of the measure
     *
     * @return solution count
     */
    long getSolutionCount();

    /**
     * Get the best objective value of a solution.
     */
    int getObjectiveValue();

    boolean isObjectiveOptimal();

    boolean hasObjective();

    void setObjectiveValue(int value);

    void setObjectiveOptimal(boolean objectiveOptimal);

    void declareObjective();

    long getReadingTimeCount();

    void setReadingTimeCount(long time);

    long getInitialPropagationTimeCount();

    long getInitialisationTimeCount();

    void updatePropagationCount();

    /**
     * Updates the time recorder
     */
    void updateTimeCount();
}
