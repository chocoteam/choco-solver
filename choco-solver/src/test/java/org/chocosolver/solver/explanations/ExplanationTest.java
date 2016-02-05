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
import org.chocosolver.solver.search.loop.LearnCBJ;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static java.lang.System.out;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.fill;
import static org.chocosolver.solver.explanations.ExplanationFactory.DBT;
import static org.chocosolver.solver.search.loop.SearchLoopFactory.learnCBJ;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.lexico_LB;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.random_value;
import static org.chocosolver.solver.trace.Chatterbox.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

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
                    final Model model = new Model();
                    IntVar[] vars = model.intVarArray("p", n, 0, n - 2, true);
                    model.arithm(vars[n - 2], "=", vars[n - 1]).post();
                    model.arithm(vars[n - 2], "!=", vars[n - 1]).post();
                    model.set(lexico_LB(vars));
                    engines[e].plugin(model, ng == 1, false);
                    assertFalse(model.findSolution());
                    out.printf("\t%s", model.getMeasures().toOneShortLineString());
                    // get the last contradiction, which is
                    if (e > 0) {
                        assertEquals(model.getMeasures().getNodeCount(), (n - 2) * 2);
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testUserExpl() {
        int n = 7;
        final Model model = new Model();
        IntVar[] vars = model.intVarArray("p", n, 0, n - 2, false);
        model.arithm(vars[n - 2], "=", vars[n - 1]).post();
        model.arithm(vars[n - 2], "!=", vars[n - 1]).post();
        model.set(lexico_LB(vars));

        learnCBJ(model, false, true);
        LearnCBJ cbj = (LearnCBJ) model.getResolver().getLearn();
        assertFalse(model.findSolution());
        Explanation exp = cbj.getLastExplanation();
        assertEquals(2, exp.nbCauses());
    }

    @Test(groups="10s", timeOut=60000)
    public void testPigeons() {
        for (int n = 5; n < 9; n++) {
            for (long seed = 0; seed < 25; seed++) {
                for (int e = 0; e < engines.length; e++) {
                    for (int ng = 0; ng < 2; ng++) {
                        final Model model = new Model();
                        IntVar[] pigeons = model.intVarArray("p", n, 0, n - 2, false);
                        model.allDifferent(pigeons, "NEQS").post();
                        model.set(random_value(pigeons, seed));
                        engines[e].plugin(model, ng == 1, false);
                        assertFalse(model.findSolution());
                        printShortStatistics(model);
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

                        final Model model = new Model();
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

                        model.allDifferent(vars, "NEQS").post();

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
                        model.set(random_value(vars, seed));

                        engines[e].plugin(model, ng == 1, false);
//                    SMF.shortlog(solver);
                        assertEquals(n > 2, model.findSolution());
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
                    final Model model = new Model();
                    IntVar[] p = model.intVarArray("p", 10, 0, 3, false);
                    BoolVar[] bs = model.boolVarArray("b", 2);
                    model.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
                    model.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
                    model.arithm(bs[0], "=", bs[1]).post();

                    model.sum(copyOfRange(p, 0, 8), "=", 5).post();
                    model.arithm(p[9], "+", p[8], ">", 4).post();
                    model.set(random_value(p, seed));
                    engines[e].plugin(model, ng == 1, false);
                    showShortStatistics(model);
                    assertFalse(model.findSolution());
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testReif2() { // to test PropagatorActivation, from bs to p
        for (int e = 0; e < engines.length; e++) {
            for (int ng = 0; ng < 2; ng++) {
                final Model model = new Model();
                IntVar[] p = model.intVarArray("p", 10, 0, 3, false);
                BoolVar[] bs = model.boolVarArray("b", 2);
                model.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
                model.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
                model.arithm(bs[0], "=", bs[1]).post();

                model.sum(copyOfRange(p, 0, 8), "=", 5).post();
                model.arithm(p[9], "+", p[8], ">", 4).post();
                // p[0], p[1] are just for fun
                model.set(lexico_LB(p[0], p[1], p[9], p[8], bs[0]));
                engines[e].plugin(model, ng == 1, false);
                showStatistics(model);
                showSolutions(model);
                showDecisions(model);
                assertFalse(model.findSolution());
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testReif3() { // to test PropagatorActivation, from bs to p
        for (int e = 0; e < engines.length; e++) {
            for (int ng = 0; ng < 2; ng++) {
                final Model model = new Model();
                IntVar[] p = model.intVarArray("p", 10, 0, 3, false);
                BoolVar[] bs = model.boolVarArray("b", 2);
                model.arithm(p[9], "=", p[8]).reifyWith(bs[0]);
                model.arithm(p[9], "!=", p[8]).reifyWith(bs[1]);
                model.arithm(bs[0], "=", bs[1]).post();

                model.sum(copyOfRange(p, 0, 8), "=", 5).post();
                model.arithm(p[9], "+", p[8], ">", 4).post();
                // p[0], p[1] are just for fun
                model.set(lexico_LB(p[0], p[1], bs[0], p[9], p[8]));
                engines[e].plugin(model, ng == 1, false);
                showStatistics(model);
                showSolutions(model);
                showDecisions(model);
                assertFalse(model.findSolution());
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testLazy() {
        for (int ng = 0; ng < 2; ng++) {
            Model model = new Model();
            // The set of variables
            IntVar[] p = model.intVarArray("p", 5, 0, 4, false);
            // The initial constraints
            model.sum(copyOfRange(p, 0, 3), ">=", 3).post();
            model.arithm(p[2], "+", p[3], ">=", 1).post();
            model.arithm(p[3], "+", p[4], ">", 4).post();

            // The false constraints
            BoolVar[] bs = new BoolVar[2];
            bs[0] = model.arithm(p[3], "=", p[4]).reify();
            bs[1] = model.arithm(p[3], "!=", p[4]).reify();
            model.arithm(bs[0], "=", bs[1]).post();

            model.set(lexico_LB(p[0], p[1], bs[0], p[2], p[3], p[4]));
            DBT.plugin(model, ng == 1, false);
            showStatistics(model);
            showSolutions(model);
            showDecisions(model);
            assertFalse(model.findSolution());
        }
    }
}
