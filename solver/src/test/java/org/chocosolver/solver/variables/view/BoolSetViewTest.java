/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;


import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/**
 * Test suite for BoolsSetView class
 * @author Dimitri Justeau-Allaire
 * @since 08/03/2021
 */
public class BoolSetViewTest {

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        SetVar setVar = m.setVar(new int[] {}, IntStream.range(0, 8).toArray());
        BoolVar[] boolVars = m.setBoolsView(setVar, 8, 0);
        while (m.getSolver().solve()) {
            for (int i = 0; i < 8; i++) {
                if (setVar.getValue().contains(i)) {
                    Assert.assertTrue(boolVars[i].isInstantiatedTo(1));
                } else {
                    Assert.assertTrue(boolVars[i].isInstantiatedTo(0));
                }
            }
        }
        Assert.assertEquals(m.getSolver().getSolutionCount(), Math.pow(2, 8));
    }

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateTo1() {
        Model m = new Model();
        SetVar setVar = m.setVar(new int[] {}, IntStream.range(2, 10).toArray());
        BoolVar[] boolVars = m.setBoolsView(setVar, 8, 2);
        try {
            setVar.instantiateTo(new int[] {2, 3, 4}, new ICause() {});
            for (int i = 0; i < 8; i++) {
                Assert.assertTrue(boolVars[i].isInstantiated());
                if (i >= 0 && i <= 2) {
                    Assert.assertTrue(boolVars[i].isInstantiatedTo(1));
                } else {
                    Assert.assertTrue(boolVars[i].isInstantiatedTo(0));
                }
            }
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateTo2() {
        Model m = new Model();
        SetVar setVar = m.setVar(new int[] {}, IntStream.range(2, 10).toArray());
        BoolVar[] boolVars = m.setBoolsView(setVar, 8, 2);
        try {
            for (int i = 0 ; i < 7; i++) {
                boolVars[i].instantiateTo(0, new ICause() {});
            }
            boolVars[7].instantiateTo(1, new ICause() {});
            Assert.assertTrue(setVar.isInstantiated());
            Assert.assertTrue(setVar.getValue().size() == 1);
            Assert.assertTrue(setVar.getValue().contains(9));
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        SetVar setVar = m.setVar(new int[] {}, IntStream.range(0, 100).toArray());
        BoolVar[] boolVars = m.setBoolsView(setVar, 100, 0);
        IntVar card = setVar.getCard();
        m.arithm(card, "<=", 4).post();
        m.member(2, setVar).post();
        m.member(7, setVar).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(boolVars[2].isInstantiatedTo(1));
            Assert.assertTrue(boolVars[7].isInstantiatedTo(1));
            for (int i = 0; i < 100; i++) {
                if (setVar.getValue().contains(i)) {
                    Assert.assertTrue(boolVars[i].isInstantiatedTo(1));
                } else {
                    Assert.assertTrue(boolVars[i].isInstantiatedTo(0));
                }
            }
        }
    }

}
