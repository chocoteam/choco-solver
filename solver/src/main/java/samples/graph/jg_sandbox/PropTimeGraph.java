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
import choco.kernel.common.util.tools.ArrayUtils;
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

public class PropTimeGraph extends GraphPropagator<IntVar>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private UndirectedGraphVar g;
	private IntVar[] start,end;
	private int[] task_start, task_end;
	private int firstTaskIndex;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTimeGraph(UndirectedGraphVar graph, int ftIdx,
						 IntVar[] st, IntVar[] en, int[] t_start, int[] t_end,
						 Constraint constraint, Solver solver) {
		super(ArrayUtils.append(st,en), solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		start = st;
		end = en;
		firstTaskIndex = ftIdx;
		task_start = t_start;
		task_end   = t_end;
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************


	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<firstTaskIndex;i++){
			adjustGraph(i);
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(idxVarInProp>=firstTaskIndex){
			idxVarInProp -= firstTaskIndex;
		}
		adjustGraph(idxVarInProp);
	}

	private void adjustGraph(int employeeIdx) throws ContradictionException {
		INeighbors nei = g.getEnvelopGraph().getNeighborsOf(employeeIdx);
		for(int t=nei.getFirstElement();t>=0;t=nei.getNextElement()){
			if(task_start[t-firstTaskIndex]<start[employeeIdx].getLB()
			|| task_end[t-firstTaskIndex]>end[employeeIdx].getUB()){
				g.removeArc(employeeIdx,t,this);
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INSTANTIATE.mask+EventType.DECUPP.mask+EventType.INCLOW.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;
	}
}