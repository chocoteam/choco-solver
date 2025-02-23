/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.IIntervalDelta;
import org.chocosolver.solver.variables.delta.IntervalDelta;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.monitor.IntervalDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.util.iterators.*;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

import java.util.Iterator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class IntervalIntVarImpl extends AbstractVariable implements IntVar {

    /**
     * Set to <tt>true</tt> if this variable reacts is associated with at least one propagator which reacts
     * on value removal
     */
    private boolean reactOnRemoval = false;
    /**
     * Lower bound of the current domain
     */
    private final IStateInt LB;
    /**
     * Upper bound of the current domain
     */
    private final IStateInt UB;
    /**
     * To iterate over removed values
     */
    private IIntervalDelta delta = NoDelta.singleton;
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
     * Create a bounded domain IntVar : [min,max]
     *
     * @param name  name of the variable
     * @param min   lower bound
     * @param max   upper bound
     * @param model declaring model
     * @implNote Only bounds modifications are handled
     * (any value removals in the middle of the domain will be ignored).
     */
    public IntervalIntVarImpl(String name, int min, int max, Model model) {
        super(name, model);
        IEnvironment env = model.getEnvironment();
        this.LB = env.makeInt(min);
        this.UB = env.makeInt(max);
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
     * @implNote Can only update bounds.
     * Any other value removals will be ignored.
     */
    @Override
    public boolean removeValue(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        if (value == getLB()) {
            return updateLowerBound(value + 1, cause);
        } else if (value == getUB()) {
            return updateUpperBound(value - 1, cause);
        }
        return false;
    }

    /**
     * @implNote Can only update bounds.
     * Any other value removals will be ignored.
     */
    @Override
    public boolean removeValues(IntIterableSet values, ICause cause) throws ContradictionException {
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        if (nlb > oub || nub < olb) {
            return false;
        }
        if (nlb == olb) {
            // look for the new lb
            do {
                olb = nextValue(olb);
                nlb = values.nextValue(olb - 1);
            } while (olb < Integer.MAX_VALUE && oub < Integer.MAX_VALUE && nlb == olb);

        }
        if (nub == oub) {
            // look for the new ub
            do {
                oub = previousValue(oub);
                nub = values.previousValue(oub + 1);
            } while (olb > Integer.MIN_VALUE && oub > Integer.MIN_VALUE && nub == oub);
        }
        // the new bounds are now known, delegate to the right method
        return updateBounds(olb, oub, cause);
    }

    /**
     * @implNote Can only update bounds.
     * Any other value removals will be ignored.
     */
    @Override
    public boolean removeAllValuesBut(IntIterableSet values, ICause cause) throws ContradictionException {
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        // the new bounds are now known, delegate to the right method
        return updateBounds(nlb, nub, cause);
    }

    /**
     * @implNote Can only update bounds.
     * Any other value removals will be ignored.
     */
    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        assert cause != null;
        if (from <= getLB())
            return updateLowerBound(to + 1, cause);
        else if (getUB() <= to)
            return updateUpperBound(from - 1, cause);
        return false;
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
    public boolean instantiateTo(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        if (!this.contains(value)) {
            this.contradiction(cause, MSG_INST);
        } else if (!isInstantiated()) {
            int lb = this.getLB();
            int ub = this.getUB();
            IntEventType e = IntEventType.INSTANTIATE;
            if (reactOnRemoval) {
                if (lb <= value - 1) delta.add(lb, value - 1, cause);
                if (value + 1 <= ub) delta.add(value + 1, ub, cause);
            }
            this.LB.set(value);
            this.UB.set(value);
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
    public boolean updateLowerBound(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        int old = this.getLB();
        if (old < value) {
            int oub = this.getUB();
            if (oub < value) {
                this.contradiction(cause, MSG_LOW);
            } else {
                IntEventType e = IntEventType.INCLOW;
                if (reactOnRemoval) {
                    delta.add(old, value - 1, cause);
                }
                LB.set(value);
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
    public boolean updateUpperBound(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        int old = this.getUB();
        if (old > value) {
            int olb = this.getLB();
            if (olb > value) {
                this.contradiction(cause, MSG_UPP);
            } else {
                IntEventType e = IntEventType.DECUPP;
                if (reactOnRemoval) {
                    delta.add(value + 1, old, cause);
                }
                UB.set(value);

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
        if (olb < lb || ub < oub) {
            IntEventType e = null;
            if (oub < lb) {
                this.contradiction(cause, MSG_LOW);
            } else if (olb < lb) {
                e = IntEventType.INCLOW;
                if (reactOnRemoval) {
                    delta.add(olb, lb - 1, cause);
                }
                LB.set(lb);
                olb = lb;
            }
            if (olb > ub) {
                this.contradiction(cause, MSG_UPP);
            } else if (oub > ub) {
                e = e == null ? IntEventType.DECUPP : IntEventType.BOUND;
                if (reactOnRemoval) {
                    delta.add(ub + 1, oub, cause);
                }
                UB.set(ub);
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
        return LB.get() == UB.get();
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return isInstantiated() && getLB() == value;
    }

    @Override
    public boolean contains(int aValue) {
        return ((aValue >= LB.get()) && (aValue <= UB.get()));
    }

    @Override
    public int getValue() throws IllegalStateException{
        if(!isInstantiated()) {
            throw new IllegalStateException("getValue() can be only called on instantiated variable. " +
                    name + " is not instantiated");
        }
        return getLB();
    }

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    @Override
    public int getLB() {
        return this.LB.get();
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    @Override
    public int getUB() {
        return this.UB.get();
    }

    @Override
    public int getDomainSize() {
        return UB.get() - LB.get() + 1;
    }

    @Override
    public int getRange() {
        return getDomainSize();
    }

    @Override
    public int nextValue(int aValue) {
        int lb = LB.get();
        if (aValue < lb) {
            return lb;
        } else if (aValue < UB.get()) {
            return aValue + 1;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int nextValueOut(int v) {
        int ub = UB.get();
        if (LB.get() - 1 <= v && v <= ub) {
            return ub + 1;
        } else {
            return v + 1;
        }
    }

    @Override
    public int previousValue(int aValue) {
        int ub = UB.get();
        if (aValue > ub) {
            return ub;
        } else if (aValue > LB.get()) {
            return aValue - 1;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public int previousValueOut(int v) {
        int lb = LB.get();
        if (lb <= v && v <= UB.get() + 1) {
            return lb - 1;
        } else {
            return v - 1;
        }
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return false;
    }

    @Override
    public IIntervalDelta getDelta() {
        return delta;
    }

    @Override
    public String toString() {
        if (LB.get() == UB.get()) {
            return String.format("%s = %d", name, getLB());
        }
        return String.format("%s = [%d,%d]", name, getLB(), getUB());
    }

    ////////////////////////////////////////////////////////////////
    ///// methode liees au fait qu'une variable est observable /////
    ////////////////////////////////////////////////////////////////


    @Override
    public void createDelta() {
        if (!reactOnRemoval) {
            delta = new IntervalDelta(model.getEnvironment());
            reactOnRemoval = true;
        }
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new IntervalDeltaMonitor(delta, propagator);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getTypeAndKind() {
        return VAR | INT;
    }

    @Override
    protected EvtScheduler<IntEventType> createScheduler() {
        return new IntEvtScheduler();
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

}
