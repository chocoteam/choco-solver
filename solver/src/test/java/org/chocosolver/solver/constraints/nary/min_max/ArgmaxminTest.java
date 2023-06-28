/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.min_max;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Providers;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Occurrence;
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
    private Object[][] decAndOffset() {
        List<Object[]> args = new ArrayList<>();
        for (int offset = -3; offset < 4; offset++) {
            args.add(new Object[]{true, offset});
            args.add(new Object[]{false, offset});
        }
        return args.toArray(new Object[0][0]);
    }

    @DataProvider
    private Object[][] decAndSeed() {
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
        while (solver.solve()) {
            for (int i = 0; i < x.length; i++) {
                Assert.assertFalse(i != z.getValue() && x[i].getValue() < min.getValue());
            }
        }
        Assert.assertEquals(solver.getSolutionCount(), 234);
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
        while (solver.solve()) {
            for (int i = 0; i < x.length; i++) {
                Assert.assertFalse(i != z.getValue() && x[i].getValue() > max.getValue());
            }
        }
        Assert.assertEquals(solver.getSolutionCount(), 84);
    }

    @Test(groups = "1s", dataProvider = "decAndSeed")
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
    }

    @Test(groups = "1s", dataProvider = "decAndSeed")
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
        Assert.assertEquals(solver.getSolutionCount(), 84);
    }

    @Test(groups = "1s", dataProvider = "decAndSeed")
    public void test1o2(boolean dec, long seed) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[5];
        IntVar z = model.intVar("z", 1, 5);
        x[0] = model.intVar("X_INTRODUCED_143_", new int[]{14, 19, 25});
        x[1] = model.intVar("X_INTRODUCED_833_", new int[]{0, 24});
        x[2] = model.intVar("X_INTRODUCED_26_", new int[]{0, 2, 8, 18});
        x[3] = model.intVar("X_INTRODUCED_833_", 0, 29);
        x[4] = model.intVar("X_INTRODUCED_834_", new int[]{0, 13});
        if (dec) {
            model.argmaxDec(z, 1, x);
        } else {
            model.argmax(z, 1, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(ArrayUtils.append(x, new IntVar[]{z}), seed));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 1440);
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

    @Test(groups = "1s", dataProvider = "decAndSeed")
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
    }

    @Test(groups = "1s", dataProvider = "decAndSeed")
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

    @Test(groups = "1s", dataProvider = "decAndOffset")
    public void testAAA0(boolean dec, int o) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{o, 1 + o, 2 + o, 3 + o});
        x[0] = model.intVar("x1", 1, 3);
        x[1] = model.intVar("x2", 2, 4);
        x[2] = model.intVar("x3", 3, 5);
        x[3] = model.intVar("x4", 4, 6);
        if (dec) {
            model.argmaxDec(z, o, x);
        } else {
            model.argmax(z, o, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.minDomLBSearch(ArrayUtils.append(x, new IntVar[]{z})));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 81);
        Assert.assertEquals(solver.getNodeCount(), 161);
    }

    @Test(groups = "1s", dataProvider = "decAndOffset")
    public void testAAB0(boolean dec, int o) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{3 + o});
        x[0] = model.intVar("x1", 1);
        x[1] = model.intVar("x2", 2);
        x[2] = model.intVar("x3", 3, 5);
        x[3] = model.intVar("x4", 4, 6);
        if (dec) {
            model.argmaxDec(z, o, x);
        } else {
            model.argmax(z, o, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.minDomLBSearch(ArrayUtils.append(x, new IntVar[]{z})));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 6);
        Assert.assertEquals(solver.getNodeCount(), 11);
    }

    @Test(groups = "1s", dataProvider = "decAndOffset")
    public void testAAC0(boolean dec, int o) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{2 + o, 3 + o});
        x[0] = model.intVar("x1", 4, 5);
        x[1] = model.intVar("x2", 1);
        x[2] = model.intVar("x3", 2);
        x[3] = model.intVar("x4", 4, 6);
        if (dec) {
            model.argmaxDec(z, o, x);
        } else {
            model.argmax(z, o, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.minDomLBSearch(ArrayUtils.append(x, new IntVar[]{z})));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 3);
        Assert.assertEquals(solver.getNodeCount(), 5);
    }

    @Test(groups = "1s", dataProvider = "decAndOffset")
    public void testAAD0(boolean dec, int o) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{o, 3 + o});
        x[0] = model.intVar("x1", 4, 5);
        x[1] = model.intVar("x2", 1);
        x[2] = model.intVar("x3", 2);
        x[3] = model.intVar("x4", 4, 5);
        if (dec) {
            model.argmaxDec(z, o, x);
        } else {
            model.argmax(z, o, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.minDomLBSearch(ArrayUtils.append(new IntVar[]{z}, x)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 4);
        Assert.assertEquals(solver.getNodeCount(), 7);
    }

    @Test(groups = "1s", dataProvider = "decAndOffset")
    public void testAAE0(boolean dec, int o) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[4];
        IntVar z = model.intVar("z", new int[]{o, 3 + o});
        x[0] = model.intVar("x1", 4);
        x[1] = model.intVar("x2", 1);
        x[2] = model.intVar("x3", 2);
        x[3] = model.intVar("x4", 4, 5);
        if (dec) {
            model.argmaxDec(z, o, x);
        } else {
            model.argmax(z, o, x).post();
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.minDomLBSearch(ArrayUtils.append(new IntVar[]{z}, x)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 2);
        Assert.assertEquals(solver.getNodeCount(), 3);
    }

    @Test(groups = "1s", dataProvider = "trueOrFalse", dataProviderClass = Providers.class)
    public void testMats1(boolean dec) {
        Model model = new Model(Settings.init());
        IntVar[] x = new IntVar[5];
        x[0] = model.intVar("B", new int[]{1, 7});
        x[1] = model.intVar("E", new int[]{1, 7});
        x[2] = x[0];
        x[3] = model.intVar("1", 1);
        x[4] = model.intVar("7", 7);

        IntVar z = model.intVar("C", 1, 4);
        if (dec) {
            model.argminDec(z, 1, x);
        } else {
            model.argmin(z, 1, x).post();
        }
        Solver solver = model.getSolver();
        //int_search([B,C,E],occurrence,indomain_reverse_split,complete)
        solver.showDecisions(() -> "");
        solver.showSolutions();
        solver.setSearch(
                Search.intVarSearch(
                        new Occurrence<>(),
                        new IntDomainMiddle(!IntDomainMiddle.FLOOR),
                        DecisionOperatorFactory.makeIntReverseSplit(),
                        x[0], z, x[1]));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 4);
    }

    @Test(groups = "1s")
    public void testMats2() {
        Model model = new Model();
        IntVar C = model.intVar("C", new int[]{4, 5, 7});
        IntVar one = model.intVar(1);
        IntVar[] vs = new IntVar[]{one, one, one, C, C};
        model.argmax(model.intVar(5), 1, vs).post();
        Solver solver = model.getSolver();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 0);
    }

}
