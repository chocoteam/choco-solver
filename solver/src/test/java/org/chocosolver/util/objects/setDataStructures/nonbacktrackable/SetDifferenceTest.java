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
import org.chocosolver.util.objects.setDataStructures.dynamic.SetDifference;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Tests for SetDifference class
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class SetDifferenceTest {

    @Test(groups = "1s", timeOut=60000)
    public void testSetDifference1() {
        ISet setA = SetFactory.makeConstantSet(new int[] {0, 1, 2, 3, 4, 5});
        ISet setB = SetFactory.makeConstantSet(new int[] {1, 3, 5});
        ISet difference = new SetDifference(setA, setB);
        Assert.assertEquals(difference.min(), 0);
        Assert.assertEquals(difference.max(), 4);
        Assert.assertEquals(difference.size(), 3);
        int[] vals = difference.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, new int[] {0, 2, 4}));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetDifference2() {
        Model m = new Model();
        ISet setA = SetFactory.makeStoredSet(SetType.BITSET, 0, m);
        ISet setB = SetFactory.makeStoredSet(SetType.BITSET, -2, m);
        ISet difference = new SetDifference(m, setA, setB);
        setA.add(0); setA.add(2); setA.add(4);
        setB.add(-2); setB.add(-1); setB.add(4);
        Assert.assertEquals(difference.size(), 2);
        int[] vals = difference.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, new int[] {0, 2}));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetDifference3() {
        ISet setA = SetFactory.makeBitSet(0);
        ISet setB = SetFactory.makeBitSet(-2);
        ISet setC = SetFactory.makeBipartiteSet(0);
        ISet difference = new SetDifference(setA, setB);
        ISet difference2 = new SetDifference(difference, setC);
        setA.add(0); setA.add(2); setA.add(4); setA.add(6);
        setB.add(-2); setB.add(-1); setB.add(6); setB.add(4); setB.add(0); setB.remove(0);
        setC.add(0); setC.add(7);
        int[] vals = difference2.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, new int[] {2}));
    }
}
