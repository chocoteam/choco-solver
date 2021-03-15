/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for NbEdges graph constraint
 */
public class NbEdgesTest {

    @Test(groups="1s", timeOut=60000)
    public void instantiatedUndirectedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 3}, {5, 2} }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 3}, {5, 2} }
        );
        UndirectedGraphVar g = model.undirectedGraphVar("g", LB, UB);
        IntVar nbEdges = model.intVar(0, 20);
        model.nbEdges(g, nbEdges).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbEdges.getValue(), 3);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedDirectedSuccessTest() {
        Model model = new Model();
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 4, 5},
                new int[][]{ {0, 1}, {1, 4}, {5, 1} }
        );
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 4, 5},
                new int[][]{ {0, 1}, {1, 4}, {5, 1} }
        );
        DirectedGraphVar g = model.directedGraphVar("g", LB, UB);
        IntVar nbEdges = model.intVar(0, 20);
        model.nbEdges(g, nbEdges).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbEdges.getValue(), 3);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedUndirectedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 3}, {5, 2} }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1}, {1, 3}, {5, 2} }
        );
        UndirectedGraphVar g = model.undirectedGraphVar("g", LB, UB);
        IntVar nbEdges = model.intVar(10, 20);
        Constraint c = model.nbEdges(g, nbEdges);
        c.post();
        Assert.assertEquals(c.getPropagator(0).isEntailed(), ESat.FALSE);
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedDirectedFailTest() {
        Model model = new Model();
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 4, 5},
                new int[][]{ {0, 1}, {1, 4}, {5, 3} }
        );
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 4, 5},
                new int[][]{ {0, 1}, {1, 4}, {5, 3} }
        );
        DirectedGraphVar g = model.directedGraphVar("g", LB, UB);
        IntVar nbEdges = model.intVar(0, 2);
        Constraint c = model.nbEdges(g, nbEdges);
        c.post();
        Assert.assertEquals(c.getPropagator(0).isEntailed(), ESat.FALSE);
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void ConstrainedNbNodesSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3},
                new int[][]{}
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3},
                new int[][]{ {0, 1}, {1, 2}, {2, 3}, {3, 1}}
        );
        UndirectedGraphVar g = model.undirectedGraphVar("g", LB, UB);
        IntVar nbEdges = model.intVar(0, 20);
        model.nbEdges(g, nbEdges).post();
        model.arithm(nbEdges, "<=", 3).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(nbEdges.getValue() <= 3);
            int nbE = 0;
            for (int node : g.getValue().getNodes()) {
                for (int neigh : g.getValue().getNeighborsOf(node)) {
                    nbE++;
                }
            }
            Assert.assertTrue((nbE / 2) <= 3);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 15);
    }

    @Test(groups="1s", timeOut=60000)
    public void ConstrainedNbNodesFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, 10, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2},
                new int[][]{}
        );
        GraphVar g = model.undirectedGraphVar("g", LB, UB);
        IntVar nbEdges = model.intVar(0, 20);
        model.nbEdges(g, nbEdges).post();
        model.arithm(nbEdges, ">=", 3).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }
}
