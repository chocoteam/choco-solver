/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * X >= Y + C
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/06/12
 */
public final class PropGreaterOrEqualX_YC extends Propagator<IntVar> {

    private final IntVar x;
    private final IntVar y;
    private final int cste;

    @SuppressWarnings({"unchecked"})
    public PropGreaterOrEqualX_YC(IntVar[] vars, int c) {
        super(vars, PropagatorPriority.BINARY, true);
        this.x = vars[0];
        this.y = vars[1];
        this.cste = c;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        if (vIdx == 0) {
            return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.DECUPP);
        } else {
            return IntEventType.combine(IntEventType.INSTANTIATE, IntEventType.INCLOW);
        }
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        x.updateLowerBound(y.getLB() + this.cste, this);
        y.updateUpperBound(x.getUB() - this.cste, this);
        if (x.getLB() >= y.getUB() + this.cste) {
            this.setPassive();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (idxVarInProp == 0) {
            y.updateUpperBound(x.getUB() - this.cste, this);
        } else {
            x.updateLowerBound(y.getLB() + this.cste, this);
        }
        if (x.getLB() >= y.getUB() + this.cste) {
            this.setPassive();
        }
    }


    @Override
    public ESat isEntailed() {
        if (x.getUB() < y.getLB() + cste)
            return ESat.FALSE;
        else if (x.getLB() >= y.getUB() + this.cste)
            return ESat.TRUE;
        else
            return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return x.getName() + " >= " + y.getName() + " + " + cste;
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        boolean newrules = ruleStore.addPropagatorActivationRule(this);
        if (var.equals(x)) {
            newrules |= ruleStore.addLowerBoundRule(y);
        } else if (var.equals(y)) {
            newrules |= ruleStore.addUpperBoundRule(x);
        } else {
            newrules |= super.why(ruleStore, var, evt, value);
        }
        return newrules;
    }

}
