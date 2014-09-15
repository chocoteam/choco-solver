/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package util.objects.setDataStructures.linkedlist;

import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetType;

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

    /**
     * The first cell of the linked list
     */
    protected IntCell first;
	protected IntCell last;
    protected int size;
    protected IntCell nextCell; // enables to iterate
    protected IntCell poolGC;

    public Set_LinkedList() {
        this.first = null;
		this.last = null;
        nextCell = null;
        this.size = 0;
        poolGC = null;
    }

    @Override
    /**
     * Test for an empty list
     * @return true iff the list is empty
     */
    public boolean isEmpty() {
        return this.first == null;
    }

    @Override
    /**
     * The number of elements in the linked list
     * @return the number of cells
     */
    public int getSize() {
        return this.size;
    }

    @Override
    /**
     * Check if the linked list contain the value element
     * @param element an int
     * @return true iff the linked list contain the value element
     */
    public boolean contain(int element) {
        boolean res = false;
        IntCell current = first;
        while (!res && current != null) {
            if (current.element == element) {
                res = true;
            }
            current = current.next;
        }
        return res;
    }

    @Override
    /**
     * Add an element in the first position. Beware, there is no garanty this element does not exist in the linked list
     * BEWARE if an element is added during an iteration, as it is before the first element, then this element will not appear
     * @param element an int
     */
    public boolean add(int element) {
        if (poolGC == null) {
            this.first = new IntCell(element, first);
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
    /**
     * Remove the first occurrence of the element.
     * @param element an int
     * @return true iff the element has been effectively removed
     */
    public boolean remove(int element) {
        IntCell current = first;
        IntCell previous = null;
        boolean removed = false;
        while ((!removed) && current != null) {
            if (current.element == element) {
                if (current == nextCell) {
                    nextCell = nextCell.next;
                }
                IntCell old = poolGC;
                poolGC = current;
                if (previous == null) {
                    this.first = current.next;
					if(first==null){
						last = null;
					}
                } else {
                    previous.next = current.next;
					if(current.next==null){
						last = previous;
					}
                }
                removed = true;
                poolGC.next = old;
            }
            previous = current;
            current = current.next;
        }
        if (removed) {
            this.size--;
        }
        return removed;
    }

    @Override
    public String toString() {
        String res = "";
        IntCell current = first;
        while (current != null) {
            res += current;
            current = current.next;
        }
        return res;
    }

    @Override
    public void clear() {
        if (first != null) {
////            first.next = poolGC;
			last.next = poolGC;
            poolGC = first;
        }
        first = null;
		last = null;
        nextCell = null;
        size = 0;
    }

    // --- Iterations
    @Override
    public int getFirstElement() {
        if (first == null) {
            return -1;
        }
        nextCell = first.next;
        return first.element;
    }

    @Override
    public int getNextElement() {
        if (nextCell == null) {
            return -1;
        }
        int el = nextCell.element;
        nextCell = nextCell.next;
        return el;
    }

	@Override
	public SetType getSetType(){
		return SetType.LINKED_LIST;
	}

	@Override
	public int[] toArray(){
		int[] a = new int[getSize()];
		int idx = 0;
		for(int i=getFirstElement();i>=0;i=getNextElement()){
			a[idx++] = i;
		}
		return a;
	}

	@Override
	public int getMaxSize(){
		return -1;
	}
}
