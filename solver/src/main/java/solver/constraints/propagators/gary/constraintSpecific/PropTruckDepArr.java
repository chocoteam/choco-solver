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

package solver.constraints.propagators.gary.constraintSpecific;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * @author Jean-Guillaume Fages
 */
public class PropTruckDepArr<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g; 
	int nbTrucks;
	IntProcedure removeProc;
	IntProcedure enforceProc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTruckDepArr(
			DirectedGraphVar graph, int nbt,
			Solver sol,
			Constraint<V, Propagator<V>> constraint,
			PropagatorPriority priority, boolean reactOnPromotion) {
		super((V[]) new Variable[]{graph}, sol, constraint, priority, reactOnPromotion);
		g = graph;
		nbTrucks = nbt;
		removeProc = new RemProc(this);
		enforceProc = new EnfLoop(this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if (request instanceof GraphRequest) {
			GraphRequest gv = (GraphRequest) request;
			if ((mask & EventType.ENFORCENODE.mask) != 0){
				IntDelta d = (IntDelta) g.getDelta().getNodeEnforcingDelta();
				d.forEach(enforceProc, gv.fromNodeEnforcing(), gv.toNodeEnforcing());
			}
			if ((mask & EventType.REMOVENODE.mask) != 0){
				IntDelta d = (IntDelta) g.getDelta().getNodeRemovalDelta();
				d.forEach(removeProc, gv.fromNodeRemoval(), gv.toNodeRemoval());
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCENODE.mask+EventType.REMOVENODE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/**
	 * @author Jean-Guillaume Fages
	 */
	private static class RemProc implements IntProcedure {

		private final PropTruckDepArr p;

		public RemProc(PropTruckDepArr p) {
			this.p = p;
		}

		public void execute(int i) throws ContradictionException {
			if(i<2*p.nbTrucks){
				if(i/2==0){
					p.g.removeNode(i+1, p);
				}else{
					p.g.removeNode(i-1, p);
				}
			}
		}
	}

	/**
	 * @author Jean-Guillaume Fages
	 */
	private static class EnfLoop implements IntProcedure {

		private final PropTruckDepArr p;

		public EnfLoop(PropTruckDepArr p) {
			this.p = p;
		}

		public void execute(int i) throws ContradictionException {
			if(i<2*p.nbTrucks){
				if(i/2==0){
					p.g.enforceNode(i+1, p);
				}else{
					p.g.enforceNode(i-1, p);
				}
			}
		}
	}
}
