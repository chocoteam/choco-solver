/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables.graph.graphStructure.adjacencyList;

import solver.variables.graph.ISet;

/**Linked list of m elements with double link (predecessor and successor)
 * add : O(1)
 * testPresence: O(m)
 * remove: O(m)
 * Enable deletion of the current item in O(1) (except for the last one)
 * iteration : O(m)
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 17/11/2011
 */
public class IntDoubleLinkedList implements ISet {

	/**
	 * The first cell of the linked list
	 */
	protected int size;
	protected DoubleIntCell first;
	protected DoubleIntCell nextCell; // enables to iterate
	protected DoubleIntCell poolGC;

	//***********************************************************************************
	// TCONSTRUCTOR
	//***********************************************************************************

	public IntDoubleLinkedList() {
		this.first    = null;
		this.nextCell = null;
		this.size	  = 0;
		poolGC = null;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

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
		DoubleIntCell current = first;
		while (!res && current != null) {
			if (current.element==element) {
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
	public void add(int element) {
		if(poolGC==null){
			this.first = new DoubleIntCell(element, first);
		}else{
			DoubleIntCell recycled = poolGC;
			poolGC = poolGC.next;
			recycled.init(element,first);
			first = recycled;
		}
		this.size++;
	}

	@Override
	/**
	 * Remove the first occurrence of the element.
	 * @param element an int
	 * @return true iff the element has been effectively removed
	 */
	public boolean remove(int element) {
		if(first!=null && first.element==element){
			DoubleIntCell old = poolGC;
			poolGC = first;
			first = first.next;
			if(first!=null)first.pred = null;
			size--;
			poolGC.next = old;
			return true;
		}
		if(nextCell!=null && nextCell.pred!=null && nextCell.pred.element==element){
			DoubleIntCell pred = nextCell.pred.pred;
			nextCell.pred.pred = null;
			nextCell.pred.next = null;

			DoubleIntCell old = poolGC;
			poolGC = nextCell.pred;

			if(pred!=null){
				pred.next = nextCell;
			}
			nextCell.pred = pred;
			size--;
			poolGC.next = old;
			return true;
		}
		DoubleIntCell current = first;
		DoubleIntCell nextCurrent, prevCurrent;
		boolean removed = false;
		while ((!removed) && current != null) {
			if (current.element==element) {
				if(current == nextCell){
					nextCell = nextCell.next;
				}
				DoubleIntCell old = poolGC;
				poolGC = current;
				nextCurrent = current.next;
				prevCurrent = current.pred;
				if(nextCurrent!=null)nextCurrent.pred = prevCurrent;
				if(prevCurrent!=null)prevCurrent.next = nextCurrent;
				removed = true;
				poolGC.next = old;
			}
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
		DoubleIntCell current = first;
		while (current != null) {
			res += current;
			current = current.next;
		}
		return res;
	}

	@Override
	public void clear() {
		if(first!=null){
			first.next=poolGC;
			poolGC = first;
		}
		first = null;
		nextCell = null;
		size = 0;
	}

	// --- Iterations	
	@Override
	public int getFirstElement() {
		if(first==null){
			return -1;
		}
		nextCell = first.next;
		return first.element;
	}

	@Override
	public int getNextElement() {
		if(nextCell==null){
			return -1;
		}
		int el = nextCell.element;
		nextCell = nextCell.next;
		return el;
	}

	//***********************************************************************************
	// STRUCTURE
	//***********************************************************************************

	private class DoubleIntCell {

		DoubleIntCell pred,next;
		int element;

		public DoubleIntCell(int element, DoubleIntCell next) {
			init(element,next);
		}

		public void init(int element, DoubleIntCell next) {
			this.element = element;
			this.next = next;
			this.pred = null;
			if(next!=null)next.pred = this;
		}

		public String toString() {
			if (next == null) {
				return ""+element;
			} else {
				return ""+element+" -> ";
			}
		}
	}

}
