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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class BoolsIntChannelingTest {


    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testNominal(boolean bounded, Settings viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(5);
        IntVar intVar = makeVariable(model, 0, 4, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();

        checkSolutions(model, boolVars, intVar);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testTwoTrue(boolean bounded, Settings viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = new BoolVar[] {
                model.boolVar(true),
                model.boolVar(true),
                model.boolVar(),
                model.boolVar()
        };
        IntVar intVar = makeVariable(model, 0, 3, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();

        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testAllFalse(boolean bounded, Settings viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = new BoolVar[5];
        for (int i = 0; i < boolVars.length; i++) {
            boolVars[i] = model.boolVar(false);
        }
        IntVar intVar = makeVariable(model, 0, 4, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();

        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testInstantiatedIndex(boolean bounded, Settings viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(5);
        IntVar variable = makeVariable(model, 2, 2, bounded);
        model.boolsIntChanneling(boolVars, variable, 0).post();

        int nbSol = checkSolutions(model, boolVars, variable);
        assertEquals(nbSol, 1);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testOutOfBoundsOK(boolean bounded, Settings viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(3);
        IntVar intVar = makeVariable(model, 0, 3, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();
        checkSolutions(model, boolVars, intVar);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testOutOfBoundsKO(boolean bounded, Settings viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = new BoolVar[] {
                model.boolVar(false),
                model.boolVar(false),
                model.boolVar(false)
        };
        // 3 is deleted from the domain
        IntVar intVar = makeVariable(model, 0, 3, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testDifferentDomains(boolean bounded, Settings viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(5);
        IntVar var = makeVariable(model, 5, 10, bounded);
        model.boolsIntChanneling(boolVars, var, 0).post();
        // no matching between indexes and domain
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "boundsAndViews", dataProviderClass = TestData.class)
    public void testEmptyArray(boolean bounded, Settings viewPolicy) {
        Model model = new Model(viewPolicy);
        BoolVar[] boolVars = model.boolVarArray(0);
        IntVar intVar = makeVariable(model, 0, 100, bounded);
        model.boolsIntChanneling(boolVars, intVar, 0).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
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

    private int checkSolutions(Model model, BoolVar[] boolVars, IntVar intVar) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (int i = 0; i < boolVars.length; i++) {
                if(boolVars[i].getValue() == 1) {
                    assertEquals(i, intVar.getValue());
                }
            }
            assertTrue(boolVars[intVar.getValue()].getValue() == 1);
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }


}
