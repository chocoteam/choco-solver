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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class AtMost1EmptyTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        SetVar[] vars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3});
        model.post(atMost1EmptySet(vars));

        checkSolutions(model, vars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoEmptySet() {
        Model model = new Model();

        SetVar[] vars = model.setVarArray(5, new int[]{1}, new int[]{1, 2, 3, 4});
        model.post(atMost1EmptySet(vars));

        checkSolutions(model, vars);
    }


    @Test(groups = "1s", timeOut=60000)
    public void testOneEmptySet() {
        Model model = new Model();

        SetVar[] vars = new SetVar[5];
        vars[0] = model.setVar(new int[]{});
        for (int i = 0; i <= 4; i++) {
            vars[i] = model.setVar(new int[]{1}, new int[]{1, 2, 3, 4});
        }
        model.post(atMost1EmptySet(vars));

        checkSolutions(model, vars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void twoEmptySets() {
        Model model = new Model();

        SetVar[] vars = new SetVar[5];
        vars[0] = model.setVar(new int[]{});
        vars[1] = model.setVar(new int[]{});
        for (int i = 2; i <= 4; i++) {
            vars[i] = model.setVar(new int[]{1}, new int[]{1, 2, 3, 4});
        }
        model.post(atMost1EmptySet(vars));

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }



    private void checkSolutions(Model model, SetVar[] vars) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            boolean atMostOne = Arrays.stream(vars)
                    .map(SetVar::getValue)
                    .filter(ISet::isEmpty)
                    .count() <= 1;
            assertTrue(atMostOne);
        }
        assertTrue(nbSol > 0);
    }

    private Constraint atMost1EmptySet(SetVar... vars) {
        return new Constraint("AtMost1Empty", new PropAtMost1Empty(vars));
    }
}
