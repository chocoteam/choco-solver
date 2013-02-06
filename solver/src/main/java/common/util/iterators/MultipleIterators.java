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

package common.util.iterators;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 18 nov. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class MultipleIterators<E> extends DisposableIterator<E> {

    static MultipleIterators _iterator;

    public static <E> MultipleIterators getMultipleIterators(DisposableIterator<E>... its) {
        if (_iterator == null) {
            _iterator = new MultipleIterators<E>(its);
        } else {
            _iterator.init(its);
        }
        return _iterator;
    }


    DisposableIterator<E>[] its;
    int idx = 0;

    public MultipleIterators(DisposableIterator<E>[] its) {
        init(its);
    }

    public void init(DisposableIterator<E>[] its) {
        super.init();
        this.its = its;
        idx = 0;
    }

    /**
     * This method allows to declare that the iterator is not usefull anymoure. It
     * can be reused by another object.
     */
    @Override
    public void dispose() {
        super.dispose();
        for (DisposableIterator<E> it : its) {
            it.dispose();
        }
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    @Override
    public boolean hasNext() {
        while (idx < its.length
                && !its[idx].hasNext()) {
            idx++;
        }
        return (idx < its.length && its[idx].hasNext());
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          iteration has no more elements.
     */
    @Override
    public E next() {
        return its[idx].next();
    }
}
