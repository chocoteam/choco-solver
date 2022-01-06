/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.bool;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.view.BoolIntView;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;


/**
 * declare an BoolVar based on X and C, such as (X = C) is reified by this. <br/> Based on "Views
 * and Iterators for Generic Constraint Implementations" <br/> C. Shulte and G. Tack.<br/> Eleventh
 * International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class BoolEqView<I extends IntVar> extends BoolIntView<I> {

    /**
     * A boolean view based on <i>var<i/> such that <i>var<i/> = <i>cste<i/>
     *
     * @param var  an integer variable
     * @param cste an int
     */
    public BoolEqView(final I var, final int cste) {
        super(var, "=", cste);
    }

    @Override
    public ESat getBooleanValue() {
        if (var.isInstantiated()) {
            return var.getValue() == cste ? ESat.TRUE : ESat.FALSE;
        } else if (var.contains(cste)) {
            return ESat.UNDEFINED;
        }
        return ESat.FALSE;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        assert cause != null;
        boolean done = false;
        if (!this.contains(value)) {
            model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
            this.contradiction(cause, MSG_EMPTY);
        } else if (!isInstantiated()) {
            model.getSolver().getEventObserver().instantiateTo(this, value, cause, getLB(), getUB());
            this.fixed.set(true);
            if (reactOnRemoval) {
                delta.add(1 - value, cause);
            }
            if (value == 1) {
                done = var.instantiateTo(cste, this);
            } else {
                done = var.removeValue(cste, this);
            }
            notifyPropagators(IntEventType.INSTANTIATE, cause);
        }
        return done;
    }

    @Override
    public int getDomainSize() {
        return isInstantiated()?1:2;
    }

    @Override
    public boolean isInstantiated() {
        if (var.isInstantiated()) {
            return true;
        } else return !var.contains(cste);
    }

    @Override
    public boolean contains(int value) {
        if (value == 0) {
            return !var.isInstantiatedTo(cste);
        } else if (value == 1) {
            return var.contains(cste);
        }
        return false;
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        if (value == 0) {
            return !var.contains(cste);
        } else if (value == 1) {
            return var.isInstantiatedTo(cste);
        }
        return false;
    }

    @Override
    public int getLB() {
        if (var.isInstantiatedTo(cste)) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getUB() {
        if (!var.contains(cste)) {
            return 0;
        }
        return 1;
    }

    @Override
    public int nextValue(int v) {
        if (v < 0 && contains(0)) {
            return 0;
        }
        return v <= 0 && contains(1) ? 1 : Integer.MAX_VALUE;
    }

    @Override
    public int nextValueOut(int v) {
        int lb = 0, ub = 1;
        if (!var.contains(cste)) {
            ub = 0;
        } else if (var.isInstantiated()) {
            lb = 1;
        }
        if (lb - 1 <= v && v <= ub) {
            return ub + 1;
        } else {
            return v + 1;
        }
    }

    @Override
    public int previousValue(int v) {
        if (v > 1 && contains(1)) {
            return 1;
        }
        return v >= 1 && contains(0) ? 0 : Integer.MIN_VALUE;
    }

    @Override
    public int previousValueOut(int v) {
        int lb = 0, ub = 1;
        if (!var.contains(cste)) {
            ub = 0;
        } else if (var.isInstantiated()) {
            lb = 1;
        }
        if (lb <= v && v <= ub + 1) {
            return lb - 1;
        } else {
            return v - 1;
        }
    }

    @Override
    public void justifyEvent(IntEventType mask, int one, int two, int three) {
        if (this.isInstantiated()) return;
        switch (mask) {
            case DECUPP:
                if (one < cste) {
                    model.getSolver().getEventObserver().instantiateTo(this, 0, this, 0, 1);
                } else if (this.var.getLB() == cste && (one == cste || this.var.previousValue(one + 1) == cste)) {
                    model.getSolver().getEventObserver().instantiateTo(this, 1, this, 0, 1);
                }
                break;
            case INCLOW:
                if (cste < one) {
                    model.getSolver().getEventObserver().instantiateTo(this, 0, this, 0, 1);
                } else if (this.var.getUB() == cste && (one == cste || this.var.nextValue(one - 1) == cste)) {
                    model.getSolver().getEventObserver().instantiateTo(this, 1, this, 0, 1);
                }
                break;
            case REMOVE:
                if (one == cste) {
                    model.getSolver().getEventObserver().instantiateTo(this, 0, this, 0, 1);
                } else if (this.var.getDomainSize() == 2 && this.var.contains(cste)) {
                    model.getSolver().getEventObserver().instantiateTo(this, 1, this, 0, 1);
                }
                break;
            case INSTANTIATE:
                if (one == cste) {
                    model.getSolver().getEventObserver().instantiateTo(this, 1, this, 0, 1);
                } else {
                    model.getSolver().getEventObserver().instantiateTo(this, 0, this, 0, 1);
                }
                break;
        }
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntVar pivot = explanation.readVar(p);
        int value = getValue();
        if (value == 1) { // b is true and X = c holds
            if (pivot == this) { // b is the pivot
                this.intersectLit(1, explanation);
                IntIterableRangeSet dom0 = explanation.universe();
                dom0.remove(cste);
                var.unionLit(dom0, explanation);
            } else if (pivot == var) { // x is the pivot
                this.unionLit(0, explanation);
                var.intersectLit(cste, explanation);
            }
        } else if (value == 0) {
            if (pivot == this) { // b is the pivot
                this.intersectLit(0, explanation);
                var.unionLit(cste, explanation);
            } else if (pivot == var) { // x is the pivot, case e. in javadoc
                this.unionLit(1, explanation);
                IntIterableRangeSet dom0 = explanation.universe();
                dom0.remove(cste);
                var.intersectLit(dom0, explanation);
            }
        }
    }
}
