/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.hcp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.restart.MonotonicRestartStrategy;
import org.chocosolver.solver.search.strategy.strategy.GraphCostBasedSearch;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.annotations.Test;

/**
 * Solves the Hamiltonian Cycle Problem
 * <p/>
 * Uses graph variables and a light but fast filtering
 * Parses HCP instances of the TSPLIB:
 * See <a href = "http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/">TSPLIB</a>
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class HamiltonianCycleProblem {

    @Test(groups = "1s", timeOut = 60000)
    public void testHCP() {
        // TSPLIB HCP Instance (see http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/)
        String inst = getClass().getResource("alb1000.hcp").getPath();
        boolean[][] matrix = HCP_Utils.parseTSPLIBInstance(inst);
        int n = matrix.length;

        Model model = new Model("solving the Hamiltonian Cycle Problem");
        // variables (use linked lists because the graph is sparse)
        UndirectedGraph GLB = new UndirectedGraph(model,n,SetType.LINKED_LIST,true);
        UndirectedGraph GUB = new UndirectedGraph(model,n,SetType.LINKED_LIST,true);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j]) {
                    GUB.addEdge(i, j);
                }
            }
        }
        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);
        // constraints
        model.cycle(graph).post();


        Solver solver = model.getSolver();
        solver.setSearch(new GraphCostBasedSearch(graph).configure(GraphCostBasedSearch.MIN_P_DEGREE).useLastConflict());
        solver.limitTime("10s");
        solver.showStatistics();
        // restart search every 100 fails
        solver.setRestarts(new FailCounter(model,100), new MonotonicRestartStrategy(100), 1000);

        model.getSolver().solve();
    }
}
