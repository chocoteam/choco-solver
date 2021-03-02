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
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.impl.UndirectedGraphVarImpl;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
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
        UndirectedGraph LB = GraphFactory.makeEmptyStoredGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        GraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        GraphNodeSetView s = new GraphNodeSetView("s", g);
        while (m.getSolver().solve()) {
            ISet nodes = g.getValue().getNodes();
            ISet nodeSet = s.getValue();
            Assert.assertEquals(nodes, nodeSet);
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
        UndirectedGraph LB = GraphFactory.makeEmptyStoredGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        GraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        GraphNodeSetView s = new GraphNodeSetView("s", g);
        m.allEqual(s, m.setVar(new int[] {0, 2, 4})).post();
        while (m.getSolver().solve()) {
            int[] nodes = g.getValue().getNodes().toArray();
            int[] nodeSet = s.getValue().toArray();
            Arrays.sort(nodes);
            Arrays.sort(nodeSet);
            Assert.assertEquals(nodes, nodeSet);
        }
    }
}
