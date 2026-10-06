/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * V0 * V1 = V2
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class PropTimesNaiveWithLong extends Propagator<IntVar> {

    protected static final long MAX = Long.MAX_VALUE - 1, MIN = Long.MIN_VALUE + 1;

    private final IntVar v0;
    private final IntVar v1;
    private final IntVar v2;

    public PropTimesNaiveWithLong(IntVar v1, IntVar v2, IntVar result) {
        super(new IntVar[]{v1, v2, result}, PropagatorPriority.TERNARY, false);
        this.v0 = vars[0];
        this.v1 = vars[1];
        this.v2 = vars[2];
    }

    @Override
    public final int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public final void propagate(int evtmask) throws ContradictionException {
        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = div(0, v2.getLB(), v2.getUB(), v1.getLB(), v1.getUB());
            hasChanged |= div(1, v2.getLB(), v2.getUB(), v0.getLB(), v0.getUB());
            hasChanged |= mul(v2, v0.getLB(), v0.getUB(), v1.getLB(), v1.getUB());
        }
        if (v2.isInstantiatedTo(0) && (v0.isInstantiatedTo(0) || v1.isInstantiatedTo(0))) {
            setPassive();
        }
    }

    @Override
    public final ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(v0.getValue() * v1.getValue() == v2.getValue());
        }
        return ESat.UNDEFINED;
    }

    private boolean div(int vidx, long a, long b, long c, long d) throws ContradictionException {
        long min, max;
        IntVar var = vars[vidx];

        if (a <= 0 && b >= 0 && c <= 0 && d >= 0) { // case 1
            min = MIN;
            max = MAX;
            return var.updateLowerBound(min, this, explain(1 - vidx, 2))
                    | var.updateUpperBound(max, this, explain(1 - vidx, 2));
        } else if (a > 0 || b < 0) {
            if (c == 0 && d == 0) // case 2
                fails(); // TODO: could be more precise, for explanation purpose
            else if (c < 0 && d > 0) { // case 3
                max = Math.max(Math.abs(a), Math.abs(b));
                min = -max;
                return var.updateLowerBound(min, this, explain(1 - vidx, 2))
                        | var.updateUpperBound(max, this, explain(1 - vidx, 2));
            } else if (c == 0) // case 4 a
                return div(vidx, a, b, 1, d);
            else if (d == 0) // case 4 b
                return div(vidx, a, b, c, -1);
        } else { // if (c > 0 || d < 0) { // case 5
            long ceilAC = ceilDiv(a, c);
            long ceilAD = ceilDiv(a, d);
            long ceilBC = ceilDiv(b, c);
            long ceilBD = ceilDiv(b, d);
            min = Math.min(Math.min(ceilAC, ceilAD), Math.min(ceilBC, ceilBD));

            // floor(a / b) = Math.floorDiv(a, b)
            long floorAC = Math.floorDiv(a, c);
            long floorAD = Math.floorDiv(a, d);
            long floorBC = Math.floorDiv(b, c);
            long floorBD = Math.floorDiv(b, d);
            max = Math.max(Math.max(floorAC, floorAD), Math.max(floorBC, floorBD));

            if (min > max) {
                this.fails(explain(1 - vidx, 2));
            }
            return var.updateLowerBound(min, this, explain(1 - vidx, 2))
                    | var.updateUpperBound(max, this, explain(1 - vidx, 2));
        }
        return false;
    }

    private long ceilDiv(long a, long b) {
        return -Math.floorDiv(-a, b);
    }

    private boolean mul(IntVar var, long a, long b, long c, long d) throws ContradictionException {
        long min = Math.min(Math.min(a * c, a * d), Math.min(b * c, b * d));
        long max = Math.max(Math.max(a * c, a * d), Math.max(b * c, b * d));
        return var.updateLowerBound(min, this, explain(0, 1))
                | var.updateUpperBound(max, this, explain(0, 1));
    }

    private Reason explain(int i, int j) {
        if (lcg()) {
            int[] ps = new int[5];
            ps[1] = vars[i].getMinLit();
            ps[2] = vars[i].getMaxLit();
            ps[3] = vars[j].getMinLit();
            ps[4] = vars[j].getMaxLit();
            return this.r(ps);
        }
        return Reason.undef();
    }

}
