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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Test suite for SetDifferenceView class
 * @author Dimitri Justeau-Allaire
 * @since 08/03/2021
 */
public class SetDifferenceViewTest {

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB));
        SetVar difference = m.setDifferenceView(setA, setB);
        while (m.getSolver().solve()) {
            int[] sA = setA.getValue().toArray();
            int[] diff = IntStream.of(sA).filter(i -> !setB.getValue().contains(i)).toArray();
            Arrays.sort(diff);
            int[] diffValue = difference.getValue().toArray();
            Arrays.sort(diffValue);
            Assert.assertTrue(Arrays.equals(diff, diffValue));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedSuccess() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB));
        SetVar difference = m.setDifferenceView(setA, setB);
        m.member(0, difference).post();
        IntVar card = difference.getCard();
        m.arithm(card, "=", 2).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(difference.getValue().contains(0));
            Assert.assertEquals(card.getValue(), 2);
            int[] sA = setA.getValue().toArray();
            int[] diff = IntStream.of(sA).filter(i -> !setB.getValue().contains(i)).toArray();
            Arrays.sort(diff);
            int[] diffValue = difference.getValue().toArray();
            Arrays.sort(diffValue);
            Assert.assertTrue(Arrays.equals(diff, diffValue));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedFail() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB));
        SetVar difference = m.setDifferenceView(setA, setB);
        m.member(0, difference).post();
        m.member(0, setB).post();
        IntVar card = difference.getCard();
        m.arithm(card, "=", 2).post();
        m.getSolver().solve();
        Assert.assertEquals(m.getSolver().getSolutionCount(), 0);
    }
}
