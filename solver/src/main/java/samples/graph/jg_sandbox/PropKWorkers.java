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

package samples.graph.jg_sandbox;

import choco.kernel.ESat;
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;

public class PropKWorkers<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IntVar k;
	int n,firstTaskIndex;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropKWorkers(GraphVar graph, int firstTaskIdx,IntVar k, GraphConstraint constraint,Solver solver) {
		super((V[]) new Variable[]{graph,k}, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		this.k = k;
		n = g.getEnvelopGraph().getNbNodes();
		firstTaskIndex = firstTaskIdx;
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		filter();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		filter();
	}

	private void filter() throws ContradictionException{
		int nbK = 0;
		int nbE = 0;
		int nbKNotWorking = 0;
		IActiveNodes env = g.getEnvelopGraph().getActiveNodes();
		IActiveNodes ker = g.getKernelGraph().getActiveNodes();
		for(int i=env.getFirstElement();i>=0 && i<firstTaskIndex;i=env.getNextElement()){
			nbE++;
			if(ker.isActive(i)){
				nbK++;
				if(g.getKernelGraph().getNeighborsOf(i).isEmpty()){
					nbKNotWorking++;
				}
			}
		}
		INeighbors nei;
		int nbNotAssigned = 0;
		for(int i=firstTaskIndex;i<n;i++){
			nei = g.getKernelGraph().getNeighborsOf(i);
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(j<firstTaskIndex){
					nbNotAssigned--;break;
				}
			}
			nbNotAssigned++;
		}
		if(nbNotAssigned<nbKNotWorking ){
			contradiction(g,"");
		}
		if(nbNotAssigned==nbKNotWorking ){
			for(int i=env.getFirstElement();i>=0;i=env.getNextElement()){
				if(!ker.isActive(i)){
					g.removeNode(i,this);
					nbE--;
				}
			}
		}
		k.updateLowerBound(nbK,this);
		k.updateUpperBound(nbE,this);
		if(nbK!=nbE && k.instantiated()){
			if(k.getValue()==nbK){
				for(int i=env.getFirstElement();i>=0 && i<firstTaskIndex;i=env.getNextElement()){
					if(!ker.isActive(i)){
						g.removeNode(i,this);
					}
				}
			}
			else if(k.getValue()==nbE){
				for(int i=env.getFirstElement();i>=0 && i<firstTaskIndex;i=env.getNextElement()){
					if(!ker.isActive(i)){
						g.enforceNode(i,this);
					}
				}
			}
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVENODE.mask + EventType.ENFORCENODE.mask + EventType.INSTANTIATE.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}
}
