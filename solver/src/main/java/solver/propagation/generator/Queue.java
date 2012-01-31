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

import solver.Solver;
import solver.exception.ContradictionException;
import solver.propagation.ISchedulable;
import solver.propagation.PropagationEngine;
import solver.recorders.IEventRecorder;

import java.util.*;

/**
 * A specific propagation strategy that works like a queue (fifo).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/11
 */
public class Queue<S extends ISchedulable> extends PropagationStrategy<S> {

    protected S lastPopped;

    //protected FixSizeCircularQueue<S> toPropagate;
    protected java.util.Queue<S> toPropagate;


    private Queue(List elements) {
        super(elements);
    }

    //<-- DSL

    public static Queue build(Generator... generators) {
        if (generators.length == 0) {
            throw new RuntimeException("Sort::Empty generators array");
        }
        return new Queue(Arrays.asList(generators));
    }

    @Override
    public List<Queue<S>> populate(PropagationEngine propagationEngine, Solver solver) {
        List<S> elements = new ArrayList<S>();
        for (int i = 0; i < generators.size(); i++) {
            Generator gen = generators.get(i);
            elements.addAll(gen.populate(propagationEngine, solver));
        }
        for (int e = 0; e < elements.size(); e++) {
            elements.get(e).setScheduler(this, 0);
        }
        //toPropagate = new FixSizeCircularQueue<S>(elements.size());
        toPropagate = new ArrayDeque();
        return Collections.singletonList(this);
    }
    //-->

    //<-- PROPAGATION ENGINE

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
    protected boolean _pickOne() throws ContradictionException {
        if (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.remove();//pop();
            lastPopped.deque();
            if (!lastPopped.execute()) {
                schedule(lastPopped);
            }
        }
        return toPropagate.isEmpty();
    }

    @Override
    protected boolean _sweepUp() throws ContradictionException {
        return _clearOut();
    }

    protected boolean _clearOut() throws ContradictionException {
        while (!toPropagate.isEmpty()) {
            lastPopped = toPropagate.remove();//.pop();
            lastPopped.deque();
            if (!lastPopped.execute()) {
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
            lastPopped = toPropagate.remove();//.pop();
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
