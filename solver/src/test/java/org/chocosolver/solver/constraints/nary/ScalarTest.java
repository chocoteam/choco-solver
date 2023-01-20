/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class ScalarTest {

    private Model model;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        model = new Model();
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testNominal() {
        int[] coeffs = new int[]{1, 5, 7, 8};
        IntVar[] vars = model.intVarArray(4, new int[]{1, 3, 5});
        model.scalar(vars, coeffs, "=", 35).post();

        checkSolutions(coeffs, vars, model.intVar(35));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNominalBounded() {
        int[] coeffs = new int[]{1, 5, 7, 8};
        IntVar[] vars = model.intVarArray(4, 1, 6, true);
        model.scalar(vars, coeffs, "=", 35).post();

        checkSolutions(coeffs, vars, model.intVar(35));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNominalBoundedWithNegatives() {
        int[] coeffs = new int[]{5, 6, 7, 9};
        IntVar[] vars = model.intVarArray(4, -5, 5, true);
        model.scalar(vars, coeffs, "<=", 0).post();

        checkSolutions(coeffs, vars, model.intVar(0), "<=");
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCoeffAtZeroSolutions() {
        int[] coeffs = new int[]{0, 4, 5};
        IntVar[] vars = model.intVarArray(3, 0, 1000);
        model.scalar(vars, coeffs, "=", 9).post();

        model.getEnvironment().worldPush();
        int nbSol = checkSolutions(coeffs, vars, model.intVar(9));
        model.getEnvironment().worldPop();

        assertEquals(nbSol, 1001);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testCoeffAtZeroNoSolutions() {
        int[] coeffs = new int[]{0};
        IntVar[] vars = new IntVar[]{
                model.intVar(1, 10)
        };
        model.scalar(vars, coeffs, ">=", 1).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testWithSumVariable() {
        int[] coeffs = new int[]{1};
        IntVar[] vars = new IntVar[]{
                model.intVar(1, 100)
        };
        IntVar sum = model.intVar(1, 100);
        model.scalar(vars, coeffs, "=", sum).post();

        assertEquals(checkSolutions(coeffs, vars, sum), 100);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSameVariableNoSolution() {
        IntVar ref = model.intVar(new int[]{1, 5});
        IntVar[] vars = new IntVar[]{ref, ref};
        int[] coeffs = new int[]{1, 1};
        model.scalar(vars, coeffs, "=", 6).post();

        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSameVariableSolution() {
        IntVar ref = model.intVar(1, 5);
        IntVar[] vars = new IntVar[]{ref, ref};
        int[] coeffs = new int[]{1, 3};
        model.scalar(vars, coeffs, "=", 20).post();

        checkSolutions(coeffs, vars, model.intVar(20));
    }

    private int checkSolutions(int[] coeffs, IntVar[] vars, IntVar sum, String operator) {
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
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }

    private int checkSolutions(int[] coeffs, IntVar[] vars, IntVar sum) {
        return checkSolutions(coeffs, vars, sum, "=");
    }

    @Test(groups = "10s")
    public void testBigCoeffs1() throws ContradictionException {
        for (int i = 0; i < 70; i++) {
            int rhs = 0;
            Model cp = new Model(Settings.init().setMinCardinalityForSumDecomposition(1001));
            IntVar[] xs = cp.intVarArray(101, 0, 100);
            int[] coeffs = new int[101];
            Arrays.fill(coeffs,  IntVar.MAX_INT_BOUND);

            cp.post(cp.scalar(xs, coeffs,">=", rhs));
            cp.getSolver().setSearch(Search.randomSearch(xs, i));
            Assert.assertNotNull(cp.getSolver().findSolution());
        }
    }

    @Test(groups = "10s")
    public void testBigCoeffs2() throws ContradictionException {
        for (int i = 0; i < 70; i++) {
            int rhs = 0;
            Model cp = new Model(Settings.init().setMinCardinalityForSumDecomposition(1001));
            IntVar[] xs = cp.intVarArray(1000, 0, 100);
            int[] coeffs = new int[1000];
            Arrays.fill(coeffs,  IntVar.MAX_INT_BOUND);

            cp.post(cp.scalar(xs, coeffs,"=", rhs));
            cp.getSolver().setSearch(Search.randomSearch(xs, i));
            Assert.assertNotNull(cp.getSolver().findSolution());
        }
    }

    @Test(groups = "10s")
    public void testBigCoeffs3() throws ContradictionException {
        for (int i = 0; i < 70; i++) {
            int rhs = 0;
            Model cp = new Model(Settings.init().setMinCardinalityForSumDecomposition(1001));
            IntVar[] xs = cp.intVarArray(1000, 0, 100);
            int[] coeffs = new int[1000];
            Arrays.fill(coeffs,  IntVar.MAX_INT_BOUND);

            cp.post(cp.scalar(xs, coeffs,"!=", rhs));
            cp.getSolver().setSearch(Search.randomSearch(xs, i));
            Assert.assertNotNull(cp.getSolver().findSolution());
        }
    }

}
