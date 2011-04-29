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
import solver.variables.domain.delta.IntDelta;
import solver.variables.domain.delta.NoDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28 juil. 2010
 */
public final class IntervalIntDomain implements IIntDomain {

    /**
     * The backtrackable minimal value of the variable.
     */

    private final choco.kernel.memory.IStateInt lowerbound;

    /**
     * The backtrackable maximal value of the variable.
     */

    private final IStateInt upperbound;

    private final choco.kernel.memory.IStateInt size;

    protected DisposableIntIterator _cachedIterator;

    IntDelta delta = NoDelta.singleton;

    public IntervalIntDomain(int a, int b, IEnvironment environment) {
        lowerbound = environment.makeInt(a);
        upperbound = environment.makeInt(b);
        size = environment.makeInt(b - a + 1);
//        delta = new Delta();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(int aValue) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAndUpdateDelta(int aValue) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean restrictAndUpdateDelta(int aValue) {
        int ub = this.upperbound.get();
        int i = this.lowerbound.get();
        for (; i < aValue; i++) {
            delta.add(i);
        }
        i = aValue;
        for (; i < ub; i++) {
            delta.add(i);
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
    public boolean restrict(int aValue) {
        this.lowerbound.set(aValue);
        this.upperbound.set(aValue);
        this.size.set(1);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean instantiated() {
        return size.get() == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final int aValue) {
        return ((aValue >= lowerbound.get()) && (aValue <= upperbound.get()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateUpperBound(int aValue) {
        size.add(aValue - upperbound.get());
        upperbound.set(aValue);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateUpperBoundAndDelta(int aValue) {
        boolean change = false;
        for (int i = upperbound.get(); i > aValue; i--) {
            change = true;
            //BEWARE: this line significantly decreases performances
            delta.add(i);
        }
        size.add(aValue - upperbound.get());
        upperbound.set(aValue);
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateLowerBound(int aValue) {
        size.add(lowerbound.get() - aValue);
        lowerbound.set(aValue);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateLowerBoundAndDelta(int aValue) {
        boolean change = false;
        for (int i = lowerbound.get(); i < aValue; i++) {
            change = true;
            //BEWARE: this line significantly decreases performances
            delta.add(i);
        }
        size.add(lowerbound.get() - aValue);
        lowerbound.set(aValue);
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean empty() {
        return size.get() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLB() {
        return this.lowerbound.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUB() {
        return this.upperbound.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getSize() {
        return size.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnumerated() {
        return false;
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
        //nothing to do, interval domain does not react on value removals
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNextValue(int aValue) {
        return aValue < upperbound.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nextValue(final int aValue) {
        if (aValue < lowerbound.get()) {
            return lowerbound.get();
        } else if (aValue < upperbound.get()) {
            return aValue + 1;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPreviousValue(int aValue) {
        return aValue > lowerbound.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int previousValue(final int aValue) {
        if (aValue > upperbound.get()) {
            return upperbound.get();
        } else if (aValue > lowerbound.get()) {
            return aValue - 1;
        } else {
            return Integer.MIN_VALUE;
        }
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
        protected IntervalIntDomain domain;
        protected int nextValue;

        private IntDomainIterator(IntervalIntDomain dom) {
            this.domain = dom;
            init();
        }

        @Override
        public void init() {
            super.init();
            if (domain.getSize() >= 1) {
                nextValue = domain.getLB() - 1;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        public boolean hasNext() {
            return nextValue < domain.getUB();
        }

        public int next() {
            return ++nextValue;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (size.get() == 1) {
            return Integer.toString(getLB());
        }
        return String.format("[%d,%d]", getLB(), getUB());
    }
}
