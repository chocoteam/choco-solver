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
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.limits.FailCounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.lns.neighbors.IntNeighbor;
import org.chocosolver.solver.search.strategy.strategy.GraphCostBasedSearch;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Iterator;
import java.util.Random;

/**
 * LNS approach to solve the Traveling Salesman Problem
 * Parses TSP instances of the TSPLIB library
 * See <a href = "http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/">TSPLIB</a>
 * <p/>
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class TSP_lns {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int bestSolutionValue = -1;
    private boolean opt = false;

    //***********************************************************************************
    // MAIN
    //***********************************************************************************

    public static void main(String[] args) {
        String REPO = "src/test/java/org/chocosolver/samples/tsp";
        String INSTANCE = "bier127";
        int[][] data = TSP_Utils.parseInstance(REPO + "/" + INSTANCE + ".tsp", 300);
        new TSP_lns(data);
    }

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public TSP_lns(int[][] costMatrix){
        super();
        int LIMIT = 30; // in seconds

        final int n = costMatrix.length;
        // variables
        Model model = new Model();
        IntVar totalCost = model.intVar("obj", 0, 99999999, true);
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
        model.tsp(graph, totalCost, costMatrix, 2).post();

        // intuitive heuristic (cheapest edges first)
        final GraphCostBasedSearch search = new GraphCostBasedSearch(graph, costMatrix).configure(GraphCostBasedSearch.MIN_COST);
        Solver solver = model.getSolver();
        solver.setSearch(search);
        solver.limitTime(LIMIT+"s");

        // LNS (relaxes consecutive edges)
        INeighbor LNS = new SubpathLNS(graph);
        solver.setLNS(LNS,new FailCounter(model,30));

        model.setObjective(Model.MINIMIZE, totalCost);

        while (solver.solve()){
            search.configure(GraphCostBasedSearch.MIN_DELTA_DEGREE);
            System.out.println("solution found : " + totalCost);
            bestSolutionValue = totalCost.getValue();
        }

        if(solver.getTimeCount()<LIMIT){
            opt = true;
            System.out.println("Optimality proved with LNS");
        }else{
            opt = false;
            if(solver.getSolutionCount()>0) {
                System.out.println("Best solution found : " +solver.getBestSolutionValue()+" (but no optimality proof");
            }else{
                System.out.println("no solution found");
            }
        }
    }

    public int getBestSolutionValue() {
        return bestSolutionValue;
    }

    public boolean optimalityProved() {
        return opt;
    }

    //***********************************************************************************
    // LNS
    //***********************************************************************************

    /**
     * Object describing which edges to freeze and which others to relax in the LNS
     * Relaxes a (sub)path of the previous solution (freezes the rest)
     */
    private class SubpathLNS extends IntNeighbor {

        Random rd = new Random(0);
        int n, nbRL;
        UndirectedGraph solution;
        int nbFreeEdges = 15;
        UndirectedGraphVar graph;

        protected SubpathLNS(UndirectedGraphVar graph) {
            super(new IntVar[]{});
            this.graph = graph;
            this.n = graph.getNbMaxNodes();
            this.solution = new UndirectedGraph(n,SetType.LINKED_LIST,true);
        }

        @Override
        public void init() {}

        @Override
        public void recordSolution() {
            // stores a solution in a graph object
            for(int i=0;i<n;i++)solution.getNeighborsOf(i).clear();
            for(int i=0;i<n;i++){
                ISet nei = graph.getMandatoryNeighborsOf(i);
                for(int j:nei){
                    solution.addEdge(i,j);
                }
            }
        }

        @Override
        public void fixSomeVariables() throws ContradictionException {
            // relaxes a sub-path (a set of consecutive edges in a solution)
            int i1 = rd.nextInt(n);
            ISet nei = solution.getNeighborsOf(i1);
            Iterator<Integer> iter = nei.iterator();
            int i2 = iter.next();
            if (rd.nextBoolean()) {
                i2 = iter.next();
            }
            for (int k = 0; k < n - nbFreeEdges; k++) {
                graph.enforceEdge(i1,i2,this);
                int i3 = -1;
                for (int z : solution.getNeighborsOf(i2)) {
                    if (z != i1) {
                        i3 = z;
                        break;
                    }
                }
                assert i3 >= 0;
                i1 = i2;
                i2 = i3;
            }
        }

        @Override
        public void restrictLess() {
            nbRL++;
            // Eventually increases the size of the relaxes fragment (not necessary)
            if(nbRL>nbFreeEdges){
                nbRL = 0;
                nbFreeEdges += (nbFreeEdges*3)/2;
            }
        }

        @Override
        public boolean isSearchComplete() {
            return nbFreeEdges>=n;
        }

        @Override
        public void loadFromSolution(Solution solution) {
            throw new UnsupportedOperationException("not implemented");
        }

    }
    }
