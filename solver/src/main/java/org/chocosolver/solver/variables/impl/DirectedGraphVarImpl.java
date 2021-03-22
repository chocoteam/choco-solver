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

import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.objects.graphs.DirectedGraph;

public class DirectedGraphVarImpl extends AbstractGraphVar<DirectedGraph> implements DirectedGraphVar {

    ////////////////////////////////// GRAPH PART ///////////////////////////////////////

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates a graph variable
     *
     * @param name
     * @param solver
     * @param LB
     * @param UB
     */
    public DirectedGraphVarImpl(String name, Model solver, DirectedGraph LB, DirectedGraph UB) {
        super(name, solver, LB, UB);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean removeEdge(int x, int y, ICause cause) throws ContradictionException {
        assert cause != null;
        if (LB.containsEdge(x, y)) {
            this.contradiction(cause, "remove mandatory edge " + x + "->" + y);
            return false;
        }
        if (UB.removeEdge(x, y)) {
            if (reactOnModification) {
                delta.add(x, GraphDelta.EDGE_REMOVED_TAIL, cause);
                delta.add(y, GraphDelta.EDGE_REMOVED_HEAD, cause);
            }
            GraphEventType e = GraphEventType.REMOVE_EDGE;
            notifyPropagators(e, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean enforceEdge(int x, int y, ICause cause) throws ContradictionException {
        assert cause != null;
        enforceNode(x, cause);
        enforceNode(y, cause);
        if (UB.containsEdge(x, y)) {
            if (LB.addEdge(x, y)) {
                if (reactOnModification) {
                    delta.add(x, GraphDelta.EDGE_ENFORCED_TAIL, cause);
                    delta.add(y, GraphDelta.EDGE_ENFORCED_HEAD, cause);
                }
                GraphEventType e = GraphEventType.ADD_EDGE;
                notifyPropagators(e, cause);
                return true;
            }
            return false;
        }
        this.contradiction(cause, "enforce edge which is not in the domain");
        return false;
    }
}
