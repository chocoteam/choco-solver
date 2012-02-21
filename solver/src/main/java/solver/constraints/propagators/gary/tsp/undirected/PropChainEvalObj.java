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

package solver.constraints.propagators.gary.tsp.undirected;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.matrix.BitSetNeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * Compute the cost of the graph by summing edge costs
 * - For minimization problem
 * */
public class PropChainEvalObj<V extends Variable> extends PropCycleEvalObj<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int source, sink;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropChainEvalObj(UndirectedGraphVar graph, IntVar obj, int[][] costMatrix,
							int source, int sink,
							Constraint<V, Propagator<V>> constraint, Solver solver) {
		super(graph,obj,costMatrix,constraint,solver);
		this.source = source;
		this.sink   = sink;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

//	@Override
//	protected void filter(int minSum) throws ContradictionException {
//		INeighbors succs;
//		int delta = sum.getUB()-minSum;
//		for (int i = 0; i < n; i++) {
//			succs = g.getEnvelopGraph().getSuccessorsOf(i);
//			for (int j = succs.getFirstElement(); j >= 0; j = succs.getNextElement()) {
//				if(i<j && !g.getKernelGraph().edgeExists(i,j)){
//					if (replacementCost[i]==-1 || replacementCost[j]==-1) {
//						throw new UnsupportedOperationException();
//					}
//					if ((2*distMatrix[i][j]-replacementCost[i]-replacementCost[j])/2 > delta) {
//						g.removeArc(i, j, this);
//					}
////					if(i==source || i==sink){
////						if (distMatrix[i][j]-replacementCost[i] > delta) {
////							g.removeArc(i, j, this);
////						}
////					}else if(j==source || j==sink){
////						if (distMatrix[i][j]-replacementCost[j] > delta) {
////							g.removeArc(i, j, this);
////						}
////					}
//				}
//			}
//		}
//	}

//	protected int findTwoBest0(){
//		int mc1 = g.getKernelGraph().getSuccessorsOf(0).getFirstElement();
//		int mc2 = g.getKernelGraph().getSuccessorsOf(n-1).getFirstElement();
//		if(mc1!=-1){
//			replacementCost[0] = -1;
//			if(mc2!=-1){
//				replacementCost[n-1] = -1;
//			}else{
//				mc2 = getBestNot(n-1,mc1);
//				replacementCost[n-1] = distMatrix[n-1][mc2];
//			}
//		}else{
//			if(mc2!=-1){
//				replacementCost[n-1] = -1;
//				mc1 = getBestNot(0,mc2);
//				replacementCost[0] = distMatrix[0][mc1];
//			}else{
//				mc2 = getBestNot(n-1,-2);
//				mc1 = getBestNot(0,mc2);
//				int k = distMatrix[0][mc1]+distMatrix[n-1][mc2];
//				int mc1bis = getBestNot(0,-1);
//				int mc2bis = getBestNot(n-1,mc1bis);
//				int kbis = distMatrix[0][mc1bis]+distMatrix[n-1][mc2bis];
//				if(kbis<k){
//					mc2 = mc2bis;
//					mc1 = mc1bis;
//				}
//				replacementCost[n-1] = distMatrix[n-1][mc2];
//				replacementCost[0] = distMatrix[0][mc1];
//			}
//		}
//		return distMatrix[0][mc1]+distMatrix[n-1][mc2];
//	}

	protected int findTwoBest0(){
		int mc1 = g.getKernelGraph().getSuccessorsOf(0).getFirstElement();
		if(mc1!=-1){
			int mc2 = g.getKernelGraph().getSuccessorsOf(n-1).getNextElement();
			if(mc2!=-1){
				replacementCost[n-1] = replacementCost[0] = -1;
				return distMatrix[0][mc1] + distMatrix[0][mc2];
			}
			int cost = distMatrix[0][getBestNot(n-1,mc1)];
			replacementCost[n-1] = replacementCost[0] = cost;
			return distMatrix[0][mc1] + cost;
		}
		mc1 = g.getKernelGraph().getSuccessorsOf(n-1).getFirstElement();
		if(mc1!=-1){
			int cost = distMatrix[0][getBestNot(0,mc1)];
			replacementCost[n-1] = replacementCost[0] = cost;
			return distMatrix[0][mc1] + cost;
		}
		//todo
		INeighbors nei = new BitSetNeighbors(n);
		INeighbors suc = g.getEnvelopGraph().getSuccessorsOf(0);
		if(suc.neighborhoodSize()!=g.getEnvelopGraph().getSuccessorsOf(n-1).neighborhoodSize()){
			System.out.println("env");
			System.out.println(g.getEnvelopGraph());
			System.out.println("ker");
			System.out.println(g.getKernelGraph());
			System.exit(0);
			throw new UnsupportedOperationException();
		}
		for(int i=suc.getFirstElement();i>=0;i=suc.getNextElement()){
			nei.add(i);
		}
		suc = g.getEnvelopGraph().getSuccessorsOf(n-1);
		for(int i=suc.getFirstElement();i>=0;i=suc.getNextElement()){
			nei.add(i);
		}
		mc1 = getBestNot(nei,-2);
		int cost = distMatrix[0][getBestNot(nei,mc1)];
		replacementCost[n-1] = replacementCost[0] = cost;
		return distMatrix[0][mc1]+cost;
	}


//	protected int findTwoBest0(){
//		int mc1 = g.getKernelGraph().getSuccessorsOf(0).getFirstElement();
//		int mc2 = g.getKernelGraph().getSuccessorsOf(n-1).getFirstElement();
//		if(mc1!=-1){
//			if(mc2!=-1){
//				replacementCost[0] = -1;
//			}else{
//				mc2 = getBestNot(n-1,mc1);
//				replacementCost[0] = distMatrix[n-1][mc2];
//			}
//		}else{
//			if(mc2!=-1){
//				mc1 = getBestNot(0,mc2);
//				replacementCost[0] = distMatrix[0][mc1];
//			}else{
//				mc2 = getBestNot(n-1,-2);
//				mc1 = getBestNot(0,mc2);
//				int k = distMatrix[0][mc1]+distMatrix[n-1][mc2];
//				int mc1bis = getBestNot(0,-1);
//				int mc2bis = getBestNot(n-1,mc1bis);
//				int kbis = distMatrix[0][mc1bis]+distMatrix[n-1][mc2bis];
//				if(kbis<k){
//					mc2 = mc2bis;
//					mc1 = mc1bis;
//				}
//				replacementCost[0] = Math.max(distMatrix[0][mc1],distMatrix[0][mc2]);
//			}
//		}
//		replacementCost[n-1] = replacementCost[0];
//		return distMatrix[0][mc1]+distMatrix[n-1][mc2];
//	}

	protected int findTwoWorst0(){
		int mc1 = g.getKernelGraph().getSuccessorsOf(0).getFirstElement();
		int mc2 = g.getKernelGraph().getSuccessorsOf(n-1).getFirstElement();
		if(mc1!=-1){
			if(mc2!=-1){
			}else{
				mc2 = getWorstNot(n-1,mc1);
			}
		}else{
			if(mc2!=-1){
				mc1 = getWorstNot(0,mc2);
			}else{
				mc2 = getWorstNot(n-1,-2);
				mc1 = getWorstNot(0,mc2);
				int k = distMatrix[0][mc1]+distMatrix[n-1][mc2];
				int mc1bis = getWorstNot(0,-1);
				int mc2bis = getWorstNot(n-1,mc1bis);
				int kbis = distMatrix[0][mc1bis]+distMatrix[n-1][mc2bis];
				if(kbis>k){
					mc2 = mc2bis;
					mc1 = mc1bis;
				}
			}
		}
		return distMatrix[0][mc1]+distMatrix[n-1][mc2];
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int minSum =0;
		int maxSum =0;
		for (int i = 1; i < n-1; i++) {
			minSum += findTwoBest(i);
			maxSum += findTwoWorst(i);
		}
		minSum += findTwoBest0();
		maxSum += findTwoWorst0();
		if(maxSum%2!=0){
			maxSum++;
		}
		if(minSum%2!=0){
			minSum--;
		}
		minSum /= 2;
		maxSum /= 2;
		if(maxSum<0){
			maxSum = Integer.MAX_VALUE;
		}
		sum.updateLowerBound(minSum, this);
//		sum.updateUpperBound(maxSum, this);
		filter(minSum);
	}

	protected int getBestNot(INeighbors nei, int not) {
		int cost = -1;
		int idx = -1;
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			if(j!=not && (idx==-1 || cost>distMatrix[0][j])){
				idx = j;
				cost = distMatrix[0][j];
			}
		}
		if(idx==-1){
			throw new UnsupportedOperationException();
		}
		return idx;
	}
	protected int getBestNot(int i, int not) {
		if(not==0 || not==n-1){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			int cost = -1;
			int idx = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j!=0 && j!=n-1 && (cost==-1 || cost>distMatrix[i][j])){
					idx = j;
					cost = distMatrix[i][j];
				}
			}
			if(idx==-1){
				throw new UnsupportedOperationException();
			}
			return idx;
		}else{
			return super.getBestNot(i,not);
		}
	}
	protected int getWorstNot(int i, int not) {
		if(not==0 || not==n-1){
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
			int cost = -1;
			int idx = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j!=0 && j!=n-1 && (cost==-1 || cost<distMatrix[i][j])){
					idx = j;
					cost = distMatrix[i][j];
				}
			}
			if(idx==-1){
				throw new UnsupportedOperationException();
			}
			return idx;
		}else{
			return super.getWorstNot(i,not);
		}
	}
}
