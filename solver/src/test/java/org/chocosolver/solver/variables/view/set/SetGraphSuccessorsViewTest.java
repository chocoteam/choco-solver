/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.set;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.graph.basic.PropNbEdges;
import org.chocosolver.solver.constraints.graph.basic.PropNbNodes;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.procedure.IntProcedure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test suite for GraphNeighSetView class
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class SetGraphSuccessorsViewTest {

    /**
     * Test the instantiation of a graph neigh set view over an undirected graph variable
     * Generate all possible solutions and ensure that the view is properly updated.
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerateUndirectedGraph() {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        SetSuccessorsGraphView s = new SetSuccessorsGraphView("s", g, 0);
        Assert.assertEquals(s.getLB().size(), 0);
        Assert.assertEquals(s.getUB().size(), 4);
        while (m.getSolver().solve()) {
            int[] neighsInGraph = g.getValue().getNeighborsOf(0).toArray();
            int[] neighSet = s.getValue().toArray();
            Arrays.sort(neighsInGraph);
            Arrays.sort(neighSet);
            Assert.assertEquals(neighsInGraph, neighSet);
        }
    }

    /**
     * Post a constraint on the view to force it to a particular value and ensure that the observed
     * graph is properly affected.
     */
    @Test(groups="1s", timeOut=60000)
    public void testFixedViewUndirectedGraph() {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        SetSuccessorsGraphView s = new SetSuccessorsGraphView("s", g, 0);
        m.allEqual(s, m.setVar(1, 2, 4)).post();
        while (m.getSolver().solve()) {
            int[] neighsInGraph = g.getValue().getNeighborsOf(0).toArray();
            int[] neighSet = s.getValue().toArray();
            Arrays.sort(neighsInGraph);
            Arrays.sort(neighSet);
            Assert.assertEquals(neighsInGraph, neighSet);
            Assert.assertEquals(neighsInGraph, new int[] {1, 2, 4});
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
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        SetSuccessorsGraphView s = new SetSuccessorsGraphView("s", g, 0);
        s.instantiateTo(new int[] {2, 3}, s);
        while (m.getSolver().solve()) {
            int[] neighsInGraph = g.getValue().getNeighborsOf(0).toArray();
            int[] neighSet = s.getValue().toArray();
            Arrays.sort(neighsInGraph);
            Arrays.sort(neighSet);
            Assert.assertEquals(neighsInGraph, neighSet);
            Assert.assertEquals(neighsInGraph, new int[] {2, 3});
        }
    }

    /**
     * Post contradictory constraints on the view and on the variable and ensure failure.
     */
    @Test(groups="1s", timeOut=60000)
    public void testFail() throws ContradictionException {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeStoredAllNodesUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UB.addEdge(0, 1);
        UB.addEdge(0, 2);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        SetVar s = m.graphNeighborsSetView(g, 0);
        m.allEqual(s, m.setVar(1, 2, 3)).post();
        Assert.assertFalse(m.getSolver().solve());
    }

    /**
     * Post constraints on the graph and the view and ensure that the propagation is effective.
     */
    @Test(groups="1s", timeOut=60000)
    public void testConstrainedGraphAndView() {
        Model m = new Model();
        int n = 10;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeStoredAllNodesDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        for (int i = 1; i < n; i ++) {
            UB.addEdge(0, i);
        }
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        SetVar s = m.graphSuccessorsSetView(g, 0);
        Constraint nbEdges = new Constraint("NbEdges", new PropNbEdges(g, m.intVar(3, 7)));
        m.post(nbEdges);
        m.member(2, s).post();
        IntVar card = s.getCard();
        m.arithm(card, "<=", 4).post();
        Constraint nbNodes = new Constraint("NbNodes", new PropNbNodes(g, m.intOffsetView(card, 1)));
        m.post(nbNodes);
        while (m.getSolver().solve()) {
            Assert.assertTrue(g.getValue().getNodes().contains(0));
            Assert.assertTrue(g.getValue().getNodes().contains(2));
            Assert.assertTrue(g.getValue().getSuccessorsOf(0).contains(2));
            Assert.assertTrue(card.getValue() >= 3 && card.getValue() <= 4);
            Assert.assertTrue(s.getValue().size() >= 3 && s.getValue().size() <= 4);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testDelta() throws ContradictionException {
        Model m = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.digraphVar("g", LB, UB);
        SetVar setView = m.graphSuccessorsSetView(g, 0);
        ICause fakeCauseA = new ICause() {};
        ICause fakeCauseB = new ICause() {};
        ISetDeltaMonitor monitor = setView.monitorDelta(fakeCauseA);
        monitor.startMonitoring();
        ISet delta = SetFactory.makeBitSet(0);
        IntProcedure addToDelta = delta::add;
        // Test add elements
        g.enforceEdge(1, 0, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.ADD_TO_KER);
        Assert.assertEquals(delta.size(), 0);
        delta.clear();
        g.enforceEdge(0, 2, fakeCauseB);
        g.enforceEdge(0, 3, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.ADD_TO_KER);
        Assert.assertEquals(delta.size(), 2);
        Assert.assertTrue(delta.contains(2));
        Assert.assertTrue(delta.contains(3));
        // Test remove elements
        delta.clear();
        g.removeEdge(0, 1, fakeCauseB);
        g.removeEdge(0, 4, fakeCauseB);
        monitor.forEach(addToDelta, SetEventType.REMOVE_FROM_ENVELOPE);
        Assert.assertTrue(delta.contains(1));
        Assert.assertTrue(delta.contains(4));
        Assert.assertEquals(delta.size(), 2);
        delta.clear();
        monitor.forEach(addToDelta, SetEventType.REMOVE_FROM_ENVELOPE);
        Assert.assertEquals(delta.size(), 0);
    }
}
