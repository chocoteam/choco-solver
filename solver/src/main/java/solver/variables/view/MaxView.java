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

import choco.kernel.common.util.iterators.DisposableRangeBoundIterator;
import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueBoundIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;

import static solver.variables.AbstractVariable.*;

/**
 * declare an IntVar based on X and Y, such max(X,Y)
 * <br/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 01/09/11
 */
public class MaxView extends AbstractView {


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
                this.contradiction(this, MSG_EMPTY);
            }
            if (change) {
                if (ilb == iub) {
                    notifyPropagators(EventType.INSTANTIATE, this);
                } else {
                    notifyPropagators(e, this);
                }
            }
        }
    }

    @Override
    public String getName() {
        return String.format("max(%s,%s)", A, B);
    }

    @Override
    public boolean removeValue(int value, ICause cause, boolean informCause) throws ContradictionException {
        ICause antipromo = cause;
        if (informCause) {
            cause = Cause.Null;
        }
        int inf = getLB();
        int sup = getUB();
        if (value == inf && value == sup) {
            this.contradiction(cause, AbstractVariable.MSG_REMOVE);
        } else if (inf == value || value == sup) {
            EventType e;
            if (value == inf) {
                // todo: delta...
                LB.set(value + 1);
                e = EventType.INCLOW;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
                if (A.getLB() > B.getUB()) {
                    A.updateLowerBound(value + 1, this, false);
                }
                if (B.getLB() > A.getUB()) {
                    B.updateLowerBound(value + 1, this, false);
                }
            } else {
                // todo: delta...
                UB.set(value - 1);
                e = EventType.DECUPP;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
                A.updateUpperBound(value - 1, this, false);
                B.updateUpperBound(value - 1, this, false);
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
                this.contradiction(cause, MSG_EMPTY);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause, boolean informCause) throws ContradictionException {
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause, informCause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause, informCause);
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause, boolean informCause) throws ContradictionException {
        if (informCause) {
            cause = Cause.Null;
        }
        if (this.instantiated()) {
            if (value != this.getValue()) {
                this.contradiction(cause, MSG_INST);
            }
            return false;
        } else if (contains(value)) {
            //todo: delta
            this.LB.set(value);
            this.UB.set(value);
            this.SIZE.set(1);

            A.updateUpperBound(value, this, false);
            B.updateUpperBound(value, this, false);
            if (!A.contains(value)) {
                B.instantiateTo(value, this, false);
            }
            if (!B.contains(value)) {
                A.instantiateTo(value, this, false);
            }

            this.notifyPropagators(EventType.INSTANTIATE, cause);
            return true;
        } else {
            this.contradiction(cause, MSG_UNKNOWN);
            return false;
        }
    }

    @Override
    public boolean updateLowerBound(int aValue, ICause cause, boolean informCause) throws ContradictionException {
        ICause antipromo = cause;
        if (informCause) {
            cause = Cause.Null;
        }
        int old = this.getLB();
        if (old < aValue) {
            if (this.getUB() < aValue) {
                this.contradiction(cause, MSG_LOW);
            } else {
                EventType e = EventType.INCLOW;
                //todo delta
                SIZE.add(old - aValue);
                LB.set(aValue);

                if (A.getLB() > B.getUB()) {
                    A.updateLowerBound(aValue, this, false);
                }
                if (B.getLB() > A.getUB()) {
                    B.updateLowerBound(aValue, this, false);
                }

                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                this.notifyPropagators(e, cause);
                return true;

            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int aValue, ICause cause, boolean informCause) throws ContradictionException {
        ICause antipromo = cause;
        if (informCause) {
            cause = Cause.Null;
        }
        int old = this.getUB();
        if (old > aValue) {
            if (this.getLB() > aValue) {
                this.contradiction(cause, MSG_UPP);
            } else {
                EventType e = EventType.DECUPP;
                //todo delta
                SIZE.add(aValue - old);
                UB.set(aValue);

                A.updateUpperBound(aValue, this, false);
                B.updateUpperBound(aValue, this, false);

                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                this.notifyPropagators(e, cause);
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
    public boolean hasEnumeratedDomain() {
        return false;
    }
}
