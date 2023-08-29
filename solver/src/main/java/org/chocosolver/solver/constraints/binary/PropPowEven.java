/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.tools.ArrayUtils;
import org.chocosolver.util.tools.MathUtils;

/**
 * Enforces X = Y^n where n is even
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
public class PropPowEven extends Propagator<IntVar> {
    final IntIterableBitSet vrms;
    final boolean bothEnum;
    final int exponent;

    public PropPowEven(IntVar X, IntVar Y, int n) {
        super(ArrayUtils.toArray(X, Y), PropagatorPriority.BINARY, false);
        bothEnum = X.hasEnumeratedDomain() && Y.hasEnumeratedDomain();
        vrms = new IntIterableBitSet();
        this.exponent = n;
    }


    @Override
    public int getPropagationConditions(int vIdx) {
        return bothEnum ? IntEventType.all() : IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        do {
            setBounds();
        } while (updateHolesinX() | updateHolesinY());
    }

    @Override
    public ESat isEntailed() {
        if (vars[0].getUB() < 0) {
            return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) {
            return ESat.eval(vars[0].getValue() == pow(vars[1].getValue()));
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return String.format("%s = %s^%d", vars[0].toString(), vars[1].toString(), exponent);
    }

    void setBounds() throws ContradictionException {
        updateLowerBoundofX();
        updateUpperBoundofX();
        if (updateLowerBoundofY() | updateUpperBoundofY()) {
            setBounds();
        }
    }

    int floor_nroot(int n) {
        if (n < 0) {
            return 0;
        }
        return MathUtils.safeCast((long) Math.floor(Math.pow(n, 1. / exponent)));
    }

    int ceil_nroot(int n) {
        if (n < 0) {
            return 0;
        }
        return MathUtils.safeCast((long) Math.ceil(Math.pow(n, 1. / exponent)));
    }

    int pow(int n) {
        return MathUtils.safeCast((long) Math.pow(n, exponent));
    }

    boolean perfectNth(int n){
        return Math.pow(floor_nroot(n), exponent) == n;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void updateLowerBoundofX() throws ContradictionException {
        int a0 = vars[1].nextValue(-1);
        int b0 = Math.max(Integer.MIN_VALUE + 1, vars[1].previousValue(1));
        vars[0].updateLowerBound(Math.min(pow(a0), pow(b0)), this);
        if (!perfectNth(vars[0].getLB())) { // might happen when Y.getLB() < 0 < Y.getUB() and both X and Y have bounded domains
            vars[0].updateLowerBound(pow(ceil_nroot(vars[0].getLB())), this);
        }
    }

    protected void updateUpperBoundofX() throws ContradictionException {
        vars[0].updateUpperBound(Math.max(pow(vars[1].getLB()), pow(vars[1].getUB())), this);
    }

    protected boolean updateHolesinX() throws ContradictionException {
        if (!vars[0].hasEnumeratedDomain()) {
            return false;
        }
        boolean impact = false;
        if (model.getSolver().getNodeCount() == 0) { // only at root node propagation
            // check perfect squares once and for all
            int ub = vars[0].getUB();
            vrms.clear();
            vrms.setOffset(vars[0].getLB());
            for (int value = vars[0].getLB(); value <= ub; value = vars[0].nextValue(value)) {
                if (!perfectNth(value)) {
                    vrms.add(value);
                }
            }
            impact = vars[0].removeValues(vrms, this);
        }

        // remove intervals to deal with consecutive value removal and upper bound modification
        if (bothEnum) {
            int ub = vars[0].getUB();
            vrms.clear();
            vrms.setOffset(vars[0].getLB());
            for (int value = vars[0].getLB(); value <= ub; value = vars[0].nextValue(value)) {
                int nroot = floor_nroot(value);
                if (!vars[1].contains(nroot) && !vars[1].contains(-nroot)) {
                    vrms.add(value);
                }
            }
            impact |= vars[0].removeValues(vrms, this);
        }
        return impact;
    }

    protected boolean updateLowerBoundofY() throws ContradictionException {
        if (vars[1].getLB() >= 0) {
            return vars[1].updateLowerBound(ceil_nroot(vars[0].getLB()), this);
        } else {
            return vars[1].updateLowerBound(-floor_nroot(vars[0].getUB()), this);
        }
    }

    protected boolean updateUpperBoundofY() throws ContradictionException {
        if (vars[1].getUB() < 0) {
            return vars[1].updateUpperBound(-ceil_nroot(vars[0].getLB()), this);
        } else {
            return vars[1].updateUpperBound(floor_nroot(vars[0].getUB()), this);
        }
    }

    protected final boolean updateHolesinY() throws ContradictionException {
        if (!vars[1].hasEnumeratedDomain()) {
            return false;
        }
        boolean impact = false;
        // remove interval around 0 based on X LB
        int val = ceil_nroot(vars[0].getLB()) - 1;
        if (val >= 0) {
            impact = vars[1].removeInterval(-val, val, this);
        }
        // remove values based on X holes
        if (bothEnum) {
            int ub = vars[1].getUB();
            vrms.clear();
            vrms.setOffset(vars[1].getLB());
            for (int value = vars[1].getLB(); value <= ub; value = vars[1].nextValue(value)) {
                if (!vars[0].contains(pow(value))) {
                    vrms.add(value);
                }
            }
            impact |= vars[1].removeValues(vrms, this);
        }
        return impact;
    }
}
