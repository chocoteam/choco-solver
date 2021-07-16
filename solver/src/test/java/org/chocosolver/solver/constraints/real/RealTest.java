/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import static java.lang.System.out;
import static org.testng.Assert.assertEquals;

/**
 * -Djava.library.path=-Djava.library.path=/Users/cprudhom/Sources/Ibex/ibex-2.3.1/__build__/plugins/java
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/12
 */
public class RealTest {

    public void cmpDomains(double[] a1, double[] a2) {
        double DELTA = 1e-10;
        for (int i = 0; i < a1.length; i++)
            assertEquals(a1[i], a2[i], DELTA);
    }


    @Test(groups = "ibex", timeOut = 60000)
    public void test1() {
        for (int i = 0; i < 10; i++) {
            Ibex ibex = new Ibex(new double[]{0.001, 0.001});

            ibex.add_ctr("{0}+{1}=3");
            ibex.build();
            double domains[] = {1.0, 10.0, 1.0, 10.0};
            System.out.println("Before contract:");
            System.out.println("([" + domains[0] + "," + domains[1] + "] ; [" + domains[2] + "," + domains[3] + "])");

            int result = ibex.contract(0, domains);

            if (result == Ibex.FAIL) {
                System.out.println("Failed!");
            } else if (result == Ibex.CONTRACT) {
                System.out.println("After contract:");
                System.out.println("([" + domains[0] + "," + domains[1] + "] ; [" + domains[2] + "," + domains[3] + "])");
            } else {
                System.out.println("Nothing.");
            }
            ibex.release();
        }
    }

    @Test(groups = "ibex", timeOut = 60000)
        public void test11() {
            Ibex ibex = new Ibex(new double[]{0.1});
            ibex.add_ctr("{0}=59.5");
            ibex.build();
            double domains[] = {59.5, 59.5};
            // see: https://github.com/ibex-team/ibex-java/issues/2
            Assert.assertEquals(ibex.contract(0, domains, 0), Ibex.NOTHING);
            Assert.assertEquals(ibex.contract(0, domains, 1), Ibex.NOTHING);
            ibex.release();
        }

    @Test(groups = "ibex", timeOut = 60000)
    public void test2() {
        for (int i = 0; i < 10; i++) {
            Ibex ibex = new Ibex(new double[]{0.001, 0.001});
            ibex.add_ctr("{0}^2+{1}^2<=1");
            ibex.build();
            double[] domains;
            double vv = Math.sqrt(2.) / 2.;

            // CASE 1: the boolean is set to TRUE
            assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.TRUE), Ibex.FAIL);
            assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.TRUE), Ibex.ENTAILED);
            domains = new double[]{-2., 1., -2., 1.};
            assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.CONTRACT);
            cmpDomains(domains, new double[]{-1., 1., -1., 1.});
            assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.NOTHING);


            // CASE 2: the boolean is set to FALSE
            assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.FALSE), Ibex.FAIL);
            assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.FALSE), Ibex.ENTAILED);
            assertEquals(ibex.contract(0, new double[]{-2., 1., -2., -1.}, Ibex.FALSE), Ibex.NOTHING);
            domains = new double[]{0., 2., -vv, vv};
            assertEquals(ibex.contract(0, domains, Ibex.FALSE), Ibex.CONTRACT);
            cmpDomains(domains, new double[]{vv, 2., -vv, vv});

            // CASE 3: the boolean is set to UNKNOWN
            assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.FALSE_OR_TRUE), Ibex.FAIL);
            assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.FALSE_OR_TRUE), Ibex.ENTAILED);
            assertEquals(ibex.contract(0, new double[]{-2., 1., -2., -1.}, Ibex.FALSE_OR_TRUE), Ibex.NOTHING);
            domains = new double[]{0., 2., -vv, vv};
            assertEquals(ibex.contract(0, domains, Ibex.FALSE_OR_TRUE), Ibex.NOTHING);
            cmpDomains(domains, new double[]{0., 2., -vv, vv});

            ibex.release();
        }
    }

    @Test(groups = "ibex")
    public void test4() {
        for (int i = 0; i < 10; i++) {
            Model model = new Model();
            IntVar x = model.intVar("x", 0, 9, true);
            IntVar y = model.intVar("y", 0, 9, true);
            IntVar[] vars = {x, y};
//            RealVar[] vars = model.realIntViewArray(new IntVar[]{x, y}, precision);
            // Actually ,we need the calculated result like these :
            // x : [2.000000, 2.000000], y : [4.000000, 4.000000]
            // or x : [1.000000, 1.000000], y : [8.000000, 8.000000]
            // but it always like this : x : [2.418267, 2.418267], y : [3.308154, 3.308154]
//        rcons.discretize(x,y);
            model.realIbexGenericConstraint("{0} * {1} = 8", vars).post();
            Solver solver = model.getSolver();
            solver.setSearch(Search.randomSearch(vars, i));
            solver.findAllSolutions();
            assertEquals(solver.getSolutionCount(), 4);
//            assertEquals(y.getValue(), 1);
        }
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testFreemajb1() {
        Model model = new Model();

        // Declare variables
        RealVar attr = model.realVar("attr", 0.0, 20.0, 0.1);

        // Create and reify constraints to assign values to the real
        RealConstraint attrEquals1 = model.realIbexGenericConstraint("{0}=4.0", attr);
        BoolVar attrEquals1Reification = attrEquals1.reify();
        RealConstraint attrEquals2 = model.realIbexGenericConstraint("{0}=8.0", attr);
        BoolVar attrEquals2Reification = attrEquals2.reify();

        // Walk and print the solutions
        int numSolutions = 0;
        boolean foundSolution = model.getSolver().solve();
        while (foundSolution) {
            numSolutions++;
            foundSolution = model.getSolver().solve();
        }
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testFreemajb2() {
        Model model = new Model();

        RealVar x = model.realVar("x", 0.0, 5.0, 0.001);
        out.println("Before solving:");

        RealConstraint newRange = new RealConstraint("1.4142<{0};{0}<3.1416", x);
        newRange.post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        out.printf("%s\n", model.toString());

    }

    @Test(groups = "1s", timeOut = 60000)
        public void testFreemajb2b() {
        Model model = new Model();

        RealVar x = model.realVar("x", 0.0, 5.0, 0.001);
        out.println("Before solving:");

        x.gt(1.4142).equation().post();
        x.lt(3.1416).equation().post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        out.printf("%s\n", model.toString());

    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testFreemajb3() {
        Model model = new Model();

        RealVar x = model.realVar("x", 0.0, 5.0, 0.001);
        out.println("Before solving:");

        model.realIbexGenericConstraint("1.4142<{0};{0}<3.1416", x).post();

        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        out.printf("%s\n", model.toString());

    }

    @DataProvider(name = "coeffs")
    public Object[][] provideCoeffs() {
        return new String[][]{
                {"{0}*0.5) = {1}"},
                {"{0}/2) = {1}"},
        };
    }

    @Test(groups = "ibex", timeOut = 60000, dataProvider = "coeffs")
    public void testHM1(String coeffs) {
        Model model = new Model("Test model");
        double precision = 1.e-6;
        double MAX_VALUE = 10000;
        double MIN_VALUE = -10000;
        RealVar weldingCurrent = model.realVar("weldingCurrent", 120, 250, precision);
        RealVar MTBF_WS = model.realVar("MTBF_WS", MIN_VALUE, MAX_VALUE, precision);
        RealVar MTBF_MT = model.realVar("MTBF_MT", MIN_VALUE, MAX_VALUE, precision);
        RealVar global_min = model.realVar("global_min", MIN_VALUE, MAX_VALUE, precision);
        Solver solver = model.getSolver();
        model.realIbexGenericConstraint("(" + coeffs + ";{0}+100={2};min({1},{2}) ={3}", weldingCurrent, MTBF_WS, MTBF_MT, global_min).post();
        model.setPrecision(precision);
        model.setObjective(false, global_min);
        solver.showDecisions(() -> "" + solver.getNodeCount());
        while (solver.solve()) ;
    }

    @Test(groups = "1s", timeOut = 60000)
        public void testHM1a() {
        Model model = new Model("Test model");
        double precision = 1.e-6;
        double MAX_VALUE = 10000;
        double MIN_VALUE = -10000;
        RealVar weldingCurrent = model.realVar("weldingCurrent", 120, 250, precision);
        RealVar MTBF_WS = model.realVar("MTBF_WS", MIN_VALUE, MAX_VALUE, precision);
        RealVar MTBF_MT = model.realVar("MTBF_MT", MIN_VALUE, MAX_VALUE, precision);
        RealVar global_min = model.realVar("global_min", MIN_VALUE, MAX_VALUE, precision);
        Solver solver = model.getSolver();
        weldingCurrent.mul(.5).eq(MTBF_WS).equation().post();
        weldingCurrent.add(100).eq(MTBF_MT).equation().post();
        global_min.eq(weldingCurrent.min(MTBF_WS).min(MTBF_MT)).equation().post();
        model.setPrecision(precision);
        model.setObjective(false, global_min);
        solver.showDecisions(() -> "" + solver.getNodeCount());
        while (solver.solve()) ;
    }

    @Test(groups = "1s", timeOut = 60000)
            public void testHM1b() {
        Model model = new Model("Test model");
        double precision = 1.e-6;
        double MAX_VALUE = 10000;
        double MIN_VALUE = -10000;
        RealVar weldingCurrent = model.realVar("weldingCurrent", 120, 250, precision);
        RealVar MTBF_WS = model.realVar("MTBF_WS", MIN_VALUE, MAX_VALUE, precision);
        RealVar MTBF_MT = model.realVar("MTBF_MT", MIN_VALUE, MAX_VALUE, precision);
        RealVar global_min = model.realVar("global_min", MIN_VALUE, MAX_VALUE, precision);
        Solver solver = model.getSolver();

        weldingCurrent.div(2).eq(MTBF_WS).equation().post();
        weldingCurrent.add(100).eq(MTBF_MT).equation().post();
        global_min.eq(weldingCurrent.min(MTBF_WS).min(MTBF_MT)).equation().post();
        model.setPrecision(precision);
        model.setObjective(false, global_min);
        solver.showDecisions(() -> "" + solver.getNodeCount());
        while (solver.solve()) ;
    }


    @Test(groups = "ibex", timeOut = 60000)
    public void testHM2() {
        Model model = new Model("Default model");
        double precision = 1.e-1;
        RealVar current = model.realVar("current", 121, 248, precision);
        RealVar MTBF = model.realVar("MTBF", 0, 300, precision);
        RealVar MTBF_MT = model.realVar("MTBF_MT", 0, 200, precision);
        Solver solver = model.getSolver();
        model.realIbexGenericConstraint("932.6-(8.664*{0})+(0.02678*({0}^2))-(0.000028*({0}^3)) = {1}", current, MTBF_MT).post();
        model.realIbexGenericConstraint("min(20,{0}) = {1}", MTBF_MT, MTBF).post();//MTBF;
        model.setPrecision(precision);
        model.setObjective(false, MTBF);
        solver.solve();
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = UnsupportedOperationException.class)
        public void testHM2a() {
        Model model = new Model("Default model");
        double precision = 1.e-1;
        RealVar current = model.realVar("current", 121, 248, precision);
        RealVar MTBF = model.realVar("MTBF", 0, 300, precision);
        RealVar MTBF_MT = model.realVar("MTBF_MT", 0, 200, precision);
        Solver solver = model.getSolver();
        model.realVar(932.6)
                .sub(current.mul(8.664))
                .add(model.realVar(0.02678).mul(current.pow(2)))
                .add(model.realVar(0.000028).mul(current.pow(3)))
                .eq(MTBF_MT).post();
        MTBF_MT.min(20).eq(MTBF).post();
        model.setPrecision(precision);
        model.setObjective(false, MTBF);
        solver.solve();
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testHM21() {
        Ibex ibex = new Ibex(new double[]{1.e-1, 1.e-1, 1.e-1});
        ibex.add_ctr("932.6-(8.664*{0})+(0.02678*({0}^2))-(0.000028*({0}^3)) = {1}");
        ibex.add_ctr("min(20,{1}) = {2}");
        int result = ibex.contract(0, new double[]{121., 248., 0., 200.}, Ibex.TRUE);
        System.out.printf("Expected: %d, found: %d\n", Ibex.NOTHING, result);
    }

    @Test(groups = "ibex")
    public void testPG1() throws Exception {
        Model model = new Model();
        RealVar rv = model.realVar(0, 5, 4.E-2);
        BoolVar bv = model.realIbexGenericConstraint("{0}=4", rv).reify();
        model.arithm(bv, "=", 0).post();
        Solver solver = model.getSolver();


        solver.setSearch(
                Search.inputOrderLBSearch(bv),
                Search.realVarSearch(4.E-2, rv));
        while (solver.solve()) ;
        ;
        assertEquals(model.getSolver().getSolutionCount(), 63);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJJ35() {
        Ibex ibex = new Ibex(new double[]{1.0E-1});
        ibex.add_ctr("{0} = 4");
        double domains[] = {0., 5.};
        ibex.build();
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE), Ibex.NOTHING);
    }

    @Test(groups = "ibex")
    public void testJiiTee1() throws Exception {
        Model model = new Model("model");
        RealVar dim_A = model.realVar("dim_A", 150.0, 470.0, 1.0E-5);
        IntVar ll = model.intVar("ll", 1, 5, false);
        BoolVar[] dim_A_guards = new BoolVar[5];
        dim_A_guards[0] = model.realIbexGenericConstraint("{0} = 150.0", dim_A).reify();
        dim_A_guards[1] = model.realIbexGenericConstraint("{0} = 195.0", dim_A).reify();
        dim_A_guards[2] = model.realIbexGenericConstraint("{0} = 270.0", dim_A).reify();
        dim_A_guards[3] = model.realIbexGenericConstraint("{0} = 370.0", dim_A).reify();
        dim_A_guards[4] = model.realIbexGenericConstraint("{0} = 470.0", dim_A).reify();

        Constraint bigA = model.realIbexGenericConstraint("{0} > 300", dim_A);
        Constraint smallA = model.realIbexGenericConstraint("{0} < 200", dim_A);

        // The following or does not work.
        // the first 'and' within 'or' works, the second does not
        // if the order is reversed, also the results change: the results of the first 'and' are found
        // How to get these both?
        model.or(
                model.and(
                        bigA,
                        model.arithm(ll, "<", 3)),
                model.and(
                        smallA,
                        model.arithm(ll, ">=", 3))
        ).post();
        model.sum(dim_A_guards, "=", 1).post();
        LinkedList<Variable> printVars = new LinkedList<Variable>();
        printVars.add(dim_A);
        printVars.add(ll);
        Solver solver = model.getSolver();

    /*try {
        model.getSolver().propagate();
    } catch (ContradictionException e) {
        e.printStackTrace();
    }*/
        int i = 0;
        while (solver.solve()) {
            i++;
        }
        assertEquals(solver.getSolutionCount(), 10);
    }

    @Test(groups = "1s")
    public void testJiiTee1a() throws Exception {
        Model model = new Model("model");
        RealVar dim_A = model.realVar("dim_A", 150.0, 470.0, 1.0E-5);
        IntVar ll = model.intVar("ll", 1, 5, false);
        BoolVar[] dim_A_guards = new BoolVar[5];
        dim_A_guards[0] = dim_A.eq(150.).reify();
        dim_A_guards[1] = dim_A.eq(195.).reify();
        dim_A_guards[2] = dim_A.eq(270.).reify();
        dim_A_guards[3] = dim_A.eq(370.).reify();
        dim_A_guards[4] = dim_A.eq(470.).reify();

        Constraint bigA = dim_A.gt(300).equation();
        Constraint smallA = dim_A.lt(200).equation();

        // The following or does not work.
        // the first 'and' within 'or' works, the second does not
        // if the order is reversed, also the results change: the results of the first 'and' are found
        // How to get these both?
        model.or(
                model.and(
                        bigA,
                        model.arithm(ll, "<", 3)),
                model.and(
                        smallA,
                        model.arithm(ll, ">=", 3))
        ).post();
        model.sum(dim_A_guards, "=", 1).post();
        LinkedList<Variable> printVars = new LinkedList<Variable>();
        printVars.add(dim_A);
        printVars.add(ll);
        Solver solver = model.getSolver();

        /*try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }*/
        int i = 0;
        while (solver.solve()) {
            i++;
        }
        assertEquals(solver.getSolutionCount(), 10);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testPeter() {
        Random ds = new Random();
        Model model = new Model();
        RealVar[] rv1 = model.realVarArray(10, 0, 10, 0.1d);
        RealVar[] rv2 = model.realVarArray(10, 0, 10, 0.1d);
        RealVar opt = model.realVar(0, 100, 0.1d);
        RealVar srv1 = model.realVar(0, 100, 0.1d);
        RealVar srv2 = model.realVar(0, 100, 0.1d);
        BoolVar[] bv1 = model.boolVarArray(10);
        BoolVar[] bv2 = model.boolVarArray(10);
        model.realIbexGenericConstraint("{0}={1}+{2}+{3}+{4}+{5}+{6}+{7}+{8}+{9}+{10}",
                srv1, rv1[0], rv1[1], rv1[2], rv1[3], rv1[4], rv1[5], rv1[6], rv1[7], rv1[8], rv1[9]).post();

        model.realIbexGenericConstraint("{0}={1}+{2}+{3}+{4}+{5}+{6}+{7}+{8}+{9}+{10}",
                srv2, rv2[0], rv2[1], rv2[2], rv2[3], rv2[4], rv2[5], rv2[6], rv2[7], rv2[8], rv2[9]).post();

        for (int i = 0; i < 10; ++i) {
            model.ifThenElse(bv1[i], model.realIbexGenericConstraint("{0}=" + ds.nextDouble() * 10.0, rv1[i]), model.realIbexGenericConstraint("{0}=0.0", rv1[i]));
            model.ifThenElse(bv2[i], model.realIbexGenericConstraint("{0}=" + ds.nextDouble() * 10.0, rv2[i]), model.realIbexGenericConstraint("{0}=0.0", rv2[i]));
            model.arithm(bv1[i], "!=", bv2[i]).post();
        }
        //NO CRASH
        //	model.realIbexGenericConstraint("{0}={1}+{2}", opt, srv1, srv2).post();
        //CRASH
        model.realIbexGenericConstraint("{0}=max({1},{2})", opt, srv1, srv2).post();
        model.setObjective(false, opt);

        while (model.getSolver().solve()) {}
    }

    @Test(groups = "1s", timeOut = 60000)
        public void testPetera() {
        Random ds = new Random();
        Model model = new Model();
        RealVar[] rv1 = model.realVarArray(10, 0, 10, 0.1d);
        RealVar[] rv2 = model.realVarArray(10, 0, 10, 0.1d);
        RealVar opt = model.realVar(0, 100, 0.1d);
        RealVar srv1 = model.realVar(0, 100, 0.1d);
        RealVar srv2 = model.realVar(0, 100, 0.1d);
        BoolVar[] bv1 = model.boolVarArray(10);
        BoolVar[] bv2 = model.boolVarArray(10);
        srv1.eq(
                rv1[0].add(rv1[0]).add(rv1[1]).add(rv1[2]).add(rv1[3]).add(rv1[4])
                        .add(rv1[5]).add(rv1[6]).add(rv1[7]).add(rv1[8]).add(rv1[9])
        ).post();
        srv1.eq(
                rv2[0].add(rv2[0]).add(rv2[1]).add(rv2[2]).add(rv2[3]).add(rv2[4])
                        .add(rv2[5]).add(rv2[6]).add(rv2[7]).add(rv2[8]).add(rv2[9])
        ).post();
        for (int i = 0; i < 10; ++i) {
            model.ifThenElse(bv1[i],
                    rv1[i].eq(ds.nextDouble() * 10.0).equation(),
                    rv1[i].eq(0.0).equation()
            );
            model.ifThenElse(bv2[i],
                    rv2[i].eq(ds.nextDouble() * 10.0).equation(),
                    rv2[i].eq(0.0).equation()
            );
            model.arithm(bv1[i], "!=", bv2[i]).post();
        }
        //NO CRASH
        //	model.realIbexGenericConstraint("{0}={1}+{2}", opt, srv1, srv2).post();
        //CRASH
        opt.eq(srv1.max(srv2)).post();
        model.setObjective(false, opt);

        while (model.getSolver().solve()) {
        }
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJiTee1() throws ContradictionException {
        double[] posA = new double[]{150.0, 195.0, 270.0, 370.0, 470.0};
        Model model = new Model("model");
        IntVar load = model.intVar("load", new int[]{0, 100, 200, 300, 400, 500, 600, 700});
        double min = 150.0;
        double max = 470.0;
        RealVar dim_A = model.realVar("dim_A", min, max, 1.0E-5);
        BoolVar[] rVarGuards = new BoolVar[posA.length];
        for (int i = 0; i < posA.length; i++) {
            rVarGuards[i] = model.realIbexGenericConstraint("{0} = " + posA[i], dim_A).reify();
        }
        model.sum(rVarGuards, "=", 1).post();

        model.realIbexGenericConstraint("{0}<=271.", dim_A).post();
        model.arithm(load, ">", 400).post();
        for (int i = 0; i < 500; i++) {
            model.realIbexGenericConstraint("{0} > " + i, dim_A);
            System.gc();
        }
        model.getSolver().findSolution();
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJiTee1a() {
        double[] posA = new double[]{150.0, 195.0, 270.0, 370.0, 470.0};
        Model model = new Model("model");
        IntVar load = model.intVar("load", new int[]{0, 100, 200, 300, 400, 500, 600, 700});
        double min = 150.0;
        double max = 470.0;
        RealVar dim_A = model.realVar("dim_A", min, max, 1.0E-5);
        BoolVar[] rVarGuards = new BoolVar[posA.length];
        for (int i = 0; i < posA.length; i++) {
            rVarGuards[i] = dim_A.eq(posA[i]).reify();
        }
        model.sum(rVarGuards, "=", 1).post();
        dim_A.le(271.).post();
        model.arithm(load, ">", 400).post();
        for (int i = 0; i < 500; i++) {
            dim_A.gt(i);
            System.gc();
        }
        model.getSolver().findSolution();
    }


    @Test(groups = "ibex", timeOut = 60000)
    public void testPostUnpost() {
        LinkedList<Variable> printVars = new LinkedList<Variable>();
        Constraint stickyCstr = null;
        Random rr = new Random(2); //2 gives a suitable first requirement 500 for 'load'
        double[] posA = new double[]{150.0, 195.0, 270.0, 370.0, 470.0};
        Model model = new Model("model");
        IntVar load = model.intVar("load", new int[]{0, 100, 200, 300, 400, 500, 600, 700});
        RealVar dim_A = addEnumReal(model, "dim_A", posA);

        model.and(
                model.realIbexGenericConstraint("{0}<=271.", dim_A),
                model.arithm(load, ">", 400)).post();

        printVars.add(dim_A);
        printVars.add(load);


        int sameRoundPostUnpost = 0;
        //Repeatedly post / unpost. This is unstable on Windows, Ibex crashes quite often. But main concern is to make this work!
        //I cannot understand why solutions are lost after the first contradiction has been found, even when propagation is not on!
        for (int round = 0; round < 350; round++) {
            model.getSolver().reset();
            model.getEnvironment().worldPush();

            //Randomly unpost a sticky constraint that remains between iterations. Probability of unpost() annd permanent removal is higher than creation and post()
            boolean unPostedNow = false;
            if (stickyCstr != null) {
                int r = rr.nextInt(100);
                if (r <= 12) {
                    model.unpost(stickyCstr);
                    stickyCstr = null;
                    unPostedNow = true;
                }
            }

            //a constraint at each round: post and unpost
            Constraint c;
            int reqInt = (round % 100);
            c = model.realIbexGenericConstraint("{0} > " + 5 * reqInt, dim_A);
            c.post();

            //Randomly post a sticky constraint that remains between iterations. Probability to post() is lower than unpost()
            boolean postedNow = false;
            if (stickyCstr == null) {
                int r = rr.nextInt(100);
                if (r <= 7) {
                    stickyCstr = model.arithm(load, "=", r * 100);
                    model.post(stickyCstr);
                    postedNow = true;
                }
            }

            if (postedNow && unPostedNow) {
                sameRoundPostUnpost++;
            }

            boolean propagate = false;
            if (propagate) {
                model.getSolver().getEnvironment().worldPush();
                try {
                    model.getSolver().propagate();
                } catch (ContradictionException e) {
                    e.printStackTrace();
                }
                model.getSolver().getEnvironment().worldPop();
            }

            int i = 0;
            while (model.getSolver().solve()) {
                i++;
            }
            model.unpost(c);
        }
    }

    private static RealVar addEnumReal(Model model, String name, double[] possibles) {
        double min = possibles[0];
        double max = possibles[possibles.length - 1];
        RealVar rVar = model.realVar(name, min, max, 1.0E-5);
        BoolVar[] rVarGuards = new BoolVar[possibles.length];
        for (int i = 0; i < possibles.length; i++)
            rVarGuards[i] = model.realIbexGenericConstraint("{0} = " + possibles[i], rVar).reify();
        model.sum(rVarGuards, "=", 1).post();
        return rVar;

    }


    @Test(groups = "ibex", timeOut = 60000)
    public void testDetec() {
        Model model = new Model();

        RealVar x = model.realVar("x", 0.0, 5.0, 0.001);
        RealVar y = model.realVar("y", 0.0, 5.0, 0.001);
        RealVar z = model.realVar("z", 0.0, 5.0, 0.001);

        RealConstraint newRange = new RealConstraint("1.4142<{0};{2}<3.1416;{1}>{0};{2}*2<{0}", x, y, z);
        assertEquals(newRange.toString(),
                "REALCONSTRAINT ([" +
                        "RealPropagator(x) ->(\"1.4142<{0}\"), " +
                        "RealPropagator(z) ->(\"{0}<3.1416\"), " +
                        "RealPropagator(y, x) ->(\"{0}>{1}\"), " +
                        "RealPropagator(z, x) ->(\"{0}*2<{1}\")])");

    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJJ1() {
        Ibex ibex = new Ibex(new double[]{1.0E-5});
        ibex.add_ctr("{0}<=200.0");
        ibex.build();
        double domains[] = {150., 470.};
        Assert.assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.CONTRACT);
        Assert.assertEquals(domains[0], 150.);
        Assert.assertEquals(domains[1], 200.);
        domains[0] = 150.;
        domains[1] = 470.;
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE), Ibex.CONTRACT);
        Assert.assertEquals(domains[0], 200.);
        Assert.assertEquals(domains[1], 470.);
        domains[0] = 150.;
        domains[1] = 470.;
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE_OR_TRUE), Ibex.NOTHING);
        Assert.assertEquals(domains[0], 150.);
        Assert.assertEquals(domains[1], 470.);

        domains[0] = 201.;
        domains[1] = 470.;
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE_OR_TRUE), Ibex.FAIL);
        Assert.assertEquals(domains[0], 201.);
        Assert.assertEquals(domains[1], 470.);

        domains[0] = 150.;
        domains[1] = 199.;
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE_OR_TRUE), Ibex.ENTAILED);
        Assert.assertEquals(domains[0], 150.);
        Assert.assertEquals(domains[1], 199.);

        domains[0] = 201.;
        domains[1] = 470.;
        Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE), Ibex.FAIL);
        Assert.assertEquals(domains[0], 201.);
        Assert.assertEquals(domains[1], 470.);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJJ2() {
        Ibex ibex = new Ibex(new double[]{1.0E-5});
        ibex.add_ctr("{0}=150.0");
        ibex.build();
        double domains[] = {150., 150.};
        Assert.assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.NOTHING);
        Assert.assertEquals(domains[0], 150.);
        Assert.assertEquals(domains[1], 150.);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJJ3() {
        Ibex ibex = new Ibex(new double[]{1.0E-5});
        ibex.add_ctr("{0} < 150.0");
        ibex.build();
        double domains[] = {140., 151.};
        Assert.assertEquals(ibex.start_solve(domains), Ibex.STARTED);
        while (ibex.next_solution(domains) != Ibex.SEARCH_OVER) { }
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testMove1() {
        Model model = new Model();
        RealVar[] x = model.realVarArray(3, 0., 5., 1.E-2);
        RealVar m = model.realVar(0.0, 5.0, 1.E-2);
        model.realIbexGenericConstraint(
                "({0} + {1} + {2})/ 3 = {3};",
                ArrayUtils.append(x, new RealVar[]{m})
        ).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.ibexSolving(model));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 1);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testMove2() {
        Model model = new Model();
        IntVar[] x = model.intVarArray(3, 0, 5);
        RealVar m = model.realVar(0.0, 5.0, 1.E-2);
        RealVar n = model.realVar(0.0, 5.0, 1.E-2);
        model.realIbexGenericConstraint(
                "({0} + {1} + {2})/ 3 = {3};",
                ArrayUtils.append(x, new RealVar[]{m})
        ).post();
        model.realIbexGenericConstraint(
                "{0} + {1} = 2.6",
                m, n
        ).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.randomSearch(x, 0), Search.ibexSolving(model));

        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 108);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJJ34() {
        Ibex ibex = new Ibex(new double[]{-1.0, -1.0, -1.0, 1.0E-2, 1.0E-2});
        ibex.add_ctr("({0} + {1} + {2}) / 3 = {3}");
        ibex.add_ctr("{3} + {4} < 4.5");
        ibex.build();
        double domains[] = {0., 5., 0., 5., 0., 5., 0., 5., 0., 5.};
        Assert.assertEquals(ibex.contract(1, domains, Ibex.TRUE), Ibex.CONTRACT);
    }


    @Test(groups = "ibex", timeOut = 60000)
    public void testMove3() {
        Model model = new Model();
        RealVar[] x = model.realVarArray(3, -10., 10., 1.E-2);
        model.realIbexGenericConstraint(
                " {0}^2*{1}^2*{2}^2=1;\n" +
                        " {0}^2={1}^2;\n" +
                        " abs({0})=abs({2});",
                x).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.ibexSolving(model));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 8);
    }

    @Test(groups = "ibex"/*, timeOut = 60000*/)
    public void testMove4() {
        Model model = new Model();
        RealVar[] y = model.realVarArray(3, -10., 10., 1.E-5);
        model.realIbexGenericConstraint(
                "{0}^2*{1}^2*{2}^2=1;\n" +
                        "{0}^2={1}^2;\n" +
                        "abs({0})=abs({2});",
                y).post();
        Solver solver = model.getSolver();

        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 8);
    }

    @Test(groups = "1s", expectedExceptions = UnsupportedOperationException.class)
        public void testMove4a() {
        Model model = new Model();
        RealVar[] y = model.realVarArray(3, -10., 10., 1.E-5);
        y[0].pow(2).mul(y[1]).pow(2).mul(y[2]).pow(2).eq(1).post();
        y[0].pow(2).eq(y[1].pow(2)).post();
        y[0].abs().eq(y[1].abs()).post();
        model.realIbexGenericConstraint(
                "{0}^2*{1}^2*{2}^2=1;\n" +
                        "{0}^2={1}^2;\n" +
                        "abs({0})=abs({2});",
                y).post();
        Solver solver = model.getSolver();

        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 8);
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testMove5() {
        Model model = new Model();
        RealVar y = model.realVar(-10., 10., 1.E-1);
        BoolVar b = model.boolVar();
        String f1 = "{0}>= 1.";
        String f2 = "{0}<= 2.";
        model.realIbexGenericConstraint(
                f1+";"+f2,
                y).reifyWith(b);
        Solver solver = model.getSolver();
        solver.setSearch(Search.realVarSearch(1.E-1, y));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 100);
    }

    @Test(groups = "ibex", timeOut = 60000)
        public void testMove5a() {
        Model model = new Model();
        RealVar y = model.realVar(-10., 10., 1.E-1);
        BoolVar b = model.boolVar();
        model.and(
                y.ge(1.).equation(),
                y.le(2.).equation()
        ).reifyWith(b);
        Solver solver = model.getSolver();
        solver.setSearch(Search.realVarSearch(1.E-1, y));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 101);
    }

    @Test(groups="ibex", timeOut=60000)
    public void testJuha1() {
        Model model = new Model("model");
        IntVar foo = model.intVar("foo", 0, 20);
        IntVar wow = model.intVar("wow", new int[]{1, 2, 4});
        RealVar rfoo = model.realIntView(foo, 1E-5);
        RealVar rwow = model.realIntView(wow, 1E-5);
        model.realIbexGenericConstraint("{0} / {1} = 4.5", rfoo, rwow).reify();
        model.arithm(foo, "!=", 10).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(foo, wow));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 60);
    }

    @Test(groups="ibex", timeOut=60000)
    public void testJuha2() {
        Model model = new Model("model");
        IntVar foo = model.intVar("foo", new int[]{0,15, 20});
        IntVar wow = model.intVar("wow", new int[]{1, 2, 4});
        RealVar rfoo = model.realIntView(foo, 1E-5);
        RealVar rwow = model.realIntView(wow, 1E-5);
        model.realIbexGenericConstraint("{0} / {1} = 4.5", rfoo, rwow).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(foo, wow));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJuha2a() {
        Model model = new Model("model");
        IntVar foo = model.intVar("foo", new int[]{0,15, 20});
        IntVar wow = model.intVar("wow", new int[]{1, 2, 4});
        RealVar rfoo = model.realVar("rfoo", 0,20, 1e-5);
        RealVar rwow = model.realVar("rwow", 1,4, 1e-5);
        model.eq(rfoo, foo).post();
        model.eq(rwow, wow).post();
        rfoo.div(rwow).eq(4.5).post();
        model.arithm(foo, "!=", 10).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(foo, wow));
        solver.showSolutions();
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = UnsupportedOperationException.class)
    public void testJuha2b() {
        Model model = new Model("model");
        IntVar foo = model.intVar("foo", 0, 20);
        IntVar wow = model.intVar("wow", new int[]{1, 2, 4});
        RealVar rfoo = model.realIntView(foo, 1E-5);
        RealVar rwow = model.realIntView(wow, 1E-5);
        rfoo.div(rwow).eq(4.5).post();
        model.arithm(foo, "!=", 10).post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.inputOrderLBSearch(foo, wow));
        solver.findAllSolutions();
        Assert.assertEquals(solver.getSolutionCount(), 0);
    }

    @Test(groups="ibex", timeOut=60000, threadPoolSize = 4, invocationCount = 10, priority = 10)
    public void testJuha3(){
        Model model = new Model("model" + Thread.currentThread().getId());
        IntVar dim_H = model.intVar("dim_h", new int[]{2000, 2100, 2200});
        RealVar dim_A = model.realVar("dim_A", 150.0, 470.0, 1.0E-5);
        BoolVar[] dim_A_guards = new BoolVar[5];
        dim_A_guards[0] = new RealConstraint("{0} = 150.0", dim_A).reify();
        dim_A_guards[1] = new RealConstraint("{0} = 195.0", dim_A).reify();
        dim_A_guards[2] = new RealConstraint("{0} = 270.0", dim_A).reify();
        dim_A_guards[3] = new RealConstraint("{0} = 370.0", dim_A).reify();
        dim_A_guards[4] = new RealConstraint("{0} = 470.0", dim_A).reify();
        model.sum(dim_A_guards, "=", 1).post();
        RealVar dim_H_asReal = model.realIntView(dim_H, 1.0E-5);
        model.realIbexGenericConstraint("{0}+{1} > 2500", dim_A, dim_H_asReal).post();

        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 3);
    }

    @Test(enabled = false, groups="1s", timeOut=60000, threadPoolSize = 4, invocationCount = 10, priority = 10)
    public void testJuha3a() {
        Model model = new Model("model" + Thread.currentThread().getId());
        IntVar dim_H = model.intVar("dim_h", new int[]{2000, 2100, 2200});
        RealVar dim_A = model.realVar("dim_A", 150.0, 470.0, 1.0E-5);
        BoolVar[] dim_A_guards = new BoolVar[5];
        dim_A_guards[0] = dim_A.eq(150.).reify();
        dim_A_guards[1] = dim_A.eq(195.).reify();
        dim_A_guards[2] = dim_A.eq(270.).reify();
        dim_A_guards[3] = dim_A.eq(370.).reify();
        dim_A_guards[4] = dim_A.eq(470.).reify();
        model.sum(dim_A_guards, "=", 1).post();
        RealVar dim_H_asReal = model.realVar(dim_H.getLB(), dim_H.getUB(), 1.0E-5);
        model.eq(dim_H_asReal, dim_H).post();
        dim_A.add(dim_H_asReal).gt(2500.).post();

        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 3);
    }


    private static synchronized void build(Ibex ibex){
        ibex.build();
    }


    @Test(groups="ibex", timeOut=60000, threadPoolSize = 4, invocationCount = 10, priority = 10)
    public void testJuha4(){
        double eps=1e-7;
        Ibex ibex = new Ibex(new double[]{eps,eps,eps,eps,eps,eps,eps,eps});
        ibex.add_ctr("3*{0}*({1}-2*{0})+{1}^2/4=0");
        ibex.add_ctr("3*{1}*({2}-2*{1}+{0})+({2}-{0})^2/4=0");
        ibex.add_ctr("3*{2}*({3}-2*{2}+{1})+({3}-{1})^2/4=0");
        ibex.add_ctr("3*{3}*({4}-2*{3}+{2})+({4}-{2})^2/4=0");
        ibex.add_ctr("3*{4}*({5}-2*{4}+{3})+({5}-{3})^2/4=0");
        ibex.add_ctr("3*{5}*({6}-2*{5}+{4})+({6}-{4})^2/4=0");
        ibex.add_ctr("3*{6}*({7}-2*{6}+{5})+({7}-{5})^2/4=0");
        ibex.add_ctr("3*{7}*(20-2*{7}+{6})+(20-{6})^2/4=0");
        build(ibex);
        double L=1e8;
        double domains[]={-L,L,-L,L,-L,L,-L,L,-L,L,-L,L,-L,L,-L,L};
        ibex.start_solve(domains);
        for (int i=0; i<256; i++) {
            if (ibex.next_solution(domains)!=Ibex.SOLUTION) {
                ibex.release();
                Assert.fail();
            }
        }
        ibex.next_solution(domains);
        ibex.release();
    }

    @Test(groups="ibex", timeOut=60000)
    public void testFN1() {
        Model model = new Model("Environment generation problem");
        //A
        RealVar x_a = model.realVar("X_a", 0, 2, 1.0d);
        RealVar y_a = model.realVar("Y_a", 0, 2, 1.0d);
        RealVar z_a = model.realVar("Z_a", 0, 270, 90.0d);

        //A
        RealVar x_b = model.realVar("X_b", 0, 2, 1.0d);
        RealVar y_b = model.realVar("Y_b", 0, 2, 1.0d);
        RealVar z_b = model.realVar("Z_b", 0, 270, 90.0d);

        model.post(model.realIbexGenericConstraint("{0}={1}+cos({2})", x_b, x_a, z_a));
        model.post(model.realIbexGenericConstraint("{0}={1}+sin({2})", y_b, y_a, z_b));
        Assert.assertNotNull(model.getSolver().findSolution());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testFN1a() {
        Model model = new Model("Environment generation problem");
        //A
        RealVar x_a = model.realVar("X_a", 0, 2, 1.0d);
        RealVar y_a = model.realVar("Y_a", 0, 2, 1.0d);
        RealVar z_a = model.realVar("Z_a", 0, 270, 90.0d);

        //A
        RealVar x_b = model.realVar("X_b", 0, 2, 1.0d);
        RealVar y_b = model.realVar("Y_b", 0, 2, 1.0d);
        RealVar z_b = model.realVar("Z_b", 0, 270, 90.0d);

        x_b.eq(x_a.add(z_a.cos())).post();
        y_b.eq(y_a.add(z_b.sin())).post();
        Assert.assertNotNull(model.getSolver().findSolution());
    }

    @Test(groups="ibex", timeOut=60000, expectedExceptions = SolverException.class)
    public void testFN2(){
        Model model = new Model("Environment generation problem");
        RealVar x_a = model.realVar("X_a", 0, 2, 1.0d);
        RealVar y_a = model.realVar("Y_a", 0, 2, 1.0d);
        RealVar z_a = model.realVar("Z_a", 0, 270, 90.0d);
        model.post(model.realIbexGenericConstraint("{0}={1}+cos{2}", y_a, x_a, z_a));
        model.getSolver().findSolution();
    }

    @Test(groups="ibex", timeOut=60000)
    public void testRoberto1(){
        Model model = new Model();
        double precision = 1.e-4;

        RealVar var1 = model.realVar("var1", 0.1);
        RealVar var2 = model.realVar("var2", 0.2);
        RealVar var3 = model.realVar("var3", -5, 5, precision);

        model.realIbexGenericConstraint("{0}+{1}={2}", new RealVar[] {var1,var2,var3}).post();

        Solver solver = model.getSolver();


        Assert.assertTrue(solver.solve());
    }

    @Test(groups="ibex", timeOut=60000)
    public void testRoberto1a() {
        Model model = new Model();
        double precision = 1.e-4;

        RealVar var1 = model.realVar("var1", 0.1);
        RealVar var2 = model.realVar("var2", 0.2);
        RealVar var3 = model.realVar("var3", -5, 5, precision);

        var1.add(var2).eq(var3).post();

        Solver solver = model.getSolver();


        Assert.assertTrue(solver.solve());
    }

    @Test(groups="ibex", timeOut=60000)
    public void testRoberto3(){
        Ibex ibex = new Ibex(new double[]{1.e-1, 1.e-1, 1.e-4});
        ibex.add_ctr("{0}+{1}={2}");
        ibex.build();
        double domains[] = {0.1, 0.1, 0.2, 0.2, -5.0, 5.0};
        Assert.assertEquals(ibex.contract(0, domains), Ibex.CONTRACT);
        ibex.release();
    }

    @Test(groups="ignored", timeOut=60000)
    public void testRoberto4(){
        Ibex ibex = new Ibex(new double[]{1.e-1, 1.e-1, 1.e-4});
        ibex.add_ctr("{0}+{1}=pi*{2}");
        Assert.assertTrue(ibex.build());
        double domains[] = {0.1, 0.1, 0.2, .2, -5.0, 5.0};
        Assert.assertEquals(ibex.contract(0, domains), Ibex.CONTRACT);
        ibex.release();
    }

    @Test(groups = "1s")
    public void testElt1() {
        Model model = new Model();
        RealVar x = model.realVar("V", 0., 10., 1.e-4);
        IntVar y = model.intVar("I", 0, 5);
        model.element(x, new double[]{-1., .8, Math.PI, 12.}, y).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(2, model.getSolver().getSolutionCount());
    }

    @Test(groups = "1s")
    public void testElt2() {
        Model model = new Model();
        RealVar x = model.realVar("V", 0., 10., 1.e-4);
        IntVar y = model.intVar("I", 0, 5);
        model.element(x, new double[]{1., 1.0000001, 1.0000002, 1.0000003, 1.0000004}, y).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(5, model.getSolver().getSolutionCount());
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJoao2() {
        Ibex ibex = new Ibex(new double[]{1.e-1});
        ibex.add_ctr("{0}>=1.");
        Assert.assertTrue(ibex.build());
        double[] domains = new double[]{0.15, 86.0};
        ibex.contract(0, domains);
        out.printf("%s\n", Arrays.toString(domains));
    }

    @Test(groups = "ibex", timeOut = 60000)
    public void testJoao3() throws ContradictionException {
        double PRECISION = 1e-8;
        Model model = new Model();
        RealVar y = model.realVar("y", -4.0, 4.0, PRECISION);
        RealVar x = model.realVar("x", -4.0, 4.0, PRECISION);
        // y - x^2 = 0
        y.sub(x.pow(2)).eq(0).ibex(PRECISION).post(); // It didn't works
        // y - x - 1 = 0
        y.sub(x).sub(1).eq(0).ibex(PRECISION).post();
        Assert.assertNotNull(model.getSolver().findSolution());
    }

    @Test(groups = "1s")
    public void testJoao4() throws ContradictionException {
        Model model = new Model();
        //RealVar x = model.realVar("x", 0.0, 8.0, 1e-6); // With this domain, y returns [0,125 .. 100,000] (CORRECT)
        RealVar x = model.realVar("x", -100.0, 8.0, 1e-6); // With this domain, y returns [0,000 .. 100,000] (INCORRECT)
        RealVar y = model.realVar("y", -100.0, 100.0, 1e-6);
        RealVar c = model.realVar("c", 1.0, 1.0, 1e-6);
        x.ge(0.0).equation().post();
        y.eq(c.div(x)).equation().post();
        model.getSolver().propagate();
        Assert.assertTrue(y.getLB() > 0.12);
    }

    @Test(groups = "1s")
    public void testJoao5() throws ContradictionException {
        Model model = new Model();
        //RealVar x = model.realVar("x", 0.0, 8.0, 1e-6); // With this domain, y returns [0,125 .. 100,000] (CORRECT)
        RealVar x = model.realVar("x", -100.0, 8.0, 1e-6); // With this domain, y returns [0,000 .. 100,000] (INCORRECT)
        RealVar y = model.realVar("y", -100.0, 100.0, 1e-6);
        RealVar c = model.realVar("c", 1.0, 1.0, 1e-6);
        x.le(0.0).equation().post();
        y.eq(c.div(x)).equation().post();
        model.getSolver().propagate();
        Assert.assertTrue(y.getLB() < -0.12);
    }


    @Test(groups = "1s")
    public void testSchmitt(){
        Model model = new Model();
        // Variables
        RealVar t = model.realVar("t", 0.0, 4.0, 0.01);
        RealVar h = model.realVar("h", 0.0, 5.0, 0.01);
        // h = -3/4*t^2 + 6*t - 9
        h.eq(t.mul(t).mul(-0.75).add(t.mul(6)).sub(9)).equation().post();
        model.setObjective(Model.MAXIMIZE, h);
        model.getSolver().showSolutions();
        model.getSolver().solve();
        Assert.assertTrue(t.getLB()>2.);
    }

    @Test(groups = "ibex")
    public void testSchmitt2() {
        // See ISSUE #702
        Model model = new Model();
        RealVar x = model.realVar("x", -100.0, 100.0, 0.01);
        RealVar y = model.realVar("y", -100.0, 100.0, 0.01);
        y.eq(x.pow(2)).ibex(0.01).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException cex) {
            Assert.fail("Should thrown contradiction");
        }
        Assert.assertEquals(y.getLB(), 0.0, 0.01);
        Assert.assertEquals(y.getUB(), 100.0, 0.01);
        Assert.assertEquals(x.getLB(), -10.0, 0.01);
        Assert.assertEquals(x.getUB(), 10.0, 0.01);
    }

    @Test(groups = "ibex")
    public void testContractionRatio() {
        Ibex ibex = new Ibex(new double[]{1e-6, 1e-6});
        ibex.add_ctr("{0}>{1}");
        ibex.build();
        // When domain contraction is less than the contraction ratio of 1%
        double domains1[] = {0.0, 100.0, 0.5, 0.5};
        int result1 = ibex.contract(0, domains1, Ibex.TRUE, 0.01);
        Assert.assertEquals(Ibex.NOTHING, result1);
        Assert.assertEquals(domains1, new double[]{0.0, 100.0, 0.5, 0.5});
        // When domain contraction is greater than the contraction of 0.1%
        double domains2[] = {0.0, 100.0, 0.5, 0.5};
        int result2 = ibex.contract(0, domains2, Ibex.TRUE, 0.001);
        Assert.assertEquals(Ibex.CONTRACT, result2);
        Assert.assertEquals(domains2, new double[]{0.5, 100.0, 0.5, 0.5});
        ibex.release();
    }

    @Test(groups = "ibex")
    public void testIbexContractSignatures() {
        Ibex ibex = new Ibex(new double[]{1e-6, 1e-6});
        ibex.add_ctr("{0}>{1}");
        ibex.build();

        Assert.assertEquals(Ibex.NOTHING, ibex.contract(0, new double[] {0.0, 100.0, 0.5, 0.5}));
        Assert.assertEquals(Ibex.CONTRACT, ibex.contract(0, new double[] {0.0, 100.0, 1.5, 1.5}));

        Assert.assertEquals(Ibex.TRUE, ibex.contract(0, new double[] {51.0, 100.0, 50.0, 50.0}, Ibex.FALSE_OR_TRUE));
        Assert.assertEquals(Ibex.FALSE, ibex.contract(0, new double[] {0.0, 49.0, 50.0, 50.0}, Ibex.FALSE_OR_TRUE));

        Assert.assertEquals(Ibex.NOTHING, ibex.contract(0, new double[] {0.0, 100.0, 0.5, 0.5}, Ibex.RATIO));
        Assert.assertEquals(Ibex.CONTRACT, ibex.contract(0, new double[] {0.0, 100.0, 0.5, 0.5}, 0.001));

        Assert.assertEquals(Ibex.NOTHING, ibex.contract(0, new double[] {0.0, 100.0, 0.5, 0.5}, Ibex.TRUE, Ibex.RATIO));
        Assert.assertEquals(Ibex.CONTRACT, ibex.contract(0, new double[] {0.0, 100.0, 0.5, 0.5}, Ibex.TRUE, 0.001));

        Assert.assertEquals(Ibex.CONTRACT, ibex.contract(0, new double[] {0.0, 100.0, 0.5, 0.5}, Ibex.FALSE, Ibex.RATIO));
        Assert.assertEquals(Ibex.CONTRACT, ibex.contract(0, new double[] {0.0, 100.0, 0.5, 0.5}, Ibex.FALSE, 0.001));

        ibex.release();
    }

    @Test(groups = "ibex")
    public void modelIbexContractSignatures() throws ContradictionException {
        double precision = 1e-3;
        // Default ibex contraction ratio (0.01) ignores constraint
        Model model = new Model();
        RealVar x1 = model.realVar(0.5);
        RealVar y1 = model.realVar(0.0, 100.0, precision);
        y1.ge(x1).ibex(precision).post();
        model.getSolver().propagate();
        Assert.assertEquals(y1.getLB(), 0.0, precision);
        Assert.assertEquals(y1.getUB(), 100.0, precision);
        Assert.assertEquals(x1.getLB(), 0.5, precision);
        Assert.assertEquals(x1.getUB(), 0.5, precision);

        // Default ibex contraction ratio (0.001) computes constraint
        Model model2 = new Model();
        model2.getSettings().setIbexContractionRatio(0.001);
        RealVar x2 = model2.realVar(0.5);
        RealVar y2 = model2.realVar(0.0, 100.0, precision);
        y2.ge(x2).ibex(precision).post();
        model2.getSolver().propagate();
        Assert.assertEquals(y2.getLB(), 0.5, precision); // contraction computed
        Assert.assertEquals(y2.getUB(), 100.0, precision);
        Assert.assertEquals(x2.getLB(), 0.5, precision);
        Assert.assertEquals(x2.getUB(), 0.5, precision);
    }
}
