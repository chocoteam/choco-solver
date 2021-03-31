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
 * Directed graph implementation : directed edges are indexed per endpoints
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
    private SetType nodeSetType;
    private SetType edgeSetType;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates an empty graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param n        maximum number of nodes
     * @param nodeSetType     data structure to use for representing node
     * @param edgeSetType     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph.
     *                 i.e. The node set is fixed to [0,n-1] and will never change
     */
    public DirectedGraph(int n, SetType nodeSetType, SetType edgeSetType, boolean allNodes) {
        this.nodeSetType = nodeSetType;
        this.edgeSetType = edgeSetType;
        this.n = n;
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeSet(edgeSetType, 0);
            successors[i] = SetFactory.makeSet(edgeSetType, 0);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeSet(nodeSetType, 0);
        }
    }

    /**
     * Creates an empty graph.
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * Nodes are stored as BITSET
     *
     * @param n        maximum number of nodes
     * @param edgeSetType     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph.
     *                 i.e. The node set is fixed to [0,n-1] and will never change
     */
    public DirectedGraph(int n, SetType edgeSetType, boolean allNodes) {
        this(n, SetType.BITSET, edgeSetType, allNodes);
    }


    /**
     * Creates an empty backtrable graph of n nodes
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * @param model   model providing the backtracking environment
     * @param n        maximum number of nodes
     * @param nodeSetType     data structure to use for representing nodes
     * @param edgeSetType     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph
     */
    public DirectedGraph(Model model, int n, SetType nodeSetType, SetType edgeSetType, boolean allNodes) {
        this.n = n;
        this.nodeSetType = nodeSetType;
        this.edgeSetType = edgeSetType;
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeStoredSet(edgeSetType, 0, model);
            successors[i] = SetFactory.makeStoredSet(edgeSetType, 0, model);
        }
        if (allNodes) {
            this.nodes = SetFactory.makeConstantSet(0,n-1);
        } else {
            this.nodes = SetFactory.makeStoredSet(nodeSetType, 0, model);
        }
    }

    /**
     * Creates an empty backtrable graph of n nodes
     * Allocates memory for n nodes (but they should then be added explicitly,
     * unless allNodes is true).
     *
     * Nodes are stored as BITSET
     *
     * @param model   model providing the backtracking environment
     * @param n        maximum number of nodes
     * @param edgeSetType     data structure to use for representing node successors and predecessors
     * @param allNodes true iff all nodes must always remain present in the graph
     */
    public DirectedGraph(Model model, int n, SetType edgeSetType, boolean allNodes) {
        this(model, n, SetType.BITSET, edgeSetType, allNodes);
    }

    /**
     * Construct a read-only copy of another graph
     * @param g the graph to copy
     */
    public DirectedGraph(DirectedGraph g) {
        this.nodeSetType = SetType.FIXED_ARRAY;
        this.edgeSetType = SetType.FIXED_ARRAY;
        this.n = g.getNbMaxNodes();
        this.nodes = SetFactory.makeConstantSet(g.getNodes().toArray());
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = SetFactory.makeConstantSet(g.getPredecessorsOf(i).toArray());
            successors[i] = SetFactory.makeConstantSet(g.getSuccessorsOf(i).toArray());
        }
    }

    public DirectedGraph(Model m, DirectedGraph g, ISet excludedNodes) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        this.nodes = new SetDifference(m, g.getNodes(), excludedNodes);
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            predecessors[i] = new SetDifference(m, g.getPredecessorsOf(i), excludedNodes);
            successors[i] = new SetDifference(m, g.getSuccessorsOf(i), excludedNodes);
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
    public SetType getEdgeSetType() {
        return edgeSetType;
    }

    @Override
    public SetType getNodeSetType() {
        return nodeSetType;
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
     * remove directed edge (from,to) from the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff directed edge (from,to) was in the graph
     */
    public boolean removeEdge(int from, int to) {
        if (successors[from].contains(to)) {
            assert (predecessors[to].contains(from)) : "incoherent directed graph";
            return successors[from].remove(to) | predecessors[to].remove(from);
        }
        return false;
    }

    /**
     * Test whether directed edge (from,to) exists or not in the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff directed edge (from,to) exists in the graph
     */
    public boolean containsEdge(int from, int to) {
        if (successors[from].contains(to)) {
            assert (predecessors[to].contains(from)) : "incoherent directed graph";
            return true;
        }
        return false;
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    /**
     * add directed edge (from,to) to the graph
     *
     * @param from a node index
     * @param to   a node index
     * @return true iff directed edge (from,to) was not already in the graph
     */
    public boolean addEdge(int from, int to) {
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
    public ISet getSuccessorsOf(int x) {
        return successors[x];
    }

    /**
     * Get predecessors of node x
     *
     * @param x node index
     * @return predecessors of x
     */
    public ISet getPredecessorsOf(int x) {
        return predecessors[x];
    }

    /**
     * Structural equality test between two directed graph vars.
     * Only existing nodes and edges are tested, i.e. graphs can have different underlying set data structures,
     * and different attributes such as nbMaxNodes and allNodes.
     * @param other
     * @return true iff `this` and `other` contains exactly the same nodes and same edges.
     */
    public boolean equals(DirectedGraph other) {
        if (getNodes().size() != other.getNodes().size()) {
            return false;
        }
        for (int i : getNodes()) {
            if (!other.containsNode(i)) {
                return false;
            }
            if (getSuccessorsOf(i).size() != other.getSuccessorsOf(i).size()) {
                return false;
            }
            for (int j : getSuccessorsOf(i)) {
                if (!other.containsEdge(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }
}
