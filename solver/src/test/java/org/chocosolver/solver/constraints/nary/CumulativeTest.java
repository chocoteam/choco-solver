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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @DataProvider(name = "provideRcpsp")
    private Object[][] provideRcpsp() {
        return new Object[][]{j3010_1(), j3010_4(), j3017_5()};
    }

    private Object[] j3010_1() {
        int optimalMakespan = 42;
        int[] resourcesCapacity = new int[]{24, 23, 25, 33};
        int[] durations = new int[]{0, 2, 5, 6, 4, 2, 9, 9, 4, 8, 7, 10, 1, 1, 1, 5, 2, 5, 5, 9, 10, 1, 5, 4, 10, 5, 9, 10, 2, 10, 3, 0};
        int[][] resourcesConsumption = new int[][]{
                {0, 0, 0, 0},
                {1, 2, 4, 0},
                {0, 5, 9, 10},
                {8, 10, 10, 0},
                {8, 3, 0, 8},
                {3, 1, 8, 5},
                {5, 0, 5, 10},
                {4, 0, 0, 3},
                {8, 0, 8, 10},
                {0, 0, 0, 5},
                {1, 6, 6, 6},
                {2, 10, 3, 8},
                {10, 8, 2, 8},
                {1, 3, 6, 1},
                {4, 0, 9, 9},
                {0, 9, 6, 9},
                {4, 3, 9, 10},
                {0, 10, 9, 0},
                {2, 7, 9, 0},
                {0, 5, 4, 1},
                {0, 8, 0, 8},
                {9, 0, 0, 6},
                {4, 8, 3, 8},
                {5, 7, 3, 5},
                {9, 0, 0, 1},
                {1, 8, 0, 7},
                {7, 3, 4, 7},
                {7, 0, 0, 3},
                {1, 3, 0, 10},
                {4, 0, 3, 7},
                {0, 4, 5, 1},
                {0, 0, 0, 0}
        };
        int[][] successors = new int[][]{
                {1, 2, 3},
                {9, 10, 27},
                {8, 14, 15},
                {4, 5, 6},
                {7},
                {17, 26},
                {13, 17},
                {11, 12, 18},
                {9},
                {21},
                {24},
                {29},
                {19},
                {30},
                {20, 23},
                {16, 26, 29},
                {17},
                {20},
                {28},
                {22},
                {25},
                {22},
                {25},
                {24},
                {25, 26, 29},
                {28},
                {30},
                {30},
                {31},
                {31},
                {31},
                {}
        };
        return new Object[]{resourcesCapacity, durations, resourcesConsumption, successors, optimalMakespan};
    }

    private Object[] j3010_4() {
        int optimalMakespan = 58;
        int[] resourcesCapacity = new int[]{20, 19, 23, 23};
        int[] durations = new int[]{0, 9, 5, 9, 10, 8, 1, 10, 6, 3, 9, 10, 4, 2, 4, 10, 9, 9, 10, 1, 7, 9, 5, 5, 3, 8, 4, 2, 1, 7, 8, 0};
        int[][] resourcesConsumption = new int[][]{
                {0, 0, 0, 0},
                {3, 7, 0, 10},
                {0, 2, 10, 6},
                {7, 5, 7, 0},
                {4, 6, 10, 8},
                {8, 4, 7, 8},
                {9, 7, 1, 3},
                {3, 0, 2, 4},
                {4, 0, 3, 8},
                {0, 7, 4, 0},
                {4, 0, 4, 8},
                {2, 0, 5, 2},
                {6, 0, 5, 3},
                {4, 10, 9, 5},
                {0, 0, 4, 7},
                {3, 6, 4, 0},
                {6, 9, 0, 7},
                {5, 2, 3, 4},
                {0, 9, 0, 0},
                {5, 4, 4, 2},
                {7, 0, 6, 4},
                {5, 5, 2, 1},
                {4, 0, 9, 9},
                {5, 0, 0, 7},
                {0, 8, 0, 3},
                {7, 10, 9, 0},
                {0, 7, 8, 10},
                {3, 0, 1, 0},
                {0, 9, 2, 7},
                {2, 2, 3, 8},
                {0, 7, 2, 5},
                {0, 0, 0, 0}
        };
        int[][] successors = new int[][]{
                {1, 2, 3},
                {4, 9, 10},
                {6, 12, 17},
                {5, 8, 15},
                {14},
                {7, 11, 16},
                {21},
                {13},
                {21},
                {13, 28, 30},
                {20},
                {29},
                {15, 24},
                {24},
                {18},
                {19, 25},
                {22},
                {21},
                {20, 22},
                {23},
                {27},
                {24, 28},
                {29},
                {27, 30},
                {25},
                {26},
                {27},
                {29},
                {31},
                {31},
                {31},
                {}
        };
        return new Object[]{resourcesCapacity, durations, resourcesConsumption, successors, optimalMakespan};
    }

    private Object[] j3017_5() {
        int optimalMakespan = 47;
        int[] resourcesCapacity = new int[]{9, 11, 9, 14};
        int[] durations = new int[]{0, 10, 3, 1, 1, 7, 10, 4, 7, 2, 4, 2, 1, 4, 7, 9, 10, 8, 5, 10, 9, 2, 3, 6, 9, 2, 9, 4, 7, 2, 1, 0};
        int[][] resourcesConsumption = new int[][]{
                {0, 0, 0, 0},
                {2, 0, 0, 0},
                {8, 0, 0, 0},
                {0, 5, 0, 0},
                {0, 0, 6, 0},
                {5, 0, 0, 0},
                {0, 6, 0, 0},
                {0, 0, 6, 0},
                {0, 3, 0, 0},
                {0, 0, 0, 3},
                {0, 0, 7, 0},
                {0, 5, 0, 0},
                {0, 3, 0, 0},
                {0, 0, 8, 0},
                {0, 0, 7, 0},
                {2, 0, 0, 0},
                {1, 0, 0, 0},
                {0, 7, 0, 0},
                {0, 9, 0, 0},
                {0, 5, 0, 0},
                {7, 0, 0, 0},
                {0, 0, 0, 2},
                {0, 8, 0, 0},
                {0, 0, 0, 9},
                {5, 0, 0, 0},
                {0, 0, 0, 9},
                {4, 0, 0, 0},
                {0, 10, 0, 0},
                {0, 0, 0, 10},
                {0, 0, 0, 4},
                {0, 0, 6, 0},
                {0, 0, 0, 0}
        };
        int[][] successors = new int[][]{
                {1, 2, 3},
                {16, 19, 20},
                {8, 9, 12},
                {4, 7, 11},
                {5, 6, 13},
                {10, 18},
                {9, 27},
                {8, 17, 24},
                {15, 16, 19},
                {10, 24},
                {14, 22},
                {23},
                {17, 23, 25},
                {14, 15, 30},
                {25, 28},
                {28},
                {21},
                {18},
                {26},
                {22},
                {24},
                {22, 27, 28},
                {23, 25},
                {29},
                {30},
                {29},
                {27, 29},
                {30},
                {31},
                {31},
                {31},
                {}
        };
        return new Object[]{resourcesCapacity, durations, resourcesConsumption, successors, optimalMakespan};
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "provideRcpsp")
    public void testRcpsp(
            int[] resourcesCapacity,
            int[] durations,
            int[][] resourcesConsumption,
            int[][] successors,
            int optimalMakespan
    ) {
        int size = durations.length;
        Model model = new Model();
        Task[] tasks = new Task[size];
        IntVar[] starts = new IntVar[size];
        int hor = Arrays.stream(durations).sum();
        for (int k = 0; k < tasks.length; k++) {
            int d = durations[k];
            starts[k] = model.intVar("start[" + k + "]", 0, hor - d);
            tasks[k] = new Task(starts[k], d);
        }

        // precedence relations
        for (int k = 0; k < tasks.length; k++) {
            for (int s : successors[k]) {
                model.arithm(tasks[k].getEnd(), "<=", tasks[s].getStart()).post();
            }
        }

        // resource management
        List<Task> taskVars = new ArrayList<>();
        List<IntVar> heightVars = new ArrayList<>();
        for (int i = 0; i < resourcesCapacity.length; i++) {
            taskVars.clear();
            heightVars.clear();
            for (int k = 0; k < tasks.length; k++) {
                int resourceConsumption = resourcesConsumption[k][i];
                if (resourceConsumption > 0) {
                    IntVar height = model.intVar(resourceConsumption);
                    heightVars.add(height);
                    taskVars.add(tasks[k]);
                }
            }
            if (!taskVars.isEmpty()) {
                model.cumulative(
                        taskVars.toArray(new Task[0]),
                        heightVars.toArray(new IntVar[0]),
                        model.intVar(resourcesCapacity[i])
                ).post();
            }
        }

        // declare makespan
        IntVar makespan = model.intVar("makespan", 0, hor);
        model.max(makespan, Arrays.stream(tasks).map(Task::getEnd).toArray(IntVar[]::new)).post();
        model.setObjective(false, makespan);

        while (model.getSolver().solve());
        Assert.assertEquals(model.getSolver().getBestSolutionValue(), optimalMakespan);
    }

    @DataProvider(name = "provideJssp")
    private Object[][] provideJssp() {
        return new Object[][]{ft06()};
    }

    private Object[] ft06() {
        int optimalMakespan = 55;
        int[][] durations = new int[][]{
                {1, 3, 6, 7, 3, 6},
                {8, 5, 10, 10, 10, 4},
                {5, 4, 8, 9, 1, 7},
                {5, 5, 5, 3, 8, 9},
                {9, 3, 5, 4, 3, 1},
                {3, 3, 9, 10, 4, 1}
        };
        int[][] idMachines = new int[][]{
                {2, 0, 1, 3, 5, 4},
                {1, 2, 4, 5, 0, 3},
                {2, 3, 5, 0, 1, 4},
                {1, 0, 2, 3, 4, 5},
                {2, 1, 4, 5, 0, 3},
                {1, 3, 5, 0, 4, 2}
        };
        return new Object[]{durations, idMachines, 6, optimalMakespan};
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "provideJssp")
    public void testJssp(
            int[][] durations,
            int[][] idMachines,
            int nbMachines,
            int optimalMakespan
    ) {
        int size = durations.length;
        Model model = new Model();
        Task[] tasks = new Task[durations.length * nbMachines];
        Task[][] tasksPerJob = new Task[size][];
        int hor = Arrays.stream(durations).flatMapToInt(Arrays::stream).sum();
        int idxTask = 0;
        for (int i = 0; i < durations.length; i++) {
            tasksPerJob[i] = new Task[durations[i].length];
            for (int j = 0; j < durations[i].length; j++) {
                final int duration = durations[i][j];
                IntVar start = model.intVar("start[" + i + "][" + j + "]", 0, hor - duration);
                tasks[idxTask] = new Task(start, duration);
                tasksPerJob[i][j] = tasks[idxTask];
                idxTask++;
            }
        }

        // precedence relations
        for (int i = 0; i < tasksPerJob.length; i++) {
            for (int j = 0; j + 1 < tasksPerJob[i].length; j++) {
                model.arithm(tasksPerJob[i][j].getEnd(), "<=", tasksPerJob[i][j + 1].getStart()).post();
            }
        }

        // resource management
        List<Task> taskVars = new ArrayList<>();
        List<IntVar> heightVars = new ArrayList<>();
        for (int k = 0; k < nbMachines; k++) {
            taskVars.clear();
            heightVars.clear();
            for (int i = 0; i < idMachines.length; i++) {
                for (int j = 0; j < idMachines[i].length; j++) {
                    if (idMachines[i][j] == k) {
                        taskVars.add(tasksPerJob[i][j]);
                        heightVars.add(model.intVar(1));
                    }
                }
            }
            if (!taskVars.isEmpty()) {
                model.cumulative(
                        taskVars.toArray(new Task[0]),
                        heightVars.toArray(new IntVar[0]),
                        model.intVar(1)
                ).post();
            }
        }

        // declare makespan
        IntVar makespan = model.intVar("makespan", 0, hor);
        model.max(makespan, Arrays.stream(tasks).map(Task::getEnd).toArray(IntVar[]::new)).post();
        model.setObjective(false, makespan);

        while (model.getSolver().solve());
        Assert.assertEquals(model.getSolver().getBestSolutionValue(), optimalMakespan);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testFilteringHeightsAndCapacity() {
        Model model = new Model();
        IntVar capacity = model.intVar("capacity", 2, 4);

        Task optTask = model.taskVar(model.intVar("optTask.start", 0, 100), 10, true);
        IntVar height = model.intVar("height", 5, 7);
        Task task = model.taskVar(model.intVar("task.start", 0, 100), 0);

        Task[] tasks = new Task[]{
                model.taskVar(model.intVar("start[0]", 0, 100), 10, true),
                model.taskVar(model.intVar("start[1]", 0, 100), 10),
                model.taskVar(model.intVar("start[2]", 0, 100), 10)
        };
        IntVar[] heights = new IntVar[]{
                model.intVar("height[0]", 5, 7),
                model.intVar("height[1]", 3, 5),
                model.intVar("height[2]", 1, 7)
        };

        model.cumulative(new Task[]{optTask}, new IntVar[]{height}, capacity).post();
        model.cumulative(new Task[]{task}, new IntVar[]{height}, capacity).post();
        model.cumulative(tasks, heights, capacity).post();

        try {
            model.getSolver().propagate();
        } catch (ContradictionException ex) {
            Assert.fail();
        }
        Assert.assertFalse(optTask.mayBePerformed());
        Assert.assertEquals(height.getLB(), 5);
        Assert.assertEquals(height.getUB(), 7);

        Assert.assertFalse(tasks[0].mayBePerformed());
        Assert.assertEquals(heights[0].getLB(), 5);
        Assert.assertEquals(heights[0].getUB(), 7);
        Assert.assertEquals(heights[1].getLB(), 3);
        Assert.assertEquals(heights[1].getUB(), 4);
        Assert.assertEquals(heights[2].getLB(), 1);
        Assert.assertEquals(heights[2].getUB(), 4);
        Assert.assertEquals(capacity.getLB(), 3);
        Assert.assertEquals(capacity.getUB(), 4);
    }
}