/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.nodes;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * Channeling between a graph variable and set variables
 * representing either node neighbors or node successors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNodeSetChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final SetVar set;
    private final GraphVar<?> g;
    private final ISetDeltaMonitor sdm;
    private final IGraphDeltaMonitor gdm;
    private final IntProcedure forceG;
    private final IntProcedure forceS;
    private final IntProcedure remG;
    private final IntProcedure remS;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNodeSetChannel(SetVar vertexSet, GraphVar<?> gV) {
        super(new Variable[]{vertexSet, gV}, PropagatorPriority.LINEAR, true);
        this.set = vertexSet;
        this.g = gV;
        sdm = set.monitorDelta(this);
        gdm = g.monitorDelta(this);
        forceS = element -> g.enforceNode(element, this);
        remS = element -> g.removeNode(element, this);
        forceG = element -> set.force(element, this);
        remG = element -> set.remove(element, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return SetEventType.ADD_TO_KER.getMask() + SetEventType.REMOVE_FROM_ENVELOPE.getMask();
        } else {
            return GraphEventType.ADD_NODE.getMask() + GraphEventType.REMOVE_NODE.getMask();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i : set.getUB()) {
            if (g.getMandatoryNodes().contains(i)) {
                set.force(i, this);
            } else if (!g.getPotentialNodes().contains(i)) {
                set.remove(i, this);
            }
        }
        for (int i : g.getPotentialNodes()) {
            if (set.getLB().contains(i)) {
                g.enforceNode(i, this);
            } else if (!set.getUB().contains(i)) {
                g.removeNode(i, this);
            }
        }
        sdm.startMonitoring();
        gdm.startMonitoring();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            sdm.forEach(forceS, SetEventType.ADD_TO_KER);
            sdm.forEach(remS, SetEventType.REMOVE_FROM_ENVELOPE);
        } else {
            gdm.forEachNode(forceG, GraphEventType.ADD_NODE);
            gdm.forEachNode(remG, GraphEventType.REMOVE_NODE);
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i : set.getLB()) {
            if (!g.getPotentialNodes().contains(i)) {
                return ESat.FALSE;
            }
        }
        for (int i : g.getMandatoryNodes()) {
            if (!set.getUB().contains(i)) {
                return ESat.FALSE;
            }
        }
        int n = g.getMandatoryNodes().size();
        if (n == g.getPotentialNodes().size() && n == set.getUB().size() && n == set.getLB().size()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
