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
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;
import java.util.BitSet;

/**Propagator that ensures that the final graph consists in K cliques
 * @author Jean-Guillaume Fages
 */
public class PropKCliques<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	public static long duration;
	private GraphVar g;
	private IntVar k;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKCliques(GraphVar graph, Solver solver, GraphConstraint constraint, IntVar k) {
		super((V[]) new Variable[]{graph,k}, solver, constraint, PropagatorPriority.LINEAR, false);//
		g = graph;
		this.k = k;
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		filter();
		duration = 0;
	}

	
	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		long time = System.currentTimeMillis();
		filter();
		duration += (System.currentTimeMillis()-time);
	}
	
	private void filter() throws ContradictionException{
		float n = g.getEnvelopGraph().getNbNodes();
		BitSet iter = new BitSet((int)n);
		IActiveNodes nodes = g.getKernelGraph().getActiveNodes();
		for (int i=nodes.getFirstElement();i>=0;i=nodes.getNextElement()){
				iter.set(i);
		}
		int idx = -1;
		INeighbors nei;
		int min = 0;
		while (iter.cardinality()>0){
			idx = iter.nextSetBit(idx+1);
			nei = g.getEnvelopGraph().getNeighborsOf(idx);
			iter.clear(idx);
			for(int j=nei.getFirstElement(); j>=0; j = nei.getNextElement()){
				iter.clear(j);
			}
			min ++;
		}
		k.updateLowerBound(min, this);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVENODE.mask +  EventType.REMOVEARC.mask +  EventType.ENFORCENODE.mask +  EventType.ENFORCEARC.mask + EventType.ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
		
}
