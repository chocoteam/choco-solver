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
import solver.variables.graph.graphStructure.adjacencyList.storedStructures.StoredArraySwapList_Array_RemoveOnly;

import java.util.BitSet;

public class PropTaskSweep extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	int n;
	IntVar[] starts;
	IntVar[] ends;
	IntVar[] durations;
	int[][] dist;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTaskSweep(IntVar[] st, IntVar[] end, IntVar[] dur, int[][] dist, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(st,end,dur), solver, constraint, PropagatorPriority.BINARY);
		starts = st;
		durations = dur;
		ends = end;
		this.n = st.length;
		this.dist = dist;
//		mates = new INeighbors[n];
//		for(int i=0;i<n;i++){
//			mates[i] = new StoredArraySwapList_Array_RemoveOnly(solver.getEnvironment(),n);
//			for(int j=i+1;j<n;j++){
//				if(st[i].getLB()<=end[j].getUB()&&st[j].getLB()<=end[i].getUB()){
//					mates[i].add(j);
//					mates[j].add(i);
//				}
//			}
//		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			updateBounds(i);
		}
		for(int i=0;i<n;i++){
			check(i);
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

	private void check(int i) throws ContradictionException {
		int first = starts[i].getLB();
		int last  = ends[i].getUB();
		int dur   = durations[i].getLB();
		if(last-first<dur*2){
			int mandFirst = last-dur;
			int mandLast  = first+dur;
			if(mandFirst>mandLast){
				throw new UnsupportedOperationException();
			}
			for(int j=0;j<n;j++){
				if(i!=j && starts[i].getLB()<=ends[j].getUB()&&starts[j].getLB()<=ends[i].getUB()){
					boolean left = starts[j].getLB()+durations[j].getLB()<=mandFirst;
					boolean right= ends[j].getUB()-durations[j].getLB()>=mandLast;
					if(left){
						if(!right){
							ends[j].updateUpperBound(mandFirst,this);
						}
					}else{
						if(right){
							starts[j].updateLowerBound(mandLast,this);
						}else{
							contradiction(starts[i],"sweep");
						}
					}
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(true){
			propagate(0);
			return;
		}
		if(idxVarInProp<3*n){
			check(idxVarInProp%n);
		}else{
			//TODO
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.DECUPP.mask + EventType.INCLOW.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
