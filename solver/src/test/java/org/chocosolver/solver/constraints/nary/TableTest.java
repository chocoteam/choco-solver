/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * @author Jean-Guillaume Fages
 * @since 10/04/14
 * Created by IntelliJ IDEA.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.extension.nary.TuplesLargeTable;
import org.chocosolver.solver.constraints.extension.nary.TuplesTable;
import org.chocosolver.solver.constraints.extension.nary.TuplesVeryLargeTable;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.MultivaluedDecisionDiagram;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.System.out;
import static org.chocosolver.solver.constraints.extension.TuplesFactory.generateTuples;
import static org.chocosolver.solver.constraints.extension.TuplesFactory.scalar;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.testng.Assert.assertEquals;

public class TableTest {

    private static final String[] ALGOS = {"CT+", "FC", "GAC2001", "GACSTR+", "GAC2001+", "GAC3rm+", "GAC3rm", "STR2+", "MDD+"};

    private static final String[] BIN_ALGOS = {"FC", "AC2001", "AC3", "AC3rm", "AC3bit+rm", "CT+"};

    @DataProvider(name = "algos")
    public Object[][] algos() {
        List<Object[]> params = new ArrayList<>();
        for (String st : ALGOS) {
            params.add(new Object[]{st});
        }
        return params.toArray(new Object[0][0]);
    }

    @DataProvider(name = "balgos")
    public Object[][] balgos() {
        List<Object[]> params = new ArrayList<>();
        for (String st : BIN_ALGOS) {
            params.add(new Object[]{st});
        }
        return params.toArray(new Object[0][0]);
    }

    @DataProvider(name = "starred")
    public Object[][] starred() {
        List<Object[]> params = new ArrayList<>();
        params.add(new Object[]{"CT+"});
        params.add(new Object[]{"STR2+"});
        return params.toArray(new Object[0][0]);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void test1(String a) {
        Tuples tuples = new Tuples(true);
        tuples.add(0, 0, 0);
        tuples.add(1, 1, 1);
        tuples.add(2, 2, 2);
        tuples.add(3, 3, 3);

        Model model = new Model();
        IntVar[] vars = model.intVarArray("X", 3, 1, 2, false);
        Constraint tableConstraint = model.table(vars, tuples, a);
        tableConstraint.post();

        model.getSolver().solve();
    }

    @Test(groups = "1s", timeOut = 300000)
    public void testTuples() {
        Model m = new Model();
        IntVar x = m.intVar(0, 4);
        IntVar y = m.boolVar();
        IntVar z = m.boolVar();
        Tuples t = new Tuples();
        t.add(0, -1, 1);
        t.add(0, 0, 1);
        t.add(5, -1, 1);
        t.add(1, 0, 1);
        m.table(new IntVar[]{x, y, z}, t, "CT+").post();

        while (m.getSolver().solve()) ;
        m.getSolver().printStatistics();
    }

    private void allEquals(Model model, IntVar[] vars, int algo) {
        if (algo > -1) {
            model.table(vars, TuplesFactory.allEquals(vars), ALGOS[algo]).post();
        } else {
            for (int i = 1; i < vars.length; i++) {
                model.arithm(vars[0], "=", vars[i]).post();
            }
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testAllEquals() {
        int[][] params = {{5, 2, 9}, {5, -2, 3}, {5, 0, 15}};//, {10, 2, 4}};
        for (int p = 0; p < params.length; p++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
            allEquals(model, vars, -1);
            long nbs = 0;
            while (model.getSolver().solve()) {
                nbs++;
            }
            long nbn = model.getSolver().getNodeCount();
            for (int a = 0; a < ALGOS.length; a++) {
                for (int s = 0; s < 10; s++) {
                    Model tsolver = new Model(ALGOS[a]);
                    IntVar[] tvars = tsolver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
                    allEquals(tsolver, tvars, a);
                    Solver r = tsolver.getSolver();
                    r.setSearch(randomSearch(tvars, s));
                    long nbSolutions = 0;
                    while (tsolver.getSolver().solve()) {
                        nbSolutions++;
                    }
                    assertEquals(nbSolutions, nbs);
                    if (a > 1) assertEquals(tsolver.getSolver().getNodeCount(), nbn);
                }
            }
        }
    }

    private void allDifferent(Model model, IntVar[] vars, int algo) {
        if (algo > -1) {
            model.table(vars, TuplesFactory.allDifferent(vars), ALGOS[algo]).post();
        } else {
            model.allDifferent(vars, "AC").post();
        }
    }

    @Test(groups = "10s", timeOut = 300000)
    public void testAllDifferent() {
        int[][] params = {{5, 2, 9}, {5, -2, 3}, {6, 0, 7}};

        for (int p = 0; p < params.length; p++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
            allDifferent(model, vars, -1);
            long nbs = 0;
            while (model.getSolver().solve()) {
                nbs++;
            }
            long nbn = model.getSolver().getNodeCount();
            for (int a = 0; a < ALGOS.length; a++) {
                for (int s = 0; s < 1; s++) {
                    Model tsolver = new Model(ALGOS[a]);
                    IntVar[] tvars = tsolver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
                    allDifferent(tsolver, tvars, a);
                    Solver r = tsolver.getSolver();
                    r.setSearch(randomSearch(tvars, s));
                    long nbSolutions = 0;
                    while (tsolver.getSolver().solve()) {
                        nbSolutions++;
                    }
                    assertEquals(nbSolutions, nbs);
                    if (a > 1) assertEquals(r.getMeasures().getNodeCount(), nbn);
                }
            }
        }
    }

    public static void test(String type) {
        Model model;
        IntVar[] vars;
        IntVar sum;
        IntVar[] reified;
        model = new Model();
        vars = model.intVarArray("vars", 6, new int[]{1, 2, 3, 4, 5, 6, 10, 45, 57});
        reified = model.intVarArray("rei", vars.length, new int[]{0, 1});
        sum = model.intVar("sum", 0, reified.length, true);
        model.allDifferent(vars, "AC").post();
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
            Constraint c = model.table(vars[i], reified[i], tuples, type);
            c.post();
        }
        model.sum(reified, "=", sum).post();
        model.setObjective(Model.MINIMIZE, sum);
        Solution sol = new Solution(model);
        while (model.getSolver().solve()) {
            sol.record();
        }
        if (model.getSolver().getSolutionCount() > 0) {
            for (int i = 0; i < vars.length; i++) {
                out.print(sol.getIntVal(vars[i]) + "\t");
            }
            out.println();
            for (int i = 0; i < reified.length; i++) {
                out.print(sol.getIntVal(reified[i]) + "\t");
            }
            out.println("\n" + "obj = " + sol.getIntVal(sum) + ", backtracks = " + model.getSolver().getBackTrackCount());
        }
        assertEquals(sol.getIntVal(sum), 5);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "balgos")
    public void testtpetit(String s) {
        test(s);
    }

    @Test(groups = "1s", timeOut = 60000)
    public static void testThierry1() {
        String[] ALGOS = {"FC", "GAC2001", "GAC3rm"};
        for (String s : ALGOS) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("vars", 10, 0, 100, false);
            Tuples t = new Tuples(false);
            t.add(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
            t.add(1, 1, 2, 1, 1, 1, 1, 1, 1, 1);
            model.table(vars, t, s).post();
            model.getSolver().solve();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMDD1() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("X", 3, 0, 1, false);
        Tuples tuples = new Tuples();
        tuples.add(0, 0, 0);
        tuples.add(1, 1, 1);
        model.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)).post();
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMDD2() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("X", 3, 0, 2, false);
        Tuples tuples = new Tuples();
        tuples.add(0, 1, 2);
        tuples.add(2, 1, 0);
        model.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)).post();
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }


    @Test(groups = "10s", timeOut = 300000)
    public void testRandom() {
        int[][] params = {{3, 1, 3}, {5, 2, 7}};//, {5, -2, 3}, {7, 2, 4}};
        final Random rnd = new Random();
        for (int p = 0; p < params.length; p++) {
            for (long seed = 0; seed < 10; seed++) {
                Model model = new Model();
                IntVar[] vars = model.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
                rnd.setSeed(seed);
                Tuples tuples = generateTuples(values -> rnd.nextBoolean(), true, vars);
                model.mddc(vars, new MultivaluedDecisionDiagram(vars, tuples)).post();
                model.getSolver().setSearch(randomSearch(vars, seed));
                long nbs = 0;
                while (model.getSolver().solve()) {
                    nbs++;
                }
                long nbn = model.getSolver().getNodeCount();
                for (int a = 0; a < ALGOS.length; a++) {
                    for (int s = 0; s < 1; s++) {
                        Model tsolver = new Model(ALGOS[a]);
                        IntVar[] tvars = tsolver.intVarArray("v1", params[p][0], params[p][1], params[p][2], false);
                        model.table(tvars, tuples, ALGOS[a]).post();
                        tsolver.getSolver().setSearch(randomSearch(tvars, s));
                        long nbSolutions = 0;
                        while (tsolver.getSolver().solve()) {
                            nbSolutions++;
                        }
                        assertEquals(nbSolutions, nbs);
                        if (a > 1) assertEquals(tsolver.getSolver().getNodeCount(), nbn);
                    }
                }
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTuplesTable1() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 4, 0, 3, false);
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

    @Test(groups = "1s", timeOut = 60000)
    public void testTuplesTable2() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 4, 0, 3, false);
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

    @Test(groups = "1s", timeOut = 60000)
    public void testTuplesLargeTable1() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 4, 0, 3, false);
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

    @Test(groups = "1s", timeOut = 60000)
    public void testTuplesLargeTable2() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 4, 0, 3, false);
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

    @Test(groups = "1s", timeOut = 60000)
    public void testTuplesVeryLargeTable1() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 4, 0, 3, false);
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

    @Test(groups = "1s", timeOut = 60000)
    public void testTuplesVeryLargeTable2() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("vars", 4, 0, 3, false);
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

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testPDav(String a) {
        Model model = new Model();
        IntVar x, y, z;
        x = model.intVar("x", 1, 3, false);
        y = model.intVar("y", 0, 3, false);
        z = model.intVar("z", 0, 1, false);
        Tuples ts = scalar(new IntVar[]{x, z, z}, new int[]{2, -1, -10}, y, 1);
        model.table(new IntVar[]{x, z, z, y}, ts, a).post();
        while (model.getSolver().solve()) ;
        assertEquals(1, model.getSolver().getSolutionCount());
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testPosTrue(String a) {
        Model model = new Model();
        IntVar x = model.intVar(1);
        IntVar y = model.intVar(3);
        IntVar z = model.intVar(1);
        Tuples ts = new Tuples(true);
        ts.add(1, 3, 1);
        Assert.assertEquals(model.table(new IntVar[]{x, y, z}, ts, a).isSatisfied(), ESat.TRUE);
        Assert.assertEquals(ts.check(x, y, z), ESat.TRUE);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testPosUndef(String a) {
        Model model = new Model();
        IntVar x = model.intVar(1);
        IntVar y = model.intVar(2, 3);
        IntVar z = model.intVar(1);
        Tuples ts = new Tuples(true);
        ts.add(1, 3, 1);
        Assert.assertEquals(model.table(new IntVar[]{x, y, z}, ts, a).isSatisfied(), ESat.UNDEFINED);
        Assert.assertEquals(ts.check(x, y, z), ESat.UNDEFINED);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testPosFalse(String a) {
        System.out.println(a);
        Model model = new Model();
        IntVar x = model.intVar(1);
        IntVar y = model.intVar(3);
        IntVar z = model.intVar(4);
        Tuples ts = new Tuples(true);
        ts.add(1, 3, 1);
        Assert.assertEquals(model.table(new IntVar[]{x, y, z}, ts, a).isSatisfied(), ESat.FALSE);
        Assert.assertEquals(ts.check(x, y, z), ESat.FALSE);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testPosFalse2(String a) {
        System.out.println(a);
        Model model = new Model();
        IntVar x = model.intVar(1, 3);
        IntVar y = model.intVar(1, 3);
        IntVar z = model.intVar(1, 3);
        Tuples ts = new Tuples(true);
//			ts.add(1,2,3);
        ts.add(1, 3, 2);
        ts.add(2, 3, 1);
        ts.add(2, 1, 3);
        ts.add(3, 2, 1);
        ts.add(3, 1, 2);
        Constraint table = model.table(new IntVar[]{x, y, z}, ts, a);
        model.arithm(x, "<", y).post();
        model.arithm(y, "<", z).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
        Assert.assertEquals(table.isSatisfied(), ESat.FALSE);
        Assert.assertEquals(ts.check(x, y, z), ESat.FALSE);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test329() {
        Model s1 = new Model(Settings.init().setMaxTupleSizeForSubstitution(0));
        {
            BoolVar[] bs = s1.boolVarArray("b", 3);
            BoolVar r = s1.boolVar("r");
            s1.scalar(bs, new int[]{-1, -1, -1}, "<=", -2).reifyWith(r);
        }
        Model s2 = new Model(Settings.init().setMaxTupleSizeForSubstitution(1000));
        {
            BoolVar[] bs = s2.boolVarArray("b", 3);
            BoolVar r = s2.boolVar("r");
            s2.scalar(bs, new int[]{-1, -1, -1}, "<=", -2).reifyWith(r);
        }


        s1.getSolver().findAllSolutions();
        s2.getSolver().findAllSolutions();
        Assert.assertEquals(s2.getSolver().getSolutionCount(), s1.getSolver().getSolutionCount());
        Assert.assertEquals(s2.getSolver().getNodeCount(), s1.getSolver().getNodeCount());
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "starred")
    public void testST1(String staralgo) {
        Model model = new Model();
        IntVar x = model.intVar(1, 3);
        IntVar y = model.intVar(1, 3);
        IntVar z = model.intVar(1, 3);
        Tuples ts = new Tuples(true);
        int ST = 99;
        ts.setUniversalValue(ST);
        ts.add(3, ST, 1);
        ts.add(1, 2, 3);
        ts.add(2, 3, 2);
        model.table(new IntVar[]{x, y, z}, ts, staralgo).post();

        Solver solver = model.getSolver();
        solver.showDecisions();
        solver.showSolutions();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 5);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "starred")
    public void testST2(String staralgo) {
        Model model = new Model();
        IntVar x = model.intVar(1, 3);
        IntVar y = model.intVar(1, 3);
        IntVar z = model.intVar(1, 3);
        Tuples ts = new Tuples(true);
        int ST = 99;
        ts.setUniversalValue(ST);
        ts.add(ST, ST, ST);
        model.table(new IntVar[]{x, y, z}, ts, staralgo).post();

        Solver solver = model.getSolver();

        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 27);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "starred")
        public void testST3(String staralgo) {
        Model model = new Model();
        //IntVar w = model.intVar("w", 0, 1);
        IntVar x = model.intVar("x",0, 1);
        IntVar y = model.intVar("y",0, 1);
        IntVar z = model.intVar("z",0, 1);
        Tuples ts = new Tuples(true);
        int ST = 99;
        ts.setUniversalValue(ST);
        ts.add(1, ST, ST);
        ts.add(ST, 1, ST);
        ts.add(ST, ST, 1);
        model.table(new IntVar[]{/*w, */x, y, z}, ts, staralgo).post();

        Solver solver = model.getSolver();

        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 7);
    }

    @Test(groups = "1s", timeOut = 60000,dataProvider = "balgos")
    public void testJuha1(String a) {
        Model model = new Model("Table MWE");
        IntVar foo = model.intVar("foo", new int[]{1, 3, 7});
        IntVar bar = model.intVar("bar", 1, 2, false);
        Tuples allowed = new Tuples(true);
        allowed.add(1, 2);
        allowed.add(2, 2);
        allowed.add(3, 2);
        BoolVar b = model.boolVar("b");
        Constraint table = model.table(foo, bar, allowed, a);
        table.reifyWith(b);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 6);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testJuha2(String a) {
        Model model = new Model("Table MWE");
        IntVar foo = model.intVar("foo", new int[]{1, 3, 7});
        IntVar foo1 = model.intVar("foo1", new int[]{1, 3, 7});
        IntVar bar = model.intVar("bar", 1, 2, false);
        Tuples allowed = new Tuples(true);
        allowed.add(1, 1, 2);
        allowed.add(2, 2, 2);
        allowed.add(3, 7, 2);
        BoolVar b = model.boolVar("b");
        Constraint table = model.table(new IntVar[]{foo, foo1, bar}, allowed, a);
        table.reifyWith(b);
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 18);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "balgos")
    public void testMVAV(String a) {
        if ("AC3bit+rm".equals(a)) return;
        Model model = new Model("Table MWE");
        IntVar foo = model.intVar("foo", 0, 7, true);
        IntVar bar = model.intVar("bar", 0, 7, true);
        Tuples allowed = new Tuples(true);
        allowed.add(1, 2);
        allowed.add(2, 2);
        allowed.add(3, 2);
        Constraint table = model.table(foo, bar, allowed, a);
        table.post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "balgos")
    public void testMVAV0(String a) {
        if ("AC3bit+rm".equals(a)) return;
        Model model = new Model("Table MWE");
        IntVar foo = model.intVar("foo", 0, 3, true);
        IntVar bar = model.intVar("bar", 0, 1_000_000, true);
        Tuples allowed = new Tuples(true);
        allowed.add(1, 1_000_000);
        allowed.add(2, 1_000_000);
        allowed.add(3, 1);
        Constraint table = model.table(foo, bar, allowed, a);
        table.post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testMVAV1() {
        Model model = new Model("Table MWE");
        IntVar foo = model.intVar("foo", 0, 7, true);
        IntVar bar = model.intVar("bar", 0, 7, true);
        Tuples allowed = new Tuples(true);
        allowed.add(1, 2);
        allowed.add(2, 2);
        allowed.add(3, 2);
        Constraint table = model.table(foo, bar, allowed, "AC3bit+rm");
        table.post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testMVAV2(String a) {
        if ("GAC2001".equals(a)) return;
        Model model = new Model("Table MWE");
        IntVar foo = model.intVar("foo", 0, 7, true);
        IntVar bar = model.intVar("bar", 0, 7, true);
        IntVar far = model.intVar("far", 0, 7, true);
        Tuples allowed = new Tuples(true);
        allowed.add(1, 2, 3);
        allowed.add(2, 2, 2);
        allowed.add(3, 2, 1);
        Constraint table = model.table(new IntVar[]{foo, bar,far}, allowed, a);
        table.post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testMVAV3(String a) {
        if ("GAC2001".equals(a) || "MDD+".equals(a) || "GAC3rm+".equals(a)) return;
        Model model = new Model("Table MWE");
        IntVar foo = model.intVar("foo", 0, 7, true);
        IntVar bar = model.intVar("bar", 0, 7, true);
        IntVar far = model.intVar("far", 0, 500_000, true);
        Tuples allowed = new Tuples(true);
        allowed.add(1, 2, 500_000);
        allowed.add(2, 2, 500_000);
        allowed.add(3, 2, 1);
        Constraint table = model.table(new IntVar[]{foo, bar, far}, allowed, a);
        table.post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = SolverException.class)
    public void testMVAV4() {
        //if (.equals(a)) return;
        Model model = new Model("Table MWE");
        IntVar foo = model.intVar("foo", 0, 7, true);
        IntVar bar = model.intVar("bar", 0, 7, true);
        IntVar far = model.intVar("far", 0, 7, true);
        Tuples allowed = new Tuples(true);
        allowed.add(1, 2, 3);
        allowed.add(2, 2, 2);
        allowed.add(3, 2, 1);
        Constraint table = model.table(new IntVar[]{foo, bar, far}, allowed, "GAC2001");
        table.post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 3);
    }

    /**
     * A table based on STR2 algorithm does not survive an empty set of tuples.
     */
    @Test(groups = "1s", timeOut = 60000)
    public void str2CrashesWhenGivenAnEmptyTable() {
        Model choco = new Model();
        Tuples empty = new Tuples();

        IntVar x0 = choco.intVar(-10, 10);
        IntVar x1 = choco.intVar(-20, 20);
        IntVar x2 = choco.intVar(-30, 30);

        // This line triggers an ArrayIndexOutOfBoundsException.
        choco.table(new IntVar[]{x0, x1, x2}, empty, "STR2+").post();
    }

    /**
     * STR2+ is not equivalent to an AC filtering.
     * <p>
     * ###########################
     * arity : 1
     * ###########################
     * Table : []
     * ###########################
     * SEED      : 166e8679608
     * CAUSE     : Property violated
     * WITNESS   : x0={0}
     * ###########################
     */
    @Test(groups = "1s", timeOut = 60000, expectedExceptions = ContradictionException.class)
    public void str2PlusTableShouldBeAc() throws ContradictionException {
        Model cp = new Model();

        IntVar x0 = cp.intVar(0, 0);
        Tuples t = new Tuples();
        cp.post(cp.table(new IntVar[]{x0}, t, "STR2+"));
        cp.getSolver().propagate(); // should trigger an inconsistency
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testCT1(String a) {
        if(a.equals("FC"))return;
        Model cp = new Model();
        IntVar x0 = cp.intVar(new int[]{-1, 1, 4});
        IntVar x1 = cp.intOffsetView(x0, 10);
        IntVar x2 = cp.intVar(new int[]{2, 3});
        Tuples t = new Tuples();
        {
            t.add(-1, 14, 2);
            t.add(1, 9, 2);
            t.add(1, 14, 3);
            t.add(1, 14, 5);
        }
        cp.post(cp.table(new IntVar[]{x0, x1, x2}, t, a));
        try {
            cp.getSolver().propagate(); // should trigger an inconsistency
            Assert.assertTrue(x2.contains(2));
            Assert.assertTrue(x2.contains(3));
            x2.instantiateTo(2, Cause.Null);
            cp.getSolver().propagate();
            Assert.fail("ALgo" + a);
        } catch (ContradictionException c) {
        }

    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "algos")
    public void testCT2(String a) {
        if(a.equals("FC"))return;
        Model cp = new Model();
        IntVar x0 = cp.intVar(new int[]{-1, 1, 4});
        IntVar x1 = cp.intOffsetView(x0, 10);
        IntVar x2 = cp.intVar(new int[]{2, 3});
        Tuples t = new Tuples();
        {
            t.add(-1, 14, 2);
            t.add(1, 9, 2);
            t.add(1, 14, 3);
            t.add(1, 14, 5);
        }
        cp.post(cp.table(new IntVar[]{x0, x1, x2}, t, a));
        try {
            cp.getSolver().propagate(); // should trigger an inconsistency
        } catch (ContradictionException c) {
        }

    }
}