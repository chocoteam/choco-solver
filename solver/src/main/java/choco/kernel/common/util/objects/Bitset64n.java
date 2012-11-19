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
package choco.kernel.common.util.objects;

import choco.kernel.memory.IStateBitSet;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A bit set based on "n" longs.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/11/12
 */
public class Bitset64n implements IBitset {

    private final static boolean CHECK = false;

    /*
    * BitSets are packed into arrays of "words."  Currently a word is
    * a long, which consists of 64 bits, requiring 6 address bits.
    * The choice of word size is determined purely by performance concerns.
    */
    private final static int ADDRESS_BITS_PER_WORD = 6;
    protected final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

    /* Used to shift left or right for a partial word mask */
    protected static final long WORD_MASK = 0xffffffffffffffffL;

    /**
     * The internal field corresponding to the serialField "bits".
     */
    protected long[] words;

    /**
     * The number of words in the logical size of this BitSet.
     */
    protected int wordsInUse;


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
        assert (wordsInUse == 0 || words[wordsInUse - 1] != 0);
        assert (wordsInUse >= 0 && wordsInUse <= words.length);
        assert (wordsInUse == words.length || words[wordsInUse] == 0);
    }

    /**
     * Set the field wordsInUse with the logical size in words of the bit
     * set.  WARNING:This method assumes that the number of words actually
     * in use is less than or equal to the current value of wordsInUse!
     */
    private void recalculateWordsInUse() {
        // Traverse the bitset until a used word is found
        int i;
        int n = wordsInUse;
        for (i = n - 1; i >= 0; i--)
            if (words[i] != 0)
                break;
        if (i + 1 < n) {
            wordsInUse = i + 1; // The new logical size
        }
    }

    /**
     * Creates a new bit set. All bits are initially <code>false</code>.
     */
    public Bitset64n() {
        this.wordsInUse = 0;
        initWords(BITS_PER_WORD);
    }

    /**
     * Creates a bit set whose initial size is large enough to explicitly
     * represent bits with indices in the range <code>0</code> through
     * <code>nbits-1</code>. All bits are initially <code>false</code>.
     *
     * @param nbits the initial size of the bit set.
     * @throws NegativeArraySizeException if the specified initial size
     *                                    is negative.
     */
    public Bitset64n(int nbits) {
        this.wordsInUse = 0;
        // nbits can't be negative; size 0 is OK
        if (nbits < 0)
            throw new NegativeArraySizeException("nbits < 0: " + nbits);

        initWords(nbits);
    }

    private void initWords(int nbits) {
        words = new long[wordIndex(nbits - 1) + 1];
        for (int i = 0; i < words.length; i++) words[i] = 0L;
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
            words = Arrays.copyOf(words, request);
            for (int i = oldSize; i < request; i++) {
                words[i] = 0L;
            }
        }
    }

    public BitSet copyToBitSet() {
        BitSet view = new BitSet(this.size());
        for (int i = this.nextSetBit(0); i >= 0; i = this.nextSetBit(i + 1)) view.set(i, true);
        return view;
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
        if (wordsInUse < wordsRequired) {
            ensureCapacity(wordsRequired);
            wordsInUse = wordsRequired;
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
     * Sets the bit at the specified index to the complement of its
     * current value.
     *
     * @param bitIndex the index of the bit to flip.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since 1.4
     */
    public void flip(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex);
        expandTo(wordIndex);

        long tmp = words[wordIndex];
        tmp ^= (1L << bitIndex);
        words[wordIndex] = tmp;

        recalculateWordsInUse();
        if (CHECK) checkInvariants();
    }

    /**
     * Sets each bit from the specified <tt>fromIndex</tt> (inclusive) to the
     * specified <tt>toIndex</tt> (exclusive) to the complement of its current
     * value.
     *
     * @param fromIndex index of the first bit to flip.
     * @param toIndex   index after the last bit to flip.
     * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
     *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
     *                                   larger than <tt>toIndex</tt>.
     * @since 1.4
     */
    public void flip(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (fromIndex == toIndex)
            return;

        int startWordIndex = wordIndex(fromIndex);
        int endWordIndex = wordIndex(toIndex - 1);
        expandTo(endWordIndex);

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            // Case 1: One word
            long tmp = words[startWordIndex];
            tmp ^= (firstWordMask & lastWordMask);
            words[startWordIndex] = tmp;
        } else {
            // Case 2: Multiple words
            // Handle first word
            words[startWordIndex] = words[startWordIndex] ^ firstWordMask;

            // Handle intermediate words, if any
            for (int i = startWordIndex + 1; i < endWordIndex; i++)
                words[i] = ~words[i];

            // Handle last word
            words[endWordIndex] = words[endWordIndex] ^ lastWordMask;
        }

        recalculateWordsInUse();
        if (CHECK) checkInvariants();
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

        words[wordIndex] = words[wordIndex] | (1L << bitIndex); // Restores invariants

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
            words[startWordIndex] = words[startWordIndex] | (firstWordMask & lastWordMask);
        } else {
            // Case 2: Multiple words
            // Handle first word
            words[startWordIndex] = words[startWordIndex] | firstWordMask;

            // Handle intermediate words, if any
            for (int i = startWordIndex + 1; i < endWordIndex; i++)
                words[i] = WORD_MASK;

            // Handle last word (restores invariants)
            words[endWordIndex] = words[endWordIndex] | lastWordMask;
        }

        if (CHECK) checkInvariants();
    }

    /**
     * Sets the bits from the specified <tt>fromIndex</tt> (inclusive) to the
     * specified <tt>toIndex</tt> (exclusive) to the specified value.
     *
     * @param fromIndex index of the first bit to be set.
     * @param toIndex   index after the last bit to be set
     * @param value     value to set the selected bits to
     * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
     *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
     *                                   larger than <tt>toIndex</tt>.
     * @since 1.4
     */
    public void set(int fromIndex, int toIndex, boolean value) {
        if (value)
            set(fromIndex, toIndex);
        else
            clear(fromIndex, toIndex);
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
        int n = wordsInUse;
        if (wordIndex >= n)
            return;

        words[wordIndex] = words[wordIndex] & ~(1L << bitIndex);

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

        int wiu = wordsInUse;
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
            words[startWordIndex] = words[startWordIndex] & ~(firstWordMask & lastWordMask);
        } else {
            // Case 2: Multiple words
            // Handle first word
            words[startWordIndex] = words[startWordIndex] & ~firstWordMask;

            // Handle intermediate words, if any
            for (int i = startWordIndex + 1; i < endWordIndex; i++)
                words[i] = 0;

            // Handle last word
            words[endWordIndex] = words[endWordIndex] & ~lastWordMask;
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
        Arrays.fill(words,0);
        wordsInUse = 0;
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
        return (wordIndex < wordsInUse)
                && ((words[wordIndex] & (1L << bitIndex)) != 0);
    }

    /**
     * Returns a new <tt>BitSet</tt> composed of bits from this <tt>BitSet</tt>
     * from <tt>fromIndex</tt> (inclusive) to <tt>toIndex</tt> (exclusive).
     *
     * @param fromIndex index of the first bit to include.
     * @param toIndex   index after the last bit to include.
     * @return a new <tt>BitSet</tt> from a range of this <tt>BitSet</tt>.
     * @throws IndexOutOfBoundsException if <tt>fromIndex</tt> is negative,
     *                                   or <tt>toIndex</tt> is negative, or <tt>fromIndex</tt> is
     *                                   larger than <tt>toIndex</tt>.
     * @since 1.4
     */
    public Bitset64n get(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);

        if (CHECK) checkInvariants();

        int len = length();

        // If no set bits in range return empty bitset
        if (len <= fromIndex || fromIndex == toIndex)
            return new Bitset64n(0);

        // An optimization
        if (toIndex > len)
            toIndex = len;

        Bitset64n result = new Bitset64n(toIndex - fromIndex);
        int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
        int sourceIndex = wordIndex(fromIndex);
        boolean wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0);

        // Process all words but the last word
        for (int i = 0; i < targetWords - 1; i++, sourceIndex++)
            words[i] = wordAligned ? words[sourceIndex] :
                    (words[sourceIndex] >>> fromIndex) |
                            (words[sourceIndex + 1] << -fromIndex);

        // Process the last word
        long lastWordMask = WORD_MASK >>> -toIndex;
        words[targetWords - 1] = ((toIndex - 1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)
                ? /* straddles source words */
                ((words[sourceIndex] >>> fromIndex) |
                        (words[sourceIndex + 1] & lastWordMask) << -fromIndex)
                :
                ((words[sourceIndex] & lastWordMask) >>> fromIndex);

        // Set wordsInUse correctly
        wordsInUse = targetWords;
        result.recalculateWordsInUse();
        if (CHECK) result.checkInvariants();

        return result;
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

        int wiu = wordsInUse;
        int u = wordIndex(fromIndex);
        if (u >= wiu)
            return -1;

        long word = words[u] & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            if (++u == wiu)
                return -1;
            word = words[u];
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

        int wiu = wordsInUse;
        int u = wordIndex(fromIndex);
        if (u >= wiu)
            return fromIndex;

        long word = ~words[u] & (WORD_MASK << fromIndex);

        while (true) {
            if (word != 0)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            if (++u == wiu)
                return wiu * BITS_PER_WORD;
            word = ~words[u];
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
        if (u >= wordsInUse)
            return length() - 1;

        long word = words[u] & (WORD_MASK >>> -(fromIndex + 1));

        while (true) {
            if (word != 0)
                return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            if (u-- == 0)
                return -1;
            word = words[u];
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
        if (u >= wordsInUse)
            return fromIndex;

        long word = ~words[u] & (WORD_MASK >>> -(fromIndex + 1));

        while (true) {
            if (word != 0)
                return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            if (u-- == 0)
                return -1;
            word = ~words[u];
        }
    }

    public int capacity() {
        return words.length * BITS_PER_WORD;
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
        int wiu = wordsInUse;
        if (wiu == 0)
            return 0;

        return BITS_PER_WORD * (wiu - 1) +
                (BITS_PER_WORD - Long.numberOfLeadingZeros(words[wiu - 1]));
    }

    /**
     * Returns true if this <code>BitSet</code> contains no bits that are set
     * to <code>true</code>.
     *
     * @return boolean indicating whether this <code>BitSet</code> is empty.
     * @since 1.4
     */
    public boolean isEmpty() {
        return wordsInUse == 0;
    }

    /**
     * Returns true if the specified <code>BitSet</code> has any bits set to
     * <code>true</code> that are also set to <code>true</code> in this
     * <code>BitSet</code>.
     *
     * @param set <code>BitSet</code> to intersect with
     * @return boolean indicating whether this <code>BitSet</code> intersects
     *         the specified <code>BitSet</code>.
     * @since 1.4
     */
    public boolean intersects(IBitset set) {
        for (int i = Math.min(wordsInUse, wordsInUse) - 1; i >= 0; i--)
            if ((words[i] & words[i]) != 0)
                return true;
        return false;
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
        for (int i = wordsInUse - 1; i >= 0; i--)
            sum += Long.bitCount(words[i]);
        return sum;
    }

    /**
     * Performs a logical <b>AND</b> of this target bit set with the
     * argument bit set. This bit set is modified so that each bit in it
     * has the value <code>true</code> if and only if it both initially
     * had the value <code>true</code> and the corresponding bit in the
     * bit set argument also had the value <code>true</code>.
     *
     * @param setI a bit set.
     */
    public void and(IStateBitSet setI) {
        Bitset64n set = (Bitset64n) setI;
        if (this == set)
            return;

        while (wordsInUse > wordsInUse) {
            wordsInUse--;
            words[wordsInUse] = 0;
        }

        // Perform logical AND on words in common
        for (int i = 0; i < wordsInUse; i++)
            words[i] = words[i] & words[i];

        recalculateWordsInUse();
        if (CHECK) checkInvariants();
    }

    /**
     * Performs a logical <b>OR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value <code>true</code> if and only if it either already had the
     * value <code>true</code> or the corresponding bit in the bit set
     * argument has the value <code>true</code>.
     *
     * @param setI a bit set.
     */
    public void or(IStateBitSet setI) {
        Bitset64n set = (Bitset64n) setI;
        if (this == set)
            return;

        int wordsInCommon = Math.min(wordsInUse, wordsInUse);

        if (wordsInUse < wordsInUse) {
            ensureCapacity(wordsInUse);
            wordsInUse = wordsInUse;
        }

        // Perform logical OR on words in common
        for (int i = 0; i < wordsInCommon; i++)
            words[i] = words[i] | words[i];

        // Copy any remaining words
        if (wordsInCommon < wordsInUse)
            System.arraycopy(set.words, wordsInCommon,
                    words, wordsInCommon,
                    wordsInUse - wordsInCommon);

        // recalculateWordsInUse() is unnecessary
        if (CHECK) checkInvariants();
    }

    /**
     * Performs a logical <b>XOR</b> of this bit set with the bit set
     * argument. This bit set is modified so that a bit in it has the
     * value <code>true</code> if and only if one of the following
     * statements holds:
     * <ul>
     * <li>The bit initially has the value <code>true</code>, and the
     * corresponding bit in the argument has the value <code>false</code>.
     * <li>The bit initially has the value <code>false</code>, and the
     * corresponding bit in the argument has the value <code>true</code>.
     * </ul>
     *
     * @param setI a bit set.
     */
    public void xor(IStateBitSet setI) {
        Bitset64n set = (Bitset64n) setI;
        int wordsInCommon = Math.min(wordsInUse, wordsInUse);

        if (wordsInUse < wordsInUse) {
            ensureCapacity(wordsInUse);
            wordsInUse = wordsInUse;
        }

        // Perform logical XOR on words in common
        for (int i = 0; i < wordsInCommon; i++)
            words[i] = words[i] ^ words[i];

        // Copy any remaining words
        if (wordsInCommon < wordsInUse)
            System.arraycopy(set.words, wordsInCommon,
                    words, wordsInCommon,
                    wordsInUse - wordsInCommon);

        recalculateWordsInUse();
        if (CHECK) checkInvariants();
    }

    /**
     * Clears all of the bits in this <code>BitSet</code> whose corresponding
     * bit is set in the specified <code>BitSet</code>.
     *
     * @param setI the <code>BitSet</code> with which to mask this
     *             <code>BitSet</code>.
     * @since 1.2
     */
    public void andNot(IStateBitSet setI) {
        Bitset64n set = (Bitset64n) setI;
        // Perform logical (a & !b) on words in common
        for (int i = Math.min(wordsInUse, wordsInUse) - 1; i >= 0; i--)
            words[i] = words[i] & ~words[i];

        recalculateWordsInUse();
        if (CHECK) checkInvariants();
    }

    public int hashCode() {
        long h = 1234;
        for (int i = wordsInUse; --i >= 0; )
            h ^= words[i] * (i + 1);

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
        if (!(obj instanceof Bitset64n))
            return false;
        if (this == obj)
            return true;

        Bitset64n set = (Bitset64n) obj;

        if (CHECK) checkInvariants();
        if (CHECK) set.checkInvariants();

        if (wordsInUse != set.wordsInUse)
            return false;

        // Check words in use by both BitSets
        for (int i = 0; i < wordsInUse; i++)
            if (words[i] != set.words[i])
                return false;

        return true;
    }

    public IBitset copy() {
        //if (!sizeIsSticky.get()) trimToSize();
        Bitset64n result = new Bitset64n(this.size());
        wordsInUse = wordsInUse;
        //result.sizeIsSticky.set(sizeIsSticky.get());
        for (int i = 0; i < wordsInUse; i++) {
            words[i] = words[i];
        }
        if (CHECK) result.checkInvariants();
        return result;
    }

    public String toString() {
        if (CHECK) checkInvariants();

        int numBits = (wordsInUse > 128) ?
                cardinality() : wordsInUse * BITS_PER_WORD;
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
