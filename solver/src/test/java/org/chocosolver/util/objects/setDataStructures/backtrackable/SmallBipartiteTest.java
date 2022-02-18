/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.backtrackable;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * @author Jean-Guillaume FAGES
 */
public class SmallBipartiteTest extends BacktrackableSetTest {

    @Override
    public ISet create(int offset) {
        return create();
    }

    @Override
    public ISet create() {
        return SetFactory.makeStoredSet(SetType.SMALLBIPARTITESET, 0, model);
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
