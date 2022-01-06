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
import org.chocosolver.util.tools.ArrayUtils;

/**
 * @since 14/01/13
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 */
public class PropIntersection extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int k;
    private final ISetDeltaMonitor[] sdm;
    private final IntProcedure intersectionForced;
    private final IntProcedure intersectionRemoved;
    private final IntProcedure setForced;
    private final IntProcedure setRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropIntersection(SetVar[] sets, SetVar intersection) {
        super(ArrayUtils.append(sets, new SetVar[]{intersection}), PropagatorPriority.LINEAR, true);
        k = sets.length;
        sdm = new ISetDeltaMonitor[k + 1];
        for (int i = 0; i <= k; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        // PROCEDURES
        intersectionForced = element -> {
            for (int i = 0; i < k; i++) {
                vars[i].force(element, this);
            }
        };
        intersectionRemoved = element -> {
            int mate = -1;
            for (int i = 0; i < k; i++)
                if (vars[i].getUB().contains(element)) {
                    if (!vars[i].getLB().contains(element)) {
                        if (mate == -1) {
                            mate = i;
                        } else {
                            mate = -2;
                            break;
                        }
                    }
                } else {
                    mate = -2;
                    break;
                }
            if (mate == -1) {
                fails(); // TODO: could be more precise, for explanation purpose
            } else if (mate != -2) {
                vars[mate].remove(element, this);
            }
        };
        setForced = element -> {
            boolean allKer = true;
            for (int i = 0; i < k; i++) {
                if (!vars[i].getUB().contains(element)) {
                    vars[k].remove(element, this);
                    allKer = false;
                    break;
                } else if (!vars[i].getLB().contains(element)) {
                    allKer = false;
                }
            }
            if (allKer) {
                vars[k].force(element, this);
            }
        };
        setRemoved = element -> vars[k].remove(element, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        SetVar intersection = vars[k];
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            ISetIterator iter = vars[0].getLB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
                boolean all = true;
                for (int i = 1; i < k; i++) {
                    if (!vars[i].getLB().contains(j)) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    intersection.force(j, this);
                }
            }
            iter = intersection.getUB().iterator();
            while (iter.hasNext()){
                int j = iter.nextInt();
                if (intersection.getLB().contains(j)) {
                    for (int i = 0; i < k; i++) {
                        vars[i].force(j, this);
                    }
                } else {
                    for (int i = 0; i < k; i++)
                        if (!vars[i].getUB().contains(j)) {
                            intersection.remove(j, this);
                            break;
                        }
                }
            }
            for (int i = 0; i <= k; i++) {
                sdm[i].startMonitoring();
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp < k) {
            sdm[idxVarInProp].forEach(setForced, SetEventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(setRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        } else {
            sdm[idxVarInProp].forEach(intersectionForced, SetEventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(intersectionRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        }
    }

    @Override
    public ESat isEntailed() {
        ISetIterator iter = vars[k].getLB().iterator();
        while (iter.hasNext()) {
            int j = iter.nextInt();
            for (int i = 0; i < k; i++)
                if (!vars[i].getUB().contains(j)) {
                    return ESat.FALSE;
                }
        }
        iter = vars[0].getLB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            if (!vars[k].getUB().contains(j)) {
                boolean all = true;
                for (int i = 1; i < k; i++) {
                    if (!vars[i].getLB().contains(j)) {
                        all = false;
                        break;
                    }
                }
                if (all) {
                    return ESat.FALSE;
                }
            }
        }
        if (isCompletelyInstantiated()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

}
