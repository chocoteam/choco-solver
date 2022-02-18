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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * Propagator for Member constraint: iv is in set
 * @since 14/01/13
 * @author Charles Prud'homme
 */
public class PropIntEnumMemberSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private IntVar iv;
    private final SetVar set;
    private final ISetDeltaMonitor sdm;
    private final IntProcedure elemRem;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for Member constraint
     * val(intVar) is in setVar
     *
     * @param setVar a set variable
     * @param intVar an integer variable
     */
    public PropIntEnumMemberSet(SetVar setVar, IntVar intVar) {
        super(new Variable[]{setVar, intVar}, PropagatorPriority.BINARY, true);
        assert intVar.hasEnumeratedDomain() : iv.toString() + " does not an enumerated domain";
        this.set = (SetVar) vars[0];
        this.iv = (IntVar) vars[1];
        this.sdm = set.monitorDelta(this);
        elemRem = i -> iv.removeValue(i, this);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
        } else {
            return IntEventType.INSTANTIATE.getMask();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (iv.isInstantiated()) {
            set.force(iv.getValue(), this);
            setPassive();
            return;
        }
        int ub = iv.getUB();
        for (int i = iv.getLB(); i <= ub; i = iv.nextValue(i)) {
            if (!set.getUB().contains(i)) {
                iv.removeValue(i, this);
            }
        }
        // now iv \subseteq set
        if (iv.isInstantiated()) {
            set.force(iv.getValue(), this);
            setPassive();
        }
        sdm.startMonitoring();
    }

    @Override
    public void propagate(int i, int mask) throws ContradictionException {
        if (i == 0) {
            sdm.forEach(elemRem, SetEventType.REMOVE_FROM_ENVELOPE);
        }
        if (iv.isInstantiated()) {
            set.force(iv.getValue(), this);
            setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if (iv.isInstantiated()) {
            if (!set.getUB().contains(iv.getValue())) {
                return ESat.FALSE;
            } else {
                if (set.getLB().contains(iv.getValue())) {
                    return ESat.TRUE;
                } else {
                    return ESat.UNDEFINED;
                }
            }
        } else {
            int minVal = iv.getLB();
            int maxVal = iv.getUB();
            boolean all = true;
            for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
                if (!set.getLB().contains(i)) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return ESat.TRUE;
            }
            for (int i = minVal; i <= maxVal; i = iv.nextValue(i)) {
                if (set.getUB().contains(i)) {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.FALSE;
        }
    }

}
