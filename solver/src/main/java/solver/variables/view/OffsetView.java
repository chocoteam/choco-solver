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
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.monitor.IntDeltaMonitor;
import solver.variables.delta.view.ViewDelta;


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
public final class OffsetView extends View<IntVar> {

    final int cste;
    DisposableValueIterator _viterator;
    DisposableRangeIterator _riterator;

    public OffsetView(final IntVar var, final int cste, Solver solver) {
        super("(" + var.getName() + "+" + cste + ")", var, solver);
        this.cste = cste;
    }


    @Override
    public void analyseAndAdapt(int mask) {
        super.analyseAndAdapt(mask);
        if (!reactOnRemoval && ((modificationEvents & EventType.REMOVE.mask) != 0)) {
            var.analyseAndAdapt(mask);
            delta = new ViewDelta(new IntDeltaMonitor(var.getDelta()) {

                @Override
                public void forEach(IntProcedure proc, EventType eventType) throws ContradictionException {
                    if (EventType.isRemove(eventType.mask)) {
                        for (int i = frozenFirst; i < frozenLast; i++) {
                            proc.execute(delta.get(i) - cste);
                        }
                    }
                }
            });
            reactOnRemoval = true;
        }
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        records.forEach(beforeModification.set(this, EventType.REMOVE, cause));
        int inf = getLB();
        int sup = getUB();
        if (value == inf && value == sup) {
            solver.getExplainer().removeValue(this, value, cause);
            this.contradiction(cause, EventType.REMOVE, MSG_REMOVE);
        } else {
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
                    this.notifyMonitors(e, cause);
                    solver.getExplainer().removeValue(this, value, cause);
                    return true;
                }
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
            boolean done = var.removeInterval(from - cste, to - cste, cause);
            if (done) {
                notifyMonitors(EventType.REMOVE, cause);
            }
            return done;
        }
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        records.forEach(beforeModification.set(this, EventType.INSTANTIATE, cause));
        solver.getExplainer().instantiateTo(this, value, cause);
        if (this.instantiated()) {
            if (value != this.getValue()) {
                this.contradiction(cause, EventType.INSTANTIATE, MSG_INST);
            }
            return false;
        } else if (contains(value)) {
            EventType e = EventType.INSTANTIATE;

            boolean done = var.instantiateTo(value - cste, this);
            if (done) {
                notifyMonitors(EventType.INSTANTIATE, cause);
                return true;
            }

        } else {
            this.contradiction(cause, EventType.INSTANTIATE, MSG_UNKNOWN);
        }
        return false;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        records.forEach(beforeModification.set(this, EventType.INCLOW, cause));
        int old = this.getLB();
        if (old < value) {
            if (this.getUB() < value) {
                solver.getExplainer().updateLowerBound(this, old, value, cause);
                this.contradiction(cause, EventType.INCLOW, MSG_LOW);
            } else {
                EventType e = EventType.INCLOW;
                boolean done = var.updateLowerBound(value - cste, this);
                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                if (done) {
                    this.notifyMonitors(e, cause);
                    solver.getExplainer().updateLowerBound(this, old, value, cause);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        records.forEach(beforeModification.set(this, EventType.DECUPP, cause));
        ICause antipromo = cause;
        int old = this.getUB();
        if (old > value) {
            if (this.getLB() > value) {
                solver.getExplainer().updateUpperBound(this, old, value, antipromo);
                this.contradiction(cause, EventType.DECUPP, MSG_UPP);
            } else {
                EventType e = EventType.DECUPP;
                boolean done = var.updateUpperBound(value - cste, cause);
                if (instantiated()) {
                    e = EventType.INSTANTIATE;
                    if (cause.reactOnPromotion()) {
                        cause = Cause.Null;
                    }
                }
                if (done) {
                    this.notifyMonitors(e, cause);
                    solver.getExplainer().updateLowerBound(this, old, value, antipromo);
                    return true;
                }
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
    public int getType() {
        return INTEGER;
    }

    @Override
    public Explanation explain(VariableState what, int val) {
        return var.explain(what, val - cste);
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
