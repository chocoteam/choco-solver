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

package solver.constraints.propagators.gary.degree;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Propagator that ensures that a node has at most N successors
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtMostNSuccessors extends GraphPropagator<DirectedGraphVar>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private DirectedGraphVar g;
	private IntProcedure enf_proc;
	private int[] n_Succs;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	@Deprecated
	public PropAtMostNSuccessors(DirectedGraphVar graph, Solver solver, Constraint constraint, int nbSuccs) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		int n = g.getEnvelopGraph().getNbNodes();
		n_Succs = new int[n];
		for(int i=0;i<n;i++){
			n_Succs[i] = nbSuccs;
		}
		enf_proc = new ArcEnf(n);
	}

	public PropAtMostNSuccessors(DirectedGraphVar graph, int nbSuccs, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		int n = g.getEnvelopGraph().getNbNodes();
		n_Succs = new int[n];
		for(int i=0;i<n;i++){
			n_Succs[i] = nbSuccs;
		}
		enf_proc = new ArcEnf(n);
	}

	public PropAtMostNSuccessors(DirectedGraphVar graph, int[] nbSuccs, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		int n = g.getEnvelopGraph().getNbNodes();
		n_Succs = nbSuccs;
		enf_proc = new ArcEnf(n);
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************


    @Override
    public void propagate(int evtmask) throws ContradictionException {
		IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			checkNode(node);
		}
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		eventRecorder.getDeltaMonitor(this, g).forEach(enf_proc, EventType.ENFORCEARC);
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
			if(g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize()>n_Succs[node]){
				return ESat.FALSE;
			}
		}
		act = g.getEnvelopGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			if(g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()>n_Succs[node]){
				return ESat.UNDEFINED;
			}
		}
		return ESat.TRUE;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/** When a node has more than N successors then it must be removed,
	 *  If it has N successors in the kernel then other incident edges
	 *  should be removed */
	private void checkNode(int i) throws ContradictionException {
		INeighbors ker = g.getKernelGraph().getSuccessorsOf(i);
		INeighbors env = g.getEnvelopGraph().getSuccessorsOf(i);
		int size = ker.neighborhoodSize();
		if(size>n_Succs[i]){
			g.removeNode(i, this);
		}else if (size==n_Succs[i] && env.neighborhoodSize()>size){
			for(int next = env.getFirstElement(); next>=0; next = env.getNextElement()){
				if(!ker.contain(next)){
					g.removeArc(i, next, this);
				}
			}
		}
	}
	
	private class ArcEnf implements IntProcedure{
		private int n;
		ArcEnf(int n){
			this.n = n;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			checkNode(i/n-1);
		}
	}
}
