/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

/**
 * Let x be an integer variable with n values and v be a real variable. Given n constant values a1 to an,
 * this constraint ensures that:
 * <p/>
 * <code>x = i iff v = ai</code>
 * <p/>
 * a1... an sequence is supposed to be ordered (a1&lt;a2&lt;... an)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/01/2020
 */
public class PropMixedElement extends Propagator<Variable> {
    RealVar x;
    IntVar y;
    protected double[] values;

    public PropMixedElement(RealVar v0, IntVar v1, double[] values) {
        super(new Variable[]{v0, v1}, PropagatorPriority.BINARY, false);
        x = v0;
        y = v1;
        this.values = values;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            y.updateLowerBound(0, this);
            y.updateUpperBound(values.length - 1, this);
        }
        updateIInf();
        updateISup();
        updateReal();
    }

    public void updateIInf() throws ContradictionException {
        int inf = y.getLB();
        while (values[inf] < x.getLB()) {
            inf++;
        }
        y.updateLowerBound(inf, this);
    }

    public void updateISup() throws ContradictionException {
        int sup = y.getUB();
        while (values[sup] > x.getUB()) {
            sup--;
        }
        y.updateUpperBound(sup, this);
    }

    public void updateReal() throws ContradictionException {
        x.intersect(values[y.getLB()], values[y.getUB()], this);
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int idx = y.getValue();
            if(idx < 0 || idx >= values.length) {
                return ESat.FALSE;
            }
            return ESat.eval(
                (idx == 0 || values[idx-1] < x.getLB())
                && x.getLB() <= values[idx] && values[idx] <= x.getUB()
                && (idx == values.length-1 || x.getUB() < values[idx+1])
            );
        }
        return ESat.UNDEFINED;
    }
}
