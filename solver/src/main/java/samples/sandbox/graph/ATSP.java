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

package samples.sandbox.graph;

import choco.kernel.ResolutionPolicy;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.setDataStructures.ISet;
import choco.kernel.memory.setDataStructures.SetType;
import samples.sandbox.graph.input.ATSP_Utils;
import samples.sandbox.graph.output.TextWriter;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.IGraphRelaxation;
import solver.constraints.propagators.gary.arborescences.PropAntiArborescence;
import solver.constraints.propagators.gary.arborescences.PropArborescence;
import solver.constraints.propagators.gary.tsp.directed.*;
import solver.constraints.propagators.gary.tsp.directed.lagrangianRelaxation.PropLagr_MST_BSTdual;
import solver.exception.ContradictionException;
import solver.objective.ObjectiveStrategy;
import solver.objective.OptimizationPolicy;
import solver.search.loop.monitors.IMonitorInitPropagation;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.search.strategy.strategy.graph.GraphStrategies;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.DirectedGraph;
import solver.variables.graph.DirectedGraphVar;

import java.io.File;
import java.util.BitSet;
import java.util.Random;

/**
 * Solves the Asymmetric Traveling Salesman Problem
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class ATSP {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private static final long TIMELIMIT = 30000;
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
    private static ISet[] outArcs;
    private static DirectedGraph G_R;
    private static IStateInt[] sccFirst, sccNext;
    // Branching data structure
    private static IGraphRelaxation relax;
    private static double bcTime;

    //***********************************************************************************
    // MODEL CONFIGURATION
    //***********************************************************************************

    private static int arbo = 0, rg = 1, allDiff = 2;
    private static boolean khun;
    private static int NB_PARAM = 3;
    private static BitSet config = new BitSet(NB_PARAM);
    private static boolean bst;

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
    private static String[] searchMode = new String[]{"top-down", "bottom-up", "dichotomic"};

    //***********************************************************************************
    // BENCHMARK
    //***********************************************************************************

    public static void main(String[] args) {
        outFile = "atsp_fast.csv";
        TextWriter.clearFile(outFile);
        TextWriter.writeTextInto("instance;sols;fails;nodes;time;obj;search;arbo;rg;pos;adAC;bst;\n", outFile);
        bench();
//        benchRandomWithSCC();
    }

    private static void bench() {
        String dir = "/Users/jfages07/github/In4Ga/atsp_instances";
        File folder = new File(dir);
        String[] list = folder.list();
        main_search = 0;
        khun = true;
        for (String s : list) {
            if ((s.contains(".atsp"))) {// && (!s.contains("ftv170")) && (!s.contains("p43"))){
//				if(s.contains("p43.atsp"))System.exit(0);
                loadTSPLIBInstance(dir + "/" + s);
                if (n > 0 && n < 160) {// || s.contains("p43.atsp")){
                    bst = false;
                    configParameters((1 << allDiff));
//                    solve();
                    bst = true;
                    configParameters((1 << rg) + (1 << allDiff));
                    solve();
                }
            }
        }
    }

    private static void benchRandom() {
        bst = false;
        main_search = 2;
        int[] sizes = new int[]{200};
        int[] costs = new int[]{10, 20};
        for (int s : sizes) {
            for (int c : costs) {
                for (int k = 0; k < 50; k++) {
                    generateInstance(s, c, System.currentTimeMillis());
                    bst = false;
                    configParameters(0);
                    solve();
                    bst = true;
                    configParameters(1 << rg);
                    solve();
                }
            }
        }
    }

    private static void benchRandomWithSCC() {
        TextWriter.clearFile(outFile);
        TextWriter.writeTextInto("n;cMax;sccMax;sols;fails;nodes;time;obj;search;arbo;rg;pos;adAC;bst;bcTime;\n", outFile);
        main_search = 1;
        int[] sizes = new int[]{100};
        int[] costs = new int[]{10};
        Random rd = new Random(0);
        for (int s : sizes) {
            for (int c : costs) {
                for (int k = 0; k < 10; k++) {
                    for (int scc = 0; scc < 30; scc++) {
                        generateInstanceWithSCC(s, c, scc, rd);
                        bst = false;
//						configParameters(1<<allDiff);
//						solve();
                        configParameters((1 << allDiff) + (1 << rg));
                        solve();
                        bst = true;
                        solve();
                    }
                }
            }
        }
    }

    //***********************************************************************************
    // MODEL-SEARCH-RESOLUTION-OUTPUT
    //***********************************************************************************

    public static void solve() {
        createModel();
        addPropagators();
        configureAndSolve();
        printOutput();
    }

    public static void createModel() {
        // create model
        solver = new Solver();
        initialUB = optimum;
        System.out.println(initialUB);
        System.out.println("initial UB : " + initialUB);
        graph = new DirectedGraphVar("G", solver, n, SetType.ENVELOPE_BEST, SetType.LINKED_LIST, true);
        totalCost = VariableFactory.bounded("total cost ", 0, initialUB, solver);
        try {
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n; j++) {
                    if (distanceMatrix[i][j] != noVal) {
                        graph.getEnvelopGraph().addArc(i, j);
                    }
                }
                graph.getEnvelopGraph().removeArc(i, i);
            }
            graph.getEnvelopGraph().removeArc(0, n - 1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        gc = GraphConstraintFactory.atsp(graph, totalCost, distanceMatrix, 0, n - 1);
    }

    public static void addPropagators() {
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
        if (khun) {
//			PropKhun map = new PropKhun(graph,totalCost,distanceMatrix,solver,gc);
//			gc.addPropagators(map);
//			relax = map;
            gc.addPropagators(new PropATSP_AssignmentBound(graph, totalCost, distanceMatrix, gc, solver));
        }
        // COST BASED FILTERING
        if (instanceName.contains("rbg")) {
            if (!khun) {
                PropKhun map = new PropKhun(graph, totalCost, distanceMatrix, solver, gc);
                gc.addPropagators(map);
                relax = map;
            }
        } else {
            if (config.get(rg) && bst) {// BST-based HK
                System.out.println("BST");
                PropLagr_MST_BSTdual propHK_bst = PropLagr_MST_BSTdual.bstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver, nR, sccOf, outArcs);
                propHK_bst.waitFirstSolution(initialUB != optimum);//search!=1 && initialUB!=optimum);
                gc.addPropagators(propHK_bst);
                relax = propHK_bst;
            } else {
                System.out.println("MST");
                PropLagr_MST_BSTdual propHK_mst = PropLagr_MST_BSTdual.mstBasedRelaxation(graph, 0, n - 1, totalCost, distanceMatrix, gc, solver);
                propHK_mst.waitFirstSolution(initialUB != optimum);//search!=1 && initialUB!=optimum);
                gc.addPropagators(propHK_mst);
                relax = propHK_mst;
            }
        }
        solver.post(gc);
    }

    public static void configureAndSolve() {
        //SOLVER CONFIG
//		AbstractStrategy mainStrat = StrategyFactory.graphLexico(graph);

        GraphStrategies first = new GraphStrategies(graph, distanceMatrix, relax);
        first.configure(9, true);
        GraphStrategies next = new GraphStrategies(graph, distanceMatrix, relax);
        next.configure(12, true);
        AbstractStrategy mainStrat = new Change(graph, first, next);

        switch (main_search) {
            // top-down (default)
            case 0:
                solver.set(mainStrat);
                break;
            // bottom-up
            case 1:
                solver.set(new StaticStrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.BOTTOM_UP), mainStrat));
                break;
            // dichotomic
            case 2:
                solver.set(new StaticStrategiesSequencer(new ObjectiveStrategy(totalCost, OptimizationPolicy.DICHOTOMIC), mainStrat));
                break;
            default:
                throw new UnsupportedOperationException();
        }
//        DSLEngine pengine = new DSLEngine(solver.getEnvironment());
//        PArc allArcs = new PArc(pengine, gc);
//        solver.set(pengine.set(new Sort(allArcs).clearOut()));
        solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        solver.getSearchLoop().plugSearchMonitor(new IMonitorInitPropagation() {
            @Override
            public void beforeInitialPropagation() {
            }

            @Override
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
            configst += "1";
        } else {
            configst += "0";
        }
        String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" +
                solver.getMeasures().getFailCount() + ";" + solver.getMeasures().getNodeCount() + ";"
                + (int) (solver.getMeasures().getTimeCount()) + ";" + bestCost + ";" + searchMode[main_search] + ";" + configst + ";" + (bcTime * 1000) + "\n";
        TextWriter.writeTextInto(txt, outFile);
    }

    private static class Change extends AbstractStrategy<DirectedGraphVar> {

        AbstractStrategy[] strats;

        public Change(DirectedGraphVar g, AbstractStrategy... strats) {
            super(new DirectedGraphVar[]{g});
            this.strats = strats;
        }

        @Override
        public void init() throws ContradictionException {
            for (int i = 0; i < strats.length; i++) {
                strats[i].init();
            }
        }

        @Override
        public Decision getDecision() {
            if (solver.getMeasures().getSolutionCount() == 0) {
                return strats[0].getDecision();
            }
            return strats[1].getDecision();
        }
    }

    // RANDOM INSTANCES
    private static void generateInstance(int size, int maxCost, long seed) {
        ATSP_Utils inst = new ATSP_Utils();
        inst.generateInstance(size, maxCost, seed);
        n = inst.n;
        instanceName = inst.instanceName;
        distanceMatrix = inst.distanceMatrix;
        noVal = inst.noVal;
        initialUB = inst.initialUB;
        optimum = inst.optimum;
    }

    private static void generateInstanceWithSCC(int size, int maxCost, int maxSCC, Random rd) {
        ATSP_Utils inst = new ATSP_Utils();
        inst.generateWithSCCStructure(size, maxCost, maxSCC, rd);
        n = inst.n;
        instanceName = inst.instanceName;
        distanceMatrix = inst.distanceMatrix;
        noVal = inst.noVal;
        initialUB = inst.initialUB;
        optimum = inst.optimum;
    }

    // TSPLIB INSTANCES
    private static void loadTSPLIBInstance(String url) {
        ATSP_Utils inst = new ATSP_Utils();
        inst.loadTSPLIB(url);
        n = inst.n;
        instanceName = inst.instanceName;
        distanceMatrix = inst.distanceMatrix;
        noVal = inst.noVal;
        initialUB = inst.initialUB;
        optimum = inst.optimum;
    }

    // OTHER INSTANCES
    public static void benchOthers() {
        TextWriter.clearFile(outFile);
        TextWriter.writeTextInto("instance;sols;fails;nodes;time;obj;search;arbo;rg;undi;pos;adAC;bst;bcTime;\n", outFile);
        String dir = "/Users/jfages07/github/In4Ga/newATSP";
        File folder = new File(dir);
        String[] list = folder.list();
        main_search = 0;
        configParameters(0);
        for (String s : list) {
            File file = new File(dir + "/" + s);
            if (ATSP_Utils.canParse(s))
                if (file.isFile() && !(file.isHidden() || s.contains(".xls") || s.contains(".csv")))
                    if ((s.contains(".atsp") || true)) {
                        loadNewInstance(file.getAbsolutePath());
                        if (n > 0 && n < 1000 && bcTime < 300) {
                            khun = true;
                            bst = false;
                            configParameters((1 << allDiff));
                            solve();
                            configParameters((1 << rg) + (1 << allDiff));
                            solve();
                            bst = true;
                            solve();
                        }
                    }
        }
    }

    private static void loadNewInstance(String name) {
        ATSP_Utils inst = new ATSP_Utils();
        inst.loadNewInstancesBUG(name, "/Users/jfages07/github/In4Ga/newATSP/optima.csv");
        n = inst.n;
        instanceName = inst.instanceName;
        distanceMatrix = inst.distanceMatrix;
        noVal = inst.noVal;
        initialUB = inst.initialUB;
        optimum = inst.optimum;
        bcTime = inst.loadNewTime(name, "/Users/jfages07/github/In4Ga/newATSP/BCresults.csv");
    }
}