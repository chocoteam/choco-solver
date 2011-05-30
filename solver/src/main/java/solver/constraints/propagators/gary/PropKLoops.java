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
import choco.kernel.memory.IStateInt;
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
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;

/**Propagator that ensures that K loops belong to the final graph
 * 
 * @author Jean-Guillaume Fages
 *
 */
public class PropKLoops<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IntVar k;
	private IStateInt nbInKer, nbInEnv;
	private int n;
	private IntProcedure arcEnforced;
	private IntProcedure arcRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKLoops(GraphVar graph, Solver solver, Constraint<V, Propagator<V>> constraint, IntVar k) {
		super((V[]) new Variable[]{graph,k}, solver, constraint, PropagatorPriority.LINEAR, false);
		g = graph;
		this.k = k;
		n = g.getEnvelopGraph().getNbNodes();
		nbInEnv = environment.makeInt();
		nbInKer = environment.makeInt();
		arcEnforced = new EnfArc();
		arcRemoved  = new RemArc();
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		int min = 0;
		int max = 0;
		IActiveNodes ker = g.getKernelGraph().getActiveNodes();
		IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
		for(int i=ker.nextValue(0);i>=0;i = ker.nextValue(i+1)){
			if(g.getKernelGraph().edgeExists(i, i)){
				min++;
			}
		}
		for(int i=env.nextValue(0);i>=0;i = env.nextValue(i+1)){
			if(g.getEnvelopGraph().edgeExists(i, i)){
				max++;
			}
		}
		k.updateLowerBound(min, this);
		k.updateUpperBound(max, this);
		nbInEnv.set(max);
		nbInKer.set(min);
		if(k.instantiated()){
			if(min==max){
				setPassive();
			}else{
				if(max==k.getValue()){
					IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
					for(int node=act.nextValue(0); node>=0; node = act.nextValue(node+1)){
						if(g.getEnvelopGraph().edgeExists(node, node)){
							g.enforceArc(node,node, this);
						}
					}
					setPassive();
				}else if(min==k.getValue()){
					IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
					for(int node=act.nextValue(0); node>=0; node = act.nextValue(node+1)){
						if(g.getEnvelopGraph().edgeExists(node, node) && !g.getKernelGraph().edgeExists(node, node)){
							g.removeArc(node,node, this);
						}
					}
					setPassive();
				}
			}
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		if( request instanceof GraphRequest){
			GraphRequest gr = (GraphRequest) request;
			if((mask & EventType.ENFORCEARC.mask) !=0){
				IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
				d.forEach(arcEnforced, gr.fromArcEnforcing(), gr.toArcEnforcing());
			}
			if((mask & EventType.REMOVEARC.mask)!=0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(arcRemoved, gr.fromArcRemoval(), gr.toArcRemoval());
			}
			k.updateLowerBound(nbInKer.get(), this);
			k.updateUpperBound(nbInEnv.get(), this);
		}
		if(k.instantiated()){
			if(nbInEnv.get()==k.getValue()){
				IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
				for(int node=act.nextValue(0); node>=0; node = act.nextValue(node+1)){
					if(g.getEnvelopGraph().edgeExists(node, node)){
						g.enforceArc(node,node, this);
					}
				}
				setPassive();
			}else if(nbInKer.get()==k.getValue()){
				IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
				for(int node=act.nextValue(0); node>=0; node = act.nextValue(node+1)){
					if(g.getEnvelopGraph().edgeExists(node, node) && !g.getKernelGraph().edgeExists(node, node)){
						g.removeArc(node,node, this);
					}
				}
				setPassive();
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.INSTANTIATE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class EnfArc implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			if(from == to){
				nbInKer.set(nbInKer.get()+1);
			}
		}
	}
	private class RemArc implements IntProcedure{
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			if(from == to){
				nbInEnv.set(nbInEnv.get()-1);
			}
		}
	}
}
