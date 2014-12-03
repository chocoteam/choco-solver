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

package org.chocosolver.util.objects;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.structure.IndexedObject;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.util.iterators.DisposableIntIterator;

import java.util.ArrayList;

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
public class StoredIndexedBipartiteSet /*implements IStateIntVector */{

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
    private IndexedObject[] idxToObjects;

    /**
     * The first element of the list
     */
    protected IStateInt last;

    protected BipartiteSetIterator _cachedIterator;

    /**
     * @param environment
     * @param values:     a set of DIFFERENT positive integer values !
     */
    public StoredIndexedBipartiteSet(final IEnvironment environment, final int[] values) {
        buildList(environment, values);
    }

    /**
     * @param environment
     * @param values:     a set of IndexObjects which have different indexes !
     */
    public StoredIndexedBipartiteSet(final IEnvironment environment, final IndexedObject[] values) {
        final int[] intvalues = new int[values.length];
        for (int i = 0; i < intvalues.length; i++) {
            intvalues[i] = values[i].getObjectIdx();
        }
        buildList(environment, intvalues);
        idxToObjects = new IndexedObject[position.length];
        for (int i = 0; i < intvalues.length; i++) {
            idxToObjects[values[i].getObjectIdx()] = values[i];
        }
    }

    /**
     * @param environment
     * @param values:     a set of IndexObjects which have different indexes !
     */
    public StoredIndexedBipartiteSet(final IEnvironment environment, final ArrayList<IndexedObject> values) {
        final int[] intvalues = new int[values.size()];
        for (int i = 0; i < intvalues.length; i++) {
            intvalues[i] = values.get(i).getObjectIdx();
        }
        buildList(environment, intvalues);
        idxToObjects = new IndexedObject[position.length];
        for (int i = 0; i < intvalues.length; i++) {
            idxToObjects[values.get(i).getObjectIdx()] = values.get(i);
        }
    }

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

    /**
     * Create a stored bipartite set with a size.
     * Thus the value stored will go from 0 to nbValues.
     *
     * @param environment
     * @param nbValues
     */
    public StoredIndexedBipartiteSet(final IEnvironment environment, final int nbValues) {
        final int[] values = new int[nbValues];
        for (int i = 0; i < nbValues; i++) {
            values[i] = i;
        }
        buildList(environment, values);
    }

    /**
     * Increase the number of value watched.
     * BEWARE: be sure your are correctly calling this method.
     * It deletes everything already declared
     *
     * @param gap the gap the reach the expected size
     */
    public final void increaseSize(final int gap) {
        final int l = list.length;
        final int[] newList = new int[l + gap];
        for (int i = 0; i < l + gap; i++) {
            newList[i] = i;
        }
        int maxElt = 0;
        for (int i = 0; i < newList.length; i++) {
            if (newList[i] > maxElt) {
                maxElt = newList[i];
            }
        }
        final int[] newPosition = new int[maxElt + 1];
        for (int i = 0; i < newList.length; i++) {
            newPosition[newList[i]] = i;
        }
        // record already removed values
        final int end = last.get() + 1;
        final int[] removed = new int[list.length - end];
        System.arraycopy(list, end, removed, 0, list.length - end);

        this.list = newList;
        this.position = newPosition;
        final IEnvironment env = last.getEnvironment();
        this.last = null;
        this.last = env.makeInt(list.length - 1);
        for (int i = 0; i < removed.length; i++) {
            remove(removed[i]);
        }
    }

    public final int size() {
        return last.get() + 1;
    }

    public final boolean isEmpty() {
        return last.get() == -1;
    }

    public final void add(final int i) {
        throw new UnsupportedOperationException("adding element is not permitted in this structure (the list is only meant to decrease during search)");
    }

    public final void clear() {
        last.set(-1);
    }

    public final void removeLast() {
        remove(list[last.get()]);
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

    //we assume that the object belongs to the list
    public final void remove(final IndexedObject object) {
        remove(object.getObjectIdx());
    }

    public boolean contains(final int object) {
        return position[object] <= last.get();
    }

    public final boolean contains(final IndexedObject object) {
        return contains(object.getObjectIdx());
    }

    public final int get(final int index) {
        return list[index];
    }

    public final IndexedObject getObject(final int index) {
        return idxToObjects[list[index]];
    }

    public final int set(final int index, final int val) {
        throw new SolverException("setting an element is not permitted on this structure");
    }

    public final DisposableIntIterator getIterator() {
        if (_cachedIterator == null || !_cachedIterator.isReusable()) {
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

    //a is not in the list, returns its index k in the table from
    //the end of the list.
    //It basically means that a was the k element to be removed
    public final int findIndexOfInt(final int a) {
        return list.length - position[a];
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
         * @throws java.util.NoSuchElementException
         *          iteration has no more elements.
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
