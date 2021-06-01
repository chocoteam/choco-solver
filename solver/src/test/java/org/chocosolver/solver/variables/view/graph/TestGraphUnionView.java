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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
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
}
