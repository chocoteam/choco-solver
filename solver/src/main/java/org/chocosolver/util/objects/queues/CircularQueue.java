/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.queues;



/**
 * A fixed sized circular queue optimized for removing first and last elements.
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
public class CircularQueue<E> {

    //***********************************************************************************
    // VARIABLE
    //***********************************************************************************

    private E[] elementData;
    // head points to the first logical element in the array, and
    // tail points to the element following the last.  This means
    // that the list is empty when head == tail.  It also means
    // that the elementData array has to have an extra space in it.
    private int head = 0;
    private int tail = 0;
    // Strictly speaking, we don't need to keep a handle to size,
    // as it can be calculated programmatically, but keeping it
    // makes the algorithms faster.
    private int size = 0;
    private int capacity;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    @SuppressWarnings({"unchecked"})
    public CircularQueue(int size) {
        size = closestGreater2n(size);
        elementData = (E[]) new Object[size];
        capacity = size;
    }

    //***********************************************************************************
    // API
    //***********************************************************************************

    /**
     * @return if the queue has no element (size == 0)
     */
    public boolean isEmpty() {
        return head == tail;
    }

    /**
     * Removes all the content of the queue
     */
    public void clear() {
        head = tail = size = 0;
    }

    /**
     * Get the current number of elements
     * @return current number of inserted elements in the queue
     */
    public int size() {
        return size;
    }

    /**
     * Get the <code>index</code> element of the queue, 0 being the last element
     * @param index index of the element to retrieve
     * @return the element itself, or null if it does not exist
     * @see CircularQueue#indexOf(Object)
     */
    public E get(int index) {
        if(index < 0 || index >= size){
            return null;
        }
        return elementData[convert(index, head)];
    }


    /**
     * Put a new element at the head of the queue.
     * The {@link CircularQueue} grows by itself if it reaches its max capacity
     * @param e element to add
     * @return if the element has been added or not
     */
    public boolean addFirst(E e) {
        elementData[head = convert(head, -1)] = e;
        size++;
        if (head == tail)
            doubleCapacity();
        return true;
    }

    /**
     * Put a new element at the tail of the queue.
     * The {@link CircularQueue} grows by itself if it reaches its max capacity
     * @param e element to add
     * @return if the element has been added or not
     */
    public boolean addLast(E e) {
        elementData[tail] = e;
        size++;
        if ((tail = convert(tail, 1)) == head)
            doubleCapacity();
        return true;
    }


    /**
     * Search an element equal to the parameter in the {@link CircularQueue}, and return its index (0 is the last element)
     * @param elem element to query in the {@link CircularQueue}
     * @return index of the element starting from the last of the {@link CircularQueue}, -1 if the element does not exists
     */
    public int indexOf(E elem) {
        assert elem != null;
        for (int i = 0; i < size; i++)
            if (elem.equals(elementData[convert(head, i)]))
                return i;
        return -1;
    }

    /**
     * Removes the first element of the queue and returns it
     * <br>
     * This method is the main reason we re-wrote the class.
     * It is optimized for removing first and last elements
     * but also allows you to remove in the middle of the list.
     * @return first element of the queue
     */
    public E pollFirst() {
        return pollAndClean(false);
    }

    /**
     * Removes the last element of the queue and returns it
     * <br>
     * This method is the main reason we re-wrote the class.
     * It is optimized for removing first and last elements
     * but also allows you to remove in the middle of the list.
     * @return last element of the queue
     */
    public E pollLast() {
        if(size == 0){
            return null;
        }
        int pos = convert(tail, -1);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        E tmp = elementData[pos];
        // optimized for FIFO access, i.e. adding to back and
        // removing from front
        tail = pos;
        size--;
        return tmp;
    }

    /**
     * This method is the main reason we re-wrote the class.
     * It is optimized for removing first and last elements
     * but also allows you to remove in the middle of the list.
     */
    public E remove() {
        return pollAndClean(false);
    }

    /**
     * Removes the <code>index</code> element of the queue and removes the resulting gap
     * <br>
     * This method is the main reason we re-wrote the class.
     * It is optimized for removing first and last elements
     * but also allows you to remove in the middle of the list.
     * @see CircularQueue#indexOf(Object)
     * @param index
     * @return removed element
     */
    public E remove(int index) {
        if(size == 0){
            return null;
        }
        int pos = convert(head, index);
        E tmp = elementData[pos];
//        elementData[pos] = null; // Let gc do its work
        // optimized for FIFO access, i.e. adding to back and
        // removing from front
        if (pos - head == 0) {
            head = convert(head, 1);
        } else if (pos - tail == 0) {
            tail = convert(tail - 1, capacity);
        } else {
            if (pos > head && pos > tail) { // tail/head/pos
                System.arraycopy(elementData, head, elementData, head + 1, pos - head);
                head = convert(head, 1);
            } else {
                System.arraycopy(elementData, pos + 1, elementData, pos, tail - pos - 1);
                tail = convert(tail - 1, capacity);
            }
        }
        size--;
        return tmp;
    }

    /**
     * Remove the first element equal to the value given as parameter
     * @param e value to remove from the {@link CircularQueue}
     * @return if the value has been removed
     */
    public boolean remove(E e) {
        int i = indexOf(e);
        if (i > -1) {
            remove(i);
            return true;
        }
        return false;
    }

    //***********************************************************************************
    // PRIVATE METHODS
    //***********************************************************************************

    /**
     * Compute the powers of 2 value immediately greater to <code>size</code>
     *
     * @param size the curent number of element
     * @return the powers of 2 value immediately greater to <code>size</code>
     */
    private static int closestGreater2n(int size) {
        if (size == 0) return 2;
        int _size = Integer.highestOneBit(size) << 1;
        assert (_size >= size);
        return _size;
    }

    // The convert() method takes a logical index (as if head was
    // always 0) and calculates the index within elementData
    private int convert(int base, int delta) {
        return (base + delta) & (capacity - 1);
    }


    @SuppressWarnings("SameParameterValue")
    private E pollAndClean(boolean clean){
        if(size == 0){
            return null;
        }
        int pos = convert(head, 0);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        E tmp = elementData[pos];
        if(clean){
            elementData[pos] = null; // Let gc do its work
        }
        // optimized for FIFO access, i.e. adding to back and
        // removing from front
        if (pos == head) {
            head = convert(head, 1);
        }
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
        Object[] a = new Object[newCapacity];
        System.arraycopy(elementData, p, a, 0, r);
        System.arraycopy(elementData, 0, a, r, p);
        //noinspection unchecked
        elementData = (E[]) a;
        head = 0;
        tail = n;
        capacity = newCapacity;
    }
}
