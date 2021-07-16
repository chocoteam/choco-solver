/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

/**
 * A propagator that ensures that a real variable X is equal to an integer variable Y.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/2020
 */
public class PropMixed extends Propagator<Variable> {

    RealVar x;
    IntVar y;

    public PropMixed(RealVar x, IntVar y) {
        super(new Variable[]{x, y}, PropagatorPriority.BINARY, false);
        this.x = x;
        this.y = y;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        y.updateLowerBound((int) Math.ceil(x.getLB()), this);
        y.updateUpperBound((int) Math.floor(x.getUB()), this);
        x.intersect(y.getLB(), y.getUB(), this);
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()) {
            return ESat.eval(y.getLB() <= x.getUB() && x.getLB() <= y.getUB());
        }
        return ESat.UNDEFINED;
    }
}
