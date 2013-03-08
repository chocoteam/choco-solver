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

package samples.sandbox.lns.parallel;

import samples.graph.input.TSP_Utils;
import samples.graph.output.TextWriter;
import samples.sandbox.parallelism.AbstractParallelMaster;
import solver.Solver;
import solver.constraints.gary.GraphConstraintFactory;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.strategy.graph.GraphStrategies;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.UndirectedGraphVar;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetType;

import java.io.File;

/**
 * Master for solving the TSP
 *
 * @author Jean-Guillaume Fages
 */
public class TSP_Parallel_LNS extends AbstractParallelMaster<TSPslave> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    // general
    private static final long TIMELIMIT = 300000;
    private static final int MAX_SIZE = 1000;
    private static String outFile;
    private static final boolean PARALLEL = true;
    // instance
    private int optimum;
    private int[][] distMatrix;
    private int n;
    // LNS
    private int[] bestSolution;
    private int bestCost;
    private int size = 50;

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public TSP_Parallel_LNS(int[][] distMatrix, int opt) {
        super();
        this.optimum = opt;
        this.distMatrix = distMatrix;
        this.n = distMatrix.length;
        this.size = Math.min(size, n);
    }

    //***********************************************************************************
    // INITIALIZATION
    //***********************************************************************************

    private void computeFirstSolution() {
        Solver solver = new Solver();
        // variables
        int max = 100 * optimum;
        IntVar totalCost = VariableFactory.bounded("obj", 0, max, solver);
        final UndirectedGraphVar undi = new UndirectedGraphVar("G", solver, n, SetType.ENVELOPE_BEST, SetType.LINKED_LIST, true);
        for (int i = 0; i < n; i++) {
            undi.getKernelGraph().activateNode(i);
            for (int j = i + 1; j < n; j++) {
                undi.getEnvelopGraph().addEdge(i, j);
            }
        }
        // constraints
        solver.post(GraphConstraintFactory.tsp(undi, totalCost, distMatrix, 0));
        // config
        GraphStrategies strategy = new GraphStrategies(undi, distMatrix, null);
        strategy.configure(GraphStrategies.MIN_COST, true);
        solver.set(strategy);
        SearchMonitorFactory.limitTime(solver, TIMELIMIT);
        // resolution
        solver.findSolution();
        //output
        if (!undi.instantiated()) {
            throw new UnsupportedOperationException();
        }
        System.out.println("first solution");
        bestCost = totalCost.getValue();
        System.out.println("cost : " + bestCost);
        bestSolution = new int[n];
        int x = 0;
        ISet nei = undi.getEnvelopGraph().getNeighborsOf(x);
        int y = nei.getFirstElement();
        int tmp;
        String s = "";
        for (int i = 0; i < n; i++) {
            bestSolution[i] = x;
            tmp = x;
            x = y;
            nei = undi.getEnvelopGraph().getNeighborsOf(x);
            y = nei.getFirstElement();
            if (y == tmp) {
                y = nei.getNextElement();
            }
            s += bestSolution[i] + ", ";
        }
        System.out.println(s);
    }

    //***********************************************************************************
    // RESOLUTION
    //***********************************************************************************

    private void initLNS() {
        int nb = n / size;
        slaves = new TSPslave[nb];
        for (int i = 0; i < nb - 1; i++) {
            slaves[i] = new TSPslave(this, i, size);
        }
        slaves[nb - 1] = new TSPslave(this, nb - 1, size + n % size);
    }

    private void LNS() {
        int step = size / 3;
        boolean impr = true;
        while (impr) {
            impr = false;
            for (int off = step; off < n; off += step) {
                slideSolution(step);
                impr |= LNS_one_run();
            }
            System.out.println("cost after LNS cycle : " + bestCost);
            step /= 2;
        }
    }

    private boolean LNS_one_run() {
        int idx = 0;
        int nb = slaves.length;
        int linkCost = distMatrix[bestSolution[n - 1]][bestSolution[0]];
        for (int i = 0; i < nb; i++) {
            int[] fr = slaves[i].getInputFragment();
            for (int j = 0; j < fr.length; j++) {
                fr[j] = bestSolution[idx++];
            }
            slaves[i].set(fr, distMatrix);
            if (idx < n) {
                linkCost += distMatrix[bestSolution[idx - 1]][bestSolution[idx]];
            }
        }
        if (idx != n) throw new UnsupportedOperationException();
        // solve
        if (PARALLEL) {
            distributedSlavery();
        } else {
            sequentialSlavery();
        }
        // regroup
        int obj = linkCost;
        idx = 0;
        for (int i = 0; i < nb; i++) {
            obj += slaves[i].getOutputCost();
            int[] fr = slaves[i].getOutputFragment();
            for (int j = 0; j < fr.length; j++) {
                bestSolution[idx++] = fr[j];
            }
        }
        // output
        if (obj > bestCost) {
            throw new UnsupportedOperationException();
        }
        boolean improved = obj < bestCost;
        bestCost = obj;
        checkCost();
        return improved;
    }

    private void slideSolution(int offSet) {
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

    private void checkCost() {
        int obj = distMatrix[bestSolution[n - 1]][bestSolution[0]];
        for (int i = 0; i < n - 1; i++) {
            obj += distMatrix[bestSolution[i]][bestSolution[i + 1]];
        }
        if (obj != bestCost) {
            throw new UnsupportedOperationException();
        }
    }

    //***********************************************************************************
    // MAIN
    //***********************************************************************************

    public static void main(String[] args) {
        TextWriter.clearFile(outFile = "tsp.csv");
        TextWriter.writeTextInto("instance;sols;fails;nodes;time;obj;allDiffAC;search;\n", outFile);
        String dir = "/Users/jfages07/github/In4Ga/benchRousseau";
//		String dir = "/Users/jfages07/github/In4Ga/mediumTSP";
        File folder = new File(dir);
        String[] list = folder.list();
        int[][] matrix;
        for (String s : list) {
            if (s.contains(".tsp") && (!s.contains("gz")) && (!s.contains("lin"))) {
                matrix = TSP_Utils.parseInstance(dir + "/" + s, MAX_SIZE);
                solve(s, matrix);
            }
        }
    }

    public static void solve(String s, int[][] matrix) {
        if ((matrix != null && matrix.length >= 0 && matrix.length < 4000)) {
            int optimum = TSP_Utils.getOptimum(s.split("\\.")[0], "/Users/jfages07/github/In4Ga/ALL_tsp/bestSols.csv");
            System.out.println("optimum : " + optimum);
            for (int i = 0; i < 2; i++) {
                TSP_Parallel_LNS master = new TSP_Parallel_LNS(matrix, optimum);
                master.initLNS();
                master.computeFirstSolution();
                long timeInst = System.currentTimeMillis();
                System.out.println("start LNS...");
                master.LNS();
                System.out.println("end LNS...");
                System.out.println("time : " + (System.currentTimeMillis() - timeInst) + " ms");
                double ratio = (double) (master.bestCost - optimum) * 100.0d / (double) optimum;
                ratio = (int) (ratio * 100);
                ratio /= 100;
                System.out.println(master.bestCost + "/" + optimum + " => GAP " + ratio + "%");
            }
        } else {
            System.out.println("CANNOT LOAD");
        }
    }
}