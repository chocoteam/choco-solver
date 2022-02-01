/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.structure.IndexedObject;
import org.chocosolver.util.iterators.DisposableIntIterator;

/**
 * A stored list dedicated to positive integers and three operations :
 * - iteration
 * - removal of an element
 * - check if an element is or not within the list
 * It only requires a StoredInt to denote the first element of the list
 * and proceeds by swapping element with the first one to remove them and incrementing
 * the index of the first element.
 * IT DOES NOT PRESERVE THE ORDER OF THE LIST
 */
public class StoredIndexedBipartiteSet {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    /**
     * The list of values
     */
    protected int[] list;

    /**
     * The position of each element within the list.
     * indexes[3] = k <=> list[k] = 3
     * we assume that elements ranges from 0 ... list.lenght
     * in other words the elements must be indexed.
     */
    protected int[] position;

    /**
     * If objects are added to the list, a mapping from their
     * indexes is needed.
     * idxToObjects[i] = o <=> o.getObjectIdx() == i
     */
    protected IndexedObject[] idxToObjects;

    /**
     * The first element of the list
     */
    protected IStateInt last;

    protected BipartiteSetIterator _cachedIterator;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * @param environment the environment
     * @param values     a set of DIFFERENT positive integer values !
     */
    public StoredIndexedBipartiteSet(final IEnvironment environment, final int[] values) {
        buildList(environment, values);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public void buildList(final IEnvironment environment, final int[] values) {
        this.list = values;
        int maxElt = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > maxElt) {
                maxElt = values[i];
            }
        }
        this.position = new int[maxElt + 1];
        for (int i = 0; i < values.length; i++) {
            position[values[i]] = i;
        }
        this.last = environment.makeInt(list.length - 1);
    }

    public final int size() {
        return last.get() + 1;
    }

    public final boolean isEmpty() {
        return last.get() == -1;
    }

    public void remove(final int object) {
        if (contains(object)) {
            final int idxToRem = position[object];
            if (idxToRem == last.get()) {
                last.add(-1);
            } else {
                final int temp = list[last.get()];
                list[last.get()] = object;
                list[idxToRem] = temp;
                position[object] = last.get();
                position[temp] = idxToRem;
                last.add(-1);
            }
        }
    }

    public boolean contains(final int object) {
        return position[object] <= last.get();
    }

    public final DisposableIntIterator getIterator() {
        if (_cachedIterator == null || _cachedIterator.isNotReusable()) {
            _cachedIterator = new BipartiteSetIterator();
        }
        _cachedIterator.init(list, position, last, idxToObjects);
        return _cachedIterator;
    }

    public final String pretty() {
        final StringBuilder s = new StringBuilder("[");
        for (int i = 0; i <= last.get(); i++) {
            s.append(list[i]).append(i == (last.get()) ? "" : ",");
        }
        return s.append(']').toString();
    }

    /**
     * DO NOT USE : FOR MEMORY OPTIM ONLY
     */
    public final int[] _getStructure() {
        return list;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static class BipartiteSetIterator extends DisposableIntIterator {

        private int[] list;

        private int[] position;

        private IndexedObject[] idxToObjects;

        private IStateInt last;

        private int nlast, idx;

        /**
         * Freeze the iterator, cannot be reused.
         */
        public void init(final int[] aList, final int[] aPosition, final IStateInt aLast, final IndexedObject[] anIdxToObjects) {
            super.init();
            idx = 0;
            list = aList;
            position = aPosition;
            idxToObjects = anIdxToObjects;
            last = aLast;
            nlast = last.get();
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
            return idx <= nlast;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws java.util.NoSuchElementException iteration has no more elements.
         */
        @Override
        public int next() {
            return list[idx++];
        }


        public IndexedObject nextObject() {
            return idxToObjects[list[idx++]];
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
        @Override
        public void remove() {
            idx--;
            final int idxToRem = idx;
            if (idxToRem == nlast) {
                last.add(-1);
                nlast--;
            } else {
                final int temp = list[nlast];
                list[nlast] = list[idxToRem];
                list[idxToRem] = temp;
                position[list[nlast]] = last.get();
                position[temp] = idxToRem;
                last.add(-1);
                nlast--;
            }
        }
    }
}
