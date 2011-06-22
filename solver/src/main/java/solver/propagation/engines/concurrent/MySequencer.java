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

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 21/06/11
 */
public class MySequencer extends AbstractSequencer {

    public volatile int nbWorkers; // number of thread currently working on a request
    private ArrayBlockingQueue<IRequest> _IN, _OUT;
    protected Thread[] launchers; // propagator a request -- in a thread

    public MySequencer(ThreadedPropagationEngine master, int nbRequests, int nbVars, int nbThreads) {
        super(master, nbRequests, nbVars, nbThreads);
        _IN = new ArrayBlockingQueue<IRequest>(16);
        _OUT = new ArrayBlockingQueue<IRequest>(16);
        this.nbWorkers = 0;
        this.launchers = new Thread[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            launchers[i] = new Thread(new Launcher(this), "Launcher_" + i);
            launchers[i].setDaemon(true);
            launchers[i].start();
        }
        this.setDaemon(true);
        this.start();
    }

    public void fixpoint() {
        synchronized (this) {
            assert nbWorkers == 0 : "workers";
            assert _IN.size() == 0 : "IN";
            assert _OUT.size() == 0 : "OUT";
            assert forbidden.cardinality() == 0 : "forbidden";
            fail = false;
            currentState = ISequencer.State.RUNNING;
            this.notify();
        }
    }

    public void run() {
        for (; ; ) {
            IRequest request = null;
            switch (currentState) {
                case RUNNING:
                    // treat propagated requests
                    checkOut();
                    // then look after the next request to propagate
                    synchronized (toPropagate) {
                        int idx = -1;
                        do {
                            idx = toPropagate.nextSetBit(idx + 1);
                        }
                        while (idx > -1 && !conditions(idx));
                        if (idx > -1) {
                            request = master.requests[idx];
                            int to = request.getPropagator().getNbVars();
                            for (int i = 0; i < to; i++) {
                                forbidden.set(request.getPropagator().getVar(i).getUniqueID(), true);
                            }
                            toPropagate.set(idx, false);
                            request.deque();
                            synchronized (_IN) {
                                _IN.add(request);
                            }
                        }
                    }
                    synchronized (toPropagate) {
                        if (toPropagate.cardinality() == 0 && goToSleep()) {
                            currentState = ISequencer.State.SUSPENDING;
                        }
                    }
                    break;
                case SUSPENDING:
                    // on fail, wait for active thread
                    checkOut();
                    if (goToSleep()) {
                        currentState = ISequencer.State.SLEEPING;
                    }
                    break;
                case SLEEPING:
                default:
                    assert goToSleep() : "goToSleep()";
                    assert nbWorkers == 0 : "workers";
                    assert _IN.size() == 0 : "IN";
                    assert _OUT.size() == 0 : "OUT";
                    assert forbidden.cardinality() == 0 : "forbidden";
                    synchronized (master) {
                        master.notify();
                    }
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    protected synchronized void checkOut() {
        while (_OUT.size() > 0) {
            IRequest request = _OUT.poll();
            int to = request.getPropagator().getNbVars();
            for (int i = 0; i < to; i++) {
                forbidden.set(request.getPropagator().getVar(i).getUniqueID(), false);
            }
        }
    }

    private synchronized boolean goToSleep() {
        return nbWorkers == 0 && _IN.size() == 0 && _OUT.size() == 0;
    }

    protected synchronized IRequest pop() {
        if (_IN.size() > 0) {
            nbWorkers++;
            return _IN.poll();
        }
        return null;
    }

    protected synchronized void push(IRequest request) {
        _OUT.add(request);
        nbWorkers--;
        assert nbWorkers > -1;
    }

    private class Launcher implements Runnable {

        protected boolean active = true;
        protected final MySequencer sequencer;

        public Launcher(MySequencer sequencer) {
            this.sequencer = sequencer;
        }

        @Override
        public void run() {
            for (; active; ) {
                IRequest request = sequencer.pop();
                if (request != null) {
                    try {
                        request.filter();
                    } catch (ContradictionException e) {
                        sequencer.exception(e);
                    }
                    sequencer.push(request);
                }
            }
        }

        public void kill() {
            active = false;
        }
    }
}
