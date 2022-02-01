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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for degrees graph constraints
 * @author Dimitri Justeau-Allaire
 * @since 21/03/2021
 */
public class DegreesTest {

    private abstract class BaseTest {

        Model model;
        Solver solver;
        UndirectedGraphVar g;
        int n;
        SetType setType = SetType.BITSET;

        BaseTest(int n, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            this.model = new Model();
            this.solver = model.getSolver();
            this.n = n;
            UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType, nodesLB, edgesLB);
            UndirectedGraph UB;
            if (nodesUB != null) {
                UB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType, nodesUB, edgesUB);
            } else {
                UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
            }
            this.g = model.graphVar("g", LB, UB);
        }

        BaseTest(int n) {
            this.model = new Model();
            this.solver = model.getSolver();
            this.n = n;
            UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
            UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
            this.g = model.graphVar("g", LB, UB);
        }

        BaseTest(int n, int[] nodes, int[][] edges) {
            this.model = new Model();
            this.solver = model.getSolver();
            this.n = n;
            UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType, nodes, edges);
            UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType, nodes, edges);
            this.g = model.graphVar("g", LB, UB);
        }

        abstract Constraint getConstraint();
    }

    private class MinDegreeTest extends BaseTest {

        int minDegree;

        MinDegreeTest(int n, int minDegree, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.minDegree = minDegree;
        }

        MinDegreeTest(int n, int minDegree) {
            super(n);
            this.minDegree = minDegree;
        }

        MinDegreeTest(int n, int minDegree, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.minDegree = minDegree;
        }

        @Override
        Constraint getConstraint() {
            return model.minDegree(this.g, this.minDegree);
        }
    }

    private class MaxDegreeTest extends BaseTest {

        int maxDegree;

        MaxDegreeTest(int n, int maxDegree, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.maxDegree = maxDegree;
        }

        MaxDegreeTest(int n, int maxDegree) {
            super(n);
            this.maxDegree = maxDegree;
        }

        MaxDegreeTest(int n, int maxDegree, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.maxDegree = maxDegree;
        }

        @Override
        Constraint getConstraint() {
            return this.model.maxDegree(this.g, this.maxDegree);
        }
    }

    private class MinDegreesTest extends BaseTest {

        int[] minDegrees;

        MinDegreesTest(int n, int[] minDegrees, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.minDegrees = minDegrees;
        }

        MinDegreesTest(int n, int[] minDegrees) {
            super(n);
            this.minDegrees = minDegrees;
        }

        MinDegreesTest(int n, int[] minDegrees, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.minDegrees = minDegrees;
        }

        @Override
        Constraint getConstraint() {
            return this.model.minDegrees(this.g, minDegrees);
        }
    }
    private class MaxDegreesTest extends BaseTest {

        int[] maxDegrees;

        MaxDegreesTest(int n, int[] maxDegrees, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.maxDegrees = maxDegrees;
        }

        MaxDegreesTest(int n, int[] maxDegrees) {
            super(n);
            this.maxDegrees = maxDegrees;
        }

        MaxDegreesTest(int n, int[] maxDegrees, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.maxDegrees = maxDegrees;
        }

        @Override
        Constraint getConstraint() {
            return this.model.maxDegrees(this.g, maxDegrees);
        }
    }

    private class DegreesBaseTest extends BaseTest {

        int degreesLB;
        int degreesUB;
        IntVar[] degrees;

        DegreesBaseTest(int n, int degreesLB, int degreesUB, int[] nodesLB, int[][] edgesLB, int[] nodesUB, int[][] edgesUB) {
            super(n, nodesLB, edgesLB, nodesUB, edgesUB);
            this.degreesLB = degreesLB;
            this.degreesUB = degreesUB;
            this.degrees = model.intVarArray(n, degreesLB, degreesUB);
        }

        DegreesBaseTest(int n, int degreesLB, int degreesUB) {
            super(n);
            this.degreesLB = degreesLB;
            this.degreesUB = degreesUB;
            this.degrees = model.intVarArray(n, degreesLB, degreesUB);        }

        DegreesBaseTest(int n, int degreesLB, int degreesUB, int[] nodes, int[][] edges) {
            super(n, nodes, edges);
            this.degreesLB = degreesLB;
            this.degreesUB = degreesUB;
            this.degrees = model.intVarArray(n, degreesLB, degreesUB);        }

        @Override
        Constraint getConstraint() {
            return this.model.degrees(this.g, this.degrees);
        }
    }

    //***********************************************************************************
    // minDegree(UndirectedGraphVar g, int minDegree)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void minDegree_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int minDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}, {2, 5}};
        MinDegreeTest test = new MinDegreeTest(n, minDegree, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() >= minDegree);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void minDegree_instantiatedGraphVarFailTest() {
        int n = 10;
        int minDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MinDegreeTest test = new MinDegreeTest(n, minDegree, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void minDegree_constrainedSuccessTest() {
        int n = 6;
        int minDegree = 3;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MinDegreeTest test = new MinDegreeTest(n, minDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 10);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() >= minDegree);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MinDegreeTest(n, minDegree, nodesLB, edgesLB, null, null);
        Constraint minDegreeConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 10);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.connected(test.g);
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
    public void minDegree_constrainedFailTest() {
        int n = 8;
        int minDegree = 4;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MinDegreeTest test = new MinDegreeTest(n, minDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 4);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minDegree_generateAndCheckTest() {
        int n = 7;
        int minDegree = 2;
        MinDegreeTest test = new MinDegreeTest(n, minDegree);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() >= minDegree);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MinDegreeTest(n, minDegree);
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
    // minDegrees(UndirectedGraphVar g, int[] minDegrees)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void minDegrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int[] minDegrees = new int[] {3, 1, 2, 2, 2, 2, 0, 0, 0, 0};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}, {2, 5}};
        MinDegreesTest test = new MinDegreesTest(n, minDegrees, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() >= minDegrees[i]);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void minDegrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int[] minDegrees = new int[] {3, 1, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MinDegreesTest test = new MinDegreesTest(n, minDegrees, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void minDegrees_constrainedSuccessTest() {
        int n = 6;
        int[] minDegrees = new int[] {3, 1, 1, 2, 0, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MinDegreesTest test = new MinDegreesTest(n, minDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 10);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() >= minDegrees[i]);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MinDegreesTest(n, minDegrees, nodesLB, edgesLB, null, null);
        Constraint minDegreesConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 10);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.connected(test.g);
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
    public void minDegrees_constrainedFailTest() {
        int n = 8;
        int[] minDegrees = new int[] {3, 3, 1, 2, 1, 1, 2, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MinDegreesTest test = new MinDegreesTest(n, minDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 4);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void minDegrees_generateAndCheckTest() {
        int n = 7;
        int[] minDegrees = new int[] {3, 2, 1, 2, 1, 1, 2};
        MinDegreesTest test = new MinDegreesTest(n, minDegrees);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() >= minDegrees[i]);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MinDegreesTest(n, minDegrees);
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
    // maxDegree(UndirectedGraphVar g, int maxDegree)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void maxDegree_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int maxDegree = 3;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}, {2, 5}};
        MaxDegreeTest test = new MaxDegreeTest(n, maxDegree, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() <= maxDegree);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxDegree_instantiatedGraphVarFailTest() {
        int n = 10;
        int maxDegree = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MaxDegreeTest test = new MaxDegreeTest(n, maxDegree, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxDegree_constrainedSuccessTest() {
        int n = 6;
        int maxDegree = 3;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MaxDegreeTest test = new MaxDegreeTest(n, maxDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 10);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() <= maxDegree);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MaxDegreeTest(n, maxDegree, nodesLB, edgesLB, null, null);
        Constraint maxDegreeConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 10);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.connected(test.g);
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
    public void maxDegree_constrainedFailTest() {
        int n = 8;
        int maxDegree = 2;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MaxDegreeTest test = new MaxDegreeTest(n, maxDegree, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(15, 20);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxDegree_generateAndCheckTest() {
        int n = 7;
        int maxDegree = 2;
        MaxDegreeTest test = new MaxDegreeTest(n, maxDegree);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() <= maxDegree);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MaxDegreeTest(n, maxDegree);
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
    // maxDegrees(UndirectedGraphVar g, int[] maxDegrees)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void maxDegrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int[] maxDegrees = new int[] {3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}, {2, 5}};
        MaxDegreesTest test = new MaxDegreesTest(n, maxDegrees, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() <= maxDegrees[i]);
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxDegrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int[] maxDegrees = new int[] {3, 1, 2, 2, 2, 2, 2, 2, 2, 2};
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        MaxDegreesTest test = new MaxDegreesTest(n, maxDegrees, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void maxDegrees_constrainedSuccessTest() {
        int n = 6;
        int[] maxDegrees = new int[] {3, 1, 1, 2, 0, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MaxDegreesTest test = new MaxDegreesTest(n, maxDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 10);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() <= maxDegrees[i]);
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new MaxDegreesTest(n, maxDegrees, nodesLB, edgesLB, null, null);
        Constraint maxDegreesConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 10);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.connected(test.g);
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
    public void maxDegrees_constrainedFailTest() {
        int n = 8;
        int[] maxDegrees = new int[] {1, 1, 1, 2, 1, 1, 2, 1};
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        MaxDegreesTest test = new MaxDegreesTest(n, maxDegrees, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 8);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void maxDegrees_generateAndCheckTest() {
        int n = 7;
        int[] maxDegrees = new int[] {3, 2, 1, 2, 1, 1, 2};
        MaxDegreesTest test = new MaxDegreesTest(n, maxDegrees);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() <= maxDegrees[i]);
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new MaxDegreesTest(n, maxDegrees);
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
    // degrees(UndirectedGraphVar g, IntVar[] degrees)
    //***********************************************************************************

    @Test(groups="1s", timeOut=60000)
    public void degrees_instantiatedGraphVarSuccessTest() {
        int n = 10;
        int degreesLB = 0;
        int degreesUB = 3;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}, {2, 5}};
        DegreesBaseTest test = new DegreesBaseTest(n, degreesLB, degreesUB, nodes, edges);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() == test.degrees[i].getValue());
            }
        }
        Assert.assertEquals(test.solver.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void degrees_instantiatedGraphVarFailTest() {
        int n = 10;
        int degreesLB = 0;
        int degreesUB = 2;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {3, 4}, {4, 5}};
        DegreesBaseTest test = new DegreesBaseTest(n, degreesLB, degreesUB, nodes, edges);
        test.getConstraint().post();
        test.solver.solve();
        Assert.assertEquals(test.solver.getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void degrees_constrainedSuccessTest() {
        int n = 5;
        int degreesLB = 0;
        int degreesUB = 2;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        DegreesBaseTest test = new DegreesBaseTest(n, degreesLB, degreesUB, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 10);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() == test.degrees[i].getValue());
            }
        }
        long nbSolutions = test.solver.getSolutionCount();
        Assert.assertTrue(nbSolutions > 1);
        // Check that no solution were missed
        test = new DegreesBaseTest(n, degreesLB, degreesUB, nodesLB, edgesLB, null, null);
        Constraint maxDegreesConstraint = test.getConstraint();
        nbEdges = test.model.intVar(0, 10);
        Constraint nbEdgesConstraint = test.model.nbEdges(test.g, nbEdges);
        Constraint connectedConstraint = test.model.connected(test.g);
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
    public void degrees_constrainedFailTest() {
        int n = 8;
        int degreesLB = 0;
        int degreesUB = 1;
        int[] nodesLB = new int[] {0, 1, 2};
        int[][] edgesLB = new int[][] {};
        DegreesBaseTest test = new DegreesBaseTest(n, degreesLB, degreesUB, nodesLB, edgesLB, null, null);
        test.getConstraint().post();
        IntVar nbEdges = test.model.intVar(0, 8);
        test.model.nbEdges(test.g, nbEdges).post();
        test.model.connected(test.g).post();
        test.model.getSolver().solve();
        Assert.assertEquals(test.model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void degrees_generateAndCheckTest() {
        int n = 5;
        int degreesLB = 0;
        int degreesUB = 2;
        DegreesBaseTest test = new DegreesBaseTest(n, degreesLB, degreesUB);
        test.getConstraint().post();
        while (test.solver.solve()) {
            for (int i : test.g.getValue().getNodes()) {
                Assert.assertTrue(test.g.getValue().getNeighborsOf(i).size() <= test.degrees[i].getValue());
            }
        }
        Assert.assertTrue(test.solver.getSolutionCount() > 1);
        long nbSolutions = test.solver.getSolutionCount();
        // Check that no solution were missed
        test = new DegreesBaseTest(n, degreesLB, degreesUB);
        Constraint minDegreeConstraint = test.getConstraint();
        int count = 0;
        while (test.solver.solve()) {
            if (minDegreeConstraint.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }
}
