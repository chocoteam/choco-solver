/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.structure;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.BitSet;

/**
 * Range operations must accept the exclusive end of a fixed-size word.
 *
 * @author Jaehong Park (105151063+PHJ2000@users.noreply.github.com)
 */
public class OneWordBitSetRangeTest {

    @DataProvider
    public Object[][] ranges() {
        return new Object[][] {{32, false}, {32, true}, {64, false}, {64, true}};
    }

    private IStateBitSet bitSet(IEnvironment environment, int width) {
        return width == 32 ? new OneWordS32BitSet(environment) : new OneWordS64BitSet(environment);
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "ranges")
    public void testAllRangesAndBacktracking(int width, boolean set) {
        IEnvironment environment = new EnvironmentTrailing();
        IStateBitSet actual = bitSet(environment, width);
        BitSet original = new BitSet(width);
        for (int bit = 0; bit < width; bit += 2) {
            actual.set(bit);
            original.set(bit);
        }
        for (int from = 0; from <= width; from++) {
            for (int to = from; to <= width; to++) {
                BitSet expected = (BitSet) original.clone();
                environment.worldPush();
                if (set) {
                    actual.set(from, to);
                    expected.set(from, to);
                } else {
                    actual.clear(from, to);
                    expected.clear(from, to);
                }
                assertContents(actual, expected, width);
                environment.worldPop();
                assertContents(actual, original, width);
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000, dataProvider = "ranges")
    public void testInvalidRangesLeaveBitsUnchanged(int width, boolean set) {
        IStateBitSet bits = bitSet(new EnvironmentTrailing(), width);
        bits.set(2);
        int[][] invalid = {{-1, 0}, {0, -1}, {0, width + 1}, {width + 1, width + 1},
                {width + 1, width}, {2, 1}, {width, width - 1}};
        for (int[] range : invalid) {
            Assert.assertThrows(IndexOutOfBoundsException.class, () -> {
                if (set) {
                    bits.set(range[0], range[1]);
                } else {
                    bits.clear(range[0], range[1]);
                }
            });
            Assert.assertEquals(bits.cardinality(), 1);
            Assert.assertTrue(bits.get(2));
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testSingleBitBoundsRemainExclusive() {
        for (int width : new int[] {32, 64}) {
            IStateBitSet bits = bitSet(new EnvironmentTrailing(), width);
            bits.set(width - 1);
            Assert.assertTrue(bits.get(width - 1));
            Assert.assertThrows(IndexOutOfBoundsException.class, () -> bits.set(width));
            Assert.assertThrows(IndexOutOfBoundsException.class, () -> bits.clear(width));
            Assert.assertThrows(IndexOutOfBoundsException.class, () -> bits.set(-1));
            Assert.assertThrows(IndexOutOfBoundsException.class, () -> bits.clear(-1));
            Assert.assertEquals(bits.cardinality(), 1);
        }
    }

    private void assertContents(IStateBitSet actual, BitSet expected, int width) {
        Assert.assertEquals(actual.cardinality(), expected.cardinality());
        for (int bit = 0; bit < width; bit++) {
            Assert.assertEquals(actual.get(bit), expected.get(bit), "bit " + bit);
        }
    }
}
