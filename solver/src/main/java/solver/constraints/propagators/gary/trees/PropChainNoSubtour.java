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

package solver.constraints.propagators.gary.trees;

import choco.annotations.PropAnn;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 *
 * Simple NoSubtour of Pesant when undirected graph
 * */
@PropAnn(tested=PropAnn.Status.BENCHMARK)
public class PropChainNoSubtour extends GraphPropagator<UndirectedGraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	UndirectedGraphVar g;
	int n;
	private IntProcedure arcEnforced;
	private IStateInt[] origin,end,size;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Ensures that graph has no circuit, with Caseaux/Laburthe/Pesant algorithm
	 * runs in O(1) per instantiation event
	 * @param graph
	 * @param constraint
	 * @param solver
	 * */
	public PropChainNoSubtour(UndirectedGraphVar graph, Constraint<UndirectedGraphVar, Propagator<UndirectedGraphVar>> constraint, Solver solver) {
		super(new UndirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.n = g.getEnvelopGraph().getNbNodes();
		arcEnforced = new EnfArc();
		origin = new IStateInt[n];
		size = new IStateInt[n];
		end = new IStateInt[n];
		for(int i=0;i<n;i++){
			origin[i] = environment.makeInt(i);
			size[i] = environment.makeInt(1);
			end[i] = environment.makeInt(i);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int j;
		for(int i=0;i<n;i++){
			end[i].set(i);
			origin[i].set(i);
			size[i].set(1);
		}
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getKernelGraph().getSuccessorsOf(i);
			j = nei.getFirstElement();
			if(j!=-1 && i<j){
				enforce(i,j);
			}
			j = nei.getNextElement();
			if(j!=-1 && i<j){
				enforce(i,j);
			}
			if(nei.getNextElement()!=-1){
				contradiction(g,"");
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(ALWAYS_COARSE){
			propagate(0);
			return;
		}
		eventRecorder.getDeltaMonitor(this,g).forEach(arcEnforced, EventType.ENFORCEARC);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCEARC.mask ;
	}

	@Override
	public ESat isEntailed() {
		if(g.instantiated()){
			for(int i=0;i<n;i++){
				if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()<2){
					return ESat.FALSE;
				}
				if(g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()>2){
					return ESat.UNDEFINED;
				}
			}
			boolean connected = ConnectivityFinder.findCCOf(g.getEnvelopGraph()).size()==1;
			if(connected){
				return ESat.TRUE;
			}
			return ESat.FALSE;
		}
		return ESat.UNDEFINED;
	}

	private void enforce(int i, int j) throws ContradictionException {
//		if(end[j].get()==end[i].get() && origin[i].get()==origin[j].get()){
//			if(size[origin[i].get()].get()==n){
//				return;
//			}
//			contradiction(g,"");
//		}
		if(end[i].get() == i && origin[j].get()==j){
			enforceNormal(i,j);
			return;
		}
		if(origin[i].get() == i && end[j].get()==j){
			enforceNormal(j,i);
			return;
		}
		if(origin[i].get() == i && origin[j].get()==j){
			int newOrigin = end[i].get();
			int newEnd = origin[i].get();
			origin[i].set(newOrigin);
			origin[newOrigin].set(newOrigin);
			end[i].set(newEnd);
			end[newEnd].set(newEnd);
			size[newOrigin].set(size[newEnd].get());
			enforceNormal(i,j);
			return;
		}
		if(end[i].get() == i && end[j].get()==j){
			int newOrigin = end[j].get();
			int newEnd = origin[j].get();
			end[j].set(newEnd);
			end[newEnd].set(newEnd);
			origin[j].set(newOrigin);
			origin[newOrigin].set(newOrigin);
			size[newOrigin].set(size[newEnd].get());
			enforceNormal(i,j);
			return;
		}
		contradiction(g,"");//this cas should not happen (except if deg=2 is not propagated yet)
	}

	private void enforceNormal(int i, int j) throws ContradictionException {
		int last = end[j].get();
		int start = origin[i].get();
		origin[last].set(start);
		end[start].set(last);
		size[start].add(size[j].get());
		if(size[start].get()>n){
			contradiction(g,"");
		}
		if(last!=j || start != i){
			g.removeArc(last,start,this);
		}
		if(start==0 || last == n-1){
			if(size[0].get()+size[origin[n-1].get()].get()<n){
				g.removeArc(end[0].get(),origin[n-1].get(),this);
			}
		}
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class EnfArc implements IntProcedure {
		@Override
		public void execute(int i) throws ContradictionException {
			enforce(i/n-1,i%n);
		}
	}
}
