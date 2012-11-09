/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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
package solver.search.loop.monitors;

import solver.Solver;
import solver.search.limits.ILimit;
import solver.search.loop.AbstractSearchLoop;
import solver.search.restart.IRestartStrategy;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/05/11
 */
public enum SearchMonitorFactory {
    ;

    /**
     * Print statistics
     *
     * @param solver   solver to observe
     * @param solution print solutions
     * @param choices  print choices
     */
    public static void log(Solver solver, boolean solution, boolean choices) {
        AbstractSearchLoop sl = solver.getSearchLoop();
        sl.plugSearchMonitor(new LogBasic(solver));
        if (solution) {
            sl.plugSearchMonitor(new LogSolutions(sl));
        }
        if (choices) {
            sl.plugSearchMonitor(new LogChoices(solver));
        }
    }

    public static void logWithRank(Solver solver, int s, int e) {
        AbstractSearchLoop sl = solver.getSearchLoop();
        sl.plugSearchMonitor(new LogChoicesWithRank(solver, s, e));
    }

    /**
     * Print one-line statistics every XX ms
     *
     * @param solver
     * @param everyXXmms print one-line statistics every XX ms
     */
    public static void statEveryXXms(Solver solver, long everyXXmms) {
        if (everyXXmms > 0) {
            AbstractSearchLoop sl = solver.getSearchLoop();
            sl.plugSearchMonitor(new LogStatEveryXXms(solver.getSearchLoop(), everyXXmms));
        }
    }

    /**
     * Branch a restart strategy on the search
     *
     * @param solver               the solver
     * @param restartStrategy      the kind of restart strategy
     * @param restartStrategyLimit restart trigger
     * @param restartLimit         restart limits (limit of number of restarts)
     */
    public static void restart(Solver solver, IRestartStrategy restartStrategy, ILimit restartStrategyLimit, int restartLimit) {
        solver.getSearchLoop().plugSearchMonitor(new RestartManager(
                restartStrategy, restartStrategyLimit, solver.getSearchLoop(), restartLimit
        ));
    }

    public static void prop_count(Solver solver) {
        solver.getSearchLoop().plugSearchMonitor(new LogPropagationCount(solver));
    }

    public static void event_count(Solver solver) {
        solver.getSearchLoop().plugSearchMonitor(new LogEventCount(solver));
    }

    public static void limitNode(Solver solver, long limit) {
        solver.getSearchLoop().getLimitsBox().setNodeLimit(limit);
    }

    public static void limitSolution(Solver solver, long limit) {
        solver.getSearchLoop().getLimitsBox().setSolutionLimit(limit);
    }

    public static void limitTime(Solver solver, long limit) {
        solver.getSearchLoop().getLimitsBox().setTimeLimit(limit);
    }

    public static void limitThreadTime(Solver solver, long limit) {
        solver.getSearchLoop().getLimitsBox().setThreadTimeLimit(limit);
    }

    public static void limitFail(Solver solver, long limit) {
        solver.getSearchLoop().getLimitsBox().setFailLimit(limit);
    }

    public static void limitBacktrack(Solver solver, long limit) {
        solver.getSearchLoop().getLimitsBox().setBacktrackLimit(limit);
    }

    public static void toCSV(Solver solver, String prefix, String filename) {
        solver.getSearchLoop().plugSearchMonitor(new OutputCSV(solver, prefix, filename));
    }

}
