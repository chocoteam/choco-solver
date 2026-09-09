/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
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
 * This propagator ensures that if the boolean variable b is true, then the integer variable x must belong to the set c.
 *
 * @author Charles Prud'homme
 * @since 12/03/2024
 */
@Explained
public class PropXinSHalfReif extends Propagator<IntVar> {
    /**
     * The integer variable that must belong to the set when b is true.
     */
    final IntVar var;
    
    /**
     * The set of allowed values for the integer variable when b is true.
     */
    final IntIterableRangeSet set;
    
    /**
     * The boolean variable that controls the reification: when true, x must be in the set.
     */
    final BoolVar b;

    /**
     * Creates a reified propagator for x in set.
     *
     * @param x    the integer variable to constrain
     * @param set  the set of allowed values for x when b is true
     * @param b    the boolean variable that controls the reification
     */
    public PropXinSHalfReif(IntVar x, IntIterableRangeSet set, BoolVar b) {
        super(new IntVar[]{x, b}, PropagatorPriority.BINARY, false, true);
        this.set = set.duplicate();
        this.var = x;
        this.b = b;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            // var (IntVar) requires all event types for propagation
            return IntEventType.all();
        }
        // b (BoolVar) only requires instantiation events
        return IntEventType.INSTANTIATE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // Propagation logic for b -> (x in set) constraint
        // When b is false, no filtering is required on x
        // When b is true, x must be filtered to only values in the set
        // When x is out of set bounds, b must be false
        if (b.isInstantiatedTo(0)) { // if b is false, then no filtering is required
            setPassive();
            return;
        }
        if (var.getLB() > set.max()) {
            b.setToFalse(this, lcg() ? this.r(var.getMinLit()) : Reason.undef());
            setPassive();
            return;
        } else if (var.getUB() < set.min()) {
            b.setToFalse(this, lcg() ? this.r(var.getMaxLit()) : Reason.undef());
            setPassive();
            return;
        } else if (!set.intersect(var)) {
            // b must be false
            b.setToFalse(this, explainFalse());
            setPassive();
            return;
        }
        if (b.isInstantiatedTo(1)) {
            if (var.hasEnumeratedDomain()) {
                int vub = var.getUB();
                for (int i = var.getLB(); i <= vub; i = var.nextValue(i)) {
                    if (!set.contains(i)) {
                        var.removeValue(i, this, lcg() ?
                                this.r(b.getValLit()) : Reason.undef());
                    }
                }
                setPassive();
            } else {
                int lb = var.getLB();
                int i = 0;
                while (!set.contains(lb + i)) {
                    i++;
                }
                if (i > 0) {
                    var.updateLowerBound(lb + i, this, lcg() ? this.r(b.getValLit()) : Reason.undef());
                    lb += i;
                }
                int ub = var.getUB();
                i = 0;
                while (!set.contains(ub - i)) {
                    i++;
                }
                if (i > 0) {
                    var.updateUpperBound(ub - i, this, lcg() ? this.r(b.getValLit()) : Reason.undef());
                    ub -= i;
                }
                if (!var.isInstantiated()
                        // and both bounds are in the same range
                        && set.nextValueOut(lb) > ub) {
                    setPassive();
                }
            }
        }
    }

    /**
     * Creates a reason explaining why b must be false.
     * The reason is based on the fact that x cannot be in the set (all values in x's domain are outside the set).
     *
     * @return a Reason object explaining the contradiction when using LCG, or Reason.undef() otherwise
     */
    private Reason explainFalse() {
        if (lcg()) {
            int[] ps = new int[set.cardinality() + 1];
            int m = 1;
            for (int i : set) {
                ps[m++] = var.getLit(i, IntVar.LR_EQ);
            }
            return this.r(ps);
        }
        return Reason.undef();
    }

    @Override
    public ESat isEntailed() {
        // Check if the constraint is entailed based on current variable states
        if (isCompletelyInstantiated()) {
            if (b.isInstantiatedTo(1)) {
                return ESat.eval(set.intersect(var));
            }
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
