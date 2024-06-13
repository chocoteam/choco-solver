/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.trace;

import cpp.Connector;
import cpp.Message;
import org.chocosolver.solver.Solver;

import java.io.Closeable;
import java.io.IOException;

/**
 * A search monitor to send data to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
 * It enables to profile and to visualize Constraint Programming. An installation is needed and is
 * described <a href="https://github.com/cp-profiler/cp-profiler">here</a>. This monitor relies on
 * its <a href="https://github.com/cp-profiler/java-integration">java integration</a>. <p> Note that
 * CPProfiler is {@link Closeable} and can be used as follow: <p>
 * <pre> {@code
 * Model model = ProblemMaker.makeCostasArrays(7);
 *  try (CPProfiler profiler = new CPProfiler(model)) {
 *      while (model.getSolver().solve()) ;
 *      out.println(model.getSolver().getSolutionCount());
 * }
 * }</pre>
 * <p> <p> Created by cprudhom on 22/10/2015. Project: choco.
 *
 * @author Charles Prud'homme
 * @since 3.3.2
 */
public class CPProfiler extends SearchViz {

    /**
     * Used to communicate every node
     */
    private Connector connector;

    /**
     * Active connection to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
     * This requires cp-profiler to be installed and launched before.
     *
     * @param aSolver solver to observe resolution
     */
    public CPProfiler(Solver aSolver) {
        this(aSolver, -1, 6565, false);
    }

    /**
     * Active connection to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
     * This requires cp-profiler to be installed and launched before.
     *
     * @param aSolver    solver to observe resolution
     * @param sendDomain set to <i>true</i> to send domain into 'info' field (beware, it can
     *                   increase the memory consumption and slow down the overall execution), set
     *                   to <i>false</i> otherwise.
     */
    public CPProfiler(Solver aSolver, int executionId, int port, boolean sendDomain) {
        super(aSolver, sendDomain);
        connect(aSolver.getModelName(), executionId, port);
    }

    protected boolean connect(String label, int executionId, int port) {
        if (connector == null) {
            connector = new Connector(port); // 6565 is the port used by cpprofiler by default
        }
        try {
            connector.connect();
            connector.start(label, executionId, true); // starting a new tree (also used in case of a restart)
        } catch (IOException e) {
            System.err.println("Unable to connect to CPProfiler, make sure it is started. No information will be sent.");
            return false;
        }
        return true;
    }

    @Override
    protected void disconnect() {
        if (connected) {
            try {
                connector.done();
                connector.disconnect();
            } catch (IOException e) {
                System.err.println("Unable to disconnect CPProfiler.");
            }
        }
    }

    @Override
    protected void sendNode(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        send(nc, pid, alt, kid, rid, Message.NodeStatus.BRANCH, label, info);
    }

    @Override
    protected void sendSolution(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        send(nc, pid, alt, kid, rid, Message.NodeStatus.SOLVED, label, info);
    }

    @Override
    protected void sendFailure(int nc, int pid, int alt, int kid, int rid, String label, String info) {
        send(nc, pid, alt, kid, rid, Message.NodeStatus.FAILED, label, info);
    }

    @Override
    protected void sendRestart(int rid) {
        try {
            connector.restart(rid);
        } catch (IOException e) {
            System.err.println("Lost connection with CPProfiler. No more information will be sent.");
            connected = false;
        }
    }

    private void send(int nc, int pid, int alt, int kid, int rid, Message.NodeStatus status, String label, String info) {
        try {
            connector.createNode(nc, pid, rid, alt, kid, status)
                    .setInfo(info)
                    .setLabel(label)
                    .send();
        } catch (IOException e) {
            System.err.println("Lost connection with CPProfiler. No more information will be sent.");
            connected = false;
        }
    }
}
