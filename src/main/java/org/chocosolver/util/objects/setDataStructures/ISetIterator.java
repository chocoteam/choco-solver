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
package org.chocosolver.util.objects.setDataStructures;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Object to iterate over an ISet values using
 * <code>
 *     // more readable but includes autoboxing
 *     for(int value:set){
 *         ...
 *     }
 *
 *     // more verbose but without autoboxing
 *     ISetIterator iter = set.primitiveIterator();
 *     while(iter.hasNext()){
 *         int k = iter.nextInt();
 *         ...
 *     }
 * </code>
 *
 * @author Jean-Guillaume Fages
 */
public interface ISetIterator extends Iterator<Integer> {

	/**
	 * Reset iteration (to avoid creating a new ISetIterator for every iteration)
	 */
	void reset();

	/**
	 * Inform the iterator that value <code>item</code> has been removed
	 * (may require to update iterator structure)
	 * @param item removed value
	 */
	default void notifyRemoving(int item){}

	/**
	 * Returns the next int in the iteration.
	 *
	 * Beware : avoids autoboxing
	 *
	 * @return the next int in the iteration
	 * @throws NoSuchElementException if the iteration has no more elements
	 */
	int nextInt();

	@Override
	default Integer next() {
		return nextInt();
	}
}
