/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
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

    protected boolean rangeCheck(int index) {
        return index < size.get() && index >= 0;
    }

    /**
     * Adds a new search at the end of the vector.
     *
     * @param i The search to add.
     */
    public abstract void add(int i);

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
}
