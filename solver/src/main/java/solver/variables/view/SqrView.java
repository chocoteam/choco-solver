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
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.requests.ViewRequestWrapper;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.AbstractVariable;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
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

    final IntDelta delta;

    protected HeuristicVal heuristicVal;

    protected DisposableValueIterator _viterator;
    protected DisposableRangeIterator _riterator;

    public SqrView(final IntVar var, Solver solver) {
        super("(" + var.getName() + "^2)", var, solver);
        delta = new ViewDelta(var.getDelta()) {

            @Override
            public void add(int value) {
                var.getDelta().add(value);
                var.getDelta().add(-value);
            }
        };
    }

    @Override
    public void attachPropagator(Propagator propagator, int idxInProp) {
        ViewRequestWrapper req = new ViewRequestWrapper(propagator.makeRequest(var, idxInProp),
                ViewRequestWrapper.Modifier.ABS);
        propagator.addRequest(req);
        var.addRequest(req);
    }

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        LoggerFactory.getLogger("solver").warn("AbsView#setHeuristicVal: wrong usage");
        this.heuristicVal = heuristicVal;
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return heuristicVal;
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

    // http://stackoverflow.com/questions/295579/fastest-way-to-determine-if-an-integers-square-root-is-an-integer
    private static int floor_sqrt(int n) {
        if (n < 0)
            return 0;
        return (int) Math.sqrt(n);
        /*
        switch (n & 0x3F) {
            case 0x00:
            case 0x01:
            case 0x04:
            case 0x09:
            case 0x10:
            case 0x11:
            case 0x19:
            case 0x21:
            case 0x24:
            case 0x29:
            case 0x31:
            case 0x39:
                int sqrt;
                if (n < 410881) {
                    //John Carmack hack, converted to Java.
                    // See: http://www.codemaestro.com/reviews/9
                    int i;
                    float x2, y;

                    x2 = n * 0.5F;
                    y = n;
                    i = Float.floatToRawIntBits(y);
                    i = 0x5f3759df - (i >> 1);
                    y = Float.intBitsToFloat(i);
                    y = y * (1.5F - (x2 * y * y));

                    sqrt = (int) (1.0F / y);
                } else {
                    //Carmack hack gives incorrect answer for n >= 410881.
                    sqrt = (int) Math.sqrt(n);
                }
                return sqrt;

            default:
                return  (int) Math.sqrt(n);
        }*/
    }

    @Override
    public boolean removeValue(int value, ICause cause, boolean informCause) throws ContradictionException {
        if (value < 0) {
            return false;
        }
        int rootV = floor_sqrt(value);
        boolean done = false;
        if (rootV * rootV == value) { // is a perfect square ?
            done = var.removeValue(-rootV, cause, informCause);
            done |= var.removeValue(rootV, cause, informCause);
        }
        return done;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause, boolean informCause) throws ContradictionException {
        if (to < 0) {
            return false;
        }
        if (from < 0) {
            from = 0;
        }
        int from_fX = floor_sqrt(from);
        int to_fX = floor_sqrt(to);
        boolean done = var.removeInterval(-to_fX, -from_fX, cause, informCause);
        done |= var.removeInterval(from_fX, to_fX, cause, informCause);
        return done;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause, boolean informCause) throws ContradictionException {
        if (value < 0) {
            this.contradiction(cause, AbstractVariable.MSG_UNKNOWN);
        }
        int v = floor_sqrt(value);
        boolean done = false;
        if (v * v == value) { // is a perfect square ?
            done = var.updateLowerBound(-v, cause, informCause);
            done |= var.updateUpperBound(v, cause, informCause);
            if (var.hasEnumeratedDomain()) {
                done |= var.removeInterval(-v + 1, v - 1, cause, informCause);
            }
        } else { //otherwise, impossible value for instantiation
            this.contradiction(cause, AbstractVariable.MSG_UNKNOWN);
        }

        return done;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause, boolean informCause) throws ContradictionException {
        boolean done = false;
        if (value > 0) {
            int floorV = floor_sqrt(value);
            done = var.removeInterval(-floorV + 1, floorV - 1, cause, informCause);
        }
        return done;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause, boolean informCause) throws ContradictionException {
        if (value < 0) {
            this.contradiction(cause, AbstractVariable.MSG_UNKNOWN);
        }
        int floorV = floor_sqrt(value);
        boolean done = var.updateLowerBound(-floorV, cause, informCause);
        done |= var.updateUpperBound(floorV, cause, informCause);
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
        int v = var.getValue();
        return v * v;
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
    public IntDelta getDelta() {
        return delta;
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
}
