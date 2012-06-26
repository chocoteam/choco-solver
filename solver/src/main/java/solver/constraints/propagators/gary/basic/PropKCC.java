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
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;

/**Propagator that ensures that the final graph consists in K Connected Components (CC)
 *
 * simple checker (runs in linear time)
 * 
 * @author Jean-Guillaume Fages
 */
public class PropKCC extends Propagator{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IntVar k;
	private ConnectivityFinder env_CC_finder, ker_CC_finder;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKCC(GraphVar graph, Solver solver, Constraint constraint, IntVar k) {
		super(new Variable[]{graph,k}, solver, constraint, PropagatorPriority.LINEAR);
		this.g = graph;
		this.k = k;
		env_CC_finder = new ConnectivityFinder(g.getEnvelopGraph());
		ker_CC_finder = new ConnectivityFinder(g.getKernelGraph());
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
//		if(k.getUB()==1){
//			if(!env_CC_finder.isConnectedAndFindIsthma()){
//				contradiction(g,"");
//			}
//			int nbIsma = env_CC_finder.isthmusFrom.size();
//			for(int i=0;i<nbIsma;i++){
//				g.enforceArc(env_CC_finder.isthmusFrom.get(i),env_CC_finder.isthmusTo.get(i),this);
//			}
//		}else{
			env_CC_finder.findAllCC();
			int ee = env_CC_finder.getNBCC();
			k.updateLowerBound(ee,this);
			if(g.instantiated()){
				k.updateUpperBound(ee,this);
			}
			else if(g.getEnvelopOrder()==g.getKernelOrder()){
				ker_CC_finder.findAllCC();
				int ke = ker_CC_finder.getNBCC();
				k.updateUpperBound(ke,this);
			}
//		}
	}

	
	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		propagate(0);
		// todo incremental behavior
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVENODE.mask +  EventType.REMOVEARC.mask +  EventType.ENFORCENODE.mask +  EventType.ENFORCEARC.mask + EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		env_CC_finder.findAllCC();
		int ee = env_CC_finder.getNBCC();
		if(k.getUB()<ee){
			return ESat.FALSE;
		}
		if(g.instantiated()){
			if(k.contains(ee)){
				if(k.instantiated()){
					return ESat.TRUE;
				}else{
					return ESat.UNDEFINED;
				}
			}
			return ESat.FALSE;
		}
		return ESat.UNDEFINED;
	}
	
}
