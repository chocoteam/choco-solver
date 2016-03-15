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

import org.chocosolver.solver.IMyself;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.*;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.StringUtils;

import java.io.PrintStream;

/**
 * This aims at simplifying resolution trace output by providing
 * a unique entry point for most (not to say all) resolution message.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 12/11/14
 */
public interface IOutputFactory extends IMyself {

    // http://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    /**
     * ANSI code for white
     */
    String ANSI_RESET = "\u001B[0m";
    /**
     * ANSI code for blue
     */
    String ANSI_BLUE = "\u001B[34m";
    /**
     * ANSI code for purple
     */
    String ANSI_PURPLE = "\u001B[35m";
    /**
     * ANSI code for green
     */
    String ANSI_GREEN = "\u001B[32m";
    /**
     * ANSI code for gray
     */
    String ANSI_GRAY = "\u001B[37m";

    /**
     * Set the current output stream (default is System.out)
     *
     * @param printStream a print stream
     */
    void setOut(PrintStream printStream);


    /**
     * @return the current output stream (default is System.out)
     */
    PrintStream getOut();

    /**
     * Set the current error stream (default is System.err)
     *
     * @param printStream a print stream
     */
    void setErr(PrintStream printStream);

    /**
     * @return the current error stream (default is System.err)
     */
    PrintStream getErr();

    
    /**
     * Print the version message.
     */
    default void printVersion() {
        getOut().println(_me().getModel().getSettings().getWelcomeMessage());
    }

    /**
     * Print (succint) features of the solver given in argument
     */
    default void printFeatures() {
        Attribute.printSuccint(_me());
    }

    /**
     * Print all features of the solver given in argument
     */
    default void printAllFeatures() {
        Attribute.printAll(_me());
    }

    /**
     * Print the resolution statistics.
     * <p>
     * Recommended usage: to be called after the resolution step.
     */
    default void printStatistics() {
        printVersion();
        printFeatures();
        getOut().println(_me().getMeasures().toString());
    }

    /**
     * Output the resolution statistics in a single line.
     * <p>
     * Recommended usage: to be called after the resolution step.
     */
    default void printShortStatistics() {
        getOut().println(_me().getMeasures().toOneLineString());
    }

    /**
     * Output the resolution statistics in a comma-separated single line.
     * The header is:
     * <pre>
     *     solutionCount;buildingTime(sec);initTime(sec);initPropag(sec);totalTime(sec);objective;nodes;backtracks;fails;restarts;fineProp;coarseProp;
     * </pre>
     */
    default void printCSVStatistics() {
        getOut().println(_me().getMeasures().toCSV());
    }

    /**
     * Plug a search monitor which calls {@link #printVersion()}
     * and {@link #printStatistics()} before closing the search.
     * <p>
     * Recommended usage: to be called before the resolution step.
     */
    default void showStatistics() {
        _me().plugMonitor(new IMonitorInitialize() {

            @Override
            public void beforeInitialize() {
                printVersion();
                printFeatures();
            }
        });
        _me().plugMonitor(new IMonitorClose() {
            @Override
            public void afterClose() {
                getOut().println(_me().getMeasures().toString());
            }
        });
    }

    /**
     * Plug a search monitor which calls {@link #printShortStatistics()} before closing the search.
     * <p>
     * Recommended usage: to be called before the resolution step.
     */
    default void showShortStatistics() {
        _me().plugMonitor(new IMonitorClose() {
            @Override
            public void beforeClose() {
                getOut().println(_me().getMeasures().toOneLineString());
            }
        });
    }

    /**
     * Plug a search monitor which outputs {@code message} on each solution.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @param message the message to print.
     */
    default void showSolutions(final IMessage message) {
        _me().plugMonitor((IMonitorSolution) () -> getOut().println(message.print()));
    }

    /**
     * Plug a search monitor which outputs a message on each solution.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @see IOutputFactory.DefaultSolutionMessage
     */
    default void showSolutions() {
        showSolutions(new DefaultSolutionMessage(_me()));
    }

    /**
     * Plug a search monitor which outputs {@code message} on each decision.
     * <p>
     * Recommended usage: to be called before the resolution step.
     * @param message the message to print.
     */
    default void showDecisions(final IMessage message) {
        _me().plugMonitor(new IMonitorDownBranch() {
            @Override
            public void beforeDownBranch(boolean left) {
                Decision head = _me().getDecisionPath().getLastDecision();
                getOut().printf("%s[%d/%d] %s%s ", StringUtils.pad("", _me().getEnvironment().getWorldIndex(), "."),
                        head.getArity() - head.triesLeft() +1, head.getArity(),
                        _me().getModel().getSettings().outputWithANSIColors()?ANSI_BLUE:"",
                        head.toString());
                getOut().printf("%s // %s %s\n", _me().getModel().getSettings().outputWithANSIColors()?ANSI_GRAY:"",
                        message.print(), _me().getModel().getSettings().outputWithANSIColors()?ANSI_RESET:"");
            }
        });
    }

    /**
     * Plug a search monitor which outputs a message on each decision.
     * <p>
     * Recommended usage: to be called before the resolution step.
     *
     * @see IOutputFactory.DefaultSolutionMessage
     */
    default void showDecisions() {
        showDecisions(new DefaultDecisionMessage(_me()));
    }

    /**
     * Plug a search monitor which outputs the contradictions thrown during the search.
     */
    default void showContradiction() {
        _me().plugMonitor((IMonitorContradiction) cex -> getOut().println(String.format("\t/!\\ %s", cex.toString())));
    }

    /**
     * Plug a search monitor which prints a one-line statistics every {@code f} ms.
     *
     * @param f      frequency, in millisecond
     */
    default void showStatisticsDuringResolution(long f) {
        if (f > 0) {
            _me().plugMonitor(new LogStatEveryXXms(_me(), f));
        }
    }


    //////////////

    /**
     * The default solution message format
     */
    class DefaultSolutionMessage implements IMessage {

        /**
         * Solver to output
         */
        private Solver solver;

        /**
         * Create a solution message
         * @param solver solver to output
         */
        public DefaultSolutionMessage(Solver solver) {
            this.solver = solver;
        }

        @Override
        public String print() {
            return String.format("%s- Solution #%s found. %s \n\t%s.%s",
                    solver.getModel().getSettings().outputWithANSIColors()?ANSI_GREEN:"",
                    solver.getSolutionCount(),
                    solver.getMeasures().toOneLineString(),
                    print(solver.getStrategy().getVariables()),
                    solver.getModel().getSettings().outputWithANSIColors()?ANSI_RESET:""
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
    class DefaultDecisionMessage implements IMessage {

        /**
         * Solver to output
         */
        private Solver solver;

        /**
         * Create a decision message
         * @param solver solver to output
         */
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
