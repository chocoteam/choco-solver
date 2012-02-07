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

package solver.constraints.propagators.gary.undirected;

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
 * Propagator that ensures that a node has at most N neighbors
 *
 * @param <V>
 * @author Jean-Guillaume Fages
 */
public class PropAtMostNNeighbors<V extends UndirectedGraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private UndirectedGraphVar g;
	private IntProcedure enf_proc;
	private int n_neighbors;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAtMostNNeighbors(V graph, Solver solver, Constraint<V, Propagator<V>> constraint, int nNeigh) {

		super((V[]) new UndirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		n_neighbors = nNeigh;
		final PropAtMostNNeighbors<V> instance = this;
		if (n_neighbors==1){
			enf_proc = new IntProcedure() {
				@Override
				public void execute(int i) throws ContradictionException {
					int n = g.getEnvelopGraph().getNbNodes();
					if (i>=n){
						int from = i/n-1;
						int to   = i%n;
						prune(from,to);
						prune(to,from);
					}else{
						throw new UnsupportedOperationException();
					}
				}
				private void prune(int from, int mate) throws ContradictionException{
					INeighbors succs = g.getEnvelopGraph().getNeighborsOf(from);
					for(int to = succs.getFirstElement(); to>=0; to = succs.getNextElement()){
						if (mate!=to){
							g.removeArc(from, to, instance);
						}
					}
				}
			};
		}else{
			enf_proc = new IntProcedure() {
				@Override
				public void execute(int i) throws ContradictionException {
					int n = g.getEnvelopGraph().getNbNodes();
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
					if(g.getKernelGraph().getNeighborsOf(from).neighborhoodSize()>n_neighbors){
						contradiction(g,"");
					}
					if(g.getKernelGraph().getNeighborsOf(from).neighborhoodSize()==n_neighbors &&
					   g.getEnvelopGraph().getNeighborsOf(from).neighborhoodSize()>n_neighbors){
						INeighbors succs = g.getEnvelopGraph().getNeighborsOf(from);
						for(int to = succs.getFirstElement(); to>=0; to = succs.getNextElement()){
							if (!g.getKernelGraph().edgeExists(from, to)){
								g.removeArc(from, to, instance);
							}
						}
					}
				}
			};
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
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			nei = g.getEnvelopGraph().getNeighborsOf(node); 
			if(g.getKernelGraph().getNeighborsOf(node).neighborhoodSize()==n_neighbors && nei.neighborhoodSize()>n_neighbors){
				for(next = nei.getFirstElement(); next>=0; next = nei.getNextElement()){
					if (!g.getKernelGraph().edgeExists(node, next)){
						g.removeArc(node, next, this);
					}
				}
			}
		}
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		eventRecorder.getDeltaMonitor(g).forEach(enf_proc, EventType.ENFORCEARC);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		IActiveNodes act = g.getKernelGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			if(g.getKernelGraph().getNeighborsOf(node).neighborhoodSize()>n_neighbors){
				return ESat.FALSE;
			}
		}
		if(g.instantiated()){
			return ESat.TRUE;
		}return ESat.UNDEFINED;
	}
}
