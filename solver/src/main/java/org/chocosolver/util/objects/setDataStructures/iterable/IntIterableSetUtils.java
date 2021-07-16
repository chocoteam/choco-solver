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

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableRangeIterator;

import java.util.Arrays;

/**
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 25/01/2016.
 */
public class IntIterableSetUtils {

    /**
     * @param set on which the complement is based
     * @param lbu lower bound (inclusive) of the universe
     * @param ubu upper bound (inclusive) of the universe
     * @return the complement of this set wrt to universe set [<i>lbu</i>, <i>ubu</i>].
     * Values smaller than <i>lbu</i> and greater than <i>ubu</i> are ignored.
     */
    public static IntIterableRangeSet complement(IntIterableRangeSet set, int lbu, int ubu) {
        assert lbu <= ubu;
        IntIterableRangeSet t = new IntIterableRangeSet();
        t.ELEMENTS = new int[set.SIZE + 2];
        int i = 0;
        int lb = lbu;
        while (i < set.SIZE && set.ELEMENTS[i] <= lbu) {
            i += 2;
            lb = set.ELEMENTS[i - 1] + 1;
        }
        if (i == set.SIZE) {
            if (lb <= ubu) {
                t.pushRange(lb, ubu);
            }// else: empty set
        } else {
            assert set.ELEMENTS[i] > lb;
            t.pushRange(lb, set.ELEMENTS[i++] - 1);
            while (i < set.SIZE - 2 && set.ELEMENTS[i] < ubu) {
                t.pushRange(set.ELEMENTS[i++] + 1, set.ELEMENTS[i++] - 1);
            }
            if (set.ELEMENTS[i] < ubu) {
                t.pushRange(set.ELEMENTS[i] + 1, ubu);
            }
        }
        return t;
    }

    /**
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return return a set {a + b | a in set1, b in set2}
     */
    public static IntIterableRangeSet plus(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t = new IntIterableRangeSet();
        plus(t, set1, set2);
        return t;
    }

    /**
     * Set <i>setr</i> to {a + b | a in <i>set1</i>, b in <i>set2</i>}
     *
     * @param setr set of ints
     * @param set1 a set of ints
     * @param set2 a set of ints
     */
    public static void plus(IntIterableRangeSet setr, IntIterableRangeSet set1, IntIterableRangeSet set2) {
        setr.clear();
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            setr.grow(set1.SIZE);
            int i = 0, j = 0;
            int[] is = new int[s2];
            Arrays.fill(is, -1);
            is[0] = 0;
            int lb = set1.ELEMENTS[0] + set2.ELEMENTS[0];
            int ub = set1.ELEMENTS[1] + set2.ELEMENTS[1];
            do {
                boolean extend = false;
                for (int k = i; k <= j; k++) {
                    int _lb = set1.ELEMENTS[is[k] << 1] + set2.ELEMENTS[k << 1];
                    if (lb <= _lb && _lb <= ub + 1) {
                        ub = Math.max(set1.ELEMENTS[(is[k] << 1) + 1] + set2.ELEMENTS[(k << 1) + 1], ub);
                        extend = true;
                        // add neighbors to evaluate
                        // 1. left neighbor
                        if (k < s2 - 1 && k == j) {
                            is[k + 1]++;
                            if (is[k + 1] == 0) {
                                j++;
                            }
                        }
                        // 2. bottom neighbor
                        is[k]++;
                        if (is[k] == s1) {
                            i++;
                        }
                    }
                }
                if (!extend) {
                    setr.pushRange(lb, ub);
                    lb = Integer.MAX_VALUE;
                    for (int k = i; k <= j; k++) {
                        int _lb = set1.ELEMENTS[is[k] << 1] + set2.ELEMENTS[k << 1];
                        if (lb > _lb) {
                            lb = _lb;
                            ub = set1.ELEMENTS[(is[k] << 1) + 1] + set2.ELEMENTS[(k << 1) + 1];
                        }
                    }
                }
            } while (is[s2 - 1] < s1);
            setr.pushRange(lb, ub);
        }
    }


    /**
     * Set <i>setr</i> to {a + b | a in <i>set1</i>, b in [<i>l</i>..<i>u</i>]}
     *
     * @param setr set of ints
     * @param set1 a set of ints
     * @param l    an int
     * @param u    an int
     */
    public static void plus(IntIterableRangeSet setr, IntIterableRangeSet set1, int l, int u) {
        setr.clear();
        int s1 = set1.SIZE >> 1;
        if (s1 > 0 && l <= u) {
            int k = 0;
            setr.grow(set1.SIZE);
            int lb = set1.ELEMENTS[0] + l;
            int ub = set1.ELEMENTS[1] + u;

            for (; k < s1; k++) {
                int _lb = set1.ELEMENTS[k << 1] + l;
                if (lb <= _lb && _lb <= ub + 1) {
                    ub = Math.max(set1.ELEMENTS[(k << 1) + 1] + u, ub);
                } else {
                    setr.pushRange(lb, ub);
                    lb = set1.ELEMENTS[k << 1] + l;
                    ub = set1.ELEMENTS[(k << 1) + 1] + u;
                }
            }
            setr.pushRange(lb,ub);
        }
    }

    /**
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return return a set {a - b | a in set1, b in set2}
     */
    public static IntIterableRangeSet minus(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t = new IntIterableRangeSet();
        minus(t, set1, set2);
        return t;
    }

    /**
     * Set <i>setr</i> to {a - b | a in <i>set1</i>, b in <i>set2</i>}
     *
     * @param setr set of ints
     * @param set1 a set of ints
     * @param set2 a set of ints
     */
    public static void minus(IntIterableRangeSet setr, IntIterableRangeSet set1, IntIterableRangeSet set2) {
        setr.clear();
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            setr.grow(set1.SIZE);
            int i = s2 - 1, j = s2 - 1;
            int[] is = new int[s2];
            Arrays.fill(is, -1);
            is[s2 - 1] = 0;
            int lb = set1.ELEMENTS[0] - set2.ELEMENTS[((s2 - 1) << 1) + 1];
            int ub = set1.ELEMENTS[1] - set2.ELEMENTS[(s2 - 1) << 1];
            do {
                boolean extend = false;
                for (int k = j; k >= i; k--) {
                    int _lb = set1.ELEMENTS[is[k] << 1] - set2.ELEMENTS[(k << 1) + 1];
                    if (lb <= _lb && _lb <= ub + 1) {
                        ub = Math.max(set1.ELEMENTS[(is[k] << 1) + 1] - set2.ELEMENTS[(k << 1)], ub);
                        extend = true;
                        // add neighbors to evaluate
                        // 1. left neighbor
                        if (k > 0 && k == i) {
                            is[k - 1]++;
                            if (is[k - 1] == 0) {
                                i--;
                            }
                        }
                        // 2. bottom neighbor
                        is[k]++;
                        if (is[k] == s1) {
                            j--;
                        }
                    }
                }
                if (!extend) {
                    setr.pushRange(lb, ub);
                    lb = Integer.MAX_VALUE;
                    for (int k = i; k <= j; k++) {
                        int _lb = set1.ELEMENTS[is[k] << 1] - set2.ELEMENTS[(k << 1) + 1];
                        if (lb > _lb) {
                            lb = _lb;
                            ub = set1.ELEMENTS[(is[k] << 1) + 1] - set2.ELEMENTS[k << 1];
                        }
                    }
                }
            } while (is[0] < s1);
            setr.pushRange(lb, ub);
        }
    }


    /**
     * Set <i>setr</i> to {a - b | a in <i>set1</i>, b in [<i>l</i>..<i>u</i>]}
     *
     * @param setr set of ints
     * @param set1 a set of ints
     * @param l    an int
     * @param u    an int
     */
    public static void minus(IntIterableRangeSet setr, IntIterableRangeSet set1, int l, int u) {
        setr.clear();
        int s1 = set1.SIZE >> 1;
        if (s1 > 0 && l <= u) {
            setr.grow(set1.SIZE);
            int k = s1;
            int lb = set1.ELEMENTS[0] - u;
            int ub = set1.ELEMENTS[1] - l;
            for (; k >= 0; k--) {
                int _lb = set1.ELEMENTS[k << 1] - u;
                if (lb <= _lb && _lb <= ub + 1) {
                    ub = Math.max(set1.ELEMENTS[(k << 1) + 1] - l, ub);
                } else {
                    setr.pushRange(lb, ub);
                    lb = set1.ELEMENTS[k << 1] - u;
                    ub = set1.ELEMENTS[(k << 1) + 1] - l;
                }
            }
            setr.pushRange(lb, ub);
        }
    }


    /**
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return return a set = set1 &cap; set2
     */
    public static IntIterableRangeSet intersection(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t = new IntIterableRangeSet();
        intersection(t, set1, set2);
        return t;
    }

    /**
     * Set <i>setr</i> to <i>set1</i> &cap; <i>set2</i>
     *
     * @param setr set of ints
     * @param set1 a set of ints
     * @param set2 a set of ints
     */
    @SuppressWarnings("Duplicates")
    public static void intersection(IntIterableRangeSet setr, IntIterableRangeSet set1, IntIterableRangeSet set2) {
        setr.clear();
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            setr.grow(set1.SIZE);
            int i = 0, j = 0;
            int lbi, ubi, lbj, ubj, lb, ub;
            lbi = set1.ELEMENTS[0];
            ubi = set1.ELEMENTS[1];
            lbj = set2.ELEMENTS[0];
            ubj = set2.ELEMENTS[1];
            while (i < s1 && j < s2) {
                if ((lbi <= lbj && lbj <= ubi) || (lbj <= lbi && lbi <= ubj)) {
                    lb = Math.max(lbi, lbj);
                    ub = Math.min(ubi, ubj);
                    setr.pushRange(lb, ub);
                }
                if (ubi <= ubj && ++i < s1) {
                    lbi = set1.ELEMENTS[i << 1];
                    ubi = set1.ELEMENTS[(i << 1) + 1];
                }else if (ubj <= ubi && ++j < s2) {
                    lbj = set2.ELEMENTS[j << 1];
                    ubj = set2.ELEMENTS[(j << 1) + 1];
                }
            }
        }
    }

    /**
     * Set <i>setr</i> to <i>set1</i> &cap; [<i>from</i>,<i>to</i>]
     *
     * @param setr set of ints
     * @param set1 a set of ints
     * @param from lower bound of an interval
     * @param to upper bound of an interval
     */
    @SuppressWarnings("Duplicates")
    public static void intersection(IntIterableRangeSet setr, IntIterableRangeSet set1, int from, int to) {
        setr.clear();
        int s1 = set1.SIZE >> 1;
        int s2 = from <= to ? 1 : 0;
        if (s1 > 0 && s2 > 0) {
            setr.grow(set1.SIZE);
            int i = 0, j = 0;
            int lbi, ubi, lbj, ubj, lb, ub;
            lbi = set1.ELEMENTS[0];
            ubi = set1.ELEMENTS[1];
            lbj = from;
            ubj = to;
            while (i < s1 && j < s2) {
                if ((lbi <= lbj && lbj <= ubi) || (lbj <= lbi && lbi <= ubj)) {
                    lb = Math.max(lbi, lbj);
                    ub = Math.min(ubi, ubj);
                    setr.pushRange(lb, ub);
                }
                if (ubi <= ubj && ++i < s1) {
                    lbi = set1.ELEMENTS[i << 1];
                    ubi = set1.ELEMENTS[(i << 1) + 1];
                }else if(ubj <= ubi){
                    j++;
                }
            }
        }
    }

    /**
     * Set <i>set1</i> to <i>set1</i> &cap; <i>set2</i>
     *
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return <tt>true</tt> if <i>set1</i> has changed
     */
    @SuppressWarnings("Duplicates")
    public static boolean intersectionOf(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        boolean change = false;
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            int i = 0, j = 0;
            int s = 0, c = 0;
            int[] e = new int[set1.SIZE];
            int lbi, ubi, lbj, ubj, lb, ub;
            lbi = set1.ELEMENTS[0];
            ubi = set1.ELEMENTS[1];
            lbj = set2.ELEMENTS[0];
            ubj = set2.ELEMENTS[1];
            while (i < s1 && j < s2) {
                if ((lbi <= lbj && lbj <= ubi) || (lbj <= lbi && lbi <= ubj)) {
                    lb = Math.max(lbi, lbj);
                    ub = Math.min(ubi, ubj);
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
                    change = true;
                }
                if (ubi <= ubj && ++i < s1) {
                    lbi = set1.ELEMENTS[i << 1];
                    ubi = set1.ELEMENTS[(i << 1) + 1];
                }else if (ubj <= ubi && ++j < s2) {
                    lbj = set2.ELEMENTS[j << 1];
                    ubj = set2.ELEMENTS[(j << 1) + 1];
                }
            }
            set1.ELEMENTS = e;
            set1.SIZE = s;
            change |= (set1.CARDINALITY != c);
            set1.CARDINALITY = c;
        }else{
            change = set1.CARDINALITY > 0;
            set1.clear();
        }
        return change;
    }

    /**
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return return a set = set1 &cup; set2
     */
    public static IntIterableRangeSet union(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        IntIterableRangeSet t = new IntIterableRangeSet();
        union(t, set1, set2);
        return t;
    }

    /**
     * Set <i>setr</i> to <i>set1</i> &cup; <i>set2</i>
     *
     * @param setr set of ints
     * @param set1 a set of ints
     * @param set2 a set of ints
     */
    @SuppressWarnings("Duplicates")
    public static void union(IntIterableRangeSet setr, IntIterableRangeSet set1, IntIterableRangeSet set2) {
        setr.clear();
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            setr.grow(set1.SIZE);
            int i = 0, j = 0;
            int lbi, ubi, lbj, ubj, lb, ub;
            lb = lbi = set1.ELEMENTS[0];
            ub = ubi = set1.ELEMENTS[1];
            lbj = set2.ELEMENTS[0];
            ubj = set2.ELEMENTS[1];
            if(lb > lbj){
                lb = lbj;
                ub = ubj;
            }
            boolean extend;
            while (i < s1 || j < s2) {
                extend  = false;
                if (lb - 1 <= lbi && lbi <= ub + 1) {
                    ub = Math.max(ub, ubi);
                    extend = i < s1;
                    if(++i < s1){
                        lbi = set1.ELEMENTS[i << 1];
                        ubi = set1.ELEMENTS[(i << 1) + 1];
                    }
                }
                if (lb - 1 <= lbj && lbj <= ub + 1) {
                    ub = Math.max(ub, ubj);
                    extend |= j < s2;
                    if(++j < s2){
                        lbj = set2.ELEMENTS[j << 1];
                        ubj = set2.ELEMENTS[(j << 1) + 1];
                    }
                }
                if(!extend){
                    setr.pushRange(lb, ub);
                    if(i < s1) {
                        lb = lbi;
                        ub = ubi;
                        if(j < s2 && lbi > lbj){
                            lb = lbj;
                            ub = ubj;
                        }
                    }else if(j < s2){
                        lb = lbj;
                        ub = ubj;
                    }
                }
            }
            setr.pushRange(lb, ub);
        }
    }

    /**
     * Set <i>set1</i> to <i>set1</i> &cap; <i>set2</i>
     *
     * @param set1 a set of ints
     * @param set2 a set of ints
     * @return <tt>true</tt> if <i>set1</i> has changed
     */
    @SuppressWarnings("Duplicates")
    public static boolean unionOf(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        boolean change = false;
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            int i = 0, j = 0;
            int s = 0, c = 0;
            int[] e = new int[set1.SIZE];
            int lbi, ubi, lbj, ubj, lb, ub;
            lb = lbi = set1.ELEMENTS[0];
            ub = ubi = set1.ELEMENTS[1];
            lbj = set2.ELEMENTS[0];
            ubj = set2.ELEMENTS[1];
            if (lb > lbj) {
                lb = lbj;
                ub = ubj;
            }
            boolean extend;
            while (i < s1 || j < s2) {
                extend = false;
                if (lb - 1 <= lbi && lbi <= ub + 1) {
                    ub = Math.max(ub, ubi);
                    extend = i < s1;
                    if (++i < s1) {
                        lbi = set1.ELEMENTS[i << 1];
                        ubi = set1.ELEMENTS[(i << 1) + 1];
                    }
                }
                if (lb - 1 <= lbj && lbj <= ub + 1) {
                    ub = Math.max(ub, ubj);
                    extend |= j < s2;
                    if (++j < s2) {
                        lbj = set2.ELEMENTS[j << 1];
                        ubj = set2.ELEMENTS[(j << 1) + 1];
                    }
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
            set1.ELEMENTS = e;
            set1.SIZE = s;
            change = (set1.CARDINALITY != c);
            set1.CARDINALITY = c;
        } else {
            if(s2 > 0){
                set1.grow(set2.SIZE);
                System.arraycopy(set2.ELEMENTS, 0, set1.ELEMENTS, 0, set2.SIZE);
                set1.SIZE = set2.SIZE;
                set1.CARDINALITY = set2.CARDINALITY;
                change = true;
            }
        }
        return change;
    }

    /**
     * @param var a variable
     * @param set a set
     * @return <i>true</i> if <i>var</i> is included into <i>set</i>,
     * <i>false</i> otherwise.
     */
    @SuppressWarnings("Duplicates")
    public static boolean includedIn(IntVar var, IntIterableRangeSet set) {
        int s1 = var.getDomainSize();
        int s2 = set.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            int j = 0;
            int lbi, ubi, lbj, ubj;
            lbi = var.getLB();
            ubi = var.nextValueOut(lbi) - 1;
            lbj = set.ELEMENTS[0];
            ubj = set.ELEMENTS[1];
            while (lbi < Integer.MAX_VALUE && j < s2) {
                if (ubj < lbi && ++j < s2) {
                    lbj = set.ELEMENTS[j << 1];
                    ubj = set.ELEMENTS[(j << 1) + 1];
                }else if(lbj <= lbi && ubi <= ubj){
                    if((lbi = var.nextValue(ubi)) < Integer.MAX_VALUE) {
                        ubi = var.nextValueOut(lbi) - 1;
                    }
                }else{
                    return false;
                }
            }
        }
        return s2 > 0;
    }

    /**
     * @param set1 a set
     * @param set2 a set
     * @return <i>true</i> if <i>set1</i> is included into <i>set2</i>,
     * <i>false</i> otherwise.
     */
    @SuppressWarnings("Duplicates")
    public static boolean includedIn(IntIterableRangeSet set1, IntIterableRangeSet set2) {
        int s1 = set1.SIZE >> 1;
        int s2 = set2.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            int i = 0, j = 0;
            int lbi, ubi, lbj, ubj;
            lbi = set1.ELEMENTS[0];
            ubi = set1.ELEMENTS[1];
            lbj = set2.ELEMENTS[0];
            ubj = set2.ELEMENTS[1];
            while (i < s1 && j < s2) {
                if (ubj < lbi && ++j < s2) {
                    lbj = set2.ELEMENTS[j << 1];
                    ubj = set2.ELEMENTS[(j << 1) + 1];
                }else if (lbj <= lbi && ubi <= ubj) {
                    if(++i < s1) {
                        lbi = set1.ELEMENTS[i << 1];
                        ubi = set1.ELEMENTS[(i << 1) + 1];
                    }
                }else{
                    return false;
                }
            }
        }
        return s2 > 0;
    }


    /**
     * @param var a variable
     * @param set a set
     * @return <i>true</i> if <i>var</i> is not included into <i>set</i>,
     * <i>false</i> otherwise.
     */
    @SuppressWarnings("Duplicates")
    public static boolean notIncludedIn(IntVar var, IntIterableRangeSet set) {
        int s1 = var.getDomainSize();
        int s2 = set.SIZE >> 1;
        if (s1 > 0 && s2 > 0) {
            DisposableRangeIterator rit = var.getRangeIterator(true);
            int j = 0;
            int lbi, ubi, lbj, ubj;
            lbi = rit.min();
            ubi = rit.max();
            rit.next();
            lbj = set.ELEMENTS[0];
            ubj = set.ELEMENTS[1];
            while (rit.hasNext() && j < s2) {
                if ((lbi <= lbj && lbj <= ubi) || (lbj <= lbi && lbi <= ubj)) {
                    rit.dispose();
                    return true;
                }
                if (ubi <= ubj && rit.hasNext()) {
                    lbi = rit.min();
                    ubi = rit.max();
                    rit.next();
                }else if (ubj <= ubi && ++j < s2) {
                    lbj = set.ELEMENTS[j << 1];
                    ubj = set.ELEMENTS[(j << 1) + 1];
                }
            }
            rit.dispose();
        }
        return false;
    }
}
