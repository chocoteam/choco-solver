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
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Enforces X = |Y|
 * <br/>
 *
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 18/05/11
 */
@Explained
public class PropAbsolute extends Propagator<IntVar> {

    private final IntVar X;
    private final IntVar Y;
    private final boolean bothEnumerated;

    public PropAbsolute(IntVar X, IntVar Y) {
        super(ArrayUtils.toArray(X, Y), PropagatorPriority.BINARY, true);
        this.X = vars[0];
        this.Y = vars[1];
        bothEnumerated = X.hasEnumeratedDomain() && Y.hasEnumeratedDomain();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[0].hasEnumeratedDomain() && vars[1].hasEnumeratedDomain()) {
            return IntEventType.all();
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
        X.updateLowerBound(0, this, Reason.undef());
        setBounds();
        if (bothEnumerated) {
            enumeratedFiltering();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        if (IntEventType.isInstantiate(mask)) {
            if (varIdx == 1) {
                X.instantiateTo(Math.abs(Y.getValue()), this, lcg() ? Reason.r(Y.getValLit()) : Reason.undef());
                setPassive();
            } else if (Y.hasEnumeratedDomain()) {
                int val = X.getValue();
                Y.updateLowerBound(-val, this, lcg() ? Reason.r(X.getValLit()) : Reason.undef());
                Y.updateUpperBound(val, this, lcg() ? Reason.r(X.getValLit()) : Reason.undef());
                val--;
                for (int v = -val; v <= val; v = Y.nextValue(v)) {
                    Y.removeValue(v, this, lcg() ? Reason.r(X.getValLit()) : Reason.undef());
                }
                setPassive();
            } else {
                setBounds();
            }
        } else {
            if (IntEventType.isBound(mask)) {
                setBounds();
            }
            if (bothEnumerated) {
                enumeratedFiltering();
            }
        }
    }

    private void setBounds() throws ContradictionException {
        // X = |Y|
        int max = X.getUB();
        int min = X.getLB();
        Y.updateLowerBound(-max, this, lcg() ? Reason.r(X.getMaxLit()) : Reason.undef());
        Y.updateUpperBound(max, this, lcg() ? Reason.r(X.getMaxLit()) : Reason.undef());
        for (int v = 1 - min; v <= min - 1; v = Y.nextValue(v)) {
            Y.removeValue(v, this, lcg() ? Reason.r(X.getMinLit()) : Reason.undef());
        }
        //if (!lcg()) Y.removeInterval(1 - min, min - 1, this);
        /////////////////////////////////////////////////
        int prevLB = X.getLB();
        int prevUB = X.getUB();
        min = Y.getLB();
        max = Y.getUB();
        if (max <= 0) {
            X.updateLowerBound(-max, this,
                    lcg() ? Reason.r(Y.getMaxLit()) : Reason.undef());
            X.updateUpperBound(-min, this,
                    lcg() ? Reason.r(Y.getMinLit(), Y.getMaxLit()) : Reason.undef());
        } else if (min >= 0) {
            X.updateLowerBound(min, this,
                    lcg() ? Reason.r(Y.getMinLit()) : Reason.undef());
            X.updateUpperBound(max, this,
                    lcg() ? Reason.r(Y.getMinLit(), Y.getMaxLit()) : Reason.undef());
        } else {
            if (Y.hasEnumeratedDomain() && !lcg()) {
                int mP = Y.nextValue(-1);
                int mN = -Y.previousValue(1);
                X.updateLowerBound(Math.min(mP, mN), this);
            }
                X.updateUpperBound(Math.max(-min, max), this,
                        lcg() ? Reason.r(Y.getMinLit(), Y.getMaxLit()) : Reason.undef());
        }
        if (prevLB != X.getLB() || prevUB != X.getUB()) setBounds();
    }

    private void enumeratedFiltering() throws ContradictionException {
        int min = X.getLB();
        int max = X.getUB();
        for (int v = min; v <= max; v = X.nextValue(v)) {
            if (!(Y.contains(v) || Y.contains(-v))) {
                X.removeValue(v, this,
                        lcg() ? Reason.r(Y.getLit(v, IntVar.LR_EQ), Y.getLit(-v, IntVar.LR_EQ)) : Reason.undef());
            }
        }
        min = Y.getLB();
        max = Y.getUB();
        for (int v = min; v <= max; v = Y.nextValue(v)) {
            if (!(X.contains(Math.abs(v)))) {
                Y.removeValue(v, this,
                        lcg() ? Reason.r(X.getLit(Math.abs(v), IntVar.LR_EQ)) : Reason.undef());
            }
        }
    }

    //***********************************************************************************
    // EXPLANATIONS
    //***********************************************************************************

}
