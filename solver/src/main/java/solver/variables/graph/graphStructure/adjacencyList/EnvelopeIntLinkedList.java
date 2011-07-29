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

/**LinkedList designed for envelope of graphVar : 
 * perform 
 * contain in O(1)
 * add in O(1)
 * remove in O(1)
 * iteration in O(m)
 * 
 * BUT elements can only be inserted at world 0 (which fit with the envelope context)
 * 
 * BEWARE : it is beautiful on paper but pretty heavy in practice...
 * 
 * @author Jean-Guillaume 
 */
public class EnvelopeIntLinkedList implements INeighbors {

	int first;
	int size;
	int[] next; 
	int[] prev; 
	private int nextElement; // enables to iterate

    public EnvelopeIntLinkedList(int n) {
        this.first = -1;
        next    = new int[n];
        prev    = new int[n];
        for(int i=0;i<n;i++){
        	next[i] = -1;
        	prev[i] = -1;
        }
        this.size = 0;
        this.nextElement = -1;
    }

    @Override
    /**
     * Test for an empty list
     * @return true iff the list is empty
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    /**
     * The number of elements in the linked list
     * @return the number of cells
     */
    public int neighborhoodSize() {
        return this.size;
    }

    @Override
    /**
     * Check if the linked list contain the value element
     * @param element an int
     * @return true iff the linked list contain the value element
     */
    public boolean contain(int element) {
        return prev[element]!=-1 || first ==element ;
    }

    @Override
    /**
     * Add an element in the first position. Beware, there is no garanty this element does not exist in the linked list
     * BEWARE if an element is added during an iteration, as it is before the first element, then this element will not appear
     * @param element an int
     */
    public void add(int element) {
    	int f = first;
    	if(f == -1){
    		first = element;
    		size  = 1;
    	}else{
	    	next[element] = f;
	    	prev[f] = element;
	        this.first = element;
	        this.size++;
    	}
    }

    @Override
    /**
     * Remove the first occurrence of the element.
     * @param element an int
     * @return true iff the element has been effectively removed
     */
    public boolean remove(int element) {
    	if(!contain(element)){
    		return false;
    	}
		int p = prev[element];
    	int s = next[element];
    	prev[element] = -1;
    	next[element] = -1;
    	if(nextElement == element){
    		nextElement = s;
    	}
		if(first == element){ // first
    		first = s;
    		if (s != -1){
    			prev[s] = -1;
    		}
    		size--;
    		return true;
    	}else {
    		if(s!=-1){
    			prev[s] = p;
    		}
    		next[p] = s;
    		size--;
    		return true;
    	}
    }

    @Override
    public String toString() {
        String res = "";
        int current = first;
        while (current != -1) {
            res += current+", ";
            current = next[current];
        }
        return res;
    }

	@Override
	public void clear() {
		int current = first;
		int idx;
        while (current != -1) {
        	prev[current] = -1;
        	idx = next[current];
        	next[current] = -1;
        	current = idx;
        }
		nextElement = -1;
		first = -1;
		size  =  0;
	}
	
	// --- Iterations	
	@Override
	public int getFirstElement() {
		int f = first;
		if(f != -1){
			nextElement = next[f];
		}else{
			nextElement = -1;
		}
		return f;
	}
	
	@Override
	public int getNextElement() {
		int elem = nextElement;
		if(elem !=-1) {
			nextElement = next[elem];
		}
		return elem;
	}
}
