/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;

import org.chocosolver.memory.structure.BasicIndexedBipartiteSet;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/04/2014
 */
public class BasicIndexBipartiteSetTest {

    @DataProvider(name = "env")
    public Object[][] getEnvs(){
        return new EnvironmentTrailing[][]{
                {new EnvironmentBuilder().fromFlat().build()},
                {new EnvironmentBuilder().fromChunk().build()}
        };
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "env")
    public void testBasicIndexedBipartiteSet(EnvironmentTrailing env){
        BasicIndexedBipartiteSet set = new BasicIndexedBipartiteSet(env, 2);

        int b1 = set.add();
        int b2 = set.add();
        int b3 = set.add();
        int b4 = set.add();

        set.swap(b2);

        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertTrue(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));

        env.worldPush();
        set.swap(b3); // b3 is now fixed
        int b5 = set.add();

        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertFalse(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));

        env.worldPush();

        set.swap(b1); // b1 is now fixed

        int b6 = set.add();
        set.swap(b6);

        Assert.assertFalse(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertFalse(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));
        Assert.assertFalse(set.bundle(b6));

        // go back
        env.worldPop();
        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertFalse(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));
        Assert.assertTrue(set.bundle(b6));

        env.worldPop();
        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertTrue(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));
        Assert.assertTrue(set.bundle(b6));
    }

}
