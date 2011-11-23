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

package solver.variables.graph;

public enum GraphType {
	SWAP_ARRAY,
	SWAP_HASH,
	// efficient but cannot add elements during the search
	ENVELOPE_SWAP_HASH,
	ENVELOPE_SWAP_ARRAY,// LOOKS BETTER THAN HASHMAP efficient but cannot add elements during the search
	// efficient but cannot remove elements during the search
	KERNEL_SWAP_HASH,
	KERNEL_SWAP_ARRAY,
	LINKED_LIST,
	DOUBLE_LINKED_LIST,  // enable deletion of current element in O(1)
	MATRIX;

	public final static GraphType[] ENVELOPE_TYPES = new GraphType[]{
			ENVELOPE_SWAP_ARRAY,ENVELOPE_SWAP_HASH,
			LINKED_LIST,DOUBLE_LINKED_LIST,MATRIX};

	public final static GraphType[] KERNEL_TYPES = new GraphType[]{
			KERNEL_SWAP_ARRAY,KERNEL_SWAP_HASH,
			LINKED_LIST,DOUBLE_LINKED_LIST,MATRIX};

	public final static GraphType ENVELOPE_BEST = ENVELOPE_SWAP_ARRAY;
	public final static GraphType KERNEL_BEST   = LINKED_LIST;
}

