/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cnf;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24 nov. 2010
 */
public class PropTrue extends Propagator<BoolVar> {

    public PropTrue(BoolVar one) {
        super(new BoolVar[]{one}, PropagatorPriority.UNARY, false);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        setPassive();
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.all();
    }

    @Override
    public String toString() {
        return java.lang.Boolean.TRUE.toString();
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }

    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        // nothing to do
    }

}
