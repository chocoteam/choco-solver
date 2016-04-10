/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver;

import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;
import static org.chocosolver.solver.ModelTest.*;

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
    }

    @Test(groups="1s", timeOut=60000)
    public void testParBug() {
        for (int iter = 0; iter < 50; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            pares.addModel(knapsack());
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
            finder.getSolver().printStatistics();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestSolutionValue(), 51);
        }
    }

    @Test(groups="1s", timeOut=10000)
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
    }


    @Test(groups="1s", timeOut=60000)
    public void testP2() {
        for (int iter = 0; iter < 5000; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio();
            for (int i = 0; i < 20; i++) {
                pares.addModel(knapsack());
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testP3() {
        for (int iter = 0; iter < 5000; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio(true);
            for (int i = 0; i < 20; i++) {
                Model m = knapsack();
                m.getSolver().set(activityBasedSearch(m.retrieveIntVars(true)));
                pares.addModel(m);
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testP4() {
        ParallelPortfolio pares = new ParallelPortfolio();
        int n = 4; // number of solvers to use
        for (int i = 0; i < n; i++) {
            pares.addModel(knapsack());
            pares.addModel(knapsack());
        }
        for(Model m:pares.getModels()){
            m.clearObjective();
        }
        pares.solve();
        pares.getBestModel().getSolver().printStatistics();
        Assert.assertEquals(pares.getBestModel().getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testP5() {
        for (int iter = 0; iter < 15000; iter++) {
            ParallelPortfolio pares = new ParallelPortfolio(true);
            for (int i = 0; i < 10; i++) {
                Model m = knapsack();
                m.getSolver().set(randomSearch(m.retrieveIntVars(true),iter));
                pares.addModel(m);
            }
            while(pares.solve());
            Model finder = pares.getBestModel();
            Assert.assertEquals(finder.getSolver().getObjectiveManager().getBestLB().intValue(), 51);
        }
    }
}
