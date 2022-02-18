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
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Test suite for SetUnionView class
 * @author Dimitri Justeau-Allaire
 * @since 08/03/2021
 */
public class SetUnionViewTest {

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 5, 6});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar union = m.setUnionView(setA, setB, setC);
        while (m.getSolver().solve()) {
            int[] sA = setA.getValue().toArray();
            int[] sB = setB.getValue().toArray();
            int[] sC = setC.getValue().toArray();
            int[] all = ArrayUtils.concat(sA, ArrayUtils.concat(sB, sC));
            all = IntStream.of(all).distinct().toArray();
            Arrays.sort(all);
            int[] unionValue = union.getValue().toArray();
            Arrays.sort(unionValue);
            Assert.assertTrue(Arrays.equals(all, unionValue));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedSuccess() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 5, 6, 8});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar union = m.setUnionView(setA, setB, setC);
        m.member(8, union).post();
        IntVar card = union.getCard();
        m.arithm(card, "=", 3).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(union.getValue().contains(8));
            Assert.assertEquals(card.getValue(), 3);
            int[] sA = setA.getValue().toArray();
            int[] sB = setB.getValue().toArray();
            int[] sC = setC.getValue().toArray();
            int[] all = ArrayUtils.concat(sA, ArrayUtils.concat(sB, sC));
            all = IntStream.of(all).distinct().toArray();
            Arrays.sort(all);
            int[] unionValue = union.getValue().toArray();
            Arrays.sort(unionValue);
            Assert.assertTrue(Arrays.equals(all, unionValue));
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedFail() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, new int[] {0, 1, 2});
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 5, 6});
        m.getSolver().setSearch(Search.setVarSearch(setA, setB, setC));
        SetVar union = m.setUnionView(setA, setB, setC);
        m.member(8, union).post();
        m.notMember(8, setB).post();
        IntVar card = union.getCard();
        m.arithm(card, "=", 3).post();
        m.getSolver().solve();
        Assert.assertEquals(m.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void testCompareProp() {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, IntStream.range(0, 10).toArray());
        SetVar setB = m.setVar(new int[] {}, new int[] {0, 5, 8, 9, 10});
        SetVar setC = m.setVar(new int[] {}, new int[] {2, 6, 22, 5, 14});
        SetVar union = m.setUnionView(setA, setB, setC);
        m.member(8, union).post();
        IntVar card = union.getCard();
        m.arithm(card, ">=", 3).post();
        while (m.getSolver().solve()) {}
        long nbSols = m.getSolver().getSolutionCount();
        // Test the same problem with PropUnion
        m = new Model();
        setA = m.setVar(new int[] {}, IntStream.range(0, 10).toArray());
        setB = m.setVar(new int[] {}, new int[] {0, 5, 8, 9, 10});
        setC = m.setVar(new int[] {}, new int[] {2, 6, 22, 5, 14});
        union = m.setVar(new int[] {}, IntStream.range(0, 23).toArray());
        m.union(new SetVar[] {setA, setB, setC}, union).post();
        m.member(8, union).post();
        card = union.getCard();
        m.arithm(card, ">=", 3).post();
        while (m.getSolver().solve()) {}
        Assert.assertEquals(nbSols, m.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testDelta() throws ContradictionException {
        Model m = new Model();
        SetVar setA = m.setVar(new int[] {}, IntStream.range(0, 10).toArray());
        SetVar setB = m.setVar(new int[] {}, IntStream.range(0, 10).toArray());
        SetVar setC = m.setVar(new int[] {}, IntStream.range(0, 10).toArray());
        SetVar union = m.setUnionView(setA, setB, setC);
        ICause fakeCauseA = new ICause() {};
        ICause fakeCauseB = new ICause() {};
        ISetDeltaMonitor monitor = union.monitorDelta(fakeCauseA);
        monitor.startMonitoring();
        ISet delta = SetFactory.makeBitSet(0);
        IntProcedure addToDelta = i -> delta.add(i);
        // Test add elements
        setA.force(0, fakeCauseB);
        setB.force(1, fakeCauseB);
        setC.force(2, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.ADD_TO_KER);
        Assert.assertTrue(delta.contains(0));
        Assert.assertTrue(delta.contains(1));
        Assert.assertTrue(delta.contains(2));
        Assert.assertTrue(delta.size() == 3);
        delta.clear();
        monitor.forEach(addToDelta, SetEventType.ADD_TO_KER);
        Assert.assertTrue(delta.size() == 0);
        setB.force(0, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.ADD_TO_KER);
        Assert.assertTrue(delta.size() == 0);
        // Test remove elements
        delta.clear();
        setA.remove(8, fakeCauseB);
        setB.remove(8, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.REMOVE_FROM_ENVELOPE);
        Assert.assertEquals(delta.size(), 0);
        delta.clear();
        setC.remove(8, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.REMOVE_FROM_ENVELOPE);
        Assert.assertTrue(delta.contains(8));
        Assert.assertTrue(delta.size() == 1);
    }
}
