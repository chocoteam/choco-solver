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
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IncidentSet;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * @author Jean-Guillaume Fages
 */
public class PropNeighSetChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final SetVar set;
    private final GraphVar<?> g;
    private final int vertex;
    private final IncidentSet inc;
    private final ISetDeltaMonitor sdm;
    private final IntProcedure forceS;
    private final IntProcedure remS;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNeighSetChannel(SetVar neigh, final int vertex, GraphVar<?> gV, IncidentSet incSet) {
        super(new Variable[]{neigh, gV}, PropagatorPriority.LINEAR, true);
        this.vertex = vertex;
        this.set = neigh;
        this.g = gV;
        this.inc = incSet;
        sdm = set.monitorDelta(this);
        forceS = element -> inc.enforce(g, vertex, element, this);
        remS = element -> inc.remove(g, vertex, element, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return SetEventType.ADD_TO_KER.getMask() + SetEventType.REMOVE_FROM_ENVELOPE.getMask();
        } else {
            return GraphEventType.ADD_EDGE.getMask() + GraphEventType.REMOVE_EDGE.getMask();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i : inc.getPotentialSet(g, vertex)) {
            if (!set.getUB().contains(i)) {
                inc.remove(g, vertex, i, this);
            } else if (set.getLB().contains(i)) {
                inc.enforce(g, vertex, i, this);
            }
        }
        for (int i : set.getUB()) {
            if (!inc.getPotentialSet(g, vertex).contains(i)) {
                set.remove(i, this);
            } else if (inc.getMandatorySet(g, vertex).contains(i)) {
                set.force(i, this);
            }
        }
        sdm.startMonitoring();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            sdm.forEach(forceS, SetEventType.ADD_TO_KER);
            sdm.forEach(remS, SetEventType.REMOVE_FROM_ENVELOPE);
        } else {
            for (int i : set.getUB()) {
                if (!inc.getPotentialSet(g, vertex).contains(i)) {
                    set.remove(i, this);
                } else if (inc.getMandatorySet(g, vertex).contains(i)) {
                    set.force(i, this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i : set.getLB()) {
            if (!inc.getPotentialSet(g, vertex).contains(i)) {
                return ESat.FALSE;
            }
        }
        for (int i : inc.getMandatorySet(g, vertex)) {
            if (!set.getUB().contains(i)) {
                return ESat.FALSE;
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
