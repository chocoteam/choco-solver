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
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.RecorderExplanationEngine;
import org.chocosolver.solver.explanations.arlil.ARLILExplanationEngine;
import org.chocosolver.solver.explanations.arlil.Reason;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        for (int n = 3; n < 1001; n *= 2) {
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
        for (int n = 20; n < 6000; n *= 2) {
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
                Assert.assertEquals(r.nbDeductions(), 1);
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

}
