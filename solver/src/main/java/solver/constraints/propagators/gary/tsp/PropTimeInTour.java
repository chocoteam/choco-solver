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

package solver.constraints.propagators.gary.tsp;

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

public class PropTimeInTour extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	IntVar[] intVars;
	int[][] dist;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTimeInTour(IntVar[] intVars, DirectedGraphVar graph, int[][] dist, Constraint constraint, Solver solver) {
		super(intVars, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		this.intVars = intVars;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.dist = dist;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			upUB(i);
			upLB(i);
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(true){
			propagate(0);return;
		}
		if((mask & EventType.INCLOW.mask)!=0){
			upLB(idxVarInProp);
		}
		if((mask & EventType.DECUPP.mask)!=0){
			upUB(idxVarInProp);
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.DECUPP.mask + EventType.INCLOW.mask + EventType.INSTANTIATE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private void upUB(int var) throws ContradictionException {
		int val = intVars[var].getUB();
		int p = g.getKernelGraph().getPredecessorsOf(var).getFirstElement();
		if(p!=-1){
			if(intVars[p].updateUpperBound(val-dist[p][var],this)){
				upUB(p);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getPredecessorsOf(var);
			for(p=nei.getFirstElement();p>=0;p=nei.getNextElement()){
				if(intVars[p].getLB()>val-dist[p][var]){
					g.removeArc(p,var,this);
				}
			}
		}
	}
	private void upLB(int var) throws ContradictionException {
		int val = intVars[var].getLB();
		int s = g.getKernelGraph().getSuccessorsOf(var).getFirstElement();
		if(s!=-1){
			if(intVars[s].updateLowerBound(val+dist[var][s],this)){
				upLB(s);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(var);
			for(s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
				if(intVars[s].getUB()<val+dist[var][s]){
					g.removeArc(var,s,this);
				}
			}
		}
	}

}
