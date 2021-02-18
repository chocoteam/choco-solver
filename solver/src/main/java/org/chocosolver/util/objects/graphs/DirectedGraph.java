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
 * Directed graph implementation : arcs are indexed per endpoints
 * @author Jean-Guillaume Fages, Xavier Lorca
 */
public class DirectedGraph implements IGraph {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private ISet[] successors;
    private ISet[] predecessors;
    private ISet nodes;
    private int n;
    private SetType type;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates an empty graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param n        maximum number of nodes
     * @param type     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph.
	 *                 i.e. The node set is fixed to [0,n-1] and will never change
     */
    public DirectedGraph(int n, SetType type, boolean allNodes) {
        this.type = type;
        this.n = n;
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeSet(type, 0);
            successors[i] = SetFactory.makeSet(type, 0);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeBitSet(0);
        }
    }

    /**
     * Creates an empty backtrable graph of n nodes
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param model   model providing the backtracking environment
     * @param n        maximum number of nodes
     * @param type     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph
     */
    public DirectedGraph(Model model, int n, SetType type, boolean allNodes) {
        this.n = n;
        this.type = type;
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeStoredSet(type, 0, model);
            successors[i] = SetFactory.makeStoredSet(type, 0, model);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeStoredSet(SetType.BITSET, 0, model);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nodes : \n").append(nodes).append("\n");
        sb.append("successors : \n");
        for (int i : nodes) {
            sb.append(i).append(" -> {");
            for (int j : successors[i]) {
                sb.append(j).append(" ");
            }
            sb.append("}\n");
        }
        return sb.toString();
    }

    @Override
    public int getNbMaxNodes() {
        return n;
    }

    @Override
    public ISet getNodes() {
        return nodes;
    }

    @Override
    public SetType getType() {
        return type;
    }

    @Override
    public boolean addNode(int x) {
        return !nodes.contains(x) && nodes.add(x);
    }

    @Override
    public boolean removeNode(int x) {
        if (nodes.remove(x)) {
            ISetIterator iter = successors[x].iterator();
            while (iter.hasNext()) {
                predecessors[iter.nextInt()].remove(x);
            }
            successors[x].clear();
            iter = predecessors[x].iterator();
            while (iter.hasNext()) {
                successors[iter.nextInt()].remove(x);
            }
            predecessors[x].clear();
            return true;
        }
        assert (predecessors[x].size() == 0) : "incoherent directed graph";
        assert (successors[x].size() == 0) : "incoherent directed graph";
        return false;
    }

    /**
     * remove arc (from,to) from the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff arc (from,to) was in the graph
     */
    public boolean removeArc(int from, int to) {
        if (successors[from].contains(to)) {
            assert (predecessors[to].contains(from)) : "incoherent directed graph";
            return successors[from].remove(to) | predecessors[to].remove(from);
        }
        return false;
    }

    /**
     * Test whether arc (from,to) exists or not in the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff arc (from,to) exists in the graph
     */
    public boolean arcExists(int from, int to) {
        if (successors[from].contains(to)) {
            assert (predecessors[to].contains(from)) : "incoherent directed graph";
            return true;
        }
        return false;
    }

    @Override
    public boolean isArcOrEdge(int from, int to) {
        return arcExists(from, to);
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    /**
     * add arc (from,to) to the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff arc (from,to) was not already in the graph
     */
    public boolean addArc(int from, int to) {
        addNode(from);
        addNode(to);
        if (!successors[from].contains(to)) {
            assert (!predecessors[to].contains(from)) : "incoherent directed graph";
            return successors[from].add(to) & predecessors[to].add(from);
        }
        return false;
    }

    /**
     * Get successors of node x
     *
     * @param x node index
     * @return successors of x
     */
    public ISet getSuccOf(int x) {
        return successors[x];
    }

    @Override
    public ISet getSuccOrNeighOf(int x) {
        return successors[x];
    }

    /**
     * Get predecessors of node x
     *
     * @param x node index
     * @return predecessors of x
     */
    public ISet getPredOf(int x) {
        return predecessors[x];
    }

    @Override
    public ISet getPredOrNeighOf(int x) {
        return predecessors[x];
    }
}
