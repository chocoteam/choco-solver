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
import choco.kernel.common.util.PoolManager;
import choco.kernel.memory.IStateInt;
import samples.graph.input.SOP_Utils;
import samples.graph.output.TextWriter;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.constraints.propagators.gary.IGraphRelaxation;
import solver.constraints.propagators.gary.arborescences.PropAntiArborescence;
import solver.constraints.propagators.gary.arborescences.PropArborescence;
import solver.constraints.propagators.gary.basic.PropAntiSymmetric;
import solver.constraints.propagators.gary.basic.PropTransitiveClosureFastMaintenance;
import solver.constraints.propagators.gary.basic.PropTransitivity;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.tsp.directed.PropKhun;
import solver.constraints.propagators.gary.tsp.directed.PropReducedGraphHamPath;
import solver.constraints.propagators.gary.tsp.directed.PropSCCDoorsRules;
import solver.constraints.propagators.gary.tsp.directed.lagrangianRelaxation.PropLagr_MST_BSTdual;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosGraphWithPreds;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTour;
import solver.constraints.propagators.gary.tsp.directed.position.PropPosInTourGraphReactor;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.assignments.DecisionOperator;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.decision.fast.FastDecision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.search.strategy.strategy.graph.GraphStrategies;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.setDataStructures.ISet;

import java.io.File;
import java.util.BitSet;

/**
 * Solves the Sequential Ordering Problem
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class SOP {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private static final long TIMELIMIT = 10000;
    private static String outFile = "sop.csv";
    private static int seed = 0;
    // instance
    public static String instanceName;
    public static int[][] distanceMatrix;
    public static int n, optimum, initialUB;
    private static Solver solver;
    // model
    private static DirectedGraphVar graph;
    private static IntVar totalCost;
    private static IntVar[] positions;
    private static Constraint gc;
    // RG data structure
    private static IStateInt nR;
    private static IStateInt[] sccOf;
    private static ISet[] outArcs;
    private static DirectedGraph G_R;
    private static IStateInt[] sccFirst, sccNext;
    // Branching data structure
    private static IGraphRelaxation relax;

    //***********************************************************************************
    // MODEL CONFIGURATION
    //***********************************************************************************

    private static int arbo = 0, rg = 1, undirectedMate = 2, pos = 3, allDiff = 4;//,time=5;
    private static int NB_PARAM = 5;
    private static BitSet config = new BitSet(NB_PARAM);
    private static boolean bst;
    private static boolean khun;
    private static int noVal;
    private static DirectedGraphVar transGraph;

    public static void configParameters(int mask) {
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
    private static int policy;
    private static String[] searchMode = new String[]{"top-down", "bottom-up", "dichotomic"};

    //***********************************************************************************
    // MODEL-SEARCH-RESOLUTION-OUTPUT
    //***********************************************************************************

    public static void solve() {
        createModel();
        addPropagators();
        addTransitionGraph();
        configureAndSolve();
        printOutput();
    }

    public static void createModel() {
        // create model
        solver = new Solver();
        initialUB = optimum;
        System.out.println("initial UB : " + optimum);
        graph = new DirectedGraphVar(solver, n, GraphType.ENVELOPE_SWAP_ARRAY, GraphType.LINKED_LIST, true);
        totalCost = VariableFactory.bounded("total cost ", 0, initialUB, solver);
        try {
            for (int i = 0; i < n - 1; i++) {
                for (int j = 1; j < n; j++) {
                    if (distanceMatrix[i][j] != noVal) {
                        graph.getEnvelopGraph().addArc(i, j);
                    }
                }
                graph.getEnvelopGraph().removeArc(i, i);
            }
            graph.getEnvelopGraph().removeArc(0, n - 1);
            graph.getEnvelopGraph().removeArc(n - 1, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        gc = GraphConstraintFactory.atsp(graph, totalCost, distanceMatrix, 0, n - 1, solver);
    }

    public static void addPropagators() {
        // BASIC MODEL
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
        IntVar[] pos = VariableFactory.enumeratedArray("pos", n, 0, n - 1, solver);
        positions = pos;
        try {
            pos[0].instantiateTo(0, Cause.Null);
            pos[n - 1].instantiateTo(n - 1, Cause.Null);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && distanceMatrix[i][j] == -1) {
                    solver.post(ConstraintFactory.lt(pos[j], pos[i], solver));
                }
            }
        }
        solver.post(new AllDifferent(pos, solver, AllDifferent.Type.AC));
        gc.addPropagators(new PropPosInTour(pos, graph, gc, solver));
        if (config.get(rg)) {
            gc.addPropagators(new PropPosInTourGraphReactor(pos, graph, gc, solver, nR, sccOf, outArcs, G_R));
        } else {
            gc.addPropagators(new PropPosInTourGraphReactor(pos, graph, gc, solver));
        }
        if (config.get(rg)) {
            gc.addPropagators(new PropPosGraphWithPreds(pos, graph, distanceMatrix, gc, solver, nR, sccOf, outArcs, G_R));
        } else {
            gc.addPropagators(new PropPosGraphWithPreds(pos, graph, distanceMatrix, gc, solver));
        }
        // COST BASED FILTERING
        if (khun) {
            PropKhun map = new PropKhun(graph, totalCost, distanceMatrix, solver, gc);
            gc.addPropagators(map);
        }
        if (config.get(rg) && bst) {// BST-based HK
            System.out.println("BST");
            PropLagr_MST_BSTdual propHK_bst = PropLagr_MST_BSTdual.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs);
            propHK_bst.waitFirstSolution(initialUB != optimum);
            relax = propHK_bst;
            gc.addPropagators(propHK_bst);
        } else {
            System.out.println("MST");
            PropLagr_MST_BSTdual propHK_mst = PropLagr_MST_BSTdual.mstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver);
            propHK_mst.waitFirstSolution(initialUB != optimum);
            relax = propHK_mst;
            gc.addPropagators(propHK_mst);
        }
        solver.post(gc);
    }

    public static void addTransitionGraph() {
        transGraph = new DirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.KERNEL_SWAP_ARRAY, true);
        for (int j = 1; j < n; j++) {
            transGraph.getEnvelopGraph().addArc(0, j);
            transGraph.getKernelGraph().addArc(0, j);
        }
        for (int i = 1; i < n - 1; i++) {
            for (int j = 1; j < n; j++) {
                if (i != j && distanceMatrix[i][j] != noVal)
                    transGraph.getEnvelopGraph().addArc(i, j);
            }
        }
        gc.addPropagators(new PropAntiSymmetric(transGraph, gc, solver));
        gc.addPropagators(new PropTransitivity(transGraph, solver, gc));
        gc.addPropagators(new PropTransitiveClosureFastMaintenance(graph, transGraph, solver, gc));
    }

    public static void configureAndSolve() {
        //SOLVER CONFIG
        GraphStrategies mainStrat = new GraphStrategies(graph, distanceMatrix, relax);
        mainStrat.configure(policy, true, true, true);
//		GraphStrategies mainStrat = new GraphStrategies(transGraph,distanceMatrix,relax);
//		mainStrat.configure(GraphStrategies.MAX_DELTA_DEGREE,true,true,false);
//		AbstractStrategy mainStrat = StrategyFactory.ABSrandom(positions, solver, 0.999d, 0.2d, 8, 1.1d, 1, 0);
//		AbstractStrategy mainStrat = new MySearch(positions,solver);
        switch (main_search) {
            case 0:
                solver.set(mainStrat);
                break;
            case 1:
                solver.set(new StaticStrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), mainStrat));
                break;
            case 2:
                solver.set(new StaticStrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.DICHOTOMIC), mainStrat));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        PropagationEngine pengine = new PropagationEngine(solver.getEnvironment());
        PArc allArcs = new PArc(pengine, gc);
        solver.set(pengine.set(new Sort(allArcs).clearOut()));
        solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor() {
            public void afterInitialPropagation() {
                if (totalCost.instantiated()) {
                    solver.getSearchLoop().stopAtFirstSolution(true);
                }
                System.out.println("obj after initial prop : " + totalCost);
            }
        });
        SearchMonitorFactory.log(solver, true, false);
        //SOLVE
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
    }

    public static void printOutput() {
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
                + (int) (solver.getMeasures().getTimeCount()) + ";" + bestCost + ";" + searchMode[main_search] + ";" + configst + ";\n";
        TextWriter.writeTextInto(txt, outFile);
    }

    //***********************************************************************************
    // BENCHMARK
    //***********************************************************************************

    public static void main(String[] args) {
        TextWriter.clearFile(outFile);
        TextWriter.writeTextInto("instance;sols;fails;nodes;time;obj;m;search;arbo;rg;undi;pos;adAC;bst;\n", outFile);
//		test();
        String dir = "/Users/jfages07/github/In4Ga/ALL_sop";
        File folder = new File(dir);
        String[] list = folder.list();
        main_search = 0;
        khun = false;
        policy = GraphStrategies.MAX_DELTA_DEGREE;
        configParameters(0);
        for (String s : list) {
            if (!s.contains("43.1"))
                if ((s.contains(".sop"))) {
                    loadInstance(dir + "/" + s);
                    if (n > 0 && n < 400) {
                        bst = false;
//						configParameters((1<<allDiff));
//						solve();
//						configParameters((1<<arbo)+(1<<allDiff));
//						solve();
//						configParameters((1<<rg)+(1<<allDiff));
//						solve();
//						bst = true;
//						configParameters((1<<allDiff)+(1<<rg));
//						solve();
                        configParameters((1 << rg) + (1 << arbo) + (1 << allDiff));
                        solve();
                    }
                }
        }
    }

    private static void loadInstance(String url) {
        SOP_Utils inst = new SOP_Utils();
        inst.loadTSPLIBInstance(url);
        n = inst.n;
        instanceName = inst.instanceName;
        distanceMatrix = inst.distanceMatrix;
        noVal = inst.noVal;
        initialUB = inst.initialUB;
        optimum = inst.optimum;
    }

    //***********************************************************************************
    // Search n Test
    //***********************************************************************************

    private static class MySearch extends AbstractStrategy<IntVar> {
        private PoolManager<FastDecision> pool;

        public MySearch(IntVar[] positions, Solver solver) {
            super(positions);
            this.pool = new PoolManager<FastDecision>();
        }

        @Override
        public void init() {
        }

        @Override
        public Decision getDecision() {
            int ms = 0;
            IntVar var = null;
            for (int i = 0; i < n; i++) {
                if (!vars[i].instantiated()) {
                    if (vars[i].getDomainSize() > ms) {
                        var = vars[i];
                        ms = var.getDomainSize();
                    }
                }
            }
            if (var == null) {
                return null;
            }
            FastDecision dec = pool.getE();
            if (dec == null) {
                dec = new FastDecision(pool);
            }
            dec.set(var, (var.getUB() + var.getLB()) / 2, DecisionOperator.int_split);
            return dec;
        }
    }

    public static void test() {
        n = 7;
        distanceMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                distanceMatrix[i][j] = 1000;
            }
        }
        distanceMatrix[0][1] = 2;
        distanceMatrix[0][2] = 3;
        distanceMatrix[2][1] = distanceMatrix[1][2] = 10;
        distanceMatrix[1][3] = 5;
        distanceMatrix[2][4] = 3;
        distanceMatrix[3][4] = distanceMatrix[4][3] = 6;
        distanceMatrix[3][5] = distanceMatrix[5][3] = 5;
        distanceMatrix[5][4] = distanceMatrix[4][5] = 5;
        distanceMatrix[3][6] = 4;
        distanceMatrix[5][6] = 2;

//		distanceMatrix[5][3] = -1;
//		distanceMatrix[4][3] = -1;

        initialUB = optimum = 28;
        configParameters((1 << allDiff));
//		solve();
        bst = true;
        configParameters((1 << rg) + (1 << allDiff));
        solve();
        System.out.println("Hello World");
        System.exit(0);

    }
}