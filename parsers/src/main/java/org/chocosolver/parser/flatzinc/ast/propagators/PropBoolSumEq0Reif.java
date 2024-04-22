/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.propagators;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;

/**
 * <p>
 * Project: choco-parsers.
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
@Explained
public class PropBoolSumEq0Reif extends Propagator<BoolVar> {

    public PropBoolSumEq0Reif(BoolVar... vs) {
        super(vs);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int n = vars.length - 1;
        if (vars[n].getLB() == 1) {
            for (int i = 0; i < n; i++) {
                vars[i].setToFalse(this, lcg() ? Reason.r(vars[n].getValLit()) : Reason.undef());
            }
            setPassive();
            return;
        }
        int firstOne = -1;
        int secondOne = -1;
        for (int i = 0; i < n; i++) {
            if (vars[i].getLB() == 1) {
                vars[n].setToFalse(this, lcg() ? Reason.r(vars[i].getValLit()) : Reason.undef());
                setPassive();
                return;
            }
            if (vars[i].getUB() == 1) {
                if (firstOne == -1) {
                    firstOne = i;
                } else if (secondOne == -1) {
                    secondOne = i;
                }
            }
        }
        if (firstOne == -1) {
            vars[n].setToTrue(this,
                    lcg() ? Propagator.reason(vars[n], vars) : Reason.undef());
            setPassive();
        } else if (secondOne == -1 && vars[n].getUB() == 0) {
            vars[firstOne].setToTrue(this,
                    lcg() ? Propagator.reason(vars[firstOne], vars) : Reason.undef());
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }
}
