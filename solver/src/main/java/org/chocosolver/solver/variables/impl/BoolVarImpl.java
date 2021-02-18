/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.memory.structure.IOperation;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.OneValueDelta;
import org.chocosolver.solver.variables.delta.monitor.OneValueDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.BoolEvtScheduler;
import org.chocosolver.solver.variables.impl.siglit.SignedLiteral;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.*;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

import java.util.Iterator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public class BoolVarImpl extends AbstractVariable implements BoolVar {

    /**
     * indicate the value of the domain : false = 0, true = 1, undef =  2
     */
    private int mValue;
    /**
     * To roll back mValue to its initial value
     */
    private IOperation status = () -> mValue = kUNDEF;
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
     * Set to <tt>true</tt> if this variable reacts is associated with at least one propagator which reacts
     * on value removal
     */
    private boolean reactOnRemoval = false;

    /**
     * Associate boolean variable expressing not(this)
     */
    private BoolVar not;
    /**
     * For boolean expression purpose
     */
    private boolean isNot = false;

    /**
     * Signed Literal
     */
    private SignedLiteral.Boolean literal = null;

    /**
     * Create a BoolVar {0,1} or {true, false}
     *
     * @param name  name of the variable
     * @param model declaring model
     */
    public BoolVarImpl(String name, Model model) {
        super(name, model);
        mValue = kUNDEF;
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
        if (value == kFALSE)
            return instantiateTo(kTRUE, cause);
        else if (value == kTRUE)
            return instantiateTo(kFALSE, cause);
        return false;
    }

    @Override
    public boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (values.contains(kFALSE)) {
            hasChanged = instantiateTo(kTRUE, cause);
        }
        if (values.contains(kTRUE)) {
            hasChanged = instantiateTo(kFALSE, cause);
        }
        return hasChanged;
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (!values.contains(kFALSE)) {
            hasChanged = instantiateTo(kTRUE, cause);
        }
        if (!values.contains(kTRUE)) {
            hasChanged = instantiateTo(kFALSE, cause);
        }
        return hasChanged;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (from <= to && from <= 1 && to >= 0) {
            if (from == kTRUE) {
                hasChanged = instantiateTo(kFALSE, cause);
            } else if (to == kFALSE) {
                hasChanged = instantiateTo(kTRUE, cause);
            } else {
                model.getSolver().getEventObserver().instantiateTo(this, 2, cause, kFALSE, kTRUE);
                this.contradiction(cause, MSG_UNKNOWN);
            }
        }
        return hasChanged;
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
        if ((mValue < kUNDEF && mValue != value) || (value < kFALSE || value > kTRUE)) {
            model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
            this.contradiction(cause, MSG_INST);
        } else if (mValue == kUNDEF) {
            IntEventType e = IntEventType.INSTANTIATE;
            this.getModel().getEnvironment().save(status);
            if (reactOnRemoval) {
                delta.add(kTRUE - value, cause);
            }
            mValue = value;
            model.getSolver().getEventObserver().instantiateTo(this, value, cause, kFALSE, kTRUE);
            this.notifyPropagators(e, cause);
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
        return value > kFALSE && instantiateTo(value, cause);
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
        return value < kTRUE && instantiateTo(value, cause);
    }

    @Override
    public boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (lb > kTRUE || ub < kFALSE) {
            model.getSolver().getEventObserver().instantiateTo(this, 2, cause, kFALSE, kTRUE);
            this.contradiction(cause, MSG_UNKNOWN);
        } else {
            if (lb == kTRUE) {
                hasChanged = instantiateTo(kTRUE, cause);
            } else if (ub == kFALSE) {
                hasChanged = instantiateTo(kFALSE, cause);
            }
        }
        return hasChanged;
    }

    @Override
    public boolean setToTrue(ICause cause) throws ContradictionException {
        assert cause != null;
        return instantiateTo(kTRUE, cause);
    }

    @Override
    public boolean setToFalse(ICause cause) throws ContradictionException {
        assert cause != null;
        return instantiateTo(kFALSE, cause);
    }

    @Override
    public boolean isInstantiated() {
        return mValue < kUNDEF;
    }

    @Override
    public boolean isInstantiatedTo(int aValue) {
        return isInstantiated() && mValue == aValue;
    }

    @Override
    public boolean contains(int aValue) {
        if (isInstantiated()) {
            return mValue == aValue;
        }
        return aValue == kFALSE || aValue == kTRUE;
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

    @Override
    public ESat getBooleanValue() {
        if (isInstantiated()) {
            return ESat.eval(getLB() != kFALSE);
        }
        return ESat.UNDEFINED;
    }

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    @Override
    public int getLB() {
        if (isInstantiated()) {
            return mValue;
        }
        return kFALSE;
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    @Override
    public int getUB() {
        if (isInstantiated()) {
            return mValue;
        }
        return kTRUE;
    }

    @Override
    public int getDomainSize() {
        return (isInstantiated() ? 1 : 2);
    }

    @Override
    public int getRange() {
        return getDomainSize();
    }

    @Override
    public int nextValue(int v) {
        if (isInstantiated()) {
            final int val = mValue;
            return (val > v) ? val : Integer.MAX_VALUE;
        } else {
            if (v < kFALSE) return kFALSE;
            if (v == kFALSE) return kTRUE;
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int nextValueOut(int v) {
        int lb = 0, ub = 1;
        if (isInstantiated()) { // if this is instantiated
            lb = ub = mValue;
        }
        if (lb - 1 <= v && v <= ub) {
            return ub + 1;
        } else {
            return v + 1;
        }
    }

    @Override
    public int previousValue(int v) {
        if (v > getUB()) return getUB();
        if (v > getLB()) return getLB();
        return Integer.MIN_VALUE;
    }

    @Override
    public int previousValueOut(int v) {
        int lb = 0, ub = 1;
        if (isInstantiated()) { // if this is instantiated
            lb = ub = mValue;
        }
        if (lb <= v && v <= ub + 1) {
            return lb - 1;
        } else {
            return v - 1;
        }
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
        if (isInstantiated()) {
            return this.name + " = " + mValue;
        } else {
            return this.name + " = " + "[0,1]";
        }
    }

    ////////////////////////////////////////////////////////////////
    ///// methode liees au fait qu'une variable est observable /////
    ////////////////////////////////////////////////////////////////

    @Override
    public void createDelta() {
        if (!reactOnRemoval) {
            delta = new OneValueDelta(model.getEnvironment());
            reactOnRemoval = true;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new OneValueDeltaMonitor(delta, propagator);
    }

    @Override
    public int getTypeAndKind() {
        return VAR | BOOL;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new BoolEvtScheduler();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
            _viterator = new DisposableValueBoundIterator(this);
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
            _riterator = new DisposableRangeBoundIterator(this);
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
    public void _setNot(BoolVar neg) {
        this.not = neg;
    }

    @Override
    public BoolVar not() {
        if (!hasNot()) {
            not = model.boolNotView(this);
            not._setNot(this);
        }
        return not;
    }

    @Override
    public boolean hasNot() {
        return not != null;
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    public boolean isNot() {
        return isNot;
    }

    @Override
    public void setNot(boolean isNot) {
        this.isNot = isNot;
    }

    @Override
    public void createLit(IntIterableRangeSet rootDomain) {
        if (this.literal != null) {
            throw new IllegalStateException("createLit(Implications) called twice");
        }
        this.literal = new SignedLiteral.Boolean();
    }

    @Override
    public SignedLiteral getLit() {
        if (this.literal == null) {
            throw new NullPointerException("getLit() called on null, a call to createLit(Implications) is required");
        }
        return this.literal;
    }
}
