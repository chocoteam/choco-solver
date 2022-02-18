/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.iterators;




/**
 * An interface for collections over native integers.
 * (among other things, this is useful for iterating domains)
 * Note that it does not extend Iterator, as we iterate over native int,
 * and not Object.
 * <p/>
 * DEVELOPPERS :
 * This class must be protected:
 * <p/>
 * Use directly DisposableIntIterator!!
 */

public interface IntIterator  {
    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the getIterator has more elements.
     */
    boolean hasNext();

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          iteration has no more elements.
     */
    int next();

    /**
     * Removes from the underlying collection the last element returned by the
     * getIterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an getIterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @throws java.lang.UnsupportedOperationException
     *          if the <tt>remove</tt>
     *          operation is not supported by this Iterator.
     * @throws java.lang.IllegalStateException
     *          if the <tt>next</tt> method has not
     *          yet been called, or the <tt>remove</tt> method has already
     *          been called after the last call to the <tt>next</tt>
     *          method.
     */
    void remove();
}
