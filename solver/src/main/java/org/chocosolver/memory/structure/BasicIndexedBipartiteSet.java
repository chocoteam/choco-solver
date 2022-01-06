/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.structure;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;



/**
 */
public final class BasicIndexedBipartiteSet {

    /**
     * The list of values
     */
    private int[] list;

    /**
     * The position of each element within the list.
     * indexes[3] = k <=> list[k] = 3
     * we assume that elements ranges from 0 ... list.length
     * in other words the elements must be indexed.
     */
    private int[] position;

    /**
     * The first element of the list
     */
    private final IStateInt first;

    /**
     * The size of the valid list
     */
    private int size;

    /**
     * Create a stored bipartite set with a size.
     * Thus the value stored will go from 0 to nbValues.
     *
     * @param environment a bactrackable environment
     * @param nbValues capacity
     */
    public BasicIndexedBipartiteSet(IEnvironment environment, int nbValues) {
        this.list = new int[nbValues];
        this.position = new int[nbValues];
        for (int i = 0; i < nbValues; i++) {
            list[i] = position[i] = i;
        }
        this.first = environment.makeInt(0);
        this.size = 0;
    }

    /**
     * Increase the number of value watched.
     */
    private void increaseSize() {
        final int nexSize = list.length * 3 / 2;
        int[] list_ = list;
        list = new int[nexSize];
        System.arraycopy(list_, 0, list, 0, list_.length);
        int[] position_ = position;
        position = new int[nexSize];
        System.arraycopy(position_, 0, position, 0, position_.length);
    }

    public final int size() {
        return size - first.get() + 1;
    }

    public final boolean isEmpty() {
        return first.get() == size;
    }

    public final int add() {
        if (list.length == size) {
            increaseSize();
        }
        list[size] = size;
        position[list[size]] = size;
        size++;
        return size - 1;
    }

    public void swap(final int object) {
        final int idxToSwap = position[object];
        if (idxToSwap == first.get()) {
            first.add(1);
        } else {
            final int temp = list[first.get()];
            list[first.get()] = object;
            list[idxToSwap] = temp;
            position[object] = first.get();
            position[temp] = idxToSwap;
            first.add(1);
        }
    }

    public int get(final int index) {
        return list[index];
    }

    /**
     * Returns the bundle in which the i^h object is placed.
     * <code>true</code> means IN, the object can be swapped
     * <code>false</code> means OUT, the object is already swapped
     *
     * @param i index of the object
     * @return a boolean value: true means can be swapped, false otherwise
     */
    public boolean bundle(int i) {
        return position[i] >= first.get();
    }

    public boolean contains(int i){
        return bundle(i);
    }

}
