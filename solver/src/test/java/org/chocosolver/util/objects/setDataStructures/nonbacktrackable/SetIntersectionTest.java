/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.nonbacktrackable;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetIntersection;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Tests for SetIntersection class
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class SetIntersectionTest {

    @Test(groups = "1s", timeOut=60000)
    public void testSetIntersection1() {
        ISet setA = SetFactory.makeConstantSet(new int[] {0, 2, 4, 6, 8, 10});
        ISet setB = SetFactory.makeConstantSet(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9});
        ISet setC = SetFactory.makeConstantSet(new int[] {2, 3, 4, 6, 8});
        ISet intersection = new SetIntersection(setA, setB, setC);
        Assert.assertEquals(intersection.min(), 2);
        Assert.assertEquals(intersection.max(), 8);
        Assert.assertEquals(intersection.size(), 4);
        int[] vals = intersection.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, new int[] {2, 4, 6, 8}));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetIntersection2() {
        Model m = new Model();
        ISet setA = SetFactory.makeStoredSet(SetType.BITSET, 0, m);
        ISet setB = SetFactory.makeStoredSet(SetType.BITSET, -2, m);
        ISet setC = SetFactory.makeStoredSet(SetType.BITSET, 1, m);
        ISet intersection = new SetIntersection(m, setA, setB, setC);
        setA.add(0); setA.add(2); setA.add(4);
        setB.add(-2); setB.add(-1); setB.add(4);
        setC.add(1); setC.add(6); setC.add(20);
        Assert.assertEquals(intersection.size(), 0);
        setC.add(4);
        int[] vals = intersection.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, new int[] {4}));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetIntersection3() {
        ISet setA = SetFactory.makeBitSet(0);
        ISet setB = SetFactory.makeBitSet(-2);
        ISet setC = SetFactory.makeBitSet(0);
        ISet setD = SetFactory.makeBipartiteSet(0);
        ISet intersection = new SetIntersection(setA, setB, setC);
        ISet intersection2 = new SetIntersection(intersection, setD);
        setA.add(0); setA.add(2); setA.add(4); setA.add(6);
        setB.add(-2); setB.add(-1); setB.add(6); setB.add(4); setB.add(0); setB.remove(0);
        setC.add(0); setC.add(1); setC.add(6); setC.add(4);
        setD.add(6); setD.add(7);
        int[] vals = intersection2.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, new int[] {6}));
    }
}
