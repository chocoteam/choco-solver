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
package org.chocosolver.solver.trace;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.loop.monitors.*;
import org.chocosolver.solver.search.solution.ISolutionRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.Variable;

import static org.chocosolver.util.tools.StringUtils.pad;

/**
 * This is not a logging framework (Choco relies on SLF4J)
 * but aims at simplifying resolution trace output by providing
 * a unique entry point for most (not to say all) resolution message.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 12/11/14
 */
public class Chatterbox {
    private Chatterbox() {
    }

    /**
     * Print the version message.
     *
     * @param solver the solver
     */
    public static void printVersion(Solver solver) {
        System.out.println(solver.getSettings().getWelcomeMessage());
    }

    /**
     * Print (succint) features of the solver given in argument
     *
     * @param solver a solver
     */
    public static void printFeatures(Solver solver) {
        Attribute.printSuccint(solver);
    }

    /**
     * Print all features of the solver given in argument
     *
     * @param solver a solver
     */
    public static void printAllFeatures(Solver solver) {
        Attribute.printAll(solver);
    }

    /**
     * Print the resolution statistics.
     * <p>
     * Recommended usage: to be called after the resolution step.
     * <p>
     * Equivalent to:
     * <pre>
     *     System.out.println(solver.getMeasures().toString());
     * </pre>
     *
     * @param solver the solver to evaluate
     */
    public static void printStatistics(Solver solver) {
        System.out.println(solver.getMeasures().toString());
    }

    /**
     * Output the resolution statistics in a single line.
     * <p>
     * Recommended usage: to be called after the resolution step.
     * <p>
     * Equivalent to:
     * <pre>
     *     System.out.println(solver.getMeasures().toOneLineString());
     * </pre>
     *
     * @param solver the solver to evaluate
     */
    public static void printShortStatistics(Solver solver) {
        System.out.println(solver.getMeasures().toOneLineString());
    }

    /**
     * Output the resolution statistics in a comma-separated single line.
     * The header is:
     * <pre>
     *     solutionCount;buildingTime(sec);initTime(sec);initPropag(sec);totalTime(sec);objective;nodes;backtracks;fails;restarts;fineProp;coarseProp;
     * </pre>
     * <p>
     * Equivalent to:
     * <pre>
     *     System.out.println(solver.getMeasures().toCSV());
     * </pre>
     *
     * @param solver the solver to evaluate
     */
    public static void printCSVStatistics(Solver solver) {
        System.out.println(solver.getMeasures().toCSV());
    }


    /**
     * Print a posteriori the solutions found (beware, a solution recorder must has been defined).
     * <p>
     * Recommended usage: to be called after the resolution step.
     *
     * @param solver  the solver to print solutions from
     * @param message the message to print per solution
     */
    public static void printSolutions(Solver solver, IMessage message) {
        ISolutionRecorder solrec = solver.getSolutionRecorder();
        for (Solution sol : solrec.getSolutions()) {
            try {
                sol.restore();
                System.out.println(message.print());
            } catch (ContradictionException e) {
                throw new SolverException("Unable to restore a found solution");
            }
        }
    }

    /**
     * Print a posteriori the solutions found (beware, a solution recorder must has been defined).
     * <p>
     * Recommended usage: to be called after the resolution step.
     *
     * @param solver the solver to print solutions from
     * @see Chatterbox.DefaultSolutionMessage
     */
    public static void printSolutions(Solver solver) {
        printSolutions(solver, new DefaultSolutionMessage(solver));
    }

    /**
     * Plug a search monitor which calls {@link #printVersion(org.chocosolver.solver.Solver)}
     * and {@link #printStatistics(Solver)} before closing the search.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param solver the solver to evaluate
     */
    public static void showStatistics(final Solver solver) {
        solver.plugMonitor(new IMonitorInitialize() {

            @Override
            public void beforeInitialize() {
                printVersion(solver);
                printFeatures(solver);
            }

            @Override
            public void afterInitialize() {
            }
        });
        solver.plugMonitor(new IMonitorClose() {
            @Override
            public void beforeClose() {
                printStatistics(solver);
            }

            @Override
            public void afterClose() {

            }
        });
    }

    /**
     * Plug a search monitor which calls {@link #printShortStatistics(Solver)} before closing the search.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param solver the solver to evaluate
     */
    public static void showShortStatistics(final Solver solver) {
        solver.plugMonitor(new IMonitorClose() {
            @Override
            public void beforeClose() {
                System.out.println(solver.getMeasures().toOneShortLineString());
            }

            @Override
            public void afterClose() {
            }
        });
    }

    /**
     * Plug a search monitor which outputs <code>message</code> on each solution.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param solver  the solver to evaluate
     * @param message the message to print.
     */
    public static void showSolutions(Solver solver, final IMessage message) {
        solver.plugMonitor(new IMonitorSolution() {
            @Override
            public void onSolution() {
                System.out.println(message.print());
            }
        });
    }

    /**
     * Plug a search monitor which outputs a message on each solution.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param solver the solver to evaluate
     * @see Chatterbox.DefaultSolutionMessage
     */
    public static void showSolutions(Solver solver) {
        showSolutions(solver, new DefaultSolutionMessage(solver));
    }

    /**
     * Plug a search monitor which outputs <code>message</code> on each decision.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param solver  the solver to evaluate
     * @param message the message to print.
     */
    public static void showDecisions(final Solver solver, final IMessage message) {
        solver.plugMonitor(new IMonitorDownBranch() {
            @Override
            public void beforeDownLeftBranch() {
                System.out.println(String.format("%s[L]%s //%s",
                        pad("", solver.getEnvironment().getWorldIndex(), "."),
                        solver.getSearchLoop().getLastDecision().toString(),
                        message.print()));
            }

            @Override
            public void afterDownLeftBranch() {
            }

            @Override
            public void beforeDownRightBranch() {
                System.out.println(String.format("%s[R]%s //%s",
                        pad("", solver.getEnvironment().getWorldIndex(), "."),
                        solver.getSearchLoop().getLastDecision().toString(),
                        message.print()));
            }

            @Override
            public void afterDownRightBranch() {
            }
        });
    }

    /**
     * Plug a search monitor which outputs a message on each decision.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param solver the solver to evaluate
     * @see Chatterbox.DefaultSolutionMessage
     */
    public static void showDecisions(Solver solver) {
        showDecisions(solver, new DefaultDecisionMessage(solver));
    }

    /**
     * Plug a search monitor which outputs the contradictions thrown during the search.
     *
     * @param solver the solver to evaluate
     */
    public static void showContradiction(Solver solver) {
        solver.plugMonitor((IMonitorContradiction) new IMonitorContradiction() {
            @Override
            public void onContradiction(ContradictionException cex) {
                System.out.println(String.format("\t/!\\ %s", cex.toString()));
            }
        });
    }

    /**
     * Plug a search monitor which prints a one-line statistics every <code>f</code> ms.
     *
     * @param solver the solver to evaluate
     * @param f      frequency, in millisecond
     */
    public static void showStatisticsDuringResolution(Solver solver, long f) {
        if (f > 0) {
            solver.plugMonitor(new LogStatEveryXXms(solver, f));
        }
    }


    //////////////

    /**
     * The default solution message format
     */
    public static class DefaultSolutionMessage implements IMessage {

        private Solver solver;

        public DefaultSolutionMessage(Solver solver) {
            this.solver = solver;
        }

        @Override
        public String print() {
            return String.format("- Solution #%s found. %s \n\t%s.",
                    solver.getMeasures().getSolutionCount(),
                    solver.getMeasures().toOneShortLineString(),
                    print(solver.getStrategy().getVariables())
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

    /**
     * The default decision message format
     */
    public static class DefaultDecisionMessage implements IMessage {

        private Solver solver;

        public DefaultDecisionMessage(Solver solver) {
            this.solver = solver;
        }

        @Override
        public String print() {
            int limit = 120;
            Variable[] vars = solver.getStrategy().getVariables();
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
}
