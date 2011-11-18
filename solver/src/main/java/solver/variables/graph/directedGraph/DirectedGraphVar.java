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

package solver.variables.graph.directedGraph;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 7 f�vr. 2011
 */
public class DirectedGraphVar extends GraphVar<StoredDirectedGraph> {

	////////////////////////////////// GRAPH PART ///////////////////////////////////////
	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public DirectedGraphVar(Solver solver, int nbNodes, GraphType typeEnv, GraphType typeKer) {
		super(solver);
		envelop = new StoredDirectedGraph(environment, nbNodes, typeEnv);
		kernel = new StoredDirectedGraph(environment, nbNodes, typeKer);
		kernel.getActiveNodes().clear();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public boolean removeArc(int x, int y, ICause cause, boolean informCause) throws ContradictionException {
		if(kernel.arcExists(x, y)){
			this.contradiction(cause, EventType.REMOVEARC, "remove mandatory arc");
			return false;
		}
		if (envelop.removeArc(x, y)){
			if (reactOnModification){
				delta.getArcRemovalDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
			}
			EventType e = EventType.REMOVEARC;
			notifyMonitors(e, cause);
			if(getEnvelopGraph().getPredecessorsOf(x).neighborhoodSize()==0 && getEnvelopGraph().getSuccessorsOf(x).neighborhoodSize()==0){
				removeNode(x, cause, informCause);
			}
			if(getEnvelopGraph().getPredecessorsOf(y).neighborhoodSize()==0 && getEnvelopGraph().getSuccessorsOf(y).neighborhoodSize()==0){
				removeNode(y, cause, informCause);
			}
			return true;
		}return false;
	}
	@Override
	public boolean enforceArc(int x, int y, ICause cause, boolean informCause) throws ContradictionException {
		enforceNode(x, cause, informCause);
		enforceNode(y, cause, informCause);
		if(envelop.arcExists(x, y)){
			if (kernel.addArc(x, y)){
				if (reactOnModification){
					delta.getArcEnforcingDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
				}
				EventType e = EventType.ENFORCEARC;
				notifyMonitors(e, cause);
				return true;
			}return false;
		}
		this.contradiction(cause, EventType.ENFORCEARC, "enforce arc which is not in the domain");
		return false;
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	@Override
	public StoredDirectedGraph getKernelGraph() {
		return kernel;
	}

	@Override
	public StoredDirectedGraph getEnvelopGraph() {
		return envelop;
	} 

	@Override
	public int getType() {
		return Variable.GRAPH;
	}
	
	@Override
	public boolean isDirected(){
		return true;
	}
}
