/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.propagation;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.propagation.engines.queues.FixSizeCircularQueue;
import solver.requests.IEventRecorder;
import solver.requests.coarse.CoarseEventRecorder;
import solver.requests.fine.AbstractFineEventRecorder;
import solver.requests.fine.ArcEventRecorder;
import solver.requests.fine.ViewEventRecorderWrapper;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.view.View;

/**
 * A specific propagation engine that works like a queue (fifo).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public class QueuePropagationEngine extends PropagationEngine {

    protected ISchedulable lastPopped;

    protected FixSizeCircularQueue<ISchedulable> toPropagate;

    protected boolean init = false;

    @Override
    public boolean initialized() {
        return init;
    }

    @Override
    public void init(Solver solver) {
        if (!init) {
            Constraint[] constraints = solver.getCstrs();
            int count = 0;
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] propagators = constraints[c].propagators;
                for (int p = 0; p < propagators.length; p++) {
                    count++;
                    int nbV = propagators[p].getNbVars();
                    for (int v = 0; v < nbV; v++) {
                        Variable variable = propagators[p].getVar(v);
                        AbstractFineEventRecorder er = null;
                        if (variable instanceof View) {
                            View view = (View) variable;
                            er = new ViewEventRecorderWrapper(
                                    new ArcEventRecorder(view.getVariable(), propagators[p], v, solver),
                                    view.getModifier(),
                                    solver);
                        } else {
                            er = new ArcEventRecorder(variable, propagators[p], v, solver);
                        }
                        propagators[p].addRequest(er);
                        variable.addMonitor(er);
                        er.setScheduler(this);
                        count++;
                    }
                }
            }
            toPropagate = new FixSizeCircularQueue<ISchedulable>(count);
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] propagators = constraints[c].propagators;
                for (int p = 0; p < propagators.length; p++) {
                    CoarseEventRecorder cer = (CoarseEventRecorder) propagators[p].getRequest(-1);
                    cer.setScheduler(this);
                    cer.update(EventType.FULL_PROPAGATION);
                }
            }
            init = true;
        }
    }

    @Override
    public void schedule(ISchedulable element) {
        toPropagate.add(element);
        element.enqueue();
    }

    @Override
    public void remove(ISchedulable element) {
        element.deque();
        toPropagate.remove(element);
    }

    @Override
    public boolean iterateAndExecute() throws ContradictionException {
        while (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.pop();
            lastPopped.deque();
            lastPopped.execute();
        }
        return true;
    }

    @Override
    public void flush() {
        while (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.pop();
            if (IEventRecorder.LAZY) {
                lastPopped.flush();
            }
            lastPopped.deque();
        }
    }

    @Override
    public void deleteGroups() {
    }
}
