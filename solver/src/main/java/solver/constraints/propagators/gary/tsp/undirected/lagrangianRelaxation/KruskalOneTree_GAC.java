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

package solver.constraints.propagators.gary.tsp.undirected.lagrangianRelaxation;

import solver.constraints.propagators.gary.GraphLagrangianRelaxation;
import solver.constraints.propagators.gary.trees.KruskalMSTFinder;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class KruskalOneTree_GAC extends KruskalMSTFinder {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int min1,min2;
	private int[][] map;
	private double[][] marginalCosts;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public KruskalOneTree_GAC(int nbNodes, GraphLagrangianRelaxation propagator) {
		super(nbNodes,propagator);
		map = new int[n][n];
		marginalCosts = new double[n][n];
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
		super.computeMST(costs,graph);
		add0Node();
	}

	public void performPruning(double UB) throws ContradictionException{
		double delta = UB-treeCost;
		if(delta<0){
			throw new UnsupportedOperationException("mst>ub");
		}
		prepareMandArcDetection();
		if(selectRelevantArcs(delta)){
			lca.preprocess(cctRoot, ccTree);
			pruning(0,delta);
		}
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

		int j;
		for(int arc=activeArcs.nextSetBit(0); arc>=0; arc=activeArcs.nextSetBit(arc+1)){
			i = sortedArcs[arc]/n;
			j = sortedArcs[arc]%n;
			if(!Tree.arcExists(i,j)){
				marginalCosts[i][j] = costs[i*n+j]-ccTEdgeCost[lca.getLCA(i,j)];
				if(marginalCosts[i][j] > delta){
					activeArcs.clear(arc);
					propHK.remove(i,j);
				}else{
					markTreeEdges(ccTp,i,j);
				}
			}
		}
		for(i=1;i<n;i++){
			nei = Tree.getSuccessorsOf(i);
			for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(i<j)
				if(map[i][j]==-1 || costs[map[i][j]]-costs[i*n+j]>delta){
					propHK.enforce(i,j);
				}else{
					marginalCosts[i][j] = costs[map[i][j]]-costs[i*n+j];
				}
//				if(j!=0 && costs[map[i][j]]-costs[i*n+j]>delta){
//					propHK.enforce(i,j);
//				}
//				if(i<j && map[i][j]==-1){
//					propHK.enforce(i,j);
//				}
			}
		}
	}

	protected boolean selectRelevantArcs(double delta) throws ContradictionException {
		return selectAndCompress(delta);
	}
	protected boolean selectAndCompress(double delta) throws ContradictionException {
		// Trivially no inference
		int idx = activeArcs.nextSetBit(0);
		// Maybe interesting
		while(idx>=0 && costs[sortedArcs[idx]]-maxTArc <= delta){
			idx = activeArcs.nextSetBit(idx+1);
		}
		// Trivially infeasible arcs
		while(idx>=0){
			if(!Tree.arcExists(sortedArcs[idx]/n, sortedArcs[idx]%n)){
				propHK.remove(sortedArcs[idx]/n, sortedArcs[idx]%n);
				activeArcs.clear(idx);
			}
			idx = activeArcs.nextSetBit(idx+1);
		}
		//contract ccTree
		cctRoot++;
		int newNode = cctRoot;
		ccTree.activateNode(newNode);
		ccTEdgeCost[newNode] = propHK.getMinArcVal();
		for(int i=ccTree.getActiveNodes().getFirstElement();i>=0;i=ccTree.getActiveNodes().getNextElement()){
			if(ccTree.getPredecessorsOf(i).getFirstElement()==-1){
				if(i!=cctRoot){
					ccTree.addArc(cctRoot,i);
				}
			}
		}
		return true;
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

	//***********************************************************************************
	// Detecting mandatory arcs
	//***********************************************************************************

	protected void prepareMandArcDetection(){
		// RECYCLING ccTp is used to model the compressed path
		INeighbors nei;
		for(int i=0;i<n;i++){
			ccTp[i] = -1;
		}
		useful.clear();
		useful.set(0);
		int k=1;
		useful.set(k);
		ccTp[k]=k;
		LinkedList<Integer> list = new LinkedList<Integer>();
		list.add(k);
		while(!list.isEmpty()){
			k = list.removeFirst();
			nei = Tree.getSuccessorsOf(k);
			for(int s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
				if(ccTp[s]==-1){
					ccTp[s] = k;
					map[s][k] = map[k][s] = -1;
					if(!useful.get(s)){
						list.addLast(s);
						useful.set(s);
					}
				}
			}
		}
	}

	protected void markTreeEdges(int[] next, int i, int j) {
		int rep = i*n+j;
		if(i==0){
			throw new UnsupportedOperationException();
		}
		if(next[i]==next[j]){
			if(map[i][next[i]]==-1){
				map[i][next[i]] = map[next[i]][i] = rep;
			}
			if(map[j][next[j]]==-1){
				map[j][next[j]] = map[next[j]][j] = rep;
			}
			return;
		}
		useful.clear();
		int meeting = j;
		int tmp;
		int a;
		for(a=i;a!=next[a];a=next[a]){
			useful.set(a);
		}
		useful.set(a);
		for(;!useful.get(meeting);meeting=next[meeting]){}
		for(int b=j;b!=meeting;){
			tmp = next[b];
			next[b] = meeting;
			if(map[b][tmp]==-1){
				map[b][tmp] = map[tmp][b] = rep;
			}
			b = tmp;
		}
		for(a=i;a!=meeting;){
			tmp = next[a];
			next[a] = meeting;
			if(map[a][tmp]==-1){
				map[a][tmp] = map[tmp][a] = rep;
			}
			a = tmp;
		}
	}

	public double getRepCost(int from, int to){
		if(from>to){
			return getRepCost(to,from);//to check
		}
		if(from==0){
			return 0;
		}
		return marginalCosts[from][to];
//		if(map[from][to]==-1){
//			System.out.println(map[to][from]);
//			System.exit(0);
//		}
//		return costs[map[from][to]]-costs[from*n+to];
	}

//	public double getMarginalCost(int from, int to){
//		if(from>to){
//			return getRepCost(to,from);//to check
//		}
//		if(from==0){
//			return 0;
//		}
//		return costs[from*n+to]-ccTEdgeCost[lca.getLCA(from,to)];
//	}
}
