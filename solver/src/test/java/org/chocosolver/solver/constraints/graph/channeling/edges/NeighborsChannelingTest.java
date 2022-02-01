/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.edges;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Test class for neighborsChanneling graph constraints
 * @author Dimitri Justeau-Allaire
 * @since 23/03/2021
 */
public class NeighborsChannelingTest {

    SetType setType = SetType.BITSET;

    // NodeSetsChanneling

    @Test(groups="1s", timeOut=60000)
    public void testNeighborsSetsChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        SetVar[] sets = model.setVarArray(n, LB.getNodes().toArray(), UB.getNodes().toArray());
        model.neighborsChanneling(g, sets).post();
        while (model.getSolver().solve()) {
            for (int i : g.getValue().getNodes()) {
                int[] gNodes = g.getValue().getNeighborsOf(i).toArray();
                int[] sNodes = sets[i].getValue().toArray();
                Arrays.sort(gNodes);
                Arrays.sort(sNodes);
                Assert.assertTrue(Arrays.equals(gNodes, sNodes));
            }
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        g = model.graphVar("g", LB, UB);
        while (model.getSolver().solve()) {}
        Assert.assertEquals(nbSolutions, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testNeighborsSetsChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        SetVar[] sets = model.setVarArray(n, LB.getNodes().toArray(), UB.getNodes().toArray());
        LB.addNode(0);
        UB.removeNode(5);
        model.member(3, sets[0]).post();
        model.notMember(1, sets[0]).post();
        model.neighborsChanneling(g, sets).post();
        IntVar card = sets[1].getCard();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">", 0).post();
        while (model.getSolver().solve()) {
            for (int i : g.getValue().getNodes()) {
                int[] gNodes = g.getValue().getNeighborsOf(i).toArray();
                int[] sNodes = sets[i].getValue().toArray();
                Arrays.sort(gNodes);
                Arrays.sort(sNodes);
                Assert.assertTrue(Arrays.equals(gNodes, sNodes));
            }
            Assert.assertTrue(g.getValue().getNodes().contains(0));
            Assert.assertTrue(g.getValue().getNeighborsOf(0).contains(3));
            Assert.assertFalse(g.getValue().getNeighborsOf(0).contains(1));
            Assert.assertTrue(g.getValue().getNeighborsOf(1).size() > 0
                && g.getValue().getNeighborsOf(1).size() <= 3);
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UB.removeNode(5);
        g = model.graphVar("g", LB, UB);
        int count = 0;
        while (model.getSolver().solve()) {
            if (g.getValue().getNodes().contains(0)
                    && g.getValue().getNeighborsOf(0).contains(3)
                    && !g.getValue().getNeighborsOf(0).contains(1)
                    && g.getValue().getNeighborsOf(1).size() > 0
                    && g.getValue().getNeighborsOf(1).size() <= 3) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    // BoolsSetChanneling

    @Test(groups="1s", timeOut=60000)
    public void testNeighborsBoolsChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        BoolVar[][] boolVars = model.boolVarMatrix(n, n);
        model.neighborsChanneling(g, boolVars).post();
        while (model.getSolver().solve()) {
            for (int i : g.getValue().getNodes()) {
                int[] gNodes = g.getValue().getNeighborsOf(i).toArray();
                int[] sNodes = IntStream.range(0, n).filter(j -> boolVars[i][j].getValue() == 1).toArray();
                Arrays.sort(gNodes);
                Assert.assertTrue(Arrays.equals(gNodes, sNodes));
            }
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        g = model.graphVar("g", LB, UB);
        while (model.getSolver().solve()) {}
        Assert.assertEquals(nbSolutions, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testNeighborsBoolsChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        BoolVar[][] boolVars = model.boolVarMatrix(n, n);
        LB.addNode(0);
        UB.removeNode(5);
        model.arithm(boolVars[0][3], "=", 1).post();
        model.arithm(boolVars[0][1], "=", 0).post();
        model.neighborsChanneling(g, boolVars).post();
        IntVar card = model.intVar(0, n);
        model.sum(boolVars[1], "=", card).post();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">", 0).post();
        while (model.getSolver().solve()) {
            for (int i : g.getValue().getNodes()) {
                int[] gNodes = g.getValue().getNeighborsOf(i).toArray();
                int[] sNodes = IntStream.range(0, n).filter(j -> boolVars[i][j].getValue() == 1).toArray();
                Arrays.sort(gNodes);
                Arrays.sort(sNodes);
                Assert.assertTrue(Arrays.equals(gNodes, sNodes));
            }
            Assert.assertTrue(g.getValue().getNodes().contains(0));
            Assert.assertTrue(g.getValue().getNeighborsOf(0).contains(3));
            Assert.assertFalse(g.getValue().getNeighborsOf(0).contains(1));
            Assert.assertTrue(g.getValue().getNeighborsOf(1).size() > 0
                    && g.getValue().getNeighborsOf(1).size() <= 3);
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UB.removeNode(5);
        g = model.graphVar("g", LB, UB);
        int count = 0;
        while (model.getSolver().solve()) {
            if (g.getValue().getNodes().contains(0)
                    && g.getValue().getNeighborsOf(0).contains(3)
                    && !g.getValue().getNeighborsOf(0).contains(1)
                    && g.getValue().getNeighborsOf(1).size() > 0
                    && g.getValue().getNeighborsOf(1).size() <= 3) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    // NodeSetChanneling

    @Test(groups="1s", timeOut=60000)
    public void testNeighborsSetChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        SetVar set = model.setVar(LB.getNodes().toArray(), UB.getNodes().toArray());
        model.neighborsChanneling(g, set, 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getNeighborsOf(0).toArray();
            int[] sNodes = set.getValue().toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        g = model.graphVar("g", LB, UB);
        while (model.getSolver().solve()) {}
        Assert.assertEquals(nbSolutions, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testNeighborsSetChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        SetVar set = model.setVar(LB.getNodes().toArray(), UB.getNodes().toArray());
        UB.removeNode(5);
        model.member(3, set).post();
        model.notMember(1, set).post();
        model.neighborsChanneling(g, set, 0).post();
        IntVar card = set.getCard();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">", 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getNeighborsOf(0).toArray();
            int[] sNodes = set.getValue().toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
            Assert.assertTrue(g.getValue().getNodes().contains(0));
            Assert.assertTrue(g.getValue().getNeighborsOf(0).contains(3));
            Assert.assertFalse(g.getValue().getNeighborsOf(0).contains(1));
            Assert.assertTrue(g.getValue().getNeighborsOf(0).size() > 0
                    && g.getValue().getNeighborsOf(0).size() <= 3);
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UB.removeNode(5);
        g = model.graphVar("g", LB, UB);
        int count = 0;
        while (model.getSolver().solve()) {
            if (g.getValue().getNodes().contains(0)
                    && g.getValue().getNeighborsOf(0).contains(3)
                    && !g.getValue().getNeighborsOf(0).contains(1)
                    && g.getValue().getNeighborsOf(0).size() > 0
                    && g.getValue().getNeighborsOf(0).size() <= 3) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    // NodeBoolChanneling

    @Test(groups="1s", timeOut=60000)
    public void testNeighborsBoolChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        BoolVar[] boolVars = model.boolVarArray(n);
        model.neighborsChanneling(g, boolVars, 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getNeighborsOf(0).toArray();
            int[] sNodes = IntStream.range(0, n).filter(i -> boolVars[i].getValue() == 1).toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        g = model.graphVar("g", LB, UB);
        while (model.getSolver().solve()) {}
        Assert.assertEquals(nbSolutions, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testNeighborsBoolChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        BoolVar[] boolVars = model.boolVarArray(n);
        UB.removeNode(5);
        model.arithm(boolVars[3], "=", 1).post();
        model.arithm(boolVars[1], "=", 0).post();
        model.neighborsChanneling(g, boolVars, 0).post();
        IntVar card = model.intVar(0, n);
        model.sum(boolVars, "=", card).post();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">", 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getNeighborsOf(0).toArray();
            int[] sNodes = IntStream.range(0, n).filter(i -> boolVars[i].getValue() == 1).toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
            Assert.assertTrue(g.getValue().getNodes().contains(0));
            Assert.assertTrue(g.getValue().getNeighborsOf(0).contains(3));
            Assert.assertFalse(g.getValue().getNeighborsOf(0).contains(1));
            Assert.assertTrue(g.getValue().getNeighborsOf(0).size() > 0
                    && g.getValue().getNeighborsOf(0).size() <= 3);
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UB.removeNode(5);
        g = model.graphVar("g", LB, UB);
        int count = 0;
        while (model.getSolver().solve()) {
            if (g.getValue().getNodes().contains(0)
                    && g.getValue().getNeighborsOf(0).contains(3)
                    && !g.getValue().getNeighborsOf(0).contains(1)
                    && g.getValue().getNeighborsOf(0).size() > 0
                    && g.getValue().getNeighborsOf(0).size() <= 3) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }
}
