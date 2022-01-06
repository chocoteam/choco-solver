/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.tree;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for tree graph constraint.
 * @author Dimitri Justeau-Allaire
 * @since 22/03/2021
 */
public class TreeTest {

    @Test(groups="1s", timeOut=60000)
    public void undirectedInstantiatedSuccessTest() {
        // Empty graph is acyclic
        Model model = new Model();
        int n = 0;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.tree(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // Test acyclic non-empty graph
        model = new Model();
        n = 10;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5},
        };
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.tree(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void undirectedInstantiatedFailTest() {
        // Graph with one node and a self loop is not acyclic
        Model model = new Model();
        int n = 1;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        LB.addNode(0);
        UB.addNode(0);
        LB.addEdge(0, 0);
        UB.addEdge(0, 0);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.tree(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
        // Test non-empty graph with cycles
        model = new Model();
        n = 10;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5},
                {2, 0}, {5, 3}
        };
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.tree(g).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void undirectedConstrainedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{ {0, 1}, {1, 2}, {3, 4}, {6, 7} }
        );
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, 10, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.tree(g).post();
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
    public void undirectedConstrainedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {3, 4}, {6, 7}
                }
        );
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, 10, SetType.BITSET, SetType.BITSET, false);
        for (int i = 0; i <= 8; i++) {
            UB.removeEdge(2, i);
        }
        UB.addEdge(2, 3);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.tree(g).post();
        model.nbEdges(g, model.intVar(15, 20)).post();
        SetVar nodeSet = model.graphNodeSetView(g);
        model.member(2, nodeSet).post();
        model.member(3, nodeSet).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void undirectedGenerateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.tree(g).post();
        List<UndirectedGraph> l1 = new ArrayList<>();
        int realCount = 0;
        while (model.getSolver().solve()) {
            boolean b = true;
            for (UndirectedGraph gl1 : l1) {
                if (gl1.equals(g.getValue())) {
                    b = false;
                }
            }
            if (b) {
                realCount++;
                UndirectedGraph a = GraphFactory.makeUndirectedGraph(n, SetType.BITSET, SetType.BITSET, g.getValue().getNodes().toArray(), new int[][]{});
                for (int i : g.getValue().getNodes()) {
                    for (int j : g.getValue().getNeighborsOf(i)) {
                        a.addEdge(i, j);
                    }
                }
                l1.add(a);
            }
        }
        Assert.assertEquals(realCount, model.getSolver().getSolutionCount());
        // Generate solutions with checker
        Model model2 = new Model();
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model2.graphVar("g", LB2, UB2);
        Constraint cons = model2.tree(g2);
        int count = 0;
        List<UndirectedGraph> l2 = new ArrayList<>();
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
                boolean b = true;
                for (UndirectedGraph gl : l1) {
                    if (gl.equals(g2.getValue())) {
                        b = false;
                    }
                }
                if (b) {
                    System.out.println(g2.getValue());
                    l2.add(g2.getValue());
                }
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }

    @Test(groups="10s", timeOut=60000)
    public void undirectedGenerateAllNodesTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredAllNodesUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.tree(g).post();
        List<UndirectedGraph> l1 = new ArrayList<>();
        int realCount = 0;
        while (model.getSolver().solve()) {
            boolean b = true;
            for (UndirectedGraph gl1 : l1) {
                if (gl1.equals(g.getValue())) {
                    b = false;
                }
            }
            if (b) {
                realCount++;
                UndirectedGraph a = GraphFactory.makeUndirectedGraph(n, SetType.BITSET, SetType.BITSET, g.getValue().getNodes().toArray(), new int[][]{});
                for (int i : g.getValue().getNodes()) {
                    for (int j : g.getValue().getNeighborsOf(i)) {
                        a.addEdge(i, j);
                    }
                }
                l1.add(a);
            }
        }
        Assert.assertEquals(realCount, model.getSolver().getSolutionCount());
        // Generate solutions with checker
        Model model2 = new Model();
        UndirectedGraph LB2 = GraphFactory.makeStoredAllNodesUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model2.graphVar("g", LB2, UB2);
        Constraint cons = model2.tree(g2);
        int count = 0;
        List<UndirectedGraph> l2 = new ArrayList<>();
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
                boolean b = true;
                for (UndirectedGraph gl : l1) {
                    if (gl.equals(g2.getValue())) {
                        b = false;
                    }
                }
                if (b) {
                    System.out.println(g2.getValue());
                    l2.add(g2.getValue());
                }
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
        // Assert that there ase exactly n^(n-2) solutions (Cayley's formula for counting the number of
        // trees with exactly n nodes.
        Assert.assertEquals(count, (int) (Math.pow(n, n - 2)));
    }
}
