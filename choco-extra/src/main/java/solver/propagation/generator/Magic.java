/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.propagation.generator;

import solver.Configuration;
import solver.exception.ContradictionException;
import solver.propagation.ISchedulable;
import solver.propagation.queues.DoubleMinHeap;
import solver.search.loop.ISearchLoop;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @revision 04/03/12 add update feature
 * @revision 04/03/12 change schedule
 * @since 15/12/11
 */
public final class Magic<S extends ISchedulable> extends PropagationStrategy<S> {

    protected double[] w;
    protected DoubleMinHeap toPropagate;
    protected S lastPopped;
    protected final double g;

    long timestamp; // to force updating

    final ISearchLoop sl;

    @SuppressWarnings({"unchecked"})
    public Magic(ISearchLoop sl, double g, S[] schedulables) {
        super(schedulables);
        for (int e = 0; e < elements.length; e++) {
            elements[e].setScheduler(this, e);
        }
        this.toPropagate = new DoubleMinHeap(elements.length);
        w = new double[elements.length];
        this.sl = sl;
        timestamp = -1;
        this.g = g;

    }


    @Override
    public S[] getElements() {
        return (S[]) new ISchedulable[]{this};
    }

    //<-- PROPAGATION ENGINE
    @Override
    public void schedule(S element) {
        // CONDITION: the element must not be already present (checked in element)
        assert !element.enqueued();
        int idx = element.getIndexInScheduler();
        double _w = w[idx];
        toPropagate.insert(_w, idx);
        element.enqueue();
        if (!enqueued) {
            scheduler.schedule(this);
        }
    }

    @Override
    public void remove(S element) {
        element.deque();
        int idx = element.getIndexInScheduler();
        toPropagate.remove(idx);
    }


    @Override
    protected boolean _pickOne() throws ContradictionException {
        update();
        if (!toPropagate.isEmpty()) {
            _execute();
        }
        return toPropagate.isEmpty();
    }

    private void _execute() throws ContradictionException {
        int idx = toPropagate.removemin();
        lastPopped = elements[idx];
        lastPopped.deque();
        if (!lastPopped.execute() && !lastPopped.enqueued()) {
            schedule(lastPopped);
        }
        w[idx] += 1;
    }


    @Override
    protected boolean _sweepUp() throws ContradictionException {
        return _clearOut();
    }

    protected boolean _loopOut() throws ContradictionException {
        return _clearOut();
    }

    protected boolean _clearOut() throws ContradictionException {
        update();
        while (!toPropagate.isEmpty()) {
            _execute();
        }
        return true;
    }

    private void update() {
        if (timestamp != sl.getTimeStamp()) {
            timestamp = sl.getTimeStamp();
            for (int i = 0; i < w.length; i++) {
                w[i] *= g;
            }
        }
    }

    @Override
    public void flush() {
        if (lastPopped != null) {
            lastPopped.flush();
        }
        //CPRU : should be improved
        while (!toPropagate.isEmpty()) {
            int idx = toPropagate.removemin();
            lastPopped = elements[idx];
            if (Configuration.LAZY_UPDATE) {
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

    @Override
    public boolean needUpdate() {
        return false;
    }

    @Override
    public void update(S element) {
    }

    @Override
    public String toString() {
        StringBuffer st = new StringBuffer();
        for (int i = 0; i < elements.length; i++) {
            st.append(elements[i]).append(" - ").append(w[i]).append("\n");
        }
        return st.toString();
    }

    //-->
}
