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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * @PropAnn(tested = {BENCHMARK})
 * @param <V>
 */
public class PropFastHeldKarp<V extends Variable> extends PropHeldKarp {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected double[] inPenalities,outPenalities;
	protected double totalPenalities;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** MST based HK */
	private PropFastHeldKarp(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super(graph,from,to,cost,costMatrix,constraint,solver);
		inPenalities = new double[n];
		outPenalities = new double[n];
	}


	/** MST based HK */
	public static PropFastHeldKarp mstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver) {
		PropFastHeldKarp phk = new PropFastHeldKarp(graph,from,to,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalMSTFinderWithFiltering(phk.n,phk);
//		phk.HKfilter = new PrimMSTFinder(phk.n,phk);
		phk.HK = new PrimMSTFinder(phk.n,phk);
		return phk;
	}

	/** BST based HK */
	public static PropFastHeldKarp bstBasedRelaxation(DirectedGraphVar graph, int from, int to, IntVar cost, int[][] costMatrix, Constraint constraint, Solver solver, IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs) {
		PropFastHeldKarp phk = new PropFastHeldKarp(graph,from,to,cost,costMatrix,constraint,solver);
		phk.HKfilter = new KruskalBSTFinderWithFiltering(phk.n,phk,nR,sccOf,outArcs);
//		phk.HKfilter = new KruskalMSTFinderWithFiltering(phk.n,phk);
		phk.sccOf = sccOf;
		phk.nr = nR;
//		phk.HKfilter = new PrimBSTFinder(phk.n,phk,from,nR,sccOf,outArcs);
		phk.HK = new PrimBSTFinder(phk.n,phk,from,nR,sccOf,outArcs);
//		phk.HK = new PrimMSTFinder(phk.n,phk);
		return phk;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	protected void setupMatrix() throws ContradictionException {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				costs[i][j] = originalCosts[i][j] + outPenalities[i] + inPenalities[j];
			}
		}
	}

	protected double getTotalPenalties(){
		return totalPenalities;
	}

	protected void HKPenalities() {
		if(step==0){
			return;
		}
		double totalPenalities = 0;
		int inDeg,outDeg;
		for(int i=0;i<n;i++){
			inDeg = mst.getPredecessorsOf(i).neighborhoodSize();
			outDeg = mst.getSuccessorsOf(i).neighborhoodSize();
			if(i==source){
				outPenalities[i] += (outDeg-1)*step;
				if(inPenalities[i]!=0){
					throw new UnsupportedOperationException();
				}
			}else if(i==sink){
				inPenalities[i] += (inDeg-1)*step;
				if(outPenalities[i]!=0){
					throw new UnsupportedOperationException();
				}
			}else{
				inPenalities[i] += (inDeg-1)*step;
				outPenalities[i]+= (outDeg-1)*step;
			}
			totalPenalities += inPenalities[i]+outPenalities[i];
		}
		this.totalPenalities = totalPenalities;
	}
	
	protected void updateCostMatrix() {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
				costs[i][j] = originalCosts[i][j] + outPenalities[i] + inPenalities[j];
			}
		}
	}
}
