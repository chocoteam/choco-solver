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
     * Position of X in {@link #vars}
     */
    private final int x = 0;
    /**
     * Position of Y in {@link #vars}
     */
    private final int y = 1;
    /**
     * Position of Z in {@link #vars}
     */
    private final int z = 2;
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
            loop = filterPlus(z, x, y);
            loop |= filterMinus(x, z, y);
            loop |= filterMinus(y, z, x);
            loop &= allbounded; // loop only when BC is selected
        } while (loop);
    }

    /**
     * Remove from vars[vr] holes resulting of vars[v1] + vars[v2]
     *
     * @param vr position of in vars
     * @param v1 position of in vars
     * @param v2 position of in vars
     * @return <tt>true</tt> if vars[vr] has changed
     * @throws ContradictionException if failure occurs
     */
    private boolean filterPlus(int vr, int v1, int v2) throws ContradictionException {
        int lb = vars[v1].getLB() + vars[v2].getLB();
        int ub = vars[v1].getUB() + vars[v2].getUB();
        boolean change = vars[vr].updateLowerBound(lb, this,
                !getModel().getSolver().isLCG() ? Reason.undef() : Reason.r(vars[v1].getMinLit(), vars[v2].getMinLit()));
        change |= vars[vr].updateUpperBound(ub, this,
                !getModel().getSolver().isLCG() ? Reason.undef() : Reason.r(vars[v1].getMaxLit(), vars[v2].getMaxLit()));
        if ((long) vars[v1].getDomainSize() * vars[v2].getDomainSize() > THRESHOLD || getModel().getSolver().isLCG()) return change;
        if (!allbounded) {
            set.clear();
            int ub1 = vars[v1].getUB();
            int ub2 = vars[v2].getUB();
            int l1 = vars[v1].getLB();
            int u1 = vars[v1].nextValueOut(l1) - 1;
            while (u1 <= ub1) {
                int l2 = vars[v2].getLB();
                int u2 = vars[v2].nextValueOut(l2) - 1;
                while (u2 <= ub2) {
                    set.addBetween(l1 + l2, u1 + u2);
                    l2 = vars[v2].nextValue(u2);
                    u2 = vars[v2].nextValueOut(l2) - 1;
                }
                l1 = vars[v1].nextValue(u1);
                u1 = vars[v1].nextValueOut(l1) - 1;
            }
            change |= vars[vr].removeAllValuesBut(set, this); // todo explain
        }
        return change;
    }

    /**
     * Remove from vars[vr] holes resulting of vars[v1] - vars[v2]
     *
     * @param vr position of in vars
     * @param v1 position of in vars
     * @param v2 position of in vars
     * @return <tt>true</tt> if vars[vr] has changed
     * @throws ContradictionException if failure occurs
     */
    private boolean filterMinus(int vr, int v1, int v2) throws ContradictionException {
        int lb = vars[v1].getLB() - vars[v2].getUB();
        int ub = vars[v1].getUB() - vars[v2].getLB();
        boolean change = vars[vr].updateLowerBound(lb, this,
                !getModel().getSolver().isLCG() ? Reason.undef() : Reason.r(vars[v1].getMinLit(), vars[v2].getMaxLit()));
        change |= vars[vr].updateUpperBound(ub, this,
                !getModel().getSolver().isLCG() ? Reason.undef() : Reason.r(vars[v1].getMaxLit(), vars[v2].getMinLit()));
        if ((long) vars[v1].getDomainSize() * vars[v2].getDomainSize() > THRESHOLD || getModel().getSolver().isLCG()) return change;
        if (!allbounded) {
            set.clear();
            int ub1 = vars[v1].getUB();
            int ub2 = vars[v2].getUB();
            int l1 = vars[v1].getLB();
            int u1 = vars[v1].nextValueOut(l1) - 1;
            while (u1 <= ub1) {
                int l2 = vars[v2].getLB();
                int u2 = vars[v2].nextValueOut(l2) - 1;
                while (u2 <= ub2) {
                    set.addBetween(l1 - u2, u1 - l2);
                    l2 = vars[v2].nextValue(u2);
                    u2 = vars[v2].nextValueOut(l2) - 1;
                }
                l1 = vars[v1].nextValue(u1);
                u1 = vars[v1].nextValueOut(l1) - 1;
            }
            change |= vars[vr].removeAllValuesBut(set, this); // todo explain
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
