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
import org.chocosolver.solver.variables.IncidentSet;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * @author Jean-Guillaume Fages
 */
public class PropNeighBoolChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final BoolVar[] bools;
    private final GraphVar g;
    private final int vertex;
    private final IncidentSet inc;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNeighBoolChannel(BoolVar[] neigh, final int vertex, GraphVar gV, IncidentSet incSet) {
        super(ArrayUtils.append(neigh, new Variable[]{gV}), PropagatorPriority.LINEAR, true);
        this.vertex = vertex;
        this.bools = neigh;
        this.g = gV;
        this.inc = incSet;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == bools.length) {
            return GraphEventType.ADD_EDGE.getMask() + GraphEventType.REMOVE_EDGE.getMask();
        } else {
            return IntEventType.all();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i : inc.getPotentialSet(g, vertex)) {
            if (bools[i].getUB() == 0) {
                inc.remove(g, vertex, i, this);
            } else if (bools[i].getLB() == 1) {
                inc.enforce(g, vertex, i, this);
            }
        }
        for (int i = 0; i < bools.length; i++) {
            if (!inc.getPotentialSet(g, vertex).contains(i)) {
                bools[i].setToFalse(this);
            } else if (inc.getMandatorySet(g, vertex).contains(i)) {
                bools[i].setToTrue(this);
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < bools.length) {
            if (bools[idxVarInProp].getLB() == 1) {
                inc.enforce(g, vertex, idxVarInProp, this);
            } else {
                inc.remove(g, vertex, idxVarInProp, this);
            }
        } else {
            for (int i = 0; i < bools.length; i++) {
                if (!inc.getPotentialSet(g, vertex).contains(i)) {
                    bools[i].setToFalse(this);
                } else if (inc.getMandatorySet(g, vertex).contains(i)) {
                    bools[i].setToTrue(this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < bools.length; i++) {
            if (bools[i].getLB() == 1 && !inc.getPotentialSet(g, vertex).contains(i)) {
                return ESat.FALSE;
            }
        }
        for (int i : inc.getMandatorySet(g, vertex)) {
            if (bools[i].getUB() == 0) {
                return ESat.FALSE;
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
