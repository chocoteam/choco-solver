/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.tree;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DirectedTreeForestTest {

    private static SetType graphTypeEnv = SetType.BIPARTITESET;
    private static SetType graphTypeKer = SetType.BIPARTITESET;

    public static Solver model(int n, int seed, boolean gac) {
        final Model m = new Model();
        DirectedGraph GLB = new DirectedGraph(m,n,graphTypeKer,false);
        DirectedGraph GUB = new DirectedGraph(m,n,graphTypeEnv,false);
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < n; j++) {
                GUB.addEdge(i, j);
            }
        }
        GLB.addNode(0);
        final DirectedGraphVar g = m.digraphVar("G", GLB, GUB);
        int[] preds = new int[n];
        for (int i = 0; i < n; i++) {
            preds[i] = 1;
        }
        preds[0] = 0;
        System.out.println("%%%%%%%%%");
        if(gac) {
            m.directedTree(g, 0).post();
        }else{
            m.directedForest(g).post();
            int[] indeg = new int[n];
            for(int i=0;i<n;i++) {
                indeg[i] = 1;
            }
            indeg[0] = 0;
            m.minInDegrees(g, indeg).post();
        }
        m.nbNodes(g, m.intVar("nbNodes", n / 3, n)).post();
        m.getSolver().setSearch(Search.randomGraphVarSearch(seed, g));
        m.getSolver().limitSolution(100);
        while(m.getSolver().solve());
        return m.getSolver();
    }

    @Test(groups = "10s")
    public static void smallTrees() {
        int s = 0;
        for (int n = 3; n < 8; n++) {
            System.out.println("Test n=" + n + ", with seed=" + s);
            Solver good = model(n, s, true);
            assertEquals(good.getMeasures().getFailCount(), 0);
            assertTrue(good.getMeasures().getSolutionCount() > 0);
            Solver slow = model(n, s, false);
            assertEquals(good.getMeasures().getSolutionCount(), slow.getMeasures().getSolutionCount());
        }
    }

    @Test(groups = "10s")
    public static void bigTrees() {
        int s = 0;
        int n = 60;
        System.out.println("Test n=" + n + ", with seed=" + s);
        Solver good = model(n, s, true);
        assertEquals(good.getMeasures().getFailCount(), 0);
        assertTrue(good.getMeasures().getSolutionCount() > 0);
        Solver slow = model(n, s, false);
        assertEquals(good.getMeasures().getSolutionCount(), slow.getMeasures().getSolutionCount());
    }

    @Test(groups = "1m")
    public static void testAllDataStructure() {
        for (SetType ge : new SetType[]{SetType.BIPARTITESET,SetType.LINKED_LIST,SetType.BITSET}) {
            graphTypeEnv = ge;
            graphTypeKer = ge;
            System.out.println("env:" + ge + " ker :" + ge);
            smallTrees();
        }
    }

    @Test(groups="10s", timeOut=60000)
    public void directedTreeGenerateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        model.directedTree(g, 0).post();
        List<DirectedGraph> l1 = new ArrayList<>();
        while (model.getSolver().solve()) {
            l1.add(new DirectedGraph(g.getValue()));
        }
        // Generate solutions with checker
        Model model2 = new Model();
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = model2.digraphVar("g", LB2, UB2);
        Constraint cons = model2.directedTree(g2, 0);
        Constraint cons2 = model2.noCircuit(g2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE && cons2.isSatisfied() == ESat.TRUE) {
                boolean found = false;
                for (DirectedGraph graph : l1) {
                    if (g2.getValue().equals(graph)) {
                        found = true;
                    }
                }
                if (!found) {
                    System.out.println(g2.getValue());
                }
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }

    @Test(groups="10s", timeOut=60000)
    public void directedForestGenerateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        model.directedForest(g).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = model2.digraphVar("g", LB2, UB2);
        Constraint cons = model2.directedForest(g2);
        Constraint cons2 = model2.noCircuit(g2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE && cons2.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }
}
