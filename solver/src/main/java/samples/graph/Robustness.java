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

import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.arborescences.PropAntiArborescence;
import solver.constraints.propagators.gary.arborescences.PropArborescence;
import solver.constraints.propagators.gary.constraintSpecific.PropAllDiffGraphIncremental;
import solver.constraints.propagators.gary.degree.PropAtLeastNPredecessors;
import solver.constraints.propagators.gary.degree.PropAtLeastNSuccessors;
import solver.constraints.propagators.gary.degree.PropAtMostNPredecessors;
import solver.constraints.propagators.gary.degree.PropAtMostNSuccessors;
import solver.constraints.propagators.gary.tsp.directed.PropPathNoCycle;
import solver.constraints.propagators.gary.tsp.directed.PropReducedGraphHamPath;
import solver.constraints.propagators.gary.tsp.directed.PropSCCDoorsRules;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import solver.variables.graph.directedGraph.IDirectedGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

/**
 * Parse and solve an Asymmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class Robustness {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private static final long TIMELIMIT = 60000;
    private static String outFile;
    private static long seed = 0;
    // instance
    private static String instanceName;
    private static boolean[][] input;
    private static int n;
    private static Solver solver;
    // model
    private static DirectedGraphVar graph;
    private static Constraint gc;
    // RG data structure
    private static IStateInt nR;
    private static IStateInt[] sccOf;
    private static INeighbors[] outArcs;
    private static IDirectedGraph G_R;
    private static IStateInt[] sccFirst, sccNext;

    //***********************************************************************************
    // MODEL CONFIGURATION
    //***********************************************************************************

    private static int arbo = 0, rg = 1;//,undirectedMate=2,pos=3;
    private static int NB_PARAM = 4;
    private static BitSet config = new BitSet(NB_PARAM);

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
        graph = new DirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
        try {
            for (int i = 0; i < n - 1; i++) {
                graph.getKernelGraph().activateNode(i);
                for (int j = 0; j < n; j++) {
                    if (input[i][j]) {
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
        int[] succs = new int[n];
        int[] preds = new int[n];
        for (int i = 0; i < n; i++) {
            succs[i] = preds[i] = 1;
        }
        succs[n - 1] = preds[0] = 0;
        gc.addPropagators(new PropAtMostNSuccessors(graph, succs, gc, solver));
        gc.addPropagators(new PropAtLeastNSuccessors(graph, succs, gc, solver));
        gc.addPropagators(new PropAtMostNPredecessors(graph, preds, gc, solver));
        gc.addPropagators(new PropAtLeastNPredecessors(graph, preds, gc, solver));

        gc.addPropagators(new PropPathNoCycle(graph, 0, n - 1, gc, solver));
        gc.addPropagators(new PropAllDiffGraphIncremental(graph, n - 1, solver, gc));
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
        solver.post(gc);
    }

    public static void configureAndSolve() {
        //SOLVER CONFIG
//		AbstractStrategy mainStrat = StrategyFactory.graphATSP(graph, ATSP_heuristics.enf_sparse, null);
        AbstractStrategy mainStrat = StrategyFactory.graphStrategy(graph, null, new OrderedArcs(graph, seed), GraphStrategy.NodeArcPriority.ARCS);
        solver.set(mainStrat);
//		solver.set(StrategyFactory.graphLexico(graph));
        IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
        solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
        solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        SearchMonitorFactory.log(solver, true, false);
        //SOLVE
        solver.findSolution();
        if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
            throw new UnsupportedOperationException();
        }
        // OUTPUT
        String configst = "";
        for (int i = 0; i < NB_PARAM; i++) {
            if (config.get(i)) {
                configst += "1;";
            } else {
                configst += "0;";
            }
        }
        String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" +
                solver.getMeasures().getFailCount() + ";" + solver.getMeasures().getNodeCount() + ";"
                + (int) (solver.getMeasures().getTimeCount()) + ";" + seed + ";" + configst + "\n";
        HCP_Parser.writeTextInto(txt, outFile);
    }

    //***********************************************************************************
    // BENCHMARK
    //***********************************************************************************

    public static void main(String[] args) {
        outFile = "hpp.csv";
        HCP_Parser.clearFile(outFile);
        HCP_Parser.writeTextInto("instance;sols;fails;nodes;time;seed;arbo;rg;undi;pos;\n", outFile);
        benchRD();
    }

    private static void bench() {
        String dir = "/Users/jfages07/Documents/code/ALL_hcp";
        File folder = new File(dir);
        String[] list = folder.list();
        for (String s : list) {
            if ((s.contains(".hcp"))) {
                instanceName = s;
                input = HCP_Parser.transformMatrix(HCP_Parser.parseInstance(dir + "/" + s));
                n = input.length;
                if (n > 50 && n < 10070 && !s.contains("p43.atsp")) {
                    configParameters(0);
                    solve();
                    configParameters((1 << arbo));
                    solve();
                    configParameters((1 << rg));
                    solve();
                    configParameters((1 << arbo) + (1 << rg));
                    solve();
                }
            }
        }
    }

    private static void benchRD() {
        int[] sizes = new int[]{5, 10, 25, 50, 100};
        for (int s : sizes) {
            for (int k = 0; k < 100; k++) {
                seed = k;//System.currentTimeMillis();
                instanceName = s + "";
                GraphGenerator gen = new GraphGenerator(s, seed, GraphGenerator.InitialProperty.HamiltonianCircuit);
                input = HCP_Parser.transformMatrix(gen.neighborBasedGenerator(4));
                n = input.length;
                if (n > 0 && n < 10070) {
                    configParameters(0);
                    solve();
                    configParameters((1 << arbo));
                    solve();
                    configParameters((1 << rg));
                    solve();
                    configParameters((1 << arbo) + (1 << rg));
                    solve();
                }
            }
        }
    }

    //***********************************************************************************
    // ASSIGNMENTS & GLOBAL BRANCHING
    //***********************************************************************************
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class OrderedArcs extends ArcStrategy<GraphVar> {

        private ArrayList<Integer> arcs;

        public OrderedArcs(GraphVar g, long seed) {
            super(g);
            arcs = new ArrayList<Integer>(n * n);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    arcs.add(i * n + j);
                }
            }
            Collections.shuffle(arcs, new Random(seed));
        }

        @Override
        public boolean computeNextArc() {
            int i, j;
            for (int a : arcs) {
                i = a / n;
                j = a % n;
                if (g.getEnvelopGraph().arcExists(i, j) && !g.getKernelGraph().arcExists(i, j)) {
                    this.from = i;
                    this.to = j;
                    return true;
                }
            }
            this.from = this.to = -1;
            return false;
        }
    }
}