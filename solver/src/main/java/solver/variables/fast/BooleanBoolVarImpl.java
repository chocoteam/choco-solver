/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables.fast;

import choco.kernel.ESat;
import choco.kernel.common.util.iterators.DisposableRangeBoundIterator;
import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueBoundIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import choco.kernel.memory.structure.IndexedBipartiteSet;
import com.sun.istack.internal.NotNull;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.OffsetIStateBitset;
import solver.explanations.VariableState;
import solver.requests.IRequestWithVariable;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.AbstractVariable;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
import solver.variables.delta.NoDelta;
import solver.variables.delta.OneValueDelta;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class BooleanBoolVarImpl extends AbstractVariable implements BoolVar {

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

    IntDelta delta = NoDelta.singleton;

    protected boolean reactOnRemoval = false;

    protected HeuristicVal heuristicVal;

    private DisposableValueIterator _viterator;

    private DisposableRangeIterator _riterator;

    //////////////////////////////////////////////////////////////////////////////////////

    public BooleanBoolVarImpl(String name, Solver solver) {
        super(name, solver);
        notInstanciated = solver.getEnvironment().getSharedBipartiteSetForBooleanVars();
        this.offset = solver.getEnvironment().getNextOffset();
        mValue = 0;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        this.heuristicVal = heuristicVal;
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return heuristicVal;
    }

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
     * @param value       value to remove from the domain (int)
     * @param cause       removal releaser
     * @param informCause
     * @return true if the value has been removed, false otherwise
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    public boolean removeValue(int value, ICause cause, boolean informCause) throws ContradictionException {
        if (value == 0)
            return instantiateTo(1, cause, informCause);
        else if (value == 1)
            return instantiateTo(0, cause, informCause);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeInterval(int from, int to, ICause cause, boolean informCause) throws ContradictionException {
        if (from <= getLB())
            return updateLowerBound(to + 1, cause, informCause);
        else if (getUB() <= to)
            return updateUpperBound(from - 1, cause, informCause);
        else if (hasEnumeratedDomain()) {     // TODO: really ugly .........
            boolean anyChange = false;
            for (int v = this.nextValue(from - 1); v <= to; v = nextValue(v)) {
                anyChange |= removeValue(v, cause, informCause);
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
     * @param value       instantiation value (int)
     * @param cause       instantiation releaser
     * @param informCause
     * @return true if the instantiation is done, false otherwise
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    public boolean instantiateTo(int value, ICause cause, boolean informCause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
        requests.forEach(beforeModification.set(this, EventType.INSTANTIATE, cause));
        solver.getExplainer().instantiateTo(this, value, cause);
        if (informCause) {
            cause = Cause.Null;
        }
        if (this.instantiated()) {
            if (value != this.getValue()) {
                this.contradiction(cause, EventType.INSTANTIATE, MSG_INST);
            }
            return false;
        } else {
            if (value == 0 || value == 1) {
                EventType e = EventType.INSTANTIATE;
                notInstanciated.contains(value);
                notInstanciated.remove(offset);
                if (reactOnRemoval) {
                    delta.add(1 - value);
                }
                mValue = value;
                this.notifyMonitors(e, cause);
                return true;
            } else {
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
     * @param value       new lower bound (included)
     * @param cause       updating releaser
     * @param informCause
     * @return true if the lower bound has been updated, false otherwise
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    public boolean updateLowerBound(int value, ICause cause, boolean informCause) throws ContradictionException {
        return value > 0 && instantiateTo(value, cause, informCause);
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
     * @param value       new upper bound (included)
     * @param cause       update releaser
     * @param informCause
     * @return true if the upper bound has been updated, false otherwise
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    public boolean updateUpperBound(int value, ICause cause, boolean informCause) throws ContradictionException {
        return value < 1 && instantiateTo(value, cause, informCause);
    }

    @Override
    public boolean setToTrue(ICause cause, boolean informCause) throws ContradictionException {
        return instantiateTo(1, cause, informCause);
    }

    @Override
    public boolean setToFalse(ICause cause, boolean informCause) throws ContradictionException {
        return instantiateTo(0, cause, informCause);
    }

    public boolean instantiated() {
        return !notInstanciated.contains(offset);
    }

    @Override
    public boolean instantiatedTo(int aValue) {
        return !notInstanciated.contains(offset) && mValue == aValue;
    }

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
    public int getUB() {
        if (!notInstanciated.contains(offset)) {
            return mValue;
        }
        return 1;
    }

    public int getDomainSize() {
        return (notInstanciated.contains(offset) ? 2 : 1);
    }

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
    public IntDelta getDelta() {
        return delta;
    }

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
    public void updatePropagationConditions(Propagator propagator, int idxInProp) {
        modificationEvents |= propagator.getPropagationConditions(idxInProp);
        if (!reactOnRemoval && ((modificationEvents & EventType.REMOVE.mask) != 0)) {
            delta = new OneValueDelta();
            reactOnRemoval = true;
        }
//        reactOnRemoval |= ((modificationEvents & EventType.REMOVE.mask) != 0);
    }

    @Override
    public void attachPropagator(Propagator propagator, int idxInProp) {
        IRequestWithVariable<BoolVar> request = propagator.makeRequest(this, idxInProp);
        propagator.addRequest(request);
        this.addMonitor(request);
    }

    public void notifyMonitors(EventType event, @NotNull ICause cause) throws ContradictionException {
        if ((modificationEvents & event.mask) != 0) {
            requests.forEach(afterModification.set(this, event, cause));
        }
        notifyViews(event, cause);
    }

    /**
     * {@inheritDoc}
     *
     * @param what
     */
    @Override
    public Explanation explain(VariableState what) {
        Explanation expl = new Explanation(null, null);
        OffsetIStateBitset invdom = solver.getExplainer().getRemovedValues(this);
        DisposableValueIterator it = invdom.getValueIterator();
        while (it.hasNext()) {
            int val = it.next();
            if ((what == VariableState.LB && val < this.getLB())
                    || (what == VariableState.UB && val > this.getUB())
                    || (what == VariableState.DOM)) {
                expl.add(solver.getExplainer().explain(this, val));
            }
        }
        return expl;
    }

    @Override
    public Explanation explain(VariableState what, int val) {
        Explanation expl = new Explanation();
        expl.add(solver.getExplainer().explain(this, val));
        return expl;
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        requests.forEach(onContradiction.set(this, event, cause));
        engine.fails(cause, this, message);
    }

    @Override
    public int getType() {
        return Variable.INTEGER;
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
