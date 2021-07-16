/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

/**
 * A propagator to ensure that X + Y = Z holds, where X, Y and Z are IntVar.
 * This propagator ensures AC when all variables are enumerated, BC otherwise.
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 03/02/2016.
 */
public class PropXplusYeqZ extends Propagator<IntVar> {

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
    private final IntIterableRangeSet r1;
    private final IntIterableRangeSet r2;
    private final IntIterableRangeSet r3;

    /**
     * Create propagator for ternary sum: X + Y =Z
     * @param X an integer variable
     * @param Y an integer variable
     * @param Z an integer variable
     */
    public PropXplusYeqZ(IntVar X, IntVar Y, IntVar Z) {
        super(new IntVar[]{X, Y, Z}, PropagatorPriority.TERNARY, false);
        allbounded = (!X.hasEnumeratedDomain() & !Y.hasEnumeratedDomain() & !Z.hasEnumeratedDomain());
        r1 = new IntIterableRangeSet();
        r2 = new IntIterableRangeSet();
        r3 = new IntIterableRangeSet();
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
     * @param vr position of in vars
     * @param v1 position of in vars
     * @param v2 position of in vars
     * @return <tt>true</tt> if vars[vr] has changed
     * @throws ContradictionException if failure occurs
     */
    private boolean filterPlus(int vr, int v1, int v2) throws ContradictionException {
        int lb = vars[v1].getLB() + vars[v2].getLB();
        int ub = vars[v1].getUB() + vars[v2].getUB();
        boolean change = vars[vr].updateBounds(lb, ub, this);
        if (!allbounded) {
            r1.copyFrom(vars[v1]);
            r2.copyFrom(vars[v2]);
            IntIterableSetUtils.plus(r3, r1, r2);
            change |= vars[vr].removeAllValuesBut(r3, this);
        }
        return change;
    }

    /**
     * Remove from vars[vr] holes resulting of vars[v1] - vars[v2]
     * @param vr position of in vars
     * @param v1 position of in vars
     * @param v2 position of in vars
     * @return <tt>true</tt> if vars[vr] has changed
     * @throws ContradictionException if failure occurs
     */
    private boolean filterMinus(int vr, int v1, int v2) throws ContradictionException {
        int lb = vars[v1].getLB() - vars[v2].getUB();
        int ub = vars[v1].getUB() - vars[v2].getLB();
        boolean change = vars[vr].updateBounds(lb, ub, this);
        if (!allbounded) {
            r1.copyFrom(vars[v1]);
            r2.copyFrom(vars[v2]);
            IntIterableSetUtils.minus(r3, r1, r2);
            change |= vars[vr].removeAllValuesBut(r3, this);
        }
        return change;
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(vars[x].getValue() + vars[y].getValue() == vars[z].getValue());
        }
        return ESat.UNDEFINED;
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
//        super.explain(explanation, front, ig, p);
        int m = explanation.readMask(p);
        IntVar pivot = explanation.readVar(p);
        IntIterableRangeSet dx, dy, dz;
        if (IntEventType.isInclow(m)) {
            if (pivot == vars[z]) {
                int a = explanation.readDom(vars[x]).min();
                int b = explanation.readDom(vars[y]).min();
                dx = explanation.universe();
                dx.removeBetween(a, IntIterableRangeSet.MAX);
                dy = explanation.universe();
                dy.removeBetween(b, IntIterableRangeSet.MAX);
                vars[x].unionLit(dx, explanation);
                vars[y].unionLit(dy, explanation);
                vars[z].intersectLit(a + b, IntIterableRangeSet.MAX, explanation);
            } else if (pivot == vars[x]) {
                int a = explanation.readDom(vars[y]).max();
                int b = explanation.readDom(vars[z]).min();
                dy = explanation.universe();
                dy.removeBetween(IntIterableRangeSet.MIN, a);
                dz = explanation.universe();
                dz.removeBetween(b, IntIterableRangeSet.MAX);
                vars[x].intersectLit(b - a, IntIterableRangeSet.MAX, explanation);
                vars[y].unionLit(dy, explanation);
                vars[z].unionLit(dz, explanation);
            } else {
                int a = explanation.readDom(vars[x]).max();
                int b = explanation.readDom(vars[z]).min();
                dx = explanation.universe();
                dx.removeBetween(IntIterableRangeSet.MIN, a);
                dz = explanation.universe();
                dz.removeBetween(b, IntIterableRangeSet.MAX);
                vars[x].unionLit(dx, explanation);
                vars[y].intersectLit(b - a, IntIterableRangeSet.MAX, explanation);
                vars[z].unionLit(dz, explanation);
            }
        } else if (IntEventType.isDecupp(m)) {
            if (pivot == vars[z]) {
                int a = explanation.readDom(vars[x]).max();
                int b = explanation.readDom(vars[y]).max();
                dx = explanation.universe();
                dx.removeBetween(IntIterableRangeSet.MIN, a);
                dy = explanation.universe();
                dy.removeBetween(IntIterableRangeSet.MIN, b);
                vars[x].unionLit(dx, explanation);
                vars[y].unionLit(dy, explanation);
                vars[z].intersectLit(IntIterableRangeSet.MIN, a + b, explanation);
            } else if (pivot == vars[x]) {
                int a = explanation.readDom(vars[y]).min();
                int b = explanation.readDom(vars[z]).max();
                dy = explanation.universe();
                dy.removeBetween(a, IntIterableRangeSet.MAX);
                dz = explanation.universe();
                dz.removeBetween(IntIterableRangeSet.MIN, b);
                vars[x].intersectLit(IntIterableRangeSet.MIN, b - a, explanation);
                vars[y].unionLit(dy, explanation);
                vars[z].unionLit(dz, explanation);
            } else {
                int a = explanation.readDom(vars[x]).min();
                int b = explanation.readDom(vars[z]).max();
                dx = explanation.universe();
                dx.removeBetween(a, IntIterableRangeSet.MAX);
                dz = explanation.universe();
                dz.removeBetween(IntIterableRangeSet.MIN, b);
                vars[x].unionLit(dx, explanation);
                vars[y].intersectLit(IntIterableRangeSet.MIN, b - a, explanation);
                vars[z].unionLit(dz, explanation);
            }
        } else { // remove
            assert IntEventType.isRemove(m);
            Propagator.defaultExplain(this, p, explanation);
        }
    }
}
