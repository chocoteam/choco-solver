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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.loop.LearnCBJ;
import org.chocosolver.solver.search.loop.SLF;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

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

    @Test(groups="5m", timeOut=300000)
    public void testNosol0() {
        for (int n = 500; n < 4501; n += 500) {
            for (int e = 1; e < engines.length; e++) {
                for (int ng = 0; ng < 2; ng++) {
                    final Solver solver = new Solver();
                    IntVar[] vars = solver.intVarArray("p", n, 0, n - 2, true);
                    solver.post(ICF.arithm(vars[n - 2], "=", vars[n - 1]));
                    solver.post(ICF.arithm(vars[n - 2], "!=", vars[n - 1]));
                    solver.set(ISF.lexico_LB(vars));
                    engines[e].plugin(solver, ng == 1, false);
                    Assert.assertFalse(solver.findSolution());
                    System.out.printf("\t%s", solver.getMeasures().toOneShortLineString());
                    // get the last contradiction, which is
                    if (e > 0) {
                        Assert.assertEquals(solver.getMeasures().getNodeCount(), (n - 2) * 2);
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testUserExpl() {
        int n = 7;
        final Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("p", n, 0, n - 2, false);
        solver.post(ICF.arithm(vars[n - 2], "=", vars[n - 1]));
        solver.post(ICF.arithm(vars[n - 2], "!=", vars[n - 1]));
        solver.set(ISF.lexico_LB(vars));

        SLF.learnCBJ(solver, false, true);
        LearnCBJ cbj = (LearnCBJ) solver.getSearchLoop().getLearn();
        Assert.assertFalse(solver.findSolution());
        Explanation exp = cbj.getLastExplanation();
        Assert.assertEquals(2, exp.nbCauses());
    }

    @Test(groups="10s", timeOut=60000)
    public void testPigeons() {
        for (int n = 5; n < 9; n++) {
            for (long seed = 0; seed < 25; seed++) {
                for (int e = 0; e < engines.length; e++) {
                    for (int ng = 0; ng < 2; ng++) {
                        final Solver solver = new Solver();
                        IntVar[] pigeons = solver.intVarArray("p", n, 0, n - 2, false);
                        solver.post(ICF.alldifferent(pigeons, "NEQS"));
                        solver.set(ISF.random_value(pigeons, seed));
                        engines[e].plugin(solver, ng == 1, false);
                        Assert.assertFalse(solver.findSolution());
                        Chatterbox.printShortStatistics(solver);
                    }
                }
            }
        }
    }

    @Test(groups="5m", timeOut=300000)
    public void testMS() {
        for (int n = 2; n < 5; n++) {
            for (long seed = 0; seed < 25; seed++) {
                for (int e = 0; e < engines.length; e++) {
                    for (int ng = 0; ng < 2; ng++) {
                        int ms = n * (n * n + 1) / 2;

                        final Solver solver = new Solver();
                        IntVar[][] matrix = new IntVar[n][n];
                        IntVar[][] invMatrix = new IntVar[n][n];
                        IntVar[] vars = new IntVar[n * n];

                        int k = 0;
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < n; j++, k++) {
                                matrix[i][j] = solver.intVar("square" + i + "," + j, 1, n * n, false);
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
                        for (int i = 0; i < n; i++) {
                            solver.post(IntConstraintFactory.scalar(matrix[i], coeffs, "=", ms));
                            solver.post(IntConstraintFactory.scalar(invMatrix[i], coeffs, "=", ms));
                        }
                        solver.post(IntConstraintFactory.scalar(diag1, coeffs, "=", ms));
                        solver.post(IntConstraintFactory.scalar(diag2, coeffs, "=", ms));

                        // Symetries breaking
                        solver.post(IntConstraintFactory.arithm(matrix[0][n - 1], "<", matrix[n - 1][0]));
                        solver.post(IntConstraintFactory.arithm(matrix[0][0], "<", matrix[n - 1][n - 1]));
                        solver.post(IntConstraintFactory.arithm(matrix[0][0], "<", matrix[n - 1][0]));
                        solver.set(ISF.random_value(vars, seed));

                        engines[e].plugin(solver, ng == 1, false);
//                    SMF.shortlog(solver);
                        Assert.assertEquals(n > 2, solver.findSolution());
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testReif() {
        for (long seed = 0; seed < 1; seed++) {
            for (int e = 1; e < engines.length - 1; e++) {
                for (int ng = 0; ng < 2; ng++) {
                    final Solver solver = new Solver();
                    IntVar[] p = solver.intVarArray("p", 10, 0, 3, false);
                    BoolVar[] bs = solver.boolVarArray("b", 2);
                    ICF.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
                    ICF.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
                    solver.post(ICF.arithm(bs[0], "=", bs[1]));

                    solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 8), "=", 5));
                    solver.post(ICF.arithm(p[9], "+", p[8], ">", 4));
                    solver.set(ISF.random_value(p, seed));
                    engines[e].plugin(solver, ng == 1, false);
                    Chatterbox.showShortStatistics(solver);
                    Assert.assertFalse(solver.findSolution());
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testReif2() { // to test PropagatorActivation, from bs to p
        for (int e = 0; e < engines.length; e++) {
            for (int ng = 0; ng < 2; ng++) {
                final Solver solver = new Solver();
                IntVar[] p = solver.intVarArray("p", 10, 0, 3, false);
                BoolVar[] bs = solver.boolVarArray("b", 2);
                ICF.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
                ICF.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
                solver.post(ICF.arithm(bs[0], "=", bs[1]));

                solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 8), "=", 5));
                solver.post(ICF.arithm(p[9], "+", p[8], ">", 4));
                // p[0], p[1] are just for fun
                solver.set(ISF.lexico_LB(p[0], p[1], p[9], p[8], bs[0]));
                engines[e].plugin(solver, ng == 1, false);
                Chatterbox.showStatistics(solver);
                Chatterbox.showSolutions(solver);
                Chatterbox.showDecisions(solver);
                Assert.assertFalse(solver.findSolution());
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testReif3() { // to test PropagatorActivation, from bs to p
        for (int e = 0; e < engines.length; e++) {
            for (int ng = 0; ng < 2; ng++) {
                final Solver solver = new Solver();
                IntVar[] p = solver.intVarArray("p", 10, 0, 3, false);
                BoolVar[] bs = solver.boolVarArray("b", 2);
                ICF.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
                ICF.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
                solver.post(ICF.arithm(bs[0], "=", bs[1]));

                solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 8), "=", 5));
                solver.post(ICF.arithm(p[9], "+", p[8], ">", 4));
                // p[0], p[1] are just for fun
                solver.set(ISF.lexico_LB(p[0], p[1], bs[0], p[9], p[8]));
                engines[e].plugin(solver, ng == 1, false);
                Chatterbox.showStatistics(solver);
                Chatterbox.showSolutions(solver);
                Chatterbox.showDecisions(solver);
                Assert.assertFalse(solver.findSolution());
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testLazy() {
        for (int ng = 0; ng < 2; ng++) {
            Solver solver = new Solver();
            // The set of variables
            IntVar[] p = solver.intVarArray("p", 5, 0, 4, false);
            // The initial constraints
            solver.post(ICF.sum(Arrays.copyOfRange(p, 0, 3), ">=", 3));
            solver.post(ICF.arithm(p[2], "+", p[3], ">=", 1));
            solver.post(ICF.arithm(p[3], "+", p[4], ">", 4));

            // The false constraints
            BoolVar[] bs = new BoolVar[2];
            bs[0] = ICF.arithm(p[3], "=", p[4]).reif();
            bs[1] = ICF.arithm(p[3], "!=", p[4]).reif();
            solver.post(ICF.arithm(bs[0], "=", bs[1]));

            solver.set(ISF.lexico_LB(p[0], p[1], bs[0], p[2], p[3], p[4]));
            ExplanationFactory.DBT.plugin(solver, ng == 1, false);
            Chatterbox.showStatistics(solver);
            Chatterbox.showSolutions(solver);
            Chatterbox.showDecisions(solver);
            Assert.assertFalse(solver.findSolution());
        }
    }
}
