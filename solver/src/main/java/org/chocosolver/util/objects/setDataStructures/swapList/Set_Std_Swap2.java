/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.swapList;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Set of integers based on BipartiteSet implementation for small sets (arraylist inside)
 * BEWARE : CANNOT BOTH ADD AND REMOVE ELEMENTS DURING SEARCH
 *
 * @author : Charles Prud'homme, Jean-Guillaume FAGES (fix remove)
 */
public class Set_Std_Swap2 implements ISet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

    protected IStateInt size;
    protected TIntArrayList values;
    private ISetIterator iter = newIterator();


    //***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an empty bipartite set
	 * @param e backtracking environment
	 */
	public Set_Std_Swap2(IEnvironment e){
		size = e.makeInt(0);
        values = new TIntArrayList(4);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    @Override
    public boolean add(int element) {
        if(!contains(element)){
            int pos = size.add(1);
            values.insert(pos - 1, element);
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
            size.add(-1);
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
        return size.get();
    }

    @Override
    public void clear() {
        size.set(0);
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
