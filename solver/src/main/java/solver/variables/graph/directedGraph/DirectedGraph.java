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

import solver.variables.graph.GraphTools;
import solver.variables.graph.GraphType;
import solver.variables.graph.ISet;
import solver.variables.graph.graphStructure.FullSet;
import solver.variables.graph.graphStructure.adjacencyList.ArraySwapList_Array;
import solver.variables.graph.graphStructure.adjacencyList.ArraySwapList_HashMap;
import solver.variables.graph.graphStructure.adjacencyList.IntDoubleLinkedList;
import solver.variables.graph.graphStructure.adjacencyList.IntLinkedList;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume
 * Date: 9 f�vr. 2011
 *
 * *
 * Specific implementation of a directed graph
 */
public class DirectedGraph implements IDirectedGraph {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	ISet[] successors;
	ISet[] predecessors;
	/** activeIdx represents the nodes available in the graph */
	ISet nodes;
	int n;
	GraphType type;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public DirectedGraph(int nbits, GraphType type, boolean allNodes) {
		this.type = type;
		this.n = nbits;
		switch (type) {
			// ARRAY SWAP
			case ENVELOPE_SWAP_ARRAY:
			case KERNEL_SWAP_ARRAY:
			case SWAP_ARRAY:
				this.successors = new ArraySwapList_Array[nbits];
				this.predecessors = new ArraySwapList_Array[nbits];
				for (int i = 0; i < nbits; i++) {
					this.successors[i] = new ArraySwapList_Array(nbits);
					this.predecessors[i] = new ArraySwapList_Array(nbits);
				}
				break;
			case ENVELOPE_SWAP_HASH:
			case KERNEL_SWAP_HASH:
			case SWAP_HASH:
				this.successors = new ArraySwapList_HashMap[nbits];
				this.predecessors = new ArraySwapList_HashMap[nbits];
				for (int i = 0; i < nbits; i++) {
					this.successors[i] = new ArraySwapList_HashMap(nbits);
					this.predecessors[i] = new ArraySwapList_HashMap(nbits);
				}
				break;
			// LINKED LISTS
			case DOUBLE_LINKED_LIST:
				this.successors = new IntDoubleLinkedList[nbits];
				this.predecessors = new IntDoubleLinkedList[nbits];
				for (int i = 0; i < nbits; i++) {
					this.successors[i] = new IntDoubleLinkedList();
					this.predecessors[i] = new IntDoubleLinkedList();
				}
				break;
			case LINKED_LIST:
				this.successors = new IntLinkedList[nbits];
				this.predecessors = new IntLinkedList[nbits];
				for (int i = 0; i < nbits; i++) {
					this.successors[i] = new IntLinkedList();
					this.predecessors[i] = new IntLinkedList();
				}
				break;
			case MATRIX:
				this.successors = new BitSetNeighbors[nbits];
				this.predecessors = new BitSetNeighbors[nbits];
				for (int i = 0; i < nbits; i++) {
					this.successors[i] = new BitSetNeighbors(nbits);
					this.predecessors[i] = new BitSetNeighbors(nbits);
				}
				break;
			default:
				throw new UnsupportedOperationException();
		}
		if(allNodes){
			this.nodes = new FullSet(nbits);
		}else{
			this.nodes = new BitSetNeighbors(nbits);
		}
	}

	public DirectedGraph(int order, boolean[][] matrix, GraphType type, boolean allNodes) {
		this(order,type,allNodes);
		for (int i = 0; i < order; i++) {
			for (int j = 0; j < order; j++) {
				if (matrix[i][j]) {
					this.successors[i].add(j);
					this.predecessors[j].add(i);
				}
			}
		}
	}

	public DirectedGraph() {}


	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public String toString() {
		return "Successors :\n"+toStringSuccs() +"\nPredecessors :\n"+ toStringPreds();
	}

	public String toStringSuccs() {
		String res = "";
		for (int i = nodes.getFirstElement(); i>=0; i = nodes.getNextElement()) {
			res += "pot-" + i + ": ";
			for(int j=successors[i].getFirstElement();j>=0; j=successors[i].getNextElement()){
				res += j + " ";
			}
			res += "\n";
		}
		return res;
	}

	public String toStringPreds() {
		String res = "";
		for (int i = nodes.getFirstElement(); i>=0; i = nodes.getNextElement()) {
			res += "pot-" + i + ": ";
			for(int j=predecessors[i].getFirstElement();j>=0; j=predecessors[i].getNextElement()){
				res += j + " ";
			}
			res += "\n";
		}
		return res;
	}

	@Override
	/**
	 * @inheritedDoc
	 */
	public int getNbNodes() {
		return n;
	}

	@Override
	/**
	 * @inheritedDoc
	 */
	public ISet getActiveNodes() {
		return nodes;
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
		if(nodes.contain(x))return false;
		nodes.add(x);
		return true;
	}

	@Override
	public boolean desactivateNode(int x) {
		if(!nodes.contain(x))return false;
		nodes.remove(x);
		for(int j=successors[x].getFirstElement();j>=0; j=successors[x].getNextElement()){
			predecessors[j].remove(x);
		}
		successors[x].clear();
		for(int j=predecessors[x].getFirstElement();j>=0; j=predecessors[x].getNextElement()){
			successors[j].remove(x);
		}
		predecessors[x].clear();
		return true;
	}

	@Override
	public boolean addEdge(int x, int y) {
		if(x==y){
			return addArc(x, y);
		}
		boolean b = addArc(x, y);
		b |= addArc(y, x);
		return b;
	}

	@Override
	public boolean removeEdge(int x, int y) {
		boolean b = removeArc(x, y) || removeArc(y, x);
		return b;
	}

	@Override
	public boolean edgeExists(int x, int y) {
		boolean b = arcExists(x, y) || arcExists(y, x);
		return b;
	}

	@Override
	public boolean removeArc(int from, int to) {
		if ((successors[from].contain(to)) && (predecessors[to].contain(from))){
			successors[from].remove(to);
			predecessors[to].remove(from);
			return true;
		}
		assert (!((successors[from].contain(to)) || (predecessors[to].contain(from)))):
				"incoherent directed graph";
		return false;
	}

	@Override
	public boolean arcExists(int from, int to){
		if (successors[from].contain(to) || predecessors[to].contain(from)){
			if (successors[from].contain(to) && predecessors[to].contain(from)){
				return true;
			}
			throw new UnsupportedOperationException("incoherent directed graph");
		}return false;
	}

	@Override
	public boolean addArc(int from, int to) {
		activateNode(from);
		activateNode(to);
		if ((!successors[from].contain(to)) && (!predecessors[to].contain(from))){
			successors[from].add(to);
			predecessors[to].add(from);
			return true;
		}
		assert (!((!successors[from].contain(to)) || (!predecessors[to].contain(from)))):
			"incoherent directed graph";
		return false;
	}

	@Override
	/**
	 * @inheritedDoc
	 * WARNING : not in O(1) but in O(nbSuccs[x]+nbPreds[x])
	 */
	public ISet getNeighborsOf(int x) {
		return GraphTools.mergeNeighborhoods(successors[x],predecessors[x], getNbNodes());
	}

	@Override
	public ISet getSuccessorsOf(int x) {
		return successors[x];
	}

	@Override
	public ISet getPredecessorsOf(int x) {
		return predecessors[x];
	}
}
