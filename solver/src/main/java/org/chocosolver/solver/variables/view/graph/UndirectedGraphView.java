/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.view.graph;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.view.GraphView;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * An abstract class for undirected graph views over other variables
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public abstract class UndirectedGraphView<V extends Variable> extends GraphView<V, UndirectedGraph> implements UndirectedGraphVar {

    /**
     * Creates a graph view.
     *
     * @param name      name of the view
     * @param variables observed variables
     */
    protected UndirectedGraphView(String name, V[] variables) {
        super(name, variables);
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public boolean removeNode(int node, ICause cause) throws ContradictionException {
        assert cause != null;
        assert (node >= 0 && node < getNbMaxNodes());
        if (getMandatoryNodes().contains(node)) {
            this.contradiction(cause, "remove mandatory node");
            return false;
        } else if (!getPotentialNodes().contains(node)) {
            return false;
        }
        ISet nei = getPotentialNeighborsOf(node);
        for (int i : nei) {
            removeEdge(node, i, cause);
        }
        if (doRemoveNode(node)) {
            if (reactOnModification) {
                delta.add(node, GraphDelta.NODE_REMOVED, cause);
            }
            GraphEventType e = GraphEventType.REMOVE_NODE;
            notifyPropagators(e, cause);
            return true;
        }
        return false;
    }
}
