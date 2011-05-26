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
import choco.kernel.memory.IEnvironment;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.requests.IRequest;

/**Propagator that ensures that K nodes belong to the final graph
 * 
 * @author Jean-Guillaume Fages
 *
 */
public class PropKNodes<V extends GraphVar> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IntVar k;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKNodes(V graph, IEnvironment environment, Constraint<V, Propagator<V>> constraint, IntVar k) {
		super((V[]) new GraphVar[]{graph}, environment, constraint, PropagatorPriority.LINEAR, false);
		g = graph;
		this.k = k;
		if(k.getLB()<=0){
			throw new UnsupportedOperationException("K must be > 0");
		}
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		k.updateLowerBound(g.getKernelOrder(), this);
		k.updateUpperBound(g.getEnvelopOrder(), this);
		if(k.instantiated()){
			if(g.getEnvelopOrder()==g.getKernelOrder()){
				setPassive();
			}else{
				if(g.getEnvelopOrder()==k.getValue()){
					IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
					for(int node=act.nextValue(0); node>=0; node = act.nextValue(node+1)){
						g.enforceNode(node, this);
					}
					setPassive();
				}else if(g.getKernelOrder()==k.getValue()){
					IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
					for(int node=act.nextValue(0); node>=0; node = act.nextValue(node+1)){
						if(!g.getKernelGraph().getActiveNodes().isActive(node)){
							g.removeNode(node, this);
						}
					}
					setPassive();
				}
			}
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		k.updateLowerBound(g.getKernelOrder(), this);
		k.updateUpperBound(g.getEnvelopOrder(), this);
		if(k.instantiated()){
			if(g.getEnvelopOrder()==g.getKernelOrder()){
				setPassive();
			}else{
				if(g.getEnvelopOrder()==k.getValue()){
					IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
					for(int node=act.nextValue(0); node>=0; node = act.nextValue(node+1)){
						g.enforceNode(node, this);
					}
					setPassive();
				}else if(g.getKernelOrder()==k.getValue()){
					IActiveNodes act = g.getEnvelopGraph().getActiveNodes();
					for(int node=act.nextValue(0); node>=0; node = act.nextValue(node+1)){
						if(!g.getKernelGraph().getActiveNodes().isActive(node)){
							g.removeNode(node, this);
						}
					}
					setPassive();
				}
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVENODE.mask + EventType.INSTANTIATE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
	}

	@Override
	public ESat isEntailed() {
		if(g.getKernelOrder()>k.getUB()){
			return ESat.FALSE;
		}
		if(g.getEnvelopOrder()<k.getLB()){
			return ESat.FALSE;
		}
		if(g.getEnvelopOrder() == g.getKernelOrder() && (g.getEnvelopOrder() != k.getValue() && k.instantiated())){
			return ESat.FALSE;
		}
		if(g.getEnvelopOrder() != g.getKernelOrder()){
			return ESat.UNDEFINED;
		}
		if(!k.instantiated()){
			return ESat.UNDEFINED;
		}
		return ESat.TRUE;
	}
}
