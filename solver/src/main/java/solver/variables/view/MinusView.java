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
import choco.kernel.common.util.procedure.SafeIntProcedure;
import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.NoDelta;
import solver.variables.delta.monitor.IntDeltaMonitor;

/**
 * View for -V, where V is a IntVar or view
 * <p/>
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class MinusView extends IntView<IntVar> {

    DisposableValueIterator _viterator;
    DisposableRangeIterator _riterator;

    public MinusView(final IntVar var, Solver solver) {
        super("-(" + var.getName() + ")", var, solver);
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
        }
        return new IntDeltaMonitor(var.getDelta(), propagator) {
            @Override
            public void forEach(SafeIntProcedure proc, EventType eventType) {
                if (EventType.isRemove(eventType.mask)) {
                    for (int i = frozenFirst; i < frozenLast; i++) {
                        if (propagator != delta.getCause(i)) {
                            proc.execute(-delta.get(i));
                        }
                    }
                }
            }

            @Override
            public void forEach(IntProcedure proc, EventType eventType) throws ContradictionException {
                if (EventType.isRemove(eventType.mask)) {
                    for (int i = frozenFirst; i < frozenLast; i++) {
                        if (propagator != delta.getCause(i)) {
                            proc.execute(-delta.get(i));
                        }
                    }
                }
            }
        };
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
//        records.forEach(beforeModification.set(this, EventType.REMOVE, cause));
        int inf = getLB();
        int sup = getUB();
//        if (value == inf && value == sup) {
//            solver.getExplainer().removeValue(this, value, cause);
//            this.contradiction(this, EventType.REMOVE, MSG_REMOVE);
//        } else {
        if (inf <= value && value <= sup) {
            EventType e = EventType.REMOVE;

            boolean done = var.removeValue(-value, this);

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
            if (done) {
                if (this.instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                this.notifyPropagators(e, cause);
//                    solver.getExplainer().removeValue(this, value, cause);
                return true;
            }
        }
//        }
        return false;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (from <= getLB()) {
            return updateLowerBound(to + 1, cause);
        } else if (getUB() <= to) {
            return updateUpperBound(from - 1, cause);
        } else {
            boolean done = var.removeInterval(-to, -from, cause);
            if (done) {
                notifyPropagators(EventType.REMOVE, cause);
            }
            return done;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
//        records.forEach(beforeModification.set(this, EventType.INSTANTIATE, cause));
        boolean done = var.instantiateTo(-value, this);
        if (done) {
            notifyPropagators(EventType.INSTANTIATE, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
//        records.forEach(beforeModification.set(this, EventType.INCLOW, cause));
        int old = this.getLB();
        if (old < value) {
//            if (this.getUB() < value) {
//                solver.getExplainer().updateLowerBound(this, -old, -value, cause);
//                this.contradiction(cause, EventType.INCLOW, MSG_LOW);
//            } else {
            EventType e = EventType.INCLOW;
            boolean done = var.updateUpperBound(-value, this);
            if (instantiated()) {
                e = EventType.INSTANTIATE;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            if (done) {
                this.notifyPropagators(e, cause);
//                    solver.getExplainer().updateLowerBound(this, -old, -value, cause);
                return true;
            }
        }
//        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
//        records.forEach(beforeModification.set(this, EventType.DECUPP, cause));
        int old = this.getUB();
        if (old > value) {
//            if (this.getLB() > value) {
//                solver.getExplainer().updateUpperBound(this, old, value, cause);
//                this.contradiction(cause, EventType.DECUPP, MSG_UPP);
//            } else {
            EventType e = EventType.DECUPP;
            boolean done = var.updateLowerBound(-value, this);
            if (instantiated()) {
                e = EventType.INSTANTIATE;
                if (cause.reactOnPromotion()) {
                    cause = Cause.Null;
                }
            }
            if (done) {
                this.notifyPropagators(e, cause);
//                    solver.getExplainer().updateLowerBound(this, old, value, cause);
                return true;
            }
        }
//        }
        return false;
    }

    @Override
    public boolean contains(int value) {
        return var.contains(-value);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return var.instantiatedTo(-value);
    }

    @Override
    public int getValue() {
        return -var.getValue();
    }

    @Override
    public int getLB() {
        return -var.getUB();
    }

    @Override
    public int getUB() {
        return -var.getLB();
    }

    @Override
    public int nextValue(int v) {
        int value = var.previousValue(-v);
        if (value == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        return -value;
    }

    @Override
    public int previousValue(int v) {
        int value = var.nextValue(-v);
        if (value == Integer.MAX_VALUE) return Integer.MIN_VALUE;
        return -value;
    }

    @Override
    public String toString() {
        return "-(" + this.var.toString() + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public void explain(VariableState what, Explanation to) {
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

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        var.explain(what, -val, to);
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || !_viterator.isReusable()) {
            _viterator = new DisposableValueIterator() {

                DisposableValueIterator vit;

                @Override
                public void bottomUpInit() {
                    super.bottomUpInit();
                    vit = var.getValueIterator(false);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vit = var.getValueIterator(true);
                }

                @Override
                public boolean hasNext() {
                    return vit.hasPrevious();
                }

                @Override
                public boolean hasPrevious() {
                    return vit.hasNext();
                }

                @Override
                public int next() {
                    return -vit.previous();
                }

                @Override
                public int previous() {
                    return -vit.next();
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
                    vir = var.getRangeIterator(false);
                }

                @Override
                public void topDownInit() {
                    super.topDownInit();
                    vir = var.getRangeIterator(true);
                }

                @Override
                public boolean hasNext() {
                    return vir.hasPrevious();
                }

                @Override
                public boolean hasPrevious() {
                    return vir.hasNext();
                }

                @Override
                public void next() {
                    vir.previous();
                }

                @Override
                public void previous() {
                    vir.next();
                }

                @Override
                public int min() {
                    return -vir.max();
                }

                @Override
                public int max() {
                    return -vir.min();
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

    @Override
    public void transformEvent(EventType evt, ICause cause) throws ContradictionException {
        if (evt == EventType.INCLOW) {
            evt = EventType.DECUPP;
        } else if (evt == EventType.DECUPP) {
            evt = EventType.INCLOW;
        }
        notifyPropagators(evt, cause);
    }
}
