/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.dynamic;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Abstract class for defining dynamic sets with predicate-based filtering.
 * This type of set is useful to define set objects that do not store data but instead reflect the state of an object
 * (or a collection of objects) according to a property defined by a predicate (to implement in the `contains` method).
 *
 * This class is abstract an must be extended according to the observed object(s) by implementing (at least)
 * the `contains` method and a SetDynamicFilterIterator (iterator dedicated to SetDynamicFilter) implementing the
 * `resetPointers` and `findNext` methods. Default `min` and `max` methods are provided but it is recommended to
 * override them if the observed object(s) provide a quick way to get these values without iterating.
 *
 * @author Dimitri Justeau-Allaire
 * @since 09/03/2021
 */
public abstract class SetDynamicFilter implements ISet {

    protected SetDynamicFilterIterator iter = createIterator();

    @Override
    public SetDynamicFilterIterator iterator() {
        iter.reset();
        return iter;
    }

    /**
     * @return a new instance of SetDynamicFilterIterator adapted to the observed object(s)
     */
    protected abstract SetDynamicFilterIterator createIterator();

    @Override
    public SetDynamicFilterIterator newIterator() {
        SetDynamicFilterIterator iterator = createIterator();
        iterator.reset();
        return iterator;
    }

    @Override
    public boolean add(int element) {
        throw new UnsupportedOperationException("this set is read-only");
    }

    @Override
    public boolean remove(int element) {
        throw new UnsupportedOperationException("this set is read-only");
    }

    /**
     * The contains method of a SetDynamicFilter is the predicate on which is based the set.
     * Given an element, this predicate determines whether the set contains or not the element.
     * @param element the element to test the existence on.
     * @return True if the predicate is satisfied by the element, thus in the set.
     */
    @Override
    public abstract boolean contains(int element);

    @Override
    public void clear() {
        throw new UnsupportedOperationException("this set is read-only");
    }

    @Override
    public int size() {
        int size = 0;
        SetDynamicFilterIterator iterator = newIterator();
        while (iterator.hasNext()) {
            size++;
            iterator.findNext();
        }
        return size;
    }

    @Override
    public int min() {
        int minVal = Integer.MAX_VALUE;
        SetDynamicFilterIterator iterator = newIterator();
        while (iterator.hasNext()) {
            int current = iterator.nextInt();
            if (current < minVal) {
                minVal = current;
            }
        }
        return minVal;
    }

    @Override
    public int max() {
        int maxVal = Integer.MIN_VALUE;
        SetDynamicFilterIterator iterator = newIterator();
        while (iterator.hasNext()) {
            int current = iterator.nextInt();
            if (current > maxVal) {
                maxVal = current;
            }
        }
        return maxVal;
    }

    @Override
    public SetType getSetType() {
        return SetType.DYNAMIC;
    }

    @Override
    public void registerObserver(ISet set, int idx) {
        throw new UnsupportedOperationException("SetDynamicFilter is not (yet) observable");
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("{");
        ISetIterator iter = newIterator();
        while (iter.hasNext()) {
            st.append(iter.nextInt()).append(", ");
        }
        st.append("}");
        return st.toString().replace(", }","}");
    }

    /**
     * Specific iterator for dynamic filter sets.
     */
    public abstract class SetDynamicFilterIterator implements ISetIterator {

        protected Integer next = null;

        /**
         * Reset internal pointers such that the next call to nextInt() will return the first value of the set.
         * /!\ This method must NOT call findNext() after resetting internal pointer, this is done by reset().
         */
        protected abstract void resetPointers();

        @Override
        public void reset() {
            resetPointers();
            findNext();
        }

        @Override
        public int nextInt() {
            int value = next;
            findNext();
            return value;
        }

        /**
         * Finds the next value and assign it to this.next.
         * If there is no next value, this.next must be assigned to null.
         */
        protected abstract void findNext();

        @Override
        public boolean hasNext() {
            return next != null;
        }
    }
}
