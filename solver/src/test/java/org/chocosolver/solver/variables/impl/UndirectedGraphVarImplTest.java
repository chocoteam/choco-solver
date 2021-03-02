/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test suite for UndirectedGraphVarImpl classe
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class UndirectedGraphVarImplTest {

    /**
     * Test the instantiation of a single undirected graph variable with all combinations of node and arc sets types.
     * Enumerate of possible graph instantiations with the default strategy and assert that no value has been missed.
     */
    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 3;
        for (SetType nodeSetType : SetType.values()) {
            if(!nodeSetType.name().contains("FIXED")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED")) {
                        UndirectedGraph LB = GraphFactory.makeEmptyStoredGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph UB = GraphFactory.makeCompleteStoredGraph(m, n, nodeSetType, arcSetType, false);
                        GraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
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
            if(!nodeSetType.name().contains("FIXED")) {
                for (SetType arcSetType : SetType.values()) {
                    if (!arcSetType.name().contains("FIXED")) {
                        UndirectedGraph LB1 = GraphFactory.makeEmptyStoredGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph LB2 = GraphFactory.makeEmptyStoredGraph(m, n, nodeSetType, arcSetType);
                        UndirectedGraph UB1 = GraphFactory.makeCompleteStoredGraph(m, n, nodeSetType, arcSetType, false);
                        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredGraph(m, n, nodeSetType, arcSetType, false);
                        GraphVar g1 = new UndirectedGraphVarImpl("g1", m, LB1, UB1);
                        GraphVar g2 = new UndirectedGraphVarImpl("g2", m, LB2, UB2);
                        while (m.getSolver().solve()) ;
                        Assert.assertEquals(18 * 18, m.getSolver().getSolutionCount());
                    }
                }
            }
        }
    }
}
