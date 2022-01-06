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
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test suite for UndirectedGraphVarImpl class
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class UndirectedGraphVarImplTest {

    /**
     * Instantiate an UndirectedGraphVar and test the basic methods.
     */
    @Test(groups="1s", timeOut=60000)
    public void basicTest() {
        Model m = new Model();
        int n = 3;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        int[] mandNeigZero = g.getMandatoryNeighborsOf(0).toArray();
        int[] potNeigZero = g.getPotentialNeighborsOf(0).toArray();
        int[] mandPredZero = g.getMandatoryPredecessorsOf(0).toArray();
        int[] potPredZero = g.getPotentialPredecessorOf(0).toArray();
        int[] mandSuccZero = g.getMandatorySuccessorsOf(0).toArray();
        int[] potSuccZero = g.getPotentialSuccessorsOf(0).toArray();
        Arrays.sort(mandNeigZero);
        Arrays.sort(potNeigZero);
        Arrays.sort(mandPredZero);
        Arrays.sort(potPredZero);
        Arrays.sort(mandSuccZero);
        Arrays.sort(potSuccZero);
        Assert.assertTrue(Arrays.equals(g.getMandatoryPredecessorsOf(0).toArray(), mandNeigZero));
        Assert.assertTrue(Arrays.equals(g.getMandatorySuccessorsOf(0).toArray(), mandNeigZero));
        Assert.assertTrue(Arrays.equals(g.getPotentialPredecessorOf(0).toArray(), potNeigZero));
        Assert.assertTrue(Arrays.equals(g.getPotentialSuccessorsOf(0).toArray(), potNeigZero));
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
        int n = 3;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED") && !nodeSetType.name().contains("DYNAMIC")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED") && !arcSetType.name().contains("DYNAMIC")) {
                        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, nodeSetType, arcSetType, false);
                        UndirectedGraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
                        Assert.assertFalse(g.isDirected());
                        while (m.getSolver().solve()) ;
                        Assert.assertEquals(18, m.getSolver().getSolutionCount());
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
        int n = 3;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED") && !nodeSetType.name().contains("DYNAMIC")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED") && !arcSetType.name().contains("DYNAMIC")) {
                        UndirectedGraph LB1 = GraphFactory.makeStoredUndirectedGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, nodeSetType, arcSetType, false);
                        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, nodeSetType, arcSetType, false);
                        UndirectedGraphVar g1 = new UndirectedGraphVarImpl("g1", m, LB1, UB1);
                        UndirectedGraphVar g2 = new UndirectedGraphVarImpl("g2", m, LB2, UB2);
                        while (m.getSolver().solve()) ;
                        Assert.assertEquals(18 * 18, m.getSolver().getSolutionCount());
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testGraphVarInstantiated() {
        Model m = new Model();
        int n = 3;
        UndirectedGraph LB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        Assert.assertTrue(g.isInstantiated());
        UndirectedGraph gval = g.getValue();
        Assert.assertEquals(gval.getNodes().size(), 3);
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, true);
        UndirectedGraphVar g2 = new UndirectedGraphVarImpl("g2", m, LB2, UB2);
        Assert.assertFalse(g2.isInstantiated());
    }
}
