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

package solver.constraints.propagators.gary.tsp.relaxationHeldKarp;

import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.exception.ContradictionException;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.graphOperations.connectivity.LCAGraphManager;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

public class KruskalBSTFinderWithFiltering extends AbstractBSTFinder {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected TIntArrayList ma; 	//mandatory arcs (i,j) <-> i*n+j
	// indexes are sorted
	int[] sortedArcs;   // from sorted to lex
	int[][] indexOfArc; // from lex (i,j) to sorted (i+1)*n+j
	private BitSet activeArcs; // if sorted is active
	// UNSORTED
	double[] costs;			 // cost of the lex arc
	int[] p, rank;
	// CCtree
	private int ccN;
	private DirectedGraph ccTree;
	private int[] ccTp;
	private double[] ccTEdgeCost;
	private LCAGraphManager lca;
	private int fromInterest, cctRoot;
	private BitSet useful;
	private double minTArc,maxTArc;
	TIntArrayList links;
	private int[] minCostOutArcs;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public KruskalBSTFinderWithFiltering(int nbNodes, HeldKarp propagator, IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs) {
		super(nbNodes,propagator,nR,sccOf,outArcs);
		activeArcs = new BitSet(n*n);
		rank = new int[n];
		costs = new double[n*n];
		sortedArcs = new int[n*n];
		indexOfArc = new int[n][n];
		p = new int[n];
		ccN = 2*n+1;
		ccTree = new DirectedGraph(ccN,GraphType.LINKED_LIST);
		ccTEdgeCost = new double[ccN];
		ccTp = new int[n];
		useful = new BitSet(n);
		lca = new LCAGraphManager(ccN);
		links = new TIntArrayList();
		minCostOutArcs = new int[n];
	}

	private void sortArcs(double[][] costMatrix) throws ContradictionException {
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
		for(int i=0;i<n;i++){
			p[i] = i;
			rank[i] = 0;
			ccTp[i] = i;
			Tree.getSuccessorsOf(i).clear();
			Tree.getPredecessorsOf(i).clear();
			ccTree.desactivateNode(i);
			ccTree.activateNode(i);
			size+=g.getSuccessorsOf(i).neighborhoodSize();
		}
		Integer[] integers = new Integer[size];
		int idx  = 0;
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei =g.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				integers[idx]=i*n+j;
				costs[i*n+j] = costMatrix[i][j];
				idx++;
			}
		}
		for(int i=n; i<ccN; i++){
			ccTree.desactivateNode(i);
		}
		Arrays.sort(integers,comp);
		int v;
		activeArcs.clear();
		activeArcs.flip(0,size);
		for(idx = 0; idx<size; idx++){
			v = integers[idx];
			sortedArcs[idx] = v;
			indexOfArc[v/n][v%n] = idx;
		}
		int f,t;
		for(int x=nR.get()-1;x>=0;x--){
			minCostOutArcs[x] = -1;
			int mand = -1;
			for(int a=outArcs[x].getFirstElement();a>=0;a=outArcs[x].getNextElement()){
				f = a/n-1;
				t = a%n;
				if(g.arcExists(f,t)){
					activeArcs.clear(indexOfArc[f][t]);
					links.add(a-n);
					if(minCostOutArcs[x]==-1 || costs[minCostOutArcs[x]]>costs[a-n]){
						minCostOutArcs[x] = a-n;
					}
				}
				if(propHK.getMandatorySuccessorOf(f)==t){
					mand = a-n;
				}
			}
			if(mand!=-1){
				minCostOutArcs[x] = mand;
			}
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void computeMST(double[][] costs, DirectedGraph graph) throws ContradictionException {
		g = graph;
		links.clear();
		ma = propHK.getMandatoryArcsList();
		sortArcs(costs);
		treeCost = 0;
		cctRoot = n-1;
		int tSize = addMandatoryArcs();
		tSize += addOutArcs();
		if(tSize>n-1){throw new UnsupportedOperationException("too many arcs in the MST");}
		connectMST(tSize);
	}

	public void performPruning(double UB) throws ContradictionException{
		double delta = UB-treeCost;
		if(delta<0){
			throw new UnsupportedOperationException("mst>ub");
		}
		fromInterest = 0;
		if(selectRelevantArcs(delta)){
			lca.preprocess(cctRoot, ccTree);
			pruning(fromInterest,delta);
		}
	}

	private boolean selectRelevantArcs(double delta) throws ContradictionException {
		// Trivially no inference
		int idx = activeArcs.nextSetBit(0);
		while(idx>=0 && costs[sortedArcs[idx]]-minTArc <= delta){
			idx = activeArcs.nextSetBit(idx+1);
		}
		if(idx==-1){
			return false;
		}
		fromInterest = idx;
		// Maybe interesting
		useful.clear();
		while(idx>=0 && costs[sortedArcs[idx]]-maxTArc <= delta){
			useful.set(sortedArcs[idx]/n);
			useful.set(sortedArcs[idx]%n);
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
		if(useful.cardinality()==0){
			return false;
		}
		// contract ccTree
		int p,s;
		for(int i=useful.nextClearBit(0);i<n;i=useful.nextClearBit(i+1)){
			ccTree.desactivateNode(i);
		}
		for(int i=ccTree.getActiveNodes().getFirstElement();i>=0;i=ccTree.getActiveNodes().getNextElement()){
			s = ccTree.getSuccessorsOf(i).getFirstElement();
			if(s==-1){
				if(i>=n){
					ccTree.desactivateNode(i);
				}
			}else if (ccTree.getSuccessorsOf(i).getNextElement()==-1){
				p = ccTree.getPredecessorsOf(i).getFirstElement();
				ccTree.desactivateNode(i);
				if(p!=-1){
					ccTree.addArc(p,s);
				}
			}
		}
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

	private void pruning(int fi, double delta) throws ContradictionException {
		int i,j;
		double repCost;
		for(int arc=activeArcs.nextSetBit(fi); arc>=0; arc=activeArcs.nextSetBit(arc+1)){
			i = sortedArcs[arc]/n;
			j = sortedArcs[arc]%n;
			if(!Tree.arcExists(i, j)){
				repCost = ccTEdgeCost[lca.getLCA(i,j)];
				if(costs[i*n+j]-repCost > delta){
					propHK.remove(i,j);
				}
			}
		}
		int arc,x;
		for(int k=links.size()-1;k>=0;k--){
			arc = links.get(k);
			i = arc/n;
			j = arc%n;
			if(!Tree.arcExists(i, j)){
				x = sccOf[i].get();
				repCost = costs[minCostOutArcs[x]];
				if(costs[i*n+j]-repCost > delta){
					propHK.remove(i,j);
				}
			}
		}
	}

	//***********************************************************************************
	// Kruskal's
	//***********************************************************************************

	private int addMandatoryArcs() throws ContradictionException {
		int from,to,rFrom,rTo,arc;
		int tSize = 0;
		double val = propHK.getMinArcVal();
		for(int i=ma.size()-1;i>=0;i--){
			arc = ma.get(i);
			from = arc/n;
			to = arc%n;
			rFrom = FIND(from);
			rTo   = FIND(to);
			if(rFrom != rTo){
				LINK(rFrom, rTo);
				Tree.addArc(from, to);
				treeCost += costs[arc];
				// ne peut pas etre utilise pour remplacer un autre arc
				updateCCTree(rFrom, rTo, val);
				tSize++;
			}else{
				propHK.contradiction();
			}
		}
		return tSize;
	}

	private int addOutArcs() throws ContradictionException {
		int rFrom,rTo, from,to;
		int tSize = 0;
		int minArc;
		double val = propHK.getMinArcVal();
		double cost;
		for(int i=nR.get()-1;i>=0;i--){
			minArc = minCostOutArcs[i];
			if(minArc!=-1){
				minArc = minCostOutArcs[i];
				from = minArc/n;
				to   = minArc%n;
				rFrom = FIND(from);
				rTo   = FIND(to);
				if(rFrom != rTo){
					LINK(rFrom, rTo);
					Tree.addArc(from, to);
					cost = costs[minArc];
					updateCCTree(rFrom, rTo, val);// TODO devrait pouvoir dégager
					treeCost += cost;
					tSize++;
				}// else edge is mandatory and thus already treated
			}
		}
		return tSize;
	}

	private void connectMST(int tSize) throws ContradictionException {
		int from,to,rFrom,rTo;
		int idx = activeArcs.nextSetBit(0);
		minTArc = -propHK.getMinArcVal();
		maxTArc = propHK.getMinArcVal();
		double cost;
		while(tSize < n-1){
			if(idx<0){
				propHK.contradiction();
			}
			from = sortedArcs[idx]/n;
			to = sortedArcs[idx]%n;
			rFrom = FIND(from);
			rTo   = FIND(to);
			if(rFrom != rTo){
				LINK(rFrom, rTo);
				Tree.addArc(from, to);
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

	private void updateCCTree(int rfrom, int rto, double arcCost) {
		cctRoot++;
		int newNode = cctRoot;
		ccTree.activateNode(newNode);
		ccTree.addArc(newNode,ccTp[rfrom]);
		ccTree.addArc(newNode,ccTp[rto]);
		ccTp[rfrom] = newNode;
		ccTp[rto] = newNode;
		ccTEdgeCost[newNode] = arcCost;
	}
	
	private void LINK(int x, int y) {
		if(rank[x]>rank[y]){
			p[y] = p[x];
		}else{
			p[x] = p[y];
		}
		if(rank[x] == rank[y]){
			rank[y]++;
		}
	}

	private int FIND(int i) {
		if(p[i]!=i){
			p[i] = FIND(p[i]);
		}
		return p[i];
	}

//	private int getLCA(int i, int j) {
//		BitSet marked = new BitSet(ccN);
//		marked.set(i);
//		marked.set(j);
//		int p = ccTree.getPredecessorsOf(i).getFirstElement();
//		while(p!=-1){
//			marked.set(p);
//			p = ccTree.getPredecessorsOf(p).getFirstElement();
//		}
//		p = ccTree.getPredecessorsOf(j).getFirstElement();
//		while(p!=-1 && !marked.get(p)){
//			p = ccTree.getPredecessorsOf(p).getFirstElement();
//		}
//		return p;
//	}
}
