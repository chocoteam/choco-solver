/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

import static org.chocosolver.solver.constraints.PropagatorPriority.LINEAR;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 15/12/2013
 */
@Explained
public class PropMin extends Propagator<IntVar> {

    private final int n;

    public PropMin(IntVar[] variables, IntVar maxVar) {
        super(concat(variables, maxVar), LINEAR, false);
        n = variables.length;
        assert n > 0;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean filter;
        do {
            filter = false;
            int lb = Integer.MAX_VALUE;
            int ub = Integer.MAX_VALUE;
            int min = vars[n].getLB();
            // update min
            for (int i = 0; i < n; i++) {
                filter |= vars[i].updateLowerBound(min, this,
                        lcg() ? Reason.r(vars[n].getMinLit()) : Reason.undef());
                lb = Math.min(lb, vars[i].getLB());
                ub = Math.min(ub, vars[i].getUB());
            }
            filter |= vars[n].updateLowerBound(lb, this,
                    lcg() ? Propagator.lbounds(vars[n], vars) : Reason.undef());
            filter |= vars[n].updateUpperBound(ub, this,
                    lcg() ? Propagator.ubounds(vars[n], vars) : Reason.undef());
            ub = Math.min(ub, vars[n].getUB()); // to deal with holes in vars[n] or its instantiation
            // back-propagation
            int c = 0, idx = -1;
            for (int i = 0; i < n && c >= i - 1; i++) {
                if (vars[i].getLB() > ub) {
                    c++;
                } else {
                    idx = i;
                }
            }
            if (c == vars.length - 2) {
                Reason r = Reason.undef();
                if (lcg()) {
                    int[] ps = new int[n + 1];
                    for (int i = 0, m = 1; i < n; i++, m++) {
                        ps[m] = vars[i].getMinLit();
                    }
                    ps[idx + 1] = vars[n].getMaxLit();
                    r = Reason.r(ps);
                }
                filter |= vars[idx].updateUpperBound(vars[n].getUB(), this, r);
                if (vars[n].isInstantiated() && vars[idx].isInstantiatedTo(vars[n].getValue())) {
                    setPassive();
                    return;
                }
            }
        } while (filter);
    }

    @Override
    public ESat isEntailed() {
        int lb = vars[n].getLB();
        int minLb = vars[0].getLB();
        for (int i = 0; i < n; i++) {
            minLb = Math.min(minLb, vars[i].getLB());
            if (vars[i].getUB() < lb) {
                return ESat.FALSE;
            }
        }
        if (minLb > vars[n].getUB()) {
            return ESat.FALSE;
        }
        for (int i = 0; i < n; i++) {
            if (vars[i].getLB() < lb) {
                return ESat.UNDEFINED;
            }
        }
        if (vars[n].isInstantiated()) {
            for (int i = 0; i < n; i++) {
                if (vars[i].isInstantiatedTo(lb)) {
                    return ESat.TRUE;
                }
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(vars[n]).append(" = min(");
        sb.append(vars[0]);
        for (int i = 1; i < n; i++) {
            sb.append(", ");
            sb.append(vars[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
