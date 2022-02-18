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

/**
 * Test for DirectedGraph class
 * @author Dimitri Justeau-Allaire
 * @since 17/03/2021
 */
public class DirectedGraphTest {

    @Test(groups="1s", timeOut=60000)
    public void equalsTest() {
        Model model = new Model();
        int n = 10;
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5, 6};
        int[][] edges = new int[][] {
                {0, 1}, {2, 3}, {4, 5},
                {6, 0}, {2, 1}, {5, 3}
        };
        DirectedGraph g1 = GraphFactory.makeStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        DirectedGraph g2 = GraphFactory.makeDirectedGraph(n, SetType.BIPARTITESET, SetType.BIPARTITESET, nodes, edges);
        Assert.assertTrue(g1.equals(g2));
        Assert.assertTrue(g2.equals(g1));
        g1.addNode(7);
        Assert.assertFalse(g1.equals(g2));
        Assert.assertFalse(g2.equals(g1));
        g2.addNode(7);
        Assert.assertTrue(g1.equals(g2));
        Assert.assertTrue(g2.equals(g1));
        g2.addEdge(0, 7);
        g1.addEdge(7, 0);
        Assert.assertFalse(g1.equals(g2));
        Assert.assertFalse(g2.equals(g1));
        g1 = GraphFactory.makeStoredAllNodesDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, true);
        g2 = GraphFactory.makeAllNodesDirectedGraph(n + 1, SetType.BIPARTITESET, SetType.BIPARTITESET, false);
        Assert.assertFalse(g1.equals(g2));
        Assert.assertFalse(g2.equals(g1));
        g2.removeNode(n);
        Assert.assertTrue(g1.equals(g2));
        Assert.assertTrue(g2.equals(g1));
        g2 = GraphFactory.makeCompleteDirectedGraph(n, SetType.BITSET, SetType.RANGESET, false);
        Assert.assertFalse(g1.equals(g2));
        Assert.assertFalse(g2.equals(g1));
        g1 = GraphFactory.makeCompleteStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, true);
        Assert.assertTrue(g1.equals(g2));
        Assert.assertTrue(g2.equals(g1));
    }

}
