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
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test suite for UndirectedNodeInducedGraphVarImpl class
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class UndirectedNodeInducedGraphVarImplTest {

    /**
     * Instantiate an UndirectedNodeInducedGraphVar and test the basic methods.
     */
    @Test(groups="1s", timeOut=60000)
    public void basicTest() {
        Model m = new Model();
        int n = 3;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraphVar g = m.nodeInducedGraphVar("g", LB, UB);
        ICause fakeCause = new ICause() {};
        try {
            g.instantiateTo(UB, fakeCause);
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertTrue(g.isInstantiated());
    }

    /**
     * Test the instantiation of a single undirected graph variable with all combinations of node and arc sets types.
     * Enumerate of possible graph instantiations with the default strategy and assert that no value has been missed.
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 10;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED") && !nodeSetType.name().contains("DYNAMIC")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED") && !arcSetType.name().contains("DYNAMIC")) {
                        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, nodeSetType, arcSetType, false);
                        UndirectedGraphVar g = m.nodeInducedGraphVar("g", LB, UB);
                        Assert.assertFalse(g.isDirected());
                        while (m.getSolver().solve());
                        // There are exactly 2^n node-induced subgraphs of a graph with n nodes.
                        Assert.assertEquals(m.getSolver().getSolutionCount(), Math.pow(2, n));
                    }
                }
            }
        }
    }

    /**
     * Same as previous but with two undirected graph variables
     * (needed because current implementation of default search uses one GraphStrategy for one graph variable)
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerateTwo() {
        Model m = new Model();
        int n = 6;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED") && !nodeSetType.name().contains("DYNAMIC")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED") && !arcSetType.name().contains("DYNAMIC")) {
                        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, nodeSetType, arcSetType, false);
                        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, nodeSetType, arcSetType, false);
                        UndirectedGraphVar g1 = m.nodeInducedGraphVar("g1", LB1, UB1);
                        UndirectedGraphVar g2 = m.nodeInducedGraphVar("g2", LB2, UB2);
                        while (m.getSolver().solve()) ;
                        Assert.assertEquals(Math.pow(2, n) * Math.pow(2, n), m.getSolver().getSolutionCount());
                    }
                }
            }
        }
    }

    /**
     * Instantiate a node-induced graph with a value which is not a node-induced subgraph of the envelope
     * and assert that is fails.
     */
    @Test(groups="1s", timeOut=60000)
    public void testForbiddenInstantiation() {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.nodeInducedGraphVar("g", LB, UB);
        UndirectedGraph wrongValue = GraphFactory.makeUndirectedGraph(
                n, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2},
                new int[][] { {0, 1} }
        );
        try {
            g.instantiateTo(wrongValue, new ICause() {});
            Assert.fail();
        } catch (ContradictionException e) {
            // OK
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testGraphVarInstantiated() {
        Model m = new Model();
        int n = 3;
        UndirectedGraph LB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraphVar g = m.nodeInducedGraphVar("g", LB, UB);
        Assert.assertTrue(g.isInstantiated());
        UndirectedGraph gval = g.getValue();
        Assert.assertEquals(gval.getNodes().size(), 3);
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraphVar g2 = m.nodeInducedGraphVar("g2", LB2, UB2);
        Assert.assertFalse(g2.isInstantiated());
    }

    /**
     * Test a basic constrained problem (connectedness) for which the number of solutions can be counted and
     * ensure that every solution is found.
     */
    @Test(groups="1s", timeOut=60000)
    public void testUseCase() {
        Model m = new Model();
        int n = 4;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UB.removeEdge(0, 3); UB.removeEdge(1, 2);
        UndirectedGraphVar g = m.nodeInducedGraphVar("g", LB, UB);
        m.connected(g).post();
        while (m.getSolver().solve());
        Assert.assertEquals(m.getSolver().getSolutionCount(), 14);
        // Test with a general graph var.
        m = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UB.removeEdge(0, 3); UB.removeEdge(1, 2);
        g = m.graphVar("g", LB, UB);
        m.connected(g).post();
        while (m.getSolver().solve());
        Assert.assertEquals(m.getSolver().getSolutionCount(), 18);
    }

    /**
     * Ensure that the removeEdge operation does not filters possible solutions:
     *     if LB = empty graph, UB = 0 - 1, and edge (0, 1) is forbidden by a constraint, then
     *     the possible solutions are 0, 1, and the empty graph.
     */
    @Test(groups="1s", timeOut=60000)
    public void testUseCase2() {
        Model m = new Model();
        int n = 2;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UB.addNode(0); UB.addNode(1);
        UB.addEdge(0, 1);
        UndirectedGraphVar g = m.nodeInducedGraphVar("g", LB, UB);
        SetVar s = m.graphNeighborsSetView(g, 0);
        m.notMember(1, s).post();
        while (m.getSolver().solve());
        Assert.assertEquals(m.getSolver().getSolutionCount(), 3);
    }
}
