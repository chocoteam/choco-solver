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
package org.chocosolver.solver.trace;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.loop.monitors.*;
import org.chocosolver.solver.search.solution.ISolutionRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;

import java.io.PrintStream;

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

    // http://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_GRAY = "\u001B[37m";

    private Chatterbox() {
    }

    /**
     * The standard output stream (default: System.out)
     */
    public static PrintStream out = System.out;

    /**
     * The standard error stream (default: System.err)
     */
    public static PrintStream err = System.err;

    /**
     * Set the current output stream (default is System.out)
     *
     * @param printStream a print stream
     */
    public static void setOut(PrintStream printStream) {
        out = printStream;
    }

    /**
     * Set the current error stream (default is System.err)
     *
     * @param printStream a print stream
     */
    public static void setErr(PrintStream printStream) {
        err = printStream;
    }


    /**
     * Print the version message.
     *
     * @param model the solver
     */
    public static void printVersion(Model model) {
        out.println(model.getSettings().getWelcomeMessage());
    }

    /**
     * Print (succint) features of the solver given in argument
     *
     * @param model a solver
     */
    public static void printFeatures(Model model) {
        Attribute.printSuccint(model);
    }

    /**
     * Print all features of the solver given in argument
     *
     * @param model a solver
     */
    public static void printAllFeatures(Model model) {
        Attribute.printAll(model);
    }

    /**
     * Print the resolution statistics.
     * <p>
     * Recommended usage: to be called after the resolution step.
     * <p>
     * Equivalent to:
     * <pre>
     *     out.println(solver.getMeasures().toString());
     * </pre>
     *
     * @param model the solver to evaluate
     */
    public static void printStatistics(Model model) {
        printVersion(model);
        printFeatures(model);
        out.println(model.getMeasures().toString());
    }

    /**
     * Output the resolution statistics in a single line.
     * <p>
     * Recommended usage: to be called after the resolution step.
     * <p>
     * Equivalent to:
     * <pre>
     *     out.println(solver.getMeasures().toOneLineString());
     * </pre>
     *
     * @param model the solver to evaluate
     */
    public static void printShortStatistics(Model model) {
        out.println(model.getMeasures().toOneLineString());
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
     *     out.println(solver.getMeasures().toCSV());
     * </pre>
     *
     * @param model the solver to evaluate
     */
    public static void printCSVStatistics(Model model) {
        out.println(model.getMeasures().toCSV());
    }


    /**
     * Print a posteriori the solutions found (beware, a solution recorder must has been defined).
     * <p>
     * Recommended usage: to be called after the resolution step.
     *
     * @param model  the solver to print solutions from
     * @param message the message to print per solution
     */
    public static void printSolutions(Model model, IMessage message) {
        ISolutionRecorder solrec = model.getSolutionRecorder();
        for (Solution sol : solrec.getSolutions()) {
            try {
                model.restoreSolution(sol);
                out.println(message.print());
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
     * @param model the solver to print solutions from
     * @see Chatterbox.DefaultSolutionMessage
     */
    public static void printSolutions(Model model) {
        printSolutions(model, new DefaultSolutionMessage(model));
    }

    /**
     * Plug a search monitor which calls {@link #printVersion(Model)}
     * and {@link #printStatistics(Model)} before closing the search.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param model the solver to evaluate
     */
    public static void showStatistics(final Model model) {
        model.plugMonitor(new IMonitorInitialize() {

            @Override
            public void beforeInitialize() {
                printVersion(model);
                printFeatures(model);
            }
        });
        model.plugMonitor(new IMonitorClose() {
            @Override
            public void afterClose() {
                out.println(model.getMeasures().toString());
            }
        });
    }

    /**
     * Plug a search monitor which calls {@link #printShortStatistics(Model)} before closing the search.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param model the solver to evaluate
     */
    public static void showShortStatistics(final Model model) {
        model.plugMonitor(new IMonitorClose() {
            @Override
            public void beforeClose() {
                out.println(model.getMeasures().toOneShortLineString());
            }
        });
    }

    /**
     * Plug a search monitor which outputs <code>message</code> on each solution.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param model  the solver to evaluate
     * @param message the message to print.
     */
    public static void showSolutions(Model model, final IMessage message) {
        model.plugMonitor((IMonitorSolution) () -> out.println(message.print()));
    }

    /**
     * Plug a search monitor which outputs a message on each solution.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param model the solver to evaluate
     * @see Chatterbox.DefaultSolutionMessage
     */
    public static void showSolutions(Model model) {
        showSolutions(model, new DefaultSolutionMessage(model));
    }

    /**
     * Plug a search monitor which outputs <code>message</code> on each decision.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param model  the solver to evaluate
     * @param message the message to print.
     */
    public static void showDecisions(final Model model, final IMessage message) {
        model.plugMonitor(new IMonitorDownBranch() {
            @Override
            public void beforeDownBranch(boolean left) {
                Decision d = model.getResolver().getLastDecision();
                out.printf("%s[%d/%d] %s%s ", pad("", model.getEnvironment().getWorldIndex(), "."),
                        d.getArity() - d.triesLeft() +1, d.getArity(),
                        model.getSettings().outputWithANSIColors()?ANSI_BLUE:"",
                        d.toString());
                out.printf("%s // %s %s\n", model.getSettings().outputWithANSIColors()?ANSI_GRAY:"",
                        message.print(), model.getSettings().outputWithANSIColors()?ANSI_RESET:"");
            }
        });
    }

    /**
     * Plug a search monitor which outputs a message on each decision.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param model the solver to evaluate
     * @see Chatterbox.DefaultSolutionMessage
     */
    public static void showDecisions(Model model) {
        showDecisions(model, new DefaultDecisionMessage(model));
    }

    /**
     * Plug a search monitor which outputs the contradictions thrown during the search.
     *
     * @param model the solver to evaluate
     */
    public static void showContradiction(Model model) {
        model.plugMonitor((IMonitorContradiction) cex -> out.println(String.format("\t/!\\ %s", cex.toString())));
    }

    /**
     * Plug a search monitor which prints a one-line statistics every <code>f</code> ms.
     *
     * @param model the solver to evaluate
     * @param f      frequency, in millisecond
     */
    public static void showStatisticsDuringResolution(Model model, long f) {
        if (f > 0) {
            model.plugMonitor(new LogStatEveryXXms(model, f));
        }
    }


    //////////////

    /**
     * The default solution message format
     */
    public static class DefaultSolutionMessage implements IMessage {

        private Model model;

        public DefaultSolutionMessage(Model model) {
            this.model = model;
        }

        @Override
        public String print() {
            return String.format("%s- Solution #%s found. %s \n\t%s.%s",
                    model.getSettings().outputWithANSIColors()?ANSI_GREEN:"",
                    model.getMeasures().getSolutionCount(),
                    model.getMeasures().toOneShortLineString(),
                    print(model.getStrategy().getVariables()),
                    model.getSettings().outputWithANSIColors()?ANSI_RESET:""
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

        private Model model;

        public DefaultDecisionMessage(Model model) {
            this.model = model;
        }

        @Override
        public String print() {
            int limit = 120;
            Variable[] vars = model.getStrategy().getVariables();
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
