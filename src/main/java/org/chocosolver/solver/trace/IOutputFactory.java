/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.trace;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorClose;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.trace.frames.StatisticsPanel;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.StringUtils;

import java.io.PrintStream;

import javax.swing.*;

/**
 * This aims at simplifying resolution trace output by providing
 * a unique entry point for most (not to say all) resolution message.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 12/11/14
 */
public interface IOutputFactory extends ISelf<Solver> {
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
        _me().getMeasures().setReadingTimeCount(System.nanoTime() - _me().getModel().getCreationTime());
        _me().getOut().printf("- Model[%s] features:\n", _me().getModel().getName());
        _me().getOut().printf("\tVariables : %d\n", _me().getModel().getNbVars());
        _me().getOut().printf("\tConstraints : %d\n", _me().getModel().getNbCstrs());
        _me().getOut().printf("\tBuilding time : %.3fs\n", _me().getMeasures().getReadingTimeCount());
        _me().getOut().printf("\tUser-defined search strategy : %s\n", _me().getModel().getSolver().isDefaultSearchUsed() ? "yes" : "no");
        _me().getOut().printf("\tComplementary search strategy : %s\n", _me().isSearchCompleted() ? "yes" : "no");
    }

    /**
     * Print (succint) features of the solver given in argument in a single line.
     */
    default void printShortFeatures() {
        _me().getMeasures().setReadingTimeCount(System.nanoTime() - _me().getModel().getCreationTime());
        StringBuilder st = new StringBuilder(256);
        st.append("Model[").append(_me().getModelName()).append("], ");
        st.append(String.format("%d variables, %d constraints, building time: %.3fs, %s user-defined search strategy, %s complementary search strategy",
                _me().getModel().getNbVars(),
                _me().getModel().getNbCstrs(),
                (System.nanoTime() - _me().getModel().getCreationTime())  / IMeasures.IN_SEC,
                _me().getModel().getSolver().isDefaultSearchUsed() ? "w/" : "w/o",
                _me().isSearchCompleted() ? "w/" : "w/o"));
        getOut().println(st.toString());
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
                printShortStatistics();
            }
        });
    }


    /**
     * Calls {@link #printShortStatistics()} before the program ends (normally or not)?
     * This adds a shutdown hook.
     * <p>
     * Recommended usage: to be called before the resolution step.
     */
    default void showShortStatisticsOnShutdown(){
        Runtime.getRuntime().addShutdownHook(new Thread(()->_me().printShortStatistics()));
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
                getOut().printf("%s %s ", StringUtils.pad("", _me().getEnvironment().getWorldIndex(), "."),
                        _me().getDecisionPath().lastDecisionToString());
                getOut().printf(" // %s \n", message.print());
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


    /**
     * Create and show a simple dashboard that render resolution statistics every 100 milliseconds.
     */
    default void showDashboard(){
        this.showDashboard(100L);
    }

    /**
     * Create and show a simple dashboard that render resolution statistics every 'refresh' milliseconds.
     * Note that a low refresh rate will slow down the entire process.

     * @param refresh frequency rate, in milliseconds.
     */
    default void showDashboard(long refresh){
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


        //Create and set up the content pane.
        JComponent newContentPane = new StatisticsPanel(_me(), refresh);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
//        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
            return String.format("- Solution #%s found. %s \n\t%s.",
                    solver.getSolutionCount(),
                    solver.getMeasures().toOneLineString(),
                    print(solver.getSearch().getVariables())
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
            Variable[] vars = solver.getSearch().getVariables();
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
