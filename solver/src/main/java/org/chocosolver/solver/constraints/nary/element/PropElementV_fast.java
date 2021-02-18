/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 10/05/13
 * Time: 01:32
 */

package org.chocosolver.solver.constraints.nary.element;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Fast Element constraint
 *
 * @author Jean-Guillaume Fages
 * @since 05/2013
 */
public class PropElementV_fast extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar var, index;
    private int offset;
    private final boolean fast;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropElementV_fast(IntVar value, IntVar[] values, IntVar index, int offset, boolean fast) {
        super(ArrayUtils.append(new IntVar[]{value, index}, values), PropagatorPriority.LINEAR, false);
        this.var = vars[0];
        this.index = vars[1];
        this.offset = offset;
        this.fast = fast;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean filter;
        do {
            filter = index.updateBounds(offset, vars.length + offset - 3, this);
            int lb = index.getLB();
            int ub = index.getUB();
            int min = MAX_VALUE / 2;
            int max = MIN_VALUE / 2;
            // 1. bottom up loop
            for (int i = lb; i <= ub; i = index.nextValue(i)) {
                if (disjoint(var, vars[2 + i - offset])) {
                    filter |= index.removeValue(i, this);
                }
                min = min(min, vars[2 + i - offset].getLB());
                max = max(max, vars[2 + i - offset].getUB());
            }
            // 2. top-down loop for bounded domains
            if (!index.hasEnumeratedDomain()) {
                if (index.getUB() < ub) {
                    for (int i = ub - 1; i >= lb; i = index.previousValue(i)) {
                        if (disjoint(var, vars[2 + i - offset])) {
                            filter |= index.removeValue(i, this);
                        } else break;
                    }
                }
            }
            filter |= var.updateBounds(min, max, this);
            if (index.isInstantiated()) {
                filter |= propagateEquality(var, vars[2 + index.getValue() - offset]);
            }
        } while (filter);
        if (var.isInstantiated() && index.isInstantiated()) {
            IntVar v = vars[2 + index.getValue() - offset];
            if (v.isInstantiated() && v.getValue() == var.getValue()) {
                setPassive();
            }
        }
    }

    private boolean propagateEquality(IntVar a, IntVar b) throws ContradictionException {
        int s = a.getDomainSize() + b.getDomainSize();
        boolean filter = a.updateBounds(b.getLB(), b.getUB(), this);
        filter |= b.updateBounds(a.getLB(), a.getUB(), this);
        if (!fast) {
            filterFrom(a, b);
            filterFrom(b, a);
        }
        if (a.getDomainSize() + b.getDomainSize() != s) {
            filter |= propagateEquality(a, b);
        }
        return filter;
    }

    private boolean filterFrom(IntVar a, IntVar b) throws ContradictionException {
        boolean filter = false;
        if (a.getDomainSize() != b.getDomainSize()) {
            int lb = a.getLB();
            int ub = a.getUB();
            for (int i = lb; i <= ub; i = a.nextValue(i)) {
                if (!b.contains(i)) {
                    filter |= a.removeValue(i, this);
                }
            }
        }
        return filter;
    }

    private boolean disjoint(IntVar a, IntVar b) {
        if (a.getLB() > b.getUB() || b.getLB() > a.getUB()) {
            return true;
        }
        if (fast) {
            return false;
        }
        int lb = a.getLB();
        int ub = a.getUB();
        for (int i = lb; i <= ub; i = a.nextValue(i)) {
            if (b.contains(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ESat isEntailed() {
        int lb = index.getLB();
        int ub = index.getUB();
        int min = MAX_VALUE / 2;
        int max = MIN_VALUE / 2;
        int val = var.getLB();
        boolean exists = false;
        for (int i = lb; i <= ub; i = index.nextValue(i)) {
            int j = 2 + i - offset;
            if (j >= 2 && j < vars.length) {
                min = min(min, vars[j].getLB());
                max = max(max, vars[j].getUB());
                exists |= vars[j].contains(val);
            }
        }
        if (min > var.getUB() || max < var.getLB()) {
            return ESat.FALSE;
        }
        if (var.isInstantiated() && !exists) {
            return ESat.FALSE;
        }
        if (var.isInstantiated() && min == max) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
