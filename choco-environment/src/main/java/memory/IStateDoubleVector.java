/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package memory;

import java.io.Serializable;

/**
 * Describes an search vector with states (describing some history of the data structure).
 */
public abstract class IStateDoubleVector implements Serializable {

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
     * Returns the current size of the stored search vector.
     */

    public final int size() {
        return size.get();
    }


    /**
     * Checks if the vector is empty.
     */

    public final boolean isEmpty() {
        return (size.get() == 0);
    }

    /**
     * Adds a new search at the end of the vector.
     *
     * @param i The search to add.
     */

    public abstract void add(double i);


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
        assert (rangeCheck(index));
        return elementData[index];
    }

    protected boolean rangeCheck(int index) {
        return index < size.get() && index >= 0;
    }

    /**
     * Assigns a new value <code>val</code> to the element <code>index</code> and returns
     * the old value
     */

    public abstract double set(int index, double val);

    /**
     * Unsafe setter => don't do bound verification
     *
     * @param index the index of the replaced value
     * @param val   the new value
     * @return the old value
     */
    public abstract double quickSet(int index, double val);


    public double[] deepCopy() {
        double[] ret = new double[size.get()];
        System.arraycopy(elementData, 0, ret, 0, size.get());
        return ret;
    }
}
