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

import solver.propagation.engines.ThreadedPropagationEngine;
import solver.requests.IRequest;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/06/11
 */
public class Sequencer {

    private enum State {
        RUN, SUSPEND, SLEEP
    }

    public int nbWorkers; // number of thread currently working on a request
    private final ThreadedPropagationEngine master;
    private BitSet toPropagate; // indices of requests to propagate
    private BitSet forbidden; // indices of forbidden variables for active requests
    protected Launcher[] launchers; // propagator a request -- in a thread
    protected volatile State currentState;
    protected volatile boolean fail;

    public Sequencer(ThreadedPropagationEngine master, int nbRequests, int nbVars, int nbThreads) {
        this.master = master;
        this.toPropagate = new BitSet(nbRequests);
        this.forbidden = new BitSet(nbVars);
        this.nbWorkers = 0;
        currentState = State.SLEEP;
        this.launchers = new Launcher[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            launchers[i] = new Launcher(this, i);
            launchers[i].start();
        }
    }

    public void awake() {
        synchronized (this) {
//            LoggerFactory.getLogger("solver").info("awake");
            assert nbWorkers == 0;
            assert forbidden.cardinality() == 0;
            fail = false;
            currentState = State.RUN;
        }
    }

    public synchronized IRequest getFreeRequestId() {
//        synchronized (this) {
        IRequest request = null;
        switch (currentState) {
            case RUN:
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
                    currentState = State.SUSPEND;
                }
            case SUSPEND:
//                LoggerFactory.getLogger("solver").info("~{}", forbidden);
                if (nbWorkers == 0) {
                    assert forbidden.cardinality() == 0;
                    currentState = State.SLEEP;
                }
                break;
            case SLEEP:
            default:
                synchronized (master) {
                    master.notify();
                }
                break;
        }
        return request;
    }

    private boolean conditions(int ridx) {
        int nbV = master.requests[ridx].getPropagator().getNbVars();
        for (int i = 0; i < nbV; i++) {
            int idx = master.requests[ridx].getPropagator().getVar(i).getUniqueID();
            if (forbidden.get(idx)) return false;
        }
        return true;
    }

    public void allow(IRequest request) {
        synchronized (this) {
//            LoggerFactory.getLogger("solver").info("{} free {}", this, request);
            int to = request.getPropagator().getNbVars();
            for (int i = 0; i < to; i++) {
                forbidden.set(request.getPropagator().getVar(i).getUniqueID(), false);
            }
            nbWorkers--;
        }
    }

    public void set(int index, boolean value) {
        synchronized (this) {
            toPropagate.set(index, value);
        }
    }

    public boolean hasFailed() {
        return fail;
    }

    public void interrupt() {
        synchronized (this) {
//            LoggerFactory.getLogger("solver").info("toPropagate.card = {}, fail = {}", toPropagate.cardinality(), fail);
            fail = true;
            currentState = State.SUSPEND;
        }
    }

    public void flushAll() {
        assert currentState == State.SLEEP : "wrong state: " + currentState;
        for (int i = toPropagate.nextSetBit(0); i >= 0; i = toPropagate.nextSetBit(i + 1)) {
            master.requests[i].deque();
            toPropagate.clear(i);
        }
    }

    @Override
    public String toString() {
        return "W:" + nbWorkers + ", C:" + toPropagate.cardinality();
    }
}
