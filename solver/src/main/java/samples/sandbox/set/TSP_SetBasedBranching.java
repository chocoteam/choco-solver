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

package samples.sandbox.set;

import choco.kernel.ResolutionPolicy;
import choco.kernel.memory.setDataStructures.SetType;
import samples.sandbox.graph.input.TSP_Utils;
import samples.sandbox.graph.output.TextWriter;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.tsp.undirected.lagrangianRelaxation.PropLagr_OneTree;
import solver.constraints.set.SetConstraintsFactory;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.loop.monitors.IMonitorInitPropagation;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecisionSet;
import solver.search.strategy.pattern.SearchPattern;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.search.strategy.strategy.graph.GraphStrategies;
import solver.search.strategy.strategy.set.SetSearchStrategy;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VariableFactory;
import solver.variables.graph.UndirectedGraphVar;

import java.io.File;

/**
 * Solves the (symmetric) Traveling Salesman Problem
 * the filtering is based on a graph variable while the branching
 * is based on a set representation (in order to use Last Fail directly)
 *
 * @author Jean-Guillaume Fages
 * @since Janv. 2013
 */
public class TSP_SetBasedBranching {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private static final long TIMELIMIT = 300000;
    private static final int MAX_SIZE = 60000;
    private static String outFile;
    private static int upperBound = Integer.MAX_VALUE / 4;
    private static IntVar totalCost;
    private static Solver solver;
    private static boolean allDiffAC = false;
    private static boolean optProofOnly = true;
    private static PropLagr_OneTree mst;
    private static int search;
    private static int policy;

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public static void main(String[] args) {
        TextWriter.clearFile(outFile = "tsp.csv");
        TextWriter.writeTextInto("instance;sols;fails;nodes;time;obj;allDiffAC;search;\n", outFile);
        String dir = "/Users/jfages07/github/In4Ga/benchRousseau";
//		String dir = "/Users/jfages07/github/In4Ga/ALL_tsp";
//		String dir = "/Users/jfages07/github/In4Ga/mediumTSP";
        File folder = new File(dir);
        String[] list = folder.list();
        int[][] matrix;
        optProofOnly = true;
        allDiffAC = false;
        search = 0;
        policy = GraphStrategies.MAX_COST;
        for (String s : list) {
            if (s.contains(".tsp") && (!s.contains("gz")) && (!s.contains("lin"))) {
                matrix = TSP_Utils.parseInstance(dir + "/" + s, MAX_SIZE);
                if ((matrix != null && matrix.length >= 0 && matrix.length < 4000)) {
                    if (optProofOnly) {
                        upperBound = TSP_Utils.getOptimum(s.split("\\.")[0], "/Users/jfages07/github/In4Ga/ALL_tsp/bestSols.csv");
                        System.out.println("optimum : " + upperBound);
                    }
                    solve(matrix, s);
                } else {
                    System.out.println("CANNOT LOAD");
                }
            }
        }
    }

    private static void solve(final int[][] matrix, String instanceName) {
        final int n = matrix.length;
        solver = new Solver();
        solver.set(SearchPattern.LAST_FAIL);
        // variables
        totalCost = VariableFactory.bounded("obj", 0, upperBound, solver);
        final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, SetType.LINKED_LIST, SetType.LINKED_LIST, true);
        final SetVar[] sets = new SetVar[n];
        for (int i = 0; i < n; i++) {
            undi.getKernelGraph().activateNode(i);
            for (int j = i + 1; j < n; j++) {
                undi.getEnvelopGraph().addEdge(i, j);
            }
            sets[i] = VariableFactory.set("neigh",
                    undi.getEnvelopGraph().getNeighborsOf(i),
                    undi.getKernelGraph().getNeighborsOf(i),
                    solver);
        }
        // constraints
        Constraint gc = GraphConstraintFactory.tsp(undi, totalCost, matrix, 0, solver);
        mst = PropLagr_OneTree.oneTreeBasedRelaxation(undi, totalCost, matrix, gc, solver);
        mst.waitFirstSolution(search != 0);
        gc.addPropagators(mst);
        solver.post(gc);
        solver.post(SetConstraintsFactory.graph_channel(sets, undi, solver));
        // config
        GraphStrategies graph_strategy = new GraphStrategies(undi, matrix, mst);
        graph_strategy.configure(policy, true, true, false);
        SetSearchStrategy set_strategy = new SetSearchStrategy(sets) {
            @Override
            public Decision<SetVar> getDecision() {
                int maxCost = -1;
                SetVar set = null;
                int elem = -1;
                for (int i = 0; i < n; i++) {
                    SetVar s = vars[i];
                    if (!s.instantiated()) {
                        for (int j = s.getEnvelope().getFirstElement(); j >= 0; j = s.getEnvelope().getNextElement()) {
                            if (!s.getKernel().contain(j)) {
                                if (elem == -1 || maxCost < matrix[i][j]) {
                                    maxCost = matrix[i][j];
                                    set = s;
                                    elem = j;
                                }
                            }
                        }
                    }
                }
                if (set != null) {
                    FastDecisionSet d = pool.getE();
                    if (d == null) {
                        d = new FastDecisionSet(pool);
                    }
                    d.set(set, elem, DecisionOperator.set_force);
                    int i = 0;
                    for (int k = 0; k < n; k++) {
                        if (sets[k] == set) i = k;
                    }
                    if (!sets[elem].getEnvelope().contain(i)) {
                        throw new UnsupportedOperationException();
                    }
                    if (undi.getKernelGraph().arcExists(i, elem)) {
                        throw new UnsupportedOperationException();
                    }
                    if (!undi.getEnvelopGraph().arcExists(i, elem)) {
                        throw new UnsupportedOperationException();
                    }
                    return d;
                } else {
                    return null;
                }
            }

            @Override
            public Decision<SetVar> computeDecision(SetVar s) {
                if (s.instantiated()) {
                    return null;
                }
                int maxCost = 0;
                int elem = -1;
                int i = 0;
                for (int k = 0; k < n; k++) {
                    if (sets[k] == s) i = k;
                }
                for (int j = s.getEnvelope().getFirstElement(); j >= 0; j = s.getEnvelope().getNextElement()) {
                    if (!s.getKernel().contain(j)) {
                        if (elem == -1 || maxCost < matrix[i][j]) {
                            maxCost = matrix[i][j];
                            elem = j;
                        }
                    }
                }
                FastDecisionSet d = pool.getE();
                if (d == null) {
                    d = new FastDecisionSet(pool);
                }
                d.set(s, elem, DecisionOperator.set_force);
                return d;
            }
        };

//		AbstractStrategy strategy = graph_strategy;
        AbstractStrategy strategy = set_strategy;
        switch (search) {
            case 0:
                solver.set(strategy);
                break;
            case 1:
                solver.set(new StaticStrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), strategy));
                break;
            case 2:
                solver.set(new StaticStrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.DICHOTOMIC), strategy));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        solver.getSearchLoop().plugSearchMonitor(new IMonitorInitPropagation() {
            @Override
            public void beforeInitialPropagation() {
            }

            @Override
            public void afterInitialPropagation() {
                System.out.println("cost after prop ini : " + totalCost);
                int e = 0;
                int k = 0;
                for (int i = 0; i < n; i++) {
                    e += undi.getEnvelopGraph().getNeighborsOf(i).getSize();
                    k += undi.getKernelGraph().getNeighborsOf(i).getSize();
                }
                e /= 2;
                k /= 2;
                System.out.println(k + "/" + e);
            }
        });
        solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        SearchMonitorFactory.log(solver, false, false);
        // resolution
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
        check(solver, undi, totalCost, matrix);
        //output
        int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
        String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
                + solver.getMeasures().getNodeCount() + ";"
                + (int) (solver.getMeasures().getTimeCount()) + ";" + bestCost + ";" + allDiffAC + ";" + search + ";\n";
        TextWriter.writeTextInto(txt, outFile);
    }

    private static void check(Solver solver, UndirectedGraphVar undi, IntVar totalCost, int[][] matrix) {
        int n = matrix.length;
        if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
            throw new UnsupportedOperationException();
        }
        if (solver.getMeasures().getSolutionCount() > 0) {
            int sum = 0;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (undi.getEnvelopGraph().edgeExists(i, j)) {
                        sum += matrix[i][j];
                    }
                }
            }
            if (sum != solver.getSearchLoop().getObjectivemanager().getBestValue()) {
                throw new UnsupportedOperationException();
            }
        }
    }
}