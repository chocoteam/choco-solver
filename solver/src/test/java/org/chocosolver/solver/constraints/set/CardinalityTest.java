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
 * @author Alexandre LEBRUN
 */
public class CardinalityTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        setVar.setCard(model.intVar(4));

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertEquals(setVar.getValue().size(), 4);
        }
        assertEquals(nbSol, 126); // binomial coefficient, 4 in 9
    }


    @Test(groups = "1s", timeOut=60000)
    public void testTwoVariables() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        IntVar intVar = model.intVar(0, 100);
        setVar.setCard(intVar);

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertEquals(setVar.getValue().size(), intVar.getValue());
        }
        assertTrue(nbSol > 0);
        assertEquals(nbSol, 32); // (1,5) + (2,5) + (3,5) + (4, 5) + (5,5)
    }


    @Test(groups = "1s", timeOut=60000)
    public void testEmpty() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        setVar.setCard(model.intVar(0));

        assertTrue(model.getSolver().solve());
        assertTrue(setVar.getValue().isEmpty());
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testUnfeasibleLB() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{1, 2, 3}, new int[]{1, 2, 3, 4, 5});
        IntVar intVar = model.intVar(0, 2);
        setVar.setCard(intVar);

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testUnfeasibleUB() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3});
        IntVar intVar = model.intVar(4, 10);
        setVar.setCard(intVar);

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
    }

}
