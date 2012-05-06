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
import choco.kernel.common.util.procedure.PairProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Propagator that ensures that a node has at least N Successors
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtLeastNSuccessors extends GraphPropagator<DirectedGraphVar>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private DirectedGraphVar g;
	private int[] n_succs;
	private IntProcedure enf_nodes_proc;
	private PairProcedure rem_arc_proc;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropAtLeastNSuccessors(DirectedGraphVar graph, int[] nbSuccs, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		n_succs = nbSuccs;
		int n = g.getEnvelopGraph().getNbNodes();
		enf_nodes_proc = new NodeEnf();
		rem_arc_proc = new ArcRem();
	}

	public PropAtLeastNSuccessors(DirectedGraphVar graph, int nbSuccs, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		int n = g.getEnvelopGraph().getNbNodes();
		n_succs = new int[n];
		for(int i=0;i<n;i++){
			n_succs[i] = nbSuccs;
		}
		enf_nodes_proc = new NodeEnf();
		rem_arc_proc = new ArcRem();
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************


	@Override
	public void propagate(int evtmask) throws ContradictionException {
		IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
		IActiveNodes kerAct = g.getKernelGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			checkNode(node);
			if(kerAct.isActive(node)){
				enforceNode(node);
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if((mask & EventType.REMOVEARC.mask) != 0){
			GraphDeltaMonitor gdm = (GraphDeltaMonitor) eventRecorder.getDeltaMonitor(this,g);
			gdm.forEachArc(rem_arc_proc, EventType.REMOVEARC);
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
			if(g.getEnvelopGraph().getSuccessorsOf(node).neighborhoodSize()<n_succs[node]){
				return ESat.FALSE;
			}
		}
		act = g.getEnvelopGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			if(g.getKernelGraph().getSuccessorsOf(node).neighborhoodSize()<n_succs[node]){
				return ESat.UNDEFINED;
			}
		}
		return ESat.TRUE;
	}

	//***********************************************************************************
	// FILTERING
	//***********************************************************************************

	/** When a node is enforced,
	 * if it has less than N successors then a contradiction should be raised
	 * if it has N successors in the envelop then they must figure in the kernel */
	private void enforceNode(int i) throws ContradictionException {
		INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
		int size = nei.neighborhoodSize();
		if(size<n_succs[i]){
			contradiction(g,"");
		}else if(size==n_succs[i] && g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize()<size){
			for(int s = nei.getFirstElement(); s >= 0; s = nei.getNextElement()){
				g.enforceArc(i, s, this);
			}
		}
	}

	/** When a node has less than N successors then it must be removed,
	 *  If it has N successors and is in the kernel then its incident edges
	 *  should be enforced */
	private void checkNode(int i) throws ContradictionException {
		INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
		int size = nei.neighborhoodSize();
		if(size<n_succs[i]){
			g.removeNode(i, this);
		}else if (size==n_succs[i] && g.getKernelGraph().getActiveNodes().isActive(i)
				&& g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize()<size){
			for(int s = nei.getFirstElement(); s>=0; s = nei.getNextElement()){
				g.enforceArc(i, s, this);
			}
		}
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class NodeEnf implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			enforceNode(i);
		}
	}

	private class ArcRem implements PairProcedure{
		@Override
		public void execute(int i, int j) throws ContradictionException {
			checkNode(i);
		}
	}
}