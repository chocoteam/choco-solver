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

package solver.constraints.propagators.gary.tsp.disjunctive;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.util.BitSet;

/**
 * @PropAnn(tested = {BENCHMARK})
 */
public class PropTaskDefinition extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	IntVar[] starts;
	IntVar[] ends;
	IntVar[] durations;
	int[][] dist;
	IntProcedure arcRemoved;
	private BitSet nodeChanged;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTaskDefinition(IntVar[] st, IntVar[] end, IntVar[] dur, DirectedGraphVar graph, int[][] dist, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(st,end,dur,new Variable[]{graph}), solver, constraint, PropagatorPriority.BINARY);
		g = graph;
		starts = st;
		durations = dur;
		ends = end;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.dist = dist;
		arcRemoved = new RemArc();
		nodeChanged = new BitSet(n);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			setDuration(i);
			updateBounds(i);
			checkGraph(i);
		}
	}

	private void setDuration(int i) throws ContradictionException {
		INeighbors suc = g.getEnvelopGraph().getSuccessorsOf(i);
		int min = suc.getFirstElement();
		int max = min;
		for(int s=suc.getFirstElement();s>=0;s=suc.getNextElement()){
			if(dist[i][s]<dist[i][min]){
				min = s;
			}else if(dist[i][s]>dist[i][max]){
				max = s;
			}
		}
		if(min==-1){
			durations[i].instantiateTo(0,this);
		}else{
			durations[i].updateLowerBound(dist[i][min],this);
			durations[i].updateUpperBound(dist[i][max], this);
		}
	}

	private void updateBounds(int i) throws ContradictionException {
		starts[i].updateUpperBound(ends[i].getUB() - durations[i].getLB(), this);
		starts[i].updateLowerBound(ends[i].getLB() - durations[i].getUB(), this);
		ends[i].updateLowerBound(starts[i].getLB()+durations[i].getLB(),this);
		ends[i].updateUpperBound(starts[i].getUB()+durations[i].getUB(),this);
		durations[i].updateUpperBound(ends[i].getUB()-starts[i].getLB(),this);
		durations[i].updateLowerBound(ends[i].getLB()-starts[i].getUB(),this);
	}

	private void checkGraph(int i) throws ContradictionException {
		int min = durations[i].getLB();
		int max = durations[i].getUB();
		INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
		for(int s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
			if(dist[i][s]<min || dist[i][s]>max){
				g.removeArc(i,s,this);
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(ALWAYS_COARSE){
			propagate(0);
			return;
		}
		if(idxVarInProp<3*n){
			int i = idxVarInProp%n;
			updateBounds(i);
			checkGraph(i);
		}else{
			nodeChanged.clear();
			eventRecorder.getDeltaMonitor(g).forEach(arcRemoved,EventType.REMOVEARC);
			for(int i=nodeChanged.nextSetBit(0);i>=0;i=nodeChanged.nextSetBit(i+1)){
				setDuration(i);
				updateBounds(i);
				checkGraph(i);
			}
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.DECUPP.mask + EventType.INCLOW.mask + EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class RemArc implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			nodeChanged.set(i/n-1);
		}
	}
}
