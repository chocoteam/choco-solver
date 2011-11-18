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

package solver.variables.graph.directedGraph;

import choco.kernel.memory.IEnvironment;
import solver.variables.graph.GraphType;
import solver.variables.graph.IStoredGraph;
import solver.variables.graph.graphStructure.adjacencyList.CompositeList;
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.StoredDoubleIntLinkedList;
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.StoredIntLinkedList;
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.StoredEnvelopeIntLinkedList;
import solver.variables.graph.graphStructure.matrix.StoredBitSetNeighbors;
import solver.variables.graph.graphStructure.nodes.StoredActiveNodes;

/**Class representing a directed graph with a backtrable structure
 * @author Jean-Guillaume Fages */
public class StoredDirectedGraph extends DirectedGraph implements IStoredGraph{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected IEnvironment environment;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public StoredDirectedGraph(IEnvironment env, int nb, GraphType type) {
		super();
		this.type = type;
		environment = env;
		switch (type) {
			case COMPOSITE:
				this.successors = new CompositeList[nb];
				this.predecessors = new CompositeList[nb];
				for (int i = 0; i < nb; i++) {
					this.successors[i] = new CompositeList(new StoredIntLinkedList(env),new StoredBitSetNeighbors(env, nb));
					this.predecessors[i] = new CompositeList(new StoredIntLinkedList(env),new StoredBitSetNeighbors(env, nb));
				}
				break;
			case DOUBLE_LINKED_LIST:
				this.successors = new StoredDoubleIntLinkedList[nb];
				this.predecessors = new StoredDoubleIntLinkedList[nb];
				for (int i = 0; i < nb; i++) {
					this.successors[i] = new StoredDoubleIntLinkedList(environment);
					this.predecessors[i] = new StoredDoubleIntLinkedList(environment);
				}
				break;
			case LINKED_LIST:
				this.successors = new StoredIntLinkedList[nb];
				this.predecessors = new StoredIntLinkedList[nb];
				for (int i = 0; i < nb; i++) {
					this.successors[i] = new StoredIntLinkedList(environment);
					this.predecessors[i] = new StoredIntLinkedList(environment);
				}
				break;
			case ENVELOPE_LINKEDLIST:
				this.successors = new StoredEnvelopeIntLinkedList[nb];
				this.predecessors = new StoredEnvelopeIntLinkedList[nb];
				for (int i = 0; i < nb; i++) {
					this.successors[i] = new StoredEnvelopeIntLinkedList(nb,environment);
					this.predecessors[i] = new StoredEnvelopeIntLinkedList(nb,environment);
				}
				break;
			case MATRIX:
				this.successors = new StoredBitSetNeighbors[nb];
				this.predecessors = new StoredBitSetNeighbors[nb];
				for (int i = 0; i < nb; i++) {
					this.successors[i] = new StoredBitSetNeighbors(environment,nb);
					this.predecessors[i] = new StoredBitSetNeighbors(environment,nb);
				}
				break;
			default:
				throw new UnsupportedOperationException();
		}
		this.activeIdx = new StoredActiveNodes(environment, nb);
		for (int i = 0; i < nb; i++) {
			this.activeIdx.activate(i);
		}
	}

	@Override
	public IEnvironment getEnvironment() {
		return environment;
	}
}
