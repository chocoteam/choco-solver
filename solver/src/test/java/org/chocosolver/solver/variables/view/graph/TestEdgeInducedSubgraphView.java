/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.graph;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test class for edge-induced subgraph view
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestEdgeInducedSubgraphView {

    @Test(groups = "1s", timeOut = 60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1},
                {1, 2}, {1, 3}, {1, 5},
                {2, 5}
        };
        UndirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, false);
        ISet[] neigh = UndirectedGraph.edgesArrayToEdgesSets(g.getNbMaxNodes(), edges);
        Assert.assertEquals(g2.getMandatoryNodes().size(), 0);
        Assert.assertEquals(g2.getPotentialNodes().size(), 5);
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            Assert.assertTrue(!g2.getValue().containsNode(4));
            for (int i : g.getValue().getNodes()) {
                for (int j : g.getValue().getNeighborsOf(i)) {
                    if (neigh[i].contains(j)) {
                        Assert.assertTrue(g2.getValue().containsNode(i));
                        Assert.assertTrue(g2.getValue().containsNode(j));
                    }
                }
                if (g2.getValue().containsNode(i)) {
                    Assert.assertTrue(g2.getValue().getNeighborsOf(i).size() <= neigh[i].size());
                    for (int j : g2.getValue().getNeighborsOf(i)) {
                        Assert.assertTrue(g.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testInstantiate() {
        Model m = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1},
                {1, 2}, {1, 3}, {1, 5},
                {2, 5}
        };
        UndirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, false);
        UndirectedGraph gVal = GraphFactory.makeUndirectedGraph(
                n, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 3},
                new int[][] {
                        {0, 1}, {1, 3}
                }
        );
        try {
            g2.instantiateTo(gVal, new ICause() {});
            Assert.assertTrue(g2.isInstantiated());
            Assert.assertTrue(g2.getValue().equals(gVal));
            Assert.assertFalse(g.isInstantiated());
            Assert.assertEquals(g.getPotentialNodes().size(), 4);
            Assert.assertEquals(g.getMandatoryNeighborsOf(0).size(), 1);
            Assert.assertEquals(g.getPotentialNeighborsOf(0).size(), 3);
            Assert.assertEquals(g.getMandatoryNeighborsOf(1).size(), 2);
            Assert.assertEquals(g.getPotentialNeighborsOf(1).size(), 3);
            Assert.assertEquals(g.getMandatoryNeighborsOf(3).size(), 1);
            Assert.assertEquals(g.getPotentialNeighborsOf(3).size(), 3);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1},
                {1, 2}, {1, 3}, {1, 5},
                {2, 5}
        };
        UndirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, false);
        ISet[] neigh = UndirectedGraph.edgesArrayToEdgesSets(g.getNbMaxNodes(), edges);
        SetVar nodesG = m.graphNodeSetView(g);
        m.member(3, nodesG).post();
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.connected(g2).post();
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            Assert.assertTrue(g.getValue().containsNode(3));
            for (int i : g.getValue().getNodes()) {
                for (int j : g.getValue().getNeighborsOf(i)) {
                    if (neigh[i].contains(j)) {
                        Assert.assertTrue(g2.getValue().containsNode(i));
                        Assert.assertTrue(g2.getValue().containsNode(j));
                    }
                }
                if (g2.getValue().containsNode(i)) {
                    Assert.assertTrue(g2.getValue().getNeighborsOf(i).size() <= neigh[i].size());
                    for (int j : g2.getValue().getNeighborsOf(i)) {
                        Assert.assertTrue(g.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }
}
