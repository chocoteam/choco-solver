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

package solver.variables.impl;

import memory.structure.IndexedBipartiteSet;
import solver.Configuration;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.explanations.antidom.AntiDomBool;
import solver.explanations.antidom.AntiDomain;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.VF;
import solver.variables.VariableFactory;
import solver.variables.delta.IEnumDelta;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import solver.variables.delta.OneValueDelta;
import solver.variables.delta.monitor.OneValueDeltaMonitor;
import util.ESat;
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
public final class BoolVarImpl extends AbstractVariable implements BoolVar {

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

    protected final IndexedBipartiteSet notInstanciated;

    IEnumDelta delta = NoDelta.singleton;

    protected boolean reactOnRemoval = false;

    private DisposableValueIterator _viterator;

    private DisposableRangeIterator _riterator;

    private BoolVar not;

    //////////////////////////////////////////////////////////////////////////////////////

    public BoolVarImpl(String name, Solver solver) {
        super(name, solver);
        notInstanciated = solver.getEnvironment().getSharedBipartiteSetForBooleanVars();
        this.offset = solver.getEnvironment().getNextOffset();
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
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
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
        else if (hasEnumeratedDomain()) {     // TODO: really ugly .........
            boolean anyChange = false;
            for (int v = this.nextValue(from - 1); v <= to; v = nextValue(v)) {
                anyChange |= removeValue(v, cause);
            }
            return anyChange;
        } else {
            return false;
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
	@Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
//        records.forEach(beforeModification.set(this, EventType.INSTANTIATE, cause));
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
        } else {
            if (value == 0 || value == 1) {
                EventType e = EventType.INSTANTIATE;
                assert notInstanciated.contains(offset);
                notInstanciated.remove(offset);
                if (reactOnRemoval) {
                    delta.add(1 - value, cause);
                }
                mValue = value;
                if (Configuration.PLUG_EXPLANATION) {
                    solver.getExplainer().instantiateTo(this, value, cause, 0, 1);
                }
                this.notifyPropagators(e, cause);
                return true;
            } else {
                if (Configuration.PLUG_EXPLANATION) {
                    solver.getExplainer().instantiateTo(this, value, cause, 0, 1);
                }
                this.contradiction(cause, EventType.INSTANTIATE, MSG_UNKNOWN);
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
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
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
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
	@Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        return value < 1 && instantiateTo(value, cause);
    }

    @Override
    public void wipeOut(ICause cause) throws ContradictionException {
        assert cause != null;
        removeInterval(0, 1, cause);
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
    public boolean instantiated() {
        return !notInstanciated.contains(offset);
    }

    @Override
    public boolean instantiatedTo(int aValue) {
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
        if (instantiated()) {
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
            delta = new OneValueDelta(solver.getSearchLoop());
            reactOnRemoval = true;
        }
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        return new OneValueDeltaMonitor(delta, propagator);
    }

	@Override
    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        notifyMonitors(event);
        if ((modificationEvents & event.mask) != 0) {
            //records.forEach(afterModification.set(this, event, cause));
            solver.getEngine().onVariableUpdate(this, event, cause);
        }
        notifyViews(event, cause);
    }

	@Override
    public void notifyMonitors(EventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public AntiDomain antiDomain() {
        return new AntiDomBool(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param what
     * @param to
     */
    @Override
    public void explain(VariableState what, Explanation to) {
        AntiDomain invdom = solver.getExplainer().getRemovedValues(this);
        DisposableValueIterator it = invdom.getValueIterator();
        while (it.hasNext()) {
            int val = it.next();
            if ((what == VariableState.LB && val < this.getLB())
                    || (what == VariableState.UB && val > this.getUB())
                    || (what == VariableState.DOM)) {
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
        return VAR | BOOL;
    }

    @Override
    public BoolVar duplicate() {
        return VariableFactory.bool(StringUtils.randomName(this.name), this.getSolver());
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
        if (not == null) {
            not = VF.not(this);
            not._setNot(this);
        }
        return not;
    }

    @Override
    public boolean isLit() {
        return true;
    }

    @Override
    public boolean isNot() {
        return false;
    }
}
