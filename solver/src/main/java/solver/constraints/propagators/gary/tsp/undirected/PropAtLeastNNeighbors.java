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

package solver.constraints.propagators.gary.tsp.undirected;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * @PropAnn(tested = {CORRECTION,CONSISTENCY})
 * Propagator that ensures that a node has at least N neighbors
 * <p/>
 * BEWARE : the case where N=1 is useless because it is ensured by default
 *
 * @param <V>
 * @author Jean-Guillaume Fages
 */
public class PropAtLeastNNeighbors<V extends UndirectedGraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private UndirectedGraphVar g;
	private IntProcedure rem_proc;
	private IntProcedure enf_nodes_proc;
	private int[] n_neighbors;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAtLeastNNeighbors(V graph, int[] nNeigh, Solver solver, Constraint<V, Propagator<V>> constraint) {
		super((V[]) new UndirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		n_neighbors = nNeigh;
		int n = g.getEnvelopGraph().getNbNodes();
		enf_nodes_proc = new NodeEnf(this);
		rem_proc = new ArcRem(this, n);
	}
	public PropAtLeastNNeighbors(V graph, int nNeigh, Solver solver, Constraint<V, Propagator<V>> constraint) {
		this(graph,null,solver,constraint);
		int n = g.getEnvelopGraph().getNbNodes();
		n_neighbors = new int[n];
		for(int i=n-1;i>=0;i--){
			n_neighbors[i] = nNeigh;
		}
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************


    @Override
    public void propagate(int evtmask) throws ContradictionException {
		IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
		int next;
		INeighbors nei;
		int nbNei;
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			nbNei = this.n_neighbors[node];
			if(g.getEnvelopGraph().getNeighborsOf(node).neighborhoodSize()<nbNei){
				g.removeNode(node, this);
			}else if (g.getKernelGraph().getActiveNodes().isActive(node) 
					&& g.getEnvelopGraph().getNeighborsOf(node).neighborhoodSize()==nbNei
					&& g.getKernelGraph().getNeighborsOf(node).neighborhoodSize()<nbNei){
				nei = g.getEnvelopGraph().getNeighborsOf(node);
				for(next = nei.getFirstElement(); next >= 0; next = nei.getNextElement()){
					g.enforceArc(node, next, this);
				}
			}
		}
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if((mask & EventType.REMOVEARC.mask) != 0){
            eventRecorder.getDeltaMonitor(this, g).forEach(rem_proc, EventType.REMOVEARC);
		}
		if((mask & EventType.ENFORCENODE.mask) != 0){
            eventRecorder.getDeltaMonitor(this, g).forEach(enf_nodes_proc, EventType.ENFORCENODE);
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
		IActiveNodes act = g.getKernelGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			if(g.getEnvelopGraph().getNeighborsOf(node).neighborhoodSize()<n_neighbors[node]){
				return ESat.FALSE;
			}
		}
		if(!g.instantiated()){
			return ESat.UNDEFINED;
		}
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/** When a node is enforced, if it has N neighbors in the envelop then they must figure in the kernel */
	private class NodeEnf implements IntProcedure{

		private Propagator p;

		NodeEnf(Propagator p){
			this.p = p;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			if(g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize()<n_neighbors[i]){
				contradiction(g,"");
			}
			if(g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize()==n_neighbors[i]){
				INeighbors nei = g.getEnvelopGraph().getNeighborsOf(i);
				for(int next = nei.getFirstElement(); next >= 0; next = nei.getNextElement()){
					g.enforceArc(i, next, p);
				}
			}
		}
	}
	/** When has less than N neighbors then it must be removed,
	 *  If it has N neighbors and is in the kernel then its incident edges
	 *  should be enforced */
	private class ArcRem implements IntProcedure{

		private Propagator p;
		private int n;

		ArcRem(Propagator p, int n){
			this.p = p;
			this.n = n;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			if (i>=n){
				prune(i/n-1);
				prune(i%n);
			}else{
				throw new UnsupportedOperationException();
			}
		}
		private void prune(int from) throws ContradictionException{
			if(g.getEnvelopGraph().getNeighborsOf(from).neighborhoodSize()<n_neighbors[from]){
				g.removeNode(from, p);
			}else if (g.getKernelGraph().getActiveNodes().isActive(from) &&
					g.getEnvelopGraph().getNeighborsOf(from).neighborhoodSize()==n_neighbors[from]){
				INeighbors nei = g.getEnvelopGraph().getNeighborsOf(from);
				for(int next = nei.getFirstElement(); next>=0; next = nei.getNextElement()){
					g.enforceArc(from, next, p);
				}
			}
		}
	}
}
