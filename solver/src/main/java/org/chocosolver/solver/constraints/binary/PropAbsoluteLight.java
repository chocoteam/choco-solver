/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Enforces X = |Y|
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/07/2025
 */
@Explained
public class PropAbsoluteLight extends Propagator<IntVar> {

    private final IntVar absY;
    private final IntVar Y;

    public PropAbsoluteLight(IntVar X, IntVar Y) {
        super(ArrayUtils.toArray(X, Y), PropagatorPriority.BINARY, false);
        this.absY = vars[0];
        this.Y = vars[1];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return IntEventType.upperBoundAndInst();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public ESat isEntailed() {
        if (vars[0].getUB() < 0) {
            return ESat.FALSE;
        } else if (vars[0].isInstantiated()) {
            if (vars[1].isInstantiated()) {
                return ESat.eval(vars[0].getValue() == Math.abs(vars[1].getValue()));
            } else if (vars[1].getDomainSize() == 2 &&
                    vars[1].contains(vars[0].getValue()) &&
                    vars[1].contains(-vars[0].getValue())) {
                return ESat.TRUE;
            } else if (!vars[1].contains(vars[0].getValue()) &&
                    !vars[1].contains(-vars[0].getValue())) {
                return ESat.FALSE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.UNDEFINED;
        }
    }

    @Override
    public String toString() {
        return String.format("%s = |%s|", vars[0].toString(), vars[1].toString());
    }

    //***********************************************************************************
    // FILTERING
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        absY.updateLowerBound(0, this, Reason.undef());
        boolean loop;
        do {
            int l = Y.getLB();
            int u = Y.getUB();
            if (l >= 0) {
                absY.updateLowerBound(l, this, lcg() ? Reason.r(Y.getMinLit()) : Reason.undef());
                absY.updateUpperBound(u, this, lcg() ? Reason.r(Y.getMinLit(), Y.getMaxLit()) : Reason.undef());
            } else if (u <= 0) {
                absY.updateLowerBound(-u, this, lcg() ? Reason.r(Y.getMaxLit()) : Reason.undef());
                absY.updateUpperBound(-l, this, lcg() ? Reason.r(Y.getMaxLit(), Y.getMinLit()) : Reason.undef());
            } else {
                int t = Math.max(-l, u);
                absY.updateUpperBound(t, this, lcg() ? Reason.r(Y.getMaxLit(), Y.getMinLit()) : Reason.undef());
            }
            int au = absY.getUB();
            loop = Y.updateUpperBound(au, this, lcg() ? Reason.r(absY.getMaxLit()) : Reason.undef());
            loop |= Y.updateLowerBound(-au, this, lcg() ? Reason.r(absY.getMaxLit()) : Reason.undef());
        }while(loop);
    }

}
