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
import org.chocosolver.util.objects.setDataStructures.dynamic.SetUnion;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Tests for SetUnion class
 * @author Dimitri Justeau-Allaire
 * @since 30/03/2021
 */
public class SetUnionTest {

    @Test(groups = "1s", timeOut=60000)
    public void testSetUnion1() {
        ISet setA = SetFactory.makeConstantSet(new int[] {0, 2, 4, 6, 8, 10});
        ISet setB = SetFactory.makeConstantSet(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9});
        ISet setC = SetFactory.makeConstantSet(new int[] {11, 12, 13});
        ISet union = new SetUnion(setA, setB, setC);
        Assert.assertEquals(union.min(), 0);
        Assert.assertEquals(union.max(), 13);
        Assert.assertEquals(union.size(), 14);
        int[] vals = union.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, IntStream.range(0, 14).toArray()));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetUnion2() {
        Model m = new Model();
        ISet setA = SetFactory.makeStoredSet(SetType.BITSET, 0, m);
        ISet setB = SetFactory.makeStoredSet(SetType.BITSET, -2, m);
        ISet setC = SetFactory.makeStoredSet(SetType.BITSET, 1, m);
        ISet union = new SetUnion(m, setA, setB, setC);
        setA.add(0);
        setA.add(2);
        setA.add(4);
        setB.add(-2);
        setB.add(-1);
        setB.add(4);
        setC.add(1);
        setC.add(6);
        setC.add(20);
        Assert.assertEquals(union.min(), -2);
        Assert.assertEquals(union.max(), 20);
        Assert.assertEquals(union.size(), 8);
        int[] vals = union.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, new int[] {-2, -1, 0, 1, 2, 4, 6, 20}));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSetUnion3() {
        ISet setA = SetFactory.makeBitSet(0);
        ISet setB = SetFactory.makeBitSet(-2);
        ISet setC = SetFactory.makeBitSet(0);
        ISet setD = SetFactory.makeBipartiteSet(0);
        ISet union = new SetUnion(setA, setB, setC);
        ISet union2 = new SetUnion(union, setD);
        setA.add(0);
        setA.add(2);
        setA.add(4);
        setB.add(-2);
        setB.add(-1);
        setB.add(4);
        setC.add(1);
        setC.add(6);
        setC.add(20);
        setC.remove(6);
        setD.add(8);
        setD.add(7);
        setB.remove(-2);
        int[] vals = union2.toArray();
        Arrays.sort(vals);
        Assert.assertTrue(Arrays.equals(vals, new int[] {
                -1, 0, 1, 2, 4, 7, 8, 20
        }));
    }
}
