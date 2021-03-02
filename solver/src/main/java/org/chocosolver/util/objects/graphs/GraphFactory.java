/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.graphs;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * Factory for creating graph data structures.
 * @author Dimitri Justeau-Allaire
 * @since 02/03/2021
 */
public class GraphFactory {

    /**
     * Return an empty stored undirected graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param arcSetType set type for storing arcs
     * @return an empty stored undirected graph.
     */
    public static UndirectedGraph makeEmptyStoredGraph(Model model, int n, SetType nodeSetType, SetType arcSetType) {
        return new UndirectedGraph(model, n, nodeSetType, arcSetType, false);
    }

    /**
     * Return a complete (all nodes and all arcs, no loops) stored undirected graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param arcSetType set type for storing arcs
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return an complete (all nodes, all arcs, no loops) stored undirected graph.
     */
    public static UndirectedGraph makeCompleteStoredGraph(Model model, int n, SetType nodeSetType, SetType arcSetType, boolean allNodesFixed) {
        UndirectedGraph g = new UndirectedGraph(model, n, nodeSetType, arcSetType, allNodesFixed);
        if (!allNodesFixed) {
            for (int i = 0; i < n; i++) {
                g.addNode(i);
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.addEdge(i, j);
            }
        }
        return g;
    }
}
