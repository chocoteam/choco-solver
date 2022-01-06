/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.util.objects.graphs.IGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * A Graph Variable is defined by a domain which is a graph interval [LB, UB].
 * An instantiation of a graph variable is a graph composed of nodes and edges (directed or not).
 * LB is the kernel graph (or lower bound), that must be a subgraph of any instantiation.
 * UB is the envelope graph (or upper bound), such that any instantiation is a subgraph of it.
 *
 * --- GRAPH API REFACTORING 04/03/2021 ---
 *
 * In accordance with IGraph:
 *
 * - The semantic distinction between arcs and edges has been removed for more clarity. If the graph is undirected,
 *      edges are undirected, if the graph is directed, the graph is directed. Methods related to edges are
 *      `enforceEdge` and `removeEdge`.
 *
 * - The object model is such that the more abstract interface specifies directed graph variable accessors on edges,
 *      `getMandatorySuccessorsOf`, `getPotentialSuccessorsOf`, `getMandatoryPredecessorsOf`, and
 *      `getPotentialPredecessorsOf`. When the graph is undirected, the methods `getMandatoryNeighborsOf` and
 *      `getPotentialNeighborsOf` are available. Note that an undirected graph is equivalent to a directed graph
 *      with couples of opposite directed edges. Thus, the neighbors of a node in an undirected graph are both
 *      successors and predecessors, and this is why these methods are equivalent to `getMandatoryNeighbors` and
 *      `getPotentialNeighborssOf` in the case of an undirected graph. To encourage unambiguous use and facilitate
 *      code reading, the successors and predecessors related method have been defined as deprecated in explicit uses
 *      of UndirectedGraphs.
 *
 *  - The possibility to chose (in constructors) and get the set data structure for nodes has also been added,
 *      as it was only implemented for neighbors: `getNodeSetType` and `getEdgeSetType`. The previous default behaviour
 *      has been conserved with default constructors.
 *
 */
public interface GraphVar<E extends IGraph> extends Variable {

    /**
     * Get GraphVar lower bound (or kernel): the graph which is a subgraph of any instantiation.
     * @return The lower bound (or kernel) graph of this GraphVar.
     */
    E getLB();

    /**
     * Get GraphVar upper bound (or envelope): any instantiation of this GraphVar is a subgraph of the upper bound.
     * @return The upper bound (or envelope) graph of this GraphVar.
     */
    E getUB();

    /**
     * Adds a node to the lower bound graph
     *
     * @param node node's index
     * @param cause cause of node addition
     * @return true iff the node has been added to the lower bound
     */
    boolean enforceNode(int node, ICause cause) throws ContradictionException;

    /**
     * Removes a node from the upper bound graph
     *
     * @param node node's index
     * @param cause cause of node removal
     * @return true iff the node has been removed from the upper bound
     */
    boolean removeNode(int node, ICause cause) throws ContradictionException;

    /**
     * Adds edge (directed directed graph variable) (x,y) in the lower bound
     *
     * @param x     node's index
     * @param y     node's index
     * @param cause cause of edge addition
     * @return true iff the edge has been add to the lower bound
     * @throws ContradictionException
     */
    boolean enforceEdge(int x, int y, ICause cause) throws ContradictionException;

    /**
     * Removes edge (directed in case of directed graph variable) (x,y) from the upper bound
     *
     * @param x node's index
     * @param y node's index
     * @param cause cause of edge removal
     * @return true iff the edge has been removed from the upper bound
     * @throws ContradictionException if the edge was mandatory
     */
    boolean removeEdge(int x, int y, ICause cause) throws ContradictionException;

    /**
     * Get the set of successors (if directed) or neighbors (if undirected) of vertex 'node'
     * in the lower bound graph (mandatory outgoing edges)
     *
     * @param node a vertex
     * @return The set of successors (if directed) or neighbors (if undirected) of 'node' in LB
     */
    default ISet getMandatorySuccessorsOf(int node) {
        return getLB().getSuccessorsOf(node);
    }

    /**
     * Get the set of successors (if directed) or neighbors (if undirected) of vertex 'node'
     * in the upper bound graph (potential outgoing edges)
     *
     * @param node a vertex
     * @return The set of successors (if directed) or neighbors (if undirected) of 'node' in UB
     */
    default ISet getPotentialSuccessorsOf(int node) {
        return getUB().getSuccessorsOf(node);
    }

    /**
     * Get the set of predecessors (if directed) or neighbors (if undirected) of vertex 'node'
     * in the lower bound graph (mandatory ingoing edges)
     *
     * @param node a vertex
     * @return The set of predecessors (if directed) or neighbors (if undirected) of 'node' in LB
     */
    default ISet getMandatoryPredecessorsOf(int node) {
        return getLB().getPredecessorsOf(node);
    }

    /**
     * Get the set of predecessors (if directed) or neighbors (if undirected) of vertex 'node'
     * in the upper bound graph (potential ingoing edges)
     *
     * @param node a vertex
     * @return The set of predecessors (if directed) or neighbors (if undirected) of 'node' in UB
     */
    default ISet getPotentialPredecessorOf(int node) {
        return getUB().getPredecessorsOf(node);
    }

    /**
     * @return the maximum number of node the graph variable may have.
     * Nodes are comprised in the interval [0,getNbMaxNodes()]
     * Therefore, any vertex index should be strictly lower than getNbMaxNodes()
     */
    int getNbMaxNodes();

    /**
     * @return the node set of the lower bound graph,
     * i.e. nodes that belong to every solution
     */
    default ISet getMandatoryNodes() {
        return getLB().getNodes();
    }

    /**
     * @return the node set of the upper bound graph,
     * i.e. nodes that may belong to one solution
     */
    default ISet getPotentialNodes() {
        return getUB().getNodes();
    }

    /**
     * @return true iff the graph is directed. It is undirected otherwise.
     */
    boolean isDirected();

    /**
     * Instantiates <code>this</code> to value given in parameter.
     * This method is not supposed to be used except for restoring solutions.
     *
     * @param value value of <code>this</code>
     * @param cause
     * @throws ContradictionException
     */
    void instantiateTo(E value, ICause cause) throws ContradictionException;

    /**
     * Retrieves the current value of the variable if instantiated, otherwise the lower bound (kernel).
     *
     * @return the current value (or kernel if not yet instantiated).
     */
    default E getValue() {
        assert isInstantiated();
        return getLB();
    }

    @Override
    GraphDelta getDelta();

    /**
     * Make the propagator 'prop' have an incremental filtering w.r.t. this graph variable
     *
     * @param propagator A propagator involving this graph variable
     * @return A new instance of GraphDeltaMonitor to make incremental propagators
     */
    IGraphDeltaMonitor monitorDelta(ICause propagator);
}
