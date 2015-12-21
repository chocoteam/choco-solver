/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.IIntervalDelta;
import org.chocosolver.solver.variables.delta.IntervalDelta;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.monitor.IntervalDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.util.iterators.DisposableRangeBoundIterator;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueBoundIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.tools.StringUtils;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class IntervalIntVarImpl extends AbstractVariable implements IntVar {

    private static final long serialVersionUID = 1L;

    protected boolean reactOnRemoval = false;

    private final IStateInt LB, UB, SIZE;

    IIntervalDelta delta = NoDelta.singleton;

    private DisposableValueIterator _viterator;

    private DisposableRangeIterator _riterator;

    //////////////////////////////////////////////////////////////////////////////////////

    public IntervalIntVarImpl(String name, int min, int max, Solver solver) {
        super(name, solver);
        IEnvironment env = solver.getEnvironment();
        this.LB = env.makeInt(min);
        this.UB = env.makeInt(max);
        this.SIZE = env.makeInt(max - min + 1);
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
        assert cause != null;
//        records.forEachRemVal(beforeModification.set(this, EventType.REMOVE, cause));
        int inf = getLB();
        int sup = getUB();
        if (value == inf && value == sup) {
            if (_plugexpl) {
                solver.getEventObserver().removeValue(this, value, cause);
            }
            this.contradiction(cause, MSG_REMOVE);
        } else if (inf == value || value == sup) {
            IntEventType e;
            if (value == inf) {
                if (reactOnRemoval) {
                    delta.add(value, value, cause);
                }
                SIZE.add(-1);
                LB.set(value + 1);
                e = IntEventType.INCLOW;
            } else {
                if (reactOnRemoval) {
                    delta.add(value, value, cause);
                }
                SIZE.add(-1);
                UB.set(value - 1);
                e = IntEventType.DECUPP;
            }
            if (SIZE.get() > 0) {
                if (this.isInstantiated()) {
                    e = IntEventType.INSTANTIATE;
                }
                this.notifyPropagators(e, cause);
            } else if (SIZE.get() == 0) {
                if (_plugexpl) {
                    solver.getEventObserver().removeValue(this, value, cause);
                }
                this.contradiction(cause, MSG_EMPTY);
            }
            if (_plugexpl) {
                solver.getEventObserver().removeValue(this, value, cause);
            }
            return true;
        }
        return false;
    }

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
     * {@inheritDoc}
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
        assert cause != null;
        if (this.isInstantiated()) {
            int cvalue = this.getValue();
            if (value != cvalue) {
                if (_plugexpl) {
                    solver.getEventObserver().instantiateTo(this, value, cause, cvalue, cvalue);
                }
                this.contradiction(cause, MSG_INST);
            }
            return false;
        } else if (contains(value)) {
            IntEventType e = IntEventType.INSTANTIATE;

            int lb = 0;
            int ub = 0;
            if (reactOnRemoval) {
                lb = this.LB.get();
                ub = this.UB.get();
                if (lb <= value - 1) delta.add(lb, value - 1, cause);
                if (value + 1 <= ub) delta.add(value + 1, ub, cause);
            } else if (_plugexpl) {
                lb = LB.get();
                ub = UB.get();
            }
            this.LB.set(value);
            this.UB.set(value);
            this.SIZE.set(1);

            if (_plugexpl) {
                solver.getEventObserver().instantiateTo(this, value, cause, lb, ub);
            }
            this.notifyPropagators(e, cause);
            return true;
        } else {
            if (_plugexpl) {
                solver.getEventObserver().instantiateTo(this, value, cause, LB.get(), UB.get());
            }
            this.contradiction(cause, MSG_UNKNOWN);
            return false;
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
                this.contradiction(cause, MSG_LOW);
            } else {
                IntEventType e = IntEventType.INCLOW;

                if (reactOnRemoval) {
                    if (old <= value - 1) delta.add(old, value - 1, cause);
                }
                SIZE.add(old - value);
                LB.set(value);
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
                this.contradiction(cause, MSG_UPP);
            } else {
                IntEventType e = IntEventType.DECUPP;

                if (reactOnRemoval) {
                    if (value + 1 <= old) delta.add(value + 1, old, cause);
                }
                SIZE.add(value - old);
                UB.set(value);

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
    public boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = this.getLB();
        int oub = this.getUB();
        boolean update = false;
        if (olb < lb || ub < oub) {
            if (oub >= lb && olb <= ub) {
                int d = 0;
                IntEventType e = null;
                if (olb < lb) {
                    if (reactOnRemoval) {
                        if (olb <= lb - 1) delta.add(olb, lb - 1, cause);
                    }
                    d += olb - lb;
                    LB.set(lb);
                    e = IntEventType.INCLOW;
                }
                if (ub < oub) {
                    if (reactOnRemoval) {
                        if (ub + 1 <= oub) delta.add(ub + 1, oub, cause);
                    }
                    d += ub - oub;
                    UB.set(ub);
                    e = e == null ? IntEventType.DECUPP : IntEventType.BOUND;
                }
                SIZE.add(d);
                if (isInstantiated()) {
                    e = IntEventType.INSTANTIATE;
                }
                this.notifyPropagators(e, cause);

                if (_plugexpl) {
                    if (olb < lb) solver.getEventObserver().updateLowerBound(this, lb, olb, cause);
                    if (oub > ub) solver.getEventObserver().updateUpperBound(this, ub, oub, cause);
                }
                update = true;
            } else { // fails
                if (oub < lb) {
                    if (_plugexpl) {
                        solver.getEventObserver().updateLowerBound(this, oub + 1, olb, cause);
                    }
                    this.contradiction(cause, MSG_LOW);
                } else {
                    //if (olb > ub) {
                    if (_plugexpl) {
                        solver.getEventObserver().updateUpperBound(this, olb - 1, oub, cause);
                    }
                    this.contradiction(cause, MSG_UPP);
                }
            }
        }
        return update;
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
        return ((aValue >= LB.get()) && (aValue <= UB.get()));
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
        return SIZE.get();
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
    public boolean hasEnumeratedDomain() {
        return false;
    }

    @Override
    public IIntervalDelta getDelta() {
        return delta;
    }

    @Override
    public String toString() {
        if (SIZE.get() == 1) {
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
            delta = new IntervalDelta(solver.getEnvironment());
            reactOnRemoval = true;
        }
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new IntervalDeltaMonitor(delta, propagator);
    }

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
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
        return new IntervalIntVarImpl(StringUtils.randomName(this.name), this.LB.get(), this.UB.get(), solver);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
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
        if (_riterator == null || !_riterator.isReusable()) {
            _riterator = new DisposableRangeBoundIterator(this);
        }
        if (bottomUp) {
            _riterator.bottomUpInit();
        } else {
            _riterator.topDownInit();
        }
        return _riterator;
    }
}
