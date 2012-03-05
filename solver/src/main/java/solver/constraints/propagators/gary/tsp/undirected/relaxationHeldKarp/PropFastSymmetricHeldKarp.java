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

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * @PropAnn(tested = {BENCHMARK})
 * @param <V>
 */
public class PropFastSymmetricHeldKarp<V extends Variable> extends PropSymmetricHeldKarp {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	double[] penalities;
	double totalPenalities;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	private PropFastSymmetricHeldKarp(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super(graph,cost,costMatrix,constraint,solver);
		penalities = new double[n];
	}

	/** MST based HK */
//	public static PropFastSymmetricHeldKarp mstBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
//		PropFastSymmetricHeldKarp phk = new PropFastSymmetricHeldKarp(graph,cost,costMatrix,constraint,solver);
//		phk.HKfilter = new KruskalMSTFinder(phk.n,phk);
//		phk.HK = new PrimMSTFinder(phk.n,phk);
//		phk.treeMode = MST;
//		throw new UnsupportedOperationException("Are you sure?");
////		return phk;
//	}
	/** ONE TREE based HK */
	public static PropFastSymmetricHeldKarp oneTreeBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropFastSymmetricHeldKarp phk = new PropFastSymmetricHeldKarp(graph,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalOneTree_GAC(phk.n,phk);
//		phk.HKfilter = new KruskalOneTreeFinder(phk.n,phk);
		phk.HK = new PrimOneTreeFinder(phk.n,phk);
//		phk.HKfilter = phk.HK;
//		System.exit(0);
		phk.treeMode = ONE_TREE;
		return phk;
	}
	/** TWO TREE based HK */
//	public static PropFastSymmetricHeldKarp twoTreeBasedRelaxation(UndirectedGraphVar graph, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
//		PropFastSymmetricHeldKarp phk = new PropFastSymmetricHeldKarp(graph,cost,costMatrix,constraint,solver);
////		phk.HKfilter = new KruskalTwoTreeFinder(phk.n,phk);
//		phk.HK = new PrimTwoTreeFinder(phk.n,phk);
//		phk.HKfilter = phk.HK;
//		phk.treeMode = TWO_TREE;
//		return phk;
//	}

	//***********************************************************************************
	// HK Algorithm(s)
	//***********************************************************************************

	protected void setCosts() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					costs[j][i] = costs[i][j];
				}
			}
		}
	}

	//***********************************************************************************
	// DETAILS
	//***********************************************************************************

	protected void HKPenalities() {
		if(step==0){
			return;
		}
		double sumPenalities = 0;
		int deg;
		for(int i=1;i<n-1;i++){
			deg = mst.getNeighborsOf(i).neighborhoodSize();
			penalities[i] += (deg-2)*step;
			if(penalities[i]>Double.MAX_VALUE/(n-1) || penalities[i]<-Double.MAX_VALUE/(n-1)){
				throw new UnsupportedOperationException();
			}
			sumPenalities += penalities[i];
		}
		int degFirst = mst.getNeighborsOf(0).neighborhoodSize();
		int degLast = mst.getNeighborsOf(n-1).neighborhoodSize();
		switch(treeMode){
			case MST:
				penalities[0] += (degFirst-1)*step;
				penalities[n-1]+=(degLast-1)*step;
				sumPenalities += (penalities[0]+penalities[n-1])/2;break;
			case ONE_TREE:
				penalities[n-1] += (degLast-2)*step;
				sumPenalities += penalities[n-1];
				if(degFirst!=2){
					throw new UnsupportedOperationException();
				};break;
			case TWO_TREE:
				if(degFirst!=1||degFirst!=1){
					throw new UnsupportedOperationException();
				}break;
		}
		this.totalPenalities = 2*sumPenalities;
	}

	protected void updateCostMatrix() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				if(i<j){
					costs[i][j] = originalCosts[i][j] + penalities[i] + penalities[j];
					costs[j][i] = costs[i][j];
				}
			}
		}
	}

	protected double getTotalPen(){
		return totalPenalities;
	}
}
