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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.MathUtils;

/**
 * Enforces X = Y^n where n is odd
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
public class PropPowOdd extends PropPowEven {
    public PropPowOdd(IntVar X, IntVar Y, int n) {
        super(X, Y, n);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.eval(vars[0].getValue() == pow(vars[1].getValue()));
        }
        return ESat.UNDEFINED;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void updateLowerBoundofX() throws ContradictionException {
        vars[0].updateLowerBound(pow(vars[1].getLB()), this);
    }

    @Override
    protected void updateUpperBoundofX() throws ContradictionException {
        vars[0].updateUpperBound(pow(vars[1].getUB()), this);
    }

    @Override
    protected boolean updateLowerBoundofY() throws ContradictionException {
        if (vars[0].getLB() < 0) {
            return vars[1].updateLowerBound(floor_nroot(vars[0].getLB()), this);
        } else {
            return vars[1].updateLowerBound(ceil_nroot(vars[0].getLB()), this);
        }
    }

    @Override
    protected boolean updateUpperBoundofY() throws ContradictionException {
        if (vars[0].getUB() < 0) {
            return vars[1].updateUpperBound(ceil_nroot(vars[0].getUB()), this);
        } else {
            return vars[1].updateUpperBound(floor_nroot(vars[0].getUB()), this);
        }
    }

}
