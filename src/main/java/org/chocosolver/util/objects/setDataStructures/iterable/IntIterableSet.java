/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
public interface IntIterableSet extends ISet{

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
     * @return the value after aValue
     */
    int nextValue(int aValue);

    /**
     * @param aValue (exclusive)
     * @return the value before aValue
     */
    int previousValue(int aValue);

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