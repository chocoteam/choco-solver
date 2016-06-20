/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.util.objects.setDataStructures.constant;

import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Arrays;
import java.util.Iterator;

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
		if(contain(element))return false;
		throw new UnsupportedOperationException("Cannot add element to Set_FixedArray");
	}

	@Override
	public boolean remove(int element) {
		if(!contain(element))return false;
		throw new UnsupportedOperationException("Cannot remove element from Set_FixedArray");
	}

	@Override
	public boolean contain(int element) {
		return Arrays.binarySearch(values,element) >= 0;
	}

	@Override
	public int getSize() {
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
		String st = "{";
		for(int i:this){
			st+=i+", ";
		}
		st+="}";
		return st.replace(", }","}");
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
	public Iterator<Integer> iterator(){
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
			public Integer next() {
				idx ++;
				return values[idx-1];
			}
		};
	}
}
