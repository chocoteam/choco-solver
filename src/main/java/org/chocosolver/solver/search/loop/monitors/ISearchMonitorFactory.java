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
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.*;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;
import org.chocosolver.util.tools.TimeUtils;

/**
 * Interface to define some search monitors to be used
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 */
public interface ISearchMonitorFactory extends ISelf<Solver> {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Record nogoods from solution, that is, anytime a solution is found, a nogood is produced to prevent from
     * finding the same solution later during the search.
     * <code>vars</code> are the decision variables (to reduce ng size).
     *
     * @param vars array of decision variables
     */
    default void setNoGoodRecordingFromSolutions(IntVar... vars) {
        _me().plugMonitor(new NogoodFromSolutions(vars));
    }

    /**
     * * Record nogoods from restart, that is, anytime the search restarts, a nogood is produced, based on the decision path, to prevent from
     * scanning the same sub-search tree.
     */
    default void setNoGoodRecordingFromRestarts() {
        _me().plugMonitor(new NogoodFromRestarts(_me().getModel()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Connect and send data to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
     * This requires to have installed the library and to start it before launching the resolution.
     */
    @SuppressWarnings("unused")
    default void connectocpprofiler(){
        _me().plugMonitor(new CPProfiler(_me().getModel()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Limit the exploration of the search space with the help of a <code>aStopCriterion</code>.
     * When the condition depicted in the criterion is met,
     * the search stops.
     *
     * @param aStopCriterion the stop criterion which, when met, stops the search.
     */
    default void limitSearch(Criterion aStopCriterion) {
        _me().addStopCriterion(aStopCriterion);
    }

    /**
     * Defines a limit on the number of nodes allowed in the tree search.
     * When the limit is reached, the resolution is stopped.
     * @param limit maximal number of nodes to open
     */
    default void limitNode(long limit) {
       limitSearch(new NodeCounter(_me().getModel(), limit));
    }

    /**
     * Defines a limit over the number of fails allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     * @param limit maximal number of fails
     */

    default void limitFail(long limit) {
        limitSearch(new FailCounter(_me().getModel(), limit));
    }

    /**
     * Defines a limit over the number of backtracks allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param limit maximal number of backtracks
     */
    default void limitBacktrack(long limit) {
        limitSearch(new BacktrackCounter(_me().getModel(), limit));
    }

    /**
     * Defines a limit over the number of solutions found during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     * @param limit maximal number of solutions
     */
    default void limitSolution(long limit) {
        limitSearch(new SolutionCounter(_me().getModel(), limit));
    }

    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     * <br/>
     * <b>One must consider also {@code SearchMonitorFactory.limitThreadTime(long)}, that runs the limit in a separated thread.</b>
     * @param limit  maximal resolution time in millisecond
     */
    default void limitTime(long limit) {
        limitSearch(new TimeCounter(_me().getModel(), limit * TimeUtils.MILLISECONDS_IN_NANOSECONDS));
    }

    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     * <br/>
     * <br/>
     * <b>One must consider also {@code SearchMonitorFactory.limitThreadTime(String)}, that runs the limit in a separated thread.</b>
     * <p>
     * Based on {@code SearchMonitorFactory.convertInMilliseconds(String duration)}
     *
     * @param duration a String which states the duration like "WWd XXh YYm ZZs".
     * @see TimeUtils#convertInMilliseconds(String)
     */
    default void limitTime(String duration) {
        limitTime(TimeUtils.convertInMilliseconds(duration));
    }
}
