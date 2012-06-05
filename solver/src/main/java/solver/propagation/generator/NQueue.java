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
package solver.propagation.generator;

import solver.exception.ContradictionException;
import solver.propagation.ISchedulable;
import solver.propagation.generator.sorter.evaluator.IEvaluator;
import solver.propagation.queues.FixSizeCircularQueue;
import solver.recorders.IEventRecorder;

import java.util.BitSet;

/**
 * A specific propagation strategy that works like a n-queue (fifo).
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 change schedule
 * @since 15/12/11
 */
public final class NQueue<S extends ISchedulable> extends PropagationStrategy<S> {

    protected IEvaluator<S> evaluator;
    protected int offset, size;
    protected S lastPopped;
    protected FixSizeCircularQueue<S>[] toPropagate;
    protected BitSet notEmpty;

    @SuppressWarnings({"unchecked"})
    public NQueue(IEvaluator<S> evaluator, int min, int max, Generator<S>... generators) {
        super(generators);
        int nbe = 0;
        for (int e = 0; e < elements.length; e++) {
            elements[e].setScheduler(this, e);
            nbe++;
        }
        this.evaluator = evaluator;
        this.offset = min;
        this.size = max - min + 1;
        toPropagate = new FixSizeCircularQueue[size];
        for (int i = 0; i < size; i++) {
            toPropagate[i] = new FixSizeCircularQueue<S>(nbe);
        }
        notEmpty = new BitSet(size);
    }

    @Override
    public S[] getElements() {
        return (S[]) new ISchedulable[]{this};
    }

    //-->

    //<-- PROPAGATION ENGINE

    @Override
    public void schedule(S element) {
        // CONDITION: the element must not be already present (checked in element)
        assert !element.enqueued();
        int q = evaluator.eval(element) - offset;
        try {
            toPropagate[q].add(element);
            notEmpty.set(q);
        } catch (ArrayIndexOutOfBoundsException e) {
            if (q < offset) {
                toPropagate[0].add(element);
                notEmpty.set(0);
            } else if (q >= size) {
                toPropagate[size - 1].add(element);
                notEmpty.set(size - 1);
            }
        }
        element.enqueue();
        if (!enqueued) {
            scheduler.schedule(this);
        }
    }

    @Override
    public void remove(S element) {
        int q = notEmpty.nextSetBit(0);
        // first find the correct queue
        while (q > -1 && toPropagate[q].indexOf(element) == -1) {
            q = notEmpty.nextSetBit(q + 1);
        }
        assert q < size : "unknown element";
        // then remove the element
        toPropagate[q].remove(element);
        if (toPropagate[q].isEmpty()) {
            notEmpty.clear(q);
        }
        element.deque();
        if (this.enqueued && notEmpty.isEmpty()) {
            scheduler.remove(this);
        }
    }

    @Override
    protected boolean _pickOne() throws ContradictionException {
        if (!notEmpty.isEmpty()) {
            int q = notEmpty.nextSetBit(0);
            lastPopped = toPropagate[q].remove();//pop();
            lastPopped.deque();
            if (!lastPopped.execute() && !lastPopped.enqueued()) {
                schedule(lastPopped);
            }
            boolean empty = toPropagate[q].isEmpty();
            if (empty) {
                notEmpty.clear(q);
            }
        }
        return notEmpty.isEmpty();
    }

    @Override
    protected boolean _sweepUp() throws ContradictionException {
        return _clearOut();
    }

    @Override
    protected boolean _loopOut() throws ContradictionException {
        return _clearOut();
    }

    protected boolean _clearOut() throws ContradictionException {
        int q = notEmpty.nextSetBit(0);
        while (q > -1) {
            assert !toPropagate[q].isEmpty();
            lastPopped = toPropagate[q].remove();//.pop();
            lastPopped.deque();
            if (!lastPopped.execute() && !lastPopped.enqueued()) {
                schedule(lastPopped);
            }
            if (toPropagate[q].isEmpty()) {
                notEmpty.clear(q);
            }
            q = notEmpty.nextSetBit(0);
        }
        return true;
    }

    @Override
    public void flush() {
        if (lastPopped != null) {
            lastPopped.flush();
        }
        int q = notEmpty.nextSetBit(0);
        while (q > -1) {
            while (!toPropagate[q].isEmpty()) {
                lastPopped = toPropagate[q].remove();//.pop();
                if (IEventRecorder.LAZY) {
                    lastPopped.flush();
                }
                lastPopped.deque();
            }
            notEmpty.clear(q);
            q = notEmpty.nextSetBit(q + 1);
        }
    }

    @Override
    public boolean isEmpty() {
        int q = notEmpty.nextSetBit(0);
        while (q > -1) {
            if (!toPropagate[q].isEmpty()) {
                return false;
            }
            q = notEmpty.nextSetBit(q + 1);
        }
        return true;
    }

    @Override
    public int size() {
        int size = 0;
        int q = notEmpty.nextSetBit(0);
        while (q > -1) {
            size += toPropagate[q].size();
            q = notEmpty.nextSetBit(q + 1);
        }
        return size;
    }

    //-->

}
