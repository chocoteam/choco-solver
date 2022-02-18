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
 * Test class for DirectedSubgraphExcludedNodesView
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestDirectedNodeExcludedSubgraphView {

    @Test(groups = "1s", timeOut = 60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        DirectedGraphVar g2 = m.nodeInducedSubgraphView(g, SetFactory.makeConstantSet(new int[]{0, 4}), true);
        Assert.assertEquals(g2.getMandatoryNodes().size(), 0);
        Assert.assertEquals(g2.getPotentialNodes().size(), 3);
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
        DirectedGraphVar g2 = m.nodeInducedSubgraphView(g, excluded, true);
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
        DirectedGraphVar g2 = m.nodeInducedSubgraphView(g, excluded, true);
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.stronglyConnected(g2).post();
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

    @Test(groups="1s", timeOut=60000)
    public void testDelta() throws ContradictionException {
        Model m = new Model();
        int n = 10;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        ISet nodes = SetFactory.makeConstantSet(new int[] {0, 1, 2});
        DirectedGraphVar g2 = m.nodeInducedSubgraphView(g, nodes, true);
        ICause fakeCauseA = new ICause() {};
        ICause fakeCauseB = new ICause() {};
        IGraphDeltaMonitor monitor = g2.monitorDelta(fakeCauseA);
        monitor.startMonitoring();
        ISet delta = SetFactory.makeBitSet(0);
        IntProcedure nodeProc = delta::add;
        // Test add nodes
        g.enforceNode(0, fakeCauseB);
        g.enforceNode(1, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertEquals(delta.size(), 0);
        g.enforceNode(4, fakeCauseB);
        g.enforceNode(6, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertEquals(delta.size(), 2);
        Assert.assertTrue(delta.contains(4));
        Assert.assertTrue(delta.contains(6));
        delta.clear();
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertEquals(delta.size(), 0);
        // Test remove node
        g.removeNode(2, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertEquals(delta.size(), 0);
        g.removeNode(7, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertTrue(delta.contains(7));
        Assert.assertEquals(delta.size(), 1);
        delta.clear();
        // Test add edges
        // First clear monitor from node operations that can cause edge operations
        monitor.forEachEdge((i, j) -> {}, GraphEventType.ADD_EDGE);
        monitor.forEachEdge((i, j) -> {}, GraphEventType.REMOVE_EDGE);
        PairProcedure edgeProc = (i, j) -> {
            if (i == 4) {
                delta.add(j);
            } else if (j == 4) {
                delta.add(i);
            }
        };
        g.enforceEdge(0, 4, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertEquals(delta.size(), 0);
        g.enforceEdge(4, 3, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertEquals(delta.size(), 1);
        Assert.assertTrue(delta.contains(3));
        delta.clear();
        // Test remove edges
        g.removeEdge(4, 1, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertEquals(delta.size(), 0);
        g.removeEdge(5, 4, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertEquals(delta.size(), 1);
        Assert.assertTrue(delta.contains(5));
    }
}
