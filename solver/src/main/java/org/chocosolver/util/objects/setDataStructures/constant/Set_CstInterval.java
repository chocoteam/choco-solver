/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.constant;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Constant Interval set of the form [min, max]
 * BEWARE: Cannot add/remove elements
 *
 * @author Jean-Guillaume Fages
 */
public class Set_CstInterval implements ISet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final int lb;
    private final int ub;
	private final ISetIterator iter = newIterator();

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Creates a constant set of integers encoded as an interval [min, max]
	 * @param min lowest value in the set
	 * @param max highest value in the set
	 */
	public Set_CstInterval(int min, int max) {
		if(min>max){
			throw new UnsupportedOperationException("Wrong interval definition ["+min+", "+max+"] for Set_CstInterval (lb should be lower or equal than ub)");
		}
		assert min!=Integer.MIN_VALUE;
		assert max!=Integer.MAX_VALUE;
		this.lb = min;
		this.ub = max;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean add(int element) {
		if(contains(element)){
			return false;
		}else {
			throw new UnsupportedOperationException("It is forbidden to add an element to a constant set (Set_CstInterval)");
		}
	}

	@Override
	public boolean remove(int element) {
		if(contains(element)) {
			throw new UnsupportedOperationException("It is forbidden to remove an element from a constant set (Set_CstInterval)");
		}else{
			return false;
		}
	}

	@Override
	public boolean contains(int element) {
		return lb<=element && element<=ub;
	}

	@Override
	public int size() {
		return ub-lb+1;
	}

	@Override
	public void clear() {
		if(!isEmpty()) {
			throw new UnsupportedOperationException("It is forbidden to remove an element from a constant set (Set_CstInterval)");
		}
	}

	@Override
	public SetType getSetType(){
		return SetType.FIXED_INTERVAL;
	}

    @Override
    public void registerObserver(ISet set, int idx) {
        // Set is fixed, no need to register.
    }

    @Override
	public int min() {
		return lb;
	}

	@Override
	public int max() {
		return ub;
	}

	@Override
	public String toString(){
		return "["+lb+","+ub+"]";
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
			private int value = lb;
			@Override
			public void reset() {
				value = lb;
			}
			@Override
			public boolean hasNext() {
				return value <= ub;
			}
			@Override
			public int nextInt() {
				value++;
				return value-1;
			}
		};
	}
}
