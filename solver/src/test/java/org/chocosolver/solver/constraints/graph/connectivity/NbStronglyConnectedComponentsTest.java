/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.connectivity;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the `nbStronglyConnectedComponents` graph constraint.
 * This class is also a test for the `stronglyConnected` graph constraint, as both rely on the same
 * propagator. If in the future a dedicated propagator is implemented for `stronglyConnected` a dedicated
 * test class should also be implemented.
 * @author Dimitri Justeau-Allaire
 * @since 18/03/2021
 */
public class NbStronglyConnectedComponentsTest {

    @Test(groups="1s", timeOut=60000)
    public void instantiatedGraphSuccessTest() {
        // Test a graph with 0 strongly connected components, i.e. empty graph
        Model model = new Model();
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, 10, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(model, 10, SetType.BITSET, SetType.BITSET);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        IntVar nbSCC = model.intVar("nbSCC", 0, 10);
        model.nbStronglyConnectedComponents(g, nbSCC).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbSCC.getValue(), 0);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // Test a graph with 1 node (should be one SCC)
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(model, 10, SetType.BITSET, SetType.BITSET);
        LB.addNode(0);
        UB = GraphFactory.makeStoredDirectedGraph(model, 10, SetType.BITSET, SetType.BITSET);
        UB.addNode(0);
        g = model.digraphVar("g", LB, UB);
        nbSCC = model.intVar("nbSCC", 0, 10);
        model.nbStronglyConnectedComponents(g, nbSCC).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbSCC.getValue(), 1);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // Test a graph with 1 strongly connected component
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 0}, {2, 5} }
        );
        UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 0}, {2, 5} }
        );
        g = model.digraphVar("g", LB, UB);
        nbSCC = model.intVar("nbSCC", 0, 10);
        model.nbStronglyConnectedComponents(g, nbSCC).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbSCC.getValue(), 1);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // Test a graph with 2 strongly connected components
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 0}, {3, 4}, {4, 5}, {5, 4} }
        );
        UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 0}, {3, 4}, {4, 5}, {5, 4} }
        );
        g = model.digraphVar("g", LB, UB);
        nbSCC = model.intVar("nbSCC", 0, 10);
        model.nbStronglyConnectedComponents(g, nbSCC).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbSCC.getValue(), 2);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
        // Test a graph with 3 strongly connected components
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 0}, {2, 3}, {3, 2}, {4, 5}, {5, 4} }
        );
        UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 0}, {2, 3}, {3, 2}, {4, 5}, {5, 4} }
        );
        g = model.digraphVar("g", LB, UB);
        nbSCC = model.intVar("nbSCC", 0, 10);
        model.nbStronglyConnectedComponents(g, nbSCC).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbSCC.getValue(), 3);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedGraphFailTest() {
        // Test a graph with 1 strongly connected component but expected between 3 and 4
        Model model = new Model();
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 0}, {2, 5} }
        );
        DirectedGraph UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 0}, {2, 5} }
        );
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        IntVar nbSCC = model.intVar("nbSCC", 3, 4);
        model.nbStronglyConnectedComponents(g, nbSCC).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
        // Test a graph with 0 strongly connected components but expected strongly connected
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5} }
        );
        UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5} }
        );
        g = model.digraphVar("g", LB, UB);
        model.stronglyConnected(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
        // Test a graph with 3 strongly connected components but expected 1
        model = new Model();
        LB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 0}, {2, 3}, {3, 2}, {4, 5}, {5, 4} }
        );
        UB = GraphFactory.makeStoredDirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5},
                new int[][] { {0, 1}, {1, 0}, {2, 3}, {3, 2}, {4, 5}, {5, 4} }
        );
        g = model.digraphVar("g", LB, UB);
        model.stronglyConnected(g).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void emptyGraphTest() {
        // A number of strongly connected components forced to zero must result in an empty graph
        Model model = new Model();
        int n = 6;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        IntVar nbSCC = model.intVar("nbSCC", 0);
        model.nbStronglyConnectedComponents(g, nbSCC).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(g.getValue().getNodes().size(), 0);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="10s", timeOut=60000)
    public void constrainedSuccessTest() {
        Model model = new Model();
        int n = 6;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UB.removeEdge(0, 1); UB.removeEdge(0, 3); UB.removeEdge(0, 5);
        UB.removeEdge(1, 2); UB.removeEdge(1, 4);
        UB.removeEdge(3, 4); UB.removeEdge(3, 5);
        UB.removeEdge(4, 5);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        IntVar nbSCC = model.intVar("nbSCC", 0, 4);
        model.nbStronglyConnectedComponents(g, nbSCC).post();
        IntVar nbEdges = model.intVar("nbEdges", 0, 8);
        model.nbEdges(g, nbEdges).post();
        SetVar nodeSet = model.graphNodeSetView(g);
        model.member(3, nodeSet).post();
        model.member(5, nodeSet).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(nodeSet.getValue().contains(3));
            Assert.assertTrue(nodeSet.getValue().contains(5));
            Assert.assertTrue(nbEdges.getValue() >= 0 && nbEdges.getValue() <= 8);
            Assert.assertTrue(nbSCC.getValue() > 0 && nbSCC.getValue() <= 4);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedFailTest() {
        Model model = new Model();
        int n = 6;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UB.removeEdge(0, 1); UB.removeEdge(0, 3); UB.removeEdge(0, 5);
        UB.removeEdge(1, 2); UB.removeEdge(1, 4);
        UB.removeEdge(3, 4); UB.removeEdge(3, 5);
        UB.removeEdge(4, 5);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        model.stronglyConnected(g).post();
        IntVar nbEdges = model.intVar("nbEdges", 25, 36);
        model.nbEdges(g, nbEdges).post();
        SetVar nodeSet = model.graphNodeSetView(g);
        model.member(3, nodeSet).post();
        model.member(5, nodeSet).post();
        while (model.getSolver().solve()) {}
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void generateTest() {
        // Generate solutions with filtering - with constraints
        Model model = new Model();
        int n = 5;
        int nbSCCLB = 0;
        int nbSCCUB = 3;
        DirectedGraph LB = GraphFactory.makeStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB = GraphFactory.makeCompleteStoredDirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g = model.digraphVar("g", LB, UB);
        model.nbStronglyConnectedComponents(g, model.intVar(nbSCCLB, nbSCCUB)).post();
        model.nbEdges(g, model.intVar(4)).post();
        SetVar nodeSet = model.graphNodeSetView(g);
        model.member(3, nodeSet).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        DirectedGraph LB2 = GraphFactory.makeStoredDirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        DirectedGraph UB2 = GraphFactory.makeCompleteStoredDirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        DirectedGraphVar g2 = model2.digraphVar("g", LB2, UB2);
        Constraint consNbEdges = model2.nbEdges(g2, model2.intVar(4));
        SetVar nodeSet2 = model2.graphNodeSetView(g2);
        Constraint consMember = model2.member(3, nodeSet2);
        Constraint consNSCC = model2.nbStronglyConnectedComponents(g2, model2.intVar(nbSCCLB, nbSCCUB));
        int count = 0;
        while (model2.getSolver().solve()) {
            if (consNSCC.isSatisfied() == ESat.TRUE
                    && consNbEdges.isSatisfied() == ESat.TRUE
                    && consMember.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }
}
