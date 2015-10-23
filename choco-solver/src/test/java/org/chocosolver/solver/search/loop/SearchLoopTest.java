/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgement:
 * This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.restart.LubyRestartStrategy;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.loop.SLF.*;

/**
 * Created by cprudhom on 02/09/15.
 * Project: choco.
 */
public class SearchLoopTest {

    public void queen(Solver solver, int n) {
        IntVar[] q = VF.enumeratedArray("Q", n, 1, n, solver);
        solver.post(ICF.alldifferent(q, "AC"));
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                solver.post(IntConstraintFactory.arithm(q[i], "!=", q[j], "+", -k));
                solver.post(IntConstraintFactory.arithm(q[i], "!=", q[j], "+", k));
            }
        }
    }

    public void golomb(Solver solver, int m) {
        IntVar[] ticks = VariableFactory.enumeratedArray("a", m, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);
        IntVar[] diffs = VariableFactory.enumeratedArray("d", (m * m - m) / 2, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);

        solver.post(IntConstraintFactory.arithm(ticks[0], "=", 0));

        for (int i = 0; i < m - 1; i++) {
            solver.post(IntConstraintFactory.arithm(ticks[i + 1], ">", ticks[i]));
        }
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                solver.post(IntConstraintFactory.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, diffs[k]));
                solver.post(IntConstraintFactory.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2));
                solver.post(IntConstraintFactory.arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2));
                solver.post(IntConstraintFactory.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2));
            }
        }
        solver.post(IntConstraintFactory.alldifferent(diffs, "BC"));
        // break symetries
        if (m > 2) {
            solver.post(IntConstraintFactory.arithm(diffs[0], "<", diffs[diffs.length - 1]));
        }
        solver.setObjectives(ticks[m - 1]);
    }

    @Test(groups = "1s")
    public void test1DFS() {
        Solver solver = new Solver();
        queen(solver, 8);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars()));
        Chatterbox.showSolutions(solver);
        solver.findSolution();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 24);
    }

    @Test(groups = "1s")
    public void test1LDS() {
        Solver solver = new Solver();
        queen(solver, 8);
        lds(solver, ISF.lexico_LB(solver.retrieveIntVars()), 4);
        solver.findSolution();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 140);
    }

    @Test(groups = "1s")
    public void test1DDS() {
        Solver solver = new Solver();
        queen(solver, 8);
        dds(solver, ISF.lexico_LB(solver.retrieveIntVars()), 4);
        solver.findSolution();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 66);
    }

    @Test(groups = "1s")
    public void test2DFS() {
        Solver solver = new Solver();
        queen(solver, 8);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars()));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 92);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 480);
    }

    @Test(groups = "1s")
    public void test2LDS() {
        Solver solver = new Solver();
        queen(solver, 8);
        lds(solver, ISF.lexico_LB(solver.retrieveIntVars()), 4);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 7);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 201);
    }

    @Test(groups = "1s")
    public void test2DDS() {
        Solver solver = new Solver();
        queen(solver, 8);
        dds(solver, ISF.lexico_LB(solver.retrieveIntVars()), 5);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 125);
    }

    @Test(groups = "1s")
    public void test2DDS2() {
        Solver solver = new Solver();
        IntVar[] bs = VF.boolArray("b", 4, solver);
        dds(solver, ISF.lexico_LB(bs), 3);
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 8);
    }

    @Test(groups = "1s")
    public void test3() {
        Solver solver = new Solver();
        golomb(solver, 6);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars()));
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Chatterbox.printShortStatistics(solver);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 3);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 17);
    }

    @Test(groups = "1s")
    public void test4() {
        Solver solver = new Solver();
        queen(solver, 8);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars()));
        restart(solver,
                limit -> solver.getMeasures().getNodeCount() >= limit,
                new LubyRestartStrategy(2, 2), 2);
        solver.findAllSolutions();
        Chatterbox.printShortStatistics(solver);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
    }

    @Test(groups = "1s")
    public void test5() {
        Solver solver = new Solver();
        golomb(solver, 5);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars()));
        lns(solver, new RandomNeighborhood(solver, solver.retrieveIntVars(), 15, 0),
                () -> solver.getMeasures().getNodeCount() % 10 == 0);
        SMF.limitSearch(solver, () -> solver.getMeasures().getNodeCount() >= 1000);
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Chatterbox.printShortStatistics(solver);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 315);
    }

    @Test(groups = "1s")
    public void test6() {
        Solver solver = new Solver();
        golomb(solver, 6);
        dfs(solver, ISF.lexico_LB(solver.retrieveIntVars()));
        lns(solver, new RandomNeighborhood(solver, solver.retrieveIntVars(), 15, 0),
                () -> solver.getMeasures().getNodeCount() % 10 == 0);
        solver.addStopCriterion(() -> solver.getMeasures().getNodeCount() >= 1000);
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE);
        Chatterbox.printShortStatistics(solver);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 972);
    }


}
