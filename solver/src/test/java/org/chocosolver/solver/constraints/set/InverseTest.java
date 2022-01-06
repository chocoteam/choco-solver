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
import org.chocosolver.solver.variables.SetVar;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class InverseTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4});
        SetVar[] inverseSetVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4});
        model.inverseSet(setVars, inverseSetVars, 0, 0).post();

        boolean solutionFound = false;
        while(model.getSolver().solve()) {
            solutionFound = true;
            checkSolution(setVars, inverseSetVars);
        }
        assertTrue(solutionFound);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEnumerateLength1() {
        Model model = new Model();

        SetVar[] setVars = model.setVarArray(1, new int[]{}, new int[]{0, 1});
        SetVar[] inverseSetVars = model.setVarArray(1, new int[]{}, new int[]{0, 1});
        model.inverseSet(setVars, inverseSetVars, 0, 0).post();

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            checkSolution(setVars, inverseSetVars);
        }
        assertEquals(nbSol, 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEnumerateLength2() {
        Model model = new Model();

        SetVar[] setVars = model.setVarArray(2, new int[]{}, new int[]{0, 1});
        SetVar[] inverseSetVars = model.setVarArray(2, new int[]{}, new int[]{0, 1});
        model.inverseSet(setVars, inverseSetVars, 0, 0).post();

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            checkSolution(setVars, inverseSetVars);
        }
        assertEquals(nbSol, 16);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBuildInverse() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[2];
        setVars[0] = model.setVar(0, 1);
        setVars[1] = model.setVar(0);
        SetVar[] inverseSetVars = model.setVarArray(2, new int[]{}, new int[]{0, 1});

        model.inverseSet(setVars, inverseSetVars, 0, 0).post();

        assertTrue(model.getSolver().solve());

        assertEquals(inverseSetVars[0].getValue().toArray(), new int[]{0, 1});
        assertEquals(inverseSetVars[1].getValue().toArray(), new int[]{0});

        assertFalse(model.getSolver().solve());
    }

    private void checkSolution(SetVar[] setVars, SetVar[] inverseSetVars) {
        for (int i = 0; i < setVars.length; i++) {
            for (Integer val : setVars[i].getValue()) {
                assertTrue(inverseSetVars[val].getValue().contains(i));
            }
        }
        for (int i = 0; i < inverseSetVars.length; i++) {
            for (Integer val : inverseSetVars[i].getValue()) {
                assertTrue(setVars[val].getValue().contains(i));
            }
        }
    }
}
