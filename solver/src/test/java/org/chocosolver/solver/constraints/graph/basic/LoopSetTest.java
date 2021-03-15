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
import org.chocosolver.solver.variables.SetVar;
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
 * Test class for loopSet graph constraint
 */
public class LoopSetTest {

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
        SetVar loopSet = model.setVar("loopSet", new int[] {}, IntStream.range(0, 10).toArray());
        model.loopSet(g, loopSet).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(loopSet.getValue().size(), 3);
            Assert.assertTrue(loopSet.getValue().contains(0));
            Assert.assertTrue(loopSet.getValue().contains(1));
            Assert.assertTrue(loopSet.getValue().contains(3));
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
        SetVar loopSet = model.setVar("loopSet", new int[] {0, 1}, IntStream.range(0, 5).toArray());
        Constraint c =  model.loopSet(g, loopSet);
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
        SetVar loopSet = model.setVar("loopSet", new int[] {}, IntStream.range(0, 10).toArray());
        model.loopSet(g, loopSet).post();
        IntVar card = loopSet.getCard();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">=", 2).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(loopSet.getValue().size() >= 2);
            Assert.assertTrue(loopSet.getValue().size() <= 3);
            Assert.assertFalse(loopSet.getValue().contains(2));
            Assert.assertFalse(loopSet.getValue().contains(5));
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
        SetVar loopSet = model.setVar("loopSet", new int[] {2}, IntStream.range(0, 10).toArray());
        Constraint c =  model.loopSet(g, loopSet);
        c.post();
        Assert.assertEquals(c.getPropagator(0).isEntailed(), ESat.FALSE);
        IntVar card = loopSet.getCard();
        model.arithm(card, ">=", 5).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }
}
