/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.bitset;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.structure.S64BitSet;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * BitSet implementation for a set of integers
 * Supports negative numbers if offset is set properly
 *
 * @author : chameau, Jean-Guillaume Fages
 */
public class Set_Std_BitSet implements ISet {

	//***********************************************************************************
	// VARIABLE
	//***********************************************************************************

	private IStateInt card;	// enables to get the cardinality in O(1)
	private int offset;		// allow using negative numbers
	private S64BitSet values;
	private ISetIterator iter = newIterator();

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an empty bitset having numbers greater or equal than <code>offSet</code> (possibly < 0)
	 *
	 * @param environment backtracking environment
	 * @param offSet smallest allowed value in the set
	 */
	public Set_Std_BitSet(IEnvironment environment, int offSet) {
		values = new S64BitSet(environment);
		card = environment.makeInt(0);
		offset = offSet;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean add(int element) {
		if(element < offset) throw new IllegalStateException("Cannot add "+element+" to set of offset "+offset);
		if (values.get(element-offset)) {
			return false;
		}else{
			card.add(1);
			values.set(element-offset, true);
			return true;
		}
	}

	@Override
	public boolean remove(int element) {
		if(contains(element)) {
			values.set(element - offset, false);
			card.add(-1);
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean contains(int element) {
		return element >= offset && values.get(element - offset);
	}

	@Override
	public int size() {
		return this.card.get();
	}

	@Override
	public void clear() {
		values.clear();
		card.set(0);
	}

	@Override
	public SetType getSetType(){
		return SetType.BITSET;
	}

	@Override
	public int min() {
		if(isEmpty()) throw new IllegalStateException("cannot find minimum of an empty set");
		return offset+ values.nextSetBit(0);
	}

	@Override
	public int max() {
		if(isEmpty()) throw new IllegalStateException("cannot find maximum of an empty set");
		return offset+ values.prevSetBit(values.length());
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
			private int current = -1;
			@Override
			public void reset() {
				current = -1;
			}
			@Override
			public boolean hasNext() {
				return values.nextSetBit(current+1) >= 0;
			}
			@Override
			public int nextInt() {
				current = values.nextSetBit(current + 1);
				return current+offset;
			}
		};
	}
}
