/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.tsp;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.strategy.GraphCostBasedSearch;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Solves the Traveling Salesman Problem
 * Parses TSP instances of the TSPLIB library
 * See <a href = "http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/">TSPLIB</a>
 * <p/>
 *
 * This is an exact approach dedicated to prove optimality of a solution.
 * It is assumed that a local search (e.g. LKH) algorithm has been performed
 * as a pre-processing step
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class TSP_exact {

    //***********************************************************************************
    // MAIN
    //***********************************************************************************

    public static void main(String[] args) {
        String REPO = "src/test/java/org/chocosolver/samples/tsp";
        String INSTANCE = "bier127";
        int[][] data = TSP_Utils.parseInstance(REPO+"/"+INSTANCE+".tsp", 300);
        int presolve = 118282;//TSP_Utils.getOptimum(INSTANCE,REPO+"/bestSols.csv");
        new TSP_exact(data,presolve);
    }

    //***********************************************************************************
    // SOLVER
    //***********************************************************************************

    public TSP_exact(int[][] costMatrix, int initialUB){
        super();
        final int n = costMatrix.length;
        int LIMIT = 30; // in seconds

        Model model = new Model();
        // variables
        IntVar totalCost = model.intVar("obj", 0, initialUB, true);
        // creates a graph containing n nodes
        UndirectedGraph GLB = new UndirectedGraph(model, n, SetType.LINKED_LIST, true);
        UndirectedGraph GUB = new UndirectedGraph(model, n, SetType.BIPARTITESET, true);
        // adds potential edges
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                GUB.addEdge(i, j);
            }
        }
        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

        // constraints (TSP basic model + lagrangian relaxation)
        model.tsp(graph, totalCost, costMatrix, 1).post();


        Solver solver = model.getSolver();
        // Fail first principle (requires a very good initial upper bound)
        solver.setSearch(new GraphCostBasedSearch(graph, costMatrix).configure(GraphCostBasedSearch.MAX_COST).useLastConflict());
        solver.limitTime(LIMIT+"s");

        model.setObjective(Model.MINIMIZE,totalCost);
        while (solver.solve()){
            System.out.println("solution found : " + totalCost);
        }
        if(solver.getTimeCount()<LIMIT){
            System.out.println("Optimality proved with exact CP approach");
        }else{
            if(solver.getSolutionCount()>0) {
                System.out.println("Best solution found : " + solver.getBestSolutionValue() + " (but no optimality proof");
            }else{
                System.out.println("no solution found");
            }
        }
    }
}
