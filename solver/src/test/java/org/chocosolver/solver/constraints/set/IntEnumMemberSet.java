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
import org.chocosolver.util.ESat;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class IntEnumMemberSet {


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        Model model = new Model();
        SetVar set = model.setVar(new int[]{1, 2}, new int[]{1, 2, 3, 4, 5});
        IntVar member = model.intVar(1, 3, false);
        model.member(member, set).post();

        checkSolutions(model, set, member);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testWithForgivenValue() {
        Model model = new Model();
        SetVar setVar = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6});
        IntVar member = model.intVar(new int[]{5, 6, 7});
        model.member(member, setVar).post();

        checkSolutions(model, setVar, member);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testIncompatibleDomains() {
        Model model = new Model();
        SetVar set = model.setVar(new int[]{}, new int[]{1, 2, 3, 4});
        IntVar member = model.intVar(5, 7, false);
        model.member(member, set).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEmptySet() {
        Model model = new Model();
        SetVar set = model.setVar(new int[]{});
        IntVar member = model.intVar(0, 7, false);
        model.member(member, set).post();

        assertEquals(model.getSolver().isSatisfied(), ESat.FALSE);
        assertFalse(model.getSolver().solve());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testDifferentDomains() {
        Model model = new Model();
        SetVar set = model.setVar(new int[]{}, new int[]{1, 2, 3, 4, 5, 6, 7, 8});
        IntVar member = model.intVar(-100, 100, false);
        model.member(member, set).post();
        checkSolutions(model, set, member);
    }

    private int checkSolutions(Model model, SetVar set, IntVar member) {
        int nbSol = 0;
        while(model.getSolver().solve()) {
            nbSol++;
            assertTrue(set.getValue().contains(member.getValue()));
        }
        assertTrue(nbSol > 0);
        return nbSol;
    }

}
