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

import gnu.trove.list.array.TIntArrayList;
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
 * Ensures that all sets are equal
 * @since 14/01/13
 * @author Jean-Guillaume Fages
 */
public class PropAllEqual extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final ISetDeltaMonitor[] sdm;
    private final IntProcedure elementForced;
    private final IntProcedure elementRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that all sets are equal
     *
     * @param sets array of set variables
     */
    public PropAllEqual(SetVar[] sets) {
        super(sets, PropagatorPriority.LINEAR, true);
        n = sets.length;
        // delta monitors
        sdm = new ISetDeltaMonitor[n];
        for (int i = 0; i < n; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = element -> {
            for (int i = 0; i < n; i++) {
                vars[i].force(element, this);
            }
        };
        elementRemoved = element -> {
            for (int i = 0; i < n; i++) {
                vars[i].remove(element, this);
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
			TIntArrayList toRemove = new TIntArrayList();
            ISetIterator iter = vars[0].getUB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
				for (int i = 1; i < n; i++) {
					if(!vars[i].getUB().contains(j)){
						toRemove.add(j);
						break;
					}
				}
			}
            for (int i = 0; i < n; i++) {
                iter = vars[i].getUB().iterator();
                while (iter.hasNext()){
                    int j = iter.nextInt();
					if((i>0 && !vars[0].getUB().contains(j)) || toRemove.contains(j)){
						vars[i].remove(j, this);
					}
				}
				iter = vars[i].getLB().iterator();
                while (iter.hasNext()){
                    int j = iter.nextInt();
                    for (int i2 = 0; i2 < n; i2++) {
                        vars[i2].force(j, this);
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
        sdm[idxVarInProp].forEach(elementForced, SetEventType.ADD_TO_KER);
        sdm[idxVarInProp].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
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
                    if (!vars[i2].getUB().contains(j)) {
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
