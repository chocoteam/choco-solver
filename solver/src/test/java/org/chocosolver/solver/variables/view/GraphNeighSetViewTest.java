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
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.impl.UndirectedGraphVarImpl;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test suite for GraphNeighSetView class
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class GraphNeighSetViewTest {

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
        UndirectedGraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        GraphNeighSetView s = new GraphNeighSetView("s", g, 0);
        Assert.assertEquals(s.getLB().size(), 0);
        Assert.assertEquals(s.getUB().size(), 4);
        while (m.getSolver().solve()) {
            int[] neighsInGraph = g.getValue().getNeighOf(0).toArray();
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
        UndirectedGraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        GraphNeighSetView s = new GraphNeighSetView("s", g, 0);
        m.allEqual(s, m.setVar(new int[] {1, 2, 4})).post();
        while (m.getSolver().solve()) {
            int[] neighsInGraph = g.getValue().getNeighOf(0).toArray();
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
        UndirectedGraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        GraphNeighSetView s = new GraphNeighSetView("s", g, 0);
        s.instantiateTo(new int[] {2, 3}, s);
        while (m.getSolver().solve()) {
            int[] neighsInGraph = g.getValue().getNeighOf(0).toArray();
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
        UndirectedGraphVar g = new UndirectedGraphVarImpl("g", m, LB, UB);
        GraphNeighSetView s = new GraphNeighSetView("s", g, 0);
        m.allEqual(s, m.setVar(new int[] {1, 2, 3})).post();
        Assert.assertFalse(m.getSolver().solve());
    }
}
