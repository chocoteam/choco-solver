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

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.util.objects.setDataStructures.*;

import java.util.Arrays;

/**
 * Bipartite set of integers:
 *
 * add : O(1)
 * contain: O(1)
 * remove: O(1)
 * iteration : O(m)
 *
 * @author : Jean-Guillaume Fages
 */
public class Set_Swap extends AbstractSet implements ISet.WithOffset {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int size;
    private final int mapOffset;
	private int[] values;
	private int[] map;
	private final MyISetIterator iter = new MyISetIterator();

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an empty bipartite set having numbers greater or equal than <code>offSet</code> (possibly < 0)
	 * @param offSet smallest allowed value in this set (possibly < 0)
	 */
	public Set_Swap(int offSet) {
		mapOffset = offSet;
		size = 0;
		values = new int[16];
		map = new int[16];
		Arrays.fill(map, -1);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public int getOffset() {
		return mapOffset;
	}

	/**
	 * Get the nth element in the set
	 * @param nth must respect : 0 <= nth <size
	 * @return the nth element in this
	 */
	public int getNth(int nth){
		if(nth<0 || nth>=size()) throw new SolverException(" invalid getNth parameter, must  be 0 <= nth ("+nth+") < size ("+size()+")");
		return values[nth];
	}

	@Override
	public boolean add(int element) {
		if(element < mapOffset) throw new IllegalStateException("Cannot add "+element+" to set of offset "+mapOffset);
		if (contains(element)) {
			return false;
		}
		int size = size();
		if (size == values.length) {
			int[] tmp = values;
			int ns = tmp.length + 1 + (tmp.length * 2) / 3;
			values = new int[ns];
			System.arraycopy(tmp, 0, values, 0, tmp.length);
		}
		if(element-mapOffset>=map.length){
			int[] tmp = map;
			int ns = Math.max(
					element-mapOffset+1,
					tmp.length + 1 + (tmp.length * 2) / 3
			);
			map = new int[ns];
			System.arraycopy(tmp, 0, map, 0, tmp.length);
			for(int i=tmp.length;i<ns;i++){
				map[i] = -1;
			}
		}
		values[size] = element;
		map[element-mapOffset] = size;
		addSize(1);
		notifyObservingElementAdded(element);
		return true;
	}

	@Override
	public boolean remove(int element) {
		if (!contains(element)) {
			return false;
		}
		int size = size();
		if (size > 1) {
			int idx = map[element-mapOffset];
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
		int value1 = values[idx1];
		int value2 = values[idx2];
		map[value2 - mapOffset] = idx1;
		values[idx1] = value2;
		map[value1 - mapOffset] = idx2;
		values[idx2] = value1;
	}

	@Override
	public boolean contains(int element) {
		if (element < mapOffset || element >= mapOffset + map.length) {
			return false;
		}
		return map[element - mapOffset] >= 0 && map[element - mapOffset] < size() && values[map[element - mapOffset]] == element;
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
		int min = values[0];
		for(int i = 1; i< size(); i++){
			if(min > values[i]){
				min = values[i];
			}
		}
		return min;
	}

	@Override
	public int max() {
		if(isEmpty()) throw new IllegalStateException("cannot find maximum of an empty set");
		int max = values[0];
		for(int i = 1; i< size(); i++){
			if(max < values[i]){
				max = values[i];
			}
		}
		return max;
	}

	@Override
	public SetType getSetType(){
		return SetType.BIPARTITESET;
	}

	@Override
	public int[] toArray(){
		int[] valuesScreenshot = new int[size()];
		System.arraycopy(values,0,valuesScreenshot,0,size());
		return valuesScreenshot;
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

	private class MyISetIterator implements ISetIterator {
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
			return values[idx++];
		}
	}
}
