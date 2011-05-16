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

import gnu.trove.TIntIntHashMap;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
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
	private RemVal valRemoved;
	private IntProcedure arcEnforced;
	private IntProcedure arcRemoved;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public PropIntVarsGraphChanneling(IntVar[] vars, UndirectedGraphVar graph,IEnvironment environment, Constraint mixtedAllDiff,PropagatorPriority storeThreshold, boolean b, int[] v, TIntIntHashMap vH) {
		super((V[]) ArrayUtils.append(vars,new Variable[]{graph}), environment, mixtedAllDiff, storeThreshold, b);
		g = graph;
		intVars = vars;
		this.values = v;
		this.valuesHash = vH;
		final PropIntVarsGraphChanneling instance = this;
		final int n = values.length;
		// IntVar events
		valRemoved = new RemVal(this);
		// Graph events
		arcEnforced = new IntProcedure() {
			@Override
			public void execute(int i) throws ContradictionException {
				int from = i/n-1;
				int to   = i%n;
				if(from<to){
					intVars[from].instantiateTo(values[to], instance);
				}else{
					intVars[to].instantiateTo(values[from], instance);
				}
			}
		};
		arcRemoved = new IntProcedure() {
			@Override
			public void execute(int i) throws ContradictionException {
				int from = i/n-1;
				int to   = i%n;
				if(from<to){
					intVars[from].removeValue(values[to], instance);
				}else{
					intVars[to].removeValue(values[from], instance);
				}
			}
		};
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if (request instanceof GraphRequest) {
			GraphRequest gv = (GraphRequest) request;
			if((mask & EventType.ENFORCEARC.mask) !=0){
				IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
				d.forEach(arcEnforced, gv.fromArcEnforcing(), gv.toArcEnforcing());
			}
			if((mask & EventType.REMOVEARC.mask) !=0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(arcRemoved, gv.fromArcRemoval(), gv.toArcRemoval());
			}
		}else{
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
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class RemVal implements IntProcedure {
		private int idx;
		private Propagator p;

		private RemVal(Propagator p){
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
}
