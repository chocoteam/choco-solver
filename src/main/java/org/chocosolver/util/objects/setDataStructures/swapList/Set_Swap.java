/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.util.objects.setDataStructures.swapList;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Iterator;

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
public class Set_Swap implements ISet {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int size, mapOffset;
	private int[] values, map;
	private ISetIterator iter = newIterator();

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	/**
	 * Creates an empty bipartite set having numbers greater or equal than <code>offSet</code> (possibly < 0)
	 * @param offSet smallest allowed value in this set (possibly < 0)
	 */
	public Set_Swap(int offSet){
		mapOffset = offSet;
		size = 0;
		values = new int[16];
		map = new int[16];
		for(int i=0;i<map.length;i++)map[i] = -1;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean add(int element) {
		assert element>=mapOffset;
		if (contain(element)) {
			return false;
		}
		int size = getSize();
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
		return true;
	}

	@Override
	public boolean remove(int element) {
		if (!contain(element)) {
			return false;
		}
		iter.notifyRemoving(element);
		int size = getSize();
		if (size > 1) {
			int idx = map[element-mapOffset];
			int replacer = values[size - 1];
			map[replacer-mapOffset] = idx;
			values[idx] = replacer;
			map[element-mapOffset] = size - 1;
			values[size - 1] = element;
		}
		addSize(-1);
		return true;
	}

	@Override
	public boolean contain(int element) {
		if(element<mapOffset || element >= mapOffset+map.length){
			return false;
		}
		return map[element-mapOffset] >= 0 && map[element-mapOffset] < getSize() && values[map[element-mapOffset]] == element;
	}

	@Override
	public int getSize() {
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
		return SetType.BIPARTITESET;
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
			public void notifyRemoving(int item) {
				if(idx>0 && item == values[idx-1]){
					idx--;
				}
			}
			@Override
			public boolean hasNext() {
				return idx < getSize();
			}
			@Override
			public Integer next() {
				idx ++;
				return values[idx-1];
			}
		};
	}
}
