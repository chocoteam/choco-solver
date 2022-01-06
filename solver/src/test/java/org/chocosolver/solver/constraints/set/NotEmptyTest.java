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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Alexandre LEBRUN
 */
public class NotEmptyTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        // Set which could be empty
        SetVar var = model.setVar(new int[]{}, new int[]{1, 2, 3});
        model.post(notEmpty(var));

        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertFalse(var.getValue().isEmpty());
        }
        assertEquals(nbSol, 7);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialTrue() {
        Model model = new Model();

        // Set which can't be empty
        SetVar var = model.setVar(new int[]{5}, new int[]{5, 6, 7, 8});
        model.post(notEmpty(var));

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertFalse(var.getValue().isEmpty());
        }
        assertEquals(8, nbSol);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialFalse() {
        Model model = new Model();

        // Set which must be empty
        SetVar var = model.setVar(new int[]{}, new int[]{});
        model.post(notEmpty(var));

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups="10s", timeOut=60000)
    public void testComparedToNbEmpty() {
        Model model = new Model();
        int[] ub = new int[20];
        for (int i = 0; i < 20; i++) {
            ub[i] = i;
        }
        SetVar var = model.setVar(new int[]{}, ub);
        model.post(notEmpty(var));

        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        int nbSol1 = 0;
        while(model.getSolver().solve()) {
            assertFalse(var.getValue().isEmpty());
            nbSol1++;
        }
        System.out.println("step 1");

        model = new Model();
        var = model.setVar(new int[]{}, ub);
        model.nbEmpty(new SetVar[]{var}, 0).post();
        int nbSol2 = 0;
        while(model.getSolver().solve()) {
            assertFalse(var.getValue().isEmpty());
            nbSol2++;
        }
        System.out.println("step 2");

        assertEquals(nbSol1, nbSol2);
    }


    private Constraint notEmpty(SetVar var) {
        return new Constraint("NotEmpty", new PropNotEmpty(var));
    }

}
