/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.util.objects.queues;

import org.testng.annotations.Test;

import java.util.NoSuchElementException;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
// TODO rename AQueue to ADeque ?
public abstract class QueueTest {

    public abstract AQueue<Integer> create();

    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        AQueue<Integer> queue = create();
        queue.addFirst(1);
        assertEquals(queue.pollLast(), Integer.valueOf(1));
        queue.addFirst(2);

        assertEquals(queue.pollFirst(), Integer.valueOf(2));
        assertThrows(NoSuchElementException.class, queue::pollLast);
        assertThrows(NoSuchElementException.class, queue::pollFirst);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testNoElement() {
        AQueue<Integer> queue = create();
        assertThrows(NoSuchElementException.class, queue::pollFirst);
        assertThrows(NoSuchElementException.class, queue::pollLast);
        assertThrows(IndexOutOfBoundsException.class, () -> queue.get(0));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSameElement() {
        AQueue<Integer> queue = create();
        assertTrue(queue.addFirst(1));
        assertTrue(queue.addFirst(1));

        assertEquals(queue.pollLast(), Integer.valueOf(1));
        assertEquals(queue.pollLast(), Integer.valueOf(1));
        assertThrows(NoSuchElementException.class, queue::pollLast);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSeveralIterationsOneElement() {
        AQueue<Integer> queue = create();

        for(int i = 0; i < 100; i++) {
            queue.addFirst(i);
            assertEquals(queue.pollLast(), Integer.valueOf(i));

            queue.addLast(i);
            assertEquals(queue.pollFirst(), Integer.valueOf(i));
        }
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSeveralIterationsMultipleElements() {
        AQueue<Integer> queue = create();

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
        AQueue<Integer> queue = create();

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
        AQueue<Integer> queue = create();

        queue.addFirst(1);

        assertFalse(queue.remove(Integer.valueOf(2)));
        assertTrue(queue.remove(Integer.valueOf(1)));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testClear() {
        AQueue<Integer> queue = create();
        queue.addFirst(1);
        queue.addLast(2);
        assertFalse(queue.isEmpty());
        queue.clear(); // TODO Exception -> remove clear from the interface
        assertTrue(queue.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testCover() {
        AQueue<Integer> queue = create();

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
        AQueue<Integer> queue = create();
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
        AQueue<Integer> a = create();
        AQueue<Integer> b = create();

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
        AQueue<Integer> queue = create();
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
        assertThrows(IndexOutOfBoundsException.class, () -> queue.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> queue.get(3));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemove() {
        AQueue<Integer> queue = create();

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
