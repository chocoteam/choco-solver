/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;

import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
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
    public Object[][] env() {
        return new IEnvironment[][]{
            {new EnvironmentBuilder().fromFlat().build()},
            {new EnvironmentBuilder().fromChunk().build()}
        };
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "env")
    public void testBool(IEnvironment environment) {
        IStateBool prim = environment.makeBool(true);
        Assert.assertTrue(prim.get());
        prim.set(false);
        Assert.assertFalse(prim.get());
        environment.worldPush();
        prim.set(true);
        Assert.assertTrue(prim.get());
        environment.worldPop();
        Assert.assertFalse(prim.get());
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
        Assert.assertTrue(prim.get(i8));
        Assert.assertFalse(prim.get(i9));
        Assert.assertFalse(prim.get(i10));
        prim.clear(i8, i10+1);
        prim.set(i9, true);
        Assert.assertFalse(prim.get(i8));
        Assert.assertTrue(prim.get(i9));
        Assert.assertFalse(prim.get(i10));
        environment.worldPush();
        prim.set(i8, i10+1);
        prim.set(i9, false);
        Assert.assertTrue(prim.get(i8));
        Assert.assertFalse(prim.get(i9));
        Assert.assertTrue(prim.get(i10));
        environment.worldPop();
        Assert.assertFalse(prim.get(i8));
        Assert.assertTrue(prim.get(i9));
        Assert.assertFalse(prim.get(i10));

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
        Assert.assertFalse(prim.isEmpty());

        prim.clear();

        Assert.assertEquals(prim.toString(), "{}");
        Assert.assertEquals(prim.cardinality(), 0);
        Assert.assertTrue(prim.isEmpty());
        try{
            prim.set(-1);
            Assert.fail();
        } catch (IndexOutOfBoundsException ignored) { }
    }

    @Test(groups = "1s")
    public void testConor1() {
        int N = 10;
        // 1. Modeling part
        Model model = new Model("all-interval series of size " + N);
        // 1.a declare the variables
        IntVar[] S = model.intVarArray("s", N, 0, N - 1, false);
        IntVar[] V = model.intVarArray("V", N - 1, 1, N - 1, false);
        // 1.b post the constraints
        for (int i = 0; i < N - 1; i++) {
            model.distance(S[i + 1], S[i], "=", V[i]).post();
        }
        model.allDifferent(S).post();
        model.allDifferent(V).post();
        S[1].gt(S[0]).post();
        V[1].gt(V[N - 2]).post();

        // 2. Solving part
        Solver solver = model.getSolver();
        // 2.a define a search strategy
        solver.setSearch(Search.minDomLBSearch(S));
        solver.solve();
        model.getEnvironment().worldCommit();
    }

    @Test(groups = "1s")
    public void testConor2() {
        int N = 3;
        // 1. Modeling part
        Model model = new Model("all-interval series of size " + N);
        // 1.a declare the variables
        IntVar[] S = model.intVarArray("s", N, 0, N - 1, false);
        model.allDifferent(S, "NEQS").post();
        // 2. Solving part
        Solver solver = model.getSolver();
        // 2.a define a search strategy
        solver.setSearch(Search.minDomLBSearch(S));
        solver.showDecisions();
        solver.solve();
        model.getEnvironment().worldCommit();
        while(model.getEnvironment().getWorldIndex()>0){
            model.getEnvironment().worldPop();
        }
    }

    @Test(groups = "1s")
    public void testConor3() {
        EnvironmentTrailing env = new EnvironmentTrailing();
        IStateInt snt = env.makeInt(0);
        env.worldPush();
        snt.set(2);
        env.worldPush();
        snt.set(4);
        env.worldPush();
        snt.set(6);
        Assert.assertEquals(snt.get(), 6);
        env.worldCommit();
        Assert.assertEquals(snt.get(), 6);
        env.worldPop();
        Assert.assertEquals(snt.get(), 2);
        env.worldPop();
        Assert.assertEquals(snt.get(), 0);
    }

    @Test(groups = "1s")
    public void testConor4() {
        EnvironmentTrailing env = new EnvironmentTrailing();
        IStateLong snt = env.makeLong(0L);
        env.worldPush();
        snt.set(2L);
        env.worldPush();
        snt.set(4L);
        env.worldPush();
        snt.set(6L);
        Assert.assertEquals(snt.get(), 6L);
        env.worldCommit();
        Assert.assertEquals(snt.get(), 6L);
        env.worldPop();
        Assert.assertEquals(snt.get(), 2L);
        env.worldPop();
        Assert.assertEquals(snt.get(), 0L);
    }

    @Test(groups = "1s")
    public void testConor5() {
        EnvironmentTrailing env = new EnvironmentTrailing();
        IStateBool snt = env.makeBool(true);
        env.worldPush();
        snt.set(false);
        env.worldPush();
        snt.set(true);
        env.worldPush();
        snt.set(false);
        Assert.assertFalse(snt.get());
        env.worldCommit();
        Assert.assertFalse(snt.get());
        env.worldPop();
        Assert.assertFalse(snt.get());
        env.worldPop();
        Assert.assertTrue(snt.get());
    }

    @Test(groups = "1s")
    public void testConor6() {
        EnvironmentTrailing env = new EnvironmentTrailing();
        final int[] val = {0};
        env.worldPush();
        val[0] = 2;
        env.save(() -> val[0] = 0);
        env.worldPush();
        val[0] = 4;
        env.save(() -> val[0] = 2);
        env.worldPush();
        val[0] = 6;
        env.save(() -> val[0] = 4);
        Assert.assertEquals(val[0], 6);
        env.worldCommit();
        Assert.assertEquals(val[0], 6);
        env.worldPop();
        Assert.assertEquals(val[0], 2);
        env.worldPop();
        Assert.assertEquals(val[0], 0);
    }

    @Test(groups = "1s")
    public void testWorldCommit() {
        Model model = new Model();

        IntVar a = model.intVar(0, 10);
        IntVar b = model.intVar(0, 10);
        a.lt(b).post();
        model.getEnvironment().worldPush();


        IntVar c = model.intVar(0, 10);
        c.lt(a).post();
        try {
            model.getSolver().propagate();
        } catch (Exception e) {
        }
        model.getEnvironment().worldPush();

        b.lt(9).post();
        try {
            model.getSolver().propagate();
        } catch (Exception e) {
        }
        model.getEnvironment().worldPush();

        // Commit thrice - all the way back
        model.getEnvironment().worldCommit();
        model.getEnvironment().worldCommit();
        model.getEnvironment().worldCommit();

        try {
            c.instantiateTo(2, Cause.Null);
            model.getSolver().propagate();
            assert b.getLB() == 4 : b;
            assert b.getUB() == 8 : b;
        } catch (Exception e) {
            assert false;
        }
    }
}