/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;

/**
 @author Arthur Godet <arth.godet@gmail.com>
 @since 23/05/2019
 */
public abstract class AbstractCumulativeTest {
    private static Random RND = new Random(System.currentTimeMillis());
    private static final int MAX_DURATION = 100;
    private static final int MAX_HEIGHT = 5;

    public static Task[] buildTasks(int[][] values, Model model) {
        IntVar[] starts = new IntVar[values.length];
        IntVar[] ends = new IntVar[values.length];
        Task[] tasks = new Task[values.length];
        for(int i = 0; i<values.length; i++) {
            starts[i] = model.intVar(values[i][0], values[i][1]);
            ends[i] = model.intVar(values[i][3], values[i][4]);
            tasks[i] = new Task(starts[i], model.intVar(values[i][2]), ends[i]);
        }
        return tasks;
    }

    protected static Task[] buildOpposite(Task[] tasks) {
        Task[] opposite = new Task[tasks.length];
        for(int k = 0; k<opposite.length; k++) {
            opposite[k] = new Task(tasks[k].getEnd().neg().intVar(), tasks[k].getDuration(), tasks[k].getStart().neg().intVar());

        }
        return opposite;
    }

    public static boolean checkProp(Task[] tasks, int[] afterProp) {
        boolean propOk = true;
        for(int i = 0; i<tasks.length && propOk; i++) {
            propOk = (tasks[i].getStart().getLB() == afterProp[i]);
        }
        return propOk;
    }

    static int[][] generateData(int nTasks) {
        int[][] res = new int[nTasks][5];
        for(int i = 0; i<res.length; i++) {
            int[] v = new int[3];
            int a = RND.nextInt(MAX_DURATION);
            int b = RND.nextInt(MAX_DURATION);
            while(b == a) {
                b = RND.nextInt(MAX_DURATION);
            }
            v[0] = Math.min(a,b);
            v[2] = Math.max(a,b);
            if(v[2]-v[0] == 1) {
                v[1] = 1;
            } else {
                v[1] = RND.nextInt(v[2]-v[0]);
                while(v[1] == 0) {
                    v[1] = RND.nextInt(v[2]-v[0]);
                }
            }

            res[i][0] = v[0];
            res[i][1] = v[2]-v[1];
            res[i][2] = v[1];
            res[i][3] = v[0]+v[1];
            res[i][4] = v[2];
        }
        return res;
    }

    public abstract CumulativeFilter propagator(Task[] tasks, IntVar[] heights, IntVar capacity);

    private Model[] buildModels(int[][] values, int[] heights, int capacity) {
        Model model = new Model();
        Task[] tasks = buildTasks(values, model);
        IntVar[] heightsVarMod = new IntVar[heights.length];
        for(int i = 0; i<heights.length; i++) {
            heightsVarMod[i] = model.intVar(heights[i]);
        }
        IntVar cap = model.intVar(capacity);

        model.post(new Constraint("CUMULATIVE", new PropagatorCumulative(tasks, heightsVarMod, cap, propagator(tasks, heightsVarMod, cap), propagator(buildOpposite(tasks), heightsVarMod, cap))));

        Model modelComparison = new Model();
        Task[] tasksComparison = buildTasks(values, modelComparison);
        IntVar[] heightsVar = new IntVar[heights.length];
        for(int i = 0; i<heights.length; i++) {
            heightsVar[i] = modelComparison.intVar(heights[i]);
        }
        IntVar C = modelComparison.intVar(capacity);
        modelComparison.cumulative(tasksComparison, heightsVar, C).post();

        return new Model[]{modelComparison, model};
    }

    @Test(groups="1s", timeOut=60000)
    public void testCumulativePropagator() {
        for(int i = 0; i<10; i++) {
            int[][] values = generateData(3);
            int[] heights = new int[values.length];
            for(int j = 0; j<heights.length; j++) {
                heights[j] = RND.nextInt(MAX_HEIGHT);
                while(heights[j] == 0) {
                    heights[j] = RND.nextInt(MAX_HEIGHT);
                }
            }
            int capacity = MAX_HEIGHT;
            Model[] models = buildModels(values, heights, capacity);
            int[] nbSolutions = new int[2];
            for(int j = 0; j<nbSolutions.length; j++) {
                nbSolutions[j] = models[j].getSolver().findAllSolutions(() -> false).size();
//                System.out.println(j+":"+nbSolutions[j]);
            }

            for(int j = 1; j<nbSolutions.length; j++) {
                if(nbSolutions[0] != nbSolutions[j]) {
                    System.out.println(Arrays.deepToString(values));
                    System.out.println("heights: "+Arrays.toString(heights));
                    System.out.println("capacity: "+capacity);
                    System.out.println("nbSolutions: "+nbSolutions[j]+" instead of "+nbSolutions[0]);
                }
                Assert.assertEquals(nbSolutions[0], nbSolutions[j]);
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test() {
        int[][] values = new int[][]{
                new int[]{52, 56, 4, 56, 60},
                new int[]{23, 34, 11, 34, 45},
                new int[]{37, 85, 2, 39, 87}
        };
        int[] heights = new int[]{2, 3, 3};
        int capacity = 5;

        Model model = new Model();
        Task[] tasks = buildTasks(values, model);
        IntVar[] heightsVar = new IntVar[heights.length];
        for(int i = 0; i<heights.length; i++) {
            heightsVar[i] = model.intVar(heights[i]);
        }
        IntVar cap = model.intVar(capacity);
        model.post(new Constraint("CUMULATIVE", new PropagatorCumulative(tasks, heightsVar, cap, propagator(tasks, heightsVar, cap), propagator(buildOpposite(tasks), heightsVar, cap))));

        int nbSolution = model.getSolver().findAllSolutions(() -> false).size();

        Assert.assertEquals(nbSolution, 2760);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        int[][] values = new int[][]{
                new int[]{35, 55, 5, 40, 60},
                new int[]{30, 43, 55, 85, 98},
                new int[]{36, 68, 6, 42, 74}
        };
        int[] heights = new int[]{2, 4, 2};
        int capacity = 5;

        Model model = new Model();
        Task[] tasks = buildTasks(values, model);
        IntVar[] heightsVar = new IntVar[heights.length];
        for(int i = 0; i<heights.length; i++) {
            heightsVar[i] = model.intVar(heights[i]);
        }
        IntVar cap = model.intVar(capacity);
        model.post(new Constraint("CUMULATIVE", new PropagatorCumulative(tasks, heightsVar, cap, propagator(tasks, heightsVar, cap), propagator(buildOpposite(tasks), heightsVar, cap))));

        int nbSolution = model.getSolver().findAllSolutions(() -> false).size();

        Assert.assertEquals(nbSolution, 11);
    }
}
