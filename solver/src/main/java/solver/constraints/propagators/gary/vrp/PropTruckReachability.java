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

package solver.constraints.propagators.gary.vrp;

import choco.kernel.ESat;
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

public class PropTruckReachability extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	int nbTrucks;
	IntVar[] trucks;
	int[] stack;
	BitSet inStack;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTruckReachability(DirectedGraphVar graph, IntVar[] trucks, int nbTrucks,
								 Constraint constraint, Solver solver) {
		super(ArrayUtils.append(trucks,new Variable[]{graph}), solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.nbTrucks = nbTrucks;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.trucks = trucks;
		stack = new int[n];
		inStack = new BitSet(n);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<nbTrucks;i++){
			checkTruckReachability(i);
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
	}

	private void checkTruckReachability(int truck) throws ContradictionException {
		int node;
		int size = 0;
		int idx  = 0;
		inStack.clear();
		stack[size++] = 2*truck;
		inStack.set(2*truck);
		INeighbors nei;
		for(int i=0;i<n;i++){
			if(!trucks[i].contains(truck)){
				inStack.set(i);
			}
		}
		while(idx!=size){
			node = stack[idx++];
			nei = g.getEnvelopGraph().getSuccessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!inStack.get(j)){
					stack[size++] = j;
					inStack.set(j);
				}
			}
		}
		for(int i=inStack.nextClearBit(0);i<n;i=inStack.nextClearBit(i+1)){
			trucks[i].removeValue(truck,this);
		}
		size = 0;
		idx  = 0;
		inStack.clear();
		stack[size++] = 2*truck+1;
		inStack.set(2*truck+1);
		for(int i=0;i<n;i++){
			if(!trucks[i].contains(truck)){
				inStack.set(i);
			}
		}
		while(idx!=size){
			node = stack[idx++];
			nei = g.getEnvelopGraph().getPredecessorsOf(node);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!inStack.get(j)){
					stack[size++] = j;
					inStack.set(j);
				}
			}
		}
		for(int i=inStack.nextClearBit(0);i<n;i=inStack.nextClearBit(i+1)){
			trucks[i].removeValue(truck,this);
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask+EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
