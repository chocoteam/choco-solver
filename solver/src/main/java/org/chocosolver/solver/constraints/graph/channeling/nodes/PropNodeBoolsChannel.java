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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Channeling between a graph variable and set variables
 * representing either node neighbors or node successors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNodeBoolsChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final BoolVar[] bools;
    private final GraphVar<?> g;
    private final IGraphDeltaMonitor gdm;
    private final IntProcedure remG;
    private final IntProcedure forceG;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNodeBoolsChannel(BoolVar[] vertices, GraphVar<?> gV) {
        super(ArrayUtils.append(vertices, new Variable[]{gV}), PropagatorPriority.LINEAR, true);
        this.bools = vertices;
        this.g = gV;
        gdm = g.monitorDelta(this);
        forceG = element -> bools[element].setToTrue(this);
        remG = element -> bools[element].setToFalse(this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == bools.length) {
            return GraphEventType.ADD_NODE.getMask() + GraphEventType.REMOVE_NODE.getMask();
        } else {
            return IntEventType.all();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < bools.length; i++) {
            if (!g.getPotentialNodes().contains(i)) {
                bools[i].setToFalse(this);
            } else if (g.getMandatoryNodes().contains(i)) {
                bools[i].setToTrue(this);
            }
        }
        for (int i : g.getPotentialNodes()) {
            if (!bools[i].contains(1)) {
                g.removeNode(i, this);
            } else if (bools[i].getLB() == 1) {
                g.enforceNode(i, this);
            }
        }
        gdm.startMonitoring();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < bools.length) {
            if (bools[idxVarInProp].getValue() == 1) {
                g.enforceNode(idxVarInProp, this);
            } else {
                g.removeNode(idxVarInProp, this);
            }
        } else {
            gdm.forEachNode(forceG, GraphEventType.ADD_NODE);
            gdm.forEachNode(remG, GraphEventType.REMOVE_NODE);
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < bools.length; i++) {
            if (bools[i].getLB() == 1 && !g.getPotentialNodes().contains(i)) {
                return ESat.FALSE;
            }
            if (bools[i].getUB() == 0 && g.getMandatoryNodes().contains(i)) {
                return ESat.FALSE;
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
