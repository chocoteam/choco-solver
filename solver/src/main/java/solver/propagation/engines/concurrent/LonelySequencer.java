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

import org.slf4j.LoggerFactory;
import solver.exception.ContradictionException;
import solver.propagation.engines.ThreadedPropagationEngine;
import solver.requests.IRequest;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 10/06/11
 */
public class LonelySequencer extends AbstractSequencer {

    private boolean runnable = true;
    protected volatile int modcount = 0;
    protected Launcher[] launchers; // propagator a request -- in a thread
    protected final BitSet waiting;

    public LonelySequencer(ThreadedPropagationEngine master, int nbRequests, int nbVars, int nbThreads) {
        super(master, nbRequests, nbVars, nbThreads);
        waiting = new BitSet(nbThreads);
        this.launchers = new Launcher[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            launchers[i] = new Launcher(this, i);
            launchers[i].start();
            waiting.set(i, true);
        }
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        while (runnable) {
            IRequest request = null;
            switch (currentState) {
                case RUNNING:
                    synchronized (LonelySequencer.this) {
//                        System.out.println(Thread.currentThread() + ":" + Thread.holdsLock(this));
                        int idx = -1;
                        do {
                            idx = toPropagate.nextSetBit(idx + 1);
                        }
                        while (idx > -1 && !conditions(idx));
                        LoggerFactory.getLogger("solver").info("::" + modcount);
                        if (idx > -1) {
                            int tidx = waiting.nextSetBit(0);
                            if (tidx > -1) {
                                request = master.requests[idx];
                                assert request.enqueued() : idx + ":" + request + " not enqueued >> " + toPropagate.toString();
                                int to = request.getPropagator().getNbVars();
                                for (int i = 0; i < to; i++) {
                                    forbidden.set(request.getPropagator().getVar(i).getUniqueID(), true);
                                }
                                toPropagate.set(idx, false);
                                assert (waiting.get(tidx));
                                modcount++;
                                LoggerFactory.getLogger("solver").info("<<" + modcount);
                                waiting.set(tidx, false);
                                assert request.enqueued() : request + " not enqueued";
                                request.deque();
                                launchers[tidx].execute(request);
                            }
                        } else if (toPropagate.cardinality() == 0 && waiting.cardinality() == nbThreads) {
                            currentState = ISequencer.State.SLEEPING;
                        }
                    }
                    break;
                case SUSPENDING:
                    if (waiting.cardinality() == nbThreads) {
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
        }
    }

    protected void allow(IRequest request) {
        synchronized (LonelySequencer.this) {
            int to = request.getPropagator().getNbVars();
            for (int i = 0; i < to; i++) {
                forbidden.set(request.getPropagator().getVar(i).getUniqueID(), false);
            }
            int tidx = ((Launcher) Thread.currentThread()).getIndex();
            assert (!waiting.get(tidx));
            waiting.set(tidx, true);
            modcount--;
            LoggerFactory.getLogger("solver").info(">>" + modcount);
            assert (waiting.get(tidx));
        }
    }

    public void kill() {
        runnable = false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class Launcher extends Thread {

        int id;

        private boolean runnable = true;

        private IRequest request;

        private volatile boolean fed = false;

        private final LonelySequencer master;

        private Launcher(LonelySequencer master, int id) {
            super("Launcher " + id);
            this.master = master;
            this.id = id;
            setDaemon(true);
            setPriority(3);
        }

        @Override
        public void run() {
            while (runnable) {
                if (fed) {
                    LoggerFactory.getLogger("solver").info("{} in {}", this.toString(), request.toString());
                    try {
                        request.filter();
                    } catch (ContradictionException e) {
                        master.exception(e);
                    }
                    master.allow(request);
                    LoggerFactory.getLogger("solver").info("{} ends {}", this.toString(), request.toString());
                    request = null;
                    fed = false;
                }
            }
        }

        public void execute(IRequest request) {
//            LoggerFactory.getLogger("solver").info("{} starts {}", this.toString(), request.toString());
            this.request = request;
            fed = true;
        }

        public void kill() {
            runnable = false;
        }

        public int getIndex() {
            return id;
        }

        @Override
        public String toString() {
            return "T" + id;
        }
    }
}
