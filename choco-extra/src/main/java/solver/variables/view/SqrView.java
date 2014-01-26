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

package solver.variables.view;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueIterator;


/**
 * declare an IntVar based on X, such |X|
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * BEWARE: floor and ceil !!
 * @author Charles Prud'homme
 * @since 09/08/11
 */
public final class SqrView extends IntView {

    public SqrView(IntVar var, Solver solver) {
        super("(" + var.getName() + "^2)", var, solver);
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
//            throw new UnsupportedOperationException();
        }
        return new ViewDeltaMonitor(var.monitorDelta(propagator), propagator) {

            @Override
            protected void filter() {
                int[] _values = values.toArray();
                values.clear();
                for (int i = 0; i < _values.length; i++) {
                    int v = _values[i];
                    if (!var.contains(-v)) {
                        boolean found = false;
                        for (int j = i + 1; !found && j < _values.length; j++) {
                            if (_values[j] == -v) {
                                found = true;
                            }
                        }
                        if (!found) {
                            values.add(v);
                        }
                    }
                }
            }

            @Override
            protected int transform(int value) {
                return value * value;
            }
        };
    }

    @Override
    public boolean isInstantiated() {
        if (var.isInstantiated()) {
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
        assert cause != null;
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
            } else if (value == sup) {
                evt = EventType.DECUPP;
            }
            boolean done = var.removeValue(-rootV, this);
            done |= var.removeValue(rootV, this);
            if (isInstantiated()) {
                evt = EventType.INSTANTIATE;
            }
            if (done) {
                notifyPropagators(evt, cause);
            }
        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        assert cause != null;
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause);
        } else {
            from = floor_sqrt(from);
            to = floor_sqrt(to);
            boolean done = var.removeInterval(-to, -from, this);
            done |= var.removeInterval(from, to, this);
            if (done) {
                notifyPropagators(EventType.REMOVE, cause);
            }
            return done;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int v = floor_sqrt(value);
        if (v * v == value) { // is a perfect square ?
            boolean done = var.updateLowerBound(-v, this);
            done |= var.updateUpperBound(v, this);
            EventType evt = EventType.DECUPP;
            if (var.hasEnumeratedDomain()) {
                done |= var.removeInterval(-v + 1, v - 1, this);
                evt = EventType.INSTANTIATE;
            }
            if (done) {
                notifyPropagators(evt, cause);
            }
            return done;
        } else { //otherwise, impossible value for instantiation
            wipeOut(cause);
        }

        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        if (value <= 0) {
            return false;
        }
        int floorV = floor_sqrt(value);
        boolean done = var.removeInterval(-floorV + 1, floorV - 1, this);
        if (done) {
            EventType evt = EventType.INCLOW;
            if (isInstantiated()) {
                evt = EventType.INSTANTIATE;
            }
            notifyPropagators(evt, cause);
        }
        return done;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        if (value < 0) {
            wipeOut(cause);
        }
        int floorV = floor_sqrt(value);
        boolean done = var.updateLowerBound(-floorV, this);
        done |= var.updateUpperBound(floorV, this);
        if (done) {
            EventType evt = EventType.DECUPP;
            if (isInstantiated()) {
                evt = EventType.INSTANTIATE;
            }
            notifyPropagators(evt, cause);
        }
        return done;
    }

    @Override
    public boolean contains(int value) {
        value = floor_sqrt(value);
        return var.contains(value) || var.contains(-value);
    }

    @Override
    public boolean instantiatedTo(int value) {
        value = floor_sqrt(value);
        if (var.contains(value) || var.contains(-value)) {
            return var.isInstantiated() ||
                    (var.getDomainSize() == 2 && Math.abs(var.getLB()) == var.getUB());          //<nj> fixed SQR bug
        }
        return false;
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
    public int getDomainSize() {
        return var.getDomainSize();
    }

    @Override
    public IntVar duplicate() {
        return new SqrView(this.var, solver);
    }

    @Override
    public void explain(VariableState what, Explanation to) {
        var.explain(VariableState.DOM, to);
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        int fl = floor_sqrt(val);
        var.explain(what, fl, to);
        var.explain(what, -fl, to);
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
                    if (-this.vl2u == max) {
                        if (this.l2u.hasNext()) {
                            this.vl2u = this.l2u.next();
                        } else {
                            this.vl2u = 1;
                        }
                    }
                    if (this.vu2l == max) {
                        if (this.u2l.hasPrevious()) {
                            this.vu2l = u2l.previous();
                        } else {
                            this.vu2l = -1;
                        }
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

                DisposableValueIterator vit;
                int min
                        ,
                        max;

                @Override
                public void bottomUpInit() {
                    vit = getValueIterator(true);
                    if (vit.hasNext()) {
                        min = vit.next();
                    }
                    if (min == 0 && vit.hasNext() && contains(1)) {
                        max = vit.next();
                    } else {
                        max = min;
                    }

                }

                @Override
                public void topDownInit() {
                    vit = getValueIterator(false);
                    if (vit.hasPrevious()) {
                        max = vit.previous();
                    }
                    if (max == 1 && vit.hasPrevious() && contains(0)) {
                        min = vit.previous();
                    } else {
                        min = max;
                    }
                }

                @Override
                public boolean hasNext() {
                    return min != Integer.MAX_VALUE;
                }

                @Override
                public boolean hasPrevious() {
                    return max != -Integer.MAX_VALUE;
                }

                @Override
                public void next() {
                    if (vit.hasNext()) {
                        min = max = vit.next();
                    } else {
                        min = Integer.MAX_VALUE;
                    }
                }

                @Override
                public void previous() {
                    if (vit.hasPrevious()) {
                        max = vit.previous();
                        if (max == 1 && vit.hasPrevious() && contains(0)) {
                            min = vit.previous();
                        } else {
                            min = max;
                        }
                    } else {
                        max = -Integer.MAX_VALUE;
                    }
                }

                @Override
                public int min() {
                    return min;
                }

                @Override
                public int max() {
                    return max;
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
            if (isInstantiated()) { // specific case where DOM_SIZE = 2 and LB = -UB
                notifyPropagators(EventType.INSTANTIATE, this);
            } else { // otherwise, we do not know the previous values, so its hard to tell whether it is LB or UB mod
                notifyPropagators(EventType.BOUND, this);
            }
        } else {
            notifyPropagators(evt, this);
        }
    }
}
