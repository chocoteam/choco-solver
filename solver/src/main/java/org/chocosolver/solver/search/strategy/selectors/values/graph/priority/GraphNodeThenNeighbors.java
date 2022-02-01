/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.selectors.values.graph.priority;

import org.chocosolver.solver.search.strategy.selectors.values.graph.edge.GraphEdgeSelector;
import org.chocosolver.solver.search.strategy.selectors.values.graph.node.GraphNodeSelector;
import org.chocosolver.solver.variables.GraphVar;

/**
 * @author Dimitri Justeau-Allaire
 * @since 20/04/2021
 */
public class GraphNodeThenNeighbors implements GraphNodeOrEdgeSelector, GraphNodeSelector, GraphEdgeSelector {

    private int currentNode;
    private int nextTo;
    private GraphVar g;

    public GraphNodeThenNeighbors() {
        this.currentNode = -1;
        this.nextTo = -1;
        this.g = null;
    }

    @Override
    public boolean nextIsNode(GraphVar graphVar) {
        if (g != graphVar) {
            if (init(graphVar)) {
                return true;
            }
        }
        nextTo = findNextEdge();
        while (nextTo == -1) {
            if (findNextNode()) {
                return true;
            }
            nextTo = findNextEdge();
        }
        return false;
    }

    @Override
    public int[] selectEdge(GraphVar graphVar) {
        assert graphVar == g;
        return new int[] {currentNode, nextTo};
    }

    @Override
    public int selectNode(GraphVar graphVar) {
        assert graphVar == g;
        return currentNode;
    }

    private int findNextEdge() {
        for (int j : g.getPotentialSuccessorsOf(currentNode)) {
            if (g.getMandatoryNodes().contains(j) && !g.getMandatorySuccessorsOf(currentNode).contains(j)) {
                return j;
            }
        }
        return -1;
    }

    private boolean findNextNode() {
        for (int i : g.getPotentialNodes()) {
            if (!g.getMandatoryNodes().contains(i)) {
                this.currentNode = i;
                return true;
            }
        }
        // Nodes are instantiated
        for (int i : g.getMandatoryNodes()) {
            if (g.getMandatorySuccessorsOf(i).size() != g.getPotentialSuccessorsOf(i).size()) {
                this.currentNode = i;
                return false;
            }
        }
        currentNode = -1;
        return false;
    }

    private boolean init(GraphVar graphVar) {
        this.g = graphVar;
        return findNextNode();
    }
}
