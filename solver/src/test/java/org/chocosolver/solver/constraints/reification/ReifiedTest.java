/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;


/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19 nov. 2010
 */
public class ReifiedTest {


    @Test(groups = "1s", timeOut = 60000)
    public void testRandomEq() {
        for (int seed = 0; seed < 200; seed++) {
            Random r = new Random(seed);
            double d = r.nextDouble() / 2 + 0.5;
            Model s = new Model();

            BoolVar b = s.boolVar("b");
            int[][] values = buildFullDomains(2, 0, 15, r, d, false);
            IntVar x = s.intVar("x", values[0]);
            IntVar y = s.intVar("y", values[1]);
            IntVar[] vars = new IntVar[]{b, x, y};

            Constraint cons = s.arithm(x, "=", y);
            Constraint oppCons = s.arithm(x, "!=", y);

            s.ifThenElse(b, cons, oppCons);
            s.getSolver().setSearch(inputOrderLBSearch(vars));
            while (s.getSolver().solve()) ;
            long sol = s.getSolver().getSolutionCount();
            assertEquals(sol, (long) values[0].length * values[1].length, "nb sol incorrect");
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRandomMember() {
        Model s = new Model();

        BoolVar a = s.boolVar("a");
        BoolVar b = s.boolVar("b");
        BoolVar c = s.boolVar("c");
        IntVar x = s.intVar("x", 1, 3, false);
        IntVar y = s.intVar("y", 1, 1, false);
        IntVar z = s.intVar("z", 1, 2, false);

        s.ifThenElse(a, s.member(x, new int[]{1, 1}), s.notMember(x, new int[]{1, 1}));
        s.ifThenElse(b, s.member(y, new int[]{1, 1}), s.notMember(y, new int[]{1, 1}));
        s.ifThenElse(c, s.member(z, new int[]{1, 1}), s.notMember(z, new int[]{1, 1}));

        s.sum(new IntVar[]{a, b, c}, "=", s.boolVar("sum")).post();

        s.getSolver().setSearch(inputOrderLBSearch(x, y, z));
        while (s.getSolver().solve()) ;
        long sol = s.getSolver().getSolutionCount();
        assertEquals(sol, 2, "nb sol incorrect");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRandomNeq() {
        for (int seed = 0; seed < 200; seed++) {
            Random r = new Random(seed);
            double d = r.nextDouble() / 2 + 0.5;
            Model s = new Model();

            BoolVar b = s.boolVar("b");
            int[][] values = buildFullDomains(2, 0, 15, r, d, false);
            IntVar x = s.intVar("x", values[0]);
            IntVar y = s.intVar("y", values[1]);
            IntVar[] vars = new IntVar[]{b, x, y};

            Constraint cons = s.arithm(x, "!=", y);
            Constraint oppCons = s.arithm(x, "=", y);

            s.ifThenElse(b, cons, oppCons);

            s.getSolver().setSearch(inputOrderLBSearch(vars));
            while (s.getSolver().solve()) ;
            long sol = s.getSolver().getSolutionCount();
            assertEquals(sol, (long) values[0].length * values[1].length, "nb sol incorrect");
        }
    }

    private int[] union(int[][] domains) {
        TIntHashSet union = new TIntHashSet();
        for (int i = 0; i < domains.length; i++) {
            union.addAll(domains[i]);
        }
        int[] values = union.toArray();
        Arrays.sort(values);
        return values;
    }


    private Model model1(int i, int[][] values) {
        Model s1 = new Model();

        IntVar[] vars1 = new IntVar[i];
        for (int j = 0; j < i; j++) {
            vars1[j] = s1.intVar("v_" + j, values[j]);
        }

        s1.allDifferent(vars1, "AC").post();

        s1.getSolver().setSearch(inputOrderLBSearch(vars1));
        return s1;
    }

    private Model model2(int i, int[][] values) {
        Model s2 = new Model();


        IntVar[] X = new IntVar[i];
        for (int j = 0; j < i; j++) {
            X[j] = s2.intVar("v_" + j, values[j]);
        }

        int[] union = union(values);
        int l = union[0];
        int u = union[union.length - 1];

        BoolVar[][][] mA = new BoolVar[i][][];
        List<BoolVar> listA = new ArrayList<>();
//                List<BoolVar> Blist = new ArrayList<BoolVar>();
        for (int j = 0; j < i; j++) {
            mA[j] = new BoolVar[u - l + 1][];
            for (int p = l; p <= u; p++) {
                mA[j][p - l] = new BoolVar[u - p + 1];
//                        BoolVar b = VariableFactory.bool("B" + j + "_" + p, s2);
//                        Blist.add(b);
//                        Constraint cB = ConstraintFactory.leq(X[j], l, s2, eng2);
//                        Constraint ocB = ConstraintFactory.geq(X[j], l + 1, s2, eng2);
//                        lcstrs.add(new ReifiedConstraint(b, cB, ocB, s2, eng2));
                for (int q = p; q <= u; q++) {
                    BoolVar a = s2.boolVar("A" + j + "_" + p + "_" + q);
                    mA[j][p - l][q - p] = a;
                    listA.add(a);

                    Constraint cA = s2.member(X[j], p, q);
                    Constraint ocA = s2.notMember(X[j], p, q);

                    s2.ifThenElse(a, cA, ocA);
                }
            }
        }
//                BoolVar[] B =  Blist.toArray(new BoolVar[Blist.size()]);

        ArrayList<ArrayList<ArrayList<BoolVar>>> apmA = new ArrayList<>();

        for (int p = l; p <= u; p++) {
            apmA.add(p - l, new ArrayList<>());
            for (int q = p; q <= u; q++) {
                apmA.get(p - l).add(q - p, new ArrayList<>());
                for (int j = 0; j < i; j++) {
                    apmA.get(p - l).get(q - p).add(mA[j][p - l][q - p]);
                }
            }
        }


        for (int p = l; p <= u; p++) {
            for (int q = p; q <= u; q++) {
                BoolVar[] ai = null;
                for (int j = 0; j < i; j++) {
                    ai = apmA.get(p - l).get(q - p).toArray(new BoolVar[apmA.get(p - l).get(q - p).size()]);
                }
                s2.sum(ai, "=", s2.intVar("sum", 0, q - p + 1, true)).post();
            }
        }

        s2.getSolver().setSearch(inputOrderLBSearch(X));
        return s2;
    }

    /**
     * "Decompositions of All Different, Global Cardinality and Related Constraints"
     * C. Bessiere, G. Katsirelos, N. Narodytska, C.G. Quimper, T. Walsh.
     * Proceedings IJCAI'09, Pasadena CA, pages 419-424.
     */
    @Test(groups="10s", timeOut = 300000)
    public void testAllDifferentDecomp() {

        for (int i = 1; i < 11; i++) {
//            System.out.printf("i : %d\n", i);
            Random r = new Random(i);
            for (double d = 1.0; d <= 1.0; d += 0.125) {

                int[][] values = buildFullDomains(i, 1, i, r, d, false);
                Model s1 = model1(i, values);
                while (s1.getSolver().solve()) ;

                ////////////////////////

                Model s2 = model2(i, values);
                while (s2.getSolver().solve()) ;


                ////////////////////////
                long sol1 = s1.getSolver().getSolutionCount();
                long sol2 = s2.getSolver().getSolutionCount();
                assertEquals(sol2, sol1, "nb sol incorrect");
            }
        }

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAllDifferentDecompSpe1() {

        int[][] values; //= DomainBuilder.buildFullDomains(i, 1, i, r, d, false);
        values = new int[][]{{1, 2}, {1}};
        Model s1 = model1(2, values);
        while (s1.getSolver().solve()) ;

        ////////////////////////

        Model s2 = model2(2, values);
        while (s2.getSolver().solve()) ;


        ////////////////////////
        long sol1 = s1.getSolver().getSolutionCount();
        long sol2 = s2.getSolver().getSolutionCount();
        assertEquals(sol2, sol1, "nb sol incorrect");

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBACP() {
        Model model = new Model();
        IntVar cp = model.intVar("cp", 1, 10, false);
        BoolVar[] bv = model.boolVarArray("b1", 10);
        for (int i = 1; i <= 10; i++) {
            model.ifThenElse(bv[i - 1],
                    model.arithm(cp, "=", i),
                    model.arithm(cp, "!=", i));
        }

        IntVar cp2 = model.intVar("cp27", 1, 10, false);
        model.arithm(cp2, ">=", cp).post();

        BoolVar[] bv2 = model.boolVarArray("b2", 10);
        for (int i = 1; i <= 10; i++) {
            model.ifThenElse(bv2[i - 1],
                    model.arithm(model.intVar(i), "<", cp),
                    model.arithm(model.intVar(i), ">=", cp));
        }

        try {
            model.getSolver().propagate();
            cp.updateUpperBound(5, Null);
            model.getSolver().propagate();
            bv[0].instantiateTo(1, Null);
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }

    }

    @Test(groups = "1s", timeOut = 60000)
    public void test_wellaweg1() {
        Model s = new Model();

        IntVar[] row = new IntVar[3];
        row[0] = s.intVar(2);
        row[1] = s.intVar("R", 0, 100, true);
        row[2] = s.intVar(16);

        IntVar[] calc = new IntVar[2];
        calc[0] = s.offset(row[0], 2);
        calc[1] = s.intVar("C", 0, 80, true);
        s.sum(new IntVar[]{row[0], row[1]}, "=", calc[1]).post();

        Constraint[] constraints = new Constraint[4];
        constraints[0] = s.arithm(row[1], "=", calc[0]);
        constraints[1] = s.arithm(row[1], "!=", calc[0]);
        constraints[2] = s.arithm(row[2], "=", calc[1]);
        constraints[3] = s.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.boolVarArray("A", 2);

        s.ifThenElse(ab[0], constraints[0], constraints[1]);
        s.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.sum(ab, "=", ab.length - max_abs).post();

        while (s.getSolver().solve()) ;

        assertEquals(s.getSolver().getSolutionCount(), 2);

    }

    @Test(groups = "1s", timeOut = 60000)
    public void test_wellaweg3() {
        Model s = new Model();

        IntVar[] row = new IntVar[3];
        row[0] = s.intVar(2);
        row[1] = s.intVar("R", 0, 100, true);
        row[2] = s.intVar(16);

        IntVar[] calc = new IntVar[2];
        calc[0] = s.mul(row[0], 2);
        calc[1] = s.intVar("C", 0, 1600, true);
        s.times(row[0], row[1], calc[1]).post();

        Constraint[] constraints = new Constraint[4];
        constraints[0] = s.arithm(row[1], "=", calc[0]);
        constraints[1] = s.arithm(row[1], "!=", calc[0]);
        constraints[2] = s.arithm(row[2], "=", calc[1]);
        constraints[3] = s.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.boolVarArray("A", 2);

        s.ifThenElse(ab[0], constraints[0], constraints[1]);
        s.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.sum(ab, "=", ab.length - max_abs).post();

        while (s.getSolver().solve()) ;

        assertEquals(s.getSolver().getSolutionCount(), 2);

    }

    @Test(groups = "1s", timeOut = 60000)
    public void test_wellaweg4() {
        Model s = new Model();

        IntVar[] row = new IntVar[3];
        row[0] = s.intVar(20);
        row[1] = s.intVar("R", 0, 100, true);
        row[2] = s.intVar(5);

        IntVar[] calc = s.intVarArray("C", 2, 0, 100, true);

        s.div(row[0], s.intVar(2), calc[0]).post();
        s.div(row[0], row[1], calc[1]).post();

        Constraint[] constraints = new Constraint[4];
        constraints[0] = s.arithm(row[1], "=", calc[0]);
        constraints[1] = s.arithm(row[1], "!=", calc[0]);
        constraints[2] = s.arithm(row[2], "=", calc[1]);
        constraints[3] = s.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.boolVarArray("A", 2);

        s.ifThenElse(ab[0], constraints[0], constraints[1]);
        s.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.sum(ab, "=", ab.length - max_abs).post();

//        SearchMonitorFactory.log(s, true, false);
        while (s.getSolver().solve()) ;

        assertEquals(s.getSolver().getSolutionCount(), 2);

    }

    @Test(groups = "1s", timeOut = 60000)
    public void test_wellaweg5() {
        Model s = new Model();

        IntVar[] row = new IntVar[3];
        row[0] = s.intVar(100);
        row[1] = s.intVar("R1", 0, 100, true);
        row[2] = s.intVar(5);

        IntVar[] calc = s.intVarArray("C", 2, 0, 100, true);

        s.div(row[0], s.intVar(25), calc[0]).post();
        s.div(row[0], row[1], calc[1]).post();

        Constraint[] constraints = new Constraint[4];
        constraints[0] = s.arithm(row[1], "=", calc[0]);
        constraints[1] = s.arithm(row[1], "!=", calc[0]);
        constraints[2] = s.arithm(row[2], "=", calc[1]);
        constraints[3] = s.arithm(row[2], "!=", calc[1]);

        BoolVar[] ab = s.boolVarArray("A", 2);

        s.ifThenElse(ab[0], constraints[0], constraints[1]);
        s.ifThenElse(ab[1], constraints[2], constraints[3]);


        //one row must be wrong
        int max_abs = 1;
        s.sum(ab, "=", ab.length - max_abs).post();

//        SearchMonitorFactory.log(s, true, false);
        while (s.getSolver().solve()) ;

        assertEquals(s.getSolver().getSolutionCount(), 5);

    }

    @DataProvider(name = "reif")
    public Object[][] reif() {
        return new Object[][]{
                {2, 2},
                {1, 2},
                {0, 2},
                {2, 1},
                {1, 1},
                {0, 1},
                {2, 0},
                {1, 0},
                {0, 0},
        };
    }


    @Test(groups = "1s", timeOut = 60000, dataProvider = "reif")
    public void test_reif(int b1, int b2) {
        Model m = new Model();
        IntVar[] row = m.intVarArray("r", 3, 0, 5);
        BoolVar a1 = b1 == 2 ? m.boolVar() : b1 == 1 ? m.boolVar(true) : m.boolVar(false);
        m.sum(row, "=", 5).reifyWith(a1);
        BoolVar a2 = b2 == 2 ? m.boolVar() : b2 == 1 ? m.boolVar(true) : m.boolVar(false);
        m.sum(row, "=", 5).reifyWith(a2);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "reif")
    public void test_reif2(int b1, int b2) {
        Model m = new Model();
        IntVar[] row = m.intVarArray("r", 3, 0, 5);
        BoolVar a1 = b1 == 2 ? m.boolVar("b1") : b1 == 1 ? m.boolVar("b1", true) : m.boolVar("b1", false);
        m.sum(row, "=", 5).reifyWith(a1);
        BoolVar a2 = b2 == 2 ? m.boolVar("b2") : b2 == 1 ? m.boolVar("b2", true) : m.boolVar("b2", false);
        m.sum(row, "=", 5).reifyWith(a2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJuha1() throws ContradictionException {
        Model mode = new Model();
        IntVar r = mode.intVar("r", 1,10);
        BoolVar b = mode.member(r, new int[]{7,6,5,2}).reify();
        IntVar E = mode.intVar("e", 0,1);
        b.eq(E).post();
        E.ge(1).post();
        System.out.printf("%s\n", mode);
        mode.getSolver().propagate();
    }
    
    @Test(groups = "1s", expectedExceptions = IllegalArgumentException.class)
    public void testofir1() {
        Model choco2 = new Model("wololo");
        IntVar a = choco2.intVar("a", -4, -3, true);
        IntVar b = choco2.intVar("b", -4, 20, true);

        Constraint ib = choco2.arithm(a, "=", -4);
        Constraint den = choco2.arithm(b, "=", 2);
        choco2.ifThen(ib, den);

        Solver solver2 = choco2.getSolver();
        solver2.showDecisions();
        solver2.setSearch(Search.intVarSearch(
        	   // selects the variable of smallest domain size
        	   new FirstFail(choco2),
        	   // selects the smallest domain value (lower bound)
        	   new IntDomainMin()));
        while (choco2.getSolver().solve()) {
            System.out.println("A = " + a.getValue());
            System.out.println("B = " + b.getValue());
        }
        solver2.printShortStatistics();
    }

    // ========================================================================
    // Tests for issue #1240: Unsound reified variable disequality on bounded domains
    // ========================================================================

    /**
     * MWE from issue #1240: reifyXeqY with bounded domains and b=0 should not accept x==y
     * Expected: 2 solutions (y=-1 and y=1)
     * Bug: 3 solutions including invalid x=0, y=0, equal=0
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_reifyXeqY() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar equal = model.boolVar("equal");

        model.reifyXeqY(x, y, equal);
        model.arithm(x, "=", 0).post();
        model.arithm(equal, "=", 0).post();
        System.out.println(model);
        while (model.getSolver().solve()) {
            // With x=0 and equal=0, y must not be 0
            assertEquals(x.getValue(), 0, "x should be 0");
            assertEquals(equal.getValue(), 0, "equal should be 0");
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, equal=%d", 
                    x.getValue(), y.getValue(), equal.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using reifyXneY with bounded domains and b=1
     * Expected: 2 solutions (y=-1 and y=1)
     * Bug: 3 solutions including invalid x=0, y=0, notEqual=1
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_reifyXneY() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar notEqual = model.boolVar("notEqual");

        model.reifyXneY(x, y, notEqual);
        model.arithm(x, "=", 0).post();
        model.arithm(notEqual, "=", 1).post();

        while (model.getSolver().solve()) {
            // With x=0 and notEqual=1, y must not be 0
            assertEquals(x.getValue(), 0, "x should be 0");
            assertEquals(notEqual.getValue(), 1, "notEqual should be 1");
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, notEqual=%d", 
                    x.getValue(), y.getValue(), notEqual.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using reifyXeqYC with offset c=0
     * reifyXeqYC(x, y, c, b) means: b = (x == y + c)
     * With x=0, c=0, b=0: 0 != y+0, so y != 0
     * y in [-1,1], so solutions are y=-1 and y=1
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_reifyXeqYC() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar equal = model.boolVar("equal");

        model.reifyXeqYC(x, y, 0, equal);
        model.arithm(x, "=", 0).post();
        model.arithm(equal, "=", 0).post();

        while (model.getSolver().solve()) {
            // With x=0, c=0, equal=0: x != y+0 => 0 != y
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, equal=%d", 
                    x.getValue(), y.getValue(), equal.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using reifyXneYC with offset c=0
     * This should reproduce the issue with the interior value problem
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_reifyXneYC() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar notEqual = model.boolVar("notEqual");

        model.reifyXneYC(x, y, 0, notEqual);
        model.arithm(x, "=", 0).post();
        model.arithm(notEqual, "=", 1).post();

        while (model.getSolver().solve()) {
            // With x=0, c=0, notEqual=1: x != y+0 => 0 != y
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, notEqual=%d", 
                    x.getValue(), y.getValue(), notEqual.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using reifXrelYC with "="
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_reifXrelYC_eq() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar equal = model.boolVar("equal");

        model.reifXrelYC(x, "=", y, 0, equal);
        model.arithm(x, "=", 0).post();
        model.arithm(equal, "=", 0).post();

        while (model.getSolver().solve()) {
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, equal=%d", 
                    x.getValue(), y.getValue(), equal.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using reifXrelYC with "!="
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_reifXrelYC_ne() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar notEqual = model.boolVar("notEqual");

        model.reifXrelYC(x, "!=", y, 0, notEqual);
        model.arithm(x, "=", 0).post();
        model.arithm(notEqual, "=", 1).post();

        while (model.getSolver().solve()) {
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, notEqual=%d", 
                    x.getValue(), y.getValue(), notEqual.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using impXrelYC with "!="
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_impXrelYC() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar b = model.boolVar("b", true);

        model.impXrelYC(x, "!=", y, 0, b);
        model.arithm(x, "=", 0).post();

        while (model.getSolver().solve()) {
            // With b=true (instantiated), we should have x != y
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, b=%d", 
                    x.getValue(), y.getValue(), b.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using x.eq(y).boolVar() forced to false
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_eqBoolVar() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar equal = x.eq(y).boolVar();

        model.arithm(x, "=", 0).post();
        model.arithm(equal, "=", 0).post();

        while (model.getSolver().solve()) {
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, equal=%d", 
                    x.getValue(), y.getValue(), equal.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using x.ne(y).boolVar() forced to true
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_neBoolVar() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar notEqual = x.ne(y).boolVar();

        model.arithm(x, "=", 0).post();
        model.arithm(notEqual, "=", 1).post();

        while (model.getSolver().solve()) {
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, notEqual=%d", 
                    x.getValue(), y.getValue(), notEqual.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using x.in(y).boolVar() with single IntVar operand, forced to false
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_inBoolVar() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar in = x.in(y).boolVar();

        model.arithm(x, "=", 0).post();
        model.arithm(in, "=", 0).post();

        while (model.getSolver().solve()) {
            // in=0 means x not in y, so x != y
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, in=%d", 
                    x.getValue(), y.getValue(), in.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Variant using x.notin(y).boolVar() with single IntVar operand, forced to true
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_notinBoolVar() {
        Model model = new Model();
        IntVar x = model.intVar("x", 0, 1, true);
        IntVar y = model.intVar("y", -1, 1, true);
        BoolVar notin = x.notin(y).boolVar();

        model.arithm(x, "=", 0).post();
        model.arithm(notin, "=", 1).post();

        while (model.getSolver().solve()) {
            // notin=1 means x not in y, so x != y
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, notin=%d", 
                    x.getValue(), y.getValue(), notin.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Test with different domain configurations to ensure the issue is caught
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_variousDomains() {
        // Test with x in [1,2], y in [0,2], equal=0
        Model model = new Model();
        IntVar x = model.intVar("x", 1, 2, true);
        IntVar y = model.intVar("y", 0, 2, true);
        BoolVar equal = model.boolVar("equal");

        model.reifyXeqY(x, y, equal);
        model.arithm(x, "=", 1).post();
        model.arithm(equal, "=", 0).post();

        while (model.getSolver().solve()) {
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, equal=%d", 
                    x.getValue(), y.getValue(), equal.getValue());
        }
        assertEquals(model.getSolver().getSolutionCount(), 2, "Expected 2 solutions, got " + model.getSolver().getSolutionCount());
    }

    /**
     * Test with a different interior value configuration
     */
    @Test(groups = "1s", timeOut = 60000)
    public void testIssue1240_anotherInteriorValue() {
        Model model = new Model();
        IntVar x = model.intVar("x", 5, 10, true);
        IntVar y = model.intVar("y", 0, 10, true);
        BoolVar equal = model.boolVar("equal");

        model.reifyXeqY(x, y, equal);
        model.arithm(x, "=", 5).post();
        model.arithm(equal, "=", 0).post();

        while (model.getSolver().solve()) {
            assert x.getValue() != y.getValue() : 
                String.format("Invalid solution: x=%d, y=%d, equal=%d", 
                    x.getValue(), y.getValue(), equal.getValue());
        }
        // y can be 0,1,2,3,4,6,7,8,9,10 = 10 values
        assertEquals(model.getSolver().getSolutionCount(), 10, "Expected 10 solutions, got " + model.getSolver().getSolutionCount());
    }
}