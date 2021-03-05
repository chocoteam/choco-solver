/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.graph.PropNbNodes;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test suite for GraphNodeSetView class
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class GraphNodeSetViewTest {

    /**
     * Test the instantiation of a graph node set view over an undirected graph variable
     * Generate all possible solutions and ensure that the view is properly updated.
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerateUndirectedGraph() {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        GraphVar g = m.undirectedGraphVar("g", LB, UB);
        GraphNodeSetView s = new GraphNodeSetView("s", g);
        while (m.getSolver().solve()) {
            int[] nodes = g.getValue().getNodes().toArray();
            int[] nodeSet = s.getValue().toArray();
            Arrays.sort(nodes);
            Arrays.sort(nodeSet);
            Assert.assertEquals(nodes, nodeSet);
        }
    }

    /**
     * Test the instantiation of a graph node set view over an directed graph variable
     * Generate all possible solutions and ensure that the view is properly updated.
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerateDirectedGraph() {
        Model m = new Model();
        int n = 4;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        GraphVar g = m.directedGraphVar("g", LB, UB);
        GraphNodeSetView s = new GraphNodeSetView("s", g);
        while (m.getSolver().solve()) {
            int[] nodes = g.getValue().getNodes().toArray();
            int[] nodeSet = s.getValue().toArray();
            Arrays.sort(nodes);
            Arrays.sort(nodeSet);
            Assert.assertEquals(nodes, nodeSet);
        }
    }

    /**
     * Post constraints on the view and ensure that the observed graph is properly affected.
     */
    @Test(groups="1s", timeOut=60000)
    public void testConstrainedViewUndirectedGraph() {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        GraphVar g = m.undirectedGraphVar("g", LB, UB);
        GraphNodeSetView s = new GraphNodeSetView("s", g);
        m.allEqual(s, m.setVar(new int[] {0, 2, 4})).post();
        while (m.getSolver().solve()) {
            int[] nodes = g.getValue().getNodes().toArray();
            int[] nodeSet = s.getValue().toArray();
            Arrays.sort(nodes);
            Arrays.sort(nodeSet);
            Assert.assertEquals(nodes, nodeSet);
            Assert.assertEquals(nodes, new int[] {0, 2, 4});
        }
    }

    /**
     * Post a constraint on the view to force it to a particular value and ensure that the observed
     * graph is properly affected.
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateTo() throws ContradictionException {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        GraphVar g = m.undirectedGraphVar("g", LB, UB);
        GraphNodeSetView s = new GraphNodeSetView("s", g);
        s.instantiateTo(new int[] {0, 2, 4}, s);
        while (m.getSolver().solve()) {
            int[] nodes = g.getValue().getNodes().toArray();
            int[] nodeSet = s.getValue().toArray();
            Arrays.sort(nodes);
            Arrays.sort(nodeSet);
            Assert.assertEquals(nodes, nodeSet);
            Assert.assertEquals(nodes, new int[] {0, 2, 4});
        }
    }

    /**
     * Same as previous with a directed graph var.
     */
    @Test(groups="1s", timeOut=60000)
    public void testConstrainedViewDirectedGraph() {
        Model m = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        GraphVar g = m.directedGraphVar("g", LB, UB);
        GraphNodeSetView s = new GraphNodeSetView("s", g);
        m.allEqual(s, m.setVar(new int[] {0, 2, 4})).post();
        while (m.getSolver().solve()) {
            int[] nodes = g.getValue().getNodes().toArray();
            int[] nodeSet = s.getValue().toArray();
            Arrays.sort(nodes);
            Arrays.sort(nodeSet);
            Assert.assertEquals(nodes, nodeSet);
            Assert.assertEquals(nodes, new int[] {0, 2, 4});
        }
    }

    /**
     * Post constraints on the graph and the view and ensure that the propagation is effective.
     */
    @Test(groups="1s", timeOut=60000)
    public void testConstrainedGraphAndView() {
        Model m = new Model();
        int n = 10;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        GraphVar g = m.undirectedGraphVar("g", LB, UB);
        SetVar s = m.graphNodeSetView(g);
        Constraint nbNodes = new Constraint("NbNodes", new PropNbNodes(g, m.intVar(3, 7)));
        m.post(nbNodes);
        m.member(0, s).post();
        IntVar card = s.getCard();
        m.arithm(card, "<=", 4).post();
        while (m.getSolver().solve()) {
            Assert.assertTrue(g.getValue().getNodes().contains(0));
            Assert.assertTrue(card.getValue() >= 3 && card.getValue() <= 4);
            Assert.assertTrue(s.getValue().size() >= 3 && s.getValue().size() <= 4);
        }
    }
}
