/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

/**
 * A specific propagator which posts constraint on condition.
 * <br/>
 * The user gives the condition, then <br/>
 * if the condition returns ESat.TRUE, then posts temporarily the first propagator,<br/>
 * if the condition returns Esat.FALSE, then it posts temporarily the second propagator,<br/>
 * Otherwise wait for the condition to be fully (un)satisfied.
 *
 * @author Charles Prud'homme
 * @since 06/02/2014
 */
public abstract class PropConditionnal extends Propagator<Variable> {

    Constraint[] condTrue;
    Constraint[] condFalse;

    /**
     * @param vars2observe set of variables to observe, their modifications triggers the condition checking
     * @param condTrue     the constraint to post if the condition is satisfied
     * @param condFalse    the constraint to post if the condition is not satisfied
     */
    public PropConditionnal(Variable[] vars2observe, Constraint[] condTrue, Constraint[] condFalse) {
        super(vars2observe, PropagatorPriority.VERY_SLOW, false);
        this.condTrue = condTrue.clone();
        this.condFalse = condFalse.clone();
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ESat condition = checkCondition();
        if (condition == ESat.TRUE) {
            setPassive();
            for (Constraint cstr : condTrue) {
                model.postTemp(cstr);
            }
        } else if (condition == ESat.FALSE) {
            setPassive();
            for (Constraint cstr : condFalse) {
                model.postTemp(cstr);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    /**
     * Check a specific condition.<br/>
     * If the condition returns ESat.TRUE, then posts temporarily the first propagator,<br/>
     * If the condition returns Esat.FALSE, then it posts temporarily the second propagator,<br/>
     * Otherwise wait for the condition to be fully (un)satisfied.
     *
     * @return Esat
     */
    public abstract ESat checkCondition();
}
