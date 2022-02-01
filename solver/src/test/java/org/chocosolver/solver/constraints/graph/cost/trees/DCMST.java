/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cost.trees;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.objective.ObjectiveStrategy;
import org.chocosolver.solver.objective.OptimizationPolicy;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.strategy.GraphCostBasedSearch;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.io.*;

/**
 * Solves the Degree Constrained Minimum Spanning Tree Problem
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class DCMST {

    private static final String OUT_PUT_FILE = "DR.csv";

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // input
    private int n;
    private int[] dMax;
    private int[][] dist;
    private int lb, ub;
    private final String instance;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public DCMST(String inst) {
        parse_T_DE_DR(new File(inst));
        instance = inst;
    }

    //***********************************************************************************
    // MODEL
    //***********************************************************************************

    public void solve() {
        Model model = new Model();
        IntVar totalCost = model.intVar("obj", lb, ub, true);
        // graph var domain
        UndirectedGraph GLB = new UndirectedGraph(model,n,SetType.LINKED_LIST,true);
        UndirectedGraph GUB = new UndirectedGraph(model,n,SetType.BIPARTITESET,true);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (dist[i][j] != -1 && !(dMax[i] == 1 && dMax[j] == 1)) {
                    GUB.addEdge(i, j); // possible edge
                }
            }
        }
        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);
        IntVar[] degrees = model.intVarArray(graph.getNbMaxNodes(), 0, graph.getNbMaxNodes());
        model.degrees(graph, degrees).post();
        for (int i = 0; i < n; i++) {
            model.arithm(degrees[i], "<=", dMax[i]).post();
        }

        // degree constrained-minimum spanning tree constraint
        model.dcmst(graph,degrees,totalCost,dist,2).post();

        final GraphCostBasedSearch mainSearch = new GraphCostBasedSearch(graph, dist);
        // find the first solution by selecting cheap edges
        mainSearch.configure(GraphCostBasedSearch.MIN_COST);
        Solver s = model.getSolver();
        // then select the most expensive ones (fail first principle, with last conflict)
        s.plugMonitor((IMonitorSolution) () -> {
            mainSearch.useLastConflict();
            mainSearch.configure(GraphCostBasedSearch.MIN_P_DEGREE);
            System.out.println("Solution found : "+totalCost);
        });
        // bottom-up optimization : find a first solution then reach the global minimum from below
        s.setSearch(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), mainSearch);
        s.limitSolution(2); // therefore there is at most two solutions
        long TIMELIMIT = 300000;
        s.limitTime(TIMELIMIT); // time limit

        // find optimum
        model.setObjective(Model.MINIMIZE,totalCost);
        while (s.solve()){
            System.out.println(totalCost);
        }

        if (s.getSolutionCount() == 0 && s.getTimeCount() < TIMELIMIT/1000) {
            throw new UnsupportedOperationException("Provided instances are feasible!");
        }
        String output = instance+";"+s.getSolutionCount()+";"+s.getBestSolutionValue()+";"
                +s.getNodeCount()+";"+s.getFailCount()+";"+s.getTimeCount()+";\n";
//        write(output,OUT_PUT_FILE,false);
    }

    private static void write(String text, String file, boolean clearFirst){
        try{
            FileWriter writer = new FileWriter(file, !clearFirst);
            writer.write(text);
            writer.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    //***********************************************************************************
    // PARSING
    //***********************************************************************************

    public boolean parse_T_DE_DR(File file) {
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();
            String[] numbers;
            n = Integer.parseInt(line);
            dist = new int[n][n];
            dMax = new int[n];
            for (int i = 0; i < n; i++) {
                line = buf.readLine();
                numbers = line.split(" ");
                if (Integer.parseInt(numbers[0]) != i + 1) {
                    throw new UnsupportedOperationException();
                }
                dMax[i] = Integer.parseInt(numbers[1]);
                for (int j = 0; j < n; j++) {
                    dist[i][j] = -1;
                }
            }
            line = buf.readLine();
            int from, to, cost;
            int min = 1000000;
            int max = 0;
            while (line != null) {
                numbers = line.split(" ");
                from = Integer.parseInt(numbers[0]) - 1;
                to = Integer.parseInt(numbers[1]) - 1;
                cost = Integer.parseInt(numbers[2]);
                min = Math.min(min, cost);
                max = Math.max(max, cost);
                if (dist[from][to] != -1) {
                    throw new UnsupportedOperationException();
                }
                dist[from][to] = dist[to][from] = cost;
                line = buf.readLine();
            }
            lb = (n - 1) * min;
            ub = (n - 1) * max;
            //            setUB(dirOpt, s);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        throw new UnsupportedOperationException();
    }
}
