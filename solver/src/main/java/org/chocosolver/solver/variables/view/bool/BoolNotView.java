/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.bool;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.AbstractVariable;
import org.chocosolver.solver.variables.impl.scheduler.BoolEvtScheduler;
import org.chocosolver.solver.variables.view.IntView;
import org.chocosolver.solver.variables.view.ViewDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

/**
 * A view for boolean variable, that enforce not(b). <br/>
 *
 * @author Charles Prud'homme
 * @since 31/07/12
 */
@Explained
public final class BoolNotView<B extends BoolVar> extends IntView<B> implements BoolVar {

    /**
     * Create a not view based on <i>var<i/>
     *
     * @param var a boolean variable
     */
    public BoolNotView(B var) {
        super("not(" + var.getName() + ")", var);
    }

    @Override
    public ESat getBooleanValue() {
        return ESat.not(var.getBooleanValue());
    }

    @Override
    public boolean removeValue(int value, ICause cause, Reason reason) throws ContradictionException {
        return contains(value) && instantiateTo(1 - value, cause, reason);
    }

    @Override
    public boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (values.contains(0)) {
            hasChanged = instantiateTo(1, cause);
        }
        if (values.contains(1)) {
            hasChanged = instantiateTo(0, cause);
        }
        return hasChanged;
    }

    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (!values.contains(0)) {
            hasChanged = instantiateTo(1, cause);
        }
        if (!values.contains(1)) {
            hasChanged = instantiateTo(0, cause);
        }
        return hasChanged;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (from <= to && from <= 1 && to >= 0) {
            if (from == 1) {
                hasChanged = instantiateTo(0, cause);
            } else if (to == 0) {
                hasChanged = instantiateTo(1, cause);
            } else {
                this.contradiction(cause, AbstractVariable.MSG_EMPTY);
            }
        }
        return hasChanged;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause, Reason reason) throws ContradictionException {
        if (!this.contains(value)) {
            this.contradiction(cause, MSG_INST);
        } else if (!isInstantiated()) {
            boolean done = var.instantiateTo(1 - value, this);
            notifyPropagators(IntEventType.INSTANTIATE, cause);
            return done;
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause, Reason reason) throws ContradictionException {
        return value > 0 && instantiateTo(value, cause, reason);
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause, Reason reason) throws ContradictionException {
        return value < 1 && instantiateTo(value, cause, reason);
    }

    @Override
    public boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        boolean hasChanged = false;
        if (lb > 1) {
            var.instantiateTo(-1, cause);
        } else if (ub < 0) {
            var.instantiateTo(2, cause);
        } else {
            if (lb == 1) {
                hasChanged = instantiateTo(1, cause);
            }
            if (ub == 0) {
                hasChanged = instantiateTo(0, cause);
            }
        }
        return hasChanged;
    }

    @Override
    public boolean contains(int value) {
        return var.contains(1 - value);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(1 - value);
    }

    @Override
    public int getValue() throws IllegalStateException {
        if (!isInstantiated()) {
            throw new IllegalStateException("getValue() can be only called on instantiated variable. " +
                    name + " is not instantiated");
        }
        int v = var.getValue();
        return 1 - v;
    }

    @Override
    public int getLB() {
        if (var.isInstantiated()) {
            return getValue();
        } else return 0;
    }

    @Override
    public int getUB() {
        if (var.isInstantiated()) {
            return getValue();
        } else return 1;
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
        if (var.isInstantiated()) {
            lb = ub = getValue();
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
        if (var.isInstantiated()) {
            lb = ub = getValue();
        }
        if (lb <= v && v <= ub + 1) {
            return lb - 1;
        } else {
            return v - 1;
        }
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
        }
        return new ViewDeltaMonitor(var.monitorDelta(propagator)) {

            @Override
            protected int transform(int value) {
                return 1 - value;
            }
        };
    }

    @Override
    protected EvtScheduler<IntEventType> createScheduler() {
        return new BoolEvtScheduler();
    }

    public String toString() {
        return "not(" + var + ")";
    }

    @Override
    public BoolVar not() {
        return var;
    }

    @Override
    public void _setNot(BoolVar not) {
        assert not == var;
    }

    @Override
    public boolean hasNot() {
        return true;
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    public boolean isNot() {
        return !var.isNot();
    }

    @Override
    public void setNot(boolean isNot) {
        assert isNot;
    }

    @Override
    public int getTypeAndKind() {
        return Variable.VIEW | Variable.BOOL;
    }

    @Override
    public int getLit(int val, int t) {
        if (val < 0) {
            return 1 ^ (t & 1);  // true, false, true, false
        }
        if (val > 1) {
            return t - 1 >> 1 & 1;  // true, false, false, true
        }
        if (t >= 2) {
            assert 5 - t >= 0;
            return var.getLit(1 - val, 5 - t);
        }
        return var.getLit(1 - val, t);
    }

    @Override
    public int getMinLit() {
        return var.getMaxLit();
    }

    @Override
    public int getMaxLit() {
        return var.getMinLit();
    }

    @Override
    public int getValLit() {
        return var.getValLit();
    }

    @Override
    public int satVar() {
        return (var.satVar() + 1) * -1; // for SatFactory
    }
}
