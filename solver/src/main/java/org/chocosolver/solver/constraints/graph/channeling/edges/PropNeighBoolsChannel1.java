/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.edges;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * Channeling between a graph variable and set variables
 * representing either node neighbors or node successors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNeighBoolsChannel1 extends Propagator<GraphVar<?>> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final BoolVar[][] matrix;
    private final IGraphDeltaMonitor gdm;
    private final GraphVar<?> g;
    private final PairProcedure arcForced;
    private final PairProcedure arcRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNeighBoolsChannel1(BoolVar[][] adjacencyMatrix, GraphVar<?> gV) {
        super(new GraphVar[]{gV}, PropagatorPriority.LINEAR, true);
        this.matrix = adjacencyMatrix;
        n = matrix.length;
        assert n == matrix[0].length;
        this.g = gV;
        assert (n == g.getNbMaxNodes());
        gdm = g.monitorDelta(this);
        arcForced = (i, j) -> {
            matrix[i][j].setToTrue(this);
            if (!g.isDirected()) {
                matrix[j][i].setToTrue(this);
            }
        };
        arcRemoved = (i, j) -> {
            matrix[i][j].setToFalse(this);
            if (!g.isDirected()) {
                matrix[j][i].setToFalse(this);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (g.getMandatorySuccessorsOf(i).contains(j)) {
                    matrix[i][j].setToTrue(this);
                } else if (!g.getPotentialSuccessorsOf(i).contains(j)) {
                    matrix[i][j].setToFalse(this);
                }
            }
        }
        gdm.startMonitoring();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.forEachEdge(arcForced, GraphEventType.ADD_EDGE);
        gdm.forEachEdge(arcRemoved, GraphEventType.REMOVE_EDGE);
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j].getLB() == 1 && !g.getPotentialSuccessorsOf(i).contains(j)) {
                    return ESat.FALSE;
                } else if (matrix[i][j].getUB() == 0 && g.getMandatorySuccessorsOf(i).contains(j)) {
                    return ESat.FALSE;
                }
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
