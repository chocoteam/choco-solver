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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.gary.tsp;

import choco.annotations.PropAnn;
import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * */
@PropAnn(tested=PropAnn.Status.BENCHMARK)
public class PropCyclePathChanneling extends GraphPropagator<GraphVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	UndirectedGraphVar undir;
	DirectedGraphVar dir;
	int nUndir;
	int nDir;
	private IntProcedure arcEnforced,edgeEnforced,arcRemoved,edgeRemoved;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropCyclePathChanneling(DirectedGraphVar dir, UndirectedGraphVar undir, Constraint<GraphVar, Propagator<GraphVar>> constraint, Solver solver) {
		super(new GraphVar[]{dir,undir}, solver, constraint, PropagatorPriority.LINEAR);
		this.dir = dir;
		this.undir = undir;
		nDir = dir.getEnvelopGraph().getNbNodes();
		nUndir = undir.getEnvelopGraph().getNbNodes();
		if(nDir!=nUndir+1){
			throw new UnsupportedOperationException();
		}
		arcEnforced = new EnfArc(this);
		edgeEnforced = new EnfEdge(this);
		arcRemoved = new RemArc(this);
		edgeRemoved = new RemEdge(this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		//TODO
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		if(ALWAYS_COARSE){
			propagate(0);return;
		}
		if(idxVarInProp==0){
			eventRecorder.getDeltaMonitor(dir).forEach(arcEnforced, EventType.ENFORCEARC);
			eventRecorder.getDeltaMonitor(dir).forEach(arcRemoved, EventType.REMOVEARC);
		}else{
			eventRecorder.getDeltaMonitor(undir).forEach(edgeEnforced, EventType.ENFORCEARC);
			eventRecorder.getDeltaMonitor(undir).forEach(edgeRemoved, EventType.REMOVEARC);
		}
	}

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ENFORCEARC.mask + EventType.REMOVEARC.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private class EnfEdge implements IntProcedure {
		Propagator p;
		private EnfEdge(Propagator prop){
			p = prop;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/nUndir-1;
			int to = i%nUndir;
			if(from==0){
				if(dir.getEnvelopGraph().arcExists(0,to)){
					if(!dir.getEnvelopGraph().arcExists(to,nDir-1)){
						dir.enforceArc(from,to,p);
					}
				}else{
					dir.enforceArc(to,nDir-1,p);
				}
			}else if(to==0){
				if(dir.getEnvelopGraph().arcExists(from,nDir-1)){
					if(!dir.getEnvelopGraph().arcExists(to,from)){
						dir.enforceArc(from,nDir-1,p);
					}
				}else{
					dir.enforceArc(to,from,p);
				}
			}else{
				if(dir.getEnvelopGraph().arcExists(from,to)){
					if(!dir.getEnvelopGraph().arcExists(to,from)){
						dir.enforceArc(from,to,p);
					}
				}else{
					dir.enforceArc(to,from,p);
				}
			}
		}
	}
	private class EnfArc implements IntProcedure {
		Propagator p;
		private EnfArc(Propagator prop){
			p = prop;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/nDir-1;
			int to = i%nDir;
			if(to==nDir-1){
				undir.enforceArc(from,0,p);
			}else{
				undir.enforceArc(from,to,p);
			}
		}
	}
	private class RemEdge implements IntProcedure {
		Propagator p;
		private RemEdge(Propagator prop){
			p = prop;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/nUndir-1;
			int to = i%nUndir;
			if(from==0){
				dir.removeArc(0,to,p);
				dir.removeArc(to,nDir-1,p);
			}else if(to==0){
				dir.removeArc(from,nDir-1,p);
				dir.removeArc(0,from,p);
			}else{
				dir.removeArc(from,to,p);
				dir.removeArc(to,from,p);
			}
		}
	}
	private class RemArc implements IntProcedure {
		Propagator p;
		private RemArc(Propagator prop){
			p = prop;
		}
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/nDir-1;
			int to = i%nDir;
			if(from==0){
				if(!dir.getEnvelopGraph().arcExists(to,nDir-1)){
					undir.removeArc(from,to,p);
				}
			}else if(to==nDir-1){
				if(!dir.getEnvelopGraph().arcExists(0,from)){
					undir.removeArc(from,0,p);
				}
			}else{
				if(!dir.getEnvelopGraph().arcExists(to,from)){
					undir.removeArc(from,to,p);
				}
			}
		}
	}
}
