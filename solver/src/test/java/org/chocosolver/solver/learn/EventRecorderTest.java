/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.learn;

import org.chocosolver.memory.EnvironmentBuilder;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.learn.LearnSignedClauses;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainRandom;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.selectors.variables.Random;
import org.chocosolver.solver.search.strategy.selectors.variables.Smallest;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.fill;
import static org.chocosolver.solver.search.strategy.Search.*;
import static org.testng.Assert.*;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 27/01/2017.
 */
@SuppressWarnings("Duplicates")
public class EventRecorderTest {


    @Test(groups = {"expl"}, timeOut = 60000)
    public void testNosol0E() {
        for (int n = 500; n < 4501; n += 500) {
            final Model model = new Model();
            IntVar[] vars = model.intVarArray("p", n, 0, n - 2, false);
            model.arithm(vars[n - 2], "=", vars[n - 1]).post();
            model.arithm(vars[n - 2], "!=", vars[n - 1]).post();
            model.getSolver().setSearch(inputOrderLBSearch(vars));

            Solver r = model.getSolver();
            r.setLearningSignedClauses();
            assertFalse(model.getSolver().solve());

            assertEquals(r.getMeasures().getNodeCount(), (n - 2) * 2);
            assertEquals(r.getMeasures().getFailCount(), n - 2);
        }
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testNosol0B() {
        for (int n = 500; n < 4501; n += 500) {
            final Model model = new Model();
            IntVar[] vars = model.intVarArray("p", n, 0, n - 2, true);
            model.arithm(vars[n - 2], "=", vars[n - 1]).post();
            model.arithm(vars[n - 2], "!=", vars[n - 1]).post();

            Solver r = model.getSolver();
            r.setSearch(inputOrderLBSearch(vars));

            model.getSolver().setLearningSignedClauses();
            assertFalse(model.getSolver().solve());

            assertEquals(r.getMeasures().getNodeCount(), (n - 2) * 2);
            assertEquals(r.getMeasures().getFailCount(), n - 2);
        }
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testNosol1E() {
        int m = 15000;
        for (int n = m; n < m+1; n += 500) {
            final Model model = new Model();
            IntVar[] vars = model.intVarArray("p", n, 0, n - 2, false);
            for (int i = 0; i < n - 1; i++) {
                new Constraint(i + ">" + (i + 1), new PropGreaterOrEqualX_YC(new IntVar[]{vars[i], vars[i + 1]}, 1)).post();
            }

            model.getSolver().setLearningSignedClauses();
            assertFalse(model.getSolver().solve());

            assertEquals(model.getSolver().getNodeCount(), 0);
            assertEquals(model.getSolver().getFailCount(), 1);
        }
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testNosol1B() {
        for (int n = 500; n < 4501; n += 500) {
            final Model model = new Model();
            IntVar[] vars = model.intVarArray("p", n, 0, n - 2, true);
            for (int i = 0; i < n - 1; i++) {
                new Constraint(i + ">" + (i + 1), new PropGreaterOrEqualX_YC(new IntVar[]{vars[i], vars[i + 1]}, 1)).post();
            }
            model.getSolver().setSearch(inputOrderLBSearch(vars));

            model.getSolver().setLearningSignedClauses();
            assertFalse(model.getSolver().solve());

            assertEquals(model.getSolver().getNodeCount(), 0);
            assertEquals(model.getSolver().getFailCount(), 1);
        }
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testReif() {
        for (long seed = 0; seed < 1; seed++) {
            int m = 1000;
            final Model model = new Model();
            IntVar[] p = model.intVarArray("p", m, 0, 3, false);
            BoolVar[] bs = model.boolVarArray("b", 2);
            model.arithm(p[m - 1], "=", p[m - 2]).reifyWith(bs[0]);
            model.arithm(p[m - 1], "!=", p[m - 2]).reifyWith(bs[1]);
            model.arithm(bs[0], "=", bs[1]).post();

            model.sum(copyOfRange(p, 0, m - 2), "=", 5).post();
            model.arithm(p[m - 1], "+", p[m - 2], ">", 4).post();
            model.getSolver().setSearch(Search.inputOrderLBSearch(p));
            model.getSolver().setLearningSignedClauses();
            assertFalse(model.getSolver().solve());
            assertEquals(model.getSolver().getNodeCount(),  m - 1);
            assertEquals(model.getSolver().getFailCount(), 2);
        }
    }

    @Test(groups = { "expl"}, timeOut = 60000)
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
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(p[0], p[1], p[9], p[8], bs[0]));

        r.setLearningSignedClauses();

        assertFalse(model.getSolver().solve());
        assertEquals(model.getSolver().getNodeCount(),  4);
        assertEquals(model.getSolver().getFailCount(), 2);

    }

    @Test(groups = { "expl"}, timeOut = 60000)
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
        Solver r = model.getSolver();
        r.setSearch(inputOrderLBSearch(p[0], p[1], bs[0], p[9], p[8]));

        r.setLearningSignedClauses();




        assertFalse(model.getSolver().solve());
        assertEquals(model.getSolver().getNodeCount(),  5);
        assertEquals(model.getSolver().getFailCount(), 2);
    }


    private void configure(Model model, int conf) {
//        model.getSolver().limitFail(10);
//        ClauseStore.PRINT_CLAUSE= true;
//        ExplanationForSignedClause.PROOF= ExplanationForSignedClause.FINE_PROOF = true;
        switch (conf) {
            case 0:
                System.out.print("noexp  :");
                break;
            case 1: {
                System.out.print("SCL    :");
                XParameters.ASSERT_NO_LEFT_BRANCH = false;
                XParameters.INTERVAL_TREE = false;

                model.getSolver().setLearningSignedClauses();
                break;
            }
            case 2: {
                System.out.print("SCL (TREE)    :");
                XParameters.INTERVAL_TREE = true;
                model.getSolver().setLearningSignedClauses();
                break;
            }
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
        model.getSolver().setSearch(inputOrderLBSearch(vars));
        configure(model, a);

        model.getSolver().limitTime("5m");
        assertTrue(model.getSolver().solve() || model.getSolver().isStopCriterionMet());
    }

    @DataProvider(name = "ls")
    public Object[][] dataLS() {
        List<Object[]> elt = new ArrayList<>();
        for (int m = 4; m < 15; m++) {
            for (int a = 0; a < 3; a++) {
                elt.add(new Object[]{m, a});
            }
        }
        return elt.toArray(new Object[elt.size()][2]);
    }

    @Test(groups = { "expl"}, timeOut = 60000, dataProvider = "ls")
    public void testLSsmall(int m, int a) {
        testLS(m, a);
    }

    private void testCA(int n, int a) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("c", n, 0, n - 1, false);
        IntVar[] vectors = new IntVar[(n * (n - 1)) / 2];
        IntVar[][] diff = new IntVar[n][n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                IntVar k = model.intVar(model.generateName(), -n, n, false);
                model.arithm(k, "!=", 0).post();
                model.sum(new IntVar[]{vars[i], k}, "=", vars[j]).post();
                vectors[idx] = model.intOffsetView(k, 2 * n * (j - i));
                diff[i][j] = k;
                idx++;
            }
        }
        model.allDifferent(vars, "BC").post();
        model.allDifferent(vectors, "BC").post();

        // symmetry-breaking
        model.arithm(vars[0], "<", vars[n - 1]).post();

        model.getSolver().setSearch(inputOrderLBSearch(vars));

        configure(model, a);

        model.getSolver().limitTime("5m");
        assertTrue(model.getSolver().solve());
    }

    @DataProvider(name = "ca")
    public Object[][] dataCA() {
        List<Object[]> elt = new ArrayList<>();
        for (int m = 6; m < 15; m++) {
            for (int a = 0; a < 3; a++) {
                elt.add(new Object[]{m, a});
            }
        }
        return elt.toArray(new Object[elt.size()][2]);
    }

    @Test(groups = { "expl"}, timeOut = 60000, dataProvider = "ca")
    public void testsmallCA(int n, int a) {
        testCA(n, a);
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
        model.allDifferent(diffs, "BC").post();

        // break symetries
        if (m > 2) {
            model.arithm(diffs[0], "<", diffs[diffs.length - 1]).post();
        }

        model.getSolver().setSearch(inputOrderLBSearch(ticks));

        configure(model, a);

        model.getSolver().limitTime("50s");
        model.setObjective(Model.MINIMIZE, ticks[m - 1]);
        while (model.getSolver().solve()) ;
        assertTrue(model.getSolver().getSolutionCount() > 0);
    }

    @DataProvider(name = "gr")
    public Object[][] dataGR() {
        List<Object[]> elt = new ArrayList<>();
        for (int m = 6; m < 12; m++) {
            for (int a = 0; a < 3; a++) {
                elt.add(new Object[]{m, a});
            }
        }
        return elt.toArray(new Object[elt.size()][2]);
    }

    @Test(groups = { "expl"}, timeOut = 60000, dataProvider = "gr")
    public void testsmallGR(int m, int a) {
        testGR(m, a);
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
        Solver r = model.getSolver();
        r.setSearch(minDomUBSearch(position));

        configure(model, a);

        model.getSolver().limitTime("5m");
        assertTrue(model.getSolver().solve());
    }

    @DataProvider(name = "ln")
    public Object[][] dataLN() {
        List<Object[]> elt = new ArrayList<>();
        int[][] params = new int[][]{{2, 3}, {2, 4}, {3, 9}, {3, 17}};
        for (int m = 0; m < params.length; m++) {
            int k = params[m][0];
            int n = params[m][1];
            for (int a = 0; a < 3; a++) {
                elt.add(new Object[]{n, k, a});
            }
        }
        return elt.toArray(new Object[elt.size()][3]);
    }

    @Test(groups = { "expl"}, timeOut = 60000, dataProvider = "ln")
    public void testsmallLN(int n, int k, int a) {
        testLN(n, k, a);
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

        Solver r = model.getSolver();
        r.setSearch(intVarSearch(new FirstFail(r.getModel()), new IntDomainMiddle(true), vars));

        configure(model, a);
        r.limitTime("5m");
        assertTrue(r.solve() || r.isStopCriterionMet());
    }

    @DataProvider(name = "ms")
    public Object[][] dataMS() {
        List<Object[]> elt = new ArrayList<>();
        for (int m = 4; m < 7; m++) {
            for (int a = 0; a < 3; a++) {
                elt.add(new Object[]{m, a});
            }
        }
        return elt.toArray(new Object[elt.size()][2]);
    }

    @Test(groups = { "expl"}, timeOut = 60000, dataProvider = "ms")
    public void testMSsmall(int n, int a) {
        testMS(n, a);
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


        model.getSolver().setSearch(minDomLBSearch(Ovars));

        configure(model, a);

        model.getSolver().limitTime("5m");
        assertTrue(model.getSolver().solve() || model.getSolver().isStopCriterionMet());
    }

    @DataProvider(name = "pa")
    public Object[][] dataPA() {
        List<Object[]> elt = new ArrayList<>();
        for (int m = 16; m < 32; m += 4) {
            for (int a = 0; a < 3; a++) {
                elt.add(new Object[]{m, a});
            }
        }
        return elt.toArray(new Object[elt.size()][2]);
    }

    @Test(groups = { "expl"}, timeOut = 60000, dataProvider = "pa")
    public void testPAsmall(int N, int a) {
        testPA(N, a);
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testClauses() {
        int n = 4;
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("B", n);
        for (int i = 1; i < n; i++) {
            model.addClausesBoolEq(bs[0], bs[i]);
        }
        model.addClausesBoolNot(bs[0], bs[n - 1]);

        model.getSolver().setLearningSignedClauses();
        ExplanationForSignedClause ex =
                ((LearnSignedClauses<ExplanationForSignedClause>)model.getSolver().getLearner())
                        .getExplanation();
        Solver r = model.getSolver();
        model.getMinisat().getPropSat().initialize();
        try {
            r.propagate();
            IntStrategy is = inputOrderLBSearch(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            r.propagate();
        } catch (ContradictionException c) {
            ex.learnSignedClause(c);
        }
        Assert.assertNotNull(ex.getLiterals());
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testClauses2() {
        int n = 5;
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("B", n);
        model.addClausesBoolOrArrayEqualTrue(bs); // useless
        model.addClausesBoolIsLeVar(bs[0], bs[1], bs[2]);
        model.addClausesBoolIsLeVar(bs[1], bs[0], bs[2]);
        model.addClausesBoolNot(bs[0], bs[1]);

        model.getSolver().setLearningSignedClauses();
        ExplanationForSignedClause ex =
                ((LearnSignedClauses<ExplanationForSignedClause>)model.getSolver().getLearner())
                        .getExplanation();
        Solver r = model.getSolver();
        model.getMinisat().getPropSat().initialize();
        try {
            r.propagate();
            IntStrategy is = inputOrderLBSearch(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            r.propagate();
        } catch (ContradictionException c) {
            ex.learnSignedClause(c);
        }
        Assert.assertNotNull(ex.getLiterals());
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testClauses3() {
        int n = 12;
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("B", n);
        model.addClauses(new BoolVar[]{bs[0], bs[1], bs[2]}, new BoolVar[]{});
        model.addClauses(new BoolVar[]{bs[0], bs[1]}, new BoolVar[]{bs[2]});
        // pollution
        model.addClauses(new BoolVar[]{bs[3], bs[4], bs[5]}, new BoolVar[]{bs[1]});
        model.addClauses(new BoolVar[]{bs[6], bs[7], bs[8]}, new BoolVar[]{});
        model.addClauses(new BoolVar[]{bs[9], bs[10], bs[11]}, new BoolVar[]{});

        model.getSolver().setLearningSignedClauses();
        ExplanationForSignedClause ex =
                ((LearnSignedClauses<ExplanationForSignedClause>)model.getSolver().getLearner()).getExplanation();
        Solver r = model.getSolver();
        model.getMinisat().getPropSat().initialize();
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
            Assert.fail("failure expected");
        } catch (ContradictionException c) {
            ex.learnSignedClause(c);
        }
        Assert.assertNotNull(ex.getLiterals());
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testClauses4() {
        int n = 12;
        Model model = new Model();
        BoolVar[] bs = model.boolVarArray("B", n);
        model.addClauses(new BoolVar[]{bs[2]}, new BoolVar[]{bs[0], bs[1]});
        model.addClauses(new BoolVar[]{}, new BoolVar[]{bs[0], bs[1], bs[2]});

        model.getSolver().setLearningSignedClauses();
        ExplanationForSignedClause ex =
                ((LearnSignedClauses<ExplanationForSignedClause>)model.getSolver().getLearner()).getExplanation();
        Solver r = model.getSolver();
        model.getMinisat().getPropSat().initialize();
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
            Assert.fail("failure expected");
        } catch (ContradictionException c) {
            ex.learnSignedClause(c);
        }
        Assert.assertNotNull(ex.getLiterals());
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void test01() {
        int n = 6;
        int m = 10;
        Model s1 = test(n, m, 1);
        Model s2 = test(n, m, 2);
        Assert.assertEquals(s1.getSolver().getSolutionCount(), s2.getSolver().getSolutionCount());
        Assert.assertTrue(s1.getSolver().getNodeCount() >= s2.getSolver().getNodeCount());
    }

    private Model test(int n, int m, int expMode) {
        // infeasible problem
        Model s = new Model();
        IntVar[] x = s.intVarArray("x", n, 0, m, true);
        s.allDifferent(x, "NEQS").post();
        s.arithm(x[n - 2], "=", x[n - 1]).post();
        // explanations
        if (expMode == 2) {
            s.getSolver().setLearningSignedClauses();
        }
        // logging and solution
        s.getSolver().setSearch(Search.inputOrderLBSearch(x));
        while (s.getSolver().solve()) ;
        return s;
    }


    @Test(groups = { "expl"}, timeOut = 60000)
    public void testOnce1() {
        Model model = new Model();
        int n = 4;
        IntVar[] X = model.intVarArray("X", 4, 1, 2, false);
        BoolVar[] B = model.boolVarArray("B", 4);
        for (int i = 0; i < n; i++) {
            model.arithm(X[i], ">", i).reifyWith(B[i]);
        }
        Solver r = model.getSolver();
        r.setLearningSignedClauses();
//        s.getSolver().setCBJLearning(true);
        r.setSearch(inputOrderUBSearch(B), greedySearch(inputOrderLBSearch(X)));
//
        XParameters.ASSERT_UNIT_PROP = false;

        while (model.getSolver().solve()) ;
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void testIntSat() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", -2, 3, true);
        IntVar y = model.intVar("y", 1, 4, true);
        IntVar z = model.intVar("z", -2, 2, true);

        model.scalar(new IntVar[]{x, y, z}, new int[]{1, -3, -3}, "<=", 1).post();
        model.scalar(new IntVar[]{x, y, z}, new int[]{-2, 3, 2}, "<=", -2).post();
        model.scalar(new IntVar[]{x, y, z}, new int[]{3, -3, 2}, "<=", -1).post();

        model.getSolver().setLearningSignedClauses();
        ExplanationForSignedClause ex =
                ((LearnSignedClauses<ExplanationForSignedClause>)model.getSolver().getLearner())
                        .getExplanation();

        model.getEnvironment().worldPush();
        model.getSolver().propagate();
        assertEquals(x.getLB(), 1);
        assertEquals(y.getUB(), 2);
        assertEquals(z.getUB(), 0);
        model.getEnvironment().worldPush();
        IntDecision d1 = model.getSolver().getDecisionPath().makeIntDecision(x, DecisionOperatorFactory.makeIntSplit(), 2);
        d1.buildNext();
        d1.apply();
        model.getSolver().propagate();
        assertEquals(z.getUB(), -1);
        model.getEnvironment().worldPush();
        IntDecision d2 = model.getSolver().getDecisionPath().makeIntDecision(x, DecisionOperatorFactory.makeIntSplit(), 1);
        d2.buildNext();
        d2.apply();
        ContradictionException c = null;
        try {
            model.getSolver().propagate();
            fail();
        } catch (ContradictionException ce) {
            c = ce;
        }
        ex.learnSignedClause(c);
    }

    @Test(groups = { "expl"}, timeOut = 60000)
    public void test111() throws ContradictionException {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", 0, 1, true);
        IntVar z = model.intVar("z", 0, 1, true);

        model.scalar(new IntVar[]{x, y, z}, new int[]{1, 1, 1}, "<=", 2).post();

        model.getSolver().propagate();
    }

    @Test(groups="expl", timeOut=60000)
    public void testCumul1(){
        Model model = new Model();
        IntVar start1 = model.intVar("S1", 0, 3, true);
        IntVar start2 = model.intVar("S2", 0, 3, true);
        IntVar dur1 = model.intVar("D1", 2);
        IntVar dur2 = model.intVar("D2", 2);
        IntVar end1 = model.intVar("E1", 2, 6, true);
        IntVar end2 = model.intVar("E2", 2, 6, true);
        IntVar height1 = model.intVar("H1", 2);
        IntVar height2 = model.intVar("H2", 2);
        Task[] tasks = {
                new Task(start1, dur1, end1),
                new Task(start2, dur2, end2)
        };
        model.cumulative(tasks, new IntVar[]{height1, height2}, model.intVar(3), false, Cumulative.Filter.NAIVETIME).post();
        model.arithm(start1, "=", start2).post();
        Solver solver = model.getSolver();
        solver.setLearningSignedClauses();
        solver.setSearch(Search.intVarSearch(
                new InputOrder<>(model),
                new IntDomainMiddle(true),
                DecisionOperatorFactory.makeIntSplit(),
                start1, start2));
//
        solver.findSolution();
    }

    @DataProvider(name = "rcpspP")
    public Object[][] rcpspP(){
        return new Object[][]{
                //{0, 1_380_772, 0, true},
                {1, 148, 0, true}, // 254s
                {1, 154, 0, false}, // 254s
                {2, 148, 0, true},
                {2, 154, 0, false},
        };
    }

    @DataProvider(name = "fuzzy")
    public Object[][] fuzzy() {
        int n = 200;
        Object[][] params = new Object[2 * n][2];
        long seed = System.currentTimeMillis();
        for (int i = 0, k = 0; i < n; i++) {
            params[k++] = new Object[]{seed + i, true};
            params[k++] = new Object[]{seed + i, false};
        }
        return params;
    }

    @Test(groups = { "expl"}, timeOut = 120000, dataProvider = "fuzzy")
    public void testFuzzy1(long seed, boolean iviews) {
        rcpsp(1, 0, seed, iviews);
    }

    @Test(groups = { "expl"}, timeOut = 120000, dataProvider = "fuzzy")
    public void testFuzzy2(long seed, boolean iviews) {
        rcpsp(2, 0, seed, iviews);
    }

    @Test(groups = { "expl"}, timeOut = 120000, dataProvider = "rcpspP")
    public void testRCPSP(int learn, int nbnodes, long seed, boolean eviews) {
        rcpsp(learn, nbnodes, seed, eviews);
    }

    private void rcpsp(int learn, int nbnodes, long seed, boolean eviews) {
        int[] rc = {13, 13, 12, 15};
        int[][] rr = new int[][]{
                {5, 10, 5, 0, 3, 0, 0, 5, 1, 2, 0, 10, 0, 9, 3, 10, 0, 6, 0, 0, 2, 0, 1, 1, 0, 0, 0, 0, 0, 9},
                {0, 0, 0, 0, 0, 0, 4, 0, 4, 0, 10, 0, 6, 0, 5, 4, 1, 6, 6, 0, 8, 0, 0, 0, 3, 0, 9, 7, 0, 0},
                {0, 5, 1, 10, 0, 5, 0, 0, 4, 0, 7, 0, 0, 3, 0, 3, 0, 0, 0, 5, 2, 0, 8, 0, 0, 4, 0, 0, 4, 1},
                {8, 0, 0, 5, 0, 0, 0, 7, 8, 1, 6, 5, 0, 10, 0, 10, 1, 1, 0, 0, 1, 1, 2, 8, 8, 0, 0, 0, 4, 7}
        };
        int[][] suc = {
                {11, 22},
                {4, 7, 16},
                {5, 18, 23},
                {6, 9, 25},
                {15, 21, 27},
                {8},
                {10},
                {23},
                {12, 21},
                {14, 24},
                {25},
                {13},
                {19, 27},
                {23},
                {20},
                {17},
                {26},
                {19, 20, 29},
                {28},
                {22},
                {24, 29},
                {26},
                {26},
                {30},
                {28},
                {28},
                {30},
                {},
                {},
                {}
        };

        Model model = new Model(
                new EnvironmentBuilder()
                        .fromChunk()
                        .build(),
                "rcpcp-00",
                Settings.init()
                        .setHybridizationOfPropagationEngine((byte) 0b00)
                        .setEnableViews(eviews));
        IntVar[] S = model.intVarArray("S", 30, 0, 160, false);
        int[] ds = {10, 4, 1, 3, 5, 10, 1, 4, 6, 8, 7, 7, 4, 3, 10, 3, 4, 3, 7, 5, 1, 10, 8, 1, 6, 4, 7, 6, 9, 4};
        Task[] T = new Task[30];
        for (int i = 0; i < 30; i++) {
            T[i] = new Task(S[i], ds[i]);
        }
        IntVar objective = model.intVar("objective", 0, 161, false);

        // successors
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < suc[i].length; j++) {
                model.arithm(S[i], "+", T[i].getDuration(), "<=", S[suc[i][j] - 1]).post();
            }
            model.arithm(T[i].getEnd(), "<=", objective).post();
        }
        // redundant constraints
        for (int i = 0; i < 29; i++) {
            for (int j = i + 1; j < 30; j++) {
                boolean found = false;
                for (int k = 0; k < 4 && !found; k++) {
                    if (rr[k][i] > 0 && rr[k][j] > 0 && rr[k][i] + rr[k][j] > rc[k]) {
                        BoolVar b1 = model.boolVar();
                        model.reifyXltYC(S[i], S[j], -ds[i] + 1, b1);
                        BoolVar b2 = model.boolVar();
                        model.reifyXltYC(S[j], S[i], -ds[j] + 1, b2);
                        model.arithm(b1, "+", b2, ">", 0).post();
                        found = true;
                    }
                }
            }
        }
        // cumulative
        for (int i = 0; i < rc.length; i++) {
            List<Task> ts = new ArrayList<>();
            List<IntVar> rs = new ArrayList<>();
            for (int j = 0; j < rr[i].length; j++) {
                if (rr[i][j] > 0) {
                    ts.add(T[j]);
                    rs.add(model.intVar(rr[i][j]));
                }
            }
            model.cumulativeTimeDec(
                    ts.stream().map(Task::getStart).toArray(IntVar[]::new),
                    ts.stream().mapToInt(t -> t.getDuration().getLB()).toArray(),
                    rs.stream().mapToInt(IntVar::getLB).toArray(),
                    rc[i]);
        }
        model.setObjective(false, objective);
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(
                seed > 0 && seed % 2 == 0 ? new Random<>(seed): new Smallest(),
                seed < 0 && seed % 3 == 0 ? new IntDomainRandom(seed): new IntDomainMin(),
                ArrayUtils.append(S, new IntVar[]{objective})));
        configure(model, learn);
        Thread t =  new Thread(solver::printShortStatistics);
        Runtime.getRuntime().addShutdownHook(t);
        //solver.findOptimalSolution(objective, false);
        if (solver.solve()) {
            do ;
            while (solver.solve());
        }
        solver.printShortStatistics();
        Assert.assertEquals(solver.getBoundsManager().getBestSolutionValue(), 53, "seed :" + seed);
        if (seed == 0) {
            Assert.assertEquals(solver.getSolutionCount(), 5);
            Assert.assertEquals(solver.getNodeCount(), nbnodes);
        }
        Runtime.getRuntime().removeShutdownHook(t);
    }

}
