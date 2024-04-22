/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator dedicated to express b &rArr; x <= y
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/02/2024
 */
@Explained
public class PropXleYHalfReif extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final BoolVar b;

    public PropXleYHalfReif(IntVar x, IntVar y, BoolVar b) {
        super(x, y, b);
        this.x = x;
        this.y = y;
        this.b = b;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 1) {
            return IntEventType.DECUPP.getMask();
        }
        return IntEventType.INCLOW.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (b.isInstantiatedTo(0)) { // if b is false, then no filtering is required
            setPassive();
            return;
        }
        int x_min = x.getLB();
        int y_max = y.getUB();
        if (x_min > y_max) {
            // b must be false
            b.setToFalse(this,
                    lcg() ? Reason.r(x.getMinLit(), y.getMaxLit()) : Reason.undef());
            setPassive();
            return;
        }
        if (b.isInstantiatedTo(1)) {
            // b is not false, so x >= y
            x.updateUpperBound(y_max, this,
                    lcg() ? Reason.r(y.getMaxLit(), b.getValLit()) : Reason.undef());
            y.updateLowerBound(x_min, this,
                    lcg() ? Reason.r(x.getMinLit(), b.getValLit()) : Reason.undef());
            if (x.getUB() <= y.getLB()) {
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (b.isInstantiatedTo(1)) {
                return ESat.eval(x.getValue() <= y.getValue());
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
