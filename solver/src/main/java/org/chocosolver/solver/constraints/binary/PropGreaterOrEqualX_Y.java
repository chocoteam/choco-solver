/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
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

/**
 * X >= Y
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
@Explained
public final class PropGreaterOrEqualX_Y extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;

    public PropGreaterOrEqualX_Y(IntVar[] vars) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return IntEventType.upperBoundAndInst();
        } else {
            return IntEventType.lowerBoundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateLowerBound(y.getLB(), this, lcg() ? Reason.r(y.getMinLit()) : Reason.undef());
        y.updateUpperBound(x.getUB(), this, lcg() ? Reason.r(x.getMaxLit()) : Reason.undef());
        if (x.getLB() >= y.getUB()) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (varIdx == 0) {
            y.updateUpperBound(x.getUB(), this, lcg() ? Reason.r(x.getMaxLit()) : Reason.undef());
        } else {
            x.updateLowerBound(y.getLB(), this, lcg() ? Reason.r(y.getMinLit()) : Reason.undef());
        }
        if (x.getLB() >= y.getUB()) {
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if (x.getUB() < y.getLB())
            return ESat.FALSE;
        else if (x.getLB() >= y.getUB())
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return "prop(" + vars[0].getName() + ".GEQ." + vars[1].getName() + ")";
    }

}
