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
public class PropMax extends Propagator<IntVar> {

    private final int n;

    public PropMax(IntVar[] variables, IntVar maxVar) {
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
            int lb = Integer.MIN_VALUE;
            int ub = Integer.MIN_VALUE;
            int max = vars[n].getUB();
            // update max
            for (int i = 0; i < n; i++) {
                filter |= vars[i].updateUpperBound(max, this,
                        lcg() ? Reason.r(vars[n].getMaxLit()) : Reason.undef());
                lb = Math.max(lb, vars[i].getLB());
                ub = Math.max(ub, vars[i].getUB());
            }
            filter |= vars[n].updateLowerBound(lb, this,
                    lcg() ? Propagator.lbounds(vars[n], vars) : Reason.undef());
            filter |= vars[n].updateUpperBound(ub, this,
                    lcg() ? Propagator.ubounds(vars[n], vars) : Reason.undef());
            lb = Math.max(lb, vars[n].getLB());
            // back-propagation
            int c = 0, idx = -1;
            for (int i = 0; i < n && c >= i - 1; i++) {
                if (vars[i].getUB() < lb) {
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
                        ps[m] = vars[i].getMaxLit();
                    }
                    ps[idx + 1] = vars[n].getMinLit();
                    r = Reason.r(ps);
                }
                filter = vars[idx].updateLowerBound(vars[n].getLB(), this, r);
                if (vars[n].isInstantiated() && vars[idx].isInstantiatedTo(vars[n].getValue())) {
                    setPassive();
                    return;
                }
            }
        } while (filter);
    }

    @Override
    public ESat isEntailed() {
        int ub = vars[n].getUB();
        int maxUb = vars[0].getUB();
        for (int i = 0; i < n; i++) {
            maxUb = Math.max(maxUb, vars[i].getUB());
            if (vars[i].getLB() > ub) {
                return ESat.FALSE;
            }
        }
        if (maxUb < vars[n].getLB()) {
            return ESat.FALSE;
        }
        for (int i = 0; i < n; i++) {
            if (vars[i].getUB() > ub) {
                return ESat.UNDEFINED;
            }
        }
        if (vars[n].isInstantiated()) {
            for (int i = 0; i < n; i++) {
                if (vars[i].isInstantiatedTo(ub)) {
                    return ESat.TRUE;
                }
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(vars[n]).append(" = max(");
        sb.append(vars[0]);
        for (int i = 1; i < n; i++) {
            sb.append(", ");
            sb.append(vars[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
