/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;


import java.util.Arrays;

/**
 * A specific implementation for list of integers.
 * Created by cprudhom on 20/10/2015.
 * Project: choco.
 */
public class IntList {

    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     */
    private int[] elements;

    /**
     * The size of the ArrayList (the number of elements it contains).
     */
    private int size;

    /**
     * Constructs an empty list with the initial capacity of 10
     */
    public IntList() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     */
    public IntList(int initialCapacity) {
        this.elements = new int[initialCapacity];
        this.size = 0;
    }

    /**
     * Constructs a list containing the elements of <code>values</code>.
     *
     * @param values the values that are to be placed in this list
     */
    public IntList(int... values) {
        this.size = values.length;
        this.elements = new int[size];
        System.arraycopy(values, 0, elements, 0, size);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity - elements.length > 0){
            int oldCapacity = elements.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            if (newCapacity - MAX_ARRAY_SIZE > 0)
                newCapacity = MAX_ARRAY_SIZE;
            // minCapacity is usually close to size, so this is a win:
            elements = Arrays.copyOf(elements, newCapacity);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list.
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return getQuick(index);
    }

    /**
     * Returns the element at the specified position in this list without doing any bounds checking.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list.
     */
    public int getQuick(int index) {
        return elements[index];
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public void replace(int index, int element) {
        if (index < 0 || index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        replaceQuick(index, element);
    }

    /**
     * Replaces the element at the specified position in this list with
     * the specified element without doing any bounds checking.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public void replaceQuick(int index, int element) {
        elements[index] = element;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param element element to be appended to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
    public boolean add(int element) {
        ensureCapacity(size + 1);
        elements[size++] = element;
        return true;
    }

    /**
     * Add the element at the specified position in this list.
     *
     * @param index   index of the element to put
     * @param element element to be stored at the specified position
     */
    public boolean addAt(int index, int element) {
        ensureCapacity(index);
        elements[index] = element;
        return true;
    }

    /**
     * Appends the elements of the specified list to the end of this list.
     *
     * @param list elements to be appended to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     */
    public boolean addAll(IntList list) {
        ensureCapacity(size + list.size);
        System.arraycopy(list.elements, 0, elements, size, list.size);
        size += list.size;
        return true;
    }

    /**
     * Removes the first occurrence of the specified element from this list,
     * if it is present.  If the list does not contain the element, it is
     * unchanged.
     *
     * @param value element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    public boolean remove(int value) {
        boolean removed = false;
        for (int index = 0; index < size && !removed; index++) {
            if (value == elements[index]) {
                removeAt(index);
                removed = true;
            }
        }
        return removed;
    }


    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public void removeAt(int index) {
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elements, index+1, elements, index, numMoved);
        size--;
    }

    /**
     * Removes from this list all of the elements whose index is between
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).
     * This call shortens the list by {@code (toIndex - fromIndex)} elements.
     * (If {@code toIndex==fromIndex}, this operation has no effect.)
     *
     * @throws IndexOutOfBoundsException if {@code fromIndex} or
     *         {@code toIndex} is out of range
     *         ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    public void removeRange(int fromIndex, int toIndex) {
        int numMoved = size - toIndex;
        System.arraycopy(elements, toIndex, elements, fromIndex, numMoved);
        size -= (toIndex-fromIndex);
    }

    /**
     * The list will be empty after this call returns.
     * But no value is removed, only the size is reset to 0.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     * <p>
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * @return an array containing all of the elements in this list in
     * proper sequence
     */
    public int[] toArray() {
        return Arrays.copyOf(elements, size);
    }

}
