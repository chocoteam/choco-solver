/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search.loop.monitors;

import com.github.cpprofiler.Connector;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.trace.IMessage;
import org.chocosolver.solver.variables.Variable;

import java.io.Closeable;
import java.io.IOException;

/**
 * A search monitor to send data to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
 * It enables to profile and to visualize Constraint Programming.
 * An installation is needed and is described <a href="https://github.com/cp-profiler/cp-profiler">here</a>.
 * This monitor relies on its <a href="https://github.com/cp-profiler/java-integration">java integration</a>.
 *
 * Note that CPProfiler is {@link Closeable} and can be used as follow:
 *
 * <pre> {@code
 * Model model = ProblemMaker.makeCostasArrays(7);
 *  try (CPProfiler profiler = new CPProfiler(model)) {
 *      while (model.getSolver().solve()) ;
 *      out.println(model.getSolver().getSolutionCount());
 * }
 * }</pre>
 *
 * <p>
 * Created by cprudhom on 22/10/2015.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 3.3.2
 */
public class CPProfiler implements IMonitorDownBranch, IMonitorUpBranch,
        IMonitorSolution, IMonitorContradiction, IMonitorRestart, Closeable {

    /**
     * Set to true to activate trace for debugging
     */
    public static boolean DEBUG = false;

    /**
     * Reference to the model
     */
    private Model mModel;
    /**
     *  Stacks of 'Parent Id'  used when backtrack
     */
    private TIntStack pid_stack = new TIntArrayStack();
    /**
     *  Stacks of 'Alternative' used when backtrack
     */
    private TIntStack alt_stack = new TIntArrayStack();
    /**
     * Stacks of current node, to deal with jumps
     */
    private TIntStack last_stack = new TIntArrayStack();
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
     * Used to communicate every node
     */
    private Connector connector = new Connector();

    /**
     * Is connection on
     */
    private boolean connected = false;

    /**
     * Format for solution output
     */
    private IMessage solutionMessage = new IMessage() {
        @Override
        public String print() {
            StringBuilder s = new StringBuilder(32);
            for (Variable v : mModel.getVars()) {
                s.append(v).append(' ');
            }
            return s.toString();
        }
    };

    /**
     * Active connection to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
     * This requires cp-profiler to be installed and launched before.
     * @param aModel model to observe resolution
     */
    public CPProfiler(Model aModel) {
        this.mModel = aModel;
        if (DEBUG) System.out.printf(
                "connector.restart(%d);\n",
                mModel.getSolver().getRestartCount());
        try {
            connector.connect(6565); // 6565 is the port used by cpprofiler by default
            connector.restart(aModel.getName(), 0); // starting a new tree (also used in case of a restart)
            mModel.getSolver().plugMonitor(this);
            connected = true;
            alt_stack.push(-1); // -1 is alt for the root node
            pid_stack.push(-1); // -1 is pid for the root node
            last_stack.push(-1);
        } catch (IOException e) {
            System.err.println("Unable to connect to CPProfiler, make sure it is started. No information will be sent.");
        }
    }


    /**
     * Close connection to <a href="https://github.com/cp-profiler/cp-profiler">cp-profiler</a>.
     */
    @Override
    public void close() throws IOException {
        if(connected){
            connector.disconnect();
            mModel.getSolver().unplugMonitor(this);
        }
        connected = false;
    }

    @Override
    public void beforeDownBranch(boolean left) {
        if (left) {
            DecisionPath dp = mModel.getSolver().getDecisionPath();
            int last = dp.size() - 1;
            Decision dec = dp.getLastDecision();
            String pdec = pretty(dp.getDecision(last-1));
            int ari = dec.getArity();
            send(nc, pid_stack.peek(), alt_stack.pop(), ari, rid, Connector.NodeStatus.BRANCH, pdec, "");
            for (int i = 0; i < ari; i++) {
                pid_stack.push(nc); // each child will have the same pid
            }
            nc++;
            alt_stack.push(dec.triesLeft() - 2);
            last_stack.push(nc-1);
        } else {
            nc++;
            alt_stack.push(1);
            last_stack.push(last);
        }
    }

    @Override
    public void beforeUpBranch() {
        last = last_stack.pop();
        while(pid_stack.peek() != last){
            pid_stack.pop();
        }
        pid_stack.pop();
    }

    @Override
    public void onSolution() {
        String dec = pretty(mModel.getSolver().getDecisionPath().getLastDecision());
        send(nc, pid_stack.peek(), alt_stack.pop(), 0, rid, Connector.NodeStatus.SOLVED, dec, solutionMessage.print());
    }

    @Override
    public void onContradiction(ContradictionException cex) {
        String dec = pretty(mModel.getSolver().getDecisionPath().getLastDecision());
        send(nc, pid_stack.peek(), alt_stack.pop(), 0, rid, Connector.NodeStatus.FAILED, dec, cex.toString());
    }

    @Override
    public void afterRestart() {
        if (DEBUG) System.out.printf(
                "connector.restart(%d);\n",
                mModel.getSolver().getRestartCount());
        if(connected) {
            try {
                connector.restart(++rid);
            } catch (IOException e) {
                System.err.println("Lost connection with CPProfiler. No more information will be sent.");
                connected = false;
            }
            pid_stack.clear();
            alt_stack.clear();
            alt_stack.push(-1); // -1 is alt for the root node
            pid_stack.push(-1); // -1 is pid for the root node
            last_stack.push(-1);
            nc = 0;
        }
    }

    private void send(int nc, int pid, int alt, int kid, int rid, Connector.NodeStatus status, String label, String info) {
        if (DEBUG) {
            System.out.printf(
                    "connector.sendNode(%d, %d, %d, 0, %s, %d, \"%s\", \"%s\");\n",
                    nc, pid, alt, status.toString(), rid, label, info);
        }
        if(connected) {
            try {
                connector.createNode(nc, pid, alt, kid, status)
                        .setRestartId(rid)
                        .setLabel(label)
                        .setInfo(info)
                        .send();
            } catch (IOException e) {
                System.err.println("Lost connection with CPProfiler. No more information will be sent.");
                connected = false;
            }
        }
    }

    private static String pretty(Decision dec) {
        if(dec == null){
            return "ROOT";
        }else {
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
