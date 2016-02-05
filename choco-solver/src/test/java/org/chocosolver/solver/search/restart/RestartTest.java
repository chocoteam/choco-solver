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
package org.chocosolver.solver.search.restart;

import org.chocosolver.memory.Environments;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.SLF;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.memory.Environments.TRAIL;
import static org.chocosolver.solver.search.loop.SearchLoopFactory.restartOnSolutions;
import static org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory.limitSolution;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/11
 */
public class RestartTest {

    protected static Solver buildQ(int n) {
        Solver solver = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = solver.intVar("Q_" + i, 1, n, false);
        }


        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = solver.arithm(vars[i], "!=", vars[j]);
                neq.post();
                solver.arithm(vars[i], "!=", vars[j], "+", -k).post();
                solver.arithm(vars[i], "!=", vars[j], "+", k).post();
            }
        }
        solver.set(ISF.lexico_LB(vars));
        return solver;
    }

    @Test(groups="1s", timeOut=60000)
    public void testGeometricalRestart1() {
        Solver solver = buildQ(4);
        SearchMonitorFactory.geometrical(solver, 2, 1.1, new NodeCounter(solver, 2), 2);
        solver.findAllSolutions();
        // not 2, because of restart, that found twice the same solution
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 12);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLubyRestart1() {
        Solver solver = buildQ(4);
        SearchMonitorFactory.luby(solver, 2, 2, new NodeCounter(solver, 2), 2);
        solver.findAllSolutions();
        // not 2, because of restart, that found twice the same solution
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 11);
    }


    public final static int[] LUBY_2 = {1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 16};

    public final static int[] LUBY_3 = {1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,
            1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9,
            1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 9, 27};

    public final static int[] LUBY_4 = {1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16,
            1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 1, 1, 1, 1, 4, 16, 64
    };

    public final static int[] GEOMETRIC_1_3 = {1, 2, 2, 3, 3, 4, 5, 7, 9, 11, 14, 18, 24, 31, 40};

    private void checkRestart(AbstractRestartStrategy r, double factor, int[] expected) {
        r.setGeometricalFactor(factor);
        int[] computed = r.getSequenceExample(expected.length);
        Assert.assertEquals(computed, expected);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRestartStrategy() {
        AbstractRestartStrategy r = new LubyRestartStrategy(1, 2);
        checkRestart(r, 2, LUBY_2);
        checkRestart(r, 3, LUBY_3);
        checkRestart(r, 4, LUBY_4);
        r = new GeometricalRestartStrategy(1, 1.3);
        checkRestart(r, 1.3, GEOMETRIC_1_3);
    }

    @Test(groups="10s", timeOut=60000)
    public void test1() {

        for (int j = 1; j < 5; j++) {
            int n = 200;
            Solver solver = new Solver(TRAIL.make(), "Test");
            IntVar[] X = solver.intVarArray("X", n, 1, n, false);
            IntVar[] Y = solver.intVarArray("Y", n, n + 1, 2 * (n + 1), false);
            solver.allDifferent(X).post();
            for (int i = 0; i < n; i++) {
                solver.arithm(Y[i], "=", X[i], "+", n).post();
            }
            restartOnSolutions(solver);
            solver.set(lexico_LB(X));
//            SMF.log(solver, false, false);
            limitSolution(solver, 100);
            solver.findAllSolutions();
            //System.out.printf("%d - %.3fms \n", n, solver.getMeasures().getTimeCount());
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testGeometricalRestart2() {
        Solver solver = buildQ(8);
        SearchMonitorFactory.geometrical(solver, 10, 1.2, new FailCounter(solver, 10), 2);
        solver.findAllSolutions();
        // not 2, because of restart, that found twice the same solution
//        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 92);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
    }

}
