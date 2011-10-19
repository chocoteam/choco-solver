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
import solver.constraints.binary.Absolute;
import solver.constraints.binary.EqualX_YC;
import solver.constraints.binary.GreaterOrEqualX_YC;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.constraints.ternary.MaxXYZ;
import solver.constraints.ternary.Times;
import solver.constraints.unary.Relation;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/08/11
 */
public class ViewsTest {


    @Test(groups = "10m")
    public void test1() {
        // Z = X + Y
        for (int seed = 1; seed < 1001; seed += 100) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, ref);
                IntVar y = VariableFactory.enumerated("y", 0, seed, ref);
                IntVar z = VariableFactory.enumerated("z", 0, seed, ref);
                ref.post(Sum.eq(new IntVar[]{x, y, z}, new int[]{1, 1, -1}, 0, ref));
                ref.post(new Relation(z, Relation.R.LQ, seed, ref));
                ref.set(StrategyFactory.minDomMinVal(new IntVar[]{x, y}, ref.getEnvironment()));

            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, solver);
                IntVar y = VariableFactory.enumerated("y", 0, seed, solver);
                IntVar z = Views.sum(x, y);
                solver.post(new Relation(z, Relation.R.LQ, seed, solver));
                solver.set(StrategyFactory.minDomMinVal(new IntVar[]{x, y}, solver.getEnvironment()));

            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(),
                    ref.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(),
                    ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }

    @Test(groups = "10m")
    public void test1b() {
        // Z = |X|
        for (int seed = 100; seed < 10011; seed += 2000) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, ref);
                IntVar z = VariableFactory.enumerated("z", 0, seed, ref);

                ref.post(new Absolute(x, z, ref));
                ref.set(StrategyFactory.minDomMinVal(new IntVar[]{x}, ref.getEnvironment()));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, solver);
                IntVar z = Views.abs(x);
                solver.post(new Relation(z, Relation.R.LQ, seed, solver));
                solver.set(StrategyFactory.minDomMinVal(new IntVar[]{x}, solver.getEnvironment()));

            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(ref.getMeasures().getSolutionCount(),
                    solver.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(),
                    ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }

    @Test(groups = "10m")
    public void test1c() {
        // Z = -X
        for (int seed = 1; seed < 10001; seed += 1000) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, ref);
                IntVar z = VariableFactory.enumerated("z", 0, seed, ref);

                ref.post(new EqualX_YC(x, z, 0, ref));
                ref.set(StrategyFactory.minDomMinVal(new IntVar[]{x}, ref.getEnvironment()));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, solver);
                IntVar z = Views.minus(x);
                solver.post(new Relation(z, Relation.R.LQ, seed, solver));
                solver.set(StrategyFactory.minDomMinVal(new IntVar[]{x}, solver.getEnvironment()));

            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(ref.getMeasures().getSolutionCount(),
                    solver.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(),
                    ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }

    @Test(groups = "10m")
    public void test1d() {
        // Z = X + Y + ...
        for (int seed = 2; seed < 9; seed += 1) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            int n = seed * 2;
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", n, 0, 2, ref);
                ref.post(Sum.eq(x, n, ref));
                ref.set(StrategyFactory.minDomMinVal(x, ref.getEnvironment()));
            }
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", n, 0, 2, solver);
                IntVar[] y = new IntVar[seed];
                for (int i = 0; i < seed; i++) {
                    y[i] = Views.sum(x[i], x[i + seed]);
                }
                solver.post(Sum.eq(y, n, solver));

                solver.set(StrategyFactory.minDomMinVal(x, solver.getEnvironment()));

            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(),
                    ref.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(),
                    ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }


    @Test(groups = "10m")
    public void test1e() {
        // Z = X^2
        for (int seed = 10; seed < 10001; seed += 101) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, ref);
                IntVar z = VariableFactory.enumerated("z", 0, seed * seed, ref);
                ref.post(new Times(x, x, z, ref));
                ref.post(new Relation(z, Relation.R.LQ, seed, ref));
                ref.set(StrategyFactory.minDomMinVal(new IntVar[]{x}, ref.getEnvironment()));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, solver);
                IntVar z = Views.sqr(x);
                solver.post(new Relation(z, Relation.R.LQ, seed, solver));
                solver.set(StrategyFactory.minDomMinVal(new IntVar[]{x}, solver.getEnvironment()));
            }
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(),
                    ref.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(),
                    ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }


    @Test(groups = "10m")
    public void test1f() {
        // Z = X^2
        Solver ref = new Solver();
        Solver solver = new Solver();
        {
            IntVar x = VariableFactory.enumerated("x", 160, 187, ref);
            IntVar y = VariableFactory.enumerated("y", -999, 999, ref);
            IntVar z = VariableFactory.enumerated("z", -9999, 9999, ref);
            ref.post(Sum.eq(new IntVar[]{z, x}, new int[]{1, 1}, 180, ref));
            ref.post(new MaxXYZ(y, Views.fixed(0, ref), z, ref));
//            try {
//                ref.propagate();
//                x.updateUpperBound(161, Cause.Null);
//                ref.propagate();
//            } catch (ContradictionException e) {
//                Assert.fail();
//            }
//            Assert.assertEquals(y.getLB(), 19);
//            Assert.assertEquals(y.getUB(), 20);
        }
        {
            IntVar x = VariableFactory.enumerated("x", 160, 187, solver);
            IntVar y = VariableFactory.enumerated("y", -999, 999, solver);
            IntVar z = Views.offset(Views.minus(x), 180);
            solver.post(new MaxXYZ(y, Views.fixed(0, solver), z, solver));
//            try {
//                solver.propagate();
//                x.updateUpperBound(161, Cause.Null);
//                solver.propagate();
//            } catch (ContradictionException e) {
//                Assert.fail();
//            }
//            Assert.assertEquals(y.getLB(), 19);
//            Assert.assertEquals(y.getUB(), 20);
            SearchMonitorFactory.log(ref, false, false);
            SearchMonitorFactory.log(solver, false, false);

            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(),
                    ref.getMeasures().getSolutionCount(), 0 + "");
            System.out.printf("%d : %d vs. %d (%f)\n", 0, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(),
                    ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }


    @Test(groups = "10m")
    public void test2() {
        // Z = X - Y
        for (int seed = 100; seed < 1101; seed += 200) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, ref);
                IntVar y = VariableFactory.enumerated("y", 0, seed, ref);
                IntVar z = VariableFactory.enumerated("z", 0, seed, ref);
                ref.post(Sum.eq(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 0, ref));
                ref.set(StrategyFactory.minDomMinVal(new IntVar[]{x, y}, ref.getEnvironment()));

            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, solver);
                IntVar y = VariableFactory.enumerated("y", 0, seed, solver);
                IntVar z = Views.sum(x, Views.minus(y));
                solver.post(new Relation(z, Relation.R.GQ, 0, solver));
                solver.post(new Relation(z, Relation.R.LQ, seed, solver));
                solver.set(StrategyFactory.minDomMinVal(new IntVar[]{x, y}, solver.getEnvironment()));

            }
//            SearchMonitorFactory.log(ref, true, true);
//            SearchMonitorFactory.log(solver, true, true);
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(ref.getMeasures().getSolutionCount(),
                    solver.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(), ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }

    @Test(groups = "10m")
    public void test3() {
        // Z = |X - Y|
        for (int seed = 100; seed < 1001; seed += 200) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, ref);
                IntVar y = VariableFactory.enumerated("y", 0, seed, ref);
                IntVar z = VariableFactory.enumerated("z", -seed, seed, ref);
                IntVar az = VariableFactory.enumerated("az", 0, seed, ref);
                ref.post(Sum.eq(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 0, ref));
                ref.post(new Absolute(az, z, ref));
                ref.set(StrategyFactory.minDomMinVal(new IntVar[]{x, y}, ref.getEnvironment()));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, solver);
                IntVar y = VariableFactory.enumerated("y", 0, seed, solver);
                IntVar z = Views.abs(Views.sum(x, Views.minus(y)));
                solver.post(new Relation(z, Relation.R.GQ, 0, solver));
                solver.post(new Relation(z, Relation.R.LQ, seed, solver));
                solver.set(StrategyFactory.minDomMinVal(new IntVar[]{x, y}, solver.getEnvironment()));
            }
//            SearchMonitorFactory.log(ref, true, true);
//            SearchMonitorFactory.log(solver, true, true);
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(ref.getMeasures().getSolutionCount(),
                    solver.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(), ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }

    @Test(groups = "10m")
    public void test4() {
        // Z = |X - Y| + AllDiff
        for (int seed = 100; seed < 1001; seed += 200) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, ref);
                IntVar y = VariableFactory.enumerated("y", 0, seed, ref);
                IntVar z = VariableFactory.enumerated("z", -seed, seed, ref);
                IntVar az = VariableFactory.enumerated("az", 0, seed, ref);
                ref.post(Sum.eq(new IntVar[]{x, y, z}, new int[]{1, -1, -1}, 0, ref));
                ref.post(new Absolute(az, z, ref));
                ref.post(new AllDifferent(new IntVar[]{x, y, az}, ref));
                ref.set(StrategyFactory.minDomMinVal(new IntVar[]{x, y}, ref.getEnvironment()));
            }
            {
                IntVar x = VariableFactory.enumerated("x", 0, seed, solver);
                IntVar y = VariableFactory.enumerated("y", 0, seed, solver);
                IntVar z = Views.abs(Views.sum(x, Views.minus(y)));
                solver.post(new Relation(z, Relation.R.GQ, 0, solver));
                solver.post(new Relation(z, Relation.R.LQ, seed, solver));
                solver.post(new AllDifferent(new IntVar[]{x, y, z}, solver));
                solver.set(StrategyFactory.minDomMinVal(new IntVar[]{x, y}, solver.getEnvironment()));
            }
//            SearchMonitorFactory.log(ref, true, true);
//            SearchMonitorFactory.log(solver, true, true);
            ref.findAllSolutions();
            solver.findAllSolutions();
            Assert.assertEquals(ref.getMeasures().getSolutionCount(),
                    solver.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(), ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }

    @Test(groups = "10m")
    public void test5() {
        // ~all-interval series
        for (int seed = 4; seed < 501; seed += 10) {
            Solver ref = new Solver();
            Solver solver = new Solver();
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", seed, 0, seed - 1, ref);
                IntVar[] y = VariableFactory.enumeratedArray("y", seed - 1, -(seed - 1), seed - 1, ref);
                IntVar[] t = VariableFactory.enumeratedArray("t", seed - 1, 1, seed - 1, ref);
                for (int i = 0; i < seed - 1; i++) {
                    ref.post(Sum.eq(new IntVar[]{x[i + 1], x[i], y[i]}, new int[]{1, -1, -1}, 0, ref));
                    ref.post(new Absolute(t[i], y[i], ref));
                }
                ref.post(new AllDifferent(x, ref));
                ref.post(new AllDifferent(t, ref));
                ref.post(new GreaterOrEqualX_YC(x[1], x[0], 1, ref));
                ref.post(new GreaterOrEqualX_YC(t[0], t[seed - 2], 1, ref));
                ref.set(StrategyFactory.minDomMinVal(x, ref.getEnvironment()));
                ref.findSolution();
            }
            {
                IntVar[] x = VariableFactory.enumeratedArray("x", seed, 0, seed - 1, solver);
                IntVar[] t = new IntVar[seed - 1];
                for (int i = 0; i < seed - 1; i++) {
                    t[i] = Views.abs(Views.sum(x[i + 1], Views.minus(x[i])));
                    solver.post(new Relation(t[i], Relation.R.GT, 0, solver));
                    solver.post(new Relation(t[i], Relation.R.LT, seed, solver));
                }
                solver.post(new AllDifferent(x, solver));
                solver.post(new AllDifferent(t, solver));
                solver.post(new GreaterOrEqualX_YC(x[1], x[0], 1, solver));
                solver.post(new GreaterOrEqualX_YC(t[0], t[seed - 2], 1, solver));
                solver.set(StrategyFactory.minDomMinVal(x, solver.getEnvironment()));
                solver.findSolution();
            }
            //SearchMonitorFactory.log(ref, true, true);
            //SearchMonitorFactory.log(solver, false, false);
            Assert.assertEquals(solver.getMeasures().getSolutionCount(),
                    ref.getMeasures().getSolutionCount(), seed + "");
            System.out.printf("%d : %d vs. %d (%f)\n", seed, ref.getMeasures().getTimeCount(),
                    solver.getMeasures().getTimeCount(),
                    ref.getMeasures().getTimeCount() / (float) solver.getMeasures().getTimeCount());
        }
    }


    @Test(groups = "10m")
    public void test6() throws ContradictionException {
        Solver solver = new Solver();
        IntVar x = VariableFactory.enumerated("x", 0, 10, solver);
        IntVar y = Views.abs(x);
        IntVar z = Views.abs(Views.abs(x));

        for (int j = 0; j < 200; j++) {
            long t = -System.nanoTime();
            for (int i = 0; i < 999999; i++) {
                if (y.getLB() == x.getUB()) {
                    y.updateLowerBound(0, Cause.Null, false);
                }
            }
            t += System.nanoTime();
            System.out.printf("%.2fms vs. ", t / 1000 / 1000f);
            t = -System.nanoTime();
            for (int i = 0; i < 999999; i++) {
                if (z.getLB() == x.getUB()) {
                    z.updateLowerBound(0, Cause.Null, false);
                }
            }
            t += System.nanoTime();
            System.out.printf("%.2fms\n", t / 1000 / 1000f);
        }
    }
}
