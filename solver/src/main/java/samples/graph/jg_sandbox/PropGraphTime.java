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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public class PropGraphTime extends GraphPropagator<UndirectedGraphVar>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private UndirectedGraphVar g;
	private IntVar[] start,end;
	private IntProcedure enf_proc, rem_proc;
	private int[] task_start, task_end;
	private int n,firstTaskIndex;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropGraphTime(UndirectedGraphVar graph, int ftIdx,
						 IntVar[] st, IntVar[] en, int[] t_start, int[] t_end,
						 Constraint constraint, Solver solver) {
		super(new UndirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		start = st;
		end = en;
		firstTaskIndex = ftIdx;
		n = g.getEnvelopGraph().getNbNodes();
		task_start = t_start;
		task_end   = t_end;
		enf_proc = new ArcEnf();
		rem_proc = new NodeRem();
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************


	@Override
	public void propagate(int evtmask) throws ContradictionException {
		INeighbors nei;
		for(int i=0;i<firstTaskIndex;i++){
			if(!g.getEnvelopGraph().getActiveNodes().isActive(i)){
				nodeRem(i);
			}
			nei = g.getKernelGraph().getNeighborsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				adjustTimeBounds(i,j-firstTaskIndex);
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		eventRecorder.getDeltaMonitor(this, g).forEach(enf_proc, EventType.ENFORCEARC);
		if((mask & EventType.REMOVENODE.mask)!=0){
			eventRecorder.getDeltaMonitor(this, g).forEach(rem_proc, EventType.REMOVENODE);
		}
	}

	private void adjustTimeBounds(int employeeIdx, int taskIdx) throws ContradictionException {
		start[employeeIdx].updateUpperBound(task_start[taskIdx],this);
		end[employeeIdx].updateLowerBound(task_end[taskIdx],this);
	}

	private void nodeRem(int employeeIdx) throws ContradictionException {
		start[employeeIdx].instantiateTo(start[employeeIdx].getLB(),this);
		end[employeeIdx].instantiateTo(end[employeeIdx].getLB(),this);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCEARC.mask+EventType.REMOVENODE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class ArcEnf implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			int a = i/n-1;
			int b = i%n;
			if(a<firstTaskIndex){
				adjustTimeBounds(a,b-firstTaskIndex);
			}else if(b<firstTaskIndex){
				adjustTimeBounds(b,a-firstTaskIndex);
			}
		}
	}
	
	private class NodeRem implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			nodeRem(i);
		}
	}
}