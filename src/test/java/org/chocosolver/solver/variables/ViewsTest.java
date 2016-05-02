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

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.sum.PropScalar;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.SearchStrategyFactory;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.solver.constraints.Operator.EQ;
import static org.chocosolver.solver.constraints.ternary.Max.var;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;
import static org.chocosolver.solver.search.strategy.selectors.ValSelectorFactory.randomIntBound;
import static org.chocosolver.solver.search.strategy.selectors.VarSelectorFactory.randomVar;
import static org.testng.Assert.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/08/11
 */
public class ViewsTest {

    public static void check(Model ref, Model model, long seed, boolean strict, boolean solveAll) {
        if (solveAll) {
            while (ref.solve()) ;
            while (model.solve()) ;
        } else {
            ref.solve();
            model.solve();
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(),
                ref.getSolver().getSolutionCount(), "solutions (" + seed + ")");
        if (strict) {
            Assert.assertEquals(model.getSolver().getNodeCount(), ref.getSolver().getNodeCount(), "nodes (" + seed + ")");
        } else {
            Assert.assertTrue(ref.getSolver().getNodeCount() >=
                    model.getSolver().getNodeCount(), seed + "");
        }
    }


    @Test(groups="10s", timeOut=60000)
    public void test1() {
        // Z = X + Y
//        int seed = 5;
        for (int seed = 0; seed < 9999; seed++) {
            Model ref = new Model();
            Model model = new Model();
            model.set(new Settings() {
                @Override
                public boolean enableACOnTernarySum() {
                    return true;
                }
            });
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar y = ref.intVar("y", 0, 2, false);
                IntVar z = ref.intVar("z", 0, 4, false);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, 1, -1}, 2, EQ, 0)).post();
                ref.getSolver().set(randomSearch(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = model.intVar("Z", 0, 200, false);
                model.sum(new IntVar[]{x, y}, "=", z).post();
                model.getSolver().set(randomSearch(new IntVar[]{x, y, z}, seed));

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
                ref.getSolver().set(randomSearch(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = model.intVar("x", 0, 2, true);
                IntVar y = model.intVar("y", 0, 2, true);
                IntVar z = model.intVar("Z", 0, 200, false);
                model.sum(new IntVar[]{x, y}, "=", z).post();
                model.getSolver().set(randomSearch(new IntVar[]{x, y, z}, seed));

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
                ref.getSolver().set(randomSearch(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = var(x, y);
                model.getSolver().set(randomSearch(new IntVar[]{x, y, z}, seed));

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
                ref.getSolver().set(randomSearch(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = model.intVar("x", -2, 2, false);
                IntVar z = model.intAbsView(x);
                model.getSolver().set(randomSearch(new IntVar[]{x, z}, seed));

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
                ref.getSolver().set(randomSearch(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = model.intVar("x", -2, 2, false);
                IntVar z = model.intOffsetView(x, 1);
                Solver r = model.getSolver();
                r.set(randomSearch(new IntVar[]{x, z}, seed));

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
                ref.getSolver().set(randomSearch(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = model.intVar("x", -2, 2, false);
                IntVar z = model.intScaleView(x, 2);
                Solver r = model.getSolver();
                r.set(randomSearch(new IntVar[]{x, z}, seed));

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
                ref.getSolver().set(randomSearch(new IntVar[]{x, z}, seed));
            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar z = model.intMinusView(x);
                Solver r = model.getSolver();
                r.set(randomSearch(new IntVar[]{x, z}, seed));
            }
            check(ref, model, seed, true, true);
        }
    }


    @DataProvider(name = "1d")
    public Object[][] data1D(){
        List<Object[]> elt = new ArrayList<>();
        for (int seed = 2; seed < 9; seed += 1) {
            elt.add(new Object[]{seed});
        }
        return elt.toArray(new Object[elt.size()][1]);
    }

    @Test(groups="5m", timeOut=300000, dataProvider = "1d")
    public void test1d(int seed) {
        // Z = X + Y + ...
        Model ref = new Model();
        Model model = new Model();
        int n = seed * 2;
        {
            IntVar[] x = ref.intVarArray("x", n, 0, 2, false);
            ref.sum(x, "=", n).post();
            ref.getSolver().set(minDomLBSearch(x));
        }
        {
            IntVar[] x = model.intVarArray("x", n, 0, 2, false);
            IntVar[] y = new IntVar[seed];
            for (int i = 0; i < seed; i++) {
                y[i] = model.intVar("Z", 0, 200, false);
                model.sum(new IntVar[]{x[i], x[i + seed]}, "=", y[i]).post();
            }
            model.sum(y, "=", n).post();

            model.getSolver().set(minDomLBSearch(x));

        }
        check(ref, model, seed, true, true);
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
            model.set(new Settings() {
                @Override
                public boolean enableACOnTernarySum() {
                    return true;
                }
            });
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar y = ref.intVar("y", 0, 2, false);
                IntVar z = ref.intVar("z", -2, 2, false);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 1, EQ, 0)).post();
//				System.out.println(cstr);
                ref.getSolver().set(randomSearch(new IntVar[]{x, y, z}, seed));

            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = model.intVar("Z", -200, 200, false);
                Constraint cstr = model.sum(new IntVar[]{z, y}, "=", x);
                cstr.post();
//				System.out.println(cstr);
                model.getSolver().set(randomSearch(new IntVar[]{x, y, z}, seed));

            }
            check(ref, model, seed, false, true);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testTernArithmBC() {
        // note did not pass because PropXplusYeqZ did not reach a fix point
        Model model = new Model();
        model.set(new Settings() {
            @Override
            public boolean enableACOnTernarySum() {
                return true;
            }
        });
        IntVar x = model.intVar("x", 0, 2, false);
        IntVar y = model.intVar("y", 0, 2, false);
        IntVar z = model.intVar("Z", -2, 2, false);
        IntVar absZ = model.intVar("|Z|", 0, 2, false);
        model.absolute(absZ,z).post();
        System.out.println(model.arithm(x,"-",y, "=", z));
        model.arithm(x,"-",y, "=", z).post(); // test passes if added twice
        model.arithm(absZ,"=",1).post();
        model.arithm(y,"=",0).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        System.out.println("x - y = z");
        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
        Assert.assertTrue(x.isInstantiatedTo(1));
    }

    @Test(groups="1s", timeOut=60000)
    public void testTernArithmAC() {
        // note did not pass because PropXplusYeqZ did not reach a fix point
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 2, false);
        IntVar y = model.intVar("y", 0, 2, false);
        IntVar z = model.intVar("Z", -2, 2, false);
        IntVar absZ = model.intVar("|Z|", 0, 2, false);
        model.absolute(absZ,z).post();
        System.out.println(model.arithm(x,"-",y, "=", z));
        model.arithm(x,"-",y, "=", z).post(); // test passes if added twice
        model.arithm(absZ,"=",1).post();
        model.arithm(y,"=",0).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        System.out.println("x - y = z");
        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
        Assert.assertTrue(x.isInstantiatedTo(1));
    }

    @Test(groups="10s", timeOut=60000)
    public void test3() {
        // Z = |X - Y|
        for (int seed = 0; seed < 999; seed++) {
            Model ref = new Model();
            {
                IntVar x = ref.intVar("x", 0, 2, false);
                IntVar y = ref.intVar("y", 0, 2, false);
                IntVar z = ref.intVar("z", -2, 2, false);
                ref.arithm(x,"-",y, "=", z).post();
                IntVar az = ref.intVar("az", 0, 2, false);
                ref.absolute(az, z).post();
                ref.getSolver().set(intVarSearch(randomVar(seed), randomIntBound(seed),x, y, az));
            }
            Model model = new Model();
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = model.intVar("Z", -2, 2, false);
                model.arithm(x,"-",y, "=", z).post();
                IntVar az = model.intAbsView(z);
                model.getSolver().set(intVarSearch(randomVar(seed), randomIntBound(seed),x, y, az));
            }
            check(ref, model, seed, true, true);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void test4() {
        // Z = |X - Y| + AllDiff
        for (int seed = 0; seed < 999; seed++) {
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
                ref.getSolver().set(randomSearch(new IntVar[]{x, y, az}, seed));
            }
            {
                IntVar x = model.intVar("x", 0, 2, false);
                IntVar y = model.intVar("y", 0, 2, false);
                IntVar z = model.intVar("z", -2, 2, false);
                new Constraint("SP", new PropScalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 1, EQ, 0)).post();
                IntVar az = model.intAbsView(z);
                model.allDifferent(new IntVar[]{x, y, az}, "BC").post();
                model.getSolver().set(randomSearch(new IntVar[]{x, y, az}, seed));
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
                ref.getSolver().set(randomSearch(x, seed));
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
                model.getSolver().set(randomSearch(x, seed));
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

        s.getSolver().propagate();

        assertFalse(v1.contains(1));
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL2() {
        Model model = new Model();
        SetVar v1 = model.setVar("{0,1}", new int[]{0, 1});
        SetVar v2 = model.setVar("v2", new int[]{}, new int[]{0, 1, 2, 3});
        model.subsetEq(new SetVar[]{v1, v2}).post();
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 4);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL3() {
        Model model = new Model();
        model.arithm(
                model.intVar("int", -3, 3, false),
                "=",
                model.intMinusView(model.boolVar("bool"))).post();
        while (model.solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL4() throws ContradictionException {
        Model s = new Model();
        BoolVar bool = s.boolVar("bool");
        SetVar set = s.setVar("set", new int[]{}, new int[]{0, 1});
        // 17/03/16 : seems not idempotent when multiple occurrence of same var
        // possible fix : split propagator in two ways
        s.setBoolsChanneling(new BoolVar[]{bool, bool}, set, 0).post();
        s.member(s.ONE(), set).post();
        Solver r = s.getSolver();
        r.set(minDomUBSearch(bool));
        while (s.solve()) ;
        assertEquals(s.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG() throws ContradictionException {
        Model s = new Model();
        BoolVar bool = s.boolVar("bool");
        BoolVar view = bool;
        IntVar sum = s.intVar("sum", 0, 6, true);
        s.scalar(new IntVar[]{view, bool}, new int[]{1, 5}, "=", sum).post();
        s.arithm(sum, ">", 2).post();
        s.getSolver().propagate();
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
        s.getSolver().propagate();
        assertEquals(sum.isInstantiated(), true);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJG3() throws ContradictionException {
        Model s = new Model();
        IntVar var = s.intVar("int", 0, 2, true);
        IntVar view = var;
        IntVar sum = s.intVar("sum", 0, 6, true);
        s.scalar(new IntVar[]{view, var}, new int[]{1, 5}, "=", sum).post();
        s.arithm(sum, ">", 2).post();
        s.getSolver().propagate();
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
        s.getSolver().propagate();
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
            model.getSolver().propagate();
        } catch (Exception ignored) {
        }
        assertTrue(x3.isInstantiated());
        assertEquals(x3.getValue(), 1);
    }

    @Test(groups="10s", timeOut=60000)
    public void testScale(){
        int n = 9;
        Model viewModel = makeModel(true);
        scale(viewModel,n);
        Model noViewModel = makeModel(false);
        scale(noViewModel,n);
        testModels(viewModel,noViewModel);
    }

    @Test(groups="10s", timeOut=60000)
    public void testOffset(){
        int n = 9;
        Model viewModel = makeModel(true);
        offset(viewModel,n);
        Model noViewModel = makeModel(false);
        offset(noViewModel,n);
        testModels(viewModel,noViewModel);
    }

    @Test(groups="10s", timeOut=60000)
    public void testMinus(){
        int n = 9;
        Model viewModel = makeModel(true);
        minus(viewModel,n);
        Model noViewModel = makeModel(false);
        minus(noViewModel,n);
        testModels(viewModel,noViewModel);
    }

    @Test(groups="10s", timeOut=60000)
    public void testBoolNot(){
        int n = 23;
        Model viewModel = makeModel(true);
        boolNot(viewModel,n);
        Model noViewModel = makeModel(false);
        boolNot(noViewModel,n);
        testModels(viewModel,noViewModel);
    }

    @Test(groups = "10s", timeOut=60000)
    public void testBoolNotNot() {
        int n = 20;
        Model viewModel = makeModel(true);
        boolNotNot(viewModel, n);
        Model noViewModel = makeModel(false);
        boolNotNot(noViewModel, n);
        testModels(viewModel, noViewModel);
    }

    private static Model makeModel(final boolean withViews){
        Model m = new Model("with"+(withViews?"":"out")+" views");
        m.set(new Settings() {
            @Override
            public boolean enableViews() {
                return withViews;
            }
        });
        return m;
    }

    private static void offset(Model model, int n){
        IntVar[] x = model.intVarArray(n,0,n-1);
        IntVar[] y = new IntVar[n];
        for(int i=0;i<n;i++){
            y[i] = model.intOffsetView(x[i],42);
        }
        checkDomains(true, x, y);

        model.allDifferent(x).post();
        model.sum(y, "<", n*2).post();
        model.getSolver().set(SearchStrategyFactory.randomSearch(y,0));
    }

    private static void scale(Model model, int n){
        IntVar[] x = model.intVarArray(n,0,n-1);
        IntVar[] y = new IntVar[n];
        for(int i=0;i<n;i++){
            y[i] = model.intScaleView(x[i],42);
        }
        checkDomains(false, x, y);

        model.allDifferent(x).post();
        model.sum(y, "<", n*2).post();
        model.getSolver().set(SearchStrategyFactory.randomSearch(y,0));
    }

    private static void minus(Model model, int n){
        IntVar[] x = model.intVarArray(n,0,n-1);
        IntVar[] y = new IntVar[n];
        for(int i=0;i<n;i++){
            y[i] = model.intMinusView(x[i]);
        }
        checkDomains(true, x, y);

        model.allDifferent(x).post();
        model.sum(y, "<", n*2).post();
        model.getSolver().set(SearchStrategyFactory.randomSearch(y,0));
    }

    private static void boolNot(Model model, int n){
        BoolVar[] x = model.boolVarArray(n);
        BoolVar[] y = new BoolVar[n];
        for(int i=0;i<n;i++){
            y[i] = model.boolNotView(x[i]);
        }
        checkDomains(true, x, y);

        model.sum(x,"=",n/2).post();
        model.getSolver().set(SearchStrategyFactory.randomSearch(y,0));
    }

    public static void boolNotNot(Model model, int n) {
        BoolVar[] x = model.boolVarArray(n);
        BoolVar[] y = new BoolVar[n];
        for (int i = 0; i < x.length; i++) {
            y[i] = model.boolNotView(x[i]);
        }
        BoolVar[] z = new BoolVar[n];
        for (int i = 0; i < y.length; i++) {
            z[i] = model.boolNotView(y[i]);
            Assert.assertTrue(z[i]==x[i]);
        }
        checkDomains(true, x, y, z);

        model.sum(x, "=", n/2).post();
        model.getSolver().set(SearchStrategyFactory.randomSearch(z, 0));
    }

    private static <T extends IntVar> void checkDomains(boolean noHoles, T[] ... vars) {
        assert vars.length > 0;

        for (T[] varArray : vars) {
            for (T var : varArray) {
                // Not in the domain
                int prev = -1;

                DisposableValueIterator it = var.getValueIterator(true);
                for(int i = var.getLB(); i != Integer.MAX_VALUE; i = var.nextValue(i)) {
                    assertTrue(it.hasNext());
                    if(prev != -1) {
                        if(noHoles) {
                            assertEquals(var.nextValueOut(i), var.getUB()+1);
                            assertEquals(var.previousValueOut(i), var.getLB()-1);
                        }
                        assertTrue(it.hasPrevious());
//                        assertEquals(it.previous(), prev);
                        assertEquals(var.previousValue(i), prev);
                    }
                    prev = i;
                    assertEquals(it.next(), i);
                }

            }
        }

    }

    private static void testModels(Model... models) {
        IntVar[][] vars = new IntVar[models.length][];
        for(int i=0;i<models.length;i++){
            Assert.assertEquals(models[0].getResolutionPolicy(),models[i].getResolutionPolicy());
            vars[i] = models[i].retrieveIntVars(true);
            Assert.assertEquals(vars[i].length,vars[0].length);
        }
        long t;
        long[] time = new long[models.length];
        boolean bc;
        int nbSols=0;
        do {
            t = System.currentTimeMillis();
            bc = models[0].solve();
            time[0] += System.currentTimeMillis() - t;
            if(bc) nbSols++;
            for(int k=1;k<models.length;k++) {
                t = System.currentTimeMillis();
                Assert.assertEquals(bc, models[k].solve());
                time[k] += System.currentTimeMillis() - t;
                Assert.assertEquals(
                        models[k].getSolver().getBackTrackCount(),
                        models[0].getSolver().getBackTrackCount());
                Assert.assertEquals(
                        models[k].getSolver().getCurrentDepth(),
                        models[0].getSolver().getCurrentDepth());
                Assert.assertEquals(
                        models[k].getSolver().getMaxDepth(),
                        models[0].getSolver().getMaxDepth());
                Assert.assertEquals(
                        models[k].getSolver().getFailCount(),
                        models[0].getSolver().getFailCount());
                if(models[0].getResolutionPolicy()!= ResolutionPolicy.SATISFACTION)
                    Assert.assertEquals(
                            models[k].getSolver().getBestSolutionValue(),
                            models[0].getSolver().getBestSolutionValue());
                if (bc) {
                    for (int i = 0; i < vars[k].length; i++) {
                        Assert.assertEquals(vars[0][i].getValue(), vars[0][i].getValue());
                    }
                }
            }
        }while (bc);
        System.out.println(nbSols+" solutions");
        for(int i=0;i<models.length;i++){
            System.out.println(models[i].getName()+" solved in "+time[i]+" ms");
        }
    }
}
