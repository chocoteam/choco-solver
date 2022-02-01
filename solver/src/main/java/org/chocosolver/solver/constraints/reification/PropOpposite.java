/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
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
 * Constraint representing the negation of a given constraint
 * does not filter but fails if the given constraint is satisfied
 * Can be used within any constraint
 * <p/>
 * Should not be called by the user
 *
 * @author Jean-Guillaume Fages
 * @since 15/05/2013
 */
public class PropOpposite extends Propagator<Variable> {

    // constraint to negate
    Constraint original;

    public PropOpposite(Constraint original, Variable[] vars) {
        super(vars, PropagatorPriority.LINEAR, false);
        this.original = original;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        ESat op = original.isSatisfied();
        if (op == ESat.TRUE) {
            fails();
        }
        if (op == ESat.FALSE) {
            setPassive();
        }
    }

    @Override
    public ESat isEntailed() {
        ESat op = original.isSatisfied();
        if (op == ESat.TRUE) {
            return ESat.FALSE;
        }
        if (op == ESat.FALSE) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

}
