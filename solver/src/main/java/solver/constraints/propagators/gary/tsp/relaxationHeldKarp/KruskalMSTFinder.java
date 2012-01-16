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

import gnu.trove.list.array.TIntArrayList;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

public class KruskalMSTFinder extends AbstractMSTFinder{

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

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public KruskalMSTFinder(int nbNodes, HeldKarp propagator) {
		super(nbNodes,propagator);
		activeArcs = new BitSet(n*n);
		rank = new int[n];
		costs = new double[n*n];
		sortedArcs = new int[n*n];
		indexOfArc = new int[n][n];
		p = new int[n];
	}

	private void sortArcs(double[][] costMatrix){
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
		int idx=0;
		int size = 0;
		INeighbors nei;
		for(int i=0;i<n;i++){
			p[i] = i;
			Tree.getNeighborsOf(i).clear();
			size += g.getNeighborsOf(i).neighborhoodSize();
		}
		Integer[] integers = new Integer[size];
		for(int i=0;i<n;i++){
			nei = g.getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				integers[idx]=i*n+j;
				costs[i*n+j] = costMatrix[i][j];
				idx++;
			}
		}
		Arrays.sort(integers,comp);
		int v;
		activeArcs.clear();
		activeArcs.flip(0,size-1);
		for(idx = 0; idx<size; idx++){
			v = integers[idx];
			sortedArcs[idx] = v;
			indexOfArc[v/n][v%n] = idx;
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
		g = graph;
		ma = propHK.getMandatoryArcsList();
		sortArcs(costs);
		treeCost = 0;
		int tSize = addMandatoryArcs();
		if(tSize>n-1){throw new UnsupportedOperationException("too many arcs in the MST");}
		connectMST(tSize);
	}

	public void performPruning(double UB) throws ContradictionException{
		throw new UnsupportedOperationException("bound computation only, no filtering!");
	}

	//***********************************************************************************
	// Kruskal's
	//***********************************************************************************

	private int addMandatoryArcs() throws ContradictionException {
		int from,to,rFrom,rTo,arc;
		int tSize = 0;
		for(int i=ma.size()-1;i>=0;i--){
			arc = ma.get(i);
			from = arc/n;
			to = arc%n;
			rFrom = FIND(from);
			rTo   = FIND(to);
			if(rFrom != rTo){
				LINK(rFrom, rTo);
				Tree.addEdge(from, to);
				treeCost += costs[arc];
				tSize++;
			}else{
				propHK.contradiction();
			}
		}
		return tSize;
	}

	private void connectMST(int tSize) throws ContradictionException {
		int from,to,rFrom,rTo;
		int idx = activeArcs.nextSetBit(0);
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
				Tree.addEdge(from, to);
				treeCost += costs[sortedArcs[idx]];
				tSize++;
			}
			idx = activeArcs.nextSetBit(idx+1);
		}
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
}
