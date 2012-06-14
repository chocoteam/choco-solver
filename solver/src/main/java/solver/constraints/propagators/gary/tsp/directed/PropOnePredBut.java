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

package solver.constraints.propagators.gary.tsp.directed;

import choco.annotations.PropAnn;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * Each node but "but" has only one predecessor
 * */
@PropAnn(tested=PropAnn.Status.BENCHMARK)
public class PropOnePredBut extends Propagator<DirectedGraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
    GraphDeltaMonitor gdm;
	int but,n;
	private PairProcedure arcEnforced, arcRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** All nodes of the graph but "but" have only one predecessor
	 * @param graph
	 * @param but the node which is not concerned by the constraint
	 * @param constraint
	 * @param solver
	 * */
	public PropOnePredBut(DirectedGraphVar graph, int but, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.BINARY);
		g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
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
		INeighbors preds;
		int pre;
		for(int i=0;i<n;i++){
			if(i!=but){
				pre = g.getKernelGraph().getPredecessorsOf(i).getFirstElement();
				preds = g.getEnvelopGraph().getPredecessorsOf(i);
				if (preds.neighborhoodSize()==0){
					this.contradiction(g,i+" has no predecessor");
				}
				else if (preds.neighborhoodSize()==1){
					g.enforceArc(preds.getFirstElement(),i,this);
				}
				else if (pre!=-1){
					if(g.getKernelGraph().getPredecessorsOf(i).getNextElement()!=-1){
						contradiction(g,i+" has >1 predecessors");
					}
					for(int j=preds.getFirstElement();j>=0;j=preds.getNextElement()){
						if(j!=pre){
							g.removeArc(j,i,this);
						}
					}
				}
			}
		}
		gdm.unfreeze();
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		gdm.freeze();
		gdm.forEachArc(arcEnforced, EventType.ENFORCEARC);
        gdm.forEachArc(arcRemoved, EventType.REMOVEARC);
        gdm.unfreeze();
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
				if(g.getEnvelopGraph().getPredecessorsOf(i).neighborhoodSize()<1 || g.getKernelGraph().getPredecessorsOf(i).neighborhoodSize()>1){
					return ESat.FALSE;
				}
				if(g.getKernelGraph().getPredecessorsOf(i).neighborhoodSize()!=g.getEnvelopGraph().getPredecessorsOf(i).neighborhoodSize()){
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

	private class EnfArc implements PairProcedure {
		private Propagator p;

		private EnfArc(Propagator p){
			this.p = p;
		}
		@Override
		public void execute(int from, int to) throws ContradictionException {
			if(to!=but){
				INeighbors preds = g.getEnvelopGraph().getPredecessorsOf(to);
				for(int i=preds.getFirstElement(); i>=0; i = preds.getNextElement()){
					if(i!=from){
						g.removeArc(i,to,p);
					}
				}
			}
		}
	}

	private class RemArc implements PairProcedure{
		private Propagator p;

		private RemArc(Propagator p){
			this.p = p;
		}
		@Override
		public void execute(int from, int to) throws ContradictionException {
			if(to!=but){
				INeighbors preds = g.getEnvelopGraph().getPredecessorsOf(to);
				if (preds.neighborhoodSize()==0){
					p.contradiction(g,to+" has no predecessor");
				}
				if (preds.neighborhoodSize()==1){
					g.enforceArc(preds.getFirstElement(),to,p);
				}
			}
		}
	}
}
