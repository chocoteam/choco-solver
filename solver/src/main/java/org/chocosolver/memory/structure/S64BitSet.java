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
import org.chocosolver.memory.IStateLong;

import java.lang.reflect.Array;


public class S64BitSet implements IStateBitSet {

    private final static boolean CHECK = false;

    /*
    * BitSets are packed into arrays of "words."  Currently a word is
    * a long, which consists of 64 bits, requiring 6 address bits.
    * The choice of word size is determined purely by performance concerns.
    */
    private final static int ADDRESS_BITS_PER_WORD = 6;
    protected final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;

    /* Used to shift left or right for a partial word mask */
    protected static final long WORD_MASK = 0xffffffffffffffffL;

    /**
     * The current environment.
     */
    private final IEnvironment environment;

    /**
     * The internal field corresponding to the serialField "bits".
     */
    protected IStateLong[] words;

    /**
     * The number of words in the logical size of this BitSet.
     */
    protected final IStateInt wordsInUse;


    /**
     * Given a bit index, return word index containing it.
     *
     * @param bitIndex bit index
     */
    protected static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    /**
     * Every public method must preserve these invariants.
     */
    private void checkInvariants() {
        assert (wordsInUse.get() == 0 || words[wordsInUse.get() - 1].get() != 0);
        assert (wordsInUse.get() >= 0 && wordsInUse.get() <= words.length);
        assert (wordsInUse.get() == words.length || words[wordsInUse.get()].get() == 0);
    }

    /**
     * Set the field wordsInUse with the logical size in words of the bit
     * set.  WARNING:This method assumes that the number of words actually
     * in use is less than or equal to the current value of wordsInUse!
     */
    private void recalculateWordsInUse() {
        // Traverse the bitset until a used word is found
        int i;
        int n = wordsInUse.get();
        for (i = n - 1; i >= 0; i--)
            if (words[i].get() != 0)
                break;
        if (i + 1 < n) {
            wordsInUse.set(i + 1); // The new logical size
        }
    }

    /**
     * Creates a new bit set. All bits are initially <code>false</code>.
     *
     * @param environment bactrackable environment
     */
    public S64BitSet(IEnvironment environment) {
        this.environment = environment;
        this.wordsInUse = environment.makeInt(0);
        initWords(BITS_PER_WORD);
    }

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
    public S64BitSet(IEnvironment environment, int nbits) {
        this.environment = environment;
        this.wordsInUse = environment.makeInt(0);
        // nbits can't be negative; size 0 is OK
        if (nbits < 0)
            throw new NegativeArraySizeException("nbits < 0: " + nbits);

        initWords(nbits);
    }

    private void initWords(int nbits) {
        words = new IStateLong[wordIndex(nbits - 1) + 1];
        for (int i = 0; i < words.length; i++) words[i] = this.environment.makeLong(0);
        if (CHECK) checkInvariants();
    }


    /**
     * Ensures that the BitSet can hold enough words.
     *
     * @param wordsRequired the minimum acceptable number of words.
     */
    public void ensureCapacity(int wordsRequired) {
        if (words.length < wordsRequired) {
            // Allocate larger of doubled size or required size
            int request = Math.max(2 * words.length, wordsRequired);
            int oldSize = words.length;
            words = copyOf(words, request);
            for (int i = oldSize; i < request; i++) {
                words[i] = environment.makeLong(0);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy", "RedundantCast"})
    public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        T[] copy = ((Object) newType == (Object) Object[].class)
                ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                Math.min(original.length, newLength));
        return copy;
    }

    /**
     * Ensures that the BitSet can accommodate a given wordIndex,
     * temporarily violating the invariants.  The caller must
     * restore the invariants before returning to the user,
     * possibly using recalculateWordsInUse().
     *
     * @param wordIndex the index to be accommodated.
     */
    private void expandTo(int wordIndex) {
        int wordsRequired = wordIndex + 1;
        if (wordsInUse.get() < wordsRequired) {
            ensureCapacity(wordsRequired);
            wordsInUse.set(wordsRequired);
        }
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
    public void set(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        if (CHECK) checkInvariants();
        int wordIndex = wordIndex(bitIndex);
        expandTo(wordIndex);

        words[wordIndex].set(words[wordIndex].get() | (1L << bitIndex)); // Restores invariants

        if (CHECK) checkInvariants();
    }

    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param bitIndex a bit index.
     * @param value    a boolean value to set.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since 1.4
     */
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
    public void set(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex)
            return;

        // Increase capacity if necessary
        int startWordIndex = wordIndex(fromIndex);
        int endWordIndex = wordIndex(toIndex - 1);
        expandTo(endWordIndex);

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // Case 1: One word
            words[startWordIndex].set(words[startWordIndex].get() | (firstWordMask & lastWordMask));
        } else {
            // Case 2: Multiple words
            // Handle first word
            words[startWordIndex].set(words[startWordIndex].get() | firstWordMask);

            // Handle intermediate words, if any
            for (int i = startWordIndex + 1; i < endWordIndex; i++)
                words[i].set(WORD_MASK);

            // Handle last word (restores invariants)
            words[endWordIndex].set(words[endWordIndex].get() | lastWordMask);
        }

        if (CHECK) checkInvariants();
    }

    /**
     * Sets the bit specified by the index to <code>false</code>.
     *
     * @param bitIndex the index of the bit to be cleared.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since JDK1.0
     */
    public void clear(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex);
        int n = wordsInUse.get();
        if (wordIndex >= n)
            return;

        words[wordIndex].set(words[wordIndex].get() & ~(1L << bitIndex));

        //if(wordIndex == n-1)
        recalculateWordsInUse();
        if (CHECK) checkInvariants();
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
    public void clear(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex)
            return;

        int wiu = wordsInUse.get();
        int startWordIndex = wordIndex(fromIndex);
        if (startWordIndex >= wiu)
            return;

        int endWordIndex = wordIndex(toIndex - 1);
        if (endWordIndex >= wiu) {
            toIndex = length();
            endWordIndex = wiu - 1;
        }

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // Case 1: One word
            words[startWordIndex].set(words[startWordIndex].get() & ~(firstWordMask & lastWordMask));
        } else {
            // Case 2: Multiple words
            // Handle first word
            words[startWordIndex].set(words[startWordIndex].get() & ~firstWordMask);

            // Handle intermediate words, if any
            for (int i = startWordIndex + 1; i < endWordIndex; i++)
                words[i].set(0);

            // Handle last word
            words[endWordIndex].set(words[endWordIndex].get() & ~lastWordMask);
        }

        //if(endWordIndex < wiu)
        recalculateWordsInUse();
        if (CHECK) checkInvariants();
    }

    /**
     * Sets all of the bits in this BitSet to <code>false</code>.
     *
     * @since 1.4
     */
    public void clear() {
        /*while (wordsInUse.get() > 0)
            wordsInUse.set(wordsInUse.get() - 1);
        words[wordsInUse.get()].set(0);      */
        for (IStateLong word : words) {
            word.set(0);
        }
        wordsInUse.set(0);
        if (CHECK) checkInvariants();
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
    final public boolean get(final int bitIndex) {
        //if (bitIndex < 0)
        //    throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        if (CHECK) checkInvariants();

        int wordIndex = bitIndex >> ADDRESS_BITS_PER_WORD; //wordIndex(bitIndex);
        return (wordIndex < wordsInUse.get())
                && ((words[wordIndex].get() & (1L << bitIndex)) != 0);
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
    public int nextSetBit(int fromIndex) {
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        int wiu = wordsInUse.get();
        int u = wordIndex(fromIndex);
        if (u >= wiu)
            return -1;

        long word = words[u].get() & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            if (++u == wiu)
                return -1;
            word = words[u].get();
        }
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
    public int nextClearBit(int fromIndex) {
        // Neither spec nor implementation handle bitsets of maximal length.
        // See 4816253.
        if (fromIndex < 0) {
            fromIndex = 0;
        }

        int wiu = wordsInUse.get();
        int u = wordIndex(fromIndex);
        if (u >= wiu)
            return fromIndex;

        long word = ~words[u].get() & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            if (++u == wiu)
                return wiu * BITS_PER_WORD;
            word = ~words[u].get();
        }
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
    public int prevSetBit(int fromIndex) {
        if (fromIndex < 0) {
            return -1;
        }

        int u = wordIndex(fromIndex);
        if (u >= wordsInUse.get())
            return length() - 1;

        long word = words[u].get() & (WORD_MASK >>> -(fromIndex + 1));

        while (true) {
            if (word != 0)
                return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            if (u-- == 0)
                return -1;
            word = words[u].get();
        }
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
    public int prevClearBit(int fromIndex) {
        if (fromIndex < 0) {
            return -1;
        }

        int u = wordIndex(fromIndex);
        if (u >= wordsInUse.get())
            return fromIndex;

        long word = ~words[u].get() & (WORD_MASK >>> -(fromIndex + 1));

        while (true) {
            if (word != 0)
                return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            if (u-- == 0)
                return -1;
            word = ~words[u].get();
        }
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
        int wiu = wordsInUse.get();
        if (wiu == 0)
            return 0;

        return BITS_PER_WORD * (wiu - 1) +
                (BITS_PER_WORD - Long.numberOfLeadingZeros(words[wiu - 1].get()));
    }

    /**
     * Returns true if this <code>BitSet</code> contains no bits that are set
     * to <code>true</code>.
     *
     * @return boolean indicating whether this <code>BitSet</code> is empty.
     * @since 1.4
     */
    public boolean isEmpty() {
        return wordsInUse.get() == 0;
    }

    /**
     * Returns the number of bits set to <tt>true</tt> in this
     * <code>BitSet</code>.
     *
     * @return the number of bits set to <tt>true</tt> in this
     *         <code>BitSet</code>.
     * @since 1.4
     */
    public int cardinality() {
        int sum = 0;
        for (int i = wordsInUse.get() - 1; i >= 0; i--)
            sum += Long.bitCount(words[i].get());
        return sum;
    }

    public int hashCode() {
        long h = 1234;
        for (int i = wordsInUse.get(); --i >= 0; )
            h ^= words[i].get() * (i + 1);

        return (int) ((h >> 32) ^ h);
    }

    /**
     * Returns the number of bits of space actually in use by this
     * <code>BitSet</code> to represent bit values.
     * The maximum element in the set is the size - 1st element.
     *
     * @return the number of bits currently in this bit set.
     */
    public int size() {
        return words.length * BITS_PER_WORD;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof S64BitSet))
            return false;
        if (this == obj)
            return true;

        S64BitSet set = (S64BitSet) obj;

        if (CHECK) checkInvariants();
        if (CHECK) set.checkInvariants();

        if (wordsInUse != set.wordsInUse)
            return false;

        // Check words in use by both BitSets
        for (int i = 0; i < wordsInUse.get(); i++)
            if (words[i] != set.words[i])
                return false;

        return true;
    }

    public String toString() {
        if (CHECK) checkInvariants();

        int numBits = (wordsInUse.get() > 128) ?
                cardinality() : wordsInUse.get() * BITS_PER_WORD;
        StringBuilder b = new StringBuilder(6 * numBits + 2);
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
