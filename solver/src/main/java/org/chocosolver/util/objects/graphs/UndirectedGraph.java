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
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

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
    private SetType type;

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
     * @param type     data structure storing for node neighbors
     * @param allNodes true iff all nodes will always remain in the graph
     */
    public UndirectedGraph(Model model, int n, SetType type, boolean allNodes) {
        this.type = type;
        this.n = n;
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = SetFactory.makeStoredSet(type, 0, model);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
        }
    }

    /**
     * Creates an empty (non-backtrackable) undirected graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param n        max number of nodes
     * @param type     data structure used for storing node neighbors
     * @param allNodes true iff all nodes will always remain in the graph
     */
    public UndirectedGraph(int n, SetType type, boolean allNodes) {
        this.type = type;
        this.n = n;
        neighbors = new ISet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = SetFactory.makeSet(type, 0);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeBitSet(0);
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
    public SetType getType() {
        return type;
    }

    @Override
    public boolean addNode(int x) {
        return nodes.add(x);
    }

    @Override
    public boolean removeNode(int x) {
        if (nodes.remove(x)) {
            ISetIterator nei = getNeighOf(x).iterator();
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
    public boolean edgeExists(int x, int y) {
        if (neighbors[x].contains(y)) {
            assert (neighbors[y].contains(x)) : "asymmetric adjacency matrix in an undirected graph";
            return true;
        }
        return false;
    }

    @Override
    public boolean isArcOrEdge(int x, int y) {
        return edgeExists(x, y);
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
    public ISet getNeighOf(int x) {
        return neighbors[x];
    }

    @Override
    public ISet getPredOrNeighOf(int x) {
        return neighbors[x];
    }

    @Override
    public ISet getSuccOrNeighOf(int x) {
        return neighbors[x];
    }

    @Override
    public boolean isDirected() {
        return false;
    }
}
