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
import solver.constraints.propagators.gary.tsp.heaps.Heap;
import solver.constraints.propagators.gary.tsp.heaps.VerySimpleHeap;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraph;

import java.util.BitSet;

public class PrimBSTFinder extends AbstractMSTFinder{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected BitSet ma; 	//mandatory arcs (i,j) <-> i*n+j
	double[][] costs;
	Heap heap;
	BitSet inTree;
	private int tSize;
	private double minVal;
	double maxTArc;
	protected IStateInt nR;
	protected IStateInt[] sccOf;
	protected INeighbors[] outArcs;
	protected double[] minCostOutArc;
	protected int currentSCC;
	private int start;
	private final static boolean FILTER = false;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PrimBSTFinder(int nbNodes, HeldKarp propagator,int start, IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs) {
		super(nbNodes,propagator);
		heap = new VerySimpleHeap(nbNodes);
		inTree = new BitSet(n);
		this.nR = nR;
		this.sccOf = sccOf;
		this.outArcs = outArcs;
		this.start   = start;
		minCostOutArc= new double[n];
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void computeMST(double[][] costs, UndirectedGraph graph) throws ContradictionException {
		g = graph;
		ma = propHK.getMandatoryArcsBitSet();
		for(int i=0;i<n;i++){
			Tree.getNeighborsOf(i).clear();
		}
		this.costs = costs;
		heap.clear();
		inTree.clear();
		treeCost = 0;
		tSize = 0;
		minVal = propHK.getMinArcVal();
		prim();
	}

	private void prim() throws ContradictionException {
		if(FILTER){
			maxTArc = propHK.getMinArcVal();
		}
		currentSCC = sccOf[start].get();
		addNode(start);
		int from,to;
		while (tSize<n-1 && !heap.isEmpty()){
			while (tSize<n-1 && !heap.isEmpty()){
				to = heap.pop();
				from = heap.getMate(to);
				addArc(from,to);
			}
			if(tSize<n-1){
				nextSCC();
			}
		}
		if(tSize!=n-1){
			propHK.contradiction();
		}
	}

	private void nextSCC() throws ContradictionException {
		if(outArcs[currentSCC].neighborhoodSize()==0){
			propHK.contradiction();
		}
		int smallN = n/2;
		int from,to;
		int firstArc = outArcs[currentSCC].getFirstElement();
		int minFrom = firstArc/smallN-1;
		int minTo   = firstArc%smallN+smallN;
		double minVal = costs[minFrom][minTo];
		for(int out=outArcs[currentSCC].getFirstElement();out>=0;out=outArcs[currentSCC].getNextElement()){
			from = out/smallN-1;
			to   = out%smallN+smallN;
			if(propHK.getMandatorySuccessorOf(from)==out%smallN){
				minVal = costs[from][to];
				minFrom = from;
				minTo  = to;
				break;
			}
			if(costs[from][to]<minVal){
				minVal = costs[from][to];
				minFrom = from;
				minTo  = to;
			}
		}
		minCostOutArc[currentSCC] = minVal;
		Tree.addEdge(minFrom,minTo);
		treeCost += minVal;
		tSize++;
		currentSCC = sccOf[minTo-smallN].get();
		addNode(minTo);
	}

	private void addArc(int from, int to) {
		Tree.addEdge(from,to);
		treeCost += costs[from][to];
		if(FILTER){
			if(!(ma.get(from*n+to)||ma.get(from+to*n))){
				maxTArc = Math.max(maxTArc, costs[from][to]);
			}
		}
		tSize++;
		addNode(to);
	}

	private void addNode(int i) {
		int y;
		int smallN = n/2;
		if(!inTree.get(i)){
			inTree.set(i);
			INeighbors nei = g.getNeighborsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				y = j;
				if(y>=smallN){
					y-=smallN;
				}
				if((!inTree.get(j)) && sccOf[y].get()==currentSCC){
					if(ma.get(i*n+j)||ma.get(i+j*n)){
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
			int x,y;
			int smallN = n/2;
			for(int i=0;i<n;i++){
				nei = g.getNeighborsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if((!Tree.edgeExists(i,j))){
						if(i>=smallN){
							x=j;
							y=i-smallN;
						}else{
							x=i;
							y=j-smallN;
						}
						if(sccOf[x].get()==sccOf[y].get()){
							if(costs[i][j]-maxTArc > delta){
								propHK.remove(i,j);
							}
						}else if(costs[i][j]-minCostOutArc[sccOf[x].get()] > delta){
							propHK.remove(i,j);
						}
					}
				}
			}
		}else{
			throw new UnsupportedOperationException("bound computation only, no filtering!");
		}
	}
}
