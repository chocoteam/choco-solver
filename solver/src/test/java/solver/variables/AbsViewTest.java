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

import choco.checker.DomainBuilder;
import common.util.iterators.DisposableRangeIterator;
import common.util.iterators.DisposableValueIterator;
import common.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.search.strategy.IntStrategyFactory;

import java.util.Arrays;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
public class AbsViewTest {
    private int[][] bounded(int xl, int xu, int yl, int yu) throws ContradictionException {
        Solver solver = new Solver();
        IntVar Y = VariableFactory.bounded("Y", yl, yu, solver);
        IntVar X = VariableFactory.abs(Y);
        X.updateLowerBound(xl, Cause.Null);
        X.updateUpperBound(xu, Cause.Null);
        return new int[][]{{X.getLB(), X.getUB()}, {Y.getLB(), Y.getUB()}};
    }

    private int[][] enumerated(int[] x, int[] y) throws ContradictionException {
        Solver solver = new Solver();
        IntVar Y = VariableFactory.enumerated("Y", y, solver);
        IntVar X = VariableFactory.abs(Y);

        solver.post(IntConstraintFactory.member(X, x));
        solver.propagate();

        int[] xs = new int[X.getDomainSize()];
        int i = 0;
        int ub = X.getUB();
        for (int v = X.getLB(); v <= ub; v = X.nextValue(v), i++) {
            xs[i] = v;
        }

        int[] ys = new int[Y.getDomainSize()];
        i = 0;
        ub = Y.getUB();
        for (int v = Y.getLB(); v <= ub; v = Y.nextValue(v), i++) {
            ys[i] = v;
        }

        return new int[][]{xs, ys};
    }

    private Solver reformulate(int[] x, int[] y) {
        Solver solver = new Solver();
        IntVar X = VariableFactory.enumerated("X", x, solver);
        IntVar Y = VariableFactory.enumerated("Y", y, solver);

        solver.post(IntConstraintFactory.absolute(X, Y));
        solver.set(IntStrategyFactory.random(ArrayUtils.toArray(X, Y), System.currentTimeMillis()));
        return solver;
    }

    private Solver reformulate(int lbx, int ubx, int lby, int uby) {
        Solver solver = new Solver();
        IntVar X = VariableFactory.bounded("X", lbx, ubx, solver);
        IntVar Y = VariableFactory.bounded("Y", lby, uby, solver);

        solver.post(IntConstraintFactory.absolute(X, Y));
        solver.set(IntStrategyFactory.random(ArrayUtils.toArray(X, Y), System.currentTimeMillis()));
        return solver;
    }

    @Test(groups = "1s")
    public void testBXtoY1() throws ContradictionException {
        int[][] r = bounded(-15, 15, -10, -5);
        Assert.assertEquals(r[0][0], 5);
        Assert.assertEquals(r[0][1], 10);
    }

    @Test(groups = "1s")
    public void testBXtoY2() throws ContradictionException {
        int[][] r = bounded(-15, 15, -5, 0);
        Assert.assertEquals(r[0][0], 0);
        Assert.assertEquals(r[0][1], 5);
    }

    @Test(groups = "1s")
    public void testBXtoY3() throws ContradictionException {
        int[][] r = bounded(-15, 15, -5, 5);
        Assert.assertEquals(r[0][0], 0);
        Assert.assertEquals(r[0][1], 5);
    }

    @Test(groups = "1s")
    public void testBXtoY4() throws ContradictionException {
        int[][] r = bounded(-15, 15, 0, 5);
        Assert.assertEquals(r[0][0], 0);
        Assert.assertEquals(r[0][1], 5);
    }

    @Test(groups = "1s")
    public void testBXtoY5() throws ContradictionException {
        int[][] r = bounded(-15, 15, 5, 10);
        Assert.assertEquals(r[0][0], 5);
        Assert.assertEquals(r[0][1], 10);
    }

    @Test(groups = "1s")
    public void testBYtoX1() throws ContradictionException {
        int[][] r = bounded(0, 10, -15, 15);
        Assert.assertEquals(r[1][0], -10);
        Assert.assertEquals(r[1][1], 10);
    }

    @Test(groups = "1s")
    public void testBYtoX2() throws ContradictionException {
        int[][] r = bounded(5, 10, -15, 15);
        Assert.assertEquals(r[1][0], -10);
        Assert.assertEquals(r[1][1], 10);
    }

    @Test(groups = "1s")
    public void testEXtoY1() throws ContradictionException {
        int[][] r = enumerated(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[]{-6, -5, -3, -2});
        Assert.assertEquals(r[0], new int[]{2, 3, 5, 6});
    }

    @Test(groups = "1s")
    public void testEXtoY2() throws ContradictionException {
        int[][] r = enumerated(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[]{-6, -5, -1, 0, 1});
        Assert.assertEquals(r[0], new int[]{0, 1, 5, 6});
    }


    @Test(groups = "1s")
    public void testEXtoY3() throws ContradictionException {
        int[][] r = enumerated(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[]{-6, -5, 2, 3});
        Assert.assertEquals(r[0], new int[]{2, 3, 5, 6});
    }

    @Test(groups = "1s")
    public void testEXtoY4() throws ContradictionException {
        int[][] r = enumerated(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[]{2, 3, 5, 7});
        Assert.assertEquals(r[0], new int[]{2, 3, 5, 7});
    }

    @Test(groups = "1s")
    public void testEYtoX1() throws ContradictionException {
        int[][] r = enumerated(new int[]{0, 1, 4, 5}, new int[]{-10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        Assert.assertEquals(r[1], new int[]{-5, -4, -1, 0, 1, 4, 5});
    }

    @Test(groups = "1s")
    public void testEYtoX2() throws ContradictionException {
        int[][] r = enumerated(new int[]{3, 4, 5}, new int[]{-10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        Assert.assertEquals(r[1], new int[]{-5, -4, -3, 3, 4, 5});
    }

    @Test(groups = "1s")
    public void testBUG1() throws ContradictionException {
        int[][] r = enumerated(new int[]{-21, 3, 5, 9}, new int[]{-23, 3, 5, 7});
        Assert.assertEquals(r[0], new int[]{3, 5});
        Assert.assertEquals(r[1], new int[]{3, 5});
    }

    @Test(groups = "1s")
    public void testBUG2() throws ContradictionException {
        int[][] r = enumerated(new int[]{-25, -24, -23, -22, -21, -20, -18, -17, -16, -15, -13, -11, -10, -9, -7, -6, -1, 0, 1, 2, 3, 5, 6, 7},
                new int[]{-25, -23, -22, -21, -20, -19, -16, -14, -12, -11, -9, -8, -7, -5, -4, -3, -1, 0, 1, 2, 4, 5, 6, 7});
        Assert.assertEquals(r[0], new int[]{0, 1, 2, 3, 5, 6, 7});
        Assert.assertEquals(r[1], new int[]{-7, -5, -3, -1, 0, 1, 2, 5, 6, 7});
    }

    @Test(groups = "1s")
    public void testBUG3() throws ContradictionException {
        solveE(new int[][]{{7, 10, 13, 17, 19, 21, 26}, {7, 8, 10, 15, 18, 30, 31}});
    }

    @Test(groups = "1s")
    public void testBUG4() throws ContradictionException {
        solveB(2, 4, -3, 3);
    }

    @Test(groups = "1s")
    public void testBUG5() throws ContradictionException {
        solveB(2, 12, -4, 25);
    }

    @Test(groups = "10s")
    public void testSolveAllB() {
        Random rand;
        for (int seed = 0; seed < 5000; seed++) {
            rand = new Random(seed);

            int minX = -20 + rand.nextInt(40);
            int maxX = minX + rand.nextInt(40);

            int minY = -20 + rand.nextInt(40);
            int maxY = minY + rand.nextInt(40);
            solveB(minX, maxX, minY, maxY);
        }
    }

    public void solveB(int minX, int maxX, int minY, int maxY) {
        Solver ref = reformulate(minX, maxX, minY, maxY);
//        SearchMonitorFactory.log(ref, true, false);
        ref.findAllSolutions();

        Solver solver = new Solver();
        IntVar Y = VariableFactory.bounded("Y", minY, maxY, solver);
        IntVar X = VariableFactory.abs(Y);

        solver.post(IntConstraintFactory.member(X, minX, maxX));
//        SearchMonitorFactory.log(solver, true, false);
        solver.set(IntStrategyFactory.random(ArrayUtils.toArray(Y), System.currentTimeMillis()));
        if (Boolean.TRUE == solver.findSolution()) {
            do {
                Assert.assertTrue(X.getValue() == Math.abs(Y.getValue()));
            } while (Boolean.TRUE == solver.nextSolution());
        }
        String message = String.format("[%d,%d] - [%d,%d]", minX, maxX, minY, maxY);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount(),
                message);
        Assert.assertTrue(solver.getMeasures().getNodeCount() <= ref.getMeasures().getNodeCount(),
                message);
    }


    @Test(groups = "10m")
    public void testSolveAllE() {
        Random rand;
        for (int seed = 0; seed < 5000; seed++) {
            rand = new Random(seed);
            for (double d = 0.25; d <= 1.0; d += 0.25) {
                for (int h = 0; h <= 1; h++) {
                    for (int b = 0; b <= 1; b++) {
                        int min = -20 + rand.nextInt(40);
                        int max = min + rand.nextInt(40);
                        int[][] domains = DomainBuilder.buildFullDomains(2, min, max, rand, d, h == 0);
                        solveE(domains);
                    }
                }
            }
        }
    }

    private void solveE(int[][] domains) {
        Solver ref = reformulate(domains[0], domains[1]);
        ref.findAllSolutions();

        Solver solver = new Solver();
        IntVar Y = VariableFactory.enumerated("Y", domains[1], solver);
        IntVar X = VariableFactory.abs(Y);
        solver.post(IntConstraintFactory.member(X, domains[0]));
        //SearchMonitorFactory.log(solver, true, true);
        solver.set(IntStrategyFactory.random(ArrayUtils.toArray(X, Y), System.currentTimeMillis()));
        if (Boolean.TRUE == solver.findSolution()) {
            do {
                Assert.assertTrue(X.getValue() == Math.abs(Y.getValue()));
            } while (Boolean.TRUE == solver.nextSolution());
        }
        String message = Arrays.toString(domains[0]) + " - " + Arrays.toString(domains[1]);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount(),
                message);
        Assert.assertTrue(solver.getMeasures().getNodeCount() <= ref.getMeasures().getNodeCount(),
                message + " node:" + solver.getMeasures().getNodeCount() + " <= " + ref.getMeasures().getNodeCount());
    }

    @Test(groups = "10s")
    public void testIt1() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            random.setSeed(seed);
            Solver solver = new Solver();
            int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
            IntVar o = VariableFactory.bounded("o", domains[0][0], domains[0][domains[0].length - 1], solver);
            IntVar v = VariableFactory.abs(o);
            DisposableValueIterator vit = v.getValueIterator(true);
            while (vit.hasNext()) {
                int va = vit.next();
                Assert.assertTrue(o.contains(va) || o.contains(-va), "seed:" + seed);
            }
            vit.dispose();

            vit = v.getValueIterator(false);
            while (vit.hasPrevious()) {
                int va = vit.previous();
                Assert.assertTrue(o.contains(va) || o.contains(-va), "seed:" + seed);
            }
            vit.dispose();

            DisposableRangeIterator rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                int min = rit.min();
                int max = rit.max();

                Assert.assertTrue(o.contains(min) || o.contains(-min), "seed:" + seed);
                Assert.assertTrue(o.contains(max) || o.contains(-max), "seed:" + seed);
                rit.next();
            }
            rit.dispose();

            rit = v.getRangeIterator(false);
            while (rit.hasPrevious()) {
                int min = rit.min();
                int max = rit.max();

                Assert.assertTrue(o.contains(min) || o.contains(-min), "seed:" + seed);
                Assert.assertTrue(o.contains(max) || o.contains(-max), "seed:" + seed);
                rit.previous();
            }
            rit.dispose();
        }
    }

    @Test(groups = "10s")
    public void testIt2() {
        Random random = new Random();
        for (int seed = 0; seed < 200; seed++) {
            random.setSeed(seed);
            Solver solver = new Solver();
            int[][] domains = DomainBuilder.buildFullDomains(1, -5, 5, random, random.nextDouble(), random.nextBoolean());
            IntVar o = VariableFactory.enumerated("o", domains[0], solver);
            IntVar v = VariableFactory.abs(o);
            DisposableValueIterator vit = v.getValueIterator(true);
            while (vit.hasNext()) {
                int va = vit.next();
                Assert.assertTrue(o.contains(va) || o.contains(-va), "seed:" + seed);
            }
            vit.dispose();

            vit = v.getValueIterator(false);
            while (vit.hasPrevious()) {
                int va = vit.previous();
                Assert.assertTrue(o.contains(va) || o.contains(-va), "seed:" + seed);
            }
            vit.dispose();

            DisposableRangeIterator rit = v.getRangeIterator(true);
            while (rit.hasNext()) {
                int min = rit.min();
                int max = rit.max();

                Assert.assertTrue(o.contains(min) || o.contains(-min), "seed:" + seed);
                Assert.assertTrue(o.contains(max) || o.contains(-max), "seed:" + seed);
                rit.next();
            }
            rit.dispose();

            rit = v.getRangeIterator(false);
            while (rit.hasPrevious()) {
                int min = rit.min();
                int max = rit.max();

                Assert.assertTrue(o.contains(min) || o.contains(-min), "seed:" + seed);
                Assert.assertTrue(o.contains(max) || o.contains(-max), "seed:" + seed);
                rit.previous();
            }
            rit.dispose();
        }
    }

}
