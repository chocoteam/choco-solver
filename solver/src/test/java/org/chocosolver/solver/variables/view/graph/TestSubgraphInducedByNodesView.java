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
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test class for subgraphInducedByNodesView
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestSubgraphInducedByNodesView {

    @Test(groups = "1s", timeOut = 60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        UndirectedGraphVar g2 = m.subgraphInducedByNodesView(g, SetFactory.makeConstantSet(new int[]{0, 1, 2}));
        Assert.assertEquals(g2.getMandatoryNodes().size(), 0);
        Assert.assertEquals(g2.getPotentialNodes().size(), 3);
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            Assert.assertTrue(!g2.getValue().containsNode(3));
            Assert.assertTrue(!g2.getValue().containsNode(4));
            for (int i = 0; i < 3; i++) {
                if (g.getValue().containsNode(i)) {
                    Assert.assertTrue(g2.getValue().containsNode(i));
                    for (int j : g.getValue().getNeighborsOf(i)) {
                        if (j != 3 && j != 4) {
                            Assert.assertTrue(g2.getValue().containsEdge(i, j));
                        }
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testInstantiate() {
        Model m = new Model();
        int n = 8;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        UndirectedGraphVar g2 = m.subgraphInducedByNodesView(g, SetFactory.makeConstantSet(new int[]{0, 1, 2}));
        UndirectedGraph gVal = GraphFactory.makeUndirectedGraph(
                n, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1},
                new int[][] {
                        {0, 1}, {1, 0}
                }
        );
        try {
            g2.instantiateTo(gVal, new ICause() {});
            Assert.assertTrue(g2.isInstantiated());
            Assert.assertTrue(g2.getValue().equals(gVal));
            Assert.assertFalse(g.isInstantiated());
            Assert.assertEquals(g.getPotentialNodes().size(), 7);
            Assert.assertEquals(g.getMandatoryNeighborsOf(0).size(), 1);
            Assert.assertEquals(g.getPotentialNeighborsOf(0).size(), 6);
            Assert.assertEquals(g.getMandatoryNeighborsOf(1).size(), 1);
            Assert.assertEquals(g.getPotentialNeighborsOf(1).size(), 6);
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
        ISet nodes = SetFactory.makeConstantSet(new int[] {0, 1, 2});
        UndirectedGraphVar g2 = m.subgraphInducedByNodesView(g, nodes);
        SetVar nodesG = m.graphNodeSetView(g);
        m.member(3, nodesG).post();
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.connected(g2).post();
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            Assert.assertTrue(!g2.getValue().containsNode(3));
            Assert.assertTrue(g.getValue().containsNode(3));
            Assert.assertTrue(g2.getValue().getNodes().size() >= 1 && g2.getValue().getNodes().size() <= 3);
            for (int i : nodes) {
                if (g.getValue().containsNode(i)) {
                    Assert.assertTrue(g2.getValue().containsNode(i));
                    for (int j : g.getValue().getNeighborsOf(i)) {
                        if (nodes.contains(j)) {
                            Assert.assertTrue(g2.getValue().containsEdge(i, j));
                        }
                    }
                }
            }
        }
    }
}
