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
import choco.kernel.memory.IStateInt;
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
import solver.variables.graph.directedGraph.IDirectedGraph;
import java.util.BitSet;

public class PropGraphTimeChanneling extends GraphPropagator {

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
	int[] bound;
	BitSet done;
	Heap heap;
	// RG data
	int[] minMate, minOut;
	TIntArrayList tempNextSCC;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropGraphTimeChanneling(IntVar[] time, IntVar[] trucks, DirectedGraphVar graph, int[][] matrix, int nbTrucks, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(time,trucks,new Variable[]{graph}), solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.time = time;
		this.trucks = trucks;
		this.n = g.getEnvelopGraph().getNbNodes();
		this.nbTrucks = nbTrucks;
		dist = matrix;
		tempNextSCC = new TIntArrayList();
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
		dijsktra_lb();
		dijsktra_ub();
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
	}

	//***********************************************************************************
	// LB
	//***********************************************************************************

	private void dijsktra_lb() throws ContradictionException {
		heap.clear();
		done.clear();
		for(int i=0;i<n;i++){
			bound[i] = Integer.MAX_VALUE;
		}
		int x;
		for(int i=0;i<nbTrucks*2; i+=2){
			bound[i] = time[i].getLB();
			heap.add(i, bound[i],-1);
		}
		int nb;
		INeighbors nei;
		while(!heap.isEmpty()){
			x = heap.pop();
			done.set(x);
			nei = g.getEnvelopGraph().getSuccessorsOf(x);
			for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
				if(g.getKernelGraph().arcExists(x,i)){
					enfArc(x,i);
				}
				nb = bound[x]+dist[x][i];
				if(nb>time[i].getUB()){
					g.removeArc(x,i,this);
				}else if(nb<bound[i]){
					bound[i] = nb;
					heap.add(i,bound[i],x);
				}
			}
		}
		for(int i=0;i<n;i++){
			time[i].updateLowerBound(bound[i],this);
		}
	}

	//***********************************************************************************
	// UB
	//***********************************************************************************

	private void dijsktra_ub() throws ContradictionException {
		heap.clear();
		done.clear();
		for(int i=0;i<n;i++){
			bound[i] = Integer.MAX_VALUE;
		}
		int x;
		int max = time[1].getUB();
		for(int i=1;i<2*nbTrucks;i+=2){
			bound[i] = 0;
			heap.add(i, bound[i],-1);
		}
		int nb;
		INeighbors nei;
		while(!heap.isEmpty()){
			x = heap.pop();
			done.set(x);
			nei = g.getEnvelopGraph().getPredecessorsOf(x);
			for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
				if(g.getKernelGraph().arcExists(i,x)){
					enfArc(i,x);
				}
				nb = bound[x]+dist[i][x];
				if(nb>max-time[i].getLB()){
					g.removeArc(i,x,this);
				}else if(nb<bound[i]){
					if(done.get(i)){
						throw new UnsupportedOperationException();
					}else{
						bound[i] = nb;
						heap.add(i,bound[i],x);
					}
				}
			}
		}
		for(int i=0;i<n;i++){
			time[i].updateUpperBound(max - bound[i], this);
		}
	}
}