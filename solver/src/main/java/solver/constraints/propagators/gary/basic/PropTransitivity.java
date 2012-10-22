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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.GraphVar;
import solver.variables.setDataStructures.ISet;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**Propagator that ensures that the relation of the graph is transitive : (a,b) + (b,c) => (a,c)
 * @author Jean-Guillaume Fages
 */
public class PropTransitivity<V extends GraphVar> extends Propagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private V g;
    GraphDeltaMonitor gdm;
	private PairProcedure arcEnforced;
	private PairProcedure arcRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropTransitivity(V graph, Solver solver, Constraint constraint) {
		super((V[]) new GraphVar[]{graph}, solver, constraint, PropagatorPriority.LINEAR);
		g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
		arcEnforced = new PairProcedure() {
			@Override
			public void execute(int from, int to) throws ContradictionException {
				enfArc(from,to);
			}
		};
		arcRemoved = new PairProcedure() {
			@Override
			public void execute(int from, int to) throws ContradictionException {
				remArc(from,to);
			}
		};
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int n = g.getEnvelopGraph().getNbNodes();
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(g.getKernelGraph().arcExists(i,j)){
					enfArc(i,j);
				}else if(!g.getEnvelopGraph().arcExists(i,j)){
					remArc(i,j);
				}
			}
		}
//		gdm.unfreeze();
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		gdm.freeze();
		if ((mask & EventType.ENFORCEARC.mask) != 0) {
			gdm.forEachArc(arcEnforced, EventType.ENFORCEARC);
		}
		if ((mask & EventType.REMOVEARC.mask) != 0) {
			gdm.forEachArc(arcRemoved, EventType.REMOVEARC);
		}
        gdm.unfreeze();
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask +  EventType.ENFORCEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURE
	//***********************************************************************************
	// --- Arc enforcings
	private void enfArc(int node, int succ) throws ContradictionException {
		if(node != succ){
			ISet ker = g.getKernelGraph().getPredecessorsOf(node);
			ISet env = g.getEnvelopGraph().getPredecessorsOf(node);
			for(int i=env.getFirstElement(); i>=0; i = env.getNextElement()){
				if(ker.contain(i)){
					g.enforceArc(i, succ, this);
				}else if(!g.getEnvelopGraph().arcExists(i,succ)){
					g.removeArc(i,node,this);
				}
			}
			ker = g.getKernelGraph().getSuccessorsOf(succ);
			env = g.getEnvelopGraph().getSuccessorsOf(succ);
			for(int i=env.getFirstElement(); i>=0; i = env.getNextElement()){
				if(ker.contain(i)){
					g.enforceArc(node,i, this);
				}else if(!g.getEnvelopGraph().arcExists(node,i)){
					g.removeArc(succ,i,this);
				}
			}
		}
	}

	// --- Arc removals
	private void remArc(int from, int to) throws ContradictionException {
		if(from != to){
			ISet nei = g.getKernelGraph().getSuccessorsOf(from);
			for(int i=nei.getFirstElement(); i>=0; i = nei.getNextElement()){
				g.removeArc(i, to,this);
			}
			nei = g.getKernelGraph().getPredecessorsOf(to);
			for(int i=nei.getFirstElement(); i>=0; i = nei.getNextElement()){
				g.removeArc(from,i,this);
			}
		}
	}
}
