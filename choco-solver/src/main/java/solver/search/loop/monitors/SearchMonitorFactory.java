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
import solver.search.limits.*;
import solver.search.restart.GeometricalRestartStrategy;
import solver.search.restart.LubyRestartStrategy;
import solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/05/11
 */
public class SearchMonitorFactory {
    SearchMonitorFactory() {
    }

    private static class DefaultSolutionMessage implements IMessage {

        private Solver solver;

        private DefaultSolutionMessage(Solver solver) {
            this.solver = solver;
        }

        @Override
        public String print() {
            return String.format("- Solution #%s found. %s \n\t%s.",
                    solver.getMeasures().getSolutionCount(),
                    solver.getMeasures().toOneShortLineString(),
                    print(solver.getStrategy().vars)
            );
        }

        private String print(Variable[] vars) {
            StringBuilder s = new StringBuilder(32);
            for (Variable v : vars) {
                s.append(v).append(' ');
            }
            return s.toString();

        }

    }

    private static class DefaultDecisionMessage implements IMessage {

        private Solver solver;

        private DefaultDecisionMessage(Solver solver) {
            this.solver = solver;
        }

        @Override
        public String print() {
            int limit = 120;
            Variable[] vars = solver.getStrategy().vars;
            StringBuilder s = new StringBuilder(32);
            for (int i = 0; i < vars.length && s.length() < limit; i++) {
                s.append(vars[i]).append(' ');
            }
            if (s.length() >= limit) {
                s.append("...");
            }
            return s.toString();
        }

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Print statistics
     *
     * @param solver   solver to observe
     * @param solution print solutions
     * @param choices  print choices
     */
    public static void log(Solver solver, boolean solution, boolean choices) {
        solver.plugMonitor(new LogBasic(solver));
        if (solution) {
			solver.plugMonitor(new LogSolutions(new DefaultSolutionMessage(solver)));
        }
        if (choices) {
			solver.plugMonitor(new LogChoices(solver, new DefaultDecisionMessage(solver)));
        }
    }

    /**
     * Print statistics
     *
     * @param solver          solver to observe
     * @param solution        print solutions
     * @param solutionMessage print the message on solutions
     * @param choices         print choices
     */
    public static void log(Solver solver, boolean solution, IMessage solutionMessage, boolean choices) {
		solver.plugMonitor(new LogBasic(solver));
        if (solution) {
			solver.plugMonitor(new LogSolutions(solutionMessage));
        }
        if (choices) {
			solver.plugMonitor(new LogChoices(solver, new DefaultDecisionMessage(solver)));
        }
    }

    /**
     * Print statistics
     *
     * @param solver          solver to observe
     * @param solution        print solutions
     * @param choices         print choices
     * @param decisionMessage print the message on decisions
     */
    public static void log(Solver solver, boolean solution, boolean choices, IMessage decisionMessage) {
		solver.plugMonitor(new LogBasic(solver));
        if (solution) {
			solver.plugMonitor(new LogSolutions(new DefaultSolutionMessage(solver)));
        }
        if (choices) {
			solver.plugMonitor(new LogChoices(solver, decisionMessage));
        }
    }

    /**
     * Print statistics
     *
     * @param solver          solver to observe
     * @param solution        print solutions
     * @param solutionMessage print the message on solutions
     * @param choices         print choices
     * @param decisionMessage print the message on decisions
     */
    public static void log(Solver solver, boolean solution, IMessage solutionMessage, boolean choices, IMessage decisionMessage) {
		solver.plugMonitor(new LogBasic(solver));
        if (solution) {
			solver.plugMonitor(new LogSolutions(solutionMessage));
        }
        if (choices) {
			solver.plugMonitor(new LogChoices(solver, decisionMessage));
        }
    }

    /**
     * Log execution during choices #s and #e
     *
     * @param solver a solver
     * @param s      starting choice number
     * @param e      ending choice number
     */
    public static void logWithRank(Solver solver, int s, int e) {
		solver.plugMonitor(new LogChoicesWithRank(solver, s, e, new DefaultDecisionMessage(solver)));
    }

    /**
     * Log execution during choices #s and #e
     *
     * @param solver          a solver
     * @param s               starting choice number
     * @param e               ending choice number
     * @param decisionMessage print the specific message
     */
    public static void logWithRank(Solver solver, int s, int e, IMessage decisionMessage) {
		solver.plugMonitor(new LogChoicesWithRank(solver, s, e, decisionMessage));
    }

    /**
     * Log contradictions thrown during the resolution
     *
     * @param solver a solver
     */
    public static void logContradiction(Solver solver) {
		solver.plugMonitor(new LogContradiction());
    }

    /**
     * Print one-line statistics every XX ms
     *
     * @param solver
     * @param everyXXmms print one-line statistics every XX ms
     */
    public static void statEveryXXms(Solver solver, long everyXXmms) {
        if (everyXXmms > 0) {
			solver.plugMonitor(new LogStatEveryXXms(solver, everyXXmms));
        }
    }

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
     * Print the total number of propagation count per propagator
     *
     * @param solver a solver
     */
    public static void prop_count(Solver solver) {
		solver.plugMonitor(new LogPropagationCount(solver));
    }

    /**
     * Print the total number of events per variable
     *
     * @param solver
     */
    public static void event_count(Solver solver) {
		solver.plugMonitor(new LogEventCount(solver));
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
     * @param limit maximal resolution time in millisecond
     * @see SearchMonitorFactory#limitThreadTime(solver.Solver, long)
     */
    public static void limitTime(Solver solver, long limit) {
        TimeCounter counter = new TimeCounter(solver, limit);
        counter.setAction(ActionCounterFactory.interruptSearch(solver.getSearchLoop()));
		solver.plugMonitor(counter);
    }


    /**
     * Defines a limit over the run time, set in a thread.
     * When the limit is reached, the resolution is stopped.
     *
     * @param limit maximal resolution time in millisecond
     */
    public static void limitThreadTime(Solver solver, long limit) {
        ThreadTimeCounter counter = new ThreadTimeCounter(solver, limit);
        counter.setAction(ActionCounterFactory.interruptSearch(solver.getSearchLoop()));
		solver.plugMonitor(counter);
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
     * Output results to a CSV file (append in set to true).
     *
     * @param solver   a solver
     * @param prefix   String identifying the instance that has been solved
     * @param filename absolute path of the CSV output file
     */
    public static void toCSV(Solver solver, String prefix, String filename) {
		solver.plugMonitor(new OutputCSV(solver, prefix, filename));
    }

}
