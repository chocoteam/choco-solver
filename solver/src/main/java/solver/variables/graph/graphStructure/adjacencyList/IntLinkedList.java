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

import solver.variables.graph.INeighbors;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 */
public class IntLinkedList implements INeighbors {

    /**
     * The first cell of the linked list
     */
    IntCell first;
    int size;
    IntCell nextCell; // enables to iterate

    public IntLinkedList() {
        this.first = null;
        nextCell    = null;
        this.size = 0;
    }

    public IntLinkedList(IntCell first) {
        this.first = first;
        nextCell    = first.next;
        int k = 0;
        IntCell current = first;
        while (current != null) {
            k++;
            current = current.getNext();
        }
        this.size = k;
    }

//    @Override
//    public LinkedListIterator<IntLinkedList> iterator() {
//        return new LinkedListIterator<IntLinkedList>(this);
//    }

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
    public int neighborhoodSize() {
        return this.size;
    }

//    /**
//     * Accessor on the first cell of the linked list
//     * @return a cell
//     */
//    public IntCell get() {
//        return first;
//    }

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
            if (current.contains(element)) {
                res = true;
            }
            current = current.getNext();
        }
        return res;
    }

//    public IntCell getFirst() {
//        return first;
//    }

    /**
     * Return the cell containing the value element if it exists
     * @param element an int
     * @return the cell containing the value element, null otherwise
     */
    public IntCell getCell(int element) {
        IntCell current = first;
        IntCell res = null;
        while (current != null) {
            if (current.contains(element)) {
                res = current;
            }
            current = current.getNext();
        }
        return res;
    }

//    /**
//     * Search for the last element of the linked list FROM THE BEGINING!
//     * @return the last cell of the linked list
//     */
//    @ Deprecated
//    public IntCell getLast() {
//        IntCell current = first;
//        IntCell res = null;
//        while (current != null) {
//            res = current;
//            current = current.getNext();
//        }
//        return res;
//    }

    @Override
    /**
     * Add an element in the first position. Beware, there is no garanty this element does not exist in the linked list
     * BEWARE if an element is added during an iteration, as it is before the first element, then this element will not appear
     * @param element an int
     */
    public void add(int element) {
        this.first = new IntCell(element, first);
        this.size++;
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
            if (current.contains(element)) {
            	if(current == nextCell){
            		nextCell = nextCell.next;
            	}
                if (previous == null) {
                    this.first = current.getNext();
                } else {
                    previous.setNext(current.getNext());
                }
                removed = true;
            }
            previous = current;
            current = current.getNext();
        }
        if (removed) {
            this.size--;
        }
        return removed;
    }

//    /**
//     * Add the linked list list at the end of the current linked list
//     * @param list a linked list
//     */
//    public void merge(IntLinkedList list) {
//        this.getLast().setNext(list.get());
//        this.size += list.size;
//    }

    @Override
    public String toString() {
        String res = "";
        IntCell current = first;
        while (current != null) {
            res += current;
            current = current.getNext();
        }
        return res;
    }

	@Override
	public void clear() {
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
		return first.getElement();
	}
	
	private int el; // avoid to declare an int at each getNextElement()
	
	@Override
	public int getNextElement() {
		if(nextCell==null){
			return -1;
		}
		el = nextCell.element;
		nextCell = nextCell.next;
		return el;
	}
}
