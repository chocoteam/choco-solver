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
import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * @author Jean-Guillaume Fages
 */
public class PropNeighIntsChannel2 extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int n, currentSet;
    private IIntDeltaMonitor[] idm;
    private IntVar[] succs;
    private GraphVar g;
    private IntProcedure elementRemoved;
    private boolean dir;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNeighIntsChannel2(IntVar[] succs, GraphVar gV) {
        super(succs, PropagatorPriority.LINEAR, true);
        this.succs = succs;
        n = succs.length;
        g = gV;
        assert (n == g.getNbMaxNodes());
        dir = g.isDirected();
        idm = new IIntDeltaMonitor[n];
        for (int i = 0; i < n; i++) {
            idm[i] = succs[i].monitorDelta(this);
        }
        elementRemoved = element -> {
            if (dir || !succs[element].contains(currentSet)) {
                g.removeEdge(currentSet, element, this);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            g.enforceNode(i, this);
            if (succs[i].isInstantiated()) {
                g.enforceEdge(i, succs[i].getValue(), this);
            }
            ISet tmp = g.getPotentialSuccessorsOf(i);
            for (int j : tmp) {
                if (!succs[i].contains(j) && (dir || !succs[j].contains(i))) {
                    g.removeEdge(i, j, this);
                }
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        currentSet = idxVarInProp;
        if (vars[idxVarInProp].isInstantiated()) {
            g.enforceEdge(idxVarInProp, vars[idxVarInProp].getValue(), this);
        }
        idm[currentSet].forEachRemVal(elementRemoved);
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
                    if (g.isDirected() || !succs[j].contains(i)) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
