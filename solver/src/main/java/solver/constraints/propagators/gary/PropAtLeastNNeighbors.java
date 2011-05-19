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

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.variables.graph.graphStructure.iterators.ActiveNodesIterator;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.requests.GraphRequest;
import solver.requests.IRequest;

/**Propagator that ensures that a node has at least N neighbors
 * 
 * BEWARE : the case where N=1 may should be automatic? (Who needs a node with no arcs?)
 *          the case where N>1 is not implemented yet
 * 
 * @author Jean-Guillaume Fages
 *
 * @param <V>
 */
public class PropAtLeastNNeighbors<V extends UndirectedGraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private UndirectedGraphVar g;
	private IntProcedure rem_proc;
	private int n_neighbors;
	private IntProcedure enf_nodes_proc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAtLeastNNeighbors(V graph, IEnvironment environment, Constraint<V, Propagator<V>> constraint, 
			PropagatorPriority priority, boolean reactOnPromotion, int nNeigh) {

		super((V[]) new UndirectedGraphVar[]{graph}, environment, constraint, priority, reactOnPromotion);
		g = graph;
		n_neighbors = nNeigh;
		final PropAtLeastNNeighbors<V> instance = this;
		final int n = g.getEnvelopGraph().getNbNodes();

		enf_nodes_proc = new IntProcedure() {
			@Override
			public void execute(int i) throws ContradictionException {
				if(g.getEnvelopGraph().getNeighborhoodSize(i)==1){
					g.enforceArc(i, g.getEnvelopGraph().getNeighborsOf(i).getFirstElement(), instance);
				}
			}
		};
		rem_proc = new IntProcedure() {
			@Override
			public void execute(int i) throws ContradictionException {
				if (i>=n){
					int from = i/n-1;
					int to   = i%n;
					prune(from);
					prune(to);
				}else{
					throw new UnsupportedOperationException();
				}
			}
			private void prune(int from) throws ContradictionException{
				if(g.getEnvelopGraph().getNeighborhoodSize(from)==0){
					g.removeNode(from, instance);
				}else if (g.getKernelGraph().getActiveNodes().isActive(from) &&
						g.getEnvelopGraph().getNeighborhoodSize(from)==1){
					g.enforceArc(from, g.getEnvelopGraph().getNeighborsOf(from).getFirstElement(), instance);
				}
			}
		};
		if (n_neighbors!=1){
			throw new UnsupportedOperationException("case not implemented yet... ");
		}
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		ActiveNodesIterator<IActiveNodes> niter = g.getEnvelopGraph().activeNodesIterator();
		int node,next;
		while (niter.hasNext()){
			node = niter.next();
			if(g.getEnvelopGraph().getNeighborhoodSize(node)<n_neighbors){
				g.removeNode(node, this);
			}else if (g.getKernelGraph().getActiveNodes().isActive(node) 
					&& g.getEnvelopGraph().getNeighborhoodSize(node)==n_neighbors 
					&& g.getKernelGraph().getNeighborhoodSize(node)<n_neighbors){
				INeighbors succs = g.getEnvelopGraph().getNeighborsOf(node);
				AbstractNeighborsIterator<INeighbors> iter = succs.iterator(); 
				while (iter.hasNext()){
					next = iter.next();
					g.enforceArc(node, next, this);
				}
			}
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if (request instanceof GraphRequest) {
			GraphRequest gv = (GraphRequest) request;
			if((mask & EventType.REMOVEARC.mask) != 0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(rem_proc, gv.fromArcRemoval(), gv.toArcRemoval());
			}
			if((mask & EventType.ENFORCENODE.mask) != 0){
				IntDelta d = (IntDelta) g.getDelta().getNodeEnforcingDelta();
				d.forEach(enf_nodes_proc, gv.fromNodeEnforcing(), gv.toNodeEnforcing());
			}
		}else{
			throw new UnsupportedOperationException("error ");
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
		ActiveNodesIterator<IActiveNodes> niter = g.getKernelGraph().activeNodesIterator();
		int node;
		while(niter.hasNext()){
			node = niter.next();
			if(g.getEnvelopGraph().getNeighborhoodSize(node)<n_neighbors){
				return ESat.FALSE;
			}
		}
		if(!g.instantiated()){
			return ESat.UNDEFINED;
		}
		return ESat.TRUE;
	}
}
