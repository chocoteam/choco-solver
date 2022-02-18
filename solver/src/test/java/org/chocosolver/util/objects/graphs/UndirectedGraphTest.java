/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.graphs;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test for UndirectedGraph class
 * @author Dimitri Justeau-Allaire
 * @since 17/03/2021
 */
public class UndirectedGraphTest {

    @Test(groups="1s", timeOut=60000)
    public void equalsTest() {
        Model model = new Model();
        int n = 10;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5, 6};
        int[][] edges = new int[][] {
                {0, 1}, {2, 3}, {4, 5},
                {6, 0}, {2, 1}, {5, 3}
        };
        UndirectedGraph g1 = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UndirectedGraph g2 = GraphFactory.makeUndirectedGraph(n, SetType.BIPARTITESET, SetType.BIPARTITESET, nodes, edges);
        Assert.assertTrue(g1.equals(g2));
        Assert.assertTrue(g2.equals(g1));
        g1.addNode(7);
        Assert.assertFalse(g1.equals(g2));
        Assert.assertFalse(g2.equals(g1));
        g2.addNode(7);
        Assert.assertTrue(g1.equals(g2));
        Assert.assertTrue(g2.equals(g1));
        g2.addEdge(0, 7);
        Assert.assertFalse(g1.equals(g2));
        Assert.assertFalse(g2.equals(g1));
        g1 = GraphFactory.makeStoredAllNodesUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, true);
        g2 = GraphFactory.makeAllNodesUndirectedGraph(n + 1, SetType.BIPARTITESET, SetType.BIPARTITESET, false);
        Assert.assertFalse(g1.equals(g2));
        Assert.assertFalse(g2.equals(g1));
        g2.removeNode(n);
        Assert.assertTrue(g1.equals(g2));
        Assert.assertTrue(g2.equals(g1));
        g2 = GraphFactory.makeCompleteUndirectedGraph(n, SetType.BITSET, SetType.RANGESET, false);
        Assert.assertFalse(g1.equals(g2));
        Assert.assertFalse(g2.equals(g1));
        g1 = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, true);
        Assert.assertTrue(g1.equals(g2));
        Assert.assertTrue(g2.equals(g1));
    }

    @Test(groups="1s", timeOut=60000)
    public void testEdgeInduced() {
        Model m = new Model();
        UndirectedGraph g = GraphFactory.makeCompleteStoredUndirectedGraph(m, 10, SetType.BITSET, SetType.BITSET, false);
        int[][] edges = new int[][] {
                {0, 1}, {1, 2}, {1, 7}, {3, 4}, {5, 7}
        };
        UndirectedGraph g2 = new UndirectedGraph(m, g, edges, false);
        int[] nodes = g2.getNodes().toArray();
        Arrays.sort(nodes);
        int[] expNodes = new int[] {0, 1, 2, 3, 4, 5, 7};
        Assert.assertTrue(Arrays.equals(expNodes, nodes));
        int[] neigh0 = g2.getNeighborsOf(0).toArray(); Arrays.sort(neigh0);
        int[] neigh1 = g2.getNeighborsOf(1).toArray(); Arrays.sort(neigh1);
        int[] neigh2 = g2.getNeighborsOf(2).toArray(); Arrays.sort(neigh2);
        int[] neigh3 = g2.getNeighborsOf(3).toArray(); Arrays.sort(neigh3);
        int[] neigh4 = g2.getNeighborsOf(4).toArray(); Arrays.sort(neigh4);
        int[] neigh5 = g2.getNeighborsOf(5).toArray(); Arrays.sort(neigh5);
        int[] neigh7 = g2.getNeighborsOf(7).toArray(); Arrays.sort(neigh7);
        Assert.assertTrue(Arrays.equals(neigh0, new int[] {1}));
        Assert.assertTrue(Arrays.equals(neigh1, new int[] {0, 2, 7}));
        Assert.assertTrue(Arrays.equals(neigh2, new int[] {1}));
        Assert.assertTrue(Arrays.equals(neigh3, new int[] {4}));
        Assert.assertTrue(Arrays.equals(neigh4, new int[] {3}));
        Assert.assertTrue(Arrays.equals(neigh5, new int[] {7}));
        Assert.assertTrue(Arrays.equals(neigh7, new int[] {1, 5}));
    }
}
