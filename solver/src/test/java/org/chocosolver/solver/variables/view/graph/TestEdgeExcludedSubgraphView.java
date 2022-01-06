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
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
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
 * Test class for edge-induced subgraph view when edges are excluded
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestEdgeExcludedSubgraphView {

    @Test(groups = "1s", timeOut = 60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1}, {1, 2}, {1, 3}, {1, 5},
                {2, 5}
        };
        UndirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, true);
        ISet[] neigh = UndirectedGraph.edgesArrayToEdgesSets(g.getNbMaxNodes(), edges);
        Assert.assertEquals(g2.getMandatoryNodes().size(), 0);
        Assert.assertEquals(g2.getPotentialNodes().size(), 6);
        while (m.getSolver().solve()) {
            for (int i = 0; i < g.getNbMaxNodes(); i++) {
                for (int j = 0; j < g.getNbMaxNodes(); j++) {
                    if (g.getValue().containsEdge(i, j)) {
                        Assert.assertEquals(!neigh[i].contains(j), g2.getValue().containsEdge(i, j));
                    }
                }
            }
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
        UndirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, true);
        ISet[] neigh = UndirectedGraph.edgesArrayToEdgesSets(g.getNbMaxNodes(), edges);
        SetVar nodesG = m.graphNodeSetView(g);
        m.member(3, nodesG).post();
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.connected(g2).post();
        while (m.getSolver().solve()) {
            for (int i = 0; i < g.getNbMaxNodes(); i++) {
                for (int j = 0; j < g.getNbMaxNodes(); j++) {
                    if (g.getValue().containsEdge(i, j)) {
                        Assert.assertEquals(!neigh[i].contains(j), g2.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testDelta() throws ContradictionException {
        Model m = new Model();
        int n = 10;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
//      Exclude following edges:
//                {0, 1}, {0, 4},
//                {1, 4}, {1, 2}, {1, 3}, {1, 5},
//                {2, 5}
        int[][] edges = new int[(n * n - n) / 2 - 7][];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!(i == 0 && (j == 1 || j == 4))
                        && !(i == 1 &&(j == 4 || j == 2 || j == 3 || j == 5))
                        && !(i == 2 && j ==5)) {
                    edges[idx] = new int[] {i, j};
                    idx++;
                }
            }
        }
        UndirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, true);
        ICause fakeCauseA = new ICause() {};
        ICause fakeCauseB = new ICause() {};
        IGraphDeltaMonitor monitor = g2.monitorDelta(fakeCauseA);
        monitor.startMonitoring();
        ISet delta = SetFactory.makeBitSet(0);
        IntProcedure nodeProc = delta::add;
        // Test add nodes
        g.enforceEdge(8, 9, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertEquals(delta.size(), 0);
        g.enforceEdge(4, 1, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertEquals(delta.size(), 2);
        Assert.assertTrue(delta.contains(4));
        Assert.assertTrue(delta.contains(1));
        delta.clear();
        monitor.forEachNode(nodeProc, GraphEventType.ADD_NODE);
        Assert.assertEquals(delta.size(), 0);
        // Test remove node
        g.removeNode(7, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertEquals(delta.size(), 0);
        g.removeNode(3, fakeCauseB);
        monitor.forEachNode(nodeProc, GraphEventType.REMOVE_NODE);
        Assert.assertTrue(delta.contains(3));
        Assert.assertEquals(delta.size(), 1);
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
        Assert.assertEquals(delta.size(), 0);
        g.enforceEdge(1, 5, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.ADD_EDGE);
        Assert.assertEquals(delta.size(), 1);
        Assert.assertTrue(delta.contains(5));
        delta.clear();
        // Test remove edges
        g.removeEdge(8, 1, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertEquals(delta.size(), 0);
        g.removeEdge(1, 2, fakeCauseB);
        monitor.forEachEdge(edgeProc, GraphEventType.REMOVE_EDGE);
        Assert.assertEquals(delta.size(), 1);
        Assert.assertTrue(delta.contains(2));
    }
}
