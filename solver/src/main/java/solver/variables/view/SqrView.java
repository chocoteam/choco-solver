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

import choco.kernel.common.util.iterators.DisposableRangeIterator;
import choco.kernel.common.util.iterators.DisposableValueIterator;
import choco.kernel.common.util.procedure.IntProcedure;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.monitor.IntDeltaMonitor;
import solver.variables.delta.view.ViewDelta;


/**
 * declare an IntVar based on X, such |X|
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 09/08/11
 */
public final class SqrView extends View<IntVar> {

    protected DisposableValueIterator _viterator;
    protected DisposableRangeIterator _riterator;

    public SqrView(final IntVar var, Solver solver) {
        super("(" + var.getName() + "^2)", var, solver);
    }

    @Override
    public void analyseAndAdapt(int mask) {
        super.analyseAndAdapt(mask);
        if (!reactOnRemoval && ((modificationEvents & EventType.REMOVE.mask) != 0)) {
            var.analyseAndAdapt(mask);
            delta = new ViewDelta(new IntDeltaMonitor(var.getDelta(), this) {
                @Override
                public void forEach(IntProcedure proc, EventType eventType) throws ContradictionException {
                    if (EventType.isRemove(eventType.mask)) {
                        for (int i = frozenFirst; i < frozenLast; i++) {
                            if (propagator != delta.getCause(i)) {
                                int v = delta.get(i);
                                if (!var.contains(-v)) {
                                    boolean found = false;
                                    for (int j = i + 1; !found && j < frozenLast; j++) {
                                        if (delta.get(j) == -v) {
                                            found = true;
                                        }
                                    }
                                    if (!found) {
                                        proc.execute(v * v);
                                    }
                                }

                            }
                        }
                    }
                }
            });
            reactOnRemoval = true;
        }
    }

    @Override
    public boolean instantiated() {
        if (var.instantiated()) {
            return true;
        } else {
            if (var.getDomainSize() == 2 && Math.abs(var.getLB()) == var.getUB()) {
                return true;
            }
        }
        return false;
    }

    private static int floor_sqrt(int n) {
        if (n < 0)
            return 0;
        return (int) Math.sqrt(n);
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        records.forEach(beforeModification.set(this, EventType.REMOVE, cause));
        if (value < 0) {
            return false;
        }
        int rootV = floor_sqrt(value);
        if (rootV * rootV == value) { // is a perfect square ?
            int inf = getLB();
            int sup = getUB();
            EventType evt = EventType.REMOVE;
            if (value == inf) {
                evt = EventType.INCLOW;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            } else if (value == sup) {
                evt = EventType.DECUPP;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            boolean done = var.removeValue(-rootV, this);
            done |= var.removeValue(rootV, this);
            if (instantiated()) {
                evt = EventType.INSTANTIATE;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            if (done) {
                notifyMonitors(evt, cause);
            }
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause);
        } else {
            from = floor_sqrt(from);
            to = floor_sqrt(to);
            boolean done = var.removeInterval(-to, -from, cause);
            done |= var.removeInterval(from, to, cause);
            if (done) {
                notifyMonitors(EventType.REMOVE, cause);
            }
            return done;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        records.forEach(beforeModification.set(this, EventType.INSTANTIATE, cause));
        if (value < 0) {
            //TODO: explication?
            this.contradiction(cause, EventType.INSTANTIATE, AbstractVariable.MSG_UNKNOWN);
        }
        int v = floor_sqrt(value);
        if (v * v == value) { // is a perfect square ?
            boolean done = var.updateLowerBound(-v, this);
            done |= var.updateUpperBound(v, this);
            EventType evt = EventType.DECUPP;
            if (var.hasEnumeratedDomain()) {
                done |= var.removeInterval(-v + 1, v - 1, cause);
                evt = EventType.INSTANTIATE;
            }
            if (done) {
                notifyMonitors(evt, cause);
            }
            return done;
        } else { //otherwise, impossible value for instantiation
            //TODO: explication?
            this.contradiction(cause, EventType.INSTANTIATE, AbstractVariable.MSG_UNKNOWN);
        }

        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        records.forEach(beforeModification.set(this, EventType.INCLOW, cause));
        if (value <= 0) {
            return false;
        }
        int floorV = floor_sqrt(value);
        boolean done = var.removeInterval(-floorV + 1, floorV - 1, this);
        if (done) {
            EventType evt = EventType.INCLOW;
            if (instantiated()) {
                evt = EventType.INSTANTIATE;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            notifyMonitors(evt, cause);
        }
        return done;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        records.forEach(beforeModification.set(this, EventType.DECUPP, cause));
        if (value < 0) {
            //TODO: explication?
            this.contradiction(cause, EventType.DECUPP, AbstractVariable.MSG_UNKNOWN);
        }
        int floorV = floor_sqrt(value);
        boolean done = var.updateLowerBound(-floorV, this);
        done |= var.updateUpperBound(floorV, this);
        if (done) {
            EventType evt = EventType.DECUPP;
            if (instantiated()) {
                evt = EventType.INSTANTIATE;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            notifyMonitors(evt, cause);
        }
        return done;
    }

    @Override
    public boolean contains(int value) {
        return var.contains(value) || var.contains(-value);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return var.instantiatedTo(value) || var.instantiatedTo(-value);
    }

    @Override
    public int getValue() {
        return getLB();
    }

    @Override
    public int getLB() {
        if (var.contains(0)) {
            return 0;
        }
        int elb = var.getLB();
        if (elb > 0) {
            return elb * elb;
        }
        int eub = var.getUB();
        if (eub < 0) {
            return eub * eub;
        }
        int l = var.previousValue(0);
        int u = var.nextValue(0);
        if (-l < u) {
            return l * l;
        } else {
            return u * u;
        }
    }

    @Override
    public int getUB() {
        int elb = var.getLB();
        int eub = var.getUB();
        int mm = -elb;
        if (elb < 0) {
            if (eub > 0 && eub > mm) {
                mm = eub;
            }
        } else {
            mm = eub;
        }
        return mm * mm;
    }

    @Override
    public int nextValue(int v) {
        if (v < 0 && var.contains(0)) {
            return 0;
        }
        int floorV = floor_sqrt(v);
        int l = var.previousValue(-floorV);
        if (l == Integer.MIN_VALUE) {
            l = Integer.MAX_VALUE;
        } else {
            l = Math.abs(l);
        }
        int u = var.nextValue(floorV);
        int min = Math.min(l, Math.abs(u));
        if (min == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return min * min;
    }

    @Override
    public int previousValue(int v) {
        if (v < 0) {
            return Integer.MIN_VALUE;
        }
        int floorV = floor_sqrt(v);
        if (floorV * floorV != v) {
            floorV++;
        }
        int l = var.nextValue(-floorV);
        if (l == Integer.MIN_VALUE) {
            l = Integer.MAX_VALUE;
        } else {
            l = Math.abs(l);
        }
        int u = var.previousValue(floorV);
        int max = Math.max(l, Math.abs(u));
        if (max == Integer.MAX_VALUE) {
            return Integer.MIN_VALUE;
        }
        return max * max;
    }

    @Override
    public String toString() {
        return "(" + this.var.toString() + "^2) = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return true;
    }

    @Override
    public int getType() {
        return Variable.INTEGER;
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
    }


    @Override
    public Explanation explain(VariableState what) {
        return var.explain(VariableState.DOM);
    }

    @Override
    public Explanation explain(VariableState what, int val) {
        int fl = floor_sqrt(val);
        Explanation explanation = new Explanation();
        explanation.add(var.explain(what, fl));
        explanation.add(var.explain(what, -fl));
        return explanation;
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                DisposableValueIterator u2l;
                DisposableValueIterator l2u;
                int vl2u;
                int vu2l;

                @Override
                public void bottomUpInit() {
                    l2u = var.getValueIterator(true);
                    u2l = var.getValueIterator(false);

                    super.bottomUpInit();
                    while (l2u.hasNext()) {
                        this.vl2u = l2u.next();
                        if (this.vl2u >= 0) break;
                    }
                    while (u2l.hasPrevious()) {
                        this.vu2l = u2l.previous();
                        if (this.vu2l <= 0) break;
                    }
                }

                @Override
                public void topDownInit() {
                    l2u = var.getValueIterator(true);
                    u2l = var.getValueIterator(false);

                    super.topDownInit();
                    if (l2u.hasNext()) {
                        this.vl2u = l2u.next();
                    }
                    if (u2l.hasPrevious()) {
                        this.vu2l = u2l.previous();
                    }
                }

                @Override
                public boolean hasNext() {
                    return this.vl2u < Integer.MAX_VALUE || this.vu2l > -Integer.MAX_VALUE;
                }

                @Override
                public boolean hasPrevious() {
                    return this.vl2u <= 0 || this.vu2l >= 0;
                }

                @Override
                public int next() {
                    int min = this.vl2u < -this.vu2l ? this.vl2u : -this.vu2l;
                    if (this.vl2u == min) {
                        if (this.l2u.hasNext()) {
                            this.vl2u = l2u.next();
                        } else {
                            this.vl2u = Integer.MAX_VALUE;
                        }
                    }
                    if (-this.vu2l == min) {
                        if (this.u2l.hasPrevious()) {
                            this.vu2l = u2l.previous();
                        } else {
                            this.vu2l = -Integer.MAX_VALUE;
                        }
                    }
                    return min * min;
                }

                @Override
                public int previous() {
                    int max = -this.vl2u > this.vu2l ? -this.vl2u : this.vu2l;
                    if (-this.vl2u == max && this.l2u.hasNext()) {
                        this.vl2u = this.l2u.next();
                    }
                    if (this.vu2l == max && this.u2l.hasPrevious()) {
                        this.vu2l = u2l.previous();
                    }
                    return max * max;
                }

                @Override
                public void dispose() {
                    super.dispose();
                    l2u.dispose();
                    u2l.dispose();
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

                DisposableRangeIterator u2l;
                DisposableRangeIterator l2u;
                int ml2u;
                int Ml2u;
                int mu2l;
                int Mu2l;
                int value;
                int bound;

                @Override
                public void bottomUpInit() {
                    l2u = var.getRangeIterator(true);
                    u2l = var.getRangeIterator(false);

                    super.bottomUpInit();
                    ml2u = Ml2u = mu2l = Mu2l = Integer.MAX_VALUE;

                    while (l2u.hasNext()) {
                        if (l2u.min() >= 0) {
                            ml2u = l2u.min();
                            Ml2u = l2u.max();
                            l2u.next();
                            break;
                        }
                        if (l2u.max() >= 0) {
                            ml2u = 0;
                            Ml2u = l2u.max();
                            l2u.next();
                            break;
                        }
                        l2u.next();
                    }
                    while (u2l.hasPrevious()) {
                        if (u2l.max() <= 0) {
                            Mu2l = -u2l.min();
                            mu2l = -u2l.max();
                            u2l.previous();
                            break;
                        }
                        if (u2l.min() <= 0) {
                            mu2l = 0;
                            Mu2l = -u2l.min();
                            u2l.previous();
                            break;
                        }
                        u2l.previous();
                    }
                    _next();
                }

                @Override
                public void topDownInit() {
                    l2u = var.getRangeIterator(true);
                    u2l = var.getRangeIterator(false);

                    super.topDownInit();
                    ml2u = Ml2u = mu2l = Mu2l = Integer.MIN_VALUE;

                    if (l2u.hasNext()) {
                        if (l2u.max() <= 0) {
                            this.ml2u = -l2u.max();
                            this.Ml2u = -l2u.min();
                        } else if (l2u.min() <= 0) {
                            this.ml2u = 0;
                            this.Ml2u = -l2u.min();
                        }
                        l2u.next();
                    }
                    if (u2l.hasPrevious()) {
                        if (u2l.min() >= 0) {
                            this.mu2l = u2l.min();
                            this.Mu2l = u2l.max();
                        } else if (u2l.max() >= 0) {
                            this.mu2l = 0;
                            this.Mu2l = u2l.max();
                        }
                        u2l.previous();
                    }
                    _previous();
                }

                @Override
                public boolean hasNext() {
                    if (value < bound) {
                        value++;
                    }
                    return value < Integer.MAX_VALUE;
                }

                @Override
                public boolean hasPrevious() {
                    if (value > bound) {
                        value--;
                    }
                    return value > Integer.MIN_VALUE;
                }

                @Override
                public void next() {
                    if (value >= bound) {
                        _next();
                    }
                }


                private void _next() {
                    value = bound = Integer.MAX_VALUE;
                    // disjoint ranges
                    if (Ml2u < mu2l - 1) {
                        value = ml2u - 1; //-1 due to hasNext()
                        bound = Ml2u;
                        if (l2u.hasNext()) {
                            ml2u = l2u.min();
                            Ml2u = l2u.max();
                            l2u.next();
                        } else {
                            ml2u = Integer.MAX_VALUE;
                            Ml2u = Integer.MAX_VALUE;
                        }
                    } else if (Mu2l < ml2u - 1) {
                        value = mu2l - 1; //-1 due to hasNext()
                        bound = Mu2l;
                        if (u2l.hasPrevious()) {
                            Mu2l = -u2l.min();
                            mu2l = -u2l.max();
                            u2l.previous();
                        } else {
                            mu2l = Integer.MAX_VALUE;
                            Mu2l = Integer.MAX_VALUE;
                        }
                    } else {
                        // we build the current range
                        if (Ml2u + 1 == mu2l) {
                            value = ml2u - 1; //-1 due to hasNext()
                            bound = Mu2l;
                        } else if (Mu2l + 1 == ml2u) {
                            value = mu2l - 1; //-1 due to hasNext()
                            bound = Ml2u;
                        } else {
                            value = ml2u < mu2l ? ml2u : mu2l;
                            bound = Ml2u < Mu2l ? Ml2u : Mu2l;
                        }
                        boolean change;
                        do {
                            change = false;
                            if (value < ml2u && ml2u <= bound) {
                                bound = bound > Ml2u ? bound : Ml2u;
                                if (l2u.hasNext()) {
                                    ml2u = l2u.min();
                                    Ml2u = l2u.max();
                                    l2u.next();
                                    change = true;
                                } else {
                                    ml2u = Integer.MAX_VALUE;
                                    Ml2u = Integer.MAX_VALUE;
                                }
                            }
                            if (value < mu2l && mu2l <= bound) {
                                bound = bound > Mu2l ? bound : Mu2l;
                                if (u2l.hasPrevious()) {
                                    Mu2l = -u2l.min();
                                    mu2l = -u2l.max();
                                    u2l.previous();
                                    change = true;
                                } else {
                                    mu2l = Integer.MAX_VALUE;
                                    Mu2l = Integer.MAX_VALUE;
                                }
                            }
                        } while (change);
                    }
                }

                @Override
                public void previous() {
                    if (value <= bound) {
                        _previous();
                    }
                }

                private void _previous() {
                    value = bound = Integer.MIN_VALUE;
                    // disjoint ranges
                    if (ml2u > Mu2l + 1) {
                        bound = ml2u;
                        value = Ml2u + 1; //+1 due to hasPrevious()
                        ml2u = Integer.MIN_VALUE;
                        Ml2u = Integer.MIN_VALUE;
                        if (l2u.hasNext()) {
                            //la: gérer le 0 et les autres cas
                            if (l2u.max() <= 0) {
                                this.ml2u = -l2u.max();
                                this.Ml2u = -l2u.min();
                            } else if (l2u.min() <= 0) {
                                this.ml2u = 0;
                                this.Ml2u = -l2u.min();
                            }
                            l2u.next();
                        }
                    } else if (Mu2l + 1 < ml2u) {
                        bound = mu2l;
                        value = Mu2l + 1; //+1 due to hasPrevious()
                        mu2l = Integer.MIN_VALUE;
                        Mu2l = Integer.MIN_VALUE;
                        if (u2l.min() >= 0) {
                            this.mu2l = u2l.min();
                            this.Mu2l = u2l.max();
                        } else if (u2l.max() >= 0) {
                            this.mu2l = 0;
                            this.Mu2l = u2l.max();
                        }
                    } else {
                        // we build the current range
                        if (Ml2u + 1 == mu2l) {
                            bound = ml2u;
                            value = Mu2l + 1; //+1 due to hasPrevious()
                        } else if (Mu2l + 1 == ml2u) {
                            bound = mu2l;
                            value = Ml2u + 1; //+1 due to hasPrevious()
                        } else {
                            bound = ml2u > mu2l ? ml2u : mu2l;
                            value = (Ml2u > Mu2l ? Ml2u : Mu2l) + 1; //+1 due to hasPrevious()
                        }
                        boolean change;
                        do {
                            change = false;
                            if (bound <= Ml2u && Ml2u < value) {
                                bound = bound < ml2u ? bound : ml2u;
                                ml2u = Integer.MIN_VALUE;
                                Ml2u = Integer.MIN_VALUE;
                                if (l2u.hasNext()) {
                                    if (l2u.max() <= 0) {
                                        this.ml2u = -l2u.max();
                                        this.Ml2u = -l2u.min();
                                    } else if (l2u.min() <= 0) {
                                        this.ml2u = 0;
                                        this.Ml2u = -l2u.min();
                                    }
                                    l2u.next();
                                    change = true;
                                }
                            }
                            if (bound <= mu2l && mu2l < value) {
                                bound = bound < mu2l ? bound : mu2l;
                                mu2l = Integer.MIN_VALUE;
                                Mu2l = Integer.MIN_VALUE;
                                if (u2l.hasPrevious()) {
                                    if (u2l.min() >= 0) {
                                        this.mu2l = u2l.min();
                                        this.Mu2l = u2l.max();
                                    } else if (u2l.max() >= 0) {
                                        this.mu2l = 0;
                                        this.Mu2l = u2l.max();
                                    }
                                    u2l.previous();
                                    change = true;
                                }
                            }
                        } while (change);
                    }
                }

                @Override
                public int min() {
                    return value * value;
                }

                @Override
                public int max() {
                    return value * value;
                }

                @Override
                public void dispose() {
                    super.dispose();
                    l2u.dispose();
                    u2l.dispose();
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


    @Override
    public void transformEvent(EventType evt, ICause cause) throws ContradictionException {
        if ((evt.mask & EventType.BOUND.mask) != 0) {
            if (instantiated()) { // specific case where DOM_SIZE = 2 and LB = -UB
                notifyMonitors(EventType.INSTANTIATE, cause);
            } else { // otherwise, we do not know the previous values, so its hard to tell wether it is LB or UB mod
                notifyMonitors(EventType.BOUND, cause);
            }
        } else {
            notifyMonitors(evt, cause);
        }
    }
}
