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
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.Set_ReadOnly;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public abstract class SetTest {

    @Test(groups="1s", timeOut=60000)
    public void testAddNominal() {
        ISet set = create();
        assertTrue(set.add(1));
        assertTrue(set.contains(1));
    }

    /**
     * Value which is lower than the offset
     */
    @Test(groups = "1s", timeOut=60000, expectedExceptions = IllegalStateException.class)
    public void testAddNegativeKO() {
        ISet set = create();
        assertFalse(set.add(-2)); // expected exception here
    }

    /**
     * Here the offset is negative, so we can add a negative value
     */
    @Test(groups="1s", timeOut=60000)
    public void testAddNegativeOK() {
        ISet set = create(-1);
        assertTrue(set.add(-1));
        assertEquals(set.size(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAddMaxValue() {
        ISet set = create(Integer.MAX_VALUE);
        set.add(Integer.MAX_VALUE);
        assertEquals(set.size(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAddAlreadyExists() {
        ISet set = create();
        assertTrue(set.add(5));
        assertFalse(set.add(5));
        assertEquals(set.size(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemove() {
        ISet set = create();
        assertTrue(set.add(5));
        assertFalse(set.add(5));
        assertFalse(set.remove(50));
        assertTrue(set.remove(5));
        assertTrue(set.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveInLoop() {
        ISet set = create();

        set.add(1);
        set.add(2);
        set.add(3);
        set.add(7);
        set.add(6);
        set.add(4);

        int size = 0;
        for (Integer integer : set) {
            assertNotNull(integer);
            size++;
        }
        assertEquals(6, size);

        size = 0;
        for (Integer integer : set) {
            if(set.contains(1)){
                set.remove(1);
            }
            assertNotNull(integer);
            size++;
        }
        assertTrue(5 <= size);
        assertEquals(5, set.size());

        size = 0;
        for (Integer integer : set) {
            if(set.contains(6)){
                set.remove(6);
            }
            assertNotNull(integer);
            size++;
        }
        assertTrue(4 <= size);
        assertEquals(4, set.size());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testMultipleLoops() {
        ISet set = create();

        set.add(1);
        set.add(2);
        set.add(3);

        int size = 0;
        for (Integer integer : set) {
            assertNotNull(integer);
            size++;
        }
        assertEquals(3, size);

        set.remove(3);
        size = 0;
        for (Integer integer : set) {
            assertNotNull(integer);
            size++;
        }
        assertEquals(2, size);

    }


    @Test(groups = "1s", timeOut=60000)
    public void testClear() {
        ISet set = create();
        for (int i = 0; i < 100; i++) {
            set.add(i);
        }
        set.clear();
        assertEquals(set.size(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMinMax() {
        ISet set = create(-5);
		set.add(1);
        set.add(5);
		set.add(6);
		set.add(-2);
		set.add(3);
		set.add(10);
		assertEquals(set.min(), -2);
		assertEquals(set.max(), 10);
    }

	@Test(groups="1s", timeOut=60000, expectedExceptions = IllegalStateException.class)
	public void testEmptyMin() {
		ISet set = create(0);
		set.add(1);
		set.clear();
		int error = set.min();
	}

	@Test(groups="1s", timeOut=60000, expectedExceptions = IllegalStateException.class)
	public void testEmptyMax() {
		ISet set = create(0);
		set.add(1);
		set.remove(1);
		int error = set.max();
	}

    /**
     * Tests with 2 elements
     */

    @Test(groups="1s", timeOut=60000)
    public void testAddNominal2() {
        ISet set = create();
        set.add(5);
        set.add(6);
        assertEquals(set.size(), 2);
    }

    /**
     * Misc
     */

    @Test(groups="1s", timeOut=60000)
    public void testIterator() {
        ISet set = create();
        Iterator one = set.iterator();
        Iterator two = set.iterator();
        // The iterator instances are be the same, do not use nested loops with this impl
        assertTrue(one == two);
    }

    @Test(groups="1s", timeOut=60000)
    public void testNewIterator() {
        ISet set = create();
        Iterator one = set.newIterator();
        Iterator two = set.newIterator();
        // The iterator instances must NOT be the same, otherwise loop issues
        assertTrue(one != two);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIteratorLength() {
        ISet set = create();
        set.add(1);
        ISetIterator iterator = set.newIterator();
        iterator.reset();
        int size = 0;
        while(iterator.hasNext()) {
            iterator.next();
            size++;
        }
       assertEquals(size, set.size());
    }

    /**
     * Equals is actually by reference (no deep checking)
     */
    @Test(groups="1s", timeOut=60000)
    public void testEquals() {
        ISet a = create();
        ISet b = create();

        a.add(1);
        assertEquals(a == b, a.equals(b));

        b.add(1);
        assertEquals(a == b, a.equals(b));
    }

    @Test(groups="1s", timeOut=60000)
    public void testReadOnlySet() {
        ISet set = create();
        ISet ro = new Set_ReadOnly(set);

        assertTrue(ro.isEmpty());

        assertTrue(set.add(1));
        assertTrue(ro.contains(1));
        try {
            ro.remove(1);
            fail();
        } catch(UnsupportedOperationException e) {
            assertTrue(set.contains(1));
        }

        assertTrue(set.remove(1));
        assertFalse(ro.contains(1));

        set.add(2);

        try {
            ro.clear();
        } catch(UnsupportedOperationException e) {
            assertFalse(ro.isEmpty());
        }

        assertTrue(Arrays.equals(ro.toArray(), set.toArray()));
    }

	@Test(groups = "1s", timeOut=60000)
	public void testAddRemoveReturnValueNB() {
		ISet set = create();
		set.add(1);
		assertFalse(set.add(1));
		assertTrue(set.remove(1));
		assertTrue(set.add(2));
		assertTrue(set.contains(2));
		assertFalse(set.contains(1));
		assertFalse(set.remove(1));
	}

    public abstract ISet create(int offset);

    public ISet create() {
        return create(0);
    }
}
