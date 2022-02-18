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
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test suite for DirectedGraphVarImpl class
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class DirectedGraphVarImplTest {

    /**
     * Instantiate a DirectedGraphVar and test the basic methods.
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
        DirectedGraphVar g = new DirectedGraphVarImpl("g", m, LB, UB);
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
     * Test the instantiation of a single directed graph variable with all combinations of node and arc sets types.
     * Enumerate of possible graph instantiations with the default strategy and assert that no value has been missed.
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 3;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED") && !nodeSetType.name().contains("DYNAMIC")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED") && !arcSetType.name().contains("DYNAMIC")) {
                        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(m, n, nodeSetType, arcSetType);
                        DirectedGraph UB = GraphFactory.makeStoredAllNodesDirectedGraph(m, n, nodeSetType, arcSetType, false);
                        UB.addEdge(0, 1);
                        UB.addEdge(1, 2);
                        UB.addEdge(2, 0);
                        DirectedGraphVar g = new DirectedGraphVarImpl("g", m, LB, UB);
                        Assert.assertTrue(g.isDirected());
                        while (m.getSolver().solve()) ;
                        Assert.assertEquals(18, m.getSolver().getSolutionCount());
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
        int n = 3;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED") && !nodeSetType.name().contains("DYNAMIC")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED") && !arcSetType.name().contains("DYNAMIC")) {
                        DirectedGraph LB1 = GraphFactory.makeStoredDirectedGraph(m, n, nodeSetType, arcSetType);
                        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(m, n, nodeSetType, arcSetType);
                        DirectedGraph UB1 = GraphFactory.makeStoredAllNodesDirectedGraph(m, n, nodeSetType, arcSetType, false);
                        DirectedGraph UB2 = GraphFactory.makeStoredAllNodesDirectedGraph(m, n, nodeSetType, arcSetType, false);
                        UB1.addEdge(0, 1);
                        UB1.addEdge(1, 2);
                        UB1.addEdge(2, 0);
                        UB2.addEdge(0, 1);
                        UB2.addEdge(1, 2);
                        UB2.addEdge(2, 0);
                        DirectedGraphVar g1 = new DirectedGraphVarImpl("g1", m, LB1, UB1);
                        DirectedGraphVar g2 = new DirectedGraphVarImpl("g2", m, LB2, UB2);
                        while (m.getSolver().solve()) ;
                        Assert.assertEquals(18 * 18, m.getSolver().getSolutionCount());
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testDirectedGraphVarInstantiated() {
        Model m = new Model();
        int n = 3;
        DirectedGraph LB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        DirectedGraphVar g = new DirectedGraphVarImpl("g", m, LB, UB);
        Assert.assertTrue(g.isInstantiated());
        DirectedGraph gval = g.getValue();
        Assert.assertEquals(gval.getNodes().size(), 3);
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        DirectedGraphVar g2 = new DirectedGraphVarImpl("g2", m, LB2, UB2);
        Assert.assertFalse(g2.isInstantiated());
    }
}
