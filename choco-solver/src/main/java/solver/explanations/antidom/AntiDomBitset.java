/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.solver.explanations.antidom;

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 20/10/11
 * Time: 16:52
 */
public class AntiDomBitset implements AntiDomain {
    private final int offset;

    IStateBitSet domain;

    private DisposableValueIterator _viterator;


    public AntiDomBitset(IntVar A) {
        offset = A.getLB();
        domain = A.getSolver().getEnvironment().makeBitSet(A.getUB() - offset + 1);
        DisposableRangeIterator rin = A.getRangeIterator(true);
        int p = rin.max();
        rin.next();
        while (rin.hasNext()) {
            int c = rin.min();
            for (int i = p + 1; i < c; i++) {
                add(i);
            }
            p = rin.max();
            rin.next();
        }
        rin.dispose();
    }


    @Override
    public void add(int outsideval) {
        int inside = outsideval - offset;
        domain.set(inside);
    }

    @Override
    public void updateLowerBound(int oldLB, int newLB) {
        for (int i = oldLB; i < newLB; i++) {
            add(i);
        }
    }

    @Override
    public void updateUpperBound(int oldUB, int newUB) {
        for (int i = oldUB; i > newUB; i--) {
            add(i);
        }
    }

    public boolean get(int outsideval) {
        int inside = outsideval - offset;
        return domain.get(inside);
    }

    @Override
    public int getKeyValue(int outsideval) {
        return outsideval;
    }

    @Override
    public int size() {
        return domain.cardinality();
    }

    public DisposableValueIterator getValueIterator() {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                int value;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    this.value = domain.nextSetBit(0);//LB.get() < Integer.MAX_VALUE) ? LB.get() : -1;
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                }

                @Override
                public boolean hasNext() {
                    return this.value != -1;
                }

                @Override
                public boolean hasPrevious() {
                    return this.value != -1;
                }

                @Override
                public int next() {
                    int old = this.value;
                    this.value = domain.nextSetBit(this.value + 1);
                    return old + offset;
                }

                @Override
                public int previous() {
                    int old = this.value;
                    this.value = domain.prevSetBit(this.value - 1);
                    return old + offset;
                }
            };
        }
        _viterator.bottomUpInit();
        return _viterator;
    }

    @Override
    public String toString() {

        StringBuilder bf = new StringBuilder();
        bf.append("[");
        DisposableValueIterator it = getValueIterator();
        while (it.hasNext()) {
            bf.append(" ").append(it.next());
        }
        it.dispose();
        bf.append("]");
        return bf.toString();
    }

    @Override
    public boolean isEnumerated() {
        return true;
    }
}
