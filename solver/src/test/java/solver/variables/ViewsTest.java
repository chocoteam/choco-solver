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
package solver.variables;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.Sum;
import solver.constraints.ternary.Max;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/08/11
 */
public class ViewsTest {

    public static void check(Solver ref, Solver solver, long seed, boolean strict, boolean solveAll) {
//        SearchMonitorFactory.log(ref, true, true);
//        SearchMonitorFactory.log(solver, true, true);
        if (solveAll) {
            ref.findAllSolutions();
            solver.findAllSolutions();
        } else {
//            System.out.printf("%s\n", ref.toString());
            ref.findSolution();
//            System.out.printf("%s\n", solver.toString());
            solver.findSolution();
        }
        Assert.assertEquals(solver.getMeasures().getSolutionCount(),
                ref.getMeasures().getSolutionCount(), "solutions (" + seed + ")");
//        System.out.printf("%d : %d vs. %d  -- ", seed, ref.getMeasures().getNodeCount(),
//                solver.getMeasures().getNodeCount());
        if (strict) {
            Assert.assertEquals(solver.getMeasures().getNodeCount(), ref.getMeasures().getNodeCount(), "nodes (" + seed + ")");
        } else {
            Assert.assertTrue(ref.getMeasures().getNodeCount() >=
                    solver.getMeasures().getNodeCount(), seed + "");
        }
//        System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
//                solver.getMeasures().getTimeCount(),
//                ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
    }


    @Test(groups = "1m")
    public void test1() {
        // Z = X + Y
//        int seed = 5;
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", 0, 4, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y, z}, new int[]{1, 1, -1}, "=", 0));
                ref.set(StrategyFactory.random(new IntVar[]{x, y, z}, ref.getEnvironment(), seed));

            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = Sum.var(x, y);
                solver.set(StrategyFactory.random(new IntVar[]{x, y, z}, solver.getEnvironment(), seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test1a() {
        // Z = X + Y (bounded)
//        int seed = 5;
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.bounded("x", 0, 2, ref);
                IntVar y = VariableFactory.bounded("y", 0, 2, ref);
                IntVar z = VariableFactory.bounded("z", 0, 4, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y, z}, new int[]{1, 1, -1}, "=", 0));
                ref.set(StrategyFactory.random(new IntVar[]{x, y, z}, ref.getEnvironment(), seed));

            }
            {
                IntVar x = VariableFactory.bounded("x", 0, 2, solver);
                IntVar y = VariableFactory.bounded("y", 0, 2, solver);
                IntVar z = Sum.var(x, y);
                solver.set(StrategyFactory.random(new IntVar[]{x, y, z}, solver.getEnvironment(), seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void testa() {
        // Z = max(X + Y)
        for (int seed = 0; seed < 99999; seed += 1) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.bounded("z", 0, 2, ref);
                ref.post(IntConstraintFactory.maximum(z, x, y));
                ref.set(StrategyFactory.random(new IntVar[]{x, y, z}, ref.getEnvironment(), seed));

            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = Max.var(x, y);
                solver.set(StrategyFactory.random(new IntVar[]{x, y, z}, solver.getEnvironment(), seed));

            }
            check(ref, solver, seed, false, true);
        }
    }

    @Test(groups = "1m")
    public void test1b() {
        // Z = |X|
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
                IntVar z = VariableFactory.enumerated("z", 0, 2, ref);

                ref.post(IntConstraintFactory.absolute(z, x));
                ref.set(StrategyFactory.random(new IntVar[]{x, z}, ref.getEnvironment(), seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, solver);
                IntVar z = Views.abs(x);
                solver.set(StrategyFactory.random(new IntVar[]{x, z}, solver.getEnvironment(), seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test1bb() {
        // Z = X + c
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -1, 3, ref);

                ref.post(IntConstraintFactory.arithm(z, "=", x, "+", 1));
                ref.set(StrategyFactory.random(new IntVar[]{x, z}, ref.getEnvironment(), seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, solver);
                IntVar z = Views.offset(x, 1);
                solver.set(StrategyFactory.random(new IntVar[]{x, z}, solver.getEnvironment(), seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test1bbb() {
        // Z = X * c
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -4, 4, ref);

                ref.post(IntConstraintFactory.times(x, Views.fixed(2, ref), z));
                ref.set(StrategyFactory.random(new IntVar[]{x, z}, ref.getEnvironment(), seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, solver);
                IntVar z = Views.scale(x, 2);
                solver.set(StrategyFactory.random(new IntVar[]{x, z}, solver.getEnvironment(), seed));

            }
            check(ref, solver, seed, false, true);
        }
    }

    @Test(groups = "1m")
    public void test1c() {
        // Z = -X
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -2, 0, ref);

                ref.post(IntConstraintFactory.arithm(z, "+", x, "=", 0));
                ref.set(StrategyFactory.random(new IntVar[]{x, z}, ref.getEnvironment(), seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar z = Views.minus(x);
                solver.set(StrategyFactory.random(new IntVar[]{x, z}, solver.getEnvironment(), seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test1d() {
        // Z = X + Y + ...
        for (int seed = 2; seed < 9; seed += 1) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            int n = seed * 2;
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", n, 0, 2, ref);
                ref.post(IntConstraintFactory.sum(x, "=", n));
                ref.set(StrategyFactory.minDomMinVal(x, ref.getEnvironment()));
            }
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", n, 0, 2, solver);
                IntVar[] y = new IntVar[seed];
                for (int i = 0; i < seed; i++) {
                    y[i] = Sum.var(x[i], x[i + seed]);
                }
                solver.post(IntConstraintFactory.sum(y, "=", n));

                solver.set(StrategyFactory.minDomMinVal(x, solver.getEnvironment()));

            }
            check(ref, solver, seed, true, true);
        }
    }


    @Test(groups = "30s")
    public void test1e() {
        // Z = X^2
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", -2, 2, ref);
                IntVar z = VariableFactory.enumerated("z", 0, 4, ref);
                ref.post(IntConstraintFactory.times(x, x, z));
                ref.set(StrategyFactory.random(new IntVar[]{x, z}, ref.getEnvironment(), seed));
            }
            {
                IntVar z = VariableFactory.enumerated("z", 0, 4, solver);
                IntVar x = Views.sqr(z);
                solver.set(StrategyFactory.random(new IntVar[]{x, z}, solver.getEnvironment(), seed));
            }
            check(ref, solver, seed, false, true);
        }
    }

    @Test(groups = "30s")
    public void test1f() {
        // Z = MAX(X,Y)
        Solver ref = new Solver();
        Solver solver = new Solver();
        {
            IntVar x = VariableFactory.enumerated("x", 160, 187, ref);
            IntVar y = VariableFactory.enumerated("y", -999, 999, ref);
            IntVar z = VariableFactory.enumerated("z", -9999, 9999, ref);
            ref.post(IntConstraintFactory.scalar(new IntVar[]{z, x}, new int[]{1, 1}, "=", 180));
            ref.post(IntConstraintFactory.maximum(y, Views.fixed(0, ref), z));
        }
        {
            IntVar x = VariableFactory.enumerated("x", 160, 187, solver);
            IntVar y = VariableFactory.enumerated("y", -999, 999, solver);
            IntVar z = Views.offset(Views.minus(x), 180);
            solver.post(IntConstraintFactory.maximum(y, Views.fixed(0, solver), z));

            check(ref, solver, 0, false, true);
        }
    }


    @Test(groups = "1m")
    public void test2() {
        // Z = X - Y
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -2, 2, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, "=", 0));
                ref.set(StrategyFactory.random(new IntVar[]{x, y, z}, ref.getEnvironment(), seed));

            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = Sum.var(x, Views.minus(y));
                solver.set(StrategyFactory.random(new IntVar[]{x, y, z}, solver.getEnvironment(), seed));

            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test3() {
        // Z = |X - Y|
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -2, 2, ref);
                IntVar az = VariableFactory.enumerated("az", 0, 2, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, "=", 0));
                ref.post(IntConstraintFactory.absolute(az, z));
                ref.set(StrategyFactory.random(new IntVar[]{x, y, az}, ref.getEnvironment(), seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = Views.abs(Sum.var(x, Views.minus(y)));
                solver.set(StrategyFactory.random(new IntVar[]{x, y, z}, solver.getEnvironment(), seed));
            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1m")
    public void test4() {
        // Z = |X - Y| + AllDiff
        for (int seed = 0; seed < 99999; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, ref);
                IntVar y = VariableFactory.enumerated("y", 0, 2, ref);
                IntVar z = VariableFactory.enumerated("z", -2, 2, ref);
                IntVar az = VariableFactory.enumerated("az", 0, 2, ref);
                ref.post(IntConstraintFactory.scalar(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, "=", 0));
                ref.post(IntConstraintFactory.absolute(az, z));
                ref.post(IntConstraintFactory.alldifferent(new IntVar[]{x, y, az}, "BC"));
                ref.set(StrategyFactory.random(new IntVar[]{x, y, az}, ref.getEnvironment(), seed));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, 2, solver);
                IntVar y = VariableFactory.enumerated("y", 0, 2, solver);
                IntVar z = Views.abs(Sum.var(x, Views.minus(y)));
                solver.post(IntConstraintFactory.alldifferent(new IntVar[]{x, y, z}, "BC"));
                solver.set(StrategyFactory.random(new IntVar[]{x, y, z}, solver.getEnvironment(), seed));
            }
            check(ref, solver, seed, true, true);
        }
    }

    @Test(groups = "1s")
    public void test5() {
        // ~all-interval series
        int k = 5;
        for (int seed = 0; seed < 99; seed++) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", k, 0, k - 1, ref);
                IntVar[] y = VariableFactory.enumeratedArray("y", k - 1, -(k - 1), k - 1, ref);
                IntVar[] t = VariableFactory.enumeratedArray("t", k - 1, 0, k - 1, ref);
                for (int i = 0; i < k - 1; i++) {
                    ref.post(IntConstraintFactory.scalar(new IntVar[]{x[i + 1], x[i], y[i]}, new int[]{1, -1, -1}, "=", 0));
                    ref.post(IntConstraintFactory.absolute(t[i], y[i]));
                }
                ref.post(IntConstraintFactory.alldifferent(x, "BC"));
                ref.post(IntConstraintFactory.alldifferent(t, "BC"));
                ref.post(IntConstraintFactory.arithm(x[1], ">", x[0]));
                ref.post(IntConstraintFactory.arithm(t[0], ">", t[k - 2]));
                ref.set(StrategyFactory.random(x, ref.getEnvironment(), seed));
            }
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", k, 0, k - 1, solver);
                IntVar[] t = new IntVar[k - 1];
                for (int i = 0; i < k - 1; i++) {
                    t[i] = Views.abs(Sum.var(x[i + 1], Views.minus(x[i])));
                }
                solver.post(IntConstraintFactory.alldifferent(x, "BC"));
                solver.post(IntConstraintFactory.alldifferent(t, "BC"));
                solver.post(IntConstraintFactory.arithm(x[1], ">", x[0]));
                solver.post(IntConstraintFactory.arithm(t[0], ">", t[k - 2]));
                solver.set(StrategyFactory.random(x, solver.getEnvironment(), seed));
            }
            check(ref, solver, k, true, true);
        }
    }


    @Test(groups = "1s")
    public void test6() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = VariableFactory.enumerated("x", 0, 10, solver);
        IntVar y = Views.abs(x);
        IntVar z = Views.abs(Views.abs(x));

        for (int j = 0; j < 200; j++) {
            long t = -System.nanoTime();
            for (int i = 0; i < 999999; i++) {
                if (y.getLB() == x.getUB()) {
                    y.updateLowerBound(0, Cause.Null);
                }
            }
            t += System.nanoTime();
            System.out.printf("%.2fms vs. ", t / 1000 / 1000f);
            t = -System.nanoTime();
            for (int i = 0; i < 999999; i++) {
                if (z.getLB() == x.getUB()) {
                    z.updateLowerBound(0, Cause.Null);
                }
            }
            t += System.nanoTime();
            System.out.printf("%.2fms\n", t / 1000 / 1000f);
        }
    }
}
