/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.inclusion;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the subgraph graph constraint.
 * @author Dimitri Justeau-Allaire
 * @since 22/03/2021
 */
public class SubgraphTest {

    SetType setType = SetType.BITSET;

    // Test for undirected graph variables

    @Test(groups="1s", timeOut=60000)
    public void undirectedInstantiatedSuccessTest() {
        Model model = new Model();
        int n1 = 10;
        int n2 = 5;
        int[] nodes1 = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[][] edges1 = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {8, 1}, {7, 6}, {6, 2}
        };
        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        UndirectedGraph UB1 = GraphFactory.makeStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        UndirectedGraphVar g1 = model.graphVar("g1", LB1, UB1);
        int[] nodes2 = new int[] {0, 1, 2, 3, 4};
        int[][] edges2 = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}
        };
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, nodes2, edges2);
        UndirectedGraph UB2 = GraphFactory.makeStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, nodes2, edges2);
        UndirectedGraphVar g2 = model.graphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void undirectedInstantiatedFailTest() {
        Model model = new Model();
        int n1 = 10;
        int n2 = 5;
        int[] nodes1 = new int[] {0, 2, 3, 4, 5, 6, 7, 8};
        int[][] edges1 = new int[][] {
                {0, 2}, {2, 3}, {3, 4}, {4, 5}, {8, 2}, {7, 6}, {6, 2}
        };
        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        UndirectedGraph UB1 = GraphFactory.makeStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        UndirectedGraphVar g1 = model.graphVar("g1", LB1, UB1);
        int[] nodes2 = new int[] {0, 1, 2, 3, 4};
        int[][] edges2 = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}
        };
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, nodes2, edges2);
        UndirectedGraph UB2 = GraphFactory.makeStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, nodes2, edges2);
        UndirectedGraphVar g2 = model.graphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void undirectedConstrainedSuccessTest() {
        Model model = new Model();
        int n1 = 6;
        int n2 = 4;
        int[] nodes1 = new int[] {0, 2, 3, 4};
        int[][] edges1 = new int[][] {
                {0, 2}, {2, 3}
        };
        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g1 = model.graphVar("g1", LB1, UB1);
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model.graphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        model.connected(g2).post();
        model.connected(g1).post();
        model.nbEdges(g1, model.intVar(0, 10)).post();
        model.nbEdges(g2, model.intVar(0, 6)).post();
        while (model.getSolver().solve()) {}
        Assert.assertTrue(model.getSolver().getSolutionCount() > 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void undirectedConstrainedFailTest() {
        Model model = new Model();
        int n1 = 6;
        int n2 = 4;
        int[] nodes1 = new int[] {0, 2, 3, 4};
        int[][] edges1 = new int[][] {
                {0, 2}, {2, 3}
        };
        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g1 = model.graphVar("g1", LB1, UB1);
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model.graphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        model.connected(g2).post();
        model.connected(g1).post();
        model.nbEdges(g1, model.intVar(0, 4)).post();
        model.nbEdges(g2, model.intVar(5, 10)).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void undirectedGenerateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n1 = 6;
        int n2 = 4;
        int[] nodes1 = new int[] {0, 2, 3, 4};
        int[][] edges1 = new int[][] {
                {0, 2}, {2, 3}, {4, 3}, {0, 3}
        };
        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredUndirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g1 = model.graphVar("g1", LB1, UB1);
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model.graphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        UndirectedGraph LB1_2 = GraphFactory.makeStoredUndirectedGraph(model2, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        UndirectedGraph UB1_2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n1, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g1_2 = model2.graphVar("g1", LB1_2, UB1_2);
        UndirectedGraph LB2_2 = GraphFactory.makeStoredUndirectedGraph(model2, n2, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2_2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n2, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2_2 = model2.graphVar("g2", LB2_2, UB2_2);
        Constraint cons = model2.subgraph(g2_2, g1_2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }

    // Test for directed graph variables

    @Test(groups="1s", timeOut=60000)
    public void directedInstantiatedSuccessTest() {
        Model model = new Model();
        int n1 = 10;
        int n2 = 5;
        int[] nodes1 = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[][] edges1 = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {8, 1}, {7, 6}, {6, 2}
        };
        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        DirectedGraph UB1 = GraphFactory.makeStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        DirectedGraphVar g1 = model.digraphVar("g1", LB1, UB1);
        int[] nodes2 = new int[] {0, 1, 2, 3, 4};
        int[][] edges2 = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}
        };
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, nodes2, edges2);
        DirectedGraph UB2 = GraphFactory.makeStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, nodes2, edges2);
        DirectedGraphVar g2 = model.digraphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void directedInstantiatedFailTest() {
        Model model = new Model();
        int n1 = 10;
        int n2 = 5;
        int[] nodes1 = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[][] edges1 = new int[][] {
                {0, 1}, {2, 1}, {2, 3}, {3, 4}, {4, 5}, {8, 1}, {7, 6}, {6, 2}
        };
        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        DirectedGraph UB1 = GraphFactory.makeStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        DirectedGraphVar g1 = model.digraphVar("g1", LB1, UB1);
        int[] nodes2 = new int[] {0, 1, 2, 3, 4};
        int[][] edges2 = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}
        };
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, nodes2, edges2);
        DirectedGraph UB2 = GraphFactory.makeStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, nodes2, edges2);
        DirectedGraphVar g2 = model.digraphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void directedConstrainedSuccessTest() {
        Model model = new Model();
        int n1 = 6;
        int n2 = 4;
        int[] nodes1 = new int[] {0, 2, 3, 4};
        int[][] edges1 = new int[][] {
                {0, 2}, {2, 3}, {2, 4}, {2, 0}, {4, 4}
        };
        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        DirectedGraph UB1 = GraphFactory.makeCompleteStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, false);
        UB1.addEdge(4, 4);
        DirectedGraphVar g1 = model.digraphVar("g1", LB1, UB1);
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = model.digraphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        model.directedTree(g2, 0).post();
        model.stronglyConnected(g1).post();
        model.nbEdges(g1, model.intVar(0, 10)).post();
        model.nbEdges(g2, model.intVar(0, 6)).post();
        while (model.getSolver().solve()) {}
        Assert.assertTrue(model.getSolver().getSolutionCount() > 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void directedConstrainedFailTest() {
        Model model = new Model();
        int n1 = 6;
        int n2 = 4;
        int[] nodes1 = new int[] {0, 2, 3, 4};
        int[][] edges1 = new int[][] {
                {0, 2}, {2, 3}, {2, 4}, {2, 0}, {4, 4}
        };
        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        DirectedGraph UB1 = GraphFactory.makeCompleteStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, false);
        UB1.addEdge(4, 4);
        DirectedGraphVar g1 = model.digraphVar("g1", LB1, UB1);
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = model.digraphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        model.directedTree(g1, 0).post();
        model.stronglyConnected(g2).post();
        model.nbEdges(g1, model.intVar(2, 10)).post();
        model.nbEdges(g2, model.intVar(2, 6)).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void directedGenerateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n1 = 5;
        int n2 = 3;
        int[] nodes1 = new int[] {0, 2, 3, 4};
        int[][] edges1 = new int[][] {
                {0, 2}, {2, 3}, {2, 4}, {2, 0}, {0, 4}
        };
        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        DirectedGraph UB1 = GraphFactory.makeCompleteStoredDirectedGraph(model, n1, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g1 = model.digraphVar("g1", LB1, UB1);
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(model, n2, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = model.digraphVar("g2", LB2, UB2);
        model.subgraph(g2, g1).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        DirectedGraph LB1_2 = GraphFactory.makeStoredDirectedGraph(model2, n1, SetType.BITSET, SetType.BITSET, nodes1, edges1);
        DirectedGraph UB1_2 = GraphFactory.makeCompleteStoredDirectedGraph(model2, n1, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g1_2 = model2.digraphVar("g1", LB1_2, UB1_2);
        DirectedGraph LB2_2 = GraphFactory.makeStoredDirectedGraph(model2, n2, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2_2 = GraphFactory.makeCompleteStoredDirectedGraph(model2, n2, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2_2 = model2.digraphVar("g2", LB2_2, UB2_2);
        Constraint cons = model2.subgraph(g2_2, g1_2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }
}
