/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.set;


import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.view.set.SetBoolsView;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test suite for BoolsSetView class
 * @author Dimitri Justeau-Allaire
 * @since 08/03/2021
 */
public class SetBoolsViewTest {

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        BoolVar[] boolVars = m.boolVarArray(8);
        SetVar setView = new SetBoolsView(0, boolVars);
        Assert.assertEquals(setView.getLB().size(), 0);
        Assert.assertEquals(setView.getUB().size(), 8);
        while (m.getSolver().solve()) {
            int i = 0;
            for (BoolVar v : boolVars) {
                if (v.isInstantiatedTo(BoolVar.kTRUE)) {
                    i++;
                }
            }
            Assert.assertEquals(setView.getValue().size(), i);
            Assert.assertTrue(setView.isInstantiated());
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), Math.pow(2, 8));
    }

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateTo() {
        Model m = new Model();
        BoolVar[] boolVars = m.boolVarArray(8);
        SetVar setView = new SetBoolsView(2, boolVars);
        try {
            setView.instantiateTo(new int[] {2, 3, 4}, (ICause) setView);
            Assert.assertTrue(setView.isInstantiated());
            Assert.assertEquals(setView.getValue().size(), 3);
            Assert.assertTrue(setView.getValue().contains(2));
            Assert.assertTrue(setView.getValue().contains(3));
            Assert.assertTrue(setView.getValue().contains(4));
            for (int i = 0; i < 3; i++) {
                Assert.assertEquals(boolVars[i].getValue(), BoolVar.kTRUE);
            }
            for (int i = 3; i < 8; i++) {
                Assert.assertEquals(boolVars[i].getValue(), BoolVar.kFALSE);
            }
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        BoolVar[] boolVars = m.boolVarArray(100);
        SetVar setView = m.boolsSetView(boolVars, 0);
        IntVar card = setView.getCard();
        m.arithm(card, "<=", 4).post();
        m.member(2, setView).post();
        m.member(7, setView).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(card.getValue() <= 4);
            Assert.assertEquals(card.getValue(), setView.getValue().size());
            Assert.assertTrue(setView.getValue().contains(2));
            Assert.assertTrue(setView.getValue().contains(7));
        }
    }
}
