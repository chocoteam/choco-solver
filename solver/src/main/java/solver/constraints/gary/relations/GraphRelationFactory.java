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

import solver.variables.CustomerVisitVariable;
import solver.variables.IntVar;
import solver.variables.MetaVariable;
import solver.variables.Variable;


public class GraphRelationFactory {

	/** Represents arcs by equivalence (equality) relationships R
	 * 
	 * Math : reflexive, transitive and symmetric : xRx, xRy & yRz => xRz, xRy => yRx
	 * Graph: clique partitioning (each node has a loop, (x,y) & (y,z)=>(x,z) and the graph is undirected)
	 * @param vars array of variables ordered as it is in their corresponding graph : to vars[k] corresponds node k in the relation graph
	 * @return a relation representing arcs as equalities between variables (nodes)
	 */
	public static GraphRelation equivalence(Variable[] vars){
		if (vars instanceof IntVar[]){
			return new Eq_Int((IntVar[])vars);
		}
		if (vars instanceof MetaVariable[]){
			return new Eq_IntVector((MetaVariable[])vars);
		}
		throw new UnsupportedOperationException("you must define the equivalence relationship for this kind of variable");
	}
	
	
	/** Uses member relation : 
	 * 
	 * A loop (x,x) means that the variable x takes its value in the given array values
	 * An arc (x,y), x/=y, is meaningless and thus forbidden
	 * 
	 * Graph: subgraph problem / singleton partitioning (the graph is undirected, each node has a single loop (no other incident edge))
	 * @param vars array of variables ordered as it is in their corresponding graph : to vars[k] corresponds node k in the relation graph
	 * @param values array of integer values 
	 * @return a relation representing arcs as member constraints on variables (nodes)
	 */
	public static GraphRelation member(IntVar[] vars, int[] values){
		return new Member(vars, values);
	}
	
	/** Uses IndexOf relation : 
	 * 
	 * arc (x,y) <=> vars[x] = y 
	 * 
	 * Graph: mainly for tree partitioning (the graph is directed, each node has exactly one successor in the final graph)
	 * @param vars array of variables ordered as it is in their corresponding graph : to vars[k] corresponds node k in the relation graph
	 * @return a relation representing arcs as variable value pairs
	 */
	public static GraphRelation indexOf(IntVar[] vars){
		return new IndexOf(vars);
	}
	
	public static GraphRelation customerVisit(CustomerVisitVariable[] vars, int[][] distancesMatrix){
		return new CustomerVisitRelation(vars, distancesMatrix);
	}
	public static GraphRelation distanceEq(IntVar[] vars, int[][] distancesMatrix){
		return new Dist_Int(vars, distancesMatrix);
	}
	public static GraphRelation distanceLeq(IntVar[] vars, int[][] distancesMatrix){
		return new XplusC_Leq_Y_Int(vars, distancesMatrix);
	}
}
