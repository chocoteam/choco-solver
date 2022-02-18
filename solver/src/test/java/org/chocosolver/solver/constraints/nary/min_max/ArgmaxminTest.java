/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.FullyRandom;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/06/2021
 */
public class ArgmaxminTest {

    @DataProvider
    private Object[][] decOrNot() {
        List<Object[]> args = new ArrayList<>();
        for (int seed = 0; seed < 200; seed++) {
            args.add(new Object[]{true, seed});
            args.add(new Object[]{false, seed});
        }
        return args.toArray(new Object[0][0]);
    }

    @DataProvider
    private Object[][] seeds() {
        List<Object[]> args = new ArrayList<>();
        for (int seed = 0; seed < 100; seed++) {
            args.add(new Object[]{seed});
        }
        return args.toArray(new Object[0][0]);
    }

    @Test(groups = "1s", dataProvider = "seeds")
    public void testResultMin(long seed) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        x[0] = model.intVar("x1", 1, 3);
        x[1] = model.intVar("x2", 2, 10);
        x[2] = model.intVar("x3", 3, 5);
        x[3] = model.intVar("x4", 4, 6);
        IntVar z = model.argmin("z", x);
        IntVar min = model.min("min", x);
        model.member(z, new int[]{0, 2, 3}).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(ArrayUtils.append(x, x, new IntVar[]{z}), seed));
        while (solver.solve()){
            for(int i=0;i<x.length;i++){
                Assert.assertFalse(i!=z.getValue() && x[i].getValue()<min.getValue());
            }
        }
        Assert.assertEquals(solver.getSolutionCount(), 234);
        solver.printShortStatistics();
    }

    @Test(groups = "1s", dataProvider = "seeds")
    public void testResultMax(long seed) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        x[0] = model.intVar("x1", 1, 3);
        x[1] = model.intVar("x2", 2, 10);
        x[2] = model.intVar("x3", 3, 5);
        x[3] = model.intVar("x4", 4, 6);
        IntVar z = model.argmax("z", x);
        IntVar max = model.max("max", x);
        model.member(z, new int[]{0, 2, 3}).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(ArrayUtils.append(x, x, new IntVar[]{z}), seed));
        while (solver.solve()){
            for(int i=0;i<x.length;i++){
                Assert.assertFalse(i!=z.getValue() && x[i].getValue()>max.getValue());
            }
        }
        Assert.assertEquals(solver.getSolutionCount(), 84);
        solver.printShortStatistics();
    }

    @Test(groups = "1s", dataProvider = "decOrNot")
    public void test1(boolean dec, long seed) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{0, 2, 3});
        x[0] = model.intVar("x1", 1, 3);
        x[1] = model.intVar("x2", 2, 10);
        x[2] = model.intVar("x3", 3, 5);
        x[3] = model.intVar("x4", 4, 6);
        if (dec) {
            model.argmaxDec(z, 0, x);
        } else {
            model.argmax(z, 0, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(ArrayUtils.append(x, x, new IntVar[]{z}), seed));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 84);
        solver.printShortStatistics();
    }

    @Test(groups = "1s", dataProvider = "decOrNot")
    public void test1o(boolean dec, long seed) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{1, 3, 4});
        x[0] = model.intVar("x1", 1, 3);
        x[1] = model.intVar("x2", 2, 10);
        x[2] = model.intVar("x3", 3, 5);
        x[3] = model.intVar("x4", 4, 6);
        if (dec) {
            model.argmaxDec(z, 1, x);
        } else {
            model.argmax(z, 1, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(ArrayUtils.append(x, x, new IntVar[]{z}), seed));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 24);
        solver.printShortStatistics();
    }

    @Test(groups = "1s", dataProvider = "seeds")
    public void test11(int seed) {
        int n = 20;
        Model model = new Model(Settings.init());
        IntVar[] x = model.intVarArray("x", n, 0, n - 1);
        int[][] pi = new int[n][n];
        Random rnd = new Random(0);
        for (int i = 0; i < n; i++) {
            pi[i] = rnd.ints(n, 1, n + 1).toArray();
        }
        model.allDifferent(x).post();
        for (int i = 0; i < n; i++) {
            IntVar[] xx = new IntVar[n];
            for (int j = 0; j < n; j++) {
                xx[j] = model.intScaleView(x[j], pi[i][j]);
            }
            model.argmax(x[i], 0, xx).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(new FullyRandom(x, seed));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 0);
    }

    @Test(groups = "1s", dataProvider = "decOrNot")
    public void test2(boolean dec, long seed) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{0, 2, 3});
        x[0] = model.intVar("x1", 1, 3);
        x[1] = model.intVar("x2", 2, 10);
        x[2] = model.intVar("x3", 3, 5);
        x[3] = model.intVar("x4", 4, 6);
        if (dec) {
            model.argminDec(z, 0, x);
        } else {
            model.argmin(z, 0, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(ArrayUtils.append(x, x, new IntVar[]{z}), seed));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 234);
        solver.printShortStatistics();
    }

    @Test(groups = "1s", dataProvider = "decOrNot")
    public void test2o(boolean dec, long seed) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{1, 3, 4});
        x[0] = model.intVar("x1", 1, 3);
        x[1] = model.intVar("x2", 2, 10);
        x[2] = model.intVar("x3", 3, 5);
        x[3] = model.intVar("x4", 4, 6);
        if (dec) {
            model.argminDec(z, 1, x);
        } else {
            model.argmin(z, 1, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(ArrayUtils.append(x, x, new IntVar[]{z}), seed));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 234);
        solver.printShortStatistics();
    }

    @Test(groups = "1s", dataProvider = "seeds")
    public void test21(int seed) {
        int n = 20;
        Model model = new Model(Settings.init());
        IntVar[] x = model.intVarArray("x", n, 0, n - 1);
        int[][] pi = new int[n][n];
        Random rnd = new Random(0);
        for (int i = 0; i < n; i++) {
            pi[i] = rnd.ints(n, 1, n + 1).toArray();
        }
        model.allDifferent(x).post();
        for (int i = 0; i < n; i++) {
            IntVar[] xx = new IntVar[n];
            for (int j = 0; j < n; j++) {
                xx[j] = model.intScaleView(x[j], pi[i][j]);
            }
            model.argmin(x[i], 0, xx).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(new FullyRandom(x, seed));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 0);
    }


}
