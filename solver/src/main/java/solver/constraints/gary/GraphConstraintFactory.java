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

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.gary.PropKCliques;
import solver.constraints.propagators.gary.basic.PropTransitivity;
import solver.constraints.propagators.gary.constraintSpecific.PropNLoopsTree;
import solver.constraints.propagators.gary.constraintSpecific.PropNTree;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtMost;
import solver.constraints.propagators.gary.tsp.directed.PropPathNoCycle;
import solver.constraints.propagators.gary.tsp.directed.PropPathOrCircuitEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.lagrangianRelaxation.PropLagr_OneTree;
import solver.variables.IntVar;
import solver.variables.graph.GraphVar;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * Some usual graph constraints
 *
 * @author Jean-Guillaume Fages
 */
public class GraphConstraintFactory {

    /**
     * Create a generic empty constraint
     *
     * @param solver
     * @return a generic empty constraint
     */
    public static Constraint makeConstraint(Solver solver) {
        return new Constraint(solver);
    }

    /**
     * partition a graph variable into nCliques cliques
     * BEWARE unsafe
     *
     * @param graph
     * @param nCliques expected number of cliques
     * @param solver
     * @return
     */
    public static Constraint nCliques(UndirectedGraphVar graph, IntVar nCliques, Solver solver) {
        Constraint gc = makeConstraint(solver);
        gc.addPropagators(new PropTransitivity(graph, solver, gc));
        gc.addPropagators(new PropKCliques(graph, solver, gc, nCliques));
        return gc;
    }

    /**
     * Constraint modeling the Traveling Salesman Problem
     *
     * @param graph
     * @param cost   variable
     * @param costs  matrix (should be symmetric)
     * @param hkMode use the Lagrangian relaxation of the tsp
     *               described by Held and Karp
     *               {0:noHK,1:HK,2:HK but wait a first solution before running HK}
     * @param solver
     * @return
     */
    public static Constraint tsp(UndirectedGraphVar graph, IntVar cost, int[][] costs, int hkMode, Solver solver) {
        Constraint gc = hamiltonianCycle(graph, solver);
        gc.addPropagators(new PropCycleEvalObj(graph, cost, costs, gc, solver));
        if (hkMode > 0) {
            PropLagr_OneTree hk = PropLagr_OneTree.oneTreeBasedRelaxation(graph, cost, costs, gc, solver);
            hk.waitFirstSolution(hkMode == 2);
            gc.addPropagators(hk);
        }
        return gc;
    }

    /**
     * Constraint modeling the Asymmetric Traveling Salesman Problem
     * turned as the Minimum Cost Hamiltonian PATH Problem
     *
     * @param graph
     * @param cost   variable
     * @param costs  matrix
     * @param from   origin of the path
     * @param to     end of the path
     * @param solver
     * @return
     */
    public static Constraint atsp(DirectedGraphVar graph, IntVar cost, int[][] costs, int from, int to, Solver solver) {
        Constraint gc = hamiltonianPath(graph, from, to, solver);
        gc.addPropagators(new PropPathOrCircuitEvalObj(graph, cost, costs, gc, solver));
        return gc;
    }

    /**
     * graph must form a Hamiltonian cycle
     *
     * @param graph
     * @param solver
     * @return
     */
    public static Constraint hamiltonianCycle(UndirectedGraphVar graph, Solver solver) {
        Constraint gc = makeConstraint(solver);
        gc.addPropagators(new PropNodeDegree_AtLeast(graph, 2, gc, solver));
        gc.addPropagators(new PropNodeDegree_AtMost(graph, 2, gc, solver));
        gc.addPropagators(new PropCycleNoSubtour(graph, gc, solver));
        return gc;
    }

    /**
     * graph must form a Hamiltonian cycle from origin to end
     *
     * @param graph
     * @param origin
     * @param end
     * @param solver
     * @return
     */
    public static Constraint hamiltonianPath(DirectedGraphVar graph, int origin, int end, Solver solver) {
        int n = graph.getEnvelopGraph().getNbNodes();
        int[] succs = new int[n];
        int[] preds = new int[n];
        for (int i = 0; i < n; i++) {
            succs[i] = preds[i] = 1;
        }
        succs[end] = preds[origin] = 0;
        Constraint gc = makeConstraint(solver);
        gc.addPropagators(new PropNodeDegree_AtLeast(graph, GraphVar.IncidentNodes.SUCCESSORS, succs, gc, solver));
        gc.addPropagators(new PropNodeDegree_AtMost(graph, GraphVar.IncidentNodes.SUCCESSORS, succs, gc, solver));
        gc.addPropagators(new PropNodeDegree_AtLeast(graph, GraphVar.IncidentNodes.PREDECESSORS, preds, gc, solver));
        gc.addPropagators(new PropNodeDegree_AtMost(graph, GraphVar.IncidentNodes.PREDECESSORS, preds, gc, solver));
        gc.addPropagators(new PropPathNoCycle(graph, origin, end, gc, solver));
        return gc;
    }

    /**
     * Anti arborescence partitioning constraint (CP'11)
     * also known as tree constraint
     * GAC in (almost) linear time : O(alpha.m)
     * roots are loops
     *
     * @param graph
     * @param n      number of anti arborescences
     * @param solver
     * @return tree constraint
     */
    public static Constraint nTrees(DirectedGraphVar graph, IntVar n, Solver solver) {
        Constraint tree = makeConstraint(solver);
        tree.addPropagators(new PropNodeDegree_AtLeast(graph, GraphVar.IncidentNodes.SUCCESSORS, 1, tree, solver));
        tree.addPropagators(new PropNodeDegree_AtMost(graph, GraphVar.IncidentNodes.SUCCESSORS, 1, tree, solver));
        tree.addPropagators(new PropNLoopsTree(graph, n, solver, tree));
        tree.addPropagators(new PropNTree(graph, n, solver, tree));
        return tree;
    }
}
