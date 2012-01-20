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
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class IndexOf extends GraphRelation<IntVar> {
	
	protected IndexOf(IntVar[] vars) {
		super(vars);
	}

	@Override
	public ESat isEntail(int var1, int var2) {
		IntVar x = vars[var1];
		if(!x.contains(var2)){
			return ESat.FALSE;
		}
		if(x.instantiated()){
			return ESat.TRUE;
		}
		return ESat.UNDEFINED;
	}
	
	@Override
	public void applyTrue(int var1, int var2, Solver solver, ICause cause) throws ContradictionException {
		vars[var1].instantiateTo(var2,cause);
	}
	
	@Override
	public void applyFalse(int var1, int var2, Solver solver, ICause cause) throws ContradictionException {
		vars[var1].removeValue(var2, cause);
	}

	@Override
	public boolean isDirected() {
		return true;
	}
	
	@Override
	public GraphProperty[] getGraphProperties() {
		return new GraphProperty[]{GraphProperty.ONE_SUCCESSORS_PER_NODE};
//		return new GraphProperty[]{};
	}
	
	/**create the initial graph representing the relation between input variables 
	 * @param inputVars
	 * @param solver
	 * @return the initial relational graph 
	 */
	public GraphVar generateInitialGraph(Variable[] inputVars, Solver solver){
		int n = vars.length;
		if (isDirected()){
			DirectedGraphVar g = new DirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
			for(int i=0;i<n;i++){
				for(int j=0;j<n;j++){
					if(isEntail(i,j) != ESat.FALSE){
						g.getEnvelopGraph().addArc(i, j);
					}
				}
			}
			return g;
		}throw new UnsupportedOperationException("error ");
	}
}
