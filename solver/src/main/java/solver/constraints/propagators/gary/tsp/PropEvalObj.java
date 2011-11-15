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

package solver.constraints.propagators.gary.tsp;

import choco.kernel.ESat;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Compute the cost of the graph by summing arcs costs
 * */
public class PropEvalObj<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar sum;
	int[][] distMatrix;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Ensures that obj=SUM{costMatrix[i][j], (i,j) in arcs of graph}
	 *
	 * @param graph
	 * @param obj
	 * @param costMatrix
	 * @param constraint
	 * @param solver
	 * */
	public PropEvalObj(DirectedGraphVar graph, IntVar obj, int[][] costMatrix, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new Variable[]{graph,obj}, solver, constraint, PropagatorPriority.LINEAR, false);
		g = graph;
		sum = obj;
		distMatrix = costMatrix;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		int n = g.getEnvelopGraph().getNbNodes();
		int min = 0;
		int max = 0;
		int mc;
		int Mc;
		INeighbors succs;
		int[] nodeMin = new int[n];
		for(int i=0; i<n;i++){
			succs = g.getEnvelopGraph().getSuccessorsOf(i);
			Mc = 0;
			if(succs.neighborhoodSize()==0){
				mc = 0;
			}else{
				mc = distMatrix[i][succs.getFirstElement()];
				nodeMin[i] = mc;
			}
			for(int j=succs.getFirstElement(); j>=0; j=succs.getNextElement()){
				if(distMatrix[i][j]>Mc)Mc = distMatrix[i][j];
				if(distMatrix[i][j]<mc){
					mc = distMatrix[i][j];
					nodeMin[i] = mc;
				}
			}
			min += mc;
			max += Mc;
		}
		// filter the count variable
		sum.updateLowerBound(min,this,false);
		sum.updateUpperBound(max,this,false);

		// filter the graph
		for(int i=0; i<n;i++){
			succs = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=succs.getFirstElement(); j>=0; j=succs.getNextElement()){
				if(min-nodeMin[i]+distMatrix[i][j]>sum.getUB()){
					g.removeArc(i,j, this,false);
				}
			}
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		propagate();
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
