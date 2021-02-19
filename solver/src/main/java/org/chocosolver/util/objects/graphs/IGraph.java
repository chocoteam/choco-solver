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

import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * @author Jean-Guillaume Fages, Xavier Lorca
 *         <p/>
 *         Provide an interface for the graph manipulation
 */
public interface IGraph  {


    /**
     * @return the collection of nodes present in the graph
     */
    ISet getNodes();

    /**
     * Adds node x to the node set of the graph
     *
     * @param x a node index
     * @return true iff x was not already present in the graph
     */
    boolean addNode(int x);

    /**
     * Remove node x from the graph
     *
     * @param x a node index
     * @return true iff x was present in the graph
     */
    boolean removeNode(int x);

    /**
     * The maximum number of nodes in the graph
	 * Vertices of the graph belong to [0,getNbMaxNodes()-1]
	 * This quantity is fixed at the creation of the graph
     *
     * @return the maximum number of nodes of the graph
     */
    int getNbMaxNodes();

    /**
     * Get the type of data structures used in the graph
	 *
     * @return the type of data structures used in the graph
     */
    SetType getType();


    /**
     * Get either x's successors or neighbors.
     * <p/>
     * This method enables to capitalize some code but should be called with care
     *
     * @param x a node index
     * @return x's successors if <code>this</code> is directed
     *         x's neighbors otherwise
     */
    ISet getSuccOrNeighOf(int x);

    /**
     * Get either x's predecessors or neighbors.
     * <p/>
     * This method enables to capitalize some code but should be called with care
     *
     * @param x a node index
     * @return x's predecessors if <code>this</code> is directed
     *         x's neighbors otherwise
     */
    ISet getPredOrNeighOf(int x);

    /**
     * If <code>this </code> is directed
     * returns true if and only if arc (x,y) exists
     * Else, if <code>this</code> is undirected
     * returns true if and only if edge (x,y) exists
     * <p/>
     * This method enables to capitalize some code but should be called with care
     *
     * @param x a node index
     * @param y a node index
     */
    boolean isArcOrEdge(int x, int y);

    /**
     * @return true if and only if <code>this</code> is a directed graph
     */
    boolean isDirected();

    //***********************************************************************************
    // GraphViz
    //***********************************************************************************

    /**
     * Export graph to graphviz format, see http://www.webgraphviz.com/
     *
     * @return a String encoding the graph to be displayed by graphViz
     */
    default String graphVizExport() {
        boolean directed = isDirected();
        String arc = directed ? " -> " : " -- ";
        StringBuilder sb = new StringBuilder();
        sb.append(directed ? "digraph " : "graph ").append("G" + "{\n");
        for (int i : getNodes()) sb.append(i + " ");
        sb.append(";\n");
        for (int i : getNodes()) {
            for (int j : getSuccOrNeighOf(i)) {
                if (directed || i < j) sb.append(i + arc + j + " ;\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
