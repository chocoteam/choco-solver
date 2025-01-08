/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.integer;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.BitSet;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 06/10/2023
 */
public class IntAffineViewTest {

    @DataProvider(name = "configurations")
    public Object[][] configurations() {
        return new Object[][]{
                {1, 0, 1, 2, 3},
                {1, 0, -3, -2, -1},
                {-1, 0, 1, 2, 3},
                {-1, 0, -3, -2, -1},
                {2, 3, 1, 4, 7},
                {2, -3, 1, 4, 7},
                {-2, 3, 1, 4, 7},
                {-2, -3, 1, 4, 7},
                {2, 3, 1, 4, 7},
                {2, -3, -3, 0, 3},
                {-2, 3, -3, 0, 3},
                {-2, -3, -3, 0, 3},
                {2, 3, -7, -4, -1},
                {2, -3, -7, -4, -1},
                {-2, 3, -7, -4, -1},
                {-2, -3, -7, -4, -1},
                {5, 7, 7, 8, 9},
                {-5, 7, 7, 8, 9},
                {5, -7, 7, 8, 9},
                {-5, -7, 7, 8, 9},
                {5, 7, -9, -8, -7},
                {-5, 7, -9, -8, -7},
                {5, -7, -9, -8, -7},
                {-5, -7, -9, -8, -7},
        };
    }

    @Test(groups = "1s", dataProvider = "configurations")
    public void testNextValue(int a, int b, int... domain) {
        Model model = new Model();
        IntVar x = model.intVar("x", domain);
        IntVar y = new IntAffineView<>(x, a, b);

        int[] values = Arrays.stream(domain).map(i -> (a * i) + b).sorted().toArray();

        Assert.assertEquals(y.nextValue(values[0] - 3), values[0]);
        Assert.assertEquals(y.nextValue(values[0] - 1), values[0]);
        Assert.assertEquals(y.nextValue(values[0]), values[1]);
        Assert.assertEquals(y.nextValue(values[1] - 1), values[1]);
        Assert.assertEquals(y.nextValue(values[1]), values[2]);
        Assert.assertEquals(y.nextValue(values[2] - 1), values[2]);
        Assert.assertEquals(y.nextValue(values[2]), Integer.MAX_VALUE);
    }

    @Test(groups = "1s", dataProvider = "configurations")
    public void testPreviousValue(int a, int b, int... domain) {
        Model model = new Model();
        IntVar x = model.intVar("x", domain);
        IntVar y = new IntAffineView<>(x, a, b);

        int[] values = Arrays.stream(domain).map(i -> a * i + b).sorted().toArray();

        Assert.assertEquals(y.previousValue(values[2] + 3), values[2]);
        Assert.assertEquals(y.previousValue(values[2] + 1), values[2]);
        Assert.assertEquals(y.previousValue(values[2]), values[1]);
        Assert.assertEquals(y.previousValue(values[1] + 1), values[1]);
        Assert.assertEquals(y.previousValue(values[1]), values[0]);
        Assert.assertEquals(y.previousValue(values[0] + 1), values[0]);
        Assert.assertEquals(y.previousValue(values[0]), Integer.MIN_VALUE);
    }

    @Test(groups = "1s", dataProvider = "configurations")
    public void testNextValueOut(int a, int b, int... domain) {
        Model model = new Model();
        IntVar x = model.intVar("x", domain);
        IntVar y = new IntAffineView<>(x, a, b);

        int[] values = Arrays.stream(domain).map(i -> (a * i) + b).sorted().toArray();
        BitSet valuesOut = new BitSet();
        int offset = values[0] - 1;
        for (int i = 0; i < values.length; i++) {
            valuesOut.set(values[i] - offset);
        }
        Assert.assertEquals(y.nextValueOut(values[0] - 2), values[0] - 1);
        Assert.assertEquals(y.nextValueOut(values[0] - 1), valuesOut.nextClearBit(values[0] - offset) + offset);
        Assert.assertEquals(y.nextValueOut(values[0]), valuesOut.nextClearBit(values[0] - offset) + offset);
        Assert.assertEquals(y.nextValueOut(values[1] - 1), valuesOut.nextClearBit(values[1] - offset) + offset);
        Assert.assertEquals(y.nextValueOut(values[1]), valuesOut.nextClearBit(values[1] - offset) + offset);
        Assert.assertEquals(y.nextValueOut(values[2] - 1), valuesOut.nextClearBit(values[2] - offset) + offset);
        Assert.assertEquals(y.nextValueOut(values[2]), values[2] + 1);
    }

    @Test(groups = "1s", dataProvider = "configurations")
    public void testPreviousValueOut(int a, int b, int... domain) {
        Model model = new Model();
        IntVar x = model.intVar("x", domain);
        IntVar y = new IntAffineView<>(x, a, b);

        int[] values = Arrays.stream(domain).map(i -> (a * i) + b).sorted().toArray();
        BitSet valuesOut = new BitSet();
        int offset = values[0] - 1;
        for (int i = 0; i < values.length; i++) {
            valuesOut.set(values[i] - offset);
        }
        //todo 
        Assert.assertEquals(y.previousValueOut(values[2] + 2), values[2] + 1);
        Assert.assertEquals(y.previousValueOut(values[2] + 1), valuesOut.previousClearBit(values[2] - offset) + offset);
        Assert.assertEquals(y.previousValueOut(values[2]), valuesOut.previousClearBit(values[2] - offset) + offset);
        Assert.assertEquals(y.previousValueOut(values[1] + 1), valuesOut.previousClearBit(values[1] - offset) + offset);
        Assert.assertEquals(y.previousValueOut(values[1]), valuesOut.previousClearBit(values[1] - offset) + offset);
        Assert.assertEquals(y.previousValueOut(values[0] + 1), valuesOut.previousClearBit(values[0] - offset) + offset);
        Assert.assertEquals(y.previousValueOut(values[0]), values[0] - 1);
    }

    @Test(groups = "1s", dataProvider = "configurations")
    public void testValueIterator(int a, int b, int... domain) {
        Model model = new Model();
        IntVar x = model.intVar("x", domain);
        IntVar y = new IntAffineView<>(x, a, b);

        int[] values = Arrays.stream(domain).map(i -> a * i + b).sorted().toArray();

        DisposableValueIterator it = y.getValueIterator(true);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(it.next(), values[0]);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(it.next(), values[1]);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(it.next(), values[2]);
        Assert.assertFalse(it.hasNext());
        it.dispose();
        it = y.getValueIterator(false);
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals(it.previous(), values[2]);
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals(it.previous(), values[1]);
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals(it.previous(), values[0]);
        Assert.assertFalse(it.hasPrevious());
    }

    @Test(groups = "1s", dataProvider = "configurations")
    public void testRangeIterator(int a, int b, int... domain) {
        Model model = new Model();
        IntVar x = model.intVar("x", domain);
        IntVar y = new IntAffineView<>(x, a, b);

        int[] values = Arrays.stream(domain).map(i -> a * i + b).sorted().toArray();

        DisposableRangeIterator it = y.getRangeIterator(true);
        int v = y.getLB();
        while (it.hasNext()) {
            Assert.assertEquals(it.min(), v);
            v = y.nextValueOut(v);
            Assert.assertEquals(it.max(), v - 1);
            v = y.nextValue(v);
            it.next();
        }
        it.dispose();
        it = y.getRangeIterator(false);
        v = y.getUB();
        while (it.hasPrevious()) {
            Assert.assertEquals(it.max(), v);
            v = y.previousValueOut(v);
            Assert.assertEquals(it.min(), v + 1);
            v = y.previousValue(v);
            it.previous();
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test1() {
        Model m = new Model();
        IntVar X = m.intVar("X", 1, 3, false);
        IntVar Y = m.mul(X, 2);
        IntVar[] vars = {X, Y};
        m.arithm(Y, "!=", 4).post();
        m.getSolver().setSearch(inputOrderLBSearch(vars));
        while (m.getSolver().solve()) ;
        assertEquals(m.getSolver().getSolutionCount(), 2);
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testNegIntAffineView() {
        Model m = new Model();
        IntVar x = m.intVar("x", 1, 3, false);
        IntVar y = m.neg(m.offset(x, 5));
        assertEquals(-8, y.getLB());
        assertEquals(-6, y.getUB());
    }
}