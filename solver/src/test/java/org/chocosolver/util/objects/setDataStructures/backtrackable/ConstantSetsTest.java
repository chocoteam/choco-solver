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

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.annotations.Test;

/**
 * @author Alexandre LEBRUN
 */
public class ConstantSetsTest {


    @Test(groups = "1s", timeOut = 60000, expectedExceptions = UnsupportedOperationException.class)
    public void testCstIntervalSet() {
        SetFactory.makeStoredSet(SetType.FIXED_INTERVAL, 0, new Model());
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = UnsupportedOperationException.class)
    public void testCstArraySet() {
        SetFactory.makeStoredSet(SetType.FIXED_ARRAY, 0, new Model());
    }


}
