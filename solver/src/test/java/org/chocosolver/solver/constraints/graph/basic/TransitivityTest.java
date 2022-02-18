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
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for transitivity graph constraint
 */
public class TransitivityTest {

    @Test(groups="1s", timeOut=60000)
    public void instantiatedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5},
                        {0, 2}, {1, 3}, {2, 4}, {3, 5},
                        {0, 3}, {0, 4}, {0, 5},
                        {1, 4}, {1, 5},
                        {2, 5}
                }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5},
                        {0, 2}, {1, 3}, {2, 4}, {3, 5},
                        {0, 3}, {0, 4}, {0, 5},
                        {1, 4}, {1, 5},
                        {2, 5}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.transitivity(g).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5} }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5} }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        Constraint c = model.transitivity(g);
        c.post();
        Assert.assertEquals(c.getPropagator(0).isEntailed(), ESat.FALSE);
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 2}, {2, 3} }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 2}, {2, 3}, {3, 4},
                        {0, 2}, {1, 3}, {2, 4}, {3, 5}, {0, 3},
                        {0, 4}, {1, 4}, {2, 4}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.transitivity(g).post();
        model.nbEdges(g, model.intVar(6, 10)).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 2}, {2, 3} }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 2}, {2, 3}, {3, 4},
                        {0, 2}, {1, 3}, {2, 4}, {3, 5}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.transitivity(g).post();
        model.nbEdges(g, model.intVar(3, 4)).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void generateUndirectedTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 7;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.transitivity(g).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model2.graphVar("g2", LB2, UB2);
        Constraint cons = model2.transitivity(g2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }

    @Test(groups="10s", timeOut=60000)
    public void generateDirectedTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        model.transitivity(g).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = model2.digraphVar("g2", LB2, UB2);
        Constraint cons = model2.transitivity(g2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }
}
