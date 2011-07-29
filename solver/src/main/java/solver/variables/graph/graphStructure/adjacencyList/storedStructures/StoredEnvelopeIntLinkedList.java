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

package solver.variables.graph.graphStructure.adjacencyList.storedStructures;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
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
public class StoredEnvelopeIntLinkedList implements INeighbors {

	IStateInt first;
	IStateInt size;
	IStateInt[] next; 
	IStateInt[] prev; 
	private int nextElement; // enables to iterate
	private IEnvironment environment;

    public StoredEnvelopeIntLinkedList(int n, IEnvironment env) {
        this.first = env.makeInt(-1);
        next    = new IStateInt[n];
        prev    = new IStateInt[n];
        this.environment = env;
        this.size = env.makeInt(0);
        this.nextElement = -1;
    }

    @Override
    /**
     * Test for an empty list
     * @return true iff the list is empty
     */
    public boolean isEmpty() {
        return this.size.get() == 0;
    }

    @Override
    /**
     * The number of elements in the linked list
     * @return the number of cells
     */
    public int neighborhoodSize() {
        return this.size.get();
    }

    @Override
    /**
     * Check if the linked list contain the value element
     * @param element an int
     * @return true iff the linked list contain the value element
     */
    public boolean contain(int element) {
        return prev[element]!=null && (prev[element].get()!=-1 || first.get()==element);
    }

    @Override
    /**
     * Add an element in the first position. Beware, there is no garanty this element does not exist in the linked list
     * BEWARE if an element is added during an iteration, as it is before the first element, then this element will not appear
     * @param element an int
     */
    public void add(int element) {
    	if(environment.getWorldIndex()!=0){
    		throw new UnsupportedOperationException("for efficiency reasons, element insertion in "+this.getClass().getName()+" is only possible at world 0.\n" +
    				"please use another datastructure");
    	}
    	if(prev[element]==null){
    		prev[element] = environment.makeInt(-1);
    	}
    	if(next[element]==null){
    		next[element] = environment.makeInt(-1);
    	}
    	int f = first.get();
    	if(f == -1){
    		first.set(element);
    		size.set(1);
    	}else{
	    	next[element].set(f);
	    	prev[f].set(element);
	        this.first.set(element);
	        this.size.add(1);
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
		int p = prev[element].get();
    	int s = next[element].get();
    	prev[element].set(-1);
    	next[element].set(-1);
    	if(nextElement == element){
    		nextElement = s;
    	}
		if(first.get() == element){ // first
    		first.set(s);
    		if (s != -1){
    			prev[s].set(-1);
    		}
    		size.add(-1);
    		return true;
    	}else {
    		if(s!=-1){
    			prev[s].set(p);
    		}
    		next[p].set(s);
    		size.add(-1);
    		return true;
    	}
    }

    @Override
    public String toString() {
        String res = "";
        int current = first.get();
        while (current != -1) {
            res += current+", ";
            current = next[current].get();
        }
        return res;
    }

	@Override
	public void clear() {
		int current = first.get();
		int idx;
        while (current != -1) {
        	prev[current].set(-1);
        	idx = next[current].get();
        	next[current].set(-1);
        	current = idx;
        }
		nextElement = -1;
		first.set(-1);
		size.set(0);
	}
	
	// --- Iterations	
	@Override
	public int getFirstElement() {
		int f = first.get();
		if(f != -1){
			nextElement = next[f].get();
		}else{
			nextElement = -1;
		}
		return f;
	}
	
	@Override
	public int getNextElement() {
		int elem = nextElement;
		if(elem !=-1) {
			nextElement = next[elem].get();
		}
		return elem;
	}
}
