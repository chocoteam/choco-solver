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
/**
 * @author Jean-Guillaume Fages
 * @since 10/04/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.extension.nary.LargeRelation;
import org.chocosolver.solver.constraints.extension.nary.TuplesLargeTable;
import org.chocosolver.solver.constraints.extension.nary.TuplesTable;
import org.chocosolver.solver.constraints.extension.nary.TuplesVeryLargeTable;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

public class TableTest {

    private static String[] ALGOS = {"FC", "MDD+", "GAC2001", "GACSTR+", "GAC2001+", "GAC3rm+", "GAC3rm", "STR2+"};
    private static String[] BIN_ALGOS = {"FC", "AC2001", "AC3", "AC3rm", "AC3bit+rm"};

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        for (String a : ALGOS) {
            Tuples tuples = new Tuples(true);
            tuples.add(0, 0, 0);
            tuples.add(1, 1, 1);
            tuples.add(2, 2, 2);
            tuples.add(3, 3, 3);

            Solver solver = new Solver();
            IntVar[] vars = solver.intVarArray("X", 3, 1, 2, false);
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

    @Test(groups="5m", timeOut=300000)
    public void testAllEquals() {
        int[][] params = {{5, 2, 9}, {5, -2, 3}, {10, 2, 4}, {5, 0, 20}};
        for (int p = 0; p < params.length; p++) {
            Solver solver = new Solver();
            IntVar[] vars = solver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
            allEquals(solver, vars, -1);
            long nbs = solver.findAllSolutions();
            long nbn = solver.getMeasures().getNodeCount();
//            System.out.printf("%s\n", solver.getMeasures().toOneLineString());
            for (int a = 0; a < ALGOS.length; a++) {
                for (int s = 0; s < 10; s++) {
                    Solver tsolver = new Solver(ALGOS[a]);
                    IntVar[] tvars = tsolver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
                    allEquals(tsolver, tvars, a);
                    tsolver.set(ISF.random_value(tvars, s));
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

    @Test(groups="5m", timeOut=300000)
    public void testAllDifferent() {
        int[][] params = {{5, 2, 9}, {5, -2, 3}, {7, 0, 7}};

        for (int p = 2; p < params.length; p++) {
            Solver solver = new Solver();
            IntVar[] vars = solver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
            allDifferent(solver, vars, -1);
            long nbs = solver.findAllSolutions();
            long nbn = solver.getMeasures().getNodeCount();
//            System.out.printf("%s\n===\n", solver.getMeasures().toOneLineString());
            for (int a = 0; a < ALGOS.length; a++) {
                for (int s = 0; s < 1; s++) {
                    Solver tsolver = new Solver(ALGOS[a]);
                    IntVar[] tvars = tsolver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
                    allDifferent(tsolver, tvars, a);
                    tsolver.set(ISF.random_value(tvars, s));
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
        vars = solver.intVarArray("vars", 6, new int[]{1, 2, 3, 4, 5, 6, 10, 45, 57});
        reified = solver.intVarArray("rei", vars.length, new int[]{0, 1});
        sum = solver.intVar("sum", 0, reified.length, true);
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
        solver.post(IntConstraintFactory.sum(reified, "=", sum));
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, sum);
        if (solver.getMeasures().getSolutionCount() > 0) {
            for (int i = 0; i < vars.length; i++) {
                System.out.print(solver.getSolutionRecorder().getLastSolution().getIntVal(vars[i]) + "\t");
            }
            System.out.println("");
            for (int i = 0; i < reified.length; i++) {
                System.out.print(reified[i].getValue() + "\t");
            }
            System.out.println("\n" + "obj = " + solver.getSolutionRecorder().getLastSolution().getIntVal(sum) + ", backtracks = " + solver.getMeasures().getBackTrackCount());
        }
        Assert.assertEquals(solver.getSolutionRecorder().getLastSolution().getIntVal(sum).intValue(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testtpetit() {
        for(String s : BIN_ALGOS) {
            test(s);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public static void testThierry1() {
        String[] ALGOS = {"FC", "GAC2001", "GAC3rm"};
        for(String s : ALGOS){
        Solver solver = new Solver();
            IntVar[] vars = solver.intVarArray("vars", 10, 0, 100, false);
        Tuples t = new Tuples(false);
        t.add(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        t.add(1, 1, 2, 1, 1, 1, 1, 1, 1, 1);
        solver.post(ICF.table(vars, t, s));
        solver.findSolution();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testMDD1() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("X", 3, 0, 1, false);
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0);
        tuples.add(1, 1, 1);
        solver.post(ICF.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMDD2() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("X", 3, 0, 2, false);
        Tuples tuples = new Tuples();
        tuples.add(0, 1, 2);
        tuples.add(2, 1, 0);
        solver.post(ICF.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 2);
    }


    @Test(groups="5m", timeOut=300000)
    public void testRandom() {
        int[][] params = {{3, 1, 3}, {5, 2, 9}, {5, -2, 3}, {7, 2, 4}};
        final Random rnd = new Random();
        for (int p = 0; p < params.length; p++) {
            for (long seed = 0; seed < 10; seed++) {
                Solver solver = new Solver();
                IntVar[] vars = solver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
                rnd.setSeed(seed);
                Tuples tuples = TuplesFactory.generateTuples(values -> rnd.nextBoolean(), true, vars);
                solver.post(ICF.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)));
                solver.set(ISF.random_value(vars, seed));
                long nbs = solver.findAllSolutions();
                long nbn = solver.getMeasures().getNodeCount();
                for (int a = 0; a < ALGOS.length; a++) {
                    for (int s = 0; s < 1; s++) {
                        Solver tsolver = new Solver(ALGOS[a]);
                        IntVar[] tvars = tsolver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
                        tsolver.post(ICF.table(tvars, tuples, ALGOS[a]));
                        tsolver.set(ISF.random_value(tvars, s));
                        Assert.assertEquals(tsolver.findAllSolutions(), nbs);
                        if (a > 1) Assert.assertEquals(tsolver.getMeasures().getNodeCount(), nbn);
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testTuplesTable1() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("vars", 4, 0, 3, false);
        Tuples t = new Tuples(true);
        t.add(1, 1, 1, 1);
        t.add(1, 1, 2, 1);
        TuplesTable tt = new TuplesTable(t, vars);
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{1, 2, 1, 1}));

        Assert.assertTrue(tt.isConsistent(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{1, 2, 1, 1}));
    }

    @Test(groups="1s", timeOut=60000)
    public void testTuplesTable2() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("vars", 4, 0, 3, false);
        Tuples t = new Tuples(false);
        t.add(1, 1, 1, 1);
        t.add(1, 1, 2, 1);
        TuplesTable tt = new TuplesTable(t, vars);
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{1, 2, 1, 1}));

        Assert.assertFalse(tt.isConsistent(new int[]{1, 1, 1, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{1, 1, 2, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{2, 1, 1, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{1, 2, 1, 1}));
    }

    @Test(groups="1s", timeOut=60000)
    public void testTuplesLargeTable1() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("vars", 4, 0, 3, false);
        Tuples t = new Tuples(true);
        t.add(1, 1, 1, 1);
        t.add(1, 1, 2, 1);
        TuplesLargeTable tt = new TuplesLargeTable(t, vars);
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{1, 2, 1, 1}));

        Assert.assertTrue(tt.isConsistent(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{1, 2, 1, 1}));
    }

    @Test(groups="1s", timeOut=60000)
    public void testTuplesLargeTable2() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("vars", 4, 0, 3, false);
        Tuples t = new Tuples(false);
        t.add(1, 1, 1, 1);
        t.add(1, 1, 2, 1);
        TuplesLargeTable tt = new TuplesLargeTable(t, vars);
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{1, 2, 1, 1}));

        Assert.assertFalse(tt.isConsistent(new int[]{1, 1, 1, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{1, 1, 2, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{2, 1, 1, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{1, 2, 1, 1}));
    }

    @Test(groups="1s", timeOut=60000)
    public void testTuplesVeryLargeTable1() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("vars", 4, 0, 3, false);
        Tuples t = new Tuples(true);
        t.add(1, 1, 1, 1);
        t.add(1, 1, 2, 1);
        TuplesVeryLargeTable tt = new TuplesVeryLargeTable(t, vars);
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{1, 2, 1, 1}));

        Assert.assertTrue(tt.isConsistent(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{1, 2, 1, 1}));
    }

    @Test(groups="1s", timeOut=60000)
    public void testTuplesVeryLargeTable2() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("vars", 4, 0, 3, false);
        Tuples t = new Tuples(false);
        t.add(1, 1, 1, 1);
        t.add(1, 1, 2, 1);
        TuplesVeryLargeTable tt = new TuplesVeryLargeTable(t, vars);
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{1, 2, 1, 1}));

        Assert.assertFalse(tt.isConsistent(new int[]{1, 1, 1, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{1, 1, 2, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{2, 1, 1, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{1, 2, 1, 1}));
    }

    @Test(groups="1s", timeOut=60000)
    public void testTuplesVeryLargeTableDuplicate() {
        Solver solver = new Solver();
        IntVar[] vars = solver.intVarArray("vars", 4, 0, 3, false);
        Tuples t = new Tuples(false);
        t.add(1, 1, 1, 1);
        t.add(1, 1, 2, 1);
        TuplesVeryLargeTable or = new TuplesVeryLargeTable(t, vars);
        LargeRelation tt = or.duplicate();

        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 1, 1}));
        Assert.assertTrue(tt.checkTuple(new int[]{1, 1, 2, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{2, 1, 1, 1}));
        Assert.assertFalse(tt.checkTuple(new int[]{1, 2, 1, 1}));

        Assert.assertFalse(tt.isConsistent(new int[]{1, 1, 1, 1}));
        Assert.assertFalse(tt.isConsistent(new int[]{1, 1, 2, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{2, 1, 1, 1}));
        Assert.assertTrue(tt.isConsistent(new int[]{1, 2, 1, 1}));
    }

    @Test(groups="1s", timeOut=60000)
    public void testPDav() {
        for (String a : ALGOS) {
            Solver solver = new Solver();
            IntVar x, y, z;
            x = solver.intVar("x", 1, 3, false);
            y = solver.intVar("y", 0, 3, false);
            z = solver.intVar("z", 0, 1, false);
            Tuples ts = TuplesFactory.scalar(new IntVar[]{x, z, z}, new int[]{2, -1, -10}, y, 1);
            solver.post(ICF.table(new IntVar[]{x, z, z, y}, ts, a));
            solver.findAllSolutions();
            Assert.assertEquals(1, solver.getMeasures().getSolutionCount());
        }
    }
}