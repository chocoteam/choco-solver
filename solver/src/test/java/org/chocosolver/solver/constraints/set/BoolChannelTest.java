/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class BoolChannelTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        BoolVar[] boolVars = model.boolVarArray(5);
        SetVar setVar = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4});
        model.setBoolsChanneling(boolVars, setVar).post();

        checkSolutions(model, setVar, boolVars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testBooleansFixed() {
        Model model = new Model();
        BoolVar[] boolVars = new BoolVar[] {
            model.boolVar(false),
            model.boolVar(true),
            model.boolVar(false),
            model.boolVar(true),
            model.boolVar(false)
        };
        SetVar setVar = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        model.setBoolsChanneling(boolVars, setVar).post();

        checkSolutions(model, setVar, boolVars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetFixed() {
        Model model = new Model();
        BoolVar[] boolVars = model.boolVarArray(5);
        // the booleans of index {1, 3, 4} must be set to 1, the others to 0
        SetVar setVar = model.setVar(1, 3, 4);
        model.setBoolsChanneling(boolVars, setVar).post();

        checkSolutions(model, setVar, boolVars);
    }

    /**
     * Case of a minizinc model, with index starting at 1 instead of 0
     */
    @Test(groups = "1s", timeOut=60000)
    public void testNominalMZN() {
        Model model = new Model();

        BoolVar[] boolVars = model.boolVarArray(5);
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        model.setBoolsChanneling(boolVars, setVar, 1).post();

        checkSolutions(model, setVar, boolVars, 1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoChannelingPossible() {
        Model model = new Model();

        BoolVar[] boolVars = model.boolVarArray(5);
        SetVar setVar = model.setVar(new int[]{5}, new int[]{5});

        model.setBoolsChanneling(boolVars, setVar).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    private void checkSolutions(Model model, SetVar setVar, BoolVar[] boolVars) {
        checkSolutions(model, setVar, boolVars, 0);
    }

    private void checkSolutions(Model model, SetVar setVar, BoolVar[] boolVars, int offset) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (Integer value : setVar.getValue()) {
                assertEquals(boolVars[value - offset].getBooleanValue(), ESat.TRUE);
            }
            for (int i = 0; i < boolVars.length; i++) {
                assertEquals(boolVars[i].getBooleanValue() == ESat.TRUE, setVar.getValue().contains(i + offset));
            }
        }
        assertTrue(nbSol > 0);
    }

}
