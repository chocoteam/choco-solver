/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.search.loop.monitors.IMonitorOpenNode;
import org.chocosolver.solver.search.restart.MonotonicCutoff;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    /**
     * Every model of a portfolio must start its search, even when there are more models
     * than threads in the common ForkJoinPool (issue #1262).
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1262() throws InterruptedException {
        int n = ForkJoinPool.getCommonPoolParallelism() + 3;
        CountDownLatch started = new CountDownLatch(n);
        AtomicBoolean stop = new AtomicBoolean();
        ParallelPortfolio pares = new ParallelPortfolio();
        for (int k = 0; k < n; k++) {
            // pigeonhole problem: 14 pigeons, 13 holes, long to prove unsatisfiable
            Model model = new Model();
            IntVar[] x = model.intVarArray("x", 14, 0, 12);
            for (int i = 0; i < x.length; i++) {
                for (int j = i + 1; j < x.length; j++) {
                    model.arithm(x[i], "!=", x[j]).post();
                }
            }
            model.getSolver().setSearch(intVarSearch(new InputOrder<>(model), new IntDomainMin(), x));
            model.getSolver().addStopCriterion(stop::get);
            AtomicBoolean counted = new AtomicBoolean();
            model.getSolver().plugMonitor(new IMonitorOpenNode() {
                @Override
                public void beforeOpenNode() {
                    if (counted.compareAndSet(false, true)) {
                        started.countDown();
                    }
                }
            });
            pares.addModel(model, true, true);
        }
        Thread solving = new Thread(pares::solve);
        solving.start();
        boolean allStarted;
        try {
            allStarted = started.await(10, TimeUnit.SECONDS);
        } finally {
            stop.set(true);
            solving.join();
        }
        Assert.assertTrue(allStarted, (n - started.getCount()) + "/" + n + " searches started");
    }

    private static Model pigeonhole(int pigeons) {
        Model model = new Model();
        IntVar[] x = model.intVarArray("x", pigeons, 0, pigeons - 2);
        for (int i = 0; i < x.length; i++) {
            for (int j = i + 1; j < x.length; j++) {
                model.arithm(x[i], "!=", x[j]).post();
            }
        }
        model.getSolver().setSearch(intVarSearch(new InputOrder<>(model), new IntDomainMin(), x));
        return model;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testErrorInReliableModel() {
        // the error of a reliable model is thrown, and the other models are stopped
        ParallelPortfolio pares = new ParallelPortfolio();
        Model failing = pigeonhole(14);
        failing.getSolver().plugMonitor(new IMonitorOpenNode() {
            @Override
            public void beforeOpenNode() {
                if (failing.getSolver().getNodeCount() == 1000) {
                    throw new IllegalStateException("bug in a reliable model");
                }
            }
        });
        pares.addModel(failing, true, true);
        pares.addModel(pigeonhole(14), true, true);
        IllegalStateException e = Assert.expectThrows(IllegalStateException.class, pares::solve);
        Assert.assertEquals(e.getMessage(), "bug in a reliable model");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testErrorInUnreliableModel() {
        // the error of an unreliable model is ignored
        ParallelPortfolio pares = new ParallelPortfolio();
        Model failing = pigeonhole(14);
        failing.getSolver().plugMonitor(new IMonitorOpenNode() {
            @Override
            public void beforeOpenNode() {
                throw new IllegalStateException("bug in an unreliable model");
            }
        });
        pares.addModel(failing, true, false);
        pares.addModel(pigeonhole(6), true, true);
        Assert.assertFalse(pares.solve());
        // the reliable model proves that there is no solution
        Assert.assertEquals(pares.getModels().get(1).getSolver().getSearchState(), SearchState.TERMINATED);
    }

    private static Model knapsackModel(boolean lcg, int seed) {
        java.util.Random rnd = new java.util.Random(seed);
        int n = 20;
        int[] weights = new int[n];
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            weights[i] = 5 + rnd.nextInt(30);
            values[i] = 5 + rnd.nextInt(30);
        }
        Model model = new Model("knapsack", SettingsBuilder.init().setLCG(lcg));
        BoolVar[] take = model.boolVarArray("take", n);
        IntVar profit = model.intVar("profit", 0, 1000);
        model.scalar(take, weights, "<=", 150).post();
        model.scalar(take, values, "=", profit).post();
        model.setObjective(Model.MAXIMIZE, profit);
        return model;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testLCGOptimization() {
        // the cuts are shared between LCG models running in parallel
        for (int seed = 0; seed < 5; seed++) {
            Model ref = knapsackModel(false, seed);
            int best = -1;
            while (ref.getSolver().solve()) {
                best = ((IntVar) ref.getObjective()).getValue();
            }
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int k = 0; k < 4; k++) {
                pares.addModel(knapsackModel(true, seed));
            }
            int found = -1;
            while (pares.solve()) {
                found = ((IntVar) pares.getBestModel().getObjective()).getValue();
            }
            Assert.assertEquals(found, best, "seed " + seed);
        }
    }
}
