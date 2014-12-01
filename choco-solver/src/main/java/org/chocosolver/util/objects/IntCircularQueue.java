/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.util.objects;

import java.io.Serializable;

/**
 * A fix sized circular queue optimized for removing first and last elements.
 * Some few (essential regarding the usage) methods are implemented.
 * <br/>
 * Be aware of the size computation: the modulo operation is not efficient in java.
 * On the other hand, the modulo of powers of 2 can alternatively be expressed as a bitwise AND operation:
 * <br/>
 * x % 2n == x & (2n - 1)
 * <br/>
 * That is why the size of the data is automatically set to the closest greater powers of 2 value.
 *
 * @author Charles Prud'homme
 * @since 29 sept. 2010
 */
public class IntCircularQueue implements Serializable {

    int[] elementData;
    // head points to the first logical element in the array, and
    // tail points to the element following the last.  This means
    // that the list is empty when head == tail.  It also means
    // that the elementData array has to have an extra space in it.
    int head = 0;
    int tail = 0;
    // Strictly speaking, we don't need to keep a handle to size,
    // as it can be calculated programmatically, but keeping it
    // makes the algorithms faster.
    int size = 0;
    int capacity;

    /**
     * Compute the powers of 2 value immedialty greater to <code>size</code>
     *
     * @param size the curent number of element
     * @return the powers of 2 value immedialty greater to <code>size</code>
     */
    private static int closestGreater2n(int size) {
        if (size == 0) return 2;
        int _size = Integer.highestOneBit(size) << 1;
        assert (_size >= size);
        return _size;
    }

    @SuppressWarnings({"unchecked"})
    public IntCircularQueue(int size) {
        size = closestGreater2n(size);
        elementData = new int[size];
        capacity = size;
    }

    // The convert() method takes a logical index (as if head was
    // always 0) and calculates the index within elementData

    private int convert(int base, int delta) {
        return (base + delta) & (capacity - 1);
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = tail = size = 0;
    }

    public int get(int index) {
        return elementData[convert(index, head)];
    }

    public boolean addFirst(int e) {
//        elements[head = (head - 1) & (elements.length - 1)] = e;
//        if (head == tail)
//            doubleCapacity();
        elementData[head = convert(head, -1)] = e;
        size++;
        if (head == tail)
            doubleCapacity();
        return true;
    }

    public boolean addLast(int e) {
//        elements[tail] = e;
//        if ( (tail = (tail + 1) & (elements.length - 1)) == head)
//            doubleCapacity();
        elementData[tail] = e;
        size++;
        if ((tail = convert(tail, 1)) == head)
            doubleCapacity();
        return true;
    }

    /**
     * {@inheritDoc}
     * This method is the main reason we re-wrote the class.
     * It is optimized for removing first and last elements
     * but also allows you to remove in the middle of the list.
     */
    public int pollFirst() {
        int pos = convert(head, 0);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        int tmp = elementData[pos];
        // optimized for FIFO access, i.e. adding to back and
        // removing from front
        if (pos == head) {
            head = convert(head, 1);
        }
        size--;
        return tmp;
    }

    /**
     * {@inheritDoc}
     * This method is the main reason we re-wrote the class.
     * It is optimized for removing first and last elements
     * but also allows you to remove in the middle of the list.
     */
    public int pollLast() {
        int pos = convert(tail, -1);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        int tmp = elementData[pos];
        // optimized for FIFO access, i.e. adding to back and
        // removing from front
        tail = pos;
        size--;
        return tmp;
    }

    /**
     * Double the capacity of this deque.  Call only when full, i.e.,
     * when head and tail have wrapped around to become equal.
     */
    private void doubleCapacity() {
        assert head == tail;
        int p = head;
        int n = capacity;
        int r = n - p; // number of elements to the right of p
        int newCapacity = n << 1;
        if (newCapacity < 0)
            throw new IllegalStateException("Sorry, deque too big");
        int[] a = new int[newCapacity];
        System.arraycopy(elementData, p, a, 0, r);
        System.arraycopy(elementData, 0, a, r, p);
        elementData = a;
        head = 0;
        tail = n;
        capacity = newCapacity;
    }
}
