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
import solver.constraints.Constraint;
import solver.constraints.propagators.gary.PropKCliques;
import solver.constraints.propagators.gary.basic.PropTransitivity;
import solver.constraints.propagators.gary.constraintSpecific.PropNLoopsTree;
import solver.constraints.propagators.gary.constraintSpecific.PropNTree;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtMost;
import solver.variables.IntVar;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * Some usual graph constraints
 * @author Jean-Guillaume Fages
 *
 */
public class GraphConstraintFactory {

	/** Create a generic empty constraint
	 * @param solver
	 * @return a generic empty constraint
	 */
	public static Constraint makeConstraint(Solver solver) {
		return new Constraint(solver);
	}

	public static Constraint nCliques(UndirectedGraphVar graph, IntVar nCliques, Solver solver) {
		Constraint gc = makeConstraint(solver);
		gc.addPropagators(new PropTransitivity(graph, solver, gc));
		gc.addPropagators(new PropKCliques(graph, solver, gc, nCliques));
		return gc;
	}

	/** Anti arborescence partitioning constraint (CP'11)
	 * also known as tree constraint
	 * GAC in (almost) linear time : O(alpha.m)
	 * roots are loops
	 * @param graph
	 * @param n number of anti arborescences
	 * @param solver
	 * @return tree constraint
	 */
	public static Constraint nTrees(DirectedGraphVar graph, IntVar n, Solver solver) {
		Constraint tree = makeConstraint(solver);
		tree.addPropagators(new PropNodeDegree_AtLeast(graph, GraphVar.IncidentNodes.SUCCESSORS, 1, tree, solver));
		tree.addPropagators(new PropNodeDegree_AtMost(graph, GraphVar.IncidentNodes.SUCCESSORS, 1, tree, solver));
		tree.addPropagators(new PropNLoopsTree(graph, n, solver, tree));
		tree.addPropagators(new PropNTree(graph,n,solver,tree));
		return tree;
	}
}
