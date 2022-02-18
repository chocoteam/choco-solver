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
 * Ensures that X subseteq Y
 *
 * @since 14/01/13
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 */
public class PropSubsetEq extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final ISetDeltaMonitor[] sdm;
    private final IntProcedure elementForced;
    private final IntProcedure elementRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Ensures that X subseteq Y
     *
     * @param X a set variable
     * @param Y a set variable
     */
    public PropSubsetEq(SetVar X, SetVar Y) {
        super(new SetVar[]{X, Y}, PropagatorPriority.LINEAR, true);
        // delta monitors
        sdm = new ISetDeltaMonitor[2];
        for (int i = 0; i < 2; i++) {
            sdm[i] = this.vars[i].monitorDelta(this);
        }
        elementForced = element -> vars[1].force(element, this);
        elementRemoved = element -> vars[0].remove(element, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0)
            return SetEventType.ADD_TO_KER.getMask();
        else
            return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ISetIterator iter = vars[0].getLB().iterator();
        while (iter.hasNext()){
            vars[1].force(iter.nextInt(), this);
        }
        iter = vars[0].getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            if (!vars[1].getUB().contains(j))
                vars[0].remove(j, this);
        }
        sdm[0].startMonitoring();
        sdm[1].startMonitoring();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (i == 0)
            sdm[i].forEach(elementForced, SetEventType.ADD_TO_KER);
        else
            sdm[i].forEach(elementRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
    }

    @Override
    public ESat isEntailed() {
        ISetIterator iter = vars[0].getLB().iterator();
        while (iter.hasNext()){
            if (!vars[1].getUB().contains(iter.nextInt())) {
                return ESat.FALSE;
            }
        }
        iter = vars[0].getUB().iterator();
        while (iter.hasNext()){
            if (!vars[1].getLB().contains(iter.nextInt())) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }

}
