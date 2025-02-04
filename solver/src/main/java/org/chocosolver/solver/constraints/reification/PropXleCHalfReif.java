/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator dedicated to express b &rArr; x <= c
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/03/2024
 */
@Explained
public class PropXleCHalfReif extends Propagator<IntVar> {

    private final IntVar x;
    private final int c;
    private final BoolVar b;

    public PropXleCHalfReif(IntVar x, int c, BoolVar b) {
        // The priority is set to 'LINEAR' to delay the propagation of this constraint
        super(new IntVar[]{x, b}, PropagatorPriority.LINEAR, false, false);
        this.x = x;
        this.c = c;
        this.b = b;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.INCLOW.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (b.isInstantiatedTo(0)) {
            // if b is false, then no filtering is required
            setPassive();
        } else if (b.isInstantiatedTo(1)) {
            // b is not false, so x <= c
            x.updateUpperBound(c, this, lcg() ? Reason.r(b.getValLit()) : Reason.undef());
            setPassive();
        } else if (x.getLB() > c) {
            // b must be false
            b.setToFalse(this, lcg() ? Reason.r(x.getMinLit()) : Reason.undef());
            setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (b.isInstantiatedTo(1)) {
                return ESat.eval(x.getValue() <= c);
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
