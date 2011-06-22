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

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: cprudhom
 * Date: 10/06/11
 */
public abstract class AbstractSequencer extends Thread implements ISequencer {

    public int nbThreads; // number of declared threads
    final ThreadedPropagationEngine master;
    BitSet toPropagate; // indices of requests to propagate
    BitSet forbidden; // indices of forbidden variables for active requests
    protected volatile ISequencer.State currentState;
    protected volatile boolean fail;

    protected AbstractSequencer(ThreadedPropagationEngine master, int nbRequests, int nbVars, int nbThreads) {
        super("Sequencer");
        this.master = master;
        this.toPropagate = new BitSet(nbRequests);
        this.forbidden = new BitSet(nbVars);
        this.nbThreads = nbThreads;
        currentState = ISequencer.State.SLEEPING;
    }

    public void fixpoint() {
        synchronized (this) {
            assert forbidden.cardinality() == 0;
            fail = false;
            currentState = ISequencer.State.RUNNING;
            this.notify();
        }
    }

    protected final boolean conditions(int ridx) {
        int nbV = master.requests[ridx].getPropagator().getNbVars();
        for (int i = 0; i < nbV; i++) {
            int idx = master.requests[ridx].getPropagator().getVar(i).getUniqueID();
            if (forbidden.get(idx)) return false;
        }
        return true;
    }

    public final void set(int index, boolean value) {
        synchronized (this) {
            toPropagate.set(index, value);
        }
    }

    public final boolean hasFailed() {
        return fail;
    }

    protected final void exception(ContradictionException e) {
        synchronized (this) {
            fail = true;
//            LoggerFactory.getLogger("solver").error("FAIL RU->SU");
            currentState = ISequencer.State.SUSPENDING;
        }
    }

    public final void flushAll() {
        assert currentState == ISequencer.State.SLEEPING : "wrong state: " + currentState;
        for (int i = toPropagate.nextSetBit(0); i >= 0; i = toPropagate.nextSetBit(i + 1)) {
            master.requests[i].deque();
            toPropagate.clear(i);
        }
    }
}
