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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 27/10/12
 * Time: 01:56
 */

package solver.variables.setDataStructures;

import choco.kernel.memory.IEnvironment;
import solver.variables.setDataStructures.linkedlist.*;
import solver.variables.setDataStructures.matrix.*;
import solver.variables.setDataStructures.swapList.*;

/**
 * Factory for creating sets
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetFactory {

	//***********************************************************************************
	// FACTORY
	//***********************************************************************************

	public static ISet makeSet(SetType type, int maximumSize){
		switch (type){
			case SWAP_ARRAY:return makeSwap(maximumSize, false);
			case SWAP_HASH:return makeSwap(maximumSize, true);
			case LINKED_LIST: return makeLinkedList(false);
			case DOUBLE_LINKED_LIST: return makeLinkedList(true);
			case BITSET: return makeBitSet(maximumSize);
			case BOOL_ARRAY: return makeArray(maximumSize);
		}throw new UnsupportedOperationException("unknown SetType");
	}

	public static ISet makeStoredSet(SetType type, int maximumSize, IEnvironment environment){
		switch (type){
			case SWAP_ARRAY:return makeStoredSwap(environment,maximumSize, false);
			case SWAP_HASH:return makeStoredSwap(environment,maximumSize, true);
			case LINKED_LIST: return makeStoredLinkedList(environment,false);
			case DOUBLE_LINKED_LIST: return makeStoredLinkedList(environment,true);
			case BITSET: return makeStoredBitSet(environment,maximumSize);
			case BOOL_ARRAY: return makeStoredArray(environment,maximumSize);
		}throw new UnsupportedOperationException("unknown SetType");
	}

	//***********************************************************************************
	// LISTS
	//***********************************************************************************

	/**
	* Creates a stored set based on a backtrable linked list
	* appropriate when the set has only a few elements
	*
	* @param environment
	* @param doubleLink
	* @return a new set
	*/
	public static ISet makeStoredLinkedList(IEnvironment environment, boolean doubleLink){
		if(doubleLink){
			return new Set_Std_2LinkedList(environment);
		}else{
			return new Set_Std_LinkedList(environment);
		}
	}

	/**
	* Creates a set based on a linked list
	* appropriate when the set has only a few elements
	*
	* @param doubleLink
	* @return a new set
	*/
	public static ISet makeLinkedList(boolean doubleLink){
		if(doubleLink){
			return new Set_2LinkedList();
		}else{
			return new Set_LinkedList();
		}
	}

	//***********************************************************************************
	// MATRIX
	//***********************************************************************************

	/**
	* Creates a stored set based on a backtrable BitSet
	*
	* @param environment
	* @param n maximal size of the set
	* @return a new set
	*/
	public static ISet makeStoredBitSet(IEnvironment environment, int n){
		return new Set_Std_BitSet(environment,n);
	}

	/**
	* Creates a stored set based on a BitSet
	*
	* @param n maximal size of the set
	* @return a new set
	*/
	public static ISet makeBitSet(int n){
		return new Set_BitSet(n);
	}

	/**
	* Creates a stored set based on a backtrable boolean array
	*
	* @param environment
	* @param n maximal size of the set
	* @return a new set
	*/
	public static ISet makeStoredArray(IEnvironment environment, int n){
		return new Set_Std_Array(environment,n);
	}

	/**
	* Creates a set based on a boolean array
	*
	* @param n maximal size of the set
	* @return a new set
	*/
	public static ISet makeArray(int n){
		return new Set_Array(n);
	}

	//***********************************************************************************
	// SWAP LIST
	//***********************************************************************************

	/**
	 * Creates a stored set based on swaps
	 * Optimal complexity BUT cannot be used for
	 * both adding and removing elements during search
	 *
	 * @param environment
	 * @param n maximal size of the set
	 * @param hash lighter in memory by slower (false is recommended)
	 * @return a new set
	 */
	public static ISet makeStoredSwap(IEnvironment environment, int n, boolean hash){
		if(hash){
			return new Set_Std_Swap_Hash(environment,n);
		}else{
			return new Set_Std_Swap_Array(environment,n);
		}
	}

	/**
	 * Creates a set based on swaps
	 * Optimal complexity
	 * @param n maximal size of the set
	 * @param hash lighter in memory by slower (false is recommended)
	 * @return a new set
	 */
	public static ISet makeSwap(int n, boolean hash){
		if(hash){
			return new Set_Swap_Hash(n);
		}else{
			return new Set_Swap_Array(n);
		}
	}
}
