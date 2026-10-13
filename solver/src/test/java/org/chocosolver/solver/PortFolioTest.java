/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.search.restart.MonotonicCutoff;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.chocosolver.solver.ModelTest.knapsack;
import static org.chocosolver.solver.search.strategy.Search.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 juil. 2010
 */
public class PortFolioTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testP1() {
        ParallelPortfolio pares = new ParallelPortfolio();
        int n = 1; // number of solvers to use
        for (int i = 0; i < n; i++) {
            pares.addModel(ModelTest.knapsack());
        }
        for (Model m : pares.getModels()) {
            m.clearObjective();
        }
        pares.solve();

        Assert.assertEquals(pares.getBestModel().getSolver().getSolutionCount(), 1);
        System.gc();
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testP11() {
        ParallelPortfolio pares = new ParallelPortfolio();
        int n = 6; // number of solvers to use
        for (int i = 0; i < n; i++) {
            pares.addModel(ProblemMaker.makeGolombRuler(9));
        }
        pares.stealNogoodsOnRestarts();
        int nbSols = 0;
        while (pares.solve()) {
            nbSols++;
        }
        Model finder = pares.getBestModel();
        Assert.assertTrue(nbSols > 0);
        Assert.assertNotNull(finder);
        Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestSolutionValue(), 44);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testP111() {
        ParallelPortfolio pares = new ParallelPortfolio();
        int n = 6; // number of solvers to use
        for (int i = 0; i < n; i++) {
            pares.addModel(ProblemMaker.makeGolombRuler(9, true));
        }
        int nbSols = 0;
        while (pares.solve()) {
            nbSols++;
        }
        Model finder = pares.getBestModel();
        Assert.assertTrue(nbSols > 0);
        Assert.assertNotNull(finder);
        Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestSolutionValue(), 44);
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testParBug() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            pares.addModel(knapsack());
            pares.addModel(knapsack());
            pares.addModel(knapsack());
            pares.addModel(knapsack());
            Solution sol = null;
            while (pares.solve()) {
                sol = new Solution(pares.getBestModel()).record();
            }
            Model finder = pares.getBestModel();
            Assert.assertNotNull(finder);
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestSolutionValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testParWait() {
        ParallelPortfolio pares = new ParallelPortfolio();
        // good solver
        pares.addModel(knapsack());
        // bad solver that always restarts and never terminates, to check that the first solver is able to kill it
        Model m2 = knapsack();
        m2.getSolver().setRestarts(value -> true, new MonotonicCutoff(0), 100000);
        pares.addModel(m2);

        int nbSols = 0;
        while (pares.solve()) {
            nbSols++;
        }
        Model finder = pares.getBestModel();
        Assert.assertTrue(nbSols > 0);
        Assert.assertNotNull(finder);
        Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestSolutionValue(), 51);
        System.gc();
    }


    @Test(groups = "10s", timeOut = 300000)
    public void testP2() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                pares.addModel(knapsack());
            }
            while (pares.solve()) ;
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP21() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                pares.addModel(knapsack());
            }
            List<Solution> sols = pares.streamSolutions().collect(Collectors.toList());
            Model finder = pares.getBestModel();
            Assert.assertEquals(sols.get(sols.size() - 1).getIntVal((IntVar) finder.getObjective()), 51);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP3() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(activityBasedSearch(m.retrieveIntVars(true)));
                pares.addModel(m, true, true);
            }
            while (pares.solve()) ;
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP3bug() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(activityBasedSearch(m.retrieveIntVars(true)));
                pares.addModel(m);
            }
            while (pares.solve()) ;
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP3bug2() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(inputOrderLBSearch(m.retrieveIntVars(true)));
                pares.addModel(m, true, true);
            }
            while (pares.solve()) ;
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP3bug3() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(inputOrderLBSearch(m.retrieveIntVars(true)));
                pares.addModel(m);
            }
            while (pares.solve()) ;
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP4() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int w = 0; w < 10; w++) {
                pares.addModel(knapsack());
            }
            for (Model m : pares.getModels()) {
                m.clearObjective();
            }
            pares.solve();
            Assert.assertEquals(pares.getBestModel().getSolver().getSolutionCount(), 1);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP5() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 10; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(randomSearch(m.retrieveIntVars(true), iter));
                pares.addModel(m, true, true);
            }
            while (pares.solve()) ;
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP6() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 10; i++) {
                Model m = knapsack();
                m.setName("Model_" + i);
                if (i < 5) {
                    m.getObjective().asIntVar().gt(50 + i).post(); // for test purpose
                }
                pares.addModel(m, false, i >= 5);
            }
            while (pares.solve()) ;
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testP7() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 10; i++) {
                Model m = knapsack();
                m.setName("Model_" + i);
                m.getSolver().setSearch(randomSearch(m.retrieveIntVars(true), iter));
                if (i < 5) {
                    m.getObjective().asIntVar().gt(50 + i).post(); // for test purpose
                    m.setName("Model_" + (50 + i));
                } else {
                    m.setName("Model_" + i);
                }
                pares.addModel(m, false, i >= 5);
            }
            while (pares.solve()) ;
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testEnumeration() {
        // enumerating the solutions of a satisfaction problem must terminate: x % 3 = y has 7 solutions
        for (int n : new int[]{1, 2, 4}) {
            ParallelPortfolio pares = new ParallelPortfolio();
            List<IntVar[]> vars = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Model model = new Model();
                IntVar x = model.intVar("x", 0, 6);
                IntVar y = model.intVar("y", 0, 6);
                x.mod(3).eq(y).post();
                vars.add(new IntVar[]{x, y});
                pares.addModel(model);
            }
            Set<String> solutions = new HashSet<>();
            int count = 0;
            while (pares.solve()) {
                count++;
                // each model has 7 solutions and at least one model moves to its next solution on each call
                Assert.assertTrue(count <= 7 * n, "endless enumeration with " + n + " model(s)");
                IntVar[] xy = vars.get(pares.getModels().indexOf(pares.getBestModel()));
                Assert.assertEquals(xy[0].getValue() % 3, xy[1].getValue());
                solutions.add(xy[0].getValue() + "," + xy[1].getValue());
            }
            if (n == 1) {
                Assert.assertEquals(count, 7);
                Assert.assertEquals(solutions.size(), 7);
            } else {
                // the models enumerate independently: a solution may be found by several of them
                Assert.assertTrue(count >= 7, "only " + count + " solution(s) with " + n + " model(s)");
            }
        }
    }
}
