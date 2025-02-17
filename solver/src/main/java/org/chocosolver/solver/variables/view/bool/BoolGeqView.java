/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.bool;

import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.view.BoolIntView;
import org.chocosolver.util.ESat;


/**
 * declare an BoolVar based on X and C, such as (X >= C) is reified by this. <br/> Based on "Views
 * and Iterators for Generic Constraint Implementations" <br/> C. Shulte and G. Tack.<br/> Eleventh
 * International Conference on Principles and Practice of Constraint Programming
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
@Explained
public final class BoolGeqView<I extends IntVar> extends BoolIntView<I> {

    /**
     * A boolean view based on <i>var<i/> such that <i>var<i/> &le; <i>cste<i/>
     *
     * @param var  an integer variable
     * @param cste an int
     */
    public BoolGeqView(final I var, final int cste) {
        super("(" + var.getName() + "≥" + cste + ")", var, cste);
    }

    @Override
    public ESat getBooleanValue() {
        if (var.getUB() < cste) {
            return ESat.FALSE;
        }
        return var.getLB() >= cste ? ESat.TRUE : ESat.UNDEFINED;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause, Reason reason) throws ContradictionException {
        assert cause != null;
        boolean done = false;
        if (value < 0 || value > 1) {
            if (getModel().getSolver().isLCG()) {
                getModel().getSolver().getSat().cEnqueue(0, reason);
            }
            this.contradiction(cause, MSG_EMPTY);
        } else {
            if (reactOnRemoval) {
                delta.add(1 - value, cause);
            }
            if (value == 1) {
                done = var.updateLowerBound(cste, this, reason);
            } else {
                done = var.updateUpperBound(cste - 1, this, reason);
            }
            if (done) {
                this.fixed.set(done);
                notifyPropagators(IntEventType.INSTANTIATE, cause);
            }
        }
        return done;
    }

    @Override
    public int getDomainSize() {
        return isInstantiated() ? 1 : 2;
    }

    @Override
    public boolean isInstantiated() {
        if (var.getLB() >= cste) {
            return true;
        } else return var.getUB() < cste;
    }

    @Override
    public boolean contains(int value) {
        if (value == 0) {
            return var.getLB() < cste;
        } else if (value == 1) {
            return var.getUB() >= cste;
        }
        return false;
    }

    @Override
    public boolean isInstantiatedTo(int value) {
        if (value == 0) {
            return var.getUB() < cste;
        } else if (value == 1) {
            return cste <= var.getLB();
        }
        return false;
    }

    @Override
    public int getLB() {
        if (cste > var.getLB()) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getUB() {
        if (var.getUB() >= cste) {
            return 1;
        }
        return 0;
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
        if (var.getUB() < cste) {
            ub = 0;
        } else if (var.getLB() >= cste) {
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
        if (var.getUB() < cste) {
            ub = 0;
        } else if (var.getLB() >= cste) {
            lb = 1;
        }
        if (lb <= v && v <= ub + 1) {
            return lb - 1;
        } else {
            return v - 1;
        }
    }

    @Override
    public int getLit(int val, int type) {
        if (val < 0) {
            return 1 ^ (type & 1);  // true, false, true, false
        }
        if (val > 1) {
            return type - 1 >> 1 & 1;  // true, false, false, true
        }
        switch (type) {
            case LR_NE:
                return var.getLit(cste, LR_GE) - val;
            case LR_EQ:
                return var.getLit(cste, LR_GE) + val - 1;
            case LR_GE:
                return val == 1 ? var.getLit(cste, LR_GE) + val - 1 : 1;
            case LR_LE:
                return val == 0 ? var.getLit(cste, LR_GE) + val - 1 : 1;
            default:
                throw new UnsupportedOperationException("BoolGeqView#getLit");
        }
    }

    @Override
    public int getMinLit() {
        return MiniSat.neg(getLit(getLB(), LR_GE));
    }

    @Override
    public int getMaxLit() {
        return MiniSat.neg(getLit(getUB(), LR_LE));
    }

    @Override
    public int getValLit() {
        assert (isInstantiated()) : this + " is not instantiated";
        return getLit(getLB(), LR_NE);
    }

    /**
     * Creates, or returns if already existing, the SAT variable twin of this.
     *
     * @return the SAT variable of this
     */
    public int satVar() {
        if (getModel().getSolver().isLCG()) {
            return MiniSat.var(getLit(1, LR_EQ));
        }
        return super.satVar();
    }
}
