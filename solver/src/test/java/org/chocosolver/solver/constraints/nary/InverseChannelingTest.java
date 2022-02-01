/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class InverseChannelingTest {

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testNominal(boolean bounded, Settings settings) {
        Model model = new Model(settings);
        IntVar[] intVars1 = makeArray(model, 5, 0, 4, bounded);
        IntVar[] intVars2 = makeArray(model, 5, 0, 4, bounded);
        model.inverseChanneling(intVars1, intVars2).post();

        checkSolutions(model, intVars1, intVars2);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testNoSolution(boolean bounded, Settings settings) {
        Model model = new Model(settings);
        IntVar[] intVars1 = new IntVar[] {
                makeVariable(model, 1, 1, bounded),
                makeVariable(model, 0, 1, bounded)
        };
        IntVar[] intVars2 = new IntVar[] {
                makeVariable(model, 1, 1, bounded),
                makeVariable(model, 1, 1, bounded)
        };
        model.inverseChanneling(intVars1, intVars2).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }


    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testDomainsFiltering(boolean bounded, Settings settings) {
        Model model = new Model(settings);
        IntVar[] intVars1 = makeArray(model, 2, 0, 6, bounded);
        IntVar[] intVars2 = makeArray(model, 2, 0, 6, bounded);
        model.inverseChanneling(intVars1, intVars2).post();
        assertEquals(checkSolutions(model, intVars1, intVars2), 2);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = SolverException.class,
            dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testLengthsDiffer(boolean bounded, Settings settings) {
        Model model = new Model(settings);
        IntVar[] intVars1 = makeArray(model, 3, 0, 4, bounded);
        IntVar[] intVars2 = makeArray(model, 4, 0, 4, bounded);
        model.inverseChanneling(intVars1, intVars2).post();
    }


    private int checkSolutions(Model model, IntVar[] intVars1, IntVar[] intVars2) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (int i = 0; i < intVars1.length; i++) {
                assertEquals(intVars2[intVars1[i].getValue()].getValue(), i);
            }
            for (int i = 0; i < intVars2.length; i++) {
                assertEquals(intVars1[intVars2[i].getValue()].getValue(), i);
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }


    private IntVar makeVariable(Model model, int lb, int ub, boolean bounded) {
        IntVar var = model.intVar(lb, ub, bounded);
        if(model.getSettings().enableViews()) {
            IntVar first = model.intOffsetView(var, 1);
            return model.intOffsetView(first, -1);
        } else {
            return var;
        }
    }

    private IntVar[] makeArray(Model model, int n, int lb, int ub, boolean bounded) {
        IntVar[] var = model.intVarArray(n, lb, ub, bounded);
        if(model.getSettings().enableViews()) {
            IntVar[] view1 = new IntVar[n];
            for (int i = 0; i < n; i++) {
                view1[i] = model.intOffsetView(var[i], 1);
            }
            IntVar[] view2 = new IntVar[n];
            for (int i = 0; i < n; i++) {
                view2[i] = model.intOffsetView(view1[i], -1);
            }
            return view2;
        } else {
            return var;
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test00() {
        Model model = new Model();
        IntVar[] X = model.intVarArray("X", 4, 0, 3);
        model.inverseChanneling(X, X).post();
        Solver solver = model.getSolver();
        solver.findAllSolutions();
        Assert.assertEquals(10, solver.getSolutionCount());
    }
}
