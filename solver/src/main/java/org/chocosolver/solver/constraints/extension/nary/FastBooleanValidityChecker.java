/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.variables.IntVar;

/*
* Created by IntelliJ IDEA.
* User: hcambaza
* Date: Jul 31, 2008
* Since : Choco 2.0.0
*
*/
public final class FastBooleanValidityChecker extends ValidityChecker {

    public FastBooleanValidityChecker(int arity, IntVar[] vars) {
        super(arity, vars);
    }

    // Is tuple valide ?
    public final boolean isValid(final int[] tuple) {
        for (int i = 0; i < arity; i++) {
            if (vars[sortedidx[i]].isInstantiated()) {
                if (vars[sortedidx[i]].getValue() != tuple[sortedidx[i]])
                    return false;
            } else break;
            // variable are sorted by domain size so only non instantiated variables remain
            // and non instantiated variables do not need to be checked in boolean !
        }
        return true;
    }

}
