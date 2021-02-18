/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;


import java.util.Arrays;

/**
 * A specific implementation for hash map of integers wherein all keys and values are greater or equal to 0.
 * <p>
 * Created by cprudhom on 20/10/2015.
 * Project: choco.
 */
public class IntMap {

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
     * No entry key
     */
    private final int FREE;

    /**
     * The array buffer into which the elements of the values are stored.
     */
    private int[] elements;

    /**
     * The number of key-value mappings contained in this map.
     */
    private int size;


    public IntMap() {
        this(DEFAULT_CAPACITY, -1);
    }

    public IntMap(int initialCapacity) {
        this(initialCapacity, -1);
    }

    public IntMap(int initialCapacity, int no_value) {
        this.elements = new int[initialCapacity];
        this.FREE = no_value;
        Arrays.fill(elements, FREE);
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
            Arrays.fill(elements, oldCapacity, newCapacity, FREE);
        }
    }

    /**
     * Returns the number of elements in this map.
     *
     * @return the number of elements in this map.
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no elements.
     *
     * @return <tt>true</tt> if this map contains no elements.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     */
    public void put(int key, int value) {
        ensureCapacity(key + 1);
        if (elements[key] == FREE) {
            size++;
        }
        if (value == FREE) {
            size--;
        }
        elements[key] = value;
    }

    /**
     * Adjusts the specified value associated with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is adjusted, otherwise, the value is put.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param inc increment to add to the value associated with the key
     */
    public void putOrAdjust(int key, int value, int inc) {
        ensureCapacity(key + 1);
        if (elements[key] == FREE) {
            size++;
            elements[key] = value;
        } else {
            elements[key] += inc;
        }
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code -1} if this map contains no mapping for the key.
     * <p>
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==-1 ? k==-1 :
     * key == k)}, then this method returns {@code v}; otherwise
     * it returns {@code -1}.  (There can be at most one such mapping.)
     * <p>
     * <p>A return value of {@code #FREE} <i>necessarily</i>
     * indicate that the map contains no mapping for the key.
     *
     * @see #put(int, int)
     */
    public int get(int key) {
        if (key <= 0 || key >= elements.length) {
            return FREE;
        }
        return elements[key];
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param key The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(int key) {
        return get(key) != FREE;
    }

    /**
     * Remove the value to which the specified key is mapped.
     *
     * @param key   key with which the specified value is to be cleared
     */
    public void clear(int key) {
        if (key >= 0 && key < elements.length) {
            elements[key] = FREE;
        }
    }


    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        Arrays.fill(elements, FREE);
        size = 0;
    }

}
