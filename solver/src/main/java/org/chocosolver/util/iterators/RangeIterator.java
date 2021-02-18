/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.iterators;

/**
 * An interface to declare range iterator.
 * <p/>
 * A range iterator can be iterated in 2 ways: bottom-up (from lower bound to upper bound) <br/>
 * and top-down (from upper bound to lower bound).<br/>
 * To iterate in bottom-up way, first call bottomUpInit(), then hasNext() and next().<br/>
 * To iterate in bottom-up way, first call topDownInit(), then hasPrevious() and previous().<br/>
 * <br/>
 * Once a way is selected, using the wrong methods can lead to unexpected behaviour.
 * <p/>
 * <pre>
 * RangeIterator rit = ...;
 * rit.bottomUpInit();
 * while (rit.hasNext()) {
 *     int from = rit.min();
 *     int to = rit.max();
 *     // operate on range [from,to] here
 *     rit.next();
 * }</pre>
 *
 * OR
 *
 * <pre>
 * DisposableRangeIterator rit = ...;
 * rit.topDownInit();
 * while (rit.hasPrevious()) {
 *     int from = rit.min();
 *     int to = rit.max();
 *     // operate on range [from,to] here
 *     rit.previous();
 * }</pre>
 *
 * <br/>
 * Based on <br/>"Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/10/11
 */
public interface RangeIterator {

    void bottomUpInit();

    void topDownInit();

    /**
     * Returns <tt>true</tt> if the iteration has more ranges. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return a valid range.)
     *
     * @return <tt>true</tt> if the getIterator has more ranges.
     */
    boolean hasNext();

    /**
     * Returns <tt>true</tt> if the iteration has more ranges. (In other
     * words, returns <tt>true</tt> if <tt>previous</tt> would return a valid range.)
     *
     * @return <tt>true</tt> if the getIterator has more ranges.
     */
    boolean hasPrevious();

    /**
     * Compute the next range.
     */
    void next();

    /**
     * Compute the previous range.
     */
    void previous();

    /**
     * Return the lower bound of the current range (inclusive)
     *
     * @return lower bound of the current range
     */
    int min();

    /**
     * Return the upper bound of the current range (inclusive)
     *
     * @return upper bound of the current range
     */
    int max();
}
