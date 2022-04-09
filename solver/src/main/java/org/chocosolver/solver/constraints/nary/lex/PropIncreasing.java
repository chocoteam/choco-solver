/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.lex;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

/**
 * A sweep-based algorithm for Increasing propagator
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 28/01/2022
 */
public class PropIncreasing extends Propagator<IntVar> {
    private final int strict;
    private int left, right;

    public PropIncreasing(IntVar[] vars, int strict) {
        super(vars, PropagatorPriority.LINEAR, true);
        this.strict = strict;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < vars.length - 1; i++) {
                vars[i + 1].updateLowerBound(vars[i].getLB() + strict, this);
            }
            for (int j = vars.length - 1; j > 0; j--) {
                vars[j - 1].updateUpperBound(vars[j].getUB() - strict, this);
            }
        } else {
            int i = left;
            left = vars.length - 1;
            int j = right;
            right = 0;
            while (i < vars.length - 1 && vars[i + 1].updateLowerBound(vars[i].getLB() + strict, this)) {
                i++;
            }
            while (j > 0 && vars[j - 1].updateUpperBound(vars[j].getUB() - strict, this)) {
                j--;
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (IntEventType.isInstantiate(mask)) {
            left = Math.min(left, idxVarInProp);
            right = Math.max(right, idxVarInProp);
        } else if (IntEventType.isInclow(mask)) {
            left = Math.min(left, idxVarInProp);
        } else if (IntEventType.isDecupp(mask)) {
            right = Math.max(right, idxVarInProp);
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    @Override
    public ESat isEntailed() {
        int cnt = 0;
        for (int i = 0; i < vars.length - 1; i++) {
            if (vars[i].getLB() > vars[i + 1].getLB() ||
                    vars[i].getUB() > vars[i + 1].getUB()) {
                return ESat.FALSE;
            } else if (vars[i].getUB() < vars[i + 1].getLB()) {
                cnt++;
            }
        }
        return cnt == vars.length ? ESat.TRUE : ESat.UNDEFINED;
    }
}
