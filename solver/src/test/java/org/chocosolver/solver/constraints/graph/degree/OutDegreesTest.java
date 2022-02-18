/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.degree;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OutDegreesTest {

    private abstract class BaseTest {

        Model model;
        Solver solver;
        DirectedGraphVar g;
        int n;
        SetType setType = SetType.BITSET;

        BaseTest(int n, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            this.model = new Model();
            this.solver = model.getSolver();
            this.n = n;
            DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType, nodesLB, edgesLB);
            DirectedGraph UB;
            if (nodesUB != null) {
                UB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType, nodesUB, edgesUB);
            } else {
                UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
            }
            this.g = model.digraphVar("g", LB, UB);
        }

        BaseTest(int n) {
            this.model = new Model();
            this.solver = model.getSolver();
            this.n = n;
            DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
            DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
            this.g = model.digraphVar("g", LB, UB);
        }

        BaseTest(int n, int[] nodes, int[][] edges) {
            this.model = new Model();
            this.solver = model.getSolver();
            this.n = n;
            DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType, nodes, edges);
            DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType, nodes, edges);
            this.g = model.digraphVar("g", LB, UB);
        }

        abstract Constraint getConstraint();
    }

    private class MinOutDegreeTest extends BaseTest {

        int minOutDegree;

        MinOutDegreeTest(int n, int minOutDegree, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.minOutDegree = minOutDegree;
        }

        MinOutDegreeTest(int n, int minOutDegree) {
            super(n);
            this.minOutDegree = minOutDegree;
        }

        MinOutDegreeTest(int n, int minOutDegree, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.minOutDegree = minOutDegree;
        }

        @Override
        Constraint getConstraint() {
            return model.minOutDegree(this.g, this.minOutDegree);
        }
    }

    private class MaxOutDegreeTest extends BaseTest {

        int maxOutDegree;

        MaxOutDegreeTest(int n, int maxOutDegree, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.maxOutDegree = maxOutDegree;
        }

        MaxOutDegreeTest(int n, int maxOutDegree) {
            super(n);
            this.maxOutDegree = maxOutDegree;
        }

        MaxOutDegreeTest(int n, int maxOutDegree, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.maxOutDegree = maxOutDegree;
        }

        @Override
        Constraint getConstraint() {
            return this.model.maxOutDegree(this.g, this.maxOutDegree);
        }
    }

    private class MinOutDegreesTest extends BaseTest {

        int[] minOutDegrees;

        MinOutDegreesTest(int n, int[] minOutDegrees, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.minOutDegrees = minOutDegrees;
        }

        MinOutDegreesTest(int n, int[] minOutDegrees) {
            super(n);
            this.minOutDegrees = minOutDegrees;
        }

        MinOutDegreesTest(int n, int[] minOutDegrees, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.minOutDegrees = minOutDegrees;
        }

        @Override
        Constraint getConstraint() {
            return this.model.minOutDegrees(this.g, minOutDegrees);
        }
    }
    private class MaxOutDegreesTest extends BaseTest {

        int[] maxOutDegrees;

        MaxOutDegreesTest(int n, int[] maxOutDegrees, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.maxOutDegrees = maxOutDegrees;
        }

        MaxOutDegreesTest(int n, int[] maxOutDegrees) {
            super(n);
            this.maxOutDegrees = maxOutDegrees;
        }

        MaxOutDegreesTest(int n, int[] maxOutDegrees, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.maxOutDegrees = maxOutDegrees;
        }

        @Override
        Constraint getConstraint() {
            return this.model.maxOutDegrees(this.g, maxOutDegrees);
        }
    }

    private class OutDegreesBaseTest extends BaseTest {

        int outDegreesLB;
        int outDegreesUB;
        IntVar[] outDegrees;

        OutDegreesBaseTest(int n, int outDegreesLB, int outDegreesUB, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.outDegreesLB = outDegreesLB;
            this.outDegreesUB = outDegreesUB;
            this.outDegrees = model.intVarArray(n, outDegreesLB, outDegreesUB);
        }

        OutDegreesBaseTest(int n, int outDegreesLB, int outDegreesUB) {
            super(n);
            this.outDegreesLB = outDegreesLB;
            this.outDegreesUB = outDegreesUB;
            this.outDegrees = model.intVarArray(n, outDegreesLB, outDegreesUB);
        }

        OutDegreesBaseTest(int n, int outDegreesLB, int outDegreesUB, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.outDegreesLB = outDegreesLB;
            this.outDegreesUB = outDegreesUB;
            this.outDegrees = model.intVarArray(n, outDegreesLB, outDegreesUB);
        }

        @Override
        Constraint getConstraint() {
            return this.model.outDegrees(this.g, this.outDegrees);
        }
    }

    //***********************************************************************************
    // minOutDegree(DirectedGraphVar g, int minOutDegree)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void minOutDegree_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int minOutDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {0, 1}, {0, 2}, {0, 3},
                {1, 2}, {1, 3},
                {2, 0}, {2, 4},
                {3, 4}, {3, 1},
                {4, 5}, {4, 0},
                {5, 1}, {5 ,2}
        };
        MinOutDegreeTest test = new MinOutDegreeTest(n, minOutDegree, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() >= minOutDegree);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void minOutDegree_instantiatedGraphVarFailTest() {
        int n = 10;
        int minOutDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MinOutDegreeTest test = new MinOutDegreeTest(n, minOutDegree, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minOutDegree_constrainedSuccessTest() {
        int n = 5;
        int minOutDegree = 3;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{0, 1}, {1, 2}};
        MinOutDegreeTest test = new MinOutDegreeTest(n, minOutDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() >= minOutDegree);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MinOutDegreeTest(n, minOutDegree, nodesLB, edgesLB, null, null);
        Constraint minDegreeConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 20);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.stronglyConnected(test.g);
        int count = 0;
        while (test.solver.solve()) {
            if (minDegreeConstraint.isSatisfied() == ESat.TRUE
                    && nbEdgesConstraint.isSatisfied() == ESat.TRUE
                    && connectedConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    @Test(groups="1s", timeOut=60000)
    public void minOutDegree_constrainedFailTest() {
        int n = 8;
        int minOutDegree = 4;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MinOutDegreeTest test = new MinOutDegreeTest(n, minOutDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 4);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minOutDegree_generateAndCheckTest() {
        int n = 5;
        int minOutDegree = 2;
        MinOutDegreeTest test = new MinOutDegreeTest(n, minOutDegree);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() >= minOutDegree);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MinOutDegreeTest(n, minOutDegree);
        Constraint minDegreeConstraint = test.getConstraint();
        int count = 0;
        while (test.solver.solve()) {
            if (minDegreeConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    //***********************************************************************************
    // minOutDegrees(DirectedGraphVar g, int[] minOutDegrees)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void minOutDegrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int[] minOutDegrees = new int[] {3, 1, 2, 2, 2, 2, 0, 0, 0, 0};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {0, 1}, {0, 2}, {0, 3},
                {1, 2}, {1, 3},
                {2, 0}, {2, 4},
                {3, 4}, {3, 1},
                {4, 5}, {4, 0},
                {5, 1}, {5 ,2}
        };
        MinOutDegreesTest test = new MinOutDegreesTest(n, minOutDegrees, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() >= minOutDegrees[i]);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void minOutDegrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int[] minOutDegrees = new int[] {3, 1, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MinOutDegreesTest test = new MinOutDegreesTest(n, minOutDegrees, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minOutDegrees_constrainedSuccessTest() {
        int n = 5;
        int[] minOutDegrees = new int[] {3, 1, 1, 2, 0, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{0, 1}, {1, 2}};
        MinOutDegreesTest test = new MinOutDegreesTest(n, minOutDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() >= minOutDegrees[i]);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MinOutDegreesTest(n, minOutDegrees, nodesLB, edgesLB, null, null);
        Constraint minDegreesConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 20);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.stronglyConnected(test.g);
        int count = 0;
        while (test.solver.solve()) {
            if (minDegreesConstraint.isSatisfied() == ESat.TRUE
                    && nbEdgesConstraint.isSatisfied() == ESat.TRUE
                    && connectedConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    @Test(groups="1s", timeOut=60000)
    public void minOutDegrees_constrainedFailTest() {
        int n = 8;
        int[] minOutDegrees = new int[] {3, 3, 1, 2, 1, 1, 2, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MinOutDegreesTest test = new MinOutDegreesTest(n, minOutDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 4);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minOutDegrees_generateAndCheckTest() {
        int n = 5;
        int[] minOutDegrees = new int[] {3, 2, 1, 2, 1, 1, 2};
        MinOutDegreesTest test = new MinOutDegreesTest(n, minOutDegrees);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() >= minOutDegrees[i]);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MinOutDegreesTest(n, minOutDegrees);
        Constraint minDegreeConstraint = test.getConstraint();
        int count = 0;
        while (test.solver.solve()) {
            if (minDegreeConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    //***********************************************************************************
    // maxOutDegree(DirectedGraphVar g, int maxOutDegree)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void maxOutDegree_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int maxOutDegree = 3;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {0, 1}, {0, 2}, {0, 3},
                {1, 2}, {1, 3},
                {2, 0}, {2, 4},
                {3, 4}, {3, 1},
                {4, 5}, {4, 0},
                {5, 1}, {5 ,2}
        };
        MaxOutDegreeTest test = new MaxOutDegreeTest(n, maxOutDegree, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= maxOutDegree);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxOutDegree_instantiatedGraphVarFailTest() {
        int n = 10;
        int maxOutDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MaxOutDegreeTest test = new MaxOutDegreeTest(n, maxOutDegree, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxOutDegree_constrainedSuccessTest() {
        int n = 5;
        int maxOutDegree = 3;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{0, 1}, {0, 2}};
        MaxOutDegreeTest test = new MaxOutDegreeTest(n, maxOutDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 15);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= maxOutDegree);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MaxOutDegreeTest(n, maxOutDegree, nodesLB, edgesLB, null, null);
        Constraint maxDegreeConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 15);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.stronglyConnected(test.g);
        int count = 0;
        while (test.solver.solve()) {
            if (maxDegreeConstraint.isSatisfied() == ESat.TRUE
                    && nbEdgesConstraint.isSatisfied() == ESat.TRUE
                    && connectedConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxOutDegree_constrainedFailTest() {
        int n = 6;
        int maxOutDegree = 2;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MaxOutDegreeTest test = new MaxOutDegreeTest(n, maxOutDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(20, 30);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxOutDegree_generateAndCheckTest() {
        int n = 5;
        int maxOutDegree = 2;
        MaxOutDegreeTest test = new MaxOutDegreeTest(n, maxOutDegree);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= maxOutDegree);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MaxOutDegreeTest(n, maxOutDegree);
        Constraint maxDegreeConstraint = test.getConstraint();
        int count = 0;
        while (test.solver.solve()) {
            if (maxDegreeConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    //***********************************************************************************
    // maxOutDegrees(DirectedGraphVar g, int[] maxOutDegrees)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void maxOutDegrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int[] maxOutDegrees = new int[] {3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {0, 1}, {0, 2}, {0, 3},
                {1, 2}, {1, 3},
                {2, 0}, {2, 4},
                {3, 4}, {3, 1},
                {4, 5}, {4, 0},
                {5, 1}, {5 ,2}
        };
        MaxOutDegreesTest test = new MaxOutDegreesTest(n, maxOutDegrees, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= maxOutDegrees[i]);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxOutDegrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int[] maxOutDegrees = new int[] {2, 1, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MaxOutDegreesTest test = new MaxOutDegreesTest(n, maxOutDegrees, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxOutDegrees_constrainedSuccessTest() {
        int n = 5;
        int[] maxOutDegrees = new int[] {3, 1, 1, 2, 0, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{0, 1}, {0, 2}};
        MaxOutDegreesTest test = new MaxOutDegreesTest(n, maxOutDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 15);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= maxOutDegrees[i]);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MaxOutDegreesTest(n, maxOutDegrees, nodesLB, edgesLB, null, null);
        Constraint maxDegreesConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 15);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.stronglyConnected(test.g);
        int count = 0;
        while (test.solver.solve()) {
            if (maxDegreesConstraint.isSatisfied() == ESat.TRUE
                    && nbEdgesConstraint.isSatisfied() == ESat.TRUE
                    && connectedConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxOutDegrees_constrainedFailTest() {
        int n = 8;
        int[] maxOutDegrees = new int[] {1, 1, 1, 1, 1, 1, 2, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MaxOutDegreesTest test = new MaxOutDegreesTest(n, maxOutDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(40, 70);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxOutDegrees_generateAndCheckTest() {
        int n = 5;
        int[] maxOutDegrees = new int[] {3, 2, 1, 2, 1, 1, 2};
        MaxOutDegreesTest test = new MaxOutDegreesTest(n, maxOutDegrees);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= maxOutDegrees[i]);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MaxOutDegreesTest(n, maxOutDegrees);
        Constraint minDegreeConstraint = test.getConstraint();
        int count = 0;
        while (test.solver.solve()) {
            if (minDegreeConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    //***********************************************************************************
    // outDegrees(DirectedGraphVar g, IntVar outDegrees)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void outDegrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int outDegreesLB = 0;
        int outDegreesUB = 3;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {0, 1}, {0, 2}, {0, 3},
                {1, 2}, {1, 3},
                {2, 0}, {2, 4},
                {3, 4}, {3, 1},
                {4, 5}, {4, 0},
                {5, 1}, {5 ,2}
        };
        OutDegreesBaseTest test = new OutDegreesBaseTest(n, outDegreesLB, outDegreesUB, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= test.outDegrees[i].getValue());
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void outDegrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int outDegreesLB = 0;
        int outDegreesUB = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        OutDegreesBaseTest test = new OutDegreesBaseTest(n, outDegreesLB, outDegreesUB, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void outDegrees_constrainedSuccessTest() {
        int n = 4;
        int outDegreesLB = 0;
        int outDegreesUB = 2;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{0, 1}, {0, 2}};
        OutDegreesBaseTest test = new OutDegreesBaseTest(n, outDegreesLB, outDegreesUB, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 15);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= test.outDegrees[i].getValue());
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new OutDegreesBaseTest(n, outDegreesLB, outDegreesUB, nodesLB, edgesLB, null, null);
        Constraint maxDegreeConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 15);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.stronglyConnected(test.g);
        int count = 0;
        while (test.solver.solve()) {
            if (maxDegreeConstraint.isSatisfied() == ESat.TRUE
                    && nbEdgesConstraint.isSatisfied() == ESat.TRUE
                    && connectedConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    @Test(groups="1s", timeOut=60000)
    public void outDegrees_constrainedFailTest() {
        int n = 6;
        int outDegreesLB = 0;
        int outDegreesUB = 2;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        OutDegreesBaseTest test = new OutDegreesBaseTest(n, outDegreesLB, outDegreesUB, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(20, 30);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void outDegrees_generateAndCheckTest() {
        int n = 4;
        int outDegreesLB = 0;
        int outDegreesUB = 2;
        OutDegreesBaseTest test = new OutDegreesBaseTest(n, outDegreesLB, outDegreesUB);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getSuccessorsOf(i).size() <= test.outDegrees[i].getValue());
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new OutDegreesBaseTest(n, outDegreesLB, outDegreesUB);
        Constraint maxDegreeConstraint = test.getConstraint();
        int count = 0;
        while (test.solver.solve()) {
            if (maxDegreeConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }
}
