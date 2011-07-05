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
import choco.kernel.memory.IEnvironment;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.requests.IRequest;

/**Propagator that ensures that K edges belong to the final undirected graph
 * 
 * @author Jean-Guillaume Fages
 *
 */
public class PropKArcsUndi<V extends Variable, G extends UndirectedGraphVar> extends PropKArcs<V,G>{


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKArcsUndi(G graph, Solver sol, Constraint<V, Propagator<V>> constraint, IntVar k) {
		super(graph, sol, constraint, k);
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
		INeighbors nei;
		for (int i=ker.getFirstElement();i>=0;i=ker.getNextElement()){
			min += g.getKernelGraph().getNeighborsOf(i).neighborhoodSize();
		}
		for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
			max += g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize();
		}
		min = min / 2;
		max = max / 2;
		k.updateLowerBound(min, this);
		k.updateUpperBound(max, this);
		nbInEnv.set(max);
		nbInKer.set(min);
		if(k.instantiated()){
			if(min==max){
				setPassive();
			}else{
				if(max==k.getValue()){
					for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
						nei = g.getEnvelopGraph().getNeighborsOf(i);
						for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
							g.enforceArc(i, j, this);
						}
					}
					setPassive();
				}else if(min==k.getValue()){
					INeighbors kernei;
					for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
						nei = g.getEnvelopGraph().getNeighborsOf(i);
						kernei = g.getKernelGraph().getNeighborsOf(i);
						for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
							if(!kernei.contain(j)){
								g.removeArc(i, j, this);
							}
						}
					}
					setPassive();
				}
			}
		}
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		super.propagateOnRequest(request, idxVarInProp, mask);
		if(k.instantiated()){
			if(nbInKer.get()==nbInEnv.get()){
				setPassive();
			}else{
				IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
				INeighbors nei;
				if(nbInEnv.get()==k.getValue()){
					for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
						nei = g.getEnvelopGraph().getNeighborsOf(i);
						for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
							g.enforceArc(i, j, this);
						}
					}
					setPassive();
				}else if(nbInKer.get()==k.getValue()){
					INeighbors kernei;
					for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
						nei = g.getEnvelopGraph().getNeighborsOf(i);
						kernei = g.getKernelGraph().getNeighborsOf(i);
						for(int j=nei.getFirstElement(); j>=0; j=nei.getNextElement()){
							if(!kernei.contain(j)){
								g.removeArc(i, j, this);
							}
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
		return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.INSTANTIATE.mask;
	}

	@Override
	public ESat isEntailed() {
		int min = 0;
		int max = 0;
		IActiveNodes ker = g.getKernelGraph().getActiveNodes();
		IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
		for (int i=ker.getFirstElement();i>=0;i=ker.getNextElement()){
			min += g.getKernelGraph().getNeighborsOf(i).neighborhoodSize();
		}
		for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
			max += g.getEnvelopGraph().getNeighborsOf(i).neighborhoodSize();
		}
		min = min / 2;
		max = max / 2;
		if(k.getLB()>max || k.getUB()<min){
			return ESat.FALSE;
		}
		if(min!=max || !k.instantiated()){
			return ESat.UNDEFINED;
		}
		return ESat.TRUE;
	}
}
