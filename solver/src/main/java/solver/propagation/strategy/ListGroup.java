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
package solver.propagation.strategy;

import solver.exception.ContradictionException;
import solver.propagation.ISchedulable;
import solver.recorders.IEventRecorder;

import java.util.BitSet;

/**
 * A specific propagation engine that works like a list, each element has a fixed index.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public class ListGroup extends Group {

    protected ISchedulable lastPopped;

    protected final ISchedulable[] elements;
    protected final BitSet toPropagate;

    protected boolean init = false;

    /**
     * Build a list to store schedulable elements
     *
     * @param schedulables sorted schedulable elements
     */
    public ListGroup(Iteration iteration, ISchedulable... schedulables) {
        super(iteration);
        this.elements = schedulables.clone();
        this.toPropagate = new BitSet(elements.length);
        for (int i = 0; i < elements.length; i++) {
            elements[i].setScheduler(this, i);
        }
    }

    @Override
    public void schedule(ISchedulable element) {
        int idx = element.getIndexInScheduler();
        toPropagate.set(idx);
        element.enqueue();
    }

    @Override
    public void remove(ISchedulable element) {
        element.deque();
        int idx = element.getIndexInScheduler();
        toPropagate.clear(idx);
    }


    @Override
    protected boolean pickOne() throws ContradictionException {
        if (!toPropagate.isEmpty()) {
            int idx = toPropagate.nextSetBit(0);
            lastPopped = elements[idx];
            lastPopped.deque();
            if (!lastPopped.execute()) {
                schedule(lastPopped);
            }
        }
        return toPropagate.isEmpty();
    }

    @Override
    protected boolean sweepUp() throws ContradictionException {
        int idx = toPropagate.nextSetBit(0);
        while (idx >= 0) {
            lastPopped = elements[idx];
            lastPopped.deque();
            if (!lastPopped.execute()) {
                schedule(lastPopped);
            }
            idx = toPropagate.nextSetBit(idx + 1);
        }
        return toPropagate.isEmpty();
    }

    protected boolean clearOut() throws ContradictionException {
        while (!toPropagate.isEmpty()) {
            int idx = toPropagate.nextSetBit(0);
            lastPopped = elements[idx];
            lastPopped.deque();
            if (!lastPopped.execute()) {
                schedule(lastPopped);
            }
        }
        return true;
    }

    @Override
    public void flush() {
        lastPopped.flush();
        for (int i = toPropagate.nextSetBit(0); i >= 0; i = toPropagate.nextSetBit(i)) {
            lastPopped = elements[i];
            if (IEventRecorder.LAZY) {
                lastPopped.flush();
            }
            lastPopped.deque();
        }
    }
}
