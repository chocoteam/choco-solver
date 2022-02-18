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
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class AllDisjointTest {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3, 4, 5});
        model.allDisjoint(setVars).post();

        checkSolutions(model, setVars);
    }


    @Test(groups = "1s", timeOut=60000)
    public void testTrivialTrue() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(1, 3);
        setVars[1] = model.setVar(2);
        setVars[2] = model.setVar(4, 5);
        model.allDisjoint(setVars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        checkSolutions(model, setVars);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTrivialFalse() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(new int[]{1, 2}, new int[]{1, 2, 3, 4});
        setVars[1] = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5});
        setVars[2] = model.setVar(new int[]{2, 5}, new int[]{2, 3, 4, 5});
        model.allDisjoint(setVars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    private void checkSolutions(Model model, SetVar[] setVars) {
        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            ISet set = SetFactory.makeBipartiteSet(0);
            for (SetVar setVar : setVars) {
                for (Integer integer : setVar.getValue()) {
                    assertFalse(set.contains(integer));
                    set.add(integer);
                }
            }
        }
        assertTrue(nbSol > 0);
    }

}
