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

package solver.variables.graph.undirectedGraph;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 7 févr. 2011
 */
public class UndirectedGraphVar extends GraphVar<StoredUndirectedGraph> {

	//////////////////////////////// GRAPH PART /////////////////////////////////////////
	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	
	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public UndirectedGraphVar(Solver solver, int nbNodes, GraphType typeEnv, GraphType typeKer) {
		super(solver);
    	envelop = new StoredUndirectedGraph(environment, nbNodes, typeEnv);
    	kernel = new StoredUndirectedGraph(environment, nbNodes, typeKer);
    	kernel.activeIdx.clear();
    }

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public boolean removeArc(int x, int y, ICause cause) throws ContradictionException {
    	if(kernel.edgeExists(x, y)){
    		this.contradiction(cause, "remove mandatory arc");
        	return false;
    	}
        if (envelop.removeEdge(x, y)){
        	if (reactOnModification){
        		delta.getArcRemovalDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
        	}
        	EventType e = EventType.REMOVEARC;
        	notifyPropagators(e, cause);
        	// A node has at least one arc/edge otherwise it is meaningless
        	if(getEnvelopGraph().getNeighborsOf(x).neighborhoodSize()==0){
        		removeNode(x, null);
        	}
        	if(getEnvelopGraph().getNeighborsOf(y).neighborhoodSize()==0){
        		removeNode(y, null);
        	}
        	return true;
        }return false;
    }
    public boolean enforceArc(int x, int y, ICause cause) throws ContradictionException {
    	enforceNode(x, cause);
    	enforceNode(y, cause);
    	if(envelop.edgeExists(x, y)){
        	if (kernel.addEdge(x, y)){
        		if (reactOnModification){
            		delta.getArcEnforcingDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
            	}
            	EventType e = EventType.ENFORCEARC;
            	notifyPropagators(e, cause);
            	return true;
        	}return false;
    	}
    	this.contradiction(cause, "enforce arc which is not in the domain");
    	return false;
    }
    
    //***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	@Override
	public StoredUndirectedGraph getKernelGraph() {
		return kernel;
	}

	@Override
	public StoredUndirectedGraph getEnvelopGraph() {
		return envelop;
	}
}
