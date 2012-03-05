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

package solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp;

import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.constraints.propagators.gary.tsp.HeldKarp;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import java.util.Arrays;
import java.util.Comparator;

public class KruskalBST_GAC extends KruskalMST_GAC {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int[][] indexOfArc; // from lex (i,j) to sorted (i+1)*n+j
	TIntArrayList links;
	private int[] minCostOutArcs;
	// REDUCED GRAPH STRUCTURE
	protected IStateInt nR;
	protected IStateInt[] sccOf;
	protected INeighbors[] outArcs;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public KruskalBST_GAC(int nbNodes, HeldKarp propagator, IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs) {
		super(nbNodes,propagator);
		indexOfArc = new int[n][n];
		links = new TIntArrayList();
		minCostOutArcs = new int[n];
		this.nR = nR;
		this.sccOf = sccOf;
		this.outArcs = outArcs;
	}

	protected void sortArcs(double[][] costMatrix)  {
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
				if(propHK.isMandatory(f,t)){
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

	protected void pruning(double delta) throws ContradictionException {
		int i,j;
		double repCost;
		for(int arc=activeArcs.nextSetBit(0); arc>=0; arc=activeArcs.nextSetBit(arc+1)){
			i = sortedArcs[arc]/n;
			j = sortedArcs[arc]%n;
			if(!Tree.arcExists(i, j)){
				repCost = ccTEdgeCost[lca.getLCA(i,j)];
				if(costs[i*n+j]-repCost > delta){
					propHK.remove(i,j);
				}else{
					markTreeEdges(ccTp, i, j);
				}
			}
		}
		int arc,x;
		for(int k=links.size()-1;k>=0;k--){
			arc = links.get(k);
			i = arc/n;
			j = arc%n;
			if(g.arcExists(i,j)&&!Tree.arcExists(i, j)){
				x = sccOf[i].get();
				repCost = costs[minCostOutArcs[x]];
				if(costs[i*n+j]-repCost > delta){
					propHK.remove(i,j);
				}else{
					int f = minCostOutArcs[x]/n;
					int t = minCostOutArcs[x]%n;
					if(map[f][t]==-1 || costs[map[f][t]]>costs[i*n+j]){
						map[t][f] = map[f][t] = i*n+j;
					}
				}
			}
		}
		INeighbors nei;
		for(i=0;i<n;i++){
			nei = Tree.getSuccessorsOf(i);
			for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(map[i][j]==-1 || costs[map[i][j]]-costs[i*n+j]>delta){
					propHK.enforce(i,j);
				}
			}
		}
	}

	//***********************************************************************************
	// Kruskal's
	//***********************************************************************************

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

//	public double getRepCost(int from, int to){
//		if(map[from][to]==-1){
//			System.out.println(from+" : "+to+" / "+ma.contains(from*n+to));
//			System.out.println(Tree);
//			throw new UnsupportedOperationException();
//		}
//		return costs[map[from][to]]-costs[from*n+to];
//	}
}
