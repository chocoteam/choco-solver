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

import choco.kernel.memory.IStateBitSet;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;

import static solver.variables.AbstractVariable.*;

/**
 * View for A+B, where A and B are IntVar or views, ensure bound consistency
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public final class BitsetXYSumView extends AbstractSumView {

    final int OFFSET;

    final IStateBitSet VALUES;

    public BitsetXYSumView(IntVar a, IntVar b, Solver solver) {
        super(a, b, solver);
        int lbA = A.getLB();
        int ubA = A.getUB();
        int lbB = B.getLB();
        int ubB = B.getUB();
        OFFSET = lbA + lbB;
        VALUES = solver.getEnvironment().makeBitSet((ubA + ubB) - (lbA + lbB) + 1);
        //TODO: improve
        for (int i = lbA; i <= ubA; i = A.nextValue(i)) {
            for (int j = lbB; j <= ubB; j = B.nextValue(j)) {
                VALUES.set(i + j - OFFSET);
            }
        }
        SIZE = solver.getEnvironment().makeInt(VALUES.cardinality());
    }

    /////////////// SERVICES REQUIRED FROM INTVAR //////////////////////////

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        boolean change = false;
        int inf = getLB();
        int sup = getUB();
        if (value == inf && value == sup) {
            solver.explainer.removeValue(this, value, cause);
            this.contradiction(cause, MSG_REMOVE);
        } else {
            if (inf <= value && value <= sup) {
                EventType e = EventType.REMOVE;

                int aValue = value - OFFSET;
                change = VALUES.get(aValue);
                this.VALUES.clear(aValue);
                if (change) {
                    SIZE.add(-1);
                    //todo delta
                }

                if (value == inf) {
                    inf = VALUES.nextSetBit(aValue) + OFFSET;
                    LB.set(inf);
                    e = EventType.INCLOW;
                    filterOnGeq(cause, inf);
                    cause = (cause != null && cause.reactOnPromotion() ? null : cause);
                } else if (value == sup) {
                    sup = VALUES.prevSetBit(aValue) + OFFSET;
                    UB.set(sup);
                    e = EventType.DECUPP;
                    filterOnLeq(cause, sup);
                    cause = (cause != null && cause.reactOnPromotion() ? null : cause);
                }
                if (change && !VALUES.isEmpty()) {
                    if (this.instantiated()) {
                        e = EventType.INSTANTIATE;
                        cause = (cause != null && cause.reactOnPromotion() ? null : cause);
                    }
                    this.notifyPropagators(e, cause);
                } else {
                    if (VALUES.isEmpty()) {
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

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        int lb = getLB();
        if (from <= lb && lb <= to) {
            return updateLowerBound(to + 1, cause);
        }
        int ub = getUB();
        if (from <= ub && ub <= to) {
            return updateUpperBound(from - 1, cause);
        }
        //otherwise, it's a hole in the middle:
        boolean change = false;
        for (int v = this.nextValue(from - 1); v <= to; v = nextValue(v)) {
            change |= VALUES.get(v - OFFSET);
            VALUES.clear(v - OFFSET);
        }
        if (change) {
            this.notifyPropagators(EventType.REMOVE, cause);
        }
        return change;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        int lb = LB.get();
        if (this.instantiated()) {
            if (value != lb) {
                solver.explainer.instantiateTo(this, value, cause);
                this.contradiction(cause, MSG_EMPTY);
            }
            return false;
        } else if (contains(value)) {

            int aValue = value - OFFSET;
            //todo delta
            this.VALUES.clear();
            this.VALUES.set(aValue);
            this.LB.set(value);
            this.UB.set(value);
            this.SIZE.set(1);

            if (VALUES.isEmpty()) {
                solver.explainer.removeValue(this, value, cause);
                this.contradiction(cause, MSG_EMPTY);
            }

            filterOnLeq(cause, value);
            filterOnGeq(cause, value);

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
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        boolean change;
        int lb = this.getLB();
        if (lb < value) {
            if (this.getUB() < value) {
                solver.explainer.updateLowerBound(this, lb, value, cause);
                this.contradiction(cause, MSG_LOW);
            } else {
                EventType e = EventType.INCLOW;

                int aValue = value - OFFSET;
                //todo delta
                VALUES.clear(lb - OFFSET, aValue);
                lb = VALUES.nextSetBit(aValue) + OFFSET;
                LB.set(lb);
                int _size = SIZE.get();
                int card = VALUES.cardinality();
                SIZE.set(card);
                change = _size - card > 0;

                filterOnGeq(cause, lb);

                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    cause = (cause != null && cause.reactOnPromotion() ? null : cause);
                }
                this.notifyPropagators(e, cause);

                solver.explainer.updateLowerBound(this, lb, value, cause);
                return change;
            }
        }
        return false;
    }


    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        boolean change;
        int ub = this.getUB();
        if (ub > value) {
            if (this.getLB() > value) {
                solver.explainer.updateUpperBound(this, ub, value, cause);
                this.contradiction(cause, MSG_UPP);
            } else {
                EventType e = EventType.DECUPP;
                int aValue = value - OFFSET;
                //todo delta
                VALUES.clear(aValue + 1, ub - OFFSET + 1);
                ub = VALUES.prevSetBit(aValue) + OFFSET;
                UB.set(ub);

                int _size = SIZE.get();
                int card = VALUES.cardinality();
                SIZE.set(card);
                change = _size - card > 0;

                filterOnLeq(cause, ub);

                if (card == 1) {
                    e = EventType.INSTANTIATE;
                    cause = (cause != null && cause.reactOnPromotion() ? null : cause);
                }
                this.notifyPropagators(e, cause);
                solver.explainer.updateUpperBound(this, ub, value, cause);
                return change;
            }
        }
        return false;
    }


    @Override
    public boolean contains(int aValue) {
        // based on "Bounds Consistency Techniques for Long Linear Constraint"
        aValue -= OFFSET;
        return aValue >= 0 && VALUES.get(aValue);
    }

    @Override
    public int getDomainSize() {
        return VALUES.cardinality();
    }

    @Override
    public int nextValue(int aValue) {
        // based on "Bounds Consistency Techniques for Long Linear Constraint"
        // we only check bounds of A and B, and consider VALUES inside as bounds as existing ones
        // what if lb > Integer.MAX_VALUE...
        int lb = getLB();
        if (aValue < lb) {
            return lb;
        } else if (aValue < getUB()) {
            return VALUES.nextSetBit(aValue - OFFSET + 1) + OFFSET;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int previousValue(int aValue) {
        // based on "Bounds Consistency Techniques for Long Linear Constraint"
        // we only check bounds of A and B, and consider VALUES inside as bounds as existing ones
        // what if ub > Integer.MAX_VALUE...
        int ub = getUB();
        if (aValue > ub) {
            return ub;
        } else if (aValue > getLB()) {
            return VALUES.prevSetBit(aValue - OFFSET - 1) + OFFSET;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return true;
    }

    @Override
    public String toString() {
        if (instantiated()) {
            return String.format("(%s + %s) = %d", A.getName(), B.getName(), getValue());
        } else {
            StringBuilder s = new StringBuilder(20);
            s.append('{').append(getLB());
            int nb = 5;
            for (int i = nextValue(getLB()); i < Integer.MAX_VALUE && nb > 0; i = nextValue(i)) {
                s.append(',').append(i);
                nb--;
            }
            if (nb == 0) {
                s.append("...,").append(this.getUB());
            }
            s.append('}');

            return String.format("(%s + %s) = %s", A.getName(), B.getName(), s.toString());
        }
    }

    /////////////// SERVICES REQUIRED FROM SUM //////////////////////////

    @Override
    public void backPropagate(int mask) throws ContradictionException {
        // one of the variable as changed externally, this involves a complete update of this
        if (!EventType.isRemove(mask)) {
            int elb = A.getLB() + B.getLB();
            int eub = A.getUB() + B.getUB();
            int ilb = LB.get();
            int iub = UB.get();
            boolean up = false, down = false;
            EventType e = EventType.VOID;
            if (elb > ilb) {
                VALUES.clear(ilb - OFFSET, elb - OFFSET);
                ilb = VALUES.nextSetBit(ilb - OFFSET) + OFFSET;
                if (ilb == OFFSET - 1) {
                    solver.explainer.updateLowerBound(this, LB.get(), elb, Cause.Null);
                    this.contradiction(Cause.Null, MSG_EMPTY);
                }
                LB.set(ilb);
                e = EventType.INCLOW;
                down = true;
            }
            if (eub < iub) {
                VALUES.clear(eub - OFFSET + 1, iub - OFFSET + 1);
                iub = VALUES.prevSetBit(iub - OFFSET + 1) + OFFSET;
                if (iub == OFFSET - 1) {
                    solver.explainer.updateUpperBound(this, UB.get(), eub, Cause.Null);
                    this.contradiction(null, MSG_EMPTY);
                }
                UB.set(iub);
                if (e != EventType.VOID) {
                    e = EventType.BOUND;
                } else {
                    e = EventType.DECUPP;
                }
                up = true;
            }
            if (ilb > iub) {
                solver.explainer.updateLowerBound(this, ilb, ilb, Cause.Null);
                solver.explainer.updateUpperBound(this, iub, iub, Cause.Null);
                this.contradiction(Cause.Null, MSG_EMPTY);
            }
            if (down) {
                filterOnLeq(null, iub);
            }
            if (up) {
                filterOnGeq(null, ilb);
            }
            if (ilb == iub) {
                notifyPropagators(EventType.INSTANTIATE, Cause.Null);
                solver.explainer.instantiateTo(this, ilb, Cause.Null);
            } else {
                notifyPropagators(e, Cause.Null);
                solver.explainer.updateLowerBound(this, ilb, ilb, Cause.Null);
                solver.explainer.updateUpperBound(this, iub, iub, Cause.Null);
            }
        }
    }
}
