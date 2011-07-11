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

package solver.constraints.propagators.gary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**Propagator channeling an undirected graph and an array of integer variables
 * 
 * BEWARE : for use reasons the channeling is only performed on arcs but not on nodes
 * 
 * @author Jean-Guillaume Fages
 *
 * @param <V>
 */
public class PropIntVarsGraphChanneling<V extends Variable> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private UndirectedGraphVar g;
	private IntVar[] intVars;
	private int[] values;
	private TIntIntHashMap valuesHash;
	private ValRem valRemoved;
	private IntProcedure arcEnforced;
	private IntProcedure arcRemoved;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public PropIntVarsGraphChanneling(IntVar[] vars, UndirectedGraphVar graph,Solver solver, Constraint mixtedAllDiff,PropagatorPriority storeThreshold, boolean b, int[] v, TIntIntHashMap vH) {
		super((V[]) ArrayUtils.append(vars,new Variable[]{graph}), solver, mixtedAllDiff, storeThreshold, b);
		g = graph;
		intVars = vars;
		this.values = v;
		this.valuesHash = vH;
		int n = values.length;
		// IntVar events
		valRemoved = new ValRem(this);
		// Graph events
		arcEnforced = new EdgeEnf(this, n);
		arcRemoved = new EdgeRem(this, n);
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		// BEWARE the graph is created from the variables so it is initially correct (true for a standard use)
		for(int i=0; i<intVars.length; i++){
			if(intVars[i].instantiated()){
				g.enforceArc(i, valuesHash.get(intVars[i].getValue()), this);
			}
		}
		IActiveNodes act = g.getKernelGraph().getActiveNodes();
		for (int i = act.getFirstElement(); i>=0; i = act.getNextElement()) {
			if (g.getKernelGraph().getNeighborsOf(i).neighborhoodSize()==1){
				if(i<intVars.length){
					intVars[i].instantiateTo(values[g.getKernelGraph().getNeighborsOf(i).getFirstElement()], this);
				}
			}
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if (request instanceof GraphRequest) {
			GraphRequest gr = (GraphRequest) request;
			if((mask & EventType.ENFORCEARC.mask) !=0){
				IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
				d.forEach(arcEnforced, gr.fromArcEnforcing(), gr.toArcEnforcing());
			}
			if((mask & EventType.REMOVEARC.mask)!=0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(arcRemoved, gr.fromArcRemoval(), gr.toArcRemoval());
			}
		}
		else{
			if(EventType.anInstantiationEvent(mask)){
				g.enforceArc(idxVarInProp, valuesHash.get(intVars[idxVarInProp].getValue()), this);
			}
			if((mask & (EventType.REMOVE.mask | EventType.INCLOW.mask | EventType.DECUPP.mask)) !=0){
				IntVar var = (IntVar) request.getVariable();
				IntDelta d = var.getDelta();
				valRemoved.set(idxVarInProp);
				d.forEach(valRemoved, request.fromDelta(), request.toDelta());
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ALL_MASK() + EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		for (int vIdx=0; vIdx<intVars.length; vIdx++) {
			IntVar v = intVars[vIdx];
			// An IntVar cannot be instantiated to more than one value
			if(g.getKernelGraph().getNeighborsOf(vIdx).neighborhoodSize()>1){ 
				return ESat.FALSE;
			}
			// the instantiation value of the IntVar must match with the graph
			if(g.getKernelGraph().getNeighborsOf(vIdx).neighborhoodSize()==1 && 
					!intVars[vIdx].contains(values[g.getKernelGraph().getNeighborsOf(vIdx).getFirstElement()])){
				return ESat.FALSE;
			}
			if (v.instantiated()) {
				int vv = v.getValue();
				if(!g.getKernelGraph().edgeExists(vIdx, valuesHash.get(vv))){
					return ESat.FALSE;
				}
			}
		}
		if (isCompletelyInstantiated()) {
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/** When a value y is removed from the domain of x then the edge (x,y) must be pruned*/
	private class ValRem implements IntProcedure {
		private int idx;
		private Propagator p;

		private ValRem(Propagator p){
			this.p = p;
		}

		private void set(int i) {
			idx = i;
		}

		@Override
		public void execute(int i) throws ContradictionException {
			int from = idx;
			int to   = valuesHash.get(i);
			g.removeArc(from, to,p);
		}
	}
	
	/** When an edge (x,y), x<=y, is enforced than x must be instantiated to y */
	private class EdgeEnf implements IntProcedure {

		private Propagator p;
		private int n;

		private EdgeEnf(Propagator p, int n){
			this.p = p;
			this.n = n;
		}
		
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			if(from<to){
				intVars[from].instantiateTo(values[to], p);
			}else{
				intVars[to].instantiateTo(values[from], p);
			}
		}
		
	}
	
	/** When an edge (x,y), x<=y, is removed than a value y must be removed from the domain of x */
	private class EdgeRem implements IntProcedure {

		private Propagator p;
		private int n;

		private EdgeRem(Propagator p, int n){
			this.p = p;
			this.n = n;
		}
		
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			if(from<to){
				intVars[from].removeValue(values[to], p);
			}else{
				intVars[to].removeValue(values[from], p);
			}
		}
		
	}
}
