/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.linkedlist;

import org.chocosolver.util.objects.setDataStructures.AbstractSet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetType;

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
public class Set_LinkedList extends AbstractSet {

	//***********************************************************************************
	// VARIABLE
	//***********************************************************************************

	private IntCell first, last;
	private int size;
	private IntCell poolGC;
	private final ISetIterator iter = newIterator();

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean contains(int element) {
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
		if(contains(element)){
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
		notifyObservingElementAdded(element);
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
			notifyObservingElementRemoved(element);
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
					notifyObservingElementRemoved(element);
					return true;
				}
				previous = current;
				current = current.next;
			}
			return false;
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
        notifyObservingCleared();
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
		return SetType.LINKED_LIST;
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
			private IntCell nextCell = first;
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
			public int nextInt() {
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
