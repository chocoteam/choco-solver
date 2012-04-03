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
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class PropEnergeticTime extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n,nbTrucks,horizon;
	IntVar[] time,truck;
	int[][] travelTime;
	int[] duration;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropEnergeticTime(IntVar[] time, IntVar[] truck, DirectedGraphVar graph, int[][] travelTimeMatrix, int nbTrucks, int horizon, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(time,new Variable[]{graph}), solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.time = time;
		this.truck= truck;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.travelTime = travelTimeMatrix;
		this.nbTrucks = nbTrucks;
		this.horizon  = horizon;
		duration = new int[n];
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		INeighbors nei;
		int totalTime = 0;
		int tmp,ub,minD;
		for(int i=0;i<n;i++){
			ub = time[i].getUB();
			minD = Integer.MAX_VALUE;
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				tmp = Math.max(time[j].getLB()-ub,travelTime[i][j]);
				if(tmp<minD){
					minD = tmp;
					duration[i] = minD;
				}
			}
			totalTime+=minD;
		}
		if(totalTime>(nbTrucks*horizon)){
			contradiction(g,"");
		}
		heavyTaskInter();
	}

	private void heavyTaskInter() throws ContradictionException {
		for(int i=0;i<n;i++){
			if(!truck[i].instantiated()){
				for(int j=i+1;j<n;j++){
					if(!truck[j].instantiated())
						check(i,j);
				}
			}
		}
	}

	private void check(int i, int j) throws ContradictionException {
		int first = Math.min(time[i].getLB(),time[j].getLB());
		int last  = Math.max(time[i].getUB()+duration[i],time[j].getUB()+duration[i]);
		if(first>last){
			throw new UnsupportedOperationException();
		}
		int q = 0;
		for(int k=0;k<n;k++){
			if(time[k].getLB()>=first && time[k].getUB()+duration[k]<=last){
				q+=duration[k];
			}
		}
		if(q > nbTrucks*(last-first)){
			contradiction(g,"");
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INCLOW.mask+EventType.DECUPP.mask+EventType.INSTANTIATE.mask+EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
