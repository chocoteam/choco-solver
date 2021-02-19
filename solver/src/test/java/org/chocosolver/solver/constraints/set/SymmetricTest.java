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
import org.chocosolver.solver.variables.SetVar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexandre LEBRUN
 */
public class SymmetricTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();


        SetVar[] vars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4});
        model.symmetric(vars).post();

    }

    @Test(groups = "1s", timeOut=60000)
    public void testHeadOnly() {
        Model model = new Model();
        SetVar[] vars = model.setVarArray(5, new int[]{}, new int[]{0, 1});
        model.symmetric(vars).post();
        int nbSol = checkSolutions(model, vars);

        // The number of results must be the same as a 2-sized array
        model = new Model();
        vars = model.setVarArray(2, new int[]{}, new int[]{0, 1});
        model.symmetric(vars).post();
        assertEquals(nbSol, checkSolutions(model, vars));
    }


    private int checkSolutions(Model model, SetVar[] vars) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (int i = 0; i < vars.length; i++) {
                for (Integer value : vars[i].getValue()) {
                    assertTrue(vars[value].getValue().contains(i));
                }
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }
}
