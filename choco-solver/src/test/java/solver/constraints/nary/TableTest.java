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


/**
 * @author Jean-Guillaume Fages
 * @since 10/04/14
 * Created by IntelliJ IDEA.
 */
package solver.constraints.nary;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.constraints.extension.TupleValidator;
import solver.constraints.extension.Tuples;
import solver.constraints.extension.TuplesFactory;
import solver.search.strategy.ISF;
import solver.variables.IntVar;
import solver.variables.VF;
import solver.variables.VariableFactory;
import util.logger.LoggerFactory;
import util.objects.graphs.MultivaluedDecisionDiagram;

import java.util.Random;

public class TableTest {

    private static String[] ALGOS = {"FC", "GAC2001", "GACSTR+", "GAC2001+", "GAC3rm+", "GAC3rm", "STR2+"};

    @Test(groups = "1s")
    public void test1() {
        for (String a : ALGOS) {
            Tuples tuples = new Tuples(true);
            tuples.add(0, 0, 0);
            tuples.add(1, 1, 1);
            tuples.add(2, 2, 2);

            Solver solver = new Solver();
            IntVar[] vars = VF.enumeratedArray("X", 3, 0, 1, solver);
            Constraint tableConstraint = ICF.table(vars, tuples, a);
            solver.post(tableConstraint);

            solver.findSolution();
        }
    }


    private void allEquals(Solver solver, IntVar[] vars, int algo) {
        if (algo > -1) {
            solver.post(ICF.table(vars, TuplesFactory.allEquals(vars), ALGOS[algo]));
        } else {
            for (int i = 1; i < vars.length; i++) {
                solver.post(ICF.arithm(vars[0], "=", vars[i]));
            }
        }
    }

    @Test(groups = "1m")
    public void testAllEquals() {
        int[][] params = {{5, 2, 9}, {5, -2, 3}, {10, 2, 4}, {5, 0, 20}};
        for (int p = 0; p < params.length; p++) {
            Solver solver = new Solver();
            IntVar[] vars = VF.enumeratedArray("v1", params[p][0], params[p][1], params[p][2], solver);
            allEquals(solver, vars, -1);
            long nbs = solver.findAllSolutions();
            long nbn = solver.getMeasures().getNodeCount();
//            System.out.printf("%s\n", solver.getMeasures().toOneLineString());
            for (int a = 0; a < ALGOS.length; a++) {
                for (int s = 0; s < 10; s++) {
                    Solver tsolver = new Solver(ALGOS[a]);
                    IntVar[] tvars = VF.enumeratedArray("v1", params[p][0], params[p][1], params[p][2], tsolver);
                    allEquals(tsolver, tvars, a);
                    tsolver.set(ISF.random_value(tvars));
                    Assert.assertEquals(tsolver.findAllSolutions(), nbs);
                    if (a > 1) Assert.assertEquals(tsolver.getMeasures().getNodeCount(), nbn);
//                    System.out.printf("%s\n", tsolver.getMeasures().toOneLineString());
                }
            }
        }
    }

    private void allDifferent(Solver solver, IntVar[] vars, int algo) {
        if (algo > -1) {
            solver.post(ICF.table(vars, TuplesFactory.allDifferent(vars), ALGOS[algo]));
        } else {
            solver.post(ICF.alldifferent(vars, "AC"));
        }
    }

    @Test(groups = "1m")
    public void testAllDifferent() {
        int[][] params = {{5, 2, 9}, {5, -2, 3}, {7, 0, 7}};

        for (int p = 2; p < params.length; p++) {
            Solver solver = new Solver();
            IntVar[] vars = VF.enumeratedArray("v1", params[p][0], params[p][1], params[p][2], solver);
            allDifferent(solver, vars, -1);
            long nbs = solver.findAllSolutions();
            long nbn = solver.getMeasures().getNodeCount();
//            System.out.printf("%s\n===\n", solver.getMeasures().toOneLineString());
            for (int a = 0; a < ALGOS.length; a++) {
                for (int s = 0; s < 1; s++) {
                    Solver tsolver = new Solver(ALGOS[a]);
                    IntVar[] tvars = VF.enumeratedArray("v1", params[p][0], params[p][1], params[p][2], tsolver);
                    allDifferent(tsolver, tvars, a);
                    tsolver.set(ISF.random_value(tvars));
                    Assert.assertEquals(tsolver.findAllSolutions(), nbs);
                    if (a > 1) Assert.assertEquals(tsolver.getMeasures().getNodeCount(), nbn);
//                    System.out.printf("%s\n", tsolver.getMeasures().toOneLineString());
                }
            }
//            System.out.printf("===\n%s\n", solver.getMeasures().toOneLineString());
        }
    }

    public static void main(String[] args) {
        TableTest tt = new TableTest();
        tt.testAllDifferent();
    }

    public static void test(String type) {
        Solver solver;
        IntVar[] vars;
        IntVar sum;
        IntVar[] reified;
        solver = new Solver();
        vars = VariableFactory.enumeratedArray("vars", 6, new int[]{1, 2, 3, 4, 5, 6, 10, 45, 57}, solver);
        reified = VariableFactory.enumeratedArray("rei", vars.length, new int[]{0, 1}, solver);
        sum = VariableFactory.bounded("sum", 0, reified.length, solver);
        solver.post(IntConstraintFactory.alldifferent(vars, "AC"));
        Tuples tuples = new Tuples(true);
        tuples.add(1, 0);
        tuples.add(2, 1);
        tuples.add(3, 1);
        tuples.add(4, 1);
        tuples.add(5, 1);
        tuples.add(6, 1);
        tuples.add(10, 1);
        tuples.add(45, 1);
        tuples.add(57, 1);

        for (int i = 0; i < vars.length; i++) {
            Constraint c = IntConstraintFactory.table(vars[i], reified[i], tuples, type);
            solver.post(c);
        }
        solver.post(IntConstraintFactory.sum(reified, sum));
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, sum);
        if (solver.getMeasures().getSolutionCount() > 0) {
            for (int i = 0; i < vars.length; i++) {
                System.out.print(vars[i].getValue() + "\t");
            }
            System.out.println("");
            for (int i = 0; i < reified.length; i++) {
                System.out.print(reified[i].getValue() + "\t");
            }
            System.out.println("\n" + "obj = " + sum.getValue() + ", backtracks = " + solver.getMeasures().getBackTrackCount());
        }
        Assert.assertEquals(sum.getValue(), 5);
    }

    @Test(groups = "1s")
    public void testtpetit() {
        test("AC3");
        test("AC3rm");
        test("AC3bit+rm");
        test("AC2001");
        test("FC");
    }

    @Test(groups = "1s")
    public static void testThierry() {
        Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("vars", 10, 0, 100, solver);
        Tuples t = new Tuples(false);
        t.add(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        solver.post(ICF.table(vars, t, "GAC3rm"));
        solver.findSolution();
    }

    @Test(groups = "1s")
    public void testMDD1() {
        Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("X", 3, 0, 1, solver);
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0);
        tuples.add(1, 1, 1);
        solver.post(ICF.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups = "1s")
    public void testMDD2() {
        Solver solver = new Solver();
        IntVar[] vars = VF.enumeratedArray("X", 3, 0, 2, solver);
        Tuples tuples = new Tuples();
        tuples.add(0, 1, 2);
        tuples.add(2, 1, 0);
        solver.post(ICF.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }


    @Test(groups = "1m")
    public void testRandom() {
        int[][] params = {{3, 1, 3}, {5, 2, 9}, {5, -2, 3}, {10, 2, 4}};
        final Random rnd = new Random();
        for (int p = 0; p < params.length; p++) {
            for (long seed = 0; seed < 10; seed++) {
                Solver solver = new Solver();
                IntVar[] vars = VF.enumeratedArray("v1", params[p][0], params[p][1], params[p][2], solver);
                rnd.setSeed(seed);
                Tuples tuples = TuplesFactory.generateTuples(new TupleValidator() {
                    @Override
                    public boolean valid(int... values) {
                        return rnd.nextBoolean();
                    }
                }, true, vars);
                solver.post(ICF.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)));
                solver.set(ISF.random_value(vars));
                long nbs = solver.findAllSolutions();
                long nbn = solver.getMeasures().getNodeCount();
                LoggerFactory.getLogger().infof("%s\n", solver.getMeasures().toOneLineString());
                for (int a = 0; a < ALGOS.length; a++) {
                    for (int s = 0; s < 1; s++) {
                        Solver tsolver = new Solver(ALGOS[a]);
                        IntVar[] tvars = VF.enumeratedArray("v1", params[p][0], params[p][1], params[p][2], tsolver);
                        tsolver.post(ICF.table(tvars, tuples, ALGOS[a]));
                        tsolver.set(ISF.random_value(tvars));
                        Assert.assertEquals(tsolver.findAllSolutions(), nbs);
                        if (a > 1) Assert.assertEquals(tsolver.getMeasures().getNodeCount(), nbn);
                        LoggerFactory.getLogger().infof("%s\n", tsolver.getMeasures().toOneLineString());
                    }
                }
            }
        }
    }

}
