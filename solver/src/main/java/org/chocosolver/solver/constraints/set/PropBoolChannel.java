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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Channeling between a set variable and boolean variables
 * @since 14/01/13
 * @author Jean-Guillaume Fages
 */
public class PropBoolChannel extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final int offSet;
    private final BoolVar[] bools;
    private final SetVar set;
    private final ISetDeltaMonitor sdm;
    private final IntProcedure setForced;
    private final IntProcedure setRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Channeling between a set variable and boolean variables
     * i in setVar <=> boolVars[i-offSet] = TRUE
     *
     * @param setVar a set variable
     * @param boolVars an array of boolean variables
     */
    public PropBoolChannel(SetVar setVar, BoolVar[] boolVars, final int offSet) {
        super(ArrayUtils.append(boolVars, new Variable[]{setVar}), PropagatorPriority.UNARY, true);
        this.n = boolVars.length;
        this.bools = new BoolVar[n];
        for (int i = 0; i < n; i++) {
            this.bools[i] = (BoolVar) vars[i];
        }
        this.set = (SetVar) vars[n];
        this.sdm = this.set.monitorDelta(this);
        this.offSet = offSet;
        // PROCEDURES
        setForced = element -> bools[element - offSet].setToTrue(this);
        setRemoved = element -> bools[element - offSet].setToFalse(this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        for (int i = 0; i < n; i++) {
            if (bools[i].isInstantiated()) {
                if (bools[i].getValue() == 0) {
                    set.remove(i + offSet, this);
                } else {
                    set.force(i + offSet, this);
                }
            } else if (!set.getUB().contains(i + offSet)) {
                bools[i].setToFalse(this);
            }
        }
        ISetIterator iter = set.getUB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            if (j < offSet || j >= n + offSet) {
                set.remove(j, this);
            }
        }
        iter = set.getLB().iterator();
        while (iter.hasNext()){
            int j = iter.nextInt();
            bools[j - offSet].setToTrue(this);
        }
        sdm.startMonitoring();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (i < n) {
            if (bools[i].getValue() == 0) {
                set.remove(i + offSet, this);
            } else {
                set.force(i + offSet, this);
            }
        } else {
            sdm.forEach(setForced, SetEventType.ADD_TO_KER);
            sdm.forEach(setRemoved, SetEventType.REMOVE_FROM_ENVELOPE);
        }
    }

    @Override
    public ESat isEntailed() {
        ISetIterator iter = set.getLB().iterator();
        while (iter.hasNext()){
            int i = iter.nextInt() - offSet;
            if (i < 0 || i >= bools.length || bools[i].isInstantiatedTo(0)) {
                return ESat.FALSE;
            }
        }
        for (int i = 0; i < n; i++) {
            if (bools[i].isInstantiatedTo(1)) {
                if (!set.getUB().contains(i + offSet)) {
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
