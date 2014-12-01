/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.util.objects.queues;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * A linked list where element is added after the last element, and where elements are popped randomly
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/02/11
 */
public class LinkedList<E> implements AQueue<E>, Serializable {

    transient int size; // could be computed, but more efficient to maintain

    static class Entry<E> {
        public E element;

        public Entry<E> next;

        public Entry<E> previous;
    }

    Entry<E> header;

    Entry<E> free; // should be cleared sometimes, to help GC

    @SuppressWarnings({"unchecked"})
    public LinkedList() {
        header = new Entry<>();
        header.next = header.previous = header;
        free = new Entry<>();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    private Entry<E> entry(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index +
                    ", Size: " + size);
        Entry<E> e = header;
        if (index < (size >> 1)) {
            for (int i = 0; i <= index; i++)
                e = e.next;
        } else {
            for (int i = size; i > index; i--)
                e = e.previous;
        }
        return e;
    }

    public E get(int index) {
        return getEntry(index).element;
    }

    Entry<E> getEntry(int index) {
        return entry(index);
    }

    private Entry<E> addBefore(E e, Entry<E> entry) {
        Entry<E> newEntry;
        if (free.next != null) {
            newEntry = free.next;
            free.next = newEntry.next;
        } else {
            newEntry = new Entry<>();
        }

        newEntry.element = e;
        newEntry.next = entry;
        newEntry.previous = entry.previous;

        newEntry.previous.next = newEntry;
        newEntry.next.previous = newEntry;
        size++;
        return newEntry;
    }

    public boolean addFirst(E e) {
        addBefore(e, header.next);
        return true;
    }

    public boolean addLast(E e) {
        addBefore(e, header);
        return true;
    }

    E remove(Entry<E> e) {
        if (e == header)
            throw new NoSuchElementException();

        E result = e.element;

        e.previous.next = e.next;
        e.next.previous = e.previous;
        e.next = e.previous = null;
        e.element = null;

        e.next = free.next;
        free.next = e;

        size--;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public E pollFirst() {
        return remove(header.next);
    }

    /**
     * {@inheritDoc}
     */
    public E pollLast() {
        return remove(header.previous);
    }

    /**
     * {@inheritDoc}
     */
    public E remove(int index) {
        return remove(getEntry(index));
    }


    public boolean remove(E o) {
        for (Entry<E> e = header.next; e != header; e = e.next) {
            if (o.equals(e.element)) {
                remove(e);
                return true;
            }
        }
        return false;
    }

    public int indexOf(E o) {
        assert o != null;
        int index = 0;
        for (Entry e = header.next; e != header; e = e.next) {
            if (o == e.element)
                return index;
            index++;
        }
        return -1;
    }

}
