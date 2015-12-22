/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 15/12/2015.
 */
public class ArrayUtilsTest {

    @Test(groups = "1s")
    public void testZeroToN() throws Exception {
        int[] a = ArrayUtils.zeroToN(5);
        Assert.assertArrayEquals(a, new int[]{0,1,2,3,4});
    }

    @Test(groups = "1s", expectedExceptions = NegativeArraySizeException.class)
    public void testZeroToN2() throws Exception {
        ArrayUtils.zeroToN(-2);
    }

    @Test(groups = "1s")
    public void testOneToN() throws Exception {
        int[] a = ArrayUtils.oneToN(5);
        Assert.assertArrayEquals(a, new int[]{1,2,3,4, 5});
    }

    @Test(groups = "1s", expectedExceptions = NegativeArraySizeException.class)
    public void testOneToN2() throws Exception {
        int[] a = ArrayUtils.oneToN(-5);
    }

    @Test(groups = "1s")
    public void testLinspace() throws Exception {
        int[] a = ArrayUtils.linspace(2,7);
        Assert.assertArrayEquals(a, new int[]{2,3,4,5,6});
    }

    @Test(groups = "1s", expectedExceptions = NegativeArraySizeException.class)
    public void testLinspace2() throws Exception {
        int[] a = ArrayUtils.linspace(2,1);
    }

    @Test(groups = "1s")
    public void testGetColumn() throws Exception {
        Number[][] n = new Number[2][3];
        n[0][0] = 0.D;
        n[0][1] = 0;
        n[0][2] = 0L;
        n[1][0] = 1;
        n[1][1] = 1.D;
        n[1][2] = 1L;
        Number[] nn = ArrayUtils.getColumn(n, 1);
        Assert.assertArrayEquals(nn, new Number[]{n[0][1], n[1][1]});
    }


    @Test(groups = "1s")
    public void testGetColumn1() throws Exception {
        Solver solver = new Solver();
        IntVar[][] n = new IntVar[2][2];
        n[0][0] = VF.enumerated("X1", 0,2, solver);
        n[0][1] = VF.fixed("C1", 3, solver);
        n[1][0] = VF.fixed("C2", 4, solver);
        n[1][1] = VF.enumerated("X2", -2,0, solver);
        IntVar[] nn = ArrayUtils.getColumn(n, 1);
        Assert.assertArrayEquals(nn, new IntVar[]{n[0][1], n[1][1]});
    }

    @Test(groups = "1s")
    public void testLength() throws Exception {
        Integer[] a = {1,2,3};
        Integer[] b = {4,5};
        Integer[] c = {6};
        Assert.assertEquals(ArrayUtils.length(a,b,c), 6);
    }

    @Test(groups = "1s")
    public void testContains() throws Exception {
        Integer[] a = {1,2,3};
        Assert.assertTrue(ArrayUtils.contains(a,2));
    }

    @Test(groups = "1s")
    public void testGet() throws Exception {
        Integer[] a = {1,2,3};
        Integer[] b = {4,5};
        Integer[] c = {6};
        Assert.assertEquals(ArrayUtils.get(4, a,b,c), b[1]);
    }

    @Test(groups = "1s")
    public void testGet1() throws Exception {
        Integer[] a = {1,2,3};
        Integer[] b = {4,5};
        Integer[] c = {6};
        Assert.assertEquals(ArrayUtils.get(4, a,b,c), b[1]);
    }

    @Test(groups = "1s")
    public void testAppend() throws Exception {
        Number[] n1 = new Number[2];
        n1[0] = 0.D;
        n1[1] = 1.D;
        Number[] n2 = new Number[2];
        n2[0] = 0L;
        n2[1] = 0L;
        Assert.assertArrayEquals(ArrayUtils.append(n1, n2), new Number[]{n1[0], n1[1], n2[0], n2[1]});
    }

    @Test(groups = "1s")
    public void testAppend1() throws Exception {
        int[] n1 = new int[2];
        n1[0] = 0;
        n1[1] = 1;
        int[] n2 = new int[2];
        n2[0] = 2;
        n2[1] = 3;
        Assert.assertArrayEquals(ArrayUtils.append(n1, n2), new int[]{n1[0], n1[1], n2[0], n2[1]});
    }

    @Test(groups = "1s")
    public void testInverseSign() throws Exception {
        int[] a = {1,2,3};
        ArrayUtils.inverseSign(a);
        Assert.assertArrayEquals(a, new int[]{-1,-2,-3});
    }

    @Test(groups = "1s")
    public void testReverse() throws Exception {
        int[] a = {1,2,3};
        ArrayUtils.reverse(a);
        Assert.assertArrayEquals(a, new int[]{3,2,1});
    }

    @Test(groups = "1s")
    public void testReverse1() throws Exception {
        Number[] n1 = new Number[3];
        n1[0] = 0.D;
        n1[1] = 1.D;
        n1[2] = 2.D;
        ArrayUtils.reverse(n1);
        Assert.assertArrayEquals(n1, new Number[]{2.D,1.D,0.D});
    }

    @Test(groups = "1s")
    public void testPermutation() throws Exception {
        Number[] n1 = new Number[3];
        n1[0] = 0.D;
        n1[1] = 1.D;
        n1[2] = 2.D;
        ArrayUtils.permutation(new int[]{1,2,0}, n1);
        Assert.assertArrayEquals(n1, new Number[]{1.D,2.D,0.D});
    }

    @Test(groups = "1s")
    public void testTranspose() throws Exception {

    }

    @Test(groups = "1s")
    public void testTranspose1() throws Exception {

    }

    @Test(groups = "1s")
    public void testFlatten() throws Exception {

    }

    @Test(groups = "1s")
    public void testFlatten1() throws Exception {

    }

    @Test(groups = "1s")
    public void testFlattenSubMatrix() throws Exception {

    }

    @Test(groups = "1s")
    public void testFlatten2() throws Exception {

    }

    @Test(groups = "1s")
    public void testCreateNonRedundantSortedValues() throws Exception {

    }

    @Test(groups = "1s")
    public void testSort() throws Exception {

    }

    @Test(groups = "1s")
    public void testSwallowCopy() throws Exception {

    }

    @Test(groups = "1s")
    public void testSwallowCopy1() throws Exception {
    }

    @Test(groups = "1s")
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