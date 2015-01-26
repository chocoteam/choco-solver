/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.explanations.arlil;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.SatFactory;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.explanations.RecorderExplanationEngine;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.StringUtils;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public class ARLILExplanationEngineTest {


    /**
     * This test evaluates the case where half of the generated events are useless.
     * Only one branch is evaluated.
     */
    @Test(groups = "1s")
    public void test1() {
        for (int n = 6; n < 1001; n *= 2) {
            System.out.printf("n = %d : ", n);
            Solver solver = new Solver();
            IntVar[] vs = VF.enumeratedArray("V", n, 1, n - 1, solver);
            for (int i = 0; i < n - 1; i++) {
                solver.post(new Constraint(i + ">" + (i + 1), new PropGreaterOrEqualX_YC(new IntVar[]{vs[i], vs[i + 1]}, 1)));
            }
            Solver arlil = solver.duplicateModel();

            long t = -System.currentTimeMillis();
            {
                RecorderExplanationEngine expler = new RecorderExplanationEngine(solver);
                solver.set(expler);
                expler.beforeInitialPropagation();
                Explanation ex = null;
                try {
                    solver.propagate();
                    Assert.fail();
                } catch (ContradictionException e) {
                    ex = expler.flatten(e.explain(expler));
                }
                Assert.assertNotNull(ex);
                Assert.assertEquals(ex.nbDeductions(), n - 1);
            }
            t += System.currentTimeMillis();
            System.out.printf("%.3fs vs. ", t / 1000d);

            t = -System.currentTimeMillis();
            {
                ARLILExplanationEngine ee = new ARLILExplanationEngine(arlil, false);
                Reason r = null;
                try {
                    arlil.propagate();
                    Assert.fail();
                } catch (ContradictionException e) {
                    r = ee.explain(e);
                }
                Assert.assertNotNull(r);
                Assert.assertEquals(r.nbCauses(), n - 1);
            }
            t += System.currentTimeMillis();
            System.out.printf("%.3fs \n", t / 1000d);
        }
    }

    /**
     * This test evaluates the case where only two constraints are in conflict, the remaining ones are useless.
     */
    @Test(groups = "1s")
    public void test2() {
        for (int n = 100; n < 12801; n *= 2) {
            System.out.printf("n = %d : ", n);
            Solver solver = new Solver();
            IntVar[] vs = VF.enumeratedArray("V", 2, 0, n, solver);
            solver.post(new Constraint("0>1", new PropGreaterOrEqualX_YC(new IntVar[]{vs[0], vs[1]}, 1)));
            solver.post(new Constraint("0<1", new PropGreaterOrEqualX_YC(new IntVar[]{vs[1], vs[0]}, 1)));

            Solver arlil = solver.duplicateModel();

            long t = -System.currentTimeMillis();
            {
                RecorderExplanationEngine expler = new RecorderExplanationEngine(solver);
                solver.set(expler);
                expler.beforeInitialPropagation();
                Explanation ex = null;
                try {
                    solver.propagate();
                    Assert.fail();
                } catch (ContradictionException e) {
                    ex = expler.flatten(e.explain(expler));
                }
                Assert.assertNotNull(ex);
                Assert.assertEquals(ex.nbDeductions(), 2);
            }
            t += System.currentTimeMillis();
            System.out.printf("%.3fs vs. ", t / 1000d);

            t = -System.currentTimeMillis();
            {
                ARLILExplanationEngine ee = new ARLILExplanationEngine(arlil, false);
                Reason r = null;
                try {
                    arlil.propagate();
                    Assert.fail();
                } catch (ContradictionException e) {
                    r = ee.explain(e);
                }
                Assert.assertNotNull(r);
                Assert.assertEquals(r.nbCauses(), 2);
            }
            t += System.currentTimeMillis();
            System.out.printf("%.3fs \n", t / 1000d);
        }
    }

    /**
     * This test evaluates the case where a subset of the constraints are in conflict
     */
    @Test(groups = "1s")
    public void test3() {
        for (int n = 3; n < 64000; n *= 2) {
            System.out.printf("n = %d : ", n);
            Solver solver = new Solver();
            IntVar[] vs = VF.boundedArray("V", n, 2, n + 2, solver);
            solver.post(ICF.arithm(vs[n - 2], "=", vs[n - 1]));
            solver.post(ICF.arithm(vs[n - 2], "!=", vs[n - 1]));

            Solver arlil = solver.duplicateModel();

            long t = -System.currentTimeMillis();
            {
                IntStrategy is = ISF.lexico_LB(solver.retrieveIntVars());

                RecorderExplanationEngine expler = new RecorderExplanationEngine(solver);
                solver.set(expler);
                expler.beforeInitialPropagation();
                Explanation ex = null;
                try {
                    solver.propagate();
                    for (int i = 0; i < n; i++) {
                        Decision d = is.getDecision();
                        d.buildNext();
                        d.apply();
                        solver.propagate();
                    }
                    Assert.fail();
                } catch (ContradictionException e) {
                    ex = expler.flatten(e.explain(expler));
                }
                Assert.assertNotNull(ex);
                Assert.assertEquals(ex.nbDeductions(), 3);
            }
            t += System.currentTimeMillis();
            System.out.printf("%.3fs vs. ", t / 1000d);

            t = -System.currentTimeMillis();
            {
                IntStrategy is = ISF.lexico_LB(arlil.retrieveIntVars());
                ARLILExplanationEngine ee = new ARLILExplanationEngine(arlil, false);
                Reason r = null;
                try {
                    arlil.propagate();
                    for (int i = 0; i < n; i++) {
                        Decision d = is.getDecision();
                        d.buildNext();
                        d.apply();
                        arlil.propagate();
                    }
                    Assert.fail();
                } catch (ContradictionException e) {
                    r = ee.explain(e);
                }
                Assert.assertNotNull(r);
                Assert.assertEquals(r.nbCauses(), 2);
                Assert.assertEquals(r.nbDecisions(), 1);
            }
            t += System.currentTimeMillis();
            System.out.printf("%.3fs \n", t / 1000d);
        }
    }

    /**
     * This test evaluates the case where a subset of the constraints are in conflict
     */
    @Test(groups = "1s")
    public void test4() {
        int n = 3;
        System.out.printf("n = %d : ", n);
        Solver solver = new Solver();
        IntVar[] vs = VF.enumeratedArray("V", n, 0, n, solver);
        solver.post(new Constraint((n - 2) + ">" + (n - 1), new PropGreaterOrEqualX_YC(new IntVar[]{vs[n - 2], vs[n - 1]}, 1)));
        solver.post(new Constraint((n - 2) + "<" + (n - 1), new PropGreaterOrEqualX_YC(new IntVar[]{vs[n - 1], vs[n - 2]}, 1)));

        IntStrategy is = ISF.lexico_LB(vs);
        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
        Reason r = null;
        try {
            solver.propagate();
            for (int i = 0; i < n; i++) {
                Decision d = is.getDecision();
                d.apply();
                solver.propagate();
            }
            Assert.fail();
        } catch (ContradictionException e) {
            r = ee.explain(e);
        }
        Assert.assertNotNull(r);
        Assert.assertEquals(r.nbCauses(), 2);
    }

    @Test(groups = "10s")
    public void testNosol0E() {
        for (int n = 500; n < 4501; n += 500) {
            final Solver solver = new Solver();
            IntVar[] vars = VF.enumeratedArray("p", n, 0, n - 2, solver);
            solver.post(ICF.arithm(vars[n - 2], "=", vars[n - 1]));
            solver.post(ICF.arithm(vars[n - 2], "!=", vars[n - 1]));
            solver.set(ISF.lexico_LB(vars));

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver, false);
            solver.plugMonitor(cbj);
            Assert.assertFalse(solver.findSolution());
            LoggerFactory.getLogger("test").info("\t{}", solver.getMeasures().toOneShortLineString());

            Assert.assertEquals(solver.getMeasures().getNodeCount(), (n - 2) * 2);
            Assert.assertEquals(solver.getMeasures().getFailCount(), n - 1);
        }
    }

    @Test(groups = "10s")
    public void testNosol0B() {
        for (int n = 500; n < 4501; n += 500) {
            final Solver solver = new Solver();
            IntVar[] vars = VF.boundedArray("p", n, 0, n - 2, solver);
            solver.post(ICF.arithm(vars[n - 2], "=", vars[n - 1]));
            solver.post(ICF.arithm(vars[n - 2], "!=", vars[n - 1]));
            solver.set(ISF.lexico_LB(vars));

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver, false);
            solver.plugMonitor(cbj);
            Assert.assertFalse(solver.findSolution());
            LoggerFactory.getLogger("test").info("\t{}", solver.getMeasures().toOneShortLineString());

            Assert.assertEquals(solver.getMeasures().getNodeCount(), (n - 2) * 2);
            Assert.assertEquals(solver.getMeasures().getFailCount(), n - 1);
        }
    }

    @Test(groups = "10s")
    public void testNosol1E() {
        for (int n = 500; n < 4501; n += 500) {
            final Solver solver = new Solver();
            IntVar[] vars = VF.enumeratedArray("p", n, 0, n - 2, solver);
            for (int i = 0; i < n - 1; i++) {
                solver.post(new Constraint(i + ">" + (i + 1), new PropGreaterOrEqualX_YC(new IntVar[]{vars[i], vars[i + 1]}, 1)));
            }

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver, false);
            solver.plugMonitor(cbj);
            Assert.assertFalse(solver.findSolution());
            LoggerFactory.getLogger("test").info("\t{}", solver.getMeasures().toOneShortLineString());

            Assert.assertEquals(solver.getMeasures().getNodeCount(), 0);
            Assert.assertEquals(solver.getMeasures().getFailCount(), 0);
        }
    }

    @Test(groups = "10s")
    public void testNosol1B() {
        for (int n = 500; n < 4501; n += 500) {
            final Solver solver = new Solver();
            IntVar[] vars = VF.boundedArray("p", n, 0, n - 2, solver);
            for (int i = 0; i < n - 1; i++) {
                solver.post(new Constraint(i + ">" + (i + 1), new PropGreaterOrEqualX_YC(new IntVar[]{vars[i], vars[i + 1]}, 1)));
            }
            solver.set(ISF.lexico_LB(vars));

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver, false);
            solver.plugMonitor(cbj);
            Assert.assertFalse(solver.findSolution());
            LoggerFactory.getLogger("test").info("\t{}", solver.getMeasures().toOneShortLineString());

            Assert.assertEquals(solver.getMeasures().getNodeCount(), (n - 2) * 2);
            Assert.assertEquals(solver.getMeasures().getFailCount(), n - 1);
        }
    }


    @Test(groups = "1s")
    public void testReif() {
        for (long seed = 0; seed < 10; seed++) {

            final Solver solver = new Solver();
            IntVar[] p = VF.enumeratedArray("p", 10, 0, 3, solver);
            BoolVar[] bs = VF.boolArray("b", 2, solver);
            ICF.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
            ICF.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
            solver.post(ICF.arithm(bs[0], "=", bs[1]));

            solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 8), VF.fixed(5, solver)));
            solver.post(ICF.arithm(p[9], "+", p[8], ">", 4));
            solver.set(ISF.random_value(p, seed));

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver, false);
            solver.plugMonitor(cbj);

            Chatterbox.showShortStatistics(solver);
            Assert.assertFalse(solver.findSolution());
        }
    }

    @Test(groups = "1s")
    public void testReif2() { // to test PropagatorActivation, from bs to p

        final Solver solver = new Solver();
        IntVar[] p = VF.enumeratedArray("p", 10, 0, 3, solver);
        BoolVar[] bs = VF.boolArray("b", 2, solver);
        ICF.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
        ICF.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
        solver.post(ICF.arithm(bs[0], "=", bs[1]));

        solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 8), VF.fixed(5, solver)));
        solver.post(ICF.arithm(p[9], "+", p[8], ">", 4));
        // p[0], p[1] are just for fun
        solver.set(ISF.lexico_LB(p[0], p[1], p[9], p[8], bs[0]));

        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
        CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver, false);
        solver.plugMonitor(cbj);

        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        Assert.assertFalse(solver.findSolution());

    }

    @Test(groups = "1s")
    public void testReif3() { // to test PropagatorActivation, from bs to p

        final Solver solver = new Solver();
        IntVar[] p = VF.enumeratedArray("p", 10, 0, 3, solver);
        BoolVar[] bs = VF.boolArray("b", 2, solver);
        ICF.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
        ICF.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
        solver.post(ICF.arithm(bs[0], "=", bs[1]));

        solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 8), VF.fixed(5, solver)));
        solver.post(ICF.arithm(p[9], "+", p[8], ">", 4));
        // p[0], p[1] are just for fun
        solver.set(ISF.lexico_LB(p[0], p[1], bs[0], p[9], p[8]));

        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
        CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver, false);
        solver.plugMonitor(cbj);

        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        Assert.assertFalse(solver.findSolution());
    }


    private void configure(Solver solver, int conf) {
        switch (conf) {
            case 0:
                System.out.printf("noexp; ");
                break;
            case 1: {
                System.out.printf("arlil1; ");
                ExplanationFactory.CBJ.plugin(solver, false, false);
            }
            break;
            case 2: {
                System.out.printf("arlil2; ");
                ExplanationFactory.CBJ.plugin(solver, true, false);
            }
            break;
            case 3: {
                System.out.printf("arlil3; ");
                ExplanationFactory.DBT.plugin(solver, false, false);
            }
            break;
            case 4: {
                System.out.printf("arlil4; ");
                ExplanationFactory.DBT.plugin(solver, true, false);
            }
            break;
        }
    }

    private void testLS(int m, int a) {
        Solver solver = new Solver();
        IntVar[] vars = VariableFactory.enumeratedArray("c", m * m, 0, m - 1, solver);
        // Constraints
        for (int i = 0; i < m; i++) {
            IntVar[] row = new IntVar[m];
            IntVar[] col = new IntVar[m];
            for (int x = 0; x < m; x++) {
                row[x] = vars[i * m + x];
                col[x] = vars[x * m + i];
            }
            solver.post(IntConstraintFactory.alldifferent(col, "FC"));
            solver.post(IntConstraintFactory.alldifferent(row, "FC"));
        }
        solver.set(IntStrategyFactory.lexico_LB(vars));

        configure(solver, a);
        Chatterbox.showShortStatistics(solver);
        SMF.limitTime(solver, "5m");
        Assert.assertTrue(solver.findSolution() || solver.hasReachedLimit());
    }

    @Test(groups = "1m")
    public void testLSsmall() {
        for (int m = 4; m < 18; m++) {
            System.out.printf("LS(%d)\n", m);
            for (int a = 0; a < 5; a++) {
                testLS(m, a);
            }
        }
    }


    @Test(groups = "30m")
    public void testLSbig() {
        for (int m = 18; m < 24; m++) {
            System.out.printf("LS(%d)\n", m);
            for (int a = 0; a < 5; a++) {
                testLS(m, a);
            }
        }
    }

    private void testCA(int n, int a) {
        Solver solver = new Solver();
        IntVar[] vars = VariableFactory.enumeratedArray("c", n, 0, n - 1, solver);
        IntVar[] vectors = new IntVar[(n * (n - 1)) / 2];
        IntVar[][] diff = new IntVar[n][n];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                IntVar k = VariableFactory.enumerated(StringUtils.randomName(), -n, n, solver);
                solver.post(ICF.arithm(k, "!=", 0));
                solver.post(IntConstraintFactory.sum(new IntVar[]{vars[i], k}, vars[j]));
                vectors[idx] = VariableFactory.offset(k, 2 * n * (j - i));
                diff[i][j] = k;
                idx++;
            }
        }
        solver.post(IntConstraintFactory.alldifferent(vars, "FC"));
        solver.post(IntConstraintFactory.alldifferent(vectors, "FC"));

        // symmetry-breaking
        solver.post(ICF.arithm(vars[0], "<", vars[n - 1]));

        solver.set(IntStrategyFactory.lexico_LB(vars));

        configure(solver, a);
        Chatterbox.showShortStatistics(solver);
        SMF.limitTime(solver, "5m");
        Assert.assertTrue(solver.findSolution());
    }

    @Test(groups = "1m")
    public void testCA() {
        for (int n = 6; n < 16; n++) {
            System.out.printf("CA(%d):\n", n);
            for (int a = 0; a < 5; a++) {
                testCA(n, a);
            }
        }
    }

    private void testGR(int m, int a) {
        Solver solver = new Solver();
        IntVar[] ticks = VariableFactory.enumeratedArray("a", m, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);

        solver.post(IntConstraintFactory.arithm(ticks[0], "=", 0));

        for (int i = 0; i < m - 1; i++) {
            solver.post(IntConstraintFactory.arithm(ticks[i + 1], ">", ticks[i]));
        }

        IntVar[] diffs = VariableFactory.enumeratedArray("d", (m * m - m) / 2, 0, ((m < 31) ? (1 << (m + 1)) - 1 : 9999), solver);
        IntVar[][] m_diffs = new IntVar[m][m];
        for (int k = 0, i = 0; i < m - 1; i++) {
            for (int j = i + 1; j < m; j++, k++) {
                // d[k] is m[j]-m[i] and must be at least sum of first j-i integers
                solver.post(IntConstraintFactory.scalar(new IntVar[]{ticks[j], ticks[i]}, new int[]{1, -1}, diffs[k]));
                solver.post(IntConstraintFactory.arithm(diffs[k], ">=", (j - i) * (j - i + 1) / 2));
                solver.post(IntConstraintFactory.arithm(diffs[k], "-", ticks[m - 1], "<=", -((m - 1 - j + i) * (m - j + i)) / 2));
                solver.post(IntConstraintFactory.arithm(diffs[k], "<=", ticks[m - 1], "-", ((m - 1 - j + i) * (m - j + i)) / 2));
                m_diffs[i][j] = diffs[k];
            }
        }
        solver.post(IntConstraintFactory.alldifferent(diffs, "FC"));

        // break symetries
        if (m > 2) {
            solver.post(IntConstraintFactory.arithm(diffs[0], "<", diffs[diffs.length - 1]));
        }

        solver.set(IntStrategyFactory.lexico_LB(ticks));

        configure(solver, a);
        Chatterbox.showShortStatistics(solver);
        SMF.limitTime(solver, "5m");
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, ticks[m - 1]);
        Assert.assertTrue(solver.getMeasures().getSolutionCount() > 0);
    }

    @Test(groups = "1m")
    public void testGR() {
        for (int m = 6; m < 10; m++) {
            System.out.printf("GR(%d):\n", m);
            for (int a = 0; a < 5; a++) {
                testGR(m, a);
            }
        }
    }

    private void testLN(int n, int k, int a) {
        Solver solver = new Solver();
        IntVar[] position = VariableFactory.enumeratedArray("p", n * k, 0, k * n - 1, solver);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k - 1; j++) {
                solver.post(IntConstraintFactory.arithm(VariableFactory.offset(position[i + j * n], i + 2), "=", position[i + (j + 1) * n]));
            }
        }
        solver.post(IntConstraintFactory.alldifferent(position, "FC"));
        solver.set(IntStrategyFactory.minDom_UB(position));

        configure(solver, a);
        Chatterbox.showShortStatistics(solver);
        SMF.limitTime(solver, "5m");
        Assert.assertTrue(solver.findSolution());
    }

    @Test(groups = "1m")
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
        Solver solver = new Solver();
        int ms = n * (n * n + 1) / 2;

        IntVar[][] matrix = new IntVar[n][n];
        IntVar[][] invMatrix = new IntVar[n][n];
        IntVar[] vars = new IntVar[n * n];

        int k = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++, k++) {
                matrix[i][j] = VariableFactory.enumerated("square" + i + "," + j, 1, n * n, solver);
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

        solver.post(IntConstraintFactory.alldifferent(vars, "FC"));

        int[] coeffs = new int[n];
        Arrays.fill(coeffs, 1);
        IntVar msv = VariableFactory.fixed(ms, solver);
        for (int i = 0; i < n; i++) {
            solver.post(IntConstraintFactory.scalar(matrix[i], coeffs, msv));
            solver.post(IntConstraintFactory.scalar(invMatrix[i], coeffs, msv));
        }
        solver.post(IntConstraintFactory.scalar(diag1, coeffs, msv));
        solver.post(IntConstraintFactory.scalar(diag2, coeffs, msv));

        // Symetries breaking
        solver.post(IntConstraintFactory.arithm(matrix[0][n - 1], "<", matrix[n - 1][0]));
        solver.post(IntConstraintFactory.arithm(matrix[0][0], "<", matrix[n - 1][n - 1]));
        solver.post(IntConstraintFactory.arithm(matrix[0][0], "<", matrix[n - 1][0]));

        solver.set(IntStrategyFactory.minDom_MidValue(vars));

        configure(solver, a);
        Chatterbox.showShortStatistics(solver);
        SMF.limitTime(solver, "5m");
        Assert.assertTrue(solver.findSolution() || solver.hasReachedLimit());
    }

    @Test(groups = "1m")
    public void testMSsmall() {
        for (int n = 5; n < 7; n++) {
            System.out.printf("MS(%d):\n", n);
            for (int a = 0; a < 5; a++) {
                testMS(n, a);
            }
        }
    }

    @Test(groups = "30m")
    public void testMSbig() {
        for (int n = 7; n < 12; n++) {
            System.out.printf("MS(%d):\n", n);
            for (int a = 0; a < 5; a++) {
                testMS(n, a);
            }
        }
    }

    private void testPA(int N, int a) {
        Solver solver = new Solver();

        int size = N / 2;
        IntVar[] x, y;
        x = VariableFactory.enumeratedArray("x", size, 1, 2 * size, solver);
        y = VariableFactory.enumeratedArray("y", size, 1, 2 * size, solver);

        // break symmetries
        for (int i = 0; i < size - 1; i++) {
            solver.post(IntConstraintFactory.arithm(x[i], "<", x[i + 1]));
            solver.post(IntConstraintFactory.arithm(y[i], "<", y[i + 1]));
        }
        solver.post(IntConstraintFactory.arithm(x[0], "<", y[0]));
        solver.post(IntConstraintFactory.arithm(x[0], "=", 1));

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
        solver.post(IntConstraintFactory.scalar(xy, coeffs, VariableFactory.fixed(0, solver)));

        IntVar[] sxy, sx, sy;
        sxy = new IntVar[2 * size];
        sx = new IntVar[size];
        sy = new IntVar[size];
        for (int i = size - 1; i >= 0; i--) {
            sx[i] = VF.bounded("x^", 0, x[i].getUB() * x[i].getUB(), solver);
            sxy[i] = sx[i];
            sy[i] = VF.bounded("y^", 0, y[i].getUB() * y[i].getUB(), solver);
            sxy[size + i] = sy[i];
            solver.post(IntConstraintFactory.times(x[i], x[i], sx[i]));
            solver.post(IntConstraintFactory.times(y[i], y[i], sy[i]));
            solver.post(IntConstraintFactory.member(sx[i], 1, 4 * size * size));
            solver.post(IntConstraintFactory.member(sy[i], 1, 4 * size * size));
        }
        solver.post(IntConstraintFactory.scalar(sxy, coeffs, VariableFactory.fixed(0, solver)));

        coeffs = new int[size];
        Arrays.fill(coeffs, 1);
        solver.post(IntConstraintFactory.scalar(x, coeffs, VariableFactory.fixed(2 * size * (2 * size + 1) / 4, solver)));
        solver.post(IntConstraintFactory.scalar(y, coeffs, VariableFactory.fixed(2 * size * (2 * size + 1) / 4, solver)));
        solver.post(IntConstraintFactory.scalar(sx, coeffs, VariableFactory.fixed(2 * size * (2 * size + 1) * (4 * size + 1) / 12, solver)));
        solver.post(IntConstraintFactory.scalar(sy, coeffs, VariableFactory.fixed(2 * size * (2 * size + 1) * (4 * size + 1) / 12, solver)));

        solver.post(IntConstraintFactory.alldifferent(xy, "FC"));


        solver.set(IntStrategyFactory.minDom_LB(Ovars));

        configure(solver, a);
        Chatterbox.showShortStatistics(solver);
        SMF.limitTime(solver, "5m");
        Assert.assertTrue(solver.findSolution() || solver.hasReachedLimit());
    }

    @Test(groups = "1m")
    public void testPAsmall() {
        for (int N = 40; N < 57; N += 4) {
            System.out.printf("Pa(%d)\n", N);
            for (int a = 0; a < 5; a++) {
                testPA(N, a);
            }
        }
    }

    @Test(groups = "30m")
    public void testPAbig() {
        for (int N = 56; N < 73; N += 4) {
            System.out.printf("Pa(%d)\n", N);
            for (int a = 0; a < 5; a++) {
                testPA(N, a);
            }
        }
    }

    @Test(groups = "1s")
    public void testClauses() {
        int n = 4;
        Solver solver = new Solver();
        BoolVar[] bs = VF.boolArray("B", n, solver);
        for (int i = 1; i < n; i++) {
            SatFactory.addBoolEq(bs[0], bs[i]);
        }
        SatFactory.addBoolNot(bs[0], bs[n - 1]);

        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
        Reason r = null;
        try {
            solver.propagate();
            IntStrategy is = ISF.lexico_LB(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            solver.propagate();
        } catch (ContradictionException c) {
            r = ee.explain(c);
        }
        Assert.assertNotNull(r);
    }

    @Test(groups = "1s")
    public void testClauses2() {
        int n = 5;
        Solver solver = new Solver();
        BoolVar[] bs = VF.boolArray("B", n, solver);
        SatFactory.addBoolOrArrayEqualTrue(bs); // useless
        SatFactory.addBoolIsLeVar(bs[0], bs[1], bs[2]);
        SatFactory.addBoolIsLeVar(bs[1], bs[0], bs[2]);
        SatFactory.addBoolNot(bs[0], bs[1]);

        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
        Reason r = null;
        try {
            solver.propagate();
            IntStrategy is = ISF.lexico_LB(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            solver.propagate();
        } catch (ContradictionException c) {
            r = ee.explain(c);
        }
        Assert.assertNotNull(r);
    }

    @Test(groups = "1s")
    public void testClauses3() {
        int n = 12;
        Solver solver = new Solver();
        BoolVar[] bs = VF.boolArray("B", n, solver);
        SatFactory.addClauses(new BoolVar[]{bs[0], bs[1], bs[2]}, new BoolVar[]{});
        SatFactory.addClauses(new BoolVar[]{bs[0], bs[1]}, new BoolVar[]{bs[2]});
        // pollution
        SatFactory.addClauses(new BoolVar[]{bs[3], bs[4], bs[5]}, new BoolVar[]{bs[1]});
        SatFactory.addClauses(new BoolVar[]{bs[6], bs[7], bs[8]}, new BoolVar[]{});
        SatFactory.addClauses(new BoolVar[]{bs[9], bs[10], bs[11]}, new BoolVar[]{});

        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver, false);
        Reason r = null;
        try {
            solver.propagate();
            IntStrategy is = ISF.lexico_LB(bs);
            Decision d = is.getDecision();
            d.buildNext();
            d.apply();
            solver.propagate();
            d = is.getDecision();
            d.buildNext();
            d.apply();
            solver.propagate();
        } catch (ContradictionException c) {
            r = ee.explain(c);
        }
        Assert.assertNotNull(r);
    }

}
