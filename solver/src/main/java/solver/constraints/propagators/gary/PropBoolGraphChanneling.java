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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.variables.BoolVar;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**Propagator channeling between arcs of a graph and a boolean matrix
 * 
 * @author Jean-Guillaume Fages
 */
public class PropBoolGraphChanneling<V extends Variable> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	protected GraphVar graph;
	protected BoolVar[][] relations;
	protected BoolInstG boolChanged;
	protected EnfEdge enf;
	protected RemEdge rem;
	protected int n;
	public static long duration = 0;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public PropBoolGraphChanneling(GraphVar graph, BoolVar[][] rel, Solver solver, Constraint cstr,PropagatorPriority storeThreshold) {
		super((V[]) ArrayUtils.append(ArrayUtils.flatten(rel),new Variable[]{graph}), solver, cstr, storeThreshold, false);
		this.graph = graph;
		relations = rel;
		n = rel.length;
		if (graph instanceof DirectedGraphVar){
			boolChanged = new BoolInstG(this, graph);
			enf = new EnfArc(this);
			rem = new RemArc(this);
		}else{
			boolChanged = new BoolInstG(this, graph);
			enf = new EnfEdge(this);
			rem = new RemEdge(this);
		}
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		long tps = System.currentTimeMillis();
		for(int i=0; i<n; i++){
			for(int j = 0; j<n; j++){
				if(relations[i][j].instantiated()){
					if(relations[i][j].getBooleanValue() == ESat.TRUE){
						graph.enforceArc(i, j, this, false);
					}else{
						graph.removeArc(i, j, this, false);
					}
				}else{
					if (graph instanceof DirectedGraphVar) {
						DirectedGraphVar dig = (DirectedGraphVar) graph;
						if(!dig.getEnvelopGraph().arcExists(i, j)){
							relations[i][j].setToFalse(this, false);
						}
						if(dig.getKernelGraph().arcExists(i, j)){
							relations[i][j].setToTrue(this, false);
						}
					}else{
						if(i>=j){
							if(!graph.getEnvelopGraph().edgeExists(i, j)){
								relations[i][j].setToFalse(this, false);
							}
							if(graph.getKernelGraph().edgeExists(i, j)){
								relations[i][j].setToTrue(this, false);
							}
						}
					}
				}
			}
		}
		duration+=System.currentTimeMillis()-tps;
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		long tps = System.currentTimeMillis();
		if (request instanceof GraphRequest) {
			GraphRequest gv = (GraphRequest) request;
			if((mask & EventType.ENFORCEARC.mask) !=0){
				IntDelta d = (IntDelta) graph.getDelta().getArcEnforcingDelta();
				d.forEach(enf, gv.fromArcEnforcing(), gv.toArcEnforcing());
			}
			if((mask & EventType.REMOVEARC.mask)!=0){
				IntDelta d = (IntDelta) graph.getDelta().getArcRemovalDelta();
				d.forEach(rem, gv.fromArcRemoval(), gv.toArcRemoval());
			}
		}
		else{
			int i = idxVarInProp/n;
			int j = idxVarInProp%n;
			boolChanged.execute(i,j, relations[i][j].getBooleanValue()==ESat.TRUE);
		}
		duration+=System.currentTimeMillis()-tps;
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK() + EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask;
	}

	/* (non-Javadoc)
	 * TODO : could be optimized
	 * @see solver.constraints.propagators.Propagator#isEntailed()
	 */
	@Override
	public ESat isEntailed() {
		long tps = System.currentTimeMillis();
		for(int i=0; i<n; i++){
			for(int j = 0; j<n; j++){
				if(relations[i][j].instantiated()){
					if (graph instanceof DirectedGraphVar) {
						DirectedGraphVar dig = (DirectedGraphVar) graph;
						if(relations[i][j].getBooleanValue()==ESat.TRUE && !dig.getEnvelopGraph().arcExists(i, j)){
							return ESat.FALSE;
						}
						if(relations[i][j].getBooleanValue()==ESat.FALSE && dig.getKernelGraph().arcExists(i, j)){
							return ESat.FALSE;
						}
					}else{
						if(relations[i][j].getBooleanValue()==ESat.TRUE && !graph.getEnvelopGraph().edgeExists(i, j)){
							return ESat.FALSE;
						}
						if(relations[i][j].getBooleanValue()==ESat.FALSE && graph.getKernelGraph().edgeExists(i, j)){
							return ESat.FALSE;
						}
					}
				}
			}
		}
		for(int i=0; i<n; i++){
			for(int j = 0; j<n; j++){
				if(relations[i][j].instantiated()){
					if (graph instanceof DirectedGraphVar) {
						DirectedGraphVar dig = (DirectedGraphVar) graph;
						if(relations[i][j].getBooleanValue()==ESat.TRUE && !dig.getKernelGraph().arcExists(i, j)){
							return ESat.UNDEFINED;
						}
						if(relations[i][j].getBooleanValue()==ESat.FALSE && dig.getEnvelopGraph().arcExists(i, j)){
							return ESat.UNDEFINED;
						}
					}else{
						if(relations[i][j].getBooleanValue()==ESat.TRUE && !graph.getKernelGraph().edgeExists(i, j)){
							return ESat.UNDEFINED;
						}
						if(relations[i][j].getBooleanValue()==ESat.FALSE && graph.getEnvelopGraph().edgeExists(i, j)){
							return ESat.UNDEFINED;
						}
					}
				}else{
					return ESat.UNDEFINED;
				}
			}
		}
		duration+=System.currentTimeMillis()-tps;
		return ESat.TRUE;
	}

	//***********************************************************************************
	// REACT ON BOOL MODIFICATION
	//***********************************************************************************

	/** When a boolean is instantiated, modify the graph */
	private class BoolInstG<G extends GraphVar> {
		protected Propagator p;
		protected G g;

		protected BoolInstG(Propagator p, G g){
			this.p = p;
			this.g = g;
		}

		protected void execute(int i, int j, boolean v) throws ContradictionException {
			if(v){
				g.enforceArc(i, j, p, false);
			}else{
				g.removeArc(i, j, p, false);
			}
		}
	}

	//***********************************************************************************
	// REACT ON GRAPH MODIFICATION
	//***********************************************************************************

	/** When an edge (x,y), is enforced then the relation between x and y is true */
	private class EnfEdge implements IntProcedure{
		protected Propagator p;
		protected EnfEdge(Propagator p){
			this.p = p;
		}
		public void execute(int i) throws ContradictionException{
			int from = i/n-1;
			int to   = i%n;
			set( from, to);
		}
		protected void set(int i, int j) throws ContradictionException{
			relations[i][j].setToTrue(p, false);
			relations[j][i].setToTrue(p, false);
		}
	}
	/** Directed */
	private class EnfArc extends  EnfEdge {
		protected EnfArc(Propagator p){
			super(p);
		}
		@ Override
		protected void set(int i, int j) throws ContradictionException{
			relations[i][j].setToTrue(p, false);
		}
	}

	/** When an edge (x,y), is removed then the relation between x and y is false */
	private class RemEdge implements IntProcedure{
		protected Propagator p;
		protected RemEdge(Propagator p){
			this.p = p;
		}
		public void execute(int i) throws ContradictionException{
			int from = i/n-1;
			int to   = i%n;
			set( from, to);
		}
		protected void set(int i, int j) throws ContradictionException{
			relations[i][j].setToFalse(p, false);
			relations[j][i].setToFalse(p, false);
		}
	}
	private class RemArc extends  RemEdge {
		protected RemArc(Propagator p){
			super(p);
		}
		@ Override
		protected void set(int i, int j) throws ContradictionException{
			relations[i][j].setToFalse(p, false);
		}
	}
}
