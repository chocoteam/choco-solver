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

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtMost;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleEvalObj;
import solver.constraints.propagators.gary.tsp.undirected.PropCycleNoSubtour;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.UndirectedGraphVar;
import solver.variables.setDataStructures.SetType;
import solver.variables.setDataStructures.ISet;
import java.io.File;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class Parallelized_LNS {

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
    private static int[] bestSolution;
    private static int bestCost;
    private static int SIZE = 20;
    private static Fragment[] fragments;
    private static int nbDone;
    private static boolean wait;
    private static Thread mainThread;
    private static boolean PARALLEL_SOLVING = true;

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
        mainThread = Thread.currentThread();
        long time = System.currentTimeMillis();
        for (String s : list) {
            if (s.contains(".tsp") && (!s.contains("a280")) && (!s.contains("gz")) && (!s.contains("lin"))) {
                distMatrix = Parser.parseInstance(dir + "/" + s);
                if (distMatrix != null) {
                    n = distMatrix.length;
                    if (n >= 100 && n < 500) {
                        SIZE = n / 10;
                        optimum = Parser.getOpt(s.split("\\.")[0], optFile);
                        System.out.println("optimum : " + optimum);
                        checkMatrix();
                        initLNS();
                        LNS(false, s);
                        LNS(true, s);
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

    private static void computeFirstSolution() {
        Solver solver = new Solver();
        // variables
        int max = 100 * optimum;
        IntVar totalCost = VariableFactory.bounded("obj", 0, max, solver);
        final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, true);
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
        solver.post(gc);
        // config
        solver.set(StrategyFactory.graphStrategy(undi, null, new MinCost(undi), GraphStrategy.NodeArcPriority.ARCS));
//        PropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
//        solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));
        // resolution
        solver.findSolution();
        checkUndirected(solver, undi, totalCost, distMatrix);
        //output
        if (!undi.instantiated()) {
            throw new UnsupportedOperationException();
        }
        System.out.println("first solution");
        bestCost = totalCost.getValue();
        System.out.println("cost : " + bestCost);
        bestSolution = new int[n];
        int x = 0;
        ISet nei = undi.getEnvelopGraph().getSuccessorsOf(x);
        int y = nei.getFirstElement();
        int tmp;
//		String s = "";
        for (int i = 0; i < n; i++) {
            bestSolution[i] = x;
            tmp = x;
            x = y;
            nei = undi.getEnvelopGraph().getSuccessorsOf(x);
            y = nei.getFirstElement();
            if (y == tmp) {
                y = nei.getNextElement();
            }
//			s += bestSolution[i]+", ";
        }
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

    private static void initLNS() {
        int nb = n / SIZE;
        fragments = new Fragment[nb];
        for (int i = 0; i < nb - 1; i++) {
            fragments[i] = new Fragment(SIZE);
        }
        fragments[nb - 1] = new Fragment(SIZE + n % SIZE);
    }

    private static void LNS(boolean parallel, String instanceName) {
        PARALLEL_SOLVING = parallel;
        computeFirstSolution();
        long timeInst = System.currentTimeMillis();
        System.out.println("start LNS...");
        LNS_one_run();
//		int step = SIZE/3;
//		boolean impr = true;
//		while(impr){
//			impr = false;
//			for(int off = step;off<n;off += step){
//				slideSolution(step);
//				impr |= LNS_one_run();
//			}
//		}
        System.out.println("end LNS... duration = " + (System.currentTimeMillis() - timeInst) + " ms");
        String out = instanceName + ";" + (System.currentTimeMillis() - timeInst) + ";" + bestCost + ";" + optimum + ";";
        if (parallel) {
            Parser.writeTextInto(out + "1\n", outFile);
        } else {
            Parser.writeTextInto(out + "0\n", outFile);
        }
    }

    private static boolean LNS_one_run() {
        int idx = 0;
        int nb = fragments.length;
        int linkCost = distMatrix[bestSolution[n - 1]][bestSolution[0]];
        for (int i = 0; i < nb; i++) {
            int[] fr = fragments[i].getInputFragment();
            for (int j = 0; j < fr.length; j++) {
                fr[j] = bestSolution[idx++];
            }
            fragments[i].set(fr, distMatrix);
            if (idx < n) {
                linkCost += distMatrix[bestSolution[idx - 1]][bestSolution[idx]];
            }
        }
        if (idx != n) throw new UnsupportedOperationException();
        // solve
        nbDone = 0;
        wait = true;
        if (PARALLEL_SOLVING) {
            System.out.println("start distributed solving");
            for (int i = 0; i < nb; i++) {
                fragments[i].lazyStart();
            }
            try {
//				mainThread.suspend();
                while (wait) mainThread.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            for (int i = 0; i < nb; i++) {
                fragments[i].solve();
            }
        }
        int obj = linkCost;
        idx = 0;
        for (int i = 0; i < nb; i++) {
            obj += fragments[i].getOutputCost();
            int[] fr = fragments[i].getOutputFragment();
            for (int j = 0; j < fr.length; j++) {
                bestSolution[idx++] = fr[j];
            }
        }
        // output
        System.out.println("old objective : " + bestCost + " | new objective : " + obj);
        if (obj > bestCost) {
            throw new UnsupportedOperationException();
        }
        boolean improved = obj < bestCost;
        bestCost = obj;
        checkCost();
        return improved;
    }

    public static synchronized void jobFinished() {
        nbDone++;
        if (nbDone == fragments.length && PARALLEL_SOLVING) {
            System.out.println("all jobs are finished");
            wait = false;
//			mainThread.resume();
        }
    }

    private static void slideSolution(int offSet) {
        checkCost();
        int[] ns = new int[n];
        for (int i = 0; i < n; i++) {
            if (i + offSet < n) {
                ns[i + offSet] = bestSolution[i];
            } else {
                ns[i + offSet - n] = bestSolution[i];
            }
        }
        bestSolution = ns;
        checkCost();
    }

    private static void checkCost() {
        int obj = distMatrix[bestSolution[n - 1]][bestSolution[0]];
        for (int i = 0; i < n - 1; i++) {
            obj += distMatrix[bestSolution[i]][bestSolution[i + 1]];
        }
        if (obj != bestCost) {
            throw new UnsupportedOperationException();
        }
    }

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

}