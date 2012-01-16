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

package solver.constraints.propagators.gary.constraintSpecific;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.directedGraph.DirectedGraphVar;

/**
 * @author Jean-Guillaume Fages
 * Ensures that each node in the kernel has exactly NLOOPS loops
 *
 */
public class PropNLoopsTree<V extends Variable> extends GraphPropagator<V>{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	DirectedGraphVar g;
	IntVar nLoops;
	IntProcedure removeProc, enforceProc;
	IStateInt nbKerLoop;
	IStateInt nbEnvLoop;
	IStateBool active;
	int n;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropNLoopsTree(DirectedGraphVar graph, IntVar nL, Solver sol, Constraint<V, Propagator<V>> constraint) {
		super((V[]) new Variable[]{graph,nL}, sol, constraint, PropagatorPriority.LINEAR);
		g = graph;
		n = g.getEnvelopGraph().getNbNodes();
		nLoops = nL;
		removeProc = new RemProc();
		enforceProc = new EnfLoop();
		nbEnvLoop = environment.makeInt();
		nbKerLoop = environment.makeInt();
		active = environment.makeBool(true);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		int ker = 0;
		int env = 0;
		for (int node = 0; node<n; node++) {
			if (g.getEnvelopGraph().arcExists(node, node)){
				env++;
				if (g.getKernelGraph().arcExists(node, node)){
					ker++;
				}
			}
		}
		nbEnvLoop.set(env);
		nbKerLoop.set(ker);
		nLoops.updateLowerBound(ker, this);
		nLoops.updateUpperBound(env, this);
		if(nLoops.getLB() == env && env!=ker){
			for (int node=0;node<n;node++) {
				if (g.getEnvelopGraph().arcExists(node, node)){
					g.enforceArc(node, node, this, false);
				}
			}
			nbKerLoop.set(env);
			nLoops.instantiateTo(env, this);
			active.set(false);
		}
		if(nLoops.getUB() == ker && env!=ker){
			for (int node=0;node<n;node++) {
				if (g.getEnvelopGraph().arcExists(node, node) && !g.getKernelGraph().arcExists(node, node)){
					g.removeArc(node, node, this);
				}
			}
			nbEnvLoop.set(ker);
			nLoops.instantiateTo(ker, this);
			active.set(false);
		}
	}

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(!active.get())return;

        Variable variable = vars[idxVarInProp];

        if(variable.getType() == Variable.GRAPH){
			if ((mask & EventType.REMOVEARC.mask) != 0){
                eventRecorder.getDeltaMonitor(g).forEach(removeProc, EventType.REMOVEARC);
			}
			if ((mask & EventType.ENFORCEARC.mask) != 0){
                eventRecorder.getDeltaMonitor(g).forEach(enforceProc, EventType.ENFORCEARC);
			}
			nLoops.updateUpperBound(nbEnvLoop.get(), this);
			nLoops.updateLowerBound(nbKerLoop.get(), this);
		}
		int env = nbEnvLoop.get();
		int ker = nbKerLoop.get();
		if(env!=ker){
			if(nLoops.getLB() == env){
				for (int node =0; node<n ; node++) {
					if (g.getEnvelopGraph().arcExists(node, node)){
						g.enforceArc(node, node, this, false);
					}
				}
				nbKerLoop.set(env);
				nLoops.instantiateTo(env, this);
				active.set(false);
			}else if(nLoops.getUB() == ker){
				for (int node = 0; node<n;node++) {
					if (g.getEnvelopGraph().arcExists(node, node) && !g.getKernelGraph().arcExists(node, node)){
						g.removeArc(node, node, this);
					}
				}
				nbEnvLoop.set(ker);
				nLoops.instantiateTo(ker, this);
				active.set(false);
			}
		}else {
			nLoops.instantiateTo(env,this);
			active.set(false);
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVEARC.mask+EventType.ENFORCEARC.mask+EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		if(g.instantiated() && nLoops.instantiated()){
			int nb=0;
			for(int i=0;i<n;i++){
				if(g.getEnvelopGraph().arcExists(i,i)){
					nb++;
				}
			}
			if(nb==nLoops.getValue()){
				return ESat.TRUE;
			}else{
				return ESat.FALSE;
			}
		}
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	/**
	 * Checks if a loop has been removed
	 */
	private class RemProc implements IntProcedure {

		public RemProc() {}

		@Override
		public void execute(int i) throws ContradictionException {
			if (i/n-1 == i%n){
				nbEnvLoop.add(-1);
			}
		}
	}

	/**
	 * Checks if a loop has been enforced
	 */
	private class EnfLoop implements IntProcedure {

		public EnfLoop() {}

		@Override
		public void execute(int i) throws ContradictionException {
			if (i/n-1 == i%n){
				nbKerLoop.add(1);
			}
		}
	}
}
