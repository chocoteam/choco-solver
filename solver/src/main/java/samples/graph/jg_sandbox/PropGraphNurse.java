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

package samples.graph.jg_sandbox;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public class PropGraphNurse extends GraphPropagator<Variable>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private UndirectedGraphVar g;
	private BoolVar[] nurses;
	private IntProcedure enf_proc, rem_proc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropGraphNurse(UndirectedGraphVar graph, BoolVar[] nurses, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(nurses,new Variable[]{graph}), solver, constraint, PropagatorPriority.UNARY);
		g = graph;
		this.nurses = nurses;
		enf_proc = new NodeEnf(this);
		rem_proc = new NodeRem(this);
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************


	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<nurses.length;i++){
			if(nurses[i].getBooleanValue().equals(ESat.TRUE)){
				g.enforceNode(i,this);
			}else if(nurses[i].getBooleanValue().equals(ESat.TRUE)){
				g.removeNode(i,this);
			}else{
				if(g.getKernelGraph().getActiveNodes().isActive(i)){
					nurses[i].setToTrue(this,false);
				}
				if(!g.getEnvelopGraph().getActiveNodes().isActive(i)){
					nurses[i].setToFalse(this,false);
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(idxVarInProp<nurses.length){
			if(nurses[idxVarInProp].getBooleanValue().equals(ESat.TRUE)){
				g.enforceNode(idxVarInProp,this);
			}else if(nurses[idxVarInProp].getBooleanValue().equals(ESat.FALSE)){
				g.removeNode(idxVarInProp,this);
			}else{
				throw new UnsupportedOperationException();
			}
		}else{
			eventRecorder.getDeltaMonitor(this, g).forEach(enf_proc, EventType.ENFORCENODE);
			eventRecorder.getDeltaMonitor(this, g).forEach(rem_proc, EventType.REMOVENODE);
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCENODE.mask+EventType.REMOVENODE.mask+EventType.INSTANTIATE.strengthened_mask;
	}

	@Override
	public ESat isEntailed() {
		//TODO
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class NodeEnf implements IntProcedure{
		Propagator p;
		NodeEnf(Propagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			nurses[i].setToTrue(p,false);
		}
	}
	
	private class NodeRem implements IntProcedure{
		Propagator p;
		NodeRem(Propagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			nurses[i].setToFalse(p,false);
		}
	}
}