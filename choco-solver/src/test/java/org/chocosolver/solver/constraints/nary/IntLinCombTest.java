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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.cnf.PropTrue;
import org.chocosolver.solver.constraints.nary.sum.PropScalar;
import org.chocosolver.solver.constraints.nary.sum.PropSum;
import org.chocosolver.solver.constraints.nary.sum.PropSumBool;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static java.util.Arrays.fill;
import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.firstLBSearch;
import static org.chocosolver.solver.trace.Chatterbox.*;
import static org.testng.Assert.*;

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
        Model s = new Model();
        IntVar[] vars = new IntVar[n];
        int[] coeffs = new int[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, min, max, false);
            coeffs[i] = random.nextInt(cMax);
        }
        int constant = -random.nextInt(cMax);

        IntVar sum = s.intVar("scal", -99999999, 99999999, true);


        s.scalar(vars, coeffs, "=", sum).post();
        s.arithm(sum, operatorToString(operator), constant).post();

        s.getResolver().set(firstLBSearch(vars));

        while (s.solve()) ;
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


    protected Model sum(int[][] domains, int[] coeffs, int b, int op) {
        Model model = new Model();
        IntVar[] bins = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            bins[i] = model.intVar("v_" + i, domains[i][0], domains[i][domains[i].length - 1], true);
        }
        String opname = "=";
        if (op != 0) {
            if (op > 0) {
                opname = ">=";
            } else {
                opname = "<=";
            }
        }
        IntVar sum = model.intVar("scal", -99999999, 99999999, true);
        model.scalar(bins, coeffs, "=", sum).post();
        model.arithm(sum, opname, b).post();
        model.getResolver().set(firstLBSearch(bins));
        return model;
    }

    protected Model intlincomb(int[][] domains, int[] coeffs, int b, int op) {
        Model model = new Model();
        IntVar[] bins = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            bins[i] = model.intVar("v_" + i, domains[i][0], domains[i][domains[i].length - 1], true);
        }
        String opname = "=";
        if (op != 0) {
            if (op > 0) {
                opname = ">=";
            } else {
                opname = "<=";
            }
        }
        IntVar sum = model.intVar("scal", -99999999, 99999999, true);
        model.scalar(bins, coeffs, "=", sum).post();
        model.arithm(sum, opname, b).post();
        model.getResolver().set(firstLBSearch(bins));
        return model;
    }

    @Test(groups="5m", timeOut=300000)
    public void testSumvsIntLinCombTest() {
        Random rand = new Random();
        for (int seed = 0; seed < 400; seed++) {
            rand.setSeed(seed);
            int n = 1 + rand.nextInt(6);
            int min = -10 + rand.nextInt(20);
            int max = min + rand.nextInt(20);
            int[][] domains = buildFullDomains(n, min, max, rand, 1.0, false);
            int[] coeffs = new int[n];
            for (int i = 0; i < n; i++) {
                coeffs[i] = -25 + rand.nextInt(50);
            }
            int lb = -50 + rand.nextInt(100);
            int op = -1 + rand.nextInt(3);

            Model sum = sum(domains, coeffs, lb, op);
            Model intlincomb = intlincomb(domains, coeffs, lb, op);

            while (sum.solve()) ;
            while (intlincomb.solve()) ;
            assertEquals(sum.getResolver().getMeasures().getSolutionCount(), intlincomb.getResolver().getMeasures().getSolutionCount());
            assertEquals(sum.getResolver().getMeasures().getNodeCount(), intlincomb.getResolver().getMeasures().getNodeCount());
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testUSum1() {
        Model sumleq = sum(new int[][]{{-2, 3}}, new int[]{-2}, -6, -1);
        while (sumleq.solve()) ;
    }

    /**
     * Default propagation test:
     * When an opposite var is declared, the lower (resp. upper) bound modification
     * should be transposed in upper (resp. lower) bound event...
     */
    @Test(groups="1s", timeOut=60000)
    public void testUSum2() throws ContradictionException {
        Model sum = sum(new int[][]{{-2, 7}, {-1, 6}, {2}, {-2, 5}, {-2, 4}, {-2, 6}}, new int[]{-7, 13, -3, -18, -24, 1}, 30, 0);
        PropagationEngineFactory.DEFAULT.make(sum);
        Variable[] vars = sum.getVars();
        ((IntVar) vars[0]).instantiateTo(-2, Cause.Null);
        ((IntVar) vars[1]).instantiateTo(-1, Cause.Null);
        sum.getResolver().propagate();
//        sum.getResolver().timeStamp++;
        ((IntVar) vars[2]).removeValue(-2, Cause.Null);
        sum.getResolver().propagate();
        Assert.assertTrue(vars[2].isInstantiated());
    }

    @Test(groups="1s", timeOut=60000)
    public void testIss237_1() {
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("b", 3);
        model.scalar(bs, new int[]{1, 2, 3}, "=", 2).post();
        showSolutions(model);
        while (model.solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testS1_coeff_null() {
        Model model = new Model();
        model.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        IntVar[] ivars = model.intVarArray("V", 4, 0, 5, false);
        int[] coeffs = new int[]{1, 0, 0, 2};
        IntVar res = model.intVar("R", 0, 10, false);
        Constraint c = model.scalar(ivars, coeffs, "=", res);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropScalar);
        Assert.assertEquals(3, p.getNbVars());
    }

    @Test(groups="1s", timeOut=60000)
    public void testS2_coeff_null() {
        Model model = new Model();
        model.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        IntVar[] ivars = model.intVarArray("V", 4, 0, 5, false);
        ivars[2] = ivars[1];
        int[] coeffs = new int[]{1, 1, -1, 2};
        IntVar res = model.intVar("R", 0, 10, false);
        Constraint c = model.scalar(ivars, coeffs, "=", res);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropScalar);
        Assert.assertEquals(3, p.getNbVars());
    }

    @Test(groups="1s", timeOut=60000)
    public void testD1() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 4, 0, 5, false);
        int[] coeffs = new int[]{1, 1, 1, 1};
        IntVar res = model.intVar("R", 0, 10, false);
        Constraint c = model.scalar(ivars, coeffs, "=", res);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSum);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD2() {
        Model model = new Model();
        IntVar[] ivars = model.boolVarArray("V", 4);
        int[] coeffs = new int[]{1, 1, 1, 1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD3() {
        Model model = new Model();
        IntVar[] ivars = model.boolVarArray("V", 4);
        int[] coeffs = new int[]{-1, -1, -1, -1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD4() {
        Model model = new Model();
        IntVar[] ivars = model.boolVarArray("V", 4);
        int[] coeffs = new int[]{1, -1, 1, 1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD5() {
        Model model = new Model();
        IntVar[] ivars = model.boolVarArray("V", 4);
        int[] coeffs = new int[]{-1, 1, -1, -1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD6() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 4, 0, 1, false);
        ivars[1] = model.intVar("X", 0, 2, false);
        int[] coeffs = new int[]{1, -1, 1, 1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD7() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 4, 0, 1, false);
        ivars[1] = model.intVar("X", 0, 2, false);
        int[] coeffs = new int[]{-1, 1, -1, -1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSum);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD8() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 4, 0, 1, false);
        ivars[2] = model.intVar("X", 0, 2, false);
        int[] coeffs = new int[]{1, -1, 1, 1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSum);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD9() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 4, 0, 1, false);
        ivars[2] = model.intVar("X", 0, 2, false);
        int[] coeffs = new int[]{-1, 1, -1, -1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropSumBool);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD10() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 2, 0, 2, false);
        int[] coeffs = new int[]{1, 1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD11() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 2, 0, 2, false);
        int[] coeffs = new int[]{1, -1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD12() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 2, 0, 2, false);
        int[] coeffs = new int[]{-1, 1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD13() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 2, 0, 2, false);
        int[] coeffs = new int[]{-1, -1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD14() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 1, 0, 2, false);
        int[] coeffs = new int[]{1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD15() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 1, 0, 2, false);
        int[] coeffs = new int[]{-1};
        Constraint c = model.scalar(ivars, coeffs, "=", 0);
        Assert.assertTrue(c instanceof Arithmetic);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD16() {
        Model model = new Model();
        IntVar[] ivars = model.intVarArray("V", 1, 0, 2, false);
        int[] coeffs = new int[]{1};
        Constraint c = model.scalar(ivars, coeffs, "=", ivars[0]);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropTrue);
    }

    @Test(groups="1s", timeOut=60000)
    public void testD20() {
        Model model = new Model();
        model.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        IntVar[] ivars = model.intVarArray("V", 4, 0, 5, false);
        int[] coeffs = new int[]{1, 2, 2, 1};
        IntVar res = model.intVar("R", 0, 10, false);
        Constraint c = model.scalar(ivars, coeffs, "=", res);
        Assert.assertEquals(c.getPropagators().length, 1);
        Propagator p = c.getPropagator(0);
        Assert.assertTrue(p instanceof PropScalar);
    }

    @Test(groups="1s", timeOut=60000)
    public void testExt1() {
        Model s1 = new Model();
        s1.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        {
            BoolVar[] bs = s1.boolVarArray("b", 5);
            s1.sum(bs, "!=", 3).post();
        }
        Model s2 = new Model();
        s2.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 1000;
            }
        });
        {
            BoolVar[] bs = s2.boolVarArray("b", 5);
            s2.sum(bs, "!=", 3).post();
        }
        while (s1.solve()) ;
        while (s2.solve()) ;
        assertEquals(s2.getResolver().getMeasures().getSolutionCount(), s1.getResolver().getMeasures().getSolutionCount());
        assertEquals(s2.getResolver().getMeasures().getNodeCount(), s1.getResolver().getMeasures().getNodeCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testExt2() {
        Model s1 = new Model();
        s1.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        {
            BoolVar[] bs = s1.boolVarArray("b", 5);
            s1.sum(bs, "<=", 3).post();
        }
        Model s2 = new Model();
        s2.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 1000;
            }
        });
        {
            BoolVar[] bs = s2.boolVarArray("b", 5);
            s2.sum(bs, "<=", 3).post();
        }
        while (s1.solve()) ;
        while (s2.solve()) ;
        assertEquals(s2.getResolver().getMeasures().getSolutionCount(), s1.getResolver().getMeasures().getSolutionCount());
        assertEquals(s2.getResolver().getMeasures().getNodeCount(), s1.getResolver().getMeasures().getNodeCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testExt3() {
        Model s1 = new Model();
        s1.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 0;
            }
        });
        {
            BoolVar[] bs = s1.boolVarArray("b", 3);
            BoolVar r = s1.boolVar("r");
            s1.scalar(bs, new int[]{-1, -1, -1}, "<=", -2).reifyWith(r);
        }
        Model s2 = new Model();
        s2.set(new Settings() {
            @Override
            public int getMaxTupleSizeForSubstitution() {
                return 1000;
            }
        });
        {
            BoolVar[] bs = s2.boolVarArray("b", 3);
            BoolVar r = s2.boolVar("r");
            s2.scalar(bs, new int[]{-1, -1, -1}, "<=", -2).reifyWith(r);
        }
        showDecisions(s1);
        showDecisions(s2);
        while (s1.solve()) ;
        while (s2.solve()) ;
        assertEquals(s2.getResolver().getMeasures().getSolutionCount(), s1.getResolver().getMeasures().getSolutionCount());
        assertEquals(s2.getResolver().getMeasures().getNodeCount(), s1.getResolver().getMeasures().getNodeCount());
    }

    @Test(groups="5m", timeOut=300000)
    public void testB1() {
        Model model = new Model();
        model.set(new Settings() {
            @Override
            public short[] getCoarseEventPriority() {
                return new short[]{0, 0, 0, 0, 1, 2, 3};
            }
        });
        int n = 23;
        BoolVar[] bs = model.boolVarArray("b", n);
        int[] cs = new int[n];
        int k = (int) (n * .7);
        fill(cs, 0, n, 1);
        fill(cs, k, n, -1);
        IntVar sum = model.intVar("S", -n / 2, n / 2, true);
        model.scalar(bs, cs, "=", sum).post();
        model.getResolver().set(firstLBSearch(bs));
//        Chatterbox.showDecisions(solver);
        while (model.solve()) ;
    }


    @Test(groups="1s", timeOut=60000)
    public void testB2() throws ContradictionException {
        Model model = new Model();
        int n = 3;
        BoolVar[] bs = model.boolVarArray("b", n);
        int[] cs = new int[n];
        fill(cs, 0, n, -1);
        model.scalar(bs, cs, "<=", -2).post();
        model.getResolver().propagate();
        bs[2].setToFalse(Null);
        bs[0].setToTrue(Null);
        model.getResolver().propagate();
        assertTrue(bs[1].isInstantiatedTo(1));
    }


    @Test(groups="1s", timeOut=60000)
    public void testB3() {
        Model model = new Model();
        model.scalar(new IntVar[]{model.intVar(1), model.intVar(3)}, new int[]{1, -1}, "!=", 0).post();
        try {
            model.getResolver().propagate();
        } catch (ContradictionException e) {
            fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testB4() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 1, 1, 3, false);
        model.scalar(X, new int[]{-1}, "<=", 2).post();
        while (model.solve()) ;
        assertEquals(model.getResolver().getMeasures().getSolutionCount(), 3);

    }

    @Test(groups="1s", timeOut=60000)
    public void testB5() throws ContradictionException {
        Model model = new Model();
        IntVar[] X = new IntVar[3];
        X[0] = model.intVar("X1", 6, 46, false);
        X[1] = model.intVar("X2", 6, 56, false);
        X[2] = model.intVar("X3", -1140, 1140, true);
        model.scalar(X, new int[]{1, -1, -1}, "=", 0).post();
        model.getResolver().propagate();
        X[1].updateUpperBound(46, Null);
        model.getResolver().propagate();
        assertEquals(X[2].getLB(), -40);
        assertEquals(X[2].getUB(), 40);

    }


    @Test(groups="1s", timeOut=60000)
    public void testB6() throws ContradictionException {
        Model model = new Model();
        IntVar[] X = new IntVar[2];
        X[0] = model.intVar("X1", 1, 3, false);
        X[1] = model.intVar("X2", 2, 5, false);
        model.scalar(X, new int[]{2, 3}, "<=", 10).post();
        model.getResolver().propagate();
        assertEquals(X[0].getLB(), 1);
        assertEquals(X[0].getUB(), 2);
        assertEquals(X[1].getLB(), 2);
        assertEquals(X[1].getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB61() throws ContradictionException {
        Model model = new Model();
        IntVar[] X = new IntVar[2];
        X[0] = model.intVar("X1", 1, 3, false);
        X[1] = model.intVar("X2", 2, 5, false);
        model.scalar(X, new int[]{-2, -3}, ">=", -10).post();
        model.getResolver().propagate();
        assertEquals(X[0].getLB(), 1);
        assertEquals(X[0].getUB(), 2);
        assertEquals(X[1].getLB(), 2);
        assertEquals(X[1].getUB(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB7() throws ContradictionException {
        Model model = new Model();
        IntVar[] X = new IntVar[2];
        X[0] = model.intVar("X1", 0, 3, false);
        X[1] = model.intVar("X2", 1, 5, false);
        model.scalar(X, new int[]{2, 3}, ">=", 10).post();
        model.getResolver().propagate();
        assertEquals(X[0].getLB(), 0);
        assertEquals(X[0].getUB(), 3);
        assertEquals(X[1].getLB(), 2);
        assertEquals(X[1].getUB(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testB71() throws ContradictionException {
        Model model = new Model();
        IntVar[] X = new IntVar[2];
        X[0] = model.intVar("X1", 0, 3, false);
        X[1] = model.intVar("X2", 1, 5, false);
        model.scalar(X, new int[]{-2, -3}, ">=", -10).post();
        model.getResolver().propagate();
        assertEquals(X[0].getLB(), 0);
        assertEquals(X[0].getUB(), 3);
        assertEquals(X[1].getLB(), 1);
        assertEquals(X[1].getUB(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL1() {
        Model model = new Model();
        model.sum(new IntVar[]{model.intVar(3), model.intVar(-4)}, "<", 0).post();
        assertTrue(model.solve());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL2() {
        Model model = new Model();
        model.sum(new IntVar[]{model.intVar(3), model.intVar(-4)}, "<=", 0).post();
        assertTrue(model.solve());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL3() {
        Model model = new Model();
        model.sum(new IntVar[]{model.intVar(-3), model.intVar(4)}, ">", 0).post();
        assertTrue(model.solve());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL4() {
        Model model = new Model();
        model.sum(new IntVar[]{model.intVar(-3), model.intVar(4)}, ">=", 0).post();
        assertTrue(model.solve());
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG1() {
        Model model = new Model("TestChoco 3.3.2 Briot");
        IntVar[] var = model.intVarArray("var", 3, new int[]{30, 60});
        model.sum(new IntVar[]{var[0], var[1], var[2]}, ">=", 60).post();
        model.getResolver().set(firstLBSearch(var));
        showStatistics(model);
        showSolutions(model);
        model.solve();
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG2() {
        Model model = new Model("TestChoco 3.3.2 Briot");
        IntVar[] var = model.intVarArray("var", 3, new int[]{30, 60});
        model.sum(new IntVar[]{var[0], var[1], var[2]}, "<=", 120).post();
        model.getResolver().set(firstLBSearch(var));
        showStatistics(model);
        showSolutions(model);
        model.solve();
    }
}
