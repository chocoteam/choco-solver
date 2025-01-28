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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A propagator dedicated to express b &rArr; x != y
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/02/2024
 */
@Explained
public class PropXneYHalfReif extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final BoolVar b;

    public PropXneYHalfReif(IntVar x, IntVar y, BoolVar b) {
        super(x, y, b);
        this.x = x;
        this.y = y;
        this.b = b;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < 2) {
            return IntEventType.INSTANTIATE.getMask();
        }
        return IntEventType.INCLOW.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (b.isInstantiatedTo(0)) {
            // if b is false, then no filtering is required
            setPassive();
        } else if (b.isInstantiatedTo(1)) {
            // if b is true, then x and y must be different
            if (x.isInstantiated()) {
                y.removeValue(x.getValue(), this,
                        lcg() ? Reason.r(x.getValLit(), b.getValLit()) : Reason.undef());
                setPassive();
            }else if (y.isInstantiated()) {
                x.removeValue(y.getValue(), this,
                        lcg() ? Reason.r(y.getValLit(), b.getValLit()) : Reason.undef());
                setPassive();
            }
        } else if (x.isInstantiated() && y.isInstantiated() && x.getValue() == y.getValue()) {
            // if x and y are instantiated and equal, then b must be false
            b.setToFalse(this,
                    lcg() ? Reason.r(x.getValLit(), y.getValLit()) : Reason.undef());
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (b.isInstantiatedTo(1)) {
                return ESat.eval(x.getValue() != y.getValue());
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
