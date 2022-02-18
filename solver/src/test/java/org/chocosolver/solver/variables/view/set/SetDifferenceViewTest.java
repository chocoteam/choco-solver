/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.set;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.procedure.IntProcedure;
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

    @Test(groups="1s", timeOut=60000)
    public void testDelta() throws ContradictionException {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, IntStream.range(0, 10).toArray());
        SetVar setB = m.setVar(new int[] {}, IntStream.range(1, 10).toArray());
        SetVar difference = m.setDifferenceView(setA, setB);
        ICause fakeCauseA = new ICause() {};
        ICause fakeCauseB = new ICause() {};
        ISetDeltaMonitor monitor = difference.monitorDelta(fakeCauseA);
        monitor.startMonitoring();
        ISet delta = SetFactory.makeBitSet(0);
        IntProcedure addToDelta = i -> delta.add(i);
        // Test add elements
        setA.force(0, fakeCauseB);
        setB.remove(1, fakeCauseB);
        setA.force(1, fakeCauseB);
        setA.force(2, fakeCauseB);
        setB.remove(2, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.ADD_TO_KER);
        Assert.assertTrue(delta.contains(0));
        Assert.assertTrue(delta.contains(1));
        Assert.assertTrue(delta.contains(2));
        Assert.assertTrue(delta.size() == 3);
        // Test remove elements
        delta.clear();
        setB.force(8, fakeCauseB);
        setA.remove(7, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.REMOVE_FROM_ENVELOPE);
        Assert.assertTrue(delta.contains(8));
        Assert.assertTrue(delta.contains(7));
        Assert.assertTrue(delta.size() == 2);
    }
}
