/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.real.RealBase;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class RealCubicTest extends RealBase {

    @Test
    public void sqr1Test() throws ContradictionException {
        postExpression(y.eq(x.pow(3)));
        model.getSolver().propagate();
        assertBound(x, -4.641589, 4.641589);
        assertBound(y, -100.0, 100.0);
    }

    @Test
    public void sqr2Test() throws ContradictionException {
        postExpression(x.ge(-2.5));
        postExpression(x.le(4.5));
        postExpression(y.eq(x.pow(3)));
        model.getSolver().propagate();
        assertBound(x, -2.5, 4.5);
        assertBound(y, -15.625, 91.125);
    }

    @Test
    public void sqr3Test() throws ContradictionException {
        postExpression(x.ge(2.5));
        postExpression(x.le(4.5));
        postExpression(y.eq(x.pow(3)));
        model.getSolver().propagate();
        assertBound(x, 2.5, 4.5);
        assertBound(y, 15.625, 91.125);
    }

    @Test
    public void sqr4Test() throws ContradictionException {
        postExpression(x.ge(-4.5));
        postExpression(x.le(-2.5));
        postExpression(y.eq(x.pow(3)));
        model.getSolver().propagate();
        assertBound(x, -4.5, -2.5);
        assertBound(y, -91.125, -15.625);
    }

    @Test
    @Ignore("Expecting IBEX release the contraction ratio parameter")
    public void imp1Test() throws ContradictionException {
        postExpression(x.ge(0));
        postExpression(y.eq(9.5));
        w.eq(1).imp(getExpression(x.pow(3).gt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, 2.117912, 100.0);
    }

    @Test
    public void imp2Test() throws ContradictionException {
        postExpression(x.ge(0));
        postExpression(y.eq(10.5));
        w.eq(1).imp(getExpression(x.pow(3).lt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, 0.0, 2.18976);
    }

    @Test
    public void imp4Test() throws ContradictionException {
        postExpression(x.le(0.0));
        postExpression(y.eq(10.5));
        w.eq(1).imp(getExpression(x.pow(3).lt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, -100.0, 0.0);
    }

    @Test
    public void imp5Test() throws ContradictionException {
        postExpression(x.ge(0));
        postExpression(y.eq(-9.5));
        w.eq(1).imp(getExpression(x.pow(3).gt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, 0.0, 100.0);
    }

    @Test
    public void imp7Test() throws ContradictionException {
        postExpression(x.le(0));
        postExpression(y.eq(-3.5));
        w.eq(1).post();
        w.eq(1).imp(getExpression(x.pow(3).gt(y))).post();
        model.getSolver().propagate();
        assertBound(x, -1.518294, 0.0);
    }

    @Test
    @Ignore("Expecting IBEX release the contraction ratio parameter")
    public void imp8Test() throws ContradictionException {
        postExpression(x.le(0));
        postExpression(y.eq(-10.5));
        w.eq(1).imp(getExpression(x.pow(3).lt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, -100.0, -2.18976);
    }

    @Test
    public void sqr5Test() throws ContradictionException {
        postExpression(z.eq(25.5));
        postExpression(y.eq(-4.5));
        postExpression(x.pow(3).le(z));
        postExpression(x.pow(3).ge(y));
        model.getSolver().propagate();
        assertBound(x, -1.650964, 2.943383);
    }

    @Test
    public void sqr6Test() throws ContradictionException {
        postExpression(z.eq(25.5));
        postExpression(y.eq(4.5));
        postExpression(x.ge(0.0));
        postExpression(x.pow(3).lt(z));
        postExpression(x.pow(3).gt(y));
        model.getSolver().propagate();
        assertBound(x, 1.650964, 2.943383);
    }

    @Test
    public void sqr7Test() throws ContradictionException {
        postExpression(z.eq(25.5));
        postExpression(y.eq(-4.5));
        postExpression(x.le(0.0));
        postExpression(x.pow(3).le(z));
        postExpression(x.pow(3).ge(y));
        model.getSolver().propagate();
        assertBound(x, -1.650964, 0.0);
    }

    @Test
    public void sqr8Test() throws ContradictionException {
        postExpression(x.ge(0));
        postExpression(x.le(Math.toRadians(45)));
        postExpression(y.eq(x.cos()));
        postExpression(z.eq(y.pow(3)));
        postExpression(v.eq(z.sqrt()));
        postExpression(v.le(0.9));

        model.getSolver().propagate();
        assertBound(x, 0.370436, 0.785398);
        assertBound(y, 0.707107, 0.93217);
        assertBound(z, 0.353553, 0.81);
        assertBound(v, 0.594604, 0.9);
    }

    @Test
    public void sqr9Test() throws ContradictionException {
        postExpression(x.ge(0.2));
        postExpression(x.le(0.8));
        postExpression(y.eq(x.pow(3)));
        model.getSolver().propagate();
        assertBound(x, 0.2, 0.8);
        assertBound(y, 0.008, 0.512);
    }

    @Test
    public void sqr10Test() throws ContradictionException {
        postExpression(x.ge(-0.2));
        postExpression(x.le(0.8));
        postExpression(y.eq(x.pow(3)));
        model.getSolver().propagate();
        assertBound(x, -0.2, 0.8);
        assertBound(y, -0.008, 0.512);
    }

}
