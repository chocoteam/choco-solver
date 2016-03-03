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
package org.chocosolver.memory;

import java.io.Serializable;

/**
 * Describes an search vector with states (describing some history of the data structure).
 */
public abstract class IStateIntVector implements Serializable {

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

    protected IStateIntVector(IEnvironment environment) {
        this.environment = environment;
    }

    protected boolean rangeCheck(int index) {
        return index < size.get() && index >= 0;
    }

    /**
     * Returns the current size of the stored search vector.
     */
    public int size() {
        return size.get();
    }


    /**
     * Checks if the vector is empty.
     */
    public boolean isEmpty() {
        return size.get() == 0;
    }

    /**
     * Adds a new search at the end of the vector.
     *
     * @param i The search to add.
     */
    public abstract void add(int i);


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
    public abstract void remove(int i);


    /**
     * removes the search at the end of the vector.
     * does nothing when called on an empty vector
     */
    public abstract void removeLast();

    /**
     * Returns the <code>index</code>th element of the vector.
     */
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

    public int[] deepCopy() {
        int[] ret = new int[size.get()];
        System.arraycopy(elementData, 0, ret, 0, size.get());
        return ret;
    }
}
