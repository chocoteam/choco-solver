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

public class RealSquareTest extends RealBase {

    @Test
    public void sqr1Test() throws ContradictionException {
        postExpression(y.eq(x.pow(2.0)));
        model.getSolver().propagate();
        assertBound(x, -10.0, 10.0);
        assertBound(y, 0.0, 100.0);
    }

    @Test
    public void sqr2Test() throws ContradictionException {
        postExpression(x.ge(-2.5));
        postExpression(x.le(4.5));
        postExpression(y.eq(x.pow(2.0)));
        model.getSolver().propagate();
        assertBound(x, -2.5, 4.5);
        assertBound(y, 0.0, 20.25);
    }

    @Test
    public void sqr3Test() throws ContradictionException {
        postExpression(x.ge(2.5));
        postExpression(x.le(4.5));
        postExpression(y.eq(x.pow(2.0)));
        model.getSolver().propagate();
        assertBound(x, 2.5, 4.5);
        assertBound(y, 6.25, 20.25);
    }

    @Test
    public void sqr4Test() throws ContradictionException {
        postExpression(x.ge(-4.5));
        postExpression(x.le(-2.5));
        postExpression(y.eq(x.pow(2.0)));
        model.getSolver().propagate();
        assertBound(x, -4.5, -2.5);
        assertBound(y, 6.25, 20.25);
    }

    @Test
    @Ignore("Expecting IBEX release the contraction ratio parameter")
    public void imp1Test() throws ContradictionException {
        postExpression(x.ge(0.0));
        postExpression(y.eq(3.5));
        w.eq(1).imp(getExpression(x.pow(2.0).gt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, 1.870829, 100.0);
    }

    @Test
    public void imp2Test() throws ContradictionException {
        postExpression(x.ge(0.0));
        postExpression(y.eq(10.5));
        w.eq(1).imp(getExpression(x.pow(2.0).lt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, 0.0, 3.24037);
    }

    @Test
    @Ignore("Expecting IBEX release the contraction ratio parameter")
    public void imp3Test() throws ContradictionException {
        postExpression(x.le(0.0));
        postExpression(y.eq(3.5));
        w.eq(1).imp(getExpression(x.pow(2.0).gt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, -100.0, -1.870829);
    }

    @Test
    public void imp4Test() throws ContradictionException {
        postExpression(x.le(0.0));
        postExpression(y.eq(10.5));
        w.eq(1).imp(getExpression(x.pow(2.0).lt(y))).post();
        w.eq(1).post();
        model.getSolver().propagate();
        assertBound(x, -3.24037, 0.0);
    }

    @Test
    public void sqr5Test() throws ContradictionException {
        postExpression(z.eq(25.5));
        postExpression(y.eq(4.5));
        postExpression(x.pow(2.0).le(z));
        postExpression(x.pow(2.0).ge(y));
        model.getSolver().propagate();
        assertBound(x, -5.049752, 5.049752);
    }

    @Test
    public void sqr6Test() throws ContradictionException {
        postExpression(z.eq(25.5));
        postExpression(y.eq(4.5));
        postExpression(x.ge(0.0));
        postExpression(x.pow(2.0).lt(z));
        postExpression(x.pow(2.0).gt(y));
        model.getSolver().propagate();
        assertBound(x, 2.121321, 5.049752);
    }

    @Test
    public void sqr7Test() throws ContradictionException {
        postExpression(z.eq(25.5));
        postExpression(y.eq(4.5));
        postExpression(x.le(0.0));
        postExpression(x.pow(2.0).le(z));
        postExpression(x.pow(2.0).ge(y));
        model.getSolver().propagate();
        assertBound(x, -5.049752, -2.12132);
    }

    @Test
    public void sqr8Test() throws ContradictionException {
        postExpression(x.ge(0.0));
        postExpression(x.le(Math.toRadians(45)));
        postExpression(y.eq(x.cos()));
        postExpression(z.eq(y.pow(2.0)));
        postExpression(v.eq(z.sqrt()));
        postExpression(v.le(0.9));

        model.getSolver().propagate();
        assertBound(x, 0.451027, 0.785398);
        assertBound(y, 0.707107, 0.9);
        assertBound(z, 0.5, 0.81);
        assertBound(v, 0.707107, 0.9);
    }

    @Test
    public void sqr9Test() throws ContradictionException {
        postExpression(x.ge(0.2));
        postExpression(x.le(0.8));
        postExpression(y.eq(x.pow(2.0)));
        model.getSolver().propagate();
        assertBound(x, 0.2, 0.8);
        assertBound(y, 0.04, 0.64);
    }

    @Test
    public void sqr10Test() throws ContradictionException {
        postExpression(x.ge(-0.2));
        postExpression(x.le(0.8));
        postExpression(y.eq(x.pow(2.0)));
        model.getSolver().propagate();
        assertBound(x, -0.2, 0.8);
        assertBound(y, 0.0, 0.64);
    }

}
