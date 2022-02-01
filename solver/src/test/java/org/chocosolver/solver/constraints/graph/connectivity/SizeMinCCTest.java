/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.connectivity;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.ConnectivityFinder;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * Test class for the SizeMinCC constraint.
 *
 * @author Dimitri Justeau-Allaire
 */
public class SizeMinCCTest {

    /**
     * Abstract test model, for factorization.
     */
    private class AbstractTestModel {

        // Variables
        public int N;
        public IntVar sizeMinCC;
        public UndirectedGraph GLB, GUB;
        public Model model;
        public UndirectedGraphVar g;

        // Constructor
        public AbstractTestModel(Model model, int N, int minNCC_LB, int minNCC_UB, UndirectedGraph GLB,
                                 UndirectedGraph GUB) {
            this.N = N;
            this.model = model;
            this.sizeMinCC = this.model.intVar(minNCC_LB, minNCC_UB);
            // Create the graph variable
            this.g = model.graphVar("g",GLB, GUB);
            // Post the constraint
            model.sizeMinConnectedComponents(g, sizeMinCC).post();
        }

        public AbstractTestModel(int N, int minNCC_LB, int minNCC_UB,
                                 int[] GLB_Nodes, int[] GUB_Nodes,
                                 int[][] GLB_Edges, int[][] GUB_Edges) {
            this.model = new Model();
            this.sizeMinCC = this.model.intVar(minNCC_LB, minNCC_UB);
            // Init GLB (graph kernel)
            this.GLB = GraphFactory.makeStoredUndirectedGraph(model, N, SetType.BITSET, SetType.BITSET, GLB_Nodes, GLB_Edges);
            // Init GUB (graph envelope)
            this.GUB = GraphFactory.makeStoredUndirectedGraph(model, N, SetType.BITSET, SetType.BITSET, GUB_Nodes, GUB_Edges);
            // Create the graph variable
            this.g = model.graphVar("g", GLB, GUB);
            // Post the constraint
            model.sizeMinConnectedComponents(g, sizeMinCC).post();
        }
    }

    /* --------------- */
    /* Fail test cases */
    /* --------------- */

    /**
     * Fail test case 1: The minimum size of the CC cannot be satisfied.
     * MIN_NCC = [4, 6].
     * graph GLB { 0; }
     * graph GUB {
     *     { 0; 1; 2; 3; 4; 5; 6; }
     *     # CC of size 2
     *     0 -- 1;
     *     # CC of size 2
     *     2 -- 3;
     *     # CC of size 3
     *     4 -- 5;
     *     5 -- 6;
     * }
     */
    @Test
    public void testFailCase1() {
        int N = 7;
        int minNCC_LB = 4;
        int minNCC_UB = 6;
        int[] GLB_Nodes = new int[] {0};
        int[][] GLB_Edges = new int[][] {};
        int[] GUB_Nodes = new int[] {0, 1, 2, 3, 4, 5, 6};
        int[][] GUB_Edges = new int[][] {
                {0, 1},
                {2, 3},
                {4, 5}, {5, 6}
        };
        AbstractTestModel test = new AbstractTestModel(N, minNCC_LB, minNCC_UB, GLB_Nodes, GUB_Nodes, GLB_Edges, GUB_Edges);
        Assert.assertFalse(test.model.getSolver().solve());
    }

    /**
     * Fail test case 2: sizeMaxCC is greater than the number of potential nodes.
     * graph GLB {
     *     { 0; }
     * }
     * graph GUB {
     *     { 0; 1; 2; 3; }
     *     0 -- 1;
     *     1 -- 2;
     *     1 -- 3;
     * }
     */
    @Test
    public void testFailCase2() {
        int N = 4;
        int minNCC_LB = 10;
        int minNCC_UB = 10;
        int[] GLB_Nodes = new int[] {0};
        int[][] GLB_Edges = new int[][] {};
        int[] GUB_Nodes = new int[] {0, 1, 2, 3};
        int[][] GUB_Edges = new int[][] { {0, 1}, {1, 2}, {1, 3} };
        AbstractTestModel test = new AbstractTestModel(N, minNCC_LB, minNCC_UB, GLB_Nodes, GUB_Nodes, GLB_Edges, GUB_Edges);
        Assert.assertFalse(test.model.getSolver().solve());
    }

    /**
     * Fail test case 3: sizeMaxCC upper bound is less than the minimum candidate CC in G.
     * graph GLB {
     *     { 0; 1; 2; }
     *     0 -- 1;
     *     1 -- 2;
     *     0 -- 2;
     * }
     * graph GUB {
     *     { 0; 1; 2; 3; }
     *     0 -- 1;
     *     1 -- 2;
     *     0 -- 2;
     *     1 -- 3;
     *     3 -- 0;
     * }
     */
    @Test
    public void testFailCase3() {
        int N = 4;
        int minNCC_LB = 2;
        int minNCC_UB = 2;
        int[] GLB_Nodes = new int[] {0, 1, 2};
        int[][] GLB_Edges = new int[][] { {0, 1}, {1, 2}, {0, 2}};
        int[] GUB_Nodes = new int[] {0, 1, 2, 3};
        int[][] GUB_Edges = new int[][] { {0, 1}, {1, 2}, {0, 2}, {1, 3}, {3, 0}};
        AbstractTestModel test = new AbstractTestModel(N, minNCC_LB, minNCC_UB, GLB_Nodes, GUB_Nodes, GLB_Edges, GUB_Edges);
        Assert.assertFalse(test.model.getSolver().solve());
    }

    /* ------------------ */
    /* Success test cases */
    /* ------------------ */

    /**
     * Success test case 1: The empty graph is the only solution.
     * graph GLB {}
     * graph GUB {
     *     { 0; 1; 2; 3; 4; }
     *     0 -- 1;
     *     1 -- 2;
     *     2 -- 3;
     *     3 -- 4;
     * }
     */
    @Test
    public void testSuccessCase1() {
        int N = 5;
        int minNCC_LB = 0;
        int minNCC_UB = 0;
        int[] GLB_Nodes = new int[] {};
        int[][] GLB_Edges = new int[][] {};
        int[] GUB_Nodes = new int[] {0, 1, 2, 3, 4};
        int[][] GUB_Edges = new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 4} };
        AbstractTestModel test = new AbstractTestModel(N, minNCC_LB, minNCC_UB, GLB_Nodes, GUB_Nodes, GLB_Edges, GUB_Edges);
        boolean solutionFound = test.model.getSolver().solve();
        Assert.assertTrue(solutionFound);
        Assert.assertEquals(test.g.getPotentialNodes().size(), 0); // Assert that the solution is the empty graph
        Assert.assertEquals(test.sizeMinCC.getValue(), 0);
        Assert.assertFalse(test.model.getSolver().solve()); // Assert that there is no other solution
    }

    /**
     * Success test case 2. There are several solutions.
     * graph GLB {
     *     { 0; 1; 2;}
     *     1 -- 2;
     * }
     * graph GUB {
     *     { 0; 1; 2; 3; 4; 5; 6; 7; 8; 9; 10 }
     *     # CC 1
     *     2 -- 1;
     *     1 -- 0;
     *     0 -- 5;
     *     5 -- 4;
     *     # CC 2
     *     6 -- 7;
     *     7 -- 10;
     *     # CC 3
     *     3 -- 8;
     *     8 -- 9;
     * }
     */
    @Test
    public void testSuccessCase2() {
        int N = 11;
        int minNCC_LB = 3;
        int minNCC_UB = 3;
        int[] GLB_Nodes = new int[] {0, 1, 2};
        int[][] GLB_Edges = new int[][] { {1, 2} };
        int[] GUB_Nodes = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[][] GUB_Edges = new int[][] {
                {2, 1}, {1, 0}, {0, 5}, {5, 4},
                {6, 7}, {7, 10},
                {3, 8}, {8, 9}
        };
        AbstractTestModel test = new AbstractTestModel(N, minNCC_LB, minNCC_UB, GLB_Nodes, GUB_Nodes, GLB_Edges, GUB_Edges);
        boolean solutionFound = test.model.getSolver().solve();
        Assert.assertTrue(solutionFound);
        Assert.assertTrue(test.model.getSolver().solve());
    }

    /**
     * Success test case 3. MIN_NCC = [2, 3], there are exactly 6 solutions:
     *     1. 0 -- 1.
     *     2. 1 -- 2.
     *     3. 2 -- 3.
     *     4. 0 -- 1 -- 2.
     *     5. 1 -- 2 -- 3.
     *     6. 0 -- 1    2 -- 3.
     * graph GLB {}
     * graph GUB {
     *     { 0; 1; 2; 3; }
     *     0 -- 1;
     *     1 -- 2;
     *     2 -- 3;
     * }
     */
    @Test
    public void testSuccessCase3() {
        int N = 4;
        int minNCC_LB = 2;
        int minNCC_UB = 3;
        int[] GLB_Nodes = new int[] {};
        int[][] GLB_Edges = new int[][] {};
        int[] GUB_Nodes = new int[] {0, 1, 2, 3};
        int[][] GUB_Edges = new int[][] { {0, 1}, {1, 2}, {2, 3} };
        AbstractTestModel test = new AbstractTestModel(N, minNCC_LB, minNCC_UB, GLB_Nodes, GUB_Nodes, GLB_Edges, GUB_Edges);
        test.model.getSolver().plugMonitor((IMonitorSolution) () -> {
        });
        List<Solution> solutions = test.model.getSolver().findAllSolutions();
        Assert.assertEquals(solutions.size(), 6);
    }

    /**
     * Success test case 4. A single solution that is not the empty graph, and need the enforcing of edges in GLB:
     * 0    1    2 => 0 -- 1 -- 2.
     * graph GLB {
     *     { 0; 1; 2; }
     * }
     * graph GUB {
     *     { 0; 1; 2; }
     *     0 -- 1;
     *     1 -- 2;
     * }
     */
    @Test
    public void testSuccessCase4() {
        int N = 3;
        int minNCC_LB = 3;
        int minNCC_UB = 6;
        int[] GLB_Nodes = new int[] {0, 1, 2};
        int[][] GLB_Edges = new int[][] {};
        int[] GUB_Nodes = new int[] {0, 1, 2};
        int[][] GUB_Edges = new int[][] { {0, 1}, {1, 2} };
        AbstractTestModel test = new AbstractTestModel(N, minNCC_LB, minNCC_UB, GLB_Nodes, GUB_Nodes, GLB_Edges, GUB_Edges);
        List<Solution> solutions = test.model.getSolver().findAllSolutions();
        Assert.assertEquals(solutions.size(), 1);
    }

    @Test
    public void batchTest() {
        int N = 20;
        for (int k : IntStream.range(0, 1).toArray()) {
            Model model = new Model();
            int nbCC1 = ThreadLocalRandom.current().nextInt(3, 6);
            int nbCC2 = ThreadLocalRandom.current().nextInt(3, 6);
            UndirectedGraph GLB = GraphFactory.generateRandomUndirectedGraphFromNbCC(model, N, SetType.BITSET, SetType.BITSET, nbCC1, 0.3, 10);
            UndirectedGraph GUB = GraphFactory.generateRandomUndirectedGraphFromNbCC(model, N, SetType.BITSET, SetType.BITSET, nbCC2, 0.1, 5);
            for (int i : GLB.getNodes()) {
                GUB.addNode(i);
                for (int j : GLB.getNeighborsOf(i)) {
                    GUB.addEdge(i, j);
                }
            }
            ConnectivityFinder glbCf = new ConnectivityFinder(GLB);
            glbCf.findAllCC();
            ConnectivityFinder gubCf = new ConnectivityFinder(GUB);
            gubCf.findAllCC();
            AbstractTestModel test = new AbstractTestModel(model, N, 5, 10, GLB, GUB);
            if (test.model.getSolver().findSolution() != null) {
                Assert.assertTrue(test.g.isInstantiated() && test.sizeMinCC.isInstantiated());
                ConnectivityFinder cFinder = new ConnectivityFinder(test.g.getUB());
                cFinder.findAllCC();
                Assert.assertEquals(cFinder.getSizeMinCC(), test.sizeMinCC.getValue());
            }
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void case8bTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 6;
        int[] nodesLB = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edgesLB = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4},
        };
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodesLB, edgesLB);
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodesLB, edgesLB);
        UB.addEdge(4, 5);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar sizeMinCC = model.intVar(0, 6);
        model.sizeMinConnectedComponents(g, sizeMinCC).post();
        while (model.getSolver().solve()) {}
    }

    @Test(groups="10s", timeOut=60000)
    public void case10Test() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 6;
        int[] nodesLB = new int[] {0, 1, 2, 3, 4};
        int[][] edgesLB = new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 4} };
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodesLB, edgesLB);
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodesLB, edgesLB);
        UB.addNode(5);
        UB.addEdge(4, 5);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar sizeMinCC = model.intVar(1);
        model.sizeMinConnectedComponents(g, sizeMinCC).post();
        while (model.getSolver().solve()) {}
    }

    @Test(groups="10s", timeOut=60000)
    public void generateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar sizeMinCC = model.intVar(0, 3);
        IntVar nbEdges = model.intVar(3, 10);
        model.nbEdges(g, nbEdges).post();
        model.sizeMinConnectedComponents(g, sizeMinCC).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model2.graphVar("g", LB2, UB2);
        IntVar sizeMinCC2 = model2.intVar(0, 3);
        Constraint cons = model2.sizeMinConnectedComponents(g2, sizeMinCC2);
        IntVar nbEdges2 = model2.intVar(3, 10);
        Constraint consNbEdges = model2.nbEdges(g2, nbEdges2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE && consNbEdges.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }
}
