/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.iterable;


import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.learn.XParameters;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableRangeIterator;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Arrays;
import java.util.function.IntConsumer;

/**
 * Concrete implementation of {@link IntIterableSet} wherein values are stored in range set.
 * A range is made of two ints, the lower bound and the upper bound of the range.
 * A range can be a singleton, in that case, the lb and the ub are equal.
 * If the upper bound of range A is equal to lower bound of range B, then the two ranges can be merged into a single one.
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 14/01/2016.
 */
public class IntIterableRangeSet implements IntIterableSet {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************
    public static final int MIN = Integer.MAX_VALUE / -2;
    public static final int MAX = Integer.MAX_VALUE / 2;
    /**
     * Store elements
     */
    protected int[] ELEMENTS;

    /**
     * Used size in {@link #ELEMENTS}.
     * To get the number of range simply divide by 2.
     */
    protected int SIZE;

    /**
     * Total number of elements in the set
     */
    protected int CARDINALITY;

    /**
     * Create an ISet iterator
     */
    private ISetIterator iter;

    /**
     * Makes this structure immutable
     */
    private boolean lock;

    /**
     * Every public method must preserve these invariants.
     */
    private void checkInvariants() {
        assert SIZE <= ELEMENTS.length;
        assert (SIZE & 1) == 0; // is even
        assert CARDINALITY >= 0;
    }

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create an interval-based ordered set
     */
    public IntIterableRangeSet() {
        ELEMENTS = new int[10];
        SIZE = 0;
        CARDINALITY = 0;
    }

    /**
     * Create an interval-based ordered set initialized to [a,b]
     *
     * @param a lower bound of the interval
     * @param b upper bound of the interval
     */
    public IntIterableRangeSet(int a, int b) {
        if (a > b) {
            throw new IndexOutOfBoundsException("Incorrect bounds [" + a + "," + b + "]");
        }
        ELEMENTS = new int[10];
        SIZE = 2;
        CARDINALITY = Math.addExact(b + 1, -a);
        ELEMENTS[0] = a;
        ELEMENTS[1] = b;
    }

    /**
     * Create an interval-based ordered set initialized to singleton {e}
     *
     * @param e singleton value
     */
    public IntIterableRangeSet(int e) {
        ELEMENTS = new int[10];
        SIZE = 2;
        CARDINALITY = 1;
        ELEMENTS[0] = ELEMENTS[1] = e;
    }

    /**
     * Create an interval-based ordered set initialized to an array of values
     *
     * @param values some values
     */
    public IntIterableRangeSet(int[] values) {
        this();
        addAll(values);
    }

    /**
     * Create an interval-based ordered set initialized to an array of values
     *
     * @param var an integer variable
     */
    public IntIterableRangeSet(IntVar var) {
        this();
        copyFrom(var);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        if (SIZE == 0) {
            st.append('\u2205');
        } else {
            for (int i = 0; i < SIZE - 1; i += 2) {
                if (i == 0 && ELEMENTS[i] == MIN) {
                    st.append('(').append("-∞");
                } else {
                    st.append('[').append(ELEMENTS[i]);
                }
                st.append(',');
                if (ELEMENTS[i + 1] == MAX) {
                    st.append("+∞").append(")∪");
                } else {
                    st.append(ELEMENTS[i + 1]).append("]∪");
                }
            }
            st.deleteCharAt(st.length() - 1);
        }
        return st.toString();
    }

    public String toSmartString() {
        StringBuilder st = new StringBuilder();
        st.append("{");
        for (int i = 0; i < SIZE - 1; i += 2) {
            if (ELEMENTS[i] == ELEMENTS[i + 1]) {
                st.append(ELEMENTS[i]).append(',');
            } else {
                st.append(ELEMENTS[i]).append("..").append(ELEMENTS[i + 1]).append(',');
            }
        }
        if (SIZE > 0) st.deleteCharAt(st.length() - 1);
        st.append("}");
        return st.toString();
    }

    public void lock() {
        //noinspection PointlessBooleanExpression
        this.lock = true && XParameters.ALLOW_LOCK;
    }

    public void unlock() {
        this.lock = false;
    }

    /**
     * @return number of ranges in this
     */
    public int getNbRanges() {
        return SIZE >> 1;
    }

    public int cardinality() {
        return CARDINALITY;
    }

    public int minOfRange(int r) {
        return ELEMENTS[r << 1];
    }

    public int maxOfRange(int r) {
        return ELEMENTS[(r << 1) + 1];
    }


    @Override
    public int min() {
        if (isEmpty()) {
            throw new IllegalStateException("cannot find minimum of an empty set");
        }
        return ELEMENTS[0];
    }

    @Override
    public int max() {
        if (isEmpty()) throw new IllegalStateException("cannot find maximum of an empty set");
        return ELEMENTS[SIZE - 1];
    }

    @Override
    public boolean add(int e) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        boolean modified = false;
        int p = rangeOf(e);
        // if e is not in a range
        if (p < 0) {
            grow(SIZE + 2);
            int i = (-p - 1) << 1;
            //if (i > 0) {
            int c = i > 0 && ELEMENTS[i - 1] + 1 == e ? 1 : 0;
            c += i < SIZE && e == ELEMENTS[i] - 1 ? 2 : 0;
            switch (c) {
                case 0:
                    // insert a new range
                    System.arraycopy(ELEMENTS, i, ELEMENTS, i + 2, SIZE - i);
                    ELEMENTS[i] = ELEMENTS[i + 1] = e;
                    SIZE += 2;
                    break;
                case 1:
                    // e is the new lower bound
                    assert ELEMENTS[i - 1] + 1 == e;
                    ELEMENTS[i - 1] = e;
                    break;
                case 2:
                    //  e is the new upper bound
                    assert ELEMENTS[i] - 1 == e;
                    ELEMENTS[i] = e;
                    break;
                case 3:
                    // merge two ranges
                    System.arraycopy(ELEMENTS, i + 1, ELEMENTS, i - 1, SIZE - i);
                    SIZE -= 2;
                    break;
                default:
                    throw new SolverException("Unexpected mask " + c);
            }
            modified = true;
            CARDINALITY++;
        }
        return modified;
    }

    @Override
    public boolean addAll(int... values) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        int c = CARDINALITY;
        for (int i = 0; i < values.length; i++) {
            add(values[i]);
        }
        return CARDINALITY - c > 0;
    }

    @Override
    public boolean addAll(IntIterableSet set) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        if (set.isEmpty()) return false;
        int c = CARDINALITY;
        if (!set.isEmpty()) {
            int v = set.min();
            while (v < Integer.MAX_VALUE) {
                add(v);
                v = set.nextValue(v);
            }
        }
        return CARDINALITY > c;
    }

    public boolean addAll(IntVar var) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        int c = this.CARDINALITY;
        int s2 = this.SIZE >> 1;
        if (s2 > 0) {
            DisposableRangeIterator rit = var.getRangeIterator(true);
            while (rit.hasNext()) {
                this.addBetween(rit.min(), rit.max());
                rit.next();
            }
        }
        return CARDINALITY < c;
    }

    public boolean addAll(IntIterableRangeSet set) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        int c = CARDINALITY;
        if (!set.isEmpty()) {
            int s2 = set.SIZE >> 1;
            if (s2 > 0) {
                int j = 0;
                do {
                    addBetween(set.ELEMENTS[j << 1], set.ELEMENTS[(j << 1) + 1]);
                    j++;
                } while (j < s2);
            }
        }
        return CARDINALITY < c;
    }

    @Override
    public boolean retainAll(IntIterableSet set) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        int c = CARDINALITY;
        if (set.isEmpty()) {
            this.clear();
        } else {
            int last = max();
            for (int i = min(); i <= last; i = nextValue(i)) {
                if (!set.contains(i)) {
                    remove(i);
                }
            }
        }
        return c - CARDINALITY > 0;
    }

    public boolean retainAll(IntIterableRangeSet set) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        int c = CARDINALITY;
        if (set.isEmpty()) {
            this.clear();
            return c - CARDINALITY > 0;
        } else {
            return IntIterableSetUtils.intersectionOf(this, set);
        }
    }

    @Override
    public boolean remove(int e) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        boolean modified = false;
        int p = rangeOf(e);
        // if e is not in a range
        if (p >= 0) {
            int i = (p - 1) << 1;
            int c = ELEMENTS[i] == e ? 1 : 0;
            c += ELEMENTS[i + 1] == e ? 2 : 0;
            switch (c) {
                case 0:
                    // split range in two ranges
                    grow(SIZE + 2);
                    System.arraycopy(ELEMENTS, i + 1, ELEMENTS, i + 3, SIZE - i - 1);
                    ELEMENTS[i + 1] = e - 1;
                    ELEMENTS[i + 2] = e + 1;
                    SIZE += 2;
                    break;
                case 1:
                    // increase lower of the range
                    ELEMENTS[i]++;
                    break;
                case 2:
                    // decrease upper of the range
                    ELEMENTS[i + 1]--;
                    break;
                case 3:
                    // delete a range
                    System.arraycopy(ELEMENTS, i + 2, ELEMENTS, i, SIZE - i - 2);
                    SIZE -= 2;
                    break;
                default:
                    throw new SolverException("Unexpected mask " + c);
            }
            modified = true;
            CARDINALITY--;
        }
        return modified;
    }

    @Override
    public boolean removeAll(IntIterableSet set) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        int c = CARDINALITY;
        if (!set.isEmpty()) {
            int v = set.min();
            while (v < Integer.MAX_VALUE) {
                remove(v);
                v = set.nextValue(v);
            }
        }
        return CARDINALITY < c;
    }

    public boolean removeAll(IntIterableRangeSet set) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        int c = CARDINALITY;
        if (!set.isEmpty()) {
            int s2 = set.SIZE >> 1;
            if (s2 > 0) {
                int j = 0;
                do {
                    removeBetween(set.ELEMENTS[j << 1], set.ELEMENTS[(j << 1) + 1]);
                    j++;
                } while (j < s2);
            }
        }
        return CARDINALITY < c;
    }

    @Override
    public void clear() {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        CARDINALITY = 0;
        SIZE = 0;
    }

    @Override
    public SetType getSetType() {
        return SetType.RANGESET;
    }

    public boolean addBetween(int a, int b) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        if (a > b) {
            throw new IndexOutOfBoundsException("Incorrect bounds [" + a + "," + b + "]");
        }
        boolean change;
        int s1 = SIZE >> 1;
        int s2 = 1; // since a <= b
        if (s1 > 0) {
            int i = 0, j = 0;
            int s = 0, c = 0;
            int[] e = new int[SIZE];
            int lbi, ubi, lbj, ubj, lb, ub;
            lb = lbi = ELEMENTS[0];
            ub = ubi = ELEMENTS[1];
            lbj = a;
            ubj = b;
            if (lb > lbj) {
                lb = lbj;
                ub = ubj;
            }
            boolean extend;
            // TODO: replace while loop with rangeOf
            while (i < s1 || j < s2) {
                extend = false;
                if (lb - 1 <= lbi && lbi <= ub + 1) {
                    ub = Math.max(ub, ubi);
                    extend = i < s1;
                    if (++i < s1) {
                        lbi = ELEMENTS[i << 1];
                        ubi = ELEMENTS[(i << 1) + 1];
                    }
                }
                if (lb - 1 <= lbj && lbj <= ub + 1) {
                    ub = Math.max(ub, ubj);
                    extend |= j < s2;
                    j++;
                }
                if (!extend) {
                    if (s + 2 > e.length) {
                        // overflow-conscious code
                        int oldCapacity = e.length;
                        int newCapacity = oldCapacity + (oldCapacity >> 1);
                        if (newCapacity < s + 2)
                            newCapacity = s + 2;
                        // minCapacity is usually close to size, so this is a win:
                        e = Arrays.copyOf(e, newCapacity);
                    }
                    e[s++] = lb;
                    e[s++] = ub;
                    c += ub - lb + 1;
                    if (i < s1) {
                        lb = lbi;
                        ub = ubi;
                        if (j < s2 && lbi > lbj) {
                            lb = lbj;
                            ub = ubj;
                        }
                    } else if (j < s2) {
                        lb = lbj;
                        ub = ubj;
                    }
                }
            }
            if (s + 2 > e.length) {
                // overflow-conscious code
                int oldCapacity = e.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity < s + 2)
                    newCapacity = s + 2;
                // minCapacity is usually close to size, so this is a win:
                e = Arrays.copyOf(e, newCapacity);
            }
            e[s++] = lb;
            e[s++] = ub;
            c += ub - lb + 1;
            ELEMENTS = e;
            SIZE = s;
            change = (CARDINALITY != c);
            CARDINALITY = c;
        } else {
            grow(1);
            ELEMENTS[0] = a;
            ELEMENTS[1] = b;
            SIZE = 2;
            CARDINALITY = Math.addExact(b + 1, -a);
            change = true;
        }
        return change;
    }

    @Override
    public boolean removeBetween(int f, int t) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        boolean rem = false;
        if (f > t) {
            throw new IllegalArgumentException("Cannot remove from empty range [" + f + "," + t + "]");
        }
        int rf = rangeOf(f);
        if (rf < 0) {
            // find closest after
            rf *= -1;
            if (rf > SIZE >> 1) {
                return false;
            }
            f = ELEMENTS[(rf - 1) << 1];
        }
        assert rf > 0;
        int rt = rangeOf(t, (rf - 1) << 1, SIZE);
        if (rt < 0) {
            // find closest range before
            rt = -rt - 1;
            if (rt < 1) {
                return false;
            }
            t = ELEMENTS[((rt - 1) << 1) + 1];
        }
        assert rt > 0;
        int i = (rf - 1) << 1;
        int j = (rt - 1) << 1;
        if (rf <= rt) {
            int dcard = -(f - ELEMENTS[i] + ELEMENTS[j + 1] - t);
            dcard += ELEMENTS[i + 1] - ELEMENTS[i] + 1;
            if (rf < rt) {
                for (int k = i + 2; k <= j + 1; k += 2) {
                    dcard += ELEMENTS[k + 1] - ELEMENTS[k] + 1;
                }
                // remove useless range
                System.arraycopy(ELEMENTS, j + 1, ELEMENTS, i + 1, SIZE - (j + 1));
                SIZE -= (rt - rf) << 1;
            }
            CARDINALITY -= dcard;
            int c = ELEMENTS[i] == f ? 1 : 0;
            c += ELEMENTS[i + 1] == t ? 2 : 0;
            switch (c) {
                case 0: // split the range into two ranges
                    grow(SIZE + 2);
                    System.arraycopy(ELEMENTS, i, ELEMENTS, i + 2, SIZE - i);
                    ELEMENTS[i + 1] = f - 1;
                    ELEMENTS[i + 2] = t + 1;
                    SIZE += 2;
                    break;
                case 1: // update the lower bound of the range
                    ELEMENTS[i] = t + 1;
                    break;
                case 2: // update the upper bound of the range
                    ELEMENTS[i + 1] = f - 1;
                    break;
                case 3: // remove the range
                    if (i < SIZE - 2) {
                        System.arraycopy(ELEMENTS, i + 2, ELEMENTS, i, SIZE - (i + 2));
                    }
                    SIZE -= 2;
                    break;
            }
            rem = true;
        }
        return rem;
    }

    public boolean retainBetween(int f, int t) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        if (f > t) {
            throw new IllegalArgumentException("Cannot retain from empty range [" + f + "," + t + "]");
        }
        if (isEmpty() || f <= this.min() && t >= this.max()) {
            return false;
        }
        int rf = rangeOf(f);
        if (rf < 0) {
            // find closest after
            rf *= -1;
            if (rf << 1 > SIZE) {
                this.clear();
                return true;
            }
            f = ELEMENTS[(rf - 1) << 1];
        }
        assert rf > 0;
        int rt = rangeOf(t, (rf - 1) << 1, SIZE);
        if (rt < 0) {
            // find closest range before
            rt = -rt - 1;
            if (rt < 1) {
                this.clear();
                return true;
            }
            t = ELEMENTS[((rt - 1) << 1) + 1];
        }
        assert rt > 0;
        int i = (rf - 1) << 1;
        int j = (rt - 1) << 1;
        if (rf <= rt) {
            ELEMENTS[i] = f;
            ELEMENTS[j + 1] = t;
            System.arraycopy(ELEMENTS, i, ELEMENTS, 0, j - i + 2);
            SIZE = (rt - rf + 1) << 1;
            CARDINALITY = 0;
            for (int k = 0; k < SIZE; k += 2) {
                CARDINALITY += ELEMENTS[k + 1] - ELEMENTS[k] + 1;
            }
        } else {
            this.clear();
        }
        return true;
    }

    @Override
    public int nextValue(int e) {
        e++;
        int p = rangeOf(e);
        int next = Integer.MAX_VALUE;
        if (p == -1 && SIZE > 0) {
            next = ELEMENTS[0];
        } else if (p >= 0) {
            next = e;
        } else if (p > -((SIZE >> 1) + 1)) {
            return ELEMENTS[(-p - 1) << 1];
        }
        return next;
    }

    @Override
    public int nextValueOut(int e) {
        e++;
        int p = rangeOf(e);
        int next;
        if (p >= 0) {
            next = ELEMENTS[((p - 1) << 1) + 1] + 1;
        } else {
            next = e;
        }
        return next;
    }

    @Override
    public int previousValue(int e) {
        e--;
        int p = rangeOf(e);
        int prev = Integer.MIN_VALUE;
        if (p == -((SIZE >> 1) + 1) && SIZE > 0) {
            prev = ELEMENTS[SIZE - 1];
        } else if (p >= 0) {
            prev = e;
        } else if (p < -1) {
            prev = ELEMENTS[((-p - 1) << 1) - 1];
        }
        return prev;
    }

    @Override
    public int previousValueOut(int e) {
        e--;
        int p = rangeOf(e);
        int prev;
        if (p >= 0) {
            prev = ELEMENTS[((p - 1) << 1)] - 1;
        } else {
            prev = e;
        }
        return prev;
    }

    @Override
    public boolean contains(int o) {
        if (CARDINALITY == 1) {
            return ELEMENTS[0] == o;
        }
        return rangeOf(o) >= 0;
    }

    @Override
    public IntIterableRangeSet duplicate() {
        IntIterableRangeSet ir = new IntIterableRangeSet();
        ir.ELEMENTS = this.ELEMENTS.clone();
        ir.CARDINALITY = this.CARDINALITY;
        ir.SIZE = this.SIZE;
        checkInvariants();
        return ir;
    }

    public IntIterableRangeSet copyFrom(IntIterableRangeSet me) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        this.clear();
        for (int s = 0; s < me.SIZE; s += 2) {
            pushRange(me.ELEMENTS[s], me.ELEMENTS[s + 1]);
        }
        checkInvariants();
        return this;
    }

    /**
     * Copy the domain of {@code var} in {@code this}.
     * First, it clears {@code this}, then it fills it with the value in {@code var}.
     *
     * @param var an integer variable
     */
    public void copyFrom(IntVar var) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        this.clear();
        DisposableRangeIterator rit = var.getRangeIterator(true);
        while (rit.hasNext()) {
            int lb = rit.min();
            int ub = rit.max();
            this.pushRange(lb, ub);
            rit.next();
        }
        rit.dispose();
    }

    @Override
    public int size() {
        return CARDINALITY;
    }

    @Override
    public ISetIterator newIterator() {
        return new ISetIterator() {
            private boolean started = false;
            private int current;

            @Override
            public void reset() {
                started = false;
            }

            @Override
            public boolean hasNext() {
                if (started) {
                    return nextValue(current) < Integer.MAX_VALUE;
                } else {
                    return !isEmpty();
                }
            }

            @Override
            public int nextInt() {
                if (started) {
                    current = nextValue(current);
                } else {
                    started = true;
                    current = min();
                }
                return current;
            }
        };
    }

    /**
     * add the value <i>x</i> to all integers stored in this set
     *
     * @param x value to add
     */
    public void plus(int x) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        for (int i = 0; i < SIZE; i++) {
            ELEMENTS[i] += x;
        }
    }

    @Override
    public ISetIterator iterator() {
        if (iter == null) {
            iter = newIterator();
        }
        iter.reset();
        return iter;
    }

    /**
     * subtract the value <i>x</i> to all integers stored in this set
     *
     * @param x value to add
     */
    public void minus(int x) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        for (int i = 0; i < SIZE; i++) {
            ELEMENTS[i] -= x;
        }
    }

    /**
     * multiply by <i>x</i> to all integers stored in this set
     *
     * @param x value to add
     */
    public void times(int x) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        if (x > 0) {
            for (int i = 0; i < SIZE; i++) {
                ELEMENTS[i] *= x;
            }
            CARDINALITY *= x;
        } else if (x < 0) {
            for (int i = 0; i < (SIZE >> 1); i++) {
                int t = ELEMENTS[i];
                ELEMENTS[i] = ELEMENTS[SIZE - i - 1] * x;
                ELEMENTS[SIZE - i - 1] = t * x;
            }
            CARDINALITY *= -x;
        } else {
            this.clear();
            this.add(0);
        }
    }

    /**
     * By convention, range are numbered starting from 1 (not 0).
     *
     * @param x a value
     * @return the range index if the value is in the set or -<i>range point</i> - 1 otherwise
     * where <i>range point</i> corresponds to the range directly greater than the key
     */
    public int rangeOf(int x) {
        return rangeOf(x, 0, SIZE);
    }

    /**
     * By convention, range are numbered starting from 1 (not 0).
     *
     * @param x a value
     * @return the range index if the value is in the set or -<i>range point</i> - 1 otherwise
     * where <i>range point</i> corresponds to the range directly greater than the key
     */
    protected int rangeOf(int x, int fromIndex, int toIndex) {
        if (toIndex - fromIndex < 15) {
            int q = -((toIndex >> 1) + 1);
            for (int r = fromIndex; r < toIndex - 1; r += 2) {
                if (x < ELEMENTS[r]) {
                    q = -((r >> 1) + 1);
                    break;
                } else if (x <= ELEMENTS[r + 1]) {
                    q = (r >> 1) + 1;
                    break;
                }
            }
            return q;
        }
        int p = Arrays.binarySearch(ELEMENTS, fromIndex, toIndex, x);
        // if pos is positive, the value is a bound of a range
        if (p >= 0) {
            p >>= 1;
        } else if (p == -1) {
            p--;
        } else if (p == -(toIndex + 1)) {
            p = -((toIndex >> 1) + 2);// -2 because add 1 as last instruction
        } else {
            // is x in a range or not
            p = -(p + 1);
            p >>= 1;
            if (!(ELEMENTS[p << 1] < x && x < ELEMENTS[(p << 1) + 1])) {
                p = -(p + 2); // -2 because add 1 as last instruction
            }
        }
        return p + 1;
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    void grow(int minCapacity) {
        if (minCapacity - ELEMENTS.length > 0) {
            // overflow-conscious code
            int oldCapacity = ELEMENTS.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0)
                newCapacity = minCapacity;
            // minCapacity is usually close to size, so this is a win:
            ELEMENTS = Arrays.copyOf(ELEMENTS, newCapacity);
        }
    }

    /**
     * Push a range at the end of this set
     *
     * @param lb lower bound of the range
     * @param ub upper bound of the range
     */
    void pushRange(int lb, int ub) {
        assert SIZE == 0 || ELEMENTS[SIZE - 1] < lb - 1;
        assert lb <= ub;
        grow(SIZE + 2);
        ELEMENTS[SIZE++] = lb;
        ELEMENTS[SIZE++] = ub;
        CARDINALITY += Math.addExact(ub + 1, -lb);
    }

    /**
     * Compact the array in memory
     */
    public void compact() {
        ELEMENTS = Arrays.copyOf(ELEMENTS, SIZE);
    }

    /**
     * Turn this into the complement of this.
     * calling :
     * <pre>set.flip().flip()</pre>
     * goes back to the original set.
     *
     * @return this turned into its complement, based on {@link #MIN} and {@link #MAX}
     */
    public IntIterableRangeSet flip() {
        return flip(MIN, MAX);
    }

    /**
     * Turn this into the complement of this.
     * calling :
     * <pre>set.flip().flip()</pre>
     * goes back to the original set.
     *
     * @return this turned into its complement, based on <i>lb</i>, <i>ub</i>
     */
    public IntIterableRangeSet flip(int lb, int ub) {
        if (lock) {
            throw new IllegalStateException("This set is immutable");
        }
        if (SIZE == 0) { // empty set
            pushRange(lb, ub);
        } else if (ELEMENTS[0] <= lb && ELEMENTS[1] >= ub) { // all
            clear();
        } else {
            boolean smin = ELEMENTS[0] <= lb;
            boolean emax = ELEMENTS[SIZE - 1] >= ub;
            if (!smin && !emax) {
                grow(SIZE + 2);
                SIZE += 2;
            }
            if (smin && emax) {
                SIZE -= 2;
            }// else no need to change the length of ELEMENTS
            // two cases:
            CARDINALITY = 0;
            if (smin) {
                int i = 1;
                int max = emax ? SIZE : SIZE - 2;
                while (i < max) {
                    ELEMENTS[i - 1] = ELEMENTS[i++] + 1;
                    ELEMENTS[i - 1] = ELEMENTS[i++] - 1;
                    CARDINALITY += ELEMENTS[i - 2] - ELEMENTS[i - 3] + 1;
                }
                if (!emax) {
                    ELEMENTS[i - 1] = ELEMENTS[i++] + 1;
                    ELEMENTS[i - 1] = ub;
                    CARDINALITY += ELEMENTS[i - 1] - ELEMENTS[i - 2] + 1;
                }
            } else {
                int i = SIZE - 1;
                if (!emax) {
                    ELEMENTS[i--] = ub;
                } else {
                    ELEMENTS[i--] = ELEMENTS[i] - 1;
                }
                while (i > 0) {
                    ELEMENTS[i] = ELEMENTS[--i] + 1;
                    try {
                        CARDINALITY += ELEMENTS[i + 2] - ELEMENTS[i + 1] + 1;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.print("tt");
                    }
                    ELEMENTS[i] = ELEMENTS[--i] - 1;
                }
                ELEMENTS[i] = lb;
                CARDINALITY += ELEMENTS[i + 1] - ELEMENTS[i] + 1;
            }
        }
        return this;

    }

    /**
     * @param lb an int
     * @param ub an int
     * @return <i>true</i> if the intesection between {@code this} and [{@code lb}, {@code ub}] is not empty,
     * <i>false</i> otherwise.
     */
    @SuppressWarnings("Duplicates")
    public boolean intersect(int lb, int ub) {
        if(CARDINALITY > 100) return intersectDichot(lb, ub);
        int s1 = this.SIZE >> 1;
        int s2 = ub - lb + 1;
        if (s1 > 0 && s2 > 0) {
            int i = 0;
            int lbi, ubi;
            lbi = this.ELEMENTS[0];
            ubi = this.ELEMENTS[1];
            while (true) {
                if ((lbi <= lb && lb <= ubi) || (lb <= lbi && lbi <= ub)) {
                    return true;
                } else if (ubi <= ub && ++i < s1) {
                    lbi = this.ELEMENTS[i << 1];
                    ubi = this.ELEMENTS[(i << 1) + 1];
                } else {
                    break;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("Duplicates")
    public boolean intersectDichot(int lb, int ub) {
        int rlb = rangeOf(lb);
        int rub;
        if (rlb > 0) {
            return true;
        } else {
            rub = rangeOf(ub, (-rlb - 1) << 1, SIZE);
            if (rub > 0) {
                return true;
            } else {
                return rlb != rub;
            }
        }
    }

    /**
     * @param set a set
     * @return <i>true</i> if intersection of {@code this} and {@code set} is not empty,
     * <i>false</i> otherwise.
     */
    @SuppressWarnings("Duplicates")
    public boolean intersect(IntIterableRangeSet set) {
        int s1 = this.SIZE >> 1;
        int s2 = set.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            int i = 0, j = 0;
            int lbi, ubi, lbj, ubj;
            lbi = this.ELEMENTS[0];
            ubi = this.ELEMENTS[1];
            lbj = set.ELEMENTS[0];
            ubj = set.ELEMENTS[1];
            while (i < s1 && j < s2) {
                if ((lbi <= lbj && lbj <= ubi) || (lbj <= lbi && lbi <= ubj)) {
                    return true;
                }
                if (ubi <= ubj && ++i < s1) {
                    lbi = this.ELEMENTS[i << 1];
                    ubi = this.ELEMENTS[(i << 1) + 1];
                } else if (ubj <= ubi && ++j < s2) {
                    lbj = set.ELEMENTS[j << 1];
                    ubj = set.ELEMENTS[(j << 1) + 1];
                }
            }
        }
        return false;
    }

    /**
     * @param var a variable
     * @return <i>true</i> if intersection of {@code var} and {@code set} is not empty,
     * <i>false</i> otherwise.
     */
    @SuppressWarnings("Duplicates")
    public boolean intersect(IntVar var) {
        int s1 = var.getDomainSize();
        int s2 = this.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            int j = 0;
            int lbi, ubi, lbj, ubj;
            lbi = var.getLB();
            ubi = var.nextValueOut(lbi) - 1;
            lbj = this.ELEMENTS[0];
            ubj = this.ELEMENTS[1];
            while (lbi < Integer.MAX_VALUE && j < s2) {
                if ((lbi <= lbj && lbj <= ubi) || (lbj <= lbi && lbi <= ubj)) {
                    return true;
                }
                if (ubi <= ubj && (lbi = var.nextValue(ubi)) < Integer.MAX_VALUE) {
                    ubi = var.nextValueOut(lbi) - 1;
                } else if (ubj <= ubi && ++j < s2) {
                    lbj = this.ELEMENTS[j << 1];
                    ubj = this.ELEMENTS[(j << 1) + 1];
                }
            }
        }
        return false;
    }

    /**
     * Apply the operation <i>c</i> on each value in this set
     *
     * @param c an operation
     */
    public void forEachValueIn(IntConsumer c) {
        for (int s = 0; s < SIZE; s += 2) {
            for (int i = ELEMENTS[s]; i <= ELEMENTS[s + 1]; i++) {
                c.accept(i);
            }
        }
    }

    /**
     * Apply the operation <i>c</i> on each value in : ]{@link #min()}, {@link #max()}[ \ this set.
     *
     * @param c an operation
     */
    public void forEachValueOut(IntConsumer c) {
        for (int s = 1; s < SIZE - 1; s += 2) {
            for (int i = ELEMENTS[s] + 1; i < ELEMENTS[s + 1]; i++) {
                c.accept(i);
            }
        }
    }

    /**
     * @return an array containing all of the elements in this set in
     * sorted sequence
     */
    public int[] toArray() {
        int[] a = new int[CARDINALITY];
        int k = 0;
        for (int i = 0; i < SIZE - 1; i += 2) {
            if (ELEMENTS[i] == ELEMENTS[i + 1]) {
                a[k++] = ELEMENTS[i];
            } else {
                for (int j = ELEMENTS[i]; j <= ELEMENTS[i + 1]; j++) {
                    a[k++] = j;
                }
            }
        }
        return a;
    }
}
