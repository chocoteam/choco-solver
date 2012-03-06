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
 * A specific propagation engine that works like a list, each element has a fixed index.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/11
 */
public class Sort<S extends ISchedulable> extends PropagationStrategy<S> {

    Comparator<S> comparator = new Comparator<S>() {
        @Override
        public int compare(S o1, S o2) {
            return 0;
        }
    };

    protected S lastPopped;

    protected S[] elements;
    protected BitSet toPropagate;
    protected boolean init = false;

    private Sort(List<Generator> generators) {
        super(generators);
    }

    private Sort(List<Generator> generator, Comparator<S> comparator) {
        super(generator);
        this.comparator = comparator;
    }

    //<-- DSL
    public static <S extends ISchedulable> Sort<S> build(Comparator<S> comparator, Generator... generators) {
        if (generators.length == 0) {
            throw new RuntimeException("Sort::Empty generators array");
        }
        return new Sort<S>(Arrays.asList(generators), comparator);
    }

    public static Sort build(Generator... generators) {
        if (generators.length == 0) {
            throw new RuntimeException("Sort::Empty generators array");
        }
        return new Sort(Arrays.asList(generators));
    }

    @Override
    public List<Sort<S>> populate(PropagationEngine propagationEngine, Solver solver) {
        List<S> elts = new ArrayList<S>();
        for (int i = 0; i < generators.size(); i++) {
            Generator gen = generators.get(i);
            elts.addAll(gen.populate(propagationEngine, solver));
        }
        this.elements = (S[]) elts.toArray(new ISchedulable[elts.size()]);
        Arrays.sort(elements, comparator);
        for (int e = 0; e < elements.length; e++) {
            elements[e].setScheduler(this, e);
        }
        this.toPropagate = new BitSet(elements.length);
        return Collections.singletonList(this);

    }

    /**
     * A specific method to prevent the engine to be incomplete, do not populate!
     */

    public static <S extends ISchedulable> Sort<S> corker(S... elts) {
        Sort<S> corker = new Sort<S>(Collections.<Generator>emptyList());
        corker.elements = elts.clone();
        for (int e = 0; e < corker.elements.length; e++) {
            corker.elements[e].setScheduler(corker, e);
        }
        corker.toPropagate = new BitSet(corker.elements.length);
        return corker;
    }

    //-->
    //<-- PROPAGATION ENGINE
    @Override
    public void schedule(ISchedulable element) {
        int idx = element.getIndexInScheduler();
        toPropagate.set(idx);
        element.enqueue();
        if (!this.enqueued) {
            scheduler.schedule(this);
        }
    }

    @Override
    public void remove(ISchedulable element) {
        element.deque();
        int idx = element.getIndexInScheduler();
        toPropagate.clear(idx);
    }


    @Override
    protected boolean _pickOne() throws ContradictionException {
        if (!toPropagate.isEmpty()) {
            int idx = toPropagate.nextSetBit(0);
            toPropagate.clear(idx);
            lastPopped = elements[idx];
            lastPopped.deque();
            if (!lastPopped.execute()) {
                schedule(lastPopped);
            }
        }
        return toPropagate.isEmpty();
    }

    @Override
    protected boolean _sweepUp() throws ContradictionException {
        int idx = toPropagate.nextSetBit(0);
        while (idx >= 0) {
            toPropagate.clear(idx);
            lastPopped = elements[idx];
            lastPopped.deque();
            if (!lastPopped.execute()) {
                schedule(lastPopped);
            }
            idx = toPropagate.nextSetBit(idx + 1);
        }
        return toPropagate.isEmpty();
    }

    protected boolean _clearOut() throws ContradictionException {
        while (!toPropagate.isEmpty()) {
            int idx = toPropagate.nextSetBit(0);
            toPropagate.clear(idx);
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
        if (lastPopped != null) {
            lastPopped.flush();
        }
        for (int i = toPropagate.nextSetBit(0); i >= 0; i = toPropagate.nextSetBit(i)) {
            toPropagate.clear(i);
            lastPopped = elements[i];
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
        return toPropagate.cardinality();
    }

    //-->
}
