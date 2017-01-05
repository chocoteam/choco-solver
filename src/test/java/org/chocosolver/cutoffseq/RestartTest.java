/**
 * This file is part of cutoffseq, https://github.com/chocoteam/cutoffseq
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.cutoffseq;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/11
 */
public class RestartTest {

    public final static int[] LUBY_2 = {1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 16};

    public final static int[] GEOMETRIC_1_1_3 = {1, 2, 2, 3, 3, 4, 5, 7, 9, 11, 14, 18, 24, 31, 40};

    public final static int[] GEOMETRIC_4_1_3 = {4, 6, 7, 9, 12, 15, 20, 26, 33, 43, 56, 72, 94, 122, 158, 205, 267, 347, 450, 585, 761, 989, 1285, 1671, 2172, 2823, 3670, 4771, 6202, 8062, 10480};
  
    @Test(timeOut=60000)
    public void testLubyRestarts() {
        testCutoffs(new LubyCutoffStrategy(1), LUBY_2, 1);
        testCutoffs(new LubyCutoffStrategy(4), LUBY_2, 4);
    }

    @Test(timeOut=60000)
    public void testGeomRestarts() {
        testCutoffs(new GeometricalCutoffStrategy(1, 1.3), GEOMETRIC_1_1_3, 1);
        testCutoffs(new GeometricalCutoffStrategy(4, 1.3), GEOMETRIC_4_1_3, 1);
    }

    private static void testCutoffs(ICutoffStrategy strat, int[] expected, int scale) {
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(scale * expected[i], strat.getNextCutoff());
        }
    }
}
