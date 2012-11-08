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

import solver.exception.ContradictionException;
import solver.propagation.ISchedulable;
import solver.propagation.generator.sorter.evaluator.IEvaluator;

import java.lang.reflect.Array;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/06/12
 */
public class Switcher<S extends ISchedulable> extends PropagationStrategy<S> {

    final PropagationStrategy<S>[] pstrats;
    protected IEvaluator<S> evaluator;
    protected int offset, size;
    protected int[] queues;


    private static <S extends ISchedulable> PropagationStrategy<S>[] maker(int min, int max, PropagationStrategy<S> pstrat) {
        PropagationStrategy<S>[] tmp = (PropagationStrategy<S>[]) Array.newInstance(pstrat.getClass(), max - min + 1);
        tmp[0] = pstrat;
        for (int i = 1; i < tmp.length; i++) {
            tmp[i] = pstrat.duplicate();
        }
        return tmp;
    }

    public Switcher(IEvaluator<S> evaluator, int min, int max, PropagationStrategy<S> pstrat,
                    S[] generators) {

        this(evaluator, min, max, maker(min, max, pstrat), generators);
    }

    private Switcher(IEvaluator<S> evaluator, int min, int max, PropagationStrategy<S>[] pstrats,
                     S[] generators) {
        super(generators);
        for (int e = 0; e < elements.length; e++) {
            elements[e].setScheduler(this, e);
        }
        queues = new int[elements.length];
        this.pstrats = pstrats;
        this.evaluator = evaluator;
        this.offset = min;
        size = max - min + 1;
        assert pstrats.length >= size : "wrong size";
    }

    public PropagationStrategy<S>[] getPS() {
        return pstrats;
    }

    @Override
    public S[] getElements() {
        return (S[]) pstrats;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        int i = 0;
        while (i < size && pstrats[i].isEmpty()) {
            i++;
        }
        return i == size;
    }

    private void _schedule(S element, int q) {
        pstrats[q].schedule(element);
        queues[element.getIndexInScheduler()] = q;
    }

    @Override
    public void schedule(S element) {
        assert !element.enqueued();
        int q = evaluator.eval(element) - offset;
        try {
            _schedule(element, q);
        } catch (ArrayIndexOutOfBoundsException e) {
            if (q < 0) _schedule(element, 0);
            else _schedule(element, size - 1);
        }
    }

    @Override
    public void remove(S element) {
        int q = queues[element.getIndexInScheduler()];
        pstrats[q].remove(element);
    }

    @Override
    public void flush() {
        int i = 0;
        while (i < size) {
            pstrats[i].flush();
            i++;
        }
    }

    @Override
    protected boolean _pickOne() throws ContradictionException {
        throw new UnsupportedOperationException("unexpected call to Switcher, should be delegated");
    }

    @Override
    protected boolean _sweepUp() throws ContradictionException {
        throw new UnsupportedOperationException("unexpected call to Switcher, should be delegated");
    }

    @Override
    protected boolean _loopOut() throws ContradictionException {
        throw new UnsupportedOperationException("unexpected call to Switcher, should be delegated");
    }

    @Override
    protected boolean _clearOut() throws ContradictionException {
        throw new UnsupportedOperationException("unexpected call to Switcher, should be delegated");
    }

}
