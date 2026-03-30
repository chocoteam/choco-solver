/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.solver.Providers;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class IReasonManagerTest {

    @Test(groups = "1s", dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"2", "16", "1"})
    public void testGather(int shift) {
        System.setProperty("reason.manager.chunk.shift", Integer.toString(shift));
        IEnvironment env = new EnvironmentTrailing();
        IReasonManager manager = IReasonManager.makeManager(env, 2);
        Reason r = manager.r(0,1,2,3,4);
        assertEquals(r.getConflict().size(), 5);
        for(int i = 0; i < 5; i++){
            assertEquals(r.getConflict()._g(i), i);
        }
        env.worldPush();
        Reason r2 = manager.gather(r, 5);
        assertEquals(r2.getConflict().size(), 6);
        for(int i = 0; i < 6; i++){
            assertEquals(r2.getConflict()._g(i), i);
        }
        env.worldPop();
        env.worldPush();
        Reason r3 = manager.r(0,7,8);
        Reason r4 = manager.gather(r3, 9);
        assertEquals(r4.getConflict().size(), 4);
        for(int i = 1; i < 3; i++){
            assertEquals(r4.getConflict()._g(i), i + 6);
        }
    }


    @Test(groups = "1s", dataProvider = "random", dataProviderClass = Providers.class)
    @Providers.Arguments(values = {"2", "16", "1"})
    public void testGather2(int shift) {
        System.setProperty("reason.manager.chunk.shift", Integer.toString(shift));
        IEnvironment env = new EnvironmentTrailing();
        IReasonManager manager = IReasonManager.makeManager(env, 2);
        manager.r(0,0,62);
        manager.r(0,0,305,371);
        Reason r = manager.r(0,0,305);
        manager.gather(r, 33);
        Reason r2 = manager.gather(r, 153);
        manager.r(0,0,305,371);
        Assert.assertEquals(r2.getConflict()._g(3), 153);

    }
}