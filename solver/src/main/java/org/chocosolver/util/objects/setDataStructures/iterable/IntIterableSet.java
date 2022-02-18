/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.iterable;

import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * An interface to store a set of values, to be used with
 * {@link org.chocosolver.solver.variables.IntVar#removeValues(IntIterableSet, org.chocosolver.solver.ICause)} and
 * {@link org.chocosolver.solver.variables.IntVar#removeAllValuesBut(IntIterableSet, org.chocosolver.solver.ICause)}
 * Created by cprudhom on 09/07/15.
 * Project: choco.
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public interface IntIterableSet extends ISet {

    /**
     * Adds all of the elements in the array to this set.
     * @param values array containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     */
    boolean addAll(int... values);

    /**
     * Adds all of the elements in the specified set to this set.
     * @param set set containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     */
    boolean addAll(IntIterableSet set);

    /**
     * Retains only the elements in this set that are contained in the
     * specified set.  In other words, removes from
     * this set all of its elements that are not contained in the
     * specified set.
     *
     * @param set set containing elements to be retained in this set
     * @return <tt>true</tt> if this set changed as a result of the call
     */
    boolean retainAll(IntIterableSet set);

    /**
     * Removes all of this set's elements that are also contained in the
     * specified set.  After this call returns,
     * this set will contain no elements in common with the specified
     * set.
     *
     * @param set set containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the
     *         call
     */
    boolean removeAll(IntIterableSet set);

    /**
     * Removes all values between <i>f</i> (inclusive) and <i>t</i> (inclusive)
     * @param f first value to remove
     * @param t last value to remove
     * @return <tt>true</tt> if this set changed as a result of the call
     */
    boolean removeBetween(int f, int t);

    /**
     * @param aValue (exclusive)
     * @return the value after 'aValue' or {@link Integer#MAX_VALUE}
     */
    int nextValue(int aValue);


    /**
     * @param aValue (exclusive)
     * @return the value outside thisn after 'aValue'
     */
    int nextValueOut(int aValue);

    /**
     * @param aValue (exclusive)
     * @return the value before 'aValue' or or {@link Integer#MIN_VALUE}
     */
    int previousValue(int aValue);

    /**
     * @param aValue (exclusive)
     * @return the value outside this, before'aValue'
     */
    int previousValueOut(int aValue);

    /**
     * Returns a carbon-copy of this set
     * @return a carbon-copy of this set
     */
    IntIterableSet duplicate();

    /**
     * add the value x to all integers stored in this set
     *
     * @param x value to add
     */
    void plus(int x);

    /**
     * subtract the value x to all integers stored in this set
     *
     * @param x value to add
     */
    void minus(int x);
}