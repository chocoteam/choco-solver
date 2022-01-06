/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.integer;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.solver.variables.view.IntView;
import org.chocosolver.solver.variables.view.ViewDeltaMonitor;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.EvtScheduler;

import static org.chocosolver.solver.variables.events.IntEventType.BOUND;
import static org.chocosolver.solver.variables.events.IntEventType.DECUPP;
import static org.chocosolver.solver.variables.events.IntEventType.INCLOW;
import static org.chocosolver.solver.variables.events.IntEventType.INSTANTIATE;

/**
 * View for -V, where V is a IntVar or view
 * <p>
 * <p>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class IntMinusView<I extends IntVar> extends IntView<I> {


    /**
     * Create a -<i>var<i/> view
     * @param var a integer variable
     */
    public IntMinusView(final I var) {
        super("-(" + var.getName() + ")", var);
    }

    @Override
    public IIntDeltaMonitor monitorDelta(ICause propagator) {
        var.createDelta();
        if (var.getDelta() == NoDelta.singleton) {
            return IIntDeltaMonitor.Default.NONE;
        }
        return new ViewDeltaMonitor(var.monitorDelta(propagator)) {
            @Override
            protected int transform(int value) {
                return -value;
            }
        };
    }

    @Override
    public boolean updateBounds(int lb, int ub, ICause cause) throws ContradictionException {
        assert cause != null;
        int olb = this.getLB();
        int oub = this.getUB();
        boolean hasChanged = false;
        if (olb < lb || oub > ub) {
            IntEventType e = null;

            if (olb < lb) {
                model.getSolver().getEventObserver().updateLowerBound(this, lb, getLB(), cause);
                e = INCLOW;
                if (var.updateUpperBound(-lb, this)) {
                    hasChanged = true;
                } else {
                    model.getSolver().getEventObserver().undo();
                }
            }
            if (oub > ub) {
                e = e == null ? DECUPP : BOUND;
                model.getSolver().getEventObserver().updateUpperBound(this, ub, getUB(), cause);
                if (var.updateLowerBound(-ub, this)) {
                    hasChanged = true;
                } else {
                    model.getSolver().getEventObserver().undo();
                }
            }
            if (isInstantiated()) {
                e = INSTANTIATE;
            }
            if (hasChanged) {
                this.notifyPropagators(e, cause);
            }
        }
        return hasChanged;
    }

    @Override
    protected boolean doInstantiateVar(int value) throws ContradictionException {
        return var.instantiateTo(-value, this);
    }

    @Override
    protected boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException {
        return var.updateUpperBound(-value, this);
    }

    @Override
    protected boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException {
        return var.updateLowerBound(-value, this);
    }

    @Override
    protected boolean doRemoveValueFromVar(int value) throws ContradictionException {
        return var.removeValue(-value, this);
    }

    @Override
    protected boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException {
        return var.removeInterval(-to, -from, this);
    }

    @Override
    public boolean contains(int value) {
        return var.contains(-value);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(-value);
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
    public int nextValueOut(int v) {
        return -var.previousValueOut(-v);
    }

    @Override
    public int previousValue(int v) {
        int value = var.nextValue(-v);
        if (value == Integer.MAX_VALUE) return Integer.MIN_VALUE;
        return -value;
    }

    @Override
    public int previousValueOut(int v) {
        return -var.nextValueOut(-v);
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new IntEvtScheduler();
    }

    @Override
    public String toString() {
        return "-(" + this.var.toString() + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
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
        if (_riterator == null || _riterator.isNotReusable()) {
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
    public IEventType transformEvent(IEventType evt) {
        if (evt == INCLOW) {
            return DECUPP;
        } else if (evt == DECUPP) {
            return INCLOW;
        }
        return evt;
    }

    @Override
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        switch (mask) {
            case DECUPP:
                model.getSolver().getEventObserver().updateLowerBound(this, -one, -two, this);
                break;
            case INCLOW:
                model.getSolver().getEventObserver().updateUpperBound(this, -one, -two, this);
                break;
            case REMOVE:
                model.getSolver().getEventObserver().removeValue(this, -one, this);
                break;
            case INSTANTIATE:
                model.getSolver().getEventObserver().instantiateTo(this, -one, this, -three, -two);
                break;
        }
    }
}
