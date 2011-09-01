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

package solver.variables;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.requests.IRequest;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.delta.IntDelta;
import solver.variables.domain.IIntDomain;

import java.util.BitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 nov. 2010
 */
public final class IntVarImpl extends AbstractVariable implements IntVar {

    private static final long serialVersionUID = 1L;

    /**
     * Domain definition
     */
    protected IIntDomain domain;

    protected boolean reactOnRemoval = false;

    protected HeuristicVal heuristicVal;

    private DisposableIntIterator _iterator;

    //////////////////////////////////////////////////////////////////////////////////////

    protected IntVarImpl(String name, Solver solver) {
        super(name, solver);
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
     * @param value value to remove from the domain (int)
     * @param cause removal releaser
     * @return true if the value has been removed, false otherwise
     * @throws solver.exception.ContradictionException
     *          if the domain become empty due to this action
     */
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
        boolean change = false;
        int inf = domain.getLB();
        int sup = domain.getUB();
        if (value == inf && value == sup) {
            solver.explainer.removeValue(this, value, cause);
            this.contradiction(cause, MSG_REMOVE);
        } else {
            if (inf <= value && value <= sup) {
                EventType e = EventType.REMOVE;
                if (value == inf) {
                    if (reactOnRemoval) {
                        change = this.domain.updateLowerBoundAndDelta(value + 1);
                    } else {
                        change = this.domain.updateLowerBound(value + 1);
                    }
                    e = EventType.INCLOW;
                    cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                } else if (value == sup) {
                    if (reactOnRemoval) {
                        change = this.domain.updateUpperBoundAndDelta(value - 1);
                    } else {
                        change = this.domain.updateUpperBound(value - 1);
                    }
                    e = EventType.DECUPP;
                    cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                } else {
                    if (reactOnRemoval) {
                        change = this.domain.removeAndUpdateDelta(value);
                    } else {
                        change = this.domain.remove(value);
                    }
                }
                if (change && !this.domain.empty()) {
                    if (this.instantiated()) {
                        e = EventType.INSTANTIATE;
                        cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                    }
                    this.notifyPropagators(e, cause);
                } else {
                    if (this.domain.empty()) {
                        solver.explainer.removeValue(this, value, cause);
                        this.contradiction(cause, MSG_EMPTY);
                    }
                }
            }
        }
        if (change) {
            solver.explainer.removeValue(this, value, cause);
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= domain.getLB())
            return updateLowerBound(to + 1, cause);
        else if (domain.getUB() <= to)
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
     * @throws ContradictionException if the domain become empty due to this action
     */
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
        if (this.instantiated()) {
            if (value != this.getValue()) {
                solver.explainer.instantiateTo(this, value, cause);
                this.contradiction(cause, MSG_INST);
            }
            return false;
        } else if (contains(value)) {
            EventType e = EventType.INSTANTIATE;
            if (reactOnRemoval) {
                this.domain.restrictAndUpdateDelta(value);
            } else {
                this.domain.restrict(value);
            }
            if (this.domain.empty()) {
                solver.explainer.removeValue(this, value, cause);
                this.contradiction(cause, MSG_EMPTY);
            }
            this.notifyPropagators(e, cause);
            solver.explainer.instantiateTo(this, value, cause);
            return true;
        } else {
            solver.explainer.instantiateTo(this, value, cause);
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
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
        boolean change;
        int old = this.getLB();
        if (old < value) {
            if (this.getUB() < value) {
                solver.explainer.updateLowerBound(this, old, value, cause);
                this.contradiction(cause, MSG_LOW);
            } else {
                EventType e = EventType.INCLOW;
                if (reactOnRemoval) {
                    change = domain.updateLowerBoundAndDelta(value);
                } else {
                    change = domain.updateLowerBound(value);
                }
                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                }
                assert (change);
                this.notifyPropagators(e, cause);

                solver.explainer.updateLowerBound(this, old, value, cause);
                return change;

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
        // BEWARE: THIS CODE SHOULD NOT BE MOVED TO THE DOMAIN TO NOT DECREASE PERFORMANCES!
        boolean change;
        int old = this.getUB();
        if (old > value) {
            if (this.getLB() > value) {
                solver.explainer.updateUpperBound(this, old, value, cause);
                this.contradiction(cause, MSG_UPP);
            } else {
                EventType e = EventType.DECUPP;
                if (reactOnRemoval) {
                    change = domain.updateUpperBoundAndDelta(value);
                } else {
                    change = domain.updateUpperBound(value);
                }
                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                }
                assert (change);
                this.notifyPropagators(e, cause);
                solver.explainer.updateUpperBound(this, old, value, cause);
                return change;
            }
        }
        return false;
    }

    public boolean instantiated() {
        return this.domain.instantiated();
    }

    @Override
    public boolean instantiatedTo(int value) {
        return this.domain.instantiated() && this.domain.contains(value);
    }

    public boolean contains(int value) {
        return this.domain.contains(value);
    }

    /**
     * Retrieves the current value of the variable if instantiated, otherwier the lower bound.
     *
     * @return the current value (or lower bound if not yet instantiated).
     */
    public int getValue() {
        assert instantiated() : name + " not instantiated";
        return this.domain.getLB();
    }

    /**
     * Retrieves the lower bound of the variable
     *
     * @return the lower bound
     */
    public int getLB() {
        return this.domain.getLB();
    }

    /**
     * Retrieves the upper bound of the variable
     *
     * @return the upper bound
     */
    public int getUB() {
        return this.domain.getUB();
    }

    public int getDomainSize() {
        return this.domain.getSize();
    }

    public int nextValue(int v) {
        if (v < getLB()) {
            return getLB();
        } else {
            return this.domain.nextValue(v);
        }
    }

    @Override
    public int previousValue(int v) {
        if (v > getUB()) {
            return getUB();
        } else {
            return this.domain.previousValue(v);
        }
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return domain.isEnumerated();
    }

    @Override
    public IntDelta getDelta() {
        return domain.getDelta();
    }

    public String toString() {
        return this.name +/* '[' + this.nbConstraints() + "]:*/"=" + this.domain.toString();
    }

    ////////////////////////////////////////////////////////////////
    ///// methode liees au fait qu'une variable est observable /////
    ////////////////////////////////////////////////////////////////

    @Override
    public void updatePropagationConditions(Propagator observer, int idxInProp) {
        modificationEvents |= observer.getPropagationConditions(idxInProp);
        if (!reactOnRemoval && ((modificationEvents & EventType.REMOVE.mask) != 0)) {
            domain.recordRemoveValues();
            reactOnRemoval = true;
        }
    }

    @Override
    public void attachPropagator(Propagator propagator, int idxInProp) {
        IRequest<IntVar> request = propagator.makeRequest(this, idxInProp);
        propagator.addRequest(request);
        this.addRequest(request);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Explanation explain() {
        Explanation expl = new Explanation(null, null);
        BitSet invdom = solver.explainer.getRemovedValues(this);
        int val = invdom.nextSetBit(0);
        while (val != -1) {
            expl.add(solver.explainer.explain(this, val));
            val = invdom.nextSetBit(val + 1);
        }
        return expl;
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        engine.fails(cause, this, message);
    }


    @Override
    public int getType() {
        return Variable.INTEGER;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DisposableIntIterator getLowUppIterator() {
        if (_iterator == null || !_iterator.isReusable()) {
            _iterator = new DisposableIntIterator() {

                int value;
                int ub;

                @Override
                public void init() {
                    super.init();
                    this.value = getLB();
                    this.ub = getUB();
                }

                @Override
                public boolean hasNext() {
                    return this.value < ub;
                }

                @Override
                public int next() {
                    int old = value;
                    value = domain.nextValue(this.value);
                    return old;
                }
            };
        }
        _iterator.init();
        return _iterator;
    }

    @Override
    public DisposableIntIterator getUppLowIterator() {
        if (_iterator == null || !_iterator.isReusable()) {
            _iterator = new DisposableIntIterator() {

                int value;
                int lb;

                @Override
                public void init() {
                    super.init();
                    this.value = getUB();
                    this.lb = getLB();
                }

                @Override
                public boolean hasNext() {
                    return this.value > lb;
                }

                @Override
                public int next() {
                    int old = value;
                    value = domain.previousValue(this.value);
                    return old;
                }
            };
        }
        _iterator.init();
        return _iterator;
    }
}
