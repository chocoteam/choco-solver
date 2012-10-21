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

package solver.constraints.propagators.gary.tsp.directed.lagrangianRelaxation;

import choco.kernel.memory.IStateInt;
import solver.constraints.propagators.gary.GraphLagrangianRelaxation;
import solver.constraints.propagators.gary.tsp.specificHeaps.FastArrayHeap;
import solver.constraints.propagators.gary.tsp.specificHeaps.MST_Heap;
import solver.exception.ContradictionException;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;

import java.util.BitSet;

public class PrimBSTFinder extends AbstractBSTFinder {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	double[][] costs;
	MST_Heap heap;
	BitSet inTree;
	private int tSize;
	private double minVal;
	double maxTArc;
	protected int currentSCC;
	private int start;
	private double[] minCostOutArc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PrimBSTFinder(int nbNodes, GraphLagrangianRelaxation propagator,int start, IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs) {
		super(nbNodes,propagator,nR,sccOf,outArcs);
		heap = new FastArrayHeap(nbNodes);
		inTree = new BitSet(n);
		this.start   = start;
		minCostOutArc = new double[n];
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public void computeMST(double[][] costs, DirectedGraph graph) throws ContradictionException {
		g = graph;
		for(int i=0;i<n;i++){
			Tree.getSuccessorsOf(i).clear();
			Tree.getPredecessorsOf(i).clear();
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
			maxTArc = minVal;
		}
		currentSCC = sccOf[start].get();
		addNode(start);
		while(tSize<n-1 && heap.isEmpty()){
			nextSCC();
		}
		int from,to;
		while (tSize<n-1 && !heap.isEmpty()){
			while (tSize<n-1 && !heap.isEmpty()){
				to = heap.pop();
				from = heap.getMate(to);
				addArc(from,to);
			}
			while(tSize<n-1 && heap.isEmpty()){
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
		int from,to;
		int firstArc = outArcs[currentSCC].getFirstElement();
		int minFrom = firstArc/n-1;
		int minTo   = firstArc%n;
		double minVal = costs[minFrom][minTo];
		for(int out=outArcs[currentSCC].getFirstElement();out>=0;out=outArcs[currentSCC].getNextElement()){
			from = out/n-1;
			to   = out%n;
			if(propHK.isMandatory(from,to)){
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
		Tree.addArc(minFrom,minTo);
		treeCost += minVal;
		tSize++;
		currentSCC = sccOf[minTo].get();
		addNode(minTo);
	}

	private void addArc(int from, int to) {
		if(from<n){
			if(sccOf[from].get()!=sccOf[to].get()){
				throw new UnsupportedOperationException();
			}
			if(Tree.arcExists(to,from)){
				return;
			}
			Tree.addArc(from,to);
			treeCost += costs[from][to];
			if(FILTER){
				if(!propHK.isMandatory(from,to)){
					maxTArc = Math.max(maxTArc, costs[from][to]);
				}
			}
		}else{
			from -= n;
			if(sccOf[from].get()!=sccOf[to].get()){
				throw new UnsupportedOperationException();
			}
			if(Tree.arcExists(from,to)){
				return;
			}
			Tree.addArc(to, from);
			treeCost += costs[to][from];
			if(FILTER){
				if(!propHK.isMandatory(to,from)){
					maxTArc = Math.max(maxTArc, costs[to][from]);
				}
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
				if((!inTree.get(j)) && sccOf[j].get()==currentSCC){
					if(propHK.isMandatory(i,j)){
						heap.add(j,minVal,i);
					}else{
						heap.add(j,costs[i][j],i);
					}
				}
			}
			nei = g.getPredecessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if((!inTree.get(j)) && sccOf[j].get()==currentSCC){
					if(propHK.isMandatory(j,i)){
						heap.add(j,minVal,i+n);
					}else{
						heap.add(j,costs[j][i],i+n);
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
					if((!Tree.arcExists(i,j))){
						if(sccOf[i].get()==sccOf[j].get()){
							if(costs[i][j]-maxTArc > delta){
								propHK.remove(i,j);
							}
						}else if(costs[i][j]-minCostOutArc[sccOf[i].get()] > delta){
							propHK.remove(i,j);
						}
					}
				}
			}
		}else{
			throw new UnsupportedOperationException("bound computation only, no filtering!");
		}
	}

	public double getRepCost(int from, int to){
		return 0;//costs[from][to];// approximation of course
	}
}
