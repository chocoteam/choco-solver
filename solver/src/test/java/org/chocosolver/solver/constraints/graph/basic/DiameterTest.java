/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for diameter graph constraint
 */
public class DiameterTest {

    // Undirected graph variable

    @Test(groups="1s", timeOut=60000)
    public void instantiatedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}
                }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar diam = model.intVar("diameter", 0, 20);
        model.diameter(g, diam).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(diam.getValue(), 9);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedFailTestBounds() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}
                }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar diam = model.intVar("diameter", 5);
        Constraint c = model.diameter(g, diam);
        c.post();
        Assert.assertEquals(c.isSatisfied(), ESat.FALSE);
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedFailTestNotConnected() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {6, 7}, {7, 8}, {8, 9}
                }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {6, 7}, {7, 8}, {8, 9}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar diam = model.intVar("diameter", 5);
        Constraint c = model.diameter(g, diam);
        c.post();
        Assert.assertEquals(c.isSatisfied(), ESat.FALSE);
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {3, 4, 5, 6, 7, 8},
                new int[][]{ }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar diam = model.intVar(7);
        model.diameter(g, diam).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {3, 4, 5, 6, 7, 8},
                new int[][]{ }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 8}, {8, 9}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar diam = model.intVar(0, 7);
        model.diameter(g, diam).post();
        model.nbEdges(g, model.intVar(8, 10)).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void generateTest() {
        Model model = new Model();
        int n = 5;
        int lbDiam = 0;
        int ubDiam = 4;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar diam = model.intVar("diam", lbDiam, ubDiam);
        model.diameter(g, diam).post();
        List<UndirectedGraph> l1 = new ArrayList<>();
        int realCount = 0;
        while (model.getSolver().solve()) {
            boolean b = true;
            for (UndirectedGraph gl1 : l1) {
                if (gl1.equals(g.getValue())) {
                    b = false;
                }
            }
            if (b) {
                realCount++;
                UndirectedGraph a = GraphFactory.makeUndirectedGraph(n, SetType.BITSET, SetType.BITSET, g.getValue().getNodes().toArray(), new int[][]{});
                for (int i : g.getValue().getNodes()) {
                    for (int j : g.getValue().getNeighborsOf(i)) {
                        a.addEdge(i, j);
                    }
                }
                l1.add(a);
            }
        }
        Assert.assertEquals(realCount, model.getSolver().getSolutionCount());
        // Check that no solution was missed
        Model model2 = new Model();
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model2.graphVar("g", LB2, UB2);
        Constraint cons = model.diameter(g2, model2.intVar(lbDiam, ubDiam));
        int count = 0;
        int realCount2 = 0;
        List<UndirectedGraph> l2 = new ArrayList<>();
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
                boolean b = true;
                for (UndirectedGraph gl2 : l2) {
                    if (gl2.equals(g2.getValue())) {
                        b = false;
                    }
                }
                if (b) {
                    realCount2++;
                }
            }
        }
        Assert.assertEquals(count, realCount2);
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }

    // Directed graph variable
}
