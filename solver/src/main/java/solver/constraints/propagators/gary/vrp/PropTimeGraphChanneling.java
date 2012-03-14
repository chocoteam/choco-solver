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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.gary.vrp;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class PropTimeGraphChanneling extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	IntVar[] time;
	int[][] travelTime;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTimeGraphChanneling(IntVar[] time, DirectedGraphVar graph, int[][] travelTimeMatrix, Constraint constraint, Solver solver) {
		super(time, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.time = time;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.travelTime = travelTimeMatrix;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			if(nei.neighborhoodSize()>0){
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(time[i].getLB()+travelTime[i][j]>time[j].getUB()){
						g.removeArc(i,j,this);
					}
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		int i = idxVarInProp;
		INeighbors nei;
		nei = g.getEnvelopGraph().getSuccessorsOf(i);
		if(nei.neighborhoodSize()>0){
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(time[i].getLB()+travelTime[i][j]>time[j].getUB()){
					g.removeArc(i,j,this);
				}
			}
		}
		nei = g.getEnvelopGraph().getPredecessorsOf(i);
		if(nei.neighborhoodSize()>0){
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(time[j].getLB()+travelTime[j][i]>time[i].getUB()){
					g.removeArc(j,i,this);
				}
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INCLOW.mask+EventType.DECUPP.mask+EventType.INSTANTIATE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
