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

    //***********************************************************************************
    // STORED GRAPHS
    //***********************************************************************************

    /**
     * Return an EMPTY stored undirected graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @return an empty stored undirected graph.
     */
    public static UndirectedGraph makeStoredUndirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType) {
        return new UndirectedGraph(model, n, nodeSetType, edgeSetType, false);
    }

    /**
     * Return an EMPTY stored directed graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @return an empty stored directed graph.
     */
    public static DirectedGraph makeStoredDirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType) {
        return new DirectedGraph(model, n, nodeSetType, edgeSetType, false);
    }

    /**
     * Return a stored undirected graph with all nodes (no edges) from 0 to n-1.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a stored undirected graph with all nodes (no edges) from 0 to n-1.
     */
    public static UndirectedGraph makeStoredAllNodesUndirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, boolean allNodesFixed) {
        UndirectedGraph g = new UndirectedGraph(model, n, nodeSetType, edgeSetType, allNodesFixed);
        if (!allNodesFixed) {
            for (int i = 0; i < n; i++) {
                g.addNode(i);
            }
        }
        return g;
    }

    /**
     * Return a stored directed graph with all nodes (no edges) from 0 to n-1.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a stored directed graph with all nodes (no edges) from 0 to n-1.
     */
    public static DirectedGraph makeStoredAllNodesDirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, boolean allNodesFixed) {
        DirectedGraph g = new DirectedGraph(model, n, nodeSetType, edgeSetType, allNodesFixed);
        if (!allNodesFixed) {
            for (int i = 0; i < n; i++) {
                g.addNode(i);
            }
        }
        return g;
    }

    /**
     * Return a complete (all nodes and all edges, no loops) stored undirected graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a complete (all nodes, all edges, no loops) stored undirected graph.
     */
    public static UndirectedGraph makeCompleteStoredUndirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, boolean allNodesFixed) {
        UndirectedGraph g = makeStoredAllNodesUndirectedGraph(model, n, nodeSetType, edgeSetType, allNodesFixed);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.addEdge(i, j);
            }
        }
        return g;
    }

    /**
     * Return a complete (all nodes and all edges, no loops) stored directed graph.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a complete (all nodes, all edges, no loops) stored directed graph.
     */
    public static DirectedGraph makeCompleteStoredDirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, boolean allNodesFixed) {
        DirectedGraph g = makeStoredAllNodesDirectedGraph(model, n, nodeSetType, edgeSetType, allNodesFixed);
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



    /**
     * Return a stored undirected graph with a given set of nodes and edges.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param nodes list of nodes' indices to instantiate the graph with
     * @param edges list of edges (in the form { {start, end}, ...} to instantiate the graph with
     * @return a stored undirected graph with a nodes from `nodes` and edges from `edges`.
     */
    public static UndirectedGraph makeStoredUndirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, int[] nodes, int[][] edges) {
        UndirectedGraph g = makeStoredUndirectedGraph(model, n, nodeSetType, edgeSetType);
        for (int i : nodes) {
            g.addNode(i);
        }
        for (int[] e : edges) {
            assert e.length == 2;
            g.addEdge(e[0], e[1]);
        }
        return g;
    }

    /**
     * Return a stored directed graph with a given set of nodes and edges.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param nodes list of nodes' indices to instantiate the graph with
     * @param edges list of directed edges (in the form { {start, end}, ...} to instantiate the graph with
     * @return a stored directed graph with a nodes from `nodes` and edges from `edges`.
     */
    public static DirectedGraph makeStoredDirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, int[] nodes, int[][] edges) {
        DirectedGraph g = makeStoredDirectedGraph(model, n, nodeSetType, edgeSetType);
        for (int i : nodes) {
            g.addNode(i);
        }
        for (int[] e : edges) {
            assert e.length == 2;
            g.addEdge(e[0], e[1]);
        }
        return g;
    }

    //***********************************************************************************
    // UNSTORED GRAPHS
    //***********************************************************************************

    /**
     * Return an EMPTY undirected graph.
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @return an empty (non backtrackable) undirected graph.
     */
    public static UndirectedGraph makeUndirectedGraph(int n, SetType nodeSetType, SetType edgeSetType) {
        return new UndirectedGraph(n, nodeSetType, edgeSetType, false);
    }

    /**
     * Return an EMPTY directed graph.
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @return an empty (non backtrackable) directed graph.
     */
    public static DirectedGraph makeDirectedGraph(int n, SetType nodeSetType, SetType edgeSetType) {
        return new DirectedGraph(n, nodeSetType, edgeSetType, false);
    }

    /**
     * Return a undirected graph with all nodes (no edges) from 0 to n-1.
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a (non backtrackable) undirected graph with all nodes (no edges) from 0 to n-1.
     */
    public static UndirectedGraph makeAllNodesUndirectedGraph(int n, SetType nodeSetType, SetType edgeSetType, boolean allNodesFixed) {
        UndirectedGraph g = new UndirectedGraph(n, nodeSetType, edgeSetType, allNodesFixed);
        if (!allNodesFixed) {
            for (int i = 0; i < n; i++) {
                g.addNode(i);
            }
        }
        return g;
    }

    /**
     * Return a directed graph with all nodes (no edges) from 0 to n-1.
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a (non backtrackable) directed graph with all nodes (no edges) from 0 to n-1.
     */
    public static DirectedGraph makeAllNodesDirectedGraph(int n, SetType nodeSetType, SetType edgeSetType, boolean allNodesFixed) {
        DirectedGraph g = new DirectedGraph(n, nodeSetType, edgeSetType, allNodesFixed);
        if (!allNodesFixed) {
            for (int i = 0; i < n; i++) {
                g.addNode(i);
            }
        }
        return g;
    }

    /**
     * Return a complete (all nodes and all edges, no loops) undirected graph.
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a complete (all nodes, all edges, no loops) non backtrackable undirected graph.
     */
    public static UndirectedGraph makeCompleteUndirectedGraph(int n, SetType nodeSetType, SetType edgeSetType, boolean allNodesFixed) {
        UndirectedGraph g = makeAllNodesUndirectedGraph(n, nodeSetType, edgeSetType, allNodesFixed);
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                g.addEdge(i, j);
            }
        }
        return g;
    }

    /**
     * Return a complete (all nodes and all edges, no loops) directed graph.
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param allNodesFixed if true all nodes are fixed to the graph (cannod be removed)
     * @return a complete (all nodes, all edges, no loops) non backtrackable directed graph.
     */
    public static DirectedGraph makeCompleteDirectedGraph(int n, SetType nodeSetType, SetType edgeSetType, boolean allNodesFixed) {
        DirectedGraph g = makeAllNodesDirectedGraph(n, nodeSetType, edgeSetType, allNodesFixed);
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



    /**
     * Return an undirected graph with a given set of nodes and edges.
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param nodes list of nodes' indices to instantiate the graph with
     * @param edges list of edges (in the form { {start, end}, ...} to instantiate the graph with
     * @return a (non backtrackable) undirected graph with a nodes from `nodes` and edges from `edges`.
     */
    public static UndirectedGraph makeUndirectedGraph(int n, SetType nodeSetType, SetType edgeSetType, int[] nodes, int[][] edges) {
        UndirectedGraph g = makeUndirectedGraph(n, nodeSetType, edgeSetType);
        for (int i : nodes) {
            g.addNode(i);
        }
        for (int[] e : edges) {
            assert e.length == 2;
            g.addEdge(e[0], e[1]);
        }
        return g;
    }

    /**
     * Return a directed graph with a given set of nodes and edges.
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param nodes list of nodes' indices to instantiate the graph with
     * @param edges list of directed edges (in the form { {start, end}, ...} to instantiate the graph with
     * @return a (non backtrackable) directed graph with a nodes from `nodes` and edges from `edges`.
     */
    public static DirectedGraph makeDirectedGraph(int n, SetType nodeSetType, SetType edgeSetType, int[] nodes, int[][] edges) {
        DirectedGraph g = makeDirectedGraph(n, nodeSetType, edgeSetType);
        for (int i : nodes) {
            g.addNode(i);
        }
        for (int[] e : edges) {
            assert e.length == 2;
            g.addEdge(e[0], e[1]);
        }
        return g;
    }
}
