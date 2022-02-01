/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl.siglit;

import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * TODO: obfuscate calls to ImplicationGraph and domains
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/07/2020
 */
public interface SignedLiteral {

    /**
     * Add {@code value} to {@code this}.
     *
     * @param value an int
     * @implNote Addition is restricted to the values present in the initial domain.
     */
    void add(int value);

    /**
     * Add all values between [{@code lower},{@code upper}] into {@code this}.
     * <p>
     * {@code lower} &le; {@code upper}
     * </p>
     *
     * @param lower an int
     * @param upper an int
     * @implNote Addition is restricted to the values present in the initial domain.
     */
    void addBetween(int lower, int upper);

    /**
     * Add all values from {@code set} to {@code this}
     *
     * @param set a set of ints
     * @implNote Addition is restricted to the values present in the initial domain.
     */
    void addAll(IntIterableRangeSet set);

    /**
     * Retain {@code value} from {@code this}.
     * <p>
     * May become empty as a result.
     * </p>
     *
     * @param value an int
     * @implNote Retention is restricted to the values present in the initial domain.
     */
    void retain(int value);

    /**
     * Remove all values not between [{@code lower},{@code upper}] from {@code this}.
     * <p>
     * May become empty as a result.
     * </p>
     * <p>
     * {@code lower} &le; {@code upper}
     * </p>
     *
     * @param lower an int
     * @param upper an int
     * @implNote Retention is restricted to the values present in the initial domain.
     */
    void retainBetween(int lower, int upper);

    /**
     * Remove all values not in {@code set} from {@code this}.
     * <p>
     * May become empty as a result.
     * </p>
     *
     * @param set a set of ints
     * @implNote Retention is restricted to the values present in the initial domain.
     */
    void retainAll(IntIterableRangeSet set);

    /**
     * Remove all values in {@code this}
     */
    void clear();

    /**
     * Turn {@code this} into an {@link IntIterableRangeSet}
     *
     * @return a new set of ranges
     */
    IntIterableRangeSet export();

    /**
     * @return {@code true} if {@code this} is empty, {@code false} otherwise.
     */
    boolean isEmpty();

    /**
     * @return {@code true} if the intersection between {@code this} and {@code set} is empty,
     * {@code false} otherwise.
     */
    boolean disjoint(IntIterableRangeSet set);

    /**
     * Implementation of the {@link SignedLiteral} interface dedicated to binaries.
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    class Boolean implements SignedLiteral {

        private static final byte bEmpty = 0b00;
        private static final byte bTrue = 0b01;
        private static final byte bFalse = 0b10;
        private static final byte bBoth = 0b11;

        byte lit;

        @Override
        public void add(int value) {
            byte add = bEmpty;
            switch (value) {
                case 0:
                    add = bFalse;
                    break;
                case 1:
                    add = bTrue;
                    break;
            }
            lit |= add;
        }

        @Override
        public void addBetween(int lower, int upper) {
            if (upper < 0 || lower > 1) return;
            if (lower <= 0) {
                lit |= bFalse;
            }
            if (1 <= upper) {
                lit |= bTrue;
            }
        }

        @Override
        public void addAll(IntIterableRangeSet set) {
            if (set.contains(0)) {
                add(0);
            }
            if (set.contains(1)) {
                add(1);
            }
        }

        @Override
        public void retain(int value) {
            byte keep = bEmpty;
            switch (value) {
                case 0:
                    keep = bFalse;
                    break;
                case 1:
                    keep = bTrue;
                    break;
            }
            lit &= keep;
        }

        @Override
        public void retainBetween(int lower, int upper) {
            if (upper < 0 || lower > 1) {
                lit = bEmpty;
            }
            if (lower > 0) {
                lit &= bTrue;
            }
            if (upper < 1) {
                lit &= bFalse;
            }
        }

        @Override
        public void retainAll(IntIterableRangeSet set) {
            int l = 1;
            int u = 0;
            if (set.contains(0)) {
                l = 0;
            }
            if (set.contains(1)) {
                u = 1;
            }
            retainBetween(l, u);
        }

        @Override
        public void clear() {
            lit = bEmpty;
        }

        @Override
        public boolean isEmpty() {
            return lit == bEmpty;
        }

        @Override
        public boolean disjoint(IntIterableRangeSet set) {
            return (lit == bTrue && !set.contains(1))
                    || (lit == bFalse && !set.contains(0));
        }

        @Override
        public IntIterableRangeSet export() {
            IntIterableRangeSet set = new IntIterableRangeSet();
            switch (lit) {
                case bBoth:
                    set.addBetween(0, 1);
                    break;
                case bTrue:
                    set.add(1);
                    break;
                case bFalse:
                    set.add(0);
                    break;
            }
            return set;
        }

        @Override
        public String toString() {
            switch (lit) {
                default:
                case bEmpty:
                    return "\u2205";
                case bFalse:
                    return "{0}";
                case bTrue:
                    return "{1}";
                case bBoth:
                    return "{0,1}";
            }
        }
    }

    /**
     * Implementation of the {@link SignedLiteral} interface dedicated to set of ints.
     */
    class Set implements SignedLiteral {

        IntIterableRangeSet root;
        IntIterableRangeSet lit;

        public Set(IntIterableRangeSet root) {
            this.root = root;
            this.lit = new IntIterableRangeSet();
        }

        @Override
        public void add(int value) {
            if (root.contains(value)) {
                lit.add(value);
            }
        }

        @Override
        public void addBetween(int lower, int upper) {
            if (root.intersect(lower, upper)) {
                lit.addBetween(lower, upper);
            }
        }

        @Override
        public void addAll(IntIterableRangeSet set) {
            if (root.intersect(set)) {
                lit.addAll(set);
            }
        }

        @Override
        public void retain(int value) {
            if (root.contains(value)) {
                lit.retainBetween(value, value);
            } else {
                lit.clear();
            }
        }

        @Override
        public void retainBetween(int lower, int upper) {
            if (root.intersect(lower, upper)) {
                lit.retainBetween(lower, upper);
            } else {
                lit.clear();
            }
        }

        @Override
        public void retainAll(IntIterableRangeSet set) {
            if (root.intersect(set)) {
                lit.retainAll(set);
            }else {
                lit.clear();
            }
        }

        @Override
        public void clear() {
            this.lit.clear();
        }

        @Override
        public IntIterableRangeSet export() {
            lit.retainAll(root);
            return lit.duplicate();
        }

        @Override
        public boolean isEmpty() {
            return lit.isEmpty();
        }

        @Override
        public boolean disjoint(IntIterableRangeSet set) {
            return !this.lit.intersect(set);
        }

        @Override
        public String toString() {
            return lit.toString();
        }
    }
}
