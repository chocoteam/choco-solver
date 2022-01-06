/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.bool;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.monitor.OneValueDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.AbstractVariable;
import org.chocosolver.solver.variables.impl.scheduler.BoolEvtScheduler;
import org.chocosolver.solver.variables.impl.siglit.SignedLiteral;
import org.chocosolver.solver.variables.view.AbstractView;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.*;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

import java.util.Iterator;

/**
 * Boolean view b over a set variable S:
 *
 * With v an integer, b = true iff S contains v.
 *
 * @author Dimitri Justeau-Allaire
 * @since 08/2021
 */
public class BoolSetView<S extends SetVar> extends AbstractView<S> implements BoolVar {

    /**
     * The observed set variables
     */
    protected S setVar;
    /**
     * The value to observe in the set variable
     */
    protected int v;
    /**
     * To iterate over removed values
     */
    protected IEnumDelta delta = NoDelta.singleton;
    /**
     * Set to <tt>true</tt> if this variable reacts is associated with at least one propagator which
     * reacts on value removal
     */
    protected boolean reactOnRemoval = false;
    /**
     * indicate if the view is fixed
     */
    protected IStateBool fixed;
    /**
     * Associate boolean variable expressing not(this)
     */
    private BoolVar not;
    /**
     * For boolean expression purpose
     */
    private boolean isNot = false;
    /**
     * Value iterator
     */
    protected DisposableValueIterator _viterator;
    /**
     * Range iterator
     */
    protected DisposableRangeIterator _riterator;
    /**
     * Value iterator allowing for(int i:this) loops
     */
    private final IntVarValueIterator _javaIterator = new IntVarValueIterator(this);
    /**
     * Signed Literal
     */
    protected SignedLiteral literal;


    public BoolSetView(int v, S setVar) {
        super("boolSetView[" + v + " in " + setVar.getName() + "]", setVar);
        this.setVar = setVar;
        this.v = v;
        boolean initialValue = setVar.getLB().contains(v) || !setVar.getUB().contains(v);
        this.fixed = getModel().getEnvironment().makeBool(initialValue);
    }

    // BoolVar methods

    @Override
    public ESat getBooleanValue() {
        if (setVar.getLB().contains(v)) {
            return ESat.TRUE;
        }
        if (!setVar.getUB().contains(v)) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    public void notify(IEventType event, int variableIdx) throws ContradictionException {
        if (!fixed.get() && isInstantiated()) {
            fixed.set(true);
            notifyPropagators(event, this);
        }
    }

    @Override
    public boolean setToTrue(ICause cause) throws ContradictionException {
        return instantiateTo(1, cause);
    }

    @Override
    public boolean setToFalse(ICause cause) throws ContradictionException {
        return instantiateTo(0, cause);
    }


    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        boolean done = false;
        if (!this.contains(value)) {
            model.getSolver().getEventObserver().instantiateTo(this, v, cause, getLB(), getUB());
            this.contradiction(cause, MSG_EMPTY);
        } else if (!isInstantiated()) {
            model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
            this.fixed.set(true);
            if (reactOnRemoval) {
                delta.add(1 - value, cause);
            }
            if (value == 1) {
                done = setVar.force(v, this);
            } else {
                done = setVar.remove(v, this);
            }
            notifyPropagators(IntEventType.INSTANTIATE, cause);
        }
        return done;
    }

    @Override
    public boolean contains(int value) {
        if (value == 0) {
            return !setVar.getLB().contains(v);
        } else if (value == 1) {
            return setVar.getUB().contains(v);
        }
        return false;
    }

    @Override
    public boolean isInstantiated() {
        if (setVar.getLB().contains(v)) {
            return true;
        }
        return !setVar.getUB().contains(v);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        if (value == 0) {
            return !setVar.getUB().contains(v);
        } else if (value == 1) {
            return setVar.getLB().contains(v);
        }
        return false;
    }

    @Override
    public int getDomainSize() {
        return isInstantiated() ? 1 : 2;
    }

    @Override
    public int getRange() {
        return getUB() - getLB();
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return false;
    }

    @Override
    public int getLB() {
        if (setVar.getLB().contains(v)) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getUB() {
        if (setVar.getUB().contains(v)) {
            return 1;
        }
        return 0;
    }

    @Override
    public int nextValue(int val) {
        if (val < 0 && contains(0)) {
            return 0;
        }
        return val <= 0 && contains(1) ? 1 : Integer.MAX_VALUE;
    }

    @Override
    public final int getTypeAndKind() {
        return Variable.VIEW | Variable.BOOL;
    }

    @Override
    public final int getValue() {
        return getLB();
    }

    @Override
    protected final BoolEvtScheduler createScheduler() {
        return new BoolEvtScheduler();
    }

    @Override
    public IDelta getDelta() {
        return delta;
    }

    @Override
    public void createDelta() {
        setVar.createDelta();
    }

    @Override
    public final IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new OneValueDeltaMonitor(delta, propagator);
    }

    @Override
    public final void _setNot(BoolVar neg) {
        this.not = neg;
    }

    @Override
    public final BoolVar not() {
        if (!hasNot()) {
            not = model.boolNotView(this);
            not._setNot(this);
        }
        return not;
    }

    @Override
    public final boolean hasNot() {
        return not != null;
    }

    @Override
    public final boolean isLit() {
        return true;
    }

    @Override
    public final boolean isNot() {
        return isNot;
    }

    @Override
    public final void setNot(boolean isNot) {
        this.isNot = isNot;
    }

    @Override
    public int nextValueOut(int val) {
        int lb = getLB();
        int ub = getUB();
        if (lb - 1 <= val && val <= ub) {
            return ub + 1;
        } else {
            return val + 1;
        }
    }

    @Override
    public int previousValue(int val) {
        if (val > 1 && contains(1)) {
            return 1;
        }
        return val >= 1 && contains(0) ? 0 : Integer.MIN_VALUE;
    }

    @Override
    public int previousValueOut(int val) {
        int lb = getLB();
        int ub = getUB();
        if (lb <= val && val <= ub + 1) {
            return lb - 1;
        } else {
            return val - 1;
        }
    }

    @Override
    public final boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        if (value == kFALSE)
            return instantiateTo(kTRUE, cause);
        else if (value == kTRUE)
            return instantiateTo(kFALSE, cause);
        return false;
    }

    @Override
    public final boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException {
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
    public final boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
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
    public final boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (from <= to && from <= 1 && to >= 0) {
            if (from == kTRUE) {
                hasChanged = instantiateTo(kFALSE, cause);
            } else if (to == kFALSE) {
                hasChanged = instantiateTo(kTRUE, cause);
            } else {
                model.getSolver().getEventObserver().instantiateTo(this, 2, cause, kFALSE, kTRUE);
                this.contradiction(cause, AbstractVariable.MSG_EMPTY);
            }
        }
        return hasChanged;
    }

    @Override
    public final boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        return value > kFALSE && instantiateTo(value, cause);
    }

    @Override
    public final boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        return value < kTRUE && instantiateTo(value, cause);
    }

    @Override
    public final boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (lb > kTRUE || ub < kFALSE) {
            model.getSolver().getEventObserver().instantiateTo(this, 2, cause, kFALSE, kTRUE);
            this.contradiction(cause, MSG_EMPTY);
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
    public final DisposableValueIterator getValueIterator(boolean bottomUp) {
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
    public final DisposableRangeIterator getRangeIterator(boolean bottomUp) {
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
        _javaIterator.reset();
        return _javaIterator;
    }

    @Override
    public void createLit(IntIterableRangeSet rootDomain) {
        if(this.literal != null){
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

    @Override
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        throw new UnsupportedOperationException("Bool view over set variables does not support explanations");
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        throw new UnsupportedOperationException("Bool view over set variables does not support explanations");
    }
}
