/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.swapList;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.util.objects.setDataStructures.AbstractSet;
import org.chocosolver.util.objects.setDataStructures.FixedIntArrayIterator;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Set of integers based on BipartiteSet implementation for small sets (arraylist inside)
 * BEWARE : CANNOT BOTH ADD AND REMOVE ELEMENTS DURING SEARCH
 *
 * @author : Charles Prud'homme, Jean-Guillaume FAGES
 */
public class Set_Swap2 extends AbstractSet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    private int size;
    protected final TIntArrayList values;
    protected final Swap2SetIterator iter = new Swap2SetIterator();

    //***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an empty bipartite set
	 */
	public Set_Swap2(){
		size = 0;
        values = new TIntArrayList(4);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    @Override
    public boolean add(int element) {
        if(!contains(element)){
            values.insert(size(), element);
            addSize(1);
            notifyObservingElementAdded(element);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int element) {
        int idx = values.indexOf(element);
        if (idx <= -1 || idx >= size()) {
            return false;
        }
        int size = size();
        if (size > 1) {
            swap(idx, size-1);
            if (iter.idx < size()) {
                if (idx == iter.idx - 1) {
                    iter.idx--;
                } else if (idx < iter.idx - 1) {
                    swap(idx, iter.idx - 1);
                    iter.idx--;
                }
            }
        }
        addSize(-1);
        notifyObservingElementRemoved(element);
        return true;
    }

    private void swap(int idx1, int idx2) {
        int value1 = values.get(idx1);
        int value2 = values.get(idx2);
        values.set(idx1, value2);
        values.set(idx2, value1);
    }

    @Override
    public boolean contains(int element) {
        int pos = values.indexOf(element);
        return pos > -1 && pos < size();
    }

    @Override
    public int size() {
        return size;
    }

    protected void setSize(int s) {
        size = s;
    }

    protected void addSize(int delta) {
        size += delta;
    }

    @Override
    public void clear() {
        setSize(0);
        notifyObservingCleared();
    }

    @Override
    public int min() {
        if(isEmpty()) throw new IllegalStateException("cannot find minimum of an empty set");
        int min = values.get(0);
        for(int i = 1; i< size(); i++){
            if(min > values.get(i)){
                min = values.get(i);
            }
        }
        return min;
    }

    @Override
    public int max() {
        if(isEmpty()) throw new IllegalStateException("cannot find maximum of an empty set");
        int max = values.get(0);
        for(int i = 1; i< size(); i++){
            if(max < values.get(i)){
                max = values.get(i);
            }
        }
        return max;
    }

    @Override
    public SetType getSetType() {
        return SetType.SMALLBIPARTITESET;
    }

    @Override
    public int[] toArray(){
        return values.toArray(0, size());
    }

    //***********************************************************************************
    // ITERATOR
    //***********************************************************************************

    @Override
    public ISetIterator iterator(){
        iter.reset();
        return iter;
    }

    @Override
    public ISetIterator newIterator() {
        return new FixedIntArrayIterator(toArray());
    }

    private class Swap2SetIterator implements ISetIterator {

        private int idx;

        @Override
        public void reset() {
            idx = 0;
        }

        @Override
        public boolean hasNext() {
            return idx < size();
        }

        @Override
        public int nextInt() {
            return values.get(idx++);
        }
    }
}
