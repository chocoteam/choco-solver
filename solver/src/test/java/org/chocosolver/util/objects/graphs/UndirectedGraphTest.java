/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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

}
