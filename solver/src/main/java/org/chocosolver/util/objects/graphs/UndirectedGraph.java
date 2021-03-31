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
import org.chocosolver.util.objects.setDataStructures.*;

/**
 * Specific implementation of an undirected graph
 *
 * @author Jean-Guillaume Fages, Xavier Lorca
 */
public class UndirectedGraph implements IGraph {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private ISet[] neighbors;
    private ISet nodes;
    private int n;
    private SetType edgeSetType;
    private SetType nodeSetType;

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

    /**
     * Construct a backtrackable graph G' = (V', E') from another graph G = (V, E) such that:
     *      V' = E \ excludedNodes;
     *      E' = { (x, y) \in E | x \notIn excludedNodes \land y \notIn excludedNodes };
     * with excludedNodes a fixed set of nodes.
     * @param model the model
     * @param g the graph to con
     * @param excludedNodes
     */
    public UndirectedGraph(Model model, UndirectedGraph g, ISet excludedNodes) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        this.nodes = new SetDifference(model, g.getNodes(), excludedNodes);
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = new SetDifference(model, g.getNeighborsOf(i), excludedNodes);
        }
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
        if (x == y && !neighbors[x].contains(y)) {
            neighbors[x].add(y);
            return true;
        }
        if (!neighbors[x].contains(y)) {
            assert (!neighbors[y].contains(x)) : "asymmetric adjacency matrix in an undirected graph";
            neighbors[x].add(y);
            neighbors[y].add(x);
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
        if (x == y && neighbors[x].contains(y)) {
            neighbors[y].remove(x);
            return true;
        }
        if (neighbors[x].contains(y)) {
            assert (neighbors[y].contains(x)) : "asymmetric adjacency matrix in an undirected graph";
            neighbors[x].remove(y);
            neighbors[y].remove(x);
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
