/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexandre LEBRUN
 */
public class MixedScalarTest {

    private Model model;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        model = new Model();
    }


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        double[] coeffs = new double[]{1, 5, 7, 8};
        IntVar[] vars = model.intVarArray(4, new int[]{1, 3, 5});
        model.scalar(vars, coeffs, "=", 35).post();

        checkSolutions(coeffs, vars, model.intVar(35));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalBounded() {
        double[] coeffs = new double[]{1, 5, 7, 8};
        IntVar[] vars = model.intVarArray(4, 1, 6, true);
        model.scalar(vars, coeffs, "<=", 35).post();

        checkSolutions(coeffs, vars, model.intVar(35), "<=");
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalBounded2() {
        double[] coeffs = new double[]{1, 5, 7, 8};
        RealVar[] vars = model.realVarArray(4, 1., 6., .1);
        model.scalar(vars, coeffs, "=", 35).post();

        checkSolutions(coeffs, vars, 35., "=");
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalBounded3() {
        double[] coeffs = new double[]{1, 5, 7, 8};
        RealVar[] vars = model.realVarArray(4, 1.2, 1.7, .1);
        model.scalar(vars, coeffs, "<=", 35).post();

        checkSolutions(coeffs, vars, 35., "<=");
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalBoundedWithNegatives() {
        double[] coeffs = new double[]{5, 6, 7, 9};
        IntVar[] vars = model.intVarArray(4, -5, 5, true);
        model.scalar(vars, coeffs, "<=", 0).post();

        checkSolutions(coeffs, vars, model.intVar(0), "<=");
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalBoundedWithNegatives2() {
        double[] coeffs = new double[]{5, 6, 7, 9};
        RealVar[] vars = model.realVarArray(4, -1, 1, .1);
        model.scalar(vars, coeffs, "<=", 0).post();

        checkSolutions(coeffs, vars, 0., "<=");
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCoeffAtZeroSolutions() {
        double[] coeffs = new double[]{0, 4, 5};
        IntVar[] vars = model.intVarArray(3, 0, 1000);
        model.scalar(vars, coeffs, "=", 9).post();

        model.getEnvironment().worldPush();
        int nbSol = checkSolutions(coeffs, vars, model.intVar(9));
        model.getEnvironment().worldPop();

        assertEquals(nbSol, 1001);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCoeffAtZeroSolutions2() {
        double[] coeffs = new double[]{0, 4, 5};
        RealVar[] vars = model.realVarArray(3, 0, 1000,1.d);
        model.scalar(vars, coeffs, "=", 9.).post();

        model.getEnvironment().worldPush();
        int nbSol = checkSolutions(coeffs, vars, 9, "=");
        model.getEnvironment().worldPop();

        assertEquals(nbSol, 4096);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCoeffAtZeroNoSolutions() {
        double[] coeffs = new double[]{0};
        IntVar[] vars = new IntVar[]{
                model.intVar(1, 10)
        };
        model.scalar(vars, coeffs, ">=", 1).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSameVariableNoSolution() {
        IntVar ref = model.intVar(new int[]{1, 5});
        IntVar[] vars = new IntVar[]{ref, ref};
        double[] coeffs = new double[]{1, 1};
        model.scalar(vars, coeffs, "=", 6).post();

        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSameVariableSolution() {
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        double[] coeffs = new double[]{1, 3};
        model.scalar(vars, coeffs, "=", 20).post();

        checkSolutions(coeffs, vars, model.intVar(20));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSameVariableSolution2() {
        RealVar ref = model.realVar(1, 5,1);
        RealVar[] vars = new RealVar[]{ref, ref};
        double[] coeffs = new double[]{1, 3};
        model.scalar(vars, coeffs, "=", 20).post();

        checkSolutions(coeffs, vars, 20., "=");
    }


    private int checkSolutions(double[] coeffs, IntVar[] vars, IntVar sum, String operator) {
        Model model = vars[0].getModel();
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            int computed = 0;
            for (int i = 0; i < vars.length; i++) {
                computed += coeffs[i] * vars[i].getValue();
            }
            switch (operator) {
                case "=":
                    assertEquals(sum.getValue(), computed);
                    break;
                case "<=":
                    assertTrue(computed <= sum.getValue());
                    break;
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }

    private int checkSolutions(double[] coeffs, IntVar[] vars, IntVar sum) {
        return checkSolutions(coeffs, vars, sum, "=");
    }

    private int checkSolutions(double[] coeffs, RealVar[] vars, double sum, String operator) {
        Model model = vars[0].getModel();
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            double inf = 0., sup = 0.;
            for (int i = 0; i < vars.length; i++) {
                inf += coeffs[i] * vars[i].getLB();
                sup += coeffs[i] * vars[i].getUB();
            }
            switch (operator) {
                case "=":
                    assertTrue(inf <= sum, inf + "> "+sum);
                    assertTrue(sum <= sup, sum + "> "+sup);
                    break;
                case "<=":
                    assertTrue(inf <= sum, inf + "> "+sum);
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }

}
