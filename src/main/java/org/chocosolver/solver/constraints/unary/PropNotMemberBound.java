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
import org.chocosolver.util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class PropNotMemberBound extends Propagator<IntVar> {

    private final int lb, ub;


    public PropNotMemberBound(IntVar var, int lb, int ub) {
        super(new IntVar[]{var}, PropagatorPriority.UNARY, false);
        this.lb = lb;
        this.ub = ub;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars[0].removeInterval(lb, ub, this)
                || vars[0].getUB() < lb || vars[0].getLB() > ub) {
            this.setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        int UB = this.vars[0].getUB();
        int nb = 0;
        for (int val = this.vars[0].getLB(); val <= UB; val = this.vars[0].nextValue(val)) {
            if (val < lb || val > ub) {
                nb++;
            }
        }
        if (nb == 0) return ESat.FALSE;
        else if (nb == vars[0].getDomainSize()) return ESat.TRUE;
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return vars[0].getName() + " outside [" + lb + "," + ub + "]";
    }

    @Override
    public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
        return ruleStore.addPropagatorActivationRule(this);
    }

}
