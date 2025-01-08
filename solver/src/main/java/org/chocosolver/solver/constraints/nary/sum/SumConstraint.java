/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sum;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.ternary.PropXplusYeqZ;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/06/2016.
 */
public class SumConstraint extends Constraint {
    /**
     * Make a new constraint defined as a set of given propagators
     *
     * @param propagator propagator defining the constraint
     */
    public SumConstraint(Propagator<?> propagator) {
        super(ConstraintsName.SUM, propagator);
    }

    /**
     * The only reason this class exists
     *
     * @return an accurate opposite constraint
     */
    @Override
    protected Constraint makeOpposite() {
        if (propagators[0] instanceof PropSum) {
            PropSum me = (PropSum) propagators[0];
            return new SumConstraint(me.opposite());
        } else if (propagators[0] instanceof PropXplusYeqZ) {
            PropXplusYeqZ me = (PropXplusYeqZ) propagators[0];
            return new SumConstraint(new PropSum(
                    me.getVars(),
                    2,
                    Operator.NQ,
                    0
            ));
        }
        return super.makeOpposite();
    }
}
