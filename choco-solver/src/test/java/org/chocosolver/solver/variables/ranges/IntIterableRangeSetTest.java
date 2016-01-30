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
package org.chocosolver.solver.variables.ranges;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 14/01/2016.
 */
public class IntIterableRangeSetTest {

    @Test(groups = "1s", timeOut=1000)
    public void testrangeOf1(){
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,3,5,5,7,10};
        is.SIZE = 6;
        is.CARDINALITY = 8;

        // check bounds
        Assert.assertEquals(is.rangeOf(1), 1);
        Assert.assertEquals(is.rangeOf(3), 1);
        Assert.assertEquals(is.rangeOf(5), 2);
        Assert.assertEquals(is.rangeOf(7), 3);
        Assert.assertEquals(is.rangeOf(10), 3);

        // check values inside ranges
        Assert.assertEquals(is.rangeOf(2), 1);
        Assert.assertEquals(is.rangeOf(8), 3);
        Assert.assertEquals(is.rangeOf(9), 3);


        // check values outside ranges
        Assert.assertEquals(is.rangeOf(-1), -1);
        Assert.assertEquals(is.rangeOf(0), -1);
        Assert.assertEquals(is.rangeOf(4), -2);
        Assert.assertEquals(is.rangeOf(6), -3);
        Assert.assertEquals(is.rangeOf(11), -4);
        Assert.assertEquals(is.rangeOf(12), -4);
    }

    @Test(groups = "1s", timeOut=1000)
    public void testrangeOf2(){
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,2,4,4,6,7,9,13,15,15};
        is.SIZE = 10;
        is.CARDINALITY = 11;

        // check bounds
        Assert.assertEquals(is.rangeOf(1), 1);
        Assert.assertEquals(is.rangeOf(2), 1);
        Assert.assertEquals(is.rangeOf(4), 2);
        Assert.assertEquals(is.rangeOf(6), 3);
        Assert.assertEquals(is.rangeOf(7), 3);
        Assert.assertEquals(is.rangeOf(9), 4);
        Assert.assertEquals(is.rangeOf(13), 4);
        Assert.assertEquals(is.rangeOf(15), 5);

        // check values inside ranges
        Assert.assertEquals(is.rangeOf(10), 4);
        Assert.assertEquals(is.rangeOf(11), 4);
        Assert.assertEquals(is.rangeOf(12), 4);


        // check values outside ranges
        Assert.assertEquals(is.rangeOf(0), -1);
        Assert.assertEquals(is.rangeOf(3), -2);
        Assert.assertEquals(is.rangeOf(5), -3);
        Assert.assertEquals(is.rangeOf(8), -4);
        Assert.assertEquals(is.rangeOf(16), -6);
    }

    @Test(groups = "1s", timeOut=1000)
    public void testAdd() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        Assert.assertTrue(is.add(2));
        Assert.assertFalse(is.add(2));
        Assert.assertTrue(is.add(1));
        Assert.assertFalse(is.add(1));
        Assert.assertTrue(is.add(3));
        Assert.assertFalse(is.add(3));
        Assert.assertTrue(is.add(5));
        Assert.assertFalse(is.add(5));
        Assert.assertTrue(is.add(10));
        Assert.assertFalse(is.add(10));
        Assert.assertTrue(is.add(7));
        Assert.assertFalse(is.add(7));
        Assert.assertTrue(is.add(8));
        Assert.assertFalse(is.add(8));
        Assert.assertTrue(is.add(9));
        Assert.assertFalse(is.add(9));
        Assert.assertEquals(is.SIZE, 6);
        Assert.assertEquals(is.CARDINALITY, 8);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{1, 3, 5, 5, 7, 10});
        is.add(4);
        is.add(6);
        Assert.assertEquals(is.SIZE, 2);
        Assert.assertEquals(is.CARDINALITY, 10);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{1, 10});
    }

    @Test(groups = "1s", timeOut=1000)
    public void testAdd2(){
        IntIterableRangeSet sLi = new IntIterableRangeSet();
        sLi.ELEMENTS = new int[]{2,7,9,10,0,10,0,0,0,0};
        sLi.SIZE = 4;
        sLi.CARDINALITY = 8;
        sLi.clear();
        sLi.add(7);
        sLi.add(8);
        Assert.assertEquals(sLi.ELEMENTS,new int[]{7,8,9,10,0,10,0,0,0,0});
        Assert.assertEquals(sLi.SIZE, 2);
        Assert.assertEquals(sLi.CARDINALITY, 2);
    }

    @Test(groups = "1s", timeOut=1000)
    public void testRemove() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,3,5,5,7,10};
        is.SIZE = 6;
        is.CARDINALITY = 8;

        Assert.assertTrue(is.remove(1));
        Assert.assertFalse(is.remove(1));
        Assert.assertTrue(is.remove(3));
        Assert.assertFalse(is.remove(3));
        Assert.assertTrue(is.remove(2));
        Assert.assertFalse(is.remove(2));
        Assert.assertTrue(is.remove(5));
        Assert.assertFalse(is.remove(5));
        Assert.assertTrue(is.remove(8));
        Assert.assertFalse(is.remove(8));
        Assert.assertTrue(is.remove(9));
        Assert.assertFalse(is.remove(9));
        Assert.assertTrue(is.remove(7));
        Assert.assertFalse(is.remove(7));
        Assert.assertTrue(is.remove(10));
        Assert.assertFalse(is.remove(10));
        Assert.assertEquals(is.SIZE, 0);
        Assert.assertEquals(is.CARDINALITY, 0);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{});
    }

    @Test(groups = "1s", timeOut=1000)
    public void testRemove2(){
        IntIterableRangeSet sLi = new IntIterableRangeSet();
        sLi.ELEMENTS = new int[]{2,2,4,4,6,6,8,15,0,0};
        sLi.SIZE = 8;
        sLi.CARDINALITY = 11;
        sLi.remove(14);
        Assert.assertEquals(sLi.ELEMENTS,new int[]{2,2,4,4,6,6,8,13,15,15});
        Assert.assertEquals(sLi.SIZE, 10);
        Assert.assertEquals(sLi.CARDINALITY, 10);
    }

    @Test(groups = "1s", timeOut=1000)
    public void testNextValue() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,3,5,5,7,10};
        is.SIZE = 6;
        is.CARDINALITY = 8;

        Assert.assertEquals(is.nextValue(-10), 1);
        Assert.assertEquals(is.nextValue(0), 1);
        Assert.assertEquals(is.nextValue(1), 2);
        Assert.assertEquals(is.nextValue(2), 3);
        Assert.assertEquals(is.nextValue(3), 5);
        Assert.assertEquals(is.nextValue(4), 5);
        Assert.assertEquals(is.nextValue(5), 7);
        Assert.assertEquals(is.nextValue(6), 7);
        Assert.assertEquals(is.nextValue(7), 8);
        Assert.assertEquals(is.nextValue(8), 9);
        Assert.assertEquals(is.nextValue(9), 10);
        Assert.assertEquals(is.nextValue(10), Integer.MAX_VALUE);
        Assert.assertEquals(is.nextValue(15), Integer.MAX_VALUE);

    }

    @Test(groups = "1s", timeOut=1000)
    public void testNextValue2() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,2,4,4,6,7,9,13,15,15};
        is.SIZE = 10;
        is.CARDINALITY = 11;

        Assert.assertEquals(is.nextValue(3), 4);
        is.remove(4);
        Assert.assertEquals(is.nextValue(4), 6);

    }

    @Test(groups = "1s", timeOut=1000)
    public void testNextValue3() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,2,4,4,6,7,9,13,15,15};
        is.SIZE = 10;
        is.CARDINALITY = 11;

        Assert.assertEquals(is.nextValue(8), 9);
    }

    @Test(groups = "1s", timeOut=1000)
    public void testPrevValue() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,3,5,5,7,10};
        is.SIZE = 6;
        is.CARDINALITY = 8;

        Assert.assertEquals(is.previousValue(15), 10);
        Assert.assertEquals(is.previousValue(11), 10);
        Assert.assertEquals(is.previousValue(10), 9);
        Assert.assertEquals(is.previousValue(9), 8);
        Assert.assertEquals(is.previousValue(8), 7);
        Assert.assertEquals(is.previousValue(7), 5);
        Assert.assertEquals(is.previousValue(6), 5);
        Assert.assertEquals(is.previousValue(5), 3);
        Assert.assertEquals(is.previousValue(4), 3);
        Assert.assertEquals(is.previousValue(3), 2);
        Assert.assertEquals(is.previousValue(2), 1);
        Assert.assertEquals(is.previousValue(1), Integer.MIN_VALUE);
        Assert.assertEquals(is.previousValue(0), Integer.MIN_VALUE);
        Assert.assertEquals(is.previousValue(-1), Integer.MIN_VALUE);

    }

    @Test(groups = "1s", timeOut=1000)
    public void testPrevValue2() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,2,4,4,6,7,9,13,15,15};
        is.SIZE = 10;
        is.CARDINALITY = 11;

        Assert.assertEquals(is.previousValue(14), 13);

    }

    @Test(groups = "1s", timeOut=1000)
    public void testPrevValue3() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,2,4,4,6,7,9,13,15,15};
        is.SIZE = 10;
        is.CARDINALITY = 11;

        Assert.assertEquals(is.previousValue(6), 4);
        is.remove(4);
        Assert.assertEquals(is.previousValue(4), 2);

    }

    @Test(groups = "1s", timeOut=1000)
    public void testFirst() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,3,5,5,7,10};
        is.SIZE = 6;
        is.CARDINALITY = 8;
        Assert.assertEquals(is.first(), 1);
    }

    @Test(groups = "1s", timeOut=1000)
    public void testLast() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,3,5,5,7,10};
        is.SIZE = 6;
        is.CARDINALITY = 8;
        Assert.assertEquals(is.last(), 10);
    }

    @Test(groups = "1s", timeOut=1000)
    public void testComplement(){
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1,3,5,5,7,10};
        is.SIZE = 6;
        is.CARDINALITY = 8;

        IntIterableRangeSet is1;

        is1 = is.complement(0, 11);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 11);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);

        is1 = is.complement(Integer.MIN_VALUE, Integer.MAX_VALUE);
        Assert.assertEquals(is1.first(), Integer.MIN_VALUE);
        Assert.assertEquals(is1.last(), Integer.MAX_VALUE);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = is.complement(1, 15);
        Assert.assertEquals(is1.first(), 4);
        Assert.assertEquals(is1.last(), 15);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = is.complement(2, 15);
        Assert.assertEquals(is1.first(), 4);
        Assert.assertEquals(is1.last(), 15);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = is.complement(3, 15);
        Assert.assertEquals(is1.first(), 4);
        Assert.assertEquals(is1.last(), 15);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = is.complement(4, 15);
        Assert.assertEquals(is1.first(), 4);
        Assert.assertEquals(is1.last(), 15);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = is.complement(5, 15);
        Assert.assertEquals(is1.first(), 6);
        Assert.assertEquals(is1.last(), 15);
        Assert.assertEquals(is1.nextValue(0), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = is.complement(6, 15);
        Assert.assertEquals(is1.first(), 6);
        Assert.assertEquals(is1.last(), 15);
        Assert.assertEquals(is1.nextValue(0), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = is.complement(7, 15);
        Assert.assertEquals(is1.first(), 11);
        Assert.assertEquals(is1.last(), 15);
        Assert.assertEquals(is1.nextValue(0), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = is.complement(0, 10);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = is.complement(0, 9);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = is.complement(0, 8);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = is.complement(0, 7);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = is.complement(0, 6);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = is.complement(0, 5);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 4);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), Integer.MAX_VALUE);

        is1 = is.complement(0, 4);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 4);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), Integer.MAX_VALUE);

        is1 = is.complement(0, 3);
        Assert.assertEquals(is1.first(), 0);
        Assert.assertEquals(is1.last(), 0);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), Integer.MAX_VALUE);

    }
}