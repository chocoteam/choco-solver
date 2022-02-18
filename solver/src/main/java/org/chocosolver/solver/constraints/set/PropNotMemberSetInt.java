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
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.procedure.IntProcedure;

/**
 * 	Not Member propagator filtering Set->Int
 *  @author Jean-Guillaume Fages
 */
public class PropNotMemberSetInt extends Propagator<SetVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private final IntVar iv;
	private final SetVar sv;

	private final ISetDeltaMonitor sdm;
	private final IntProcedure elemRem;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

    public PropNotMemberSetInt(IntVar intVar, SetVar setVar) {
        super(new SetVar[]{setVar}, PropagatorPriority.UNARY, true);
        this.iv = intVar;
        this.sv = setVar;
        this.sdm = sv.monitorDelta(this);
        this.elemRem = i -> iv.removeValue(i, this);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vidx){
		return SetEventType.ADD_TO_KER.getMask();
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
        ISetIterator iter = sv.getLB().iterator();
        while (iter.hasNext()){
			iv.removeValue(iter.nextInt(), this);
		}
		if(sv.isInstantiated()) setPassive();
        sdm.startMonitoring();
	}

	@Override
	public void propagate(int idxVarInProp, int mask) throws ContradictionException {
		sdm.forEach(elemRem, SetEventType.ADD_TO_KER);
		if(sv.isInstantiated()) setPassive();
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
