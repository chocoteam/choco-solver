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
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * @author Jean-Guillaume Fages
 */
public class PropTruckDepArr<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g; 
	int nbMaxTrucks;
	IntVar nbtrucks;
	IntProcedure removeProc;
	IntProcedure enforceProc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTruckDepArr(DirectedGraphVar graph, IntVar nbt,
			Solver sol,	Constraint<V, Propagator<V>> constraint) {
		super((V[]) new Variable[]{graph,nbt}, sol, constraint, PropagatorPriority.UNARY);
		g = graph;
		nbtrucks = nbt;
		nbMaxTrucks = nbt.getUB();
		removeProc = new RemNode(this);
		enforceProc = new EnfNode(this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		for(int i=2*nbMaxTrucks;i<g.getEnvelopGraph().getNbNodes();i++){
			g.enforceNode(i, this, false);
		}
		int min = 2*nbtrucks.getLB();
		for(int i=0;i<min;i++){
			g.enforceNode(i, this, false);
		}
	}

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
		}else{
			if ((mask & EventType.DECUPP.mask) != 0){
				int ub = 2*nbtrucks.getUB();
				for(int i=ub;i<nbMaxTrucks;i++){
					g.removeNode(i, this, false);
				}
			}
			if ((mask & EventType.INCLOW.mask) != 0){
				int lb = 2*nbtrucks.getLB();
				for(int i=0;i<lb;i++){
					g.enforceNode(i, this, false);
				}
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCENODE.mask+EventType.REMOVENODE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
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
	private static class RemNode implements IntProcedure {

		private final PropTruckDepArr p;

		public RemNode(PropTruckDepArr p) {
			this.p = p;
		}

		public void execute(int i) throws ContradictionException {
			if(i<2*p.nbMaxTrucks){
				int j = i+1;
				if(i%2==0){
					p.g.removeNode(i+1, p, false);
					j++;
				}else{
					p.g.removeNode(i-1, p, false);
				}
				for(;j<p.nbMaxTrucks;j++){
					p.g.removeNode(j, p, false);
				}
			}
		}
	}

	/**
	 * @author Jean-Guillaume Fages
	 */
	private static class EnfNode implements IntProcedure {

		private final PropTruckDepArr p;

		public EnfNode(PropTruckDepArr p) {
			this.p = p;
		}

		public void execute(int i) throws ContradictionException {
			if(i<2*p.nbMaxTrucks){
				int j = i-1;
				if(i%2==0){
					p.g.enforceNode(i+1, p, false);
				}else{
					p.g.enforceNode(i-1, p, false);
					j--;
				}
				for(;j>=0;j--){
					p.g.enforceNode(j, p, false);
				}
			}
		}
	}
}
