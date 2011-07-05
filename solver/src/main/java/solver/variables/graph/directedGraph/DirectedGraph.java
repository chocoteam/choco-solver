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
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.adjacencyList.IntLinkedList;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;
import solver.variables.graph.graphStructure.nodes.ActiveNodes;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 9 févr. 2011
 *
 * *
 * Specific implementation of a directed graph
 */
public class DirectedGraph implements IDirectedGraph {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	INeighbors[] successors;
	INeighbors[] predecessors;
	/** activeIdx represents the nodes available in the graph */
	IActiveNodes activeIdx;
	GraphType type;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public DirectedGraph(int order, GraphType type) {
		this.type = type;
		switch (type) {
		case SPARSE:
			this.successors = new IntLinkedList[order];
			this.predecessors = new IntLinkedList[order];
			for (int i = 0; i < order; i++) {
				this.successors[i] = new IntLinkedList();
				this.predecessors[i] = new IntLinkedList();
			}
			break;
		case DENSE:
			this.successors = new BitSetNeighbors[order];
			this.predecessors = new BitSetNeighbors[order];
			for (int i = 0; i < order; i++) {
				this.successors[i] = new BitSetNeighbors(order);
				this.predecessors[i] = new BitSetNeighbors(order);
			}
			break;
		default:
			throw new UnsupportedOperationException();
		}
		this.activeIdx = new ActiveNodes(order);
		for (int i = 0; i < order; i++) {
			this.activeIdx.activate(i);
		}
	}

	public DirectedGraph(int order, boolean[][] matrix, GraphType type) {
		this(order,type);
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
		for (int i = activeIdx.getFirstElement(); i>=0; i = activeIdx.getNextElement()) {
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
		for (int i = activeIdx.getFirstElement(); i>=0; i = activeIdx.getNextElement()) {
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
	 * WARNING : not in O(1) but in O(nbSuccs[x]+nbPreds[x])
	 */
	public int getNeighborhoodSize(int x) {
		return GraphTools.mergeNeighborhoods(successors[x],predecessors[x], getNbNodes()).neighborhoodSize();
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
//
//		for(int j=successors[x].getFirstElement();j>=0; j=successors[x].getNextElement()){
//			predecessors[j].remove(x);
//		}
//		successors[x].clear();
//		for(int j=predecessors[x].getFirstElement();j>=0; j=predecessors[x].getNextElement()){
//			successors[j].remove(x);
//		}
//		predecessors[x].clear();
		return true;
	}

	@Override
	public boolean addEdge(int x, int y) {
		if(x==y){
			return addArc(x, y);
		}
		boolean b = addArc(x, y) || addArc(y, x);
		return b;
	}

	@Override
	public boolean removeEdge(int x, int y) {
		return removeArc(x, y) || removeArc(y, x);
	}

	@Override
	public boolean edgeExists(int x, int y) {
		return arcExists(x, y) || arcExists(y, x);
	}

	@Override
	public boolean removeArc(int from, int to) {
		if ((successors[from].contain(to)) && (predecessors[to].contain(from))){
			successors[from].remove(to);
			predecessors[to].remove(from);
			return true;
		}
		if ((successors[from].contain(to)) || (predecessors[to].contain(from))){
			throw new UnsupportedOperationException("incoherent directed graph");
		}
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
		if ((!successors[from].contain(to)) && (!predecessors[to].contain(from))){
			successors[from].add(to);
			predecessors[to].add(from);
			return true;
		}
		if ((!successors[from].contain(to)) || (!predecessors[to].contain(from))){
			throw new UnsupportedOperationException("incoherent directed graph");
		}
		return false;
	}

	@Override
	public INeighbors getNeighborsOf(int x) {
		return GraphTools.mergeNeighborhoods(successors[x],predecessors[x], getNbNodes());
	}

	@Override
	public INeighbors getSuccessorsOf(int x) {
		return successors[x];
	}

	@Override
	public INeighbors getPredecessorsOf(int x) {
		return predecessors[x];
	}
}
