/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 15/12/2015.
 */
public class ArrayUtilsTest {

    @Test(groups="1s", timeOut=60000)
    public void toArray() throws Exception {
        List<Integer> l = new LinkedList<>();
		l.add(1);
		l.add(5);
		Assert.assertEquals(ArrayUtils.toArray(l), new int[]{1,5});
		l = new ArrayList<>();
		l.add(1);
		l.add(5);
		Assert.assertEquals(ArrayUtils.toArray(l), new int[]{1,5});
    }

    @Test(groups="1s", timeOut=60000)
    public void testZeroToN() throws Exception {
        int[] a = ArrayUtils.array(0,4);
        Assert.assertEquals(a, new int[]{0,1,2,3,4});
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = NegativeArraySizeException.class)
    public void testZeroToN2() throws Exception {
        ArrayUtils.array(0,-2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testOneToN() throws Exception {
        int[] a = ArrayUtils.array(1,5);
        Assert.assertEquals(a, new int[]{1,2,3,4, 5});
    }

    @Test(groups="1s", timeOut=60000)
    public void testArray() throws Exception {
        int[] a = ArrayUtils.array(5,8);
        Assert.assertEquals(a, new int[]{5,6,7,8});
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = NegativeArraySizeException.class)
    public void testArray2() throws Exception {
        ArrayUtils.array(8,5);
    }

	@Test(groups="1s", timeOut=60000)
	public void testArray3() {
		int[] a = ArrayUtils.array(-8,-5);
		Assert.assertEquals(a, new int[]{-8,-7,-6,-5});
	}

    @Test(groups="1s", timeOut=60000, expectedExceptions = NegativeArraySizeException.class)
    public void testOneToN2() throws Exception {
        int[] a = ArrayUtils.array(1,-5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLinspace() throws Exception {
        int[] a = ArrayUtils.linspace(2,7);
        Assert.assertEquals(a, new int[]{2,3,4,5,6});
    }

    @Test(groups="1s", timeOut=60000, expectedExceptions = NegativeArraySizeException.class)
    public void testLinspace2() throws Exception {
        int[] a = ArrayUtils.linspace(2,1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetColumn() throws Exception {
        Number[][] n = new Number[2][3];
        n[0][0] = 0.D;
        n[0][1] = 0;
        n[0][2] = 0L;
        n[1][0] = 1;
        n[1][1] = 1.D;
        n[1][2] = 1L;
        Number[] nn = ArrayUtils.getColumn(n, 1);
        Assert.assertEquals(nn, new Number[]{n[0][1], n[1][1]});
    }


    @Test(groups="1s", timeOut=60000)
    public void testGetColumn1() throws Exception {
        Model model = new Model();
        IntVar[][] n = new IntVar[2][2];
        n[0][0] = model.intVar("X1", 0, 2, false);
        n[0][1] = model.intVar("C1", 3);
        n[1][0] = model.intVar("C2", 4);
        n[1][1] = model.intVar("X2", -2, 0, false);
        IntVar[] nn = ArrayUtils.getColumn(n, 1);
        Assert.assertEquals(nn, new IntVar[]{n[0][1], n[1][1]});
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetColumn3() throws Exception {
        int[][] n = new int[2][3];
        n[0][0] = 0;
        n[0][1] = 1;
        n[0][2] = 2;
        n[1][0] = 3;
        n[1][1] = 4;
        n[1][2] = 5;
        int[] nn = ArrayUtils.getColumn(n, 1);
        Assert.assertEquals(nn, new Number[]{n[0][1], n[1][1]});
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetColumn4() throws Exception {
        int[][] n = null;
        int[] nn = ArrayUtils.getColumn(n, 1);
        Assert.assertEquals(nn, null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetColumn5() throws Exception {
        double[][] n = new double[2][3];
        n[0][0] = 0d;
        n[0][1] = 1d;
        n[0][2] = 2d;
        n[1][0] = 3d;
        n[1][1] = 4d;
        n[1][2] = 5d;
        double[] nn = ArrayUtils.getColumn(n, 1);
        Assert.assertEquals(nn, new Number[]{n[0][1], n[1][1]});
    }

    @Test(groups="1s", timeOut=60000)
    public void testGetColumn6() throws Exception {
        double[][] n = null;
        double[] nn = ArrayUtils.getColumn(n, 1);
        Assert.assertEquals(nn, null);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLength() throws Exception {
        Integer[] a = {1,2,3};
        Integer[] b = {4,5};
        Integer[] c = {6};
        Assert.assertEquals(ArrayUtils.length(a,b,c), 6);
    }

    @Test(groups="1s", timeOut=60000)
    public void testContains() throws Exception {
        Integer[] a = {1,2,3};
        Assert.assertTrue(ArrayUtils.contains(a,2));
    }

    @Test(groups="1s", timeOut=60000)
    public void testGet() throws Exception {
        Integer[] a = {1,2,3};
        Integer[] b = {4,5};
        Integer[] c = {6};
        Assert.assertEquals(ArrayUtils.get(4, a,b,c), b[1]);
    }

    @Test(groups="1s", timeOut=60000)
    public void testGet1() throws Exception {
        List<Integer> a = new ArrayList<Integer>(){{
            add(1); add(2);add(3);
        }};
        List<Integer> b = new ArrayList<Integer>(){{
            add(4); add(5);
        }};
        List<Integer> c = new ArrayList<Integer>(){{
            add(6);
        }};
        Assert.assertEquals(ArrayUtils.get(4, a,b,c), b.get(1));
    }

    @Test(groups="1s", timeOut=60000)
    public void testAppend() throws Exception {
        Number[] n1 = new Number[2];
        n1[0] = 0.D;
        n1[1] = 1.D;
        Number[] n2 = new Number[2];
        n2[0] = 0L;
        n2[1] = 0L;
        Assert.assertEquals(ArrayUtils.append(n1, n2), new Number[]{n1[0], n1[1], n2[0], n2[1]});
    }

    @Test(groups="1s", timeOut=60000)
    public void testAppend1() throws Exception {
        int[] n1 = new int[2];
        n1[0] = 0;
        n1[1] = 1;
        int[] n2 = new int[2];
        n2[0] = 2;
        n2[1] = 3;
        Assert.assertEquals(ArrayUtils.append(n1, n2), new int[]{n1[0], n1[1], n2[0], n2[1]});
    }

    @Test(groups="1s", timeOut=60000)
    public void testAppend3() throws Exception {
        Model model = new Model();
        IntVar[] n1 = new IntVar[2];
        n1[0] = model.intVar(2);
        n1[1] = model.intVar(3);
        IntVar[] n2 = new IntVar[2];
        n2[0] = model.intVar(4);
        n2[1] = model.intVar(5);
        Assert.assertEquals(ArrayUtils.append(n1, n2), new IntVar[]{n1[0], n1[1], n2[0], n2[1]});
    }

    @Test(groups="1s", timeOut=60000)
    public void testAppend4() throws Exception {
        Model model = new Model();
        BoolVar[] n1 = new BoolVar[2];
        n1[0] = model.boolVar(false);
        n1[1] = model.boolVar(true);
        BoolVar[] n2 = new BoolVar[2];
        n2[0] = model.boolVar();
        n2[1] = model.boolVar();
        Assert.assertEquals(ArrayUtils.append(n1, n2), new BoolVar[]{n1[0], n1[1], n2[0], n2[1]});
    }

    @Test(groups="1s", timeOut=60000)
    public void testInverseSign() throws Exception {
        int[] a = {1,2,3};
        ArrayUtils.inverseSign(a);
        Assert.assertEquals(a, new int[]{-1,-2,-3});
    }

    @Test(groups="1s", timeOut=60000)
    public void testReverse() throws Exception {
        int[] a = {1,2,3};
        ArrayUtils.reverse(a);
        Assert.assertEquals(a, new int[]{3,2,1});
    }

    @Test(groups="1s", timeOut=60000)
    public void testReverse1() throws Exception {
        Number[] n1 = new Number[3];
        n1[0] = 0.D;
        n1[1] = 1.D;
        n1[2] = 2.D;
        ArrayUtils.reverse(n1);
        Assert.assertEquals(n1, new Number[]{2.D,1.D,0.D});
    }

    @Test(groups="1s", timeOut=60000)
    public void testReverse2() throws Exception {
        Number[] n1 = new Number[4];
        n1[0] = 0.D;
        n1[1] = 1.D;
        n1[2] = 2.D;
        n1[3] = 3.D;
        ArrayUtils.reverse(n1);
        Assert.assertEquals(n1, new Number[]{3.D,2.D,1.D,0.D});
    }

    @Test(groups="1s", timeOut=60000)
    public void testPermutation() throws Exception {
        Number[] n1 = new Number[3];
        n1[0] = 0.D;
        n1[1] = 1.D;
        n1[2] = 2.D;
        ArrayUtils.permutation(new int[]{1,2,0}, n1);
        Assert.assertEquals(n1, new Number[]{1.D,2.D,0.D});
    }

    @Test(groups="1s", timeOut=60000)
    public void testTranspose() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testTranspose1() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testFlatten() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testFlatten1() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testFlattenSubMatrix() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testFlatten2() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testCreateNonRedundantSortedValues() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testSort() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testSwallowCopy() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testSwallowCopy1() throws Exception {
    }

    @Test(groups="1s", timeOut=60000)
    public void testBinarySearchInc1() throws Exception {
        int[] a = {1, 3, 5, 7, 9, 11};
        Assert.assertEquals(ArrayUtils.binarySearchInc(a, 1, 5, 7, true), 3);
        Assert.assertEquals(ArrayUtils.binarySearchInc(a, 1, 5, 7, false), 3);
        Assert.assertEquals(ArrayUtils.binarySearchInc(a, 1, 5, 2, true), 1);
        Assert.assertEquals(ArrayUtils.binarySearchInc(a, 1, 5, 2, false), 0);
        Assert.assertEquals(5, ArrayUtils.binarySearchInc(a, 1, 5, 10, true));
        Assert.assertEquals(ArrayUtils.binarySearchInc(a, 1, 5, 10, false), 4);
    }

}