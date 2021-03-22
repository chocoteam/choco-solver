/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.channeling.edges;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.DirectedGraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IGraphDeltaMonitor;
import org.chocosolver.solver.variables.events.GraphEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.PairProcedure;

/**
 * @author Jean-Guillaume Fages
 */
public class PropSuccIntsChannel1 extends Propagator<DirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n;
    private IntVar[] succs;
    private IGraphDeltaMonitor gdm;
    private DirectedGraphVar g;
    private PairProcedure arcForced, arcRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropSuccIntsChannel1(final IntVar[] succs, DirectedGraphVar gV) {
        super(new DirectedGraphVar[]{gV}, PropagatorPriority.LINEAR, true);
        this.succs = succs;
        n = succs.length;
        this.g = gV;
        assert (n == g.getNbMaxNodes());
        gdm = g.monitorDelta(this);
        for (int i = 0; i < n; i++) {
            assert succs[i].hasEnumeratedDomain() : "channeling variables should be enumerated";
        }
        arcForced = (i, j) -> succs[i].instantiateTo(j, this);
        arcRemoved = (i, j) -> succs[i].removeValue(j, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            ISet tmp = g.getMandatorySuccessorsOf(i);
            for (int j : tmp) {
                succs[i].instantiateTo(j, this);
            }
            for (int j = succs[i].getLB(); j <= succs[i].getUB(); j = succs[i].nextValue(j)) {
                if (!g.getPotentialSuccessorsOf(i).contains(j)) {
                    succs[i].removeValue(j, this);
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.forEachEdge(arcForced, GraphEventType.ADD_EDGE);
        gdm.forEachEdge(arcRemoved, GraphEventType.REMOVE_EDGE);
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            if (succs[i].isInstantiated() && !g.getPotentialSuccessorsOf(i).contains(succs[i].getValue())) {
                return ESat.FALSE;
            }
            ISet tmp = g.getMandatorySuccessorsOf(i);
            for (int j : tmp) {
                if (!succs[i].contains(j)) {
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
