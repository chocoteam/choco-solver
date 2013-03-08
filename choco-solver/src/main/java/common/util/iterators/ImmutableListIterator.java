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

import java.util.ListIterator;

public class ImmutableListIterator<E> implements ListIterator<E> {

    private final static String MSG = "can not modify the list";

    private final ListIterator<E> iter;

    /**
     *
     */
    public ImmutableListIterator(final ListIterator<E> iter) {
        super();
        this.iter = iter;
    }

    @Override
    public void add(final E e) {
        throw new UnsupportedOperationException(MSG);

    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return iter.hasPrevious();
    }

    @Override
    public E next() {
        return iter.next();
    }

    @Override
    public int nextIndex() {
        return iter.nextIndex();
    }

    @Override
    public E previous() {
        return iter.previous();
    }

    @Override
    public int previousIndex() {
        return iter.previousIndex();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void set(final E e) {
        throw new UnsupportedOperationException(MSG);

    }

}
