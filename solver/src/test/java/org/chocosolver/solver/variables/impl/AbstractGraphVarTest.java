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
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test call for AbstractGraphVar
 */
public class AbstractGraphVarTest {

    /**
     * Instantiate an AbstractGraphVar and test the basic methods.
     */
    @Test(groups="1s", timeOut=60000)
    public void basicTest() {
        Model m = new Model();
        int n = 3;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UB.removeNode(2);
        AbstractGraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        ICause fakeCause = new ICause() {};
        try {
            g.instantiateTo(UB, fakeCause);
        } catch (ContradictionException e) {
            Assert.fail();
        }
        Assert.assertTrue(g.isInstantiated());
        g.toString();
        // Attempt to remove kernel node
        try {
            g.removeNode(0, fakeCause);
            Assert.fail();
        } catch (ContradictionException e) {
            // SUCCESS
        }
        // Remove node that does not belong to UB
        try {
            Assert.assertFalse(g.removeNode(2, fakeCause));
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        // Enforce node not in the domain
        try {
            g.enforceNode(2, fakeCause);
            Assert.fail();
        } catch (ContradictionException e) {
            // SUCCESS
        }
        // Test getValueAsBoolMatrix
        boolean[][] expected = new boolean[][] {
                {false, true, false},
                {true, false, false},
                {false, false, false},
                {true, true, false}
        };
        Assert.assertTrue(Arrays.deepEquals(expected, g.getValueAsBoolMatrix()));
        // Test instantiateTo from bool matrix
        try {
            g.instantiateTo(expected, fakeCause);
        } catch (ContradictionException e) {
            Assert.fail();
        }
        // Test graphviz export (just in case of bug, the output is not tested here)
        g.getValue().graphVizExport();
    }
}
