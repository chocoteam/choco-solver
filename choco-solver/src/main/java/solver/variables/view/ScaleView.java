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

import gnu.trove.map.hash.THashMap;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import util.iterators.DisposableRangeIterator;
import util.iterators.DisposableValueIterator;
import util.tools.MathUtils;

/**
 * declare an IntVar based on X and C, such as X * C
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class ScaleView extends IntView {

    public final int cste;

    public ScaleView(final IntVar var, final int cste, Solver solver) {
        super("(" + var.getName() + "*" + cste + ")", var, solver);
        assert (cste > 0) : "view cste must be >0";
        this.cste = cste;
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
            protected int transform(int value) {
                return cste * value;
            }
        };
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        if (value % cste == 0) {
            int inf = getLB();
            int sup = getUB();
            if (inf <= value && value <= sup) {
                EventType e = EventType.REMOVE;

                boolean done = var.removeValue(value / cste, this);
                if (done) {
                    if (value == inf) {
                        e = EventType.INCLOW;
                    } else if (value == sup) {
                        e = EventType.DECUPP;
                    }
                    if (this.isInstantiated()) {
                        e = EventType.INSTANTIATE;
                    }
                    this.notifyPropagators(e, cause);
                    return true;
                }
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
            boolean done = var.removeInterval(MathUtils.divCeil(from, cste), MathUtils.divFloor(to, cste), this);
            if (done) {
                notifyPropagators(EventType.REMOVE, cause);
            }
            return done;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        if (value % cste != 0) {
            contradiction(cause, EventType.INSTANTIATE, "Not a multiple of " + cste);
        }
        boolean done = var.instantiateTo(value / cste, this);
        if (done) {
            notifyPropagators(EventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getLB();
        if (old < value) {
            EventType e = EventType.INCLOW;
            boolean done = var.updateLowerBound(MathUtils.divCeil(value, cste), this);
            if (isInstantiated()) {
                e = EventType.INSTANTIATE;
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getUB();
        if (old > value) {
            EventType e = EventType.DECUPP;
            boolean done = var.updateUpperBound(MathUtils.divFloor(value, cste), this);
            if (isInstantiated()) {
                e = EventType.INSTANTIATE;
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(int value) {
        return value % cste == 0 && var.contains(value / cste);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return value % cste == 0 && var.isInstantiatedTo(value / cste);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return isInstantiatedTo(value);
    }

    @Override
    public int getValue() {
        return var.getValue() * cste;
    }

    @Override
    public int getLB() {
        return var.getLB() * cste;
    }

    @Override
    public int getUB() {
        return var.getUB() * cste;
    }

    @Override
    public int nextValue(int v) {
        int value = var.nextValue(MathUtils.divFloor(v, cste));
        if (value == Integer.MAX_VALUE) {
            return value;
        }
        return value * cste;
    }

    @Override
    public int previousValue(int v) {
        int value = var.previousValue(MathUtils.divCeil(v, cste));
        if (value == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value * cste;
    }

    @Override
    public String toString() {
        return "(" + this.var.toString() + " * " + this.cste + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public IntVar duplicate() {
        return VariableFactory.scale(this.var, this.cste);
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            this.var.duplicate(solver, identitymap);
            ScaleView clone = new ScaleView((IntVar) identitymap.get(this.var), this.cste, solver);
            identitymap.put(this, clone);
        }
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        var.explain(what, val / cste, to);
    }

    @Override
    public void explain(VariableState what, Explanation to) {
        if (cste > 0) {
            var.explain(what, to);
        } else {
            switch (what) {
                case UB:
                    var.explain(VariableState.LB, to);
                    break;
                case LB:
                    var.explain(VariableState.UB, to);
                    break;
                default:
                    var.explain(what, to);
                    break;
            }
        }
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                DisposableValueIterator vit;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    vit = var.getValueIterator(true);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vit = var.getValueIterator(false);
                }

                @Override
                public boolean hasNext() {
                    return vit.hasNext();
                }

                @Override
                public boolean hasPrevious() {
                    return vit.hasPrevious();
                }

                @Override
                public int next() {
                    return vit.next() * cste;
                }

                @Override
                public int previous() {
                    return vit.previous() * cste;
                }

                @Override
                public void dispose() {
                    super.dispose();
                    vit.dispose();
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
        if (cste == 1) return var.getRangeIterator(bottomUp);
        // cste > 2, so no range anymore!
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
                    max = min;
                }

                @Override
                public void topDownInit() {
                    vit = getValueIterator(false);
                    if (vit.hasPrevious()) {
                        max = vit.previous();
                    }
                    min = max;
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
                        min = max;
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
}
