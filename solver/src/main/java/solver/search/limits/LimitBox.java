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

package solver.search.limits;

import solver.Solver;
import solver.search.loop.AbstractSearchLoop;
import solver.search.loop.monitors.ISearchMonitor;
import solver.search.loop.monitors.VoidSearchMonitor;

import java.io.Serializable;

/**
 * This classes provides services to <code>add</code> and <code>delete</code> limits over the search loop.
 * At the very beginning of the search, the limit checkers are started, using <code>start</code>, and are
 * stopped, using <code>interrupt</code> at the end of the search.
 * During the search, when a limit is reached, the search loop is interrupted, and the resolution stops.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @see ILimit
 * @since 15 juil. 2010
 */
public class LimitBox extends VoidSearchMonitor implements Serializable, ISearchMonitor {

    int index;
    ILimit[] limits;
    final AbstractSearchLoop searchloop;


    public LimitBox(AbstractSearchLoop searchloop) {
        this.index = 0;
        this.limits = new ILimit[8];
        this.searchloop = searchloop;
    }

    /**
     * Adds a <code>limit</code> to the pool of limit checkers.
     *
     * @param limit object to add
     */
    public void add(ILimit limit) {
        if (index == 0) { // if not plugged uet
            searchloop.plugSearchMonitor(this);
        }
        ensureCapacity(index + 1);
        limits[index++] = limit;
    }

    private void ensureCapacity(int i) {
        if (limits.length <= i) {
            ILimit[] newLimits = new ILimit[limits.length * 3 / 2 + 1];
            System.arraycopy(limits, 0, newLimits, 0, index);
            limits = newLimits;
        }
    }

    /**
     * Deletes the <code>limit</code> from the list of limit checkers
     *
     * @param limit object to delete
     */
    public void delete(ILimit limit) {
        throw new UnsupportedOperationException();
    }


    private void hasEncounteredLimit() {
        for (int i = 0; i < index; i++) {
            if (limits[i].isReached()) {
                searchloop.interrupt();
                return;
            }
        }
    }

    /**
     * Inits the limit checkers
     */
    private void init() {
        for (int i = 0; i < index; i++) {
            limits[i].init();
        }
    }

    public boolean isReached() {
        for (int i = 0; i < index; i++) {
            if (limits[i].isReached()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     *
     * @param timelimit maximal resolution time in millisecond
     */
    public void setTimeLimit(long timelimit) {
        this.add(new TimeLimit(this.searchloop, timelimit));
    }

    /**
     * Defines a limit over the run time, set in a thread.
     * When the limit is reached, the resolution is stopped.
     *
     * @param timelimit maximal resolution time in millisecond
     */
    public void setThreadTimeLimit(long timelimit) {
        this.add(new ThreadTimeLimit(timelimit));
    }

    /**
     * Defines a limit on the number of nodes allowed in the tree search.
     * When the limit is reached, the resolution is stopped.
     *
     * @param nodelimit maximal number of nodes to open
     */
    public void setNodeLimit(long nodelimit) {
        this.add(new NodeLimit(this.searchloop, nodelimit));
    }

    /**
     * Defines a limit over the number of backtracks allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param backtracklimit maximal number of backtracks
     */
    public void setBacktrackLimit(long backtracklimit) {
        this.add(new BacktrackLimit(this.searchloop, backtracklimit));
    }

    /**
     * Defines a limit over the number of fails allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param failLimit maximal number of fails
     */
    public void setFailLimit(long failLimit) {
        this.add(new FailLimit(this.searchloop, failLimit));
    }

    /**
     * Defines a limit over the number of solutions found during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param solutionLimit maximal number of solutions
     */
    public void setSolutionLimit(long solutionLimit) {
        this.add(new SolutionLimit(this.searchloop, solutionLimit));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static ILimit timeLimit(Solver solver, long timelimit) {
        return new TimeLimit(solver.getSearchLoop(), timelimit);
    }

    public static ILimit nodeLimit(Solver solver, long nodeLimit) {
        return new NodeLimit(solver.getSearchLoop(), nodeLimit);
    }

    public static ILimit backTrackLimit(Solver solver, long backtracklimit) {
        return new BacktrackLimit(solver.getSearchLoop(), backtracklimit);
    }

    public static ILimit failLimit(Solver solver, long faillimit) {
        return new FailLimit(solver.getSearchLoop(), faillimit);
    }

    public static ILimit solutionLimit(Solver solver, long solutionLimit) {
        return new SolutionLimit(solver.getSearchLoop(), solutionLimit);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void afterInitialize() {
        this.init();
    }

    @Override
    public void afterOpenNode() {
        this.hasEncounteredLimit();
    }

    @Override
    public void afterUpBranch() {
        this.hasEncounteredLimit();
    }
}
