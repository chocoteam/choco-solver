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

import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PopTest {

    @DataProvider(name = "env")
    public Object[][] getEnvs(){
        return new EnvironmentTrailing[][]{
                {new EnvironmentBuilder().fromFlat().build()},
                {new EnvironmentBuilder().fromChunk().build()}
        };
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "env")
    public void testEnvTPop(EnvironmentTrailing env) {
        try {
            env.worldPop();
            Assert.fail("poping above 0 is forbidden");
        }catch (AssertionError e){}
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "env")
    public void testEnvPushPop(EnvironmentTrailing env) {
        env.worldPush();
        env.worldPop();
        env = new EnvironmentTrailing();
        env.worldPush();
        env.worldPop();
    }
}
