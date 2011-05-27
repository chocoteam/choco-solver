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

package solver.constraints.propagators.gary;

import java.util.LinkedList;

import gnu.trove.TIntArrayList;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.requests.GraphRequest;
import solver.requests.IRequest;

/**Propagator that ensures that the relation modeled by edges (undirected graph) is transitive
 * 
 * TODO Incrementalite
 * 
 * @author Jean-Guillaume Fages
 */
public class PropTransitivityUndirected<V extends UndirectedGraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private V g;
	private int n;
//	private IntProcedure arcEnforced;
	private IntProcedure arcRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTransitivityUndirected(V graph, IEnvironment environment, GraphConstraint constraint) {
		super((V[]) new UndirectedGraphVar[]{graph}, environment, constraint, PropagatorPriority.LINEAR, false);
		g = graph;
		n = graph.getEnvelopGraph().getNbNodes();
//		arcEnforced = new EnfArc(this);
		arcRemoved  = new RemArc(this);
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		
	}

	
	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		LinkedList<TIntArrayList> kerCC = ConnectivityFinder.findCCOf(g.getKernelGraph());
		for(TIntArrayList cc:kerCC){
			for(int i = 0; i<cc.size(); i++){
				for(int j = 0; j<cc.size(); j++){
					g.enforceArc(cc.get(i), cc.get(j), this);
				}
			}
		}
		
		if( request instanceof GraphRequest){
			GraphRequest gr = (GraphRequest) request;
//			if((mask & EventType.ENFORCEARC.mask) !=0){
//				IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
//				d.forEach(arcEnforced, gr.fromArcEnforcing(), gr.toArcEnforcing());
//			}
			if((mask & EventType.REMOVEARC.mask)!=0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(arcRemoved, gr.fromArcRemoval(), gr.toArcRemoval());
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask +  EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
	
	//***********************************************************************************
	// PROCEDURE
	//***********************************************************************************

//	private class EnfArc implements IntProcedure{
//		private GraphPropagator p;
//		
//		private EnfArc(GraphPropagator p){
//			this.p = p;
//		}
//		@Override
//		public void execute(int i) throws ContradictionException {
//			int from = i/n-1;
//			int to   = i%n;
//			if(from != to){
//				apply(from,to);
//				apply(to,from);
//			}
//		}
//
//		private void apply(int node, int mate) throws ContradictionException {
//			INeighbors nei = g.getEnvelopGraph().getNeighborsOf(node);
//			for(int i=nei.getFirstElement(); i>=0; i = nei.getNextElement()){
//				g.enforceArc(i, mate, p);
//			}
//		}
//	}
//	
	private class RemArc implements IntProcedure{
		private GraphPropagator p;
		
		private RemArc(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			if(from != to){
				apply(from,to);
				apply(to,from);
			}
		}

		private void apply(int node, int mate) throws ContradictionException {
			INeighbors nei = g.getEnvelopGraph().getNeighborsOf(node);
			for(int i=nei.getFirstElement(); i>=0; i = nei.getNextElement()){
				if(g.getKernelGraph().edgeExists(i, mate)){
					g.removeArc(node, i, p);
				}
			}
		}
	}
}
