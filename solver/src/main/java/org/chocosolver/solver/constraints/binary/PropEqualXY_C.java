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
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * X + Y = C
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 1 oct. 2010
 */
@Explained
public final class PropEqualXY_C extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final int cste;
    // incremental filtering of enumerated domains
    private boolean bothEnumerated;
    private IIntDeltaMonitor[] idms;
    private IntProcedure rem_proc;
    private int indexToFilter;

    public PropEqualXY_C(IntVar[] vars, int c) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
        if (x.hasEnumeratedDomain() && y.hasEnumeratedDomain()) {
            bothEnumerated = true;
            idms = new IIntDeltaMonitor[2];
            idms[0] = vars[0].monitorDelta(this);
            idms[1] = vars[1].monitorDelta(this);
            rem_proc = i -> vars[indexToFilter].removeValue(cste - i, this,
                    lcg() ? Reason.r(vars[1 - indexToFilter].getLit(i, IntVar.LR_NE)) : Reason.undef());
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vars[0].hasEnumeratedDomain() && vars[1].hasEnumeratedDomain())
            return IntEventType.all();
        else
            return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        updateBounds();
        // ensure that, in case of enumerated domains, holes are also propagated
        if (bothEnumerated) {
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (!y.contains(cste - val)) {
                    x.removeValue(val, this, lcg() ? Reason.r(x.getLit(val, IntVar.LR_NE)) : Reason.undef());
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!x.contains(cste - val)) {
                    y.removeValue(val, this, lcg() ? Reason.r(y.getLit(val, IntVar.LR_NE)) : Reason.undef());
                }
            }
            idms[0].startMonitoring();
            idms[1].startMonitoring();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        updateBounds();
        if (bothEnumerated) {
            indexToFilter = 1 - varIdx;
            idms[varIdx].forEachRemVal(rem_proc);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void updateBounds() throws ContradictionException {
        if (lcg()) {
            while (x.updateLowerBound(cste - y.getUB(), this, Reason.r(y.getMaxLit()))
                    | y.updateUpperBound(cste - x.getLB(), this, Reason.r(x.getMinLit()))) ;
            while (x.updateUpperBound(cste - y.getLB(), this, Reason.r(y.getMinLit()))
                    | y.updateLowerBound(cste - x.getUB(), this, Reason.r(x.getMaxLit()))) ;
        } else {
            while (x.updateLowerBound(cste - y.getUB(), this)
                    | y.updateUpperBound(cste - x.getLB(), this)) ;
            while (x.updateUpperBound(cste - y.getLB(), this)
                    | y.updateLowerBound(cste - x.getUB(), this)) ;
        }
    }

    @Override
    public ESat isEntailed() {
        if ((x.getUB() + y.getUB() < cste) ||
                (x.getLB() + y.getLB() > cste) ||
                x.hasEnumeratedDomain() && y.hasEnumeratedDomain() && !match())
            return ESat.FALSE;
        else if (x.isInstantiated() &&
                y.isInstantiated() &&
                (x.getValue() + y.getValue() == cste))     // <nj> was false
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }


    private boolean match() {
        int lb = x.getLB();
        int ub = x.getUB();
        for (; lb <= ub; lb = x.nextValue(lb)) {
            if (y.contains(cste - lb)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return vars[0] + " + " + vars[1] + " = " + cste;
    }

}
