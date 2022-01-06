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

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class NbEmptyTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        SetVar set1 = model.setVar(new int[]{1}, new int[]{1, 2, 3, 4});
        SetVar set2 = model.setVar(new int[]{}, new int[]{5, 9, 8});

        model.nbEmpty(new SetVar[]{set1, set2}, 1).post();

        boolean solutionFound = false;
        while(model.getSolver().solve()) {
            solutionFound = true;
            assertFalse(set1.getLB().isEmpty());
            assertTrue(set2.getLB().isEmpty());
        }
        assertTrue(solutionFound);
    }



    @Test(groups = "1s", timeOut=60000)
    public void testNoEmpty() {
        Model model = new Model();

        SetVar[] vars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 5});

        model.nbEmpty(vars, 2).post();
        boolean solutionFound = false;
        while(model.getSolver().solve()) {
            solutionFound = true;
            long nbEmpty = asList(vars)
                    .stream()
                    .filter(s -> s.getLB().isEmpty())
                    .count();
            assertEquals(nbEmpty, 2);
        }
        assertTrue(solutionFound);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testWithIntVar() {
        Model model = new Model();
        SetVar[] sets = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3});
        IntVar intVar = model.intVar(0, 5);
        model.nbEmpty(sets, intVar).post();

        boolean solutionFound = false;
        while(model.getSolver().solve()) {
            solutionFound = true;
            long nbEmpty = asList(sets)
                    .stream()
                    .filter(s -> s.getLB().isEmpty())
                    .count();
            assertEquals(nbEmpty, intVar.getValue());
        }
        assertTrue(solutionFound);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoEmptyUnfeasible() {
        Model model = new Model();

        SetVar[] vars = new SetVar[]{
                model.setVar(), // always empty
                model.setVar(new int[]{}, new int[]{5, 7, 5}),
                model.setVar(new int[]{1, 5}, new int[]{1, 5, 8})
        };

        model.nbEmpty(vars, 0).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEmptyUnfeasible() {
        Model model = new Model();

        SetVar[] vars = model.setVarArray(5, new int[]{5}, new int[]{5, 10, 12, 15});

        model.nbEmpty(vars, 1).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testMoreEmptyThanSetVar() {
        Model model = new Model();

        SetVar[] vars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4});

        model.nbEmpty(vars, 6).post();
        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }
}
