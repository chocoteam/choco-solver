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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for edge channeling
 * @author Dimitri Justeau-Allaire
 * @since 23/03/2021
 */
public class EdgeChannelingTest {

    SetType setType = SetType.BITSET;

    // Undirected graph variable

    @Test(groups="1s", timeOut=60000)
    public void testUndirectedGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        BoolVar bool = model.boolVar();
        model.edgeChanneling(g, bool, 0, 3).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(
                    g.getValue().containsNode(3) && g.getValue().getNeighborsOf(3).contains(0),
                    bool.getValue() == 1
            );
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
    public void testUndirectedGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, setType, setType, false);
        UB.removeNode(5);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        BoolVar bool = model.boolVar();
        model.edgeChanneling(g, bool, 0, 3).post();
        model.arithm(bool, "=", 1).post();
        model.connected(g).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(
                    g.getValue().containsNode(3) && g.getValue().getNeighborsOf(3).contains(0)
                    && bool.getValue() == 1
            );
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
            if (cons.isSatisfied() == ESat.TRUE && nodes.contains(3) && g.getValue().getNeighborsOf(3).contains(0)) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }

    // Directed graph variable

    @Test(groups="10s", timeOut=60000)
    public void testDirectedGenerate() {
        // Generate all solutions
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        BoolVar bool = model.boolVar();
        model.edgeChanneling(g, bool, 0, 3).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(
                    g.getValue().containsNode(3) && g.getValue().getPredecessorsOf(3).contains(0),
                    bool.getValue() == 1
            );
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
    public void testDirectedGenerateConstrained() {
        // Generate all solutions
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        UB.removeNode(4);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        BoolVar bool = model.boolVar();
        model.edgeChanneling(g, bool, 0, 3).post();
        model.arithm(bool, "=", 0).post();
        model.stronglyConnected(g).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(!g.getValue().getPredecessorsOf(3).contains(0) && bool.getValue() == 0);
        }
        long nbSolutions = model.getSolver().getSolutionCount();
        // Generate without constraint and check
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        LB.addNode(0);
        UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        UB.removeNode(4);
        g = model.digraphVar("g", LB, UB);
        Constraint cons = model.stronglyConnected(g);
        int count = 0;
        while (model.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE && !g.getValue().getPredecessorsOf(3).contains(0)) {
                count++;
            }
        }
        Assert.assertEquals(nbSolutions, count);
    }
}
