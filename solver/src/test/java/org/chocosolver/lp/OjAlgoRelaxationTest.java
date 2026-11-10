/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.lp;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Unit tests for {@link OjAlgoRelaxation}, driving the {@link ILinearRelaxation} API directly.
 * <p>
 * The LP-correctness cases (objective values and exact reduced costs) are lifted from ojAlgo's own
 * {@code org.ojalgo.optimisation.linear.ReducedCostsTest}. ojAlgo asserts those reduced costs on the
 * bounded-variable (dual) simplex path, which is exactly the path {@link OjAlgoRelaxation} runs
 * ({@code OPTIONS.linear().dual()}), so the same values must come out here.
 * <p>
 * The interface optimises a single target column, whereas these cases minimise a general {@code c·x}. We
 * bridge that with an auxiliary column {@code t} and a linking equality {@code t = c·x}, then optimise
 * {@code t} - precisely how {@link PropSimplex} drives the relaxation. The linking equality has dual 1 at the
 * optimum, so the decision variables' reduced costs are unchanged; {@code t}'s own reduced cost is ~0.
 */
public class OjAlgoRelaxationTest {

    /**
     * Build "optimise c·x s.t. (aLe)x &le; bLe, (aEq)x == bEq, lb &le; x &le; ub" through the single-target
     * API, by adding an auxiliary objective column {@code t} with {@code t == c·x} and optimising it.
     * Inequality/equality blocks may be {@code null} when absent.
     */
    private static OjAlgoRelaxation build(double[][] aLe, double[] bLe,
                                          double[][] aEq, double[] bEq,
                                          double[] c, int[] lb, int[] ub, boolean maximise) {
        int n = c.length;
        int t = n; // auxiliary objective column
        OjAlgoRelaxation lp = new OjAlgoRelaxation(n + 1);
        if (bLe != null) {
            for (int i = 0; i < bLe.length; i++) {
                ILinearRelaxation.RowMutator row = lp.addInequalityConstraint(bLe[i]);
                for (int j = 0; j < n; j++) {
                    if (aLe[i][j] != 0.) {
                        row.add(j, aLe[i][j]);
                    }
                }
            }
        }
        if (bEq != null) {
            for (int i = 0; i < bEq.length; i++) {
                ILinearRelaxation.RowMutator row = lp.addEqualityConstraint(bEq[i]);
                for (int j = 0; j < n; j++) {
                    if (aEq[i][j] != 0.) {
                        row.add(j, aEq[i][j]);
                    }
                }
            }
        }
        // t - c·x == 0
        ILinearRelaxation.RowMutator obj = lp.addEqualityConstraint(0.);
        obj.add(t, 1.);
        for (int j = 0; j < n; j++) {
            if (c[j] != 0.) {
                obj.add(j, -c[j]);
            }
        }
        lp.setObjective(t, maximise);
        for (int j = 0; j < n; j++) {
            lp.updateRange(j, lb[j], ub[j]);
        }
        lp.updateRange(t, -1_000_000, 1_000_000);
        return lp;
    }

    private static void assertOptimum(String id, OjAlgoRelaxation lp, double expectedValue, double[] expectedRc) {
        assertValue(id, lp, expectedValue);
        double[] rc = lp.getReducedCosts();
        for (int j = 0; j < expectedRc.length; j++) {
            Assert.assertEquals(rc[j], expectedRc[j], 1e-6, id + " rc[" + j + "]");
        }
    }

    /** Asserts feasibility, the objective value, and that all reduced costs are finite (no exact rc). */
    private static void assertValue(String id, OjAlgoRelaxation lp, double expectedValue) {
        Assert.assertEquals(lp.solve(), ILinearRelaxation.Status.OPTIMAL, id + ": expected feasible");
        Assert.assertEquals(lp.getObjectiveValue(), expectedValue, 1e-6, id + " value");
        double[] rc = lp.getReducedCosts();
        for (int j = 0; j < rc.length; j++) {
            Assert.assertFalse(Double.isNaN(rc[j]) || Double.isInfinite(rc[j]), id + " rc[" + j + "] must be finite");
        }
    }

    /**
     * ReducedCostsTest#testMinInequalities: min -x-2y s.t. x+y&le;4, x&le;3, x,y in [0,10]. Optimum -8 at
     * {0,4}. Here x sits at a bound while the binding constraint's dual is shared with the basic y, so the
     * reduced costs are path-dependent (ojAlgo's own doVerify only checks these via complementary slackness,
     * not strict equality) - we assert the objective value and leave rc at the well-formed check.
     */
    @Test(groups = "1s")
    public void minInequalities() {
        OjAlgoRelaxation lp = build(new double[][]{{1, 1}}, new double[]{4}, null, null,
                new double[]{-1, -2}, new int[]{0, 0}, new int[]{3, 10}, false);
        assertValue("minInequalities", lp, -8.);
    }

    /** ReducedCostsTest#testMaxInequalities: max x+2y s.t. x+y&le;4, x&le;3 (bound), x,y in [0,10]. Optimum 8
     * at {0,4} - same dual-degenerate case as {@link #minInequalities()}, so rc stays at the well-formed check. */
    @Test(groups = "1s")
    public void maxInequalities() {
        OjAlgoRelaxation lp = build(new double[][]{{1, 1}}, new double[]{4}, null, null,
                new double[]{1, 2}, new int[]{0, 0}, new int[]{3, 10}, true);
        assertValue("maxInequalities", lp, 8.);
    }

    /** ReducedCostsTest#testTallModel: min -3x-2y-z s.t. x+y+z&le;10, x&le;3, y&le;4 (as bounds), z in [0,10]. */
    @Test(groups = "1s")
    public void tallModel() {
        OjAlgoRelaxation lp = build(new double[][]{{1, 1, 1}}, new double[]{10}, null, null,
                new double[]{-3, -2, -1}, new int[]{0, 0, 0}, new int[]{3, 4, 10}, false);
        assertOptimum("tallModel", lp, -20., new double[]{-2, -1, 0});
    }

    /** ReducedCostsTest#testVariableAtUpperBound: min -3x-2y s.t. x+y&le;10, x in [0,4], y in [0,10]. */
    @Test(groups = "1s")
    public void variableAtUpperBound() {
        OjAlgoRelaxation lp = build(new double[][]{{1, 1}}, new double[]{10}, null, null,
                new double[]{-3, -2}, new int[]{0, 0}, new int[]{4, 10}, false);
        assertOptimum("variableAtUpperBound", lp, -24., new double[]{-1, 0});
    }

    /** ReducedCostsTest#testWideModel: min -x1-2x2-3x3-4x4-10x5 s.t. sum&le;8, xi in [0,5]. */
    @Test(groups = "1s")
    public void wideModel() {
        OjAlgoRelaxation lp = build(new double[][]{{1, 1, 1, 1, 1}}, new double[]{8}, null, null,
                new double[]{-1, -2, -3, -4, -10}, new int[]{0, 0, 0, 0, 0}, new int[]{5, 5, 5, 5, 5}, false);
        assertOptimum("wideModel", lp, -62., new double[]{3, 2, 1, 0, -6});
    }

    /** ReducedCostsTest#testNegativeBounds: min x+2y s.t. x+y&ge;0, x in [-5,5], y in [-3,3]. */
    @Test(groups = "1s")
    public void negativeBounds() {
        // x + y >= 0  ->  -x - y <= 0
        OjAlgoRelaxation lp = build(new double[][]{{-1, -1}}, new double[]{0}, null, null,
                new double[]{1, 2}, new int[]{-5, -3}, new int[]{5, 3}, false);
        assertOptimum("negativeBounds", lp, -3., new double[]{0, 1});
    }

    /** ReducedCostsTest#testEqualityConstraint: min 3x+2y s.t. x+y==5, x,y in [0,10]. */
    @Test(groups = "1s")
    public void equalityConstraint() {
        OjAlgoRelaxation lp = build(null, null, new double[][]{{1, 1}}, new double[]{5},
                new double[]{3, 2}, new int[]{0, 0}, new int[]{10, 10}, false);
        assertOptimum("equalityConstraint", lp, 10., new double[]{1, 0});
    }

    /**
     * ReducedCostsTest#testFreeVariable: min x-y s.t. y&le;3, x==0, x free, y in (-inf,5]. The two
     * single-variable constraints (x==0, y&le;3) are carried as variable bounds: x fixed to [0,0], y capped
     * at its tighter UB. y's "unbounded below" is approximated by a wide finite lower bound.
     */
    @Test(groups = "1s")
    public void freeVariable() {
        OjAlgoRelaxation lp = build(null, null, null, null,
                new double[]{1, -1}, new int[]{0, -1_000_000}, new int[]{0, 3}, false);
        assertOptimum("freeVariable", lp, -3., new double[]{1, -1});
    }

    /** ReducedCostsTest#testFixedByPresolve: min x+3y s.t. x+y&le;10, y&ge;5, x in [0,10], y in [0,5]. */
    @Test(groups = "1s")
    public void fixedByPresolve() {
        // y >= 5  ->  -y <= -5
        OjAlgoRelaxation lp = build(new double[][]{{1, 1}, {0, -1}}, new double[]{10, -5}, null, null,
                new double[]{1, 3}, new int[]{0, 0}, new int[]{10, 5}, false);
        Assert.assertEquals(lp.solve(), ILinearRelaxation.Status.OPTIMAL, "fixedByPresolve: expected feasible");
        Assert.assertEquals(lp.getObjectiveValue(), 15., 1e-6, "fixedByPresolve value");
        double[] rc = lp.getReducedCosts();
        for (int j = 0; j < rc.length; j++) {
            Assert.assertFalse(Double.isNaN(rc[j]) || Double.isInfinite(rc[j]), "fixedByPresolve rc[" + j + "] must be finite");
        }
    }

    /** An infeasible LP must report infeasible. */
    @Test(groups = "1s")
    public void infeasibleIsDetected() {
        // x in [0,1] with x >= 2  ->  -x <= -2
        OjAlgoRelaxation lp = new OjAlgoRelaxation(1);
        ILinearRelaxation.RowMutator row = lp.addInequalityConstraint(-2.);
        row.add(0, -1.);
        lp.setObjective(0, false);
        lp.updateRange(0, 0, 1);
        Assert.assertEquals(lp.solve(), ILinearRelaxation.Status.INFEASIBLE, "infeasible LP should report infeasible");
    }

    /** Tightening a variable range via updateRange must be reflected by the next solve. */
    @Test(groups = "1s")
    public void rangeAffectsNextSolve() {
        // max x, x in [0,10], with a non-binding x <= 100
        OjAlgoRelaxation lp = new OjAlgoRelaxation(1);
        ILinearRelaxation.RowMutator row = lp.addInequalityConstraint(100.);
        row.add(0, 1.);
        lp.setObjective(0, true);
        lp.updateRange(0, 0, 10);
        Assert.assertEquals(lp.solve(), ILinearRelaxation.Status.OPTIMAL, "before tightening: feasible");
        Assert.assertEquals(lp.getObjectiveValue(), 10., 1e-6, "before tightening");
        lp.updateRange(0, 0, 4);
        Assert.assertEquals(lp.solve(), ILinearRelaxation.Status.OPTIMAL, "after tightening: feasible");
        Assert.assertEquals(lp.getObjectiveValue(), 4., 1e-6, "after tightening");
    }

    /**
     * Smoke test for the {@link OjAlgoDebugRelaxation} backend: it must solve to the same optimum through the
     * same {@link ILinearRelaxation} API, and expose the model for debugging/export.
     */
    @Test(groups = "1s")
    public void debugRelaxationSolvesAndExposesModel() {
        // tallModel: min -3x-2y-z s.t. x+y+z<=10, x<=3, y<=4 (as bounds), z in [0,10]; objective via t=c*x.
        OjAlgoDebugRelaxation lp = new OjAlgoDebugRelaxation(4); // x, y, z, t
        ILinearRelaxation.RowMutator c1 = lp.addInequalityConstraint(10.);
        c1.add(0, 1.);
        c1.add(1, 1.);
        c1.add(2, 1.);
        ILinearRelaxation.RowMutator link = lp.addEqualityConstraint(0.); // t + 3x + 2y + z == 0
        link.add(3, 1.);
        link.add(0, 3.);
        link.add(1, 2.);
        link.add(2, 1.);
        lp.setObjective(3, false);
        lp.updateRange(0, 0, 3);
        lp.updateRange(1, 0, 4);
        lp.updateRange(2, 0, 10);
        lp.updateRange(3, -1_000_000, 1_000_000);

        Assert.assertEquals(lp.solve(), ILinearRelaxation.Status.OPTIMAL, "debug relaxation: expected feasible");
        Assert.assertEquals(lp.getObjectiveValue(), -20., 1e-6, "debug relaxation objective");
        double[] rc = lp.getReducedCosts();
        for (int j = 0; j < rc.length; j++) {
            Assert.assertFalse(Double.isNaN(rc[j]) || Double.isInfinite(rc[j]), "debug relaxation rc[" + j + "] must be finite");
        }

        Assert.assertNotNull(lp.getModel(), "debug relaxation exposes the ojAlgo model");
        String dump = lp.toString();
        Assert.assertTrue(dump != null && !dump.isEmpty(), "debug relaxation toString should render the model");
    }

    /**
     * Replay a recorded relaxation: the rows and the objective, the initial ranges ({@code bnd}), then the
     * range updates ({@code upd}) and the solves of a search, all on the same relaxation.
     * One item per line: {@code n N}, {@code obj J C}, {@code eq RHS J:A ...}, {@code le RHS J:A ...},
     * {@code bnd J LO UP}, {@code upd J LO UP} and {@code solve}; {@code #} starts a comment.
     *
     * @return the outcome of the last solve
     */
    private static boolean replay(OjAlgoRelaxation[] holder, String resource) throws IOException {
        OjAlgoRelaxation lp = null;
        boolean feasible = false;
        boolean solved = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(OjAlgoRelaxationTest.class.getResourceAsStream(resource)),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] t = line.split(" ");
                switch (t[0]) {
                    case "n":
                        lp = new OjAlgoRelaxation(Integer.parseInt(t[1]));
                        break;
                    case "obj":
                        lp.setObjective(Integer.parseInt(t[1]), Double.parseDouble(t[2]) < 0);
                        break;
                    case "eq":
                    case "le":
                        ILinearRelaxation.RowMutator row = t[0].equals("eq")
                                ? lp.addEqualityConstraint(Double.parseDouble(t[1]))
                                : lp.addInequalityConstraint(Double.parseDouble(t[1]));
                        for (int k = 2; k < t.length; k++) {
                            String[] e = t[k].split(":");
                            row.add(Integer.parseInt(e[0]), Double.parseDouble(e[1]));
                        }
                        break;
                    case "bnd":
                    case "upd":
                        lp.updateRange(Integer.parseInt(t[1]), Integer.parseInt(t[2]), Integer.parseInt(t[3]));
                        break;
                    case "solve":
                        feasible = lp.solve() == ILinearRelaxation.Status.OPTIMAL;
                        solved = true;
                        break;
                    default:
                        throw new IllegalArgumentException(line);
                }
            }
        }
        if (!solved) {
            feasible = lp.solve() == ILinearRelaxation.Status.OPTIMAL;
        }
        holder[0] = lp;
        return feasible;
    }

    /**
     * A feasible relaxation that the sparse dual simplex of ojAlgo 57.1.0 reports infeasible when built from
     * scratch.
     */
    @Test(groups = "1s")
    public void testFeasibleDespiteInfeasibleVerdictCold() throws IOException {
        OjAlgoRelaxation[] lp = new OjAlgoRelaxation[1];
        Assert.assertTrue(replay(lp, "/lp/steiner-cold.txt"), "the relaxation is feasible");
        Assert.assertEquals(lp[0].getObjectiveValue(), 16702536., 1e-3);
    }

    /**
     * A feasible relaxation that the sparse dual simplex of ojAlgo 57.1.0 reports infeasible on the 34th solve
     * of a reused solver instance.
     */
    @Test(groups = "1s")
    public void testFeasibleDespiteInfeasibleVerdictWarm() throws IOException {
        OjAlgoRelaxation[] lp = new OjAlgoRelaxation[1];
        Assert.assertTrue(replay(lp, "/lp/steiner-warm.txt"), "the relaxation is feasible");
        Assert.assertEquals(lp[0].getObjectiveValue(), 16551763., 1e-3);
    }

    /**
     * A feasible relaxation that the dual simplex of ojAlgo 57.1.1 to 57.4.0 reports infeasible on a reused
     * solver instance, once 55 ranges have been relaxed in place.
     *
     * @see <a href="https://github.com/optimatika/ojAlgo/issues/699">ojAlgo #699</a>
     */
    @Test(groups = "1s")
    public void testFeasibleDespiteInfeasibleVerdictAfterRelaxation() throws IOException {
        OjAlgoRelaxation[] lp = new OjAlgoRelaxation[1];
        Assert.assertTrue(replay(lp, "/lp/steiner-relax-699.txt"), "the relaxation is feasible");
        Assert.assertEquals(lp[0].getObjectiveValue(), 17556214., 1e-3);
    }

    /**
     * A feasible relaxation that the sparse dual simplex of ojAlgo 57.5.0 reports infeasible on a reused solver
     * instance, once 4 ranges have been tightened in place.
     *
     * @see <a href="https://github.com/optimatika/ojAlgo/issues/704">ojAlgo #704</a>
     */
    @Test(groups = "1s")
    public void testFeasibleDespiteInfeasibleVerdictAfterTightening() throws IOException {
        OjAlgoRelaxation[] lp = new OjAlgoRelaxation[1];
        Assert.assertTrue(replay(lp, "/lp/fm3-tightening-704.txt"), "the relaxation is feasible");
    }

    /**
     * The confirmation of infeasible verdicts, off by default since ojAlgo 57.6.0, still works when turned on: it
     * finds nothing to overturn on the scenario of ojAlgo #704.
     */
    @Test(groups = "1s")
    public void testConfirmationOfInfeasibleVerdicts() throws IOException {
        System.setProperty(OjAlgoRelaxation.CONFIRM_INFEASIBLE_PROPERTY, "true");
        try {
            OjAlgoRelaxation[] lp = new OjAlgoRelaxation[1];
            Assert.assertTrue(replay(lp, "/lp/fm3-tightening-704.txt"), "the relaxation is feasible");
            Assert.assertEquals(lp[0].getNbOverturnedVerdicts(), 0);
        } finally {
            System.clearProperty(OjAlgoRelaxation.CONFIRM_INFEASIBLE_PROPERTY);
        }
    }

    @Test(groups = "1s")
    public void testDenseConfirmationOnlyForSmallProblems() {
        Assert.assertTrue(OjAlgoRelaxation.fitsDense(100, 500));
        Assert.assertTrue(OjAlgoRelaxation.fitsDense(1_000, 2_000));
        // the instances that ran out of memory: tens of thousands of variables
        Assert.assertFalse(OjAlgoRelaxation.fitsDense(2_000, 75_000));
        Assert.assertFalse(OjAlgoRelaxation.fitsDense(40_000, 500));
    }

    /**
     * An infeasible LP too large for a dense tableau: its verdict is confirmed with a sparse solver.
     */
    @Test(groups = "1s")
    public void testInfeasibleVerdictOfALargeProblem() {
        int n = 2_100;
        Assert.assertFalse(OjAlgoRelaxation.fitsDense(n - 1, n));
        OjAlgoRelaxation lp = new OjAlgoRelaxation(n);
        // x_0 <= x_1 <= ... <= x_{n-1}, with x_0 = 1 and x_{n-1} = 0
        for (int i = 0; i + 1 < n; i++) {
            ILinearRelaxation.RowMutator row = lp.addInequalityConstraint(0.);
            row.add(i, 1.);
            row.add(i + 1, -1.);
        }
        lp.updateRange(0, 1, 1);
        for (int i = 1; i + 1 < n; i++) {
            lp.updateRange(i, 0, 1);
        }
        lp.updateRange(n - 1, 0, 0);
        Assert.assertEquals(lp.solve(), ILinearRelaxation.Status.INFEASIBLE);
        Assert.assertEquals(lp.getNbOverturnedVerdicts(), 0);
    }

    /**
     * The certificate of an infeasible LP, when the version of ojAlgo provides one, only blames the bounds that
     * make it infeasible.
     */
    @Test(groups = "1s")
    public void testInfeasibilityCertificate() {
        int n = 5;
        OjAlgoRelaxation lp = new OjAlgoRelaxation(n + 1);
        // x_0 <= x_1 <= ... <= x_{n-1}, with x_0 >= 1 and x_{n-1} <= 0; x_n is not involved
        for (int i = 0; i + 1 < n; i++) {
            ILinearRelaxation.RowMutator row = lp.addInequalityConstraint(0.);
            row.add(i, 1.);
            row.add(i + 1, -1.);
        }
        lp.updateRange(0, 1, 5);
        for (int i = 1; i + 1 < n; i++) {
            lp.updateRange(i, -5, 5);
        }
        lp.updateRange(n - 1, -5, 0);
        lp.updateRange(n, -5, 5);
        Assert.assertEquals(lp.solve(), ILinearRelaxation.Status.INFEASIBLE);
        double[] g = lp.getInfeasibilityCertificate();
        if (g != null) {
            // the lower bound of x_0 and the upper bound of x_{n-1}, and nothing else
            Assert.assertTrue(g[0] > 0, Arrays.toString(g));
            Assert.assertTrue(g[n - 1] < 0, Arrays.toString(g));
            for (int i = 1; i + 1 < n; i++) {
                Assert.assertEquals(g[i], 0., 1e-9, Arrays.toString(g));
            }
            Assert.assertEquals(g[n], 0., 1e-9, Arrays.toString(g));
        }
    }
}
