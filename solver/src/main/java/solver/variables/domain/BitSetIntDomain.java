/**
 *  Copyright (c) 2010, Ecole des Mines de Nantes
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

package solver.variables.domain;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.OneWordS32BitSet;
import choco.kernel.memory.structure.OneWordS64BitSet;
import solver.variables.domain.delta.Delta;
import solver.variables.domain.delta.IntDelta;
import solver.variables.domain.delta.NoDelta;

/**
 * TODO: complete when Domain interface will be created!
 * <br/>
 * <br/>
 * <i>Based on Choco-2.1.1</i>
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @since 0.01
 */
public final class BitSetIntDomain implements IIntDomain {

    /* Bitset of available values -- includes offset */
    choco.kernel.memory.IStateBitSet values;
    /* Lower bound of the current domain -- includes offset */
    IStateInt lowerbound;
    /* Upper bound of the current domain -- includes offset */
    IStateInt upperbound;
    IStateInt size;

    IntDelta delta = NoDelta.singleton;

    /**
     * offset of the lower bound and the first value in the domain
     */
    int offset;

    protected DisposableIntIterator _cachedIterator;

    public BitSetIntDomain(int[] sortedValues, IEnvironment env) {
        offset = sortedValues[0];
        int capacity = sortedValues[sortedValues.length - 1] - offset + 1;
        if (capacity < 32) {
            this.values = new OneWordS32BitSet(env, capacity);
        } else if (capacity < 64) {
            this.values = new OneWordS64BitSet(env, capacity);
        } else {
            this.values = env.makeBitSet(capacity);
        }
        for (int i = 0; i < sortedValues.length; i++) {
            this.values.set(sortedValues[i] - offset, true);
        }
        this.lowerbound = env.makeInt(0);
        this.upperbound = env.makeInt(capacity - 1);
        this.size = env.makeInt(sortedValues.length);
    }

    public BitSetIntDomain(int min, int max, IEnvironment env) {
        this.offset = min;
        int capacity = max - min + 1;
        if (capacity < 32) {
            this.values = new OneWordS32BitSet(env, capacity);
        } else if (capacity < 64) {
            this.values = new OneWordS64BitSet(env, capacity);
        } else {
            this.values = env.makeBitSet(capacity);
        }
        for (int i = 0; i <= max - min; i++) {
            this.values.set(i, true);
        }
        this.lowerbound = env.makeInt(0);
        this.upperbound = env.makeInt(max - min);
        this.size = env.makeInt(capacity);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(int aValue) {
        aValue -= offset;
        boolean change = values.get(aValue);
        this.values.set(aValue, false);
        if (change) {
            this.size.add(-1);
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAndUpdateDelta(int aValue) {
        aValue -= offset;
        boolean change = values.get(aValue);
        this.values.set(aValue, false);
        if (change) {
            this.size.add(-1);
            delta.add(aValue + offset);
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean restrict(int aValue) {
        aValue -= offset;
        int i = values.nextSetBit(this.lowerbound.get());
        for (; i < aValue; i = values.nextSetBit(i + 1)) {
            values.set(i, false);
        }
        i = values.nextSetBit(aValue + 1);
        for (; i >= 0; i = values.nextSetBit(i + 1)) {
            values.set(i, false);
        }
        this.lowerbound.set(aValue);
        this.upperbound.set(aValue);
        boolean change = size.get() > 1;
        this.size.set(1);
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean restrictAndUpdateDelta(int aValue) {
        aValue -= offset;
        int i = values.nextSetBit(this.lowerbound.get());
        for (; i < aValue; i = values.nextSetBit(i + 1)) {
            values.set(i, false);
            delta.add(i + offset);
        }
        i = values.nextSetBit(aValue + 1);
        for (; i >= 0; i = values.nextSetBit(i + 1)) {
            values.set(i, false);
            delta.add(i + offset);
        }
        this.lowerbound.set(aValue);
        this.upperbound.set(aValue);
        boolean change = size.get() > 1;
        this.size.set(1);
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean instantiated() {
        return this.size.get() == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(int aValue) {
        aValue -= offset;
        return aValue >= 0 && this.values.get(aValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateUpperBound(int aValue) {
        aValue -= offset;
        int c = 0;
        for (int i = upperbound.get(); i > aValue; i = values.prevSetBit(i - 1)) {
            values.clear(i);
            c++;
        }
        upperbound.set(values.prevSetBit(aValue));
        size.add(-c);
        return c > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateUpperBoundAndDelta(int aValue) {
        aValue -= offset;
        int c = 0;
        for (int i = upperbound.get(); i > aValue; i = values.prevSetBit(i - 1)) {
            values.clear(i);
            //BEWARE: this line significantly decreases performances
            delta.add(i + offset);
            c++;
        }
        upperbound.set(values.prevSetBit(aValue));
        size.add(-c);
        return c > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateLowerBound(int aValue) {
        aValue -= offset;
        int c = 0;
        for (int i = lowerbound.get(); i < aValue; i = values.nextSetBit(i + 1)) {
            values.clear(i);
            c++;
        }
        lowerbound.set(values.nextSetBit(aValue));
        size.add(-c);
        return c > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateLowerBoundAndDelta(int aValue) {
        aValue -= offset;
        int c = 0;
        for (int i = lowerbound.get(); i < aValue; i = values.nextSetBit(i + 1)) {
            values.clear(i);
            //BEWARE: this line significantly decreases performances
            delta.add(i + offset);
            c++;
        }
        lowerbound.set(values.nextSetBit(aValue));
        size.add(-c);
        return c > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean empty() {
        return this.values.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public int getLB() {
        return this.lowerbound.get() + offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUB() {
        return this.upperbound.get() + offset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return size.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnumerated() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntDelta getDelta() {
        return delta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordRemoveValues() {
        delta = new Delta();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNextValue(int aValue) {
        return values.nextSetBit(aValue - offset + 1) >= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nextValue(int aValue) {
        aValue -= offset;
        if (aValue < 0 || aValue < lowerbound.get()) return getLB();
        aValue = values.nextSetBit(aValue + 1);
        if (aValue > -1) return aValue + offset;
        return Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPreviousValue(int aValue) {
        return values.prevSetBit(aValue - offset - 1) >= 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int previousValue(int aValue) {
        aValue -= offset;
        if (aValue > upperbound.get()) return getUB();
        aValue = values.prevSetBit(aValue - 1);
        if (aValue > -1) return aValue + offset;
        return Integer.MIN_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (size.get() == 1) {
            return Integer.toString(this.getLB());
        }
        StringBuilder s = new StringBuilder(20);
        s.append('{').append(this.getLB());
        int nb = 5;
        for (int i = nextValue(this.getLB()); i < Integer.MAX_VALUE && nb > 0; i = nextValue(i)) {
            s.append(',').append(i);
            nb--;
        }
        if (nb == 0) {
            s.append("...,").append(this.getUB());
        }
        s.append('}');
        return s.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DisposableIntIterator getIterator() {
        IntDomainIterator iter = (IntDomainIterator) _cachedIterator;
        if (iter != null && iter.reusable) {
            iter.init();
            return iter;
        }
        _cachedIterator = new IntDomainIterator(this);
        return _cachedIterator;
    }

    protected static class IntDomainIterator extends DisposableIntIterator {
        protected BitSetIntDomain domain;
        protected int nextValue;
        //protected int supBound = -1;
        protected int offset;

        private IntDomainIterator(BitSetIntDomain dom) {
            this.domain = dom;
            this.offset = dom.offset;
            init();
        }

        @Override
        public void init() {
            super.init();
            if (domain.size.get() >= 1) {
                nextValue = domain.getLB();
            } else {
                throw new UnsupportedOperationException();
            }
            //supBound = domain.getUB();
        }

        public boolean hasNext() {
            return nextValue < Integer.MAX_VALUE;
        }

        public int next() {
            int v = nextValue;
            nextValue = domain.nextValue(nextValue);
            return v;
        }
    }

}
