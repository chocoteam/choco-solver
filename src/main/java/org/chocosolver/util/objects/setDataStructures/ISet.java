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
package org.chocosolver.util.objects.setDataStructures;


import java.util.Iterator;

/**
 * Class representing a set of integers
 * Created by IntelliJ IDEA.
 * @since 9 feb. 2011, update 2016
 * @author chameau, Jean-Guillaume Fages
 */
public interface ISet extends Iterable<Integer>{

	/**
	 * Use the following loop to iterate over this set.
	 * for(int i:this){
	 *     //
	 * }
	 * Do not use this iterator to make nested loops over {@link ISet} (prefer {@link ISet#newIterator()})
	 * @return the default iterator (singleton) of this set
	 */
	Iterator<Integer> iterator();

	/**
	 * Use the following loop to iterate over this set.
	 * for(int i:this){
	 *     //
	 * }
	 * @return a new iterator for this set
	 */
	ISetIterator newIterator();

    /**
     * Add element to the set
     *
     * @param element element to add
     * @return true iff element was not in the set and has been added
     */
    boolean add(int element);

    /**
     * Remove the first occurrence of element from the set
     *
     * @param element element to add
     * @return true iff element was in the set and has been removed
     */
    boolean remove(int element);

    /**
     * Test the existence of element in the set
     *
     * @param element element to add
     * @return true iff the set contains element
     */
    boolean contain(int element);

    /**
     * @return true iff the set is empty
     */
    default boolean isEmpty(){
		return getSize()==0;
	}

    /**
     * @return the number of elements in the set
     */
    int getSize();

    /**
     * Remove all elements from the set
     */
    void clear();

	/**
	 * @return the implementation type of this set
	 */
	SetType getSetType();

	/**
	 * Copies the set in an array if integers
	 * @return an array containing every integer of the set
	 */
	default int[] toArray(){
		int[] a = new int[getSize()];
		int idx = 0;
		for(int i:this){
			a[idx++] = i;
		}
		return a;
	}

	/**
	 * @return the smallest element in the set
	 * throws an exception if the set is empty
	 * Time complexity is linear for BIPARTITESET and LINKED_LIST (constant time otherwise)
	 */
	int min();

	/**
	 * @return the largest element in the set
	 * throws an exception if the set is empty
	 * Time complexity is linear for BIPARTITESET and LINKED_LIST (constant time otherwise)
	 */
	int max();
}
