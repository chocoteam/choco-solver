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
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * Propagator for symmetric sets
 * x in set[y-offSet] <=> y in set[x-offSet]
 *
 * @since 14/01/13
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 */
public class PropSymmetric extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private int currentSet;
    private final int offSet;
    private final ISetDeltaMonitor[] sdm;
    private final IntProcedure elementForced;
    private final IntProcedure elementRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for symmetric sets
     * x in set[y-offSet] <=> y in set[x-offSet]
     */
    public PropSymmetric(SetVar[] sets, final int offSet) {
        super(sets, PropagatorPriority.LINEAR, true);
        n = sets.length;
        this.offSet = offSet;
        sdm = new ISetDeltaMonitor[n];
        for (int i = 0; i < n; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = element -> vars[element - offSet].force(currentSet + offSet, this);
        elementRemoved = element -> vars[element - offSet].remove(currentSet + offSet, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            ISetIterator iter = vars[i].getUB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
                if (j < offSet || j >= n + offSet || !vars[j - offSet].getUB().contains(i + offSet)) {
                    vars[i].remove(j, this);
                }
            }
            iter = vars[i].getLB().iterator();
            while (iter.hasNext()){
                vars[iter.nextInt() - offSet].force(i + offSet, this);
            }
        }
        for (int i = 0; i < n; i++) {
            sdm[i].startMonitoring();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        currentSet = idxVarInProp;
        sdm[currentSet].forEach(elementForced, SetEventType.ADD_TO_KER);
        sdm[currentSet].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < n; i++) {
            for (int j : vars[i].getLB()) {
                if (!vars[j - offSet].getUB().contains(i + offSet)) {
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
