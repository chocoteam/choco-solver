/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.swapList;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.util.objects.setDataStructures.AbstractSet;
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

    protected int size;
    protected TIntArrayList values;
    private final ISetIterator iter = newIterator();


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
            values.insert(size, element);
            size++;
            notifyObservingElementAdded(element);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(int element) {
        int pos = values.indexOf(element);
        int s = size();
        if(pos > -1 && pos < s){
            iter.notifyRemoving(element);
            s--;
            int t = values.get(s);
            values.set(pos, t);
            values.set(s, element);
            size--;
            notifyObservingElementRemoved(element);
            return true;
        }else return false;
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

    @Override
    public void clear() {
        size = 0;
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
	public String toString() {
		StringBuilder st = new StringBuilder("{");
		ISetIterator iter = newIterator();
		while (iter.hasNext()) {
			st.append(iter.nextInt()).append(", ");
		}
		st.append("}");
		return st.toString().replace(", }","}");
	}

    @Override
    public ISetIterator iterator(){
        iter.reset();
        return iter;
    }

    @Override
    public ISetIterator newIterator(){
        return new ISetIterator() {
            private int idx;
            @Override
            public void reset() {
                idx = 0;
            }
            @Override
            public void notifyRemoving(int item) {
                if(idx>0 && item == values.get(idx-1)){
                    idx--;
                }
            }
            @Override
            public boolean hasNext() {
                return idx < size();
            }
            @Override
            public int nextInt() {
                idx ++;
                return values.get(idx-1);
            }
        };
    }
}
