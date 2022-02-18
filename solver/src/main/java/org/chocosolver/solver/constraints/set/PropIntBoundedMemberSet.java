/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 16:36
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
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Propagator for Member constraint: iv is in set
 *
 * @author Jean-Guillaume Fages
 */
public class PropIntBoundedMemberSet extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final IntVar iv;
    private final SetVar set;

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
    public PropIntBoundedMemberSet(SetVar setVar, IntVar intVar) {
        super(new Variable[]{setVar, intVar}, PropagatorPriority.BINARY, false);
        assert !intVar.hasEnumeratedDomain();
        this.set = (SetVar) vars[0];
        this.iv = (IntVar) vars[1];
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return SetEventType.REMOVE_FROM_ENVELOPE.getMask();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
		if (iv.isInstantiated()) {
			set.force(iv.getValue(), this);
			setPassive();
		}else {
			ISet ub = set.getUB();
			if (ub.size() == 0) {
				fails();
			} else {
				if(ub.contains(iv.getLB()) && ub.contains(iv.getUB())){
					return;
				}
				iv.updateBounds(ub.min(), ub.max(), this);
				if (iv.isInstantiated()) {
					set.force(iv.getValue(), this);
					setPassive();
				}
			}
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
            int lb = iv.getLB();
            int ub = iv.getUB();
            boolean all = true;
            for (int i = lb; i <= ub; i++) {
                if (!set.getLB().contains(i)) {
                    all = false;
                    break;
                }
            }
            if (all) {
                return ESat.TRUE;
            }
            for (int i = lb; i <= ub; i++) {
                if (set.getUB().contains(i)) {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.FALSE;
        }
    }

}
