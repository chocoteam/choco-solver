/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.trace;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.monitors.IMonitorClose;
import org.chocosolver.solver.search.loop.monitors.IMonitorInitialize;

/**
 * A search monitor logger which prints statistics every XX ms.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 aug. 2010
 */
public class LogStatEveryXXms implements IMonitorInitialize, IMonitorClose {

    /**
     * A thread which prints short line statistics to {@link Solver#getOut()}.
     */
    private Thread printer;

    /**
     * A boolean to kill the printer when the resolution ends.
     */
    private volatile boolean alive;

    /**
     * Create a monitor which outputs shot-line statistics every <i>duration</i> milliseconds
     * @param solver the solver to instrument
     * @param duration delay between two outputs, in milliseconds
     */
    public LogStatEveryXXms(final Solver solver, final long duration) {

        printer = new Thread(() -> {
            alive = true;
            try {
                Thread.sleep(duration);
                //noinspection InfiniteLoopStatement
                do {
                    solver.getOut().println(String.format(">> %s", solver.toOneLineString()));
                    Thread.sleep(duration);
                } while (alive);
            } catch (InterruptedException ignored) {
            }
        });
        printer.setDaemon(true);
    }

    @Override
    public void afterInitialize(boolean correct) {
        if(correct){
            printer.start();
        }
    }

    @Override
    public void afterClose() {
        alive = false;
        printer.interrupt();
    }
}
