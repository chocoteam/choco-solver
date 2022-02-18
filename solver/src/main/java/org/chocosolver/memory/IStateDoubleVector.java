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
public abstract class IStateDoubleVector  {

    /**
     * Minimal capacity of a vector
     */
    public static final int MIN_CAPACITY = 8;

    /**
     * Contains the elements of the vector.
     */
    protected double[] elementData;

    /**
     * A backtrackable search with the size of the vector.
     */
    protected IStateInt size;


    /**
     * The current environment.
     */
    protected final IEnvironment environment;


    protected IStateDoubleVector(IEnvironment env, int initialSize, double initialValue) {
        int initialCapacity = MIN_CAPACITY;
        if (initialCapacity < initialSize)
            initialCapacity = initialSize;

        this.environment = env;
        this.elementData = new double[initialCapacity];
        for (int i = 0; i < initialSize; i++) {
            this.elementData[i] = initialValue;
        }
        this.size = env.makeInt(initialSize);
    }

    protected IStateDoubleVector(IEnvironment env, double[] entries) {
        int initialCapacity = MIN_CAPACITY;
        int initialSize = entries.length;

        if (initialCapacity < initialSize)
            initialCapacity = initialSize;

        this.environment = env;
        this.elementData = new double[initialCapacity];
        System.arraycopy(entries, 0, this.elementData, 0, initialSize);
        this.size = env.makeInt(initialSize);
    }

    /**
     * Returns the <code>index</code>th element of the vector.
     */
    public final double get(int index) {
        if (rangeCheck(index)) {
            return elementData[index];
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
    }

    /**
     * return the indexth element of the vector without an bound check.
     *
     * @param index index
     * @return the element
     */
    public final double quickGet(int index) {
        assert rangeCheck(index);
        return elementData[index];
    }

    protected boolean rangeCheck(int index) {
        return index < size.get() && index >= 0;
    }

    /**
     * Unsafe setter => don't do bound verification
     *
     * @param index the index of the replaced value
     * @param val   the new value
     * @return the old value
     */
    public abstract double quickSet(int index, double val);

}
