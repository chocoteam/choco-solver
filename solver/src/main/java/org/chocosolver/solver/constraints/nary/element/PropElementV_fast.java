/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.element;

import org.chocosolver.sat.MiniSat;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
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
 * @author Charles Prud'homme
 * @since 05/2013
 */
@Explained(ignored = true, comment = "Replaced by PropElement2")
public class PropElementV_fast extends Propagator<IntVar> {

    private static final int THRESHOLD = 100;

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
            index.updateLowerBound(offset, this, Reason.undef());
            index.updateUpperBound(vars.length + offset - 3, this, Reason.undef());
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
            filter = false;
            int lb = index.getLB();
            int ub = index.getUB();
            int min = MAX_VALUE / 2;
            int max = MIN_VALUE / 2;
            // 1. bottom-up and top-down loops for bounded domains
            for (int i = lb, j = ub; i <= j; i = index.nextValue(i), j = index.previousValue(j)) {
                if (disjoint(var, vars[2 + i - offset])) {
                    filter |= index.removeValue(i, this, reason(i));
                } else {
                    min = min(min, vars[2 + i - offset].getLB());
                    max = max(max, vars[2 + i - offset].getUB());
                }
                if(i==j)break;
                if (disjoint(var, vars[2 + j - offset])) {
                    filter |= index.removeValue(j, this, reason(j));
                } else {
                    min = min(min, vars[2 + j - offset].getLB());
                    max = max(max, vars[2 + j - offset].getUB());
                }
            }
            filter |= var.updateLowerBound(min, this, reason(true));
            filter |= var.updateUpperBound(max, this, reason(false));
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

    private Reason reason(int v) {
        if (lcg()) {
            int[] ps = new int[index.getRange() + 3];
            ps[1] = index.getMinLit();
            ps[2] = index.getMaxLit();
            int lb = index.getLB();
            for (int j = lb, m = 3; j <= index.getUB(); j++, m++) {
                if (index.contains(j)) {
                    ps[m] = MiniSat.neg(vars[2 + j - offset].getLit(v, IntVar.LR_NE));
                } else {
                    ps[m] = MiniSat.neg(index.getLit(j + offset, IntVar.LR_NE));
                }
            }
            return Reason.r(ps);
        } else return Reason.undef();
    }

    private Reason reason(boolean min) {
        if (lcg()) {
            int[] ps = new int[vars.length - 2 + 1];
            for (int j = 0, m = 1; j < vars.length - 2; j++, m++) {
                if (index.contains(j)) {
                    ps[m] = min ? vars[j - offset].getMinLit() : vars[j - offset].getMaxLit();
                } else {
                    ps[m] = index.getLit(j + offset, IntVar.LR_EQ);
                }
            }
            return Reason.r(ps);
        } else return Reason.undef();
    }

    private boolean propagateEquality(IntVar a, IntVar b) throws ContradictionException {
        int s = a.getDomainSize() + b.getDomainSize();
        boolean filter = a.updateLowerBound(b.getLB(), this, lcg() ? Reason.r(index.getValLit(), b.getMinLit()) : Reason.undef());
        filter |= a.updateUpperBound(b.getUB(), this, lcg() ? Reason.r(index.getValLit(), b.getMaxLit()) : Reason.undef());
        filter |= b.updateLowerBound(a.getLB(), this, lcg() ? Reason.r(index.getValLit(), a.getMinLit()) : Reason.undef());
        filter |= b.updateUpperBound(a.getUB(), this, lcg() ? Reason.r(index.getValLit(), a.getMaxLit()) : Reason.undef());
        if (!fast || (a.getDomainSize() + b.getDomainSize() <= THRESHOLD)) {
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
                    filter |= a.removeValue(i, this,
                            lcg() ? Reason.r(index.getValLit(), b.getLit(i, IntVar.LR_NE)) : Reason.undef());
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
        if (fast && a.getDomainSize() + b.getDomainSize() > THRESHOLD) {
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
        for (int i = v1.nextValue(low - 1); i <= upp; i = v1.nextValue(i)) {
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
