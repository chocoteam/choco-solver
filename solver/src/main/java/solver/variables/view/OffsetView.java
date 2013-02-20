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

import common.util.iterators.DisposableRangeIterator;
import common.util.iterators.DisposableValueIterator;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.IntDelta;
import solver.variables.delta.NoDelta;


/**
 * declare an IntVar based on X and C, such as X + C
 * <p/>
 * declare an IntVar based on X and Y, such max(X,Y)
 * <br/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class OffsetView extends IntView<IntDelta, IntVar<IntDelta>> {

    public final int cste;

    public OffsetView(final IntVar var, final int cste, Solver solver) {
        super("(" + var.getName() + "+" + cste + ")", var, solver);
        this.cste = cste;
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
        }
        return new ViewDeltaMonitor(var.monitorDelta(propagator), propagator) {
            @Override
            protected int transform(int value) {
                return value + cste;
            }
        };
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int inf = getLB();
        int sup = getUB();
        if (inf <= value && value <= sup) {
            EventType e = EventType.REMOVE;

            boolean done = var.removeValue(value - cste, this);
            if (done) {
                if (value == inf) {
                    e = EventType.INCLOW;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                } else if (value == sup) {
                    e = EventType.DECUPP;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                if (this.instantiated()) {
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
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        assert cause != null;
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause);
        } else {
            boolean done = var.removeInterval(from - cste, to - cste, this);
            if (done) {
                notifyPropagators(EventType.REMOVE, cause);
            }
            return done;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        boolean done = var.instantiateTo(value - cste, this);
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
            boolean done = var.updateLowerBound(value - cste, this);
            if (instantiated()) {
                e = EventType.INSTANTIATE;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            if (done) {
                this.notifyPropagators(e, cause);
                return true;
            }
        }
//        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        int old = this.getUB();
        if (old > value) {
            EventType e = EventType.DECUPP;
            boolean done = var.updateUpperBound(value - cste, this);
            if (instantiated()) {
                e = EventType.INSTANTIATE;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
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
        return var.contains(value - cste);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return var.instantiatedTo(value - cste);
    }

    @Override
    public int getValue() {
        return var.getValue() + cste;
    }

    @Override
    public int getLB() {
        return var.getLB() + cste;
    }

    @Override
    public int getUB() {
        return var.getUB() + cste;
    }

    @Override
    public int nextValue(int v) {
        int value = var.nextValue(v - cste);
        if (value == Integer.MAX_VALUE) {
            return value;
        }
        return value + cste;
    }

    @Override
    public int previousValue(int v) {
        int value = var.previousValue(v - cste);
        if (value == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value + cste;
    }

    @Override
    public String toString() {
        return "(" + this.var.toString() + " + " + this.cste + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        var.explain(what, val - cste, to);
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
                    return vit.next() + cste;
                }

                @Override
                public int previous() {
                    return vit.previous() + cste;
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
        if (_riterator == null || !_riterator.isReusable()) {
            _riterator = new DisposableRangeIterator() {

                DisposableRangeIterator vir;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    vir = var.getRangeIterator(true);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vir = var.getRangeIterator(false);
                }

                @Override
                public boolean hasNext() {
                    return vir.hasNext();
                }

                @Override
                public boolean hasPrevious() {
                    return vir.hasPrevious();
                }

                @Override
                public void next() {
                    vir.next();
                }

                @Override
                public void previous() {
                    vir.previous();
                }

                @Override
                public int min() {
                    return vir.min() + cste;
                }

                @Override
                public int max() {
                    return vir.max() + cste;
                }

                @Override
                public void dispose() {
                    super.dispose();
                    vir.dispose();
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
