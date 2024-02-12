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
import org.chocosolver.util.tools.MathUtils;

/**
 * X + Y >= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/06/12
 */
@Explained(partial = true, comment = "must be tested")
public final class PropGreaterOrEqualXY_C extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final int cste;

    @SuppressWarnings({"unchecked"})
    public PropGreaterOrEqualXY_C(IntVar[] vars, int c) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.INSTANTIATE.getMask() + IntEventType.DECUPP.getMask();
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateLowerBound(MathUtils.safeSubstract(this.cste, y.getUB()), this,
                lcg() ? Reason.r(y.getMaxLit()) : Reason.undef());
        y.updateLowerBound(MathUtils.safeSubstract(this.cste, x.getUB()), this,
                lcg() ? Reason.r(x.getMaxLit()) : Reason.undef());
        if (x.getLB() + y.getLB() >= this.cste) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            y.updateLowerBound(this.cste - x.getUB(), this,
                    lcg() ? Reason.r(x.getMaxLit()) : Reason.undef());
        } else {
            x.updateLowerBound(this.cste - y.getUB(), this,
                    lcg() ? Reason.r(y.getMaxLit()) : Reason.undef());
        }
        if (x.getLB() + y.getLB() >= this.cste) {
            this.setPassive();
        }
    }


    @Override
    public ESat isEntailed() {
        if (x.getUB() + y.getUB() < cste)
            return ESat.FALSE;
        else if (x.getLB() + y.getLB() >= this.cste)
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return x.getName() + " + " + y.getName() + " >= " + cste;
    }

}
