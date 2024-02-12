/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * X = MIN(Y,Z)
 * <br/>
 * ensures bound consistency
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
@Explained
public class PropMinBC extends Propagator<IntVar> {

    private final IntVar BST;
    private final IntVar v1;
    private final IntVar v2;

    public PropMinBC(IntVar X, IntVar Y, IntVar Z) {
        super(new IntVar[]{X, Y, Z}, PropagatorPriority.TERNARY, false);
        this.BST = vars[0];
        this.v1 = vars[1];
        this.v2 = vars[2];
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        boolean change;
        do {
            change = vars[0].updateLowerBound(Math.min(vars[1].getLB(), vars[2].getLB()), this,
                    lcg() ? Reason.r(vars[1].getMinLit(), vars[2].getMinLit()) : Reason.undef());
            change |= vars[0].updateUpperBound(Math.min(vars[1].getUB(), vars[2].getUB()),
                    this, lcg() ? Reason.r(vars[1].getMaxLit(), vars[2].getMaxLit()) : Reason.undef());
            change |= vars[1].updateLowerBound(vars[0].getLB(), this,
                    lcg() ? Reason.r(vars[0].getMinLit()) : Reason.undef());
            change |= vars[2].updateLowerBound(vars[0].getLB(), this,
                    lcg() ? Reason.r(vars[0].getMinLit()) : Reason.undef());
            if (vars[2].getLB() > vars[0].getUB()) {
                change |= vars[1].updateUpperBound(vars[0].getUB(), this,
                        lcg() ? Reason.r(vars[0].getMaxLit(), vars[2].getMinLit()) : Reason.undef());
            }
            if (vars[1].getLB() > vars[0].getUB()) {
                change |= vars[2].updateUpperBound(vars[0].getUB(), this,
                        lcg() ? Reason.r(vars[0].getMaxLit(), vars[1].getMinLit()) : Reason.undef());
            }
        } while (change);
        if (vars[0].isInstantiated()) {
            int bst = vars[0].getValue();
            if (vars[1].isInstantiatedTo(bst) || vars[2].isInstantiatedTo(bst)) {
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        int lb = vars[0].getLB();
        if (vars[1].getUB() < lb || vars[2].getUB() < lb) {
            return ESat.FALSE;
        }
        if (Math.min(vars[1].getLB(), vars[2].getLB()) > vars[0].getUB()) {
            return ESat.FALSE;
        }
        if (vars[1].getLB() < lb || vars[2].getLB() < lb) {
            return ESat.UNDEFINED;
        }
        if (vars[0].isInstantiated()) {
            if (vars[1].isInstantiatedTo(lb) || vars[2].isInstantiatedTo(lb)) {
                return ESat.TRUE;
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return BST.toString() + ".MIN(" + v1.toString() + "," + v2.toString() + ")";
    }

}
