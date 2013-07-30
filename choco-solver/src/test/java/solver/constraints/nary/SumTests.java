/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.nary;

import choco.checker.DomainBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.deprecatedPropagators.PropBigSum;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/04/12
 */
public class SumTests {

    @Test(groups = "1m")
    public void testBound() {
        Random random = new Random();
        int m = 6;
        int b = 14;
        for (int seed = 0; seed < 20; seed++) {
            random.setSeed(seed);
            int[][] dom = DomainBuilder.buildFullDomains(random.nextInt(m) + 2, -random.nextInt(b), random.nextInt(b) + 1);
            PropBigSum.BIG_SUM_SIZE = 100;
			PropBigSum.BIG_SUM_GROUP = 100;
            Solver view = sum(dom, seed, 0);
			PropBigSum.BIG_SUM_SIZE = 1;
			PropBigSum.BIG_SUM_GROUP = 2;
            Solver cons = sum(dom, seed, 0);
            view.findAllSolutions();
            cons.findAllSolutions();
            Assert.assertEquals(view.getMeasures().getSolutionCount(), cons.getMeasures().getSolutionCount(), "S - seed:" + seed);
            Assert.assertEquals(view.getMeasures().getNodeCount(), cons.getMeasures().getNodeCount(), "N - seed:" + seed);
            System.out.printf("%d %.3fms - %.3fms\n", view.getMeasures().getSolutionCount(),
                    view.getMeasures().getTimeCount(), cons.getMeasures().getTimeCount());
        }
    }

    private Solver sum(int[][] dom, int seed, int inc) {
        Random rand = new Random(seed);
        Solver solver = new Solver();
        int n = dom[0].length - 1;
        IntVar r = VariableFactory.bounded("r", dom[0][0], dom[0][n - 1], solver);
        int[] coeffs = new int[dom.length - 1];
        IntVar[] x = new IntVar[dom.length - 1];
        for (int i = 1; i < dom.length; i++) {
            x[i - 1] = VariableFactory.bounded("x_" + i, dom[i][0], dom[i][n - 1], solver);
            coeffs[i - 1] = (rand.nextBoolean() ? -1 : 1) * rand.nextInt(n);
        }
        int fact = (rand.nextBoolean() ? -1 : 1) * (1 + rand.nextInt(n));
        for (int i = 1; i < dom.length; i++) {
            coeffs[i - 1] /= (fact);
        }
        Constraint c = IntConstraintFactory.scalar(x, coeffs, r);
        solver.post(c);
//        System.out.printf("%s\n", solver);

//        SearchMonitorFactory.log(solver, true, true);
        solver.set(IntStrategyFactory.random(x, seed + inc));
        return solver;
    }

}
