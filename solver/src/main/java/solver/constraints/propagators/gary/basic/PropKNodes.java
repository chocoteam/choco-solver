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

package solver.constraints.propagators.gary.basic;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;

/**Propagator that ensures that K nodes belong to the final graph
 * 
 * BEWARE : in theory a Minimum Hitting set problem should be solved to evaluate the lower bound of k
 * As it is NP Hard nothing is done yet. It may be good to have a greedy approach...?
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

	public PropKNodes(V graph, Solver sol, Constraint<V, Propagator<V>> constraint, IntVar k) {
		super((V[]) new GraphVar[]{graph}, sol, constraint, PropagatorPriority.LINEAR);
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
	public void propagate(int evtmask) throws ContradictionException {
		k.updateLowerBound(g.getKernelOrder(), this, false);
		k.updateUpperBound(g.getEnvelopOrder(), this, false);
		if(k.instantiated()){
			if(g.getEnvelopOrder()==g.getKernelOrder()){
				setPassive();
			}else{
				if(g.getEnvelopOrder()==k.getValue()){
					IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
					for (int node = env.getFirstElement(); node>=0; node = env.getNextElement()) {
						g.enforceNode(node, this, false);
					}
					setPassive();
				}else if(g.getKernelOrder()==k.getValue()){
					IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
					for (int node = env.getFirstElement(); node>=0; node = env.getNextElement()) {
						if(!g.getKernelGraph().getActiveNodes().isActive(node)){
							g.removeNode(node, this, false);
						}
					}
					setPassive();
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		k.updateLowerBound(g.getKernelOrder(), this, false);
		k.updateUpperBound(g.getEnvelopOrder(), this, false);
		if(k.instantiated()){
			if(g.getEnvelopOrder()==g.getKernelOrder()){
				setPassive();
			}else{
				if(g.getEnvelopOrder()==k.getValue()){
					IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
					for (int node = env.getFirstElement(); node>=0; node = env.getNextElement()) {
						g.enforceNode(node, this, false);
					}
					setPassive();
				}else if(g.getKernelOrder()==k.getValue()){
					IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
					for (int node = env.getFirstElement(); node>=0; node = env.getNextElement()) {
						if(!g.getKernelGraph().getActiveNodes().isActive(node)){
							g.removeNode(node, this, false);
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
		return EventType.REMOVENODE.mask + EventType.ENFORCENODE.mask + EventType.INSTANTIATE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
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
