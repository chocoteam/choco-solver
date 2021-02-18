/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.expression.continuous.arithmetic.RealIntervalConstant;
import org.chocosolver.util.objects.RealInterval;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/07/2020
 */
public class RealUtilsTest {

    @DataProvider
    public Object[][] forDiv() {
        return new Object[][]{
                {1., 3., 1., 3., 1. / 3., 3.}, // ++
                {-3., -1., 1., 3., -3, -1. / 3.}, // -+
                {1., 3., -3., -1., -3., -1. / 3.}, // +-
                {-3., -1., -3., -1., 1. / 3., 3.}, // --
                {-3., 2., 1., 3., -3., 2.}, // 0+
                {-3., 2., -3., -1., -2., 3.}, // 0-
                {-3., 2., -1., 4., Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY},  // 00
                {-2., 3., -4., 1., Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}, // 00
                {.0, .2, -2., 2., Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}, // +0
                {-.2, 0., -2., 2., Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY},// -0
        };
    }

    @Test(groups = "1s", dataProvider = "forDiv")
    public void testOdiv1(double xl, double xh, double yl, double yh, double rl, double rh) {
        RealInterval x = new RealIntervalConstant(xl, xh);
        RealInterval y = new RealIntervalConstant(yl, yh);
        RealInterval r = RealUtils.odiv(x, y);
        Assert.assertEquals(RealUtils.prevFloat(rl), r.getLB());
        Assert.assertEquals(RealUtils.nextFloat(rh), r.getUB());
    }

    @Test(groups = "1s", expectedExceptions = ArithmeticException.class)
    public void testOdiv2() {
        testOdiv1(1., 2., 0., 0., Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @DataProvider
    public Object[][] forMul() {
        return new Object[][]{
                {1., 3., 1., 3., 1., 9.},
                {-3., -1., 1., 3., -9, -1.},
                {1., 3., -1., 3., -3., 9.},
                {1., 3., -3., -1., -9., -1.},
                {-3., -1., -3., -1., 1., 9.},
                {-3., -1., -3., 1., -3., 9.},
                {-3., 2., 1., 3., -9., 6.},
                {-3., 2., -3., -1., -6, 9.},
                {-3., 2., -1., 4., -12., 8},
                {-2., 3., -4., 1., -12., 8},
                {0., 0., -4., 1., RealUtils.nextFloat(-0.), RealUtils.prevFloat(0.)},
                {-2., 3., 0., 0., RealUtils.nextFloat(-0.), RealUtils.prevFloat(0.)},

        };
    }

    @Test(groups = "1s", dataProvider = "forMul")
    public void testMul1(double xl, double xh, double yl, double yh, double rl, double rh) {
        RealInterval x = new RealIntervalConstant(xl, xh);
        RealInterval y = new RealIntervalConstant(yl, yh);
        RealInterval r = RealUtils.mul(x, y);
        Assert.assertEquals(RealUtils.prevFloat(rl), r.getLB());
        Assert.assertEquals(RealUtils.nextFloat(rh), r.getUB());
    }
}