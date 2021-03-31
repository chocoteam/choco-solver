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
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test class for DirectedSubgraphExcludedNodesView
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestDirectedSubgraphExcludeNodesView {

    @Test(groups = "1s", timeOut = 60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        DirectedGraphVar g2 = m.subgraphExcludeNodesView(g, SetFactory.makeConstantSet(new int[]{0, 4}));
        Assert.assertEquals(g2.getMandatoryNodes().size(), 0);
        Assert.assertEquals(g2.getPotentialNodes().size(), 3);
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            Assert.assertTrue(!g2.getValue().containsNode(0));
            Assert.assertTrue(!g2.getValue().containsNode(4));
            for (int i = 1; i < 4; i++) {
                if (g.getValue().containsNode(i)) {
                    Assert.assertTrue(g2.getValue().containsNode(i));
                    for (int j : g.getValue().getSuccessorsOf(i)) {
                        if (j != 0 && j != 4) {
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
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        ISet excluded = SetFactory.makeConstantSet(new int[] {0, 4});
        DirectedGraphVar g2 = m.subgraphExcludeNodesView(g, excluded);
        DirectedGraph gVal = GraphFactory.makeDirectedGraph(
                n, SetType.BITSET, SetType.BITSET,
                new int[] {1, 2, 3, 5},
                new int[][] {
                        {1, 2}, {2, 3}, {3, 5}, {5, 1}
                }
        );
        try {
            g2.instantiateTo(gVal, new ICause() {});
            Assert.assertTrue(g2.isInstantiated());
            Assert.assertTrue(g2.getValue().equals(gVal));
            Assert.assertFalse(g.isInstantiated());
            Assert.assertEquals(g.getPotentialNodes().size(), g2.getValue().getNodes().size() + 2);
            Assert.assertEquals(g.getMandatorySuccessorsOf(1).size(), 1);
            Assert.assertEquals(g.getPotentialPredecessorOf(1).size(), 3);
            Assert.assertEquals(g.getMandatorySuccessorsOf(2).size(), 1);
            Assert.assertEquals(g.getPotentialPredecessorOf(2).size(), 3);
            Assert.assertEquals(g.getMandatorySuccessorsOf(3).size(), 1);
            Assert.assertEquals(g.getPotentialPredecessorOf(3).size(), 3);
            Assert.assertEquals(g.getMandatorySuccessorsOf(5).size(), 1);
            Assert.assertEquals(g.getPotentialPredecessorOf(5).size(), 3);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        SetVar nodesG = m.graphNodeSetView(g);
        m.member(0, nodesG).post();
        ISet excluded = SetFactory.makeConstantSet(new int[] {0, 4});
        DirectedGraphVar g2 = m.subgraphExcludeNodesView(g, excluded);
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.stronglyConnected(g2).post();
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            Assert.assertTrue(!g2.getValue().containsNode(0));
            Assert.assertTrue(g.getValue().containsNode(0));
            Assert.assertTrue(!g2.getValue().containsNode(4));
            Assert.assertTrue(g2.getValue().getNodes().size() >= 1 && g2.getValue().getNodes().size() <= 4);
            for (int i = 0; i < n; i++) {
                if (!excluded.contains(i)) {
                    if (g.getValue().containsNode(i)) {
                        Assert.assertTrue(g2.getValue().containsNode(i));
                        for (int j : g.getValue().getSuccessorsOf(i)) {
                            if (!excluded.contains(j)) {
                                Assert.assertTrue(g2.getValue().containsEdge(i, j));
                            }
                        }
                    }
                }
            }
        }
    }
}
