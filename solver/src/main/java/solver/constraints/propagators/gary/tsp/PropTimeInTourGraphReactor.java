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
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IStateInt;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.tsp.heaps.Heap;
import solver.constraints.propagators.gary.tsp.heaps.VerySimpleHeap;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

public class PropTimeInTourGraphReactor extends GraphPropagator {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n;
	IntVar[] time;
	int[][] dist;
	private IntProcedure arcEnforced;
	IStateInt nR; IStateInt[] sccOf; INeighbors[] outArcs; IDirectedGraph rg;
	private TIntArrayList tempNextSCC, tempCurSCC;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTimeInTourGraphReactor(IntVar[] intVars, DirectedGraphVar graph, int[][] matrix, Constraint constraint, Solver solver) {
		super(ArrayUtils.append(intVars,new Variable[]{graph}), solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.time = intVars;
		this.n = g.getEnvelopGraph().getNbNodes();
		arcEnforced = new EnfArc(this);
		dist = matrix;
		tempCurSCC = new TIntArrayList();
		tempNextSCC = new TIntArrayList();
	}

	public PropTimeInTourGraphReactor(IntVar[] intVars, DirectedGraphVar graph, int[][] matrix, Constraint constraint, Solver solver,
									  IStateInt nR, IStateInt[] sccOf, INeighbors[] outArcs, IDirectedGraph rg) {
		this(intVars,graph,matrix,constraint,solver);
		this.nR = nR;
		this.sccOf = sccOf;
		this.outArcs = outArcs;
		this.rg = rg;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			if(g.getKernelGraph().getSuccessorsOf(i).getFirstElement()!=-1){
				enfArc(i,g.getKernelGraph().getSuccessorsOf(i).getFirstElement());
			}
		}
		graphTrasversal();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(true){
			propagate(0);return;
		}
		if(idxVarInProp == n){
			eventRecorder.getDeltaMonitor(g).forEach(arcEnforced, EventType.ENFORCEARC);
		}
		graphTrasversal();
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.DECUPP.mask + EventType.INCLOW.mask + EventType.INSTANTIATE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// BFS global setting of time bounds
	//***********************************************************************************

	private void graphTrasversal() throws ContradictionException {
		dijsktra_lb_rg();
		dijsktra_ub_rg();
	}

	private void dijsktra_lb_rg() throws ContradictionException {
		Heap heap = new VerySimpleHeap(n);
		int[] bound = new int[n];
		for(int i=0;i<n;i++){
			bound[i] = Integer.MAX_VALUE;
		}
		int x = 0;
		bound[x] = time[x].getLB();
		heap.add(x, bound[x],-1);
		BitSet done = new BitSet(n);
		int nb;
		INeighbors nei;
		int scc = 0;
		if(sccOf!=null){
			scc = sccOf[x].get();
		}
		int scci;
		tempCurSCC.clear();
		tempNextSCC.clear();
		int maxbound = -1;
		while(!heap.isEmpty()){
			while(!heap.isEmpty()){
				x = heap.pop();
				maxbound = bound[x];
				done.set(x);
				nei = g.getEnvelopGraph().getSuccessorsOf(x);
				for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
					nb = bound[x]+dist[x][i];
					if(nb>time[i].getUB()){
						g.removeArc(x,i,this);
					}else if(nb<bound[i]){
						if(done.get(i)){
							throw new UnsupportedOperationException();
						}else{
							if(sccOf==null || scc==sccOf[i].get()){
								bound[i] = nb;
								maxbound = Math.max(maxbound,nb);
								heap.add(i,bound[i],x);
							}else{
								tempCurSCC.add(x);
								tempNextSCC.add(i);
							}
						}
					}
				}
			}
			// go to next SCC
			int cur,next;
			if(maxbound==-1){
				throw new UnsupportedOperationException();
			}
			for(int i=0;i<tempCurSCC.size();i++){
				cur = tempCurSCC.get(i);
				next = tempNextSCC.get(i);
				bound[next] = Math.min(bound[next],maxbound+dist[cur][next]);
				heap.add(next,bound[next],cur);
			}
			if(tempNextSCC.size()>0){
				scc = sccOf[tempNextSCC.get(0)].get();
				tempCurSCC.clear();
				tempNextSCC.clear();
			}
		}
		for(int i=0;i<n;i++){
			time[i].updateLowerBound(bound[i],this);
		}
	}

	private void dijsktra_ub_rg() throws ContradictionException {
		Heap heap = new VerySimpleHeap(n);
		int[] bound = new int[n];
		for(int i=0;i<n;i++){
			bound[i] = Integer.MAX_VALUE;
		}
		int x = n-1;
		int max = time[x].getUB();
		bound[x] = 0;
		heap.add(x, bound[x],-1);
		BitSet done = new BitSet(n);
		int nb;
		INeighbors nei;
		int scc = 0;
		if(sccOf!=null){
			scc = sccOf[x].get();
		}
		tempCurSCC.clear();
		tempNextSCC.clear();
		int maxbound = -1;
		while(!heap.isEmpty()){
			while(!heap.isEmpty()){
				x = heap.pop();
				maxbound = bound[x];
				done.set(x);
				nei = g.getEnvelopGraph().getPredecessorsOf(x);
				for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
					nb = bound[x]+dist[i][x];
					if(nb>max-time[i].getLB()){
						g.removeArc(i,x,this);
					}else if(nb<bound[i]){
						if(done.get(i)){
							throw new UnsupportedOperationException();
						}else{
							if(sccOf == null || scc==sccOf[i].get()){
								bound[i] = nb;
								maxbound = Math.max(maxbound,nb);
								heap.add(i,bound[i],x);
							}else{
								tempCurSCC.add(x);
								tempNextSCC.add(i);
							}
						}
					}
				}
			}
			// go to next SCC
			int cur,next;
			if(maxbound==-1){
				throw new UnsupportedOperationException();
			}
			for(int i=0;i<tempCurSCC.size();i++){
				cur = tempCurSCC.get(i);
				next = tempNextSCC.get(i);
				bound[next] = Math.min(bound[next],maxbound+dist[next][cur]);
				heap.add(next,bound[next],cur);
			}
			if(tempNextSCC.size()>0){
				scc = sccOf[tempNextSCC.get(0)].get();
				tempCurSCC.clear();
				tempNextSCC.clear();
			}
		}
		for(int i=0;i<n;i++){
			time[i].updateUpperBound(max - bound[i], this);
		}
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private void enfArc(int from, int to) throws ContradictionException {
		time[from].updateUpperBound(time[to].getUB() - dist[from][to], this);
		time[to].updateLowerBound(time[from].getLB() + dist[from][to], this);
	}

	private class EnfArc implements IntProcedure {
		private GraphPropagator p;

		private EnfArc(GraphPropagator p){
			this.p = p;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			enfArc(i/n-1,i%n);
		}
	}
}
