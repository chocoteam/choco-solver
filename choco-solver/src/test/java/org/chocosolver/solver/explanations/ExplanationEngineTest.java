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
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.learn.LearnCBJ;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.OnDemandIntStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static java.lang.System.out;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.fill;
import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.explanations.ExplanationFactory.CBJ;
import static org.chocosolver.solver.explanations.ExplanationFactory.DBT;
import static org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory.limitTime;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.*;
import static org.chocosolver.solver.search.strategy.assignments.DecisionOperator.int_split;
import static org.chocosolver.solver.search.strategy.selectors.ValSelectorFactory.midIntVal;
import static org.chocosolver.solver.search.strategy.selectors.VarSelectorFactory.minDomIntVar;
import static org.chocosolver.solver.trace.Chatterbox.*;
import static org.chocosolver.util.tools.StringUtils.randomName;
import static org.testng.Assert.*;

/**
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public class ExplanationEngineTest {


    public void model1(Model model, int n) {
        IntVar[] vs = model.intVarArray("V", n, 1, n - 1, false);
        for (int i = 0; i < n - 1; i++) {
            new Constraint(i + ">" + (i + 1), new PropGreaterOrEqualX_YC(new IntVar[]{vs[i], vs[i + 1]}, 1)).post();
        }
    }

    /**
     * This test evaluates the case where half of the generated events are useless.
     * Only one branch is evaluated.
     */
    @Test(groups="1s", timeOut=60000)
    public void test1() {
        for (int n = 6; n < 1001; n *= 2) {
            System.out.printf("n = %d : ", n);
            Model model = new Model();
            model1(model, n);
            Model expl = new Model();
            model1(expl, n);

            ExplanationEngine ee = new ExplanationEngine(expl, true, true);
            Explanation r = null;
            try {
                expl.getResolver().propagate();
                Assert.fail();
            } catch (ContradictionException e) {
                r = ee.explain(e);
            }
            Assert.assertNotNull(r);
            Assert.assertEquals(r.nbCauses(), n - 1);
        }
    }

    private void model2(Model model, int n) {
        IntVar[] vs = model.intVarArray("V", 2, 0, n, false);
        new Constraint("0>1", new PropGreaterOrEqualX_YC(new IntVar[]{vs[0], vs[1]}, 1)).post();
        new Constraint("0<1", new PropGreaterOrEqualX_YC(new IntVar[]{vs[1], vs[0]}, 1)).post();
    }

    /**
     * This test evaluates the case where only two constraints are in conflict, the remaining ones are useless.
     */
    @Test(groups="1s", timeOut=60000)
    public void test2() {
        for (int n = 100; n < 12801; n *= 2) {
            System.out.printf("n = %d : ", n);
            Model model = new Model();
            model2(model, n);

            Model expl = new Model();
            model2(expl, n);

            ExplanationEngine ee = new ExplanationEngine(expl, true, true);
            Explanation r = null;
            try {
                expl.getResolver().propagate();
                Assert.fail();
            } catch (ContradictionException e) {
                r = ee.explain(e);
            }
            Assert.assertNotNull(r);
            Assert.assertEquals(r.nbCauses(), 2);
        }
    }

    private void model3(Model model, int n) {
        IntVar[] vs = model.intVarArray("V", n, 2, n + 2, true);
        model.arithm(vs[n - 2], "=", vs[n - 1]).post();
        model.arithm(vs[n - 2], "!=", vs[n - 1]).post();
    }

    /**
     * This test evaluates the case where a subset of the constraints are in conflict
     */
    @Test(groups="5m", timeOut=300000)
    public void test3() {
        for (int n = 3; n < 64000; n *= 2) {
            System.out.printf("n = %d : ", n);
            Model model = new Model();
            model3(model, n);
            Model expl = new Model();
            model3(expl, n);

            Resolver r = model.getResolver();
            IntStrategy is = inputOrderLBSearch(expl.retrieveIntVars(false));
            ExplanationEngine ee = new ExplanationEngine(expl, true, true);
            Explanation ex = null;
            try {
                r.propagate();
                for (int i = 0; i < n; i++) {
                    Decision d = is.getDecision();
                    d.buildNext();
                    d.apply();
                    r.propagate();
                }
                Assert.fail();
            } catch (ContradictionException e) {
                ex = ee.explain(e);
            }
            Assert.assertNotNull(ex);
            Assert.assertEquals(ex.nbCauses(), 2);
            Assert.assertEquals(ex.nbDecisions(), 1);
        }
    }

    /**
     * This test evaluates the case where a subset of the constraints are in conflict
     */
    @Test(groups="1s", timeOut=60000)
    public void test4() {
        int n = 3;
        out.printf("n = %d : ", n);
        Model model = new Model();
        IntVar[] vs = model.intVarArray("V", n, 0, n, false);
        new Constraint((n - 2) + ">" + (n - 1), new PropGreaterOrEqualX_YC(new IntVar[]{vs[n - 2], vs[n - 1]}, 1)).post();
        new Constraint((n - 2) + "<" + (n - 1), new PropGreaterOrEqualX_YC(new IntVar[]{vs[n - 1], vs[n - 2]}, 1)).post();

        ExplanationEngine ee = new ExplanationEngine(model, true, true);
        Explanation ex = null;
        Resolver r = model.getResolver();
        IntStrategy is = inputOrderLBSearch(vs);
        try {
            r.propagate();
            for (int i = 0; i < n; i++) {
                Decision d = is.getDecision();
                d.apply();
                r.propagate();
            }
            fail();
        } catch (ContradictionException e) {
            ex = ee.explain(e);
        }
        assertNotNull(ex);
        assertEquals(ex.nbCauses(), 2);
    }

    @Test(groups="10s", timeOut=60000)
    public void testNosol0E() {
        for (int n = 500; n < 4501; n += 500) {
            final Model model = new Model();
            IntVar[] vars = model.intVarArray("p", n, 0, n - 2, false);
            model.arithm(vars[n - 2], "=", vars[n - 1]).post();
            model.arithm(vars[n - 2], "!=", vars[n - 1]).post();
            model.getResolver().set(inputOrderLBSearch(vars));

            Resolver r = model.getResolver();
            model.getResolver().setCBJLearning(false, false);
            assertFalse(model.solve());

            assertEquals(r.getMeasures().getNodeCount(), (n - 2) * 2);
            assertEquals(r.getMeasures().getFailCount(), n - 1);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testNosol0B() {
        for (int n = 500; n < 4501; n += 500) {
            final Model model = new Model();
            IntVar[] vars = model.intVarArray("p", n, 0, n - 2, true);
            model.arithm(vars[n - 2], "=", vars[n - 1]).post();
            model.arithm(vars[n - 2], "!=", vars[n - 1]).post();

            Resolver r = model.getResolver();
            r.set(inputOrderLBSearch(vars));

            r.setCBJLearning(false, false);
            assertFalse(model.solve());

            assertEquals(r.getMeasures().getNodeCount(), (n - 2) * 2);
            assertEquals(r.getMeasures().getFailCount(), n - 1);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testNosol1E() {
        for (int n = 500; n < 4501; n += 500) {
            final Model model = new Model();
            IntVar[] vars = model.intVarArray("p", n, 0, n - 2, false);
            for (int i = 0; i < n - 1; i++) {
                new Constraint(i + ">" + (i + 1), new PropGreaterOrEqualX_YC(new IntVar[]{vars[i], vars[i + 1]}, 1)).post();
            }

            model.getResolver().setCBJLearning(false, false);
            assertFalse(model.solve());

            assertEquals(model.getResolver().getMeasures().getNodeCount(), 0);
            assertEquals(model.getResolver().getMeasures().getFailCount(), 1);
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testNosol1B() {
        for (int n = 500; n < 4501; n += 500) {
            final Model model = new Model();
            IntVar[] vars = model.intVarArray("p", n, 0, n - 2, true);
            for (int i = 0; i < n - 1; i++) {
                new Constraint(i + ">" + (i + 1), new PropGreaterOrEqualX_YC(new IntVar[]{vars[i], vars[i + 1]}, 1)).post();
            }
            model.getResolver().set(inputOrderLBSearch(vars));

            model.getResolver().setCBJLearning(false, false);
            assertFalse(model.solve());

            assertEquals(model.getResolver().getMeasures().getNodeCount(), 0);
            assertEquals(model.getResolver().getMeasures().getFailCount(), 1);
        }
    }


    @Test(groups="1s", timeOut=60000)
    public void testReif() {
        for (long seed = 0; seed < 10; seed++) {

            final Model model = new Model();
            IntVar[] p = model.intVarArray("p", 10, 0, 3, false);
            BoolVar[] bs = model.boolVarArray("b", 2);
            model.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
            model.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
            model.arithm(bs[0], "=", bs[1]).post();

            model.sum(copyOfRange(p, 0, 8), "=", 5).post();
            model.arithm(p[9], "+", p[8], ">", 4).post();
            model.getResolver().set(randomSearch(p, seed));

            model.getResolver().setCBJLearning(false, false);

            showShortStatistics(model);
            assertFalse(model.solve());
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testReif2() { // to test PropagatorActivation, from bs to p

        final Model model = new Model();
        IntVar[] p = model.intVarArray("p", 10, 0, 3, false);
        BoolVar[] bs = model.boolVarArray("b", 2);
        model.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
        model.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
        model.arithm(bs[0], "=", bs[1]).post();

        model.sum(copyOfRange(p, 0, 8), "=", 5).post();
        model.arithm(p[9], "+", p[8], ">", 4).post();
        // p[0], p[1] are just for fun
        Resolver r = model.getResolver();
        r.set(inputOrderLBSearch(p[0], p[1], p[9], p[8], bs[0]));

        r.setCBJLearning(false, false);

        showStatistics(model);
        showSolutions(model);
        showDecisions(model);
        assertFalse(model.solve());

    }

    @Test(groups="1s", timeOut=60000)
    public void testReif3() { // to test PropagatorActivation, from bs to p

        final Model model = new Model();
        IntVar[] p = model.intVarArray("p", 10, 0, 3, false);
        BoolVar[] bs = model.boolVarArray("b", 2);
        model.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
        model.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
        model.arithm(bs[0], "=", bs[1]).post();

        model.sum(copyOfRange(p, 0, 8), "=", 5).post();
        model.arithm(p[9], "+", p[8], ">", 4).post();
        // p[0], p[1] are just for fun
        Resolver r = model.getResolver();
        r.set(inputOrderLBSearch(p[0], p[1], bs[0], p[9], p[8]));

        r.setCBJLearning(false, false);

        showStatistics(model);
        showSolutions(model);
        showDecisions(model);
        assertFalse(model.solve());
    }


    private void configure(Model model, int conf) {
        switch (conf) {
            case 0:
                System.out.printf("noexp  :");
                break;
            case 1: {
                System.out.printf("cbj    :");
                ExplanationFactory.CBJ.plugin(model, false, false);
            }
            break;
            case 2: {
                System.out.printf("cbj+ng :");
                ExplanationFactory.CBJ.plugin(model, true, false);
            }
            break;
            case 3: {
                System.out.printf("dbt    :");
                ExplanationFactory.DBT.plugin(model, false, false);
            }
            break;
            case 4: {
                System.out.printf("dbt+ng :");
                ExplanationFactory.DBT.plugin(model, true, false);
            }
            break;
        }
    }

    private void testLS(int m, int a) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("c", m * m, 0, m - 1, false);
        // Constraints
        for (int i = 0; i < m; i++) {
            IntVar[] row = new IntVar[m];
            IntVar[] col = new IntVar[m];
            for (int x = 0; x < m; x++) {
                row[x] = vars[i * m + x];
                col[x] = vars[x * m + i];
            }
            model.allDifferent(col, "FC").post();
            model.allDifferent(row, "FC").post();
        }
        model.getResolver().set(inputOrderLBSearch(vars));
//        solver.set(ISF.custom(
//                ISF.lexico_var_selector(),
//                ISF.min_value_selector(),
//                ISF.remove(),
//                vars));


        configure(model, a);
        showShortStatistics(model);
        limitTime(model, "5m");
        assertTrue(model.solve() || model.getResolver().hasReachedLimit());
    }

    @Test(groups="5m", timeOut=300000)
    public void testLSsmall() {
        for (int m = 4; m < 18; m++) {
            System.out.printf("LS(%d)\n", m);
            for (int a = 0; a < 5; a++) {
                testLS(m, a);
            }
        }
    }

    private void testCA(int n, int a) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("c", n, 0, n - 1, false);
        IntVar[] vectors = new IntVar[(n * (n - 1)) / 2];
        IntVar[][] diff = new IntVar[n][n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                IntVar k = model.intVar(randomName(), -n, n, false);
                model.arithm(k, "!=", 0).post();
                model.sum(new IntVar[]{vars[i], k}, "=", vars[j]).post();
                vectors[idx] = model.intOffsetView(k, 2 * n * (j - i));
                diff[i][j] = k;
                idx++;
            }
        }
        model.allDifferent(vars, "FC").post();
        model.allDifferent(vectors, "FC").post();

        // symmetry-breaking
        model.arithm(vars[0], "<", vars[n - 1]).post();

        model.getResolver().set(inputOrderLBSearch(vars));

        configure(model, a);
        showShortStatistics(model);
        limitTime(model, "5m");
        assertTrue(model.solve());
    }

    @Test(groups="5m", timeOut=300000)
    public void testCA() {
        for (int n = 6; n < 16; n++) {
            System.out.printf("CA(%d):\n", n);
            for (int a = 0; a < 5; a++) {
                testCA(n, a);
            }
        }
    }

    private void testGR(int m, int a) {
        Model model = new Model();
        IntVar[] ticks = model.intVarArray("a", m, 0, (m < 31) ? (1 << (m + 1)) - 1 : 9999, false);

        model.arithm(ticks[0], "=", 0).post();

        for (int i = 0; i < m - 1; i++) {
            model.arithm(ticks[i + 1], ">", ticks[i]).post();
        }

        IntVar[] diffs = model.intVarArray("d", (m * m - m) / 2, 0, (m < 31) ? (1 << (m + 1)) - 1 : 9999, false);
        IntVar[][] m_diffs = new IntVar[m][m];
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                model.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, "=", diffs[k]).post();
                model.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2).post();
                model.arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2).post();
                model.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2).post();
                m_diffs[i][j] = diffs[k];
            }
        }
        model.allDifferent(diffs, "FC").post();

        // break symetries
        if (m > 2) {
            model.arithm(diffs[0], "<", diffs[diffs.length - 1]).post();
        }

        model.getResolver().set(inputOrderLBSearch(ticks));

        configure(model, a);
        showShortStatistics(model);
        limitTime(model, "5m");
        model.setObjectives(MINIMIZE, ticks[m - 1]);
        model.solve();
        assertTrue(model.getResolver().getMeasures().getSolutionCount() > 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void testGR() {
        for (int m = 6; m < 10; m++) {
            System.out.printf("GR(%d):\n", m);
            for (int a = 0; a < 5; a++) {
                testGR(m, a);
            }
        }
    }

    private void testLN(int n, int k, int a) {
        Model model = new Model();
        IntVar[] position = model.intVarArray("p", n * k, 0, k * n - 1, false);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k - 1; j++) {
                model.arithm(model.intOffsetView(position[i + j * n], i + 2), "=", position[i + (j + 1) * n]).post();
            }
        }
        model.allDifferent(position, "FC").post();
        Resolver r = model.getResolver();
        r.set(minDomUBSearch(position));

        configure(model, a);
        showShortStatistics(model);
        limitTime(model, "5m");
        assertTrue(model.solve());
    }

    @Test(groups="10s", timeOut=60000)
    public void testLN() {
        int[][] params = new int[][]{{2, 3}, {2, 4}, {3, 9}, {3, 17}};
        for (int m = 0; m < params.length; m++) {
            int k = params[m][0];
            int n = params[m][1];
            System.out.printf("LN(%d,%d):\n", k, n);
            for (int a = 0; a < 5; a++) {
                testLN(n, k, a);
            }
        }
    }

    private void testMS(int n, int a) {
        Model model = new Model();
        int ms = n * (n * n + 1) / 2;

        IntVar[][] matrix = new IntVar[n][n];
        IntVar[][] invMatrix = new IntVar[n][n];
        IntVar[] vars = new IntVar[n * n];

        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++, k++) {
                matrix[i][j] = model.intVar("square" + i + "," + j, 1, n * n, false);
                vars[k] = matrix[i][j];
                invMatrix[j][i] = matrix[i][j];
            }
        }

        IntVar[] diag1 = new IntVar[n];
        IntVar[] diag2 = new IntVar[n];
        for (int i = 0; i < n; i++) {
            diag1[i] = matrix[i][i];
            diag2[i] = matrix[(n - 1) - i][i];
        }

        model.allDifferent(vars, "FC").post();

        int[] coeffs = new int[n];
        fill(coeffs, 1);
        for (int i = 0; i < n; i++) {
            model.scalar(matrix[i], coeffs, "=", ms).post();
            model.scalar(invMatrix[i], coeffs, "=", ms).post();
        }
        model.scalar(diag1, coeffs, "=", ms).post();
        model.scalar(diag2, coeffs, "=", ms).post();

        // Symetries breaking
        model.arithm(matrix[0][n - 1], "<", matrix[n - 1][0]).post();
        model.arithm(matrix[0][0], "<", matrix[n - 1][n - 1]).post();
        model.arithm(matrix[0][0], "<", matrix[n - 1][0]).post();

        Resolver r = model.getResolver();
        r.set(intVarSearch(minDomIntVar(), midIntVal(true), vars));

        configure(model, a);
        showShortStatistics(model);
        limitTime(model, "5m");
        assertTrue(model.solve() || r.hasReachedLimit());
    }

    @Test(groups="10s", timeOut=60000)
    public void testMSsmall() {
        for (int n = 5; n < 7; n++) {
            System.out.printf("MS(%d):\n", n);
            for (int a = 0; a < 5; a++) {
                testMS(n, a);
            }
        }
    }

    private void testPA(int N, int a) {
        Model model = new Model();

        int size = N / 2;
        IntVar[] x, y;
        x = model.intVarArray("x", size, 1, 2 * size, false);
        y = model.intVarArray("y", size, 1, 2 * size, false);

        // break symmetries
        for (int i = 0; i < size - 1; i++) {
            model.arithm(x[i], "<", x[i + 1]).post();
            model.arithm(y[i], "<", y[i + 1]).post();
        }
        model.arithm(x[0], "<", y[0]).post();
        model.arithm(x[0], "=", 1).post();

        IntVar[] xy = new IntVar[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            xy[i] = x[i];
            xy[size + i] = y[i];
        }

        IntVar[] Ovars = new IntVar[2 * size];
        for (int i = 0; i < size; i++) {
            Ovars[i * 2] = x[i];
            Ovars[i * 2 + 1] = y[i];
        }

        int[] coeffs = new int[2 * size];
        for (int i = size - 1; i >= 0; i--) {
            coeffs[i] = 1;
            coeffs[size + i] = -1;
        }
        model.scalar(xy, coeffs, "=", 0).post();

        IntVar[] sxy, sx, sy;
        sxy = new IntVar[2 * size];
        sx = new IntVar[size];
        sy = new IntVar[size];
        for (int i = size - 1; i >= 0; i--) {
            sx[i] = model.intVar("x^", 0, x[i].getUB() * x[i].getUB(), true);
            sxy[i] = sx[i];
            sy[i] = model.intVar("y^", 0, y[i].getUB() * y[i].getUB(), true);
            sxy[size + i] = sy[i];
            model.times(x[i], x[i], sx[i]).post();
            model.times(y[i], y[i], sy[i]).post();
            model.member(sx[i], 1, 4 * size * size).post();
            model.member(sy[i], 1, 4 * size * size).post();
        }
        model.scalar(sxy, coeffs, "=", 0).post();

        coeffs = new int[size];
        fill(coeffs, 1);
        model.scalar(x, coeffs, "=", 2 * size * (2 * size + 1) / 4).post();
        model.scalar(y, coeffs, "=", 2 * size * (2 * size + 1) / 4).post();
        model.scalar(sx, coeffs, "=", 2 * size * (2 * size + 1) * (4 * size + 1) / 12).post();
        model.scalar(sy, coeffs, "=", 2 * size * (2 * size + 1) * (4 * size + 1) / 12).post();

        model.allDifferent(xy, "FC").post();


        model.getResolver().set(minDomLBSearch(Ovars));

        configure(model, a);
        showShortStatistics(model);
        limitTime(model, "5m");
        assertTrue(model.solve() || model.getResolver().hasReachedLimit());
    }

    @Test(groups="5m", timeOut=300000)
    public void testPAsmall() {
        int N = 32;
        System.out.printf("Pa(%d)\n", N);
        for (int a = 0; a < 5; a++) {
            testPA(N, a);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testClauses() {
        int n = 4;
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("B", n);
        for (int i = 1; i < n; i++) {
            SatFactory.addBoolEq(bs[0], bs[i]);
        }
        SatFactory.addBoolNot(bs[0], bs[n - 1]);

        ExplanationEngine ee = new ExplanationEngine(model, true, false);
        Resolver r = model.getResolver();
        Explanation ex = null;
        try {
            r.propagate();
            IntStrategy is = inputOrderLBSearch(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            r.propagate();
        } catch (ContradictionException c) {
            ex = ee.explain(c);
        }
        Assert.assertNotNull(ex);
    }

    @Test(groups="1s", timeOut=60000)
    public void testClauses2() {
        int n = 5;
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("B", n);
        SatFactory.addBoolOrArrayEqualTrue(bs); // useless
        SatFactory.addBoolIsLeVar(bs[0], bs[1], bs[2]);
        SatFactory.addBoolIsLeVar(bs[1], bs[0], bs[2]);
        SatFactory.addBoolNot(bs[0], bs[1]);

        ExplanationEngine ee = new ExplanationEngine(model, true, false);
        Explanation ex = null;
        Resolver r = model.getResolver();
        try {
            r.propagate();
            IntStrategy is = inputOrderLBSearch(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            r.propagate();
        } catch (ContradictionException c) {
            ex = ee.explain(c);
        }
        Assert.assertNotNull(ex);
    }

    @Test(groups="1s", timeOut=60000)
    public void testClauses3() {
        int n = 12;
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("B", n);
        SatFactory.addClauses(new BoolVar[]{bs[0], bs[1], bs[2]}, new BoolVar[]{});
        SatFactory.addClauses(new BoolVar[]{bs[0], bs[1]}, new BoolVar[]{bs[2]});
        // pollution
        SatFactory.addClauses(new BoolVar[]{bs[3], bs[4], bs[5]}, new BoolVar[]{bs[1]});
        SatFactory.addClauses(new BoolVar[]{bs[6], bs[7], bs[8]}, new BoolVar[]{});
        SatFactory.addClauses(new BoolVar[]{bs[9], bs[10], bs[11]}, new BoolVar[]{});

        ExplanationEngine ee = new ExplanationEngine(model, true, false);
        Explanation ex = null;
        Resolver r = model.getResolver();
        try {
            r.propagate();
            IntStrategy is = inputOrderLBSearch(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            r.propagate();
            d = is.getDecision();
            d.buildNext();
            d.apply();
            r.propagate();
        } catch (ContradictionException c) {
            ex = ee.explain(c);
        }
        Assert.assertNotNull(ex);
    }

    @Test(groups="1s", timeOut=60000)
    public void testClauses4() {
        int n = 12;
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("B", n);
        SatFactory.addClauses(new BoolVar[]{bs[2]}, new BoolVar[]{bs[0], bs[1]});
        SatFactory.addClauses(new BoolVar[]{}, new BoolVar[]{bs[0], bs[1], bs[2]});

        ExplanationEngine ee = new ExplanationEngine(model, true, false);
        Explanation ex = null;
        Resolver r = model.getResolver();
        try {
            r.propagate();
            IntStrategy is = inputOrderUBSearch(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            r.propagate();
            d = is.getDecision();
            d.buildNext();
            d.apply();
            r.propagate();
        } catch (ContradictionException c) {
            ex = ee.explain(c);
        }
        Assert.assertNotNull(ex);
    }

    @Test(groups="1s", timeOut=60000)
    public void test01() {
        int n = 6;
        int m = 10;
        Model s1 = test(n, m, 1);
        Model s2 = test(n, m, 2);
        Model s3 = test(n, m, 3);
        Assert.assertEquals(s1.getResolver().getMeasures().getSolutionCount(), s2.getResolver().getMeasures().getSolutionCount());
        Assert.assertEquals(s1.getResolver().getMeasures().getSolutionCount(), s3.getResolver().getMeasures().getSolutionCount());
        Assert.assertTrue(s1.getResolver().getMeasures().getNodeCount() >= s2.getResolver().getMeasures().getNodeCount());
        Assert.assertTrue(s2.getResolver().getMeasures().getNodeCount() >= s3.getResolver().getMeasures().getNodeCount());
    }

    private Model test(int n, int m, int expMode) {
        // infeasible problem
        Model s = new Model();
        IntVar[] x = s.intVarArray("x", n, 0, m, true);
        s.allDifferent(x, "NEQS").post();
        s.arithm(x[n - 2], "=", x[n - 1]).post();
        // explanations
        if (expMode == 2) {
            CBJ.plugin(s, false, true);
        } else if (expMode == 3) {
            DBT.plugin(s, false, true);
        }
        // logging and solution
        showStatistics(s);
        showSolutions(s);
        while (s.solve()) ;
        return s;
    }

    @Test(groups="1s", timeOut=60000)
    public void aTest() {

        Model s = new Model();

        IntVar one = s.intVar(1);
        IntVar three = s.intVar(3);
        IntVar four = s.intVar(4);
        IntVar six = s.intVar(6);
        IntVar seven = s.intVar(7);

        IntVar x = s.intVar("x", 1, 10);
        IntVar y = s.intVar("y", 1, 10);

        Constraint xGE3 = s.arithm(x, ">=", three);
        Constraint xLE4 = s.arithm(x, "<=", four);

        Constraint yGE6 = s.arithm(y, ">=", six);
        Constraint yLE7 = s.arithm(y, "<=", seven);

        xGE3.post();
        xLE4.post();
        yGE6.post();
        yLE7.post();

        Constraint xE1 = s.arithm(x, "=", one);
        xE1.post();

        s.getResolver().setCBJLearning(false, true);
        LearnCBJ cbj = (LearnCBJ) s.getResolver().getLearn();
        showDecisions(s);
        assertFalse(s.solve());
        // If the problem has no solution, the end-user explanation can be retrieved
        out.println(cbj.getLastExplanation());
        assertEquals(cbj.getLastExplanation().nbCauses(), 3);
    }


    @Test(groups="1s", timeOut=60000)
    public void testOnce1() {
        Model model = new Model();
        int n = 4;
        IntVar[] X = model.intVarArray("X", 4, 1, 2, false);
        BoolVar[] B = model.boolVarArray("B", 4);
        for (int i = 0; i < n; i++) {
            model.arithm(X[i], ">", i).reifyWith(B[i]);
        }
        CBJ.plugin(model, false, false);
        Resolver r = model.getResolver();
        r.set(inputOrderUBSearch(B), greedySearch(inputOrderLBSearch(X)));
        showDecisions(model);
        showSolutions(model);
        while (model.solve()) ;
    }

    @Test(groups="1s", timeOut=60000)
    public void testIntSat() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", -2, 3, true);
        IntVar y = model.intVar("y", 1, 4, true);
        IntVar z = model.intVar("z", -2, 2, true);

        model.scalar(new IntVar[]{x, y, z}, new int[]{1, -3, -3}, "<=", 1).post();
        model.scalar(new IntVar[]{x, y, z}, new int[]{-2, 3, 2}, "<=", -2).post();
        model.scalar(new IntVar[]{x, y, z}, new int[]{3, -3, 2}, "<=", -1).post();

        ExplanationEngine ee = new ExplanationEngine(model, false, false);

        model.getEnvironment().worldPush();
        model.getResolver().propagate();
        assertEquals(x.getLB(), 1);
        assertEquals(y.getUB(), 2);
        assertEquals(z.getUB(), 0);
        model.getEnvironment().worldPush();
        OnDemandIntStrategy strategy = new OnDemandIntStrategy();
        IntDecision d1 = strategy.makeIntDecision(x, int_split, 2);
        d1.buildNext();
        d1.apply();
        model.getResolver().propagate();
        assertEquals(z.getUB(), -1);
        model.getEnvironment().worldPush();
        IntDecision d2 = strategy.makeIntDecision(x, int_split, 1);
        d2.buildNext();
        d2.apply();
        ContradictionException c = null;
        try {
            model.getResolver().propagate();
            fail();
        } catch (ContradictionException ce) {
            c = ce;
        }
        ee.explain(c);
    }

    @Test(groups="1s", timeOut=60000)
    public void test111() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", 0, 1, true);
        IntVar z = model.intVar("z", 0, 1, true);

        model.scalar(new IntVar[]{x, y, z}, new int[]{1, 1, 1}, "<=", 2).post();

        model.getResolver().propagate();
        out.printf("%s\n", model);
    }

}
