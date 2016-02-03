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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.*;
import org.chocosolver.solver.constraints.checker.DomainBuilder;
import org.chocosolver.solver.constraints.nary.cnf.PropTrue;
import org.chocosolver.solver.constraints.nary.sum.PropScalar;
import org.chocosolver.solver.constraints.nary.sum.PropSum;
import org.chocosolver.solver.constraints.nary.sum.PropSumBool;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
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
            vars[i] = s.makeIntVar("v_" + i, min, max, false);
            coeffs[i] = random.nextInt(cMax);
        }
        int constant = -random.nextInt(cMax);

        IntVar sum = s.makeIntVar("scal", -99999999, 99999999, true);


        Constraint[] cstrs = new Constraint[]{
                IntConstraintFactory.scalar(vars, coeffs, "=", sum),
                IntConstraintFactory.arithm(sum, operatorToString(operator), constant)
        };

        s.post(cstrs);
        s.set(IntStrategyFactory.lexico_LB(vars));

        s.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testEq() {
        testOp(2, 0, 5, 5, 29091982, Operator.EQ);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGeq() {
        testOp(2, 0, 5, 5, 29091981, Operator.GE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLeq() {
        testOp(2, 0, 5, 5, 29091981, Operator.LE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNeq() {
        testOp(2, 0, 5, 5, 29091981, Operator.NQ);
    }


    protected Solver sum(int[][] domains, int[] coeffs, int b, int op) {
        Solver solver = new Solver();
        IntVar[] bins = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            bins[i] = solver.makeIntVar("v_" + i, domains[i][0], domains[i][domains[i].length - 1], true);
        }
        String opname = "=";
        if (op != 0) {
            if (op > 0) {
                opname = ">=";
            } else {
                opname = "<=";
            }
        }
        IntVar sum = solver.makeIntVar("scal", -99999999, 99999999, true);
        Constraint[] cstrs = new Constraint[]{
                IntConstraintFactory.scalar(bins, coeffs, "=", sum),
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
            bins[i] = solver.makeIntVar("v_" + i, domains[i][0], domains[i][domains[i].length - 1], true);
        }
        String opname = "=";
        if (op != 0) {
            if (op > 0) {
                opname = ">=";
            } else {
                opname = "<=";
            }
        }
        IntVar sum = solver.makeIntVar("scal", -99999999, 99999999, true);
        Constraint[] cstrs = new Constraint[]{
                IntConstraintFactory.scalar(bins, coeffs, "=", sum),
                IntConstraintFactory.arithm(sum, opname, b)
        };
        solver.post(cstrs);
        solver.set(IntStrategyFactory.lexico_LB(bins));
        return solver;
    }

    @Test(groups="5m", timeOut=300000)
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
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testUSum1() {
        Solver sumleq = sum(new int[][]{{-2, 3}}, new int[]{-2}, -6, -1);
        sumleq.findAllSolutions();
    }

    /**
     * Default propagation test:
     * When an opposite var is declared, the lower (resp. upper) bound modification
     * should be transposed in upper (resp. lower) bound event...
     */
    @Test(groups="1s", timeOut=60000)
    public void testUSum2() throws ContradictionException {
        Solver sum = sum(new int[][]{{-2, 7}, {-1, 6}, {2}, {-2, 5}, {-2, 4}, {-2, 6}}, new int[]{-7, 13, -3, -18, -24, 1}, 30, 0);
        PropagationEngineFactory.DEFAULT.make(sum);
        Variable[] vars = sum.getVars();
        ((IntVar) vars[0]).instantiateTo(-2, Cause.Null);
        ((IntVar) vars[1]).instantiateTo(-1, Cause.Null);
        sum.propagate();
//        sum.getSearchLoop().timeStamp++;
        ((IntVar) vars[2]).removeValue(-2, Cause.Null);
        sum.propagate();
        Assert.assertTrue(vars[2].isInstantiated());
    }

    @Test(groups="1s", timeOut=60000)
    public void testIss237_1() {
        Solver solver = new Solver();
        BoolVar[] bs = solver.makeBoolVarArray("b", 3);
        solver.post(ICF.scalar(bs, new int[]{1, 2, 3}, "=", 2));
        Chatterbox.showSolutions(solver);
        solver.findAllSolutions();
    }

    @Test(groups="1s", timeOut=60000)
    public void testS1_coeff_null() {
        Solver solver = new Solver();
        solver.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        IntVar[] ivars = solver.makeIntVarArray("V", 4, 0, 5, false);
        int[] coeffs = new int[]{1, 0, 0, 2};
        IntVar res = solver.makeIntVar("R", 0, 10, false);
        Constraint c = ICF.scalar(ivars, coeffs, "=", res);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropScalar);
        Assert.assertEquals(3, p.getNbVars());
    }

    @Test(groups="1s", timeOut=60000)
    public void testS2_coeff_null() {
        Solver solver = new Solver();
        solver.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        IntVar[] ivars = solver.makeIntVarArray("V", 4, 0, 5, false);
        ivars[2] = ivars[1];
        int[] coeffs = new int[]{1, 1, -1, 2};
        IntVar res = solver.makeIntVar("R", 0, 10, false);
        Constraint c = ICF.scalar(ivars, coeffs, "=", res);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropScalar);
        Assert.assertEquals(3, p.getNbVars());
    }

    @Test(groups="1s", timeOut=60000)
    public void testD1() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 4, 0, 5, false);
        int[] coeffs = new int[]{1, 1, 1, 1};
        IntVar res = solver.makeIntVar("R", 0, 10, false);
        Constraint c = ICF.scalar(ivars, coeffs, "=", res);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSum);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD2() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeBoolVarArray("V", 4);
        int[] coeffs = new int[]{1, 1, 1, 1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD3() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeBoolVarArray("V", 4);
        int[] coeffs = new int[]{-1, -1, -1, -1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD4() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeBoolVarArray("V", 4);
        int[] coeffs = new int[]{1, -1, 1, 1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD5() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeBoolVarArray("V", 4);
        int[] coeffs = new int[]{-1, 1, -1, -1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD6() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 4, 0, 1, false);
        ivars[1] = solver.makeIntVar("X", 0, 2, false);
        int[] coeffs = new int[]{1, -1, 1, 1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD7() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 4, 0, 1, false);
        ivars[1] = solver.makeIntVar("X", 0, 2, false);
        int[] coeffs = new int[]{-1, 1, -1, -1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSum);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD8() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 4, 0, 1, false);
        ivars[2] = solver.makeIntVar("X", 0, 2, false);
        int[] coeffs = new int[]{1, -1, 1, 1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSum);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD9() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 4, 0, 1, false);
        ivars[2] = solver.makeIntVar("X", 0, 2, false);
        int[] coeffs = new int[]{-1, 1, -1, -1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD10() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 2, 0, 2, false);
        int[] coeffs = new int[]{1, 1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD11() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 2, 0, 2, false);
        int[] coeffs = new int[]{1, -1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD12() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 2, 0, 2, false);
        int[] coeffs = new int[]{-1, 1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD13() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 2, 0, 2, false);
        int[] coeffs = new int[]{-1, -1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD14() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 1, 0, 2, false);
        int[] coeffs = new int[]{1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD15() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 1, 0, 2, false);
        int[] coeffs = new int[]{-1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD16() {
        Solver solver = new Solver();
        IntVar[] ivars = solver.makeIntVarArray("V", 1, 0, 2, false);
        int[] coeffs = new int[]{1};
        Constraint c = ICF.scalar(ivars, coeffs, "=", ivars[0]);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropTrue);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD20() {
        Solver solver = new Solver();
        solver.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        IntVar[] ivars = solver.makeIntVarArray("V", 4, 0, 5, false);
        int[] coeffs = new int[]{1, 2, 2, 1};
        IntVar res = solver.makeIntVar("R", 0, 10, false);
        Constraint c = ICF.scalar(ivars, coeffs, "=", res);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropScalar);
    }

    @Test(groups="1s", timeOut=60000)
    public void testExt1() {
        Solver s1 = new Solver();
        s1.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        {
            BoolVar[] bs = s1.makeBoolVarArray("b", 5);
            s1.post(ICF.sum(bs, "!=", 3));
        }
        Solver s2 = new Solver();
        s2.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 1000;
            }
        });
        {
            BoolVar[] bs = s2.makeBoolVarArray("b", 5);
            s2.post(ICF.sum(bs, "!=", 3));
        }
        s1.findAllSolutions();
        s2.findAllSolutions();
        Assert.assertEquals(s2.getMeasures().getSolutionCount(), s1.getMeasures().getSolutionCount());
        Assert.assertEquals(s2.getMeasures().getNodeCount(), s1.getMeasures().getNodeCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testExt2() {
        Solver s1 = new Solver();
        s1.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        {
            BoolVar[] bs = s1.makeBoolVarArray("b", 5);
            s1.post(ICF.sum(bs, "<=", 3));
        }
        Solver s2 = new Solver();
        s2.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 1000;
            }
        });
        {
            BoolVar[] bs = s2.makeBoolVarArray("b", 5);
            s2.post(ICF.sum(bs, "<=", 3));
        }
        s1.findAllSolutions();
        s2.findAllSolutions();
        Assert.assertEquals(s2.getMeasures().getSolutionCount(), s1.getMeasures().getSolutionCount());
        Assert.assertEquals(s2.getMeasures().getNodeCount(), s1.getMeasures().getNodeCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testExt3() {
        Solver s1 = new Solver();
        s1.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        {
            BoolVar[] bs = s1.makeBoolVarArray("b", 3);
            BoolVar r = s1.makeBoolVar("r");
            ICF.scalar(bs, new int[]{-1, -1, -1}, "<=",-2).reifyWith(r);
        }
        Solver s2 = new Solver();
        s2.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 1000;
            }
        });
        {
            BoolVar[] bs = s2.makeBoolVarArray("b", 3);
            BoolVar r = s2.makeBoolVar("r");
            ICF.scalar(bs, new int[]{-1, -1, -1}, "<=", -2).reifyWith(r);
        }
        Chatterbox.showDecisions(s1);
        Chatterbox.showDecisions(s2);
        s1.findAllSolutions();
        s2.findAllSolutions();
        Assert.assertEquals(s2.getMeasures().getSolutionCount(), s1.getMeasures().getSolutionCount());
        Assert.assertEquals(s2.getMeasures().getNodeCount(), s1.getMeasures().getNodeCount());
    }

    @Test(groups="5m", timeOut=300000)
    public void testB1() {
        Solver solver = new Solver();
        solver.set(new Settings() {
            @Override
            public short[] getCoarseEventPriority() {
                return new short[]{0, 0, 0, 0, 1, 2, 3};
            }
        });
        int n = 23;
        BoolVar[] bs = solver.makeBoolVarArray("b", n);
        int[] cs = new int[n];
        int k = (int) (n * .7);
        Arrays.fill(cs, 0, n, 1);
        Arrays.fill(cs, k, n, -1);
        IntVar sum = solver.makeIntVar("S", -n / 2, n / 2, true);
        solver.post(ICF.scalar(bs, cs, "=", sum));
        solver.set(ISF.lexico_LB(bs));
//        Chatterbox.showDecisions(solver);
        solver.findAllSolutions();
    }


    @Test(groups="1s", timeOut=60000)
    public void testB2() throws ContradictionException {
        Solver solver = new Solver();
        int n = 3;
        BoolVar[] bs = solver.makeBoolVarArray("b", n);
        int[] cs = new int[n];
        Arrays.fill(cs, 0, n, -1);
        solver.post(ICF.scalar(bs, cs, "<=", -2));
        solver.propagate();
        bs[2].setToFalse(Cause.Null);
        bs[0].setToTrue(Cause.Null);
        solver.propagate();
        Assert.assertTrue(bs[1].isInstantiatedTo(1));
    }


    @Test(groups="1s", timeOut=60000)
    public void testB3() {
        Solver solver = new Solver();
        solver.post(ICF.scalar(new IntVar[]{solver.makeIntVar(1), solver.makeIntVar(3)}, new int[]{1, -1}, "!=", 0));
        try {
            solver.propagate();
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testB4() {
        Solver solver = new Solver();
        IntVar[] X = solver.makeIntVarArray("X", 1, 1, 3, false);
        solver.post(ICF.scalar(X, new int[]{-1}, "<=", 2));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 3);

    }

    @Test(groups="1s", timeOut=60000)
    public void testB5() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] X = new IntVar[3];
        X[0] = solver.makeIntVar("X1", 6, 46, false);
        X[1] = solver.makeIntVar("X2", 6, 56, false);
        X[2] = solver.makeIntVar("X3", -1140, 1140, true);
        solver.post(ICF.scalar(X, new int[]{1, -1, -1}, "=", 0));
        solver.propagate();
        X[1].updateUpperBound(46, Cause.Null);
        solver.propagate();
        Assert.assertEquals(X[2].getLB(), -40);
        Assert.assertEquals(X[2].getUB(), 40);

    }


    @Test(groups="1s", timeOut=60000)
    public void testB6() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] X = new IntVar[2];
        X[0] = solver.makeIntVar("X1", 1, 3, false);
        X[1] = solver.makeIntVar("X2", 2, 5, false);
        solver.post(ICF.scalar(X, new int[]{2, 3}, "<=", 10));
        solver.propagate();
        Assert.assertEquals(X[0].getLB(), 1);
        Assert.assertEquals(X[0].getUB(), 2);
        Assert.assertEquals(X[1].getLB(), 2);
        Assert.assertEquals(X[1].getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB61() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] X = new IntVar[2];
        X[0] = solver.makeIntVar("X1", 1, 3, false);
        X[1] = solver.makeIntVar("X2", 2, 5, false);
        solver.post(ICF.scalar(X, new int[]{-2, -3}, ">=", -10));
        solver.propagate();
        Assert.assertEquals(X[0].getLB(), 1);
        Assert.assertEquals(X[0].getUB(), 2);
        Assert.assertEquals(X[1].getLB(), 2);
        Assert.assertEquals(X[1].getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB7() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] X = new IntVar[2];
        X[0] = solver.makeIntVar("X1", 0, 3, false);
        X[1] = solver.makeIntVar("X2", 1, 5, false);
        solver.post(ICF.scalar(X, new int[]{2, 3}, ">=", 10));
        solver.propagate();
        Assert.assertEquals(X[0].getLB(), 0);
        Assert.assertEquals(X[0].getUB(), 3);
        Assert.assertEquals(X[1].getLB(), 2);
        Assert.assertEquals(X[1].getUB(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB71() throws ContradictionException {
        Solver solver = new Solver();
        IntVar[] X = new IntVar[2];
        X[0] = solver.makeIntVar("X1", 0, 3, false);
        X[1] = solver.makeIntVar("X2", 1, 5, false);
        solver.post(ICF.scalar(X, new int[]{-2, -3}, ">=", -10));
        solver.propagate();
        Assert.assertEquals(X[0].getLB(), 0);
        Assert.assertEquals(X[0].getUB(), 3);
        Assert.assertEquals(X[1].getLB(), 1);
        Assert.assertEquals(X[1].getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL1(){
        Solver solver = new Solver();
        solver.post(ICF.sum(new IntVar[]{solver.makeIntVar(3), solver.makeIntVar(-4)}, "<", 0));
        Assert.assertTrue(solver.findSolution());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL2(){
        Solver solver = new Solver();
        solver.post(ICF.sum(new IntVar[]{solver.makeIntVar(3), solver.makeIntVar(-4)}, "<=", 0));
        Assert.assertTrue(solver.findSolution());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL3(){
        Solver solver = new Solver();
        solver.post(ICF.sum(new IntVar[]{solver.makeIntVar(-3), solver.makeIntVar(4)}, ">", 0));
        Assert.assertTrue(solver.findSolution());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL4(){
        Solver solver = new Solver();
        solver.post(ICF.sum(new IntVar[]{solver.makeIntVar(-3), solver.makeIntVar(4)}, ">=", 0));
        Assert.assertTrue(solver.findSolution());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG1(){
        Solver solver = new Solver("TestChoco 3.3.2 Briot");
        IntVar[] var = solver.makeIntVarArray("var", 3, new int[]{30, 60});
        solver.post(ICF.sum(new IntVar[] { var[0], var[1], var[2] }, ">=", 60));
        solver.set(ISF.lexico_LB(var));
        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        solver.findSolution();
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG2(){
        Solver solver = new Solver("TestChoco 3.3.2 Briot");
        IntVar[] var = solver.makeIntVarArray("var", 3, new int[]{30, 60});
        solver.post(ICF.sum(new IntVar[] { var[0], var[1], var[2] }, "<=", 120));
        solver.set(ISF.lexico_LB(var));
        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        solver.findSolution();
    }
}
