/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco;

import choco.checker.DomainBuilder;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.IntLinComb;
import solver.constraints.nary.Sum;
import solver.exception.ContradictionException;
import solver.search.loop.AbstractSearchLoop;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.VariableFactory;

import java.util.Random;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 23 avr. 2010<br/>
 * Since : Galak 0.1<br/>
 */
public class IntLinCombTest {


    public static void testOp(int n, int min, int max, int cMax, int seed, IntLinComb.Operator operator) {
        Random random = new Random(seed);
        Solver s = new Solver();
        IntVar[] vars = new IntVar[n];
        int[] coeffs = new int[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
            coeffs[i] = random.nextInt(cMax);
        }
        int constant = -random.nextInt(cMax);

        Constraint[] cstrs = new Constraint[]{
                new IntLinComb(vars, coeffs, n, operator, constant, s)
        };

        s.post(cstrs);
        s.set(StrategyFactory.presetI(vars, s.getEnvironment()));

        s.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testEq() {
        testOp(2, 0, 5, 5, 29091982, IntLinComb.Operator.EQ);
    }

    @Test(groups = "1s")
    public void testGeq() {
        testOp(2, 0, 5, 5, 29091981, IntLinComb.Operator.GEQ);
    }

    @Test(groups = "1s")
    public void testLeq() {
        testOp(2, 0, 5, 5, 29091981, IntLinComb.Operator.LEQ);
    }

    @Test(groups = "1s")
    public void testNeq() {
        testOp(2, 0, 5, 5, 29091981, IntLinComb.Operator.NEQ);
    }


    protected Solver sum(int[][] domains, int[] coeffs, int b, int op) {
        Solver solver = new Solver();

        IntVar[] bins = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            bins[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], solver);
        }

        Constraint cons;
        if (op == 0) {
            cons = Sum.eq(bins, coeffs, b, solver);
        } else if (op > 0) {
            cons = Sum.geq(bins, coeffs, b, solver);
        } else {
            cons = Sum.leq(bins, coeffs, b, solver);
        }


        Constraint[] cstrs = new Constraint[]{cons};

        solver.post(cstrs);
        solver.set(StrategyFactory.presetI(bins, solver.getEnvironment()));
        return solver;
    }

    protected Solver intlincomb(int[][] domains, int[] coeffs, int b, int op) {
        Solver solver = new Solver();

        IntVar[] bins = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            bins[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], solver);
        }

        Constraint cons;
        if (op == 0) {
            cons = ConstraintFactory.scalar(bins, coeffs, IntLinComb.Operator.EQ, b, solver);
        } else if (op > 0) {
            cons = ConstraintFactory.scalar(bins, coeffs, IntLinComb.Operator.GEQ, b, solver);
        } else {
            cons = ConstraintFactory.scalar(bins, coeffs, IntLinComb.Operator.LEQ, b, solver);
        }

        Constraint[] cstrs = new Constraint[]{cons};

        solver.post(cstrs);
        solver.set(StrategyFactory.presetI(bins, solver.getEnvironment()));
        return solver;
    }

    @Test(groups = "1m")
    public void testSumvsIntLinCombTest() {
        Random rand = new Random();
        for (int seed = 6; seed < 400; seed++) {
            rand.setSeed(seed);
            int n = 1 + rand.nextInt(6);
            int min = -10 + rand.nextInt(20);
            int max = min + rand.nextInt(20);
            int[][] domains = DomainBuilder.buildFullDomains(n, min, max, rand, 1.0, false);
            int[] coeffs = new int[n];
            for (int i = 0; i < n; i++) {
                coeffs[i] = -25 + rand.nextInt(50);
            }
            int lb = -50 + rand.nextInt(100);
            int op = -1 + rand.nextInt(3);

            Solver sum = sum(domains, coeffs, lb, op);
            Solver intlincomb = intlincomb(domains, coeffs, lb, op);

            sum.findAllSolutions();
            intlincomb.findAllSolutions();
            Assert.assertEquals(sum.getMeasures().getSolutionCount(), intlincomb.getMeasures().getSolutionCount());
            Assert.assertEquals(sum.getMeasures().getNodeCount(), intlincomb.getMeasures().getNodeCount());
            LoggerFactory.getLogger("test").info("({}) {}ms vs {}ms",
                    new Object[]{op, sum.getMeasures().getTimeCount(), intlincomb.getMeasures().getTimeCount()});
        }
    }

    @Test(groups = "1s")
    public void testUSum1() {
        Solver sumleq = sum(new int[][]{{-2, 3}}, new int[]{-2}, -6, -1);
        sumleq.findAllSolutions();
    }

    /**
     * Default propagation test:
     * When an opposite var is declared, the lower (resp. upper) bound modification
     * should be transposed in upper (resp. lower) bound event...
     */
    @Test(groups = "1s")
    public void testUSum2() throws ContradictionException {
        Solver sum = sum(new int[][]{{-2, 7}, {-1, 6}, {2}, {-2, 5}, {-2, 4}, {-2, 6}}, new int[]{-7, 13, -3, -18, -24, 1}, 30, 0);
        sum.getSearchLoop().propEngine.init();
        Variable[] vars = sum.getVars();
        ((IntVar) vars[0]).instantiateTo(-2, Cause.Null);
        ((IntVar) vars[1]).instantiateTo(-1, Cause.Null);
        sum.getSearchLoop().propEngine.fixPoint();
        AbstractSearchLoop.timeStamp++;
        ((IntVar) vars[2]).removeValue(-2, Cause.Null);
        sum.getSearchLoop().propEngine.fixPoint();
        Assert.assertTrue(vars[2].instantiated());
    }

}
