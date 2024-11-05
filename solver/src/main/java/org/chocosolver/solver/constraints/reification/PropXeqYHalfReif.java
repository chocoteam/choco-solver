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
 * A propagator dedicated to express b &rArr; x == y
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/02/2024
 */
@Explained
public class PropXeqYHalfReif extends Propagator<IntVar> {

    private static final int THRESHOLD = 300;

    private final IntVar x;
    private final IntVar y;
    private final BoolVar b;

    public PropXeqYHalfReif(IntVar x, IntVar y, BoolVar b) {
        super(x, y, b);
        this.x = x;
        this.y = y;
        this.b = b;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx < 2) {
            return IntEventType.all();
        }
        return IntEventType.INCLOW.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (b.isInstantiatedTo(0)) {
            // if b is false, then no filtering is required
            setPassive();
        } else if (b.isInstantiatedTo(1)) {
            // if b is true, then x and y must be equal
            if (x.isInstantiated()) {
                y.instantiateTo(x.getValue(), this,
                        lcg() ? Reason.r(x.getValLit(), b.getValLit()) : Reason.undef());
                setPassive();
            } else if (y.isInstantiated()) {
                x.instantiateTo(y.getValue(), this,
                        lcg() ? Reason.r(y.getValLit(), b.getValLit()) : Reason.undef());
                setPassive();
            } else {
                // x and y are not instantiated, but b is true, so x and y must be equal
                do {
                    vars[0].updateLowerBound(vars[1].getLB(), this,
                            lcg() ? Reason.r(vars[1].getMinLit(), b.getValLit()) : Reason.undef());
                } while (vars[1].updateLowerBound(vars[0].getLB(), this,
                        lcg() ? Reason.r(vars[0].getMinLit(), b.getValLit()) : Reason.undef()));
                do {
                    vars[0].updateUpperBound(vars[1].getUB(), this,
                            lcg() ? Reason.r(vars[1].getMaxLit(), b.getValLit()) : Reason.undef());
                } while (vars[1].updateUpperBound(vars[0].getUB(), this,
                        lcg() ? Reason.r(vars[0].getMaxLit(), b.getValLit()) : Reason.undef()));

                // if x and y support value removal, then remove value from x if not in y and vice versa
                if (vars[0].hasEnumeratedDomain() && vars[1].hasEnumeratedDomain()) {
                    if ((long) vars[0].getDomainSize() + vars[1].getDomainSize() > THRESHOLD || lcg()) return;
                    int ub = vars[0].getUB();
                    for (int val = vars[0].getLB(); val <= ub; val = vars[0].nextValue(val)) {
                        if (!vars[1].contains(val)) {
                            vars[0].removeValue(val, this,
                                    lcg() ? Reason.r(vars[1].getLit(val, IntVar.LR_NE), b.getValLit()) : Reason.undef());
                        }
                    }
                    ub = vars[1].getUB();
                    for (int val = vars[1].getLB(); val <= ub; val = vars[1].nextValue(val)) {
                        if (!vars[0].contains(val)) {
                            vars[1].removeValue(val, this,
                                    lcg() ? Reason.r(vars[0].getLit(val, IntVar.LR_NE), b.getValLit()) : Reason.undef());
                        }
                    }
                }
            }
        }
        // if b is undefined and x and y are different, b must be false
        else {
            int k = (x.isInstantiated() ? 0b01 : 0b00) + (y.isInstantiated() ? 0b10 : 0b00);
            switch (k) {
                case 0b11:
                    if (x.getValue() != y.getValue()) {
                        b.setToFalse(this, lcg() ? Reason.r(x.getValLit(), y.getValLit()) : Reason.undef());
                        setPassive();
                    }
                    break;
                case 0b10:
                    if (!x.contains(y.getValue())) {
                        b.setToFalse(this, lcg() ? Reason.r(y.getValLit(), x.getLit(y.getValue(), IntVar.LR_EQ)) : Reason.undef());
                        setPassive();
                    }
                    break;
                case 0b01:
                    if (!y.contains(x.getValue())) {
                        b.setToFalse(this, lcg() ? Reason.r(x.getValLit(), y.getLit(x.getValue(), IntVar.LR_EQ)) : Reason.undef());
                        setPassive();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (b.isInstantiatedTo(1)) {
                return ESat.eval(x.getValue() == y.getValue());
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
