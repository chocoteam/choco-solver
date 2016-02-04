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

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.restart.LubyRestartStrategy;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ProblemMaker;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.loop.SLF.*;

/**
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class SearchLoopTest {

    @Test(groups="1s", timeOut=60000)
    public void test1DFS() {
        Solver solver = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars(true)));
        Chatterbox.showSolutions(solver);
        solver.findSolution();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 24);
    }

    @Test(groups="1s", timeOut=60000)
    public void test1LDS() {
        Solver solver = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        lds(solver, ISF.lexico_LB(solver.retrieveIntVars(false)), 4);
        solver.findSolution();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 144);
    }

    @Test(groups="1s", timeOut=60000)
    public void test1DDS() {
        Solver solver = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        dds(solver, ISF.lexico_LB(solver.retrieveIntVars(true)), 4);
        solver.findSolution();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 70);
    }

    @Test(groups="10s", timeOut=60000)
    public void test1HBFS() {
        Solver solver = ProblemMaker.makeGolombRuler(8);
        hbfs(solver, ISF.lexico_LB(solver.retrieveIntVars(false)), .05, .1, 32);
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 7);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 7522);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DFS() {
        Solver solver = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars(true)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 92);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 480);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2LDS() {
        Solver solver = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        lds(solver, ISF.lexico_LB(solver.retrieveIntVars(false)), 4);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 7);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 205);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DDS() {
        Solver solver = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        dds(solver, ISF.lexico_LB(solver.retrieveIntVars(false)), 5);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 130);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2DDS2() {
        Solver solver = new Solver();
        IntVar[] bs = VF.boolArray("b", 4, solver);
        dds(solver, ISF.lexico_LB(bs), 3);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 8);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        Solver solver = ProblemMaker.makeGolombRuler(6);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars(false)));
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Chatterbox.printShortStatistics(solver);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 3);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 16);
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Solver solver = ProblemMaker.makeNQueenWithOneAlldifferent(8);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars(false)));
        restart(solver,
                limit -> solver.getMeasures().getNodeCount() >= limit,
                new LubyRestartStrategy(2, 2), 2);
        solver.findAllSolutions();
        Chatterbox.printShortStatistics(solver);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Solver solver = ProblemMaker.makeGolombRuler(5);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars(false)));
        lns(solver, new RandomNeighborhood(solver, solver.retrieveIntVars(false), 15, 0),
                new NodeCounter(solver, 10));
        SMF.limitSearch(solver, () -> solver.getMeasures().getNodeCount() >= 1000);
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Chatterbox.printShortStatistics(solver);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 314);
    }

    @Test(groups="1s", timeOut=60000)
    public void test6() {
        Solver solver = ProblemMaker.makeGolombRuler(6);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars(false)));
        lns(solver, new RandomNeighborhood(solver, solver.retrieveIntVars(false), 15, 0),
                new NodeCounter(solver, 10));
        solver.addStopCriterion(() -> solver.getMeasures().getNodeCount() >= 1000);
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Chatterbox.printShortStatistics(solver);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 972);
    }


    @Test(groups="1s", timeOut=60000)
    public void test7() {
        Solver solver = new Solver();
        int n = 3;
        BoolVar[] B = VF.boolArray("b", n - 1, solver);
        SLF.lds(solver, ISF.lexico_UB(B), 1);
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);

    }

    @Test(groups="1s", timeOut=60000)
    public void test8() {
        Solver solver = new Solver();
        int n = 3;
        IntVar[] X = VF.enumeratedArray("X", n, 0, n, solver);
        BoolVar[] B = VF.boolArray("b", n - 1, solver);
        for (int i = 0; i < n - 1; i++) {
            ICF.arithm(X[i], "<", X[i + 1]).reifyWith(B[i]);
        }
        SLF.lds(solver, ISF.sequencer(ISF.lexico_UB(B), ISF.once(ISF.lexico_var_selector(), ISF.min_value_selector(), X)), 1);
        Chatterbox.showSolutions(solver);
//        Chatterbox.showDecisions(solver);
        SMF.limitSolution(solver, 10);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);

    }

    @Test(groups="1s", timeOut=60000)
    public void test9() {
        Solver solver = new Solver();
        int n = 3;
        IntVar[] X = VF.enumeratedArray("X", n, 0, n, solver);
        BoolVar[] B = VF.boolArray("b", n - 1, solver);
        for (int i = 0; i < n - 1; i++) {
            ICF.arithm(X[i], "<", X[i + 1]).reifyWith(B[i]);
        }
        SLF.seq(solver,
                new MoveBinaryLDS(ISF.lexico_UB(B), 1, solver.getEnvironment()),
                new MoveBinaryDFS(
                        // ISF.lexico_LB(X)
                        ISF.once(ISF.lexico_var_selector(), ISF.min_value_selector(), X)
                ));
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        SMF.limitSolution(solver, 10);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);

    }

}
