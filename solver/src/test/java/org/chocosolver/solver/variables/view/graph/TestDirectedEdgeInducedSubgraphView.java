/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;
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

    @Test(groups="1s", timeOut=60000)
    public void testDelta() throws ContradictionException {
        Model m = new Model();
        int n = 10;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1}, {4, 0}, {4, 1},
                {1, 2}, {1, 3}, {1, 5},
                {2, 5}
        };
        DirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, false);
        ICause fakeCauseA = new ICause() {};
        ICause fakeCauseB = new ICause() {};
        IGraphDeltaMonitor monitor = g2.monitorDelta(fakeCauseA);
        monitor.startMonitoring();
        ISet delta = SetFactory.makeBitSet(0);
        IntProcedure nodeProc = i -> delta.add(i);
        // Test add nodes
        g.enforceEdge(8, 9, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertTrue(delta.size() == 0);
        g.enforceEdge(4, 1, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertTrue(delta.size() == 2);
        Assert.assertTrue(delta.contains(4));
        Assert.assertTrue(delta.contains(1));
        delta.clear();
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertTrue(delta.size() == 0);
        // Test remove node
        g.removeNode(7, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertTrue(delta.size() == 0);
        g.removeNode(3, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertTrue(delta.contains(3));
        Assert.assertTrue(delta.size() == 1);
        delta.clear();
        // Test add edges
        // First clear monitor from node operations that can cause edge operations
        monitor.forEachEdge((i, j) -> {}, GraphEventType.ADD_EDGE);
        monitor.forEachEdge((i, j) -> {}, GraphEventType.REMOVE_EDGE);
        PairProcedure edgeProc = (i, j) -> {
            if (i == 1) {
                delta.add(j);
            } else if (j == 1) {
                delta.add(i);
            }
        };
        g.enforceEdge(0, 9, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertTrue(delta.size() == 0);
        g.enforceEdge(1, 5, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertTrue(delta.size() == 1);
        Assert.assertTrue(delta.contains(5));
        delta.clear();
        // Test remove edges
        g.removeEdge(8, 1, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertTrue(delta.size() == 0);
        g.removeEdge(1, 2, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertTrue(delta.size() == 1);
        Assert.assertTrue(delta.contains(2));
    }
}
