/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Undirected Graph variable guaranteeing that any instantiation is a node-induced subgraph of the envelope
 * used to construct the graph variable. Any two nodes that are connected in the envelope are connected in
 * any instantiation containing these two nodes. More formally:
 *
 *  - G = (V, E) \in [G_lb, G_ub], with G_ub = (V_ub, E_ub);
 *  - E = { (x, y) \in E_ub | x \in V \land y \in V }.
 *
 * @author Dimitri Justeau-Allaire
 * @since 15/04/2021
 */
public class UndirectedNodeInducedGraphVarImpl extends UndirectedGraphVarImpl {

    private UndirectedGraph originalUB;

    /**
     * Creates an undirected node-induced (from the envelope) graph variable
     *
     * @param name
     * @param solver
     * @param LB
     * @param UB
     */
    public UndirectedNodeInducedGraphVarImpl(String name, Model solver, UndirectedGraph LB, UndirectedGraph UB) {
        super(name, solver, LB, UB);
        this.originalUB = new UndirectedGraph(UB);
    }

    @Override
    public boolean enforceNode(int x, ICause cause) throws ContradictionException {
        boolean nodeEnforced = super.enforceNode(x, cause);
        if (!nodeEnforced) {
            return false;
        }
        boolean edgeEnforced = true;
        for (int y : originalUB.getNeighborsOf(x)) {
            if (LB.containsNode(y)) {
                if (!UB.containsEdge(x, y)) {
                    this.contradiction(cause, "Cannot enforce node " + x + " because edge (" + x + ", " + y + ") was removed from the envelope");
                }
                if (LB.addEdge(x, y)) {
                    if (reactOnModification) {
                        delta.add(x, GraphDelta.EDGE_ENFORCED_TAIL, cause);
                        delta.add(y, GraphDelta.EDGE_ENFORCED_HEAD, cause);
                    }
                    edgeEnforced = true;
                }
            } else if (UB.containsNode(y) && !UB.containsEdge(x, y)) {
                removeNode(y, cause);
            }
        }
        notifyPropagators(GraphEventType.ADD_NODE, cause);
        if (edgeEnforced) {
            notifyPropagators(GraphEventType.ADD_EDGE, cause);
        }
        return true;
    }

    @Override
    public boolean removeEdge(int x, int y, ICause cause) throws ContradictionException {
        boolean edgeRemoved = super.removeEdge(x, y, cause);
        if (!edgeRemoved) {
            return false;
        }
        boolean xInKer = getMandatoryNodes().contains(x);
        boolean yInKer = getMandatoryNodes().contains(y);
        if (xInKer && yInKer) {
            this.contradiction(cause, "Remove mandatory edge");
        }
        if (xInKer && !yInKer) {
            removeNode(y, cause);
        }
        if (!xInKer && yInKer) {
            removeNode(x, cause);
        }
        return true;
    }

    @Override
    public boolean removeNode(int x, ICause cause) throws ContradictionException {
        assert cause != null;
        assert (x >= 0 && x < n);
        if (LB.getNodes().contains(x)) {
            this.contradiction(cause, "remove mandatory node");
            return true;
        } else if (!UB.getNodes().contains(x)) {
            return false;
        }
        ISet nei = UB.getNeighborsOf(x);
        for (int i : nei) {
            super.removeEdge(x, i, cause);
        }
        if (UB.removeNode(x)) {
            if (reactOnModification) {
                delta.add(x, GraphDelta.NODE_REMOVED, cause);
            }
            GraphEventType e = GraphEventType.REMOVE_NODE;
            notifyPropagators(e, cause);
            return true;
        }
        return false;
    }
}
