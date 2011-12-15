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
import solver.variables.delta.monitor.GraphDeltaMonitor;
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
	private IntProcedure remArcs;
	private BitSet tocheck;

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
		remArcs = new RemArc();
		tocheck = new BitSet(n);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		checkPattern(-1,-1);
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		GraphDeltaMonitor gdm = (GraphDeltaMonitor)eventRecorder.getDeltaMonitor(g);

		if(gdm.toArcRemoval() - gdm.fromArcRemoval()>(n-2)/2){
			checkPattern(-1,-1);
		}else{
			gdm.forEach(remArcs, EventType.REMOVEARC);
		}
	}

	private void checkPattern(int idx1, int idx2) throws ContradictionException {
		tocheck.clear();
		if(idx1==-1 && idx2==-1){
			tocheck.flip(0,n);
		}else{
			tocheck.set(idx1);
			tocheck.set(idx2);
		}
		int i = tocheck.nextSetBit(0);
		INeighbors nei;
		int x,y;
		while(i>=0){
			tocheck.clear(i);
			if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()<=2
			&& g.getEnvelopGraph().getPredecessorsOf(i).neighborhoodSize()<=2){
				nei = g.getEnvelopGraph().getNeighborsOf(i);
				x=nei.getFirstElement();
				y=nei.getNextElement();
				if(nei.getNextElement()==-1 && x!=-1 && y!=-1){
					if(g.removeArc(x,y,this,false) || g.removeArc(y,x,this,false)){
						tocheck.set(i);
						tocheck.set(i);
					}
				}
			}
			i = tocheck.nextSetBit(0);
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
		@Override
		public void execute(int i) throws ContradictionException {
			checkPattern(i/n-1,i%n);
		}
	}
}
