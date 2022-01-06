/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;

import org.chocosolver.memory.IStateBool;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.IDelta;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.OneValueDelta;
import org.chocosolver.solver.variables.delta.monitor.OneValueDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.impl.AbstractVariable;
import org.chocosolver.solver.variables.impl.scheduler.BoolEvtScheduler;
import org.chocosolver.solver.variables.view.bool.BoolEqView;
import org.chocosolver.solver.variables.view.bool.BoolLeqView;
import org.chocosolver.util.iterators.DisposableRangeBoundIterator;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueBoundIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

/**
 * An abstract class for boolean views over {@link org.chocosolver.solver.variables.IntVar}.
 *
 * @see BoolEqView
 * @see BoolLeqView
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 04/12/2018.
 */
public abstract class BoolIntView<I extends IntVar> extends IntView<I> implements BoolVar {

    /**
     * indicate if the view is fixed
     */
    protected IStateBool fixed;
    /**
     * A constant value
     */
    public final int cste;
    /**
     * Associate boolean variable expressing not(this)
     */
    private BoolVar not;
    /**
     * For boolean expression purpose
     */
    private boolean isNot = false;
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
     * A view based on <i>var<i/> and a constant
     *
     * @param var  an integer variable
     * @param cste an int
     */
    protected BoolIntView(final I var, String op, final int cste) {
        super("(" + var.getName() + op + cste + ")", var);
        this.cste = cste;
        this.fixed = var.getModel().getEnvironment().makeBool(false);
    }

    @Override
    public final void notify(IEventType event, int variableIdx) throws ContradictionException {
        if(!fixed.get()) {
            if(isInstantiated()){
                this.fixed.set(Boolean.TRUE);
                super.notify(event, variableIdx);
            }
        }
    }

    @Override
    public final boolean setToTrue(ICause cause) throws ContradictionException {
        return instantiateTo(1, cause);
    }

    @Override
    public final boolean setToFalse(ICause cause) throws ContradictionException {
        return instantiateTo(0, cause);
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
    public final int getTypeAndKind() {
        return Variable.VIEW | Variable.BOOL;
    }

    @Override
    public final int getValue() {
        return getLB();
    }

    @Override
    protected final EvtScheduler createScheduler() {
        return new BoolEvtScheduler();
    }

    @Override
    public final String toString() {
        if (isInstantiated()) {
            return this.name + " = " + this.getValue();
        } else {
            return this.name + " = " + "[0,1]";
        }
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
    public final void createDelta() {
        if (!reactOnRemoval) {
            delta = new OneValueDelta(model.getEnvironment());
            reactOnRemoval = true;
        }
    }

    @Override
    public IDelta getDelta() {
        return delta;
    }

    @SuppressWarnings("unchecked")
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
}
