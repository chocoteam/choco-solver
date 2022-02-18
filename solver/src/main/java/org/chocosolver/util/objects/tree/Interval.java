/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.tree;

/**
 *  This class is adapted from : a
 *  <a href="https://github.com/masonmlai/interval-tree">balanced binary-search tree keyed by
 * Interval objects</a>.
 * @author <a href="https://github.com/masonmlai" >Mason M Lai</a>
 * @author Charles Prud'homme
 */
public interface Interval extends Comparable<Interval> {

    /**
     * Returns the starting point of this.
     */
    int start();

    /**
     * Returns the ending point of this. <p> The interval does not include this point.
     */
    int end();

    /**
     * Returns the length of this.
     */
    default int length() {
        return end() - start();
    }

    /**
     * Returns if this interval is adjacent to the specified interval. <p> Two intervals are
     * adjacent if either one ends where the other starts.
     *
     * @param other - the interval to compare this one to
     * @return if this interval is adjacent to the specified interval.
     */
    default boolean isAdjacent(Interval other) {
        return start() == other.end() || end() == other.start();
    }

    default boolean overlaps(Interval o) {
        return end() > o.start() && o.end() > start();
    }

    default boolean overlaps(int start, int end) {
        return end() > start && end > start();
    }

    default int compareTo(Interval o) {
        return compareTo(o.start(), o.end());
    }

    default int compareTo(int s, int e) {
        int d = start() -s;
        assert (start() > s && d > 0) || (start() < s && d < 0) || (start() == s && d  == 0);
        if(d == 0){
            d = end() - e;
            assert (end() > e && d > 0) || (end() < e && d < 0) || (end() == e && d  == 0);
        }
        return Integer.signum(d);
    }

}
