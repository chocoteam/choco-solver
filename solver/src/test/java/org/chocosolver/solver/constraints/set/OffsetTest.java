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
public class OffsetTest {

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{1, 2}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar offsetted = model.setVar(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        model.offSet(setVar, offsetted, 1).post();

        checkSolutions(model, setVar, offsetted, 1);
    }

    @Test(groups = "1s", timeOut=60000) 
    public void testNominalNegative() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{2, 3}, new int[]{1, 2, 3, 4, 5, 6, 7});
        SetVar offsetted = model.setVar(new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        model.offSet(setVar, offsetted, -1).post();

        checkSolutions(model, setVar, offsetted, -1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominalInverse() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar offsetted = model.setVar(new int[]{2, 3}, new int[]{2, 3, 5});
        model.offSet(setVar, offsetted, 2).post();

        checkSolutions(model, setVar, offsetted, 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEqualityFalse() {
        Model model = new Model();
        SetVar setVar = model.setVar(1, 2, 3);
        SetVar offsetted = model.setVar(0, 2, 3);
        model.offSet(setVar, offsetted, 0).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEqualityTrue() {
        Model model = new Model();
        SetVar setVar = model.setVar(1, 2, 3);
        SetVar offsetted = model.setVar(1, 2, 3);
        model.offSet(setVar, offsetted, 0).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        checkSolutions(model, setVar, offsetted, 0);
    }

    @Test(groups = "1s", timeOut=60000)
    public void wrongLowerBound() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{2, 3, 4}, new int[]{1, 2, 3, 4, 5, 6, 7});
        SetVar offsetted = model.setVar(new int[]{}, new int[]{2, 3, 4, 5, 7, 8});
        model.offSet(setVar, offsetted, -1).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void wrongUpperBound() {
        Model model = new Model();
        SetVar setVar = model.setVar(2, 3, 4);
        SetVar offsetted = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 6, 7});
        model.offSet(setVar, offsetted, 1).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    private void checkSolutions(Model model, SetVar set, SetVar offseted, int offset) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            assertEquals(set.getValue().size(), offseted.getValue().size());
            for (Integer value : set.getValue()) {
                assertTrue(offseted.getValue().contains(value + offset));
            }
        }
    }

}
