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

package solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp;

import solver.constraints.propagators.gary.tsp.HeldKarp;
import solver.constraints.propagators.gary.tsp.heaps.FastArrayHeap;
import solver.constraints.propagators.gary.tsp.heaps.Heap;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

import java.util.BitSet;

public class PrimOneTreeFinder extends AbstractOneTreeFinder {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	double[][] costs;
	Heap heap;
	BitSet inTree;
	private int tSize;
	private double minVal;
	double maxTArc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PrimOneTreeFinder(int nbNodes, HeldKarp propagator) {
		super(nbNodes,propagator);
		heap = new FastArrayHeap(nbNodes);
		inTree = new BitSet(n);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
		g = graph;
		for(int i=0;i<n;i++){
			Tree.getNeighborsOf(i).clear();
		}
		this.costs = costs;
		heap.clear();
		inTree.clear();
		treeCost = 0;
		tSize = 0;
		prim();
	}

	private void prim() throws ContradictionException {
		minVal = propHK.getMinArcVal();
		if(FILTER){
			maxTArc = minVal;
		}
		inTree.set(0);
		INeighbors nei = g.getSuccessorsOf(0);
		int min1 = -1;
		int min2 = -1;
		boolean b1=false,b2=false;
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(!b1){
				if(min1==-1){
					min1 = j;
				}
				if(costs[0][j]<costs[0][min1]){
					min2 = min1;
					min1 = j;
				}
				if(propHK.isMandatory(0,j)){
					if(min1!=j){
						min2 = min1;
					}
					min1 = j;
					b1 = true;
				}
			}
			if(min1!=j && !b2){
				if(min2==-1 || costs[0][j]<costs[0][min2]){
					min2 = j;
				}
				if(propHK.isMandatory(0,j)){
					min2 = j;
					b2 = true;
				}
			}
		}
		if(min1 == -1 || min2 == -1){
			propHK.contradiction();
		}
		addNode(min1);
		int from,to;
		while (tSize<n-2 && !heap.isEmpty()){
			to = heap.pop();
			from = heap.getMate(to);
			addArc(from,to);
		}
		if(tSize!=n-2){
			propHK.contradiction();
		}
		addArc(0,min1);
		addArc(0,min2);
		if(Tree.getNeighborsOf(0).neighborhoodSize()!=2){
			throw new UnsupportedOperationException();
		}
	}

	private void addArc(int from, int to) {
		if(Tree.edgeExists(from,to)){
			throw new UnsupportedOperationException();
		}
		Tree.addEdge(from,to);
		treeCost += costs[from][to];
		if(FILTER){
			if(!propHK.isMandatory(from,to)){
				maxTArc = Math.max(maxTArc, costs[from][to]);
			}
		}
		tSize++;
		addNode(to);
	}

	private void addNode(int i) {
		if(!inTree.get(i)){
			inTree.set(i);
			INeighbors nei = g.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!inTree.get(j)){
					if(propHK.isMandatory(i,j)){
						heap.add(j,minVal,i);
					}else{
						heap.add(j,costs[i][j],i);
					}
				}
			}
		}
	}

	public void performPruning(double UB) throws ContradictionException{
		if(FILTER){
			double delta = UB-treeCost;
			INeighbors nei;
			for(int i=0;i<n;i++){
				nei = g.getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(i<j && (!Tree.arcExists(i, j)) && costs[i][j]-maxTArc > delta){
						propHK.remove(i,j);
					}
				}
			}
		}else{
//			throw new UnsupportedOperationException("bound computation only, no filtering!");
		}
	}
}
