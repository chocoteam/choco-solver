/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.OneValueDelta;
import org.chocosolver.solver.variables.delta.monitor.OneValueDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.BoolEvtScheduler;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableRangeBoundIterator;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueBoundIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.EvtScheduler;


/**
 * declare an BoolVar based on X and C, such as (X <= C) is reified by this. <br/> Based on "Views
 * and Iterators for Generic Constraint Implementations" <br/> C. Shulte and G. Tack.<br/> Eleventh
 * International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class LeqView extends IntView<IntVar> implements BoolVar {

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
    private IEnumDelta delta = NoDelta.singleton;
    /**
     * Set to <tt>true</tt> if this variable reacts is associated with at least one propagator which
     * reacts on value removal
     */
    private boolean reactOnRemoval = false;

    /**
     * A view based on <i>var<i/> such that <i>var<i/> + <i>cste<i/>
     *
     * @param var  an integer variable
     * @param cste an int
     */
    public LeqView(final IntVar var, final int cste) {
        super("(" + var.getName() + "&le;" + cste + ")", var);
        this.cste = cste;
    }

    @Override
    public ESat getBooleanValue() {
        if (var.getUB()<=cste) {
            return ESat.TRUE;
        } else if (var.getLB()>cste){
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
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
            model.getSolver().getEventObserver().instantiateTo(this, value, this, getLB(), getUB());
            this.contradiction(this, MSG_EMPTY);
        } else if (!isInstantiated()) {
            model.getSolver().getEventObserver().instantiateTo(this, value, this, getLB(), getUB());
            notifyPropagators(IntEventType.INSTANTIATE, this);
            if (reactOnRemoval) {
                delta.add(1 - value, cause);
            }
            if (value == 1) {
                done = var.updateUpperBound(cste, this);
            } else {
                done = var.updateLowerBound(cste + 1, this);
            }
        }
        return done;
    }

    @Override
    protected boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException {
        return value > 0 && instantiateTo(value, this);
    }

    @Override
    protected boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException {
        return value < 1 && instantiateTo(value, this);
    }

    @Override
    protected boolean doRemoveValueFromVar(int value) throws ContradictionException {
        boolean hasChanged = false;
        if (value == 0 || value == 1) {
            hasChanged = instantiateTo(1 - value, this);
        }
        return hasChanged;
    }

    @Override
    protected boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException {
        boolean hasChanged = false;
        if (from <= to && from <= 1 && to >= 0) {
            if (from == 1) {
                hasChanged = instantiateTo(1, this);
            } else if (to == 0) {
                hasChanged = instantiateTo(0, this);
            } else {
                instantiateTo(2, this);
            }
        }
        return hasChanged;
    }

    @Override
    public boolean contains(int value) {
        if (value == 0) {
            return  cste < var.getUB();
        } else if (value == 1) {
            return var.getLB() <= cste;
        }
        return false;
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        if (value == 0) {
            return cste < var.getLB() ;
        } else if (value == 1) {
            return var.getUB() <= cste;
        }
        return false;
    }

    @Override
    public int getValue() {
        return getLB();
    }

    @Override
    public int getLB() {
        if (cste < var.getUB()) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getUB() {
        if (var.getLB() <= cste) {
            return 1;
        }
        return 0;
    }

    @Override
    public int nextValue(int v) {
        if (v < 0 && contains(0)) {
            return 0;
        }
        return v <= 0 && contains(1) ? 1 : Integer.MAX_VALUE;
    }

    @Override
    public int nextValueOut(int v) {
        int lb = 0, ub = 1;
        if (var.getLB() > cste) {
            ub = 0;
        } else if (cste >= var.getUB()) {
            lb = 1;
        }
        if (lb - 1 <= v && v <= ub) {
            return ub + 1;
        } else {
            return v + 1;
        }
    }

    @Override
    public int previousValue(int v) {
        if (v > 1 && contains(1)) {
            return 1;
        }
        return v >= 1 && contains(0) ? 0 : Integer.MIN_VALUE;
    }

    @Override
    public int previousValueOut(int v) {
        int lb = 0, ub = 1;
        if (var.getLB() > cste) {
            ub = 0;
        } else if (cste >= var.getUB()) {
            lb = 1;
        }
        if (lb <= v && v <= ub + 1) {
            return lb - 1;
        } else {
            return v - 1;
        }
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new BoolEvtScheduler();
    }

    @Override
    public String toString() {
        return "(" + var.getName() + " ≤ " + cste + ") = [" + getLB() + "," + getUB() + "]";
    }

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
    public void justifyEvent(IntVar var, ICause cause, IntEventType mask, int one, int two, int three) {
        switch (mask) {
            case DECUPP:
                if (one <= cste && cste < two) {
                    model.getSolver().getEventObserver().instantiateTo(this, 1, var, 0, 1);
                }
                break;
            case INCLOW:
                if (two <= cste && cste < one) {
                    model.getSolver().getEventObserver().instantiateTo(this, 0, var, 0, 1);
                }
                break;
            case REMOVE:
                break;
            case INSTANTIATE:
                if (one <= cste) {
                    model.getSolver().getEventObserver().instantiateTo(this, 1, var, 0, 1);
                } else {
                    model.getSolver().getEventObserver().instantiateTo(this, 0, var, 0, 1);
                }
                break;
        }
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


}
