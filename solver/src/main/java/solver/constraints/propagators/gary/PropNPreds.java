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
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphStructure.iterators.AbstractNeighborsIterator;
import solver.requests.GraphRequest;
import solver.requests.IRequest;

/**
 * @author Jean-Guillaume Fages
 * 
 * Ensures that each node in the kernel has exactly NPreds predecessors
 *A VERIFIER
 * @param <V>
 */
public class PropNPreds<V extends DirectedGraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int nPreds;
	RemProc rem;
	EnfProc enf;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNPreds(
			V graph,
			IEnvironment environment,
			Constraint<V, Propagator<V>> constraint,
			PropagatorPriority priority, boolean reactOnPromotion, int nbPreds) {
		super((V[]) new DirectedGraphVar[]{graph}, environment, constraint, priority, reactOnPromotion);
		g = graph;
		nPreds = nbPreds;
		rem = new RemProc(this);
		enf = new EnfProc(this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {check();}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if (request instanceof GraphRequest) {
			GraphRequest gv = (GraphRequest) request;
			if ((mask & EventType.REMOVEARC.mask)!=0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(rem, gv.fromArcRemoval(), gv.toArcRemoval());
			}else if ((mask & EventType.ENFORCEARC.mask) != 0){
				IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
				d.forEach(enf, gv.fromArcEnforcing(), gv.toArcEnforcing());
			}
		}
	}

	@Override
	public int getPropagationConditions() {
		return EventType.REMOVEARC.mask+EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/** Enable to add arcs to the kernel when only NSUCCS arcs remain in the envelop */
	private static class RemProc implements IntProcedure {

		private final PropNPreds p;

		public RemProc(PropNPreds p) {
			this.p = p;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			int n = p.g.getEnvelopGraph().getNbNodes();
			if (i>=n){
				int to   = i%n;
				INeighbors prds = p.g.getEnvelopGraph().getPredecessorsOf(to);
				if(prds.neighborhoodSize()==p.nPreds && p.g.getKernelGraph().getPredecessorsOf(to).neighborhoodSize()!=p.nPreds){
					AbstractNeighborsIterator<INeighbors> iter = prds.iterator();
					while (iter.hasNext()){
						p.g.enforceArc(iter.next(),to, p);
					}
				}
			}
		}
	}

	/** Enable to remove useless outgoing arcs of a node when the kernel contains NSUCCS outgoing arcs */
	private static class EnfProc implements IntProcedure {

		private final PropNPreds p;

		public EnfProc(PropNPreds p) {
			this.p = p;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			int n = p.g.getEnvelopGraph().getNbNodes();
			if (i>n){
				int from = i/n-1;
				int to   = i%n;
				INeighbors prds = p.g.getEnvelopGraph().getPredecessorsOf(to);
				if(prds.neighborhoodSize()>p.nPreds && p.g.getKernelGraph().getPredecessorsOf(to).neighborhoodSize()==p.nPreds){
					AbstractNeighborsIterator<INeighbors> iter = prds.iterator();
					LinkedList<Integer> toRemove = new LinkedList<Integer>();
					while (iter.hasNext()){
						from = iter.next();
						if (!p.g.getKernelGraph().arcExists(from, to)){
							toRemove.addFirst(from);
						}
					}
					for(int next:toRemove){
						p.g.removeArc(next,to, p);
					}
				}
			}
		}
	}

	private void check() throws ContradictionException {
		int n = g.getEnvelopGraph().getNbNodes();
		int k;
		LinkedList<Integer> arcs = new LinkedList<Integer>();
		for (int i=g.getEnvelopGraph().getActiveNodes().nextValue(0);i>=0;i=g.getEnvelopGraph().getActiveNodes().nextValue(i+1)){
			k = g.getKernelGraph().getPredecessorsOf(i).neighborhoodSize();
			if(k>nPreds){
				ContradictionException.throwIt(this, g, "more than one predecessors");
			}
			if(g.getEnvelopGraph().getPredecessorsOf(i).neighborhoodSize()<nPreds){
				ContradictionException.throwIt(this, g, "not enough potential predecessors");
			}
			if(k==nPreds && g.getEnvelopGraph().getPredecessorsOf(i).neighborhoodSize() != k){
				AbstractNeighborsIterator<INeighbors> iter = g.getEnvelopGraph().getPredecessorsOf(i).iterator();
				int j;
				while(iter.hasNext()){
					j = iter.next();
					if (!g.getKernelGraph().arcExists(j,i)){
						arcs.addFirst((j+1)*n+i);
					}
				}
			}
		}
		for(int next:arcs){
			g.removeArc(next/n-1, next%n, this);
		}
	}
}
