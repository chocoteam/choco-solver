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

import org.chocosolver.util.objects.setDataStructures.*;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetDynamicFilterOnSet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for SetDynamicFilter class
 * @author Dimitri Justeau-Allaire
 * @since 09/03/2021
 */
public class SetDynamicFilterTest {

    @Test(groups = "1s", timeOut=60000)
    public void testSetDynamicFilter() {
        ISet observed = SetFactory.makeConstantSet(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        ISet odd = new OddSet(observed);
        Assert.assertEquals(odd.size(), 5);
        Assert.assertTrue(odd.contains(1));
        Assert.assertTrue(odd.contains(3));
        Assert.assertTrue(odd.contains(5));
        Assert.assertTrue(odd.contains(7));
        Assert.assertTrue(odd.contains(9));
        Assert.assertEquals(odd.min(), 1);
        Assert.assertEquals(odd.max(), 9);
        Assert.assertEquals(odd.getSetType(), SetType.DYNAMIC);
        // Test iterate
        for (int i : odd) {
            Assert.assertTrue(i % 2 != 0);
        }
    }

    /**
     * Dynamic filter on the odd values of a set, for testing purposes.
     */
    private class OddSet extends SetDynamicFilterOnSet {

        public OddSet(ISet observed) {
            super(observed);
        }

        @Override
        protected boolean filter(int element) {
            return element % 2 != 0;
        }

    }
}
