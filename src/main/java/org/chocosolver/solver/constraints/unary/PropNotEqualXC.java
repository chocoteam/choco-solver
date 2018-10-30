/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * A propagator ensuring that:
 * X =/= C, where X is a variable and C a constant
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
public class PropNotEqualXC extends Propagator<IntVar> {

    private final int constant;

    public PropNotEqualXC(IntVar var, int cste) {
        super(new IntVar[]{var}, PropagatorPriority.UNARY, false, true);
        this.constant = cste;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars[0].removeValue(constant, this) || !vars[0].contains(constant)) {
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        if (vars[0].isInstantiatedTo(constant)) {
            return ESat.FALSE;
        } else if (vars[0].contains(constant)) {
            return ESat.UNDEFINED;
        }
        return ESat.TRUE;
    }

    @Override
    public String toString() {
        return vars[0].getName() + " =/= " + constant;
    }

}
