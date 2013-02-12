/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.constraints.gary;

import org.easymock.internal.matchers.Or;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.gary.arborescences.PropAntiArborescence;
import solver.constraints.propagators.gary.arborescences.PropArborescence;
import solver.constraints.propagators.gary.basic.PropKCC;
import solver.constraints.propagators.gary.basic.PropKCliques;
import solver.constraints.propagators.gary.basic.PropTransitivity;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtMost;
import solver.constraints.propagators.gary.path.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.path.PropPathNoCycle;
import solver.constraints.propagators.gary.path.PropReducedPath;
import solver.constraints.propagators.gary.path.PropSCCDoorsRules;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.lagrangianRelaxation.PropLagr_OneTree;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.GraphVar;
import solver.variables.graph.UndirectedGraphVar;

/**
 * Some usual graph constraints
 *
 * @author Jean-Guillaume Fages
 */
public class GraphConstraintFactory {

    /**
     * partition a graph variable into nCliques cliques
     *
     * @param GRAPHVAR   graph variable partitioned into cliques
     * @param NB_CLIQUES expected number of cliques
     * @return a constraint which partitions GRAPHVAR into NB_CLIQUES cliques
     */
    public static Constraint nCliques(UndirectedGraphVar GRAPHVAR, IntVar NB_CLIQUES) {
        Solver solver = GRAPHVAR.getSolver();
        Constraint gc = new Constraint(new Variable[]{GRAPHVAR, NB_CLIQUES}, solver);
        gc.addPropagators(new PropTransitivity(GRAPHVAR));
        gc.addPropagators(new PropKCliques(GRAPHVAR, NB_CLIQUES));
        gc.addPropagators(new PropKCC(GRAPHVAR, NB_CLIQUES));
        return gc;
    }

    /**
     * Constraint modeling the Traveling Salesman Problem
     *
     * @param GRAPHVAR   graph variable representing a Hamiltonian cycle
     * @param COSTVAR    variable representing the cost of the cycle
     * @param EDGE_COSTS cost matrix (should be symmetric)
     * @param HELD_KARP  use the Lagrangian relaxation of the tsp
     *                   described by Held and Karp
     *                   {0:noHK,1:HK,2:HK but wait a first solution before running it}
     * @return a tsp constraint
     */
    public static Constraint tsp(UndirectedGraphVar GRAPHVAR, IntVar COSTVAR, int[][] EDGE_COSTS, int HELD_KARP) {
        Constraint gc = hamiltonianCycle(GRAPHVAR);
        gc.addPropagators(new PropCycleEvalObj(GRAPHVAR, COSTVAR, EDGE_COSTS));
        if (HELD_KARP > 0) {
            PropLagr_OneTree hk = PropLagr_OneTree.oneTreeBasedRelaxation(GRAPHVAR, COSTVAR, EDGE_COSTS);
            hk.waitFirstSolution(HELD_KARP == 2);
            gc.addPropagators(hk);
        }
        return gc;
    }

    /**
     * GRAPHVAR must form a Hamiltonian cycle
	 * <p/> Filtering algorithms are incremental and run in O(1) per enforced/removed edge.
	 * <p/> Subtour elimination is an undirected adaptation of the
	 * nocycle constraint of Caseau & Laburthe in Solving small TSPs with Constraints.
     *
     * @param GRAPHVAR
     * @return a hamiltonian cycle constraint
     */
    public static Constraint hamiltonianCycle(UndirectedGraphVar GRAPHVAR) {
        Solver solver = GRAPHVAR.getSolver();
        Constraint gc = new Constraint(new Variable[]{GRAPHVAR}, solver);
        gc.addPropagators(new PropNodeDegree_AtLeast(GRAPHVAR, 2));
        gc.addPropagators(new PropNodeDegree_AtMost(GRAPHVAR, 2));
        gc.addPropagators(new PropCycleNoSubtour(GRAPHVAR));
        return gc;
    }

    /**
     * GRAPHVAR must form a Hamiltonian path from ORIGIN to DESTINATION.
     * <p/> Basic filtering algorithms are incremental and run in O(1) per enforced/removed arc.
	 * <p/> Subtour elimination is the nocycle constraint of Caseau & Laburthe in Solving small TSPs with Constraints.
	 *
	 * <p/> Assumes that ORIGIN has no predecessor, DESTINATION has no successor and each node is mandatory.
	 *
     * @param GRAPHVAR    variable representing a path
     * @param ORIGIN      first node of the path
     * @param DESTINATION last node of the path
	 * @param STRONG_FILTER true iff it should be worth to spend time on advanced filtering algorithms (that runs
	 * in linear time). If so, then it uses dominator-based and SCCs-based filtering algorithms. This option should
	 * be used on small-size.
     * @return a hamiltonian path constraint
     */
    public static Constraint hamiltonianPath(DirectedGraphVar GRAPHVAR, int ORIGIN, int DESTINATION, boolean STRONG_FILTER) {
        Solver solver = GRAPHVAR.getSolver();
        int n = GRAPHVAR.getEnvelopGraph().getNbNodes();
        int[] succs = new int[n];
        int[] preds = new int[n];
        for (int i = 0; i < n; i++) {
            succs[i] = preds[i] = 1;
        }
        succs[DESTINATION] = preds[ORIGIN] = 0;
        Constraint gc = new Constraint(new Variable[]{GRAPHVAR}, solver);
        gc.setPropagators(
				new PropNodeDegree_AtLeast(GRAPHVAR, GraphVar.IncidentNodes.SUCCESSORS, succs),
				new PropNodeDegree_AtMost(GRAPHVAR, GraphVar.IncidentNodes.SUCCESSORS, succs),
				new PropNodeDegree_AtLeast(GRAPHVAR, GraphVar.IncidentNodes.PREDECESSORS, preds),
				new PropNodeDegree_AtMost(GRAPHVAR, GraphVar.IncidentNodes.PREDECESSORS, preds),
				new PropPathNoCycle(GRAPHVAR, ORIGIN, DESTINATION));
		if(STRONG_FILTER){
			PropReducedPath red = new PropReducedPath(GRAPHVAR);
			PropSCCDoorsRules rules = new PropSCCDoorsRules(GRAPHVAR,red);
			PropArborescence arbo = new PropArborescence(GRAPHVAR, ORIGIN,true);
			PropAntiArborescence aa = new PropAntiArborescence(GRAPHVAR,DESTINATION,true);
			PropAllDiffGraphIncremental ad = new PropAllDiffGraphIncremental(GRAPHVAR,n-1);
			gc.addPropagators(red,rules,arbo,aa,ad);
		}
        return gc;
    }

    /**
     * Anti arborescence partitioning constraint
     * also known as tree constraint (CP'11)
     * GAC in (almost) linear time : O(alpha.m)
     * roots are identified by loops 
     * <p/>
     * BEWARE this implementation supposes that every node is part of the solution graph
     *
     * @param GRAPHVAR
     * @param NB_TREE  number of anti arborescences
     * @return tree constraint
     */
    public static Constraint nTrees(DirectedGraphVar GRAPHVAR, IntVar NB_TREE) {
        return new NTree(GRAPHVAR, NB_TREE);
    }
}
