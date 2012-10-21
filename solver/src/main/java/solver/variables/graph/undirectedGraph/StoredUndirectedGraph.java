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

package solver.variables.graph.undirectedGraph;

import choco.kernel.memory.IEnvironment;
import solver.variables.graph.GraphType;
import solver.variables.graph.IStoredGraph;
import solver.variables.graph.graphStructure.FullSet;
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.*;
import solver.variables.graph.graphStructure.matrix.StoredBitSetNeighbors;

/**Class representing an undirected graph with a backtrable structure
 * @author Jean-Guillaume Fages */
public class StoredUndirectedGraph extends UndirectedGraph implements IStoredGraph {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected IEnvironment environment;


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public StoredUndirectedGraph(IEnvironment env, int nbits, GraphType type, boolean allNodes) {
		this.type = type;
		this.n = nbits;
		environment = env;
		switch (type) {
			// SWAP ARRAYS
			case ENVELOPE_SWAP_ARRAY:
				neighbors = new StoredArraySwapList_Array_RemoveOnly[nbits];
				for (int i = 0; i < nbits; i++) {
					neighbors[i] = new StoredArraySwapList_Array_RemoveOnly(env,nbits);
				}
				break;
			case ENVELOPE_SWAP_HASH:
				neighbors = new StoredArraySwapList_HashMap_RemoveOnly[nbits];
				for (int i = 0; i < nbits; i++) {
					neighbors[i] = new StoredArraySwapList_HashMap_RemoveOnly(env,nbits);
				}
				break;
			case KERNEL_SWAP_ARRAY:
				neighbors = new StoredArraySwapList_Array_AddOnly[nbits];
				for (int i = 0; i < nbits; i++) {
					neighbors[i] = new StoredArraySwapList_Array_AddOnly(env,nbits);
				}
				break;
			case KERNEL_SWAP_HASH:
				neighbors = new StoredArraySwapList_HashMap_AddOnly[nbits];
				for (int i = 0; i < nbits; i++) {
					neighbors[i] = new StoredArraySwapList_HashMap_AddOnly(env,nbits);
				}
				break;
			// LINKED LISTS
			case DOUBLE_LINKED_LIST:
				this.neighbors = new StoredDoubleIntLinkedList[nbits];
				for (int i = 0; i < nbits; i++) {
					this.neighbors[i] = new StoredDoubleIntLinkedList(environment);
				}
				break;
			case LINKED_LIST:
				this.neighbors = new StoredIntLinkedList[nbits];
				for (int i = 0; i < nbits; i++) {
					this.neighbors[i] = new StoredIntLinkedList(environment);
				}
				break;
			// MATRIX
			case MATRIX:
				this.neighbors = new StoredBitSetNeighbors[nbits];
				for (int i = 0; i < nbits; i++) {
					this.neighbors[i] = new StoredBitSetNeighbors(environment,nbits);
				}
				break;
			default:
				throw new UnsupportedOperationException();
		}
		if(allNodes){
			this.nodes = new FullSet(nbits);
		}else{
			this.nodes = new StoredBitSetNeighbors(environment, nbits);
		}
	}

	@Override
	public IEnvironment getEnvironment() {
		return environment;
	}
}
