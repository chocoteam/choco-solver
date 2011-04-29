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

package choco.kernel.memory;

import choco.kernel.common.util.iterators.DisposableIntIterator;

import java.io.Serializable;

/**
 * Describes an search vector with states (describing some history of the data structure).
 */
public interface IStateIntVector extends Serializable {

    /**
     * Minimal capacity of a vector
     */
    int MIN_CAPACITY = 8;

    /**
     * Returns the current size of the stored search vector.
     */

    int size();

    /**
     * Checks if the vector is empty.
     */

    boolean isEmpty();

    /**
     * Adds a new search at the end of the vector.
     *
     * @param i The search to add.
     */

    void add(int i);

    boolean contains(int val);

    /**
     * Removes an int.
     *
     * @param i The search to remove.
     */

    void remove(int i);


    /**
     * removes the search at the end of the vector.
     * does nothing when called on an empty vector
     */

    void removeLast();

    /**
     * Returns the <code>index</code>th element of the vector.
     */

    int get(int index);

    /**
     * access an element without any bound check
     *
     * @param index
     * @return
     */
    int quickGet(int index);

    /**
     * Assigns a new value <code>val</code> to the element <code>index</code> and returns
     * the old value
     */

    int set(int index, int val);

    /**
     * Assigns a new value val to the element indexth and return the old value without bound check
     *
     * @param index the index where the value is modified
     * @param val   the new value
     * @return the old value
     */
    int quickSet(int index, int val);

    DisposableIntIterator getIterator();
}
