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

package solver.constraints.propagators.gary.basic;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.procedure.PairProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;

/**
 * Propagator that ensures that each node of the given subset of nodes has a loop
 *
 * @author Jean-Guillaume Fages
 */
public class PropEachNodeHasLoop extends Propagator<GraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IntProcedure enfNode;
	private PairProcedure remArc;
	private INeighbors concernedNodes;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropEachNodeHasLoop(GraphVar graph, INeighbors concernedNodes, Solver sol, Constraint constraint) {
		super(new GraphVar[]{graph}, sol, constraint, PropagatorPriority.UNARY);
		this.g = graph;
		this.enfNode = new NodeEnf(this);
		this.remArc = new ArcRem(this);
		this.concernedNodes = concernedNodes;
	}

	public PropEachNodeHasLoop(GraphVar graph, Solver sol, Constraint constraint) {
		this(graph, graph.getEnvelopGraph().getActiveNodes(), sol, constraint);
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
		for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
			if (concernedNodes.contain(i)) {
				if(g.getEnvelopGraph().arcExists(i,i)){
					if(g.getKernelGraph().getActiveNodes().isActive(i)){
						g.enforceArc(i,i,this);
					}
				}else{
					g.removeNode(i,this);
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if ((mask & EventType.REMOVEARC.mask) != 0) {
			GraphDeltaMonitor gdm = (GraphDeltaMonitor) eventRecorder.getDeltaMonitor(this,g);
			gdm.forEachArc(remArc, EventType.REMOVEARC);
		}
		if ((mask & EventType.ENFORCENODE.mask) != 0) {
			eventRecorder.getDeltaMonitor(this, g).forEach(enfNode, EventType.ENFORCENODE);
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCENODE.mask + EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		IActiveNodes ker = g.getKernelGraph().getActiveNodes();
		for (int i = ker.getFirstElement(); i >= 0; i = ker.getNextElement()) {
			if (concernedNodes.contain(i) && !g.getKernelGraph().getNeighborsOf(i).contain(i)) {
				return ESat.FALSE;
			}
		}
		if (g.getEnvelopOrder() != g.getKernelOrder()) {
			return ESat.UNDEFINED;
		}
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURE
	//***********************************************************************************

	private class NodeEnf implements IntProcedure {
		private PropEachNodeHasLoop p;
		private NodeEnf(PropEachNodeHasLoop p) {
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			if (p.concernedNodes.contain(i)) {
				g.enforceArc(i, i, p);
			}
		}
	}

	private class ArcRem implements PairProcedure {
		private PropEachNodeHasLoop p;
		private ArcRem(PropEachNodeHasLoop p) {
			this.p = p;
		}
		@Override
		public void execute(int from, int to) throws ContradictionException {
			if (from == to && concernedNodes.contain(to)) {
				g.removeNode(from, p);
			}
		}
	}
}
