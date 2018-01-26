/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.ModelTest.knapsack;
import static org.chocosolver.solver.search.strategy.Search.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23 juil. 2010
 */
public class PortFolioTest {

    @Test(groups="1s", timeOut=60000)
    public void testP1() {
        ParallelPortfolio pares = new ParallelPortfolio();
        int n = 1; // number of solvers to use
        for (int i = 0; i < n; i++) {
            pares.addModel(ModelTest.knapsack());
        }
        for(Model m:pares.getModels()){
            m.clearObjective();
        }
        pares.solve();
        pares.getBestModel().getSolver().printStatistics();
        Assert.assertEquals(pares.getBestModel().getSolver().getSolutionCount(), 1);
        System.gc();
    }

    @Test(groups="10s", timeOut=60000)
    public void testParBug() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            pares.addModel(knapsack());
            pares.addModel(knapsack());
            pares.addModel(knapsack());
            pares.addModel(knapsack());
            Solution sol = null;
            while(pares.solve()){
                sol = new Solution(pares.getBestModel()).record();
            }
            Model finder = pares.getBestModel();
            System.out.println(sol);
            Assert.assertNotNull(finder);
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestSolutionValue(), 51);
            System.gc();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testParWait() {
        ParallelPortfolio pares = new ParallelPortfolio();
        // good solver
        pares.addModel(knapsack());
        // bad solver that always restarts and never terminates, to check that the first solver is able to kill it
        Model m2 = knapsack();
        m2.getSolver().setRestarts(value -> true, new MonotonicRestartStrategy(0),100000);
        pares.addModel(m2);

        int nbSols = 0;
        while(pares.solve()){
            nbSols++;
        }
        Model finder = pares.getBestModel();
        Assert.assertTrue(nbSols>0);
        Assert.assertNotNull(finder);
        Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestSolutionValue(), 51);
        System.gc();
    }


    @Test(groups="10s", timeOut=300000)
    public void testP2() {
        for (int iter = 0; iter < 50; iter++) {
            System.out.println("ITERATION "+iter);
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                pares.addModel(knapsack());
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void testP3() {
        for (int iter = 0; iter < 50; iter++) {
            System.out.println("ITERATION "+iter);
            ParallelPortfolio pares = new ParallelPortfolio(false);
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(activityBasedSearch(m.retrieveIntVars(true)));
                pares.addModel(m);
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void testP3bug() {
        for (int iter = 0; iter < 50; iter++) {
            System.out.println("ITERATION "+iter);
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(activityBasedSearch(m.retrieveIntVars(true)));
                pares.addModel(m);
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void testP3bug2() {
        for (int iter = 0; iter < 50; iter++) {
            System.out.println("ITERATION "+iter);
            ParallelPortfolio pares = new ParallelPortfolio(false);
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(inputOrderLBSearch(m.retrieveIntVars(true)));
                pares.addModel(m);
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void testP3bug3() {
        for (int iter = 0; iter < 50; iter++) {
            System.out.println("ITERATION "+iter);
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(inputOrderLBSearch(m.retrieveIntVars(true)));
                pares.addModel(m);
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void testP4() {
        for (int iter = 0; iter < 50; iter++) {
            System.out.println("ITERATION "+iter);
            ParallelPortfolio pares = new ParallelPortfolio();
            for(int w=0;w<10;w++) {
                pares.addModel(knapsack());
            }
            for(Model m:pares.getModels()){
                m.clearObjective();
            }
            pares.solve();
            Assert.assertEquals(pares.getBestModel().getSolver().getSolutionCount(), 1);
            System.gc();
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void testP5() {
        for (int iter = 0; iter < 50; iter++) {
            System.out.println("ITERATION "+iter);
            ParallelPortfolio pares = new ParallelPortfolio(false);
            for (int i = 0; i < 10; i++) {
                Model m = knapsack();
                m.getSolver().setSearch(randomSearch(m.retrieveIntVars(true),iter));
                pares.addModel(m);
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
            System.gc();
        }
    }
}
