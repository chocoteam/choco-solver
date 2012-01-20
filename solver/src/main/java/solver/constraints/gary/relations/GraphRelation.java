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
package solver.constraints.gary.relations;

import choco.kernel.ESat;
import solver.ICause;
import solver.Solver;
import solver.constraints.gary.GraphProperty;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

public abstract class GraphRelation<V extends Variable> {

	protected V[] vars;

	protected GraphRelation(V[] vars){
		this.vars = vars.clone();
	}

	// --- Services 
	/**Check the consistency of the relation xRy
	 * @param x index of node/var
	 * @param y index of node/var
	 * @return TRUE iff the relation is necessarly true, FALSE iff the relation is necessarly false or UNDEFINED otherwise
	 */
	public abstract ESat isEntail(int x, int y);
	
	/** Apply the filtering defined by xRy
	 *
     * @param x index of node/var
     * @param y index of node/var
     * @param solver
     * @param cause
     * @throws ContradictionException
	 */
	public abstract void applyTrue(int x, int y, Solver solver, ICause cause) throws ContradictionException;
	
	/** Apply the filtering defined by !(xRy) so it apply the filtering of the opposite relation : x(!R)y
	 *
     * @param x index of node/var
     * @param y index of node/var
     * @param solver
     * @param cause
     * @throws ContradictionException
	 */
	public abstract void applyFalse(int x, int y, Solver solver, ICause cause) throws ContradictionException;
	

	/** say !(xRy) AND !(yRx)
	 * TO USE ONLY WHEN A DIRECTED META RELATION COMPOSED OF AT LEAST ONE UNDIRECTED RELATION IS USED
	 * Apply the filtering defined by !(xRy) so it apply the filtering of the opposite relation : x(!R)y
	 *
     * @param x index of node/var
     * @param y index of node/var
     * @param solver
     * @param cause
     * @throws ContradictionException
	 */
	public void applySymmetricFalse(int x, int y, Solver solver, ICause cause) throws ContradictionException {
		applyFalse(x, y, solver, cause);
	}

	/**
	 * @return true iff the relation is not symmetric : the corresponding graph should be directed. 
	 * false <=> R is symmetric <=> undirected graph
	 */
	public abstract boolean isDirected();

	/**
	 * @return Graph properties due to the relation itself
	 * Should not include properties ensured by the generation of the graph!
	 */
	public abstract GraphProperty[] getGraphProperties();
	
	/**
	 * @return the artity of the relation (binary by default but can be unary)
	 */
	public PropagatorPriority getPriority(){ 
		return PropagatorPriority.BINARY;   // by default but can be unary if only loops are enabled
	}
	
	/**create the initial graph representing the relation between input variables 
	 * @param inputVars
	 * @param solver
	 * @return the initial relational graph 
	 */
	public GraphVar generateInitialGraph(Variable[] inputVars, Solver solver){
		int n = vars.length;
		if (isDirected()){
			DirectedGraphVar g = new DirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.LINKED_LIST);
			for(int i=0;i<n;i++){
				for(int j=0;j<n;j++){
					if(isEntail(i,j) != ESat.FALSE){
						g.getEnvelopGraph().addArc(i, j);
					}
				}
			}
			return g;
		}else{
			UndirectedGraphVar g = new UndirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.LINKED_LIST);
			for(int i=0;i<n;i++){
				for(int j=i;j<n;j++){
					if(isEntail(i,j) != ESat.FALSE){
						g.getEnvelopGraph().addEdge(i, j);
					}
				}
			}
			return g;
		}
	}
	
	protected ESat and(ESat e1, ESat e2){
		if(e1 == ESat.FALSE || e2 == ESat.FALSE){
			return ESat.FALSE;
		}
		if(e1 == ESat.TRUE && e2 == ESat.TRUE){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
}
