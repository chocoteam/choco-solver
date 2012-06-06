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

package solver.constraints.propagators.gary.tsp.directed;

import choco.annotations.PropAnn;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 *
 * Simple nocircuit contraint (from NoSubtour of Pesant or noCycle of Caseaux/Laburthe)
 * */
@PropAnn(tested=PropAnn.Status.BENCHMARK)
public class PropCircuitNoSubtour extends Propagator<DirectedGraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
    GraphDeltaMonitor gdm;
	int n;
	private PairProcedure arcEnforced;
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
	public PropCircuitNoSubtour(DirectedGraphVar graph, Constraint constraint, Solver solver) {
		super(new DirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
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
		int i,j;
		for(i=0;i<n;i++){
			end[i].set(i);
			origin[i].set(i);
			size[i].set(1);
		}
		for(i=0;i<n;i++){
			j = g.getKernelGraph().getSuccessorsOf(i).getFirstElement();
			if(j!=-1){
				enforce(i,j);
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		gdm.freeze();
		gdm.forEachArc(arcEnforced, EventType.ENFORCEARC);
        gdm.unfreeze();
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
		if(end[i].get()!=i){
			contradiction(g,"");
		}
		if(i==last && j==start){
			if(size[start].get()!=n){
				contradiction(g,"");
			}
			return;
		}
		origin[last].set(start);
		end[start].set(last);
		size[start].add(size[j].get());
		if(size[start].get()!=n){
			g.removeArc(last,start,this);
		}else{
			g.enforceArc(last,start,this);
		}
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class EnfArc implements PairProcedure {
		@Override
		public void execute(int i, int j) throws ContradictionException {
			enforce(i,j);
		}
	}
}
