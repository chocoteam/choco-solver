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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.loop.move.MoveBinaryLDS;
import org.chocosolver.solver.search.restart.LubyRestartStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;
import static org.chocosolver.solver.trace.Chatterbox.*;
import static org.chocosolver.util.ProblemMaker.makeGolombRuler;
import static org.chocosolver.util.ProblemMaker.makeNQueenWithOneAlldifferent;
import static org.testng.Assert.assertEquals;

/**
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class SolverTest {

    @Test(groups="1s", timeOut=60000)
    public void test1DFS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.set(inputOrderLBSearch(model.retrieveIntVars(true)));
        r.setDFS();
        showSolutions(model);
        model.solve();
        assertEquals(r.getMeasures().getSolutionCount(), 1);
        assertEquals(r.getMeasures().getNodeCount(), 24);
    }

    @Test(groups="1s", timeOut=60000)
    public void test1LDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.set(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setLDS(4);
        model.solve();
        assertEquals(r.getMeasures().getSolutionCount(), 1);
        assertEquals(r.getMeasures().getNodeCount(), 144);
    }

    @Test(groups="1s", timeOut=60000)
    public void test1DDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.set(inputOrderLBSearch(model.retrieveIntVars(true)));
        r.setDDS(4);
        model.solve();
        assertEquals(r.getMeasures().getSolutionCount(), 1);
        assertEquals(r.getMeasures().getNodeCount(), 70);
    }

    @Test(groups="10s", timeOut=60000)
    public void test1HBFS() {
        Model model = makeGolombRuler(8);
        Solver r = model.getSolver();
        r.set(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setHBFS(.05, .1, 32);
        while(model.solve());
        assertEquals(model.getSolver().getMeasures().getSolutionCount(), 7);
        assertEquals(model.getSolver().getMeasures().getNodeCount(), 7522);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DFS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setDFS();
        r.set(inputOrderLBSearch(model.retrieveIntVars(true)));
        while (model.solve()) ;
        assertEquals(model.getSolver().getMeasures().getSolutionCount(), 92);
        assertEquals(model.getSolver().getMeasures().getNodeCount(), 480);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2LDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.set(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setLDS(4);
        while (model.solve()) ;
        assertEquals(model.getSolver().getMeasures().getSolutionCount(), 7);
        assertEquals(model.getSolver().getMeasures().getNodeCount(), 205);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.set(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setDDS(5);
        while (model.solve()) ;
        assertEquals(model.getSolver().getMeasures().getSolutionCount(), 2);
        assertEquals(model.getSolver().getMeasures().getNodeCount(), 130);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DDS2() {
        Model model = new Model();
        Solver r = model.getSolver();
        IntVar[] bs = model.boolVarArray("b", 4);
        r.set(inputOrderLBSearch(bs));
        r.setDDS(3);
        while (model.solve()) ;
        assertEquals(model.getSolver().getMeasures().getSolutionCount(), 8);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = makeGolombRuler(6);
        Solver r = model.getSolver();
        r.setDFS();
        r.set(inputOrderLBSearch(model.retrieveIntVars(false)));
        while(model.solve());
        printShortStatistics(model);
        assertEquals(model.getSolver().getMeasures().getSolutionCount(), 3);
        assertEquals(model.getSolver().getMeasures().getNodeCount(), 16);
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model model = makeNQueenWithOneAlldifferent(8);
        Solver r = model.getSolver();
        r.setDFS();
        r.set(inputOrderLBSearch(model.retrieveIntVars(false)));
        model.getSolver().setRestarts(limit -> model.getSolver().getMeasures().getNodeCount() >= limit, new LubyRestartStrategy(2, 2), 2);
        while (model.solve()) ;
        printShortStatistics(model);
        assertEquals(model.getSolver().getMeasures().getRestartCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Model model = makeGolombRuler(5);
        Solver r = model.getSolver();
        r.setDFS();
        r.set(inputOrderLBSearch(model.retrieveIntVars(false)));
        r.setLNS(new RandomNeighborhood(model.retrieveIntVars(false), 15, 0), new NodeCounter(model, 10));
        r.limitSearch(() -> r.getMeasures().getNodeCount() >= 1000);
        while(model.solve());
        printShortStatistics(model);
        assertEquals(model.getSolver().getMeasures().getRestartCount(), 314);
    }

    @Test(groups="1s", timeOut=60000)
    public void test6() {
        Model model = makeGolombRuler(6);
        Solver r = model.getSolver();
        r.setDFS();
        r.set(inputOrderLBSearch(model.retrieveIntVars(false)));
        model.getSolver().setLNS(new RandomNeighborhood(model.retrieveIntVars(false), 15, 0), new NodeCounter(model, 10));
        r.addStopCriterion(() -> r.getMeasures().getNodeCount() >= 1000);
        while(model.solve());
        printShortStatistics(model);
        assertEquals(r.getMeasures().getRestartCount(), 972);
    }


    @Test(groups="1s", timeOut=60000)
    public void test7() {
        Model model = new Model();
        int n = 3;
        BoolVar[] B = model.boolVarArray("b", n - 1);
        Solver r = model.getSolver();
        r.set(inputOrderUBSearch(B));
        r.setLDS(1);
        showSolutions(model);
        showDecisions(model);
        while (model.solve()) ;
        assertEquals(model.getSolver().getMeasures().getSolutionCount(), 4);

    }

    @Test(groups="1s", timeOut=60000)
    public void test8() {
        Model model = new Model();
        int n = 3;
        IntVar[] X = model.intVarArray("X", n, 0, n, false);
        BoolVar[] B = model.boolVarArray("b", n - 1);
        for (int i = 0; i < n - 1; i++) {
            model.arithm(X[i], "<", X[i + 1]).reifyWith(B[i]);
        }
        Solver r = model.getSolver();
        r.set(inputOrderUBSearch(B), greedySearch(inputOrderLBSearch(X)));
        r.setLDS(1);
        showSolutions(model);
//        Chatterbox.showDecisions(solver);
        r.limitSolution(10);
        while (model.solve()) ;
        assertEquals(model.getSolver().getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void test9() {
        Model model = new Model();
        int n = 3;
        IntVar[] X = model.intVarArray("X", n, 0, n, false);
        BoolVar[] B = model.boolVarArray("b", n - 1);
        for (int i = 0; i < n - 1; i++) {
            model.arithm(X[i], "<", X[i + 1]).reifyWith(B[i]);
        }

        Solver r = model.getSolver();
        r.set(new MoveBinaryLDS(inputOrderUBSearch(B), 1,model.getEnvironment()),
                new MoveBinaryDFS(greedySearch(inputOrderLBSearch(X)))
        );
        showSolutions(model);
        showDecisions(model);
        r.limitSolution(10);
        while (model.solve()) ;
        assertEquals(r.getMeasures().getSolutionCount(), 4);
    }
}
