/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.graph;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test class for edge-induced subgraph view when edges are excluded
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestEdgeExcludedSubgraphView {

    @Test(groups = "1s", timeOut = 60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1}, {1, 2}, {1, 3}, {1, 5},
                {2, 5}
        };
        UndirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, true);
        ISet[] neigh = UndirectedGraph.edgesArrayToEdgesSets(g.getNbMaxNodes(), edges);
        Assert.assertEquals(g2.getMandatoryNodes().size(), 0);
        Assert.assertEquals(g2.getPotentialNodes().size(), 6);
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            for (int i = 0; i < g.getNbMaxNodes(); i++) {
                for (int j = 0; j < g.getNbMaxNodes(); j++) {
                    if (g.getValue().containsEdge(i, j)) {
                        Assert.assertEquals(!neigh[i].contains(j), g2.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        int n = 6;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        int[][] edges = new int[][] {
                {0, 1},
                {1, 2}, {1, 3}, {1, 5},
                {2, 5}
        };
        UndirectedGraphVar g2 = m.edgeInducedSubgraphView(g, edges, true);
        ISet[] neigh = UndirectedGraph.edgesArrayToEdgesSets(g.getNbMaxNodes(), edges);
        SetVar nodesG = m.graphNodeSetView(g);
        m.member(3, nodesG).post();
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.connected(g2).post();
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            for (int i = 0; i < g.getNbMaxNodes(); i++) {
                for (int j = 0; j < g.getNbMaxNodes(); j++) {
                    if (g.getValue().containsEdge(i, j)) {
                        Assert.assertEquals(!neigh[i].contains(j), g2.getValue().containsEdge(i, j));
                    }
                }
            }
        }
    }
}
