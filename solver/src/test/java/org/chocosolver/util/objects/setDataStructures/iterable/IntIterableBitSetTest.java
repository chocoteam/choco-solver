/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.iterable;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 10/12/2018.
 */
public class IntIterableBitSetTest {

    @Test(groups="1s", timeOut=60000)
    public void testNextValue() {
        IntIterableBitSet is = new IntIterableBitSet();
        is.addAll(1,2,3,5,7,8,9,10);

        Assert.assertEquals(is.nextValue(-10), 1);
        Assert.assertEquals(is.nextValue(0), 1);
        Assert.assertEquals(is.nextValue(1), 2);
        Assert.assertEquals(is.nextValue(2), 3);
        Assert.assertEquals(is.nextValue(3), 5);
        Assert.assertEquals(is.nextValue(4), 5);
        Assert.assertEquals(is.nextValue(5), 7);
        Assert.assertEquals(is.nextValue(6), 7);
        Assert.assertEquals(is.nextValue(7), 8);
        Assert.assertEquals(is.nextValue(8), 9);
        Assert.assertEquals(is.nextValue(9), 10);
        Assert.assertEquals(is.nextValue(10), Integer.MAX_VALUE);
        Assert.assertEquals(is.nextValue(15), Integer.MAX_VALUE);

    }

    @Test(groups="1s", timeOut=60000)
    public void testNextValueOut() {
        IntIterableBitSet is = new IntIterableBitSet();
        is.addAll(1,2,3,5,7,8,9,10);

        Assert.assertEquals(is.nextValueOut(-10), -9);
        Assert.assertEquals(is.nextValueOut(-1), 0);
        Assert.assertEquals(is.nextValueOut(0), 4);
        Assert.assertEquals(is.nextValueOut(1), 4);
        Assert.assertEquals(is.nextValueOut(2), 4);
        Assert.assertEquals(is.nextValueOut(3), 4);
        Assert.assertEquals(is.nextValueOut(4), 6);
        Assert.assertEquals(is.nextValueOut(5), 6);
        Assert.assertEquals(is.nextValueOut(6), 11);
        Assert.assertEquals(is.nextValueOut(7), 11);
        Assert.assertEquals(is.nextValueOut(8), 11);
        Assert.assertEquals(is.nextValueOut(9), 11);
        Assert.assertEquals(is.nextValueOut(10), 11);
        Assert.assertEquals(is.nextValueOut(15), 16);

    }

    @Test(groups="1s", timeOut=60000)
    public void testPrevValue() {
        IntIterableBitSet is = new IntIterableBitSet();
        is.addAll(1,2,3,5,7,8,9,10);

        Assert.assertEquals(is.previousValue(15), 10);
        Assert.assertEquals(is.previousValue(11), 10);
        Assert.assertEquals(is.previousValue(10), 9);
        Assert.assertEquals(is.previousValue(9), 8);
        Assert.assertEquals(is.previousValue(8), 7);
        Assert.assertEquals(is.previousValue(7), 5);
        Assert.assertEquals(is.previousValue(6), 5);
        Assert.assertEquals(is.previousValue(5), 3);
        Assert.assertEquals(is.previousValue(4), 3);
        Assert.assertEquals(is.previousValue(3), 2);
        Assert.assertEquals(is.previousValue(2), 1);
        Assert.assertEquals(is.previousValue(1), Integer.MIN_VALUE);
        Assert.assertEquals(is.previousValue(0), Integer.MIN_VALUE);
        Assert.assertEquals(is.previousValue(-1), Integer.MIN_VALUE);

    }

    @Test(groups="1s", timeOut=60000)
    public void testPrevValueOut() {
        IntIterableBitSet is = new IntIterableBitSet();
        is.addAll(1,2,3,5,7,8,9,10);

        Assert.assertEquals(is.previousValueOut(15), 14);
        Assert.assertEquals(is.previousValueOut(11), 6);
        Assert.assertEquals(is.previousValueOut(10), 6);
        Assert.assertEquals(is.previousValueOut(9), 6);
        Assert.assertEquals(is.previousValueOut(8), 6);
        Assert.assertEquals(is.previousValueOut(7), 6);
        Assert.assertEquals(is.previousValueOut(6), 4);
        Assert.assertEquals(is.previousValueOut(5), 4);
        Assert.assertEquals(is.previousValueOut(4), 0);
        Assert.assertEquals(is.previousValueOut(3), 0);
        Assert.assertEquals(is.previousValueOut(2), 0);
        Assert.assertEquals(is.previousValueOut(1), 0);
        Assert.assertEquals(is.previousValueOut(0), -1);
        Assert.assertEquals(is.previousValueOut(-1), -2);

    }

}