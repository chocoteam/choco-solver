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

package samples.graph;

import common.util.objects.setDataStructures.SetType;
import org.kohsuke.args4j.Option;
import samples.AbstractProblem;
import samples.graph.input.TSP_Utils;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.gary.GraphConstraintFactory;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.search.strategy.strategy.graph.GraphStrategies;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.UndirectedGraphVar;

/**
 * Solves the Traveling Salesman Problem
 * parses TSP instances of the TSPLIB library
 * See <a href = "http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/">TSPLIB</a>
 * proposes several optimization strategies
 * <p/>
 * Note that using the LKH heuristic as a pre-processing would speed up the resolution
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class TravelingSalesmanProblem extends AbstractProblem {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    @Option(name = "-tl", usage = "time limit.", required = false)
    private long limit = 60000;
    // instance file path
    @Option(name = "-inst", usage = "TSPLIB TSP Instance file path.", required = false)
    private String instancePath = "/Users/jfages07/github/In4Ga/ALL_tsp/eil101.tsp";
    @Option(name = "-optPolicy", usage = "Optimization policy (0:top-down,1:bottom-up,2:dichotomic).", required = false)
    private int policy = 1; // the lower bound of the Lagrangian relaxation is pretty good so Bottom-Up is a good choise


    // input cost matrix
    private int[][] costMatrix;
    // graph variable representing the cycle
    private UndirectedGraphVar graph;
    // integer variable representing the objective
    private IntVar totalCost;

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public static void main(String[] args) {
        new TravelingSalesmanProblem().execute(args);
    }

    @Override
    public void createSolver() {
        solver = new Solver("solving the Traveling Salesman Problem");
    }

    @Override
    public void buildModel() {
        costMatrix = TSP_Utils.parseInstance(instancePath, 200);
        final int n = costMatrix.length;
        solver = new Solver();
        // variables
        totalCost = VariableFactory.bounded("obj", 0, 99999, solver);
        // creates a graph containing n nodes
        graph = new UndirectedGraphVar("G", solver, n, SetType.SWAP_ARRAY, SetType.LINKED_LIST, true);
        // adds potential edges
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                graph.getEnvelopGraph().addEdge(i, j);
            }
        }
        // constraints (TSP basic model + lagrangian relaxation)
        solver.post(GraphConstraintFactory.tsp(graph, totalCost, costMatrix, 2));
    }

    @Override
    public void configureSearch() {
        GraphStrategies strategy = new GraphStrategies(graph, costMatrix, null);
        strategy.configure(GraphStrategies.MIN_COST, true);
        switch (policy) {
            case 0:
                solver.set(strategy);
                System.out.println("classical top-down minimization");
                break;
            case 1:
                solver.set(new StaticStrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), strategy));
                System.out.println("bottom-up minimization");
                break;
            case 2:
                solver.set(new StaticStrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.DICHOTOMIC), strategy));
                System.out.println("dichotomic minimization");
                break;
            default:
                throw new UnsupportedOperationException("policy should be 0, 1 or 2");
        }
        SearchMonitorFactory.limitTime(solver, limit);
        SearchMonitorFactory.log(solver, true, false);
    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
    }

    @Override
    public void prettyOut() {
        System.out.println("optimum in ["
                + solver.getSearchLoop().getObjectivemanager().getBestLB() + ","
                + solver.getSearchLoop().getObjectivemanager().getBestUB() + "]");
    }
}