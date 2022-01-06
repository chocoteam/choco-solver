/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.cycles;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CycleTest {

    @Test
    public void test() throws ContradictionException {
        Model m = new Model();
        int n = 4;
        UndirectedGraph GLB = new UndirectedGraph(m, n, SetType.BITSET, false);
        UndirectedGraph GUB = new UndirectedGraph(m, n, SetType.BITSET, false);
        for (int i = 0; i < n; i++) {
            GUB.addNode(i);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                GUB.addEdge(i, j);
            }
        }
        UndirectedGraphVar g = m.graphVar("g", GLB, GUB);
        m.cycle(g).post();
        m.nbNodes(g, m.intVar(n)).post();

        Solver s = m.getSolver();
        s.propagate();

        while (s.solve()){
            System.out.println("sol ---");
            System.out.println(g.getValue().graphVizExport());
        }
        s.printStatistics();
        Assert.assertEquals(s.getSolutionCount(), 3);
    }

    @Test
    public void testPropag() throws ContradictionException {
        Model m = new Model();
        int n = 4;
        UndirectedGraph GLB = new UndirectedGraph(m, n, SetType.LINKED_LIST, false);
        UndirectedGraph GUB = new UndirectedGraph(m, n, SetType.LINKED_LIST, false);
        for (int i = 0; i < n; i++) {
            GUB.addNode(i);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                GUB.addEdge(i, j);
            }
        }
        GLB.addEdge(0, 3);
        GLB.addEdge(0, 2);
        UndirectedGraphVar g = m.graphVar("g", GLB, GUB);
        System.out.println(g.getMandatoryNodes());
        m.cycle(g).post();
        m.nbNodes(g, m.intVar(n)).post();

        Solver s = m.getSolver();
        s.propagate();

        System.out.println(g);

        Assert.assertTrue(g.isInstantiated());

        while (s.solve()){
            System.out.println("sol ---");
            System.out.println(g.getValue().graphVizExport());
        }
        s.printStatistics();
        Assert.assertEquals(s.getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedTest() {
        // Empty graph must be accepted by the constraints
        Model model = new Model();
        int n = 10;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.cycle(g).post();
        while (model.getSolver().solve()) {
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // Graph with 1 node and no loop contains no cycle
        model = new Model();
        n = 1;
        int[] nodes = new int[] {0};
        int[][] edges = new int[][] {};
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.cycle(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
        // Graph with 1 node and a loop is a cycle
        model = new Model();
        n = 1;
        nodes = new int[] {0};
        edges = new int[][] {{0, 0}};
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.cycle(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // A simple path graph contains no cycle
        model = new Model();
        n = 10;
        nodes = new int[] {0, 1, 2, 3, 4, 5};
        edges = new int[][] {
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}
        };
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        UB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, nodes, edges);
        g = model.graphVar("g", LB, UB);
        model.cycle(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void generateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.cycle(g).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model2.graphVar("g", LB2, UB2);
        Constraint cons = model2.cycle(g2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }
}
