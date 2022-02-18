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
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.graph.edge.GraphEdgeSelector;
import org.chocosolver.solver.search.strategy.selectors.values.graph.priority.GraphEdgesOnly;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.annotations.Test;

/**
 * Solves the Knight's Tour Problem
 * <p/>
 * Uses graph variables (light data structure)
 * better with -Xms1048m -Xmx2048m for memory allocation
 * when solving large instances
 *
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class KnightTourProblem {

    @Test(groups = "10s", timeOut = 60000)
    public void testKnightTourProblem() {
        boolean[][] matrix;
        boolean closedTour = true; //Open tour (path instead of cycle)
        int boardLength = 60;
        // This generates the boolean incidence matrix of the chessboard graph
        // It is responsible of the high memory consumption of this example
        // and could be replaced by lighter data structure
        if (closedTour) {
            matrix = HCP_Utils.generateKingTourInstance(boardLength);
        } else {
            matrix = HCP_Utils.generateOpenKingTourInstance(boardLength);
        }
        Model model = new Model("solving the knight's tour problem with a graph variable");
        // variables
        int n = matrix.length;
        // graph representing mandatory nodes and edges
        // (linked list data structure as the expected solution is expected to be sparse,
        // every vertex in [0,n-1] is mandatory)
        UndirectedGraph GLB = new UndirectedGraph(model,n,SetType.LINKED_LIST,true);
        // graph representing potential nodes and edges
        // (linked list data structure as its initial value is sparse,
        UndirectedGraph GUB = new UndirectedGraph(model,n,SetType.LINKED_LIST,true);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (matrix[i][j]) { // adds possible edge
                    GUB.addEdge(i, j);
                }
            }
        }
        // creates the graph variable
        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

        // hamiltonian cycle constraint
        model.cycle(graph).post();

        // basically branch on sparse areas of the graph
        Solver solver = model.getSolver();
        solver.setSearch(Search.graphVarSearch(
                new InputOrder<>(model),
                new GraphEdgesOnly(),
                null,
                new MinNeigh(),
                true,
                graph
        ));
        solver.limitTime("20s");

        if(solver.solve()){
            System.out.println(graph.getValue().graphVizExport());
        }
        solver.printStatistics();
    }

    //***********************************************************************************
    // HEURISTICS
    //***********************************************************************************

    private static class MinNeigh implements GraphEdgeSelector<UndirectedGraphVar> {

        public int[] selectEdge(UndirectedGraphVar g) {
            ISet suc;
            int n = g.getNbMaxNodes();
            int size = n + 1;
            int sizi;
            int from = -1;
            for (int i = 0; i < n; i++) {
                sizi = g.getPotentialNeighborsOf(i).size() - g.getMandatoryNeighborsOf(i).size();
                if (sizi < size && sizi > 0) {
                    from = i;
                    size = sizi;
                }
            }
            if (from == -1) {
                return new int[] {-1, -1};
            }
            suc = g.getPotentialNeighborsOf(from);
            for (int j : suc) {
                if (!g.getMandatoryNeighborsOf(from).contains(j)) {
                    int to = j;
                    return new int[] {from, to};
                }
            }
            throw new UnsupportedOperationException();
        }
    }
}
