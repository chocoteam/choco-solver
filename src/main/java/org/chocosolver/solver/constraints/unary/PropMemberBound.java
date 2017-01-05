/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class PropMemberBound extends Propagator<IntVar> {

    private final int lb, ub;

    public PropMemberBound(IntVar var, int lb, int ub) {
        super(new IntVar[]{var}, PropagatorPriority.UNARY, false);
        this.lb = lb;
        this.ub = ub;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // with views such as abs(...), the prop can be not entailed after initial propagation
        vars[0].updateBounds(lb, ub, this);
        if (lb <= vars[0].getLB() && ub >= vars[0].getUB()) {
            this.setPassive();
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }


    @Override
    public ESat isEntailed() {
        if (vars[0].getLB() >= lb && vars[0].getUB() <= ub) {
            return ESat.TRUE;
        // Check if vars[0] contains no values between lb and ub inclusive.
        } else if (vars[0].nextValue(lb - 1) > ub) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return vars[0].getName() + " in [" + lb + "," + ub + "]";
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return ruleStore.addPropagatorActivationRule(this);
    }
}
