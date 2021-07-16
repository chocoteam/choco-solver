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
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author Alexandre LEBRUN
 */
public class AllDifferentTest {

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();

        SetVar[] vars = model.setVarArray(5, new int[]{}, new int[]{1, 2, 3});
        model.allDifferent(vars).post();

        checkSolution(model, vars);
    }

    /**
     * A single set must be diferent from the others -> always satisfied
     */
    @Test(groups = "1s", timeOut=60000)
    public void oneElement() {
        Model model = new Model();
        SetVar[] vars = model.setVarArray(1, new int[]{}, new int[]{1, 2, 3});
        model.allDifferent(vars).post();
        assertEquals(checkSolution(model, vars), 8);
    }

    /**
     * An array of fixed already different sets
     */
    @Test(groups = "1s", timeOut=60000)
    public void alreadyDifferent() {
        Model model = new Model();
        SetVar[] vars = new SetVar[3];
        vars[0] = model.setVar(new int[]{1, 2});
        vars[1] = model.setVar(new int[]{3, 4});
        vars[2] = model.setVar(new int[]{4, 5});
        model.allDifferent(vars).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.TRUE);
        assertEquals(checkSolution(model, vars), 1);
    }


    private int checkSolution(Model model, SetVar... vars) {
        int nbSol = 0;
        while (model.getSolver().solve()) {
            nbSol++;
            for (SetVar var : vars) {
                for (SetVar innerVar : vars) {
                    if(var != innerVar) {
                        assertNotEquals(toSet(var.getValue()), toSet(innerVar.getValue()));
                    }
                }
            }
        }

        return nbSol;
    }

    private Set<Integer> toSet(ISet value) {
        Set<Integer> set = new HashSet<>();
        for (int i : value) {
            set.add(i);
        }
        return set;
    }

}
