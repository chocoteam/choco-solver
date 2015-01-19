/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.choco.explanations;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.explanations.PropagatorActivation;
import org.chocosolver.solver.explanations.RecorderExplanationEngine;
import org.chocosolver.solver.explanations.strategies.ConflictBasedBackjumping;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 08/10/2014
 */
public class ExplanationTest {

    private final ExplanationFactory[] engines = {ExplanationFactory.NONE, ExplanationFactory.CBJ, ExplanationFactory.DBT};

    @Test(groups = "10s")
    public void testNosol0() {
        for (int n = 500; n < 4501; n += 500) {
            for (int e = 1; e < engines.length-1; e++) {
                for (int flat = 0; flat < 2; flat++) {
                    final Solver solver = new Solver();
                    IntVar[] vars = VF.boundedArray("p", n, 0, n - 2, solver);
                    solver.post(ICF.arithm(vars[n - 2], "=", vars[n - 1]));
                    solver.post(ICF.arithm(vars[n - 2], "!=", vars[n - 1]));
                    solver.set(ISF.lexico_LB(vars));
                    engines[e].plugin(solver, flat == 1);
                    Assert.assertFalse(solver.findSolution());
                    LoggerFactory.getLogger("test").info("\t{}", solver.getMeasures().toOneShortLineString());
                    // get the last contradiction, which is
                    if (e > 0) {
                        Assert.assertEquals(solver.getMeasures().getNodeCount(), (n - 2) * 2);
                    }
                }
            }
        }
    }

    @Test(groups = "ignored")
    public void testNosolCBJThread1() {
        long pn = 0;
        for (int n = 7; n < 100; n++) {
            for (int flatAndThread = 0; flatAndThread < 4; flatAndThread++) {
                LoggerFactory.getLogger("test").info("\t n = {}, e = {}, f = {}, t = {}", n, ExplanationFactory.CBJ, flatAndThread % 2 == 1, flatAndThread / 2 == 1);
                final Solver solver = new Solver();
                IntVar[] vars = VF.enumeratedArray("p", n, 0, n - 2, solver);
                solver.post(ICF.arithm(vars[n - 2], "=", vars[n - 1]));
                solver.post(ICF.arithm(vars[n - 2], "!=", vars[n - 1]));
                solver.set(ISF.lexico_LB(vars));
//                ExplanationFactory.CBJ.plugin(solver, flatAndThread % 2 == 1);
                ExplanationFactory.plugExpl(solver, flatAndThread % 2 == 1, flatAndThread / 2 == 1);
                new ConflictBasedBackjumping(solver.getExplainer());
                Assert.assertFalse(solver.findSolution());
                LoggerFactory.getLogger("test").info("\t{}", solver.getMeasures().toOneShortLineString());
                // get the last contradiction, which is
                if (flatAndThread == 0) {
                    pn = solver.getMeasures().getNodeCount();
                } else {
                    Assert.assertEquals(solver.getMeasures().getNodeCount(), pn);
                }
            }
        }
    }

    @Test(groups = "1s")
    public void testUserExpl() {
        int n = 7;
        final Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("p", n, 0, n - 2, solver);
        solver.post(ICF.arithm(vars[n - 2], "=", vars[n - 1]));
        solver.post(ICF.arithm(vars[n - 2], "!=", vars[n - 1]));
        solver.set(ISF.lexico_LB(vars));

        solver.set(new RecorderExplanationEngine(solver));
        ConflictBasedBackjumping cbj = new ConflictBasedBackjumping(solver.getExplainer());
        cbj.activeUserExplanation(true);
//            SMF.shortlog(solver);
        Assert.assertFalse(solver.findSolution());
        Explanation exp = cbj.getUserExplanation();
        List<Propagator> pas = new ArrayList<>();
        for (int i = 0; i < exp.nbDeductions(); i++) {
            if (exp.getDeduction(i).getmType() == Explanation.Type.PropAct) {
                pas.add(((PropagatorActivation) exp.getDeduction(i)).getPropagator());
            }
        }
        Assert.assertEquals(2, pas.size());
    }

    @Test(groups = "1s")
    public void testPigeons() {
        for (int n = 5; n < 9; n++) {
            for (long seed = 0; seed < 25; seed++) {
                for (int e = 0; e < engines.length; e++) {
                    final Solver solver = new Solver();
                    IntVar[] pigeons = VF.enumeratedArray("p", n, 0, n - 2, solver);
                    solver.post(ICF.alldifferent(pigeons, "NEQS"));
                    solver.set(ISF.random_value(pigeons, seed));
                    engines[e].plugin(solver, false);
                    Assert.assertFalse(solver.findSolution());
                    Chatterbox.printShortStatistics(solver);
                }
            }
        }
    }

    @Test(groups = "1m")
    public void testMS() {
        for (int n = 2; n < 5; n++) {
            for (long seed = 0; seed < 25; seed++) {
                for (int e = 0; e < engines.length; e++) {
                    int ms = n * (n * n + 1) / 2;

                    final Solver solver = new Solver();
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

                    solver.post(IntConstraintFactory.alldifferent(vars, "NEQS"));

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
                    solver.set(ISF.random_value(vars, seed));

                    engines[e].plugin(solver, false);
//                    SMF.shortlog(solver);
                    Assert.assertEquals(n > 2, solver.findSolution());
                }
            }
        }
    }

    @Test(groups = "1s")
    public void testReif() {
        for (long seed = 0; seed < 1; seed++) {
            for (int e = 1; e < engines.length - 1; e++) {
                final Solver solver = new Solver();
                IntVar[] p = VF.enumeratedArray("p", 10, 0, 3, solver);
                BoolVar[] bs = VF.boolArray("b", 2, solver);
                ICF.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
                ICF.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
                solver.post(ICF.arithm(bs[0], "=", bs[1]));

                solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 8), VF.fixed(5, solver)));
                solver.post(ICF.arithm(p[9], "+", p[8], ">", 4));
                solver.set(ISF.random_value(p, seed));
                engines[e].plugin(solver, false);
                Chatterbox.showShortStatistics(solver);
                Assert.assertFalse(solver.findSolution());
            }
        }
    }

    @Test(groups = "1s")
    public void testReif2() { // to test PropagatorActivation, from bs to p
        for (int e = 0; e < engines.length; e++) {
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
            engines[e].plugin(solver, false);
            Chatterbox.showStatistics(solver);
            Chatterbox.showSolutions(solver);
            Chatterbox.showDecisions(solver);
            Assert.assertFalse(solver.findSolution());
        }
    }

    @Test(groups = "1s")
    public void testReif3() { // to test PropagatorActivation, from bs to p
        for (int e = 0; e < engines.length; e++) {
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
            engines[e].plugin(solver, false);
            Chatterbox.showStatistics(solver);
            Chatterbox.showSolutions(solver);
            Chatterbox.showDecisions(solver);
            Assert.assertFalse(solver.findSolution());
        }
    }

    @Test(groups = "1s")
    public void testLazy() {
        Solver solver = new Solver();
        // The set of variables
        IntVar[] p = VF.enumeratedArray("p", 5, 0, 4, solver);
        // The initial constraints
        solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 3), ">=", VF.fixed(3, solver)));
        solver.post(ICF.arithm(p[2], "+", p[3], ">=", 1));
        solver.post(ICF.arithm(p[3], "+", p[4], ">", 4));

        // The false constraints
        BoolVar[] bs = new BoolVar[2];
        bs[0] = ICF.arithm(p[3], "=", p[4]).reif();
        bs[1] = ICF.arithm(p[3], "!=", p[4]).reif();
        solver.post(ICF.arithm(bs[0], "=", bs[1]));

        solver.set(ISF.lexico_LB(p[0], p[1], bs[0], p[2], p[3], p[4]));
        ExplanationFactory.DBT.plugin(solver, false);
        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        Chatterbox.showDecisions(solver);
        Assert.assertFalse(solver.findSolution());
    }
}
