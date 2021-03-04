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
     * Return an EMPTY stored undirected graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param arcSetType set type for storing arcs
     * @return an empty stored undirected graph.
     */
    public static UndirectedGraph makeStoredUndirectedGraph(Model model, int n, SetType nodeSetType, SetType arcSetType) {
        return new UndirectedGraph(model, n, nodeSetType, arcSetType, false);
    }

    /**
     * Return an EMPTY stored directed graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param arcSetType set type for storing arcs
     * @return an empty stored directed graph.
     */
    public static DirectedGraph makeStoredDirectedGraph(Model model, int n, SetType nodeSetType, SetType arcSetType) {
        return new DirectedGraph(model, n, nodeSetType, arcSetType, false);
    }

    /**
     * Return a stored undirected graph with all nodes (no arcs) from 0 to n-1.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param arcSetType set type for storing arcs
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a stored undirected graph with all nodes (no arcs) from 0 to n-1.
     */
    public static UndirectedGraph makeStoredAllNodesUndirectedGraph(Model model, int n, SetType nodeSetType, SetType arcSetType, boolean allNodesFixed) {
        UndirectedGraph g = new UndirectedGraph(model, n, nodeSetType, arcSetType, allNodesFixed);
        if (!allNodesFixed) {
            for (int i = 0; i < n; i++) {
                g.addNode(i);
            }
        }
        return g;
    }

    /**
     * Return a stored directed graph with all nodes (no arcs) from 0 to n-1.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param arcSetType set type for storing arcs
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a stored directed graph with all nodes (no arcs) from 0 to n-1.
     */
    public static DirectedGraph makeStoredAllNodesDirectedGraph(Model model, int n, SetType nodeSetType, SetType arcSetType, boolean allNodesFixed) {
        DirectedGraph g = new DirectedGraph(model, n, nodeSetType, arcSetType, allNodesFixed);
        if (!allNodesFixed) {
            for (int i = 0; i < n; i++) {
                g.addNode(i);
            }
        }
        return g;
    }

    /**
     * Return a complete (all nodes and all arcs, no loops) stored undirected graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param arcSetType set type for storing arcs
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a complete (all nodes, all arcs, no loops) stored undirected graph.
     */
    public static UndirectedGraph makeCompleteStoredUndirectedGraph(Model model, int n, SetType nodeSetType, SetType arcSetType, boolean allNodesFixed) {
        UndirectedGraph g = makeStoredAllNodesUndirectedGraph(model, n, nodeSetType, arcSetType, allNodesFixed);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.addEdge(i, j);
            }
        }
        return g;
    }

    /**
     * Return a complete (all nodes and all arcs, no loops) stored directed graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param arcSetType set type for storing arcs
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a complete (all nodes, all arcs, no loops) stored directed graph.
     */
    public static DirectedGraph makeCompleteStoredDirectedGraph(Model model, int n, SetType nodeSetType, SetType arcSetType, boolean allNodesFixed) {
        DirectedGraph g = makeStoredAllNodesDirectedGraph(model, n, nodeSetType, arcSetType, allNodesFixed);
        if (!allNodesFixed) {
            for (int i = 0; i < n; i++) {
                g.addNode(i);
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.addEdge(i, j);
                g.addEdge(j, i);
            }
        }
        return g;
    }
}
