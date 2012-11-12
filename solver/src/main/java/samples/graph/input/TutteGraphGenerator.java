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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/06/12
 * Time: 14:03
 */

package samples.graph.input;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.gary.basic.PropBiconnected;
import solver.constraints.propagators.gary.basic.PropMaxDiameter;
import solver.constraints.propagators.gary.basic.PropMaxDiameterFromNode;
import solver.constraints.propagators.gary.basic.PropNoTriangle;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtMost;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.variables.setDataStructures.ISet;

import java.util.ArrayList;

/**
 * Generate Tutte graphs with CP
 *
 * @author Jean-Guillaume Fages
 * @since Oct. 2012
 */
public class TutteGraphGenerator {

    public static boolean[][] createTutteGraph(int n) {
//		int nodeDiam = 4;
        int diam = 8;
        boolean[][] output = new boolean[n][n];
        Solver solver = new Solver();
        UndirectedGraphVar g = new UndirectedGraphVar(solver, n, GraphType.ENVELOPE_SWAP_ARRAY, GraphType.LINKED_LIST, true);
        Constraint c = GraphConstraintFactory.makeConstraint(solver);
        c.addPropagators(new PropNodeDegree_AtLeast(g, 3, c, solver));
        c.addPropagators(new PropNodeDegree_AtMost(g, 3, c, solver));
//		c.addPropagators(new PropMaxDiameterFromNode(g,nodeDiam,0,c,solver));
        c.addPropagators(new PropMaxDiameter(g, diam, c, solver));
        c.addPropagators(new PropNoTriangle(g, c, solver));
        c.addPropagators(new PropBiconnected(g, c, solver));

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.getEnvelopGraph().addEdge(i, j);
            }
        }
        for (int i = 0; i < n; i += 4) {
            if (i + 1 < n)
                g.getKernelGraph().addEdge(i, i + 1);
            if (i + 2 < n)
                g.getKernelGraph().addEdge(i, i + 2);
            if (i + 3 < n)
                g.getKernelGraph().addEdge(i, i + 3);
        }

        solver.post(c);
        solver.set(StrategyFactory.graphLexico(g));
        SearchMonitorFactory.log(solver, true, false);
        solver.findSolution();
        if (solver.getMeasures().getSolutionCount() == 0) {
            System.out.println("no Tutte graph of size " + n + " exists");
            return null;
        } else {
            System.out.println(g.getEnvelopGraph());
        }
        ISet nei;
        for (int i = 0; i < n; i++) {
            nei = g.getEnvelopGraph().getNeighborsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                output[i][j] = true;
            }
        }
        return output;
    }

    public static ArrayList<int[][]> createAllTutteGraphs(final int n) {
        final ArrayList<int[][]> output = new ArrayList();
        Solver solver = new Solver();
        final UndirectedGraphVar g = new UndirectedGraphVar(solver, n, GraphType.MATRIX, GraphType.LINKED_LIST, true);
        Constraint c = GraphConstraintFactory.makeConstraint(solver);
        c.addPropagators(new PropNodeDegree_AtLeast(g, 3, c, solver));
        c.addPropagators(new PropNodeDegree_AtMost(g, 3, c, solver));
        c.addPropagators(new PropMaxDiameterFromNode(g, 6, 0, c, solver));
        c.addPropagators(new PropMaxDiameter(g, 8, c, solver));
        c.addPropagators(new PropBiconnected(g, c, solver));
        // breaking some symmetries
        for (int j = 1; j < 4; j++) {
            g.getEnvelopGraph().addEdge(0, j);
        }
        for (int i = 1; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.getEnvelopGraph().addEdge(i, j);
            }
        }
        solver.post(c);
        solver.set(StrategyFactory.graphLexico(g));
        solver.getSearchLoop().getLimitsBox().setSolutionLimit(100);
        solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor() {
            public void onSolution() {
                int[][] sol = new int[n][3];
                ISet nei;
                for (int i = 0; i < n; i++) {
                    int idx = 0;
                    nei = g.getEnvelopGraph().getNeighborsOf(i);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        sol[i][idx++] = j;
                    }
                }
                output.add(sol);
            }
        });
        solver.findAllSolutions();
        System.out.println(solver.getMeasures().getSolutionCount());
        if (solver.getMeasures().getSolutionCount() == 0) {
            System.out.println("no Tutte graph of size " + n + " exists");
            return null;
        }
        return output;
    }

    public static void main(String[] args) {
        System.out.println("Hello World");
        int i = 46;
        if (createTutteGraph(i) != null) {
            System.out.println("Tutte graph found for i=" + i);
        }
        System.exit(0);
        createAllTutteGraphs(i);
        System.exit(0);
    }

    public static boolean[][] toBooleanMatrix(int[][] tutte) {
        int n = tutte.length + 1;
        boolean[][] m = new boolean[n][n];
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < 3; j++) {
                if (tutte[i][j] == 0) {
                    m[i][n - 1] = true;
                } else {
                    m[i][tutte[i][j]] = true;
                }
            }
        }
        return m;
    }

    //***********************************************************************************
    // TUTTE
    //***********************************************************************************

    public static boolean[][] makeWikiTutteGraph() {
        int n = 46;
        boolean[][] m = new boolean[n][n];
        makeThird(m, 0);
        makeThird(m, 15);
        makeThird(m, 30);
        // center
        add(45, 0, m, 0);
        add(45, 15, m, 0);
        add(45, 30, m, 0);
        // extremities
        add(14, 12 + 15, m, 0);
        add(14 + 15, 12 + 30, m, 0);
        add(14 + 30, 12, m, 0);
        return m;
    }

    private static void makeThird(boolean[][] m, int i) {
        add(0, 1, m, i);
        add(0, 3, m, i);
        add(1, 2, m, i);
        add(2, 3, m, i);
        add(1, 4, m, i);
        add(2, 6, m, i);
        add(3, 8, m, i);
        add(4, 5, m, i);
        add(5, 6, m, i);
        add(6, 7, m, i);
        add(7, 8, m, i);
        add(5, 9, m, i);
        add(7, 10, m, i);
        add(8, 11, m, i);
        add(9, 10, m, i);
        add(10, 11, m, i);
        add(4, 12, m, i);
        add(9, 13, m, i);
        add(11, 14, m, i);
        add(12, 13, m, i);
        add(13, 14, m, i);
    }

    private static void add(int from, int to, boolean[][] matrix, int offset) {
        matrix[from + offset][to + offset] = true;
        matrix[to + offset][from + offset] = true;
    }

}
