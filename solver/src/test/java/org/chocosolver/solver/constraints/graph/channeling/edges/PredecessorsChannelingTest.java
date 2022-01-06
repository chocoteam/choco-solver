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
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Test class for predecessorsChanneling graph constraints
 * @author Dimitri Justeau-Allaire
 * @since 23/03/2021
 */
public class PredecessorsChannelingTest {

    SetType setType = SetType.BITSET;

    // NodeSetChanneling

    @Test(groups="1s", timeOut=60000)
    public void testPredecessorsSetChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        SetVar set = model.setVar(LB.getNodes().toArray(), UB.getNodes().toArray());
        model.predecessorsChanneling(g, set, 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getPredecessorsOf(0).toArray();
            int[] sNodes = set.getValue().toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        g = model.digraphVar("g", LB, UB);
        while (model.getSolver().solve()) {}
        Assert.assertEquals(nbSolutions, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testPredecessorsSetChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        SetVar set = model.setVar(LB.getNodes().toArray(), UB.getNodes().toArray());
        UB.removeNode(4);
        model.member(3, set).post();
        model.notMember(1, set).post();
        model.predecessorsChanneling(g, set, 0).post();
        IntVar card = set.getCard();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">", 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getPredecessorsOf(0).toArray();
            int[] sNodes = set.getValue().toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
            Assert.assertTrue(g.getValue().getNodes().contains(0));
            Assert.assertTrue(g.getValue().getPredecessorsOf(0).contains(3));
            Assert.assertFalse(g.getValue().getPredecessorsOf(0).contains(1));
            Assert.assertTrue(g.getValue().getPredecessorsOf(0).size() > 0
                    && g.getValue().getPredecessorsOf(0).size() <= 3);
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        UB.removeNode(4);
        g = model.digraphVar("g", LB, UB);
        int count = 0;
        while (model.getSolver().solve()) {
            if (g.getValue().getNodes().contains(0)
                    && g.getValue().getPredecessorsOf(0).contains(3)
                    && !g.getValue().getPredecessorsOf(0).contains(1)
                    && g.getValue().getPredecessorsOf(0).size() > 0
                    && g.getValue().getPredecessorsOf(0).size() <= 3) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    // NodeBoolChanneling

    @Test(groups="1s", timeOut=60000)
    public void testPredecessorsBoolChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        BoolVar[] boolVars = model.boolVarArray(n);
        model.predecessorsChanneling(g, boolVars, 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getPredecessorsOf(0).toArray();
            int[] sNodes = IntStream.range(0, n).filter(i -> boolVars[i].getValue() == 1).toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        g = model.digraphVar("g", LB, UB);
        while (model.getSolver().solve()) {}
        Assert.assertEquals(nbSolutions, model.getSolver().getSolutionCount());
    }

    @Test(groups="1s", timeOut=60000)
    public void testPredecessorsBoolChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        BoolVar[] boolVars = model.boolVarArray(n);
        UB.removeNode(4);
        model.arithm(boolVars[3], "=", 1).post();
        model.arithm(boolVars[1], "=", 0).post();
        model.predecessorsChanneling(g, boolVars, 0).post();
        IntVar card = model.intVar(0, n);
        model.sum(boolVars, "=", card).post();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">", 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getPredecessorsOf(0).toArray();
            int[] sNodes = IntStream.range(0, n).filter(i -> boolVars[i].getValue() == 1).toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
            Assert.assertTrue(g.getValue().getNodes().contains(0));
            Assert.assertTrue(g.getValue().getPredecessorsOf(0).contains(3));
            Assert.assertFalse(g.getValue().getPredecessorsOf(0).contains(1));
            Assert.assertTrue(g.getValue().getPredecessorsOf(0).size() > 0
                    && g.getValue().getPredecessorsOf(0).size() <= 3);
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        UB.removeNode(4);
        g = model.digraphVar("g", LB, UB);
        int count = 0;
        while (model.getSolver().solve()) {
            if (g.getValue().getNodes().contains(0)
                    && g.getValue().getPredecessorsOf(0).contains(3)
                    && !g.getValue().getPredecessorsOf(0).contains(1)
                    && g.getValue().getPredecessorsOf(0).size() > 0
                    && g.getValue().getPredecessorsOf(0).size() <= 3) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }
}
