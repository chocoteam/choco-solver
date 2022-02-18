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
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * Channeling between a graph variable and set variables
 * representing either node neighbors or node successors
 *
 * @author Jean-Guillaume Fages
 */
public class PropNeighSetsChannel1 extends Propagator<GraphVar<?>> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final SetVar[] sets;
    private final IGraphDeltaMonitor gdm;
    private final GraphVar<?> g;
    private final PairProcedure arcForced;
    private final PairProcedure arcRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Channeling between a graph variable and set variables
     * representing either node neighbors or node successors
     */
    public PropNeighSetsChannel1(SetVar[] setsV, GraphVar<?> gV) {
        super(new GraphVar[]{gV}, PropagatorPriority.LINEAR, true);
        this.sets = setsV;
        n = sets.length;
        this.g = gV;
        assert (n == g.getNbMaxNodes());
        gdm = g.monitorDelta(this);
        arcForced = (i, j) -> {
            sets[i].force(j, this);
            if (!g.isDirected()) {
                sets[j].force(i, this);
            }
        };
        arcRemoved = (i, j) -> {
            sets[i].remove(j, this);
            if (!g.isDirected()) {
                sets[j].remove(i, this);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            ISet tmp = g.getMandatorySuccessorsOf(i);
            for (int j : tmp) {
                sets[i].force(j, this);
                if (!g.isDirected()) {
                    sets[j].force(i, this);
                }
            }
            for (int j : sets[i].getUB()) {
                if (!g.getPotentialSuccessorsOf(i).contains(j)) {
                    sets[i].remove(j, this);
                    if (!g.isDirected()) {
                        sets[j].remove(i, this);
                    }
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
            for (int j : sets[i].getLB()) {
                if (!g.getPotentialSuccessorsOf(i).contains(j)) {
                    return ESat.FALSE;
                }
            }
            ISet tmp = g.getMandatorySuccessorsOf(i);
            for (int j : tmp) {
                if (!sets[i].getUB().contains(j)) {
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
