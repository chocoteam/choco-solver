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

import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.gary.arborescences.PropAntiArborescence;
import solver.constraints.gary.arborescences.PropArborescence;
import solver.constraints.gary.basic.*;
import solver.constraints.gary.degree.PropNodeDegree_AtLeast_Coarse;
import solver.constraints.gary.degree.PropNodeDegree_AtLeast_Incr;
import solver.constraints.gary.degree.PropNodeDegree_AtMost_Incr;
import solver.constraints.gary.path.PropAllDiffGraphIncremental;
import solver.constraints.gary.path.PropPathNoCycle;
import solver.constraints.gary.path.PropReducedPath;
import solver.constraints.gary.path.PropSCCDoorsRules;
import solver.constraints.gary.trees.PropTreeNoSubtour;
import solver.constraints.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.gary.tsp.undirected.lagrangianRelaxation.PropLagr_OneTree;
import solver.variables.IntVar;
import solver.variables.VF;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.UndirectedGraphVar;
import util.objects.graphs.Orientation;
import util.tools.ArrayUtils;

/**
 * Some usual graph constraints
 *
 * @author Jean-Guillaume Fages
 */
public class GraphConstraintFactory {

    //***********************************************************************************
    // UNDIRECTED GRAPHS
    //***********************************************************************************

    /**
     * partition a graph variable into nCliques cliques
     *
     * @param GRAPHVAR   graph variable partitioned into cliques
     * @param NB_CLIQUES expected number of cliques
     * @return a constraint which partitions GRAPHVAR into NB_CLIQUES cliques
     */
    public static Constraint nCliques(UndirectedGraphVar GRAPHVAR, IntVar NB_CLIQUES) {
		return new Constraint("NCliques",
				new PropTransitivity(GRAPHVAR),
				new PropKCliques(GRAPHVAR, NB_CLIQUES),
				new PropKCC(GRAPHVAR, NB_CLIQUES)
		);
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
		Propagator[] props = ArrayUtils.append(hamiltonianCycle(GRAPHVAR).getPropagators(),
				new Propagator[]{new PropCycleEvalObj(GRAPHVAR, COSTVAR, EDGE_COSTS)});
        if (HELD_KARP > 0) {
            PropLagr_OneTree hk = new PropLagr_OneTree(GRAPHVAR, COSTVAR, EDGE_COSTS);
            hk.waitFirstSolution(HELD_KARP == 2);
			props = ArrayUtils.append(props,new Propagator[]{hk});
        }
        return new Constraint("Graph_TSP",props);
    }

    /**
     * GRAPHVAR must form a Hamiltonian cycle
     * <p/> Filtering algorithms are incremental and run in O(1) per enforced/removed edge.
     * <p/> Subtour elimination is an undirected adaptation of the
     * nocycle constraint of Caseau & Laburthe in Solving small TSPs with Constraints.
     *
     * @param GRAPHVAR graph variable representing a Hamiltonian cycle
     * @return a hamiltonian cycle constraint
     */
    public static Constraint hamiltonianCycle(UndirectedGraphVar GRAPHVAR) {
		int m = 0;
		int n = GRAPHVAR.getEnvelopGraph().getNbNodes();
		for(int i=0;i<n;i++){
			m += GRAPHVAR.getEnvelopGraph().getNeighborsOf(i).getSize();
		}
		m /= 2;
		if(m<20*n){
			return new Constraint("Graph_HamiltonianCycle",
					new PropNodeDegree_AtLeast_Incr(GRAPHVAR, 2),
					new PropNodeDegree_AtMost_Incr(GRAPHVAR, 2),
					new PropCycleNoSubtour(GRAPHVAR)
			);
		}else{
			return new Constraint("Graph_HamiltonianCycle",
					new PropNodeDegree_AtLeast_Coarse(GRAPHVAR, 2),
					new PropNodeDegree_AtMost_Incr(GRAPHVAR, 2),
					new PropCycleNoSubtour(GRAPHVAR)
			);
		}
    }

	/**
	 * GRAPHVAR must form a spanning tree, i.e. an acyclic and connected undirected graph spanning every vertex
	 * <p/> Incremental degree constraint, runs in O(1) time per force/removed edge
	 * <p/> Connectivity checker and bridge detection in O(n+m) time (Tarjan's algorithm)
	 * <p/> Subtour elimination in O(n) worst case time per enforced edge
	 *
	 * @param GRAPHVAR graph variable forming a tree
	 * @return a constraint ensuring that GRAPHVAR is a spanning tree
	 */
	public static Constraint spanning_tree(UndirectedGraphVar GRAPHVAR) {
		IntVar nbNodes = VF.fixed(GRAPHVAR.getEnvelopGraph().getNbNodes(),GRAPHVAR.getSolver());
		return new Constraint("Graph_SpanningTree",ArrayUtils.append(
				tree(GRAPHVAR).getPropagators(),
				new Propagator[]{new PropKNodes(GRAPHVAR, nbNodes)}
		));
	}

	/**
	 * GRAPHVAR must form a tree, i.e. an acyclic and connected undirected graph
	 * <p/> Incremental degree constraint, runs in O(1) time per force/removed edge
	 * <p/> Connectivity checker and bridge detection in O(n+m) time (Tarjan's algorithm)
	 * <p/> Subtour elimination in O(n) worst case time per enforced edge
	 *
	 * @param GRAPHVAR graph variable forming a tree
	 * @return a constraint ensuring that GRAPHVAR is a tree
	 */
	public static Constraint tree(UndirectedGraphVar GRAPHVAR) {
		return new Constraint("Graph_Tree",
				new PropNodeDegree_AtLeast_Coarse(GRAPHVAR, 1),
				new PropTreeNoSubtour(GRAPHVAR),
				new PropConnected(GRAPHVAR)
		);
	}

    //***********************************************************************************
    // DIRECTED GRAPHS
    //***********************************************************************************

    /**
     * GRAPHVAR must form a Hamiltonian path from ORIGIN to DESTINATION.
     * <p/> Basic filtering algorithms are incremental and run in O(1) per enforced/removed arc.
     * <p/> Subtour elimination is the nocycle constraint of Caseau & Laburthe in Solving small TSPs with Constraints.
     * <p/>
     * <p/> Assumes that ORIGIN has no predecessor, DESTINATION has no successor and each node is mandatory.
     *
     * @param GRAPHVAR      variable representing a path
     * @param ORIGIN        first node of the path
     * @param DESTINATION   last node of the path
     * @param STRONG_FILTER true iff it should be worth to spend time on advanced filtering algorithms (that runs
     *                      in linear time). If so, then it uses dominator-based and SCCs-based filtering algorithms. This option should
     *                      be used on small-size.
     * @return a hamiltonian path constraint
     */
    public static Constraint hamiltonianPath(DirectedGraphVar GRAPHVAR, int ORIGIN, int DESTINATION, boolean STRONG_FILTER) {
        int n = GRAPHVAR.getEnvelopGraph().getNbNodes();
        int[] succs = new int[n];
        int[] preds = new int[n];
        for (int i = 0; i < n; i++) {
            succs[i] = preds[i] = 1;
        }
        succs[DESTINATION] = preds[ORIGIN] = 0;
		Propagator[] props = new Propagator[]{
				new PropNodeDegree_AtLeast_Coarse(GRAPHVAR, Orientation.SUCCESSORS, succs),
				new PropNodeDegree_AtMost_Incr(GRAPHVAR, Orientation.SUCCESSORS, succs),
				new PropNodeDegree_AtLeast_Coarse(GRAPHVAR, Orientation.PREDECESSORS, preds),
				new PropNodeDegree_AtMost_Incr(GRAPHVAR, Orientation.PREDECESSORS, preds),
				new PropPathNoCycle(GRAPHVAR, ORIGIN, DESTINATION)
		};
		if (STRONG_FILTER) {
			PropReducedPath red = new PropReducedPath(GRAPHVAR);
			PropSCCDoorsRules rules = new PropSCCDoorsRules(GRAPHVAR, red);
			PropArborescence arbo = new PropArborescence(GRAPHVAR, ORIGIN, true);
			PropAntiArborescence aa = new PropAntiArborescence(GRAPHVAR, DESTINATION, true);
			PropAllDiffGraphIncremental ad = new PropAllDiffGraphIncremental(GRAPHVAR, n - 1);
			props = ArrayUtils.append(props,ArrayUtils.toArray(red, rules, arbo, aa, ad));
		}
        return new Constraint("Graph_HamiltonianPath",props);
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
