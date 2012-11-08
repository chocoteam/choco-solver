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

import choco.kernel.ResolutionPolicy;
import choco.kernel.memory.IStateInt;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.gary.IRelaxation;
import solver.constraints.propagators.gary.arborescences.PropAntiArborescence;
import solver.constraints.propagators.gary.arborescences.PropArborescence;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.tsp.PropCyclePathChanneling;
import solver.constraints.propagators.gary.tsp.directed.*;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTour;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTourGraphReactor;
import solver.constraints.propagators.gary.tsp.directed.relaxationHeldKarp.PropHeldKarp;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.objective.strategies.BottomUp_Minimization;
import solver.objective.strategies.Dichotomic_Minimization;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.ATSP_heuristics;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.*;
import java.util.BitSet;

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class ATSP_ResolutionPolicy {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private static final long TIMELIMIT = 60000;
    private static String outFile = "atsp.csv";
    private static int seed = 0;
    // instance
    private static String instanceName;
    private static int[][] distanceMatrix;
    private static int n, noVal, optimum, initialUB;
    private static Solver solver;
    // model
    private static DirectedGraphVar graph;
    private static IntVar totalCost;
    private static Constraint gc;
    // RG data structure
    private static IStateInt nR;
    private static IStateInt[] sccOf;
    private static INeighbors[] outArcs;
    private static IDirectedGraph G_R;
    private static IStateInt[] sccFirst, sccNext;
    // Branching data structure
    private static IRelaxation relax;

    //***********************************************************************************
    // MODEL CONFIGURATION
    //***********************************************************************************

    private static int arbo = 0, rg = 1, undirectedMate = 2, pos = 3, allDiff = 4;//,time=5;
    private static int NB_PARAM = 5;
    private static BitSet config = new BitSet(NB_PARAM);
    private static boolean bst;

    private static void configParameters(int mask) {
        String bytes = Integer.toBinaryString(mask);
        while (bytes.length() < NB_PARAM) {
            bytes = "0" + bytes;
        }
        for (int i = 0; i < bytes.length(); i++) {
            config.set(i, bytes.charAt(NB_PARAM - 1 - i) == '1');
        }
    }

    //***********************************************************************************
    // SEARCH CONFIGURATION
    //***********************************************************************************

    private static int main_search;
    private static ATSP_heuristics heuristic;
    private static String[] searchMode = new String[]{"top-down", "bottom-up", "dichotomic"};

    //***********************************************************************************
    // MODEL-SEARCH-RESOLUTION-OUTPUT
    //***********************************************************************************

    public static void solve() {
        createModel();
        addPropagators();
        configureAndSolve();
    }

    public static void createModel() {
        // create model
        solver = new Solver();
        initialUB = optimum * 100;
        System.out.println("optimum : " + optimum);
        System.out.println("initial UB : " + initialUB);
        graph = new DirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
        totalCost = VariableFactory.bounded("total cost ", 0, initialUB, solver);
        try {
            for (int i = 0; i < n - 1; i++) {
                graph.getKernelGraph().activateNode(i);
                for (int j = 0; j < n; j++) {
                    if (distanceMatrix[i][j] != noVal) {
                        graph.getEnvelopGraph().addArc(i, j);
                    }
                }
                graph.getEnvelopGraph().removeArc(i, i);
            }
            graph.getKernelGraph().activateNode(n - 1);
            graph.getEnvelopGraph().removeArc(0, n - 1);
            graph.getEnvelopGraph().removeArc(n - 1, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        gc = GraphConstraintFactory.makeConstraint(solver);
    }

    public static void addPropagators() {
        // BASIC MODEL
//		Do not use to no decrease performances
// 		(difference due to lists and fix point within non monotonicity)
//		int[] succs = new int[n];
//		int[] preds = new int[n];
//		for(int i=0;i<n;i++){
//			succs[i] = preds[i] = 1;
//		}
//		succs[n-1] = preds[0] = 0;
//		gc.addAdHocProp(new PropAtMostNSuccessors(graph,succs,gc,solver));
//		gc.addAdHocProp(new PropAtLeastNSuccessors(graph,succs,gc,solver));
//		gc.addAdHocProp(new PropAtMostNPredecessors(graph,preds,gc,solver));
//		gc.addAdHocProp(new PropAtLeastNPredecessors(graph,preds,gc,solver));

        gc.addPropagators(new PropOneSuccBut(graph, n - 1, gc, solver));
        gc.addPropagators(new PropOnePredBut(graph, 0, gc, solver));

        gc.addPropagators(new PropPathNoCycle(graph, 0, n - 1, gc, solver));
        gc.addPropagators(new PropSumArcCosts(graph, totalCost, distanceMatrix, gc, solver));
        if (config.get(allDiff)) {
            gc.addPropagators(new PropAllDiffGraphIncremental(graph, n - 1, solver, gc));
        }
        // STRUCTURAL FILTERING
        if (config.get(arbo)) {
            gc.addPropagators(new PropArborescence(graph, 0, gc, solver, true));
            gc.addPropagators(new PropAntiArborescence(graph, n - 1, gc, solver, true));
        }
        if (config.get(rg)) {
            PropReducedGraphHamPath RP = new PropReducedGraphHamPath(graph, gc, solver);
            nR = RP.getNSCC();
            sccOf = RP.getSCCOF();
            outArcs = RP.getOutArcs();
            G_R = RP.getReducedGraph();
            sccFirst = RP.getSCCFirst();
            sccNext = RP.getSCCNext();
            gc.addPropagators(RP);
            PropSCCDoorsRules SCCP = new PropSCCDoorsRules(graph, gc, solver, nR, sccOf, outArcs, G_R, sccFirst, sccNext);
            gc.addPropagators(SCCP);
        }
        if (config.get(undirectedMate)) {
            UndirectedGraphVar undi = new UndirectedGraphVar(solver, n - 1, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
            INeighbors nei;
            for (int i = 0; i < n - 1; i++) {
                undi.getKernelGraph().activateNode(i);
                nei = graph.getEnvelopGraph().getSuccessorsOf(i);
                for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                    if (j == n - 1) {
                        undi.getEnvelopGraph().addEdge(i, 0);
                    } else {
                        undi.getEnvelopGraph().addEdge(i, j);
                    }
                }
            }
            gc.addPropagators(new PropCycleNoSubtour(undi, gc, solver));
            gc.addPropagators(new PropAtLeastNNeighbors(undi, 2, gc, solver));
            gc.addPropagators(new PropAtMostNNeighbors(undi, 2, gc, solver));
            gc.addPropagators(new PropCyclePathChanneling(graph, undi, gc, solver));
        }
        if (config.get(pos)) {
            IntVar[] pos = VariableFactory.boundedArray("pos", n, 0, n - 1, solver);
            try {
                pos[0].instantiateTo(0, Cause.Null);
                pos[n - 1].instantiateTo(n - 1, Cause.Null);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
            gc.addPropagators(new PropPosInTour(pos, graph, gc, solver));
            if (config.get(rg)) {
                gc.addPropagators(new PropPosInTourGraphReactor(pos, graph, gc, solver, nR, sccOf, outArcs, G_R));
            } else {
                gc.addPropagators(new PropPosInTourGraphReactor(pos, graph, gc, solver));
            }
            solver.post(new AllDifferent(pos, solver, AllDifferent.Type.BC));
        }
        // COST BASED FILTERING
        if (instanceName.contains("rbg")) {
            PropKhun map = new PropKhun(graph, totalCost, distanceMatrix, solver, gc);
            gc.addPropagators(map);
            relax = map;
        } else {
            if (config.get(rg) && bst) {// BST-based HK
                System.out.println("BST");
                PropHeldKarp propHK_bst = PropHeldKarp.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs);
                propHK_bst.waitFirstSolution(false);//search!=1 && initialUB!=optimum);
                gc.addPropagators(propHK_bst);
                relax = propHK_bst;
            } else {// MST-based HK
                System.out.println("MST");
                PropHeldKarp propHK_mst = PropHeldKarp.mstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver);
                propHK_mst.waitFirstSolution(false);//search!=1 && initialUB!=optimum);
                gc.addPropagators(propHK_mst);
                relax = propHK_mst;
            }
        }
        solver.post(gc);
    }

    public static void configureAndSolve() {
        //SOLVER CONFIG
        AbstractStrategy mainStrat = StrategyFactory.graphATSP(graph, heuristic, relax);
        switch (main_search) {
            // top-down (default)
            case 0:
                solver.set(mainStrat);
                break;
            // bottom-up
            case 1:
                solver.set(new StaticStrategiesSequencer(new BottomUp_Minimization(totalCost), mainStrat));
                break;
            // dichotomic
            case 2:
                solver.set(new StaticStrategiesSequencer(new Dichotomic_Minimization(totalCost, solver), mainStrat));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor() {
            public void afterInitialPropagation() {
                if (totalCost.instantiated()) {
                    solver.getSearchLoop().stopAtFirstSolution(true);
                }
            }
        });
        SearchMonitorFactory.log(solver, true, false);
        //SOLVE
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
        if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
            throw new UnsupportedOperationException();
        }
        // OUTPUT
        int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
        String configst = "";
        for (int i = 0; i < NB_PARAM; i++) {
            if (config.get(i)) {
                configst += "1;";
            } else {
                configst += "0;";
            }
        }
        if (bst) {
            configst += "1;";
        } else {
            configst += "0;";
        }
        String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" +
                solver.getMeasures().getFailCount() + ";" + solver.getMeasures().getNodeCount() + ";"
                + (int) (solver.getMeasures().getTimeCount()) + ";" + bestCost + ";" + searchMode[main_search] + ";" + configst + "\n";
        writeTextInto(txt, outFile);
    }

    //***********************************************************************************
    // BENCHMARK
    //***********************************************************************************

    public static void main(String[] args) {
        outFile = "atsp_fast.csv";
        clearFile(outFile);
        writeTextInto("instance;sols;fails;nodes;time;obj;search;arbo;rg;undi;pos;adAC;bst;\n", outFile);
        bench();
//		String instance = "/Users/jfages07/github/In4Ga/atsp_instances/ft53.atsp";
//		testInstance(instance);
    }

    private static void bench() {
        String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
        File folder = new File(dir);
        String[] list = folder.list();
        heuristic = ATSP_heuristics.enf_sparse;
        main_search = 1;
        configParameters(0);
        for (String s : list) {
            if ((s.contains(".atsp"))) {// && (!s.contains("ftv170")) && (!s.contains("p43"))){
//				if(s.contains("p43.atsp"))System.exit(0);
                loadInstance(dir + "/" + s);
                if (n > 0 && n < 170) {// || s.contains("p43.atsp")){
                    bst = false;
                    configParameters((1 << allDiff));
                    solve();
                }
            }
        }
    }

    private static void loadInstance(String url) {
        File file = new File(url);
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();
            instanceName = line.split(":")[1].replaceAll(" ", "");
            System.out.println("parsing instance " + instanceName + "...");
            line = buf.readLine();
            line = buf.readLine();
            line = buf.readLine();
            n = Integer.parseInt(line.split(":")[1].replaceAll(" ", "")) + 1;
            distanceMatrix = new int[n][n];
            line = buf.readLine();
            line = buf.readLine();
            line = buf.readLine();
            String[] lineNumbers;
            for (int i = 0; i < n - 1; i++) {
                int nbSuccs = 0;
                while (nbSuccs < n - 1) {
                    line = buf.readLine();
                    line = line.replaceAll(" * ", " ");
                    lineNumbers = line.split(" ");
                    for (int j = 1; j < lineNumbers.length; j++) {
                        if (nbSuccs == n - 1) {
                            i++;
                            if (i == n - 1) break;
                            nbSuccs = 0;
                        }
                        distanceMatrix[i][nbSuccs] = Integer.parseInt(lineNumbers[j]);
                        nbSuccs++;
                    }
                }
            }
            noVal = distanceMatrix[0][0];
            if (noVal == 0) noVal = Integer.MAX_VALUE / 2;
            int maxVal = 0;
            for (int i = 0; i < n; i++) {
                distanceMatrix[i][n - 1] = distanceMatrix[i][0];
                distanceMatrix[n - 1][i] = noVal;
                distanceMatrix[i][0] = noVal;
                for (int j = 0; j < n; j++) {
                    if (distanceMatrix[i][j] != noVal && distanceMatrix[i][j] > maxVal) {
                        maxVal = distanceMatrix[i][j];
                    }
                }
            }
            line = buf.readLine();
            line = buf.readLine();
            initialUB = maxVal * n;
            optimum = Integer.parseInt(line.replaceAll(" ", ""));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    //***********************************************************************************
    // RECORDING RESULTS
    //***********************************************************************************

    public static void writeTextInto(String text, String file) {
        try {
            FileWriter out = new FileWriter(file, true);
            out.write(text);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearFile(String file) {
        try {
            FileWriter out = new FileWriter(file, false);
            out.write("");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}