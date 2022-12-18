/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

/**
 * @author Guillaume Le Lou�t [guillaume.lelouet@gmail.com] 2016, Jean-Guillaume Fages
 */
public class SetVarImplTest {

    static final boolean originalHardCoded = SetFactory.HARD_CODED;

    @DataProvider
    public static Object[][] maxValue() {
        return IntStream.range(0, 10).map(i -> 1 << i).mapToObj(i -> new Object[]{i}).toArray(Object[][]::new);
    }

    @AfterClass
    public void tearDown() {
        SetFactory.HARD_CODED = originalHardCoded;
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testStructures() {
        for (SetType type : SetType.values()) {
            if (!type.name().contains("FIXED") && !type.name().contains("DYNAMIC")) {
                boolean hardCodedDefault = SetFactory.HARD_CODED;
                for (boolean b : new boolean[]{true, false}) {
                    SetFactory.HARD_CODED = b;
                    Model m = new Model();
                    SetVar s1 = new SetVarImpl("s1", new int[0], type, ArrayUtils.array(0, 2), type, m);
                    SetVar s2 = new SetVarImpl("s2", new int[0], type, ArrayUtils.array(0, 2), type, m);
                    while (m.getSolver().solve()) ;
                    Assert.assertEquals(64, m.getSolver().getSolutionCount());
                }
                SetFactory.HARD_CODED = hardCodedDefault;
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSetVarInstantiated() {
        Model m = new Model();
        Assert.assertTrue(m.setVar("var", -1, -2, 3, 18).getCard().isInstantiatedTo(4));
        Assert.assertTrue(m.setVar("var", -1, -1, -1, 0).getCard().isInstantiatedTo(2));
        Assert.assertTrue(m.setVar("var").getCard().isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPropagate() {
        Model m = new Model();
        SetVar s = m.setVar("var", new int[]{}, new int[]{0, 1, 2, 3});
        IntVar a = m.intVar("a", 0), b = m.intVar("b", 0, 3), c = m.intVar("c", 0, 3), d = m.intVar("d", 0, 3);
        m.member(a, s).reifyWith(m.member(b, s).getOpposite().reify());// a in s XOR b in s
        m.member(c, s).reifyWith(m.member(d, s).getOpposite().reify());// c in s XOR d in s
        m.allDifferent(a, b, c, d).post();
        // 4 different variables, among them two belong to s so c = 2. Which of a or b belong to s makes o difference
        s.setCard(c);
        Assert.assertTrue(m.getSolver().solve());
        Assert.assertTrue(c.isInstantiatedTo(2), "" + c);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSetVarInstantiated2() {
        Model m = new Model();
        Assert.assertTrue(new SetVarImpl("var", SetFactory.makeConstantSet(-2, 0), m).getCard().isInstantiatedTo(3));
        Assert.assertTrue(new SetVarImpl("var", SetFactory.makeConstantSet(new int[]{-1, 1}), m).getCard().isInstantiatedTo(2));
        Assert.assertTrue(new SetVarImpl("var", SetFactory.makeConstantSet(new int[]{}), m).getCard().isInstantiatedTo(0));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPropagate2() {
        Model m = new Model();
        ISet lb = SetFactory.makeStoredSet(SetType.LINKED_LIST, 0, m);
        ISet ub = SetFactory.makeStoredSet(SetType.BITSET, 0, m);
        for (int i : new int[]{0, 1, 2, 3}) {
            ub.add(i);
        }
        SetVar s = new SetVarImpl("var", lb, ub, m);
        IntVar a = m.intVar("a", 0), b = m.intVar("b", 0, 3), c = m.intVar("c", 0, 3), d = m.intVar("d", 0, 3);
        m.member(a, s).reifyWith(m.member(b, s).getOpposite().reify());// a in s XOR b in s
        m.member(c, s).reifyWith(m.member(d, s).getOpposite().reify());// c in s XOR d in s
        m.allDifferent(a, b, c, d).post();
        // 4 different variables, among them two belong to s so c = 2. Which of a or b belong to s makes o difference
        s.setCard(c);
        Assert.assertTrue(m.getSolver().solve());
        Assert.assertTrue(c.isInstantiatedTo(2), "" + c);
    }

    @Test(groups = "1s")
    public void testDomSize() throws ContradictionException {
        Model m = new Model();
        SetVar s = m.setVar("var", new int[]{}, new int[]{0, 1, 2, 3});
        Assert.assertEquals(s.getDomainSize(), 16);
        s.force(0, Cause.Null);
        Assert.assertEquals(s.getDomainSize(), 8);
        s.remove(1, Cause.Null);
        Assert.assertEquals(s.getDomainSize(), 4);
        s.force(2, Cause.Null);
        Assert.assertEquals(s.getDomainSize(), 2);
        s.remove(3, Cause.Null);
        Assert.assertEquals(s.getDomainSize(), 1);
    }


    @Test(groups = "1s", dataProvider = "maxValue")
    public void testLargeDom(int max) {
        Model model = new Model("Rebalancer");
        SetVar setVar1 = model.setVar(
                "set1",
                new int[0],
                IntStream.range(0, max).toArray()
        );
        Assert.assertEquals(setVar1.getDomainSize(), max < 32 ? 1 << (max) : Integer.MAX_VALUE);
    }

}
