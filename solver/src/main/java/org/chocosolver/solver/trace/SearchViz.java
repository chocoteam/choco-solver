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

import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.search.loop.monitors.IMonitorRestart;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.IMonitorUpBranch;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.iterators.DisposableRangeIterator;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by cprudhom on 22/10/2015.
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 3.3.2
 */
public abstract class SearchViz implements IMonitorDownBranch, IMonitorUpBranch,
        IMonitorSolution, IMonitorContradiction, IMonitorRestart, Closeable {

    /**
     * Reference to the model
     */
    protected Solver mSolver;
    /**
     * Stacks of 'Parent Id'  used when backtrack
     */
    private final TIntStack pid_stack = new TIntArrayStack();
    /**
     * Stacks of 'Alternative' used when backtrack
     */
    private final TIntStack alt_stack = new TIntArrayStack();
    /**
     * Stacks of current node, to deal with jumps
     */
    private final TIntStack last_stack = new TIntArrayStack();
    /**
     * Node count: different from measures.getNodeCount() as we count failure nodes as well
     */
    private int nc = 0;
    /**
     * restart id
     */
    private int rid;
    /**
     * last node index sent
     */
    private int last;
    /**
     * Is connection alive
     */
    protected boolean connected;

    /**
     * set to <i>true</i> to send domain into 'info' field
     */
    private final boolean sendDomain;

    /**
     * Format for solution output
     */
    private final IMessage solutionMessage = new IMessage() {
        @Override
        public String print() {
            StringBuilder s = new StringBuilder(32);
            for (Variable v : mSolver.getModel().getVars()) {
                s.append(v).append(' ');
            }
            return s.toString();
        }
    };

    /**
     * Format for domain output
     * "{ "domains": {"VarA": "1..10, 12, 14..19", "VarB": "4"} }"
     */
    private final IMessage domainMessage = new IMessage() {
        @Override
        public String print() {
            StringBuilder s = new StringBuilder(32);
            s.append("{\"domains\":{");
            for (Variable v : mSolver.getModel().getVars()) {
                if ((v.getTypeAndKind() & Variable.INT) > 0) {
                    s.append("\"").append(v.getName()).append("\":\"");
                    IntVar iv = (IntVar) v;
                    DisposableRangeIterator rit = iv.getRangeIterator(true);
                    while (rit.hasNext()) {
                        int from = rit.min();
                        int to = rit.max();
                        s.append(from);
                        if(from < to){
                            s.append("..").append(to);
                        }
                        s.append(',');
                        rit.next();
                    }
                    rit.dispose();
                }
                s.setLength(s.length() - 1);
                s.append("\",");
            }
            s.setLength(s.length() - 1);
            s.append("}}");
            return s.toString();
        }
    };

    /**
     * Active connection to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
     * This requires cp-profiler to be installed and launched before.
     *
     * @param aSolver     solver to observe resolution
     * @param sendDomain set to <i>true</i> to send domain into 'info' field (beware, it can increase the memory consumption
     *                   and slow down the overall execution), set to <i>false</i> otherwise.
     */
    public SearchViz(Solver aSolver, boolean sendDomain) {
        this.mSolver = aSolver;
        this.sendDomain = sendDomain;
        if(connected = connect(mSolver.getModel().getName())) {
            mSolver.plugMonitor(this);
        }
        alt_stack.push(-1); // -1 is alt for the root node
        pid_stack.push(-1); // -1 is pid for the root node
        last_stack.push(-1);
    }

    protected abstract boolean connect(String label);

    protected abstract void disconnect();

    protected abstract void sendNode(int nc, int pid, int alt, int kid, int rid, String label, String info);

    protected abstract void sendSolution(int nc, int pid, int alt, int kid, int rid, String label, String info);

    protected abstract void sendFailure(int nc, int pid, int alt, int kid, int rid, String label, String info);

    protected abstract void sendRestart(int rid);

    /**
     * Close connection to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
     */
    @Override
    public final void close() throws IOException {
        disconnect();
        mSolver.unplugMonitor(this);
        connected = false;
    }

    @Override
    public final void beforeDownBranch(boolean left) {
        if (left) {
            DecisionPath dp = mSolver.getDecisionPath();
            int last = dp.size() - 1;
            if (last > 0) {
                String pdec;
                pdec = pretty(dp.getDecision(last - 1));
                Decision dec = dp.getLastDecision();
                int ari = dec.getArity();
                sendNode(nc, pid_stack.peek(), alt_stack.pop(), ari, rid, pdec,
                        sendDomain? domainMessage.print():"");
                for (int i = 0; i < ari; i++) {
                    pid_stack.push(nc); // each child will have the same pid
                }
                nc++;
                alt_stack.push(0);
                last_stack.push(nc - 1);
            }
        } else {
            nc++;
            alt_stack.push(1);
            last_stack.push(last);
        }
    }

    @Override
    public final void beforeUpBranch() {
        last = last_stack.pop();
        while (pid_stack.peek() != last) {
            pid_stack.pop();
        }
        pid_stack.pop();
    }

    @Override
    public final void onSolution() {
        String dec = pretty(mSolver.getDecisionPath().getLastDecision());
        sendSolution(nc, pid_stack.peek(), alt_stack.pop(), 0, rid, dec, solutionMessage.print());
    }

    @Override
    public final void onContradiction(ContradictionException cex) {
        String dec = pretty(mSolver.getDecisionPath().getLastDecision());
        sendFailure(nc, pid_stack.peek(), alt_stack.pop(), 0, rid, dec, cex.toString());
    }

    @Override
    public final void afterRestart() {
        sendRestart(++rid);
        pid_stack.clear();
        alt_stack.clear();
        alt_stack.push(-1); // -1 is alt for the root node
        pid_stack.push(-1); // -1 is pid for the root node
        last_stack.push(-1);
        nc = 0;
    }

    private static String pretty(Decision dec) {
        if (dec == null) {
            return "ROOT";
        } else {
            // to print decision correctly (since the previous one is sent)
            int a = dec.getArity();
            int b = dec.triesLeft();
            dec.rewind();
            while (dec.triesLeft() > b + 1) {
                a--;
                dec.buildNext();
            }
            String pretty = dec.toString();
            while (a > b) {
                b++;
                dec.buildNext();
            }
            return pretty;
        }
    }
}
