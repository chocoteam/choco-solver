/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values.graph;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.graph.node.GraphLexNode;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the GraphNodeThenNeighbors graph search strategy.
 *
 * @author Dimitri Justeau-Allaire
 * @since 20/04/2021
 */
public class GraphNodeThenNeighborsTest {

    @Test(groups="1s", timeOut=60000)
    public void test() {
        // Generate solutions with default search
        Model model = new Model();
        int n = 7;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        while (model.getSolver().solve()) {}
        long count = model.getSolver().getSolutionCount();
        // Generate solutions with node then neighbors search
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        g = model.graphVar("g", LB, UB);
        model.getSolver().setSearch(Search.nodeThenNeighborsGraphVarSearch(g));
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }

    @Test(groups="10s", timeOut=60000)
    public void generateTest() {
        // Generate solutions with default search
        Model model = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        LB.addNode(1); LB.addNode(2); LB.addNode(3);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        model.nbConnectedComponents(g, model.intVar(1, 1)).post();
        model.nbEdges(g, model.intVar(2)).post();
        List<UndirectedGraph> sols = new ArrayList<>();
        while (model.getSolver().solve()) {
            sols.add(new UndirectedGraph(g.getValue()));
        }
        long count = model.getSolver().getSolutionCount();
        // Generate solutions with node then neighbors search
        model = new Model();
        LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        LB.addNode(1); LB.addNode(2); LB.addNode(3);
        UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        g = model.graphVar("g", LB, UB);
        model.nbConnectedComponents(g, model.intVar(1, 1)).post();
        model.nbEdges(g, model.intVar(2)).post();
        model.getSolver().setSearch(Search.nodeThenNeighborsGraphVarSearch(g));
//        model.getSolver().showDecisions();
        List<UndirectedGraph> sols2 = new ArrayList<>();
        while (model.getSolver().solve()) {
            sols2.add(new UndirectedGraph(g.getValue()));
//            System.out.println("***");
//            System.out.println(g.getValue());
        }
        for (UndirectedGraph gg : sols) {
            boolean b = false;
            for (UndirectedGraph ggg : sols2) {
                if (gg.equals(ggg)) {
                    b = true;
                }
            }
            if (!b) {
                System.out.println("--------");
                System.out.println(gg);
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }
}
