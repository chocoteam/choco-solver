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
import memory.IStateBitSet;
import memory.IStateInt;
import solver.Cause;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.explanations.antidom.AntiDomBitset;
import solver.explanations.antidom.AntiDomain;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.EnumDelta;
import solver.variables.delta.IEnumDelta;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import solver.variables.delta.monitor.EnumDeltaMonitor;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueIterator;
import util.tools.StringUtils;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class BitsetIntVarImpl extends AbstractVariable<IEnumDelta, IntVar<IEnumDelta>> implements IntVar<IEnumDelta> {

    private static final long serialVersionUID = 1L;

    protected boolean reactOnRemoval = false;

    //  Bitset of available values -- includes offset
    private final IStateBitSet VALUES;
    // Lower bound of the current domain -- includes offset
    private final IStateInt LB;
    // Upper bound of the current domain -- includes offset
    private final IStateInt UB;
    private final IStateInt SIZE;
    //offset of the lower bound and the first value in the domain
    private final int OFFSET;
    private final int LENGTH;

    private IEnumDelta delta = NoDelta.singleton;

    private DisposableValueIterator _viterator;
    private DisposableRangeIterator _riterator;

    //////////////////////////////////////////////////////////////////////////////////////

    public BitsetIntVarImpl(String name, int[] sortedValues, Solver solver) {
        super(name, solver);
        solver.associates(this);
        IEnvironment env = solver.getEnvironment();
        OFFSET = sortedValues[0];
        int capacity = sortedValues[sortedValues.length - 1] - OFFSET + 1;
        this.VALUES = env.makeBitSet(capacity);
        for (int i = 0; i < sortedValues.length; i++) {
            this.VALUES.set(sortedValues[i] - OFFSET);
        }
        this.LB = env.makeInt(0);
        this.UB = env.makeInt(capacity - 1);
        this.SIZE = env.makeInt(VALUES.cardinality());
        LENGTH = capacity;
    }

    public BitsetIntVarImpl(String name, int offset, IStateBitSet values, Solver solver) {
        super(name, solver);
        solver.associates(this);
        IEnvironment env = solver.getEnvironment();
        OFFSET = offset;
        int cardinality = values.cardinality();
        this.VALUES = values.copy();
        this.LB = env.makeInt(0);
        this.UB = env.makeInt(values.prevSetBit(values.size()));
        this.SIZE = env.makeInt(cardinality);
        LENGTH = this.UB.get();
    }

    public BitsetIntVarImpl(String name, int min, int max, Solver solver) {
        super(name, solver);
        solver.associates(this);
        IEnvironment env = solver.getEnvironment();
        this.OFFSET = min;
        int capacity = max - min + 1;
        this.VALUES = env.makeBitSet(capacity);
        for (int i = 0; i <= max - min; i++) {
            this.VALUES.set(i);
        }
        this.LB = env.makeInt(0);
        this.UB = env.makeInt(max - min);
        this.SIZE = env.makeInt(capacity);
        LENGTH = capacity;
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
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
//        records.forEach(beforeModification.set(this, EventType.REMOVE, cause));
        assert cause != null;
        ICause antipromo = cause;
        int aValue = value - OFFSET;
        boolean change = aValue >= 0 && aValue <= LENGTH && VALUES.get(aValue);
        if (change) {
            if (SIZE.get() == 1) {
                if (Configuration.PLUG_EXPLANATION){
                    solver.getExplainer().removeValue(this, value, antipromo);
                }
//            monitors.forEach(onContradiction.set(this, EventType.REMOVE, cause));
                this.contradiction(cause, EventType.REMOVE, MSG_REMOVE);
            }
            EventType e = EventType.REMOVE;
            this.VALUES.clear(aValue);
            this.SIZE.add(-1);
            if (reactOnRemoval) {
                delta.add(aValue + OFFSET, cause);
            }

            if (value == getLB()) {
                LB.set(VALUES.nextSetBit(aValue));
                e = EventType.INCLOW;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            } else if (value == getUB()) {
                UB.set(VALUES.prevSetBit(aValue));
                e = EventType.DECUPP;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            assert !VALUES.isEmpty();
            if (this.instantiated()) {
                e = EventType.INSTANTIATE;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            this.notifyPropagators(e, cause);
            if (Configuration.PLUG_EXPLANATION){
                solver.getExplainer().removeValue(this, value, antipromo);
            }
        }
        return change;
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
        else {     // TODO: really ugly .........
            boolean anyChange = false;
            for (int v = this.nextValue(from - 1); v <= to; v = nextValue(v)) {
                anyChange |= removeValue(v, cause);
            }
            return anyChange;
        }
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
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
        assert cause != null;
        if (this.instantiated()) {
            int cvalue = this.getValue();
            if (value != cvalue) {
                if (Configuration.PLUG_EXPLANATION){
                    solver.getExplainer().instantiateTo(this, value, cause, cvalue, cvalue);
                }
                this.contradiction(cause, EventType.INSTANTIATE, MSG_INST);
            }
            return false;
        } else if (contains(value)) {
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
            int oldLB = 0;
            int oldUB = 0;
            if (Configuration.PLUG_EXPLANATION) {
                oldLB = getLB(); // call getter to avoid adding OFFSET..
                oldUB = getUB();
            }

            this.VALUES.clear();
            this.VALUES.set(aValue);
            this.LB.set(aValue);
            this.UB.set(aValue);
            this.SIZE.set(1);

            if (VALUES.isEmpty()) {
                this.contradiction(cause, EventType.INSTANTIATE, MSG_EMPTY);
            }
            if (Configuration.PLUG_EXPLANATION){
                solver.getExplainer().instantiateTo(this, value, cause, oldLB, oldUB);
            }
            this.notifyPropagators(EventType.INSTANTIATE, cause);
            return true;
        } else {
            if (Configuration.PLUG_EXPLANATION){
                solver.getExplainer().instantiateTo(this, value, cause, getLB(), getUB());
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
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
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

                int aValue = value - OFFSET;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = old - OFFSET; i < aValue; i = VALUES.nextSetBit(i + 1)) {
                        delta.add(i + OFFSET, cause);
                    }
                }
                VALUES.clear(old - OFFSET, aValue);
                LB.set(VALUES.nextSetBit(aValue));
				assert SIZE.get()>VALUES.cardinality();
                SIZE.set(VALUES.cardinality());
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
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
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
                int aValue = value - OFFSET;
                if (reactOnRemoval) {
                    //BEWARE: this loop significantly decreases performances
                    for (int i = old - OFFSET; i > aValue; i = VALUES.prevSetBit(i - 1)) {
                        delta.add(i + OFFSET, cause);
                    }
                }
                VALUES.clear(aValue + 1, old - OFFSET + 1);
                UB.set(VALUES.prevSetBit(aValue));
				assert SIZE.get()>VALUES.cardinality();
                SIZE.set(VALUES.cardinality());
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
        aValue -= OFFSET;
        return aValue >= 0 && this.VALUES.get(aValue);
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
        return this.LB.get() + OFFSET;
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    public int getUB() {
        return this.UB.get() + OFFSET;
    }

    public int getDomainSize() {
        return SIZE.get();
    }

    public int nextValue(int aValue) {
        aValue -= OFFSET;
        int lb = LB.get();
        if (aValue < 0 || aValue < lb) return lb + OFFSET;
        aValue = VALUES.nextSetBit(aValue + 1);
        if (aValue > -1) return aValue + OFFSET;
        return Integer.MAX_VALUE;
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
    public boolean hasEnumeratedDomain() {
        return true;
    }

    @Override
    public IEnumDelta getDelta() {
        return delta;
    }

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
            delta = new EnumDelta(solver.getSearchLoop());
            reactOnRemoval = true;
        }
    }

    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new EnumDeltaMonitor(delta, propagator);
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
        return new AntiDomBitset(this);
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
//        System.out.println("BitsetIntVarImpl.explain " + this + invdom +  " expl: " + expl);
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
        return new BitsetIntVarImpl(StringUtils.randomName(this.name), this.OFFSET, this.VALUES.copy(), this.getSolver());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

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
        if (_riterator == null || !_riterator.isReusable()) {
            _riterator = new DisposableRangeIterator() {

                int from;
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
}
