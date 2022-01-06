/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.nodes;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Test class for node(s)Channeling constraints
 */
public class NodesChannelingTest {

    SetType setType = SetType.BITSET;

    // NodeSetChanneling

    @Test(groups="1s", timeOut=60000)
    public void testNodeSetChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        GraphVar g = model.graphVar("g", LB, UB);
        SetVar set = model.setVar(LB.getNodes().toArray(), UB.getNodes().toArray());
        model.nodesChanneling(g, set).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getNodes().toArray();
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
    public void testNodeSetChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        GraphVar g = model.graphVar("g", LB, UB);
        SetVar set = model.setVar(LB.getNodes().toArray(), UB.getNodes().toArray());
        UB.removeNode(5);
        model.member(3, set).post();
        model.notMember(1, set).post();
        model.nodesChanneling(g, set).post();
        IntVar card = set.getCard();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">", 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getNodes().toArray();
            int[] sNodes = set.getValue().toArray();
            Arrays.sort(gNodes);
            Arrays.sort(sNodes);
            Assert.assertTrue(Arrays.equals(gNodes, sNodes));
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
            ISet nodes = g.getValue().getNodes();
            if (nodes.size() > 0 && nodes.size() <= 3 && nodes.contains(3) && !nodes.contains(1)) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    // NodeBoolsChanneling

    @Test(groups="1s", timeOut=60000)
    public void testNodeBoolsChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        GraphVar g = model.graphVar("g", LB, UB);
        BoolVar[] bools = model.boolVarArray(n);
        model.nodesChanneling(g, bools).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getNodes().toArray();
            int[] bNodes = IntStream.range(0, n).filter(i -> bools[i].getValue() == 1).toArray();
            Arrays.sort(gNodes);
            Assert.assertTrue(Arrays.equals(gNodes, bNodes));
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
    public void testNodeBoolsChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UB.removeNode(5);
        GraphVar g = model.graphVar("g", LB, UB);
        BoolVar[] bools = model.boolVarArray(n);
        model.arithm(bools[3], "=", 1).post();
        model.arithm(bools[1], "=", 0).post();
        model.nodesChanneling(g, bools).post();
        IntVar card = model.intVar(0, n);
        model.sum(bools, "=", card).post();
        model.arithm(card, "<=", 3).post();
        model.arithm(card, ">", 0).post();
        while (model.getSolver().solve()) {
            int[] gNodes = g.getValue().getNodes().toArray();
            int[] bNodes = IntStream.range(0, n).filter(i -> bools[i].getValue() == 1).toArray();
            Arrays.sort(gNodes);
            Assert.assertTrue(Arrays.equals(gNodes, bNodes));
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
            ISet nodes = g.getValue().getNodes();
            if (nodes.size() > 0 && nodes.size() <= 3 && nodes.contains(3) && !nodes.contains(1)) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    // NodeBoolChanneling

    @Test(groups="1s", timeOut=60000)
    public void testNodeBoolChannelingGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        GraphVar g = model.graphVar("g", LB, UB);
        BoolVar bool = model.boolVar();
        model.nodeChanneling(g, bool, 3).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(g.getValue().containsNode(3), bool.getValue() == 1);
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
    public void testNodeBoolChannelingGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UB.removeNode(5);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        BoolVar bool = model.boolVar();
        model.nodeChanneling(g, bool, 3).post();
        model.arithm(bool, "=", 1).post();
        model.connected(g).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(g.getValue().containsNode(3) && bool.getValue() == 1);
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UB.removeNode(5);
        g = model.graphVar("g", LB, UB);
        Constraint cons = model.connected(g);
        int count = 0;
        while (model.getSolver().solve()) {
            ISet nodes = g.getValue().getNodes();
            if (cons.isSatisfied() == ESat.TRUE && nodes.contains(3)) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }
}
