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
package org.chocosolver.util.objects.setDataStructures.linkedlist;

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Iterator;

/**
 * LinkedList of m elements
 * add : O(1)
 * testPresence: O(m)
 * remove: O(m)
 * iteration : O(m)
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages, chameau
 * Date: 9 fevr. 2011
 */
public class Set_LinkedList implements ISet {

	//***********************************************************************************
	// VARIABLE
	//***********************************************************************************

	private IntCell first, last;
	private int size;
	private IntCell poolGC;
	private ISetIterator iter = newIterator();

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public boolean contain(int element) {
		IntCell cell = first;
		while(cell != null){
			if(cell.element == element){
				return true;
			}
			cell = cell.next;
		}
		return false;
	}

	@Override
	public boolean add(int element) {
		if(contain(element)){
			return false;
		}
		if (poolGC == null) {
			first = new IntCell(element, first);
		} else {
			IntCell recycled = poolGC;
			poolGC = poolGC.next;
			recycled.init(element, first);
			first = recycled;
		}
		if(last==null){
			assert size==0;
			last=first;
		}
		this.size++;
		return true;
	}

	@Override
	public boolean remove(int element) {
		if(first == null){
			return false;
		} else if(first.element == element){
			iter.notifyRemoving(element);
			first = first.next;
			if(first==null)last=null;
			size--;
			return true;
		}else {
			IntCell previous = first;
			IntCell current = first.next;
			while (current != null) {
				if (current.element == element) {
					iter.notifyRemoving(element);
					previous.next = current.next;
					if(previous.next==null) last = previous;
					current.next = poolGC;
					poolGC = current;
					size--;
					return true;
				}
				previous = current;
				current = current.next;
			}
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void clear() {
		if (first != null) {
			last.next = poolGC;
			poolGC = first;
		}
		first = null;
		last = null;
		size = 0;
	}

	@Override
	public int min() {
		if(isEmpty()) throw new IllegalStateException("cannot find minimum of an empty set");
		IntCell current = first;
		int min = current.element;
		while(current.next!=null){
			current = current.next;
			if(min > current.element){
				min = current.element;
			}
		}
		return min;
	}

	@Override
	public int max() {
		if(isEmpty()) throw new IllegalStateException("cannot find maximum of an empty set");
		IntCell current = first;
		int max = current.element;
		while(current.next!=null){
			current = current.next;
			if(max < current.element){
				max = current.element;
			}
		}
		return max;
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
		return SetType.LINKED_LIST;
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
			private IntCell nextCell;
			@Override
			public void reset() {
				nextCell = first;
			}
			@Override
			public void notifyRemoving(int item) {
				if(nextCell != null && nextCell.element == item){
					nextCell = nextCell.next;
				}
			}
			@Override
			public boolean hasNext() {
				return nextCell != null;
			}
			@Override
			public Integer next() {
				int e = nextCell.element;
				nextCell = nextCell.next;
				return e;
			}
		};
	}

	//***********************************************************************************
	// STRUCTURE
	//***********************************************************************************

	private class IntCell  {

		private int element;
		private IntCell next;

		public IntCell(int element, IntCell next) {
			init(element, next);
		}

		public void init(int element, IntCell next) {
			this.element = element;
			this.next = next;
		}

		@Override
		public String toString(){
			return element+"";
		}
	}
}
