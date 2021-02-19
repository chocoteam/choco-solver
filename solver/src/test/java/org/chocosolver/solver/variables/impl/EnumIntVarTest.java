/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexandre LEBRUN
 */
public class EnumIntVarTest extends IntVarTest {


    @BeforeMethod(alwaysRun = true)
    @Override
    public void setup() {
        Model model = new Model();
        this.var = model.intVar(1, 4, false);
    }


    /******************************************************
     * Specific tests related to the enumerated domain
     *****************************************************/

    //------------------------------------
    //------- Remove interval ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveIntervalInner() throws ContradictionException {
        var.removeInterval(2, 3, Cause.Null);
        enumDomainIn(1, 4);
        enumDomainNotIn(2, 3);
    }

    //------------------------------------
    //-------   Remove value  ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValueInner() throws ContradictionException {
        var.removeValue(2, Cause.Null);
        enumDomainIn(1, 3, 4);
        enumDomainNotIn(2);
    }

    //------------------------------------
    //-------   Remove values ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesInner() throws ContradictionException {
        IntIterableRangeSet set = new IntIterableRangeSet(2, 3);
        var.removeValues(set, Cause.Null);
        enumDomainIn(1, 4);
        enumDomainNotIn(2, 3);
    }

    //------------------------------------
    //-----  Remove all values but  ------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesButInner() throws ContradictionException {
        IntIterableSet set = new IntIterableBitSet();
        set.add(1);
        set.add(4);
        var.removeAllValuesBut(set, Cause.Null);
        enumDomainIn(1, 4);
        enumDomainNotIn(2, 3);
    }

    //------------------------------------
    //----------- Utilities  -------------
    //------------------------------------

    private void enumDomainIn(int... values) {
        for (int value : values) {
            assertTrue(var.contains(value));
        }
    }

    private void enumDomainNotIn(int... values) {
        for (int value : values) {
            assertFalse(var.contains(value));
        }
    }

}
