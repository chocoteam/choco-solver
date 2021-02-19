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
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class UnionTest {


    @Test(groups = "1s", timeOut=60000)
    public void testUnionFixed() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        SetVar union = model.setVar(new int[]{1, 2, 3, 4, 5});
        model.union(setVars, union).post();

        checkSolutions(model, setVars, union);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetVarsFixed() {
        Model model = new Model();
        SetVar[] setVars = new SetVar[3];
        setVars[0] = model.setVar(new int[]{1, 2});
        setVars[1] = model.setVar(new int[]{}, new int[]{3});
        setVars[2] = model.setVar(new int[]{4, 5});
        SetVar union = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7});
        model.union(setVars, union).post();

        assertEquals(checkSolutions(model, setVars, union), 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testImpossible() {
        Model model = new Model();
        SetVar[] setVars = model.setVarArray(3, new int[]{1}, new int[]{1, 2, 3, 4});
        SetVar union = model.setVar(new int[]{2, 3, 4}); // different domains
        model.union(setVars, union).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }


    private int checkSolutions(Model model, SetVar[] setVars, SetVar union) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            ISet computed = SetFactory.makeLinkedList();
            for (SetVar setVar : setVars) {
                for (Integer value : setVar.getValue()) {
                    assertTrue(union.getValue().contains(value));
                    computed.add(value);
                }
            }
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }

}
