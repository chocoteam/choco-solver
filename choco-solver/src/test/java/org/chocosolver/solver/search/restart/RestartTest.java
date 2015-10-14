/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.limits.NodeCounter;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

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
            vars[i] = VariableFactory.enumerated("Q_" + i, 1, n, solver);
        }


        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                Constraint neq = IntConstraintFactory.arithm(vars[i], "!=", vars[j]);
                solver.post(neq);
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", -k));
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", k));
            }
        }
        solver.set(ISF.lexico_LB(vars));
        return solver;
    }

    @Test(groups = "1s")
    public void testGeometricalRestart1() {
        Solver solver = buildQ(4);
        SearchMonitorFactory.geometrical(solver, 2, 1.2, new NodeCounter(2), 2);
        solver.findAllSolutions();
        // not 2, because of restart, that found twice the same solution
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 12);
    }

    @Test(groups = "1s")
    public void testLubyRestart1() {
        Solver solver = buildQ(4);
        SearchMonitorFactory.luby(solver, 2, 2, new NodeCounter(2), 2);
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

    @Test(groups = "1s")
    public void testRestartStrategy() {
        AbstractRestartStrategy r = new LubyRestartStrategy(1, 2);
        checkRestart(r, 2, LUBY_2);
        checkRestart(r, 3, LUBY_3);
        checkRestart(r, 4, LUBY_4);
        r = new GeometricalRestartStrategy(1, 1.3);
        checkRestart(r, 1.3, GEOMETRIC_1_3);
    }

    @Test(groups = "10s")
    public void test1() {

        for (int j = 1; j < 5; j++) {
            int n = 200;
            Solver solver = new Solver(Environments.TRAIL.make(), "Test");
            IntVar[] X = VF.enumeratedArray("X", n, 1, n, solver);
            IntVar[] Y = VF.enumeratedArray("Y", n, n + 1, 2 * (n + 1), solver);
            solver.post(ICF.alldifferent(X));
            for (int i = 0; i < n; i++) {
                solver.post(ICF.arithm(Y[i], "=", X[i], "+", n));
            }
            SMF.restartAfterEachSolution(solver);
            solver.set(ISF.lexico_LB(X));
//            SMF.log(solver, false, false);
            SMF.limitSolution(solver, 100);
            solver.findAllSolutions();
            //System.out.printf("%d - %.3fms \n", n, solver.getMeasures().getTimeCount());
        }
    }

    @Test(groups = "1s")
    public void testGeometricalRestart2() {
        Solver solver = buildQ(8);
        SearchMonitorFactory.geometrical(solver, 10, 1.2, new FailCounter(10), 2);
        solver.findAllSolutions();
        // not 2, because of restart, that found twice the same solution
//        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 92);
        Assert.assertEquals(solver.getMeasures().getRestartCount(), 2);
    }

}
