/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.EnumDelta;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.monitor.EnumDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.solver.variables.impl.siglit.SignedLiteral;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.iterators.IntVarValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

import java.util.Iterator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class BitsetIntVarImpl extends AbstractVariable implements IntVar {

    /**
     * Set to <tt>true</tt> if this variable reacts is associated with at least one propagator which reacts
     * on value removal
     */
    private boolean reactOnRemoval = false;
    /**
     * Bitset of available values -- includes offset
     */
    private final IStateBitSet VALUES;
    /**
     * Lower bound of the current domain -- includes offset
     */
    private final IStateInt LB;
    /**
     * Upper bound of the current domain -- includes offset
     */
    private final IStateInt UB;
    /**
     * Current size of domain
     */
    private final IStateInt SIZE;
    /**
     * offset of the lower bound and the first value in the domain
     */
    private final int OFFSET;
    /**
     * number of total bits used
     */
    private final int LENGTH;
    /**
     * To iterate over removed values
     */
    private IEnumDelta delta = NoDelta.singleton;
    /**
     * To iterate over values in the domain
     */
    private DisposableValueIterator _viterator;
    /**
     * To iterate over ranges
     */
    private DisposableRangeIterator _riterator;

    /**
     * Value iterator allowing for(int i:this) loops
     */
    private IntVarValueIterator _javaIterator;

    /**
     * Signed Literal
     */
    protected SignedLiteral.Set literal;

    /**
     * Create an enumerated IntVar based on a bitset
     *
     * @param name         name of the variable
     * @param sortedValues original domain values
     * @param model        declaring model
     */
    public BitsetIntVarImpl(String name, int[] sortedValues, Model model) {
        super(name, model);
        IEnvironment env = model.getEnvironment();
        OFFSET = sortedValues[0];
        int capacity = sortedValues[sortedValues.length - 1] - OFFSET + 1;
        this.VALUES = env.makeBitSet(capacity);
        for (int sortedValue : sortedValues) {
            this.VALUES.set(sortedValue - OFFSET);
        }
        this.LB = env.makeInt(0);
        this.UB = env.makeInt(capacity - 1);
        this.SIZE = env.makeInt(VALUES.cardinality());
        LENGTH = capacity;
    }

    /**
     * Create an enumerated IntVar based on a bitset
     *
     * @param name  name of the variable
     * @param min   lower bound
     * @param max   upper bound
     * @param model declaring model
     */
    public BitsetIntVarImpl(String name, int min, int max, Model model) {
        super(name, model);
        IEnvironment env = this.model.getEnvironment();
        this.OFFSET = min;
        int capacity = max - min + 1;
        this.VALUES = env.makeBitSet(capacity);
        this.VALUES.set(0, max - min + 1);
        this.LB = env.makeInt(0);
        this.UB = env.makeInt(max - min);
        this.SIZE = env.makeInt(capacity);
        LENGTH = capacity;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Removes {@code value}from the domain of {@code this}. The instruction comes from {@code propagator}.
     * <ul>
     * <li>If {@code value} is out of the domain, nothing is done and the return value is {@code false},</li>
     * <li>if removing {@code value} leads to a dead-end (domain wipe-out),
     * a {@code ContradictionException} is thrown,</li>
     * <li>otherwise, if removing {@code value} from the domain can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is {@code true}</li>
     * </ul>
     *
     * @param value value to remove from the domain (int)
     * @param cause removal releaser
     * @return true if the value has been removed, false otherwise
     * @throws ContradictionException if the domain become empty due to this action
     */
    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int aValue = value - OFFSET;
        boolean change = aValue >= 0 && aValue <= LENGTH && VALUES.get(aValue);
        if (change) {
            model.getSolver().getEventObserver().removeValue(this, value, cause);
            if (SIZE.get() == 1) {
                this.contradiction(cause, MSG_REMOVE);
            }
            IntEventType e = IntEventType.REMOVE;
            this.VALUES.clear(aValue);
            this.SIZE.add(-1);
            if (reactOnRemoval) {
                delta.add(value, cause);
            }

            if (value == getLB()) {
                LB.set(VALUES.nextSetBit(aValue));
                e = IntEventType.INCLOW;
            } else if (value == getUB()) {
                UB.set(VALUES.prevSetBit(aValue));
                e = IntEventType.DECUPP;
            }
            assert !VALUES.isEmpty();
            if (this.isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
        }
        return change;
    }

    @Override
    public boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        if (nlb > oub || nub < olb) {
            return false;
        }
        int i;
        // look for the new lb
        while (nlb == olb && olb < Integer.MAX_VALUE) {
            i = VALUES.nextSetBit(nlb + 1 - OFFSET);
            olb = i > -1 ? i + OFFSET : Integer.MAX_VALUE;
            nlb = values.nextValue(olb - 1);
        }
        if (nlb <= nub) {
            // look for the new ub
            while (nub == oub && oub > Integer.MIN_VALUE) {
                i = VALUES.prevSetBit(nub - 1 - OFFSET);
                oub = i > -1 ? i + OFFSET : Integer.MIN_VALUE;
                nub = values.previousValue(oub + 1);
            }
        }
        // the new bounds are now known, delegate to the right method
        boolean hasChanged = updateBounds(olb, oub, cause);
        // now deal with holes
        int value = nlb;
        int to = nub;
        boolean hasRemoved = false;
        int count = SIZE.get();
        while (value <= to) {
            int aValue = value - OFFSET;
            if (aValue >= 0 && aValue <= LENGTH && VALUES.get(aValue)) {
                model.getSolver().getEventObserver().removeValue(this, value, cause);
                if (count == 1) {
                    this.contradiction(cause, MSG_REMOVE);
                }
                count--;
                hasRemoved = true;
                VALUES.clear(aValue);
                if (reactOnRemoval) {
                    delta.add(value, cause);
                }
            }
            value = values.nextValue(value);
        }
        if (hasRemoved) {
            notifyRemovals(count, cause);
        }
        return hasRemoved || hasChanged;
    }

    private void notifyRemovals(int count, ICause cause) throws ContradictionException {
        SIZE.set(count);
        IntEventType e = IntEventType.REMOVE;
        if (count == 1) {
            e = IntEventType.INSTANTIATE;
        }
        this.notifyPropagators(e, cause);
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        int i;
        // look for the new lb
        while (nlb != olb && olb < Integer.MAX_VALUE && nlb < Integer.MAX_VALUE) {
            i = VALUES.nextSetBit(nlb - OFFSET);
            olb = i > -1 ? i + OFFSET : Integer.MAX_VALUE;
            nlb = values.nextValue(olb - 1);
        }
        // look for the new ub
        if (nlb <= nub) {
            while (nub != oub && oub > Integer.MIN_VALUE && nub > Integer.MIN_VALUE) {
                i = VALUES.prevSetBit(nub - OFFSET);
                oub = i > -1 ? i + OFFSET : Integer.MIN_VALUE;
                nub = values.previousValue(oub + 1);
            }
        }
        // the new bounds are now known, delegate to the right method
        boolean hasChanged = updateBounds(nlb, nub, cause);
        // now deal with holes
        boolean hasRemoved = false;
        int count = SIZE.get();
        int value;
        int m0 = values.nextValueOut(nlb);
        while (m0 < nub) {
            int m1 = values.nextValue(m0) - OFFSET;
            // iterate over the values in the domain, remove the ones that are not in values
            int f = VALUES.nextSetBit(m0 - OFFSET);
            int t = VALUES.nextClearBit(f);
            int k = f;
            while (k < m1) {
                if (k < t) {
                    value = k + OFFSET;
                    assert !values.contains(value);
                    model.getSolver().getEventObserver().removeValue(this, value, cause);
                    if (count == 1) {
                        this.contradiction(cause, MSG_REMOVE);
                    }
                    count--;
                    hasRemoved = true;
                    if (reactOnRemoval) {
                        delta.add(value, cause);
                    }
                    k++;
                } else {
                    VALUES.clear(f, t);
                    k = f = VALUES.nextSetBit(t);
                    t = VALUES.nextClearBit(f);
                }
            }
            if (f < m1) VALUES.clear(f, m1);
            m0 = values.nextValueOut(m1 + OFFSET);
        }

        if (hasRemoved) {
            notifyRemovals(count, cause);
        }
        return hasRemoved || hasChanged;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws
            ContradictionException {
        assert cause != null;
        if (from <= getLB())
            return updateLowerBound(to + 1, cause);
        else if (getUB() <= to)
            return updateUpperBound(from - 1, cause);
        else {
            boolean anyChange = false;
            int i = VALUES.nextSetBit(from - OFFSET);
            to -= OFFSET;
            int count = SIZE.get();
            // the iteration is mandatory for delta and observers
            for (; i > -1 && i <= to; i = VALUES.nextSetBit(i + 1)) {
                int aValue = i + OFFSET;
                anyChange = true;
                count--;
                this.VALUES.clear(i);
                if (reactOnRemoval) {
                    delta.add(aValue, cause);
                }
                model.getSolver().getEventObserver().removeValue(this, aValue, cause);
            }
            if (anyChange) {
                SIZE.set(count);
                this.notifyPropagators(IntEventType.REMOVE, cause);
            }
            return anyChange;
        }
    }

    /**
     * Instantiates the domain of {@code this} to {@code value}. The instruction comes from {@code propagator}.
     * <ul>
     * <li>If the domain of {@code this} is already instantiated to {@code value},
     * nothing is done and the return value is {@code false},</li>
     * <li>If the domain of {@code this} is already instantiated to another value,
     * then a {@code ContradictionException} is thrown,</li>
     * <li>Otherwise, the domain of {@code this} is restricted to {@code value} and the observers are notified
     * and the return value is {@code true}.</li>
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
        if (!contains(value)) {
            model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
            this.contradiction(cause, MSG_INST);
        } else if (!isInstantiated()) {
            model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
            int aValue = value - OFFSET;
            if (reactOnRemoval) {
                int i = VALUES.nextSetBit(this.LB.get());
                for (; i < aValue; i = VALUES.nextSetBit(i + 1)) {
                    delta.add(i + OFFSET, cause);
                }
                i = VALUES.nextSetBit(aValue + 1);
                for (; i >= 0; i = VALUES.nextSetBit(i + 1)) {
                    delta.add(i + OFFSET, cause);
                }
            }
            this.VALUES.clear();
            this.VALUES.set(aValue);
            this.LB.set(aValue);
            this.UB.set(aValue);
            this.SIZE.set(1);
            assert !VALUES.isEmpty();
            this.notifyPropagators(IntEventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    /**
     * Updates the lower bound of the domain of {@code this} to {@code value}.
     * The instruction comes from {@code propagator}.
     * <ul>
     * <li>If {@code value} is smaller than the lower bound of the domain, nothing is done and the return value is {@code false},</li>
     * <li>if updating the lower bound to {@code value} leads to a dead-end (domain wipe-out),
     * a {@code ContradictionException} is thrown,</li>
     * <li>otherwise, if updating the lower bound to {@code value} can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is {@code true}</li>
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
            model.getSolver().getEventObserver().updateLowerBound(this, value, old, cause);
            if (oub < value) {
                this.contradiction(cause, MSG_LOW);
            } else {
                IntEventType e = IntEventType.INCLOW;
                int aValue = value - OFFSET;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = old - OFFSET; i < aValue; i = VALUES.nextSetBit(i + 1)) {
                        delta.add(i + OFFSET, cause);
                    }
                }
                VALUES.clear(old - OFFSET, aValue);
                LB.set(VALUES.nextSetBit(aValue));
                assert SIZE.get() > VALUES.cardinality();
                SIZE.set(VALUES.cardinality());
                if (isInstantiated()) {
                    e = IntEventType.INSTANTIATE;
                }

                this.notifyPropagators(e, cause);
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the upper bound of the domain of {@code this} to {@code value}.
     * The instruction comes from {@code propagator}.
     * <ul>
     * <li>If {@code value} is greater than the upper bound of the domain, nothing is done and the return value is {@code false},</li>
     * <li>if updating the upper bound to {@code value} leads to a dead-end (domain wipe-out),
     * a {@code ContradictionException} is thrown,</li>
     * <li>otherwise, if updating the upper bound to {@code value} can be done safely,
     * the event type is created (the original event can be promoted) and observers are notified
     * and the return value is {@code true}</li>
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
        int oub = this.getUB();
        if (oub > value) {
            int olb = this.getLB();
            model.getSolver().getEventObserver().updateUpperBound(this, value, oub, cause);
            if (olb > value) {
                this.contradiction(cause, MSG_UPP);
            } else {
                IntEventType e = IntEventType.DECUPP;
                int aValue = value - OFFSET;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = oub - OFFSET; i > aValue; i = VALUES.prevSetBit(i - 1)) {
                        delta.add(i + OFFSET, cause);
                    }
                }
                VALUES.clear(aValue + 1, oub - OFFSET + 1);
                UB.set(VALUES.prevSetBit(aValue));
                assert SIZE.get() > VALUES.cardinality();
                SIZE.set(VALUES.cardinality());
                if (isInstantiated()) {
                    e = IntEventType.INSTANTIATE;
                }
                this.notifyPropagators(e, cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = this.getLB();
        int oub = this.getUB();
        boolean update = false;
        if (olb < lb || oub > ub) {
            IntEventType e = null;
            if (oub < lb) {
                model.getSolver().getEventObserver().updateLowerBound(this, lb, olb, cause);
                this.contradiction(cause, MSG_LOW);
            } else if (olb < lb) {
                model.getSolver().getEventObserver().updateLowerBound(this, lb, olb, cause);
                e = IntEventType.INCLOW;
                int aLB = lb - OFFSET;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = olb - OFFSET; i < aLB; i = VALUES.nextSetBit(i + 1)) {
                        delta.add(i + OFFSET, cause);
                    }
                }
                VALUES.clear(olb - OFFSET, aLB);
                olb = VALUES.nextSetBit(aLB); // olb is used as a temporary variable
                LB.set(olb);
                SIZE.set(VALUES.cardinality());
                olb += OFFSET; // required because we will treat upper bound just after
            }
            if (olb > ub) {
                model.getSolver().getEventObserver().updateUpperBound(this, ub, oub, cause);
                this.contradiction(cause, MSG_UPP);
            } else if (oub > ub) {
                model.getSolver().getEventObserver().updateUpperBound(this, ub, oub, cause);
                e = e == null ? IntEventType.DECUPP : IntEventType.BOUND;
                int aUB = ub - OFFSET;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = oub - OFFSET; i > aUB; i = VALUES.prevSetBit(i - 1)) {
                        delta.add(i + OFFSET, cause);
                    }
                }
                VALUES.clear(aUB + 1, oub - OFFSET + 1);
                UB.set(VALUES.prevSetBit(aUB));
                SIZE.set(VALUES.cardinality());
            }
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
            update = true;
        }
        return update;
    }

    @Override
    public boolean isInstantiated() {
        return SIZE.get() == 1;
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return isInstantiated() && getLB() == value;
    }

    @Override
    public boolean contains(int aValue) {
        aValue -= OFFSET;
        return LB.get() <= aValue && aValue <= UB.get() && this.VALUES.get(aValue);
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
        return this.LB.get() + OFFSET;
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    @Override
    public int getUB() {
        return this.UB.get() + OFFSET;
    }

    @Override
    public int getDomainSize() {
        return SIZE.get();
    }

    @Override
    public int getRange() {
        return getUB() - getLB() + 1;
    }

    @Override
    public int nextValue(int aValue) {
        aValue -= OFFSET;
        int lb = LB.get();
        if (aValue < 0 || aValue < lb) return lb + OFFSET;
        aValue = VALUES.nextSetBit(aValue + 1);
        if (aValue > -1) return aValue + OFFSET;
        return Integer.MAX_VALUE;
    }

    @Override
    public int nextValueOut(int aValue) {
        int lb = getLB();
        int ub = getUB();
        if (lb - 1 <= aValue && aValue <= ub) {
            return VALUES.nextClearBit(aValue - OFFSET + 1) + OFFSET;
        }
        return aValue + 1;
    }

    @Override
    public int previousValue(int aValue) {
        aValue -= OFFSET;
        int ub = UB.get();
        if (aValue > ub) return ub + OFFSET;
        aValue = VALUES.prevSetBit(aValue - 1);
        if (aValue > -1) return aValue + OFFSET;
        return Integer.MIN_VALUE;
    }

    @Override
    public int previousValueOut(int aValue) {
        int lb = getLB();
        int ub = getUB();
        if (lb <= aValue && aValue <= ub + 1) {
            return VALUES.prevClearBit(aValue - OFFSET - 1) + OFFSET;
        }
        return aValue - 1;
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
            int v = getLB(), w;
            s.append('{').append(v);
            w = nextValueOut(v);
            if (v < w - 1) s.append("..").append(w - 1);
            v = nextValue(w);
            while (v < Integer.MAX_VALUE) {
                s.append(",").append(v);
                w = nextValueOut(v);
                if (v < w - 1) s.append("..").append(w - 1);
                v = nextValue(w);
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
            delta = new EnumDelta(model.getEnvironment());
            reactOnRemoval = true;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new EnumDeltaMonitor(delta, propagator);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getTypeAndKind() {
        return VAR | INT;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new IntEvtScheduler();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
            _viterator = new DisposableValueIterator() {

                /**
                 * Current value
                 */
                int value;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    this.value = LB.get();
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    this.value = UB.get();
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
                    this.value = VALUES.nextSetBit(this.value + 1);
                    return old + OFFSET;
                }

                @Override
                public int previous() {
                    int old = this.value;
                    this.value = VALUES.prevSetBit(this.value - 1);
                    return old + OFFSET;
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
        if (_riterator == null || _riterator.isNotReusable()) {
            _riterator = new DisposableRangeIterator() {

                /**
                 * Lower bound of the current range
                 */
                int from;
                /**
                 * Upper bound of the current range
                 */
                int to;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    this.from = VALUES.nextSetBit(0);
                    this.to = VALUES.nextClearBit(from + 1) - 1;
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    this.to = VALUES.prevSetBit(VALUES.size() - 1);
                    this.from = VALUES.prevClearBit(to) + 1;
                }

                public boolean hasNext() {
                    return this.from != -1;
                }

                @Override
                public boolean hasPrevious() {
                    return this.to != -1;
                }

                public void next() {
                    this.from = VALUES.nextSetBit(this.to + 1);
                    this.to = VALUES.nextClearBit(this.from) - 1;
                }

                @Override
                public void previous() {
                    this.to = VALUES.prevSetBit(this.from - 1);
                    this.from = VALUES.prevClearBit(this.to) + 1;
                }

                @Override
                public int min() {
                    return from + OFFSET;
                }

                @Override
                public int max() {
                    return to + OFFSET;
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

    @Override
    public Iterator<Integer> iterator() {
        if (_javaIterator == null) {
            _javaIterator = new IntVarValueIterator(this);
        }
        _javaIterator.reset();
        return _javaIterator;
    }

    @Override
    public void createLit(IntIterableRangeSet rootDomain) {
        if (this.literal != null) {
            throw new IllegalStateException("createLit(Implications) called twice");
        }
        this.literal = new SignedLiteral.Set(rootDomain);
    }

    @Override
    public SignedLiteral getLit() {
        if (this.literal == null) {
            throw new NullPointerException("getLit() called on null, a call to createLit(Implications) is required");
        }
        return this.literal;
    }
}
