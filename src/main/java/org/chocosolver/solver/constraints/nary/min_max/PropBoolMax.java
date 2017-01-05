/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 15/12/2013
 */
public class PropBoolMax extends Propagator<BoolVar> {

    private final int n;
    private int x1, x2;

    public PropBoolMax(BoolVar[] variables, BoolVar maxVar) {
        super(ArrayUtils.append(variables, new BoolVar[]{maxVar}), PropagatorPriority.UNARY, true);
        n = variables.length;
        x1 = -1;
        x2 = -1;
        assert n > 0;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x1 = -1;
        x2 = -1;
        for (int i = 0; i < n; i++) {
            if (!vars[i].isInstantiated()) {
                if (x1 == -1) {
                    x1 = i;
                } else if (x2 == -1) {
                    x2 = i;
                }
            } else if (vars[i].getValue() == 1) {
                vars[n].instantiateTo(1, this);
                if (vars[n].isInstantiatedTo(1)) {
                    setPassive();
                    return;
                }
            }
        }
        filter();
    }

    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == n) {
            filter();
        } else {
            if (vars[idxVarInProp].isInstantiatedTo(1)) {
                vars[n].instantiateTo(1, this);
                if (vars[n].isInstantiatedTo(1)) {
                    setPassive();
                }
            } else if (idxVarInProp == x1 || idxVarInProp == x2) {
                if (idxVarInProp == x1) {
                    x1 = x2;
                }
                x2 = -1;
                for (int i = 0; i < n; i++) {
                    if (i != x1 && !vars[i].isInstantiated()) {
                        x2 = i;
                        break;
                    }
                }
                filter();
            }
        }
    }

    public void filter() throws ContradictionException {
        if (x1 == -1) {
            vars[n].instantiateTo(0, this);
            if (vars[n].isInstantiatedTo(0)) {
                setPassive();
            }
        }else if (x2 == -1 && vars[n].isInstantiatedTo(1)) {
            vars[x1].instantiateTo(1, this);
            if (vars[x1].isInstantiatedTo(1)) {
                setPassive();
            }
        }else if (vars[n].isInstantiatedTo(0)) {
            for (int i = 0; i < n; i++) {
                vars[i].instantiateTo(0, this);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int ub = vars[n].getUB();
        for (int i = 0; i < n; i++) {
            if (vars[i].getLB() > ub) {
                return ESat.FALSE;
            }
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
        StringBuilder sb = new StringBuilder("");
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
