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

import java.util.stream.IntStream;

/**
 * Test class for nbLoops graph constraint
 */
public class NbLoopsTest {

    @Test(groups="1s", timeOut=60000)
    public void instantiatedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 0}, {1, 1}, {5, 2}, {3, 3} }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 0}, {1, 1}, {5, 2}, {3, 3} }
        );
        UndirectedGraphVar g = model.undirectedGraphVar("g", LB, UB);
        IntVar nbLoops = model.intVar("nbLoops", 0, 10);
        model.nbLoops(g, nbLoops).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbLoops.getValue(), 3);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6},
                new int[][]{ {0, 0}, {1, 1}, {5, 2}, {3, 3}, {6, 6} }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6},
                new int[][]{ {0, 0}, {1, 1}, {5, 2}, {3, 3}, {6, 6} }
        );
        UndirectedGraphVar g = model.undirectedGraphVar("g", LB, UB);
        IntVar nbLoops = model.intVar("nbLoops", 2, 3);
        Constraint c =  model.nbLoops(g, nbLoops);
        c.post();
        Assert.assertEquals(c.getPropagator(0).isEntailed(), ESat.FALSE);
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedSuccessTest() {
        Model model = new Model();
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ }
        );
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 0}, {1, 1}, {3, 3}, {4, 4}, {1, 2} }
        );
        GraphVar g = model.directedGraphVar("g", LB, UB);
        IntVar nbLoops = model.intVar("nbLoops", 0, 10);
        model.nbLoops(g, nbLoops).post();
        model.arithm(nbLoops, "<=", 3).post();
        model.arithm(nbLoops, ">=", 2).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(nbLoops.getValue() >= 2);
            Assert.assertTrue(nbLoops.getValue() <= 3);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 20);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedFailTest() {
        Model model = new Model();
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ }
        );
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 0}, {1, 1}, {3, 3}, {4, 4}, {1, 2} }
        );
        GraphVar g = model.directedGraphVar("g", LB, UB);
        IntVar nbLoops = model.intVar("nbLoops", 1, 10);
        Constraint c =  model.nbLoops(g, nbLoops);
        c.post();
        Assert.assertEquals(c.getPropagator(0).isEntailed(), ESat.UNDEFINED);
        model.arithm(nbLoops, ">=", 5).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }
}
