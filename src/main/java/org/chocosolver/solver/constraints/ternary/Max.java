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

import static java.lang.Math.max;

/**
 * X = MAX(Y,Z)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class Max extends Constraint {

    public Max(IntVar X, IntVar Y, IntVar Z) {
        super("Max",new PropMaxBC(X, Y, Z));
    }

    public static IntVar var(IntVar a, IntVar b) {
        if (a.getLB() >= b.getUB()) {
            return a;
        } else if (b.getLB() >= a.getUB()) {
            return b;
        } else {
            Model model = a.getModel();
            IntVar z = new IntervalIntVarImpl(model.generateName("MAX_"),
                    max(a.getLB(), b.getLB()), max(a.getUB(), b.getUB()), model);
            model.max(z, a, b).post();
            return z;
        }
    }
}
