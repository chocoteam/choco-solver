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

package solver.constraints.propagators.gary.tsp.directed;

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

public class PropIntVarChanneling extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	IntVar[] intVars;
	private int varIdx;
	private IntProcedure arcEnforced;
	private IntProcedure arcRemoved;
	private IntProcedure valRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** Links intVars and the graph
	 * arc (x,y)=var[x]=y
	 * values outside range [0,n-1] are not considered
	 * @param intVars
	 * @param graph
	 * @param constraint
	 * @param solver
	 * */
	public PropIntVarChanneling(IntVar[] intVars, DirectedGraphVar graph, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(intVars,new Variable[]{graph}), solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.intVars = intVars;
		this.n = g.getEnvelopGraph().getNbNodes();
		valRemoved  = new ValRem(this);
		arcEnforced = new EnfArc(this);
		if(intVars[0].hasEnumeratedDomain()){
			arcRemoved  = new RemArcAC(this);
		}else{
			arcRemoved  = new RemArcBC(this);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		INeighbors nei;
		IntVar v;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!intVars[i].contains(j)){
					g.removeArc(i,j,this);
				}
			}
			v = intVars[i];
			int ub = v.getUB();
			for(int j=v.getLB();j<=ub;j=v.nextValue(j)){
				if(j<n && !g.getEnvelopGraph().arcExists(i,j)){
					v.removeValue(j,this);
				}
			}
			if(!v.hasEnumeratedDomain()){
				ub = v.getUB();
				while(ub>=0 && ub<n && !g.getEnvelopGraph().arcExists(i,ub)){
					v.removeValue(ub,this);
					ub--;
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if((vars[idxVarInProp].getTypeAndKind() & Variable.GRAPH)!=0) {
			if((mask & EventType.ENFORCEARC.mask) !=0){
				eventRecorder.getDeltaMonitor(this, g).forEach(arcEnforced, EventType.ENFORCEARC);
			}
			if((mask & EventType.REMOVEARC.mask)!=0){
				eventRecorder.getDeltaMonitor(this, g).forEach(arcRemoved, EventType.REMOVEARC);
			}
		}else{
			varIdx = idxVarInProp;
			int val = intVars[varIdx].getLB();
			if((mask & EventType.INSTANTIATE.mask)!=0 && val<n){
				g.enforceArc(varIdx,val,this);
			}
			eventRecorder.getDeltaMonitor(this, vars[idxVarInProp]).forEach(valRemoved, EventType.REMOVE);
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		for(int i=0;i<vars.length;i++){
			if(!vars[i].instantiated()){
				return ESat.UNDEFINED;
			}
		}
		int val;
		for(int i=0;i<n;i++){
			val = intVars[i].getValue();
			if(val<n && !g.getEnvelopGraph().arcExists(i,val)){
				return ESat.FALSE;
			}
			if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()>1){
				return ESat.FALSE;
			}
		}
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class ValRem implements IntProcedure{
		private GraphPropagator p;

		private ValRem(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			g.removeArc(varIdx,i,p);
		}
	}

	private class EnfArc implements IntProcedure {
		private GraphPropagator p;

		private EnfArc(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			intVars[i/n-1].instantiateTo(i%n,p);
		}
	}

	private class RemArcAC implements IntProcedure{
		private GraphPropagator p;

		private RemArcAC(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			intVars[i/n-1].removeValue(i%n,p);
		}
	}

	private class RemArcBC implements IntProcedure{
		private GraphPropagator p;

		private RemArcBC(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to = i%n;
			if(to==intVars[from].getLB()){
				while(to<n && !g.getEnvelopGraph().arcExists(from,to)){
					to++;
				}
				intVars[from].updateLowerBound(to,p);
			}else if(to==intVars[from].getUB()){
				while(to>=0 && !g.getEnvelopGraph().arcExists(from,to)){
					to--;
				}
				intVars[from].updateUpperBound(to, p);
			}
		}
	}
}
