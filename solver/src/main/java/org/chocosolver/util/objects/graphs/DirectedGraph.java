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

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Directed graph implementation : directed edges are indexed per endpoints
 * @author Jean-Guillaume Fages, Xavier Lorca
 */
public class DirectedGraph implements IGraph {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final ISet[] successors;
    private final ISet[] predecessors;
    private ISet nodes;
    private final int n;
    private final SetType nodeSetType;
    private final SetType edgeSetType;

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

    /**
     * CONSTRUCTOR FOR BACKTRACKABLE DIRECTED NODE INDUCED SUBGRAPHS:
     *
     * Construct a backtrackable directed graph G' = (V', E') from another directed graph G = (V, E) such that:
     *          V' = E \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * with nodes a fixed set of nodes.
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param nodes the set of nodes to construct the subgraph from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public DirectedGraph(Model model, DirectedGraph g, ISet nodes, boolean exclude) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        if (exclude) {
            this.nodes = new SetDifference(model, g.getNodes(), nodes);
        } else {
            this.nodes = new SetIntersection(model, g.getNodes(), nodes);
        }
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            if (exclude) {
                predecessors[i] = new SetDifference(model, g.getPredecessorsOf(i), nodes);
                successors[i] = new SetDifference(model, g.getSuccessorsOf(i), nodes);
            } else {
                predecessors[i] = new SetIntersection(model, g.getPredecessorsOf(i), nodes);
                successors[i] = new SetIntersection(model, g.getSuccessorsOf(i), nodes);
            }
        }
    }

    /**
     * CONSTRUCTOR FOR BACKTRACKABLE DIRECTED NODE INDUCED SUBGRAPHS:
     *
     * Construct a backtrackable directed graph G' = (V', E') from another directed graph G = (V, E) such that:
     *          V' = E \ nodes (set difference) if exclude = true, else V' = V \cap nodes (set intersection)
     *          E' = { (x, y) \in E | x \in V' \land y \in V' }.
     *
     * with nodes a fixed set of nodes.
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param nodes the set of nodes to construct the subgraph from (see exclude parameter)
     * @param exclude if true, V' = V \ nodes (set difference), else V' = V \cap nodes (set intersection)
     */
    public DirectedGraph(Model model, DirectedGraph g, DirectedGraph UB, ISet nodes, boolean exclude) {
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
                    int offset = g.getNodes() instanceof ISet.WithOffset ?
                            ((ISet.WithOffset)g.getNodes()).getOffset() : 0;
                    this.nodes = new SetIntersection(model, nodeSetType, offset, g.getNodes(), nodes);
                    neighNeeded = new SetIntersection(model, nodeSetType, offset, UB.getNodes(), nodes);
                    break;
                }
            }
        }
        if (!needDynamic) {
            this.nodes = g.nodes;
        }
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i : neighNeeded) {
            // SUCCESSORS
            needDynamic = false;
            if (exclude) {
                for (int j : nodes) {
                    if (UB.getSuccessorsOf(i).contains(j)) {
                        needDynamic = true;
                        successors[i] = new SetDifference(model, g.getSuccessorsOf(i), nodes);
                        break;
                    }
                }
            } else {
                for (int j : UB.getSuccessorsOf(i)) {
                    if (!nodes.contains(j)) {
                        needDynamic = true;
                        SetType edgeSetType = g.getEdgeSetType();
                        successors[i] = new SetIntersection(model, edgeSetType, 0, g.getSuccessorsOf(i), nodes);
                    }
                }
            }
            if (!needDynamic) {
                successors[i] = g.successors[i];
            }
            // PREDECESSORS
            needDynamic = false;
            if (exclude) {
                for (int j : nodes) {
                    if (UB.getPredecessorsOf(i).contains(j)) {
                        needDynamic = true;
                        predecessors[i] = new SetDifference(model, g.getPredecessorsOf(i), nodes);
                        break;
                    }
                }
            } else {
                for (int j : UB.getPredecessorsOf(i)) {
                    if (!nodes.contains(j)) {
                        needDynamic = true;
                        SetType edgeSetType = g.getEdgeSetType();
                        predecessors[i] = new SetIntersection(model, edgeSetType, 0, g.getPredecessorsOf(i), nodes);
                    }
                }
            }
            if (!needDynamic) {
                predecessors[i] = g.predecessors[i];
            }
        }
    }

    /**
     * GENERIC CONSTRUCTOR FOR BACKTRACKABLE EDGE INDUCED DIRECTED SUBGRAPHS:
     *
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' or (y, x) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param edgesPredecessors the set of edges (node predecessors) to construct the subgraph from (see exclude parameter)
     * @param edgesSuccessors the set of edges (node successors) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public DirectedGraph(Model model, DirectedGraph g, ISet[] edgesPredecessors, ISet[] edgesSuccessors, boolean exclude) {
        assert edgesPredecessors.length == g.getNbMaxNodes();
        assert edgesSuccessors.length == g.getNbMaxNodes();
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        this.predecessors = new ISet[n];
        this.successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            if (exclude) {
                predecessors[i] = new SetDifference(model, g.getPredecessorsOf(i), edgesPredecessors[i]);
                successors[i] = new SetDifference(model, g.getSuccessorsOf(i), edgesSuccessors[i]);
            } else {
                predecessors[i] = new SetIntersection(model, g.getPredecessorsOf(i), edgesPredecessors[i]);
                successors[i] = new SetIntersection(model, g.getSuccessorsOf(i), edgesSuccessors[i]);
            }
        }
        this.nodes = new SetUnion(model, new SetUnion(model, predecessors), new SetUnion(model, successors));
    }

    /**
     * GENERIC CONSTRUCTOR FOR BACKTRACKABLE EDGE INDUCED DIRECTED SUBGRAPHS:
     *
     * /!\ Optimized for graph views instantiation: avoids unnecessary dynamic data structures /!\
     *
     * Construct a backtrackable graph G = (V', E') from G = (V, E) such that:
     *     V' = { x \in V | \exists y \in V s.t. (x, y) \in E' or (y, x) \in E' }
     *     E' = E \ edges (set difference) if exclude = true, else E' = E \cap edges (set intersection).
     *
     * with edges a fixed set of edges.
     *
     * @param model the model
     * @param g the graph to construct a subgraph from
     * @param UB If used to instantiate a graph view: the observed graph variable upper bound, used to detect whether
     *           a dynamic data structure is necessary for node and neighbors sets.
     * @param edgesPredecessors the set of edges (node predecessors) to construct the subgraph from (see exclude parameter)
     * @param edgesSuccessors the set of edges (node successors) to construct the subgraph from (see exclude parameter)
     * @param exclude if true, E' = E \ edges (set difference), else E' = E \cap edges (set intersection)
     */
    public DirectedGraph(Model model, DirectedGraph g, DirectedGraph UB, ISet[] edgesPredecessors, ISet[] edgesSuccessors, boolean exclude) {
        assert edgesPredecessors.length == g.getNbMaxNodes();
        assert edgesSuccessors.length == g.getNbMaxNodes();
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = g.getNbMaxNodes();
        this.predecessors = new ISet[n];
        this.successors = new ISet[n];
        boolean nodeNeedDynamic = false;
        for (int i = 0; i < n; i++) {
            // PREDECESSORS
            boolean needDynamic = false;
            if (exclude) {
                for (int j : edgesPredecessors[i]) {
                    if (UB.getPredecessorsOf(i).contains(j)) {
                        needDynamic = true;
                        nodeNeedDynamic = true;
                        predecessors[i] = new SetDifference(model, g.getPredecessorsOf(i), edgesPredecessors[i]);
                        break;
                    }
                }
            } else {
                for (int j : UB.getPredecessorsOf(i)) {
                    if (!edgesPredecessors[i].contains(j)) {
                        needDynamic = true;
                        nodeNeedDynamic = true;
                        predecessors[i] = new SetIntersection(model, g.getPredecessorsOf(i), edgesPredecessors[i]);
                        break;
                    }
                }
            }
            if (!needDynamic) {
                predecessors[i] = g.predecessors[i];
            }
            // SUCCESSORS
            needDynamic = false;
            if (exclude) {
                for (int j : edgesSuccessors[i]) {
                    if (UB.getSuccessorsOf(i).contains(j)) {
                        needDynamic = true;
                        nodeNeedDynamic = true;
                        successors[i] = new SetDifference(model, g.getSuccessorsOf(i), edgesSuccessors[i]);
                        break;
                    }
                }
            } else {
                for (int j : UB.getSuccessorsOf(i)) {
                    if (!edgesSuccessors[i].contains(j)) {
                        needDynamic = true;
                        nodeNeedDynamic = true;
                        successors[i] = new SetIntersection(model, g.getSuccessorsOf(i), edgesSuccessors[i]);
                        break;
                    }
                }
            }
            if (!needDynamic) {
                successors[i] = g.successors[i];
            }
        }
        if (nodeNeedDynamic) {
            this.nodes = new SetUnion(model, new SetUnion(model, predecessors), new SetUnion(model, successors));
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
    public DirectedGraph(Model model, DirectedGraph g, int[][] edges, boolean exclude) {
        this(model, g, edgesArrayToPredecessorsSets(g.getNbMaxNodes(), edges), edgesArrayToSuccessorsSets(g.getNbMaxNodes(), edges), exclude);
    }

    public DirectedGraph(Model model, DirectedGraph g, DirectedGraph UB, int[][] edges, boolean exclude) {
        this(model, g, UB, edgesArrayToPredecessorsSets(g.getNbMaxNodes(), edges), edgesArrayToSuccessorsSets(g.getNbMaxNodes(), edges), exclude);
    }

    // Graph arithmetic constructors

    /**
     * Construct an directed graph G = (V, E) as the union of a set of directed graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)}, i.e. :
     *      V = V_1 \cup ... \cup V_k (\cup = set union);
     *      E = E_1 \cup ... \cup E_k.
     * @param model the model
     * @param graphs the graphs to construct the union graph from
     */
    public DirectedGraph(Model model, DirectedGraph... graphs) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = Arrays.stream(graphs).mapToInt(DirectedGraph::getNbMaxNodes).max().getAsInt();
        ISet[] nodeSets = new ISet[graphs.length];
        for (int i = 0; i < graphs.length; i++) {
            nodeSets[i] = graphs[i].getNodes();
        }
        this.nodes = new SetUnion(model, nodeSets);
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            ISet[] predSet = new ISet[graphs.length];
            ISet[] succSet = new ISet[graphs.length];
            for (int j = 0; j < graphs.length; j++) {
                predSet[j] = graphs[j].getPredecessorsOf(i);
                succSet[j] = graphs[j].getSuccessorsOf(i);
            }
            predecessors[i] = new SetUnion(model, predSet);
            successors[i] = new SetUnion(model, succSet);
        }
    }

    /**
     * Construct an directed graph G = (V, E) as the union of a set of directed graphs {G_1 = (V_1, E_1), ..., G_k = (V_k, E_k)}, i.e. :
     *      V = V_1 \cup ... \cup V_k (\cup = set union);
     *      E = E_1 \cup ... \cup E_k.
     * @param model the model
     * @param graphs the graphs to construct the union graph from
     * @param additionalNodes additional nodes to include in the graph
     * @param additionalSuccs additional edges (successors) to include in the graph
     */
    public DirectedGraph(Model model, ISet additionalNodes, ISet[] additionalSuccs, DirectedGraph... graphs) {
        this.nodeSetType = SetType.DYNAMIC;
        this.edgeSetType = SetType.DYNAMIC;
        this.n = Arrays.stream(graphs).mapToInt(DirectedGraph::getNbMaxNodes).max().getAsInt();
        ISet[] nodeSets = new ISet[graphs.length + 1];
        for (int i = 0; i < graphs.length; i++) {
            nodeSets[i] = graphs[i].getNodes();
        }
        nodeSets[graphs.length] = additionalNodes;
        this.nodes = new SetUnion(model, nodeSets);
        predecessors = new ISet[n];
        successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            ISet[] predSet = new ISet[graphs.length];
            ISet[] succSet = new ISet[graphs.length + 1];
            for (int j = 0; j < graphs.length; j++) {
                predSet[j] = graphs[j].getPredecessorsOf(i);
                succSet[j] = graphs[j].getSuccessorsOf(i);
            }
            succSet[graphs.length] = additionalSuccs[i];
            predecessors[i] = new SetUnion(model, predSet);
            successors[i] = new SetUnion(model, succSet);
        }
    }

    public static ISet[] edgesArrayToPredecessorsSets(int n, int[][] edges) {
        ISet[] predecessors = new ISet[n];
        for (int i = 0; i < n; i++) {
            int finalI = i;
            predecessors[i] = SetFactory.makeConstantSet(IntStream.range(0, edges.length)
                    .filter(v -> {
                        assert edges[v].length == 2;
                        return edges[v][1] == finalI;
                    })
                    .map(v -> edges[v][0])
                    .toArray()
            );
        }
        return predecessors;
    }

    public static ISet[] edgesArrayToSuccessorsSets(int n, int[][] edges) {
        ISet[] successors = new ISet[n];
        for (int i = 0; i < n; i++) {
            int finalI = i;
            successors[i] = SetFactory.makeConstantSet(IntStream.range(0, edges.length)
                    .filter(v -> {
                        assert edges[v].length == 2;
                        return edges[v][0] == finalI;
                    })
                    .map(v -> edges[v][1])
                    .toArray()
            );
        }
        return successors;
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
        return nodes.add(x);
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
        if (successors[from].remove(to)) {
            boolean b = predecessors[to].remove(from);
            assert b : "incoherent directed graph";
            return true;
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
        if (successors[from].add(to)) {
            boolean b = predecessors[to].add(from);
            assert b : "incoherent directed graph";
            return true;
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
     * @param other a directed graph
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
