/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class IntChannelTest {

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        IntVar[] intVars = model.intVarArray(5, 1, 5);
        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{0, 1, 2, 3, 4});

        model.setsIntsChanneling(setVars, intVars).post();
        checkSolutions(model, setVars, intVars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalMZN() {
        Model model = new Model();

        IntVar[] intVars = model.intVarArray(5, 2, 6);
        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4, 5});

        model.setsIntsChanneling(setVars, intVars, 1, 1).post();
        checkSolutions(model, setVars, intVars, 1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoChannelingPossible() {
        Model model = new Model();

        IntVar[] intVars = model.intVarArray(5, 0, 4);
        model.getEnvironment().worldPush();
        SetVar[] setVars = model.setVarArray(5, new int[]{5}, new int[]{5});

        model.setsIntsChanneling(setVars, intVars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());

        model.getEnvironment().worldPop();
        setVars = model.setVarArray(5, new int[]{}, new int[]{5});
        model.setsIntsChanneling(setVars, intVars).post();
        assertFalse(model.getSolver().solve());
    }


    private void checkSolutions(Model model, SetVar[] setVars, IntVar[] intVars) {
        checkSolutions(model, setVars, intVars, 0);
    }

    private void checkSolutions(Model model, SetVar[] setVars, IntVar[] intVars, int offset) {
        boolean solutionFound = false;
        while(model.getSolver().solve()) {
            solutionFound = true;
            for (int i = 0; i < setVars.length; i++) {
                for (Integer value : setVars[i].getValue()) {
                    assertTrue(intVars[value - offset].getValue() - offset == i);
                }
            }
            for (int i = 0; i < intVars.length; i++) {
                assertTrue(setVars[intVars[i].getValue() - offset].getValue().contains(i + offset));
            }
        }
        assertTrue(solutionFound);
    }

}
