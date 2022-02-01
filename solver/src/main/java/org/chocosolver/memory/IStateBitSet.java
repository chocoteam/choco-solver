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
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: 12 juil. 2007
 * Time: 10:16:08
 */
public interface IStateBitSet  {


    /**
     * Number of bits on. Sums the number of on bits in each integer.
     *
     * @return the total number of bits on
     */
    int cardinality();

    /**
     * Size of the bitset
     *
     * @return size of the bitset
     */
    int size();

    /**
     * Puts the specified bit on.
     *
     * @param bitIndex the bit to put on
     */
    void set(int bitIndex);

    /**
     * Puts the specified bit off.
     *
     * @param bitIndex the bit to put off
     */
    void clear(int bitIndex);

    /**
     * Remove all bits;
     */
    void clear();

    void clear(int fromIndex, int toIndex);

    void set(int index, boolean value);

    void set(int fromIdex, int toIndex);

    boolean get(int bitIndex);

    /**
     * Returns the index of the first bit that is set to <code>true</code>
     * that occurs on or after the specified starting index. If no such
     * bit exists then -1 is returned.
     * <p/>
     * To iterate over the <code>true</code> bits in a <code>BitSet</code>,
     * use the following loop:
     * <p/>
     * for(int i=bs.nextSetBit(0); i>=0; i=bs.nextSetBit(i+1)) {
     * // operate on index i here
     * }
     *
     * @param fromIndex the index to start checking from (inclusive).
     * @return the index of the next set bit.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since 1.4
     */
    // TODO: write the currentElement file + prevSetBit + nextClearBit
    int nextSetBit(int fromIndex);

    /**
     * Returns the index of the first bit that is set to <code>false</code>
     * that occurs on or after the specified starting index.
     *
     * @param fromIndex the index to start checking from (inclusive).
     * @return the index of the next clear bit.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since 1.4
     */
    int nextClearBit(int fromIndex);

    /**
     * Returns the index of the first bit that is set to <code>true</code>
     * that occurs on or before the specified starting index. If no such
     * bit exists then -1 is returned.
     *
     * @param fromIndex the index to start checking from (inclusive).
     * @return the index of the previous set bit.
     * @throws IndexOutOfBoundsException if the specified index is
     *                                   negative or too large
     */
    int prevSetBit(int fromIndex);


    /**
     * Returns the index of the first bit that is set to <code>false</code>
     * that occurs on or before the specified starting index. If no such
     * bit exists then -1 is returned.
     *
     * @param fromIndex the index to start checking from (inclusive).
     * @return the index of the previous set bit.
     * @throws IndexOutOfBoundsException if the specified index is
     *                                   negative or too large
     */
    int prevClearBit(int fromIndex);

    boolean isEmpty();
}
