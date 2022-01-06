/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 14/03/2016.
 */
public class DecisionPathTest {

    Model m;

    DecisionPath dp;

    IntVar[] vars;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        m = new Model();
        dp = m.getSolver().getDecisionPath();
        vars = m.intVarArray(4, 1, 10);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testPushDecision() throws Exception {
        Assert.assertEquals(dp.size(), 1);
        Assert.assertEquals(dp.last.get(), 1);
        IntDecision d1 = dp.makeIntDecision(vars[0], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d1);
        Assert.assertEquals(d1.getPosition(), 1);
        Assert.assertEquals(dp.size(), 2);
        Assert.assertEquals(dp.last.get(), 1);

        IntDecision d2 = dp.makeIntDecision(vars[1], DecisionOperatorFactory.makeIntEq(), 4);
        Assert.assertEquals(d2.getArity(), 2);
        dp.pushDecision(d2);
        Assert.assertEquals(d2.getPosition(), 1);
        Assert.assertEquals(dp.size(), 2);
        Assert.assertEquals(dp.last.get(), 1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testApply() throws Exception {
        m.getEnvironment().worldPush();
        dp.buildNext();
        dp.apply();
        IntDecision d1 = dp.makeIntDecision(vars[0], DecisionOperatorFactory.makeIntEq(), 4);
        d1.setRefutable(true);

        Assert.assertTrue(d1.hasNext());
        Assert.assertEquals(d1.getPosition(), 0);
        dp.pushDecision(d1);
        Assert.assertEquals(dp.size(), 2);
        Assert.assertEquals(dp.last.get(), 1);
        m.getEnvironment().worldPush();
        dp.buildNext();
        dp.apply();
        Assert.assertTrue(d1.hasNext());
        Assert.assertEquals(dp.size(), 2);
        Assert.assertEquals(dp.last.get(), 2);

        IntDecision d2 = dp.makeIntDecision(vars[1], DecisionOperatorFactory.makeIntEq(), 4);
        d1.setRefutable(true);
        IntDecision d3 = dp.makeIntDecision(vars[2], DecisionOperatorFactory.makeIntEq(), 4);
        d1.setRefutable(true);
        dp.pushDecision(d2);
        Assert.assertTrue(d2.hasNext());
        Assert.assertEquals(dp.size(), 3);
        Assert.assertEquals(dp.last.get(), 2);
        dp.pushDecision(d3);
        Assert.assertTrue(d3.hasNext());
        Assert.assertEquals(dp.size(), 3);
        Assert.assertEquals(dp.last.get(), 2);

        m.getEnvironment().worldPush();
        dp.buildNext();
        dp.apply();
        Assert.assertTrue(d3.hasNext());
        Assert.assertTrue(d2.hasNext());
        Assert.assertEquals(dp.size(), 3);
        Assert.assertEquals(dp.last.get(), 3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveLast() throws Exception {
        m.getEnvironment().worldPush();
        dp.buildNext();
        dp.apply();
        IntDecision d1 = dp.makeIntDecision(vars[0], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d1);
        m.getEnvironment().worldPush();
        dp.buildNext();
        dp.apply();
        IntDecision d2 = dp.makeIntDecision(vars[1], DecisionOperatorFactory.makeIntEq(), 4);
        IntDecision d3 = dp.makeIntDecision(vars[2], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d2);
        dp.pushDecision(d3);
        m.getEnvironment().worldPush();
        dp.buildNext();
        dp.apply();
        Assert.assertEquals(dp.size(), 3);
        Assert.assertEquals(dp.last.get(), 3);

        m.getEnvironment().worldPop();
        dp.synchronize();
        Assert.assertEquals(dp.size(), 2);
        Assert.assertEquals(dp.last.get(), 2);

        m.getEnvironment().worldPop();
        dp.synchronize();
        Assert.assertEquals(dp.size(), 1);
        Assert.assertEquals(dp.last.get(), 1);

        m.getEnvironment().worldPop();
        dp.synchronize();
        Assert.assertEquals(dp.size(), 1);
        Assert.assertEquals(dp.last.get(), 1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testGetLastDecision() throws Exception {

        Assert.assertEquals(dp.getLastDecision(), RootDecision.ROOT);

        IntDecision d1 = dp.makeIntDecision(vars[0], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d1);
        Assert.assertEquals(dp.getLastDecision(), d1);

        IntDecision d2 = dp.makeIntDecision(vars[1], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d2);
        Assert.assertEquals(dp.getLastDecision(), d2);

        IntDecision d3 = dp.makeIntDecision(vars[2], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d3);
        Assert.assertEquals(dp.getLastDecision(), d3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSize() throws Exception {
        IntDecision d1 = dp.makeIntDecision(vars[0], DecisionOperatorFactory.makeIntEq(), 4);
        IntDecision d2 = dp.makeIntDecision(vars[1], DecisionOperatorFactory.makeIntEq(), 4);
        IntDecision d3 = dp.makeIntDecision(vars[2], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d1);
        dp.pushDecision(d2);
        dp.pushDecision(d3);
        Assert.assertEquals(dp.size(), 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testGetDecision1() throws Exception {
        IntDecision d1 = dp.makeIntDecision(vars[0], DecisionOperatorFactory.makeIntEq(), 4);
        IntDecision d2 = dp.makeIntDecision(vars[1], DecisionOperatorFactory.makeIntEq(), 4);
        IntDecision d3 = dp.makeIntDecision(vars[2], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d1);
        dp.pushDecision(d2);
        dp.pushDecision(d3);
        Assert.assertEquals(dp.getDecision(1), d3);
        Assert.assertEquals(dp.last.get(), 1);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetDecision2() throws Exception {
        dp.getDecision(4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetDecision3() throws Exception {
        dp.getDecision(-1);
    }


    @Test(groups = "1s", timeOut=60000)
    public void testTransferInto() throws Exception {
        IntDecision d1 = dp.makeIntDecision(vars[0], DecisionOperatorFactory.makeIntEq(), 4);
        IntDecision d2 = dp.makeIntDecision(vars[1], DecisionOperatorFactory.makeIntEq(), 4);
        IntDecision d3 = dp.makeIntDecision(vars[2], DecisionOperatorFactory.makeIntEq(), 4);
        dp.pushDecision(d1);
        dp.pushDecision(d2);
        dp.pushDecision(d3);
        List<Decision> decisions = new ArrayList<>();
        dp.transferInto(decisions, false);
        Assert.assertEquals(decisions.size(), 1);
        Assert.assertEquals(decisions.get(0), d3);
    }
}
