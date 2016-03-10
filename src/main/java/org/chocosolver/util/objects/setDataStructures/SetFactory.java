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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 27/10/12
 * Time: 01:56
 */

package org.chocosolver.util.objects.setDataStructures;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.copy.EnvironmentCopying;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.array.Set_FixedArray;
import org.chocosolver.util.objects.setDataStructures.bitset.Set_BitSet;
import org.chocosolver.util.objects.setDataStructures.bitset.Set_Std_BitSet;
import org.chocosolver.util.objects.setDataStructures.interval.Set_CstInterval;
import org.chocosolver.util.objects.setDataStructures.linkedlist.Set_LinkedList;
import org.chocosolver.util.objects.setDataStructures.swapList.Set_Std_Swap;
import org.chocosolver.util.objects.setDataStructures.swapList.Set_Swap;

/**
 * Factory for creating sets
 *
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetFactory {

	//***********************************************************************************
	// FACTORY - STORED SET
	//***********************************************************************************

	public static boolean HARD_CODED = true;

	/**
	 * Creates a stored set of integers greater or equal than <code>offSet</code>
	 * Such a set is restored after a backtrack
	 * @param type      of set data structure
	 * @param offSet	smallest value allowed in the set (possibly < 0)
	 * @param model		model providing the backtracking environment
	 * @return a new set which can be restored upon backtrack
	 */
	public static ISet makeStoredSet(SetType type, int offSet, Model model) {
		IEnvironment environment = model.getEnvironment();
		if (HARD_CODED)
			switch (type) {
				case BIPARTITESET:
					return new Set_Std_Swap(environment, offSet);
				case BITSET:
					return new Set_Std_BitSet(environment, offSet);
			}
		if (environment instanceof EnvironmentTrailing) {
			return new Set_Trail((EnvironmentTrailing) environment, makeSet(type,offSet));
		} else if (environment instanceof EnvironmentCopying) {
			return new Set_Copy((EnvironmentCopying) environment, makeSet(type,offSet));
		} else if(environment==null){
			throw new UnsupportedOperationException("Cannot create a backtrackable set with a backtracking environment equal to null.");
		} else {
			throw new UnsupportedOperationException("Set not implemented yet for environment "+environment.getClass().getSimpleName());
		}
	}


	//***********************************************************************************
	// FACTORY - SET
	//***********************************************************************************

	/**
	 * Creates an empty set of integers greater or equal than <code>offSet</code>
	 * @param type      	implementation type
	 * @param offSet		smallest value allowed in the set (possibly < 0)
	 * @return a new set
	 */
	public static ISet makeSet(SetType type, int offSet) {
		switch (type) {
			case BIPARTITESET:
				return makeBipartiteSet(offSet);
			case LINKED_LIST:
				return makeLinkedList();
			case BITSET:
				return makeBitSet(offSet);
			case FIXED_ARRAY: throw new UnsupportedOperationException("Please use makeConstantSet method to create a "+SetType.FIXED_ARRAY+" set");
			case FIXED_INTERVAL: throw new UnsupportedOperationException("Please use makeConstantSet method to create a "+SetType.FIXED_INTERVAL+" set");
		}
		throw new UnsupportedOperationException("SetType");
	}

	// --- List

	/**
	 * Creates a set based on a linked list
	 * appropriate when the set has only a few elements
	 * @return a new set
	 */
	public static ISet makeLinkedList() {
		return new Set_LinkedList();
	}

	// --- Bit Set

	/**
	 * Creates a set of integers, based on an offseted BitSet,
	 * Supports integers greater or equal than <code>offSet</code>
	 * @param offSet	smallest value allowed in the set (possibly < 0)
	 * @return a new set
	 */
	public static ISet makeBitSet(int offSet) {
		return new Set_BitSet(offSet);
	}

	// --- Bipartite Set

	/**
	 * Creates a set of integers, based on an offseted bipartite set,
	 * Supports integers greater or equal than <code>offSet</code>
	 * Optimal complexity
	 * @param offSet	smallest value allowed in the set (possibly < 0)
	 * @return a new bipartite set
	 */
	public static ISet makeBipartiteSet(int offSet) {
		return new Set_Swap(offSet);
	}

	// --- Constant Set

	/**
	 * Creates a fixed set of integers, equal to <code>cst</code>
	 * @param cst	set value
	 * @return a fixed set
	 */
	public static ISet makeConstantSet(int[] cst) {
		return new Set_FixedArray(cst);
	}

	/**
	 * Creates a constant set of integers represented with an interval [lb, ub]
	 * @return a new interval (constant)
	 */
	public static ISet makeConstantSet(int lb, int ub) {
		return new Set_CstInterval(lb, ub);
	}
}
