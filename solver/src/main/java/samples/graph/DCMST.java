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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.basic.PropKCC;
import solver.constraints.propagators.gary.degree.PropAtLeastNNeighbors;
import solver.constraints.propagators.gary.degree.PropAtMostNNeighbors;
import solver.constraints.propagators.gary.trees.PropTreeEvalObj;
import solver.constraints.propagators.gary.trees.PropTreeNoSubtour;
import solver.constraints.propagators.gary.trees.lagrangianRelaxation.PropIterativeMST;
import solver.constraints.propagators.gary.trees.lagrangianRelaxation.PropTreeHeldKarp;
import solver.exception.ContradictionException;
import solver.objective.strategies.BottomUp_Minimization;
import solver.objective.strategies.Dichotomic_Minimization;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.TSP_heuristics;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class DCMST {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    //	private static int upperBound;
    // input
    private static String dir = "/Users/jfages07/Desktop/Constrained Trees/instances";
    private static String instanceName;
    private static int n, nMin, nMax;
    private static int[] dMax;
    private static int[][] dist;
    // model
    private static IntVar totalCost;
    private static Solver solver;
    private static int search;
    private static int lb, ub;
    // other
    private static final long TIMELIMIT = 30000;
    private static String outFile;
    private static PropTreeHeldKarp hk;
    private static final boolean optGiven = true;

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public static void main(String[] args) {
        //DE,DR,instanciasT
        bench("DR");
    }

    public static void bench(String type) {
        if (optGiven) {
            search = 0;
        } else {
            search = 2;
        }
        HCP_Parser.clearFile(outFile = "big_dcmst_" + type + "_s" + search + ".csv");
        HCP_Parser.writeTextInto("instance;sols;fails;nodes;time;obj;search;\n", outFile);
        File folder = new File(dir + "/" + type);
        String[] list = folder.list();
        nMin = 500;
        nMax = 700;
        for (String s : list) {
            File file = new File(dir + "/" + type + "/" + s);
            if ((!file.isHidden()) && (!s.contains("bounds.csv")) && (!s.contains("bug"))) {
                instanceName = s;
                System.out.println(s);
                if (parse(file)) {
                    if (optGiven) setUB(dir + "/" + type, s);
                    solveDCMST(s);
//					System.exit(0);
                }
                System.gc();
            }
        }
    }

    public static boolean parse(File file) {
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();
            String[] numbers;
            n = Integer.parseInt(line);
            if (n < nMin || n > nMax) {
                return false;
            }
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
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        throw new UnsupportedOperationException();
    }

    private static void setUB(String dir, String inst) {
        if (dir.contains("ham")) {
            setHamUB(dir, inst);
            return;
        }
        File file = new File(dir + "/bounds.csv");
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();
            String[] numbers;
            line = buf.readLine();
            while (line != null) {
                numbers = line.split(";");
                if (n == Integer.parseInt(numbers[0])) {
                    if (inst.contains("0_1")) {
                        // nothing to do
                    } else if (inst.contains("0_2")) {
                        line = buf.readLine();
                        numbers = line.split(";");
                    } else if (inst.contains("0_3")) {
                        line = buf.readLine();
                        line = buf.readLine();
                        numbers = line.split(";");
                    } else if (inst.contains("0_4")) {
                        line = buf.readLine();
                        line = buf.readLine();
                        line = buf.readLine();
                        numbers = line.split(";");
                    } else if (inst.contains("0_5")) {
                        line = buf.readLine();
                        line = buf.readLine();
                        line = buf.readLine();
                        line = buf.readLine();
                        numbers = line.split(";");
                    } else {
                        throw new UnsupportedOperationException(inst);
                    }
                    ub = Integer.parseInt(numbers[2]);
                    System.out.println("ub : " + ub);
                    return;
                }
                line = buf.readLine();
            }
            System.out.println("no bound");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void setHamUB(String dir, String inst) {
        File file = new File(dir + "/bounds.csv");
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line = buf.readLine();
            String[] numbers;
            line = buf.readLine();
            while (line != null) {
                numbers = line.split(";");
                if (n == Integer.parseInt(numbers[0])) {
                    if (inst.contains("0_0")) {
                        // nothing to do
                    } else if (inst.contains("0_1")) {
                        line = buf.readLine();
                        numbers = line.split(";");
                    } else if (inst.contains("0_2")) {
                        line = buf.readLine();
                        line = buf.readLine();
                        numbers = line.split(";");
                    } else {
                        throw new UnsupportedOperationException(inst);
                    }
                    ub = Integer.parseInt(numbers[2]);
                    System.out.println("ub : " + ub);
                    return;
                }
                line = buf.readLine();
            }
            System.out.println("no bound");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void solveDCMST(String instanceName) {
        solver = new Solver();
        // variables
        totalCost = VariableFactory.bounded("obj", lb, ub, solver);
        final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST);
        for (int i = 0; i < n; i++) {
            undi.getKernelGraph().activateNode(i);
            for (int j = i + 1; j < n; j++) {
                if (dist[i][j] != -1) {
                    undi.getEnvelopGraph().addEdge(i, j);
                }
            }
        }
        // constraints
        Constraint gc = GraphConstraintFactory.makeConstraint(solver);
        gc.addPropagators(new PropAtLeastNNeighbors(undi, 1, gc, solver));
        gc.addPropagators(new PropAtMostNNeighbors(undi, dMax, gc, solver));
        gc.addPropagators(new PropTreeNoSubtour(undi, gc, solver));
        gc.addPropagators(new PropKCC(undi, solver, gc, VariableFactory.bounded("1", 1, 1, solver)));
        gc.addPropagators(new PropTreeEvalObj(undi, totalCost, dist, gc, solver));
        hk = PropTreeHeldKarp.mstBasedRelaxation(undi, totalCost, dMax, dist, gc, solver);
        hk.waitFirstSolution(!optGiven);
        gc.addPropagators(hk);
        gc.addPropagators(PropIterativeMST.mstBasedRelaxation(undi, totalCost, dMax, dist, gc, solver));
        solver.post(gc);

        // config
//		AbstractStrategy strat = StrategyFactory.graphStrategy(undi,null,new MySearch(undi), GraphStrategy.NodeArcPriority.ARCS);
        AbstractStrategy firstSol = StrategyFactory.graphStrategy(undi, null, new MinCost(undi), GraphStrategy.NodeArcPriority.ARCS);
//		AbstractStrategy middle   = StrategyFactory.graphStrategy(undi,null,new MaxRC(undi), GraphStrategy.NodeArcPriority.ARCS);
        AbstractStrategy proveOpt = StrategyFactory.graphTSP(undi, TSP_heuristics.enf_multisparse, null);
//		AbstractStrategy strat = StrategyFactory.graphLexico(undi);
//		middle = proveOpt;
        AbstractStrategy strat = new Change(undi, firstSol, proveOpt);
        switch (search) {
            case 0:
                solver.set(proveOpt);
                break;
            case 1:
                solver.set(new StaticStrategiesSequencer(new BottomUp_Minimization(totalCost), strat));
                break;
            case 2:
                solver.set(new StaticStrategiesSequencer(new Dichotomic_Minimization(totalCost, solver), strat));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
        solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
        solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
        SearchMonitorFactory.log(solver, true, false);
        // resolution
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
        if (solver.getMeasures().getSolutionCount() == 0 && solver.getMeasures().getTimeCount() < TIMELIMIT) {
            throw new UnsupportedOperationException();
        }
        if (solver.getMeasures().getSolutionCount() > 1) {
//			throw new UnsupportedOperationException();
        }
        //output
        int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
        String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
                + solver.getMeasures().getNodeCount() + ";" + (int) (solver.getMeasures().getTimeCount()) + ";" + bestCost + ";" + search + ";\n";
        HCP_Parser.writeTextInto(txt, outFile);
    }

    private static class MinKerDeg extends ArcStrategy<UndirectedGraphVar> {

        public MinKerDeg(UndirectedGraphVar g) {
            super(g);
        }

        @Override
        public boolean computeNextArc() {
            return milleFeuille();
//			return biggest();
        }

        private boolean milleFeuille() {
            int minK = n;
            int minE = n + 1;
            int ker, env;
            for (int i = 0; i < n; i++) {
                ker = g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize();
                env = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
                if (env != ker) {
                    if (ker < minK) {
                        minK = ker;
                        minE = env;
                    } else if (ker == minK && env < minE) {
                        minE = env;
                    }
                }
            }
            if (minE == minK) {
                from = to = -1;
                return false;
            }
            int cost;
            int minCost = 0;
            from = -1;
            for (int i = 0; i < n; i++) {
                ker = g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize();
                env = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
                if (ker == minK && env == minE) {
                    INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        if (!g.getKernelGraph().arcExists(i, j)) {
                            cost = dist[i][j];
                            if (from == -1 || cost < minCost) {
                                minCost = cost;
                                this.from = i;
                                this.to = j;
                            }
                        }
                    }
                }
            }
            if (from == -1) {
                throw new UnsupportedOperationException();
            }
            return true;
        }

        private boolean biggest() {
            int deltaMax = 0;
            int delta;
            for (int i = 0; i < n; i++) {
                delta = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()
                        - g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize();
                if (delta > deltaMax) {
                    deltaMax = delta;
                }
            }
            if (deltaMax == 0) {
                from = to = -1;
                return false;
            }
            int cost;
            int minCost = 0;
            from = -1;
            for (int i = 0; i < n; i++) {
                delta = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize()
                        - g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize();
                if (delta == deltaMax) {
                    INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(i);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        if (!g.getKernelGraph().arcExists(i, j)) {
                            cost = dist[i][j];
                            if (from == -1 || cost < minCost) {
                                minCost = cost;
                                this.from = i;
                                this.to = j;
                            }
                        }
                    }
                }
            }
            if (from == -1) {
                throw new UnsupportedOperationException();
            }
            return true;
        }
    }

    private static class MySearch extends ArcStrategy<UndirectedGraphVar> {

        public MySearch(UndirectedGraphVar g) {
            super(g);
        }

        @Override
        public boolean computeNextArc() {
            int deltaMin = n + 1;
            int ker, env;
            from = -1;
            to = -1;
            for (int i = 0; i < n; i++) {
                ker = g.getKernelGraph().getSuccessorsOf(i).neighborhoodSize();
                env = g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize();
                if (env != ker) {
                    if (dMax[i] - ker < deltaMin) {
                        deltaMin = dMax[i] - ker;
                        from = i;
                    }
                }
            }
            if (from == -1) {
                return false;
            }
            int cost;
            int minCost = 0;
            INeighbors nei = g.getEnvelopGraph().getSuccessorsOf(from);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (!g.getKernelGraph().arcExists(from, j)) {
                    cost = dist[from][j];
                    if (to == -1 || cost < minCost) {
                        minCost = cost;
                        to = j;
                    }
                }
            }
            if (from == -1 || to == -1) {
                throw new UnsupportedOperationException();
            }
            return true;
        }
    }

    private static class MaxRC extends ArcStrategy<UndirectedGraphVar> {

        public MaxRC(UndirectedGraphVar g) {
            super(g);
        }

        @Override
        public boolean computeNextArc() {
            from = -1;
            to = -1;
            double cost, bestCost = 0;
            INeighbors nei;
            for (int i = 0; i < n; i++) {
                nei = hk.getMST().getSuccessorsOf(i);
                for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                    if (!g.getKernelGraph().arcExists(i, j)) {
                        cost = dist[i][j];//hk.getReplacementCost(i,j);
                        if (from == -1 || cost < bestCost) {
                            bestCost = cost;
                            from = i;
                            to = j;
                        }
                    }
                }
            }
            if (from == -1) {
                return false;
            }
            return true;
        }
    }

    private static class MinCost extends ArcStrategy<UndirectedGraphVar> {

        public MinCost(UndirectedGraphVar g) {
            super(g);
        }

        @Override
        public boolean computeNextArc() {
            from = -1;
            to = -1;
            int minCost = 0;
            INeighbors env, ker;
            for (int i = 0; i < n; i++) {
                ker = g.getKernelGraph().getSuccessorsOf(i);
                env = g.getEnvelopGraph().getSuccessorsOf(i);
                if (env.neighborhoodSize() != ker.neighborhoodSize()) {
                    for (int j = env.getFirstElement(); j >= 0; j = env.getNextElement()) {
                        if (i < j && !ker.contain(j)) {
                            int cost = dist[i][j];
                            if (to == -1 || cost < minCost) {
                                minCost = cost;
                                from = i;
                                to = j;
                            }
                        }
                    }
                }
            }
            if (from == -1) {
                return false;
            }
            return true;
        }
    }

    private static class Change extends AbstractStrategy<UndirectedGraphVar> {

        AbstractStrategy[] strats;

        public Change(UndirectedGraphVar g, AbstractStrategy... strats) {
            super(new UndirectedGraphVar[]{g});
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
}