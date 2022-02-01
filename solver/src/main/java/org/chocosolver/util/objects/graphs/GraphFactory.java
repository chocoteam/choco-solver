/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.graphs;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

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
     * Return a stored undirected graph with a given set of nodes and edges.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param nodes list of nodes' indices to instantiate the graph with
     * @param adjacencyMatrix adjacency (boolean) matrix of directed edges to instantiate the graph with
     * @return a stored undirected graph with a nodes from `nodes` and edges from `edges`.
     */
    public static UndirectedGraph makeStoredUndirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, int[] nodes, boolean[][] adjacencyMatrix) {
        UndirectedGraph g = makeStoredUndirectedGraph(model, n, nodeSetType, edgeSetType);
        for (int i : nodes) {
            g.addNode(i);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (adjacencyMatrix[i][j]) {
                    g.addEdge(i, j);
                }
            }
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

    /**
     * Return a stored directed graph with a given set of nodes and edges, with edges represented by an adjacency matrix.
     * @param model The choco model
     * @param n the maximum number of nodes
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param nodes list of nodes' indices to instantiate the graph with
     * @param adjacencyMatrix adjacency (boolean) matrix of directed edges to instantiate the graph with
     * @return a stored directed graph with a nodes from `nodes` and edges from `edges`.
     */
    public static DirectedGraph makeStoredDirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, int[] nodes, boolean[][] adjacencyMatrix) {
        DirectedGraph g = makeStoredDirectedGraph(model, n, nodeSetType, edgeSetType);
        for (int i : nodes) {
            g.addNode(i);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adjacencyMatrix[i][j]) {
                    g.addEdge(i, j);
                }
            }
        }
        return g;
    }

    /**
     * Generate a random undirected graph (backtrackable) containing nbCC connected components.

     * @param model The Choco model (providing the backtracking environment).
     * @param n The max number of nodes.
     * @param nodeSetType set type for storing nodes
     * @param edgeSetType set type for storing edges
     * @param nbCC The number of connected components.
     * @param density The wanted density of the connected components
     * @param maxSizeCC The maximum size of the CCs.
     * @return A randomly generated undirected graph (backtrackable) containing nbCC connected components.
     */
    public static UndirectedGraph generateRandomUndirectedGraphFromNbCC(Model model, int n, SetType nodeSetType, SetType edgeSetType, int nbCC, double density, int maxSizeCC) {
        assert (nbCC <= n);
        int remaining = n;
        int next_node = 0;
        boolean[][] edges = new boolean[n][n];
        for (int cc = 0; cc < nbCC; cc++) {
            int size = ThreadLocalRandom.current().nextInt(1, Math.min(remaining - nbCC + cc + 1, maxSizeCC));
            remaining -= size;
            boolean[][] adj = generateRandomUndirectedAdjacencyMatrix(size, density);
            for (int i = 0; i < size; i++) {
                for (int j = i; j < size; j++) {
                    edges[i + next_node][j + next_node] = adj[i][j];
                }
            }
            next_node += size;
        }
        next_node = (next_node == 0) ? 1 : next_node;
        return makeStoredUndirectedGraph(model, n, nodeSetType, edgeSetType, IntStream.range(0, next_node).toArray(), edges);
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

    //***********************************************************************************
    // SUBGRAPHS
    //***********************************************************************************

    /**
     * Construct a backtrackable graph G' = (V', E') from another graph G = (V, E) such that:
     *          V' = E \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * with nodes a fixed set of nodes.
     *
     * @param model the model
     * @param graph the graph to construct a subgraph from
     * @param nodes
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public static UndirectedGraph makeNodeInducedSubgraph(Model model, UndirectedGraph graph, ISet nodes, boolean exclude) {
        return new UndirectedGraph(model, graph, nodes, exclude);
    }

    /**
     * Construct a backtrackable graph G' = (V', E') from another graph G = (V, E) such that:
     *          V' = E \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * with nodes a fixed set of nodes.
     *
     * /!\ Optimized for graph views instantiation: avoids unnecessary dynamic data structures /!\
     *
     * @param model the model
     * @param graph the graph to construct a subgraph from
     * @param UB If used to instantiate a graph view: the observed graph variable upper bound, used to detect whether
     *           a dynamic data structure is necessary for node and neighbors sets.
     * @param nodes
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public static UndirectedGraph makeNodeInducedSubgraph(Model model, UndirectedGraph graph, UndirectedGraph UB, ISet nodes, boolean exclude) {
        return new UndirectedGraph(model, graph, UB, nodes, exclude);
    }


    /**
     * Construct a backtrackable directed graph G' = (V', E') from another graph G = (V, E) such that:
     *          V' = E \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * with nodes a fixed set of nodes.
     *
     * @param model the model
     * @param graph the graph to construct a subgraph from
     * @param nodes
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public static DirectedGraph makeNodeInducedSubgraph(Model model, DirectedGraph graph, ISet nodes, boolean exclude) {
        return new DirectedGraph(model, graph, nodes, exclude);
    }

    /**
     * Construct a backtrackable directed graph G' = (V', E') from another graph G = (V, E) such that:
     *          V' = E \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * with nodes a fixed set of nodes.
     *
     * /!\ Optimized for graph views instantiation: avoids unnecessary dynamic data structures /!\
     *
     * @param model the model
     * @param graph the graph to construct a subgraph from
     * @param UB If used to instantiate a graph view: the observed graph variable upper bound, used to detect whether
     *           a dynamic data structure is necessary for node and neighbors sets.
     * @param nodes
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public static DirectedGraph makeNodeInducedSubgraph(Model model, DirectedGraph graph, DirectedGraph UB, ISet nodes, boolean exclude) {
        return new DirectedGraph(model, graph, UB, nodes, exclude);
    }

    /**
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param model the model
     * @param graph the graph to construct a subgraph from
     * @param edges the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude the type of subgraph to construct
     */
    public static UndirectedGraph makeEdgeInducedSubgraph(Model model, UndirectedGraph graph, int[][] edges, boolean exclude) {
        return new UndirectedGraph(model, graph, edges, exclude);
    }

    /**
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param model the model
     * @param graph the graph to construct a subgraph from
     * @param edges the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude the type of subgraph to construct
     */
    public static UndirectedGraph makeEdgeInducedSubgraph(Model model, UndirectedGraph graph, UndirectedGraph UB, int[][] edges, boolean exclude) {
        return new UndirectedGraph(model, graph, UB, edges, exclude);
    }

    /**
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param model the model
     * @param graph the graph to construct a subgraph from
     * @param edges the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public static DirectedGraph makeEdgeInducedSubgraph(Model model, DirectedGraph graph, int[][] edges, boolean exclude) {
        return new DirectedGraph(model, graph, edges, exclude);
    }

    /**
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * /!\ Optimized for graph views instantiation: avoids unnecessary dynamic data structures /!\
     *
     * @param model the model
     * @param graph the graph to construct a subgraph from
     * @param UB If used to instantiate a graph view: the observed graph variable upper bound, used to detect whether
     *           a dynamic data structure is necessary for node and neighbors sets.
     * @param edges the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public static DirectedGraph makeEdgeInducedSubgraph(Model model, DirectedGraph graph, DirectedGraph UB, int[][] edges, boolean exclude) {
        return new DirectedGraph(model, graph, UB, edges, exclude);
    }

    //***********************************************************************************
    // ARITHMETIC GRAPH VIEWS
    //***********************************************************************************

    /**
     * Construct an undirected graph G = (V, E) as the union of a set of undirected graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)}, i.e. :
     *      V = V_1 \cup ... \cup V_k (\cup = set union);
     *      E = E_1 \cup ... \cup E_k.
     * @param model the model
     * @param graphs the graphs to construct the union graph from
     */
    public static UndirectedGraph makeUnionGraph(Model model, UndirectedGraph... graphs) {
        return new UndirectedGraph(model, graphs);
    }

    /**
     * Construct an directed graph G = (V, E) as the union of a set of directed graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)}, i.e. :
     *      V = V_1 \cup ... \cup V_k (\cup = set union);
     *      E = E_1 \cup ... \cup E_k.
     * @param model the model
     * @param graphs the graphs to construct the union graph from
     */
    public static DirectedGraph makeUnionGraph(Model model, DirectedGraph... graphs) {
        return new DirectedGraph(model, graphs);
    }

    //***********************************************************************************
    // ADJACENCY MATRIX
    //***********************************************************************************

    /**
     * Randomly generate an undirected graph adjacency matrix from a given density.
     * @param n The number of nodes
     * @param density The wanted density
     * @return
     */
    public static boolean[][] generateRandomUndirectedAdjacencyMatrix(int n, double density) {
        assert (density >= 0 && density <= 1);
        int[] nodes = IntStream.range(0, n).toArray();
        boolean[][] edges = new boolean[n][n];
        for (int i : nodes) {
            for (int j : nodes) {
                double r = Math.random();
                if (r < density) {
                    edges[i][j] = true;
                    edges[j][i] = true;
                }
            }
        }
        return edges;
    }

}
