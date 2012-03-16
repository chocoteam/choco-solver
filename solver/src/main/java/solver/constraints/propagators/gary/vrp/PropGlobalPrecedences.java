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
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
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

public class PropGlobalPrecedences extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	IntVar[] time;
	IStateInt[] extremity;
	int[][] travelTime;
	private TIntArrayList precedFrom,precedTo;
	int nbRequests;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropGlobalPrecedences(IntVar[] time, DirectedGraphVar graph, int[][] travelTimeMatrix, TIntArrayList precedFrom,TIntArrayList precedTo,Constraint constraint, Solver solver) {
		super(ArrayUtils.append(time,new Variable[]{graph}), solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.time = time;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.travelTime = travelTimeMatrix;
		this.precedFrom = precedFrom;
		this.precedTo = precedTo;
		nbRequests = precedFrom.size();
		extremity = new IStateInt[n];
		for(int k=0;k<nbRequests;k++){
			extremity[precedFrom.get(k)] = environment.makeInt(precedFrom.get(k));
			extremity[precedTo.get(k)] = environment.makeInt(precedTo.get(k));
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int k=0;k<nbRequests;k++){
//			simpleCheckPrec(precedFrom.get(k),precedTo.get(k));
			pathCheckPrec(precedFrom.get(k), precedTo.get(k));
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	private void simpleCheckPrec(int from, int to) throws ContradictionException {
		INeighbors nei;
		int tmp;
		int minD = Integer.MAX_VALUE;
		int lb  = time[from].getLB();
		nei = g.getEnvelopGraph().getSuccessorsOf(from);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			tmp = Math.max(time[j].getLB(),lb+travelTime[from][j]);
			if(tmp<minD){
				minD = tmp;
			}
		}
		time[to].updateLowerBound(minD,this);

		minD = 0;
		int ub  = time[to].getUB();
		nei = g.getEnvelopGraph().getPredecessorsOf(to);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			tmp = Math.min(time[j].getUB(),ub-travelTime[j][to]);
			if(tmp>minD){
				minD = tmp;
			}
		}
		time[from].updateUpperBound(minD, this);
	}

	private void pathCheckPrec(int orifrom, int oriTo) throws ContradictionException {
		int from = extremity[orifrom].get();
		int to   = extremity[oriTo].get();
		int x = g.getKernelGraph().getSuccessorsOf(from).getFirstElement();
		while(x!=-1){
			if(x==to){
				return;
			}
			from = x;
			x = g.getKernelGraph().getSuccessorsOf(from).getFirstElement();
		}
		extremity[orifrom].set(from);
		int y = g.getKernelGraph().getPredecessorsOf(to).getFirstElement();
		while(y!=-1){
			if(y==from){
				throw new UnsupportedOperationException();
			}
			to = y;
			y = g.getKernelGraph().getPredecessorsOf(to).getFirstElement();
		}
		extremity[oriTo].set(to);

		INeighbors nei;
		int tmp;
		int minD = Integer.MAX_VALUE;
		int lb  = time[from].getLB();
		nei = g.getEnvelopGraph().getSuccessorsOf(from);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			tmp = Math.max(time[j].getLB(),lb+travelTime[from][j]);
			if(tmp<minD){
				minD = tmp;
			}
		}
		time[to].updateLowerBound(minD,this);
		minD = 0;
		int ub  = time[to].getUB();
		nei = g.getEnvelopGraph().getPredecessorsOf(to);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			tmp = Math.min(time[j].getUB(),ub-travelTime[j][to]);
			if(tmp>minD){
				minD = tmp;
			}
		}
		time[from].updateUpperBound(minD, this);
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
