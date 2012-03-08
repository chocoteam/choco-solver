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

package solver.constraints.propagators.gary.directed;

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
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;


/**
 * @author Jean-Guillaume Fages
 * 
 * Ensures that each node in the kernel has exactly NPreds proper predecessors (excluding loops)
 * TODO parameter modification events
 * @param <V>
 */
public class PropNProperPreds<V extends DirectedGraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar nPreds;
	RemProc rem;
	EnfProc enf;
	int n;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNProperPreds(
			V graph,
			Solver solver,
			Constraint<V, Propagator<V>> constraint, IntVar nbPreds) {
		super((V[]) new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.UNARY);
		g = graph;
		nPreds = nbPreds;
		rem = new RemProc(this);
		enf = new EnfProc(this);
		n = g.getEnvelopGraph().getNbNodes();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int np=0;
		for(int i=0; i<n ; i++){
			INeighbors preds = g.getEnvelopGraph().getPredecessorsOf(i);
			np = preds.neighborhoodSize();
			if(g.getEnvelopGraph().arcExists(i, i)){
				np--;
			}
			if(np==nPreds.getLB()){
				for(int j=preds.getFirstElement(); j>=0; j=preds.getNextElement()){
					if(i!=j){
						g.enforceArc(j,i, this);
					}
				}
			}
			if(np<nPreds.getLB()){
				this.contradiction(g, "not enough proper predecessor");
			}
		}
		//TODO
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		Variable var = vars[idxVarInProp];
        if ((var.getTypeAndKind() & Variable.GRAPH)!=0) {
			if ((mask & EventType.REMOVEARC.mask)!=0){
                eventRecorder.getDeltaMonitor(this, g).forEach(rem, EventType.REMOVEARC);
			}
			if ((mask & EventType.ENFORCEARC.mask) != 0){
                eventRecorder.getDeltaMonitor(this, g).forEach(enf, EventType.ENFORCEARC);
			}
		}else{
			//TODO
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		IActiveNodes ker = g.getKernelGraph().getActiveNodes();
		for(int i=ker.getFirstElement(); i>=0; i = ker.getNextElement()){
			if(g.getEnvelopGraph().getPredecessorsOf(i).neighborhoodSize()<nPreds.getLB() ||
			   g.getKernelGraph().getPredecessorsOf(i).neighborhoodSize()>nPreds.getUB()){
				return ESat.FALSE;
			}
		}
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/** Enable to add arcs to the kernel when only NSUCCS arcs remain in the envelop */
	private static class RemProc implements IntProcedure {

		private final PropNProperPreds p;

		public RemProc(PropNProperPreds p) {
			this.p = p;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			int np = p.nPreds.getLB();
			if (i>=p.n){
				int to   = i%p.n;
				INeighbors prds = p.g.getEnvelopGraph().getPredecessorsOf(to);
				int envSize = prds.neighborhoodSize();
				if(p.g.getEnvelopGraph().arcExists(to, to)){
					envSize --;
				}
				if(envSize<np){
					p.contradiction(p.g, "no proper predecessor");
				}
				if(envSize==np){
					int kerSize = p.g.getKernelGraph().getPredecessorsOf(to).neighborhoodSize();
					if(p.g.getKernelGraph().arcExists(to, to)){
						kerSize--;
					}
					if(kerSize > np){
						throw new UnsupportedOperationException("error ");
					}
					if(kerSize != np){
						for(int j=prds.getFirstElement(); j>=0; j=prds.getNextElement()){
							if(j!=to) {
								p.g.enforceArc(j,to, p);
							}
						}
					}
				}
			}
		}
	}

	/** Enable to remove useless outgoing arcs of a node when the kernel contains NSUCCS outgoing arcs */
	private static class EnfProc implements IntProcedure {

		private final PropNProperPreds p;

		public EnfProc(PropNProperPreds p) {
			this.p = p;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			int np = p.nPreds.getUB();
			if (i>p.n){
				int from = i/p.n-1;
				int to   = i%p.n;
				int kerSize = p.g.getKernelGraph().getPredecessorsOf(to).neighborhoodSize();
				if(p.g.getKernelGraph().arcExists(to, to)){
					kerSize--;
				}
				if(kerSize>np){
					p.contradiction(p.g, "too many predecessors");
				}
				INeighbors prds = p.g.getEnvelopGraph().getPredecessorsOf(to);
				int envSize = prds.neighborhoodSize();
				if(p.g.getEnvelopGraph().arcExists(to, to)){
					envSize--;
				}
				if(envSize>np && kerSize==np){
					for(from=prds.getFirstElement(); from>=0; from=prds.getNextElement()){
						if (from!=to && !p.g.getKernelGraph().arcExists(from, to)){
							p.g.removeArc(from,to, p);
						}
					}
				}
			}
		}
	}

	//	private void check() throws ContradictionException {
	//		int n = g.getEnvelopGraph().getNbNodes();
	//		int k;
	//		INeighbors nei;
	//		LinkedList<Integer> arcs = new LinkedList<Integer>();
	//		for (int i=g.getEnvelopGraph().getActiveNodes().nextValue(0);i>=0;i=g.getEnvelopGraph().getActiveNodes().nextValue(i+1)){
	//			k = g.getKernelGraph().getPredecessorsOf(i).neighborhoodSize();
	//			if(k>nPreds){
	//				this.contradiction(g, "more than one predecessors");
	//			}
	//			if(g.getEnvelopGraph().getPredecessorsOf(i).neighborhoodSize()<nPreds){
	//				this.contradiction(g, "not enough potential predecessors");
	//			}
	//			if(k==nPreds && g.getEnvelopGraph().getPredecessorsOf(i).neighborhoodSize() != k){
	//				nei = g.getEnvelopGraph().getPredecessorsOf(i);
	//				for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
	//					if (!g.getKernelGraph().arcExists(j,i)){
	//						arcs.addFirst((j+1)*n+i);
	//					}
	//				}
	//			}
	//		}
	//		for(int next:arcs){
	//			g.removeArc(next/n-1, next%n, this);
	//		}
	//	}
}
