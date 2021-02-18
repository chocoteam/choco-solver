/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.bandit;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29/06/2020
 */
public class PolicyTest {

    @Test(groups = "1s")
    public void testUCB1() {
        UCB1 ucb1 = new UCB1(3);
        ucb1.init();
        int t = 0;
        Assert.assertEquals(ucb1.nextAction(t++), 0);
        ucb1.update(0, 20);
        Assert.assertEquals(ucb1.nextAction(t++), 1);
        ucb1.update(1, 30);
        Assert.assertEquals(ucb1.nextAction(t++), 2);
        ucb1.update(2, 10);
        int a = ucb1.nextAction(t++);
        Assert.assertEquals(a, 1);
        ucb1.update(1, 5);
        a = ucb1.nextAction(t++);
        Assert.assertEquals(a, 1);
        ucb1.update(1, 5);
        a = ucb1.nextAction(t);
        Assert.assertEquals(a, 0);
        ucb1.update(0, 25);
    }

    @Test(groups = "1s")
    public void testMOSS() {
        MOSS moss = new MOSS(3);
        moss.init();
        int t = 0;
        Assert.assertEquals(moss.nextAction(t++), 0);
        moss.update(0, 20);
        Assert.assertEquals(moss.nextAction(t++), 1);
        moss.update(1, 30);
        Assert.assertEquals(moss.nextAction(t++), 2);
        moss.update(2, 10);
        int a = moss.nextAction(t++);
        Assert.assertEquals(a, 1);
        moss.update(1, 5);
        a = moss.nextAction(t++);
        Assert.assertEquals(a, 1);
        moss.update(1, 5);
        a = moss.nextAction(t);
        Assert.assertEquals(a, 0);
        moss.update(0, 25);
    }

}