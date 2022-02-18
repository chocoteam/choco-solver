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
public class PropUnion extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int k;
    private final ISetDeltaMonitor[] sdm;
    private final IntProcedure unionForced;
    private final IntProcedure unionRemoved;
    private final IntProcedure setForced;
    private final IntProcedure setRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * The union of sets is equal to union
     *
     * @param sets set variables to unify
     * @param union resulting set variable
     */
    public PropUnion(SetVar[] sets, SetVar union) {
        super(ArrayUtils.append(sets, new SetVar[]{union}), PropagatorPriority.LINEAR, true);
        k = sets.length;
        sdm = new ISetDeltaMonitor[k + 1];
        for (int i = 0; i <= k; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        // PROCEDURES
        unionForced = element -> {
            int mate = -1;
            for (int i = 0; i < k && mate != -2; i++) {
                if (vars[i].getUB().contains(element)) {
                    if (mate == -1) {
                        mate = i;
                    } else {
                        mate = -2;
                    }
                }
            }
            if (mate == -1) {
                fails(); // TODO: could be more precise, for explanation purpose
            } else if (mate != -2) {
                vars[mate].force(element, this);
            }
        };
        unionRemoved = element -> {
            for (int i = 0; i < k; i++) {
                vars[i].remove(element, this);
            }
        };
        setForced = element -> vars[k].force(element, this);
        setRemoved = element -> {
            if (vars[k].getUB().contains(element)) {
                int mate = -1;
                for (int i = 0; i < k && mate != -2; i++) {
                    if (vars[i].getUB().contains(element)) {
                        if (mate == -1) {
                            mate = i;
                        } else {
                            mate = -2;
                        }
                    }
                }
                if (mate == -1) {
                    vars[k].remove(element, this);
                } else if (mate != -2 && vars[k].getLB().contains(element)) {
                    vars[mate].force(element, this);
                }
            }
        };
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            SetVar union = vars[k];
            for (int i = 0; i < k; i++) {
                ISetIterator iter = vars[i].getLB().iterator();
                while(iter.hasNext())
                    union.force(iter.nextInt(),this);
                iter = vars[i].getUB().iterator();
                while(iter.hasNext()) {
                    int j = iter.nextInt();
                    if (!union.getUB().contains(j))
                        vars[i].remove(j, this);
                }
            }
            ISetIterator unionUB = union.getUB().iterator();
            while (unionUB.hasNext()) {
                int j = unionUB.nextInt();
                if (union.getLB().contains(j)) {
                    int mate = -1;
                    for (int i = 0; i < k && mate != -2; i++) {
                        if (vars[i].getUB().contains(j)) {
                            if (mate == -1) {
                                mate = i;
                            } else {
                                mate = -2;
                            }
                        }
                    }
                    if (mate == -1) {
                        fails(); // TODO: could be more precise, for explanation purpose
                    } else if (mate != -2) {
                        vars[mate].force(j, this);
                    }
                } else {
                    int mate = -1;
                    for (int i = 0; i < k; i++) {
                        if (vars[i].getUB().contains(j)) {
                            mate = i;
                            break;
                        }
                    }
                    if (mate == -1) union.remove(j, this);
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
            sdm[idxVarInProp].forEach(unionForced, SetEventType.ADD_TO_KER);
            sdm[idxVarInProp].forEach(unionRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < k; i++) {
            for (int j : vars[i].getLB())
                if (!vars[k].getUB().contains(j))
                    return ESat.FALSE;
        }
        for (int j : vars[k].getLB()) {
            int mate = -1;
            for (int i = 0; i < k; i++)
                if (vars[i].getUB().contains(j)) {
                    mate = i;
                    break;
                }
            if (mate == -1) return ESat.FALSE;
        }
        if (isCompletelyInstantiated()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

}
