/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.constant;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Arrays;

/**
 * Fixed array of integers (cannot add nor remove items)
 *
 * @author : Jean-Guillaume Fages, jimmy
 */
public class Set_FixedArray implements ISet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected final int size;
	protected final int[] values;
	protected ISetIterator iter = newIterator();

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an empty array of integers
	 */
	public Set_FixedArray(int[] vls){
		values = new TIntHashSet(vls).toArray();
		Arrays.sort(values);
		size = values.length;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean add(int element) {
		if(contains(element))return false;
		throw new UnsupportedOperationException("Cannot add element to Set_FixedArray");
	}

	@Override
	public boolean remove(int element) {
		if(!contains(element))return false;
		throw new UnsupportedOperationException("Cannot remove element from Set_FixedArray");
	}

	@Override
	public boolean contains(int element) {
		return Arrays.binarySearch(values,element) >= 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		if(size>0){
			throw new UnsupportedOperationException("Cannot clear Set_FixedArray");
		}
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
	public SetType getSetType(){
		return SetType.FIXED_ARRAY;
	}

	@Override
	public int min() {
		if(isEmpty()) throw new IllegalStateException("cannot find minimum of an empty set");
		return values[0];
	}

	@Override
	public int max() {
		if(isEmpty()) throw new IllegalStateException("cannot find maximum of an empty set");
		return values[size-1];
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
	public ISetIterator newIterator(){
		return new ISetIterator() {
			private int idx;
			@Override
			public void reset() {
				idx = 0;
			}
			@Override
			public boolean hasNext() {
				return idx < size;
			}
			@Override
			public int nextInt() {
				idx ++;
				return values[idx-1];
			}
		};
	}
}
