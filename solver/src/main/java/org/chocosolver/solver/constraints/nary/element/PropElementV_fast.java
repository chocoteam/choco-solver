/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.element;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Random;

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

    private final IntVar var;
    private final IntVar index;
    private final int offset;
    private boolean fast;
    private final Random rd;
    private int calls, success;
    private boolean rem;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropElementV_fast(IntVar value, IntVar[] values, IntVar index, int offset) {
        super(ArrayUtils.append(new IntVar[]{value, index}, values), PropagatorPriority.LINEAR, false);
        this.var = vars[0];
        this.index = vars[1];
        this.offset = offset;
        this.fast = true;
        rd = new Random(vars[0].getModel().getSeed());
        calls = success = 1;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        double p = (success * 1.d) / (calls * 1.d);
        fast = rd.nextFloat() < p;
        //if((calls % 1_000)==0) System.out.printf("%d\t%d\t%.3f%n", success, calls, p);
        rem = false; // is modified in filter
        try {
            filter();
        } finally {
            calls++;
            if (rem) {
                success++;
            }
        }
    }

    private void filter() throws ContradictionException {
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
            rem |= filterFrom(a, b);
            rem |= filterFrom(b, a);
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
        int la, ua, lb, ub;
        if ((la = a.getLB()) > (ub = b.getUB()) || (lb = b.getLB()) > (ua = a.getUB())) {
            return true;
        }
        if (fast) {
            return false;
        }
        if (a.getDomainSize() <= b.getDomainSize()) {
            if (intersect(a, la, ua, b, lb, ub)) {
                return false;
            }
        } else {
            if (intersect(b, lb, ub, a, la, ua)) {
                return false;
            }
        }
        rem = true;
        return true;
    }

    private boolean intersect(IntVar v1, int l1, int u1, IntVar v2, int l2, int u2) {
        int low = Math.max(l1, l2);
        int upp = Math.min(u1, u2);
        for (int i = v1.nextValue(low -1); i <= upp; i = v1.nextValue(i)) {
            if (v2.contains(i)) {
                return true;
            }
        }
        return false;
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
