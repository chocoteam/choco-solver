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
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public class PropAtLeastOneGuy extends GraphPropagator<UndirectedGraphVar>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private UndirectedGraphVar g;
	private IntProcedure rem_proc;
	private int n,firstTaskIndex;
	private IntProcedure enf_nodes_proc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAtLeastOneGuy(UndirectedGraphVar graph, int firstTaskIndex, Constraint constraint, Solver solver) {
		super(new UndirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		this.firstTaskIndex = firstTaskIndex;
		n = g.getEnvelopGraph().getNbNodes();
		enf_nodes_proc = new NodeEnf();
		rem_proc = new ArcRem();
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************


	@Override
	public void propagate(int evtmask) throws ContradictionException {
		IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			checkNode(node,g.getKernelGraph().getActiveNodes().isActive(node));
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		eventRecorder.getDeltaMonitor(this, g).forEach(rem_proc, EventType.REMOVEARC);
		eventRecorder.getDeltaMonitor(this, g).forEach(enf_nodes_proc, EventType.ENFORCENODE);
	}

	private void checkNode(int i, boolean enforced) throws ContradictionException {
		if(i>=firstTaskIndex){
			INeighbors nei = g.getEnvelopGraph().getNeighborsOf(i);
			int guy = -1;
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j<firstTaskIndex){
					if(guy==-1){
						guy = j;
					}else{
						return;
					}
				}
			}
			if(enforced){
				if(guy==-1){
					contradiction(g,"");
				}else{
					g.enforceArc(i,guy,this);
				}
			}else if(guy==-1){
				g.removeNode(i,this);
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCENODE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class NodeEnf implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			checkNode(i, true);
		}
	}
	private class ArcRem implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			checkNode(i/n-1,g.getKernelGraph().getActiveNodes().isActive(i/n-1));
			checkNode(i%n,g.getKernelGraph().getActiveNodes().isActive(i%n));
		}
	}
}