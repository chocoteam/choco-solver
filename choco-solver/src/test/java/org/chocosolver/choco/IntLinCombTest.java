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
package org.chocosolver.choco;

import org.chocosolver.choco.checker.DomainBuilder;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.*;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 23 avr. 2010<br/>
 */
public class IntLinCombTest {

    private static String operatorToString(Operator operator) {
        String opSt;
        switch (operator) {
            case EQ:
                opSt = "=";
                break;
            case NQ:
                opSt = "!=";
                break;
            case GE:
                opSt = ">=";
                break;
            case GT:
                opSt = ">";
                break;
            case LE:
                opSt = "<=";
                break;
            case LT:
                opSt = "<";
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return opSt;
    }

    public static void testOp(int n, int min, int max, int cMax, int seed, Operator operator) {
        Random random = new Random(seed);
        Solver s = new Solver();
        IntVar[] vars = new IntVar[n];
        int[] coeffs = new int[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
            coeffs[i] = random.nextInt(cMax);
        }
        int constant = -random.nextInt(cMax);

        IntVar sum = VariableFactory.bounded("scal", -99999999, 99999999, s);


        Constraint[] cstrs = new Constraint[]{
                IntConstraintFactory.scalar(vars, coeffs, sum),
                IntConstraintFactory.arithm(sum, operatorToString(operator), constant)
        };

        s.post(cstrs);
        s.set(IntStrategyFactory.lexico_LB(vars));

        s.findAllSolutions();
    }

    @Test(groups = "1s")
    public void testEq() {
        testOp(2, 0, 5, 5, 29091982, Operator.EQ);
    }

    @Test(groups = "1s")
    public void testGeq() {
        testOp(2, 0, 5, 5, 29091981, Operator.GE);
    }

    @Test(groups = "1s")
    public void testLeq() {
        testOp(2, 0, 5, 5, 29091981, Operator.LE);
    }

    @Test(groups = "1s")
    public void testNeq() {
        testOp(2, 0, 5, 5, 29091981, Operator.NQ);
    }


    protected Solver sum(int[][] domains, int[] coeffs, int b, int op) {
        Solver solver = new Solver();
        IntVar[] bins = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            bins[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], solver);
        }
        String opname = "=";
        if (op != 0) {
            if (op > 0) {
                opname = ">=";
            } else {
                opname = "<=";
            }
        }
        IntVar sum = VariableFactory.bounded("scal", -99999999, 99999999, solver);
        Constraint[] cstrs = new Constraint[]{
                IntConstraintFactory.scalar(bins, coeffs, sum),
                IntConstraintFactory.arithm(sum, opname, b)
        };
        solver.post(cstrs);
        solver.set(IntStrategyFactory.lexico_LB(bins));
        return solver;
    }

    protected Solver intlincomb(int[][] domains, int[] coeffs, int b, int op) {
        Solver solver = new Solver();
        IntVar[] bins = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            bins[i] = VariableFactory.bounded("v_" + i, domains[i][0], domains[i][domains[i].length - 1], solver);
        }
        String opname = "=";
        if (op != 0) {
            if (op > 0) {
                opname = ">=";
            } else {
                opname = "<=";
            }
        }
        IntVar sum = VariableFactory.bounded("scal", -99999999, 99999999, solver);
        Constraint[] cstrs = new Constraint[]{
                IntConstraintFactory.scalar(bins, coeffs, sum),
                IntConstraintFactory.arithm(sum, opname, b)
        };
        solver.post(cstrs);
        solver.set(IntStrategyFactory.lexico_LB(bins));
        return solver;
    }

    @Test(groups = "1m")
    public void testSumvsIntLinCombTest() {
        Random rand = new Random();
        for (int seed = 0; seed < 400; seed++) {
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
                    op, sum.getMeasures().getTimeCount(), intlincomb.getMeasures().getTimeCount());
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
        PropagationEngineFactory.DEFAULT.make(sum);
        Variable[] vars = sum.getVars();
        int offSet = 2;// ZERO and ONE constants
        ((IntVar) vars[offSet]).instantiateTo(-2, Cause.Null);
        ((IntVar) vars[1 + offSet]).instantiateTo(-1, Cause.Null);
        sum.propagate();
//        sum.getSearchLoop().timeStamp++;
        ((IntVar) vars[2 + offSet]).removeValue(-2, Cause.Null);
        sum.propagate();
        Assert.assertTrue(vars[2 + offSet].isInstantiated());
    }

    @Test(groups = "1s")
    public void testIss237_1() {
        Solver solver = new Solver();
        BoolVar[] bs = VF.boolArray("b", 3, solver);
        solver.post(ICF.scalar(bs, new int[]{1, 2, 3}, "=", VF.fixed(2, solver)));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

}
