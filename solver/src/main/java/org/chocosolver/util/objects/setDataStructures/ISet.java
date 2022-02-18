/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures;

/**
 * Class representing a set of integers
 * Created by IntelliJ IDEA.
 * @since 9 feb. 2011, update 2016
 * @author chameau, Jean-Guillaume Fages
 */
public interface ISet extends Iterable<Integer>{

    /**
     * Use the following loop to iterate over this set without autoboxing.
     * <code>
     *
     *     // more readable but with autoboxing
     *     for(int value:set){
     *         ...
     *     }
     *
     *     // more verbose but without autoboxing
     *     ISetIterator iter = set.primitiveIterator();
     *     while(iter.hasNext()){
     *         int k = iter.next();
     *         ...
     *     }
     * </code>
     * Do not use this iterator to make nested loops over {@link ISet} (prefer {@link ISet#newIterator()})
     * @return the default iterator (singleton) of this set
     */
    ISetIterator iterator();

    /**
     * Creates a new iterator object, for nested loops only.
     * @return a new iterator for this set
     */
    ISetIterator newIterator();

    /**
     * Add element to the set
     *
     * @param element element to add
     * @return true iff element was not in the set and has been added
     */
    boolean add(int element);

    /**
     * Remove the first occurrence of element from the set
     *
     * @param element element to add
     * @return true iff element was in the set and has been removed
     */
    boolean remove(int element);

    /**
     * Test the existence of element in the set
     *
     * @param element element to add
     * @return true iff the set contains element
     */
    boolean contains(int element);

    /**
     * @return true iff the set is empty
     */
    default boolean isEmpty(){
        return size()==0;
    }

    /**
     * @return the number of elements in the set
     */
    int size();

    /**
     * Remove all elements from the set
     */
    void clear();

    /**
     * @return the smallest element in the set
     * throws an exception if the set is empty
     * Time complexity is linear for BIPARTITESET and LINKED_LIST (constant time otherwise)
     */
    int min();

    /**
     * @return the largest element in the set
     * throws an exception if the set is empty
     * Time complexity is linear for BIPARTITESET and LINKED_LIST (constant time otherwise)
     */
    int max();

    /**
     * @return the implementation type of this set
     */
    SetType getSetType();

    /**
     * Register an observer to this set. Observers are dynamic set data structures whose value depends on observed
     * sets. This method must be called by observers' constructors.
     * @param set The observer to register
     * @param idx This index of this set in the observing set.
     */
    void registerObserver(ISet set, int idx);

    /**
     * Operations to perform when an observed set has an element added.
     * @param element the element added to the observed set.
     * @param idx the index of the observed set.
     */
    default void notifyElementAdded(int element, int idx) {
        throw new UnsupportedOperationException();
    }

    /**
     * Operations to perform when an observed set has an element removed.
     * @param element the element removed to the observed set.
     * @param idx the index of the observed set.
     */
    default void notifyElementRemoved(int element, int idx) {
        throw new UnsupportedOperationException();
    }

    /**
     * Operations to perform when an observed set is cleared
     * @param idx the index of the observed set.
     */
    default void notifyCleared(int idx) {
        throw new UnsupportedOperationException();
    }

    /**
     * Copies the set in an array if integers
     * @return an array containing every integer of the set
     */
    default int[] toArray(){
        int[] a = new int[size()];
        int idx = 0;
        ISetIterator iter = iterator();
        while(iter.hasNext()){
            a[idx++] = iter.nextInt();
        }
        return a;
    }

    interface WithOffset {
        int getOffset();
    }
}
