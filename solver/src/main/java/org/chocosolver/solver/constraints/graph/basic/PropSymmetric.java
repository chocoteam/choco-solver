/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * Ensures that the final graph is symmetric
 * i.e. if G has arc (x,y) then it also has (y,x)
 * <p>
 * Note that it may be preferable to use an undirected graph variable instead!
 *
 * @author Jean-Guillaume Fages
 */
public class PropSymmetric extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final DirectedGraphVar g;
    private final IGraphDeltaMonitor gdm;
    private final PairProcedure enf;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropSymmetric(DirectedGraphVar graph) {
        super(new DirectedGraphVar[]{graph}, PropagatorPriority.UNARY, true);
        g = graph;
        gdm = g.monitorDelta(this);
        enf = (from, to) -> {
            if (from != to) {
                g.enforceEdge(to, from, PropSymmetric.this);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ISet ker = g.getMandatoryNodes();
        ISet succ;
        for (int i : ker) {
            succ = g.getMandatorySuccessorsOf(i);
            for (int j : succ) {
                g.enforceEdge(j, i, this);
            }
        }
        gdm.startMonitoring();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.forEachEdge(enf, GraphEventType.ADD_EDGE);
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return GraphEventType.ADD_EDGE.getMask();
    }

    @Override
    public ESat isEntailed() {
        ISet ker = g.getMandatoryNodes();
        ISet succ;
        for (int i : ker) {
            succ = g.getMandatorySuccessorsOf(i);
            for (int j : succ) {
                if (!g.getPotentialSuccessorsOf(j).contains(i)) {
                    return ESat.FALSE;
                }
            }
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
