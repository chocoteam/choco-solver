/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables.fast;

import memory.IEnvironment;
import memory.IStateInt;
import solver.Cause;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.explanations.antidom.AntiDomInterval;
import solver.explanations.antidom.AntiDomain;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.IIntervalDelta;
import solver.variables.delta.IntervalDelta;
import solver.variables.delta.NoDelta;
import solver.variables.delta.monitor.IntervalDeltaMonitor;
import util.iterators.DisposableRangeBoundIterator;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueBoundIterator;
import util.iterators.DisposableValueIterator;
import util.tools.StringUtils;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class IntervalIntVarImpl extends AbstractVariable<IIntervalDelta, IntVar<IIntervalDelta>> implements IntVar<IIntervalDelta> {

    private static final long serialVersionUID = 1L;

    protected boolean reactOnRemoval = false;

    private final IStateInt LB, UB, SIZE;

    IIntervalDelta delta = NoDelta.singleton;

    private DisposableValueIterator _viterator;

    private DisposableRangeIterator _riterator;

    //////////////////////////////////////////////////////////////////////////////////////

    public IntervalIntVarImpl(String name, int min, int max, Solver solver) {
        super(name, solver);
        solver.associates(this);
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
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
//        records.forEach(beforeModification.set(this, EventType.REMOVE, cause));
        ICause antipromo = cause;
        int inf = getLB();
        int sup = getUB();
        if (value == inf && value == sup) {
            if (Configuration.PLUG_EXPLANATION) {
                solver.getExplainer().removeValue(this, value, antipromo);
            }
            this.contradiction(cause, EventType.REMOVE, MSG_REMOVE);
        } else if (inf == value || value == sup) {
            EventType e;
            if (value == inf) {
                if (reactOnRemoval) {
                    delta.add(value, value, cause);
                }
                SIZE.add(-1);
                LB.set(value + 1);
                e = EventType.INCLOW;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            } else {
                if (reactOnRemoval) {
                    delta.add(value, value, cause);
                }
                SIZE.add(-1);
                UB.set(value - 1);
                e = EventType.DECUPP;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            if (SIZE.get() > 0) {
                if (this.instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                this.notifyPropagators(e, cause);
            } else if (SIZE.get() == 0) {
                if (Configuration.PLUG_EXPLANATION) {
                    solver.getExplainer().removeValue(this, value, antipromo);
                }
                this.contradiction(cause, EventType.REMOVE, MSG_EMPTY);
            }
            if (Configuration.PLUG_EXPLANATION) {
                solver.getExplainer().removeValue(this, value, antipromo);
            }
            return true;
        }
        return false;
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
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        if (this.instantiated()) {
            int cvalue = this.getValue();
            if (value != cvalue) {
                if (Configuration.PLUG_EXPLANATION) {
                    solver.getExplainer().instantiateTo(this, value, cause, cvalue, cvalue);
                }
                this.contradiction(cause, EventType.INSTANTIATE, MSG_INST);
            }
            return false;
        } else if (contains(value)) {
            EventType e = EventType.INSTANTIATE;

            int lb = 0;
            int ub = 0;
            if (reactOnRemoval) {
                lb = this.LB.get();
                ub = this.UB.get();
                if (lb <= value - 1) delta.add(lb, value - 1, cause);
                if (value + 1 <= ub) delta.add(value + 1, ub, cause);
            } else if (Configuration.PLUG_EXPLANATION) {
                lb = LB.get();
                ub = UB.get();
            }
            this.LB.set(value);
            this.UB.set(value);
            this.SIZE.set(1);

            if (Configuration.PLUG_EXPLANATION) {
                solver.getExplainer().instantiateTo(this, value, cause, lb, ub);
            }
            this.notifyPropagators(e, cause);
            return true;
        } else {
            if (Configuration.PLUG_EXPLANATION) {
                solver.getExplainer().instantiateTo(this, value, cause, LB.get(), UB.get());
            }
            this.contradiction(cause, EventType.INSTANTIATE, MSG_UNKNOWN);
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
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        ICause antipromo = cause;
        int old = this.getLB();
        if (old < value) {
            int oub = this.getUB();
            if (oub < value) {
                if (Configuration.PLUG_EXPLANATION){
                    solver.getExplainer().updateLowerBound(this, old, oub+1, antipromo);
                }
                this.contradiction(cause, EventType.INCLOW, MSG_LOW);
            } else {
                EventType e = EventType.INCLOW;

                if (reactOnRemoval) {
                    if (old <= value - 1) delta.add(old, value - 1, antipromo);
                }
                SIZE.add(old - value);
                LB.set(value);
                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                this.notifyPropagators(e, cause);

                if (Configuration.PLUG_EXPLANATION){
                    solver.getExplainer().updateLowerBound(this, old, value, antipromo);
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
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        ICause antipromo = cause;
        int old = this.getUB();
        if (old > value) {
            int olb = this.getLB();
            if (olb > value) {
                if (Configuration.PLUG_EXPLANATION){
                    solver.getExplainer().updateUpperBound(this, old, olb-1, antipromo);
                }
                this.contradiction(cause, EventType.DECUPP, MSG_UPP);
            } else {
                EventType e = EventType.DECUPP;

                if (reactOnRemoval) {
                    if (value + 1 <= old) delta.add(value + 1, old, cause);
                }
                SIZE.add(value - old);
                UB.set(value);

                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                this.notifyPropagators(e, cause);
                if (Configuration.PLUG_EXPLANATION){
                    solver.getExplainer().updateUpperBound(this, old, value, antipromo);
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public void wipeOut(ICause cause) throws ContradictionException {
        assert cause != null;
        removeInterval(this.getLB(), this.getUB(), cause);
    }

    public boolean instantiated() {
        return SIZE.get() == 1;
    }

    @Override
    public boolean instantiatedTo(int value) {
        return instantiated() && contains(value);
    }

    public boolean contains(int aValue) {
        return ((aValue >= LB.get()) && (aValue <= UB.get()));
    }

    /**
     * Retrieves the current value of the variable if instantiated, otherwier the lower bound.
     *
     * @return the current value (or lower bound if not yet instantiated).
     */
    public int getValue() {
        assert instantiated() : name + " not instantiated";
        return getLB();
    }

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    public int getLB() {
        return this.LB.get();
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    public int getUB() {
        return this.UB.get();
    }

    public int getDomainSize() {
        return SIZE.get();
    }

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
            delta = new IntervalDelta(solver.getSearchLoop());
            reactOnRemoval = true;
        }
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new IntervalDeltaMonitor(delta, propagator);
    }

    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        notifyMonitors(event);
        if ((modificationEvents & event.mask) != 0) {
            //records.forEach(afterModification.set(this, event, cause));
            //solver.getEngine().onVariableUpdate(this, afterModification.set(this, event, cause));
            solver.getEngine().onVariableUpdate(this, event, cause);
        }
        notifyViews(event, cause);
    }

    public void notifyMonitors(EventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public AntiDomain antiDomain() {
        return new AntiDomInterval(this);
    }

    public void explain(VariableState what, Explanation to) {
        AntiDomain invdom = solver.getExplainer().getRemovedValues(this);
        DisposableValueIterator it = invdom.getValueIterator();
        while (it.hasNext()) {
            int val = it.next();
            if ((what == VariableState.LB && val < this.getLB())
                    || (what == VariableState.UB && val > this.getUB())
                    || (what == VariableState.DOM)) {
//                System.out.println("solver.explainer.explain(this,"+ val +") = " + solver.explainer.explain(this, val));
                to.add(solver.getExplainer().explain(this, val));
            }
        }
        it.dispose();
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        to.add(solver.getExplainer().explain(this, val));
    }


    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        assert cause != null;
//        records.forEach(onContradiction.set(this, event, cause));
        solver.getEngine().fails(cause, this, message);
    }


    @Override
    public int getTypeAndKind() {
        return VAR | INT;
    }

    @Override
    public IntVar duplicate() {
        return new IntervalIntVarImpl(StringUtils.randomName(this.name), this.LB.get(), this.UB.get(), this.getSolver());
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
