
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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * @PropAnn(tested = {BENCHMARK,CORRECTION})
 * Each node but "but" has only one successor
 * */
public class PropOneSuccBut<V extends DirectedGraphVar> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int but,n;
	private IntProcedure arcEnforced;
	private IntProcedure arcRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** All nodes of the graph but "but" have only one successor
	 * @param graph
	 * @param but the node which is not concerned by the constraint
	 * @param constraint
	 * @param solver
	 * */
	public PropOneSuccBut(DirectedGraphVar graph, int but, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.but = but;
		arcEnforced = new EnfArc(this);
		arcRemoved  = new RemArc(this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		INeighbors succs;
		int next;
		for(int i=0;i<n;i++){
			if(i!=but){
				succs = g.getEnvelopGraph().getSuccessorsOf(i);
				next = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
				if (succs.neighborhoodSize()==0){
					this.contradiction(g,i+" has no successor");
				}
				else if (succs.neighborhoodSize()==1){
					g.enforceArc(i,succs.getFirstElement(),this);
				}
				else if(next!=-1){
					if(g.getKernelGraph().getSuccessorsOf(i).getNextElement()!=-1){
						contradiction(g,"too many successors");
					}
					for(int j=succs.getFirstElement();j>=0;j=succs.getNextElement()){
						if(j!=next){
							g.removeArc(i,j,this);
						}
					}
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(ALWAYS_COARSE){
			propagate(0);return;
		}
//		System.out.println("propag");
//		GraphDeltaMonitor gdm = (GraphDeltaMonitor) eventRecorder.getDeltaMonitor(g);
//		System.out.println("delta : "+gdm.fromArcEnforcing()+" -> "+gdm.toArcEnforcing()+"  /// "+g.getDelta().getArcEnforcingDelta().size());
//		try{
//			System.out.println("SUCCESSIRS");
//			for(int i=0;i<n;i++){
//				System.out.println(i+" : "+g.getEnvelopGraph().getSuccessorsOf(i));
//			}
		eventRecorder.getDeltaMonitor(g).forEach(arcEnforced, EventType.ENFORCEARC);
		eventRecorder.getDeltaMonitor(g).forEach(arcRemoved, EventType.REMOVEARC);
//			}
//		catch(Exception e){
//			e.printStackTrace();
//			throw new UnsupportedOperationException();
//		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		boolean done = true;
		for(int i=0;i<n;i++){
			if(i!=but){
				if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()<1 || g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize()>1){
					return ESat.FALSE;
				}
				if(g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize()!=g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()){
					done = false;
				}
			}
		}
		if(done){
			return ESat.TRUE;
		}
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
			int from = i/n-1;
//			System.out.println("ENFORCE "+from+" -> "+(i%n));
			if(from!=but){
				int to   = i%n;
				INeighbors succs = g.getEnvelopGraph().getSuccessorsOf(from);
				for(i=succs.getFirstElement(); i>=0; i = succs.getNextElement()){
					if(i!=to){
						g.removeArc(from,i,p);
					}
				}
			}
		}
	}

	private class RemArc implements IntProcedure{
		private GraphPropagator p;

		private RemArc(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
//			System.out.println("REMOVE "+from+" -> "+(i%n));
			if(from!=but){
				INeighbors succs = g.getEnvelopGraph().getSuccessorsOf(from);
				if (succs.neighborhoodSize()==0){
					p.contradiction(g,from+" has no successor");
				}
				if (succs.neighborhoodSize()==1){
					g.enforceArc(from,succs.getFirstElement(),p);
				}
			}
		}
	}
}
