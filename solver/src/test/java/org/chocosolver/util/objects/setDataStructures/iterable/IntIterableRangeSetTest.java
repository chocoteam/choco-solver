/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.iterable;

import org.chocosolver.util.objects.setDataStructures.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 14/01/2016.
 */
public class IntIterableRangeSetTest {

    @Test(groups = "1s", timeOut = 60000)
    public void testrangeOf1() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
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

    @Test(groups = "1s", timeOut = 60000)
    public void testrangeOf2() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 2, 4, 4, 6, 7, 9, 13, 15, 15};
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

    @Test(groups = "1s", timeOut = 60000)
    public void testAdd() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        PassiveCopySet cop = new PassiveCopySet(is);
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
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testAdd2() {
        IntIterableRangeSet sLi = new IntIterableRangeSet();
        sLi.ELEMENTS = new int[]{2, 7, 9, 10, 0, 10, 0, 0, 0, 0};
        sLi.SIZE = 4;
        sLi.CARDINALITY = 8;
        PassiveCopySet cop = new PassiveCopySet(sLi);
        sLi.clear();
        sLi.add(7);
        sLi.add(8);
        Assert.assertEquals(sLi.ELEMENTS, new int[]{7, 8, 9, 10, 0, 10, 0, 0, 0, 0});
        Assert.assertEquals(sLi.SIZE, 2);
        Assert.assertEquals(sLi.CARDINALITY, 2);
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemove() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
        is.SIZE = 6;
        is.CARDINALITY = 8;
        PassiveCopySet cop = new PassiveCopySet(is);

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
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemove2() {
        IntIterableRangeSet sLi = new IntIterableRangeSet();
        sLi.ELEMENTS = new int[]{2, 2, 4, 4, 6, 6, 8, 15, 0, 0};
        sLi.SIZE = 8;
        sLi.CARDINALITY = 11;
        PassiveCopySet cop = new PassiveCopySet(sLi);
        sLi.remove(14);
        Assert.assertEquals(sLi.ELEMENTS, new int[]{2, 2, 4, 4, 6, 6, 8, 13, 15, 15});
        Assert.assertEquals(sLi.SIZE, 10);
        Assert.assertEquals(sLi.CARDINALITY, 10);
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNextValue() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
        is.SIZE = 6;
        is.CARDINALITY = 8;
        PassiveCopySet cop = new PassiveCopySet(is);

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
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNextValue2() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 2, 4, 4, 6, 7, 9, 13, 15, 15};
        is.SIZE = 10;
        is.CARDINALITY = 11;
        PassiveCopySet cop = new PassiveCopySet(is);

        Assert.assertEquals(is.nextValue(3), 4);
        is.remove(4);
        Assert.assertEquals(is.nextValue(4), 6);
        Assert.assertTrue(cop.isEqualToObserved());

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNextValue3() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 2, 4, 4, 6, 7, 9, 13, 15, 15};
        is.SIZE = 10;
        is.CARDINALITY = 11;

        Assert.assertEquals(is.nextValue(8), 9);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNextValueOut() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
        is.SIZE = 6;
        is.CARDINALITY = 8;

        Assert.assertEquals(is.nextValueOut(-10), -9);
        Assert.assertEquals(is.nextValueOut(-1), 0);
        Assert.assertEquals(is.nextValueOut(0), 4);
        Assert.assertEquals(is.nextValueOut(1), 4);
        Assert.assertEquals(is.nextValueOut(2), 4);
        Assert.assertEquals(is.nextValueOut(3), 4);
        Assert.assertEquals(is.nextValueOut(4), 6);
        Assert.assertEquals(is.nextValueOut(5), 6);
        Assert.assertEquals(is.nextValueOut(6), 11);
        Assert.assertEquals(is.nextValueOut(7), 11);
        Assert.assertEquals(is.nextValueOut(8), 11);
        Assert.assertEquals(is.nextValueOut(9), 11);
        Assert.assertEquals(is.nextValueOut(10), 11);
        Assert.assertEquals(is.nextValueOut(15), 16);

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPrevValue() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
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

    @Test(groups = "1s", timeOut = 60000)
    public void testPrevValue2() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 2, 4, 4, 6, 7, 9, 13, 15, 15};
        is.SIZE = 10;
        is.CARDINALITY = 11;

        Assert.assertEquals(is.previousValue(14), 13);

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPrevValue3() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 2, 4, 4, 6, 7, 9, 13, 15, 15};
        is.SIZE = 10;
        is.CARDINALITY = 11;

        Assert.assertEquals(is.previousValue(6), 4);
        is.remove(4);
        Assert.assertEquals(is.previousValue(4), 2);

    }

    @Test(groups = "1s", timeOut = 60000)
    public void testPrevValueOut() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
        is.SIZE = 6;
        is.CARDINALITY = 8;

        Assert.assertEquals(is.previousValueOut(15), 14);
        Assert.assertEquals(is.previousValueOut(11), 6);
        Assert.assertEquals(is.previousValueOut(10), 6);
        Assert.assertEquals(is.previousValueOut(9), 6);
        Assert.assertEquals(is.previousValueOut(8), 6);
        Assert.assertEquals(is.previousValueOut(7), 6);
        Assert.assertEquals(is.previousValueOut(6), 4);
        Assert.assertEquals(is.previousValueOut(5), 4);
        Assert.assertEquals(is.previousValueOut(4), 0);
        Assert.assertEquals(is.previousValueOut(3), 0);
        Assert.assertEquals(is.previousValueOut(2), 0);
        Assert.assertEquals(is.previousValueOut(1), 0);
        Assert.assertEquals(is.previousValueOut(0), -1);
        Assert.assertEquals(is.previousValueOut(-1), -2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMin() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
        is.SIZE = 6;
        is.CARDINALITY = 8;
        Assert.assertEquals(is.min(), 1);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testMax() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
        is.SIZE = 6;
        is.CARDINALITY = 8;
        Assert.assertEquals(is.max(), 10);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testComplement() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 3, 5, 5, 7, 10};
        is.SIZE = 6;
        is.CARDINALITY = 8;

        IntIterableRangeSet is1;

        is1 = IntIterableSetUtils.complement(is, 0, 11);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 11);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);

        is1 = IntIterableSetUtils.complement(is, IntIterableRangeSet.MIN, IntIterableRangeSet.MAX);
        Assert.assertEquals(is1.min(), IntIterableRangeSet.MIN);
        Assert.assertEquals(is1.max(), IntIterableRangeSet.MAX);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = IntIterableSetUtils.complement(is, 1, 15);
        Assert.assertEquals(is1.min(), 4);
        Assert.assertEquals(is1.max(), 15);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = IntIterableSetUtils.complement(is, 2, 15);
        Assert.assertEquals(is1.min(), 4);
        Assert.assertEquals(is1.max(), 15);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = IntIterableSetUtils.complement(is, 3, 15);
        Assert.assertEquals(is1.min(), 4);
        Assert.assertEquals(is1.max(), 15);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = IntIterableSetUtils.complement(is, 4, 15);
        Assert.assertEquals(is1.min(), 4);
        Assert.assertEquals(is1.max(), 15);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = IntIterableSetUtils.complement(is, 5, 15);
        Assert.assertEquals(is1.min(), 6);
        Assert.assertEquals(is1.max(), 15);
        Assert.assertEquals(is1.nextValue(0), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = IntIterableSetUtils.complement(is, 6, 15);
        Assert.assertEquals(is1.min(), 6);
        Assert.assertEquals(is1.max(), 15);
        Assert.assertEquals(is1.nextValue(0), 6);
        Assert.assertEquals(is1.nextValue(6), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = IntIterableSetUtils.complement(is, 7, 15);
        Assert.assertEquals(is1.min(), 11);
        Assert.assertEquals(is1.max(), 15);
        Assert.assertEquals(is1.nextValue(0), 11);
        Assert.assertEquals(is1.nextValue(11), 12);

        is1 = IntIterableSetUtils.complement(is, 0, 10);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = IntIterableSetUtils.complement(is, 0, 9);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = IntIterableSetUtils.complement(is, 0, 8);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = IntIterableSetUtils.complement(is, 0, 7);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = IntIterableSetUtils.complement(is, 0, 6);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 6);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), 6);
        Assert.assertEquals(is1.nextValue(6), Integer.MAX_VALUE);

        is1 = IntIterableSetUtils.complement(is, 0, 5);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 4);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), Integer.MAX_VALUE);

        is1 = IntIterableSetUtils.complement(is, 0, 4);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 4);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), 4);
        Assert.assertEquals(is1.nextValue(4), Integer.MAX_VALUE);

        is1 = IntIterableSetUtils.complement(is, 0, 3);
        Assert.assertEquals(is1.min(), 0);
        Assert.assertEquals(is1.max(), 0);
        Assert.assertEquals(is1.nextValue(-1), 0);
        Assert.assertEquals(is1.nextValue(0), Integer.MAX_VALUE);

    }


    private IntIterableRangeSet makeItv(Random rnd, int g) {
        IntIterableRangeSet t1 = new IntIterableRangeSet();
        t1.SIZE = (1 + rnd.nextInt(g)) * 2;
        t1.ELEMENTS = new int[t1.SIZE];
        int k = 0;
        for (int j = 0; j < t1.SIZE; j += 2) {
            k = t1.ELEMENTS[j] = k + 2 + rnd.nextInt(g);
            k = t1.ELEMENTS[j + 1] = k + rnd.nextInt(g);
            t1.CARDINALITY += t1.ELEMENTS[j + 1] - t1.ELEMENTS[j] + 1;
        }
        return t1;
    }


    @Test(groups = "10s", timeOut = 60000)
    public void testPlus() {
        Random rnd = new Random();
        for (int i = 0; i < 200; i++) {
            rnd.setSeed(i);
            // build t1
            IntIterableRangeSet t1 = makeItv(rnd, 50);
            IntIterableRangeSet t2 = makeItv(rnd, 50);
            IntIterableRangeSet s1 = plus1(t1, t2);
            IntIterableRangeSet s2 = IntIterableSetUtils.plus(t1, t2);
            Assert.assertEquals(s2.SIZE, s1.SIZE);
            Assert.assertEquals(s2.CARDINALITY, s1.CARDINALITY);
            Assert.assertEquals(Arrays.copyOf(s2.ELEMENTS, s2.SIZE), Arrays.copyOf(s1.ELEMENTS, s1.SIZE));
        }
    }

    public static IntIterableRangeSet plus1(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t = new IntIterableRangeSet();
        for (int i = set1.min(); i <= set1.max(); i = set1.nextValue(i)) {
            for (int j = set2.min(); j <= set2.max(); j = set2.nextValue(j)) {
                t.add(i + j);
            }
        }
        return t;
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testMinus() {
        Random rnd = new Random();
        for (int i = 0; i < 200; i++) {
            rnd.setSeed(i);
            // build t1
            IntIterableRangeSet t1 = makeItv(rnd, 50);
            IntIterableRangeSet t2 = makeItv(rnd, 50);
            IntIterableRangeSet s1 = minus1(t1, t2);
            IntIterableRangeSet s2 = IntIterableSetUtils.minus(t1, t2);
            Assert.assertEquals(s2.SIZE, s1.SIZE);
            Assert.assertEquals(s2.CARDINALITY, s1.CARDINALITY);
            Assert.assertEquals(Arrays.copyOf(s2.ELEMENTS, s2.SIZE), Arrays.copyOf(s1.ELEMENTS, s1.SIZE));
        }
    }

    public static IntIterableRangeSet minus1(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t = new IntIterableRangeSet();
        for (int i = set1.min(); i <= set1.max(); i = set1.nextValue(i)) {
            for (int j = set2.min(); j <= set2.max(); j = set2.nextValue(j)) {
                t.add(i - j);
            }
        }
        return t;
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testIntersect() {
        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            rnd.setSeed(i);
            IntIterableRangeSet t1 = makeItv(rnd, 50);
            IntIterableRangeSet t2 = makeItv(rnd, 50);
            IntIterableRangeSet s1 = intersect1(t1, t2);
            IntIterableRangeSet s2 = IntIterableSetUtils.intersection(t1, t2);
            Assert.assertEquals(s2.SIZE, s1.SIZE);
            Assert.assertEquals(s2.CARDINALITY, s1.CARDINALITY);
            Assert.assertEquals(Arrays.copyOf(s2.ELEMENTS, s2.SIZE), Arrays.copyOf(s1.ELEMENTS, s1.SIZE));
        }
    }

    public static IntIterableRangeSet intersect1(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t;
        if (set1.size() < set2.size()) {
            t = set1.duplicate();
            t.retainAll(set2);
        } else {
            t = set2.duplicate();
            t.retainAll(set1);
        }
        return t;
    }


    @Test(groups = "10s", timeOut = 60000)
    public void testIntersect2() {
        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            rnd.setSeed(i);
            IntIterableRangeSet t1 = makeItv(rnd, 50);
            IntIterableRangeSet t2 = makeItv(rnd, 50);
            IntIterableRangeSet s1 = intersect1(t1, t2);
            IntIterableSetUtils.intersectionOf(t1, t2);
            Assert.assertEquals(t1.SIZE, s1.SIZE);
            Assert.assertEquals(t1.CARDINALITY, s1.CARDINALITY);
            Assert.assertEquals(Arrays.copyOf(t1.ELEMENTS, t1.SIZE), Arrays.copyOf(s1.ELEMENTS, s1.SIZE));
        }
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testIntersect3() {
        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            rnd.setSeed(i);
            IntIterableRangeSet t1 = makeItv(rnd, 50);
            IntIterableRangeSet t2 = makeItv(rnd, 50);
            IntIterableRangeSet s1 = intersect1(t1, t2);
            Assert.assertEquals(t1.intersect(t2), s1.size() > 0);
        }
    }

    @Test(groups = "10s", timeOut = 60000)
    public void testIntersect4() {
        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            rnd.setSeed(i);
            IntIterableRangeSet t1 = makeItv(rnd, 50);
            IntIterableRangeSet t2 = makeItv(rnd, 50);
            IntIterableRangeSet s1 = intersect1(t1, t2);
            Assert.assertEquals(t1.intersect(t2), s1.size() > 0);
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIntersect5() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40};
        is.SIZE = 12;
        is.CARDINALITY = 25;
        Assert.assertTrue(is.intersectDichot(0, 41));
        Assert.assertTrue(is.intersectDichot(1, 40));
        Assert.assertTrue(is.intersectDichot(2, 38));
        Assert.assertTrue(is.intersectDichot(18, 33));
        Assert.assertFalse(is.intersectDichot(21, 24));
        Assert.assertFalse(is.intersectDichot(-3, -1));
        Assert.assertFalse(is.intersectDichot(41, 43));
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testIntersect6() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 40};
        is.SIZE = 2;
        is.CARDINALITY = 39;
        Assert.assertTrue(is.intersectDichot(0, 41));
        Assert.assertTrue(is.intersectDichot(1, 40));
        Assert.assertTrue(is.intersectDichot(2, 38));
        Assert.assertTrue(is.intersectDichot(18, 33));
        Assert.assertTrue(is.intersectDichot(21, 24));
        Assert.assertFalse(is.intersectDichot(-3, -1));
        Assert.assertFalse(is.intersectDichot(41, 43));
    }


    @Test(groups = "10s", timeOut = 60000)
    public void testUnion() {
        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            rnd.setSeed(i);
            IntIterableRangeSet t1 = makeItv(rnd, 50);
            IntIterableRangeSet t2 = makeItv(rnd, 50);
            IntIterableRangeSet s1 = union1(t1, t2);
            IntIterableRangeSet s2 = IntIterableSetUtils.union(t1, t2);
            Assert.assertEquals(s2.SIZE, s1.SIZE);
            Assert.assertEquals(s2.CARDINALITY, s1.CARDINALITY);
            Assert.assertEquals(Arrays.copyOf(s2.ELEMENTS, s2.SIZE), Arrays.copyOf(s1.ELEMENTS, s1.SIZE));
        }
    }

    public static IntIterableRangeSet union1(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t;
        if (set1.size() < set2.size()) {
            t = set1.duplicate();
            t.addAll(set2);
        } else {
            t = set2.duplicate();
            t.addAll(set1);
        }
        return t;
    }


    @Test(groups = "10s", timeOut = 60000)
    public void testUnion2() {
        Random rnd = new Random();
        for (int i = 0; i < 2000; i++) {
            rnd.setSeed(i);
            IntIterableRangeSet t1 = makeItv(rnd, 50);
            IntIterableRangeSet t2 = makeItv(rnd, 50);
            IntIterableRangeSet s1 = union1(t1, t2);
            IntIterableSetUtils.unionOf(t1, t2);
            Assert.assertEquals(t1.SIZE, s1.SIZE);
            Assert.assertEquals(t1.CARDINALITY, s1.CARDINALITY);
            Assert.assertEquals(Arrays.copyOf(t1.ELEMENTS, t1.SIZE), Arrays.copyOf(s1.ELEMENTS, s1.SIZE));
        }
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testRemoveBetween1() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40};
        is.SIZE = 12;
        is.CARDINALITY = 25;
        PassiveCopySet cop = new PassiveCopySet(is);
        Assert.assertTrue(is.removeBetween(35, 40));
        Assert.assertEquals(is.CARDINALITY, 19);
        Assert.assertEquals(is.SIZE, 10);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32});
        Assert.assertTrue(is.removeBetween(1, 5));
        Assert.assertEquals(is.CARDINALITY, 14);
        Assert.assertEquals(is.SIZE, 8);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{10, 15, 20, 20, 25, 30, 32, 32});
        Assert.assertTrue(is.removeBetween(29, 30));
        Assert.assertEquals(is.CARDINALITY, 12);
        Assert.assertEquals(is.SIZE, 8);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{10, 15, 20, 20, 25, 28, 32, 32});
        Assert.assertTrue(is.removeBetween(10, 12));
        Assert.assertEquals(is.CARDINALITY, 9);
        Assert.assertEquals(is.SIZE, 8);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{13, 15, 20, 20, 25, 28, 32, 32});
        Assert.assertTrue(is.removeBetween(26, 27));
        Assert.assertEquals(is.CARDINALITY, 7);
        Assert.assertEquals(is.SIZE, 10);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{13, 15, 20, 20, 25, 25, 28, 28, 32, 32});
        Assert.assertTrue(is.removeBetween(20, 20));
        Assert.assertEquals(is.CARDINALITY, 6);
        Assert.assertEquals(is.SIZE, 8);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{13, 15, 25, 25, 28, 28, 32, 32});
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemoveBetween2() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40};
        is.SIZE = 12;
        is.CARDINALITY = 25;
        PassiveCopySet cop = new PassiveCopySet(is);
        Assert.assertTrue(is.removeBetween(34, 41));
        Assert.assertEquals(is.CARDINALITY, 19);
        Assert.assertEquals(is.SIZE, 10);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32});
        Assert.assertTrue(is.removeBetween(0, 6));
        Assert.assertEquals(is.CARDINALITY, 14);
        Assert.assertEquals(is.SIZE, 8);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{10, 15, 20, 20, 25, 30, 32, 32});
        Assert.assertTrue(is.removeBetween(29, 31));
        Assert.assertEquals(is.CARDINALITY, 12);
        Assert.assertEquals(is.SIZE, 8);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{10, 15, 20, 20, 25, 28, 32, 32});
        Assert.assertTrue(is.removeBetween(9, 12));
        Assert.assertEquals(is.CARDINALITY, 9);
        Assert.assertEquals(is.SIZE, 8);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{13, 15, 20, 20, 25, 28, 32, 32});
        Assert.assertTrue(is.removeBetween(19, 21));
        Assert.assertEquals(is.CARDINALITY, 8);
        Assert.assertEquals(is.SIZE, 6);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{13, 15, 25, 28, 32, 32});
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemoveBetween3() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40};
        is.SIZE = 12;
        is.CARDINALITY = 25;
        PassiveCopySet cop = new PassiveCopySet(is);
        Assert.assertTrue(is.removeBetween(26, 38));
        Assert.assertEquals(is.CARDINALITY, 15);
        Assert.assertEquals(is.SIZE, 10);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{1, 5, 10, 15, 20, 20, 25, 25, 39, 40});
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRemoveBetween4() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{0, 1, 4, 4, 7, 7, 11, 12, 15, 15};
        is.SIZE = 10;
        is.CARDINALITY = 7;
        PassiveCopySet cop = new PassiveCopySet(is);
        Assert.assertTrue(is.removeBetween(0, 5));
        Assert.assertEquals(is.CARDINALITY, 4);
        Assert.assertEquals(is.SIZE, 6);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{7, 7, 11, 12, 15, 15});
        Assert.assertTrue(cop.isEqualToObserved());
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testRetainBetween() {
        IntIterableRangeSet is = new IntIterableRangeSet();
        is.ELEMENTS = new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40};
        is.SIZE = 12;
        is.CARDINALITY = 25;
        PassiveCopySet cop = new PassiveCopySet(is);
        Assert.assertFalse(is.retainBetween(0, 41));
        Assert.assertEquals(is.CARDINALITY, 25);
        Assert.assertEquals(is.SIZE, 12);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40});
        Assert.assertFalse(is.retainBetween(1, 40));
        Assert.assertEquals(is.CARDINALITY, 25);
        Assert.assertEquals(is.SIZE, 12);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40});
        Assert.assertTrue(is.retainBetween(2, 38));
        Assert.assertEquals(is.CARDINALITY, 22);
        Assert.assertEquals(is.SIZE, 12);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{2, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 38});
        Assert.assertTrue(is.retainBetween(18, 33));
        Assert.assertEquals(is.CARDINALITY, 8);
        Assert.assertEquals(is.SIZE, 6);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{20, 20, 25, 30, 32, 32});
        Assert.assertTrue(is.retainBetween(21, 24));
        Assert.assertEquals(is.CARDINALITY, 0);
        Assert.assertEquals(is.SIZE, 0);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{});
        Assert.assertTrue(cop.isEqualToObserved());
        is.ELEMENTS = new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40};
        is.SIZE = 12;
        is.CARDINALITY = 25;
        cop = new PassiveCopySet(is);
        Assert.assertTrue(is.retainBetween(-3, -1));
        Assert.assertEquals(is.CARDINALITY, 0);
        Assert.assertEquals(is.SIZE, 0);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{});
        Assert.assertTrue(cop.isEqualToObserved());
        is.ELEMENTS = new int[]{1, 5, 10, 15, 20, 20, 25, 30, 32, 32, 35, 40};
        is.SIZE = 12;
        is.CARDINALITY = 25;
        cop = new PassiveCopySet(is);
        Assert.assertTrue(is.retainBetween(41, 43));
        Assert.assertEquals(is.CARDINALITY, 0);
        Assert.assertEquals(is.SIZE, 0);
        Assert.assertEquals(Arrays.copyOf(is.ELEMENTS, is.SIZE), new int[]{});
        Assert.assertTrue(cop.isEqualToObserved());
    }

    class PassiveCopySet extends AbstractSet {

        private final ISet set;
        private final ISet values;

        public PassiveCopySet(ISet set) {
            super();
            this.set = set;
            this.values = SetFactory.makeLinkedList();
            this.set.registerObserver(this, 0);
            for (int e : this.set) {
                this.values.add(e);
            }
        }

        @Override
        public void notifyElementAdded(int element, int idx) {
            this.values.add(element);
        }

        @Override
        public void notifyElementRemoved(int element, int idx) {
            this.values.remove(element);
        }

        @Override
        public void notifyCleared(int idx) {
            this.values.clear();
        }

        @Override
        public int size() {
            return values.size();
        }

        public boolean isEqualToObserved() {
            int[] obsValues = set.toArray();
            int[] thisValues = values.toArray();
            Arrays.sort(obsValues);
            Arrays.sort(thisValues);
            return Arrays.equals(obsValues, thisValues);
        }

        @Override
        public ISetIterator iterator() {
            return null;
        }

        @Override
        public ISetIterator newIterator() {
            return null;
        }

        @Override
        public boolean add(int element) {
            return false;
        }

        @Override
        public boolean remove(int element) {
            return false;
        }

        @Override
        public boolean contains(int element) {
            return false;
        }


        @Override
        public void clear() {

        }

        @Override
        public int min() {
            return 0;
        }

        @Override
        public int max() {
            return 0;
        }

        @Override
        public SetType getSetType() {
            return null;
        }
    }
}