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
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.PairProcedure;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test class for subgraphInducedByNodesView
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestGraphUnionView {

    @Test(groups = "1s", timeOut = 60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 4;
        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g1 = m.graphVar("g1", LB1, UB1);
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = m.graphVar("g2", LB2, UB2);
        //
        UndirectedGraphVar g3 = m.graphUnionView(g1, g2);
        while (m.getSolver().solve()) {
            for (int i = 0; i < n; i++) {
                boolean b = g3.getValue().containsNode(i);
                Assert.assertEquals(g1.getValue().containsNode(i) || g2.getValue().containsNode(i), b);
                if (b) {
                    for (int j = 0; j < n; j++) {
                        Assert.assertEquals(g1.getValue().containsEdge(i, j) || g2.getValue().containsEdge(i, j), g3.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g1 = m.graphVar("g1", LB1, UB1);
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = m.graphVar("g2", LB2, UB2);
        //
        UndirectedGraphVar g3 = m.graphUnionView(g1, g2);
        SetVar nodesG = m.graphNodeSetView(g3);
        m.member(3, nodesG).post();
        m.notMember(2, nodesG).post();
        SetVar neigh3 = m.graphNeighborsSetView(g3, 3);
        m.member(1, neigh3).post();
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.connected(g2).post();
        m.getSolver().setSearch(Search.graphVarSearch(g1, g2, g3));
        m.getSolver().solve();
        while (m.getSolver().solve()) {
            for (int i = 0; i < n; i++) {
                boolean b = g3.getValue().containsNode(i);
                Assert.assertEquals(g1.getValue().containsNode(i) || g2.getValue().containsNode(i), b);
                if (b) {
                    for (int j = 0; j < n; j++) {
                        Assert.assertEquals(g1.getValue().containsEdge(i, j) || g2.getValue().containsEdge(i, j), g3.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testInstantiateAndGenerateDirected() {
        Model m = new Model();
        int n = 3;
        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB1 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g1 = m.digraphVar("g1", LB1, UB1);
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = m.digraphVar("g2", LB2, UB2);
        //
        DirectedGraphVar g3 = m.graphUnionView(g1, g2);
        while (m.getSolver().solve()) {
            for (int i = 0; i < n; i++) {
                boolean b = g3.getValue().containsNode(i);
                Assert.assertEquals(g1.getValue().containsNode(i) || g2.getValue().containsNode(i), b);
                if (b) {
                    for (int j = 0; j < n; j++) {
                        Assert.assertEquals(g1.getValue().containsEdge(i, j) || g2.getValue().containsEdge(i, j), g3.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrainedDirected() {
        Model m = new Model();
        int n = 4;
        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB1 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g1 = m.digraphVar("g1", LB1, UB1);
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = m.digraphVar("g2", LB2, UB2);
        //
        DirectedGraphVar g3 = m.graphUnionView(g1, g2);
        SetVar nodesG = m.graphNodeSetView(g3);
        m.member(3, nodesG).post();
        m.notMember(2, nodesG).post();
        SetVar neigh3 = m.graphPredecessorsSetView(g3, 3);
        m.member(1, neigh3).post();
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.stronglyConnected(g2).post();
        m.getSolver().setSearch(Search.graphVarSearch(g1, g2, g3));
        m.getSolver().solve();
        while (m.getSolver().solve()) {
            for (int i = 0; i < n; i++) {
                boolean b = g3.getValue().containsNode(i);
                Assert.assertEquals(g1.getValue().containsNode(i) || g2.getValue().containsNode(i), b);
                if (b) {
                    for (int j = 0; j < n; j++) {
                        Assert.assertEquals(g1.getValue().containsEdge(i, j) || g2.getValue().containsEdge(i, j), g3.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testDeltaUndirected() throws ContradictionException {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g1 = m.graphVar("g1", LB1, UB1);
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = m.graphVar("g2", LB2, UB2);
        UndirectedGraphVar g3 = m.graphUnionView(g1, g2);
        ICause fakeCauseA = new ICause() {};
        ICause fakeCauseB = new ICause() {};
        IGraphDeltaMonitor monitor = g3.monitorDelta(fakeCauseA);
        monitor.startMonitoring();
        ISet delta = SetFactory.makeBitSet(0);
        IntProcedure nodeProc = delta::add;
        // Test add nodes
        g1.enforceNode(0, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertTrue(delta.size() == 1);
        Assert.assertTrue(delta.contains(0));
        delta.clear();
        g2.enforceNode(0, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertTrue(delta.size() == 0);
        // Test remove node
        g1.removeNode(2, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertTrue(delta.size() == 0);
        g2.removeNode(2, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertTrue(delta.size() == 1);
        Assert.assertTrue(delta.contains(2));
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
        g1.enforceEdge(1, 3, fakeCauseB);
        g2.enforceEdge(1, 4, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertTrue(delta.size() == 2);
        Assert.assertTrue(delta.contains(3));
        Assert.assertTrue(delta.contains(4));
        delta.clear();
        g1.enforceEdge(1, 4, fakeCauseB);
        g2.enforceEdge(1, 3, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertTrue(delta.size() == 0);
        // Test remove edges
        g1.removeEdge(0, 1, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertTrue(delta.size() == 0);
        g2.removeEdge(1, 0, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertTrue(delta.size() == 1);
        Assert.assertTrue(delta.contains(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testDeltaDirected() throws ContradictionException {
        Model m = new Model();
        int n = 5;
        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB1 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g1 = m.digraphVar("g1", LB1, UB1);
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = m.digraphVar("g2", LB2, UB2);
        DirectedGraphVar g3 = m.graphUnionView(g1, g2);
        ICause fakeCauseA = new ICause() {};
        ICause fakeCauseB = new ICause() {};
        IGraphDeltaMonitor monitor = g3.monitorDelta(fakeCauseA);
        monitor.startMonitoring();
        ISet delta = SetFactory.makeBitSet(0);
        IntProcedure nodeProc = delta::add;
        // Test add nodes
        g1.enforceNode(0, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertTrue(delta.size() == 1);
        Assert.assertTrue(delta.contains(0));
        delta.clear();
        g2.enforceNode(0, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertTrue(delta.size() == 0);
        // Test remove node
        g1.removeNode(2, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertTrue(delta.size() == 0);
        g2.removeNode(2, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertTrue(delta.size() == 1);
        Assert.assertTrue(delta.contains(2));
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
        g1.enforceEdge(1, 3, fakeCauseB);
        g2.enforceEdge(1, 4, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertTrue(delta.size() == 2);
        Assert.assertTrue(delta.contains(3));
        Assert.assertTrue(delta.contains(4));
        delta.clear();
        g1.enforceEdge(1, 4, fakeCauseB);
        g2.enforceEdge(1, 3, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertTrue(delta.size() == 0);
        // Test remove edges
        g1.removeEdge(0, 1, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertTrue(delta.size() == 0);
        g2.removeEdge(1, 0, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertTrue(delta.size() == 0);
        g1.removeEdge(1, 0, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertTrue(delta.size() == 1);
        Assert.assertTrue(delta.contains(0));
    }
}
