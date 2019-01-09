/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/09/2017.
 */
public class EnvironmentTest {

    @DataProvider(name = "env")
    private Object[][] env() {
        return new IEnvironment[][]{
                {new EnvironmentBuilder().fromFlat().build()},
                {new EnvironmentBuilder().fromChunk().build()}
        };
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "env")
    public void testBool(IEnvironment environment) {
        IStateBool prim = environment.makeBool(true);
        Assert.assertEquals(prim.get(), true);
        prim.set(false);
        Assert.assertEquals(prim.get(), false);
        environment.worldPush();
        prim.set(true);
        Assert.assertEquals(prim.get(), true);
        environment.worldPop();
        Assert.assertEquals(prim.get(), false);
        Assert.assertEquals(prim.toString(), "false");
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "env")
    public void testInt(IEnvironment environment) {
        IStateInt prim = environment.makeInt(0);
        Assert.assertEquals(prim.get(), 0);
        prim.set(10);
        Assert.assertEquals(prim.get(), 10);
        environment.worldPush();
        prim.set(20);
        Assert.assertEquals(prim.get(), 20);
        environment.worldPop();
        Assert.assertEquals(prim.get(), 10);
        Assert.assertEquals(prim.toString(), "10");
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "env")
    public void testLong(IEnvironment environment) {
        IStateLong prim = environment.makeLong(0L);
        Assert.assertEquals(prim.get(), 0L);
        prim.set(10L);
        Assert.assertEquals(prim.get(), 10L);
        environment.worldPush();
        prim.set(20L);
        Assert.assertEquals(prim.get(), 20L);
        environment.worldPop();
        Assert.assertEquals(prim.get(), 10L);
        Assert.assertEquals(prim.toString(), "10");
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "env")
    public void testFloat(IEnvironment environment) {
        IStateDouble prim = environment.makeFloat(0.2d);
        Assert.assertEquals(prim.get(), 0.2d);
        prim.set(10.2d);
        Assert.assertEquals(prim.get(), 10.2d);
        environment.worldPush();
        prim.set(20.2d);
        Assert.assertEquals(prim.get(), 20.2d);
        environment.worldPop();
        Assert.assertEquals(prim.get(), 10.2d);
        Assert.assertEquals(prim.toString(), "10.2");
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "env")
    public void testBitset1(IEnvironment environment) {
        testBitset(environment, 16, 48, 8, 9, 10);
        testBitset(environment, 48, 72, 28, 29, 30);
        testBitset(environment, 356, 512, 218, 219, 220);
    }

    private void testBitset(IEnvironment environment, int size, int max, int i8, int i9, int i10) {
        IStateBitSet prim = environment.makeBitSet(size);
        prim.set(i8);
        Assert.assertEquals(prim.get(i8), true);
        Assert.assertEquals(prim.get(i9), false);
        Assert.assertEquals(prim.get(i10), false);
        prim.clear(i8, i10+1);
        prim.set(i9, true);
        Assert.assertEquals(prim.get(i8), false);
        Assert.assertEquals(prim.get(i9), true);
        Assert.assertEquals(prim.get(i10), false);
        environment.worldPush();
        prim.set(i8, i10+1);
        prim.set(i9, false);
        Assert.assertEquals(prim.get(i8), true);
        Assert.assertEquals(prim.get(i9), false);
        Assert.assertEquals(prim.get(i10), true);
        environment.worldPop();
        Assert.assertEquals(prim.get(i8), false);
        Assert.assertEquals(prim.get(i9), true);
        Assert.assertEquals(prim.get(i10), false);

        Assert.assertEquals(prim.nextSetBit(-1), i9);
        Assert.assertEquals(prim.nextSetBit(max), -1);
        Assert.assertEquals(prim.nextSetBit(0), i9);
        Assert.assertEquals(prim.nextSetBit(i9), i9);
        Assert.assertEquals(prim.nextSetBit(i10), -1);

        Assert.assertEquals(prim.nextClearBit(-1), 0);
        Assert.assertEquals(prim.nextClearBit(max), max);
        Assert.assertEquals(prim.nextClearBit(0), 0);
        Assert.assertEquals(prim.nextClearBit(i9), i10);
        Assert.assertEquals(prim.nextClearBit(i10), i10);

        Assert.assertEquals(prim.prevSetBit(i9), i9);
        Assert.assertEquals(prim.prevSetBit(size - 1), i9);
        Assert.assertEquals(prim.prevSetBit(i8), -1);
        Assert.assertEquals(prim.prevClearBit(i9), i9-1);
        Assert.assertEquals(prim.prevClearBit(size - 1), size - 1);
        Assert.assertEquals(prim.prevClearBit(i8), i8);

        Assert.assertEquals(prim.toString(), "{" + i9 + "}");
        Assert.assertEquals(prim.cardinality(), 1);
        Assert.assertEquals(prim.isEmpty(), false);

        prim.clear();

        Assert.assertEquals(prim.toString(), "{}");
        Assert.assertEquals(prim.cardinality(), 0);
        Assert.assertEquals(prim.isEmpty(), true);
        try{
            prim.set(-1);
            Assert.fail();
        }catch (IndexOutOfBoundsException e){}
    }

}

