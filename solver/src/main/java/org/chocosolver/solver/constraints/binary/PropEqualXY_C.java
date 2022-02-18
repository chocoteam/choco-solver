/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * X + Y = C
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 1 oct. 2010
 */

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
            rem_proc = i -> vars[indexToFilter].removeValue(cste - i, this);
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
                    x.removeValue(val, this);
                }
            }
            ub = y.getUB();
            for (int val = y.getLB(); val <= ub; val = y.nextValue(val)) {
                if (!x.contains(cste - val)) {
                    y.removeValue(val, this);
                }
            }
            idms[0].startMonitoring();
            idms[1].startMonitoring();
        }
        if (x.isInstantiated()) {
            assert (y.isInstantiated());
            setPassive();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        updateBounds();
        if (x.isInstantiated()) {
            assert (y.isInstantiated());
            setPassive();
        } else if (bothEnumerated) {
            indexToFilter = 1 - varIdx;
            idms[varIdx].forEachRemVal(rem_proc);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void updateBounds() throws ContradictionException {
        while (x.updateLowerBound(cste - y.getUB(), this) | y.updateUpperBound(cste - x.getLB(), this)) ;
        while (x.updateUpperBound(cste - y.getLB(), this) | y.updateLowerBound(cste - x.getUB(), this)) ;
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
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntIterableRangeSet set0, set1;
        if (explanation.readVar(p) == vars[0]) { // case a. (see javadoc)
            set1 = explanation.complement(vars[1]);
            set0 = explanation.domain(vars[1]);
            set0.times(-1);
            set0.plus(cste);
            vars[0].intersectLit(set0, explanation);
            vars[1].unionLit(set1, explanation);
        } else { // case b. (see javadoc)
            assert explanation.readVar(p) == vars[1];
            set0 = explanation.complement(vars[0]);
            set1 = explanation.domain(vars[0]);
            set1.times(-1);
            set1.plus(cste);
            vars[0].unionLit(set0, explanation);
            vars[1].intersectLit(set1, explanation);
        }
    }

    @Override
    public String toString() {
        return vars[0] + " + " + vars[1] + " = " + cste;
    }

}
