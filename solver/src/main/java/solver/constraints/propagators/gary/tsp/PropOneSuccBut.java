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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.delta.IntDelta;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Each node but "but" has only one successor
 * */
public class PropOneSuccBut<V extends DirectedGraphVar> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int but,n;
	private IntProcedure arcEnforced;
	private IntProcedure arcRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropOneSuccBut(DirectedGraphVar graph, int but, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY, false);
		g = graph;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.but = but;
		arcEnforced = new EnfArc(this);
		arcRemoved  = new RemArc(this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		INeighbors succs;
		for(int i=0;i<n;i++){
			if(i!=but){
				succs = g.getEnvelopGraph().getSuccessorsOf(i);
				if (succs.neighborhoodSize()==0){
					this.contradiction(g,i+" has no successor");
				}
				if (succs.neighborhoodSize()==1){
					g.enforceArc(i,succs.getFirstElement(),this,false);
				}
			}
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if( request instanceof GraphRequest){
			GraphRequest gr = (GraphRequest) request;
			if((mask & EventType.ENFORCEARC.mask) !=0){
				IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
				d.forEach(arcEnforced, gr.fromArcEnforcing(), gr.toArcEnforcing());
			}
			if((mask & EventType.REMOVEARC.mask)!=0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(arcRemoved, gr.fromArcRemoval(), gr.toArcRemoval());
			}
		}else{
			throw new UnsupportedOperationException("error ");
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class EnfArc implements IntProcedure {
		private GraphPropagator p;

		private EnfArc(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			if(from!=but){
				int to   = i%n;
				INeighbors succs = g.getEnvelopGraph().getSuccessorsOf(from);
				for(i=succs.getFirstElement(); i>=0; i = succs.getNextElement()){
					if(i!=to)g.removeArc(from,i,p,false);
				}
			}
		}
	}

	private class RemArc implements IntProcedure{
		private GraphPropagator p;

		private RemArc(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			if(from!=but){
				INeighbors succs = g.getEnvelopGraph().getSuccessorsOf(from);
				if (succs.neighborhoodSize()==0){
					p.contradiction(g,from+" has no successor");
				}
				if (succs.neighborhoodSize()==1){
					g.enforceArc(from,succs.getFirstElement(),p,false);
				}
			}
		}
	}
}
