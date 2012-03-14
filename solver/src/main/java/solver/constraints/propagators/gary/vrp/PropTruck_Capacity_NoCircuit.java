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
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class PropTruck_Capacity_NoCircuit<V extends DirectedGraphVar> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	int n, truckCapa;
	int[] supply;
	private IntProcedure arcEnforced;
	private IStateInt[] origin,end,currentCapa;


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTruck_Capacity_NoCircuit(DirectedGraphVar graph, int[] capaSupply, int trucksCapacity,
										Constraint<V, Propagator<V>> constraint, Solver solver) {
		super((V[]) new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.n = g.getEnvelopGraph().getNbNodes();
		arcEnforced = new EnfArc();
		origin = new IStateInt[n];
		end = new IStateInt[n];
		currentCapa = new IStateInt[n];
		this.supply = capaSupply;
		this.truckCapa = trucksCapacity;
		for(int i=0;i<n;i++){
			origin[i] = environment.makeInt(i);
			end[i] = environment.makeInt(i);
			currentCapa[i] = environment.makeInt(capaSupply[i]);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int j;
		for(int i=0;i<n;i++){
			origin[i].set(i);
			end[i].set(i);
			currentCapa[i].set(supply[i]);
		}
		INeighbors nei;
		for(int i=0;i<n;i++){
			nei = g.getEnvelopGraph().getSuccessorsOf(i);
			for(j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(supply[i]+supply[j]>truckCapa){
					g.removeArc(i,j,this);
				}
			}
		}
		for(int i=0;i<n;i++){
			j = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
			if(j!=-1){
				enforce(i,j);
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		eventRecorder.getDeltaMonitor(this, g).forEach(arcEnforced, EventType.ENFORCEARC);
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCEARC.mask ;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	private void enforce(int i, int j) throws ContradictionException {
		int last = end[j].get();
		int start = origin[i].get();
		if(origin[j].get()!=j){
			contradiction(g,"");
		}
		g.removeArc(last,start,this);
		origin[last].set(start);
		end[start].set(last);
		currentCapa[last].add(currentCapa[i].get());
		int capa = currentCapa[last].get();
		if(capa>truckCapa || currentCapa[j].get()+currentCapa[i].get()>truckCapa){
			contradiction(g,"");
		}
		INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(last);
		for(int k=nei.getFirstElement();k>=0;k=nei.getNextElement()){
			if(capa+currentCapa[k].get()>truckCapa){
				g.removeArc(last,k,this);
			}
		}
		capa = currentCapa[start].get();
		nei = g.getEnvelopGraph().getPredecessorsOf(start);
		for(int k=nei.getFirstElement();k>=0;k=nei.getNextElement()){
			if(capa+currentCapa[k].get()>truckCapa){
				g.removeArc(k,start,this);
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
