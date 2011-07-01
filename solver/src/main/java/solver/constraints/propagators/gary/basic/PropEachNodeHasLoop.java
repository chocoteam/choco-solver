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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.requests.GraphRequest;
import solver.requests.IRequest;

/**Propagator that ensures that each node of the final graph has a loop
 * 
 * @author Jean-Guillaume Fages
 */
public class PropEachNodeHasLoop<V extends GraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IntProcedure enfNodeProc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropEachNodeHasLoop(V graph, Solver sol, Constraint<V, Propagator<V>> constraint) {
		super((V[]) new GraphVar[]{graph}, sol, constraint, PropagatorPriority.VERY_SLOW, false);
		g = graph;
		enfNodeProc = new NodeEnf(this);
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		IActiveNodes act = g.getKernelGraph().getActiveNodes();
		for (int node = act.nextValue(0); node>=0; node = act.nextValue(node+1)) {
			g.enforceArc(node, node, this);
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		GraphRequest gr = (GraphRequest) request;
		IntDelta d = (IntDelta) g.getDelta().getNodeEnforcingDelta();
		d.forEach(enfNodeProc, gr.fromNodeEnforcing(), gr.toNodeEnforcing());
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCENODE.mask;
	}

	@Override
	public ESat isEntailed() {
		IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
		for (int node = act.nextValue(0); node>=0; node = act.nextValue(node+1)) {
			if(!g.getEnvelopGraph().getNeighborsOf(node).contain(node)){
				return ESat.FALSE;
			}
		}
		if(g.getEnvelopOrder() != g.getKernelOrder()){
			return ESat.UNDEFINED;
		}
		return ESat.TRUE;
	}
	
	//***********************************************************************************
	// PROCEDURE
	//***********************************************************************************

	private class NodeEnf implements IntProcedure{
		private Propagator p;
		private NodeEnf(Propagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			g.enforceArc(i, i, p);
		}
	}
}
