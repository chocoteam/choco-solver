/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

import gnu.trove.map.hash.THashMap;
import org.chocosolver.memory.structure.BasicIndexedBipartiteSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.ISolver;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.delta.OneValueDelta;
import org.chocosolver.solver.variables.delta.monitor.OneValueDeltaMonitor;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.ranges.IRemovals;
import org.chocosolver.util.ESat;
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
public class BoolVarImpl extends AbstractVariable implements BoolVar {

    private static final long serialVersionUID = 1L;

    /**
     * The offset, that is the minimal value of the domain (stored at index 0).
     * Thus the entry at index i corresponds to x=i+offset).
     */
    protected final int offset;

    /**
     * indicate the value of the domain : false = 0, true = 1
     */
    protected int mValue;

    /**
     * A bi partite set indicating for each value whether it is present or not.
     * If the set contains the domain, the variable is not instanciated.
     */

    protected final BasicIndexedBipartiteSet notInstanciated;

    IEnumDelta delta = NoDelta.singleton;

    protected boolean reactOnRemoval = false;

    private DisposableValueIterator _viterator;

    private DisposableRangeIterator _riterator;

    private BoolVar not;

    //////////////////////////////////////////////////////////////////////////////////////

    public BoolVarImpl(String name, ISolver isolver) {
        super(name, isolver);
        notInstanciated = this.solver.getEnvironment().getSharedBipartiteSetForBooleanVars();
        this.offset = notInstanciated.add();
        mValue = 0;
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
        if (value == 0)
            return instantiateTo(1, cause);
        else if (value == 1)
            return instantiateTo(0, cause);
        return false;
    }

    @Override
    public boolean removeValues(IRemovals values, ICause cause) throws ContradictionException {
        int olb = getLB();
        int oub = getUB();
        int nlb = values.nextValue(olb - 1);
        int nub = values.previousValue(oub + 1);
        boolean hasChanged = false;
        if (nlb == olb) {
            // look for the new lb
            do {
                olb = nextValue(olb);
                nlb = values.nextValue(nlb);
            } while (olb < Integer.MAX_VALUE && oub < Integer.MAX_VALUE && nlb == olb);
            // the new lower bound is now known,  delegate to the right method
            hasChanged = updateLowerBound(olb, cause);
        } else if (nlb > oub) {
            return false;
        }
        if (nub == oub) {
            // look for the new ub
            do {
                oub = previousValue(oub);
                nub = values.previousValue(nub);
            } while (olb > Integer.MIN_VALUE && oub > Integer.MIN_VALUE && nub == oub);
            // the new upper bound is now known, delegate to the right method
            hasChanged |= updateUpperBound(oub, cause);
        } else if (nub < olb) {
            return hasChanged;
        }
        assert nlb >= nub;
        return hasChanged;
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
        else {
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
     * @throws ContradictionException if the domain become empty due to this action
     */
    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
//        records.forEachRemVal(beforeModification.set(this, EventType.INSTANTIATE, cause));
        assert cause != null;
        if (this.isInstantiated()) {
            int cvalue = this.getValue();
            if (value != cvalue) {
                if (_plugexpl) {
                    solver.getEventObserver().instantiateTo(this, value, cause, cvalue, cvalue);
                }
                this.contradiction(cause, IntEventType.INSTANTIATE, MSG_INST);
            }
            return false;
        } else {
            if (value == 0 || value == 1) {
                IntEventType e = IntEventType.INSTANTIATE;
                assert notInstanciated.contains(offset);
                notInstanciated.swap(offset);
                if (reactOnRemoval) {
                    delta.add(1 - value, cause);
                }
                mValue = value;
                if (_plugexpl) {
                    solver.getEventObserver().instantiateTo(this, value, cause, 0, 1);
                }
                this.notifyPropagators(e, cause);
                return true;
            } else {
                if (_plugexpl) {
                    solver.getEventObserver().instantiateTo(this, value, cause, 0, 1);
                }
                this.contradiction(cause, IntEventType.INSTANTIATE, MSG_UNKNOWN);
                return false;
            }
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
        return value > 0 && instantiateTo(value, cause);
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
        return value < 1 && instantiateTo(value, cause);
    }

    @Override
    public boolean setToTrue(ICause cause) throws ContradictionException {
        assert cause != null;
        return instantiateTo(1, cause);
    }

    @Override
    public boolean setToFalse(ICause cause) throws ContradictionException {
        assert cause != null;
        return instantiateTo(0, cause);
    }

    @Override
    public boolean isInstantiated() {
        return !notInstanciated.contains(offset);
    }

    @Override
    public boolean isInstantiatedTo(int aValue) {
        return !notInstanciated.contains(offset) && mValue == aValue;
    }

    @Override
    public boolean contains(int aValue) {
        if (!notInstanciated.contains(offset)) {
            return mValue == aValue;
        }
        return aValue == 0 || aValue == 1;
    }

    /**
     * Retrieves the current value of the variable if instantiated, otherwier the lower bound.
     *
     * @return the current value (or lower bound if not yet instantiated).
     */
    @Override
    public int getValue() {
        return getLB();
    }

    @Override
    public ESat getBooleanValue() {
        if (isInstantiated()) {
            return ESat.eval(getLB() != 0);
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
        if (!notInstanciated.contains(offset)) {
            return mValue;
        }
        return 0;
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    @Override
    public int getUB() {
        if (!notInstanciated.contains(offset)) {
            return mValue;
        }
        return 1;
    }

    @Override
    public int getDomainSize() {
        return (notInstanciated.contains(offset) ? 2 : 1);
    }

    @Override
    public int nextValue(int v) {
        if (!notInstanciated.contains(offset)) {
            final int val = mValue;
            return (val > v) ? val : Integer.MAX_VALUE;
        } else {
            if (v < 0) return 0;
            if (v == 0) return 1;
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int previousValue(int v) {
        if (v > getUB()) return getUB();
        if (v > getLB()) return getLB();
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

    @Override
    public String toString() {
        if (!notInstanciated.contains(offset)) {
            return this.name + " = " + Integer.toString(mValue);
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
            delta = new OneValueDelta(solver.getEnvironment());
            reactOnRemoval = true;
        }
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new OneValueDeltaMonitor(delta, propagator);
    }

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void contradiction(ICause cause, IEventType event, String message) throws ContradictionException {
        assert cause != null;
//        records.forEachRemVal(onContradiction.set(this, event, cause));
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return VAR | BOOL;
    }

    @Override
    public BoolVar duplicate() {
        return VariableFactory.bool(StringUtils.randomName(this.name), isolver);
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            BoolVarImpl clone = new BoolVarImpl(this.name, solver);
            identitymap.put(this, clone);
            if (this.not != null && identitymap.containsKey(this.not)) {
                clone._setNot((BoolVar) identitymap.get(this.not));
                clone.not._setNot(clone);
            }
            for (int i = mIdx - 1; i >= 0; i--) {
                monitors[i].duplicate(solver, identitymap);
            }
        }
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

    @Override
    public void _setNot(BoolVar neg) {
        this.not = neg;
    }

    @Override
    public BoolVar not() {
        if (!hasNot()) {
            not = VF.not(this);
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

    private boolean isNot = false;

    @Override
    public boolean isNot() {
        return isNot;
    }

    @Override
    public void setNot(boolean isNot) {
        this.isNot = isNot;
    }
}
