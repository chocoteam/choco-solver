/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * set2 is an offSet view of set1
 * x in set1 <=> x+offSet in set2
 *
 * @author Jean-Guillaume Fages
 */
public class PropOffSet extends Propagator<SetVar> {

    private final int offSet;
    private int tmp;
    private SetVar tmpSet;
    private final IntProcedure forced;
    private final IntProcedure removed;
    private final ISetDeltaMonitor[] sdm;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * set2 is an offSet view of set1
     * x in set1 <=> x+offSet in set2
     */
    public PropOffSet(SetVar set1, SetVar set2, int offSet) {
        super(new SetVar[]{set1, set2}, PropagatorPriority.UNARY, true);
        this.offSet = offSet;
        sdm = new ISetDeltaMonitor[2];
        sdm[0] = vars[0].monitorDelta(this);
        sdm[1] = vars[1].monitorDelta(this);
        this.forced = i -> tmpSet.force(i + tmp, this);
        this.removed = i -> tmpSet.remove(i + tmp, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // kernel
        for (int j : vars[0].getLB()) {
            vars[1].force(j + offSet, this);
        }
        for (int j : vars[1].getLB()) {
            vars[0].force(j - offSet, this);
        }
        // envelope
        for (int j : vars[0].getUB()) {
            if (!vars[1].getUB().contains(j + offSet)) {
                vars[0].remove(j, this);
            }
        }
        for (int j : vars[1].getUB()) {
            if (!vars[0].getUB().contains(j - offSet)) {
                vars[1].remove(j, this);
            }
        }
        sdm[0].startMonitoring();
        sdm[1].startMonitoring();
    }

    @Override
    public void propagate(int v, int mask) throws ContradictionException {
        if (v == 0) {
            tmp = offSet;
            tmpSet = vars[1];
        } else {
            tmp = -offSet;
            tmpSet = vars[0];
        }
        sdm[v].forEach(forced, SetEventType.ADD_TO_KER);
        sdm[v].forEach(removed, SetEventType.REMOVE_FROM_ENVELOPE);
    }

    @Override
    public ESat isEntailed() {
        for (int j : vars[0].getLB()) {
            if (!vars[1].getUB().contains(j + offSet)) {
                return ESat.FALSE;
            }
        }
        for (int j : vars[1].getLB()) {
            if (!vars[0].getUB().contains(j - offSet)) {
                return ESat.FALSE;
            }
        }
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
