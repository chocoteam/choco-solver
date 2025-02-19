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
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * A propagator to ensure that X + Y = Z holds, where X, Y and Z are IntVar.
 * This propagator ensures AC when all variables are enumerated, BC otherwise.
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 03/02/2016.
 */
@Explained(partial = true, comment = "AC disabled due to lack of explanation")
public class PropXplusYeqZ extends Propagator<IntVar> {

    private static final int THRESHOLD = 300;
    /**
     * Set to <tt>true</tt> if X, Y and Z are bounded
     */
    private final boolean allbounded;
    /**
     * Temporary structure to ease filtering
     */
    private final IntIterableRangeSet set;

    /**
     * Create propagator for ternary sum: X + Y =Z
     *
     * @param X an integer variable
     * @param Y an integer variable
     * @param Z an integer variable
     */
    public PropXplusYeqZ(IntVar X, IntVar Y, IntVar Z) {
        super(new IntVar[]{X, Y, Z}, PropagatorPriority.TERNARY, false);
        allbounded = (!X.hasEnumeratedDomain() & !Y.hasEnumeratedDomain() & !Z.hasEnumeratedDomain());
        set = new IntIterableRangeSet();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        /*while */
        boolean loop;
        do {
            loop = filterPlus();
            loop |= filterMinus(0); // x is at pos 0 in vars
            loop |= filterMinus(1); // y is at pos 1 in vars
        } while (loop);
    }

    /**
     * Remove from vars[2] holes resulting of vars[0] + vars[1]
     *
     * @return <tt>true</tt> if vars[vr] has changed
     * @throws ContradictionException if failure occurs
     */
    private boolean filterPlus() throws ContradictionException {
        int lb = vars[0].getLB() + vars[1].getLB();
        int ub = vars[0].getUB() + vars[1].getUB();
        boolean change = vars[2].updateLowerBound(lb, this,
                lcg() ? Reason.r(vars[0].getMinLit(), vars[1].getMinLit()) : Reason.undef());
        change |= vars[2].updateUpperBound(ub, this,
                lcg() ? Reason.r(vars[0].getMaxLit(), vars[1].getMaxLit()) : Reason.undef());
        if (!allbounded) {
            if ((long) vars[0].getDomainSize() * vars[1].getDomainSize() > THRESHOLD || lcg()) return change;
            set.clear();
            int ub1 = vars[0].getUB();
            int ub2 = vars[1].getUB();
            int l1 = vars[0].getLB();
            int u1 = vars[0].nextValueOut(l1) - 1;
            while (u1 <= ub1) {
                int l2 = vars[1].getLB();
                int u2 = vars[1].nextValueOut(l2) - 1;
                while (u2 <= ub2) {
                    set.addBetween(l1 + l2, u1 + u2);
                    l2 = vars[1].nextValue(u2);
                    u2 = vars[1].nextValueOut(l2) - 1;
                }
                l1 = vars[0].nextValue(u1);
                u1 = vars[0].nextValueOut(l1) - 1;
            }
            vars[2].removeAllValuesBut(set, this); // todo explain
        }
        return change;
    }

    /**
     * Remove from vars[vr] holes resulting of vars[2] - vars[1- vr]
     *
     * @param vr position of in vars
     * @return <tt>true</tt> if vars[vr] has changed
     * @throws ContradictionException if failure occurs
     */
    private boolean filterMinus(int vr) throws ContradictionException {
        int vo = 1 - vr;
        int lb = vars[2].getLB() - vars[vo].getUB();
        int ub = vars[2].getUB() - vars[vo].getLB();
        boolean change = vars[vr].updateLowerBound(lb, this,
                lcg() ? Reason.r(vars[2].getMinLit(), vars[vo].getMaxLit()) : Reason.undef());
        change |= vars[vr].updateUpperBound(ub, this,
                lcg() ? Reason.r(vars[2].getMaxLit(), vars[vo].getMinLit()) : Reason.undef());
        if (!allbounded) {
            if ((long) vars[2].getDomainSize() * vars[vo].getDomainSize() > THRESHOLD || lcg()) return change;
            set.clear();
            int ub1 = vars[2].getUB();
            int ub2 = vars[vo].getUB();
            int l1 = vars[2].getLB();
            int u1 = vars[2].nextValueOut(l1) - 1;
            while (u1 <= ub1) {
                int l2 = vars[vo].getLB();
                int u2 = vars[vo].nextValueOut(l2) - 1;
                while (u2 <= ub2) {
                    set.addBetween(l1 - u2, u1 - l2);
                    l2 = vars[vo].nextValue(u2);
                    u2 = vars[vo].nextValueOut(l2) - 1;
                }
                l1 = vars[2].nextValue(u1);
                u1 = vars[2].nextValueOut(l1) - 1;
            }
            vars[vr].removeAllValuesBut(set, this); // todo explain
        }
        return change;
    }

    @Override
    public ESat isEntailed() {
        int sumUB = 0, sumLB = 0, i = 0;
        for (; i < 2; i++) { // first the positive coefficients
            sumLB += vars[i].getLB();
            sumUB += vars[i].getUB();
        }
        for (; i < 3; i++) { // then the negative ones
            sumLB -= vars[i].getUB();
            sumUB -= vars[i].getLB();
        }
        if (sumLB == 0 && sumUB == 0) {
            return ESat.TRUE;
        }
        if (sumUB < 0 || sumLB > 0) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }
}
