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

public class InDegreesTest {

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

    private class MinInDegreeTest extends BaseTest {

        int minInDegree;

        MinInDegreeTest(int n, int minInDegree, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.minInDegree = minInDegree;
        }

        MinInDegreeTest(int n, int minInDegree) {
            super(n);
            this.minInDegree = minInDegree;
        }

        MinInDegreeTest(int n, int minInDegree, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.minInDegree = minInDegree;
        }

        @Override
        Constraint getConstraint() {
            return model.minInDegree(this.g, this.minInDegree);
        }
    }

    private class MaxInDegreeTest extends BaseTest {

        int maxInDegree;

        MaxInDegreeTest(int n, int maxInDegree, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.maxInDegree = maxInDegree;
        }

        MaxInDegreeTest(int n, int maxInDegree) {
            super(n);
            this.maxInDegree = maxInDegree;
        }

        MaxInDegreeTest(int n, int maxInDegree, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.maxInDegree = maxInDegree;
        }

        @Override
        Constraint getConstraint() {
            return this.model.maxInDegree(this.g, this.maxInDegree);
        }
    }

    private class MinInDegreesTest extends BaseTest {

        int[] minInDegrees;

        MinInDegreesTest(int n, int[] minInDegrees, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.minInDegrees = minInDegrees;
        }

        MinInDegreesTest(int n, int[] minInDegrees) {
            super(n);
            this.minInDegrees = minInDegrees;
        }

        MinInDegreesTest(int n, int[] minInDegrees, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.minInDegrees = minInDegrees;
        }

        @Override
        Constraint getConstraint() {
            return this.model.minInDegrees(this.g, minInDegrees);
        }
    }

    private class MaxInDegreesTest extends BaseTest {

        int[] maxInDegrees;

        MaxInDegreesTest(int n, int[] maxInDegrees, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.maxInDegrees = maxInDegrees;
        }

        MaxInDegreesTest(int n, int[] maxInDegrees) {
            super(n);
            this.maxInDegrees = maxInDegrees;
        }

        MaxInDegreesTest(int n, int[] maxInDegrees, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.maxInDegrees = maxInDegrees;
        }

        @Override
        Constraint getConstraint() {
            return this.model.maxInDegrees(this.g, maxInDegrees);
        }
    }

    private class InDegreesBaseTest extends BaseTest {

        int inDegreesLB;
        int inDegreesUB;
        IntVar[] inDegrees;

        InDegreesBaseTest(int n, int inDegreesLB, int inDegreesUB, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.inDegreesLB = inDegreesLB;
            this.inDegreesUB = inDegreesUB;
            this.inDegrees = model.intVarArray(n, inDegreesLB, inDegreesUB);
        }

        InDegreesBaseTest(int n, int inDegreesLB, int inDegreesUB) {
            super(n);
            this.inDegreesLB = inDegreesLB;
            this.inDegreesUB = inDegreesUB;
            this.inDegrees = model.intVarArray(n, inDegreesLB, inDegreesUB);
        }

        InDegreesBaseTest(int n, int inDegreesLB, int inDegreesUB, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.inDegreesLB = inDegreesLB;
            this.inDegreesUB = inDegreesUB;
            this.inDegrees = model.intVarArray(n, inDegreesLB, inDegreesUB);
        }

        @Override
        Constraint getConstraint() {
            return this.model.inDegrees(this.g, this.inDegrees);
        }
    }

    //***********************************************************************************
    // minInDegree(DirectedGraphVar g, int minInDegree)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void minInDegree_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int minInDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {1, 0}, {2, 0},
                {0, 1}, {3, 1},
                {0, 2}, {5, 2},
                {4, 3}, {3, 3},
                {2, 4}, {5, 4},
                {0, 5}, {1, 5}
        };
        MinInDegreeTest test = new MinInDegreeTest(n, minInDegree, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() >= minInDegree);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void minInDegree_instantiatedGraphVarFailTest() {
        int n = 10;
        int minInDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MinInDegreeTest test = new MinInDegreeTest(n, minInDegree, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minInDegree_constrainedSuccessTest() {
        int n = 5;
        int minInDegree = 3;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{0, 1}, {1, 2}};
        MinInDegreeTest test = new MinInDegreeTest(n, minInDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() >= minInDegree);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MinInDegreeTest(n, minInDegree, nodesLB, edgesLB, null, null);
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
    public void minInDegree_constrainedFailTest() {
        int n = 8;
        int minInDegree = 4;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MinInDegreeTest test = new MinInDegreeTest(n, minInDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 4);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minInDegree_generateAndCheckTest() {
        int n = 5;
        int minInDegree = 2;
        MinInDegreeTest test = new MinInDegreeTest(n, minInDegree);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() >= minInDegree);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MinInDegreeTest(n, minInDegree);
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
    // minInDegrees(DirectedGraphVar g, int[] minInDegrees)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void minInDegrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int[] minInDegrees = new int[] {3, 1, 2, 2, 2, 2, 0, 0, 0, 0};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {1, 0}, {2, 0}, {5, 0},
                {0, 1}, {3, 1},
                {0, 2}, {5, 2},
                {4, 3}, {3, 3},
                {2, 4}, {5, 4},
                {0, 5}, {1, 5}
        };
        MinInDegreesTest test = new MinInDegreesTest(n, minInDegrees, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() >= minInDegrees[i]);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void minInDegrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int[] minInDegrees = new int[] {3, 1, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MinInDegreesTest test = new MinInDegreesTest(n, minInDegrees, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minInDegrees_constrainedSuccessTest() {
        int n = 5;
        int[] minInDegrees = new int[] {3, 1, 1, 2, 0, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{1, 2}, {2, 0}};
        MinInDegreesTest test = new MinInDegreesTest(n, minInDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 15);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() >= minInDegrees[i]);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MinInDegreesTest(n, minInDegrees, nodesLB, edgesLB, null, null);
        Constraint minDegreesConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 15);
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
    public void minInDegrees_constrainedFailTest() {
        int n = 8;
        int[] minInDegrees = new int[] {3, 3, 1, 2, 1, 1, 2, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MinInDegreesTest test = new MinInDegreesTest(n, minInDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 4);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minInDegrees_generateAndCheckTest() {
        int n = 5;
        int[] minInDegrees = new int[] {3, 2, 1, 2, 1, 1, 2};
        MinInDegreesTest test = new MinInDegreesTest(n, minInDegrees);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() >= minInDegrees[i]);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MinInDegreesTest(n, minInDegrees);
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
    // maxInDegree(DirectedGraphVar g, int maxInDegree)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void maxInDegree_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int maxInDegree = 3;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {1, 0}, {2, 0}, {5, 0},
                {0, 1}, {3, 1},
                {0, 2}, {5, 2},
                {4, 3}, {3, 3},
                {2, 4}, {5, 4},
                {0, 5}, {1, 5}
        };
        MaxInDegreeTest test = new MaxInDegreeTest(n, maxInDegree, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= maxInDegree);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxInDegree_instantiatedGraphVarFailTest() {
        int n = 10;
        int maxInDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{1, 0}, {2, 0}, {3, 0}, {1, 2}, {3, 4}, {4, 5}};
        MaxInDegreeTest test = new MaxInDegreeTest(n, maxInDegree, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxInDegree_constrainedSuccessTest() {
        int n = 5;
        int maxInDegree = 3;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{0, 1}, {0, 2}};
        MaxInDegreeTest test = new MaxInDegreeTest(n, maxInDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= maxInDegree);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MaxInDegreeTest(n, maxInDegree, nodesLB, edgesLB, null, null);
        Constraint maxDegreeConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 20);
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
    public void maxInDegree_constrainedFailTest() {
        int n = 6;
        int maxInDegree = 2;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MaxInDegreeTest test = new MaxInDegreeTest(n, maxInDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(15, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxInDegree_generateAndCheckTest() {
        int n = 5;
        int maxInDegree = 2;
        MaxInDegreeTest test = new MaxInDegreeTest(n, maxInDegree);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= maxInDegree);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MaxInDegreeTest(n, maxInDegree);
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
    // maxInDegrees(DirectedGraphVar g, int[] maxInDegrees)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void maxInDegrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int[] maxInDegrees = new int[] {3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {1, 0}, {2, 0}, {5, 0},
                {0, 1}, {3, 1},
                {0, 2}, {5, 2},
                {4, 3}, {3, 3},
                {2, 4}, {5, 4},
                {0, 5}, {1, 5}        };
        MaxInDegreesTest test = new MaxInDegreesTest(n, maxInDegrees, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= maxInDegrees[i]);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxInDegrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int[] maxInDegrees = new int[] {2, 1, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{1, 0}, {2, 0}, {3, 0}, {1, 2}, {3, 4}, {4, 5}};
        MaxInDegreesTest test = new MaxInDegreesTest(n, maxInDegrees, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxInDegrees_constrainedSuccessTest() {
        int n = 5;
        int[] maxInDegrees = new int[] {3, 2, 2, 2, 0, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{1, 2}, {2, 1}};
        MaxInDegreesTest test = new MaxInDegreesTest(n, maxInDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= maxInDegrees[i]);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MaxInDegreesTest(n, maxInDegrees, nodesLB, edgesLB, null, null);
        Constraint maxDegreesConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 20);
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
    public void maxInDegrees_constrainedFailTest() {
        int n = 8;
        int[] maxInDegrees = new int[] {1, 1, 1, 2, 1, 1, 2, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MaxInDegreesTest test = new MaxInDegreesTest(n, maxInDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(30, 40);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxInDegrees_generateAndCheckTest() {
        int n = 5;
        int[] maxInDegrees = new int[] {3, 2, 1, 2, 1, 1, 2};
        MaxInDegreesTest test = new MaxInDegreesTest(n, maxInDegrees);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= maxInDegrees[i]);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MaxInDegreesTest(n, maxInDegrees);
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
    // inDegrees(DirectedGraphVar g, IntVar inDegree)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void inDegrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int inDegreesLB = 0;
        int inDegreesUB = 3;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {1, 0}, {2, 0}, {5, 0},
                {0, 1}, {3, 1},
                {0, 2}, {5, 2},
                {4, 3}, {3, 3},
                {2, 4}, {5, 4},
                {0, 5}, {1, 5}
        };
        InDegreesBaseTest test = new InDegreesBaseTest(n, inDegreesLB, inDegreesUB, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= test.inDegrees[i].getValue());
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void inDegrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int inDegreesLB = 0;
        int inDegreesUB = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{1, 0}, {2, 0}, {3, 0}, {1, 2}, {3, 4}, {4, 5}};
        InDegreesBaseTest test = new InDegreesBaseTest(n, inDegreesLB, inDegreesUB, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void inDegrees_constrainedSuccessTest() {
        int n = 4;
        int inDegreesLB = 0;
        int inDegreesUB = 2;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {{0, 1}, {0, 2}};
        InDegreesBaseTest test = new InDegreesBaseTest(n, inDegreesLB, inDegreesUB, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= test.inDegrees[i].getValue());
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new InDegreesBaseTest(n, inDegreesLB, inDegreesUB, nodesLB, edgesLB, null, null);
        Constraint maxDegreeConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 20);
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
    public void inDegrees_constrainedFailTest() {
        int n = 6;
        int inDegreesLB = 0;
        int inDegreesUB = 2;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        InDegreesBaseTest test = new InDegreesBaseTest(n, inDegreesLB, inDegreesUB, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(15, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.stronglyConnected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void inDegrees_generateAndCheckTest() {
        int n = 4;
        int inDegreesLB = 0;
        int inDegreesUB = 2;
        InDegreesBaseTest test = new InDegreesBaseTest(n, inDegreesLB, inDegreesUB);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getPredecessorsOf(i).size() <= test.inDegrees[i].getValue());
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new InDegreesBaseTest(n, inDegreesLB, inDegreesUB);
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
