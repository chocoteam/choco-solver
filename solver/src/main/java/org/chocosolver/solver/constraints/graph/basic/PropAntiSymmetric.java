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
 * Ensures that the final graph is antisymmetric
 * i.e. if G has arc (x,y) then it does not have (y,x)
 * Except for loops : (x,x) is allowed
 *
 * @author Jean-Guillaume Fages
 */
public class PropAntiSymmetric extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final DirectedGraphVar g;
    private final IGraphDeltaMonitor gdm;
    private final PairProcedure remove;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropAntiSymmetric(DirectedGraphVar graph) {
        super(new DirectedGraphVar[]{graph}, PropagatorPriority.UNARY, true);
        g = graph;
        gdm = g.monitorDelta(this);
        remove = (from, to) -> {
            if (from != to) {
                g.removeEdge(to, from, PropAntiSymmetric.this);
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
                if (i != j) {
                    g.removeEdge(j, i, this);
                }
            }
        }
        gdm.startMonitoring();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.forEachEdge(remove, GraphEventType.ADD_EDGE);
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
                if (j != i) {
                    if (g.getMandatorySuccessorsOf(j).contains(i)) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        if (g.isInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
