/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NbCliquesTest {

    @Test(groups="1s", timeOut=60000)
    public void instantiatedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0},
                        {3, 4}, {4, 5}, {5, 3},
                        {6, 7}, {7, 8}, {8, 6}
                }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0},
                        {3, 4}, {4, 5}, {5, 3},
                        {6, 7}, {7, 8}, {8, 6}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar nbCliques = model.intVar("nbCliques", 0, 10);
        model.nbCliques(g, nbCliques).post();
        while (model.getSolver().solve()) {
            Assert.assertEquals(nbCliques.getValue(), 3);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="1s", timeOut=60000)
    public void instantiatedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0}, {0, 3},
                        {4, 5}, {5, 6}, {6, 4},
                        {7, 8}, {8, 9}, {9, 7}
                }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0}, {0, 3},
                        {4, 5}, {5, 6}, {6, 4},
                        {7, 8}, {8, 9}, {9, 7}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar nbCliques = model.intVar("nbCliques", 0, 10);
        model.nbCliques(g, nbCliques).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedSuccessTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0},
                        {3, 4},
                        {6, 7}
                }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0},
                        {3, 4}, {4, 5}, {5, 3},
                        {6, 7}, {7, 8}, {8, 6}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar nbCliques = model.intVar("nbCliques", 0, 10);
        model.nbCliques(g, nbCliques).post();
        model.arithm(nbCliques, "<=", 4).post();
        IntVar nbEdges = model.intVar("nbEdges", 0, 10);
        model.nbEdges(g, nbEdges).post();
        while (model.getSolver().solve()) {
            Assert.assertTrue(nbCliques.getValue() <= 4);
            Assert.assertTrue(nbEdges.getValue() == 7 || nbEdges.getValue() == 9);
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void constrainedFailTest() {
        Model model = new Model();
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0},
                        {3, 4},
                        {6, 7}
                }
        );
        UndirectedGraph UB = GraphFactory.makeStoredUndirectedGraph(
                model, 10, SetType.BITSET, SetType.BITSET,
                new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new int[][]{
                        {0, 1}, {1, 2}, {2, 0},
                        {3, 4}, {4, 5}, {5, 3},
                        {6, 7}, {7, 8}, {8, 6}
                }
        );
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar nbCliques = model.intVar("nbCliques", 0, 10);
        model.nbCliques(g, nbCliques).post();
        model.arithm(nbCliques, "<=", 4).post();
        IntVar nbEdges = model.intVar("nbEdges", 0, 10);
        model.nbEdges(g, nbEdges).post();
        model.arithm(nbEdges, "<=", 6).post();
        model.getSolver().solve();
        Assert.assertEquals(model.getSolver().getSolutionCount(), 0);
    }

    @Test(groups="10s", timeOut=60000)
    public void generateTest() {
        // Generate solutions with filtering
        Model model = new Model();
        int n = 6;
        int nbCliquesLB = 0;
        int nbCliquesUB = 4;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = model.graphVar("g", LB, UB);
        IntVar nbCliques = model.intVar("nbCliques", nbCliquesLB, nbCliquesUB);
        model.nbCliques(g, nbCliques).post();
        while (model.getSolver().solve()) {}
        // Generate solutions with checker
        Model model2 = new Model();
        UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g2 = model2.graphVar("g", LB2, UB2);
        IntVar nbCliques2 = model2.intVar("nbCliques2", nbCliquesLB, nbCliquesUB);
        Constraint cons = model2.nbCliques(g2, nbCliques2);
        int count = 0;
        while (model2.getSolver().solve()) {
            if (cons.isSatisfied() == ESat.TRUE) {
                count++;
            }
        }
        Assert.assertEquals(model.getSolver().getSolutionCount(), count);
    }
}
