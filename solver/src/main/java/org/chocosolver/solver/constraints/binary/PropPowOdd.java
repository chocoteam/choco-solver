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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.MathUtils;

/**
 * Enforces X = Y^n
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
public class PropPowOdd extends PropPowEven {
    private static final double PRECISION = 1e-9;

    public PropPowOdd(IntVar X, IntVar Y, int n) {
        super(X, Y, n);
    }

    @Override
    public ESat isEntailed() {
        if (vars[0].isInstantiated()) {
            if (vars[1].isInstantiated()) {
                return ESat.eval(vars[0].getValue() == pow(vars[1].getValue()));
            } else if (vars[1].getDomainSize() == 2 &&
                    vars[1].contains(-floor_nroot(vars[0].getValue())) &&
                    vars[1].contains(-floor_nroot(vars[0].getValue()))) {
                return ESat.TRUE;
            } else if (!vars[1].contains(floor_nroot(vars[0].getValue())) &&
                    !vars[1].contains(-floor_nroot(vars[0].getValue()))) {
                return ESat.FALSE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.UNDEFINED;
        }
    }

    @Override
    int floor_nroot(int b) {
        if (b < 0) {
            // See Math.pow Javadoc for reasons of this case
            return -MathUtils.safeCast((long) Math.floor(Math.pow(-b, 1. / (exponent - PRECISION))));
        } else {
            return MathUtils.safeCast((long) Math.floor(Math.pow(b, 1. / (exponent - PRECISION))));
        }
    }

    @Override
    int ceil_nroot(int b) {
        if (b < 0) {
            // See Math.pow Javadoc for reasons of this case
            return -MathUtils.safeCast((long) Math.ceil(Math.pow(-b, 1. / (exponent + PRECISION))));
        } else {
            return MathUtils.safeCast((long) Math.ceil(Math.pow(b, 1. / (exponent + PRECISION))));
        }
    }

    @Override
    boolean perfectNth(int n) {
        long tst;
        if (n < 0) {
            // See Math.pow Javadoc for reasons of this case
            tst = -(long) Math.pow(-n, 1. / (exponent - PRECISION));
        } else {
            tst = (long) Math.pow(n, 1. / (exponent - PRECISION));
        }
        return Math.pow(tst, exponent) == n;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void updateLowerBoundofX() throws ContradictionException {
        int a0 = vars[1].nextValue(-1);
        int b0 = Math.max(Integer.MIN_VALUE + 1, vars[1].previousValue(1));
        int lb = Math.min(pow(a0), pow(b0));
        lb = Math.min(lb, pow(vars[1].getLB()));
        vars[0].updateLowerBound(lb, this);
    }

    @Override
    protected void updateUpperBoundofX() throws ContradictionException {
        vars[0].updateUpperBound(Math.max(pow(vars[1].getLB()), pow(vars[1].getUB())), this);
    }

    @Override
    protected boolean updateLowerBoundofY() throws ContradictionException {
        return vars[1].updateLowerBound(Math.min(ceil_nroot(vars[0].getLB()), floor_nroot(vars[0].getUB())), this);
    }

    @Override
    protected boolean updateUpperBoundofY() throws ContradictionException {
        return vars[1].updateUpperBound(Math.max(ceil_nroot(vars[0].getLB()), floor_nroot(vars[0].getUB())), this);
    }

}
