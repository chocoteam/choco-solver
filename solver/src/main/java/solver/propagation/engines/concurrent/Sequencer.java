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
 * A sequencer oriented from Thread to sequencer:
 * a thread (<code>Launcher</code>) ask to the sequencer the index of the next request to propagate.
 * <br/>
 * Getting the next ID locks the sequencer during the computation. The propagation is then executed in a thread.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class Sequencer extends AbstractSequencer {

    public int nbWorkers; // number of thread currently working on a request
    protected Launcher[] launchers; // propagator a request -- in a thread

    public Sequencer(ThreadedPropagationEngine master, int nbRequests, int nbVars, int nbThreads) {
        super(master, nbRequests, nbVars, nbThreads);
        this.nbWorkers = 0;
        this.launchers = new Launcher[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            launchers[i] = new Launcher(this, i);
            launchers[i].start();
        }
    }

    private synchronized IRequest getFreeRequestId() {
//        synchronized (this) {
        IRequest request = null;
        switch (currentState) {
            case RUNNING:
                int idx = -1;
                do {
                    idx = toPropagate.nextSetBit(idx + 1);
                }
                while (idx > -1 && !conditions(idx));
                if (idx > -1) {
//                    LoggerFactory.getLogger("solver").info("<{}", forbidden);
                    request = master.requests[idx];
                    assert request.enqueued() : idx + ":" + request + " not enqueued >> " + toPropagate.toString();
                    int to = request.getPropagator().getNbVars();
                    for (int i = 0; i < to; i++) {
                        forbidden.set(request.getPropagator().getVar(i).getUniqueID(), true);
                    }
                    toPropagate.set(idx, false);
                    nbWorkers++;
//                    LoggerFactory.getLogger("solver").info("{} get {}", this, request);
                    assert request.enqueued() : request + " not enqueued";
//                    LoggerFactory.getLogger("solver").info(">{}", forbidden);
                    request.deque();
                } else if (toPropagate.cardinality() == 0 && nbWorkers == 0) {
                    currentState = ISequencer.State.SLEEPING;
                }
            case SUSPENDING:
//                LoggerFactory.getLogger("solver").info("~{}", forbidden);
                if (nbWorkers == 0) {
                    assert forbidden.cardinality() == 0;
                    currentState = ISequencer.State.SLEEPING;
                }
                break;
            case SLEEPING:
            default:
                synchronized (master) {
                    master.notify();
                }
                break;
        }
        return request;
    }

    protected void allow(IRequest request) {
        synchronized (this) {
            int to = request.getPropagator().getNbVars();
            for (int i = 0; i < to; i++) {
                forbidden.set(request.getPropagator().getVar(i).getUniqueID(), false);
            }
            nbWorkers--;
        }
    }

    @Override
    public String toString() {
        return "W:" + nbWorkers + ", C:" + toPropagate.cardinality();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class Launcher extends Thread {
        int id;
        IRequest lastPoppedRequest;
        final Sequencer master;
        boolean runnable = true;

        public Launcher(Sequencer master, int id) {
            super("Launcher " + id);
            this.master = master;
            this.id = id;
            setDaemon(true);
            setPriority(3);
        }

        @Override
        public void run() {
            while (runnable) {
                lastPoppedRequest = master.getFreeRequestId();
                if (lastPoppedRequest != null) {
                    try {
                        lastPoppedRequest.filter();
                    } catch (ContradictionException e) {
                        master.exception(e);
                    }
                    master.allow(lastPoppedRequest);
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
}
