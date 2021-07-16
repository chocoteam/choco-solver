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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 15/12/2015.
 */
public class MathUtilsTest {

    @Test(groups="1s", timeOut=60000)
    public void testDivFloor() throws Exception {
        Assert.assertEquals(0, MathUtils.divFloor(3,5));
        Assert.assertEquals(-1, MathUtils.divFloor(-3,5));
        Assert.assertEquals(-1, MathUtils.divFloor(3,-5));
        Assert.assertEquals(0, MathUtils.divFloor(-3,-5));
        Assert.assertEquals(-1, MathUtils.divFloor(-3,3));
        Assert.assertEquals(-1, MathUtils.divFloor(-1,3));
        Assert.assertEquals(1, MathUtils.divFloor(3,3));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.divFloor(10,0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testDivCeil() throws Exception {
        Assert.assertEquals(1, MathUtils.divCeil(3,5));
        Assert.assertEquals(0, MathUtils.divCeil(-3,5));
        Assert.assertEquals(0, MathUtils.divCeil(3,-5));
        Assert.assertEquals(1, MathUtils.divCeil(-3,-5));
        Assert.assertEquals(-1, MathUtils.divCeil(-3,3));
        Assert.assertEquals(0, MathUtils.divCeil(-1,3));
        Assert.assertEquals(1, MathUtils.divCeil(3,3));
        Assert.assertEquals(Integer.MIN_VALUE, MathUtils.divCeil(10,0));
    }


    @Test(groups="1s", timeOut=60000)
    public void testSafeAdd() {
        Assert.assertEquals(MathUtils.safeAdd(1, 1), 2);
        Assert.assertEquals(MathUtils.safeAdd(Integer.MAX_VALUE, 1), Integer.MAX_VALUE);
        Assert.assertEquals(MathUtils.safeAdd(Integer.MIN_VALUE, -1), Integer.MIN_VALUE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSafeSubstract() {
        Assert.assertEquals(MathUtils.safeSubstract(1, 1), 0);
        Assert.assertEquals(MathUtils.safeSubstract(Integer.MIN_VALUE, 1), Integer.MIN_VALUE);
        Assert.assertEquals(MathUtils.safeSubstract(Integer.MAX_VALUE, -1), Integer.MAX_VALUE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSafeMultiply() {
        Assert.assertEquals(MathUtils.safeMultiply(1, 1), 1);
        Assert.assertEquals(MathUtils.safeMultiply(Integer.MAX_VALUE, 10), Integer.MAX_VALUE);
        Assert.assertEquals(MathUtils.safeSubstract(Integer.MIN_VALUE, 10), Integer.MIN_VALUE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMinMax() {
        int[] vls = new int[]{0,-5,12,-3,0,4,-1};
        Assert.assertEquals(-5,MathUtils.min(vls));
        Assert.assertEquals(12,MathUtils.max(vls));
        int[][] vls2 = new int[][]{{0,-5},{12,-3,0,4,-1}};
        Assert.assertEquals(MathUtils.min(vls2),MathUtils.min(vls));
        Assert.assertEquals(MathUtils.max(vls2),MathUtils.max(vls));
        Assert.assertEquals(MathUtils.min(ArrayUtils.flatten(vls2)),MathUtils.min(vls));
        Assert.assertEquals(MathUtils.max(ArrayUtils.flatten(vls2)),MathUtils.max(vls));
    }

}