/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.impl;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.ISolver;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.EnumDelta;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.monitor.EnumDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.ranges.IRemovals;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.tools.StringUtils;

/**
 * <br/>IntVar implementation for quite small domains bit with very distant values e.g. {-51900,42,235923}
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 14/05/2013
 */
public final class BitsetArrayIntVarImpl extends AbstractVariable implements IntVar {

    private static final long serialVersionUID = 1L;

    protected boolean reactOnRemoval = false;

    //  VALUES
    private final int[] VALUES;
    private final TIntIntHashMap V2I;
    //  Bitset of indexes
    private final IStateBitSet INDEXES;
    // Lower bound of the current domain
    private final IStateInt LB;
    // Upper bound of the current domain
    private final IStateInt UB;
    // Size of the current domain
    private final IStateInt SIZE;
    //offset of the lower bound and the first value in the domain
    private final int LENGTH;

    private IEnumDelta delta = NoDelta.singleton;

    private DisposableValueIterator _viterator;
    private DisposableRangeIterator _riterator;

    //////////////////////////////////////////////////////////////////////////////////////

    public BitsetArrayIntVarImpl(String name, int[] sortedValues, ISolver isolver) {
        super(name, isolver);
        IEnvironment env = solver.getEnvironment();
        this.LENGTH = sortedValues.length;
        this.VALUES = sortedValues.clone();
        this.V2I = new TIntIntHashMap(VALUES.length, .5f, Integer.MIN_VALUE, -1);
        this.INDEXES = env.makeBitSet(LENGTH);
        this.INDEXES.set(0, LENGTH);
        for (int i = 0; i < VALUES.length; i++) {
            V2I.put(VALUES[i], i);
        }
        this.LB = env.makeInt(0);
        this.UB = env.makeInt(LENGTH - 1);
        this.SIZE = env.makeInt(LENGTH);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Removes <code>value</code>from the domain of <code>this</code>. The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If <code>value</code> is out of the domain, nothing is done and the return value is <code>false</code>,</li>
     * <li>if removing <code>value</code> leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if removing <code>value</code> from the domain can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param value value to remove from the domain (int)
     * @param cause removal releaser
     * @return true if the value has been removed, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
//        records.forEachRemVal(beforeModification.set(this, EventType.REMOVE, cause));
        assert cause != null;
        if (value < VALUES[LB.get()] || value > VALUES[UB.get()]) {
            return false;
        }
        int index = V2I.get(value);
        if (index != -1) {
            if (SIZE.get() == 1) {
                if (_plugexpl) {
                    solver.getEventObserver().removeValue(this, value, cause);
                }
                //            monitors.forEachRemVal(onContradiction.set(this, EventType.REMOVE, cause));
                this.contradiction(cause, IntEventType.REMOVE, MSG_REMOVE);
            }
            IntEventType e = IntEventType.REMOVE;
            this.INDEXES.clear(index);
            this.SIZE.add(-1);
            if (reactOnRemoval) {
                delta.add(value, cause);
            }
            if (value == getLB()) {
                LB.set(INDEXES.nextSetBit(LB.get()));
                e = IntEventType.INCLOW;
            } else if (value == getUB()) {
                UB.set(INDEXES.prevSetBit(UB.get()));
                e = IntEventType.DECUPP;
            }
            assert !INDEXES.isEmpty();
            if (this.isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
            if (_plugexpl) {
                solver.getEventObserver().removeValue(this, value, cause);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeValues(IRemovals values, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        boolean hasChanged = false;
        if (nlb == olb) {
            // look for the new lb
            do {
                olb = nextValue(olb);
                nlb = values.nextValue(nlb);
            } while (olb < Integer.MAX_VALUE && oub < Integer.MAX_VALUE && nlb == olb);
            // the new lower bound is now known,  delegate to the right method
            hasChanged = updateLowerBound(olb, cause);
        } else if (nlb > oub) {
            return false;
        }
        if (nub == oub) {
            // look for the new ub
            do {
                oub = previousValue(oub);
                nub = values.previousValue(nub);
            } while (olb > Integer.MIN_VALUE && oub > Integer.MIN_VALUE && nub == oub);
            // the new upper bound is now known, delegate to the right method
            hasChanged |= updateUpperBound(oub, cause);
        } else if (nub < olb) {
            return hasChanged;
        }
        // now deal with holes
        int value = nlb;
        int to = nub;
        boolean hasRemoved = false;
        int count = SIZE.get();
        while (value <= to) {
            int index = V2I.get(value);
            if (index > -1) {
                if (count == 1) {
                    if (_plugexpl) {
                        solver.getEventObserver().removeValue(this, value, cause);
                    }
                    this.contradiction(cause, IntEventType.REMOVE, MSG_REMOVE);
                }
                count--;
                hasRemoved = true;
                INDEXES.clear(index);
                if (reactOnRemoval) {
                    delta.add(value, cause);
                }
                if (_plugexpl) {
                    solver.getEventObserver().removeValue(this, value, cause);
                }
            }
            value = values.nextValue(value);
        }
        if (hasRemoved) {
            SIZE.set(count);
            IntEventType e = IntEventType.REMOVE;
            if (count == 1) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
        }
        return hasRemoved || hasChanged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        assert cause != null;
        if (from <= getLB())
            return updateLowerBound(to + 1, cause);
        else if (getUB() <= to)
            return updateUpperBound(from - 1, cause);
        else {     // TODO: really ugly .........
            boolean anyChange = false;
            for (int v = this.nextValue(from - 1); v <= to; v = nextValue(v)) {
                anyChange |= removeValue(v, cause);
            }
            return anyChange;
        }
    }

    /**
     * Instantiates the domain of <code>this</code> to <code>value</code>. The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If the domain of <code>this</code> is already instantiated to <code>value</code>,
     * nothing is done and the return value is <code>false</code>,</li>
     * <li>If the domain of <code>this</code> is already instantiated to another value,
     * then a <code>ContradictionException</code> is thrown,</li>
     * <li>Otherwise, the domain of <code>this</code> is restricted to <code>value</code> and the observers are notified
     * and the return value is <code>true</code>.</li>
     * </ul>
     *
     * @param value instantiation value (int)
     * @param cause instantiation releaser
     * @return true if the instantiation is done, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
        assert cause != null;
        if (this.isInstantiated()) {
            int cvalue = this.getValue();
            if (value != cvalue) {
                if (_plugexpl) {
                    solver.getEventObserver().instantiateTo(this, value, cause, cvalue, cvalue);
                }
                this.contradiction(cause, IntEventType.INSTANTIATE, MSG_INST);
            }
            return false;
        } else {
            int index = V2I.get(value);
            if (index != -1) {
                if (reactOnRemoval) {
                    for (int i = INDEXES.nextSetBit(LB.get()); i >= 0; i = INDEXES.nextSetBit(i + 1)) {
                        if (i != index) {
                            delta.add(VALUES[i], cause);
                        }
                    }
                }
                int oldLB = 0;
                int oldUB = 0;
                if (_plugexpl) {
                    oldLB = getLB(); // call getter to avoid adding OFFSET..
                    oldUB = getUB();
                }

                this.INDEXES.clear();
                this.INDEXES.set(index);
                this.LB.set(index);
                this.UB.set(index);
                this.SIZE.set(1);

                if (INDEXES.isEmpty()) {
                    this.contradiction(cause, IntEventType.INSTANTIATE, MSG_EMPTY);
                }
                if (_plugexpl) {
                    solver.getEventObserver().instantiateTo(this, value, cause, oldLB, oldUB);
                }
                this.notifyPropagators(IntEventType.INSTANTIATE, cause);
                return true;
            } else {
                if (_plugexpl) {
                    solver.getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
                }
                this.contradiction(cause, IntEventType.INSTANTIATE, MSG_UNKNOWN);
                return false;
            }
        }
    }

    /**
     * Updates the lower bound of the domain of <code>this</code> to <code>value</code>.
     * The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If <code>value</code> is smaller than the lower bound of the domain, nothing is done and the return value is <code>false</code>,</li>
     * <li>if updating the lower bound to <code>value</code> leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if updating the lower bound to <code>value</code> can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param value new lower bound (included)
     * @param cause updating releaser
     * @return true if the lower bound has been updated, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getLB();
        if (old < value) {
            int oub = this.getUB();
            if (oub < value) {
                if (_plugexpl) {
                    solver.getEventObserver().updateLowerBound(this, oub + 1, old, cause);
                }
                this.contradiction(cause, IntEventType.INCLOW, MSG_LOW);
            } else {
                IntEventType e = IntEventType.INCLOW;
                int index;
                index = INDEXES.nextSetBit(LB.get());
                while (index >= 0 && VALUES[index] < value) {
                    index = INDEXES.nextSetBit(index + 1);
                }
                assert index >= 0 && VALUES[index] >= value;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = LB.get(); i >= 0 && i < index; i = INDEXES.nextSetBit(i + 1)) {
                        delta.add(VALUES[i], cause);
                    }
                }
                INDEXES.clear(LB.get(), index);
                LB.set(index);
                assert SIZE.get() > INDEXES.cardinality();
                SIZE.set(INDEXES.cardinality());
                if (isInstantiated()) {
                    e = IntEventType.INSTANTIATE;
                }
                this.notifyPropagators(e, cause);
                if (_plugexpl) {
                    solver.getEventObserver().updateLowerBound(this, value, old, cause);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the upper bound of the domain of <code>this</code> to <code>value</code>.
     * The instruction comes from <code>propagator</code>.
     * <ul>
     * <li>If <code>value</code> is greater than the upper bound of the domain, nothing is done and the return value is <code>false</code>,</li>
     * <li>if updating the upper bound to <code>value</code> leads to a dead-end (domain wipe-out),
     * a <code>ContradictionException</code> is thrown,</li>
     * <li>otherwise, if updating the upper bound to <code>value</code> can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is <code>true</code></li>
     * </ul>
     *
     * @param value new upper bound (included)
     * @param cause update releaser
     * @return true if the upper bound has been updated, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getUB();
        if (old > value) {
            int olb = this.getLB();
            if (olb > value) {
                if (_plugexpl) {
                    solver.getEventObserver().updateUpperBound(this, olb - 1, old, cause);
                }
                this.contradiction(cause, IntEventType.DECUPP, MSG_UPP);
            } else {
                IntEventType e = IntEventType.DECUPP;
                int index;
                index = INDEXES.prevSetBit(UB.get());
                while (index >= 0 && VALUES[index] > value) {
                    index = INDEXES.prevSetBit(index - 1);
                }
                assert index >= 0 && VALUES[index] <= value;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = UB.get(); i > index; i = INDEXES.prevSetBit(i - 1)) {
                        delta.add(VALUES[i], cause);
                    }
                }
                INDEXES.clear(index + 1, UB.get() + 1);
                UB.set(index);
                assert SIZE.get() > INDEXES.cardinality();
                SIZE.set(INDEXES.cardinality());
                if (isInstantiated()) {
                    e = IntEventType.INSTANTIATE;
                }
                this.notifyPropagators(e, cause);
                if (_plugexpl) {
                    solver.getEventObserver().updateUpperBound(this, value, old, cause);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInstantiated() {
        return SIZE.get() == 1;
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return isInstantiated() && contains(value);
    }

    @Override
    public boolean contains(int aValue) {
        if (aValue >= getLB() && aValue <= getUB()) {
            int i = V2I.get(aValue);
            return i > -1 && INDEXES.get(V2I.get(aValue));
        }
        return false;
    }

    /**
     * Retrieves the current value of the variable if instantiated, otherwier the lower bound.
     *
     * @return the current value (or lower bound if not yet instantiated).
     */
    @Override
    public int getValue() {
        assert isInstantiated() : name + " not instantiated";
        return getLB();
    }

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    @Override
    public int getLB() {
        assert LB.get() >= 0 && LB.get() < LENGTH;
        return VALUES[LB.get()];
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    @Override
    public int getUB() {
        assert UB.get() >= 0 && UB.get() < LENGTH;
        return VALUES[UB.get()];
    }

    @Override
    public int getDomainSize() {
        return SIZE.get();
    }

    @Override
    public int nextValue(int aValue) {
        int lb = getLB();
        if (aValue < lb) return lb;
        if (aValue >= getUB()) return Integer.MAX_VALUE;
        int i;
        i = INDEXES.nextSetBit(LB.get());
        while (i >= 0 && VALUES[i] <= aValue) {
            i = INDEXES.nextSetBit(i + 1);
        }
        return (i >= 0) ? VALUES[i] : Integer.MAX_VALUE;
    }

    @Override
    public int previousValue(int aValue) {
        int ub = getUB();
        if (aValue > ub) return ub;
        if (aValue <= getLB()) return Integer.MIN_VALUE;
        int i;
        i = INDEXES.prevSetBit(UB.get());
        while (i >= 0 && VALUES[i] >= aValue) {
            i = INDEXES.prevSetBit(i - 1);
        }
        return (i >= 0) ? VALUES[i] : Integer.MIN_VALUE;
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return true;
    }

    @Override
    public IEnumDelta getDelta() {
        return delta;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(20);
        s.append(name).append(" = ");
        if (SIZE.get() == 1) {
            s.append(this.getLB());
        } else {
            s.append('{').append(getLB());
            int nb = 5;
            for (int i = nextValue(getLB()); i < Integer.MAX_VALUE && nb > 0; i = nextValue(i)) {
                s.append(',').append(i);
                nb--;
            }
            if (nb == 0 && SIZE.get() > 6) {
                s.append("...,").append(this.getUB());
            }
            s.append('}');
        }
        return s.toString();
    }

    ////////////////////////////////////////////////////////////////
    ///// methode liees au fait qu'une variable est observable /////
    ////////////////////////////////////////////////////////////////


    @Override
    public void createDelta() {
        if (!reactOnRemoval) {
            delta = new EnumDelta(solver.getEnvironment());
            reactOnRemoval = true;
        }
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new EnumDeltaMonitor(delta, propagator);
    }

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void contradiction(ICause cause, IEventType event, String message) throws ContradictionException {
        assert cause != null;
//        records.forEachRemVal(onContradiction.set(this, event, cause));
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return VAR | INT;
    }

    @Override
    public IntVar duplicate() {
        return new BitsetArrayIntVarImpl(StringUtils.randomName(this.name), this.VALUES.clone(), isolver);
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            BitsetArrayIntVarImpl clone = new BitsetArrayIntVarImpl(this.name, this.VALUES, solver);
            identitymap.put(this, clone);
            for (int i = mIdx - 1; i >= 0; i--) {
                monitors[i].duplicate(solver, identitymap);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                int index;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    index = INDEXES.nextSetBit(LB.get());
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    index = INDEXES.prevSetBit(UB.get());
                }

                @Override
                public boolean hasNext() {
                    return index != -1;
                }

                @Override
                public boolean hasPrevious() {
                    return index != -1;
                }

                @Override
                public int next() {
                    int old = VALUES[index];
                    index = INDEXES.nextSetBit(index + 1);
                    return old;
                }

                @Override
                public int previous() {
                    int old = VALUES[index];
                    index = INDEXES.prevSetBit(index - 1);
                    return old;
                }
            };
        }
        if (bottomUp) {
            _viterator.bottomUpInit();
        } else {
            _viterator.topDownInit();
        }
        return _viterator;
    }

    @Override
    public DisposableRangeIterator getRangeIterator(boolean bottomUp) {
        if (_riterator == null || !_riterator.isReusable()) {
            _riterator = new DisposableRangeIterator() {

                int from;
                int to;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    this.from = INDEXES.nextSetBit(LB.get());
//					this.to = INDEXES.nextClearBit(from + 1) - 1;
                    this.to = from;
                    while (INDEXES.get(to + 1)
                            && (VALUES[to] == VALUES[to + 1] - 1)) {
                        to++;
                    }
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    this.to = INDEXES.prevSetBit(UB.get());
                    this.from = to;
                    while (INDEXES.get(from - 1)
                            && (VALUES[from - 1] == VALUES[from] - 1)) {
                        from--;
                    }
                }

                public boolean hasNext() {
                    return this.from != -1;
                }

                @Override
                public boolean hasPrevious() {
                    return this.to != -1;
                }

                public void next() {
                    this.from = INDEXES.nextSetBit(this.to + 1);
                    this.to = from;
                    while (to > -1 && INDEXES.get(to + 1)
                            && (VALUES[to] == VALUES[to + 1] - 1)) {
                        to++;
                    }
                }

                @Override
                public void previous() {
                    this.to = INDEXES.prevSetBit(this.from - 1);
                    this.from = to;
                    while (from > -1 && INDEXES.get(from - 1)
                            && (VALUES[from - 1] == VALUES[from] - 1)) {
                        from--;
                    }
                }

                @Override
                public int min() {
                    return VALUES[from];
                }

                @Override
                public int max() {
                    return VALUES[to];
                }
            };
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }
}
