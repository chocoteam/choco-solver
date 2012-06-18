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
import solver.recorders.IEventRecorder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;


/**
 * A specific propagation strategy that works like a queue (fifo).
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 change schedule
 * @since 15/12/11
 */
public final class Queue<S extends ISchedulable> extends PropagationStrategy<S> {

    private static final int HIGH = 99999999 ;

    protected S lastPopped;
//    protected AQueue<S> toPropagate;
    protected Deque<S> toPropagate;

    @SuppressWarnings({"unchecked"})
    public Queue(Generator<S>... generators) {
        super(generators);
        int nbe = 0;
        for (int e = 0; e < elements.length; e++) {
            elements[e].setScheduler(this, e);
            nbe++;
        }
        if (nbe > HIGH) {
            toPropagate = new LinkedList<S>();
        } else {
            toPropagate = new ArrayDeque<S>(nbe);
        }
//
//        toPropagate = new FixSizeCircularQueue<S>(nbe);
    }

    public Queue(S[] schedulables) {
        super(schedulables);
        int nbe = 0;
        for (int e = 0; e < elements.length; e++) {
            elements[e].setScheduler(this, e);
            nbe++;
        }

        if (nbe > HIGH) {
            toPropagate = new LinkedList<S>();
        } else {
            toPropagate = new ArrayDeque<S>(nbe);
        }


//        toPropagate = new FixSizeCircularQueue<S>(nbe);
    }


    @Override
    public S[] getElements() {
        return (S[]) new ISchedulable[]{this};
    }

    //-->

    //<-- PROPAGATION ENGINE
    public static int count;
    public static double mean;
    public static double variance;
    public static int max;

    protected static void update(int size) throws ContradictionException {
        count++;
        double U = size - mean;
        mean += U / count;
        variance = U * (size - mean);
        if (max < size) {
            max = size;
        }
        //if(count >= 500000)throw new ContradictionException();
        double st = Math.sqrt(variance / (count - 1));
        //System.out.printf("%d\t%d\t%.3f\t%.3f\t%.3f\n", count,size, mean, mean - st, mean + st);
    }

    public static void print() {
        //System.out.printf("count:%d, max:%d, mean:%.3f, sd:%.3f\n", count, max, mean, Math.sqrt(variance / (count - 1)));
    }

    @Override
    public void schedule(S element) {
        // CONDITION: the element must not be already present (checked in element)
        assert !element.enqueued();
        toPropagate.add(element);
        element.enqueue();
        if (!enqueued) {
            scheduler.schedule(this);
        }
    }

    @Override
    public void remove(S element) {
        toPropagate.remove(element);
        element.deque();
        if (this.enqueued && toPropagate.isEmpty()) {
            scheduler.remove(this);
        }
    }

    @Override
    protected boolean _pickOne() throws ContradictionException {
        if (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.remove();//pop();
            lastPopped.deque();
            if (!lastPopped.execute() && !lastPopped.enqueued()) {
                schedule(lastPopped);
            }
        }
        return toPropagate.isEmpty();
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
        while (!toPropagate.isEmpty()) {
            update(toPropagate.size());
            lastPopped = toPropagate.pop();//.pop();
            lastPopped.deque();
            if (!lastPopped.execute() && !lastPopped.enqueued()) {
                schedule(lastPopped);
            }
        }
        return true;
    }

    @Override
    public void flush() {
        if (lastPopped != null) {
            lastPopped.flush();
        }
        while (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.pop();//.pop();
            if (IEventRecorder.LAZY) {
                lastPopped.flush();
            }
            lastPopped.deque();
        }
    }

    @Override
    public boolean isEmpty() {
        return toPropagate.isEmpty();
    }

    @Override
    public int size() {
        return toPropagate.size();
    }

    //-->

}
