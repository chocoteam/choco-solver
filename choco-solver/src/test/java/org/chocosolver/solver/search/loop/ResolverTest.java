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
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.loop.move.MoveBinaryDFS;
import org.chocosolver.solver.search.loop.move.MoveBinaryLDS;
import org.chocosolver.solver.search.restart.LubyRestartStrategy;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.loop.SLF.*;
import static org.chocosolver.solver.search.loop.SearchLoopFactory.lds;
import static org.chocosolver.solver.search.loop.SearchLoopFactory.seq;
import static org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory.limitSolution;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.*;
import static org.chocosolver.solver.trace.Chatterbox.*;
import static org.chocosolver.util.ProblemMaker.makeNQueenWithOneAlldifferent;
import static org.testng.Assert.assertEquals;

/**
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class ResolverTest {

    @Test(groups="1s", timeOut=60000)
    public void test1DFS() {
        Model model = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        dfs(model, ISF.lexico_LB(model.retrieveIntVars(true)));
        Chatterbox.showSolutions(model);
        model.solve();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(model.getMeasures().getNodeCount(), 24);
    }

    @Test(groups="1s", timeOut=60000)
    public void test1LDS() {
        Model model = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        lds(model, ISF.lexico_LB(model.retrieveIntVars(false)), 4);
        model.solve();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(model.getMeasures().getNodeCount(), 144);
    }

    @Test(groups="1s", timeOut=60000)
    public void test1DDS() {
        Model model = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        dds(model, ISF.lexico_LB(model.retrieveIntVars(true)), 4);
        model.solve();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(model.getMeasures().getNodeCount(), 70);
    }

    @Test(groups="10s", timeOut=60000)
    public void test1HBFS() {
        Model model = ProblemMaker.makeGolombRuler(8);
        hbfs(model, ISF.lexico_LB(model.retrieveIntVars(false)), .05, .1, 32);
        model.solve();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 7);
        Assert.assertEquals(model.getMeasures().getNodeCount(), 7522);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DFS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        dfs(model, lexico_LB(model.retrieveIntVars(true)));
        while (model.solve()) ;
        assertEquals(model.getMeasures().getSolutionCount(), 92);
        assertEquals(model.getMeasures().getNodeCount(), 480);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2LDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        lds(model, lexico_LB(model.retrieveIntVars(false)), 4);
        while (model.solve()) ;
        assertEquals(model.getMeasures().getSolutionCount(), 7);
        assertEquals(model.getMeasures().getNodeCount(), 205);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DDS() {
        Model model = makeNQueenWithOneAlldifferent(8);
        dds(model, lexico_LB(model.retrieveIntVars(false)), 5);
        while (model.solve()) ;
        assertEquals(model.getMeasures().getSolutionCount(), 2);
        assertEquals(model.getMeasures().getNodeCount(), 130);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DDS2() {
        Model model = new Model();
        IntVar[] bs = model.boolVarArray("b", 4);
        dds(model, lexico_LB(bs), 3);
        while (model.solve()) ;
        assertEquals(model.getMeasures().getSolutionCount(), 8);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Model model = ProblemMaker.makeGolombRuler(6);
        dfs(model, ISF.lexico_LB(model.retrieveIntVars(false)));
        model.solve();
        Chatterbox.printShortStatistics(model);
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 3);
        Assert.assertEquals(model.getMeasures().getNodeCount(), 16);
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model model = makeNQueenWithOneAlldifferent(8);
        dfs(model, lexico_LB(model.retrieveIntVars(false)));
        restart(model,
                limit -> model.getMeasures().getNodeCount() >= limit,
                new LubyRestartStrategy(2, 2), 2);
        while (model.solve()) ;
        printShortStatistics(model);
        assertEquals(model.getMeasures().getRestartCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Model model = ProblemMaker.makeGolombRuler(5);
        dfs(model, ISF.lexico_LB(model.retrieveIntVars(false)));
        lns(model, new RandomNeighborhood(model, model.retrieveIntVars(false), 15, 0),
                new NodeCounter(model, 10));
        SMF.limitSearch(model, () -> model.getMeasures().getNodeCount() >= 1000);
        model.solve();
        Chatterbox.printShortStatistics(model);
        Assert.assertEquals(model.getMeasures().getRestartCount(), 314);
    }

    @Test(groups="1s", timeOut=60000)
    public void test6() {
        Model model = ProblemMaker.makeGolombRuler(6);
        dfs(model, ISF.lexico_LB(model.retrieveIntVars(false)));
        lns(model, new RandomNeighborhood(model, model.retrieveIntVars(false), 15, 0),
                new NodeCounter(model, 10));
        model.addStopCriterion(() -> model.getMeasures().getNodeCount() >= 1000);
        model.solve();
        Chatterbox.printShortStatistics(model);
        Assert.assertEquals(model.getMeasures().getRestartCount(), 972);
    }


    @Test(groups="1s", timeOut=60000)
    public void test7() {
        Model model = new Model();
        int n = 3;
        BoolVar[] B = model.boolVarArray("b", n - 1);
        lds(model, lexico_UB(B), 1);
        showSolutions(model);
        showDecisions(model);
        while (model.solve()) ;
        assertEquals(model.getMeasures().getSolutionCount(), 4);

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
        lds(model, sequencer(lexico_UB(B), once(lexico_var_selector(), min_value_selector(), X)), 1);
        showSolutions(model);
//        Chatterbox.showDecisions(solver);
        limitSolution(model, 10);
        while (model.solve()) ;
        assertEquals(model.getMeasures().getSolutionCount(), 4);

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
        seq(model,
                new MoveBinaryLDS(lexico_UB(B), 1, model.getEnvironment()),
                new MoveBinaryDFS(
                        // ISF.lexico_LB(X)
                        once(lexico_var_selector(), min_value_selector(), X)
                ));
        showSolutions(model);
        showDecisions(model);
        limitSolution(model, 10);
        while (model.solve()) ;
        assertEquals(model.getMeasures().getSolutionCount(), 4);

    }

}
