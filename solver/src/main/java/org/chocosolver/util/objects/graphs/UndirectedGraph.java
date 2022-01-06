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
import org.chocosolver.util.objects.setDataStructures.*;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetDifference;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetIntersection;
import org.chocosolver.util.objects.setDataStructures.dynamic.SetUnion;

import java.util.stream.IntStream;

/**
 * Specific implementation of an undirected graph
 *
 * @author Jean-Guillaume Fages, Xavier Lorca
 */
public class UndirectedGraph implements IGraph {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final ISet[] neighbors;
    private ISet nodes;
    private final int n;
    private final SetType edgeSetType;
    private final SetType nodeSetType;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates an empty backtrable undirected graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param model   model providing the backtracking environment
     * @param n        max number of nodes
     * @param nodeSetType     data structure storing for nodes
     * @param edgeSetType     data structure storing for node neighbors
     * @param allNodes true iff all nodes will always remain in the graph
     */
    public UndirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, boolean allNodes) {
        this.edgeSetType = edgeSetType;
        this.nodeSetType = nodeSetType;
        this.n = n;
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = SetFactory.makeStoredSet(this.edgeSetType, 0, model);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeStoredSet(this.nodeSetType, 0, model);
        }
    }

    /**
     * Creates an empty backtrable undirected graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * Nodes are stored as BITSET
     *
     * @param model   model providing the backtracking environment
     * @param n        max number of nodes
     * @param edgeSetType     data structure storing for node neighbors
     * @param allNodes true iff all nodes will always remain in the graph
     */
    public UndirectedGraph(Model model, int n, SetType edgeSetType, boolean allNodes) {
        this(model, n, SetType.BITSET, edgeSetType, allNodes);
    }

    /**
     * Creates an empty (non-backtrackable) undirected graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param n        max number of nodes
     * @param nodeSetType     data structure storing for nodes
     * @param edgeSetType     data structure used for storing node neighbors
     * @param allNodes true iff all nodes will always remain in the graph
     */
    public UndirectedGraph(int n, SetType nodeSetType, SetType edgeSetType, boolean allNodes) {
        this.edgeSetType = edgeSetType;
        this.nodeSetType = nodeSetType;
        this.n = n;
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = SetFactory.makeSet(edgeSetType, 0);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeSet(nodeSetType, 0);
        }
    }

    /**
     * Creates an empty (non-backtrackable) undirected graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * Nodes are stored as BITSET
     *
     * @param n        max number of nodes
     * @param edgeSetType     data structure used for storing node neighbors
     * @param allNodes true iff all nodes will always remain in the graph
     */
    public UndirectedGraph(int n, SetType edgeSetType, boolean allNodes) {
        this(n, SetType.BITSET, edgeSetType, allNodes);
    }

    /**
     * Construct a read-only copy of another graph
     * @param g the graph to copy
     */
    public UndirectedGraph(UndirectedGraph g) {
        this.nodeSetType = SetType.FIXED_ARRAY;
        this.edgeSetType = SetType.FIXED_ARRAY;
        this.n = g.getNbMaxNodes();
        this.nodes = SetFactory.makeConstantSet(g.getNodes().toArray());
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = SetFactory.makeConstantSet(g.getNeighborsOf(i).toArray());
        }
    }

    // Subgraph constructors

    /**
     * CONSTRUCTOR FOR BACKTRACKABLE NODE INDUCED SUBGRAPHS:
     *
     * Construct a backtrackable graph G' = (V', E') from another graph G = (V, E) such that:
     *          V' = V \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * with nodes a fixed set of nodes.
     *
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param nodes the set of nodes to construct the subgraph from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public UndirectedGraph(Model model, UndirectedGraph g, ISet nodes, boolean exclude) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        if (exclude) {
            this.nodes = new SetDifference(model, g.getNodes(), nodes);
        } else {
            this.nodes = new SetIntersection(model, g.getNodes(), nodes);
        }
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            if (exclude) {
                neighbors[i] = new SetDifference(model, g.getNeighborsOf(i), nodes);
            } else {
                neighbors[i] = new SetIntersection(model, g.getNeighborsOf(i), nodes);
            }
        }
    }

    /**
     * CONSTRUCTOR FOR BACKTRACKABLE NODE INDUCED SUBGRAPHS:
     *
     * /!\ Optimized for graph views instantiation: avoids unnecessary dynamic data structures /!\
     *
     * Construct a backtrackable graph G' = (V', E') from another graph G = (V, E) such that:
     *          V' = V \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * with nodes a fixed set of nodes.
     *
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param UB If used to instantiate a graph view: the observed graph variable upper bound, used to detect whether
     *           a dynamic data structure is necessary for node and neighbors sets.
     * @param nodes the set of nodes to construct the subgraph from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public UndirectedGraph(Model model, UndirectedGraph g, UndirectedGraph UB, ISet nodes, boolean exclude) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        boolean needDynamic = false;
        ISet neighNeeded = UB.nodes;
        if (exclude) {
            for (int i : nodes) {
                if (UB.getNodes().contains(i)) {
                    needDynamic = true;
                    this.nodes = new SetDifference(model, g.getNodes(), nodes);
                    neighNeeded = new SetDifference(model, UB.getNodes(), nodes);
                    break;
                }
            }
        } else {
            for (int i : UB.getNodes()) {
                if (!nodes.contains(i)) {
                    needDynamic = true;
                    SetType nodeSetType = g.getNodeSetType();
                    this.nodes = new SetIntersection(model, nodeSetType, 0, g.getNodes(), nodes);
                    neighNeeded = new SetIntersection(model, nodeSetType, 0, UB.getNodes(), nodes);
                    break;
                }
            }
        }
        if (!needDynamic) {
            this.nodes = g.nodes;
        }
        neighbors = new ISet[n];
        for (int i : neighNeeded) {
            needDynamic = false;
            if (exclude) {
                for (int j : nodes) {
                    if (UB.getNeighborsOf(i).contains(j)) {
                        needDynamic = true;
                        neighbors[i] = new SetDifference(model, g.getNeighborsOf(i), nodes);
                        break;
                    }
                }
            } else {
                for (int j : UB.getNeighborsOf(i)) {
                    if (!nodes.contains(j)) {
                        needDynamic = true;
                        SetType edgeSetType = g.getEdgeSetType();
                        neighbors[i] = new SetIntersection(model, edgeSetType, 0, g.getNeighborsOf(i), nodes);
                        break;
                    }
                }
            }
            if (!needDynamic) {
                neighbors[i] = g.neighbors[i];
            }
        }
    }

    /**
     * GENERIC CONSTRUCTOR FOR BACKTRACKABLE EDGE INDUCED SUBGRAPHS:
     *
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param edges the set of edges to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public UndirectedGraph(Model model, UndirectedGraph g, ISet[] edges, boolean exclude) {
        assert edges.length == g.getNbMaxNodes();
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            if (exclude) {
                neighbors[i] = new SetDifference(model, g.getNeighborsOf(i), edges[i]);
            } else {
                neighbors[i] = new SetIntersection(model, g.getNeighborsOf(i), edges[i]);
            }
        }
        this.nodes = new SetUnion(model, neighbors);
    }

    /**
     * GENERIC CONSTRUCTOR FOR BACKTRACKABLE EDGE INDUCED SUBGRAPHS:
     *
     * /!\ Optimized for graph views instantiation: avoids unnecessary dynamic data structures /!\
     *
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param UB If used to instantiate a graph view: the observed graph variable upper bound, used to detect whether
     *           a dynamic data structure is necessary for node and neighbors sets.
     * @param edges the set of edges to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public UndirectedGraph(Model model, UndirectedGraph g, UndirectedGraph UB, ISet[] edges, boolean exclude) {
        assert edges.length == g.getNbMaxNodes();
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        neighbors = new ISet[n];
        boolean nodeNeedDynamic = false;
        for (int i = 0; i < n; i++) {
            boolean needDynamic = false;
            if (exclude) {
                for (int j : edges[i]) {
                    if (UB.getNeighborsOf(i).contains(j)) {
                        needDynamic = true;
                        nodeNeedDynamic = true;
                        neighbors[i] = new SetDifference(model, g.getNeighborsOf(i), edges[i]);
                        break;
                    }
                }
            } else {
                for (int j : UB.getNeighborsOf(i)) {
                    if (!edges[i].contains(j)) {
                        needDynamic = true;
                        nodeNeedDynamic = true;
                        neighbors[i] = new SetIntersection(model, g.getEdgeSetType(), 0, g.getNeighborsOf(i), edges[i]);
                        break;
                    }
                }
            }
            if (!needDynamic) {
                neighbors[i] = g.neighbors[i];
            }
        }
        if (nodeNeedDynamic) {
            this.nodes = new SetUnion(model, g.nodeSetType, 0, neighbors);
        } else {
            this.nodes = g.nodes;
        }
    }

    /**
     * GENERIC CONSTRUCTOR FOR BACKTRACKABLE EDGE INDUCED SUBGRAPHS:
     *
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param edges the set of edges (array of couples) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public UndirectedGraph(Model model, UndirectedGraph g, int[][] edges, boolean exclude) {
        this(model, g, edgesArrayToEdgesSets(g.getNbMaxNodes(), edges), exclude);
    }

    public UndirectedGraph(Model model, UndirectedGraph g, UndirectedGraph UB, int[][] edges, boolean exclude) {
        this(model, g, UB, edgesArrayToEdgesSets(g.getNbMaxNodes(), edges), exclude);
    }

    // Graph arithmetic constructors

    /**
     * Construct an undirected graph G = (V, E) as the union of a set of undirected graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)}, i.e. :
     *      V = V_1 \cup ... \cup V_k (\cup = set union);
     *      E = E_1 \cup ... \cup E_k.
     * @param model the model
     * @param graphs the graphs to construct the union graph from
     */
    public UndirectedGraph(Model model, UndirectedGraph... graphs) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = IntStream.range(0, graphs.length).map(i -> graphs[i].getNbMaxNodes()).max().getAsInt();
        ISet[] nodeSets = new ISet[graphs.length];
        for (int i = 0; i < graphs.length; i++) {
            nodeSets[i] = graphs[i].getNodes();
        }
        this.nodes = new SetUnion(model, nodeSets);
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            ISet[] neighSet = new ISet[graphs.length];
            for (int j = 0; j < graphs.length; j++) {
                neighSet[j] = graphs[j].getNeighborsOf(i);
            }
            neighbors[i] = new SetUnion(model, neighSet);
        }
    }

    /**
     * Construct an undirected graph G = (V, E) as the union of a set of undirected graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)}, i.e. :
     *      V = V_1 \cup ... \cup V_k (\cup = set union);
     *      E = E_1 \cup ... \cup E_k.
     * @param model the model
     * @param graphs the graphs to construct the union graph from
     * @param additionalNodes additional nodes to include in the graph
     * @param additionalNeigh additional edges to include in the graph
     */
    public UndirectedGraph(Model model, ISet additionalNodes, ISet[] additionalNeigh, UndirectedGraph... graphs) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = IntStream.range(0, graphs.length).map(i -> graphs[i].getNbMaxNodes()).max().getAsInt();
        ISet[] nodeSets = new ISet[graphs.length + 1];
        for (int i = 0; i < graphs.length; i++) {
            nodeSets[i] = graphs[i].getNodes();
        }
        nodeSets[graphs.length] = additionalNodes;
        this.nodes = new SetUnion(model, nodeSets);
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            ISet[] neighSet = new ISet[graphs.length + 1];
            for (int j = 0; j < graphs.length; j++) {
                neighSet[j] = graphs[j].getNeighborsOf(i);
            }
            neighSet[graphs.length] = additionalNeigh[i];
            neighbors[i] = new SetUnion(model, neighSet);
        }
    }

    public static ISet[] edgesArrayToEdgesSets(int n, int[][] edges) {
        ISet[] neigh = new ISet[n];
        for (int i = 0; i < n; i++) {
            int finalI = i;
            neigh[i] = SetFactory.makeConstantSet(IntStream.range(0, edges.length)
                    .filter(v -> {
                        assert edges[v].length == 2;
                        return edges[v][0] == finalI || edges[v][1] == finalI;
                    })
                    .map(v -> {
                        if (edges[v][0] == finalI) {
                            return edges[v][1];
                        } else {
                            return edges[v][0];
                        }
                    }).toArray()
            );
        }
        return neigh;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nodes : \n").append(nodes).append("\n");
        sb.append("neighbors : \n");
        for (int i : nodes) {
            sb.append(i).append(" -> {");
            for (int j : neighbors[i]) {
                sb.append(j).append(" ");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

    @Override
    /**
     * @inheritedDoc
     */
    public int getNbMaxNodes() {
        return n;
    }

    @Override
    /**
     * @inheritedDoc
     */
    public ISet getNodes() {
        return nodes;
    }

    @Override
    /**
     * @inheritedDoc
     */
    public SetType getEdgeSetType() {
        return edgeSetType;
    }

    @Override
    /**
     * @inheritedDoc
     */
    public SetType getNodeSetType() {
        return nodeSetType;
    }

    @Override
    public boolean addNode(int x) {
        return nodes.add(x);
    }

    @Override
    public boolean removeNode(int x) {
        if (nodes.remove(x)) {
            ISetIterator nei = getNeighborsOf(x).iterator();
            while (nei.hasNext()) {
                neighbors[nei.nextInt()].remove(x);
            }
            neighbors[x].clear();
            return true;
        }
        return false;
    }

    /**
     * Add edge (x,y) to the graph
     *
     * @param x a node index
     * @param y a node index
     * @return true iff (x,y) was not already in the graph
     */
    public boolean addEdge(int x, int y) {
        addNode(x);
        addNode(y);
        if (x == y) {
            return neighbors[x].add(y);
        }
        if (neighbors[x].add(y)) {
            boolean b = neighbors[y].add(x);
            assert b : "asymmetric adjacency matrix in an undirected graph";
            return true;
        }
        return false;
    }

    /**
     * test whether edge (x,y) is in the graph or not
     *
     * @param x a node index
     * @param y a node index
     * @return true iff edge (x,y) is in the graph
     */
    public boolean containsEdge(int x, int y) {
        if (neighbors[x].contains(y)) {
            assert (neighbors[y].contains(x)) : "asymmetric adjacency matrix in an undirected graph";
            return true;
        }
        return false;
    }

    /**
     * Remove edge (x,y) from the graph
     *
     * @param x a node index
     * @param y a node index
     * @return true iff (x,y) was in the graph
     */
    public boolean removeEdge(int x, int y) {
        if (x == y) {
            return neighbors[y].remove(x);
        }
        if (neighbors[x].remove(y)) {
            boolean b = neighbors[y].remove(x);
            assert b : "asymmetric adjacency matrix in an undirected graph";
            return true;
        }
        return false;
    }

    /**
     * Get neighbors of node x
     *
     * @param x node index
     * @return neighbors of x (predecessors and/or successors)
     */
    public ISet getNeighborsOf(int x) {
        return neighbors[x];
    }

    /**
     * @deprecated For an undirected graph, this method is equivalent to getNeighborsOf.
     */
    @Deprecated
    @Override
    public ISet getPredecessorsOf(int x) {
        return neighbors[x];
    }

    /**
     * @deprecated For an undirected graph, this method is equivalent to getNeighborsOf.
     */
    @Deprecated
    @Override
    public ISet getSuccessorsOf(int x) {
        return neighbors[x];
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    /**
     * Structural equality test between two undirected graph vars.
     * Only existing nodes and edges are tested, i.e. graphs can have different underlying set data structures,
     * and different attributes such as nbMaxNodes, allNodes, stored or not.
     * @param other
     * @return true iff `this` and `other` contains exactly the same nodes and same edges.
     */
    public boolean equals(UndirectedGraph other) {
        if (getNodes().size() != other.getNodes().size()) {
            return false;
        }
        for (int i : getNodes()) {
            if (!other.containsNode(i)) {
                return false;
            }
            if (getNeighborsOf(i).size() != other.getNeighborsOf(i).size()) {
                return false;
            }
            for (int j : getNeighborsOf(i)) {
                if (!other.containsEdge(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }
}
