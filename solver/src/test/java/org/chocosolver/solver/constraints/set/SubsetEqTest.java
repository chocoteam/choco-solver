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
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class SubsetEqTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar[] vars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4});

        model.subsetEq(vars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        checkSolution(model, vars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testOneToFill() {
        Model model = new Model();
        SetVar[] vars = new SetVar[3];
        vars[0] = model.setVar(0);
        vars[1] = model.setVar(0, 1);
        vars[2] = model.setVar(new int[]{}, new int[]{0, 1, 2});
        model.subsetEq(vars).post();

        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertTrue(vars[2].getValue().contains(0));
            assertTrue(vars[2].getValue().contains(1));
        }
        assertEquals(nbSol, 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoSolution() {
        Model model = new Model();
        SetVar[] vars = new SetVar[3];
        vars[0] = model.setVar(0);
        vars[1] = model.setVar(0, 1);
        vars[2] = model.setVar(new int[]{}, new int[]{0, 2});
        model.subsetEq(vars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoSolutionBadData() {
        Model model = new Model();
        SetVar[] vars = new SetVar[3];
        vars[0] = model.setVar(0);
        vars[1] = model.setVar(1);
        vars[2] = model.setVar(new int[]{}, new int[]{0, 1, 2});

        model.subsetEq(vars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }




    private void checkSolution(Model model, SetVar[] vars) {
        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            for (int i = 0; i < vars.length - 1; i++) {
                for (Integer value : vars[i].getValue()) {
                    assertTrue(vars[i + 1].getValue().contains(value));
                }
            }
        }
        assertTrue(nbSol > 0);
    }

}
