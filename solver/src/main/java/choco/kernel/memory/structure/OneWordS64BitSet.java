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

package choco.kernel.memory.structure;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateLong;

import java.lang.reflect.Array;
import java.util.BitSet;


public class OneWordS64BitSet implements IStateBitSet {

    /*
    * BitSets are packed into arrays of "word."  Currently a word is
    * a long, which consists of 64 bits, requiring 6 address bits.
    * The choice of word size is determined purely by performance concerns.
    */
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

    /* Used to shift left or right for a partial word mask */
    private static final long WORD_MASK = 0xffffffffffffffffL;

    /**
     * The current environment.
     */
    private final IEnvironment environment;

    /**
     * The internal field corresponding to the serialField "bits".
     */
    private IStateLong word;

    /**
     * Creates a new bit set. All bits are initially <code>false</code>.
     *
     * @param environment bactrackable environment
     */
    public OneWordS64BitSet(IEnvironment environment) {
        this.environment = environment;
        word = this.environment.makeLong(0);
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
    public OneWordS64BitSet(IEnvironment environment, int nbits) {
        this.environment = environment;
        // nbits can't be negative; size 0 is OK
        if (nbits < 0)
            throw new NegativeArraySizeException("nbits < 0: " + nbits);
        if (nbits > 64)
            throw new ArrayIndexOutOfBoundsException("nbits > 64: " + nbits);

        word = this.environment.makeLong(0);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        T[] copy = ((Object) newType == (Object) Object[].class)
                ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                Math.min(original.length, newLength));
        return copy;
    }

    public BitSet copyToBitSet() {
        BitSet view = new BitSet(this.size());
        for (int i = this.nextSetBit(0); i >= 0; i = this.nextSetBit(i + 1)) view.set(i, true);
        return view;
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

        long tmp = word.get();
        tmp ^= (1L << bitIndex);
        word.set(tmp);
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
        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        long tmp = word.get();
        tmp ^= (firstWordMask & lastWordMask);
        word.set(tmp);
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

        word.set(word.get() | (1L << bitIndex)); // Restores invariants
        //checkInvariants();
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

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        word.set(word.get() | (firstWordMask & lastWordMask));
        //checkInvariants();
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

        word.set(word.get() & ~(1L << bitIndex));
        //checkInvariants();
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

        long firstWordMask = WORD_MASK << fromIndex;
        long lastWordMask = WORD_MASK >>> -toIndex;
        word.set(word.get() & ~(firstWordMask & lastWordMask));
        //checkInvariants();
    }

    /**
     * Sets all of the bits in this BitSet to <code>false</code>.
     *
     * @since 1.4
     */
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
    final public boolean get(final int bitIndex) {
        //if (bitIndex < 0)
        //    throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        //checkInvariants();
        return ((word.get() & (1L << bitIndex)) != 0);
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
    public OneWordS64BitSet get(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the index of the first bit that is set to <code>true</code>
     * that occurs on or after the specified starting index. If no such
     * bit exists then -1 is returned.
     * <p/>
     * To iterate over the <code>true</code> bits in a <code>BitSet</code>,
     * use the following loop:
     * <p/>
     * <pre>
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     *     // operate on index i here
     * }</pre>
     *
     * @param fromIndex the index to start checking from (inclusive).
     * @return the index of the next set bit.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since 1.4
     */
    public int nextSetBit(int fromIndex) {
//        if (fromIndex < 0)
//            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

        //checkInvariants();

        long word = this.word.get() & (WORD_MASK << fromIndex);

        if (word != 0) {
            return Long.numberOfTrailingZeros(word);
        } else {
            return -1;
        }
    }

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


    public int prevSetBit(int fromIndex) {
//        if (fromIndex < 0)
//            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

        //checkInvariants();
        if (fromIndex > 64) {
            return length() - 1;
        }
        long mask = ~(WORD_MASK << fromIndex + 1);
        long word = this.word.get() & (mask != 0 ? mask : WORD_MASK);

        if (word != 0) {
            return BIT_INDEX_MASK - Long.numberOfLeadingZeros(word);
        } else {
            return -1;
        }
    }

    public int prevClearBit(int fromIndex) {
//        if (fromIndex < 0)
//            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
//
        //checkInvariants();
        if(fromIndex < 0){
            return -1;
        } else
        if (fromIndex > 64) {
            return fromIndex;
        }
        long mask = ~(WORD_MASK << fromIndex + 1);
        long word = ~this.word.get() & (mask != 0 ? mask : WORD_MASK);

        if (word != 0) {
            return BIT_INDEX_MASK - Long.numberOfLeadingZeros(word);
        } else {
            return -1;
        }
    }

    public int capacity() {
        return BITS_PER_WORD;
    }

    /**
     * Returns the index of the first bit that is set to <code>false</code>
     * that occurs on or after the specified starting index.
     *
     * @param fromIndex the index to start checking from (inclusive).
     * @return the index of the next clear bit.
     * @throws IndexOutOfBoundsException if the specified index is negative.
     * @since 1.4
     */
    public int nextClearBit(int fromIndex) {
        // Neither spec nor implementation handle bitsets of maximal length.
        // See 4816253.
//        if (fromIndex < 0)
//            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

        //checkInvariants();
        if (fromIndex > 64) {
            return fromIndex;
        }

        long word = ~this.word.get() & (WORD_MASK << fromIndex);

        if (word != 0)
            return Long.numberOfTrailingZeros(word);
        else
            return BITS_PER_WORD;
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
        return (BITS_PER_WORD - Long.numberOfLeadingZeros(word.get()));
    }

    /**
     * Returns true if this <code>BitSet</code> contains no bits that are set
     * to <code>true</code>.
     *
     * @return boolean indicating whether this <code>BitSet</code> is empty.
     * @since 1.4
     */
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
    public int cardinality() {
        return Long.bitCount(word.get());
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the specified <code>BitSet</code> has any bits set to
     * <code>true</code> that are also set to <code>true</code> in this
     * <code>BitSet</code>.
     *
     * @param setI <code>BitSet</code> to intersect with
     * @return boolean indicating whether this <code>BitSet</code> intersects
     *         the specified <code>BitSet</code>.
     * @since 1.4
     */
    public boolean intersects(IStateBitSet setI) {
        throw new UnsupportedOperationException();
    }

    public int hashCode() {
        long h = 1234;
        h ^= word.get();
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
        return BITS_PER_WORD;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OneWordS64BitSet))
            return false;
        if (this == obj)
            return true;

        OneWordS64BitSet set = (OneWordS64BitSet) obj;

        //checkInvariants();
        //set.checkInvariants();

        // Check word in use by both BitSets
        return word == set.word;
    }

    public IStateBitSet copy() {
        //if (!sizeIsSticky.get()) trimToSize();
        OneWordS64BitSet result = new OneWordS64BitSet(environment, this.size());
        //result.sizeIsSticky.set(sizeIsSticky.get());
        result.word.set(word.get());
        //result.checkInvariants();
        return result;
    }

    public String toString() {
        //checkInvariants();

        int numBits = BITS_PER_WORD;
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