/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.nonbacktrackable;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexandre LEBRUN
 */
public class ConstantSetTest {

    @Test(groups = "1s", timeOut=60000)
    public void testSize() {
        ISet set = create();
        assertEquals(set.size(), 3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testIterator() {
        ISet set = SetFactory.makeConstantSet(new int[]{0, 5, 14, 8});
        Set<Integer> reached = new HashSet<>();
        for (Integer i : set) {
            reached.add(i);
        }
        assertEquals(reached.size(), set.size());
        for(int i=-5;i<=20;i++){
            assertEquals(reached.contains(i),set.contains(i));
        }
        assertEquals(reached.size(), 4);
        assertTrue(reached.contains(0));
        assertTrue(reached.contains(5));
        assertTrue(reached.contains(14));
        assertTrue(reached.contains(8));
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = UnsupportedOperationException.class)
    public void testAdd() {
        ISet set = create();
        set.add(6);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = UnsupportedOperationException.class)
    public void testRemove() {
        ISet set = create();
        set.remove(5);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = UnsupportedOperationException.class)
    public void testClear() {
        ISet set = create();
        set.clear();
    }

    @Test(groups = "1s", timeOut=60000)
    public void testDuplicates() {
        ISet set = SetFactory.makeConstantSet(new int[]{0, 5, 5, 8});
        assertEquals(set.size(), 3);
    }

    private ISet create() {
        return SetFactory.makeConstantSet(new int[]{0, 5, 8});
    }
}
