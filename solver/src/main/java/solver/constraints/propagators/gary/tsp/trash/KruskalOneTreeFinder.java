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

package solver.constraints.propagators.gary.tsp.trash;

import solver.constraints.propagators.gary.tsp.HeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.relaxationHeldKarp.KruskalMSTFinder;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;
import java.util.Arrays;
import java.util.Comparator;

public class KruskalOneTreeFinder extends KruskalMSTFinder {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int min1,min2;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public KruskalOneTreeFinder(int nbNodes, HeldKarp propagator) {
		super(nbNodes,propagator);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
		super.computeMST(costs,graph);
		add0Node();
	}

	protected void sortArcs(){
		Comparator<Integer> comp = new Comparator<Integer>(){
			@Override
			public int compare(Integer i1, Integer i2) {
				if(costs[i1]<costs[i2]){
					return -1;
				}
				if(costs[i1]>costs[i2]){
					return 1;
				}
				return 0;
			}
		};
		int size = 0;
		Tree.getNeighborsOf(0).clear();
		for(int i=1;i<n;i++){
			p[i] = i;
			rank[i] = 0;
			ccTp[i] = i;
			Tree.getNeighborsOf(i).clear();
			ccTree.desactivateNode(i);
			ccTree.activateNode(i);
			size += g.getSuccessorsOf(i).neighborhoodSize();
		}
		size -= g.getSuccessorsOf(0).neighborhoodSize();
		if(size%2!=0){
			throw new UnsupportedOperationException();
		}
		size /= 2;
		INeighbors nei;
		Integer[] integers = new Integer[size];
		int idx  = 0;
		for(int i=1;i<n;i++){
			nei =g.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					integers[idx]=i*n+j;
					costs[i*n+j] = distMatrix[i][j];
					idx++;
				}
			}
		}
		for(int i=n; i<ccN; i++){
			ccTree.desactivateNode(i);
		}
		Arrays.sort(integers,comp);
		int v;
		activeArcs.clear();
		activeArcs.set(0, size);
		for(idx = 0; idx<size; idx++){
			v = integers[idx];
			sortedArcs[idx] = v;
			indexOfArc[v/n][v%n] = idx;
		}
	}

	protected void pruning(int fi, double delta) throws ContradictionException {
		int i;
		INeighbors nei = g.getNeighborsOf(0);
		for(i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
			if(i!=min1 && i!=min2){
				if(distMatrix[0][i]-distMatrix[0][min2] > delta){
					propHK.remove(0,i);
				}
			}
		}
		super.pruning(fi,delta);
	}

	//***********************************************************************************
	// Kruskal's
	//***********************************************************************************

	@Override
	protected int addMandatoryArcs() throws ContradictionException {
		int from,to,rFrom,rTo,arc;
		int tSize = 0;
		double val = propHK.getMinArcVal();
		for(int i=ma.size()-1;i>=0;i--){
			arc = ma.get(i);
			from = arc/n;
			to = arc%n;
			if(from!=0 && to!=0){
				rFrom = FIND(from);
				rTo   = FIND(to);
				if(rFrom != rTo){
					LINK(rFrom, rTo);
					Tree.addEdge(from, to);
					updateCCTree(rFrom, rTo,val);
					treeCost += costs[arc];
					tSize++;
				}else{
					propHK.contradiction();
				}
			}
		}
		return tSize;
	}

	protected void connectMST(int tSize) throws ContradictionException {
		int from,to,rFrom,rTo;
		int idx = activeArcs.nextSetBit(0);
		minTArc = -propHK.getMinArcVal();
		maxTArc = propHK.getMinArcVal();
		double cost;
		while(tSize < n-2){
			if(idx<0){
				propHK.contradiction();
			}
			from = sortedArcs[idx]/n;
			to = sortedArcs[idx]%n;
			rFrom = FIND(from);
			rTo   = FIND(to);
			if(rFrom != rTo){
				LINK(rFrom, rTo);
				Tree.addEdge(from, to);
				cost = costs[sortedArcs[idx]];
				updateCCTree(rFrom, rTo, cost);
				if(cost > maxTArc){
					maxTArc = cost;
				}
				if(cost < minTArc){
					minTArc = cost;
				}
				treeCost += cost;
				tSize++;
			}
			idx = activeArcs.nextSetBit(idx+1);
		}
	}

	private void add0Node() throws ContradictionException {
		INeighbors nei = g.getSuccessorsOf(0);
		min1 = -1;
		min2 = -1;
		boolean b1=false,b2=false;
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(!b1){
				if(min1==-1){
					min1 = j;
				}
				if(distMatrix[0][j]<distMatrix[0][min1]){
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
				if(min2==-1 || distMatrix[0][j]<distMatrix[0][min2]){
					min2 = j;
				}
				if(propHK.isMandatory(0,j)){
					min2 = j;
					b2 = true;
				}
			}
		}
		if(min1 == min2){
			throw new UnsupportedOperationException();
		}
		if(min1 == -1 || min2 == -1){
			propHK.contradiction();
		}
		if(!propHK.isMandatory(0,min1)){
			maxTArc = Math.max(maxTArc, distMatrix[0][min1]);
		}
		if(!propHK.isMandatory(0,min2)){
			maxTArc = Math.max(maxTArc, distMatrix[0][min2]);
		}
		Tree.addEdge(0,min1);
		Tree.addEdge(0,min2);
		treeCost += distMatrix[0][min1]+distMatrix[0][min2];
	}
}
