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
import choco.kernel.common.util.procedure.IntProcedure;
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

public class PropPosInTour extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	IntVar[] intVars;
	private int varIdx;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropPosInTour(IntVar[] intVars, DirectedGraphVar graph, Constraint constraint, Solver solver) {
		super(intVars, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.intVars = intVars;
		this.n = g.getEnvelopGraph().getNbNodes();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			upUB(i,intVars[i].getUB());
			upLB(i,intVars[i].getLB());
			if(intVars[i].instantiated()){
				enfVarPos(i,intVars[i].getValue());
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		varIdx = idxVarInProp;
		if((mask & EventType.INCLOW.mask)!=0){
			upLB(idxVarInProp,intVars[idxVarInProp].getLB());
		}
		if((mask & EventType.DECUPP.mask)!=0){
			upUB(idxVarInProp,intVars[idxVarInProp].getUB());
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private void enfVarPos(int var, int val) throws ContradictionException {
		int p = g.getKernelGraph().getPredecessorsOf(var).getFirstElement();
		if(p!=-1){
			if(intVars[p].instantiateTo(val-1,this,false)){
				enfVarPos(p,val-1);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getPredecessorsOf(var);
			for(p=nei.getFirstElement();p>=0;p=nei.getNextElement()){
				if(!intVars[p].contains(val-1)){
					g.removeArc(p,var,this,false);
				}
			}
		}
		int s = g.getKernelGraph().getSuccessorsOf(var).getFirstElement();
		if(s!=-1){
			if(intVars[s].instantiateTo(val+1,this,false)){
				enfVarPos(s,val+1);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(var);
			for(s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
				if(!intVars[s].contains(val+1)){
					g.removeArc(var,s,this,false);
				}
			}
		}
	}

	private void upUB(int var, int val) throws ContradictionException {
		int p = g.getKernelGraph().getPredecessorsOf(var).getFirstElement();
		if(p!=-1){
			if(intVars[p].updateUpperBound(val-1,this,false)){
				upUB(p,val-1);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getPredecessorsOf(var);
			for(p=nei.getFirstElement();p>=0;p=nei.getNextElement()){
				if(intVars[p].getLB()>val-1){
					g.removeArc(p,var,this,false);
				}
			}
		}
		int s = g.getKernelGraph().getSuccessorsOf(var).getFirstElement();
		if(s!=-1){
			if(intVars[s].updateUpperBound(val+1,this,false)){
				upUB(s,val+1);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(var);
			for(s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
				if(intVars[s].getLB()>val+1){
					g.removeArc(var,s,this,false);
				}
			}
		}
	}
	private void upLB(int var, int val) throws ContradictionException {
		int p = g.getKernelGraph().getPredecessorsOf(var).getFirstElement();
		if(p!=-1){
			if(intVars[p].updateLowerBound(val-1,this,false)){
				upLB(p,val-1);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getPredecessorsOf(var);
			for(p=nei.getFirstElement();p>=0;p=nei.getNextElement()){
				if(intVars[p].getUB()<val-1){
					g.removeArc(p,var,this,false);
				}
			}
		}
		int s = g.getKernelGraph().getSuccessorsOf(var).getFirstElement();
		if(s!=-1){
			if(intVars[s].updateLowerBound(val+1,this,false)){
				upLB(s,val+1);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(var);
			for(s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
				if(intVars[s].getUB()<val+1){
					g.removeArc(var,s,this,false);
				}
			}
		}
	}

	private void remVarPos(int var, int val) throws ContradictionException {
		int p = g.getKernelGraph().getPredecessorsOf(var).getFirstElement();
		if(p!=-1){
			if(intVars[p].removeValue(val-1,this,false)){
				remVarPos(p,val-1);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getPredecessorsOf(var);
			for(p=nei.getFirstElement();p>=0;p=nei.getNextElement()){
				if(intVars[p].instantiatedTo(val-1)){
					g.removeArc(p,var,this,false);
				}
			}
		}
		int s = g.getKernelGraph().getSuccessorsOf(var).getFirstElement();
		if(s!=-1){
			if(intVars[s].removeValue(val+1,this,false)){
				remVarPos(s,val+1);
			}
		}else{
			INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(var);
			for(s=nei.getFirstElement();p>=0;p=nei.getNextElement()){
				if(intVars[s].instantiatedTo(val+1)){
					g.removeArc(var,s,this,false);
				}
			}
		}
	}

	private class ValRem implements IntProcedure{
		private GraphPropagator p;

		private ValRem(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			remVarPos(varIdx,i);
		}
	}
}
