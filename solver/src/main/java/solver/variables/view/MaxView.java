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
package solver.variables.view;

import choco.kernel.common.util.iterators.BoundedIntIterator;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;

import static solver.variables.AbstractVariable.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/09/11
 */
public class MaxView extends AbstractView {

    protected BoundedIntIterator _iterator;


    public MaxView(IntVar a, IntVar b, Solver solver) {
        super(a, b, solver);
        int lb = Math.max(A.getLB(), B.getLB());
        int ub = Math.max(A.getUB(), B.getUB());
        LB.set(lb);
        UB.set(ub);
        SIZE.set(ub - lb + 1);
    }

    /////////////// SERVICES REQUIRED FROM VIEW //////////////////////////

    @Override
    public void backPropagate(int mask) throws ContradictionException {
        // one of the variable as changed externally, this involves a complete update of this
        // one of the variable as changed externally, this involves a complete update of this
        if (!EventType.isRemove(mask)) {
            int lA = A.getLB(), uA = A.getUB();
            int lB = B.getLB(), uB = B.getUB();

            int elb = Math.max(lA, lB);
            int eub = Math.max(uA, uB);

            int ilb = LB.get();
            int iub = UB.get();
            boolean change = false;
            EventType e = EventType.VOID;
            if (elb > ilb) {
                if (elb > iub) {
                    solver.explainer.updateLowerBound(this, ilb, elb, this);
                    this.contradiction(this, MSG_LOW);
                }
                SIZE.add(elb - ilb);
                ilb = elb;
                LB.set(ilb);
                e = EventType.INCLOW;
                change = true;
            }
            if (eub < iub) {
                if (eub < ilb) {
                    solver.explainer.updateUpperBound(this, iub, eub, this);
                    this.contradiction(this, MSG_LOW);
                }
                SIZE.add(eub - iub);
                iub = eub;
                UB.set(iub);
                if (e != EventType.VOID) {
                    e = EventType.BOUND;
                } else {
                    e = EventType.DECUPP;
                }
                change |= true;
            }
            if (ilb > iub) {
                solver.explainer.updateLowerBound(this, ilb, ilb, this);
                solver.explainer.updateUpperBound(this, iub, iub, this);
                this.contradiction(this, MSG_EMPTY);
            }
            if (change) {
                if (ilb == iub) {
                    notifyPropagators(EventType.INSTANTIATE, this);
                    solver.explainer.instantiateTo(this, ilb, this);
                } else {
                    notifyPropagators(e, this);
                    solver.explainer.updateLowerBound(this, ilb, ilb, this);
                    solver.explainer.updateUpperBound(this, iub, iub, this);
                }
            }
        }
    }

    @Override
    public String getName() {
        return String.format("max(%s,%s)", A, B);
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        int inf = getLB();
        int sup = getUB();
        if (value == inf && value == sup) {
            solver.explainer.removeValue(this, value, cause);
            this.contradiction(cause, AbstractVariable.MSG_REMOVE);
        } else if (inf == value || value == sup) {
            EventType e;
            if (value == inf) {
                // todo: delta...
                LB.set(value + 1);
                e = EventType.INCLOW;
                cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                if (A.getLB() > B.getUB()) {
                    A.updateLowerBound(value + 1, this);
                }
                if (B.getLB() > A.getUB()) {
                    B.updateLowerBound(value + 1, this);
                }
            } else {
                // todo: delta...
                UB.set(value - 1);
                e = EventType.DECUPP;
                cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                A.updateUpperBound(value - 1, this);
                B.updateUpperBound(value - 1, this);
            }
            if (SIZE.get() > 0) {
                if (this.instantiated()) {
                    e = EventType.INSTANTIATE;
                    cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                }
                this.notifyPropagators(e, cause);
            } else if (SIZE.get() == 0) {
                solver.explainer.removeValue(this, value, cause);
                this.contradiction(cause, MSG_EMPTY);
            }
            solver.explainer.removeValue(this, value, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause);
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        if (this.instantiated()) {
            if (value != this.getValue()) {
                solver.explainer.instantiateTo(this, value, cause);
                this.contradiction(cause, MSG_INST);
            }
            return false;
        } else if (contains(value)) {
            //todo: delta
            this.LB.set(value);
            this.UB.set(value);
            this.SIZE.set(1);

            A.updateUpperBound(value, this);
            B.updateUpperBound(value, this);
            if (!A.contains(value)) {
                B.instantiateTo(value, this);
            }
            if (!B.contains(value)) {
                A.instantiateTo(value, this);
            }

            this.notifyPropagators(EventType.INSTANTIATE, cause);
            solver.explainer.instantiateTo(this, value, cause);
            return true;
        } else {
            solver.explainer.instantiateTo(this, value, cause);
            this.contradiction(cause, MSG_UNKNOWN);
            return false;
        }
    }

    @Override
    public boolean updateLowerBound(int aValue, ICause cause) throws ContradictionException {
        int old = this.getLB();
        if (old < aValue) {
            if (this.getUB() < aValue) {
                solver.explainer.updateLowerBound(this, old, aValue, cause);
                this.contradiction(cause, MSG_LOW);
            } else {
                EventType e = EventType.INCLOW;
                //todo delta
                SIZE.add(old - aValue);
                LB.set(aValue);

                if (A.getLB() > B.getUB()) {
                    A.updateLowerBound(aValue, this);
                }
                if (B.getLB() > A.getUB()) {
                    B.updateLowerBound(aValue, this);
                }

                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                }
                this.notifyPropagators(e, cause);

                solver.explainer.updateLowerBound(this, old, aValue, cause);
                return true;

            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int aValue, ICause cause) throws ContradictionException {
        int old = this.getUB();
        if (old > aValue) {
            if (this.getLB() > aValue) {
                solver.explainer.updateUpperBound(this, old, aValue, cause);
                this.contradiction(cause, MSG_UPP);
            } else {
                EventType e = EventType.DECUPP;
                //todo delta
                SIZE.add(aValue - old);
                UB.set(aValue);

                A.updateUpperBound(aValue, this);
                B.updateUpperBound(aValue, this);

                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    cause = (cause != Cause.Null && cause.reactOnPromotion() ? Cause.Null : cause);
                }
                this.notifyPropagators(e, cause);
                solver.explainer.updateUpperBound(this, old, aValue, cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(int value) {
        return A.contains(value) || B.contains(value);
    }

    @Override
    public int nextValue(int aValue) {
        int lb = getLB();
        if (aValue < lb) {
            return lb;
        } else if (aValue < getUB()) {
            return aValue + 1;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int previousValue(int aValue) {
        int ub = getUB();
        if (aValue > ub) {
            return ub;
        } else if (aValue > getLB()) {
            return aValue - 1;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public DisposableIntIterator getLowUppIterator() {
        if (_iterator == null || !_iterator.isReusable()) {
            _iterator = new BoundedIntIterator();
        }
        _iterator.init(this.LB.get(), this.UB.get());
        return _iterator;
    }

    @Override
    public DisposableIntIterator getUppLowIterator() {
        if (_iterator == null || !_iterator.isReusable()) {
            _iterator = new BoundedIntIterator();
        }
        _iterator.init(this.UB.get(), this.LB.get());
        return _iterator;
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return false;
    }
}
