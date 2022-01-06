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

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/04/2014
 */
public class PriorityQueue {

    private final int n;
    private final int[] indices;
    private final int[] values;
    private final int[] pointers;
    private int first, lastElt;

    public PriorityQueue(int _n) {
        this.n = _n;
        this.indices = new int[_n];
        this.pointers = new int[_n];
        this.values = new int[_n];
        this.clear();
    }

    /**
     * Adds an integer into the list. The element is inserted at its right
     * place (the list is sorted) in O(n).
     *
     * @param index the element to insert.
     * @param value the value to be used for the comparison of the elements to add.
     * @return <code>true</code> if and only if the list is not full.
     */
    public boolean addElement(int index, int value) {
        int i;
        int j = -1;
        if (this.lastElt == this.n) {
            return false;
        }
        this.indices[this.lastElt] = index;
        this.values[this.lastElt] = value;

        for (i = this.first; i != -1 && this.values[i] <= value; i = this.pointers[i]) {
            j = i;
        }
        this.pointers[this.lastElt] = i;
        if (j == -1) {
            this.first = this.lastElt;
        } else {
            this.pointers[j] = this.lastElt;
        }
        this.lastElt++;
        return true;
    }

    /**
     * Returns and removes the element with highest priority (i.e. lowest value) in O(1).
     *
     * @return the lowest element.
     */
    public int pop() {
        if (this.isEmpty()) {
            return -1;
        }
        int elt = this.indices[this.first];
        this.first = this.pointers[this.first];
        return elt;
    }

    /**
     * Tests if the list is empty or not.
     *
     * @return <code>true</code> if and only if the list is empty.
     */
    public boolean isEmpty() {
        return (this.first == -1);
    }

    /**
     * Clears the list.
     */
    public void clear() {
        this.first = -1;
        this.lastElt = 0;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("<");
        for (int i = this.first; i != -1; i = this.pointers[i]) {
            s.append(" ").append(this.indices[i]);
        }
        s.append(" >");
        return s.toString();
    }
}
