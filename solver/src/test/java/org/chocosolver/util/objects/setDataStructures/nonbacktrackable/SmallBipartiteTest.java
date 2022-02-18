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

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * @author Jean-Guillaume FAGES
 */
public class SmallBipartiteTest extends SetTest {

    @Test(groups="1s", timeOut=60000)
    public void testMaxSpan() {
        ISet set = create();

        set.add(1);
        set.add(Integer.MAX_VALUE / 2);
    }

    @Override
    public ISet create(int offset) {
        return SetFactory.makeSmallBipartiteSet();
    }

    /**
     * Value which is lower than the offset
     * There is no offset for linkedlists
     */
    @Test(groups = "1s", timeOut=60000)
    public void testAddNegativeKO() {
        ISet set = create();
        assertTrue(set.add(-2));
    }
}
