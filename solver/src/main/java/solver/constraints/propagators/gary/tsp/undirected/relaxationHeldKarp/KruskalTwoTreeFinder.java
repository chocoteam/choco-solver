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
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

import java.util.Arrays;
import java.util.Comparator;

public class KruskalTwoTreeFinder extends KruskalMSTFinder {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int min1,min2;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public KruskalTwoTreeFinder(int nbNodes, HeldKarp propagator) {
		super(nbNodes,propagator);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
		super.computeMST(costs,graph);
		addExtremities();
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
		Tree.getNeighborsOf(n-1).clear();
		for(int i=1;i<n-1;i++){
			p[i] = i;
			rank[i] = 0;
			ccTp[i] = i;
			Tree.getNeighborsOf(i).clear();
			ccTree.desactivateNode(i);
			ccTree.activateNode(i);
			size += g.getSuccessorsOf(i).neighborhoodSize();
		}
		size -= g.getSuccessorsOf(0).neighborhoodSize();
		size -= g.getSuccessorsOf(n-1).neighborhoodSize();
		if(size%2!=0){
			throw new UnsupportedOperationException();
		}
		size /= 2;
		INeighbors nei;
		Integer[] integers = new Integer[size];
		int idx  = 0;
		for(int i=1;i<n-1;i++){
			nei =g.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j && j!=n-1){
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
			if(i!=min1){
				if(distMatrix[0][i]-distMatrix[0][min1] > delta){
					propHK.remove(0,i);
				}
			}
		}
		nei = g.getNeighborsOf(n-1);
		for(i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
			if(i!=min2){
				if(distMatrix[n-1][i]-distMatrix[n-1][min2] > delta){
					propHK.remove(n-1,i);
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
			if(from!=0 && to!=n-1){
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
		while(tSize < n-3){
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

	protected void addExtremities(){
		int mc1 = -1,mc2 = -1;
		if(g.getSuccessorsOf(0).neighborhoodSize()==1){
			mc1 = g.getSuccessorsOf(0).getFirstElement();
		}
		if(g.getSuccessorsOf(n-1).neighborhoodSize()==1){
			mc2 = g.getSuccessorsOf(n-1).getFirstElement();
		}
		if(mc1!=-1){
			if(mc2!=-1){
			}else{
				mc2 = getBestNot(n-1,mc1);
			}
		}else{
			if(mc2!=-1){
				mc1 = getBestNot(0,mc2);
			}else{
				mc2 = getBestNot(n-1,-2);
				mc1 = getBestNot(0,mc2);
				double k = distMatrix[0][mc1]+distMatrix[n-1][mc2];
				int mc1bis = getBestNot(0,-1);
				int mc2bis = getBestNot(n-1,mc1bis);
				double kbis = distMatrix[0][mc1bis]+distMatrix[n-1][mc2bis];
				if(kbis<k){
					mc2 = mc2bis;
					mc1 = mc1bis;
				}
			}
		}
		if(!propHK.isMandatory(0,min1)){
			maxTArc = Math.max(maxTArc, distMatrix[0][min1]);
		}
		if(!propHK.isMandatory(n-1,min2)){
			maxTArc = Math.max(maxTArc, distMatrix[n-1][min2]);
		}
		Tree.addEdge(0,mc1);
		Tree.addEdge(n-1,mc2);
		min1 = mc1;
		min2 = mc2;
		treeCost += distMatrix[0][mc1]+distMatrix[n-1][mc2];
	}

	protected int getBestNot(int i, int not) {
		if(not==0 || not==n-1){
			INeighbors nei = g.getSuccessorsOf(i);
			double cost = -1;
			int idx = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j!=0 && j!=n-1 && (idx==-1 || cost>distMatrix[i][j])){
					idx = j;
					cost = distMatrix[i][j];
				}
			}
			if(idx==-1){
				throw new UnsupportedOperationException();
			}
			return idx;
		}else{
			INeighbors nei = g.getSuccessorsOf(i);
			double cost = -1;
			int idx = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j!=not && (idx==-1 || cost>distMatrix[i][j])){
					idx = j;
					cost = distMatrix[i][j];
				}
			}
			if(idx==-1){
				System.out.println(nei);
				System.out.println(propHK.isMandatory(i,nei.getFirstElement()));
				throw new UnsupportedOperationException();
			}
			return idx;
		}
	}
}
