/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.*;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.chocosolver.solver.search.strategy.Search.lastConflict;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;

/**
 * Tests the various filtering algorithms of the cumulative constraint
 *
 * @author Thierry Petit, Jean-Guillaume Fages, Arthur GODET <arth.godet@gmail.com>
 */
public class CumulativeTest {

    public static final boolean VERBOSE = false;

    // too long, but can be used manually
    public void testLong() {
        for (int mode : new int[]{1})
            for (int n = 1; n < 100; n *= 2) {
                for (int dmin = 0; dmin < 5; dmin++) {
                    for (int hmax = 0; hmax < 5; hmax++) {
                        for (int capamax = 0; capamax < 6; capamax++) {
                            for (long seed = 0; seed < 5; seed++) {
                                test(n, capamax, dmin, hmax, seed, mode);
                            }
                        }
                    }
                }
            }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testDur0() {
        Model m = new Model();

        Task t1 = new Task(
                m.intVar(9),
                m.intVar(6),
                m.intVar(15)
        );
        Task t2 = new Task(
                m.intVar(8),
                m.intVar(new int[]{0, 6}),
                m.intVar(8, 14)
        );

        m.cumulative(new Task[]{t1, t2}, new IntVar[]{m.intVar(1), m.intVar(1)}, m.intVar(1)).post();

        Solver s = m.getSolver();

        try {
            s.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertTrue(t2.getDuration().isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test1() {
        test(4, 5, 0, 2, 0, 0);
        test(4, 5, 0, 2, 0, 1);
        test(4, 5, 1, 2, 0, 0);
        test(4, 5, 1, 2, 0, 1);
        test(4, 5, 2, 2, 0, 0);
        test(4, 5, 2, 2, 0, 1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() {
        test(4, 9, 2, 4, 2, 1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() {
        test(32, 3, 0, 2, 3, 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test4() {
        test(16, 6, 2, 4, 9, 0);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test5() {
        test(32, 3, 2, 4, 1, 0);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void test6() {
        // this tests raises an exception which is in fact due to the time limit
        // and unlucky random heuristic (fixed by adding last conflict)
        test(16, 3, 2, 4, 4, 1);
        test(32, 3, 2, 2, 3, 0);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testMed() {
        for (int mode : new int[]{0, 1})
            for (int n = 1; n < 15; n *= 2) {
                for (int dmin = 0; dmin < 5; dmin += 2) {
                    for (int hmax = 0; hmax < 5; hmax += 2) {
                        for (int capamax = 0; capamax < 10; capamax += 3) {
                            test(n, capamax, dmin, hmax, 0, mode);
                        }
                    }
                }
            }
    }

    public void test(int n, int capamax, int dmin, int hmax, long seed, int mode) {
        if (VERBOSE) {
            System.out.println(n + " - " + capamax + " - " + dmin + " - " + hmax + " - " + seed + " - " + mode);
        }
        long ref = solve(n, capamax, dmin, hmax, seed, mode);
    }

    public static long solve(int n, int capamax, int dmin, int hmax, long seed, int mode) {
        final Model model = new Model();
        int dmax = 5 + dmin * 2;
        final IntVar[] s = model.intVarArray("s", n, 0, n * dmax, false);
        final IntVar[] d = model.intVarArray("d", n, dmin, dmax, false);
        final IntVar[] e = model.intVarArray("e", n, 0, n * dmax, false);
        final IntVar[] h = model.intVarArray("h", n, 0, hmax, false);
        final IntVar capa = model.intVar("capa", 0, capamax, false);
        final IntVar last = model.intVar("last", 0, n * dmax, false);
        Task[] t = new Task[n];
        for (int i = 0; i < n; i++) {
            t[i] = new Task(s[i], d[i], e[i]);
            model.arithm(e[i], "<=", last).post();
        }
        model.cumulative(t, h, capa).post();
        Solver r = model.getSolver();
        r.setSearch(lastConflict(randomSearch(model.retrieveIntVars(false), seed)));
        model.getSolver().limitTime(5000);
        switch (mode) {
            case 0:
                model.getSolver().solve();
                if (r.isStopCriterionMet()) return -1;
                return r.getMeasures().getSolutionCount();
            case 1:
                model.setObjective(Model.MINIMIZE, last);
                while (model.getSolver().solve()) ;
                if (r.isStopCriterionMet()) return -1;
                return r.getMeasures().getBestSolutionValue().longValue();
            case 2:
                while (model.getSolver().solve()) ;// too many solutions to be used
                if (r.isStopCriterionMet()) return -1;
                return r.getMeasures().getSolutionCount();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testADelsol1() {
        int[] height = new int[]{0, 1, 3, 5, 1, 4, 4, 3, 4, 3, 0};
        int capaMax = 10;
        int[] duration = new int[11];
        Arrays.fill(duration, 1);
        // dÃ©claration du modÃ¨le
        Model model = new Model("test");
        // Ajout des starting times
        IntVar[] start = model.intVarArray("start", 11, 0, 3);
        model.cumulative(start, duration, height, capaMax).post();

        Solver solver = model.getSolver();
        while (solver.solve()) {
            for (int time = 0; time < 4; ++time) {
                int max_height = 0;
                for (int i = 0; i < 11; ++i) {
                    if (start[i].getValue() == time) max_height += height[i];
                }
                Assert.assertTrue(max_height <= capaMax);
            }
        }
    }

    @Test(groups = "1s", dataProvider = "trueOrFalse", dataProviderClass = Providers.class)
    public void testGCCAT(boolean lcg) {
        Model model = new Model(Settings.dev().setLCG(lcg));
        int[][] s = new int[][]{{1, 5}, {2, 7}, {3, 6}, {1, 8}};
        int[][] d = new int[][]{{4, 4}, {6, 6}, {3, 6}, {2, 3}};
        int[][] e = new int[][]{{1, 9}, {1, 9}, {1, 9}, {1, 9}};
        int[][] h = new int[][]{{2, 6}, {3, 3}, {1, 2}, {3, 4}};

        Task[] tasks = IntStream.range(0, 4)
                .mapToObj(i -> new Task(
                        model.intVar("s" + i, s[i][0], s[i][1]),
                        model.intVar("d" + i, d[i][0], d[i][1]),
                        model.intVar("e" + i, e[i][0], e[i][1])))
                .toArray(Task[]::new);

        IntVar[] height = IntStream.range(0, 4)
                .mapToObj(i -> model.intVar("h" + i, h[i][0], h[i][1]))
                .toArray(IntVar[]::new);

        model.cumulative(tasks, height, model.intVar(5)).post();
        Solver solver = model.getSolver();
        solver.findAllSolutions();

        Assert.assertEquals(solver.getSolutionCount(), 8);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "provideFilterHeight")
    public void testFilterHeight(Integer hMax) {
        final Model model = new Model();
        final int[][] s = new int[][]{{0, 0}, {10, 10}, {0, 3}, {6, 8}, {3, 6}};
        final int[][] d = new int[][]{{5, 5}, {10, 10}, {3, 6}, {5, 7}, {3, 6}};
        final int[][] e = new int[][]{{5, 5}, {20, 20}, {4, 20}, {1, 20}, {1, 20}};
        final int[][] h = new int[][]{{hMax, hMax}, {hMax, hMax}, {0, 1}, {0, 1}, {0, 1}};

        Task[] tasks = IntStream.range(0, 5)
                .mapToObj(i -> new Task(
                        model.intVar("s" + i, s[i][0], s[i][1]),
                        model.intVar("d" + i, d[i][0], d[i][1]),
                        model.intVar("e" + i, e[i][0], e[i][1])))
                .toArray(Task[]::new);

        IntVar[] height = IntStream.range(0, 5)
                .mapToObj(i -> model.intVar("h" + i, h[i][0], h[i][1]))
                .toArray(IntVar[]::new);

        model.cumulative(tasks, height, model.intVar(hMax)).post();

        try {
//            height[4].updateLowerBound(1, Cause.Null);
            model.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertEquals(height[2].getUB(), 0);
        Assert.assertEquals(height[3].getUB(), 0);
        Assert.assertEquals(height[4].getLB(), 0);
        Assert.assertEquals(height[4].getUB(), 1);
    }

    @DataProvider(name = "provideFilterHeight")
    private Object[][] provideFilterHeight() {
        return new Integer[][]{{1}, {2}};
    }
}