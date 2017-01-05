/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.ternary;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

/**
 * X*Y = Z
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class Times extends Constraint {

    private static boolean inIntBounds(IntVar x, IntVar y) {
        boolean l1 = inIntBounds((long) x.getLB() * (long) y.getLB());
        boolean l2 = inIntBounds((long) x.getUB() * (long) y.getLB());
        boolean l3 = inIntBounds((long) x.getLB() * (long) y.getUB());
        boolean l4 = inIntBounds((long) x.getUB() * (long) y.getUB());
        return l1 && l2 && l3 && l4;
    }

	/**
	 * @param l1 a long
	 * @return Integer.MIN_VALUE < l1 < Integer.MAX_VALUE
	 */
    public static boolean inIntBounds(long l1) {
        return l1 > Integer.MIN_VALUE && l1 < Integer.MAX_VALUE;
    }

    public Times(IntVar v1, IntVar v2, IntVar result) {
        super("Times",new PropTimesNaive(v1,v2,result));
//        if (!inIntBounds(v1, v2)) {
//            throw new SolverException("Integer overflow.\nConsider reducing the variable domains.");
//        }
    }
}
