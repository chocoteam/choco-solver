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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Jean-Guillaume FAGES
 */
public class IntCstMemberTest {

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        IntVar var = model.intVar(10);
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.member(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        checkSolutions(model, setVar, var.getValue());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFalse() {
        Model model = new Model();

        IntVar var = model.intVar(12);
        SetVar setVar = model.setVar(new int[]{10}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.member(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrue() {
        Model model = new Model();

        int var = 10;
        SetVar setVar = model.setVar(new int[]{10}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.member(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        checkSolutions(model, setVar, var);
    }

    private int checkSolutions(Model model, SetVar set, int value) {
        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertTrue(set.getValue().contains(value));
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }
}
