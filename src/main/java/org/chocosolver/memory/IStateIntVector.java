/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;



/**
 * Describes an search vector with states (describing some history of the data structure).
 */
public abstract class IStateIntVector  {

    /**
     * Minimal capacity of a vector
     */
    public static final int MIN_CAPACITY = 8;

    /**
     * Contains the elements of the vector.
     */
    protected int[] elementData;

    /**
     * A backtrackable search with the size of the vector.
     */
    protected IStateInt size;


    /**
     * The current environment.
     */
    protected final IEnvironment environment;


    public IStateIntVector(IEnvironment env, int initialSize, int initialValue) {
        int initialCapacity = MIN_CAPACITY;

        if (initialCapacity < initialSize)
            initialCapacity = initialSize;

        this.environment = env;
        this.elementData = new int[initialCapacity];
        for (int i = 0; i < initialSize; i++) {
            this.elementData[i] = initialValue;
        }
        this.size = env.makeInt(initialSize);
    }


    public IStateIntVector(IEnvironment env, int[] entries) {
        int initialCapacity = MIN_CAPACITY;
        int initialSize = entries.length;

        if (initialCapacity < initialSize)
            initialCapacity = initialSize;

        this.environment = env;
        this.elementData = new int[initialCapacity];
        System.arraycopy(entries, 0, this.elementData, 0, initialSize);
        this.size = env.makeInt(initialSize);
    }

    @Deprecated // never used
    protected IStateIntVector(IEnvironment environment) {
        this.environment = environment;
    }

    protected boolean rangeCheck(int index) {
        return index < size.get() && index >= 0;
    }

    /**
     * Returns the current size of the stored search vector.
     */
    @Deprecated // never used
    public int size() {
        return size.get();
    }


    /**
     * Checks if the vector is empty.
     */
    @Deprecated // never used
    public boolean isEmpty() {
        return size.get() == 0;
    }

    /**
     * Adds a new search at the end of the vector.
     *
     * @param i The search to add.
     */
    public abstract void add(int i);

    @Deprecated // never used
    public boolean contains(int val) {
        int ssize = size.get();
        for (int i = 0; i < ssize; i++) {
            if (val == elementData[i]) return true;
        }
        return false;
    }

    /**
     * Removes an int.
     *
     * @param i The search to remove.
     */
    @Deprecated // never used
    public abstract void remove(int i);


    /**
     * removes the search at the end of the vector.
     * does nothing when called on an empty vector
     */
    @Deprecated // never used
    public abstract void removeLast();

    /**
     * Returns the <code>index</code>th element of the vector.
     */
    @Deprecated // never used
    public int get(int index) {
        if (rangeCheck(index)) {
            return elementData[index];
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
    }

    /**
     * access an element without any bound check
     *
     * @param index index of the element
     * @return the value
     */
    public int quickGet(int index) {
        assert rangeCheck(index);
        return elementData[index];
    }

    /**
     * Assigns a new value <code>val</code> to the element <code>index</code> and returns
     * the old value
     */
    public abstract int set(int index, int val);

    /**
     * Assigns a new value val to the element indexth and return the old value without bound check
     *
     * @param index the index where the value is modified
     * @param val   the new value
     * @return the old value
     */
    public abstract int quickSet(int index, int val);

    @Deprecated // never used
    public int[] deepCopy() {
        int[] ret = new int[size.get()];
        System.arraycopy(elementData, 0, ret, 0, size.get());
        return ret;
    }
}
