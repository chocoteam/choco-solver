/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.SettingsBuilder;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Test(groups = "1s", timeOut = 60000)
    public void testIntDecisionOnValueOutOfDomain() {
        // #1210: "=" and "!=" decisions are made on the closest value of the domain
        Model model = new Model();
        IntVar x = model.intVar("x", new int[]{1, 4, 5});
        DecisionPath path = model.getSolver().getDecisionPath();
        Assert.assertEquals(path.makeIntDecision(x, DecisionOperatorFactory.makeIntEq(), 3).getDecisionValue(), 4);
        Assert.assertEquals(path.makeIntDecision(x, DecisionOperatorFactory.makeIntNeq(), 2).getDecisionValue(), 1);
        Assert.assertEquals(path.makeIntDecision(x, DecisionOperatorFactory.makeIntEq(), -2).getDecisionValue(), 1);
        Assert.assertEquals(path.makeIntDecision(x, DecisionOperatorFactory.makeIntEq(), 9).getDecisionValue(), 5);
        Assert.assertEquals(path.makeIntDecision(x, DecisionOperatorFactory.makeIntEq(), 5).getDecisionValue(), 5);
        // a split does not need a value of the domain
        Assert.assertEquals(path.makeIntDecision(x, DecisionOperatorFactory.makeIntSplit(), 3).getDecisionValue(), 3);
        // tie: the smallest value
        IntVar y = model.intVar("y", new int[]{1, 5});
        Assert.assertEquals(path.makeIntDecision(y, DecisionOperatorFactory.makeIntEq(), 3).getDecisionValue(), 1);
    }

    @DataProvider
    public Object[][] lcgAndOperators() {
        List<Object[]> params = new ArrayList<>();
        for (boolean lcg : new boolean[]{false, true}) {
            params.add(new Object[]{lcg, DecisionOperatorFactory.makeIntEq()});
            params.add(new Object[]{lcg, DecisionOperatorFactory.makeIntNeq()});
            params.add(new Object[]{lcg, DecisionOperatorFactory.makeIntSplit()});
        }
        return params.toArray(new Object[0][]);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "lcgAndOperators")
    public void testMiddleValueOutOfDomain(boolean lcg, DecisionOperator<IntVar> op) {
        // #1210: IntDomainMiddle returns the middle of the bounds, which may not be in the domain;
        // with "=" or "!=", the search used to loop, or to wrongly report no solution with LCG
        Model model = new Model(SettingsBuilder.init().setLCG(lcg));
        IntVar x = model.intVar("x", 0, 4);
        IntVar y = model.intVar("y", 0, 6);
        model.arithm(x, "!=", 2).post();
        model.arithm(y, "!=", 3).post();
        model.arithm(y, "!=", 2).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(new InputOrder<>(model), new IntDomainMiddle(true), op, x, y));
        Set<String> found = new HashSet<>();
        int count = 0;
        while (solver.solve()) {
            found.add(x.getValue() + "," + y.getValue());
            count++;
        }
        // brute force: x in [0,4] \ {2}, y in [0,6] \ {2,3}
        Assert.assertEquals(count, 4 * 5);
        Assert.assertEquals(found.size(), 4 * 5);
    }
}
