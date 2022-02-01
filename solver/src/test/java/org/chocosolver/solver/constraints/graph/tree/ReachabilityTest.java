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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for the reachability graph constraint
 * @author Dimitri Justeau-Allaire
 * @since 22/03/2021
 */
public class ReachabilityTest {

    SetType setType = SetType.BITSET;

    @Test(groups="1s", timeOut=60000)
    public void instantiatedSuccessTest() {
        Model model = new Model();
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][]{ {0, 1}, {1, 2}, {2, 4}, {4, 5}, {5, 3} };
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, 10, setType, setType, nodes , edges);
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(model, 10, setType, setType, nodes, edges);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        model.reachability(g, 0).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedFailTest() {
        Model model = new Model();
        int[] nodes = new int[] {0, 1, 2, 3, 4, 5};
        int[][] edges = new int[][]{ {0, 1}, {2, 4}, {4, 5}, {5, 3} };
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, 10, setType, setType, nodes , edges);
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(model, 10, setType, setType, nodes, edges);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        Constraint c =  model.reachability(g, 0);
        c.post();
        Assert.assertEquals(c.getPropagator(0).isEntailed(), ESat.FALSE);
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedSuccessTest() {
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(
                model, n, setType, setType,
                new int[] {0, 1, 2, 3},
                new int[][]{}
        );
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        SetVar succOf5 = model.graphSuccessorsSetView(g, 3);
        model.member(2, succOf5).post();
        model.nbEdges(g, model.intVar(0, 10)).post();
        Constraint c =  model.reachability(g, 0);
        c.post();
        Assert.assertEquals(c.getPropagator(0).isEntailed(), ESat.UNDEFINED);
        while (model.getSolver().solve()) {}
        Assert.assertTrue(model.getSolver().getSolutionCount() > 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedFailTest() {
        Model model = new Model();
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, setType, setType,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][]{ {0, 1} }
        );
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, 10, setType, setType, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        model.reachability(g, 1).post();
        model.nbEdges(g, model.intVar(4)).post();
        model.getSolver().findAllSolutions();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void generateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 5;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, setType, setType);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, setType, setType, false);
        for (int i = 1; i < n; i++) {
            UB.removeEdge(0, i);
        }
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        model.reachability(g, 2).post();
        List<DirectedGraph> l1 = new ArrayList<>();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model2, n, setType, setType);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(model2, n, setType, setType, false);
        for (int i = 1; i < n; i++) {
            UB2.removeEdge(0, i);
        }
        DirectedGraphVar g2 = model2.digraphVar("g2", LB2, UB2);
        Constraint cons = model2.reachability(g2, 2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }

}
