/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/01/2023
 */
public class BooleanConstraint extends Constraint {

    private final boolean b;

    public BooleanConstraint(Model model, boolean bool) {
        super(ConstraintsName.BOOLEAN, new PropBoolean(model, bool));
        this.b = bool;
    }

    @Override
    public Constraint makeOpposite() {
        return b ? propagators[0].getModel().falseConstraint() : propagators[0].getModel().trueConstraint();
    }

    public static class PropBoolean extends Propagator<BoolVar> {
        public final boolean bool;

        public PropBoolean(Model model, boolean bool) {
            super(model.boolVar(bool));
            this.bool = bool;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if (bool) {
                setPassive();
            } else {
                fails();
            }
        }

        @Override
        public int getPropagationConditions(int vIdx) {
            return IntEventType.all();
        }

        @Override
        public String toString() {
            return java.lang.Boolean.toString(bool);
        }

        @Override
        public ESat isEntailed() {
            return ESat.eval(bool);
        }
    }
}
