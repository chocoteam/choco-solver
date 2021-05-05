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
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test class for directed edge-induced subgraph view
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestDirectedEdgeInducedSubgraphView {

    @Test(groups = "10s", timeOut = 60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1},
                {1, 2}, {1, 3},
        };
        DirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, false);
        ISet[] pred = DirectedGraph.edgesArrayToPredecessorsSets(g.getNbMaxNodes(), edges);
        ISet[] succ = DirectedGraph.edgesArrayToSuccessorsSets(g.getNbMaxNodes(), edges);
        Assert.assertEquals(g2.getMandatoryNodes().size(), 0);
        Assert.assertEquals(g2.getPotentialNodes().size(), 4);
        while (m.getSolver().solve()) {
            Assert.assertTrue(!g2.getValue().containsNode(4));
            for (int i : g.getValue().getNodes()) {
                for (int j : g.getValue().getPredecessorsOf(i)) {
                    if (pred[i].contains(j)) {
                        Assert.assertTrue(g2.getValue().containsNode(i));
                        Assert.assertTrue(g2.getValue().containsNode(j));
                    }
                }
                for (int j : g.getValue().getSuccessorsOf(i)) {
                    if (succ[i].contains(j)) {
                        Assert.assertTrue(g2.getValue().containsNode(i));
                        Assert.assertTrue(g2.getValue().containsNode(j));
                    }
                }
                if (g2.getValue().containsNode(i)) {
                    Assert.assertTrue(g2.getValue().getPredecessorsOf(i).size() <= pred[i].size());
                    Assert.assertTrue(g2.getValue().getSuccessorsOf(i).size() <= succ[i].size());
                    for (int j : g2.getValue().getPredecessorsOf(i)) {
                        Assert.assertTrue(g.getValue().containsEdge(j, i));
                    }
                    for (int j : g2.getValue().getSuccessorsOf(i)) {
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
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1},
                {1, 2}, {1, 3}, {1, 5},
                {2, 5}
        };
        DirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, false);
        DirectedGraph gVal = GraphFactory.makeDirectedGraph(
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
            Assert.assertEquals(g.getPotentialNodes().size(), 6);
            Assert.assertEquals(g.getMandatoryPredecessorsOf(0).size(), 0);
            Assert.assertEquals(g.getMandatorySuccessorsOf(0).size(), 1);
            Assert.assertEquals(g.getPotentialSuccessorsOf(0).size(), 5);
            Assert.assertEquals(g.getMandatorySuccessorsOf(1).size(), 1);
            Assert.assertEquals(g.getPotentialSuccessorsOf(1).size(), 3);
            Assert.assertEquals(g.getMandatoryPredecessorsOf(3).size(), 1);
            Assert.assertEquals(g.getPotentialSuccessorsOf(3).size(), 5);
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1},
                {1, 2}, {1, 3},
        };
        DirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, false);
        ISet[] pred = DirectedGraph.edgesArrayToPredecessorsSets(g.getNbMaxNodes(), edges);
        ISet[] succ = DirectedGraph.edgesArrayToSuccessorsSets(g.getNbMaxNodes(), edges);
        SetVar nodesG = m.graphNodeSetView(g);
        m.member(3, nodesG).post();
        m.nbNodes(g2, m.intVar(1, 4)).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(g.getValue().containsNode(3));
            for (int i : g.getValue().getNodes()) {
                for (int j : g.getValue().getPredecessorsOf(i)) {
                    if (pred[i].contains(j)) {
                        Assert.assertTrue(g2.getValue().containsNode(i));
                        Assert.assertTrue(g2.getValue().containsNode(j));
                    }
                }
                for (int j : g.getValue().getSuccessorsOf(i)) {
                    if (succ[i].contains(j)) {
                        Assert.assertTrue(g2.getValue().containsNode(i));
                        Assert.assertTrue(g2.getValue().containsNode(j));
                    }
                }
                if (g2.getValue().containsNode(i)) {
                    Assert.assertTrue(g2.getValue().getPredecessorsOf(i).size() <= pred[i].size());
                    Assert.assertTrue(g2.getValue().getSuccessorsOf(i).size() <= succ[i].size());
                    for (int j : g2.getValue().getPredecessorsOf(i)) {
                        Assert.assertTrue(g.getValue().containsEdge(j, i));
                    }
                    for (int j : g2.getValue().getSuccessorsOf(i)) {
                        Assert.assertTrue(g.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }
}
