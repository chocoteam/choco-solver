/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.ValueSortedMap;
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
    private int x = 0;
    /**
     * Position of Y in {@link #vars}
     */
    private int y = 1;
    /**
     * Position of Z in {@link #vars}
     */
    private int z = 2;
    /**
     * Set to <tt>true</tt> if X, Y and Z are bounded
     */
    private boolean allbounded;
    /**
     * Temporary structure to ease filtering
     */
    private IntIterableRangeSet r1, r2, r3;

    /**
     * Create propagator for ternary sum: X + Y =Z
     * @param X an integer variable
     * @param Y an integer variable
     * @param Z an integer variable
     */
    public PropXplusYeqZ(IntVar X, IntVar Y, IntVar Z, boolean enableAC) {
        super(new IntVar[]{X, Y, Z}, PropagatorPriority.TERNARY, false);
        allbounded = !enableAC || (!X.hasEnumeratedDomain() & !Y.hasEnumeratedDomain() & !Z.hasEnumeratedDomain());
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
            IntIterableSetUtils.copyIn(vars[v1], r1);
            IntIterableSetUtils.copyIn(vars[v2], r2);
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
            IntIterableSetUtils.copyIn(vars[v1], r1);
            IntIterableSetUtils.copyIn(vars[v2], r2);
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
    public void explain(ExplanationForSignedClause explanation, ValueSortedMap<IntVar> front, Implications ig, int p) {
//        super.explain(explanation, front, ig, p);
        int m = ig.getEventMaskAt(p);
        IntVar pivot = ig.getIntVarAt(p);
        IntIterableRangeSet dx, dy, dz;
        if (IntEventType.isInclow(m)) {
            if (pivot == vars[z]) {
                int a = ig.getDomainAt(front.getValue(vars[x])).min();
                int b = ig.getDomainAt(front.getValue(vars[y])).min();
                dz = explanation.getRootSet(vars[z]);
                dz.retainBetween(a + b, IntIterableRangeSet.MAX);
                dx = explanation.getRootSet(vars[x]);
                dx.removeBetween(a, IntIterableRangeSet.MAX);
                dy = explanation.getRootSet(vars[y]);
                dy.removeBetween(b, IntIterableRangeSet.MAX);
                explanation.addLiteral(vars[x], dx, false);
                explanation.addLiteral(vars[y], dy, false);
                explanation.addLiteral(vars[z], dz, true);
            } else if (pivot == vars[x]) {
                int a = ig.getDomainAt(front.getValue(vars[y])).max();
                int b = ig.getDomainAt(front.getValue(vars[z])).min();
                dx = explanation.getRootSet(vars[x]);
                dx.retainBetween(b - a, IntIterableRangeSet.MAX);
                dy = explanation.getRootSet(vars[y]);
                dy.removeBetween(IntIterableRangeSet.MIN, a);
                dz = explanation.getRootSet(vars[z]);
                dz.removeBetween(b, IntIterableRangeSet.MAX);
                explanation.addLiteral(vars[x], dx, true);
                explanation.addLiteral(vars[y], dy, false);
                explanation.addLiteral(vars[z], dz, false);
            } else {
                int a = ig.getDomainAt(front.getValue(vars[x])).max();
                int b = ig.getDomainAt(front.getValue(vars[z])).min();
                dy = explanation.getRootSet(vars[y]);
                dy.retainBetween(b - a, IntIterableRangeSet.MAX);
                dx = explanation.getRootSet(vars[x]);
                dx.removeBetween(IntIterableRangeSet.MIN, a);
                dz = explanation.getRootSet(vars[z]);
                dz.removeBetween(b, IntIterableRangeSet.MAX);
                explanation.addLiteral(vars[x], dx, false);
                explanation.addLiteral(vars[y], dy, true);
                explanation.addLiteral(vars[z], dz, false);
            }
        } else if (IntEventType.isDecupp(m)) {
            if (pivot == vars[z]) {
                int a = ig.getDomainAt(front.getValue(vars[x])).max();
                int b = ig.getDomainAt(front.getValue(vars[y])).max();
                dz = explanation.getRootSet(vars[z]);
                dz.retainBetween(IntIterableRangeSet.MIN, a + b);
                dx = explanation.getRootSet(vars[x]);
                dx.removeBetween(IntIterableRangeSet.MIN, a);
                dy = explanation.getRootSet(vars[y]);
                dy.removeBetween(IntIterableRangeSet.MIN, b);
                explanation.addLiteral(vars[x], dx, false);
                explanation.addLiteral(vars[y], dy, false);
                explanation.addLiteral(vars[z], dz, true);
            } else if (pivot == vars[x]) {
                int a = ig.getDomainAt(front.getValue(vars[y])).min();
                int b = ig.getDomainAt(front.getValue(vars[z])).max();
                dx = explanation.getRootSet(vars[x]);
                dx.retainBetween(IntIterableRangeSet.MIN, b - a);
                dy = explanation.getRootSet(vars[y]);
                dy.removeBetween(a, IntIterableRangeSet.MAX);
                dz = explanation.getRootSet(vars[z]);
                dz.removeBetween(IntIterableRangeSet.MIN, b);
                explanation.addLiteral(vars[x], dx, true);
                explanation.addLiteral(vars[y], dy, false);
                explanation.addLiteral(vars[z], dz, false);
            } else {
                int a = ig.getDomainAt(front.getValue(vars[x])).min();
                int b = ig.getDomainAt(front.getValue(vars[z])).max();
                dy = explanation.getRootSet(vars[y]);
                dy.retainBetween(IntIterableRangeSet.MIN, b - a);
                dx = explanation.getRootSet(vars[x]);
                dx.removeBetween(a, IntIterableRangeSet.MAX);
                dz = explanation.getRootSet(vars[z]);
                dz.removeBetween(IntIterableRangeSet.MIN, b);
                explanation.addLiteral(vars[x], dx, false);
                explanation.addLiteral(vars[y], dy, true);
                explanation.addLiteral(vars[z], dz, false);
            }
        } else { // remove
            assert IntEventType.isRemove(m);
            Propagator.defaultExplain(this, explanation, front, ig, p);
        }
    }
}
