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

import solver.variables.graph.GraphType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.IGraph;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.adjacencyList.*;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;
import solver.variables.graph.graphStructure.nodes.ActiveNodes;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 f�vr. 2011
 *
 * Specific implementation of an undirected graph
 */
public class UndirectedGraph implements IGraph {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	INeighbors[] neighbors;
	/** activeIdx represents the nodes available in the graph */
	IActiveNodes activeIdx;
	GraphType type;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	protected UndirectedGraph() {}

	public UndirectedGraph(int nbits, GraphType type) {
		this.type = type;
		switch (type) {
			case COMPOSITE:
				this.neighbors = new CompositeList[nbits];
				for (int i = 0; i < nbits; i++) {
					this.neighbors[i] = new CompositeList(new IntLinkedList(), new BitSetNeighbors(nbits));
				}
				break;
			case ENVELOPE_SWAP_ARRAY:
				neighbors = new ArraySwapList_Array[nbits];
				for (int i = 0; i < nbits; i++) {
					neighbors[i] = new ArraySwapList_Array(nbits);
				}
				break;
			case ENVELOPE_SWAP_HASH:
				neighbors = new ArraySwapList_HashMap[nbits];
				for (int i = 0; i < nbits; i++) {
					neighbors[i] = new ArraySwapList_HashMap(nbits);
				}
				break;
			case DOUBLE_LINKED_LIST:
				this.neighbors = new IntDoubleLinkedList[nbits];
				for (int i = 0; i < nbits; i++) {
					this.neighbors[i] = new IntDoubleLinkedList();
				}
				break;
			case LINKED_LIST:
				this.neighbors = new IntLinkedList[nbits];
				for (int i = 0; i < nbits; i++) {
					this.neighbors[i] = new IntLinkedList();
				}
				break;
			case MATRIX:
				this.neighbors = new BitSetNeighbors[nbits];
				for (int i = 0; i < nbits; i++) {
					this.neighbors[i] = new BitSetNeighbors(nbits);
				}
				break;
			default:
				this.neighbors = new BitSetNeighbors[nbits];
				for (int i = 0; i < nbits; i++) {
					this.neighbors[i] = new BitSetNeighbors(nbits);
				}
				break;
		}
		this.activeIdx = new ActiveNodes(nbits);
		for (int i = 0; i < nbits; i++) {
			this.activeIdx.activate(i);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public String toString() {
		String res = "";
		for (int i = activeIdx.getFirstElement(); i>=0; i = activeIdx.getNextElement()) {
			res += "pot-" + i + ": "+ getNeighborsOf(i)+"\n";
		}
		return res;
	}

	@Override
	/**
	 * @inheritedDoc
	 */
	public int getNbNodes() {
		return activeIdx.nbNodes();
	}

	@Override
	/**
	 * @inheritedDoc
	 */
	public IActiveNodes getActiveNodes() {
		return activeIdx;
	}

	@Override
	/**
	 * @inheritedDoc
	 */
	public GraphType getType() {
		return type;
	}

	@Override
	public boolean activateNode(int x) {
		if(activeIdx.isActive(x))return false;
		activeIdx.activate(x);
		return true;
	}

	@Override
	public boolean desactivateNode(int x) {
		if(!activeIdx.isActive(x))return false;
		activeIdx.desactivate(x);
		INeighbors nei = getNeighborsOf(x);
		for(int j=nei.getFirstElement(); j>=0;j=nei.getNextElement()){
			neighbors[j].remove(x);
		}
		neighbors[x].clear();
		return true;
	}

	@Override
	public boolean addEdge(int x, int y) {
		if(x==y && !neighbors[x].contain(y)){
			neighbors[x].add(y);
			return true;
		}
		if ((!neighbors[x].contain(y)) && (!neighbors[y].contain(x))){
			neighbors[x].add(y);
			neighbors[y].add(x);
			return true;
		}
		if ((!neighbors[x].contain(y)) || (!neighbors[y].contain(x))){
			throw new UnsupportedOperationException("asymmetric adjacency matrix in an undirected graph");
		}
		return false;
	}

	@Override
	public boolean edgeExists(int x, int y) {//TODO maybe remove symmetry checking
		if(neighbors[x].contain(y) && neighbors[y].contain(x)){
			return true;
		}
		if(neighbors[x].contain(y) || neighbors[y].contain(x)){
			throw new UnsupportedOperationException("asymmetric adjacency matrix in an undirected graph");
		}
		return false;
	}

	@Override
	public boolean arcExists(int x, int y) {
		if(neighbors[x].contain(y)){
			return true;
		}
		return false;
	}

	@Override
	public boolean removeEdge(int x, int y) {
		if (x==y && neighbors[x].contain(y)){
			neighbors[y].remove(x);
			return true;
		}
		if ((neighbors[x].contain(y)) && (neighbors[y].contain(x))){
			neighbors[x].remove(y);
			neighbors[y].remove(x);
			return true;
		}
		if ((neighbors[x].contain(y)) || (neighbors[y].contain(x))){
			throw new UnsupportedOperationException("asymmetric adjacency matrix in an undirected graph");
		}
		return false;
	}

	@Override
	public INeighbors getNeighborsOf(int x) {
		return neighbors[x];
	}

	@Override
	public INeighbors getPredecessorsOf(int x) {
		return neighbors[x];
	}

	@Override
	public INeighbors getSuccessorsOf(int x) {
		return neighbors[x];
	}
}
