/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.trace;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorClose;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.StringUtils;

/**
 * A search monitor logger which prints statistics every XX ms.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 aug. 2010
 */
public class VerboseSolving implements IMonitorInitialize, IMonitorSolution, IMonitorClose {

    /**
     * A thread which prints short line statistics.
     */
    private final Thread printer;

    private final Solver solver;

    private final long[] counters = new long[3];
    /**
     * A boolean to kill the printer when the resolution ends.
     */
    private volatile boolean alive;

    private int calls = 0;
    /**
     * Create a monitor which outputs shot-line statistics every <i>duration</i> milliseconds
     *
     * @param solver the solver to instrument
     */
    public VerboseSolving(final Solver solver, final long duration) {
        this.solver = solver;
        printer = new Thread(() -> {
            alive = true;
            try {
                Thread.sleep(duration);
                do {
                    body(false);
                    //noinspection BusyWait
                    Thread.sleep(duration);
                } while (alive);
            } catch (InterruptedException ignored) {
            }
        });
        printer.setDaemon(true);
    }

    @Override
    public void afterInitialize(boolean correct) {
        if (correct) {
            if(!printer.isAlive())
                printer.start();
        }
    }

    @Override
    public void onSolution() {
        body(true);
    }

    @Override
    public void afterClose() {
        if (solver.isFeasible().equals(ESat.FALSE)) {
            alive = false;
            printer.interrupt();
        }
    }

    private void header() {
        boolean opt = solver.getObjectiveManager().isOptimization();
        solver.log().white().printf((opt ? "          Objective        |" : "")
                        + "              Measures              |     Progress    %n" +
                        (opt ? "     CurrentDomain BestBnd |" : "")
                        + " Depth Decisions WrongDecs Restarts | SolCount   Time |%n"
        );
    }

    private void body(boolean onSol) {
        if((calls % 20) == 0){
            header();
        }
        boolean opt = solver.getObjectiveManager().isOptimization();
        if (opt) {
            bodyOpt(onSol);
        } else {
            bodySat(onSol);
        }
        counters[0] = solver.getNodeCount();
        counters[1] = solver.getFailCount();
        counters[2] = solver.getRestartCount();
        calls++;
    }

    private void bodySat(boolean onSol) {
        solver.log().white().printf("%s %5d %9d %8.2f%% %8d | %8d %5.0fs |%s%n%s",
                onSol ? StringUtils.ANSI_BOLD + StringUtils.ANSI_BLACK : StringUtils.ANSI_WHITE,
                solver.getCurrentDepth(),
                solver.getNodeCount() - counters[0],
                (solver.getFailCount() - counters[1]) * 100f / (solver.getNodeCount() - counters[0]),
                solver.getRestartCount() - counters[2],
                solver.getSolutionCount(),
                solver.getTimeCount(),
                onSol ? "*" : "",
                StringUtils.ANSI_RESET
        );
    }

    private void bodyOpt(boolean onSol) {
        IntVar obj = solver.getObjectiveManager().getObjective().asIntVar();
        Number best = solver.getObjectiveManager().getBestSolutionValue();

        solver.log().white().printf("%s%8d %8d %8s | %5d %9d %8.2f%% %8d | %8d %5.0fs |%s%n%s",
                onSol ? StringUtils.ANSI_BOLD + StringUtils.ANSI_BLACK : StringUtils.ANSI_WHITE,
                obj.getLB(),
                obj.getUB(),
                solver.getSolutionCount() > 0 ? best : "--",
                solver.getCurrentDepth(),
                solver.getNodeCount() - counters[0],
                (solver.getFailCount() - counters[1]) * 100f / (solver.getNodeCount() - counters[0]),
                solver.getRestartCount() - counters[2],
                solver.getSolutionCount(),
                solver.getTimeCount(),
                onSol ? "*" : "",
                StringUtils.ANSI_RESET
        );
    }
}
