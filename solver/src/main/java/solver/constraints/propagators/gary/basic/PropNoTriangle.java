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
import choco.kernel.common.util.procedure.PairProcedure;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.IGraphDeltaMonitor;
import solver.variables.graph.GraphVar;
import solver.variables.setDataStructures.ISet;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.util.BitSet;

public class PropNoTriangle extends Propagator<UndirectedGraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private GraphVar g;
	private IGraphDeltaMonitor gdm;
	private BitSet toCompute;
	private TIntArrayList list;
	private PairProcedure arcEnf;
	private int n;


	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNoTriangle(UndirectedGraphVar graph, Constraint constraint, Solver solver) {
		super(new UndirectedGraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		gdm = g.monitorDelta(this);
		arcEnf = new EnfArc();
		toCompute = new BitSet(n);
		list = new TIntArrayList();
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		for(int i=0;i<n;i++){
			check(i);
		}
		gdm.unfreeze();
	}

	private void check(int i) throws ContradictionException {
		list.clear();
		ISet nei = g.getKernelGraph().getSuccessorsOf(i);
		for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
			list.add(j);
		}
		int nl = list.size();
		for(int j1=0;j1<nl;j1++){
			for(int j2=j1+1;j2<nl;j2++){
				if(g.getKernelGraph().getSuccessorsOf(list.get(j1)).contain(list.get(j2))){
					g.removeArc(list.get(j2),i,this);
				}
			}
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		toCompute.clear();
		gdm.freeze();
		gdm.forEachArc(arcEnf,EventType.ENFORCEARC);
		gdm.unfreeze();
		for(int i=toCompute.nextSetBit(0);i>=0;i=toCompute.nextSetBit(i+1)){
			check(i);
		}
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		if(!g.instantiated()){
			return ESat.UNDEFINED;
		}
		try{
			propagate(0);
			return ESat.TRUE;
		}catch (Exception e){
			return ESat.FALSE;
		}
	}

	private class EnfArc implements PairProcedure {
		@Override
		public void execute(int i, int j) throws ContradictionException {
			toCompute.set(i);
			toCompute.set(j);
		}
	}
}
