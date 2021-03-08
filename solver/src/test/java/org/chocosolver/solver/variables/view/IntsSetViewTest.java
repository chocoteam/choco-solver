/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;


import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test suite for IntArraySetView class
 * @author Dimitri Justeau-Allaire
 * @since 08/03/2021
 */
public class IntsSetViewTest {

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        IntVar[] intVars = m.intVarArray(8, 0, 2);
        SetVar setView = new IntsSetView(1, 0, intVars);
        Assert.assertEquals(setView.getLB().size(), 0);
        Assert.assertEquals(setView.getUB().size(), 8);
        while (m.getSolver().solve()) {
            int i = 0;
            for (IntVar v : intVars) {
                if (v.isInstantiatedTo(1)) {
                    i++;
                }
            }
            Assert.assertEquals(setView.getValue().size(), i);
            Assert.assertTrue(setView.isInstantiated());
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), Math.pow(3, 8));
    }

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateTo() {
        Model m = new Model();
        IntVar[] intVars = m.intVarArray(8, 0, 2);
        SetVar setView = new IntsSetView(1, 2, intVars);
        try {
            setView.instantiateTo(new int[] {2, 3, 4}, (ICause) setView);
            Assert.assertTrue(setView.isInstantiated());
            Assert.assertEquals(setView.getValue().size(), 3);
            Assert.assertTrue(setView.getValue().contains(2));
            Assert.assertTrue(setView.getValue().contains(3));
            Assert.assertTrue(setView.getValue().contains(4));
            for (int i = 0; i < 3; i++) {
                Assert.assertTrue(intVars[i].isInstantiatedTo(1));
            }
            for (int i = 3; i < 8; i++) {
                Assert.assertFalse(intVars[i].isInstantiated());
                Assert.assertFalse(intVars[i].contains(1));
            }
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        IntVar[] intVars = m.intVarArray(8, 0, 2);
        SetVar setView = m.intsSetView(intVars, 1, 0);
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

    @Test(groups="1s", timeOut=60000)
    public void testIntArraySetArrayView() {
        Model m = new Model();
        IntVar[] intVars = m.intVarArray(8, 0, 2);
        SetVar[] setViews = m.intsSetView(intVars, 3, 0, 0);
        IntVar card = setViews[0].getCard();
        m.arithm(card, "=", 2).post();
        m.member(2, setViews[1]).post();
        m.member(7, setViews[2]).post();
        while (m.getSolver().solve()) {
            Assert.assertEquals(card.getValue(), 2);
            Assert.assertTrue(setViews[1].getValue().contains(2));
            Assert.assertTrue(setViews[2].getValue().contains(7));
            for (int i = 0; i < intVars.length; i ++) {
                int val = intVars[i].getValue();
                Assert.assertTrue(setViews[val].getValue().contains(i));
            }
        }
    }
}
