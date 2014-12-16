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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.explanations.RecorderExplanationEngine;
import org.chocosolver.solver.explanations.arlil.strategies.CBJ4ARLIL;
import org.chocosolver.solver.explanations.strategies.ConflictBasedBackjumping;
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
                ARLILExplanationEngine ee = new ARLILExplanationEngine(arlil);
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
                ARLILExplanationEngine ee = new ARLILExplanationEngine(arlil);
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
                ARLILExplanationEngine ee = new ARLILExplanationEngine(arlil);
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
        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
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

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
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

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
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

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
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

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
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

            ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
            CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
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

        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
        CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
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

        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
        CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
        solver.plugMonitor(cbj);

        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        Assert.assertFalse(solver.findSolution());
    }

    @Test(groups = "10m")
    public void testLS() {
        for (int m = 15; m < 19; m++) {
            System.out.printf("LS(%d)\n", m);
            for (int a = 0; a < 4; a+=3) {
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
                    solver.post(IntConstraintFactory.alldifferent(col, "NEQS"));
                    solver.post(IntConstraintFactory.alldifferent(row, "NEQS"));
                }
                solver.set(IntStrategyFactory.lexico_LB(vars));

                switch (a) {
                    case 0:
                        System.out.printf("noexp; ");
                        break;
                    case 1:
                        System.out.printf("flatt; ");
                        ExplanationFactory.plugExpl(solver, true, false);
                        new ConflictBasedBackjumping(solver.getExplainer());
                        break;
                    case 2:
                        System.out.printf("unfla; ");
                        ExplanationFactory.plugExpl(solver, false, false);
                        new ConflictBasedBackjumping(solver.getExplainer());
                        break;
                    case 3:
                        System.out.printf("arlil; ");
                        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
                        CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
                        solver.plugMonitor(cbj);
                        break;
                }
                Chatterbox.showShortStatistics(solver);
                SMF.limitTime(solver, "5m");
                solver.findSolution();
            }
        }
    }

    @Test(groups = "10m")
    public void testCA() {
        for (int n = 6; n < 12; n++) {
            System.out.printf("CA(%d):\n", n);
            for (int a = 3; a < 4; a++) {
                Solver solver = new Solver();
                IntVar[] vars = VariableFactory.enumeratedArray("c", n * n, 0, n - 1, solver);
                IntVar[] vectors = new IntVar[(n*(n-1))/2];
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
                solver.post(IntConstraintFactory.alldifferent(vars, "NEQS"));
                solver.post(IntConstraintFactory.alldifferent(vectors, "NEQS"));

                // symmetry-breaking
                solver.post(ICF.arithm(vars[0], "<", vars[n - 1]));

                solver.set(IntStrategyFactory.lexico_LB(vars));

                switch (a) {
                    case 0:
                        System.out.printf("noexp: ");
                        break;
                    case 1:
                        System.out.printf("flatt: ");
                        ExplanationFactory.plugExpl(solver, true, false);
                        new ConflictBasedBackjumping(solver.getExplainer());
                        break;
                    case 2:
                        System.out.printf("unfla: ");
                        ExplanationFactory.plugExpl(solver, false, false);
                        new ConflictBasedBackjumping(solver.getExplainer());
                        break;
                    case 3:
                        System.out.printf("arlil: ");
                        ARLILExplanationEngine ee = new ARLILExplanationEngine(solver);
                        CBJ4ARLIL cbj = new CBJ4ARLIL(ee, solver);
                        solver.plugMonitor(cbj);
                        break;
                }
                Chatterbox.showShortStatistics(solver);
//                Chatterbox.showDecisions(solver, () -> solver.getMeasures().toOneShortLineString());
//                SMF.limitNode(solver, 44);
                solver.findSolution();
//                if(a < 2){
//                    nc = solver.getMeasures().getFailCount();
//                }else{
//                    Assert.assertEquals(solver.getMeasures().getFailCount(), nc);
//                }
            }
        }
    }

}
