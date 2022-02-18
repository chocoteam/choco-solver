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
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;

/**
 * Not Member propagator filtering Int->Set
 *
 * @author Jean-Guillaume Fages
 */
public class PropNotMemberIntSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final IntVar iv;
    private final SetVar sv;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNotMemberIntSet(IntVar iv, SetVar sv) {
        super(new Variable[]{iv, sv}, PropagatorPriority.UNARY, true);
        this.iv = iv;
        this.sv = sv;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vidx) {
        if(vidx == 0) {
            return IntEventType.instantiation();
        }else{
            return SetEventType.VOID.getMask();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (iv.isInstantiated()) {
            sv.remove(iv.getValue(), this);
			setPassive();
        }
    }

    @Override
    public void propagate(int vidx, int evtmask) throws ContradictionException {
        assert iv.isInstantiated();
        sv.remove(iv.getValue(), this);
		setPassive();
    }

    @Override
    public ESat isEntailed() {
        if (iv.isInstantiated()) {
            int v = iv.getValue();
            if (sv.getUB().contains(v)) {
                if (sv.getLB().contains(v)) {
                    return ESat.FALSE;
                } else {
                    return ESat.UNDEFINED;
                }
            } else {
                return ESat.TRUE;
            }
        } else {
            for (int v = iv.getLB(); v <= iv.getUB(); v = iv.nextValue(v)) {
                if (!sv.getLB().contains(v)) {
                    return ESat.UNDEFINED;
                }
            }
        }
        return ESat.FALSE;
    }

}
