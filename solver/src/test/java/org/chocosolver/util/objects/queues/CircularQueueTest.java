/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.queues;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public class CircularQueueTest {


    public CircularQueue<Integer> create() {
        return new CircularQueue<>(0);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        CircularQueue<Integer> queue = create();
        queue.addFirst(1);
        assertEquals(queue.pollLast(), Integer.valueOf(1));
        assertEquals(queue.pollLast(), null);
        assertEquals(queue.pollFirst(), null);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoElement() {
        CircularQueue<Integer> queue = create();
        assertNull(queue.pollFirst());
        assertNull(queue.pollLast());
        assertNull(queue.get(0));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSameElement() {
        CircularQueue<Integer> queue = create();
        assertTrue(queue.addFirst(1));
        assertTrue(queue.addFirst(1));

        assertEquals(queue.pollLast(), Integer.valueOf(1));
        assertEquals(queue.pollLast(), Integer.valueOf(1));
        assertNull(queue.pollLast());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSeveralIterationsOneElement() {
        CircularQueue<Integer> queue = create();

        for(int i = 0; i < 100; i++) {
            queue.addFirst(i);
            assertEquals(queue.pollLast(), Integer.valueOf(i));

            queue.addLast(i);
            assertEquals(queue.pollFirst(), Integer.valueOf(i));
        }
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSeveralIterationsMultipleElements() {
        CircularQueue<Integer> queue = create();

        // Queue like
        for (int i = 0; i < 100; i++) {
            queue.addFirst(i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals(queue.pollLast(), Integer.valueOf(i));
        }

        // Stack like
        for (int i = 0; i < 100; i++) {
            queue.addLast(i);
        }
        for (int i = 99; i > 0; i--) {
            assertEquals(queue.pollLast(), Integer.valueOf(i));
        }
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveElement() {
        CircularQueue<Integer> queue = create();

        queue.addFirst(1);
        queue.addFirst(1);

        assertTrue(queue.remove(Integer.valueOf(1)));
        assertFalse(queue.isEmpty());
        assertEquals(queue.size(), 1);

        assertTrue(queue.remove(Integer.valueOf(1)));
        assertTrue(queue.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveNoSuchElement() {
        CircularQueue<Integer> queue = create();

        queue.addFirst(1);

        assertFalse(queue.remove(Integer.valueOf(2)));
        assertTrue(queue.remove(Integer.valueOf(1)));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testClear() {
        CircularQueue<Integer> queue = create();
        queue.addFirst(1);
        queue.addLast(2);
        assertFalse(queue.isEmpty());
        queue.clear();
        assertTrue(queue.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCover() {
        CircularQueue<Integer> queue = create();

        queue.addFirst(1);
        queue.addLast(2);
        queue.addFirst(3);
        queue.addLast(4);
        queue.addFirst(5);
        queue.addLast(6);

        assertEquals(queue.pollFirst(), Integer.valueOf(5));
        assertEquals(queue.pollFirst(), Integer.valueOf(3));
        assertEquals(queue.pollFirst(), Integer.valueOf(1));
        assertEquals(queue.pollFirst(), Integer.valueOf(2));
        assertEquals(queue.pollFirst(), Integer.valueOf(4));
        assertEquals(queue.pollFirst(), Integer.valueOf(6));
        assertTrue(queue.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testIndexOf() {
        CircularQueue<Integer> queue = create();
        queue.addFirst(1);
        queue.addFirst(2);
        queue.addFirst(3);

        assertEquals(queue.indexOf(1), 2);
        assertEquals(queue.indexOf(2), 1);
        assertEquals(queue.indexOf(3), 0);

        // 4 does not exists in the queue
        assertEquals(queue.indexOf(4), -1);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testEquals() {
        CircularQueue<Integer> a = create();
        CircularQueue<Integer> b = create();

        a.addFirst(1);
        assertEquals(a.equals(b), a == b);
        assertNotEquals(a, b);

        b.addFirst(2);
        assertEquals(a.equals(b), a == b);
        assertNotEquals(a, b);

        assertEquals(a, a);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testGet() {
        CircularQueue<Integer> queue = create();
        queue.addFirst(1);
        queue.addFirst(2);
        queue.addFirst(3);

        assertEquals(queue.get(0), Integer.valueOf(3));
        assertEquals(queue.get(1), Integer.valueOf(2));
        assertEquals(queue.get(2), Integer.valueOf(1));

        // The elements must NOT be removed
        assertEquals(queue.get(0), Integer.valueOf(3));
        assertEquals(queue.get(1), Integer.valueOf(2));
        assertEquals(queue.get(2), Integer.valueOf(1));

        // Missing elements
        assertNull(queue.get(-1));
        assertNull(queue.get(3));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemove() {
        CircularQueue<Integer> queue = create();

        queue.addFirst(0);
        queue.addFirst(1);
        queue.addFirst(2);
        queue.addFirst(3);
        queue.addFirst(2);

        queue.remove(Integer.valueOf(2));

        assertEquals(queue.pollFirst(), Integer.valueOf(3));
        assertEquals(queue.pollFirst(), Integer.valueOf(2));
        assertEquals(queue.pollFirst(), Integer.valueOf(1));
        assertEquals(queue.pollFirst(), Integer.valueOf(0));
    }
}
