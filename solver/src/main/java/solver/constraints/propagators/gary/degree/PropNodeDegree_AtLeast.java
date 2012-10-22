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
import choco.kernel.common.util.procedure.PairProcedure;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.variables.setDataStructures.ISet;

/**
 * Propagator that ensures that a node has at most N successors/predecessors/neighbors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNodeDegree_AtLeast extends Propagator<GraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
    GraphDeltaMonitor gdm;
	private PairProcedure enf_proc;
	private int[] degrees;
	private IncidentNodes target;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNodeDegree_AtLeast make(DirectedGraphVar graph, IncidentNodes setType, int degree, Constraint constraint, Solver solver){
		int n = g.getEnvelopGraph().getNbNodes();
		int[] degrees = new int[n];
		for(int i=0;i<n;i++){
			degrees[i] = degree;
		}
		return new PropNodeDegree_AtLeast(graph,setType,degrees,constraint,solver);
	}

	public PropNodeDegree_AtLeast(DirectedGraphVar graph, IncidentNodes setType, int[] degrees, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		target = setType;
		g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
		this.degrees = degrees;
		switch (target){
			case SUCCESSORS:enf_proc = new PairProcedure(){
				public void execute(int i, int j) throws ContradictionException {
					checkAtMost(i);
				}
			};break;
			case PREDECESSORS:enf_proc = new PairProcedure(){
				public void execute(int i, int j) throws ContradictionException {
					checkAtMost(j);
				}
			};break;
			case NEIGHBORS:enf_proc = new PairProcedure(){
				public void execute(int i, int j) throws ContradictionException {
					checkAtMost(i);
					checkAtMost(j);
				}
			};break;
			default:throw new UnsupportedOperationException();
		}
	}

	public PropNodeDegree_AtLeast(UndirectedGraphVar graph, int[] degrees, Constraint constraint, Solver solver) {
		super(new UndirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		target = IncidentNodes.NEIGHBORS;
		g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
		this.degrees = degrees;
		enf_proc = new PairProcedure(){
			public void execute(int i, int j) throws ContradictionException {
				checkAtMost(i);
				checkAtMost(j);
			}
		};
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		ISet act = g.getEnvelopGraph().getActiveNodes();
		for (int node = act.getFirstElement(); node>=0; node = act.getNextElement()) {
			checkAtMost(node);
		}
		gdm.unfreeze();
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		gdm.freeze();
		gdm.forEachArc(enf_proc, EventType.ENFORCEARC);
        gdm.unfreeze();
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
		for(int i=0;i<g.getEnvelopGraph().getNbNodes();i++){
			if(target.getSet(g.getEnvelopGraph(),i).getSize()>degrees[i]){
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

	/** When a node has more than N successors/predecessors/neighbors then it must be removed,
	 *  If it has N successors/predecessors/neighbors in the kernel then other incident edges
	 *  should be removed */
	private void checkAtMost(int i) throws ContradictionException {
		ISet ker = target.getSet(g.getKernelGraph(), i);
		ISet env = target.getSet(g.getEnvelopGraph(),i);
		int size = ker.getSize();
		if(size>degrees[i]){
			g.removeNode(i, this);
		}else if (size==degrees[i] && env.getSize()>size){
			for(int other = env.getFirstElement(); other>=0; other = env.getNextElement()){
				if(!ker.contain(other)){
					g.removeArc(i, other, this);
					target.remove(g,i,other,this);
				}
			}
		}
	}

	public enum IncidentNodes{
		PREDECESSORS(){
			protected ISet getSet(IGraph graph, int i){
				return graph.getPredecessorsOf(i);
			}
			protected void remove(GraphVar gv, int from, int to, ICause cause) throws ContradictionException {
				gv.removeArc(to,from,cause);
			}
		},
		SUCCESSORS(){
			protected ISet getSet(IGraph graph, int i){
				return graph.getSuccessorsOf(i);
			}
			protected void remove(GraphVar gv, int from, int to, ICause cause) throws ContradictionException {
				gv.removeArc(from,to,cause);
			}
		},
		NEIGHBORS(){
			protected ISet getSet(IGraph graph, int i){
				return graph.getNeighborsOf(i);
			}
			protected void remove(GraphVar gv, int from, int to, ICause cause) throws ContradictionException {
				gv.removeArc(from,to,cause);
				gv.removeArc(to,from,cause);
			}
		};
		protected abstract ISet getSet(IGraph graph, int i);
		protected abstract void remove(GraphVar gv, int from, int to, ICause cause) throws ContradictionException;
	}
}
