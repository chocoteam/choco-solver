/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.delta.GraphDelta;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.objects.graphs.UndirectedGraph;

public class UndirectedGraphVarImpl extends AbstractGraphVar<UndirectedGraph> implements UndirectedGraphVar {

    //////////////////////////////// GRAPH PART /////////////////////////////////////////

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
    public UndirectedGraphVarImpl(String name, Model solver, UndirectedGraph LB, UndirectedGraph UB) {
        super(name, solver, LB, UB);
    }

    public boolean removeNode(int x, ICause cause) throws ContradictionException {
        assert cause != null;
        assert (x >= 0 && x < n);
        if (LB.getNodes().contains(x)) {
            this.contradiction(cause, "remove mandatory node");
            return true;
        } else if (!UB.getNodes().contains(x)) {
            return false;
        }
        int[] nei = UB.getNeighborsOf(x).toArray();
        if (UB.removeNode(x)) {
            if (reactOnModification) {
                for (int i : nei) {
                    delta.add(x, GraphDelta.EDGE_REMOVED_TAIL, cause);
                    delta.add(i, GraphDelta.EDGE_REMOVED_HEAD, cause);
                }
                delta.add(x, GraphDelta.NODE_REMOVED, cause);
            }
            if (nei.length > 0) {
                notifyPropagators(GraphEventType.REMOVE_EDGE, cause);
            }
            notifyPropagators(GraphEventType.REMOVE_NODE, cause);
            return true;
        }
        return false;
    }
}
