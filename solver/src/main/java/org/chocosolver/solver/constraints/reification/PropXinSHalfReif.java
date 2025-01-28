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
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * A propagator dedicated to express in a compact way: b &rArr; (x &isin; c)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/03/2024
 */
@Explained
public class PropXinSHalfReif extends Propagator<IntVar> {
    final IntVar var;
    final IntIterableRangeSet set;
    final BoolVar b;

    public PropXinSHalfReif(IntVar x, IntIterableRangeSet set, BoolVar b) {
        super(new IntVar[]{x, b}, PropagatorPriority.BINARY, false, true);
        this.set = set.duplicate();
        this.var = x;
        this.b = b;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return IntEventType.INSTANTIATE.getMask();
        }
        return IntEventType.INCLOW.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (b.isInstantiatedTo(0)) { // if b is false, then no filtering is required
            setPassive();
            return;
        }
        if (var.getLB() > set.max()) {
            b.setToFalse(this, lcg() ? Reason.r(var.getMinLit()) : Reason.undef());
            setPassive();
            return;
        }else
        if (var.getUB() < set.min()) {
            b.setToFalse(this, lcg() ? Reason.r(var.getMaxLit()) : Reason.undef());
            setPassive();
            return;
        }else
        if (!set.intersect(var)) {
            // b must be false
            b.setToFalse(this, explainFalse());
            setPassive();
            return;
        }
        if (b.isInstantiatedTo(1)) {
            int vub = var.getUB();
            for (int i = var.getLB(); i <= vub; i = var.nextValue(i)) {
                if (!set.contains(i)) {
                    var.removeValue(i, this, lcg() ?
                            Reason.r(b.getValLit()) : Reason.undef());
                }
            }
            setPassive();
        }
    }

    private Reason explainFalse() {
        if (lcg()) {
            int[] ps = new int[set.cardinality() + 1];
            int m = 1;
            for (int i : set) {
                ps[m++] = var.getLit(i, IntVar.LR_EQ);
            }
            return Reason.r(ps);
        }
        return Reason.undef();
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (b.isInstantiatedTo(1)) {
                return ESat.eval(set.intersect(var));
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
