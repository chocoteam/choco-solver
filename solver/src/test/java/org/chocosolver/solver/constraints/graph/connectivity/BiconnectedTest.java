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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for biconnected graph constraint
 * @author Dimitri Justeau-Allaire
 * @since 19/03/2021
 */
public class BiconnectedTest {

    @Test(groups="1s", timeOut=60000)
    public void instantiatedSuccessTest() {
        // Complete graph with more than one node are biconnected
        Model model = new Model();
        int n = 10;
        UndirectedGraph LB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // Test biconnected not complete graph - 1
        model = new Model();
        n = 10;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5},
                {0, 2}, {0, 3}, {0, 4}, {0, 5},
        };
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // Test biconnected not complete graph - 3
        model = new Model();
        n = 10;
        nodes = new int[] {0, 1, 2, 3};
        edges = new int[][] {
                {0, 1}, {0, 2}, {0, 3}, {2, 3}, {1, 3}
        };
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedFailTest() {
        // Empty graph is not biconnected
        Model model = new Model();
        int n = 10;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
        // Graph with 1 node is not biconnected
        model = new Model();
        n = 1;
        int[] nodes = new int[] {0};
        int[][] edges = new int[][] {};
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
        // Test connected but not biconnected with several nodes
        model = new Model();
        n = 10;
        nodes = new int[] {0, 1, 2, 3, 4, 5};
        edges = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}
        };
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0},
                        {3, 4},
                        {6, 7}
                }
        );
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, 10, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        model.nbEdges(g, model.intVar(4, 10)).post();
        SetVar nodeSet = model.graphNodeSetView(g);
        model.member(2, nodeSet).post();
        model.member(3, nodeSet).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(g.getValue().getNodes().contains(2));
            Assert.assertTrue(g.getValue().getNodes().contains(3));
        }
        Assert.assertTrue(model.getSolver().getSolutionCount() > 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0},
                        {3, 4},
                        {6, 7}
                }
        );
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, 10, SetType.BITSET, SetType.BITSET, false);
        for (int i = 0; i <= 8; i++) {
            UB.removeEdge(2, i);
        }
        UB.addEdge(2, 3);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        model.nbEdges(g, model.intVar(4, 10)).post();
        SetVar nodeSet = model.graphNodeSetView(g);
        model.member(2, nodeSet).post();
        model.member(3, nodeSet).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void generateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.biconnected(g).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model2.graphVar("g", LB2, UB2);
        Constraint cons = model2.biconnected(g2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }

}
