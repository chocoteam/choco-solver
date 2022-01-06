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
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * @author Jean-Guillaume Fages
 */
public class PropNeighBoolsChannel2 extends Propagator<BoolVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final BoolVar[][] matrix;
    private final GraphVar g;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNeighBoolsChannel2(BoolVar[][] adjacencyMatrix, GraphVar gV) {
        super(ArrayUtils.flatten(adjacencyMatrix), PropagatorPriority.LINEAR, true);
        this.matrix = adjacencyMatrix;
        n = adjacencyMatrix.length;
        assert n == adjacencyMatrix[0].length;
        this.g = gV;
        assert (n == g.getNbMaxNodes());
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j].getLB() == 1) {
                    g.enforceEdge(i, j, this);
                } else if (matrix[i][j].getUB() == 0) {
                    g.removeEdge(i, j, this);
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        int i = idxVarInProp / n;
        int j = idxVarInProp % n;
        if (matrix[i][j].getLB() == 1) {
            g.enforceEdge(i, j, this);
        } else {
            g.removeEdge(i, j, this);
        }
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
