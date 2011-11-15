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
import choco.kernel.memory.IStateInt;
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

/** Simple nocircuit contraint (from NoSubtour of Pesant or noCycle of Caseaux/Laburthe)
 * */public class PropPathNoCycle<V extends DirectedGraphVar> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	private IntProcedure arcEnforced;
	private IStateInt[] origin,end;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropPathNoCycle(DirectedGraphVar graph, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR, false);
		g = graph;
		this.n = g.getEnvelopGraph().getNbNodes();
		arcEnforced = new EnfArc(this);
		origin = new IStateInt[n];
		end = new IStateInt[n];
		for(int i=0;i<n;i++){
			origin[i] = environment.makeInt(i);
			end[i] = environment.makeInt(i);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		// TODO
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		GraphRequest gr = (GraphRequest) request;
		if((mask & EventType.ENFORCEARC.mask) !=0){
			IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
			d.forEach(arcEnforced, gr.fromArcEnforcing(), gr.toArcEnforcing());
		}
		// TODO propagateur specifique ou inclure dans 1 succ 1 pred
		checkPattern();
	}

	private void checkPattern() throws ContradictionException {
		INeighbors suc;
		int j,k;
		for(int i=0; i<n;i++){
			suc = g.getEnvelopGraph().getSuccessorsOf(i);
			if(suc.neighborhoodSize()==2){
				j = suc.getFirstElement();
				k = suc.getNextElement() ;
				g.removeArc(j,k,this,false);
				g.removeArc(k,j,this,false);
			}
			suc = g.getEnvelopGraph().getPredecessorsOf(i);
			if(suc.neighborhoodSize()==2){
				j = suc.getFirstElement();
				k = suc.getNextElement() ;
				g.removeArc(j,k,this,false);
				g.removeArc(k,j,this,false);
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCEARC.mask;
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
			int to = i%n;
			int from = i/n-1;
			int last = end[to].get();
			int start = origin[from].get();
			g.removeArc(last,start,p,false);
			origin[last].set(start);
			end[start].set(last);
		}
	}
}
