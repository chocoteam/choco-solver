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

/**
 @author Arthur Godet <arth.godet@gmail.com>
 @since 23/05/2019
 */
public abstract class AbstractDisjunctiveTest {
    public abstract DisjunctiveFilter propagator(Task[] tasks);

    private Model[] buildModels(int[][] values) {
        Model model = new Model();
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);

        model.post(new Constraint("DISJUNCTIVE",
                new PropagatorDisjunctive(tasks, propagator(tasks), propagator(AbstractCumulativeTest.buildOpposite(tasks)))));

        Model modelComparison = new Model();
        Task[] tasksComparison = AbstractCumulativeTest.buildTasks(values, modelComparison);
        IntVar[] heightsVar = new IntVar[tasksComparison.length];
        for(int i = 0; i<tasks.length; i++) {
            heightsVar[i] = modelComparison.intVar(1);
        }
        IntVar C = modelComparison.intVar(1);
        modelComparison.cumulative(tasksComparison, heightsVar, C).post();

        return new Model[]{modelComparison, model};
    }

    @Test(groups="1s", timeOut=60000)
    public void testDisjunctivePropagator() {
        for(int i = 0; i<10; i++) {
            int[][] values = AbstractCumulativeTest.generateData(3);
            Model[] models = buildModels(values);
            int[] nbSolutions = new int[2];
            for(int j = 0; j<nbSolutions.length; j++) {
                nbSolutions[j] = models[j].getSolver().findAllSolutions(null).size();
//                System.out.println(j+":"+nbSolutions[j]);
            }

            for(int j = 1; j<nbSolutions.length; j++) {
                if(nbSolutions[0] != nbSolutions[j]) {
                    System.out.println(Arrays.deepToString(values));
                }
                Assert.assertEquals(nbSolutions[0], nbSolutions[j]);
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test() {
        int[][] values = new int[][]{
                new int[]{48, 59, 1, 49, 60},
                new int[]{15, 38, 9, 24, 47},
                new int[]{61, 82, 9, 70, 91}
        };

        Model model = new Model();
        Task[] tasks = AbstractCumulativeTest.buildTasks(values, model);
        model.post(new Constraint("DISJUNCTIVE",
                new PropagatorDisjunctive(tasks, propagator(tasks), propagator(AbstractCumulativeTest.buildOpposite(tasks)))));

        int nbSolution = model.getSolver().findAllSolutions(null).size();

        Assert.assertEquals(nbSolution, 6336);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        int[][] values = new int[][]{
                new int[]{56, 72, 11, 67, 83},
                new int[]{61, 64, 1, 62, 65},
                new int[]{71, 77, 15, 86, 92}
        };

        Model[] models = buildModels(values);
        int[] nbSolutions = new int[2];
        for(int j = 0; j<nbSolutions.length; j++) {
            nbSolutions[j] = models[j].getSolver().findAllSolutions(null).size();
//                System.out.println(j+":"+nbSolutions[j]);
        }

        for(int j = 1; j<nbSolutions.length; j++) {
            if(nbSolutions[0] != nbSolutions[j]) {
                System.out.println(Arrays.deepToString(values));
            }
            Assert.assertEquals(nbSolutions[0], nbSolutions[j]);
        }
    }
}
