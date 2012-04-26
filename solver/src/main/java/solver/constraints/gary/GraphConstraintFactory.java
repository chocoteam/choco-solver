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
package solver.constraints.gary;

import solver.Solver;
import solver.constraints.gary.relations.GraphRelation;
import solver.constraints.gary.relations.GraphRelationFactory;
import solver.constraints.propagators.gary.PropKCliques;
import solver.constraints.propagators.gary.PropTransitivity;
import solver.variables.IntVar;
import solver.variables.MetaVariable;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**Predefined graph constraints
 * @author Jean-Guillaume Fages
 *
 */
public class GraphConstraintFactory {

	// SAFE

	public static GraphConstraint nCliques(UndirectedGraphVar graph, IntVar nCliques, Solver solver) {
		GraphConstraint gc = makeConstraint(graph,solver);
		gc.addAdHocProp(new PropTransitivity(graph,solver, gc));
		gc.addAdHocProp(new PropKCliques(graph,solver, gc, nCliques));
		return gc;
	}


	// UNSAFE

	/** Anti arborescence partitioning constraint
	 * also known as tree constraint
	 * GAC in (almost) linear time : O(alpha.m) 
	 * roots are loops
	 * @param vars successor variables
	 * @param NVar number of anti arborescences
	 * @param solver
	 * @return tree constraint
	 */
	public static GraphConstraint nTrees(IntVar[] vars, IntVar NVar, Solver solver) {
		GraphRelation relation = GraphRelationFactory.indexOf(vars);
		GraphConstraint tree = makeConstraint(vars, relation, solver);
		tree.addProperty(GraphProperty.K_ANTI_ARBORESCENCES, NVar);
		return tree;
	}
	
	/**There will be exactly NVal distinct values in the collection of variables (integers,vectors,tasks,sets...) vars 
	 * It is generic!
	 * @param vars variables (nodes in the graph), they must have the same type
	 * @param NVal number of distinct values
	 * @param solver 
	 * @return a generic nValue constraint NOT RESTRICTED TO INTEGERS
	 */
	public static GraphConstraint nValues(Variable[] vars, IntVar NVal, Solver solver) {
		GraphRelation relation = GraphRelationFactory.equivalence(vars);
		GraphConstraint nEq = makeConstraint(vars, relation, solver);
		nEq.addProperty(GraphProperty.K_CLIQUES, NVal);
		return nEq;
	}
	
	/**There will be exactly NVal distinct integer values in the collection of integer variables vars 
	 * @param vars variables (nodes in the graph)
	 * @param NInt number of distinct integer values
	 * @param solver
	 * @return classical nValues constraint (for integers)
	 */
	public static GraphConstraint nIntegers(IntVar[] vars, IntVar NInt, Solver solver) {
		return nValues(vars, NInt, solver); // TODO find and add 1-dim properties
	}
	
	/**There will be exactly NVal distinct vectors in the collection of vector variables vars 
	 * @param vars variables (nodes in the graph)
	 * @param NVect number of distinct vectors
	 * @param solver
	 * @return nVectors constraint
	 */
	public static GraphConstraint nVectors(MetaVariable[] vars, IntVar NVect, Solver solver) {
		return nValues(vars, NVect, solver);
	}
	
	/**There will be exactly NVar integer variables that take value in the collection values
	 * @param vars integer variables (nodes in the graph)
	 * @param NVar number of variables that take value in values
	 * @param values collection of values
	 * @param solver
	 * @return among constraint for integer variables
	 */
	public static GraphConstraint among(IntVar[] vars, IntVar NVar, int[] values, Solver solver) {
		GraphRelation relation = GraphRelationFactory.member(vars, values);
		GraphConstraint among = makeConstraint(vars, relation, solver);
		among.addProperty(GraphProperty.K_NODES, NVar);
		return among;
	}
	
	/** Create a generic graph constraint
	 * @param vars variables represented by nodes
	 * @param relation meaning of an arc/edge
	 * @param solver
	 * @return a generic graph constraint
	 */
	public static GraphConstraint makeConstraint(Variable[] vars, GraphRelation relation, Solver solver) {
		return new GraphConstraint (vars, solver, relation);
	}

	/** Create a generic graph constraint
	 * @param g graph variable
	 * @param solver
	 * @return a generic graph constraint
	 */
	public static GraphConstraint makeConstraint(GraphVar g, Solver solver) {
		return new GraphConstraint (g, solver);
	}
}
