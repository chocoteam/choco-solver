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

package samples.parallel;

import choco.kernel.ResolutionPolicy;
import solver.Cause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtMost;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.constraints.propagators.gary.tsp.undirected.lagrangianRelaxation.PropLagr_OneTree;
import solver.exception.ContradictionException;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.search.loop.monitors.Abstract_LNS_SearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.variables.setDataStructures.ISet;

import java.io.File;
import java.util.Random;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class Sequential_LNS {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // general
    private static String outFile;
    // instance
    private static int optimum;
    private static int[][] distMatrix;
    private static int n;
    // LNS
//	private static int[] bestSolution;
//	private static int bestCost;

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public static void main(String[] args) {
        // set input
        if (args.length < 4 || !(args[0].equals("-dir") && args[2].equals("-optFile"))) {
            throw new UnsupportedOperationException("please insert first the input directory " +
                    "using the command -dir and second the path of the file containing optimum values" +
                    "using command -optFile");
        }
        String dir = args[1]; // "/Users/jfages07/github/In4Ga/benchRousseau";
        String optFile = args[3]; // "/Users/jfages07/github/In4Ga/ALL_tsp/bestSols.csv";
        File folder = new File(dir);
        String[] list = folder.list();
        // set output
        outFile = "tsp_lns_parallel.csv";
        Parser.clearFile(outFile);
        Parser.writeTextInto("instance;time;obj;opt;parallel\n", outFile);
        // start program
        long time = System.currentTimeMillis();
        for (String s : list) {
            if (s.contains(".tsp") && (!s.contains("a280")) && (!s.contains("gz")) && (!s.contains("lin"))) {
                distMatrix = Parser.parseInstance(dir + "/" + s);
                if (distMatrix != null) {
                    n = distMatrix.length;
                    if (n >= 100 && n < 500) {
                        optimum = Parser.getOpt(s.split("\\.")[0], optFile);
                        System.out.println("optimum : " + optimum);
                        checkMatrix();
                        run();
                        System.exit(0);
                    }
                } else {
                    System.out.println("CANNOT LOAD");
                }
            }
        }
        System.out.println("total time : " + (System.currentTimeMillis() - time) + " ms");
    }

    //***********************************************************************************
    // INITIALIZATION
    //***********************************************************************************

    private static void checkMatrix() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (distMatrix[i][j] != distMatrix[j][i]) {
                    System.out.println(i + " : " + j);
                    System.out.println(distMatrix[i][j] + " != " + distMatrix[j][i]);
                    throw new UnsupportedOperationException();
                }
            }
        }
    }

    private static void run() {
        Solver solver = new Solver();
        // variables
        int max = 100 * optimum;
        IntVar totalCost = VariableFactory.bounded("obj", 0, max, solver);
        final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.LINKED_LIST, GraphType.LINKED_LIST, true);
        for (int i = 0; i < n; i++) {
            undi.getKernelGraph().activateNode(i);
            for (int j = i + 1; j < n; j++) {
                undi.getEnvelopGraph().addEdge(i, j);
            }
        }
        // constraints
        Constraint gc = GraphConstraintFactory.makeConstraint(solver);
        gc.addPropagators(new PropCycleNoSubtour(undi, gc, solver));
        gc.addPropagators(new PropNodeDegree_AtLeast(undi, 2, gc, solver));
        gc.addPropagators(new PropNodeDegree_AtMost(undi, 2, gc, solver));
        gc.addPropagators(new PropCycleEvalObj(undi, totalCost, distMatrix, gc, solver));
        PropLagr_OneTree hk = PropLagr_OneTree.oneTreeBasedRelaxation(undi, totalCost, distMatrix, gc, solver);
        hk.waitFirstSolution(true);
        gc.addPropagators(hk);
        solver.post(gc);
        // config
        solver.set(StrategyFactory.graphStrategy(undi, null, new MinCost(undi), GraphStrategy.NodeArcPriority.ARCS));
//		solver.set(StrategyFactory.graphTSP(undi, TSP_heuristics.enf_sparse,null));
        PropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
        solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
//		SearchMonitorFactory.log(solver, true, false);
        solver.getSearchLoop().getLimitsBox().setTimeLimit(100000);
        solver.getSearchLoop().plugSearchMonitor(new TSP_LNS_Monitor(solver, undi, totalCost));
//		solver.getSearchLoop().restartAfterEachSolution(true);
        // resolution
        long timeInst = System.currentTimeMillis();
        System.out.println("start LNS...");
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
        System.out.println("end LNS... duration = " + (System.currentTimeMillis() - timeInst) + " ms");
//		checkUndirected(solver, undi, totalCost, distMatrix);
//		//output
//		if(!undi.instantiated()){
//			throw new UnsupportedOperationException();
//		}
//		System.out.println("first solution");
//		bestCost = totalCost.getValue();
//		System.out.println("cost : "+bestCost);
//		bestSolution = new int[n];
//		int x = 0;
//		INeighbors nei = undi.getEnvelopGraph().getSuccessorsOf(x);
//		int y = nei.getFirstElement();
//		int tmp;
//		String s = "";
//		for(int i=0;i<n;i++){
//			bestSolution[i] = x;
//			tmp = x;
//			x = y;
//			nei = undi.getEnvelopGraph().getSuccessorsOf(x);
//			y = nei.getFirstElement();
//			if(y==tmp){
//				y = nei.getNextElement();
//			}
//			s += bestSolution[i]+", ";
//		}
//		System.out.println(s);
    }

    private static void checkUndirected(Solver solver, UndirectedGraphVar undi, IntVar totalCost, int[][] matrix) {
        if (solver.getMeasures().getSolutionCount() == 0) {
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
            if (sum != solver.getSearchLoop().getObjectivemanager().getBestValue() && sum != totalCost.getValue()) {
                throw new UnsupportedOperationException();
            }
        }
    }

    //***********************************************************************************
    // RESOLUTION
    //***********************************************************************************

    //***********************************************************************************
    // BRANCHING
    //***********************************************************************************

    private static class MinCost extends ArcStrategy<UndirectedGraphVar> {
        public MinCost(UndirectedGraphVar undirectedGraphVar) {
            super(undirectedGraphVar);
        }

        @Override
        public boolean computeNextArc() {
            int cost = -1;
            ISet nei, ker;
            for (int i = 0; i < n; i++) {
                ker = g.getKernelGraph().getSuccessorsOf(i);
                if (ker.getSize() < 2) {
                    nei = g.getEnvelopGraph().getSuccessorsOf(i);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        if (!ker.contain(j)) {
                            if (cost == -1 || distMatrix[i][j] < cost) {
                                cost = distMatrix[i][j];
                                this.from = i;
                                this.to = j;
                            }
                        }
                    }
                }
            }
            return cost != -1;
        }
    }

    private static class TSP_LNS_Monitor extends Abstract_LNS_SearchMonitor {

        //variables
        private int[] bestSolution;
        private int bestCost;
        private UndirectedGraphVar g;
        private IntVar cost;
        private int n;
        private Random rd;
        private int nbFixedVars;
        private int nbFails;

        // constructor
        private TSP_LNS_Monitor(Solver solver, UndirectedGraphVar g, IntVar cost) {
            super(solver, false);
            this.n = g.getEnvelopGraph().getNbNodes();
            this.g = g;
            this.cost = cost;
            this.bestSolution = new int[n];
            this.bestCost = -1;
            rd = new Random(0);
            nbFixedVars = n / 2;
            System.out.println(n + " nodes / " + nbFixedVars + " fixed arcs");
        }

        public void onContradiction(ContradictionException cex) {
            nbFails++;
            if (nbFails == 200) {
                nbFails = 0;
                solver.getSearchLoop().restart();
            }
        }

        @Override
        protected void recordSolution() {
            if ((cost.getValue() > bestCost && bestCost != -1) || !g.instantiated()) {
                throw new UnsupportedOperationException();
            }
//			System.out.println("old objective : "+bestCost+" | new objective : "+cost.getValue());
            bestCost = cost.getValue();
            System.out.println("new objective : " + bestCost);
            int x = 0;
            ISet nei = g.getEnvelopGraph().getSuccessorsOf(x);
            int y = nei.getFirstElement();
            if (y == n - 1) {
                y = nei.getNextElement();
            }
            int tmp;
            for (int i = 0; i < n - 1; i++) {
                bestSolution[i] = x;
                tmp = x;
                x = y;
                nei = g.getEnvelopGraph().getSuccessorsOf(x);
                y = nei.getFirstElement();
                if (y == tmp) {
                    y = nei.getNextElement();
                }
            }
        }

        @Override
        protected void restrictLess() {
            nbFixedVars /= 2;
            System.out.println("nbFixedVars " + nbFixedVars);
        }

        @Override
        protected boolean isSearchComplete() {
            return nbFixedVars == 0;
        }

        @Override
        protected void fixSomeVariables() throws ContradictionException {
            int x;
            for (int k = 0; k < nbFixedVars; k++) {
                x = rd.nextInt(n);
                if (x < n - 1) {
                    g.enforceArc(bestSolution[x], bestSolution[x + 1], Cause.Null);
                } else {
                    g.enforceArc(bestSolution[x], bestSolution[0], Cause.Null);
                }
            }
        }
    }
}