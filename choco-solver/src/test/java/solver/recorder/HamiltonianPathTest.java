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

package solver.recorder;

import common.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;
import samples.graph.input.GraphGenerator;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.measure.IMeasures;
import solver.search.strategy.GraphStrategyFactory;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.graph.DirectedGraphVar;

/**
 * Find a Hamiltonian path in a sparse graph with incremental algorithm
 * test the correctness of fine event recorders
 *
 * @author Jean-Guillaume Fages
 */
public class HamiltonianPathTest {

    private final static long TIME_LIMIT = 5000;

    @Test(groups = "10m")
    public static void test() {
        int[] sizes = new int[]{50, 100, 200, 400};
        long s;
        int[] nbVoisins = new int[]{3, 5, 10};
        boolean[][] matrix;
        for (int n : sizes) {
            for (int nb : nbVoisins) {
                for (int ks = 0; ks < 50; ks++) {
                    s = System.currentTimeMillis();
                    System.out.println("n:" + n + " nbVoisins:" + nb + " s:" + s);
                    GraphGenerator gg = new GraphGenerator(n, s, GraphGenerator.InitialProperty.HamiltonianCircuit);
                    matrix = transformMatrix(gg.neighborBasedGenerator(nb));
                    testProblem(matrix, s, true, false);
                    testProblem(matrix, s, false, false);
                    testProblem(matrix, s, true, true);
                    testProblem(matrix, s, false, true);
                }
            }
        }
    }

    private static void testProblem(boolean[][] matrix, long s, boolean rd, boolean strongFilter) {
        Solver solver = new Solver();
        int n = matrix.length;
        // build model
        DirectedGraphVar graph = new DirectedGraphVar("G", solver, n, SetType.LINKED_LIST, SetType.LINKED_LIST, false);
        try {
            graph.getKernelGraph().activateNode(n - 1);
            for (int i = 0; i < n - 1; i++) {
                graph.getKernelGraph().activateNode(i);
                for (int j = 1; j < n; j++) {
                    if (matrix[i][j]) {
                        graph.getEnvelopGraph().addArc(i, j);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        Constraint gc = GraphConstraintFactory.hamiltonianPath(graph, 0, n - 1, strongFilter);
        solver.post(gc);

        // configure solver
        if (rd) {
            solver.set(GraphStrategyFactory.graphRandom(graph, s));
        } else {
            solver.set(GraphStrategyFactory.graphStrategy(graph, null, new ConstructorHeur(graph, 0), GraphStrategy.NodeArcPriority.ARCS));
        }
        SearchMonitorFactory.limitTime(solver, TIME_LIMIT);
        solver.findSolution();
        IMeasures mes = solver.getMeasures();

        // the problem has at least one solution
        Assert.assertFalse(mes.getSolutionCount() == 0 && mes.getTimeCount() < TIME_LIMIT);
    }

    private static boolean[][] transformMatrix(boolean[][] m) {
        int n = m.length + 1;
        boolean[][] matrix = new boolean[n][n];
        for (int i = 0; i < n - 1; i++) {
            for (int j = 1; j < n - 1; j++) {
                matrix[i][j] = m[i][j];
            }
            matrix[i][n - 1] = m[i][0];
        }
        return matrix;
    }

    // constructive heuristic, can be useful to debug
    private static class ConstructorHeur extends ArcStrategy<DirectedGraphVar> {
        int source, n;

        public ConstructorHeur(DirectedGraphVar graphVar, int s) {
            super(graphVar);
            source = s;
            n = graphVar.getEnvelopGraph().getNbNodes();
        }

        @Override
        public boolean computeNextArc() {
            int x = source;
            int y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
            int nb = 1;
            while (y != -1) {
                x = y;
                y = g.getKernelGraph().getSuccessorsOf(x).getFirstElement();
                nb++;
            }
            y = g.getEnvelopGraph().getSuccessorsOf(x).getFirstElement();
            if (y == -1) {
                if (x != n - 1 || nb != n) {
                    for (int i = 0; i < n; i++) {
                        if (g.getEnvelopGraph().getSuccessorsOf(i).getSize() > 1) {
                            this.from = i;
                            this.to = g.getEnvelopGraph().getSuccessorsOf(i).getFirstElement();
                            return true;
                        }
                    }
                    throw new UnsupportedOperationException();
                }
                return false;
            }
            this.from = x;
            this.to = y;
            return true;
        }
    }
}
