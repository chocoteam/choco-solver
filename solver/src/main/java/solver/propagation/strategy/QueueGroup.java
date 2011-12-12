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
import solver.propagation.queues.FixSizeCircularQueue;
import solver.recorders.IEventRecorder;

/**
 * A specific group that works like a queue (fifo).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/12/11
 */
public class QueueGroup<S extends ISchedulable> extends Group<S> {

    protected S lastPopped;

    protected FixSizeCircularQueue<S> toPropagate;

    public QueueGroup(Iteration iteration, S... schedulables) {
        super(iteration);
        toPropagate = new FixSizeCircularQueue<S>(schedulables.length);
        for (int i = 0; i < schedulables.length; i++) {
            ISchedulable er = schedulables[i];
            er.setScheduler(this, 0);
        }
    }

    @Override
    public void schedule(S element) {
        toPropagate.add(element);
        element.enqueue();
        if (!this.enqueued) {
            scheduler.schedule(this);
        }
    }

    @Override
    public void remove(S element) {
        element.deque();
        toPropagate.remove(element);
        if (this.enqueued && toPropagate.isEmpty()) {
            scheduler.remove(this);
        }
    }

    @Override
    protected boolean pickOne() throws ContradictionException {
        if (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.pop();
            lastPopped.deque();
            if (!lastPopped.execute()) {
                schedule(lastPopped);
            }
        }
        return toPropagate.isEmpty();
    }

    @Override
    protected boolean sweepUp() throws ContradictionException {
        return clearOut();
    }

    protected boolean clearOut() throws ContradictionException {
        while (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.pop();
            lastPopped.deque();
            if (!lastPopped.execute()) {
                schedule(lastPopped);
            }
        }
        return true;
    }

    @Override
    public void flush() {
        if(lastPopped!= null){
            lastPopped.flush();
        }
        while (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.pop();
            if (IEventRecorder.LAZY) {
                lastPopped.flush();
            }
            lastPopped.deque();
        }
    }

}
