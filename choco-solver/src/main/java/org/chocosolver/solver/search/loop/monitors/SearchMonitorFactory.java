/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.limits.*;
import org.chocosolver.solver.search.restart.GeometricalRestartStrategy;
import org.chocosolver.solver.search.restart.LubyRestartStrategy;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/05/11
 */
public class SearchMonitorFactory {
    SearchMonitorFactory() {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Branch a luby restart strategy to the solver
     *
     * @param solver               the solver
     * @param scaleFactor          scale factor
     * @param geometricalFactor    increasing factor
     * @param restartStrategyLimit restart trigger
     * @param restartLimit         restart limits (limit of number of restarts)
     */
    public static void luby(Solver solver, int scaleFactor, int geometricalFactor,
                            ICounter restartStrategyLimit, int restartLimit) {
        solver.plugMonitor(new RestartManager(
                new LubyRestartStrategy(scaleFactor, geometricalFactor),
                restartStrategyLimit, solver.getSearchLoop(), restartLimit
        ));
    }

    /**
     * Build a geometrical restart strategy
     *
     * @param solver               the solver
     * @param scaleFactor          scale factor
     * @param geometricalFactor    increasing factor
     * @param restartStrategyLimit restart trigger
     * @param restartLimit         restart limits (limit of number of restarts)
     */
    public static void geometrical(Solver solver, int scaleFactor, double geometricalFactor,
                                   ICounter restartStrategyLimit, int restartLimit) {
        solver.plugMonitor(new RestartManager(
                new GeometricalRestartStrategy(scaleFactor, geometricalFactor),
                restartStrategyLimit, solver.getSearchLoop(), restartLimit
        ));
    }

    /**
     * Defines a limit on the number of nodes allowed in the tree search.
     * When the limit is reached, the resolution is stopped.
     *
     * @param limit maximal number of nodes to open
     */
    public static void limitNode(Solver solver, long limit) {
        NodeCounter counter = new NodeCounter(limit);
        counter.setAction(ActionCounterFactory.interruptSearch(solver.getSearchLoop()));
        solver.plugMonitor(counter);
    }

    /**
     * Defines a limit over the number of solutions found during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param limit maximal number of solutions
     */
    public static void limitSolution(Solver solver, long limit) {
        SolutionCounter counter = new SolutionCounter(limit);
        counter.setAction(ActionCounterFactory.interruptSearch(solver.getSearchLoop()));
        solver.plugMonitor(counter);
    }


    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     * <br/>
     * <br/>
     * <b>One must consider also {@code SearchMonitorFactory.limitThreadTime(long)}, that runs the limit in a separated thread.</b>
     *
     * @param solver the solver subject to the time limit
     * @param limit  maximal resolution time in millisecond
     * @see SearchMonitorFactory#limitThreadTime(org.chocosolver.solver.Solver, long)
     */
    public static void limitTime(Solver solver, long limit) {
        TimeCounter counter = new TimeCounter(solver, limit);
        counter.setAction(ActionCounterFactory.interruptSearch(solver.getSearchLoop()));
        solver.plugMonitor(counter);
    }

    /**
     * Defines a limit over the run time.
     * When the limit is reached, the resolution is stopped.
     * <br/>
     * <br/>
     * <b>One must consider also {@code SearchMonitorFactory.limitThreadTime(String)}, that runs the limit in a separated thread.</b>
     * <p/>
     * Based on {@code SearchMonitorFactory.convertInMilliseconds(String duration)}
     *
     * @param duration a String which states the duration like "WWd XXh YYm ZZs".
     * @see SearchMonitorFactory#limitThreadTime(org.chocosolver.solver.Solver, long)
     * @see SearchMonitorFactory#convertInMilliseconds(String)
     */
    public static void limitTime(Solver solver, String duration) {
        limitTime(solver, convertInMilliseconds(duration));
    }

    /**
     * Defines a limit over the run time, set in a thread.
     * When the limit is reached, the resolution is stopped.
     *
     * @param limit maximal resolution time in millisecond
     * @see SearchMonitorFactory#limitTime(org.chocosolver.solver.Solver, long)
     * @see SearchMonitorFactory#convertInMilliseconds(String)
     */
    public static void limitThreadTime(Solver solver, long limit) {
        ThreadTimeCounter counter = new ThreadTimeCounter(limit);
        counter.setAction(ActionCounterFactory.interruptSearch(solver.getSearchLoop()));
        solver.plugMonitor(counter);
    }

    /**
     * Defines a limit over the run time, set in a thread.
     * When the limit is reached, the resolution is stopped.
     *
     * @param duration a String which states the duration like "WWd XXh YYm ZZs".
     * @see SearchMonitorFactory#limitTime(org.chocosolver.solver.Solver, String)
     * @see SearchMonitorFactory#convertInMilliseconds(String)
     */
    public static void limitThreadTime(Solver solver, String duration) {
        limitThreadTime(solver, convertInMilliseconds(duration));
    }

    public static Pattern Dp = Pattern.compile("(\\d+)d");
    public static Pattern Hp = Pattern.compile("(\\d+)h");
    public static Pattern Mp = Pattern.compile("(\\d+)m");
    public static Pattern Sp = Pattern.compile("(\\d+(\\.\\d+)?)s");


    /**
     * Convert a string which represents a duration. It can be composed of days, hours, minutes and seconds.
     * Examples:
     * <p/>
     * - "1d2h3m4.5s": one day, two hours, three minutes, four seconds and 500 milliseconds<p/>
     * - "2h30m": two hours and 30 minutes<p/>
     * - "30.5s": 30 seconds and 500 ms<p/>
     * - "180s": three minutes
     *
     * @param duration a String which describes the duration
     * @return the duration in milliseconds
     */
    public static long convertInMilliseconds(String duration) {
        long milliseconds = 0;
        duration = duration.replaceAll("\\s+", "");
        Matcher matcher = Dp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int days = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
        }
        matcher = Hp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int hours = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
        }
        matcher = Mp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 1) {
            int minutes = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
        }
        matcher = Sp.matcher(duration);
        if (matcher.find() && matcher.groupCount() == 2) {
            double seconds = Double.parseDouble(matcher.group(1));
            milliseconds += (int) (seconds * 1000);
        }
        if (milliseconds == 0) throw new SolverException("Duration cannot be parsed or must be positive" + duration);
        return milliseconds;
    }

    /**
     * Defines a limit over the number of fails allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param limit maximal number of fails
     */

    public static void limitFail(Solver solver, long limit) {
        FailCounter counter = new FailCounter(limit);
        counter.setAction(ActionCounterFactory.interruptSearch(solver.getSearchLoop()));
        solver.plugMonitor(counter);
    }

    /**
     * Defines a limit over the number of backtracks allowed during the resolution.
     * WHen the limit is reached, the resolution is stopped.
     *
     * @param limit maximal number of backtracks
     */
    public static void limitBacktrack(Solver solver, long limit) {
        BacktrackCounter counter = new BacktrackCounter(limit);
        counter.setAction(ActionCounterFactory.interruptSearch(solver.getSearchLoop()));
        solver.plugMonitor(counter);
    }

    /**
     * Force the resolution to restart at root node after each solution.
     *
     * @param solver main solver
     */
    public static void restartAfterEachSolution(final Solver solver) {
        solver.plugMonitor((IMonitorSolution) () -> solver.getSearchLoop().restart());
    }
}
