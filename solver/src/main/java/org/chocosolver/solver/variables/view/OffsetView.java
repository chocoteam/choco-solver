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
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;


/**
 * declare an IntVar based on X and C, such as X + C
 * <br/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class OffsetView extends IntView<IntVar> {

    /**
     * A constant value
     */
    public final int cste;

    /**
     * A view based on <i>var<i/> such that <i>var<i/> + <i>cste<i/>
     * @param var an integer variable
     * @param cste an int
     */
    public OffsetView(final IntVar var, final int cste) {
        super("(" + var.getName() + "+" + cste + ")", var);
        this.cste = cste;
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
                return value + cste;
            }
        };
    }

    @Override
    protected boolean doInstantiateVar(int value) throws ContradictionException {
        return var.instantiateTo(value - cste, this);
    }

    @Override
    protected boolean doUpdateLowerBoundOfVar(int value) throws ContradictionException {
        return var.updateLowerBound(value - cste, this);
    }

    @Override
    protected boolean doUpdateUpperBoundOfVar(int value) throws ContradictionException {
        return var.updateUpperBound(value - cste, this);
    }

    @Override
    protected boolean doRemoveValueFromVar(int value) throws ContradictionException {
        return var.removeValue(value - cste, this);
    }

    @Override
    protected boolean doRemoveIntervalFromVar(int from, int to) throws ContradictionException {
        return var.removeInterval(from - cste, to - cste, this);
    }

    @Override
    public boolean contains(int value) {
        return var.contains(value - cste);
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        return var.isInstantiatedTo(value - cste);
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
    public int nextValueOut(int v) {
        return var.nextValueOut(v - cste) + cste;
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
    public int previousValueOut(int v) {
        return var.previousValueOut(v - cste) + cste;
    }

    @Override
    protected EvtScheduler createScheduler() {
        return new IntEvtScheduler();
    }

    @Override
    public String toString() {
        return "(" + var.toString() + (cste >= 0 ? " + " : " - ") + Math.abs(cste) + ") = [" + getLB() + "," + getUB() + "]";
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
        if (_riterator == null || _riterator.isNotReusable()) {
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

    @Override
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        switch (mask) {
            case DECUPP:
                model.getSolver().getEventObserver().updateUpperBound(this, one + cste, two + cste, this);
                break;
            case INCLOW:
                model.getSolver().getEventObserver().updateLowerBound(this, one + cste, two + cste, this);
                break;
            case REMOVE:
                model.getSolver().getEventObserver().removeValue(this, one + cste, this);
                break;
            case INSTANTIATE:
                model.getSolver().getEventObserver().instantiateTo(this, one + cste, this, two + cste, three + cste);
                break;
        }
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntIterableRangeSet set0, set1;
        if (explanation.readVar(p) == this) { // case a. (see javadoc)
            set1 = explanation.complement(getVariable());
            set0 = explanation.domain(getVariable());
            set0.plus(cste);
            this.intersectLit(set0, explanation);
            getVariable().unionLit(set1, explanation);
        } else { // case b. (see javadoc)
            assert explanation.readVar(p) == getVariable();
            set0 = explanation.complement(this);
            set1 = explanation.domain(this);
            set1.minus(cste);
            this.unionLit(set0, explanation);
            getVariable().intersectLit(set1, explanation);
        }
    }
}
