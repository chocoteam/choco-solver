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

import gnu.trove.map.hash.TIntIntHashMap;
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
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Iterator;

/**
 * <br/>IntVar implementation for quite small domains bit with very distant values e.g. {-51900,42,235923}
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 14/05/2013
 */
public final class BitsetArrayIntVarImpl extends AbstractVariable implements IntVar {

    /**
     * Valuated to <tt>true</tt> when removed values are stored.
     */
    private boolean reactOnRemoval = false;

    /**
     * Array of domain values
     */
    private final int[] VALUES;

    /**
     * Value to index in {@link #VALUES} mapping
     */
    private final TIntIntHashMap V2I;
    /**
     * Indices of valid values
     */
    private final IStateBitSet INDICES;
    /**
     * Lower bound of the current domain
     */
    private final IStateInt LB;
    /**
     * Upper bound of the current domain
     */
    private final IStateInt UB;
    /**
     * Size of the current domain
     */
    private final IStateInt SIZE;

    /**
     * offset of the lower bound and the first value in the domain
     */
    private final int LENGTH;

    /**
     * Delta object to store removed values
     */
    private IEnumDelta delta = NoDelta.singleton;

    /**
     * Disposable values iterator
     */
    private DisposableValueIterator _viterator;

    /**
     * Disposable ranges iterator
     */
    private DisposableRangeIterator _riterator;

    /**
     * Value iterator allowing for(int i:this) loops
     */
    private IntVarValueIterator _javaIterator;

    /**
     * Signed Literal
     */
    private SignedLiteral.Set literal;

    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an {@link IntVar} based on an array of non-consecutive but ordered values.
     * Non-consecutive condition is the main reason which motivates this class.
     *
     * @param name         name of the variable
     * @param sortedValues domain values
     * @param model        the model to declare this variable in
     */
    public BitsetArrayIntVarImpl(String name, int[] sortedValues, Model model) {
        super(name, model);
        IEnvironment env = this.model.getEnvironment();
        this.LENGTH = sortedValues.length;
        this.VALUES = sortedValues;
        this.V2I = new TIntIntHashMap(VALUES.length, .5f, Integer.MIN_VALUE, -1);
        this.INDICES = env.makeBitSet(LENGTH);
        this.INDICES.set(0, LENGTH);
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
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
//        records.forEachRemVal(beforeModification.set(this, EventType.REMOVE, cause));
        assert cause != null;
        if (value < VALUES[LB.get()] || value > VALUES[UB.get()]) {
            return false;
        }
        int index = V2I.get(value);
        if (index > -1 && this.INDICES.get(index)) {
            if (SIZE.get() == 1) {
                model.getSolver().getEventObserver().removeValue(this, value, cause);
                this.contradiction(cause, MSG_REMOVE);
            }
            IntEventType e = IntEventType.REMOVE;
            this.INDICES.clear(index);
            this.SIZE.add(-1);
            if (reactOnRemoval) {
                delta.add(value, cause);
            }
            if (value == getLB()) {
                LB.set(INDICES.nextSetBit(LB.get()));
                e = IntEventType.INCLOW;
            } else if (value == getUB()) {
                UB.set(INDICES.prevSetBit(UB.get()));
                e = IntEventType.DECUPP;
            }
            assert !INDICES.isEmpty();
            if (this.isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            model.getSolver().getEventObserver().removeValue(this, value, cause);
            this.notifyPropagators(e, cause);
            return true;
        } else {
            return false;
        }
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
            i = INDICES.nextSetBit(V2I.get(olb) + 1);
            olb = i > -1 ? VALUES[i] : Integer.MAX_VALUE;
            nlb = values.nextValue(olb - 1);
        }
        if (nlb <= nub) {
            // look for the new ub
            while (nub == oub && oub > Integer.MIN_VALUE) {
                i = INDICES.prevSetBit(V2I.get(oub) - 1);
                oub = i > -1 ? VALUES[i] : Integer.MIN_VALUE;
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
            int index = V2I.get(value);
            if (index > -1 && this.INDICES.get(index)) {
                model.getSolver().getEventObserver().removeValue(this, value, cause);
                if (count == 1) {
                    this.contradiction(cause, MSG_REMOVE);
                }
                count--;
                hasRemoved = true;
                INDICES.clear(index);
                if (reactOnRemoval) {
                    delta.add(value, cause);
                }
            }
            value = values.nextValue(value);
        }
        if (hasRemoved) {
            notifyOnRemovals(count, cause);
        }
        return hasRemoved || hasChanged;
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        int i;
        // look for the new lb
        while (nlb != olb && olb < Integer.MAX_VALUE && nlb < Integer.MAX_VALUE) {
            i = INDICES.nextSetBit(V2I.get(olb) + 1);
            olb = i > -1 ? VALUES[i] : Integer.MAX_VALUE;
            nlb = values.nextValue(olb - 1);
        }
        if (nlb <= nub) {
            // look for the new ub
            while (nub != oub && olb > Integer.MIN_VALUE && oub > Integer.MIN_VALUE) {
                i = INDICES.prevSetBit(V2I.get(oub) - 1);
                oub = i > -1 ? VALUES[i] : Integer.MIN_VALUE;
                nub = values.previousValue(oub + 1);
            }
        }
        // the new bounds are now known, delegate to the right method
        boolean hasChanged = updateBounds(nlb, nub, cause);
        // now deal with holes
        int to = UB.get() - 1;
        boolean hasRemoved = false;
        int count = SIZE.get();
        int value;
        // iterate over the values in the domain, remove the ones that are not in values
        for (int index = INDICES.nextSetBit(LB.get() + 1); index > -1 && index <= to; index = INDICES.nextSetBit(index + 1)) {
            value = VALUES[index];
            if (!values.contains(value)) {
                model.getSolver().getEventObserver().removeValue(this, value, cause);
                if (count == 1) {
                    this.contradiction(cause, MSG_REMOVE);
                }
                count--;
                hasRemoved = true;
                INDICES.clear(index);
                if (reactOnRemoval) {
                    delta.add(value, cause);
                }
            }
        }
        if (hasRemoved) {
            notifyOnRemovals(count, cause);
        }
        return hasRemoved || hasChanged;
    }


    private void notifyOnRemovals(int count, ICause cause) throws ContradictionException {
        SIZE.set(count);
        IntEventType e = IntEventType.REMOVE;
        if (count == 1) {
            e = IntEventType.INSTANTIATE;
        }
        this.notifyPropagators(e, cause);
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        assert cause != null;
        if (from <= getLB())
            return updateLowerBound(to + 1, cause);
        else if (getUB() <= to)
            return updateUpperBound(from - 1, cause);
        else {
            boolean anyChange = false;
            int count = SIZE.get(), value;
            int i = V2I.get(nextValue(from - 1));
            int _to = V2I.get(previousValue(to + 1));
            // the iteration is mandatory for delta and observers
            for (; i > -1 && i <= _to; i = INDICES.nextSetBit(i + 1)) {
                value = VALUES[i];
                anyChange = true;
                count--;
                this.INDICES.clear(i);
                if (reactOnRemoval) {
                    delta.add(value, cause);
                }
                model.getSolver().getEventObserver().removeValue(this, value, cause);
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
            int index = V2I.get(value);
            assert index > -1 && this.INDICES.get(index);
            if (reactOnRemoval) {
                for (int i = INDICES.nextSetBit(LB.get()); i >= 0; i = INDICES.nextSetBit(i + 1)) {
                    if (i != index) {
                        delta.add(VALUES[i], cause);
                    }
                }
            }
            this.INDICES.clear();
            this.INDICES.set(index);
            this.LB.set(index);
            this.UB.set(index);
            this.SIZE.set(1);
            assert !INDICES.isEmpty();
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
        int lb = LB.get();
        int old = VALUES[lb];
        if (old < value) {
            model.getSolver().getEventObserver().updateLowerBound(this, value, old, cause);
            int ub = UB.get();
            int oub = VALUES[ub];
            if (oub < value) {
                this.contradiction(cause, MSG_LOW);
            } else {
                IntEventType e = IntEventType.INCLOW;
                int index = indexOfLowerBound(value, lb, ub);
                assert index >= 0 && VALUES[index] >= value;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = lb; i >= 0 && i < index; i = INDICES.nextSetBit(i + 1)) {
                        delta.add(VALUES[i], cause);
                    }
                }
                INDICES.clear(lb, index);
                LB.set(index);
                assert SIZE.get() > INDICES.cardinality();
                SIZE.set(INDICES.cardinality());
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
        int ub = UB.get();
        int old = VALUES[ub];
        if (old > value) {
            model.getSolver().getEventObserver().updateUpperBound(this, value, old, cause);
            int lb = LB.get();
            int olb = VALUES[lb];
            if (olb > value) {
                this.contradiction(cause, MSG_UPP);
            } else {
                IntEventType e = IntEventType.DECUPP;
                int index = indexOfUpperBound(value, lb, ub);
                assert index >= 0 && VALUES[index] <= value;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = ub; i >= 0 && i > index; i = INDICES.prevSetBit(i - 1)) {
                        delta.add(VALUES[i], cause);
                    }
                }
                INDICES.clear(index + 1, ub + 1);
                UB.set(index);
                assert SIZE.get() > INDICES.cardinality();
                SIZE.set(INDICES.cardinality());
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
    public boolean updateBounds(int aLB, int aUB, ICause cause) throws ContradictionException {
        assert cause != null;
        int lb = LB.get();
        int ub = UB.get();
        int olb = VALUES[lb];
        int oub = VALUES[ub];
        boolean update = false;
        if (olb < aLB || oub > aUB) {
            IntEventType e = null;
            int index, b;
            if (oub < aLB) {
                model.getSolver().getEventObserver().updateLowerBound(this, aLB, olb, cause);
                this.contradiction(cause, MSG_LOW);
            } else if (olb < aLB) {
                model.getSolver().getEventObserver().updateLowerBound(this, aLB, olb, cause);
                e = IntEventType.INCLOW;
                b = LB.get();
                index = indexOfLowerBound(aLB, lb, ub);
                assert index >= 0 && VALUES[index] >= aLB;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = b; i >= 0 && i < index; i = INDICES.nextSetBit(i + 1)) {
                        delta.add(VALUES[i], cause);
                    }
                }
                INDICES.clear(b, index);
                LB.set(index);
                SIZE.set(INDICES.cardinality());
                olb = VALUES[index];
            }
            if (olb > aUB) {
                model.getSolver().getEventObserver().updateUpperBound(this, aUB, oub, cause);
                this.contradiction(cause, MSG_UPP);
            } else if (oub > aUB) {
                model.getSolver().getEventObserver().updateUpperBound(this, aUB, oub, cause);
                e = e == null ? IntEventType.DECUPP : IntEventType.BOUND;
                b = UB.get();
                index = indexOfUpperBound(aUB, lb, ub);
                assert index >= 0 && VALUES[index] <= aUB;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = b; i >= 0 && i > index; i = INDICES.prevSetBit(i - 1)) {
                        delta.add(VALUES[i], cause);
                    }
                }
                INDICES.clear(index + 1, b + 1);
                UB.set(index);
                SIZE.set(INDICES.cardinality());
            }
            if (isInstantiated()) {
                e = IntEventType.INSTANTIATE;
            }
            this.notifyPropagators(e, cause);
            update = true;
        }
        return update;
    }

    private int indexOfLowerBound(int aLB, int lb, int ub) {
        int index = V2I.get(aLB); // if aValue is known
        if (index == -1 || !INDICES.get(index)) {
            //otherwise, a dichotomic search of the closest value greater than key
            index = ArrayUtils.binarySearchInc(VALUES, lb, ub + 1, aLB, true);
            if (index < lb || index > ub) {
                index = -1;
            } else {
                index = INDICES.nextSetBit(index);
            }
        }
        return index;
    }

    private int indexOfUpperBound(int value, int lb, int ub) {
        int index = V2I.get(value);// if aValue is known
        if (index == -1 || !INDICES.get(index)) {
            //otherwise, a dichotomic search of the closest value smaller than key
            index = ArrayUtils.binarySearchInc(VALUES, lb, ub + 1, value, false);
            if (index < lb || index > ub) {
                index = -1;
            } else {
                index = INDICES.prevSetBit(index);
            }
        }
        return index;
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
        if (aValue >= getLB() && aValue <= getUB()) {
            int i = V2I.get(aValue);
            return i > -1 && INDICES.get(V2I.get(aValue));
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
    public int getRange() {
        return getUB() - getLB() + 1;
    }

    @Override
    public int nextValue(int aValue) {
        int lb = LB.get();
        if (aValue < VALUES[lb]) return VALUES[lb];
        int ub = UB.get();
        if (aValue >= VALUES[ub]) return Integer.MAX_VALUE;
        int i = V2I.get(aValue); // if aValue is known
        if (i > -1) {
            i = INDICES.nextSetBit(i + 1);
        } else {
            //otherwise, a dichotomic search of the closest value greater than key
            i = ArrayUtils.binarySearchInc(VALUES, lb, ub + 1, aValue, true);
            if (i < lb || i > ub) {
                i = -1;
            } else {
                i = INDICES.nextSetBit(i);
            }
        }
        return (i >= 0) ? VALUES[i] : Integer.MAX_VALUE;
    }

    @Override
    public int nextValueOut(int aValue) {
        int lb = LB.get();
        int ub = UB.get();
        if (VALUES[lb] - 1 <= aValue && aValue <= VALUES[ub]) {
            int i = V2I.get(aValue); // if aValue is known
            if (i == -1) {
                i = ArrayUtils.binarySearchInc(VALUES, lb, ub + 1, aValue, true);
            }
            while (i < VALUES.length && VALUES[i] == aValue + 1 && INDICES.get(i)) {
                aValue = VALUES[i];
                i++;
            }
        }
        return aValue + 1;
    }

    @Override
    public int previousValue(int aValue) {
        int ub = UB.get();
        if (aValue > VALUES[ub]) return VALUES[ub];
        int lb = LB.get();
        if (aValue <= VALUES[lb]) return Integer.MIN_VALUE;
        int i = V2I.get(aValue);// if aValue is known
        if (i > -1) {
            i = INDICES.prevSetBit(i - 1);
        } else {
            //otherwise, a dichotomic search of the closest value smaller than key
            i = ArrayUtils.binarySearchInc(VALUES, lb, ub + 1, aValue, false);
            if (i < lb || i > ub) {
                i = -1;
            } else {
                i = INDICES.prevSetBit(i);
            }
        }
        return (i >= 0) ? VALUES[i] : Integer.MIN_VALUE;
    }

    @Override
    public int previousValueOut(int aValue) {
        int lb = LB.get();
        int ub = UB.get();
        if (VALUES[lb] <= aValue && aValue <= VALUES[ub] + 1) {
            int i = V2I.get(aValue); // if aValue is known
            if (i == -1) {
                i = ArrayUtils.binarySearchInc(VALUES, lb, ub + 1, aValue, true) - 1;
            }
            while (i > -1 && VALUES[i] == aValue - 1 && INDICES.get(i)) {
                aValue = VALUES[i];
                i--;
            }
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
            delta = new EnumDelta(model.getEnvironment());
            reactOnRemoval = true;
        }
    }

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
                 * Current position of the iterator
                 */
                int index;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    index = INDICES.nextSetBit(LB.get());
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    index = INDICES.prevSetBit(UB.get());
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
                    index = INDICES.nextSetBit(index + 1);
                    return old;
                }

                @Override
                public int previous() {
                    int old = VALUES[index];
                    index = INDICES.prevSetBit(index - 1);
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
        if (_riterator == null || _riterator.isNotReusable()) {
            _riterator = new DisposableRangeIterator() {

                /**
                 * Current range starting point
                 */
                int from;
                /**
                 * Current range ending point
                 */
                int to;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    this.from = INDICES.nextSetBit(LB.get());
//					this.to = INDICES.nextClearBit(from + 1) - 1;
                    this.to = from;
                    while (INDICES.get(to + 1)
                            && (VALUES[to] == VALUES[to + 1] - 1)) {
                        to++;
                    }
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    this.to = INDICES.prevSetBit(UB.get());
                    this.from = to;
                    while (INDICES.get(from - 1)
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
                    this.from = INDICES.nextSetBit(this.to + 1);
                    this.to = from;
                    while (to > -1 && INDICES.get(to + 1)
                            && (VALUES[to] == VALUES[to + 1] - 1)) {
                        to++;
                    }
                }

                @Override
                public void previous() {
                    this.to = INDICES.prevSetBit(this.from - 1);
                    this.from = to;
                    while (from > -1 && INDICES.get(from - 1)
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
