/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedList;

import static java.lang.System.out;
import static org.chocosolver.solver.constraints.real.Ibex.HC4;
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
            Assert.assertEquals(a1[i], a2[i], DELTA);
    }


    @Test(groups="ignored", timeOut=60000)
    public void test1() {
        for(int i=0;i<10;i++) {
            Ibex ibex = new Ibex();

            ibex.add_contractor(2, "{0}+{1}=3", Ibex.COMPO);

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

    @Test(groups="ignored", timeOut=60000)
    public void test2() {
        for(int i=0;i<10;i++) {
            Ibex ibex = new Ibex();
            ibex.add_contractor(2, "{0}^2+{1}^2<=1", Ibex.COMPO);

            double[] domains;
            double vv = Math.sqrt(2.) / 2.;

            // CASE 1: the boolean is set to TRUE
            Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.TRUE), Ibex.FAIL);
            Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.TRUE), Ibex.ENTAILED);
            domains = new double[]{-2., 1., -2., 1.};
            Assert.assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.CONTRACT);
            cmpDomains(domains, new double[]{-1., 1., -1., 1.});
            Assert.assertEquals(ibex.contract(0, domains, Ibex.TRUE), Ibex.NOTHING);


            // CASE 2: the boolean is set to FALSE
            Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.FALSE), Ibex.FAIL);
            Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.FALSE), Ibex.ENTAILED);
            Assert.assertEquals(ibex.contract(0, new double[]{-2., 1., -2., -1.}, Ibex.FALSE), Ibex.NOTHING);
            domains = new double[]{0., 2., -vv, vv};
            Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE), Ibex.CONTRACT);
            cmpDomains(domains, new double[]{vv, 2., -vv, vv});

            // CASE 3: the boolean is set to UNKNOWN
            Assert.assertEquals(ibex.contract(0, new double[]{2., 3., 2., 3.}, Ibex.FALSE_OR_TRUE), Ibex.FAIL);
            Assert.assertEquals(ibex.contract(0, new double[]{-.5, .5, -.5, .5}, Ibex.FALSE_OR_TRUE), Ibex.ENTAILED);
            Assert.assertEquals(ibex.contract(0, new double[]{-2., 1., -2., -1.}, Ibex.FALSE_OR_TRUE), Ibex.NOTHING);
            domains = new double[]{0., 2., -vv, vv};
            Assert.assertEquals(ibex.contract(0, domains, Ibex.FALSE_OR_TRUE), Ibex.NOTHING);
            cmpDomains(domains, new double[]{0., 2., -vv, vv});

            ibex.release();
        }
    }

    @Test(groups="ignored", timeOut=60000)
    public void test3() {
        for(int i=0;i<10;i++) {
            Ibex ibex = new Ibex();
            ibex.add_contractor(2, "{0}^2+{1}^2<=1", Ibex.COMPO);

            double[] domains;

            domains = new double[]{0., 1., 0., 1.};
            Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, true), Ibex.INFLATE);
            Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, true), Ibex.FULL_INFLATE);
            domains = new double[]{1., 2., 1., 2.};
            Assert.assertEquals(ibex.inflate(0, new double[]{1., 1.}, domains, true), Ibex.BAD_POINT);
            domains = new double[]{0., 1., -1., 0.};
            Assert.assertEquals(ibex.inflate(0, new double[]{1., 0.}, domains, true), Ibex.NOT_SIGNIFICANT);

            domains = new double[]{-1., 0., -1., 0.};
            Assert.assertEquals(ibex.inflate(0, new double[]{-1., -1.}, domains, false), Ibex.INFLATE);
            Assert.assertEquals(ibex.inflate(0, new double[]{-1., -1.}, domains, false), Ibex.FULL_INFLATE);
            domains = new double[]{0., .5, 0., .5};
            Assert.assertEquals(ibex.inflate(0, new double[]{0., 0.}, domains, false), Ibex.BAD_POINT);
            domains = new double[]{0., 1.01, -1., 0.};
            Assert.assertEquals(ibex.inflate(0, new double[]{1.01, 0.}, domains, false), Ibex.NOT_SIGNIFICANT);

            ibex.release();
        }
    }

    @Test(groups="ignored", timeOut=60000)
    public void test4() {
        for(int i=0;i<10;i++) {
            Model model = new Model();
            double precision = 0.00000001;
            IntVar x = model.intVar("x", 0, 9, true);
            IntVar y = model.intVar("y", 0, 9, true);
            RealVar[] vars = model.realIntViewArray(new IntVar[]{x, y}, precision);
            // Actually ,we need the calculated result like these :
            // x : [2.000000, 2.000000], y : [4.000000, 4.000000]
            // or x : [1.000000, 1.000000], y : [8.000000, 8.000000]
            // but it always like this : x : [2.418267, 2.418267], y : [3.308154, 3.308154]
//        rcons.discretize(x,y);
            model.realIbexGenericConstraint("{0} * {1} = 8", vars).post();
            model.getSolver().setSearch(new RealStrategy(vars, new Cyclic(), new RealDomainMiddle()));
            model.getSolver().solve();
            assertEquals(x.getValue(), 2);
            assertEquals(y.getValue(), 4);
            model.getIbex().release();
        }
    }

    @Test(groups="ignored", timeOut=60000)
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
            System.out.println(String.format("Solution #%d:", numSolutions));
            System.out.println("b1: " + attrEquals1Reification.getValue());
            System.out.println("b2: " + attrEquals2Reification.getValue());
            System.out.println("attr: [" + attr.getLB() + ", " + attr.getUB() + "]");
            System.out.println();

            foundSolution = model.getSolver().solve();
        }
        model.getIbex().release();
    }

    @Test(groups="ignored", timeOut=60000)
    public void testFreemajb2() {
        Model model = new Model();

        RealVar x = model.realVar("x", 0.0, 5.0, 0.001);
        out.println("Before solving:");

        RealConstraint newRange = new RealConstraint("newRange", "1.4142<{0};{0}<3.1416", HC4, x);
        newRange.post();

        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        out.printf("%s\n", model.toString());
        model.getSolver().printStatistics();
        model.getIbex().release();
    }

    @Test(groups="ignored", timeOut=60000)
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
        model.getSolver().printStatistics();
        model.getIbex().release();
    }

    @DataProvider(name="coeffs")
    public Object[][] provideCoeffs(){
        return new String[][]{
                {"{0}*0.5) = {1}"},
                {"{0}/2) = {1}"},
        };
    }

    @Test(groups="ignored", timeOut=60000, dataProvider = "coeffs")
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
        model.realIbexGenericConstraint("("+coeffs+";{0}+100={2};min({1},{2}) ={3}", weldingCurrent, MTBF_WS, MTBF_MT, global_min).post();
        model.setPrecision(precision);
        model.setObjective(false, global_min);
        model.getSolver().plugMonitor((IMonitorSolution) () -> {
            out.println("*******************");
            System.out.println("weldingCurrent LB=" + weldingCurrent.getLB() + " UB=" + weldingCurrent.getUB());
            System.out.println("MTBF_WS LB=" + MTBF_WS.getLB() + " UB=" + MTBF_WS.getUB());
            System.out.println("MTBF_MT LB=" + MTBF_MT.getLB() + " UB=" + MTBF_MT.getUB());
            System.out.println("global_min LB=" + global_min.getLB() + " UB=" + global_min.getUB());
        });
        solver.showDecisions();
        while (solver.solve()) ;
        model.getIbex().release();
    }

    @Test(groups="ignored", timeOut=600000)
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
        solver.showDecisions();
        solver.plugMonitor((IMonitorSolution) () -> {
            out.println("*******************");
            System.out.println("weldingCurrent LB=" + current.getLB() + " UB=" + current.getUB());
            System.out.println("MTBF_MT LB=" + MTBF_MT.getLB() + " UB=" + MTBF_MT.getUB());
        });
        try {
            solver.solve();
        }finally {
            model.getIbex().release();
        }
    }

    @Test(groups = "ignored")
    public void testPG1() throws Exception {
        Model model = new Model();
        RealVar rv = model.realVar(0,5,0.1d);
        BoolVar bv = model.realIbexGenericConstraint("{0}=4",rv).reify();
        model.arithm(bv,"=",0).post();
        model.getSolver().showSolutions();
        Solver solver = model.getSolver();
        solver.showDecisions();
        while(solver.solve());
        System.out.println(bv.getValue() + " " + rv.getUB());
        Assert.assertEquals(model.getSolver().getSolutionCount(), 63);
    }

    @Test(groups = "ignored")
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
        solver.showDecisions();
    /*try {
        model.getSolver().propagate();
    } catch (ContradictionException e) {
        e.printStackTrace();
    }*/
        int i = 0;
        while (solver.solve()) {
            i++;
            System.out.print("Solution " + i + " found :");
            for (Variable v : printVars) System.out.print(v + ", ");
            System.out.println("");
        }
        Assert.assertEquals(solver.getSolutionCount(), 10);
        model.getIbex().release();
    }
}
