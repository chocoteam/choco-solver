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
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/10/11
 */
public abstract class Disposable  {

    private boolean reusable = true;

    /**
     * Freeze the iterator, cannot be reused.
     */
    public void init() {
        reusable = false;
    }

    /**
     * This method allows to declare that the iterator is not used anymoure. It
     * can be reused by another object.
     */
    public void dispose() {
        reusable = true;
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *                                       operation is not supported by this Iterator.
     * @throws IllegalStateException         if the <tt>next</tt> method has not
     *                                       yet been called, or the <tt>remove</tt> method has already
     *                                       been called after the last call to the <tt>next</tt>
     *                                       method.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return true if the iterator is not free for use (you should create a new instance)
     */
    public final boolean isNotReusable() {
        return !reusable;
    }
}
