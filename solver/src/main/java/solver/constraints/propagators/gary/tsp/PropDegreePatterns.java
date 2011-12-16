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
import choco.kernel.memory.IStateBitSet;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * Find some infeasible patterns based on nodes degree
 * */
public class PropDegreePatterns<V extends DirectedGraphVar> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	private IStateBitSet computed;
	private IntProcedure remArcs;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Find some infeasible patterns
	 * @param graph
	 * @param constraint
	 * @param solver
	 * */
	public PropDegreePatterns(DirectedGraphVar graph, Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.n = g.getEnvelopGraph().getNbNodes();
		remArcs = new RemArc(this);
		computed = environment.makeBitSet(n);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
//		checkPattern();
		INeighbors nei;
		int x,y;
		boolean hasChanged = true;
		while(hasChanged){
			hasChanged = false;
			for(int i=0;i<n;i++){
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				if(nei.neighborhoodSize()==2){
					x=nei.getFirstElement();
					y=nei.getNextElement();
					hasChanged |= g.removeArc(x,y,this,false);
					hasChanged |= g.removeArc(y,x,this,false);
				}
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				if(nei.neighborhoodSize()==2){
					x=nei.getFirstElement();
					y=nei.getNextElement();
					hasChanged |= g.removeArc(x,y,this,false);
					hasChanged |= g.removeArc(y,x,this,false);
				}
			}
		}
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(true){
			propagate(EventType.FULL_PROPAGATION.mask);
			return;
		}
        eventRecorder.getDeltaMonitor(g).forEach(remArcs, EventType.REMOVEARC);
	}

	private void checkPattern() throws ContradictionException {
		BitSet notInList = new BitSet(n);
		LinkedList<Integer> list = new LinkedList<Integer>();
		INeighbors suc,pred;
		int j,k;
		notInList.clear();
		for(int i=0; i<n;i++){
			list.add(i);
		}
		int i;
		while(!list.isEmpty()){
			i = list.pop();
			notInList.set(i);
			suc = g.getEnvelopGraph().getSuccessorsOf(i);
			if(suc.neighborhoodSize()==2){
				j = suc.getFirstElement();
				k = suc.getNextElement() ;
				if(g.removeArc(j,k,this,false) || g.removeArc(k,j,this,false)){
					if(notInList.get(j)){
						list.add(j);
					}
					if(notInList.get(k)){
						list.add(k);
					}
				}
			}else{
				pred = g.getEnvelopGraph().getPredecessorsOf(i);
				if(pred.neighborhoodSize()==2){
					j = pred.getFirstElement();
					k = pred.getNextElement() ;
					if(g.removeArc(j,k,this,false) || g.removeArc(k,j,this,false)){
						if(notInList.get(j)){
							list.add(j);
						}
						if(notInList.get(k)){
							list.add(k);
						}
					}
				}
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class RemArc implements IntProcedure {
		private GraphPropagator p;
		private INeighbors nei;

		private RemArc(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int to = i%n;
			int from = i/n-1;
			checkSuccs(from);
			checkPreds(to);
		}

		private void checkPreds(int i) throws ContradictionException {
			if(!computed.get(i)){
				nei = g.getEnvelopGraph().getPredecessorsOf(i);
				if(nei.neighborhoodSize()==2){
					computed.set(i);
					int j = nei.getFirstElement();
					int k = nei.getNextElement() ;
					if(g.removeArc(j,k,p,false)){
						checkPreds(k);
						checkSuccs(j);
					}
					if(g.removeArc(k,j,p,false)){
						checkSuccs(k);
						checkPreds(j);
					}
				}
			}
		}

		private void checkSuccs(int i) throws ContradictionException {
			if(!computed.get(i)){
				nei = g.getEnvelopGraph().getSuccessorsOf(i);
				if(nei.neighborhoodSize()==2){
					computed.set(i);
					int j = nei.getFirstElement();
					int k = nei.getNextElement() ;
					if(g.removeArc(j,k,p,false)){
						checkPreds(k);
						checkSuccs(j);
					}
					if(g.removeArc(k,j,p,false)){
						checkSuccs(k);
						checkPreds(j);
					}
				}
			}
		}
	}
}
