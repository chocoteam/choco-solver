/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.impl.IntervalIntVarImpl;

import static java.lang.Math.min;

/**
 * X = MIN(Y,Z)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class Min extends Constraint {

    public Min(IntVar X, IntVar Y, IntVar Z) {
        super("Min",new PropMinBC(X, Y, Z));
    }

    public static IntVar var(IntVar a, IntVar b) {
        if (a.getUB() <= b.getLB()) {
            return a;
        } else if (b.getLB() <= a.getUB()) {
            return b;
        } else {
            Model model = a.getModel();
            IntVar z = new IntervalIntVarImpl(model.generateName("MIN_"),
                    min(a.getLB(), b.getLB()), min(a.getUB(), b.getUB()), model);
            model.min(z, a, b).post();
            return z;
        }
    }
}
