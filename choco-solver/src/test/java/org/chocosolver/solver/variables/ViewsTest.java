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
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.sum.PropScalar;
import org.chocosolver.solver.constraints.ternary.Max;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.constraints.Operator.EQ;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.*;
import static org.chocosolver.solver.search.strategy.SetStrategyFactory.force_first;
import static org.testng.Assert.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/08/11
 */
public class ViewsTest {

    public static void check(Model ref, Model model, long seed, boolean strict, boolean solveAll) {
//        SearchMonitorFactory.log(ref, true, true);
//        SearchMonitorFactory.log(solver, true, true);
        if (solveAll) {
            ref.solveAll();
            model.solveAll();
        } else {
//            System.out.printf("%s\n", ref.toString());
            ref.solve();
//            System.out.printf("%s\n", solver.toString());
            model.solve();
        }
        Assert.assertEquals(model.getMeasures().getSolutionCount(),
                ref.getMeasures().getSolutionCount(), "solutions (" + seed + ")");
//        System.out.printf("%d : %d vs. %d  -- ", seed, ref.getMeasures().getNodeCount(),
//                solver.getMeasures().getNodeCount());
        if (strict) {
            Assert.assertEquals(model.getMeasures().getNodeCount(), ref.getMeasures().getNodeCount(), "nodes (" + seed + ")");
        } else {
            Assert.assertTrue(ref.getMeasures().getNodeCount() >=
                    model.getMeasures().getNodeCount(), seed + "");
        }
//        System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
//                solver.getMeasures().getTimeCount(),
//                ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
    }


    @Test(groups="10s", timeOut=60000)
    public void test1() {
        // Z = X + Y
//        int seed = 5;
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar y = ref.intVar("y", 0, 2, false);
                IntVar z = ref.intVar("z", 0, 4, false);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, 1, -1}, 2, EQ, 0)).post();
                ref.set(random_value(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = model.intVar("Z", 0, 200, false);
                model.sum(new IntVar[]{x, y}, "=", z).post();
                model.set(random_value(new IntVar[]{x, y, z}, seed));

            }
            check(ref, model, seed, false, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test1a() {
        // Z = X + Y (bounded)
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", 0, 2, true);
                IntVar y = ref.intVar("y", 0, 2, true);
                IntVar z = ref.intVar("z", 0, 4, true);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, 1, -1}, 2, EQ, 0)).post();
                ref.set(random_bound(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = model.intVar("x", 0, 2, true);
                IntVar y = model.intVar("y", 0, 2, true);
                IntVar z = model.intVar("Z", 0, 200, false);
                model.sum(new IntVar[]{x, y}, "=", z).post();
                model.set(random_bound(new IntVar[]{x, y, z}, seed));

            }
            check(ref, model, seed, true, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testa() {
        // Z = max(X + Y)
        for (int seed = 0; seed < 9999; seed += 1) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar y = ref.intVar("y", 0, 2, false);
                IntVar z = ref.intVar("z", 0, 2, true);
                ref.max(z, x, y).post();
                ref.set(random_bound(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = Max.var(x, y);
                model.set(IntStrategyFactory.random_bound(new IntVar[]{x, y, z}, seed));

            }
            check(ref, model, seed, false, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test1b() {
        // Z = |X|
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", -2, 2, false);
                IntVar z = ref.intVar("z", 0, 2, false);

                ref.absolute(z, x).post();
                ref.set(random_value(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = model.intVar("x", -2, 2, false);
                IntVar z = model.intAbsView(x);
                model.set(IntStrategyFactory.random_bound(new IntVar[]{x, z}, seed));

            }
            check(ref, model, seed, true, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test1bb() {
        // Z = X + c
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", -2, 2, false);
                IntVar z = ref.intVar("z", -1, 3, false);

                ref.arithm(z, "=", x, "+", 1).post();
                ref.set(random_value(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = model.intVar("x", -2, 2, false);
                IntVar z = model.intOffsetView(x, 1);
                model.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));

            }
            check(ref, model, seed, true, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test1bbb() {
        // Z = X * c
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", -2, 2, false);
                IntVar z = ref.intVar("z", -4, 4, false);

                ref.times(x, ref.intVar(2), z).post();
                ref.set(random_value(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = model.intVar("x", -2, 2, false);
                IntVar z = model.intScaleView(x, 2);
                model.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));

            }
            check(ref, model, seed, false, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test1c() {
        // Z = -X
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar z = ref.intVar("z", -2, 0, false);

                ref.arithm(z, "+", x, "=", 0).post();
                ref.set(random_value(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar z = model.intMinusView(x);
                model.set(IntStrategyFactory.random_value(new IntVar[]{x, z}, seed));

            }
            check(ref, model, seed, true, true);
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void test1d() {
        // Z = X + Y + ...
        for (int seed = 2; seed < 9; seed += 1) {
            Model ref = new Model();
            Model model = new Model();
            int n = seed * 2;
            {
                IntVar[] x = ref.intVarArray("x", n, 0, 2, false);
                ref.sum(x, "=", n).post();
                ref.set(minDom_LB(x));
            }
            {
                IntVar[] x = model.intVarArray("x", n, 0, 2, false);
                IntVar[] y = new IntVar[seed];
                for (int i = 0; i < seed; i++) {
                    y[i] = model.intVar("Z", 0, 200, false);
                    model.sum(new IntVar[]{x[i], x[i + seed]}, "=", y[i]).post();
                }
                model.sum(y, "=", n).post();

                model.set(minDom_LB(x));

            }
            check(ref, model, seed, true, true);
        }
    }


    @Test(groups="1s", timeOut=60000)
    public void test1f() {
        // Z = MAX(X,Y)
        Model ref = new Model();
        Model model = new Model();
        {
            IntVar x = ref.intVar("x", 160, 187, false);
            IntVar y = ref.intVar("y", -999, 999, false);
            IntVar z = ref.intVar("z", -9999, 9999, false);
            ref.arithm(z, "+", x, "=", 180).post();
            ref.max(y, ref.intVar(0), z).post();
        }
        {
            IntVar x = model.intVar("x", 160, 187, false);
            IntVar y = model.intVar("y", -999, 999, false);
            IntVar z = model.intOffsetView(model.intMinusView(x), 180);
            model.max(y, model.intVar(0), z).post();

            check(ref, model, 0, false, true);
        }
    }


    @Test(groups="10s", timeOut=60000)
    public void test2() {
        // Z = X - Y
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar y = ref.intVar("y", 0, 2, false);
                IntVar z = ref.intVar("z", -2, 2, false);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 1, EQ, 0)).post();
//				System.out.println(cstr);
                ref.set(random_value(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = model.intVar("Z", -200, 200, false);
                Constraint cstr = model.sum(new IntVar[]{z, y}, "=", x);
                cstr.post();
//				System.out.println(cstr);
                model.set(random_value(new IntVar[]{x, y, z}, seed));

            }
            check(ref, model, seed, false, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test3() {
        // Z = |X - Y|
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar y = ref.intVar("y", 0, 2, false);
                IntVar z = ref.intVar("z", -2, 2, false);
                IntVar az = ref.intVar("az", 0, 2, false);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 1, EQ, 0)).post();
                ref.absolute(az, z).post();
                ref.set(random_bound(new IntVar[]{x, y, az}, seed));
            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = model.intVar("Z", -2, 2, false);
                IntVar az = model.intAbsView(z);
                model.sum(new IntVar[]{z, y}, "=", x).post();
                model.set(random_bound(new IntVar[]{x, y, az}, seed));
            }
            check(ref, model, seed, true, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test4() {
        // Z = |X - Y| + AllDiff
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar y = ref.intVar("y", 0, 2, false);
                IntVar z = ref.intVar("z", -2, 2, false);
                IntVar az = ref.intVar("az", 0, 2, false);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 1, EQ, 0)).post();
                ref.absolute(az, z).post();
                ref.allDifferent(new IntVar[]{x, y, az}, "BC").post();
                ref.set(random_bound(new IntVar[]{x, y, az}, seed));
            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = model.intVar("z", -2, 2, false);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 1, EQ, 0)).post();
                IntVar az = model.intAbsView(z);
                model.allDifferent(new IntVar[]{x, y, az}, "BC").post();
                model.set(random_bound(new IntVar[]{x, y, az}, seed));
            }
            check(ref, model, seed, true, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test5() {
        // ~all-interval series
        int k = 5;
        for (int seed = 0; seed < 99; seed++) {
            Model ref = new Model();
            Model model = new Model();
            {
                IntVar[] x = ref.intVarArray("x", k, 0, k - 1, false);
                IntVar[] y = ref.intVarArray("y", k - 1, -(k - 1), k - 1, false);
                IntVar[] t = ref.intVarArray("t", k - 1, 0, k - 1, false);
                for (int i = 0; i < k - 1; i++) {
                    new Constraint("SP", new PropScalar(new IntVar[]{x[i + 1], x[i], y[i]}, new int[]{1, -1, -1}, 1, EQ, 0)).post();
                    ref.absolute(t[i], y[i]).post();
                }
                ref.allDifferent(x, "BC").post();
                ref.allDifferent(t, "BC").post();
                ref.arithm(x[1], ">", x[0]).post();
                ref.arithm(t[0], ">", t[k - 2]).post();
                ref.set(random_value(x, seed));
            }
            {
                IntVar[] x = model.intVarArray("x", k, 0, k - 1, false);
                IntVar[] t = new IntVar[k - 1];
                for (int i = 0; i < k - 1; i++) {
                    IntVar z = model.intVar("Z", -200, 200, false);
                    new Constraint("SP", new PropScalar(new IntVar[]{x[i + 1], x[i], z}, new int[]{1, -1, -1}, 1, EQ, 0)).post();
                    t[i] = model.intAbsView(z);
                }
                model.allDifferent(x, "BC").post();
                model.allDifferent(t, "BC").post();
                model.arithm(x[1], ">", x[0]).post();
                model.arithm(t[0], ">", t[k - 2]).post();
                model.set(random_value(x, seed));
            }
            check(ref, model, k, true, true);
        }
    }


    @Test(groups="10s", timeOut=60000)
    public void test6() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 10, false);
        IntVar y = model.intAbsView(x);
        IntVar z = model.intAbsView(model.intAbsView(x));

        for (int j = 0; j < 200; j++) {
//            long t = -System.nanoTime();
            for (int i = 0; i < 999999; i++) {
                if (y.getLB() == x.getUB()) {
                    y.updateLowerBound(0, Cause.Null);
                }
            }
//            t += System.nanoTime();
//            System.out.printf("%.2fms vs. ", t / 1000 / 1000f);
//            t = -System.nanoTime();
            for (int i = 0; i < 999999; i++) {
                if (z.getLB() == x.getUB()) {
                    z.updateLowerBound(0, Cause.Null);
                }
            }
//            t += System.nanoTime();
//            System.out.printf("%.2fms\n", t / 1000 / 1000f);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL1() throws ContradictionException {
        Model s = new Model();
        IntVar v1 = s.intVar("v1", -2, 2, false);
        IntVar v2 = s.intMinusView(s.intMinusView(s.intVar("v2", -2, 2, false)));
        s.arithm(v1, "=", v2).post();
        s.arithm(v2, "!=", 1).post();

        s.propagate();

        assertFalse(v1.contains(1));
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL2() {
        Model model = new Model();
        SetVar v1 = model.setVar("{0,1}", new int[]{0, 1});
        SetVar v2 = model.setVar("v2", new int[]{}, new int[]{0, 1, 2, 3});
        model.subsetEq(new SetVar[]{v1, v2}).post();
        model.set(force_first(new SetVar[]{v1, v2}));
        model.solveAll();
        assertEquals(model.getMeasures().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL3() {
        Model model = new Model();
        model.arithm(
                model.intVar("int", -3, 3, false),
                "=",
                model.intMinusView(model.boolVar("bool"))).post();
        model.solveAll();
        assertEquals(model.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL4() throws ContradictionException {
        Model s = new Model();
        BoolVar bool = s.boolVar("bool");
        BoolVar view = s.boolEqView(bool);
        SetVar set = s.setVar("set", new int[]{}, new int[]{0, 1});
        s.setBoolsChanneling(new BoolVar[]{view, bool}, set, 0).post();
        s.member(s.ONE(), set).post();
        s.set(minDom_UB(bool));
        s.solveAll();
        assertEquals(s.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG() throws ContradictionException {
        Model s = new Model();
        BoolVar bool = s.boolVar("bool");
        BoolVar view = s.boolEqView(bool);
        IntVar sum = s.intVar("sum", 0, 6, true);
        s.scalar(new IntVar[]{view, bool}, new int[]{1, 5}, "=", sum).post();
        s.arithm(sum, ">", 2).post();
        s.propagate();
        assertEquals(sum.isInstantiated(), true);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG2() throws ContradictionException {
        Model s = new Model();
        BoolVar bool = s.boolVar("bool");
        BoolVar view = s.boolNotView(bool);
        IntVar sum = s.intVar("sum", 0, 6, true);
        s.scalar(new IntVar[]{view, bool}, new int[]{1, 5}, "=", sum).post();
        s.arithm(sum, ">", 2).post();
        s.propagate();
        assertEquals(sum.isInstantiated(), true);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG3() throws ContradictionException {
        Model s = new Model();
        IntVar var = s.intVar("int", 0, 2, true);
        IntVar view = s.intEqView(var);
        IntVar sum = s.intVar("sum", 0, 6, true);
        s.scalar(new IntVar[]{view, var}, new int[]{1, 5}, "=", sum).post();
        s.arithm(sum, ">", 2).post();
        s.propagate();
        assertEquals(sum.isInstantiated(), true);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG4() throws ContradictionException {
        Model s = new Model();
        IntVar var = s.intVar("int", 0, 2, true);
        IntVar view = s.intMinusView(var);
        IntVar sum = s.intVar("sum", 0, 6, true);
        s.scalar(new IntVar[]{view, var}, new int[]{1, 5}, "=", sum).post();
        s.arithm(sum, ">", 2).post();
        s.propagate();
        assertEquals(sum.isInstantiated(), true);
    }

    @Test(groups="1s", timeOut=60000)
    public void testvanH() {
        Model model = new Model();
        BoolVar x1 = model.boolVar("x1");
        BoolVar x2 = model.boolNotView(x1);
        BoolVar x3 = model.boolVar("x3");
        IntVar[] av = new IntVar[]{x1, x2, x3};
        int[] coef = new int[]{5, 3, 2};
        model.scalar(av, coef, ">=", 7).post();
        try {
            model.propagate();
        } catch (Exception ignored) {
        }
        assertTrue(x3.isInstantiated());
        assertEquals(x3.getValue(), 1);
    }
}
