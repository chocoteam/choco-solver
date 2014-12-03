/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.memory;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: 12 juil. 2007
 * Time: 10:16:08
 */
public interface IStateBitSet extends Serializable {


    /**
     * Number of bits on. Sums the number of on bits in each integer.
     *
     * @return the total number of bits on
     */
    int cardinality();

    /**
     * Size of the bitset
     *
     * @return
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
    public int nextClearBit(int fromIndex);

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
    public int prevClearBit(int fromIndex);

    int capacity();

    IStateBitSet copy();

    BitSet copyToBitSet();


    void or(IStateBitSet other);

    void and(IStateBitSet other);

    void xor(IStateBitSet other);

    void andNot(IStateBitSet other);

    boolean intersects(IStateBitSet setI);

    void flip(int bitIndex);

    void flip(int fromIndex, int toIndex);

    boolean isEmpty();

}
