/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
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
 * Test suite for DirectedNodeInducedGraphVarImpl class
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class DirectedNodeInducedGraphVarImplTest {

    /**
     * Instantiate a DirectedNodeInducedGraphVar and test the basic methods.
     */
    @Test(groups="1s", timeOut=60000)
    public void basicTest() {
        Model m = new Model();
        int n = 3;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeStoredAllNodesDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UB.addEdge(0, 1);
        UB.addEdge(1, 2);
        UB.addEdge(2, 0);
        DirectedGraphVar g = m.nodeInducedDigraphVar("g", LB, UB);
        Assert.assertEquals(g.getMandatoryPredecessorsOf(0).size(), 0);
        Assert.assertEquals(g.getMandatoryPredecessorsOf(1).size(), 0);
        Assert.assertEquals(g.getMandatoryPredecessorsOf(2).size(), 0);
        Assert.assertEquals(g.getMandatorySuccessorsOf(0).size(), 0);
        Assert.assertEquals(g.getMandatorySuccessorsOf(1).size(), 0);
        Assert.assertEquals(g.getMandatorySuccessorsOf(2).size(), 0);
        Assert.assertEquals(g.getPotentialPredecessorOf(0).size(), 1);
        Assert.assertEquals(g.getPotentialPredecessorOf(1).size(), 1);
        Assert.assertEquals(g.getPotentialPredecessorOf(2).size(), 1);
        Assert.assertEquals(g.getPotentialSuccessorsOf(0).size(), 1);
        Assert.assertEquals(g.getPotentialSuccessorsOf(1).size(), 1);
        Assert.assertEquals(g.getPotentialSuccessorsOf(2).size(), 1);
        // Try to remove a mandatory edge
        ICause fakeCause = new ICause() {};
        LB.addNode(0);
        LB.addNode(1);
        LB.addEdge(0, 1);
        try {
            g.removeEdge(0, 1, fakeCause);
            Assert.fail();
        } catch (ContradictionException e) {
            // SUCCESS
        }
        // Try to remove and edge not in the domain
        UB.removeNode(2);
        try {
            Assert.assertFalse(g.removeEdge(0, 2, fakeCause));
        } catch (ContradictionException e) {
            Assert.fail();
        }
        // Try to enforce an edge not in UB
        try {
            g.enforceEdge(0, 0, fakeCause);
            Assert.fail();
        } catch (ContradictionException e) {
            // SUCCESS
        }
    }

    /**
     * Test the instantiation of a single node-induced directed graph variable with all combinations of node and arc
     * sets types. Enumerate of possible graph instantiations with the default strategy, assert that no value is missed.
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 10;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED") && !nodeSetType.name().contains("DYNAMIC")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED") && !arcSetType.name().contains("DYNAMIC")) {
                        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, nodeSetType, arcSetType);
                        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, nodeSetType, arcSetType, false);
                        DirectedGraphVar g = m.nodeInducedDigraphVar("g", LB, UB);
                        Assert.assertTrue(g.isDirected());
                        while (m.getSolver().solve()) ;
                        // There are exactly 2^n node-induced subgraphs of a graph with n nodes.
                        Assert.assertEquals(Math.pow(2, n), m.getSolver().getSolutionCount());
                    }
                }
            }
        }
    }

    /**
     * Same as previous but with two directed graph variables
     * (needed because current implementation of default search uses one GraphStrategy for one graph variable)
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerateTwo() {
        Model m = new Model();
        int n = 5;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED") && !nodeSetType.name().contains("DYNAMIC")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED") && !arcSetType.name().contains("DYNAMIC")) {
                        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(m, n, nodeSetType, arcSetType);
                        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(m, n, nodeSetType, arcSetType);
                        DirectedGraph UB1 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, nodeSetType, arcSetType, false);
                        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, nodeSetType, arcSetType, false);
                        DirectedGraphVar g1 = m.nodeInducedDigraphVar("g1", LB1, UB1);
                        DirectedGraphVar g2 = m.nodeInducedDigraphVar("g2", LB2, UB2);
                        while (m.getSolver().solve()) ;
                        Assert.assertEquals(Math.pow(2, n) * Math.pow(2, n), m.getSolver().getSolutionCount());
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testForbiddenInstantiation() {
        Model m = new Model();
        int n = 10;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.nodeInducedDigraphVar("g", LB, UB);
        DirectedGraph wrongValue = GraphFactory.makeDirectedGraph(
                n, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 1} }
        );
        try {
            g.instantiateTo(wrongValue, new ICause() {});
            Assert.fail();
        } catch (ContradictionException e) {
            // OK
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        int n = 3;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = m.nodeInducedDigraphVar("g", LB, UB);
        m.stronglyConnected(g).post();
        while (m.getSolver().solve());
        Assert.assertEquals(m.getSolver().getSolutionCount(), 7);
        // Test with general class digraph var
        m = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        g = m.digraphVar("g", LB, UB);
        m.stronglyConnected(g).post();
        while (m.getSolver().solve());
        Assert.assertEquals(m.getSolver().getSolutionCount(), 24);
    }

    @Test(groups="1s", timeOut=60000)
    public void testDirectedGraphVarInstantiated() {
        Model m = new Model();
        int n = 3;
        DirectedGraph LB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        DirectedGraphVar g = m.nodeInducedDigraphVar("g", LB, UB);
        Assert.assertTrue(g.isInstantiated());
        DirectedGraph gval = g.getValue();
        Assert.assertEquals(gval.getNodes().size(), 3);
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        DirectedGraphVar g2 = m.nodeInducedDigraphVar("g2", LB2, UB2);
        Assert.assertFalse(g2.isInstantiated());
    }

    /**
     * Ensure that the removeEdge operation does not filters possible solutions:
     *     if LB = empty graph, UB = 0 -> 1, and edge (0, 1) is forbidden by a constraint, then
     *     the possible solutions are 0, 1, and the empty graph.
     */
    @Test(groups="1s", timeOut=60000)
    public void testUseCase2() {
        Model m = new Model();
        int n = 2;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UB.addNode(0); UB.addNode(1);
        UB.addEdge(0, 1);
        DirectedGraphVar g = m.nodeInducedDigraphVar("g", LB, UB);
        SetVar s = m.graphSuccessorsSetView(g, 0);
        m.notMember(1, s).post();
        while (m.getSolver().solve());
        Assert.assertEquals(m.getSolver().getSolutionCount(), 3);
    }
}
