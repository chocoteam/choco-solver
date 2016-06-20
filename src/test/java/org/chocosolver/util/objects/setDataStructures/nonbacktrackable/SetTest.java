/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        assertTrue(set.contain(1));
    }

    /**
     * Value which is lower than the offset
     */
    @Test(groups = "1s", timeOut=60000, expectedExceptions = AssertionError.class)
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
        assertEquals(set.getSize(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAddMaxValue() {
        ISet set = create(Integer.MAX_VALUE);
        set.add(Integer.MAX_VALUE);
        assertEquals(set.getSize(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testAddAlreadyExists() {
        ISet set = create();
        assertTrue(set.add(5));
        assertFalse(set.add(5));
        assertEquals(set.getSize(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRemove() {
        ISet set = create();
        assertTrue(set.add(5));
        assertFalse(set.add(5));
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
            if(set.contain(1)){
                set.remove(1);
            }
            assertNotNull(integer);
            size++;
        }
        assertTrue(5 <= size);
        assertEquals(5, set.getSize());

        size = 0;
        for (Integer integer : set) {
            if(set.contain(6)){
                set.remove(6);
            }
            assertNotNull(integer);
            size++;
        }
        assertTrue(4 <= size);
        assertEquals(4, set.getSize());
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
        assertEquals(set.getSize(), 0);
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
        assertEquals(set.getSize(), 2);
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
       assertEquals(size, set.getSize());
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
        assertTrue(ro.contain(1));
        try {
            ro.remove(1);
            fail();
        } catch(UnsupportedOperationException e) {
            assertTrue(set.contain(1));
        }

        assertTrue(set.remove(1));
        assertFalse(ro.contain(1));

        set.add(2);

        try {
            ro.clear();
        } catch(UnsupportedOperationException e) {
            assertFalse(ro.isEmpty());
        }

        assertTrue(Arrays.equals(ro.toArray(), set.toArray()));
    }


    public abstract ISet create(int offset);

    public ISet create() {
        return create(0);
    }
}
