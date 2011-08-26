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
import choco.kernel.common.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.constraints.binary.Absolute;
import solver.constraints.unary.Member;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.variables.view.Views;

import java.util.Arrays;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/05/11
 */
public class IntVarAbsTest {

    private int[][] bounded(int xl, int xu, int yl, int yu) throws ContradictionException {
        Solver solver = new Solver();
        IntVar Y = VariableFactory.bounded("Y", yl, yu, solver);
        IntVar X = Views.abs(Y);
        X.updateLowerBound(xl, Cause.Null);
        X.updateUpperBound(xu, Cause.Null);
        solver.propagate();
        return new int[][]{{X.getLB(), X.getUB()}, {Y.getLB(), Y.getUB()}};
    }

    private int[][] enumerated(int[] x, int[] y) throws ContradictionException {
        Solver solver = new Solver();
        IntVar Y = VariableFactory.enumerated("Y", y, solver);
        IntVar X = Views.abs(Y);

        solver.post(new Member(X, x, solver));
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

        solver.post(new Absolute(X, Y, solver));
        solver.set(StrategyFactory.random(ArrayUtils.toArray(X, Y), solver.getEnvironment()));
        return solver;
    }

    private Solver reformulate(int lbx, int ubx, int lby, int uby) {
        Solver solver = new Solver();
        IntVar X = VariableFactory.bounded("X", lbx, ubx, solver);
        IntVar Y = VariableFactory.bounded("Y", lby, uby, solver);

        solver.post(new Absolute(X, Y, solver));
        solver.set(StrategyFactory.random(ArrayUtils.toArray(X, Y), solver.getEnvironment()));
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
        //SearchMonitorFactory.log(ref, true, false);
        ref.findAllSolutions();

        Solver solver = new Solver();
        IntVar Y = VariableFactory.bounded("Y", minY, maxY, solver);
        IntVar X = Views.abs(Y);

        solver.post(new Member(X, minX, maxX, solver));
        //SearchMonitorFactory.log(solver, true, false);
        solver.set(StrategyFactory.random(ArrayUtils.toArray(Y), solver.getEnvironment()));
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
        IntVar X = Views.abs(Y);
        solver.post(new Member(X, domains[0], solver));
        //SearchMonitorFactory.log(solver, true, true);
        solver.set(StrategyFactory.random(ArrayUtils.toArray(X, Y), solver.getEnvironment()));
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

}
