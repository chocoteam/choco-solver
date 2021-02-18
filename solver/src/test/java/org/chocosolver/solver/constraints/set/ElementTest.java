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
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class ElementTest {


    private Model model;
    private SetVar[] sets;
    private SetVar element;
    private IntVar index;

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        model = new Model();
        sets = model.setVarArray(3, new int[]{}, new int[]{1, 2, 3});
        element = model.setVar(new int[]{1, 3});
        index = model.intVar(0, 100);
        model.element(index, sets, element).post();

        checkSolutions();
    }


    @Test(groups = "1s", timeOut=60000)
    public void testFixedValues() {
        Model model = new Model();
        SetVar[] sets = new SetVar[3];
        sets[0] = model.setVar(new int[]{1, 3});
        sets[1] = model.setVar(new int[]{3, 4});
        sets[2] = model.setVar(new int[]{4, 5});
        element = model.setVar(new int[]{4, 5});
        index = model.intVar(0, sets.length - 1);
        model.element(index, sets, element).post();

        assertTrue(model.getSolver().solve());
        assertEquals(index.getValue(), 2);
        assertFalse(model.getSolver().solve());
    }


    private int checkSolutions() {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (Integer val : sets[index.getValue()].getValue()) {
                assertTrue(element.getValue().contains(val));
            }
            for (Integer val : element.getValue()) {
                assertTrue(sets[index.getValue()].getValue().contains(val));
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }
}
