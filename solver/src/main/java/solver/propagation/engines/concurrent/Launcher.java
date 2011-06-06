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
package solver.propagation.engines.concurrent;

import solver.exception.ContradictionException;
import solver.propagation.engines.ThreadedPropagationEngine;
import solver.requests.IRequest;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class Launcher extends Thread {
    int id;
    IRequest lastPoppedRequest;

    final ThreadedPropagationEngine master;

    volatile boolean runnable = true;

    public Launcher(ThreadedPropagationEngine master, int id) {
        super("Launcher " + id);
        this.master = master;
        this.id = id;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (runnable) {
            if (master.isPropagating) {
                int index = master.sequencer.getFreeRequestId();
                if (index > -1) {
//                    LoggerFactory.getLogger("solver").info("{} starts on {}", this.toString(), master.requests[index].toString());
                    assert master.nbWorkers >= 0;
                    master.nbWorkers++;
                    lastPoppedRequest = master.requests[index];
                    assert lastPoppedRequest.enqueued() : "request not enqued.. double access?";
                    lastPoppedRequest.deque();
                    try {
                        lastPoppedRequest.filter();
                    } catch (ContradictionException e) {
//                            LoggerFactory.getLogger("solver").info("{} fails on {}", this.toString(), master.requests[index].toString());
                        master.interrupt();
                    }
                    int to = lastPoppedRequest.getPropagator().getNbVars();
                    for (int i = 0; i < to; i++) {
                        master.sequencer.allow(lastPoppedRequest.getPropagator().getVar(i).getUniqueID());
                    }
                    master.nbWorkers--;
                    assert master.nbWorkers >= 0;
//                        LoggerFactory.getLogger("solver").info("{} forb: {}", this.toString(), Arrays.toString(master.sequencer.forbidden));
//                    LoggerFactory.getLogger("solver").info("{} ends on {}", this.toString(), master.requests[index].toString());
                }
            }
            synchronized (master) {
                master.notify();
            }
        }
    }

    @Override
    public String toString() {
        return "T" + id;
    }

    public void kill() {
        runnable = false;
    }
}
