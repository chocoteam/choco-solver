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
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.tsp.heaps.FastArrayHeap;
import solver.constraints.propagators.gary.tsp.heaps.Heap;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

import java.util.BitSet;

public class PropTruckTimeChanneling extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	// input data
	DirectedGraphVar g;
	int n;
	IntVar[] time,trucks;
	int[][] dist;
	int nbTrucks;
	// algo data
	int[] bound,globalBound;
	BitSet done;
	Heap heap;
	int[] minMate, minOut;
	TIntArrayList tempNextSCC;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTruckTimeChanneling(IntVar[] time, IntVar[] trucks, DirectedGraphVar graph, int[][] matrix, int nbTrucks, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(time,trucks,new Variable[]{graph}), solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.time = time;
		this.trucks = trucks;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.nbTrucks = nbTrucks;
		dist = matrix;
		tempNextSCC = new TIntArrayList();
		globalBound = new int[n];
		bound = new int[n];
		done = new BitSet(n);
		heap = new FastArrayHeap(n);
		minMate = new int[n];
		minOut = new int[n];
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		graphTrasversal();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		forcePropagate(EventType.FULL_PROPAGATION);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.FULL_PROPAGATION.mask
				+EventType.INCLOW.mask+EventType.DECUPP.mask+EventType.INSTANTIATE.mask
//				+EventType.INT_ALL_MASK()
				+EventType.REMOVEARC.mask+ EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// BFS global setting of time bounds
	//***********************************************************************************

	private void graphTrasversal() throws ContradictionException {
		for(int i=0;i<n;i++){
			globalBound[i] = Integer.MAX_VALUE;
		}
		for(int i=0;i<nbTrucks;i++){
			dijsktra_lb(i);
		}
		for(int i=0;i<n;i++){
			time[i].updateLowerBound(globalBound[i],this);
			globalBound[i] = 0;
		}
		for(int i=0;i<nbTrucks;i++){
			dijsktra_ub(i);
		}
		for(int i=0;i<n;i++){
			time[i].updateUpperBound(globalBound[i],this);
		}
	}

	private void enfArc(int from, int to) throws ContradictionException {
		// time
		if(time[from].instantiated()){
			time[to].instantiateTo(Math.max(time[to].getLB(),time[from].getValue()+dist[from][to]),this);
		}
		if(time[to].instantiated()){
			time[from].instantiateTo(Math.min(time[from].getUB(),time[to].getValue()-dist[from][to]),this);
		}
		time[from].updateUpperBound(time[to].getUB() - dist[from][to], this);
		time[to].updateLowerBound(time[from].getLB() + dist[from][to], this);
		// truck
		trucks[from].updateLowerBound(trucks[to].getLB(),this);
		trucks[from].updateUpperBound(trucks[to].getUB(),this);
		trucks[to].updateLowerBound(trucks[from].getLB(),this);
		trucks[to].updateUpperBound(trucks[from].getUB(),this);
		//TODO enumerated reasoning?
	}

	//***********************************************************************************
	// LB
	//***********************************************************************************

	private void dijsktra_lb(int truck) throws ContradictionException {
		heap.clear();
		done.clear();
		for(int i=0;i<n;i++){
			bound[i] = Integer.MAX_VALUE;
		}
		int x = 2*truck;
		bound[x] = time[x].getLB();
		heap.add(x, bound[x],-1);
		int nb;
		INeighbors nei;
		while(!heap.isEmpty()){
			x = heap.pop();
			done.set(x);
			nei = g.getEnvelopGraph().getSuccessorsOf(x);
			for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
				if(trucks[i].contains(truck) && !done.get(i)){
					if(g.getKernelGraph().arcExists(x,i)){
						enfArc(x,i);
					}
					nb = bound[x]+dist[x][i];
					if(nb<bound[i]){
						bound[i] = nb;
						heap.add(i,bound[i],x);
					}
				}
			}
		}
		if(bound[truck*2+1]==Integer.MAX_VALUE){
			contradiction(g,"cannot reach depot");
		}
		for(int i=0;i<n;i++){
			if(bound[i]>time[i].getUB()){
				trucks[i].removeValue(truck,this);
			}
			globalBound[i] = Math.min(globalBound[i],bound[i]);
		}
	}

	//***********************************************************************************
	// UB
	//***********************************************************************************

	private void dijsktra_ub(int truck) throws ContradictionException {
		heap.clear();
		done.clear();
		for(int i=0;i<n;i++){
			bound[i] = Integer.MAX_VALUE;
		}
		int x = 2*truck+1;
		int max = time[1].getUB();
		bound[x] = 0;
		heap.add(x, bound[x],-1);
		int nb;
		INeighbors nei;
		while(!heap.isEmpty()){
			x = heap.pop();
			done.set(x);
			nei = g.getEnvelopGraph().getPredecessorsOf(x);
			for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
				if(trucks[i].contains(truck) && !done.get(i)){
					if(g.getKernelGraph().arcExists(i,x)){
						enfArc(i,x);
					}
					nb = bound[x]+dist[i][x];
					if(nb<bound[i]){
						bound[i] = nb;
						heap.add(i,bound[i],x);
					}
				}
			}
		}
		if(bound[truck*2]==Integer.MAX_VALUE){
			contradiction(g,"cannot reach depot");
		}
		for(int i=0;i<n;i++){
			if(bound[i]>max-time[i].getLB()){
				trucks[i].removeValue(truck,this);
			}
			globalBound[i] = Math.max(globalBound[i],max-bound[i]);
		}
	}
}