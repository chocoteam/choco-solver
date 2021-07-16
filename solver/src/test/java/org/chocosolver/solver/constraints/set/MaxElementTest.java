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
public class MaxElementTest {

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        SetVar setVar = model.setVar(new int[]{1, 2}, new int[]{1, 2, 3, 4, 5, 6, 7});
        IntVar var = model.intVar(1, 20);
        model.max(setVar, var, true).post();

        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testEmptySetOK() {
        Model model = new Model();

        // empty set
        SetVar setVar = model.setVar(new int[]{});
        IntVar var = model.intVar(1, 5);
        model.max(setVar, var, false).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
        }
        assertEquals(nbSol, var.getDomainSize());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testEmptySetKO() {
        Model model = new Model();

        // empty set
        SetVar setVar = model.setVar(new int[]{});
        IntVar var = model.intVar(1, 5);
        model.max(setVar, var, true).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testImpossible() {
        Model model = new Model();

        SetVar setVar = model.setVar(new int[]{1, 2}, new int[]{1, 2, 3, 4, 5, 6, 7});
        IntVar var = model.intVar(8, 15);
        model.max(setVar, var, true).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTrivialTrue() {
        Model model = new Model();

        SetVar setVar = model.setVar(new int[]{6, 8, 7});
        IntVar var = model.intVar(8);
        model.max(setVar, var, true).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testTrivialFalse() {
        Model model = new Model();

        SetVar setVar = model.setVar(new int[]{6, 8, 7});
        IntVar var = model.intVar(7);
        model.max(setVar, var, true).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }


    private void checkSolutions(Model model, SetVar setVar, IntVar var) {
        boolean solutionFound = false;
        while(model.getSolver().solve()) {
            solutionFound = true;
            assertEquals(var.getValue(), computeMax(setVar));
        }
        assertTrue(solutionFound);
    }

    private int computeMax(SetVar setVar) {
        int max = Integer.MIN_VALUE;
        for (Integer i : setVar.getValue()) {
            max = Math.max(max, i);
        }
        return max;
    }

}
