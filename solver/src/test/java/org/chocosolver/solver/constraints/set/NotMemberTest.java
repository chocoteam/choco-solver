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
public class NotMemberTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        IntVar var = model.intVar(0, 5);
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 6});

        model.notMember(var, setVar).post();

        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFixedValue() {
        Model model = new Model();

        IntVar var = model.intVar(10);
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.notMember(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.UNDEFINED);
        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFixedValueWrong() {
        Model model = new Model();

        IntVar var = model.intVar(10);
        SetVar setVar = model.setVar(new int[]{10}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.notMember(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testFixedValueSure() {
        Model model = new Model();

        IntVar var = model.intVar(12);
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        model.notMember(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut=60000)
    public void trivialFalse() {
        Model model = new Model();

        IntVar var = model.intVar(1, 3);
        SetVar setVar = model.setVar(new int[]{1, 2, 3}, new int[]{1, 2, 3, 4, 5, 6});
        model.notMember(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialTrue() {
        Model model = new Model();

        IntVar var = model.intVar(1);
        // different domains
        SetVar setVar = model.setVar(new int[]{}, new int[]{4, 5, 6, 7, 8, 9});
        model.notMember(var, setVar).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        checkSolutions(model, setVar, var);
    }

    @Test(groups = "1s", timeOut=60000)
    public void emptySet() {
        Model model = new Model();

        IntVar var = model.intVar(1, 10);
        SetVar setVar = model.setVar();
        model.notMember(var, setVar).post();

       checkSolutions(model, setVar, var);
    }

    private void checkSolutions(Model model, SetVar setVar, IntVar var) {
        boolean solutionFound = false;
        while(model.getSolver().solve()) {
            solutionFound = true;
            assertFalse(setVar.getValue().contains(var.getValue()));
        }
        assertTrue(solutionFound);
    }


}
