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
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * Ensures that all non-empty sets are disjoint
 * In order to forbid multiple empty set, use propagator PropAtMost1Empty in addition
 *
 * @author Jean-Guillaume Fages
 */
public class PropAllDisjoint extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private int currentSet;
    private final ISetDeltaMonitor[] sdm;
    private final IntProcedure elementForced;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that all non-empty sets are disjoint
     * In order to forbid multiple empty set, use propagator PropAtMost1Empty in addition
     *
     * @param sets array of set variables
     */
    public PropAllDisjoint(SetVar[] sets) {
        super(sets, PropagatorPriority.LINEAR, true);
        n = sets.length;
        // delta monitors
        sdm = new ISetDeltaMonitor[n];
        for (int i = 0; i < n; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = element -> {
            for (int i = 0; i < n; i++) {
                if (i != currentSet) {
                    vars[i].remove(element, this);
                }
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return SetEventType.ADD_TO_KER.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < n; i++) {
                for (int j : vars[i].getLB()) {
                    for (int i2 = 0; i2 < n; i2++) {
                        if (i2 != i) {
                            vars[i2].remove(j, this);
                        }
                    }
                }
            }
            for (int i = 0; i < n; i++) {
                sdm[i].startMonitoring();
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        currentSet = idxVarInProp;
        sdm[currentSet].forEach(elementForced, SetEventType.ADD_TO_KER);
    }

    @Override
    public ESat isEntailed() {
        boolean allInstantiated = true;
        for (int i = 0; i < n; i++) {
            if (!vars[i].isInstantiated()) {
                allInstantiated = false;
            }
            ISetIterator iter = vars[i].getLB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
                for (int i2 = 0; i2 < n; i2++) {
                    if (i2 != i && vars[i2].getLB().contains(j)) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        if (allInstantiated) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
