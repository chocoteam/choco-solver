/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.tools.MathUtils;

/**
 * declare an IntVar based on X and C, such as X * C
 * <p>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class ScaleView extends IntView<IntVar> {

    public final int cste;

    /**
     * Create a <i>cste<i/> &times; <i>var<i/> view
     * @param var a variable
     * @param cste a positive integer
     */
    public ScaleView(final IntVar var, final int cste) {
        super("(" + var.getName() + "*" + cste + ")", var);
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
        return new ViewDeltaMonitor(var.monitorDelta(propagator)) {
            @Override
            protected int transform(int value) {
                return cste * value;
            }
        };
    }

    @Override
    protected boolean doInstantiateVar(int value) throws ContradictionException {
        if (value % cste != 0) {
            model.getSolver().getEventObserver().instantiateTo(this, value, this, getLB(), getUB());
            this.contradiction(this, MSG_INST);
        }
        return var.instantiateTo(value / cste, this);
    }

    @Override
    protected boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException {
        return var.updateLowerBound(MathUtils.divCeil(value, cste), this);
    }

    @Override
    protected boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException {
        return var.updateUpperBound(MathUtils.divFloor(value, cste), this);
    }

    @Override
    protected boolean doRemoveValueFromVar(int value) throws ContradictionException {
        return value % cste == 0 && var.removeValue(value / cste, this);
    }

    @Override
    protected boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException {
        return var.removeInterval(MathUtils.divCeil(from, cste), MathUtils.divFloor(to, cste), this);
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
    public int nextValueOut(int v) {
        return var.nextValueOut(MathUtils.divFloor(v, cste)) * cste;
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
    public int previousValueOut(int v) {
        return var.previousValueOut(MathUtils.divCeil(v, cste)) * cste;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new IntEvtScheduler();
    }

    @Override
    public String toString() {
        return "(" + this.var.toString() + " * " + this.cste + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public DisposableValueIterator getValueIterator(boolean bottomUp) {
        if (_viterator == null || _viterator.isNotReusable()) {
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
        if (_riterator == null || _riterator.isNotReusable()) {
            _riterator = new DisposableRangeIterator() {


                DisposableValueIterator vit;
                int min,
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

    @Override
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        switch (mask) {
            case DECUPP:
                model.getSolver().getEventObserver().updateUpperBound(this, one * cste, two * cste, this);
                break;
            case INCLOW:
                model.getSolver().getEventObserver().updateLowerBound(this, one * cste, two * cste, this);
                break;
            case REMOVE:
                model.getSolver().getEventObserver().removeValue(this, one * cste, this);
                break;
            case INSTANTIATE:
                model.getSolver().getEventObserver().instantiateTo(this, one * cste, this, two * cste, three * cste);
                break;
        }
    }
}
