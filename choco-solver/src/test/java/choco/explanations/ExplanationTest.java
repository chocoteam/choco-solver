/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package choco.explanations;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Configuration;
import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.explanations.Explanation;
import solver.explanations.ExplanationFactory;
import solver.explanations.RecorderExplanationEngine;
import solver.explanations.strategies.ConflictBasedBackjumping;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.ISF;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VF;
import solver.variables.VariableFactory;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 08/10/2014
 */
public class ExplanationTest {

    private final ExplanationFactory[] engines = {ExplanationFactory.NONE, ExplanationFactory.CBJ, ExplanationFactory.DBT};

    @Test(groups = "1s")
    public void testNosol() {
        long pn = 0;
        for (int n = 5; n < 9; n++) {
            for (int e = 0; e < engines.length; e++) {
                final Solver solver = new Solver();
                IntVar[] vars = VF.enumeratedArray("p", n, 0, n - 2, solver);
                solver.post(ICF.arithm(vars[n - 2], "=", vars[n - 1]));
                solver.post(ICF.arithm(vars[n - 2], "!=", vars[n - 1]));
                solver.set(ISF.lexico_LB(vars));
                engines[e].plugin(solver, false);
//                SMF.shortlog(solver);
                Assert.assertFalse(solver.findSolution());
                // get the last contradiction, which is
                if (e == 0) {
                    pn = solver.getMeasures().getNodeCount();
                } else {
                    Assert.assertTrue(solver.getMeasures().getNodeCount() <= pn);
                }
            }
        }
    }

    @Test(groups = "1s")
    public void testUserExpl() {
        if (Configuration.PROP_IN_EXP) {
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
            Assert.assertEquals(2, exp.nbPropagators());
        }
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
//                    SMF.shortlog(solver);
                    Assert.assertFalse(solver.findSolution());
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
                SMF.shortlog(solver);
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
            SMF.log(solver,true, true);
            SMF.shortlog(solver);
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
            SMF.log(solver,true, true);
            SMF.shortlog(solver);
            Assert.assertFalse(solver.findSolution());
        }
    }
}
