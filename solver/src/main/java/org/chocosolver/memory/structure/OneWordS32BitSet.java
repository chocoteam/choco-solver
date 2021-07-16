/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.structure;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;


public class OneWordS32BitSet implements IStateBitSet {

    /*
    * BitSets are packed into arrays of "word."  Currently a word is
    * an int, which consists of 32 bits, requiring 6 address bits.
    * The choice of word size is determined purely by performance concerns.
    */
    private final static int ADDRESS_BITS_PER_WORD = 5;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;

    /* Used to shift left or right for a partial word mask */
    private static final int WORD_MASK = 0xffffffff;

    /**
     * The internal field corresponding to the serialField "bits".
     */
    private IStateInt word;

    /**
     * Creates a bit set whose initial size is large enough to explicitly
     * represent bits with indices in the range <code>0</code> through
     * <code>nbits-1</code>. All bits are initially <code>false</code>.
     *
     * @param environment backtrackable environment
     * @param nbits       the initial size of the bit set.
     * @throws NegativeArraySizeException if the specified initial size
     *                                    is negative.
     */
    public OneWordS32BitSet(IEnvironment environment, int nbits) {
        // nbits can't be negative; size 0 is OK
        if (nbits < 0)
            throw new NegativeArraySizeException("nbits < 0: " + nbits);
        if (nbits > 32)
            throw new ArrayIndexOutOfBoundsException("nbits > 32: " + nbits);

        word = environment.makeInt(0);
    }

    /**
     * Checks that fromIndex ... toIndex is a valid range of bit indices.
     *
     * @param fromIndex starting index
     * @param toIndex   ending index
     */
    private static void checkRange(int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        if (toIndex < 0)
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        if (fromIndex > toIndex)
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
    }

    /**
     * Sets the bit at the specified index to <code>true</code>.
     *
     * @param bitIndex a bit index.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since JDK1.0
     */
    @Override
    public void set(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        word.set(word.get() | (1 << bitIndex)); // Restores invariants
    }

    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param bitIndex a bit index.
     * @param value    a boolean value to set.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since 1.4
     */
    @Override
    public void set(int bitIndex, boolean value) {
        if (value)
            set(bitIndex);
        else
            clear(bitIndex);
    }

    /**
     * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
     * specified <tt>toIndex</tt> (exclusive) to <code>true</code>.
     *
     * @param fromIndex index of the first bit to be set.
     * @param toIndex   index after the last bit to be set.
     * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
     *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
     *                                   larger than <tt>toIndex</tt>.
     * @since 1.4
     */
    @Override
    public void set(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex)
            return;

        int firstWordMask = WORD_MASK << fromIndex;
        int lastWordMask = WORD_MASK >>> -toIndex;
        word.set(word.get() | (firstWordMask & lastWordMask));
    }

    /**
     * Sets the bit specified by the index to <code>false</code>.
     *
     * @param bitIndex the index of the bit to be cleared.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since JDK1.0
     */
    @Override
    public void clear(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        word.set(word.get() & ~(1 << bitIndex));
    }

    /**
     * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
     * specified <tt>toIndex</tt> (exclusive) to <code>false</code>.
     *
     * @param fromIndex index of the first bit to be cleared.
     * @param toIndex   index after the last bit to be cleared.
     * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
     *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
     *                                   larger than <tt>toIndex</tt>.
     * @since 1.4
     */
    @Override
    public void clear(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex)
            return;

        int firstWordMask = WORD_MASK << fromIndex;
        int lastWordMask = WORD_MASK >>> -toIndex;
        word.set(word.get() & ~(firstWordMask & lastWordMask));
    }

    /**
     * Sets all of the bits in this BitSet to <code>false</code>.
     *
     * @since 1.4
     */
    @Override
    public void clear() {
        word.set(0);
    }

    /**
     * Returns the value of the bit with the specified index. The value
     * is <code>true</code> if the bit with the index <code>bitIndex</code>
     * is currently set in this <code>BitSet</code>; otherwise, the result
     * is <code>false</code>.
     *
     * @param bitIndex the bit index.
     * @return the value of the bit with the specified index.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     */
    @Override
    final public boolean get(final int bitIndex) {
        return bitIndex < 32 && ((word.get() & (1 << bitIndex)) != 0);
    }

    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     * <p/>
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     * <p/>
     * <pre> {@code
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     *     // operate on index i here
     * }}</pre>
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since 1.4
     */
    @Override
    public int nextSetBit(int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (fromIndex >= 32)
            return -1;

        int word = this.word.get() & (WORD_MASK << fromIndex);

        if (word != 0)
            return Integer.numberOfTrailingZeros(word);
        else
            return -1;
    }

    /**
     * Returns the index of the first bit that is set to {@code false}
     * that occurs on or after the specified starting index.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next clear bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since 1.4
     */
    @Override
    public int nextClearBit(int fromIndex) {
        // Neither spec nor implementation handle bitsets of maximal length.
        // See 4816253.
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        if (fromIndex >= 32)
            return fromIndex;

        int word = ~this.word.get() & (WORD_MASK << fromIndex);

        if (word != 0)
            return Integer.numberOfTrailingZeros(word);
        else
            return 0;
    }

    /**
     * Returns the index of the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     * <p/>
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     * <p/>
     * <pre> {@code
     * for (int i = bs.length(); (i = bs.previousSetBit(i-1)) >= 0; ) {
     *     // operate on index i here
     * }}</pre>
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     * @since 1.7
     */
    @Override
    public int prevSetBit(int fromIndex) {
        if (fromIndex < 0) {
            return -1;
        }

        if (fromIndex >= 32)
            return length() - 1;

        int word = this.word.get() & (WORD_MASK >>> -(fromIndex + 1));

        if (word != 0)
            return BITS_PER_WORD - 1 - Integer.numberOfLeadingZeros(word);
        else
            return -1;
    }

    /**
     * Returns the index of the nearest bit that is set to {@code false}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous clear bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     * @since 1.7
     */
    @Override
    public int prevClearBit(int fromIndex) {
        if (fromIndex < 0) {
            return -1;
        }

        if (fromIndex >= 32)
            return fromIndex;

        int word = ~this.word.get() & (WORD_MASK >>> -(fromIndex + 1));

        if (word != 0)
            return BITS_PER_WORD - 1 - Integer.numberOfLeadingZeros(word);
        else
            return -1;
    }

    /**
     * Returns the "logical size" of this <code>BitSet</code>: the index of
     * the highest set bit in the <code>BitSet</code> plus one. Returns zero
     * if the <code>BitSet</code> contains no set bits.
     *
     * @return the logical size of this <code>BitSet</code>.
     * @since 1.2
     */
    public int length() {
        return (BITS_PER_WORD - Integer.numberOfLeadingZeros(word.get()));
    }

    /**
     * Returns true if this <code>BitSet</code> contains no bits that are set
     * to <code>true</code>.
     *
     * @return boolean indicating whether this <code>BitSet</code> is empty.
     * @since 1.4
     */
    @Override
    public boolean isEmpty() {
        return word.get() == 0;
    }

    /**
     * Returns the number of bits set to <tt>true</tt> in this
     * <code>BitSet</code>.
     *
     * @return the number of bits set to <tt>true</tt> in this
     *         <code>BitSet</code>.
     * @since 1.4
     */
    @Override
    public int cardinality() {
        return Integer.bitCount(word.get());
    }

    @Override
    public int hashCode() {
        int h = 1234;
        h ^= word.get();
        return (h >> 14) ^ h;
    }

    /**
     * Returns the number of bits of space actually in use by this
     * <code>BitSet</code> to represent bit values.
     * The maximum element in the set is the size - 1st element.
     *
     * @return the number of bits currently in this bit set.
     */
    @Override
    public int size() {
        return BITS_PER_WORD;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OneWordS32BitSet))
            return false;
        if (this == obj)
            return true;

        OneWordS32BitSet set = (OneWordS32BitSet) obj;

        // Check word in use by both BitSets
        return word == set.word;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(6 * BITS_PER_WORD + 2);
        b.append('{');

        int i = nextSetBit(0);
        if (i != -1) {
            b.append(i);
            for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
                int endOfRun = nextClearBit(i);
                do {
                    b.append(", ").append(i);
                }
                while (++i < endOfRun);
            }
        }

        b.append('}');
        return b.toString();
    }
}
