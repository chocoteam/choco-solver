/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public abstract class IntVarTest {

    protected IntVar var;


    public abstract void setup();

    @Test(groups="1s", timeOut=60000)
    public void testIterator() {
        setup();
        int value = var.getLB()-1;
        Iterator<Integer> iter = var.iterator();
        while(value < var.getUB()) {
            Assert.assertTrue(iter.hasNext());
            Assert.assertEquals(iter.next().intValue(), value+1);
            value++;
        }
        Assert.assertThrows(NoSuchElementException.class, () -> iter.next());
    }

    //------------------------------------
    //------- Remove interval ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveIntervalOK() throws ContradictionException {
        assertTrue(var.removeInterval(3, 4, Cause.Null));
        domainIn(1, 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveIntervalWrongDomain() throws ContradictionException{
        assertFalse(var.removeInterval(5, 6, Cause.Null));
        domainIn(1, 4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveIntervalEmptyDomain() throws ContradictionException {
        var.removeInterval(1, 4, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveIntervalCoverDomain() throws ContradictionException {
        var.removeInterval(0, 5, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveIntervalCoverBound() throws ContradictionException {
        assertTrue(var.removeInterval(3, 5, Cause.Null));
        domainIn(1, 2);
    }


    //------------------------------------
    //-------   Remove value  ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValueOK() throws ContradictionException {
        assertTrue(var.removeValue(4, Cause.Null));
        domainIn(1, 3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValueWrongDomain() throws ContradictionException {
        assertFalse(var.removeValue(7, Cause.Null));
        assertFalse(var.removeValue(-1, Cause.Null));
        domainIn(1, 4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveValueEmptyDomain() throws ContradictionException {
        assertTrue(var.removeValue(4, Cause.Null));
        assertTrue(var.removeValue(1, Cause.Null));
        assertTrue(var.removeValue(2, Cause.Null));
        var.removeValue(3, Cause.Null);
    }


    //------------------------------------
    //-------   Remove values ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesOK() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(1, 2);
        assertTrue(var.removeValues(set, Cause.Null));
        domainIn(3, 4);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesWrongDomain() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(5, 6);
        assertFalse(var.removeValues(set, Cause.Null));
        set = new IntIterableRangeSet(-1, -1);
        assertFalse(var.removeValues(set, Cause.Null));
        domainIn(1, 4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveValuesEmptyDomain() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(1, 4);
        var.removeValues(set, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesTwoSides() throws ContradictionException {
        IntIterableSet set = new IntIterableBitSet();
        set.add(1);
        set.add(4);
        var.removeValues(set, Cause.Null);
        domainIn(2, 3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesCoverBound() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(3, 5);
        assertTrue(var.removeValues(set, Cause.Null));
        domainIn(1, 2);
        set = new IntIterableRangeSet(0, 1);
        assertTrue(var.removeValues(set, Cause.Null));
        domainIn(2, 2);
    }


    //------------------------------------
    //-----  Remove all values but  ------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesButOK() throws ContradictionException {
        IntIterableRangeSet set = new IntIterableRangeSet(3, 4);
        assertTrue(var.removeAllValuesBut(set, Cause.Null));
        domainIn(3, 4);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveAllValuesButCoverDomain() throws ContradictionException {
        IntIterableRangeSet set = new IntIterableRangeSet(1, 4);
        assertFalse(var.removeAllValuesBut(set, Cause.Null));
        set = new IntIterableRangeSet(0, 7);
        assertFalse(var.removeAllValuesBut(set, Cause.Null));
        domainIn(1, 4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void removeAllValuesButWrongDomain() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(8, 9);
        var.removeAllValuesBut(set, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000)
    public void removeAllValuesButCoverBound() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(3, 5);
        assertTrue(var.removeAllValuesBut(set, Cause.Null));
        domainIn(3, 4);
    }


    //------------------------------------
    //----------- Utilities  -------------
    //------------------------------------

    protected void domainIn(int lb, int ub) {
        assertEquals(var.getLB(), lb);
        assertEquals(var.getUB(), ub);
        assertEquals(var.getDomainSize(), ub - lb + 1);
    }


}
